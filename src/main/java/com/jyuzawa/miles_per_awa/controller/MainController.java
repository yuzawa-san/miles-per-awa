/*
 * Copyright (c) 2022 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.controller;

import com.jyuzawa.miles_per_awa.dto.LocationsRequest;
import com.jyuzawa.miles_per_awa.dto.LocationsResponse;
import com.jyuzawa.miles_per_awa.dto.RouteResponse;
import java.util.Collections;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @GetMapping("/route")
    public RouteResponse route() {
        return RouteResponse.builder()
                .name("Boston Marathon")
                .intervalMeters(100)
                .points(Collections.emptyList())
                .build();
    }

    @PostMapping("/locations")
    public LocationsResponse locations(@RequestBody LocationsRequest in) {
        return LocationsResponse.builder().build();
    }
}
