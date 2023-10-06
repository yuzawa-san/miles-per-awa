package com.jyuzawa.miles_per_awa.service;

import org.springframework.data.repository.CrudRepository;

import com.jyuzawa.miles_per_awa.entity.CalculatedPosition;

public interface VelocityRepository extends CrudRepository<CalculatedPosition, String>  {

}
