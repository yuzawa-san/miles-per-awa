/*
 * Copyright (c) 2022 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.service;

import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jyuzawa.miles_per_awa.entity.Datapoint;
import com.jyuzawa.miles_per_awa.entity.LatLng;
import com.jyuzawa.miles_per_awa.entity.Velocity;

@Component
public final class VelocityService {
    // empirically determined for biking and running
    private static final long TIMEBUCKET_SECONDS = 30;
    private static final double ALPHA = 0.1;
    // from 4mph avg walking speed
    private static final double MIN_VELOCITY = 1.78816;

    private final Map<String, Velocity> users;

    @Autowired
    public VelocityService() {
        // TODO: better storage
        this.users = new ConcurrentHashMap<>();
    }

    public Map<String, Velocity> getUsers(Collection<String> userIds) {
        if (userIds == null) {
            return Collections.unmodifiableMap(users);
        }
        Map<String, Velocity> out = new HashMap<>(userIds.size());
        for (String userId : userIds) {
            Velocity velocity = users.get(userId);
            if (velocity != null) {
                out.put(userId, velocity);
            }
        }
        return out;
    }

    public Velocity calculate(String user, Datapoint datapoint, int index) {
        return users.compute(user, (u, old) -> {
            Instant timestamp = datapoint.getTimestamp();
            double newVelocity = datapoint.getVelocity();
            double v;
            Instant oldTimestamp;
            if (old == null) {
                // this is the first datapoint, use the new information
                v = newVelocity;
                oldTimestamp = timestamp;
                if (newVelocity < MIN_VELOCITY) {
                    // do not create if datapoint is too slow
                    return null;
                }
            } else {
                v = old.velocity();
                oldTimestamp = old.timestamp();
                if (newVelocity < MIN_VELOCITY) {
                    return new Velocity(timestamp, index, 0, v);
                }
            }
            long oldTimebucket = oldTimestamp.getEpochSecond() / TIMEBUCKET_SECONDS;
            long newTimebucket = timestamp.getEpochSecond() / TIMEBUCKET_SECONDS;
            for (long t = oldTimebucket; t < newTimebucket; t++) {
                // exponentially weighted moving average
                v = newVelocity * ALPHA + v * (1 - ALPHA);
            }
            return new Velocity(timestamp, index, newVelocity, v);
        });
    }

    public static void main(String[] args) throws Exception {

        List<String> routeLines = Files.readAllLines(new File("/Users/jtyuzawa/Documents/nyc.csv").toPath());
        List<String> participantLines =
                Files.readAllLines(new File("/Users/jtyuzawa/Documents/nyc_locations.csv").toPath());
        List<LatLng> points = new ArrayList<>();
        boolean header = true;
        for (String line : routeLines) {
            if (header) {
                header = false;
                continue;
            }
            String[] pieces = line.split(",");
            points.add(new LatLng(Double.parseDouble(pieces[0]), Double.parseDouble(pieces[1])));
        }
        RouteService route = new RouteService("",25, points);
        // Optional<RoutePoint> closest = route.getClosest(new LatLng(41.78820254440553,-72.63131040977898),30d);
        // System.out.println(closest);
        VelocityService x = new VelocityService();
        IngestService i = new IngestService(route, x);
        header = true;
        for (String line : participantLines) {
            if (header) {
                header = false;
                continue;
            }
            String[] pieces = line.split(",");
            long ms = Long.parseLong(pieces[0]);
            Instant instant = Instant.ofEpochMilli(ms);
            LatLng coords = new LatLng(Double.parseDouble(pieces[1]), Double.parseDouble(pieces[2]));
            final double v;
            //            if (prev != null) {
            //                v = prev.distance(coords) / (instant.getEpochSecond() - prevInstant.getEpochSecond());
            //            } else {
            //                v = 0;
            //            }
            v = Double.parseDouble(pieces[4]);
            Double heading = Double.parseDouble(pieces[3]);
            i.ingest(
                            "james",
                            Datapoint.builder()
                                    .coords(coords)
                                    .timestamp(instant)
                                    .heading(heading)
                                    .velocity(v)
                                    .build())
                    .ifPresent(w -> {
                        System.out.println(w.timestamp().getEpochSecond() + "\t" + w.lastVelocity() + "\t"
                                + w.velocity() + "\t" + w.index()*route.getIntervalMeters());
                    });
        }
    }
}
