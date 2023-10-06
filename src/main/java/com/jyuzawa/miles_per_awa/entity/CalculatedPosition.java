/*
 * Copyright (c) 2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.entity;

import java.util.Optional;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class CalculatedPosition{ 
	@Id
	private String id;
	private Datapoint position;
	private Optional<Velocity> velocity;
	
	public CalculatedPosition(Datapoint position, Optional<Velocity> velocity) {
		this.position = position;
		this.velocity = velocity;
	}
}
