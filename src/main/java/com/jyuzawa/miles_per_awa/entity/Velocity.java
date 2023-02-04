/*
 * Copyright (c) 2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.entity;

import java.time.Instant;

public record Velocity(Instant timestamp, int index, double lastVelocity, double velocity) {}
