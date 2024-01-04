package com.emse.spring.automacorp.model.controllers;

import com.emse.spring.automacorp.model.SensorType;
import com.emse.spring.automacorp.model.dao.HeaterDao;
import com.emse.spring.automacorp.model.dao.RoomDao;
import com.emse.spring.automacorp.model.dao.SensorDao1;
import com.emse.spring.automacorp.model.dao.WindowDao;
import com.emse.spring.automacorp.model.entities.RoomEntity;
import com.emse.spring.automacorp.model.entities.SensorEntity;
import com.emse.spring.automacorp.model.mappers.RoomMapper;
import com.emse.spring.automacorp.model.records.Room;
import com.emse.spring.automacorp.model.records.RoomCommand;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/api/rooms")
@Transactional
public class RoomController {
    private final RoomDao roomDao;
    private final SensorDao1 sensorDao1;
    private final WindowDao windowDao;
    private final HeaterDao heaterDao;

    public RoomController(RoomDao roomDao, SensorDao1 sensorDao1, WindowDao windowDao, HeaterDao heaterDao) {
        this.roomDao = roomDao;
        this.sensorDao1 = sensorDao1;
        this.windowDao = windowDao;
        this.heaterDao = heaterDao;
    }

    @GetMapping
    public List<Room> findAll() {
        return roomDao.findAll()
                .stream()
                .map(RoomMapper::of)
                .sorted(Comparator.comparing(Room::name))
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/{id}")
    public Room findById(@PathVariable Long id) {
        return roomDao.findById(id).map(RoomMapper::of).orElse(null);
    }

    @PostMapping
    public ResponseEntity<Room> createOrUpdate(@RequestBody RoomCommand command) {
        RoomEntity roomEntity = new RoomEntity(command.name(), sensorDao1.findById(command.currentTempId()).orElse(null), command.floor());
        RoomEntity saved = roomDao.save(roomEntity);
        return ResponseEntity.ok(RoomMapper.of(saved));
    }

    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable Long id) {
        RoomEntity room = roomDao.findById(id).orElse(null);
        if (room != null) {
            room.getWindows().forEach(window -> windowDao.deleteById(window.getId()));
            room.getHeaters().forEach(heater -> heaterDao.deleteById(heater.getId()));
            roomDao.deleteById(id);
        }
    }

    @PutMapping(path = "/{roomId}/openWindows")
    public void openWindows(@PathVariable Long roomId) {
        RoomEntity room = roomDao.findById(roomId).orElse(null);
        SensorEntity windowStatus = new SensorEntity(SensorType.STATUS, "Sensor");
        windowStatus.setValue(1.0);
        if (room != null) {
            room.getWindows().forEach(window -> {
                window.setWindowStatus(windowStatus);
                windowDao.save(window);
            });
        }
    }

    @PutMapping(path = "/{roomId}/closeWindows")
    public void closeWindows(@PathVariable Long roomId) {
        RoomEntity room = roomDao.findById(roomId).orElse(null);
        SensorEntity windowStatus = new SensorEntity(SensorType.STATUS, "Sensor");
        windowStatus.setValue(0.0);
        if (room != null) {
            room.getWindows().forEach(window -> {
                window.setWindowStatus(windowStatus);
                windowDao.save(window);
            });
        }
    }
}
