package com.MohafizDZ.framework_repository.Utils;

import androidx.core.util.Pair;

import com.google.android.gms.maps.model.LatLng;


public class GeoUtil {
    public static Float OneDegInKm = 89.97825789641939f;

    public static  Float getDeg(Float km){
        return km / OneDegInKm;
    }

    public static Pair<LatLng, LatLng> getProximityLocations(Float proximity, double currentLatitude, double currentLongitude) {
        Float R = GeoUtil.getDeg((float)proximity);

        Float RR = 4*R*R;

        Double X1 = (currentLatitude-R);
        Double X2 = (currentLatitude+R);
        Double Y1 = (currentLongitude-R);
        Double Y2 = (currentLongitude+R);
        return new Pair<>(new LatLng(X1, Y1), new LatLng(X2, Y2));
    }
}
