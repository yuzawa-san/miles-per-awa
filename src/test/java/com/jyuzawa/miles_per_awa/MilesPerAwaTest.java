/*
 * Copyright (c) 2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.jyuzawa.miles_per_awa.dto.LocationsRequest;
import com.jyuzawa.miles_per_awa.dto.LocationsResponse;
import com.jyuzawa.miles_per_awa.dto.OverlandRequest;
import com.jyuzawa.miles_per_awa.dto.OverlandRequest.OverlandGeometry;
import com.jyuzawa.miles_per_awa.dto.OverlandRequest.OverlandLocation;
import com.jyuzawa.miles_per_awa.dto.OverlandRequest.OverlandProperties;
import com.jyuzawa.miles_per_awa.dto.SuccessResponse;
import com.jyuzawa.miles_per_awa.entity.LatLng;
import com.jyuzawa.miles_per_awa.service.RoutePointsService;

@SpringBootTest({"spring.main.allow-bean-definition-overriding=true"})
@AutoConfigureWebTestClient
public class MilesPerAwaTest {

    @Autowired
    WebTestClient client;
    
    
    
    @TestConfiguration 
    static class Config {
    	@Bean
    	@Primary
        public RoutePointsService routePointsService () {
        	RoutePointsService mock = Mockito.mock(RoutePointsService.class);
        	Mockito.when(mock.getPoints()).thenReturn(List.of(new LatLng(10d ,10.0d),new LatLng(10.0d ,10.001d), new LatLng(10.001d ,10.001d)));
        	return mock;
        }
    }
    

    @Test
    public void testRoute() {
        client.get().uri("/route").exchange().expectStatus().is2xxSuccessful();
    }
    
    @Test
    public void testLocations() {
        client.post().uri("/locations").bodyValue(LocationsRequest.builder().build()).exchange().expectStatus().is2xxSuccessful();
    }
    
    @Test
    public void testOverland() {
    	OverlandLocation first = OverlandLocation.builder().type("Feature").properties(OverlandProperties.builder().device_id("james").horizontal_accuracy(16).timestamp(Instant.now().minusSeconds(10)).build()).geometry(OverlandGeometry.builder().type("Point").coordinates(List.of( 10d, 10.00001d)).build()).build();
    	OverlandLocation last = OverlandLocation.builder().type("Feature").properties(OverlandProperties.builder().device_id("james").horizontal_accuracy(16).timestamp(Instant.now().minusSeconds(5)).build()).geometry(OverlandGeometry.builder().type("Point").coordinates(List.of( 10d, 10.00002d)).build()).build();
    	OverlandRequest request = OverlandRequest.builder().locations(List.of(first, last)).build();
        client.post().uri("/overland").bodyValue(request).exchange().expectBody(SuccessResponse.class).consumeWith(r-> {
        	assertEquals("ok", r.getResponseBody().getStatus());
        });
        client.post().uri("/locations").bodyValue(LocationsRequest.builder().build()).exchange().expectBody(LocationsResponse.class).consumeWith(r -> {
        	assertEquals("james", r.getResponseBody().getPeople().get(0).getName());
        });
    }
}
