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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
                        routePoint.flatMap(pt -> update(
                                u,
                                datapoint,
                                pt.index(),
                                Optional.ofNullable(old).flatMap(CalculatedPosition::velocity)))));
    }

    private static Optional<Velocity> update(String u, Datapoint datapoint, int index, Optional<Velocity> old) {
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
