package com.emse.spring.automacorp.model.controllers;

import com.emse.spring.automacorp.model.dao.HeaterDao;
import com.emse.spring.automacorp.model.dao.RoomDao;
import com.emse.spring.automacorp.model.dao.SensorDao1;
import com.emse.spring.automacorp.model.entities.HeaterEntity;
import com.emse.spring.automacorp.model.mappers.HeaterMapper;
import com.emse.spring.automacorp.model.records.Heater;
import com.emse.spring.automacorp.model.records.HeaterCommand;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/api/heaters")
@Transactional
public class HeaterController {
    private final HeaterDao heaterDao;
    private final RoomDao roomDao;
    private final SensorDao1 sensorDao1;

    public HeaterController(HeaterDao heaterDao, RoomDao roomDao, SensorDao1 sensorDao1) {
        this.heaterDao = heaterDao;
        this.roomDao = roomDao;
        this.sensorDao1 = sensorDao1;
    }

    @GetMapping
    public List<Heater> findAll() {
        return heaterDao.findAll()
                .stream()
                .map(HeaterMapper::of)
                .sorted(Comparator.comparing(Heater::name))
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/{id}")
    public Heater findById(@PathVariable Long id) {
        return heaterDao.findById(id).map(HeaterMapper::of).orElse(null);
    }

    @PostMapping
    public ResponseEntity<Heater> create(@RequestBody HeaterCommand heaterCommand) {
        HeaterEntity entity = new HeaterEntity(heaterCommand.name(), roomDao.findById(heaterCommand.roomId()).orElse(null), sensorDao1.findById(heaterCommand.statusId()).orElse(null));
        HeaterEntity saved = heaterDao.save(entity);
        return ResponseEntity.ok(HeaterMapper.of(saved));
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<Heater> update(@PathVariable Long id, @RequestBody HeaterCommand heaterCommand) {
        HeaterEntity entity = heaterDao.findById(id).orElse(null);
        if (entity == null) {
            return ResponseEntity.badRequest().build();
        }
        entity.setName(heaterCommand.name());
        entity.setRoom(roomDao.findById(heaterCommand.roomId()).orElse(null));
        entity.setStatus(sensorDao1.findById(heaterCommand.statusId()).orElse(null));
        return ResponseEntity.ok(HeaterMapper.of(heaterDao.save(entity)));
    }

    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable Long id) {
        heaterDao.deleteById(id);
    }
}
