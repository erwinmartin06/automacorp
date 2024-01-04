package com.emse.spring.automacorp.model.records;

import java.util.List;

public record Room(Long id, String name, Sensor currentTemp, Double targetTemp, int floor, List<Window> windows) {
}
