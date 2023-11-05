/*
 * Copyright (c) 2022-2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.service;

import com.jyuzawa.miles_per_awa.entity.Datapoint;
import com.jyuzawa.miles_per_awa.entity.LatLng;
import com.jyuzawa.miles_per_awa.entity.RoutePoint;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class RouteService {
    public static final int INTERVAL_METERS = 30;
    private static final double INTERVAL_METERS_HALF = INTERVAL_METERS / 2d;

    private final List<RoutePoint> normalPath;

    @Getter
    private final List<BigDecimal> rawPath;

    @Autowired
    public RouteService(RoutePointsService routePointsService) {
        this(routePointsService.getPoints());
    }

    RouteService(List<LatLng> points) {
        double dist = 0;
        double[] deltas = new double[points.size()];
        List<BigDecimal> rawPath = new ArrayList<>(points.size() * 2);
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
            rawPath.add(new BigDecimal(loc.latitude()).setScale(5, RoundingMode.DOWN));
            rawPath.add(new BigDecimal(loc.longitude()).setScale(5, RoundingMode.DOWN));
            i++;
            offset = i * INTERVAL_METERS;
        }
        this.normalPath = Collections.unmodifiableList(normalPath);
        this.rawPath = Collections.unmodifiableList(rawPath);
    }

    public Optional<RoutePoint> getClosest(Datapoint datapoint) {
        LatLng coords = datapoint.getCoords();
        // this is to make sure when a person is still, the index is updated
        boolean minVelocity = datapoint.getVelocity() < VelocityService.MIN_VELOCITY;
        Double heading = datapoint.getHeading();
        List<RoutePoint> candidates = normalPath.stream()
                .map(routePoint -> new Candidate(routePoint, coords.distance(routePoint.coords())))
                .filter(candidate -> candidate.distance() < INTERVAL_METERS_HALF
                        && (minVelocity || candidate.routePoint().headingMatches(heading)))
                .sorted(Comparator.comparing(Candidate::distance))
                .map(Candidate::routePoint)
                .toList();
        if (candidates.size() == 1) {
            // there is a single candidate
            return Optional.of(candidates.get(0));
        }
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
