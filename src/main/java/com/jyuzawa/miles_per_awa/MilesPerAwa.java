/*
 * Copyright (c) 2022 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class MilesPerAwa {

    @ExceptionHandler
    public void handle(Exception e) {
        log.warn("Returning HTTP 400 Bad Request", e);
    }
    
    @Bean
    public RouterFunction<ServerResponse> getEmployeeByIdRoute() {
      return RouterFunctions.route(RequestPredicates.GET("/employees/{id}"), 
        req -> ServerResponse.ok().bodyValue("hell world" + req.pathVariable("id")));
    }

    public static void main(String[] args) {
        SpringApplication.run(MilesPerAwa.class, args);
    }
}
