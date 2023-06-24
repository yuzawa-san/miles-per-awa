/*
 * Copyright (c) 2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.entity;

import java.util.Optional;

public record CalculatedPosition(Datapoint position, Optional<Velocity> velocity) {}
