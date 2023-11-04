/*
 * Copyright (c) 2022-2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.service;

import com.jyuzawa.miles_per_awa.entity.CalculatedPosition;
import com.jyuzawa.miles_per_awa.entity.CalculatedPosition.CalculatedPositionBuilder;
import com.jyuzawa.miles_per_awa.entity.Datapoint;
import com.jyuzawa.miles_per_awa.entity.LatLng;
import com.jyuzawa.miles_per_awa.entity.RoutePoint;
import com.jyuzawa.miles_per_awa.entity.RouteTuple;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class VelocityService {
    private static final long LOOKBACK_SECONDS = 900;
    // from 4mph avg walking speed
    private static final double MIN_VELOCITY = 1.78816;

    private final VelocityRepository repository;

    public Map<String, CalculatedPosition> getUsers(Collection<String> userIds) {
        Iterable<CalculatedPosition> velocities =
                userIds == null ? repository.findAll() : repository.findAllById(userIds);
        Map<String, CalculatedPosition> out = new HashMap<>();
        for (CalculatedPosition velocity : velocities) {
            out.put(velocity.getId(), velocity);
        }
        return out;
    }

    public CalculatedPosition calculate(Datapoint datapoint, Optional<RoutePoint> routePoint) {
        Optional<CalculatedPosition> old = repository.findById(datapoint.getUser());
        CalculatedPosition out = process(datapoint, routePoint, old);
        repository.save(out);
        return out;
    }

    private static CalculatedPosition process(
            Datapoint datapoint, Optional<RoutePoint> routePoint, Optional<CalculatedPosition> prior) {
        CalculatedPositionBuilder builder;
        List<RouteTuple> oldHistory;
        if (prior.isPresent()) {
            CalculatedPosition old = prior.get();
            builder = old.toBuilder();
            oldHistory = old.getHistory();
        } else {
            builder = CalculatedPosition.builder()
                    .id(datapoint.getUser())
                    .history(List.of())
                    .timestamp(Instant.EPOCH)
                    .index(-1);
            oldHistory = List.of();
        }
        Instant timestamp = datapoint.getTimestamp();
        LatLng coords = datapoint.getCoords();
        builder.positionTimestamp(timestamp).latitude(coords.latitude()).longitude(coords.longitude());
        if (routePoint.isEmpty()) {
            return builder.build();
        }
        long timestampSeconds = timestamp.getEpochSecond();
        RoutePoint theRoutePoint = routePoint.get();
        int index = theRoutePoint.index();
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
        return builder.velocity(v).build();
    }
}
