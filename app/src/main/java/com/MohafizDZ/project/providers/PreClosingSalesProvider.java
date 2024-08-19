package com.MohafizDZ.project.providers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.MyBaseProvider;
import com.MohafizDZ.project.models.VisitOrderLineModel;

public class PreClosingSalesProvider extends MyBaseProvider {
    private static final String TAG = PreClosingSalesProvider.class.getSimpleName();
    private static final String TOUR_ID_KEY = "tour_id_key";

    @Override
    public String setAuthority() {
        return VisitOrderLineModel.PRE_CLOSING_SALES_AUTHORITY;
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] baseProjection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Model model = getModel(uri);
        final SQLiteDatabase db = model.getReadableDatabase();
        String tourId = uri.getQueryParameter(TOUR_ID_KEY);
        return db.rawQuery("select\n" +
                " (ifnull((select SUM(qty) from visit_order_line vol left join visit_order vo on vo.state not in  ('cancel') and vol.order_id = vo.id and vol.product_id = prod.id where vo.tour_id = '" + tourId + "' and vol.product_id = prod.id), 0)) as sum_qty\n" +
                ", prod.*\n" +
                "from company_product prod", selectionArgs);
    }

    public static Uri generateUri(Uri uri,@NonNull String tourId){
        return uri.buildUpon().appendQueryParameter(TOUR_ID_KEY, tourId).build();
    }
}
