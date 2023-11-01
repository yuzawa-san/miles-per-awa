/*
 * Copyright (c) 2022-2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.service;

import com.jyuzawa.miles_per_awa.entity.CalculatedPosition;
import com.jyuzawa.miles_per_awa.entity.Datapoint;
import com.jyuzawa.miles_per_awa.entity.RoutePoint;
import com.jyuzawa.miles_per_awa.entity.Velocity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Component
public final class VelocityService {
    // empirically determined for biking and running
    private static final long TIMEBUCKET_SECONDS = 30;
    private static final double ALPHA = 0.1;
    // from 4mph avg walking speed
    private static final double MIN_VELOCITY = 1.78816;

    private final Map<String, CalculatedPosition> users;

    @Autowired
    public VelocityService() {
        // TODO: better storage
        this.users = new ConcurrentHashMap<>();
    }

    public Map<String, CalculatedPosition> getUsers(Collection<String> userIds) {
        if (userIds == null) {
            return Collections.unmodifiableMap(users);
        }
        Map<String, CalculatedPosition> out = new HashMap<>(userIds.size());
        for (String userId : userIds) {
            CalculatedPosition velocity = users.get(userId);
            if (velocity != null) {
                out.put(userId, velocity);
            }
        }
        return out;
    }

    public CalculatedPosition calculate(String user, Datapoint datapoint, Optional<RoutePoint> routePoint) {
        return users.compute(
                user,
                (u, old) -> new CalculatedPosition(
                        datapoint,
                        update(
                                u,
                                datapoint,
                                routePoint,
                                Optional.ofNullable(old).flatMap(CalculatedPosition::velocity))));
    }

    private static Optional<Velocity> update(
            String uw, Datapoint datapoint, Optional<RoutePoint> routePoint, Optional<Velocity> old) {
        if (routePoint.isEmpty()) {
            return old;
        }
        if (datapoint.getVelocity() < MIN_VELOCITY) {
            if (old.isEmpty()) {
                return old;
            }
            return Optional.of(new Velocity(
                    datapoint.getTimestamp(),
                    routePoint.get().index(),
                    old.get().history(),
                    old.get().velocity()));
        }
        // TODO: presize
        List<Tuple2<Datapoint, RoutePoint>> history = new ArrayList<>();
        if (old.isPresent()) {
            history.addAll(old.get().history());
        }
        history.add(Tuples.of(datapoint, routePoint.get()));
        List<Tuple2<Datapoint, RoutePoint>> filtered = new ArrayList<>();
        for (Tuple2<Datapoint, RoutePoint> item : history) {
            long indexDelta = datapoint.getTimestamp().getEpochSecond()
                    - item.getT1().getTimestamp().getEpochSecond();
            if (indexDelta >= 0 && indexDelta < 900) {
                filtered.add(item);
            }
        }
        if (filtered.size() < 2) {
            return Optional.of(
                    new Velocity(datapoint.getTimestamp(), routePoint.get().index(), history, 0));
        }
        Tuple2<Datapoint, RoutePoint> first = filtered.get(0);
        long deltaT = (datapoint.getTimestamp().getEpochSecond()
                - first.getT1().getTimestamp().getEpochSecond());
        if (deltaT == 0) {
            return Optional.of(
                    new Velocity(datapoint.getTimestamp(), routePoint.get().index(), history, 0));
        }
        double v = (routePoint.get().index() - first.getT2().index()) * 30.0 / deltaT;
        return Optional.of(
                new Velocity(datapoint.getTimestamp(), routePoint.get().index(), history, v));
    }
}
