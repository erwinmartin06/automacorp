package com.emse.spring.automacorp.model.controllers;

import com.emse.spring.automacorp.model.dao.RoomDao;
import com.emse.spring.automacorp.model.dao.SensorDao1;
import com.emse.spring.automacorp.model.dao.WindowDao;
import com.emse.spring.automacorp.model.entities.WindowEntity;
import com.emse.spring.automacorp.model.mappers.WindowMapper;
import com.emse.spring.automacorp.model.records.Window;
import com.emse.spring.automacorp.model.records.WindowCommand;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/api/windows")
@Transactional
public class WindowController {
    private final WindowDao windowDao;
    private final SensorDao1 sensorDao1;
    private final RoomDao roomDao;

    public WindowController(WindowDao windowDao, SensorDao1 sensorDao1, RoomDao roomDao) {
        this.windowDao = windowDao;
        this.sensorDao1 = sensorDao1;
        this.roomDao = roomDao;
    }

    @GetMapping
    public List<Window> findAll() {
        return windowDao.findAll()
                .stream()
                .map(WindowMapper::of)
                .sorted(Comparator.comparing(Window::name))
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/custom/{roomName}")
    public List<Window> findWindowsByRoomName(@PathVariable String roomName) {
        return windowDao.findAllWindowsByRoomName(roomName)
                .stream()
                .map(WindowMapper::of)
                .sorted(Comparator.comparing(Window::name))
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/{id}")
    public Window findById(@PathVariable Long id) {
        return windowDao.findById(id).map(WindowMapper::of).orElse(null);
    }

    @PostMapping
    public ResponseEntity<Window> create(@RequestBody WindowCommand window) {
        WindowEntity windowEntity = new WindowEntity(window.name(), sensorDao1.findById(window.sensorId()).orElse(null), roomDao.findById(window.roomId()).orElse(null));
        WindowEntity saved = windowDao.save(windowEntity);
        return ResponseEntity.ok(WindowMapper.of(saved));
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<Window> update(@PathVariable Long id, @RequestBody WindowCommand window) {
        WindowEntity entity = windowDao.findById(id).orElse(null);
        if (entity == null) {
            return ResponseEntity.badRequest().build();
        }
        entity.setName(window.name());
        entity.setRoom(roomDao.findById(window.roomId()).orElse(null));
        entity.setWindowStatus(sensorDao1.findById(window.sensorId()).orElse(null));

        return ResponseEntity.ok(WindowMapper.of(entity));
    }

    @PutMapping(path = "/{id}/switch")
    public ResponseEntity<Window> switchWindowStatusById(@PathVariable Long id) {
        WindowEntity entity = windowDao.findById(id).orElse(null);
        if(entity != null && entity.getWindowStatus() != null){
            entity.getWindowStatus().setValue(entity.getWindowStatus().getValue() == 1.0 ? 0.0 : 1.0);
        }
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Window not found");
        }

        windowDao.save(entity);
        return ResponseEntity.ok(WindowMapper.of(entity));
    }

    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable Long id) {
        windowDao.deleteById(id);
    }
}
