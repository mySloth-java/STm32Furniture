package com.xjy.con_furniture.controller;

import com.xjy.con_furniture.model.ControlCommandRequest;
import com.xjy.con_furniture.model.StateSnapshotResponse;
import com.xjy.con_furniture.service.HomeStateService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/control")
public class ControlController {

    private final HomeStateService homeStateService;

    public ControlController(HomeStateService homeStateService) {
        this.homeStateService = homeStateService;
    }

    @PostMapping("/commands")
    public StateSnapshotResponse issue(@RequestBody ControlCommandRequest request) {
        return homeStateService.applyCommand(request);
    }
}
