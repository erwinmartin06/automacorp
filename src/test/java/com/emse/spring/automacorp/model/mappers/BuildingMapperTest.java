package com.emse.spring.automacorp.model.mappers;

import com.emse.spring.automacorp.model.SensorType;
import com.emse.spring.automacorp.model.entities.*;
import com.emse.spring.automacorp.model.records.Building;
import com.emse.spring.automacorp.model.records.Room;
import com.emse.spring.automacorp.model.records.Sensor;
import com.emse.spring.automacorp.model.records.Window;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class BuildingMapperTest {

    @Test
    void shouldMapBuilding() {
        //Arrange
        SensorEntity outsideTemperature = new SensorEntity(SensorType.TEMPERATURE, "Outside Temperature");
        outsideTemperature.setValue(20.0);
        outsideTemperature.setId(1L);

        SensorEntity roomTemp = new SensorEntity(SensorType.TEMPERATURE, "Room Temperature");
        roomTemp.setValue(25.0);
        roomTemp.setId(2L);

        RoomEntity roomEntity = new RoomEntity("Room Test", roomTemp, 1);
        SensorEntity sensorWindow = new SensorEntity(SensorType.TEMPERATURE, "Window Sensor");
        sensorWindow.setId(5L);
        sensorWindow.setValue(15.0);
        WindowEntity window1 =  new WindowEntity("Window 1", sensorWindow, roomEntity);
        window1.setId(10L);
        List<WindowEntity> windows = List.of(window1);
        roomEntity.setWindows(windows);
        roomEntity.setId(3L);
        roomEntity.setTargetTemp(21.0);

        List<RoomEntity> rooms = List.of(roomEntity);

        BuildingEntity buildingEntity = new BuildingEntity("Building Test", outsideTemperature, rooms);
        buildingEntity.setId(4L);

        //Act
        Building building = BuildingMapper.of(buildingEntity);

        //Assert
        Building expectedBuilding = new Building(
                4L,
                "Building Test",
                new Sensor(1L, "Outside Temperature", 20.0, SensorType.TEMPERATURE),
                List.of(new Room(
                        3L,
                        "Room Test",
                        new Sensor(2L, "Room Temperature", 25.0, SensorType.TEMPERATURE),
                        21.0,
                        1,
                        List.of(
                                new Window(10L, "Window 1", new Sensor(5L, "Window Sensor", 15.0, SensorType.TEMPERATURE), 3L)
                        )
                ))
        );

        Assertions.assertThat(building).usingRecursiveAssertion().isEqualTo(expectedBuilding);
    }
}