package com.emse.spring.automacorp.model.records;

import java.util.List;

public record BuildingCommand(String name, Long outsideTemperatureId, List<Long> roomId) {
}
