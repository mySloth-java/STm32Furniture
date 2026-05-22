package com.xjy.con_furniture.controller;

import com.xjy.con_furniture.model.HomeState;
import com.xjy.con_furniture.model.StateSnapshotResponse;
import com.xjy.con_furniture.service.HomeStateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/state")
public class StateController {

    private final HomeStateService homeStateService;

    public StateController(HomeStateService homeStateService) {
        this.homeStateService = homeStateService;
    }

    @GetMapping("/latest")
    public StateSnapshotResponse latest() {
        return homeStateService.snapshot();
    }

    @PostMapping("/report")
    public Map<String, Object> report(@RequestBody Map<String, Object> body) {
        System.out.println("ESP32 report = " + body);
        HomeState state = homeStateService.applyReport(body);
        return Map.of(
                "ok", true,
                "state", state
        );
    }

    @GetMapping("/stream")
    public SseEmitter stream() {
        return homeStateService.createEmitter();
    }
}
