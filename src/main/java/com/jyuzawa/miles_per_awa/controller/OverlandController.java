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
import com.jyuzawa.miles_per_awa.entity.CalculatedPosition;
import com.jyuzawa.miles_per_awa.entity.Datapoint;
import com.jyuzawa.miles_per_awa.entity.LatLng;
import com.jyuzawa.miles_per_awa.service.IngestService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
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

    private final int intervalMeters =30;

    @PostMapping("/overland")
    public SuccessResponse overland(@RequestBody JsonNode in) {
        log.info("data: " + in);
        OverlandRequest r = jacksonObjectMapper.convertValue(in, OverlandRequest.class);
        for(Datapoint point : convert(r)) {
        	CalculatedPosition out = ingestService.ingest(point.getUser(), point);
			log.info("POS "+out);
        }
        return SuccessResponse.builder().result("ok").build();
    }
    
    public List<Datapoint> convert(OverlandRequest r) {
        List<OverlandLocation> locations = r.getLocations();
        if (locations == null) {
            return List.of();
        }
        OverlandLocation lastLocation = locations.get(locations.size() - 1);
        Instant minTimestamp = lastLocation.getProperties().getTimestamp().minus(MAX_LOOKBACK);
        locations = locations.stream()
                .filter(OverlandLocation::isValid)
                .filter(location -> location.getProperties().getTimestamp().isAfter(minTimestamp))
                .toList();
        if (locations.size() < 2) {
            return List.of();
        }
        List<Datapoint> datapoints = new ArrayList<>();
        int n = locations.size();
        int i = 0;
        lbl:
        while(i < n) {
        	OverlandLocation start = locations.get(i);
        	LatLng startCoords = start.getGeometry().getLatLng();
			//System.out.println("["+startCoords.longitude() + ","+startCoords.latitude()+"],");
        	int j = i + 1;
        	while(j < n) {
        		OverlandLocation candidate = locations.get(j);
        		LatLng candidateCoords = candidate.getGeometry().getLatLng();
        		double d = startCoords.distance(candidateCoords);
        		if(d > intervalMeters) {
        			i = j;
        			Instant timestamp = candidate.getProperties().getTimestamp();
        			Datapoint datapoint = Datapoint.builder()
        					.user(start.getProperties().getDevice_id())
        	                .timestamp(timestamp)
        	                .coords(candidateCoords)
        	                .heading(startCoords.heading(candidateCoords))
        	                .velocity(d
        	                        / (timestamp.getEpochSecond() - start.getProperties().getTimestamp().getEpochSecond()))
        	                .build();
        			datapoints.add(datapoint);
        			continue lbl;
        		}
        		j++;
        	}
        	i++;
        }
        return datapoints;
    }
}
