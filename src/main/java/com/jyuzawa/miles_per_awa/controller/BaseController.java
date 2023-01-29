package com.jyuzawa.miles_per_awa.controller;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.jyuzawa.miles_per_awa.model.Datapoint;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class BaseController {
	
	private final RouteModel routeModel;
	private final Participant participantModel;
	
	public void accept(Datapoint datapoint) {
		Optional<RoutePoint> closest = routeModel.getClosest(datapoint);
		if(!closest.isPresent()) {
			return;
		}
		participantModel.calculate(null, null, 0, null);
	}
}
