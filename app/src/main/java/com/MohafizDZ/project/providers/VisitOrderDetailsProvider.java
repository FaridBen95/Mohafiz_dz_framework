package com.MohafizDZ.project.providers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.MyBaseProvider;
import com.MohafizDZ.project.models.CompanyProductModel;
import com.MohafizDZ.project.models.VisitOrderModel;

public class VisitOrderDetailsProvider extends MyBaseProvider {

    @Override
    public String setAuthority() {
        return VisitOrderModel.ORDER_DETAILS_AUTHORITY;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] baseProjection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Model model = getModel(uri);
        SQLiteDatabase db = model.getReadableDatabase();
        return db.rawQuery("select vo.*, cc.name as customer_name, cc.region_id as customer_region_id " +
                        "from visit_order vo " +
                        "left join company_customer cc on cc.id = vo.customer_id " +
                        "left join visit_order_products_rel_table vopr on vopr.base_col_id = vo.id " +
                        (selection != null && selection.length() > 0? "where " + selection : "") +
                        " group by vopr.base_col_id " +
                        (sortOrder != null && sortOrder.length() > 0? "order by " + sortOrder : "")
                , selectionArgs);
    }
}
