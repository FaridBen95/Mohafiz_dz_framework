package com.MohafizDZ.project.providers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.MohafizDZ.framework_repository.core.Model;
import com.MohafizDZ.framework_repository.core.MyBaseProvider;
import com.MohafizDZ.project.models.CompanyCustomerModel;

public class CustomersProvider extends MyBaseProvider {
    private static final String LATITUDE_KEY = "latitude_key";
    private static final String LONGITUDE_KEY = "longitude_key";
    private static final String TOUR_ID_KEY = "tour_id_key";

    @Override
    public String setAuthority() {
        return CompanyCustomerModel.CUSTOMERS_LIST_AUTHORITY;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] baseProjection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Model model = getModel(uri);
        SQLiteDatabase db = model.getReadableDatabase();
        String latitude = uri.getQueryParameter(LATITUDE_KEY);
        String longitude = uri.getQueryParameter(LONGITUDE_KEY);
        String tourId = uri.getQueryParameter(TOUR_ID_KEY);
        return db.rawQuery("SELECT cc.*, tv.visited_date, tv.state as visit_state from \n" +
                        "( SELECT (latitude - " + latitude + ") * (latitude - "+ latitude + ") + (longitude - " + longitude + ") * (longitude - "+ longitude+ ") AS squared_distance, * from company_customer) cc " +
                        " LEFT JOIN tour_visit tv on tv.customer_id = cc.id and tv.tour_id = '" + tourId + "' " +
                        (selection != null && selection.length() > 0? " where " + selection : "") +
                        " group by cc.id " +
                        (sortOrder != null && sortOrder.length() > 0? " order by " + sortOrder : ""),
                selectionArgs);
    }

    public static Uri getCustomersListUri(Uri uri, String tourId, String latitude, String longitude){
        return uri.buildUpon()
                .appendQueryParameter(LATITUDE_KEY, latitude + "")
                .appendQueryParameter(TOUR_ID_KEY, tourId)
                .appendQueryParameter(LONGITUDE_KEY, longitude + "").build();
    }
}
