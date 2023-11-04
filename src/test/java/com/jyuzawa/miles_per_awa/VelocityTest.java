/*
 * Copyright (c) 2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jyuzawa.miles_per_awa.controller.OverlandController;
import com.jyuzawa.miles_per_awa.dto.OverlandRequest;
import com.jyuzawa.miles_per_awa.entity.CalculatedPosition;
import com.jyuzawa.miles_per_awa.entity.Datapoint;
import com.jyuzawa.miles_per_awa.entity.LatLng;
import com.jyuzawa.miles_per_awa.entity.RoutePoint;
import com.jyuzawa.miles_per_awa.service.IngestService;
import com.jyuzawa.miles_per_awa.service.RoutePointsService;
import com.jyuzawa.miles_per_awa.service.RouteService;
import com.jyuzawa.miles_per_awa.service.VelocityService;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

public class VelocityTest {

    public static void main(String[] args) throws Exception {
        RoutePointsService routePoints = new RoutePointsService("/Users/jtyuzawa/Documents/chicago.csv");
        RouteService routeService = new RouteService("", false, routePoints);
        VelocityService velocityService = new VelocityService();
        IngestService ingest = new IngestService(routeService, velocityService);
        double alpha = 0.2;
        String sep = ": data: ";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.registerModule(new JavaTimeModule());

        var featureCollection = new FeatureCollection();
        var course = new Feature();
        course.properties.put("stroke", "red");
        course.properties.put("stroke-width", "1");
        var courseGeometry = new LineStringGeometry();
        var courseCoords = courseGeometry.coordinates;
        for (LatLng latLng : routePoints.getPoints()) {
            courseCoords.add(List.of(latLng.longitude(), latLng.latitude()));
        }
        course.geometry = courseGeometry;
        featureCollection.features.add(course);
        var points = new Feature();
        points.properties.put("stroke", "green");
        points.properties.put("stroke-width", "1");
        var pointsGeometry = new LineStringGeometry();
        var pointsCoords = pointsGeometry.coordinates;
        points.geometry = pointsGeometry;
        featureCollection.features.add(points);

        OverlandController overlandController = new OverlandController(objectMapper, ingest);
        List<String> lines = Files.readAllLines(Path.of("/Users/jtyuzawa/Documents/chicago_marathon.log"));
        StringBuilder sb = new StringBuilder();
        double v = 3.6;
        double sum = 0;
        int count = 0;
        for (String line : lines) {
            String[] pieces = line.split(sep);
            if (pieces.length != 2) {
                continue;
            }
            List<Datapoint> out = overlandController.convert(objectMapper.readValue(pieces[1], OverlandRequest.class));

            for (Datapoint point : out) {
                List<Double> coord =
                        List.of(point.getCoords().longitude(), point.getCoords().latitude());
                pointsCoords.add(coord);
                Optional<RoutePoint> closest = routeService.getClosest(point);

                if (closest.isPresent()) {
                    var detectedPoint = new Feature();
                    detectedPoint.properties.put(
                            "timestamp", String.valueOf(point.getTimestamp().getEpochSecond()));
                    detectedPoint.properties.put(
                            "offset", String.valueOf(closest.get().index() * RouteService.INTERVAL_METERS));
                    detectedPoint.properties.put("v", String.valueOf(26.8224 / point.getVelocity()));

                    var detectedPointGeometry = new PointGeometry();
                    detectedPointGeometry.type = "Point";
                    detectedPointGeometry.coordinates = coord;
                    detectedPoint.geometry = detectedPointGeometry;
                    featureCollection.features.add(detectedPoint);

                    // sb.append("["+point.getCoords().longitude() + ","+point.getCoords().latitude()+"],");

                    // sb.append(point.getTimestamp() + "\t"+(-26.8224/point.getVelocity())+"\t"+(-26.8224/vavg)+"\n");

                    // sb.append(point.getTimestamp().toString().replace("T"," ").replace("Z", "")+"\t"+(-26.8224/v)
                    // +"\t"+(-26.8224/vavg) + "\n");
                    CalculatedPosition n = velocityService.calculate("dning", point, closest);
                    OptionalDouble lastV = n.velocity();
                    if (lastV.isEmpty()) {
                        continue;
                    }
                    //						if(Math.abs(n.position().getVelocity() - v.lastVelocity())< 0.0001) {
                    //							sb.append("["+n.position().getCoords().longitude() +
                    // ","+n.position().getCoords().latitude()+"],");
                    //						}

                    // sb.append(n.position().get)

                    double vv = n.velocity().getAsDouble();
                    v = v * (1 - alpha) + vv * alpha;
                    sum += point.getVelocity();
                    count++;
                    v = vv;
                    sb.append(n.position()
                                    .getTimestamp()
                                    .toString()
                                    .replace("T", " ")
                                    .replace("Z", "") + "\t"
                            + (-26.8224 / n.position().getVelocity()) + "\t" + (-26.8224 / v) + "\n");
                }
            }
        }
        System.out.println(sb.toString());

        // https://geojson.io/
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("build/geo.json"), featureCollection);
    }

    static class FeatureCollection {
        public String type = "FeatureCollection";
        public List<Feature> features = new ArrayList<>();
    }

    static class Feature {
        public String type = "Feature";
        public Map<String, String> properties = new HashMap<>();
        public Object geometry;
    }

    static class LineStringGeometry {
        public String type = "LineString";
        public List<List<Double>> coordinates = new ArrayList<>();
    }

    static class PointGeometry {
        public String type = "Point";
        public List<Double> coordinates = new ArrayList<>();
    }
}
