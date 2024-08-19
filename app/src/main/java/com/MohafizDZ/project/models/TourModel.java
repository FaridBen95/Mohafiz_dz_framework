package com.MohafizDZ.project.models;

import android.content.Context;

import androidx.annotation.NonNull;

import com.MohafizDZ.framework_repository.Utils.MyUtil;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.Values;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TourModel extends Model {
    private static final String TAG = TourModel.class.getSimpleName();
    public static final String STATE_DRAFT = "draft";
    public static final String STATE_CONFIRMED = "confirmed";
    public static final String STATE_PROGRESS = "progress";
    public static final String STATE_PRE_CLOSING = "pre_closing";
    public static final String STATE_CLOSING = "closing";
    public static final String STATE_CLOSED = "closed";

    public Col name = new Col(Col.ColumnType.varchar);
    //todo remove this line
    public Col user_id = new Col(Col.ColumnType.many2one).setRelationalModel(UserModel.class).setDefaultValue("NO_DEVICE.668fec6d190a2338cf4.-6446110");
    public Col creator_id = new Col(Col.ColumnType.many2one).setRelationalModel(UserModel.class).setDefaultValue("NO_DEVICE.668fec6d190a2338cf4.-6446110");
    public Col vehicle_name = new Col(Col.ColumnType.varchar);
    public Col region_id = new Col(Col.ColumnType.many2one).setRelationalModel(RegionModel.class);
    public Col distributor_id = new Col(Col.ColumnType.many2one).setRelationalModel(DistributorModel.class);
    public Col plan_date = new Col(Col.ColumnType.varchar);
    public Col start_date = new Col(Col.ColumnType.varchar);
    public Col end_date = new Col(Col.ColumnType.varchar);
    public Col pre_closing_date = new Col(Col.ColumnType.varchar);
    public Col closing_date = new Col(Col.ColumnType.varchar);
    public Col state = new Col(Col.ColumnType.varchar).setDefaultValue(STATE_DRAFT);
    public Col configurations = new Col(Col.ColumnType.array).setRelationalModel(TourConfigurationModel.class);
    public Col current_customer_id = new Col(Col.ColumnType.many2one).setLocalColumn().setRelationalModel(CompanyCustomerModel.class);
    public Col control_expenses_amount = new Col(Col.ColumnType.bool).setDefaultValue(0);
    public Col expenses_limit = new Col(Col.ColumnType.real).setDefaultValue(0);
    //todo implement use cash box config
    public Col use_cash_box = new Col(Col.ColumnType.bool).setDefaultValue(1);
    public Col expenses_validated = new Col(Col.ColumnType.bool).setDefaultValue(0);
    public Col cash_box_validated = new Col(Col.ColumnType.bool).setDefaultValue(0);
    public Col sales_validated = new Col(Col.ColumnType.bool).setDefaultValue(0);
    public Col inventory_validated = new Col(Col.ColumnType.bool).setDefaultValue(0);
    public Col goal_text = new Col(Col.ColumnType.varchar).setDefaultValue("");
    public Col visits_goal_count = new Col(Col.ColumnType.integer).setDefaultValue("0");
    public Col initial_stock_validated = new Col(Col.ColumnType.bool).setDefaultValue(0);
    private final List<String> stateListPriorityOrder = new ArrayList<>();

    public TourModel(Context mContext) {
        super(mContext, "tour");
        stateListPriorityOrder.add(STATE_PROGRESS);
        stateListPriorityOrder.add(STATE_CONFIRMED);
        stateListPriorityOrder.add(STATE_DRAFT);
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    public DataRow getCurrentTour(DataRow distributorRow) {
        return getCurrentTour(distributorRow, false);
    }

    public DataRow getCurrentTour(DataRow distributorRow, boolean allowCreate) {
        List<DataRow> toursList = getSortedTours(distributorRow);
        if(toursList.size() > 0){
            return toursList.get(0);
        }else if(distributorRow == null) {
            return null;
        }else if(allowCreate){
            return initDraftTour(distributorRow);
        }else{
            return null;
        }
    }

    public List<DataRow> getSortedTours(DataRow distributorRow){
        if(distributorRow == null){
            return new ArrayList<>();
        }
        String selection = " distributor_id = ? and state <> ? ";
        String[] args = {distributorRow.getString(Col.SERVER_ID), STATE_CLOSED};
        List<DataRow> tours = select(selection, args);
        tours.sort((row1, row2) -> {
            String state1 = row1.getString("state");
            String state2 = row2.getString("state");
            int statePos1 = stateListPriorityOrder.indexOf(state1);
            int statePos2 = stateListPriorityOrder.indexOf(state2);
            //todo order by write_date
            return Integer.compare(statePos1, statePos2);
        });
        return tours;
    }

    public DataRow initDraftTour(@NonNull DataRow distributorRow){
        Values values = new Values();
        Date date = new Date();
        String planDateTime = MyUtil.getStringFromDate(date, MyUtil.DEFAULT_DATE_TIME_FORMAT);
        values.put("plan_date", planDateTime);
        values.put("state", STATE_DRAFT);
        String name = checkAndGenerateName(distributorRow, "", null);
        values.put("name", name);
        values.put("distributor_id", distributorRow.getString(Col.SERVER_ID));
        values.put("creator_id", distributorRow.getString("user_id"));
        values.put("user_id", distributorRow.getString("user_id"));
        int id = insert(values);
        return browse(id);
    }

    public String checkAndGenerateName(DataRow distributorRow, String name, Integer index){
        if(name.equals("")){
            Date date = new Date();
            String planDate = MyUtil.getStringFromDate(date, MyUtil.DEFAULT_DATE_FORMAT);
            name = generateName(distributorRow, planDate);
        }
        if(index == null){
            index = 2;
        }
        if(browse(" name = ? ", new String[]{name}) != null){
            if(name.contains("(" + (index -1) + ")")){
                name = name.replace("(" + (index -1) + ")", "");
            }
            name += "(" + index + ")";
            return checkAndGenerateName(distributorRow, name, index + 1);
        }
        return name;
    }

    private String generateName(DataRow distributorRow, String planDate){
        String code = distributorRow.getString("code");
        return code.concat(" ").concat(planDate);
    }

    public void startTour(DataRow tourRow) {
        Values values = new Values();
        values.put("start_date", MyUtil.getStringFromDate(new Date(), MyUtil.DEFAULT_DATE_TIME_FORMAT));
        values.put("state", STATE_PROGRESS);
        update(tourRow.getString(Col.SERVER_ID), values);
    }

    public void endTour(DataRow tourRow) {
        Values values = new Values();
        values.put("pre_closing_date", MyUtil.getStringFromDate(new Date(), MyUtil.DEFAULT_DATE_TIME_FORMAT));
        values.put("state", STATE_PRE_CLOSING);
        update(tourRow.getString(Col.SERVER_ID), values);
    }

    public void reopenTour(DataRow tourRow) {
        Values values = new Values();
        values.put("pre_closing_date", "false");
        values.put("state", STATE_PROGRESS);
        update(tourRow.getString(Col.SERVER_ID), values);
    }

    public void closeTour(DataRow tourRow) {
        Values values = new Values();
        values.put("state", TourModel.STATE_CLOSING);
        values.put("closing_date", MyUtil.getCurrentDate());
        update(tourRow.getString(Col.SERVER_ID), values);
    }

    public void validateClosingTour(DataRow tourRow) {
        Values values = new Values();
        values.put("state", TourModel.STATE_CLOSED);
        values.put("end_date", MyUtil.getCurrentDate());
        update(tourRow.getString(Col.SERVER_ID), values);
    }
}
