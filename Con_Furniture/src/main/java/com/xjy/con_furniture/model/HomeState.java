package com.xjy.con_furniture.model;

import java.time.Instant;

public class HomeState {

    private String deviceId;
    private double temperature;
    private double humidity;
    private boolean flameAlert;
    private boolean rainDetected;
    private boolean motionDetected;
    private String lastAccessCard;
    private boolean ledOn;
    private int doorAngle;
    private int rackAngle;
    private long stateVersion;
    private Instant updatedAt;

    public static HomeState defaultState() {
        HomeState state = new HomeState();
        state.setDeviceId("esp32-c3-main");
        state.setTemperature(26.3);
        state.setHumidity(53.0);
        state.setFlameAlert(false);
        state.setRainDetected(false);
        state.setMotionDetected(false);
        state.setLastAccessCard("NONE");
        state.setLedOn(false);
        state.setDoorAngle(0);
        state.setRackAngle(20);
        state.setStateVersion(1);
        state.setUpdatedAt(Instant.now());
        return state;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public boolean isFlameAlert() {
        return flameAlert;
    }

    public void setFlameAlert(boolean flameAlert) {
        this.flameAlert = flameAlert;
    }

    public boolean isRainDetected() {
        return rainDetected;
    }

    public void setRainDetected(boolean rainDetected) {
        this.rainDetected = rainDetected;
    }

    public boolean isMotionDetected() {
        return motionDetected;
    }

    public void setMotionDetected(boolean motionDetected) {
        this.motionDetected = motionDetected;
    }

    public String getLastAccessCard() {
        return lastAccessCard;
    }

    public void setLastAccessCard(String lastAccessCard) {
        this.lastAccessCard = lastAccessCard;
    }

    public boolean isLedOn() {
        return ledOn;
    }

    public void setLedOn(boolean ledOn) {
        this.ledOn = ledOn;
    }

    public int getDoorAngle() {
        return doorAngle;
    }

    public void setDoorAngle(int doorAngle) {
        this.doorAngle = doorAngle;
    }

    public int getRackAngle() {
        return rackAngle;
    }

    public void setRackAngle(int rackAngle) {
        this.rackAngle = rackAngle;
    }

    public long getStateVersion() {
        return stateVersion;
    }

    public void setStateVersion(long stateVersion) {
        this.stateVersion = stateVersion;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
