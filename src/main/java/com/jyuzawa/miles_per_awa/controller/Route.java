/*
 * Copyright (c) 2022 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


public final class Route {
    private static final double NORMAL_PATH_METERS = 25d;
    private final List<RoutePoint> normalPath;

    public Route(List<LatLng> points) {
        double dist = 0;
        double[] deltas = new double[points.size()];
        deltas[0] = 0;
        for (int i = 1; i < points.size(); i++) {
            LatLng prev = points.get(i - 1);
            LatLng curr = points.get(i);
            dist += curr.distance(prev);
            deltas[i] = dist;
        }
        List<RoutePoint> normalPath = new ArrayList<>();
        normalPath.add(new RoutePoint(points.get(0), 0, 0d));
        int i = 1;
        double offset = 0d;
        while (offset < dist) {
            LatLng loc = latLonForDistance(deltas, points, offset);
            RoutePoint prev = normalPath.get(i - 1);
            double heading = prev.coords().heading(loc);
            normalPath.add(new RoutePoint(loc, i, heading));
            i++;
            offset = i * NORMAL_PATH_METERS;
        }
        this.normalPath = Collections.unmodifiableList(normalPath);
    }

    public Optional<RoutePoint> getClosest(LatLng coords, Double heading) {
        List<RoutePoint> candidates = normalPath.stream()
                .map(routePoint -> new Candidate(routePoint, coords.distance(routePoint.coords())))
                .filter(candidate -> candidate.distance() < NORMAL_PATH_METERS / 2)
                .sorted(Comparator.comparing(Candidate::distance))
                .map(Candidate::routePoint)
                .toList();
        if (candidates.size() == 1) {
            // there is a single candidate
            return Optional.of(candidates.get(0));
        }
        if (heading != null && candidates.size() > 1) {
            // there are multiple candidates which we can narrow down using the heading
            // pick the nearest point with a matching heading
            return candidates.stream()
                    .filter(candidate -> candidate.headingMatches(heading))
                    .findFirst();
        }
        // there are no candidates nearby
        return Optional.empty();
    }

    private record Candidate(RoutePoint routePoint, double distance) {}

    private static final int bsearch(double[] A, double key) {
        int m;
        int l = 0;
        int r = A.length;
        while (r - l > 1) {
            m = l + (r - l) / 2;
            if (A[m] <= key) {
                l = m;
            } else {
                r = m;
            }
        }
        return l;
    }

    private static final LatLng latLonForDistance(double[] deltas, List<LatLng> points, double distance) {
        int i1 = bsearch(deltas, distance);
        int i2 = i1 + 1;
        double d1 = deltas[i1];
        double d2 = deltas[i2];
        LatLng p1 = points.get(i1);
        LatLng p2 = points.get(i2);
        double pct = (distance - d1) / (d2 - d1);
        return new LatLng(
                p1.latitude() + (p2.latitude() - p1.latitude()) * pct,
                p1.longitude() + (p2.longitude() - p1.longitude()) * pct);
    }
}
