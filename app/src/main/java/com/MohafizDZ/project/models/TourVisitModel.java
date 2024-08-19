package com.MohafizDZ.project.models;

import android.content.Context;

import androidx.annotation.NonNull;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;

import java.util.List;

public class TourVisitModel extends Model {
    private static final String TAG = TourVisitModel.class.getSimpleName();
    public static final String STATE_DRAFT = "draft";
    public static final String STATE_PROGRESS = "progress";
    public static final String STATE_VISITED = "visited";
    public Col tour_id = new Col(Col.ColumnType.many2one).setRelationalModel(TourModel.class);
    public Col customer_id = new Col(Col.ColumnType.many2one).setRelationalModel(CompanyCustomerModel.class);
    public Col state = new Col(Col.ColumnType.varchar).setDefaultValue(STATE_DRAFT);
    public Col planned = new Col(Col.ColumnType.bool).setDefaultValue(0);
    public Col visit_count = new Col(Col.ColumnType.integer).setDefaultValue(0);
    public Col visit_duration = new Col(Col.ColumnType.real).setDefaultValue(0);
    public Col initial_balance = new Col(Col.ColumnType.real).setDefaultValue(0);
    public Col visited_date = new Col(Col.ColumnType.varchar);
    public TourVisitModel(Context mContext) {
        super(mContext, "tour_visit");
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @NonNull
    public DataRow getCurrentVisit(String tourId, String customerId) {
        DataRow row = browse(" tour_id = ? and customer_id = ? ", new String[]{tourId, customerId});
        if(row == null){
            Values values = new Values();
            values.put("tour_id", tourId);
            values.put("customer_id", customerId);
            DataRow customerRow = new CompanyCustomerModel(mContext).browse(customerId);
            values.put("initial_balance", customerRow.get("balance"));
            values.put("state", STATE_DRAFT);
            values.put("planned", 0);
            values.put("visit_count", 0);
            values.put("visit_duration", 0);
            final int rowId = insert(values);
            return browse(rowId);
        }else {
            return row;
        }
    }

    public boolean startVisit(DataRow visitRow, DataRow customerRow, double latitude, double longitude) {
        if(visitRow.getString("state").equals(STATE_DRAFT)){
            Values values = new Values();
            values.put("state", STATE_PROGRESS);
            values.put("visit_count", visitRow.getInteger("visit_count") +1);
            update(visitRow.getString(Col.SERVER_ID), values);
            new TourVisitActionModel(mContext).startVisit(visitRow, latitude, longitude);
            return true;
        }
        return false;
    }

    public boolean restartVisit(DataRow visitRow, DataRow customerRow, double latitude, double longitude) {
        if(visitRow.getString("state").equals(STATE_VISITED)){
            Values values = new Values();
            values.put("state", STATE_PROGRESS);
            values.put("visit_count", visitRow.getInteger("visit_count") +1);
            update(visitRow.getString(Col.SERVER_ID), values);
            new TourVisitActionModel(mContext).restartVisit(visitRow, latitude, longitude);
            return true;
        }
        return false;
    }

    public boolean stopVisit(DataRow visitRow, DataRow customerRow, double latitude, double longitude) {
        if(visitRow.getString("state").equals(STATE_PROGRESS)){
            Values values = new Values();
            values.put("state", STATE_VISITED);
            new TourVisitActionModel(mContext).stopVisit(visitRow, latitude, longitude);
            float duration = calculateDuration(visitRow);
            values.put("visit_duration", duration);
            values.put("visited_date", MyUtil.getCurrentDate());
            update(visitRow.getString(Col.SERVER_ID), values);
            return true;
        }
        return false;
    }

    private long calculateDuration(DataRow visitRow){
        String selection = " visit_id = ? and action in (?, ?, ?) ";
        String[] args = {visitRow.getString(Col.SERVER_ID), TourVisitActionModel.ACTION_VISIT_START, TourVisitActionModel.ACTION_VISIT_STOP, TourVisitActionModel.ACTION_VISIT_RESTART};
        List<DataRow> visitActions = new TourVisitActionModel(mContext).select(null, selection, args, " action_date asc ");
        long duration = 0;
        for(DataRow row : visitActions){
            float timeInMillis = MyUtil.getTimeInMillis(row.getString("action_date"));
            duration += row.getString("action").equals(TourVisitActionModel.ACTION_VISIT_STOP)? timeInMillis : -timeInMillis;
        }
        return duration;
    }
}
