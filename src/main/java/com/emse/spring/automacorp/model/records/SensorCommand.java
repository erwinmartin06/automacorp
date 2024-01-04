package com.emse.spring.automacorp.model.records;

import com.emse.spring.automacorp.model.SensorType;

public record SensorCommand(String name, Double value, SensorType sensorType) {
}
