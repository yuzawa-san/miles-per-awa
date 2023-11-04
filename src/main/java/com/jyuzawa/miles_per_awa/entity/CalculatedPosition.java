/*
 * Copyright (c) 2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CalculatedPosition {
    @Id
    private String id;

    private Instant positionTimestamp;
    private double latitude;
    private double longitude;

    @Nullable
    private Instant timestamp;

    @Nullable
    private Integer index;

    @Nullable
    private Double velocity;

    @Convert(converter = HistoryConverter.class)
    private List<RouteTuple> history;
}
