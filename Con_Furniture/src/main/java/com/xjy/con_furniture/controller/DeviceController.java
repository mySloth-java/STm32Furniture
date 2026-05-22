package com.xjy.con_furniture.controller;

import com.xjy.con_furniture.model.DeviceReportRequest;
import com.xjy.con_furniture.model.HomeState;
import com.xjy.con_furniture.service.HomeStateService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/device")
public class DeviceController {

    private final HomeStateService homeStateService;

    public DeviceController(HomeStateService homeStateService) {
        this.homeStateService = homeStateService;
    }

    @PostMapping("/report")
    public HomeState report(@RequestBody DeviceReportRequest request) {
        return homeStateService.applyReport(request);
    }

    @PostMapping("/demo/pulse")
    public HomeState demoPulse() {
        return homeStateService.simulatePulse();
    }
}
