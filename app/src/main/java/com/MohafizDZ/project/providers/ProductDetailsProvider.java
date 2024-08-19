package com.MohafizDZ.project.providers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.MyBaseProvider;
import com.MohafizDZ.project.models.CompanyProductModel;
import com.MohafizDZ.project.models.DistributorStockModel;

public class ProductDetailsProvider extends MyBaseProvider {
    private static final String TOUR_ID_KEY = "tour_id";
    private static final String IS_INVENTORY_KEY = "is_inventory_key";

    @Override
    public String setAuthority() {
        return CompanyProductModel.PRODUCT_DETAILS_AUTHORITY;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] baseProjection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Model model = getModel(uri);
        SQLiteDatabase db = model.getReadableDatabase();
        String tourId = uri.getQueryParameter(TOUR_ID_KEY);
        return db.rawQuery("SELECT init_stock_qty - sum_order_qty as stock_qty, * from" +
                        "(select \n" +
                        "(ifnull((select sum(qty) from visit_order_line vol left join visit_order vo on vo.id = vol.order_id where product_id = prod.id and vo.tour_id = '" + tourId + "'), 0)) as sum_order_qty,\n" +
                        "(ifnull((select dsl.qty from distributor_stock_line dsl left join distributor_stock ds on ds.id = dsl.order_id where dsl.product_id = prod.id and ds.tour_id = '" + tourId +"'), 0)) as init_stock_qty,\n" +
                        "(ifnull((select dil.qty from distributor_inventory_line dil left join distributor_inventory di on di.id = dil.inventory_id where dil.product_id = prod.id and di.tour_id = '" + tourId +"'), 0)) as inventory_qty,\n" +
                        "prod.*, category.name as category_name\n" +
                        "from company_product prod \n" +
                        "left join company_product_category category on category.id = prod.category_id) \n"+
                        (selection != null && !selection.equals("")? " where " + selection : "")+
                        " group by id "+
                        (sortOrder != null? " order by " + sortOrder : "")
                , selectionArgs);
    }

    public static Uri getUri(Uri uri, String tourId){
        return uri.buildUpon().appendQueryParameter(TOUR_ID_KEY, tourId).build();
    }
}
