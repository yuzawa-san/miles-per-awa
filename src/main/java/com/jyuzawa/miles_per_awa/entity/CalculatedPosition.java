/*
 * Copyright (c) 2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.entity;

import java.time.Instant;
import java.util.List;
import java.util.OptionalDouble;
import lombok.Builder;

@Builder(toBuilder = true)
public record CalculatedPosition(
        Datapoint position, List<RouteTuple> history, Instant timestamp, int index, OptionalDouble velocity) {}
