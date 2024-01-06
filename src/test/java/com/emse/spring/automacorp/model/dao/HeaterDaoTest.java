package com.emse.spring.automacorp.model.dao;

import com.emse.spring.automacorp.model.entities.HeaterEntity;
import com.emse.spring.automacorp.model.entities.RoomEntity;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.stream.Collectors;

@DataJpaTest
class HeaterDaoTest {
    @Autowired
    private HeaterDao heaterDao;

    @Autowired
    private RoomDao roomDao;

    @Test
    public void shouldFindAHeaterById() {
        HeaterEntity heater = heaterDao.getReferenceById(-10L);
        Assertions.assertThat(heater.getName()).isEqualTo("Heater 1");
        Assertions.assertThat(heater.getRoom().getId()).isEqualTo(-10);
        Assertions.assertThat(heater.getStatus().getId()).isEqualTo(-6);
    }

    @Test
    public void shouldFindHeatersByRoomName() {
        List<HeaterEntity> result = heaterDao.findAllHeatersByRoomName("Room1");
        Assertions.assertThat(result)
                .hasSize(1)
                .extracting("id", "name")
                .containsExactly(
                        Tuple.tuple(-10L, "Heater 1")
                );
    }

    @Test
    public void shouldDeleteHeatersRoom() {
        RoomEntity room = roomDao.getReferenceById(-10L);
        List<Long> roomIds = room.getHeaters().stream().map(HeaterEntity::getId).collect(Collectors.toList());
        Assertions.assertThat(roomIds).hasSize(1);

        heaterDao.deleteByRoom(-10L);
        List<HeaterEntity> result = heaterDao.findAllById(roomIds);
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    public void shouldOffAllHeatersByRoom() {
        RoomEntity room = roomDao.getReferenceById(-10L);
        heaterDao.offAllHeatersByRoom(room.getId());
        List<HeaterEntity> heaterEntities = heaterDao.findAllHeatersByRoomName(room.getName());
        Assertions.assertThat(heaterEntities).allMatch(heater -> heater.getStatus().getValue() == 0.0);
    }

    @Test
    public void shouldOnAllHeatersByRoom() {
        RoomEntity room = roomDao.getReferenceById(-10L);
        heaterDao.onAllHeatersByRoom(room.getId());
        List<HeaterEntity> heaterEntities = heaterDao.findAllHeatersByRoomName(room.getName());
        Assertions.assertThat(heaterEntities).allMatch(heater -> heater.getStatus().getValue() == 1.0);
    }
}