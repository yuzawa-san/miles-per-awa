/*
 * Copyright (c) 2022 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.controller;

public record LatLng(double latitude, double longitude) {

    static double EARTH_RADIUS = 6371000;

    static double DEG_TO_RAD = 0.0174532925199;
    static double RAD_TO_DEG = 57.295779513082320876;

    // https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude
    static double distance(LatLng src, LatLng dst) {
        double lat1 = src.latitude;
        double lon1 = src.longitude;
        double lat2 = dst.latitude;
        double lon2 = dst.longitude;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2)
                        * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c; // convert to meters

        return distance;
        //    	    double height = el1 - el2;
        //
        //    	    distance = Math.pow(distance, 2) + Math.pow(height, 2);

        // return Math.sqrt(distance);
    }

    public double distance(LatLng dst) {
        return distance(this, dst);
    }

    static double heading(LatLng src, LatLng dst) {
        double lat1 = Math.toRadians(src.latitude);
        double lon1 = Math.toRadians(src.longitude);
        double lat2 = Math.toRadians(dst.latitude);
        double lon2 = Math.toRadians(dst.longitude);

        double dlon = lon2 - lon1;

        double x = Math.cos(lat2) * Math.sin(dlon);
        double y = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dlon);
        double brng = Math.atan2(x, y);
        brng = Math.toDegrees(brng);
        if (brng < 0) {
            brng += 360d;
        }
        return brng;
    }

    public double heading(LatLng dst) {
        return heading(this, dst);
    }
}
