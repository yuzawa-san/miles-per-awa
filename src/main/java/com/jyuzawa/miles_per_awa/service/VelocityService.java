/*
 * Copyright (c) 2022-2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.service;

import com.jyuzawa.miles_per_awa.entity.CalculatedPosition;
import com.jyuzawa.miles_per_awa.entity.Datapoint;
import com.jyuzawa.miles_per_awa.entity.RoutePoint;
import com.jyuzawa.miles_per_awa.entity.Velocity;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public final class VelocityService {
    // empirically determined for biking and running
    private static final long TIMEBUCKET_SECONDS = 30;
    private static final double ALPHA = 0.1;
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

    public CalculatedPosition calculate(String user, Datapoint datapoint, Optional<RoutePoint> routePoint) {
        Optional<CalculatedPosition> old = repository.findById(user);
        CalculatedPosition out = new CalculatedPosition(
                user, datapoint, update(user, datapoint, routePoint, old.flatMap(CalculatedPosition::getVelocity)));
        repository.save(out);
        return out;
    }

    private static Optional<Velocity> update(
            String u, Datapoint datapoint, Optional<RoutePoint> routePoint, Optional<Velocity> old) {
        if (routePoint.isEmpty()) {
            return old;
        }
        int index = routePoint.get().index();
        Instant timestamp = datapoint.getTimestamp();
        double newVelocity = datapoint.getVelocity();
        double v;
        Instant oldTimestamp;
        if (old.isEmpty()) {
            // this is the first datapoint, use the new information
            v = newVelocity;
            oldTimestamp = timestamp;
            if (newVelocity < MIN_VELOCITY) {
                // do not create if datapoint is too slow
                return Optional.empty();
            }
        } else {
            Velocity theOld = old.get();
            v = theOld.velocity();
            oldTimestamp = theOld.timestamp();
            if (newVelocity < MIN_VELOCITY) {
                return Optional.of(new Velocity(timestamp, index, 0, v));
            }
        }
        long oldTimebucket = oldTimestamp.getEpochSecond() / TIMEBUCKET_SECONDS;
        long newTimebucket = timestamp.getEpochSecond() / TIMEBUCKET_SECONDS;
        for (long t = oldTimebucket; t < newTimebucket; t++) {
            // exponentially weighted moving average
            v = newVelocity * ALPHA + v * (1 - ALPHA);
        }
        return Optional.of(new Velocity(timestamp, index, newVelocity, v));
    }
}
