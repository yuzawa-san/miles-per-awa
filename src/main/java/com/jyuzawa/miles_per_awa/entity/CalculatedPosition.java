/*
 * Copyright (c) 2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class CalculatedPosition {
    @Id
    private String id;

    private Instant positionTimestamp;
    private double latitude;
    private double longitude;

    @Nullable
    private Instant timestamp;

    private int index;
    private double lastVelocity;
    private double velocity;

    public CalculatedPosition(String id, Datapoint position, Optional<Velocity> velocity) {
        this.id = id;
        this.positionTimestamp = position.getTimestamp();
        LatLng coords = position.getCoords();
        this.latitude = coords.latitude();
        this.longitude = coords.longitude();
        if (velocity.isPresent()) {
            Velocity theVelocity = velocity.get();
            this.timestamp = theVelocity.timestamp();
            this.index = theVelocity.index();
            this.lastVelocity = theVelocity.lastVelocity();
            this.velocity = theVelocity.velocity();
        }
    }

    public Optional<Velocity> getVelocity() {
        if (timestamp == null) {
            return Optional.empty();
        }
        return Optional.of(new Velocity(timestamp, index, lastVelocity, velocity));
    }
}
