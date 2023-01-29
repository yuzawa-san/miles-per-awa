/*
 * Copyright (c) 2022 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.controller;

import com.jyuzawa.miles_per_awa.dto.OverlandRequest;
import com.jyuzawa.miles_per_awa.dto.OverlandRequest.OverlandLocation;
import com.jyuzawa.miles_per_awa.dto.SuccessResponse;
import com.jyuzawa.miles_per_awa.model.Datapoint;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class OverlandController {

    private static final Duration DURATION = Duration.ofMinutes(1);

    @PostMapping("/overland")
    public SuccessResponse overland(@RequestBody OverlandRequest in) {
        List<OverlandLocation> locations = in.getLocations();
        if (locations == null) {
            return SuccessResponse.builder().status("empty").build();
        }
        Instant minTimestamp = locations.get(0).getProperties().getTimestamp().minus(DURATION);
        locations = locations.stream()
                .filter(OverlandLocation::isValid)
                .filter(location -> location.getProperties().getTimestamp().isAfter(minTimestamp))
                .toList();
        if (locations.size() < 2) {
            return SuccessResponse.builder().status("filtered").build();
        }
        OverlandLocation first = locations.get(0);
        OverlandLocation last = locations.get(locations.size() - 1);
        Datapoint point = last.toPoint(first);
        log.info(point.toString());
        Route route = null;
        // find all points within X meters
        // if there is 1: that is the one
        // else find the point with the a matching heading
        // user.put(closest)
        // insert (ts, v)
        // calculate avg v
        // set (user, ts, v)
        return SuccessResponse.builder().status("ok").build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(Exception e) {
        log.warn("Returning HTTP 400 Bad Request", e);
    }
}
