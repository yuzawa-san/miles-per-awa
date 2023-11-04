/*
 * Copyright (c) 2022-2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.service;

import com.jyuzawa.miles_per_awa.entity.CalculatedPosition;
import com.jyuzawa.miles_per_awa.entity.CalculatedPosition.CalculatedPositionBuilder;
import com.jyuzawa.miles_per_awa.entity.Datapoint;
import com.jyuzawa.miles_per_awa.entity.RoutePoint;
import com.jyuzawa.miles_per_awa.entity.RouteTuple;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class VelocityService {
    private static final long LOOKBACK_SECONDS = 900;
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

    public CalculatedPosition calculate(Datapoint datapoint, Optional<RoutePoint> routePoint) {
        return users.compute(datapoint.getUser(), (u, old) -> process(datapoint, routePoint, old));
    }

    private static CalculatedPosition process(
            Datapoint datapoint, Optional<RoutePoint> routePoint, CalculatedPosition old) {
        if (old == null) {
            old = new CalculatedPosition(datapoint, List.of(), Instant.EPOCH, -1, OptionalDouble.empty());
        }
        CalculatedPositionBuilder builder = old.toBuilder().position(datapoint);
        if (routePoint.isEmpty()) {
            return builder.build();
        }
        long timestampSeconds = datapoint.getTimestamp().getEpochSecond();
        RoutePoint theRoutePoint = routePoint.get();
        int index = theRoutePoint.index();
        List<RouteTuple> oldHistory = old.history();
        List<RouteTuple> history = new ArrayList<>(oldHistory.size() + 1);
        for (RouteTuple item : oldHistory) {
            long indexDelta = timestampSeconds - item.timestampSeconds();
            if (indexDelta >= 0 && indexDelta < LOOKBACK_SECONDS) {
                history.add(item);
            }
        }
        history.add(new RouteTuple(timestampSeconds, index));
        builder.timestamp(datapoint.getTimestamp()).index(index).history(history);
        if (datapoint.getVelocity() < MIN_VELOCITY || history.size() < 2) {
            return builder.build();
        }
        RouteTuple first = history.get(0);
        int deltaD = index - first.index();
        long deltaT = timestampSeconds - first.timestampSeconds();
        if (deltaD == 0 || deltaT == 0) {
            return builder.build();
        }
        double v = deltaD * RouteService.INTERVAL_METERS / (double) deltaT;
        return builder.velocity(OptionalDouble.of(v)).build();
    }
}
