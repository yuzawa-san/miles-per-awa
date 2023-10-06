/*
 * Copyright (c) 2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.service;

import com.jyuzawa.miles_per_awa.entity.LatLng;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class RoutePointsService {

    @Getter
    private final List<LatLng> points;

    private final Instant lastModified;

    @Autowired
    public RoutePointsService(@Value("${route.path}") String rawPath) throws IOException {
        Path file = Paths.get(rawPath);
        List<String> lines = Files.readAllLines(file);
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
        BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
        this.lastModified = attr.lastModifiedTime().toInstant();
        this.points = Collections.unmodifiableList(points);
    }
}
