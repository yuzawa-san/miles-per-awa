/*
 * Copyright (c) 2022 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.entity;

public record LatLng(double latitude, double longitude) {

    static double EARTH_RADIUS = 6371000;

    // https://github.com/Leaflet/Leaflet/blob/3b62c7ec96242ee4040cf438a8101a48f8da316d/src/geo/crs/CRS.Earth.js#L24C3-L31C20
    static double distance(LatLng src, LatLng dst) {
        double lat1 = Math.toRadians(src.latitude);
    		    double lat2 = Math.toRadians(dst.latitude);
    		    double sinDLat = Math.sin(Math.toRadians(dst.latitude - src.latitude) / 2);
    		    double sinDLon = Math.sin(Math.toRadians(dst.longitude - src.longitude) / 2);
    		    double a = sinDLat * sinDLat + Math.cos(lat1) * Math.cos(lat2) * sinDLon * sinDLon;
    		    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    		return EARTH_RADIUS * c;
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
