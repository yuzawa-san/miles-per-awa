/*
 * Copyright (c) 2022 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.controller;

import com.jyuzawa.miles_per_awa.dto.OverlandRequest;
import com.jyuzawa.miles_per_awa.dto.OverlandRequest.OverlandLocation;
import com.jyuzawa.miles_per_awa.dto.SuccessResponse;
import com.jyuzawa.miles_per_awa.entity.Datapoint;
import com.jyuzawa.miles_per_awa.service.IngestService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class OverlandController {

    private static final Duration DURATION = Duration.ofMinutes(1);

    private final IngestService ingestService;

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
        // TODO: fix id
        ingestService.ingest("user", point);
        return SuccessResponse.builder().status("ok").build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(Exception e) {
        log.warn("Returning HTTP 400 Bad Request", e);
    }
}
