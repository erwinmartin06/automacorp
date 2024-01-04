package com.emse.spring.automacorp.model.controllers;

import com.emse.spring.automacorp.model.dao.*;
import com.emse.spring.automacorp.model.entities.BuildingEntity;
import com.emse.spring.automacorp.model.entities.RoomEntity;
import com.emse.spring.automacorp.model.mappers.BuildingMapper;
import com.emse.spring.automacorp.model.records.Building;
import com.emse.spring.automacorp.model.records.BuildingCommand;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/api/buildings")
@Transactional
public class BuildingController {
    private final BuildingDao buildingDao;
    private final RoomDao roomDao;
    private final SensorDao1 sensorDao1;
    private final WindowDao windowDao;
    private final HeaterDao heaterDao;

    public BuildingController(BuildingDao buildingDao, RoomDao roomDao, SensorDao1 sensorDao1, WindowDao windowDao, HeaterDao heaterDao) {
        this.buildingDao = buildingDao;
        this.roomDao = roomDao;
        this.sensorDao1 = sensorDao1;
        this.windowDao = windowDao;
        this.heaterDao = heaterDao;
    }

    @GetMapping
    public List<Building> findAll() {
        return buildingDao.findAll()
                .stream()
                .map(BuildingMapper::of)
                .sorted(Comparator.comparing(Building::name))
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/{id}")
    public Building findById(@PathVariable Long id) {
        return buildingDao.findById(id).map(BuildingMapper::of).orElse(null);
    }

    @PostMapping
    public ResponseEntity<Building> create(@RequestBody BuildingCommand command) {
        List<RoomEntity> rooms = command.roomId().stream()
                .map(roomId -> roomDao.findById(roomId).orElse(null))
                .collect(Collectors.toList());

        BuildingEntity entity = new BuildingEntity(command.name(), sensorDao1.findById(command.outsideTemperatureId()).orElse(null), rooms);
        rooms.forEach(room -> room.setBuilding(entity));

        BuildingEntity saved = buildingDao.save(entity);
        return ResponseEntity.ok(BuildingMapper.of(saved));
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<Building> update(@PathVariable Long id, @RequestBody BuildingCommand command) {
        BuildingEntity entity = buildingDao.findById(id).orElse(null);
        if (entity == null) {
            return ResponseEntity.badRequest().build();
        }

        entity.setName(command.name());
        entity.setOutsideTemperature(sensorDao1.findById(command.outsideTemperatureId()).orElse(null));

        List<RoomEntity> rooms = command.roomId().stream()
                .map(roomId -> roomDao.findById(roomId).orElse(null))
                .collect(Collectors.toList());
        entity.setRooms(rooms);
        rooms.forEach(room -> room.setBuilding(entity));

        return ResponseEntity.ok(BuildingMapper.of(buildingDao.save(entity)));
    }

    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable Long id) {
        BuildingEntity building = buildingDao.findById(id).orElse(null);
        if (building != null) {
            building.getRooms().forEach(room -> {
                room.getWindows().forEach(window -> windowDao.deleteById(window.getId()));
                room.getHeaters().forEach(heater -> heaterDao.deleteById(heater.getId()));
                roomDao.deleteById(room.getId());
            });
            buildingDao.deleteById(id);
        }
    }
}
