package com.emse.spring.automacorp.model.mappers;

import com.emse.spring.automacorp.model.entities.RoomEntity;
import com.emse.spring.automacorp.model.records.Room;

import java.util.stream.Collectors;

public class RoomMapper {
    public static Room of(RoomEntity room){
        return new Room(
                room.getId(),
                room.getName(),
                SensorMapper.of(room.getCurrentTemp()),
                room.getTargetTemp(),
                room.getFloor(),
                room.getWindows().stream().map(WindowMapper::of).collect(Collectors.toList())
        );
    }
}
