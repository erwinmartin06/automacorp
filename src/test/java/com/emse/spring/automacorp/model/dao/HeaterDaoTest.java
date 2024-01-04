package com.emse.spring.automacorp.model.dao;

import com.emse.spring.automacorp.model.entities.HeaterEntity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class HeaterDaoTest {
    @Autowired
    private HeaterDao heaterDao;

    @Test
    public void shouldFindASensorById() {
        HeaterEntity heater = heaterDao.getReferenceById(-10L); // (3)
        Assertions.assertThat(heater.getName()).isEqualTo("Heater 1");
        Assertions.assertThat(heater.getRoom().getId()).isEqualTo(-9);
        Assertions.assertThat(heater.getStatus().getId()).isEqualTo(-6);
    }
}