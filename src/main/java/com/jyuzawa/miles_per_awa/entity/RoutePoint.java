/*
 * Copyright (c) 2022 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.entity;

public record RoutePoint(LatLng coords, int index, double heading) {

    public boolean headingMatches(double heading) {
        double a = 180 - Math.abs(Math.abs(this.heading - heading) - 180);
        return a < 10;
    }
}
