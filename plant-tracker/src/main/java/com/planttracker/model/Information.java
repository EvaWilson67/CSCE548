package com.planttracker.model;

// Information.java
public class Information {
    private int plantId;
    private boolean fromAnotherPlant;
    private String soilType;
    private String potSize;
    private boolean waterGlobeRequired;

    public Information() {}

    public Information(int plantId, boolean fromAnotherPlant, String soilType, String potSize, boolean waterGlobeRequired) {
        this.plantId = plantId;
        this.fromAnotherPlant = fromAnotherPlant;
        this.soilType = soilType;
        this.potSize = potSize;
        this.waterGlobeRequired = waterGlobeRequired;
    }

    public int getPlantId() { return plantId; }
    public void setPlantId(int plantId) { this.plantId = plantId; }

    public boolean isFromAnotherPlant() { return fromAnotherPlant; }
    public void setFromAnotherPlant(boolean fromAnotherPlant) { this.fromAnotherPlant = fromAnotherPlant; }

    public String getSoilType() { return soilType; }
    public void setSoilType(String soilType) { this.soilType = soilType; }

    public String getPotSize() { return potSize; }
    public void setPotSize(String potSize) { this.potSize = potSize; }

    public boolean isWaterGlobeRequired() { return waterGlobeRequired; }
    public void setWaterGlobeRequired(boolean waterGlobeRequired) { this.waterGlobeRequired = waterGlobeRequired; }

    @Override
    public String toString() {
        return "Information{" +
                "plantId=" + plantId +
                ", fromAnotherPlant=" + fromAnotherPlant +
                ", soilType='" + soilType + '\'' +
                ", potSize='" + potSize + '\'' +
                ", waterGlobeRequired=" + waterGlobeRequired +
                '}';
    }
}
