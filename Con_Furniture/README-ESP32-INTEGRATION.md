# Con_Furniture 联调说明

当前链路已经按下面的方式打通：

`ESP32 / ESP8266` -> `Spring Boot 后端(9090)` -> `Vue 前端(5173)`

这版不接数据库，后端只保存一份内存里的最新状态和最近控制命令，前端负责实时展示。

## 后端接口

### 1. 设备上报

`POST /api/device/report`

请求体示例：

```json
{
  "deviceId": "esp32-c3-main",
  "temperature": 27.4,
  "humidity": 61.2,
  "flameAlert": false,
  "rainDetected": true,
  "motionDetected": false,
  "lastAccessCard": "CARD-1001",
  "ledOn": true,
  "doorAngle": 90,
  "rackAngle": 120
}
```

字段都不是强制必传。ESP32 上报时只传当前有的传感器值也可以。

### 2. 前端初始化读取

`GET /api/state/latest`

返回当前状态和最近控制记录。

### 3. 前端实时订阅

`GET /api/state/stream`

这是 SSE 事件流，前端已接好。

### 4. 前端控制命令

`POST /api/control/commands`

请求体示例：

```json
{
  "deviceId": "esp32-c3-main",
  "target": "door",
  "action": "set-angle",
  "value": 90,
  "source": "vue-dashboard"
}
```

当前支持：

- `target=led`，`action=on/off`
- `target=door`，`action=open/close/set-angle`
- `target=rack`，`action=extend/retract/set-angle`

说明：现在这一步只是把命令保存在后端内存并推给前端展示。后面你写 ESP32 时，可以让 ESP32 轮询命令接口，或者把后端升级成 WebSocket/MQTT。

## 启动方式

### 后端

你本机要用 JDK 17 启动。

端口：

`9090`

### 前端

前端 Vite 端口：

`5173`

代理已经指向：

`http://127.0.0.1:9090`

## ESP32-C3 示例

适合你后面主控开发。下面是 Arduino 风格的最小 HTTP 上报示例：

```cpp
#include <WiFi.h>
#include <HTTPClient.h>

const char* ssid = "YOUR_WIFI";
const char* password = "YOUR_PASSWORD";
const char* serverUrl = "http://192.168.1.100:9090/api/device/report";

void setup() {
  Serial.begin(115200);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("WiFi connected");
}

void loop() {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    http.begin(serverUrl);
    http.addHeader("Content-Type", "application/json");

    String body = "{\"deviceId\":\"esp32-c3-main\",\"temperature\":26.8,\"humidity\":57.3,\"motionDetected\":true,\"rainDetected\":false}";
    int code = http.POST(body);

    Serial.printf("HTTP code: %d\n", code);
    http.end();
  }

  delay(3000);
}
```

把 `192.168.1.100` 换成你运行 Spring Boot 那台电脑在局域网里的 IP。

## ESP8266-01S 示例

这个模块更适合做简单的 Wi-Fi 采集/转发节点。

```cpp
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>

const char* ssid = "YOUR_WIFI";
const char* password = "YOUR_PASSWORD";
const char* serverUrl = "http://192.168.1.100:9090/api/device/report";

WiFiClient client;

void setup() {
  Serial.begin(115200);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("WiFi connected");
}

void loop() {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    http.begin(client, serverUrl);
    http.addHeader("Content-Type", "application/json");

    String body = "{\"deviceId\":\"esp8266-01s-node\",\"flameAlert\":false,\"rainDetected\":true}";
    int code = http.POST(body);

    Serial.printf("HTTP code: %d\n", code);
    http.end();
  }

  delay(5000);
}
```

## 两块板子的建议分工

- `ESP32-C3`：主控板，负责多传感器采集、舵机/LED 控制、主数据上报
- `ESP8266-01S`：辅助节点，负责单一环境采集或远端无线中继

如果后面你要做得更像毕业设计成品，推荐结构是：

- `ESP32-C3` 作为主控
- `ESP8266-01S` 只做附属采集节点
- 节点数据先发给 ESP32-C3，再由 ESP32-C3 统一上报后端

这样前后端和硬件主链路会更清晰，答辩时也更容易讲。
