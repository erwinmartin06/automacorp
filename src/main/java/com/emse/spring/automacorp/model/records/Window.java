package com.emse.spring.automacorp.model.records;

public record Window(Long id, String name, Sensor windowStatus, Long roomId) {
}
