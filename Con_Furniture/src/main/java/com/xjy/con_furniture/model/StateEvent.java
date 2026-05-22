package com.xjy.con_furniture.model;

public class StateEvent {

    private String type;
    private HomeState state;
    private ControlCommand command;

    public StateEvent() {
    }

    public StateEvent(String type, HomeState state, ControlCommand command) {
        this.type = type;
        this.state = state;
        this.command = command;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HomeState getState() {
        return state;
    }

    public void setState(HomeState state) {
        this.state = state;
    }

    public ControlCommand getCommand() {
        return command;
    }

    public void setCommand(ControlCommand command) {
        this.command = command;
    }
}
