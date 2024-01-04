package com.emse.spring.automacorp.model.mappers;

import com.emse.spring.automacorp.model.entities.HeaterEntity;
import com.emse.spring.automacorp.model.records.Heater;

public class HeaterMapper {
    public static Heater of(HeaterEntity heater) {
        return new Heater(
                heater.getId(),
                heater.getName(),
                RoomMapper.of(heater.getRoom()),
                SensorMapper.of(heater.getStatus())
        );
    }
}
