/*
 * Copyright (c) 2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.service;

import com.jyuzawa.miles_per_awa.entity.CalculatedPosition;
import org.springframework.data.repository.CrudRepository;

public interface VelocityRepository extends CrudRepository<CalculatedPosition, String> {}
