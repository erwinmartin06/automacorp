package com.emse.spring.automacorp.model.entities;
import com.emse.spring.automacorp.model.SensorType;
import jakarta.persistence.*;

@Entity
@Table(name = "SP_SENSOR")
public class SensorEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable=false, length=255)
    private String name;

    @Column(name = "sensor_value")
    private Double value;

    @Column(name = "sensor_type")
    @Enumerated(EnumType.STRING)
    private SensorType sensorType;

    public SensorEntity() {
    }

    public SensorEntity(SensorType sensorType, String name) {
        this.name = name;
        this.sensorType = sensorType;
    }

    public Long getId() { // (10).
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }
}