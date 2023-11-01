/*
 * Copyright (c) 2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.entity;

import java.time.Instant;
import java.util.List;
import reactor.util.function.Tuple2;

public record Velocity(Instant timestamp, int index, List<Tuple2<Datapoint, RoutePoint>> history, double velocity) {}
