package com.xjy.con_furniture.model;

public class DeviceReportRequest {

    private String deviceId;
    private Double temperature;
    private Double humidity;
    private Boolean flameAlert;
    private Boolean rainDetected;
    private Boolean motionDetected;
    private String lastAccessCard;
    private Boolean ledOn;
    private Integer doorAngle;
    private Integer rackAngle;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Boolean getFlameAlert() {
        return flameAlert;
    }

    public void setFlameAlert(Boolean flameAlert) {
        this.flameAlert = flameAlert;
    }

    public Boolean getRainDetected() {
        return rainDetected;
    }

    public void setRainDetected(Boolean rainDetected) {
        this.rainDetected = rainDetected;
    }

    public Boolean getMotionDetected() {
        return motionDetected;
    }

    public void setMotionDetected(Boolean motionDetected) {
        this.motionDetected = motionDetected;
    }

    public String getLastAccessCard() {
        return lastAccessCard;
    }

    public void setLastAccessCard(String lastAccessCard) {
        this.lastAccessCard = lastAccessCard;
    }

    public Boolean getLedOn() {
        return ledOn;
    }

    public void setLedOn(Boolean ledOn) {
        this.ledOn = ledOn;
    }

    public Integer getDoorAngle() {
        return doorAngle;
    }

    public void setDoorAngle(Integer doorAngle) {
        this.doorAngle = doorAngle;
    }

    public Integer getRackAngle() {
        return rackAngle;
    }

    public void setRackAngle(Integer rackAngle) {
        this.rackAngle = rackAngle;
    }
}
