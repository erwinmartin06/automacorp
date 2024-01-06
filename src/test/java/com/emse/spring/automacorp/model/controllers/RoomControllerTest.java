package com.emse.spring.automacorp.model.controllers;

import com.emse.spring.automacorp.model.SensorType;
import com.emse.spring.automacorp.model.dao.*;
import com.emse.spring.automacorp.model.entities.*;
import com.emse.spring.automacorp.model.records.RoomCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(RoomController.class)
class RoomControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomDao roomDao;

    @MockBean
    private SensorDao1 sensorDao1;

    @MockBean
    private WindowDao windowDao;

    @MockBean
    private HeaterDao heaterDao;

    @MockBean
    private BuildingDao buildingDao;

    SensorEntity createSensorEntity(Long id, String name) {
        SensorEntity sensorEntity = new SensorEntity(SensorType.TEMPERATURE, name);
        sensorEntity.setId(id);
        sensorEntity.setValue(24.2);
        return sensorEntity;
    }

    RoomEntity createRoomEntity(Long id, String name, SensorEntity currentTemp, int floor, double targetTemp, List<WindowEntity> windows, List<HeaterEntity> heaters, BuildingEntity buildingEntity) {
        RoomEntity roomEntity = new RoomEntity(name, currentTemp, floor);
        roomEntity.setId(id);
        roomEntity.setTargetTemp(targetTemp);
        roomEntity.setWindows(windows);
        roomEntity.setHeaters(heaters);
        buildingEntity.setId(1L);
        roomEntity.setBuilding(buildingEntity);
        return roomEntity;
    }

    WindowEntity createWindowEntity(Long id, String name, SensorEntity windowStatus, RoomEntity room) {
        WindowEntity windowEntity = new WindowEntity(name, windowStatus, room);
        windowEntity.setId(id);
        return windowEntity;
    }

    HeaterEntity createHeaterEntity(Long id, String name, RoomEntity room, SensorEntity status) {
        HeaterEntity heaterEntity = new HeaterEntity(name, room, status);
        heaterEntity.setId(id);
        return heaterEntity;
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldFindAll() throws Exception {
        SensorEntity sensor1 = createSensorEntity(1L, "Sensor 1");
        SensorEntity sensor2 = createSensorEntity(2L, "Sensor 2");

        RoomEntity room1 = createRoomEntity(1L, "Room 1", sensor1, 3, 21.0, List.of(), List.of(), new BuildingEntity());
        RoomEntity room2 = createRoomEntity(2L, "Room 2", sensor2, 4, 22.0, List.of(), List.of(), new BuildingEntity());

        Mockito.when(roomDao.findAll()).thenReturn(List.of(room1, room2));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rooms").accept(MediaType.APPLICATION_JSON))
                // check the HTTP response
                .andExpect(MockMvcResultMatchers.status().isOk())
                // the content can be tested with Json path
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("[*].name")
                                .value(Matchers.containsInAnyOrder("Room 1", "Room 2"))
                );
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldReturnNullWhenFindByUnknownId() throws Exception {
        Mockito.when(roomDao.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rooms/999").accept(MediaType.APPLICATION_JSON))
                // check the HTTP response
                .andExpect(MockMvcResultMatchers.status().isOk())
                // the content can be tested with Json path
                .andExpect(MockMvcResultMatchers.content().string(""));
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldFindById() throws Exception {
        SensorEntity sensor = createSensorEntity(1L, "Sensor 1");
        RoomEntity room = createRoomEntity(1L, "Room 1", sensor, 3, 21.0, List.of(), List.of(), new BuildingEntity());

        Mockito.when(roomDao.findById(1L)).thenReturn(Optional.of(room));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/rooms/1").accept(MediaType.APPLICATION_JSON))
                // check the HTTP response
                .andExpect(MockMvcResultMatchers.status().isOk())
                // the content can be tested with Json path
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Room 1"));
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldCreate() throws Exception {
        SensorEntity sensor = createSensorEntity(1L, "Sensor 1");
        RoomEntity room = createRoomEntity(1L, "Room 1", sensor, 3, 21.0, List.of(), List.of(), new BuildingEntity());
        RoomCommand expectedRoom = new RoomCommand(room.getName(), sensor.getId(), room.getTargetTemp(), room.getFloor(), room.getBuilding().getId());

        String json = objectMapper.writeValueAsString(expectedRoom);

        Mockito.when(sensorDao1.findById(sensor.getId())).thenReturn(Optional.of(sensor));
        Mockito.when(roomDao.save(Mockito.any(RoomEntity.class))).thenReturn(room);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post("/api/rooms")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(csrf())
                )
                // check the HTTP response
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Room 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.targetTemp").value("21.0"));
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldUpdate() throws Exception {
        SensorEntity sensor = createSensorEntity(1L, "Sensor 1");
        BuildingEntity building = new BuildingEntity();
        RoomEntity room = createRoomEntity(1L, "Room 1", sensor, 3, 21.0, List.of(), List.of(), building);
        RoomCommand expectedRoom = new RoomCommand(room.getName(), sensor.getId(), room.getTargetTemp(), room.getFloor(), room.getBuilding().getId());

        String json = objectMapper.writeValueAsString(expectedRoom);

        Mockito.when(roomDao.findById(room.getId())).thenReturn(Optional.of(room));
        Mockito.when(sensorDao1.findById(sensor.getId())).thenReturn(Optional.of(sensor));
        Mockito.when(buildingDao.findById(building.getId())).thenReturn(Optional.of(building));
        Mockito.when(roomDao.save(Mockito.any(RoomEntity.class))).thenReturn(room);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .put("/api/rooms/1")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(csrf())
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Room 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.targetTemp").value("21.0"));
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldDeleteRoomAndAssociatedEntities() throws Exception {
        SensorEntity sensor = createSensorEntity(1L, "Sensor 1");
        List<WindowEntity> windows = List.of(
                createWindowEntity(1L, "Window 1", sensor, new RoomEntity()),
                createWindowEntity(2L, "Window 2", sensor, new RoomEntity())
        );
        List<HeaterEntity> heaters = List.of(
                createHeaterEntity(1L, "Heater 1", new RoomEntity(), new SensorEntity()),
                createHeaterEntity(2L, "Heater 2", new RoomEntity(), new SensorEntity())
        );
        RoomEntity room = createRoomEntity(1L, "Room 1", sensor, 3, 22.5, windows, heaters, new BuildingEntity());

        Mockito.when(roomDao.findById(1L)).thenReturn(Optional.of(room));

        Mockito.doNothing().when(windowDao).deleteById(Mockito.any());
        Mockito.doNothing().when(heaterDao).deleteById(Mockito.any());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/rooms/1").with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(windowDao).deleteByRoom(1L);
        Mockito.verify(heaterDao).deleteByRoom(1L);
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldOpenWindows() throws Exception {
        SensorEntity sensorEntity = new SensorEntity(SensorType.STATUS, "Sensor 1");
        sensorEntity.setValue(0.0);
        sensorEntity.setId(1L);

        RoomEntity room = new RoomEntity();
        BuildingEntity buildingEntity = new BuildingEntity();

        WindowEntity window1 = createWindowEntity(1L, "Window 1", sensorEntity, room);
        WindowEntity window2 = createWindowEntity(2L, "Window 2", sensorEntity, room);

        room.setWindows(List.of(window1, window2));
        room.setCurrentTemp(sensorEntity);
        room.setBuilding(buildingEntity);

        Mockito.when(roomDao.findById(1L)).thenReturn(Optional.of(room));
        Mockito.when(sensorDao1.findById(sensorEntity.getId())).thenReturn(Optional.of(sensorEntity));
        Mockito.when(windowDao.findById(window1.getId())).thenReturn(Optional.of(window1));
        Mockito.when(windowDao.findById(window2.getId())).thenReturn(Optional.of(window2));
        Mockito.when(buildingDao.findById(buildingEntity.getId())).thenReturn(Optional.of(buildingEntity));
        Mockito.when(roomDao.save(Mockito.any(RoomEntity.class))).thenReturn(room);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/rooms/1/openWindows").with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(windowDao).openAllWindowsByRoom(1L);
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldCloseWindows() throws Exception {
        SensorEntity sensorEntity = new SensorEntity(SensorType.STATUS, "Sensor 1");
        sensorEntity.setValue(0.0);
        sensorEntity.setId(1L);

        RoomEntity room = new RoomEntity();
        BuildingEntity buildingEntity = new BuildingEntity();

        WindowEntity window1 = createWindowEntity(1L, "Window 1", sensorEntity, room);
        WindowEntity window2 = createWindowEntity(2L, "Window 2", sensorEntity, room);

        room.setWindows(List.of(window1, window2));
        room.setCurrentTemp(sensorEntity);
        room.setBuilding(buildingEntity);

        Mockito.when(roomDao.findById(1L)).thenReturn(Optional.of(room));
        Mockito.when(sensorDao1.findById(sensorEntity.getId())).thenReturn(Optional.of(sensorEntity));
        Mockito.when(windowDao.findById(window1.getId())).thenReturn(Optional.of(window1));
        Mockito.when(windowDao.findById(window2.getId())).thenReturn(Optional.of(window2));
        Mockito.when(buildingDao.findById(buildingEntity.getId())).thenReturn(Optional.of(buildingEntity));
        Mockito.when(roomDao.save(Mockito.any(RoomEntity.class))).thenReturn(room);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/rooms/1/closeWindows").with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(windowDao).closeAllWindowsByRoom(1L);
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldTurnOnHeaters() throws Exception {
        SensorEntity sensorEntity = new SensorEntity(SensorType.STATUS, "Sensor 1");
        sensorEntity.setValue(0.0);
        sensorEntity.setId(1L);

        RoomEntity room = new RoomEntity();
        BuildingEntity buildingEntity = new BuildingEntity();

        HeaterEntity heater1 = createHeaterEntity(1L, "Heater 1", room, sensorEntity);
        HeaterEntity heater2 = createHeaterEntity(2L, "Heater 2", room, sensorEntity);

        room.setHeaters(List.of(heater1, heater2));
        room.setCurrentTemp(sensorEntity);
        room.setBuilding(buildingEntity);

        Mockito.when(roomDao.findById(1L)).thenReturn(Optional.of(room));
        Mockito.when(sensorDao1.findById(sensorEntity.getId())).thenReturn(Optional.of(sensorEntity));
        Mockito.when(heaterDao.findById(heater1.getId())).thenReturn(Optional.of(heater1));
        Mockito.when(heaterDao.findById(heater2.getId())).thenReturn(Optional.of(heater2));
        Mockito.when(buildingDao.findById(buildingEntity.getId())).thenReturn(Optional.of(buildingEntity));
        Mockito.when(roomDao.save(Mockito.any(RoomEntity.class))).thenReturn(room);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/rooms/1/onHeaters").with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(heaterDao).onAllHeatersByRoom(1L);
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldTurnOffHeaters() throws Exception {
        SensorEntity sensorEntity = new SensorEntity(SensorType.STATUS, "Sensor 1");
        sensorEntity.setValue(0.0);
        sensorEntity.setId(1L);

        RoomEntity room = new RoomEntity();
        BuildingEntity buildingEntity = new BuildingEntity();

        HeaterEntity heater1 = createHeaterEntity(1L, "Heater 1", room, sensorEntity);
        HeaterEntity heater2 = createHeaterEntity(2L, "Heater 2", room, sensorEntity);

        room.setHeaters(List.of(heater1, heater2));
        room.setCurrentTemp(sensorEntity);
        room.setBuilding(buildingEntity);

        Mockito.when(roomDao.findById(1L)).thenReturn(Optional.of(room));
        Mockito.when(sensorDao1.findById(sensorEntity.getId())).thenReturn(Optional.of(sensorEntity));
        Mockito.when(heaterDao.findById(heater1.getId())).thenReturn(Optional.of(heater1));
        Mockito.when(heaterDao.findById(heater2.getId())).thenReturn(Optional.of(heater2));
        Mockito.when(buildingDao.findById(buildingEntity.getId())).thenReturn(Optional.of(buildingEntity));
        Mockito.when(roomDao.save(Mockito.any(RoomEntity.class))).thenReturn(room);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/rooms/1/offHeaters").with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(heaterDao).offAllHeatersByRoom(1L);
    }
}
