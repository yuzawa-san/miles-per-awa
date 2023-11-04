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
import com.jyuzawa.miles_per_awa.service.MilesPerAwaProps;
import com.jyuzawa.miles_per_awa.service.RoutePointsService;
import com.jyuzawa.miles_per_awa.service.RouteService;
import com.jyuzawa.miles_per_awa.service.VelocityRepository;
import com.jyuzawa.miles_per_awa.service.VelocityService;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Backtesting {

    public static void main(String[] args) throws Exception {
        RoutePointsService routePoints = new RoutePointsService("/Users/jtyuzawa/Documents/chicago.csv");
        RouteService routeService = new RouteService(routePoints);
        Map<String, CalculatedPosition> db = new ConcurrentHashMap<String, CalculatedPosition>();
        VelocityService velocityService = new VelocityService(
                new VelocityRepository() {

                    @Override
                    public <S extends CalculatedPosition> Iterable<S> saveAll(Iterable<S> entities) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public <S extends CalculatedPosition> S save(S entity) {
                        db.put(entity.getId(), entity);
                        return entity;
                    }

                    @Override
                    public Optional<CalculatedPosition> findById(String id) {
                        return Optional.ofNullable(db.get(id));
                    }

                    @Override
                    public Iterable<CalculatedPosition> findAllById(Iterable<String> ids) {
                        List<CalculatedPosition> out = new ArrayList<>();
                        for (String id : ids) {
                            CalculatedPosition item = db.get(id);
                            if (item != null) {
                                out.add(item);
                            }
                        }
                        return out;
                    }

                    @Override
                    public Iterable<CalculatedPosition> findAll() {
                        return db.values();
                    }

                    @Override
                    public boolean existsById(String id) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public void deleteById(String id) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void deleteAllById(Iterable<? extends String> ids) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void deleteAll(Iterable<? extends CalculatedPosition> entities) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void deleteAll() {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void delete(CalculatedPosition entity) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public long count() {
                        // TODO Auto-generated method stub
                        return 0;
                    }
                },
                new MilesPerAwaProps());
        IngestService ingest = new IngestService(routeService, velocityService);
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
        StringBuilder sb = new StringBuilder("ts\tv\tv_avg\n");
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

                    CalculatedPosition n = velocityService.calculate(point, closest);
                    if (!n.isHasVelocity()) {
                        continue;
                    }

                    sb.append(Instant.ofEpochSecond(n.getPositionTimestampSeconds())
                                    .toString()
                                    .replace("T", " ")
                                    .replace("Z", "")
                            + "\t"
                            + (-26.8224 / point.getVelocity()) + "\t" + (-26.8224 / n.getVelocity()) + "\n");
                }
            }
        }
        Files.writeString(Path.of("build/out.tsv"), sb.toString());

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
