/*
 * Copyright (c) 2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.service;

import com.jyuzawa.miles_per_awa.entity.LatLng;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    private final List<LatLng> points;
    private final String eTag;

    @Autowired
    public RoutePointsService(@Value("${route.path}") String rawData) throws NoSuchAlgorithmException {
        List<LatLng> points = new ArrayList<>();
        for (String line : rawData.split("\n")) {
            if (line.isEmpty()) {
                continue;
            }
            String[] pieces = line.split(",");
            points.add(new LatLng(Double.parseDouble(pieces[0]), Double.parseDouble(pieces[1])));
        }
        byte[] buffer = new byte[points.size() * 16];
        DoubleBuffer doubleBuffer = ByteBuffer.wrap(buffer).asDoubleBuffer();
        for (LatLng point : points) {
            doubleBuffer.put(point.latitude());
            doubleBuffer.put(point.longitude());
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(buffer);
        StringBuilder result = new StringBuilder();
        for (byte aByte : hash) {
            result.append(String.format("%02x", aByte));
        }
        this.eTag = result.toString();
        this.points = Collections.unmodifiableList(points);
    }
}
