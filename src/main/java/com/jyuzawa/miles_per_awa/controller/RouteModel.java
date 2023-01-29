package com.jyuzawa.miles_per_awa.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jyuzawa.miles_per_awa.model.Datapoint;

@Component
public class RouteModel {
	private final Route route;
	
	public RouteModel(@Value("${route.path}") String rawPath) throws IOException {
		List<String> lines = Files.readAllLines(new File(rawPath).toPath());
        List<LatLng> points = new ArrayList<>();
        boolean header = true;
        for (String line : lines) {
            if (header) {
                header = false;
                continue;
            }
            String[] pieces = line.split(",");
            points.add(new LatLng(Double.parseDouble(pieces[0]), Double.parseDouble(pieces[1])));
        }
		this.route = new Route(points);
	}
	
	public Optional<RoutePoint> getClosest(Datapoint datapoint){
		return route.getClosest(datapoint.getCoords(), datapoint.getHeading());
	}
	
}
