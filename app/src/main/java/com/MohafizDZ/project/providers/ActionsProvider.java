package com.MohafizDZ.project.providers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.MyBaseProvider;
import com.MohafizDZ.project.models.TourVisitActionModel;

public class ActionsProvider extends MyBaseProvider {

    @Override
    public String setAuthority() {
        return TourVisitActionModel.ACTIONS_DETAILS_AUTHORITY;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] baseProjection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Model model = getModel(uri);
        SQLiteDatabase db = model.getReadableDatabase();
        return db.rawQuery("select tva.*, cc.name as customer_name, cc.region_id as customer_region_id " +
                        "from tour_visit_action tva " +
                        "left join company_customer cc on cc.id = tva.customer_id " +
                        (selection != null && selection.length() > 0? "where " + selection : "") +
                        " group by tva.id " +
                        (sortOrder != null && sortOrder.length() > 0? "order by " + sortOrder : "")
                , selectionArgs);
    }
}
