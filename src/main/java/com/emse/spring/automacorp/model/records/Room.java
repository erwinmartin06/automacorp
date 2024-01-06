package com.emse.spring.automacorp.model.records;

import java.util.List;

public record Room(Long id, String name, Double currentTemp, Double targetTemp, int floor, Long buildingId, Long currentTempId, List<Window> windows, List<Heater> heaters) {
}
