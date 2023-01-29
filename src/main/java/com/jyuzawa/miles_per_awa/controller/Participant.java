/*
 * Copyright (c) 2022 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.controller;

import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;

public final class Participant {
    // empirically determined for biking and running
    private static final long TIMEBUCKET_SECONDS = 30;
    private static final double ALPHA = 0.1;
    // from 4mph avg walking speed
    private static final double MIN_VELOCITY = 1.78816;

    private final Map<String, Velocity> users;

    public Participant() {
        this.users = new ConcurrentHashMap<>();
    }

    private record Velocity(Instant timestamp, double offset, double lastVelocity, double velocity) {}

    public Velocity calculate(String user, Instant timestamp, double offset, OptionalDouble velocity) {
        return users.compute(user, (u, old) -> {
            double newVelocity = velocity.getAsDouble();
            double v;
            Instant oldTimestamp;
            if(old == null) {
            	// this is the first datapoint, use the new information
            	v= newVelocity;
            	oldTimestamp = timestamp;
            	if (newVelocity < MIN_VELOCITY) {
            		// do not create if datapoint is too slow
                    return null;
                }
            }else {
            	v= old.velocity();
            	oldTimestamp = old.timestamp();
            	if (newVelocity < MIN_VELOCITY) {
                    return new Velocity(timestamp, offset, 0, v);
                }
            }
            long oldTimebucket = oldTimestamp.getEpochSecond() / TIMEBUCKET_SECONDS;
            long newTimebucket = timestamp.getEpochSecond() / TIMEBUCKET_SECONDS;
            for (long t = oldTimebucket; t < newTimebucket; t++) {
            	// exponentially weighted moving average
                v = newVelocity * ALPHA + v * (1 - ALPHA);
            }
            return new Velocity(timestamp, offset, newVelocity, v);
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
        Route route = new Route(points);
        // Optional<RoutePoint> closest = route.getClosest(new LatLng(41.78820254440553,-72.63131040977898),30d);
        // System.out.println(closest);
        Participant x = new Participant();
        header = true;
        Instant prevInstant = null;
        LatLng prev = null;
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
            v= Double.parseDouble(pieces[4]);
            prev = coords;
            prevInstant = instant;
            Double heading = Double.parseDouble(pieces[3]);

            Optional<RoutePoint> closest = route.getClosest(coords, heading);
            closest.ifPresent(c -> {
                Velocity w = x.calculate("james", instant, c.index() * 25, OptionalDouble.of(v));
                System.out.println(
                        w.timestamp().getEpochSecond() + "\t" + w.lastVelocity + "\t" + w.velocity + "\t" + w.offset);
            });
        }
    }
}
