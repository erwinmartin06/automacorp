package com.emse.spring.automacorp.model.controllers;

import com.emse.spring.automacorp.model.dao.*;
import com.emse.spring.automacorp.model.entities.RoomEntity;
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
    private final BuildingDao buildingDao;

    public RoomController(RoomDao roomDao, SensorDao1 sensorDao1, WindowDao windowDao, HeaterDao heaterDao, BuildingDao buildingDao) {
        this.roomDao = roomDao;
        this.sensorDao1 = sensorDao1;
        this.windowDao = windowDao;
        this.heaterDao = heaterDao;
        this.buildingDao = buildingDao;
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
    public ResponseEntity<Room> create(@RequestBody RoomCommand command) {
        RoomEntity roomEntity = new RoomEntity();

        roomEntity.setName(command.name());
        roomEntity.setCurrentTemp(sensorDao1.getReferenceById(command.currentTempId()));
        roomEntity.setFloor(command.floor());
        roomEntity.setTargetTemp(command.targetTemp());
        roomEntity.setBuilding(buildingDao.findById(command.buildingId()).orElse(null));

        RoomEntity saved = roomDao.save(roomEntity);
        return ResponseEntity.ok(RoomMapper.of(saved));
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<Room> update(@PathVariable Long id, @RequestBody RoomCommand command) {
        RoomEntity room = roomDao.findById(id).orElse(null);
        if (room == null) {
            return ResponseEntity.badRequest().build();
        }

        room.setName(command.name());
        room.setCurrentTemp(sensorDao1.findById(command.currentTempId()).orElse(null));
        room.setFloor(command.floor());
        room.setTargetTemp(command.targetTemp());
        room.setBuilding(buildingDao.findById(command.buildingId()).orElse(null));

        RoomEntity saved = roomDao.save(room);
        return ResponseEntity.ok(RoomMapper.of(saved));
    }

    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable Long id) {
        RoomEntity room = roomDao.findById(id).orElse(null);
        if (room != null) {
            windowDao.deleteByRoom(id);
            heaterDao.deleteByRoom(id);
            roomDao.deleteById(id);
        }
    }

    @PutMapping(path = "/{id}/openWindows")
    @Transactional
    public ResponseEntity<Room> openWindows(@PathVariable Long id) {
        RoomEntity room = roomDao.findById(id).orElse(null);
        if (room == null) {
            return ResponseEntity.badRequest().build();
        }

        windowDao.openAllWindowsByRoom(id);

        RoomEntity saved = roomDao.save(room);
        return ResponseEntity.ok(RoomMapper.of(saved));
    }


    @PutMapping(path = "/{id}/closeWindows")
    @Transactional
    public ResponseEntity<Room> closeWindows(@PathVariable Long id) {
        RoomEntity room = roomDao.findById(id).orElse(null);
        if (room == null) {
            return ResponseEntity.badRequest().build();
        }

        windowDao.closeAllWindowsByRoom(id);

        RoomEntity saved = roomDao.save(room);
        return ResponseEntity.ok(RoomMapper.of(saved));
    }


    @PutMapping(path = "/{id}/onHeaters")
    @Transactional
    public ResponseEntity<Room> onHeaters(@PathVariable Long id) {
        RoomEntity room = roomDao.findById(id).orElse(null);
        if (room == null) {
            return ResponseEntity.badRequest().build();
        }

        heaterDao.onAllHeatersByRoom(id);

        RoomEntity saved = roomDao.save(room);
        return ResponseEntity.ok(RoomMapper.of(saved));
    }


    @PutMapping(path = "/{id}/offHeaters")
    @Transactional
    public ResponseEntity<Room> offHeaters(@PathVariable Long id) {
        RoomEntity room = roomDao.findById(id).orElse(null);
        if (room == null) {
            return ResponseEntity.badRequest().build();
        }

        heaterDao.offAllHeatersByRoom(id);

        RoomEntity saved = roomDao.save(room);
        return ResponseEntity.ok(RoomMapper.of(saved));
    }
}
