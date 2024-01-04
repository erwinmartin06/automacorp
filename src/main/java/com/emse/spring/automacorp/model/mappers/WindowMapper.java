package com.emse.spring.automacorp.model.mappers;

import com.emse.spring.automacorp.model.entities.WindowEntity;
import com.emse.spring.automacorp.model.records.Window;

public class WindowMapper {
    public static Window of(WindowEntity window) {
        return new Window(
                window.getId(),
                window.getName(),
                SensorMapper.of(window.getWindowStatus()),
                window.getRoom().getId()
        );
    }
}
