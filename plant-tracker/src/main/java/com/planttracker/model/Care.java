package com.planttracker.model;

// Care.java
import java.time.LocalDate;

public class Care {
    private int plantId;
    private LocalDate lastSoilChange;
    private LocalDate lastWatering;

    public Care() {}

    public Care(int plantId, LocalDate lastSoilChange, LocalDate lastWatering) {
        this.plantId = plantId;
        this.lastSoilChange = lastSoilChange;
        this.lastWatering = lastWatering;
    }

    public int getPlantId() { return plantId; }
    public void setPlantId(int plantId) { this.plantId = plantId; }

    public LocalDate getLastSoilChange() { return lastSoilChange; }
    public void setLastSoilChange(LocalDate lastSoilChange) { this.lastSoilChange = lastSoilChange; }

    public LocalDate getLastWatering() { return lastWatering; }
    public void setLastWatering(LocalDate lastWatering) { this.lastWatering = lastWatering; }

    @Override
    public String toString() {
        return "Care{" +
                "plantId=" + plantId +
                ", lastSoilChange=" + lastSoilChange +
                ", lastWatering=" + lastWatering +
                '}';
    }
}
