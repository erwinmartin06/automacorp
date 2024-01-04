package com.emse.spring.automacorp.model.controllers;

import com.emse.spring.automacorp.model.SensorType;
import com.emse.spring.automacorp.model.dao.RoomDao;
import com.emse.spring.automacorp.model.dao.SensorDao1;
import com.emse.spring.automacorp.model.dao.WindowDao;
import com.emse.spring.automacorp.model.entities.RoomEntity;
import com.emse.spring.automacorp.model.entities.SensorEntity;
import com.emse.spring.automacorp.model.entities.WindowEntity;
import com.emse.spring.automacorp.model.records.WindowCommand;
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

@WebMvcTest(WindowController.class)
class WindowControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WindowDao windowDao;

    @MockBean
    private SensorDao1 sensorDao1;

    @MockBean
    private RoomDao roomDao;

    SensorEntity createSensorEntity(Long id, String name) {
        SensorEntity sensorEntity = new SensorEntity(SensorType.TEMPERATURE, name);
        sensorEntity.setId(id);
        sensorEntity.setValue(24.2);
        return sensorEntity;
    }

    RoomEntity createRoomEntity(Long id, String name, SensorEntity currentTemp, int floor) {
        RoomEntity roomEntity = new RoomEntity(name, currentTemp, floor);
        roomEntity.setId(id);
        return roomEntity;
    }
    WindowEntity createWindowEntity(Long id, String name, SensorEntity windowStatus, RoomEntity room) {
        WindowEntity windowEntity = new WindowEntity(name, windowStatus, room);
        windowEntity.setId(id);
        return windowEntity;
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldFindAll() throws Exception {
        SensorEntity sensorEntity = createSensorEntity(1L, "Sensor 1");
        RoomEntity roomEntity = createRoomEntity(1L, "Room 1", sensorEntity, 3);
        WindowEntity windowEntity = createWindowEntity(1L, "Window 1", sensorEntity, roomEntity);

        SensorEntity sensorEntity2 = createSensorEntity(2L, "Sensor 2");
        RoomEntity roomEntity2 = createRoomEntity(2L, "Room 2", sensorEntity2, 4);
        WindowEntity windowEntity2 = createWindowEntity(2L, "Window 2", sensorEntity2, roomEntity2);

        Mockito.when(windowDao.findAll()).thenReturn(List.of(windowEntity, windowEntity2));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/windows").accept(MediaType.APPLICATION_JSON))
                // check the HTTP response
                .andExpect(MockMvcResultMatchers.status().isOk())
                // the content can be tested with Json path
                .andExpect(
                        MockMvcResultMatchers
                                .jsonPath("[*].name")
                                .value(Matchers.containsInAnyOrder("Window 1", "Window 2"))
                );
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldReturnNullWhenFindByUnknownId() throws Exception {
        Mockito.when(windowDao.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/windows/999").accept(MediaType.APPLICATION_JSON))
                // check the HTTP response
                .andExpect(MockMvcResultMatchers.status().isOk())
                // the content can be tested with Json path
                .andExpect(MockMvcResultMatchers.content().string(""));
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldFindById() throws Exception {
        SensorEntity sensorEntity = createSensorEntity(1L, "Sensor 1");
        RoomEntity roomEntity = createRoomEntity(1L, "Room 1", sensorEntity, 3);
        WindowEntity windowEntity = createWindowEntity(1L, "Window 1", sensorEntity, roomEntity);

        Mockito.when(windowDao.findById(999L)).thenReturn(Optional.of(windowEntity));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/windows/999").accept(MediaType.APPLICATION_JSON))
                // check the HTTP response
                .andExpect(MockMvcResultMatchers.status().isOk())
                // the content can be tested with Json path
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Window 1"));
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldNotUpdateUnknownEntity() throws Exception {
        SensorEntity sensorEntity = createSensorEntity(1L, "Sensor 1");
        RoomEntity roomEntity = createRoomEntity(1L, "Room 1", sensorEntity, 3);
        WindowEntity windowEntity = createWindowEntity(1L, "Window 1", sensorEntity, roomEntity);

        WindowCommand expectedWindow = new WindowCommand(windowEntity.getName(), windowEntity.getWindowStatus().getId(), windowEntity.getRoom().getId());
        String json = objectMapper.writeValueAsString(expectedWindow);

        Mockito.when(windowDao.findById(1L)).thenReturn(Optional.empty());
        Mockito.when(sensorDao1.findById(1L)).thenReturn(Optional.of(sensorEntity));
        Mockito.when(roomDao.findById(1L)).thenReturn(Optional.of(roomEntity));

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .put("/api/windows/1")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(csrf())
                )
                // check the HTTP response
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldUpdate() throws Exception {
        SensorEntity sensorEntity = createSensorEntity(1L, "Sensor 1");
        RoomEntity roomEntity = createRoomEntity(1L, "Room 1", sensorEntity, 3);
        WindowEntity windowEntity = createWindowEntity(1L, "Window 1", sensorEntity, roomEntity);

        WindowCommand expectedWindow = new WindowCommand(windowEntity.getName(), windowEntity.getWindowStatus().getId(), windowEntity.getRoom().getId());
        String json = objectMapper.writeValueAsString(expectedWindow);

        Mockito.when(windowDao.findById(1L)).thenReturn(Optional.of(windowEntity));
        Mockito.when(sensorDao1.findById(1L)).thenReturn(Optional.of(sensorEntity));
        Mockito.when(roomDao.findById(1L)).thenReturn(Optional.of(roomEntity));

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .put("/api/windows/1")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(csrf())
                )
                // check the HTTP response
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Window 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("1"));
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldCreate() throws Exception {
        SensorEntity sensorEntity = createSensorEntity(1L, "Sensor 1");
        RoomEntity roomEntity = createRoomEntity(1L, "Room 1", sensorEntity, 3);
        WindowEntity windowEntity = createWindowEntity(1L, "Window 1", sensorEntity, roomEntity);

        WindowCommand expectedWindow = new WindowCommand(windowEntity.getName(), windowEntity.getWindowStatus().getId(), windowEntity.getRoom().getId());
        String json = objectMapper.writeValueAsString(expectedWindow);

        Mockito.when(windowDao.existsById(1L)).thenReturn(false);
        Mockito.when(windowDao.save(Mockito.any(WindowEntity.class))).thenReturn(windowEntity);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post("/api/windows")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .with(csrf())
                )
                // check the HTTP response
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Window 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("1"));
    }

    @Test
    @WithMockUser(username = "Erwin", roles = "ADMIN")
    void shouldDelete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/windows/999").with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}