package com.xjy.con_furniture.model;

import java.util.List;

public class StateSnapshotResponse {

    private HomeState state;
    private List<ControlCommand> recentCommands;

    public StateSnapshotResponse() {
    }

    public StateSnapshotResponse(HomeState state, List<ControlCommand> recentCommands) {
        this.state = state;
        this.recentCommands = recentCommands;
    }

    public HomeState getState() {
        return state;
    }

    public void setState(HomeState state) {
        this.state = state;
    }

    public List<ControlCommand> getRecentCommands() {
        return recentCommands;
    }

    public void setRecentCommands(List<ControlCommand> recentCommands) {
        this.recentCommands = recentCommands;
    }
}
