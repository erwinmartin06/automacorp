package com.emse.spring.automacorp.model.entities;
import com.emse.spring.automacorp.model.SensorType;
import jakarta.persistence.*;

@Entity // (1).
@Table(name = "SP_SENSOR") // (2).
public class SensorEntity {
    @Id // (3).
    @GeneratedValue
    private Long id;

    @Column(nullable=false, length=255)  // (4).
    private String name;

    @Column(name = "sensor_value") // (5)
    private Double value;

    @Column(name = "sensor_type") // (5).
    @Enumerated(EnumType.STRING) // (6).
    private SensorType sensorType;

    public SensorEntity() { // (8).
    }

    public SensorEntity(SensorType sensorType, String name) { // (9).
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