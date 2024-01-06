package com.emse.spring.automacorp.model.records;

public record RoomCommand(String name, Long currentTempId, Double targetTemp, int floor, Long buildingId) {
}
