package com.emse.spring.automacorp.model.mappers;

import com.emse.spring.automacorp.model.SensorType;
import com.emse.spring.automacorp.model.entities.HeaterEntity;
import com.emse.spring.automacorp.model.entities.WindowEntity;
import com.emse.spring.automacorp.model.records.Heater;
import com.emse.spring.automacorp.model.entities.RoomEntity;
import com.emse.spring.automacorp.model.entities.SensorEntity;
import com.emse.spring.automacorp.model.records.Room;
import com.emse.spring.automacorp.model.records.Sensor;
import com.emse.spring.automacorp.model.records.Window;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class HeaterMapperTest {

    @Test
    void shouldMapHeater() {
        //Arrange
        SensorEntity currentTemperature = new SensorEntity(SensorType.TEMPERATURE, "Room temperature");
        currentTemperature.setId(3L);
        currentTemperature.setValue(24.2);

        SensorEntity windowStatus = new SensorEntity(SensorType.STATUS, "Window Status");
        windowStatus.setId(4L);
        windowStatus.setValue(0.0);

        RoomEntity roomEntity = new RoomEntity("Room", currentTemperature, 1);
        roomEntity.setId(1L);
        roomEntity.setTargetTemp(22.0);
        WindowEntity window = new WindowEntity("Window 1", windowStatus, roomEntity);
        window.setId(2L);
        roomEntity.setWindows(List.of(window));

        HeaterEntity heaterEntity = new HeaterEntity();
        heaterEntity.setId(2L);
        heaterEntity.setName("Heater Test");
        heaterEntity.setRoom(roomEntity);
        heaterEntity.setStatus(currentTemperature);

        //Act
        Heater heater = HeaterMapper.of(heaterEntity);

        //Assert
        Heater expectedHeater = new Heater(
                2L,
                "Heater Test",
                new Room(1L,
                        "Room",
                        new Sensor(3L, "Room temperature", 24.2, SensorType.TEMPERATURE),
                        22.0,
                        1,
                        List.of(new Window(
                                2L,
                                "Window 1",
                                new Sensor(4L, "Window Status", 0.0, SensorType.STATUS),
                                1L
                        ))
                ),
                new Sensor(3L, "Room temperature", 24.2, SensorType.TEMPERATURE)
        );

        Assertions.assertThat(heater).usingRecursiveAssertion().isEqualTo(expectedHeater);
    }
}