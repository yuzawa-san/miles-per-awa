/*
 * Copyright (c) 2022 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.model;

import com.jyuzawa.miles_per_awa.controller.LatLng;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public final class Datapoint {
    private final Instant timestamp;
    private final LatLng coords;
    private final Double heading;
    private final Double velocity;
}
