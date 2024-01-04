package com.emse.spring.automacorp.model.controllers;

import com.emse.spring.automacorp.model.SensorType;
import com.emse.spring.automacorp.model.dao.*;
import com.emse.spring.automacorp.model.entities.*;
import com.emse.spring.automacorp.model.records.HeaterCommand;
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

@WebMvcTest(HeaterController.class)
class HeaterControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HeaterDao heaterDao;

    @MockBean
    private RoomDao roomDao;

    @MockBean
    private SensorDao1 sensorDao1;

    HeaterEntity createHeaterEntity(Long id, String name, RoomEntity room, SensorEntity status) {
        HeaterEntity heater = new HeaterEntity(name, room, status);
        heater.setId(id);
        return heater;
    }

    SensorEntity createSensorEntity() {
        SensorEntity sensor = new SensorEntity(SensorType.TEMPERATURE, "Sensor 1");
        sensor.setId(1L);
        sensor.setValue(24.2);
        return sensor;
    }

    RoomEntity createRoomEntity(Long id, String name, SensorEntity currentTemp, int floor, double targetTemp, List<WindowEntity> windows, List<HeaterEntity> heaters, BuildingEntity building) {
        RoomEntity roomEntity = new RoomEntity(name, currentTemp, floor);
        roomEntity.setId(id);
        roomEntity.setTargetTemp(targetTemp);
        roomEntity.setWindows(windows);
        roomEntity.setHeaters(heaters);
        roomEntity.setBuilding(building);
        return roomEntity;
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldFindAll() throws Exception {
        SensorEntity sensor1 = createSensorEntity();
        RoomEntity room1 = createRoomEntity(1L, "Room 1", sensor1, 3, 21.0, List.of(), List.of(), null);

        HeaterEntity heater1 = createHeaterEntity(1L, "Heater 1", room1, sensor1);
        HeaterEntity heater2 = createHeaterEntity(2L, "Heater 2", room1, sensor1);

        Mockito.when(heaterDao.findAll()).thenReturn(List.of(heater1, heater2));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/heaters").accept(MediaType.APPLICATION_JSON))
                // check the HTTP response
                .andExpect(MockMvcResultMatchers.status().isOk())
                // the content can be tested with Json path
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].id").value(Matchers.containsInAnyOrder(1, 2)));
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldReturnNullWhenFindByUnknownId() throws Exception {
        Mockito.when(heaterDao.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/heaters/999").accept(MediaType.APPLICATION_JSON))
                // check the HTTP response
                .andExpect(MockMvcResultMatchers.status().isOk())
                // the content can be tested with Json path
                .andExpect(MockMvcResultMatchers.content().string(""));
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldFindById() throws Exception {
        SensorEntity sensor1 = createSensorEntity();
        RoomEntity room1 = createRoomEntity(1L, "Room 1", sensor1, 3, 21.0, List.of(), List.of(), null);
        HeaterEntity heater = createHeaterEntity(1L, "Heater 1", room1, sensor1);

        Mockito.when(heaterDao.findById(1L)).thenReturn(Optional.of(heater));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/heaters/1").accept(MediaType.APPLICATION_JSON))
                // check the HTTP response
                .andExpect(MockMvcResultMatchers.status().isOk())
                // the content can be tested with Json path
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldNotUpdateUnknownEntity() throws Exception {
        HeaterEntity heaterEntity = createHeaterEntity(1L, "Heater 1", new RoomEntity(), new SensorEntity());
        HeaterCommand expectedHeater = new HeaterCommand(heaterEntity.getName(), heaterEntity.getRoom().getId(), heaterEntity.getStatus().getId());
        String json = objectMapper.writeValueAsString(expectedHeater);

        Mockito.when(heaterDao.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .put("/api/heaters/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                                .with(csrf())
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldUpdate() throws Exception {
        SensorEntity sensor1 = createSensorEntity();
        RoomEntity room = createRoomEntity(1L, "Room 1", sensor1, 3, 21.0, List.of(), List.of(), null);

        room.setId(1L);
        HeaterEntity heater = createHeaterEntity(1L, "Heater 1", room, sensor1);

        HeaterCommand expectedHeater = new HeaterCommand(heater.getName(), heater.getRoom().getId(), heater.getStatus().getId());
        String json = objectMapper.writeValueAsString(expectedHeater);

        Mockito.when(heaterDao.findById(1L)).thenReturn(Optional.of(heater));
        Mockito.when(roomDao.findById(1L)).thenReturn(Optional.of(room));
        Mockito.when(sensorDao1.findById(1L)).thenReturn(Optional.of(sensor1));
        Mockito.when(heaterDao.save(Mockito.any(HeaterEntity.class))).thenReturn(heater);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .put("/api/heaters/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                                .with(csrf())
                )
                // check the HTTP response
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Heater 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("1"));
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldCreate() throws Exception {
        SensorEntity sensor = createSensorEntity();
        RoomEntity room = createRoomEntity(2L, "Room 2", sensor, 1, 23.0, List.of(), List.of(), new BuildingEntity());
        HeaterEntity heater = createHeaterEntity(1L, "Heater 1", room, sensor);

        HeaterCommand expectedHeater = new HeaterCommand(heater.getName(), heater.getRoom().getId(), heater.getStatus().getId());
        String json = objectMapper.writeValueAsString(expectedHeater);

        Mockito.when(heaterDao.existsById(1L)).thenReturn(false);
        Mockito.when(heaterDao.save(Mockito.any(HeaterEntity.class))).thenReturn(heater);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post("/api/heaters")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                                .with(csrf())
                )
                // check the HTTP response
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Heater 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("1"));
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldDelete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/heaters/999").with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
