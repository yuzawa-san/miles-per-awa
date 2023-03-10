/*
 * Copyright (c) 2022-2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jyuzawa.miles_per_awa.dto.OverlandRequest;
import com.jyuzawa.miles_per_awa.dto.OverlandRequest.OverlandLocation;
import com.jyuzawa.miles_per_awa.dto.SuccessResponse;
import com.jyuzawa.miles_per_awa.entity.Datapoint;
import com.jyuzawa.miles_per_awa.entity.Velocity;
import com.jyuzawa.miles_per_awa.service.IngestService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class OverlandController {

    private static final Duration MAX_LOOKBACK = Duration.ofMinutes(1);

    private final ObjectMapper jacksonObjectMapper;

    private final IngestService ingestService;

    @PostMapping("/overland")
    public SuccessResponse overland(@RequestBody JsonNode in) {
        log.info("data: " + in);
        OverlandRequest r = jacksonObjectMapper.convertValue(in, OverlandRequest.class);
        List<OverlandLocation> locations = r.getLocations();
        if (locations == null) {
            return SuccessResponse.builder().result("empty").build();
        }
        OverlandLocation lastLocation = locations.get(locations.size() - 1);
        Instant minTimestamp = lastLocation.getProperties().getTimestamp().minus(MAX_LOOKBACK);
        locations = locations.stream()
                .filter(OverlandLocation::isValid)
                .filter(location -> location.getProperties().getTimestamp().isAfter(minTimestamp))
                .toList();
        if (locations.size() < 2) {
            return SuccessResponse.builder().result("filtered").build();
        }
        OverlandLocation first = locations.get(0);
        OverlandLocation last = locations.get(locations.size() - 1);
        Datapoint point = last.toPoint(first);
        log.info(point.toString());
        Optional<Velocity> out =
                ingestService.ingest(lastLocation.getProperties().getDevice_id(), point);
        log.info(out.toString());
        return SuccessResponse.builder().result("ok").build();
    }
}
