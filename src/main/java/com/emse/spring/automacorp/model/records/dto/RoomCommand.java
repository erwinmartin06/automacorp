package com.emse.spring.automacorp.model.records.dto;

public record RoomCommand(String name, Long currentTempId, Double targetTemp, int floor, Long buildingId) {
}
