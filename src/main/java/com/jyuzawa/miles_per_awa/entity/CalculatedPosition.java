/*
 * Copyright (c) 2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    private long positionTimestampSeconds;
    private double latitude;
    private double longitude;
    private long timestampSeconds;
    private int index;
    private double velocity;
    private boolean hasVelocity;

    @Column(columnDefinition = "LONGTEXT")
    @Convert(converter = HistoryConverter.class)
    private List<RouteTuple> history;
}
