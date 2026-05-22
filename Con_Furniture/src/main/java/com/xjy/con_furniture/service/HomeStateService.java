package com.xjy.con_furniture.service;

import com.xjy.con_furniture.model.ControlCommand;
import com.xjy.con_furniture.model.ControlCommandRequest;
import com.xjy.con_furniture.model.DeviceReportRequest;
import com.xjy.con_furniture.model.HomeState;
import com.xjy.con_furniture.model.StateEvent;
import com.xjy.con_furniture.model.StateSnapshotResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class HomeStateService {

    private static final int MAX_COMMANDS = 20;
    private static final long HEARTBEAT_INTERVAL_SECONDS = 15;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final AtomicLong versionCounter = new AtomicLong(1);
    private final AtomicLong commandIdCounter = new AtomicLong(1);
    private final List<ControlCommand> recentCommands = new ArrayList<>();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    private HomeState state = HomeState.defaultState();

    public HomeStateService() {
        heartbeatExecutor.scheduleAtFixedRate(this::broadcastHeartbeat,
                HEARTBEAT_INTERVAL_SECONDS,
                HEARTBEAT_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdownHeartbeat() {
        heartbeatExecutor.shutdownNow();
    }

    public synchronized StateSnapshotResponse snapshot() {
        return new StateSnapshotResponse(copyState(), copyCommands());
    }

    public synchronized HomeState applyReport(DeviceReportRequest request) {
        if (hasText(request.getDeviceId())) {
            state.setDeviceId(request.getDeviceId());
        }
        if (request.getTemperature() != null) {
            state.setTemperature(request.getTemperature());
        }
        if (request.getHumidity() != null) {
            state.setHumidity(request.getHumidity());
        }
        if (request.getFlameAlert() != null) {
            state.setFlameAlert(request.getFlameAlert());
        }
        if (request.getRainDetected() != null) {
            state.setRainDetected(request.getRainDetected());
        }
        if (request.getMotionDetected() != null) {
            state.setMotionDetected(request.getMotionDetected());
        }
        if (request.getLastAccessCard() != null) {
            state.setLastAccessCard(request.getLastAccessCard());
        }

        touchState();
        HomeState snapshot = copyState();
        broadcast("report", new StateEvent("report", snapshot, null));
        return snapshot;
    }

    public synchronized HomeState applyReport(Map<String, Object> body) {
        DeviceReportRequest request = new DeviceReportRequest();
        request.setDeviceId(asString(body.get("deviceId")));
        request.setTemperature(asDouble(body.get("temperature")));
        request.setHumidity(asDouble(body.get("humidity")));
        request.setFlameAlert(asBoolean(body.get("flameAlert")));
        request.setRainDetected(asBoolean(body.get("rainDetected")));
        request.setMotionDetected(asBoolean(body.get("motionDetected")));
        request.setLastAccessCard(asString(body.get("lastAccessCard")));
        return applyReport(request);
    }

    public synchronized StateSnapshotResponse applyCommand(ControlCommandRequest request) {
        ControlCommand command = new ControlCommand();
        command.setId(commandIdCounter.getAndIncrement());
        command.setDeviceId(hasText(request.getDeviceId()) ? request.getDeviceId() : state.getDeviceId());
        command.setTarget(defaultString(request.getTarget(), "unknown"));
        command.setAction(defaultString(request.getAction(), "noop"));
        command.setValue(request.getValue());
        command.setSource(defaultString(request.getSource(), "unknown"));
        command.setIssuedAt(Instant.now());

        mutateStateByCommand(command);
        touchState();
        addCommand(command);

        HomeState snapshot = copyState();
        broadcast("command", new StateEvent("command", snapshot, command));
        return new StateSnapshotResponse(snapshot, copyCommands());
    }

    public synchronized HomeState simulatePulse() {
        state.setTemperature(roundOneDecimal(state.getTemperature() + 0.4));
        state.setHumidity(roundOneDecimal(state.getHumidity() + 1.2));
        state.setMotionDetected(!state.isMotionDetected());
        state.setRainDetected(!state.isRainDetected());
        state.setFlameAlert(false);
        state.setLastAccessCard("CARD-" + (1000 + (int) (System.currentTimeMillis() % 9000)));
        touchState();

        HomeState snapshot = copyState();
        broadcast("report", new StateEvent("report", snapshot, null));
        return snapshot;
    }

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(ex -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("bootstrap")
                    .data(new StateEvent("bootstrap", copyState(), null)));
        } catch (IOException ex) {
            emitters.remove(emitter);
            emitter.completeWithError(ex);
        }

        return emitter;
    }

    private void broadcastHeartbeat() {
        if (emitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("heartbeat")
                        .data(Instant.now().toString()));
            } catch (IOException ex) {
                emitter.complete();
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }

    private void mutateStateByCommand(ControlCommand command) {
        state.setDeviceId(command.getDeviceId());

        if ("led".equalsIgnoreCase(command.getTarget())) {
            if ("on".equalsIgnoreCase(command.getAction())) {
                state.setLedOn(true);
            } else if ("off".equalsIgnoreCase(command.getAction())) {
                state.setLedOn(false);
            }
            return;
        }

        if ("door".equalsIgnoreCase(command.getTarget())) {
            if ("open".equalsIgnoreCase(command.getAction())) {
                state.setDoorAngle(90);
            } else if ("close".equalsIgnoreCase(command.getAction())) {
                state.setDoorAngle(0);
            } else if ("set-angle".equalsIgnoreCase(command.getAction()) && command.getValue() != null) {
                state.setDoorAngle(clampAngle(command.getValue()));
            }
            return;
        }

        if ("rack".equalsIgnoreCase(command.getTarget())) {
            if ("extend".equalsIgnoreCase(command.getAction())) {
                state.setRackAngle(120);
            } else if ("retract".equalsIgnoreCase(command.getAction())) {
                state.setRackAngle(20);
            } else if ("set-angle".equalsIgnoreCase(command.getAction()) && command.getValue() != null) {
                state.setRackAngle(clampAngle(command.getValue()));
            }
        }
    }

    private void addCommand(ControlCommand command) {
        recentCommands.add(0, command);
        if (recentCommands.size() > MAX_COMMANDS) {
            recentCommands.remove(recentCommands.size() - 1);
        }
    }

    private void touchState() {
        state.setStateVersion(versionCounter.incrementAndGet());
        state.setUpdatedAt(Instant.now());
    }

    private void broadcast(String eventName, StateEvent payload) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(payload));
            } catch (IOException ex) {
                emitter.complete();
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }

    private HomeState copyState() {
        HomeState copy = new HomeState();
        copy.setDeviceId(state.getDeviceId());
        copy.setTemperature(state.getTemperature());
        copy.setHumidity(state.getHumidity());
        copy.setFlameAlert(state.isFlameAlert());
        copy.setRainDetected(state.isRainDetected());
        copy.setMotionDetected(state.isMotionDetected());
        copy.setLastAccessCard(state.getLastAccessCard());
        copy.setLedOn(state.isLedOn());
        copy.setDoorAngle(state.getDoorAngle());
        copy.setRackAngle(state.getRackAngle());
        copy.setStateVersion(state.getStateVersion());
        copy.setUpdatedAt(state.getUpdatedAt());
        return copy;
    }

    private List<ControlCommand> copyCommands() {
        return recentCommands.stream().map(this::copyCommand).toList();
    }

    private ControlCommand copyCommand(ControlCommand source) {
        ControlCommand copy = new ControlCommand();
        copy.setId(source.getId());
        copy.setDeviceId(source.getDeviceId());
        copy.setTarget(source.getTarget());
        copy.setAction(source.getAction());
        copy.setValue(source.getValue());
        copy.setSource(source.getSource());
        copy.setIssuedAt(source.getIssuedAt());
        return copy;
    }

    private int clampAngle(int angle) {
        return Math.max(0, Math.min(180, angle));
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String defaultString(String value, String fallback) {
        return Objects.requireNonNullElse(hasText(value) ? value : null, fallback);
    }

    private String asString(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private Double asDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value).trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Boolean asBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = String.valueOf(value).trim();
        if ("true".equalsIgnoreCase(text) || "1".equals(text)) {
            return true;
        }
        if ("false".equalsIgnoreCase(text) || "0".equals(text)) {
            return false;
        }
        return null;
    }
}
