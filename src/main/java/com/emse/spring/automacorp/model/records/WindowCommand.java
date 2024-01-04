package com.emse.spring.automacorp.model.records;

import com.emse.spring.automacorp.model.entities.SensorEntity;

public record WindowCommand(String name, Long sensorId, Long roomId) {
}
