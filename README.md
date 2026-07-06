# MOKO BXP-Nordic Android SDK

Native Android SDK and demo app for BXP-Nordic devices. Supports BLE scanning and advertisement parsing (Eddystone UID/URL/TLM, iBeacon, T&H, 3-Axis), connection with optional password lock, 6-slot broadcast configuration, sensor triggers, T&H / 3-Axis / storage / light-sensor data via Notify, remote LED/buzzer alarms, and Nordic DFU firmware updates.

Cross-platform reference (same protocol): [Flutter-Nordic-SDK](https://github.com/BeaconX-Pro/Flutter-Nordic-SDK.git).

---

## Requirements

| Item | Description |
|------|-------------|
| Android Studio | 3.6+ (8.x recommended) |
| minSdk | 28 |
| compileSdk | 35 |
| Device | Physical device required (emulators do not support BLE) |

---

## Project Structure

```
BXP_Nordic/
├── app/                      # Demo app (scan, connect, configure, DFU, full UI)
│   ├── activity/             # NordicMainActivity, DeviceInfoActivity, DfuActivity, ...
│   ├── fragment/             # Slot / Device / Setting tabs
│   └── utils/
│       ├── BeaconXInfoParseableImpl.java   # Advertisement parser (demo impl)
│       └── BeaconXParser.java              # UID, URL, TLM, iBeacon, T&H, Axis parser
└── mokosupport/              # BLE SDK module (primary integration dependency)
    ├── MokoSupport.java      # Connect, send commands, Notify control, event callbacks
    ├── MokoBleScanner.java   # Scanning
    ├── OrderTaskAssembler.java   # Read/write task assembly (API entry)
    └── entity/
        ├── OrderCHAR.java        # GATT characteristic mapping
        ├── ParamsKeyEnum.java    # Protocol parameter keys
        └── SlotEnum.java         # SLOT_1 ~ SLOT_6
```

Communication has three stages: **scan → connect → command exchange**. The SDK reports connection status and command results via **EventBus** (you can switch to another bus in `MokoSupport`).

---

## Integrating the SDK

### 1. Add the module

Copy `mokosupport` into your project root and add to `settings.gradle`:

```gradle
include ':app', ':mokosupport'
```

In the app module `build.gradle`:

```gradle
dependencies {
    implementation project(path: ':mokosupport')
}
```

### 2. Initialize

Initialize in `Application.onCreate()` or your first Activity:

```java
MokoSupport.getInstance().init(getApplicationContext());
```

### 3. Permissions

`mokosupport` declares base BLE permissions in its `AndroidManifest.xml`. On Android 6.0+, scanning requires **runtime location permission**; on Android 12+, also request `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT`.

```java
// Example: request location (required for scanning)
if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
            REQUEST_CODE_LOCATION);
}
```

### 4. Register EventBus

Connection status, command results, and Notify data are delivered via EventBus. Register in your Activity/Fragment:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EventBus.getDefault().register(this);
}

@Override
protected void onDestroy() {
    EventBus.getDefault().unregister(this);
    super.onDestroy();
}
```

---

## 1. Scanning for Devices

### Core classes

| Class | Description |
|-------|-------------|
| `MokoBleScanner` | Start/stop scanning |
| `MokoScanDeviceCallback` | Scan started, per-device callback, scan stopped |
| `DeviceInfoParseable` | Advertisement parser interface; demo impl: `BeaconXInfoParseableImpl` |
| `BeaconXParser` | Converts parsed frame data to UID, URL, TLM, iBeacon, T&H, 3-Axis |

The demo scans without hardware filters; filter recognized frames in the parser callback.

### Code example

```java
MokoBleScanner scanner = new MokoBleScanner(context);
BeaconXInfoParseableImpl parser = new BeaconXInfoParseableImpl();

scanner.startScanDevice(new MokoScanDeviceCallback() {
    @Override
    public void onStartScan() {
        // Clear list, refresh UI
    }

    @Override
    public void onScanDevice(DeviceInfo deviceInfo) {
        BeaconXInfo info = parser.parseDeviceInfo(deviceInfo);
        if (info == null) return;
        // info.mac / info.name / info.rssi
        // info.battery / info.lockState / info.connectState
        // info.validDataHashMap — parsed broadcast frames
    }

    @Override
    public void onStopScan() {
        // Stop animation, etc.
    }
});

// Stop scanning (call before connecting)
scanner.stopScanDevice();
```

### Advertisement frame types (`BeaconXInfoParseableImpl`)

Recognized Service Data / manufacturer data:

| Source | UUID / ID | Frame types |
|--------|-----------|-------------|
| Eddystone | `0000feaa-...` | UID, URL, TLM |
| BeaconX Pro | `0000feab-...` | INFO, iBeacon, 3-Axis, T&H |
| BeaconX Pro (alt) | `0000feac-...` | INFO |
| Apple iBeacon | Manufacturer `0x004C` (23 bytes) | iBeacon |
| OTA mode | Service `0000eaff-...` + name `MK_OTA` | DFU entry |

INFO frame (`0000feab`, type `0x40`) fields:

- `battery` — battery voltage (mV)
- `lockState` — `0` = unlocked, `2` = password required (bit mask on newer firmware)
- `connectState` — connectable flag from scan result
- `ambientLightState`, `tamperState` — optional sensor flags

Parse frame payload with `BeaconXParser`:

```java
ArrayList<BeaconXInfo.ValidData> validDatas =
        new ArrayList<>(info.validDataHashMap.values());
for (BeaconXInfo.ValidData validData : validDatas) {
    switch (validData.type) {
        case BeaconXInfo.VALID_DATA_FRAME_TYPE_UID:
            BeaconXUID uid = BeaconXParser.getUID(validData.data);
            break;
        case BeaconXInfo.VALID_DATA_FRAME_TYPE_URL:
            BeaconXURL url = BeaconXParser.getURL(validData.data);
            break;
        case BeaconXInfo.VALID_DATA_FRAME_TYPE_TLM:
            BeaconXTLM tlm = BeaconXParser.getTLM(validData.data);
            break;
        case BeaconXInfo.VALID_DATA_FRAME_TYPE_IBEACON:
            BeaconXiBeacon ibeacon = BeaconXParser.getiBeacon(info.rssi, validData.data);
            break;
        case BeaconXInfo.VALID_DATA_FRAME_TYPE_TH:
            BeaconXTH th = BeaconXParser.getTH(validData.data);
            break;
        case BeaconXInfo.VALID_DATA_FRAME_TYPE_AXIS:
            BeaconXAxis axis = BeaconXParser.getAxis(validData.data);
            break;
    }
}
```

---

## 2. Connecting to a Device

### Connect

Only the device **MAC address** is required (from scan result `info.mac`):

```java
// Stop scanning before connecting
scanner.stopScanDevice();
MokoSupport.getInstance().connDevice(mac);
```

### Connection status (EventBus)

```java
@Subscribe(threadMode = ThreadMode.MAIN)
public void onConnectStatusEvent(ConnectStatusEvent event) {
    String action = event.getAction();
    if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
        // GATT disconnected (failed connect, link lost, manual disconnect, etc.)
    }
    if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
        // Service discovery done; read lock state or unlock
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getLockState());
    }
}
```

### Password verification

After `ACTION_DISCOVER_SUCCESS`, read lock state. Response on `OrderCHAR.CHAR_LOCK_STATE`:

| Value | Meaning |
|-------|---------|
| `00` | Device locked — prompt for password, reconnect, then unlock |
| `02` | No password required — proceed to configuration UI |
| Other | Unlock succeeded — proceed to configuration UI |

Unlock flow (when locked):

```java
// Step 1: read random challenge
MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getUnLock());

// Step 2: in ACTION_ORDER_RESULT for CHAR_UNLOCK (READ), encrypt and send password
MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setUnLock(password, challengeBytes));

// Step 3: after WRITE success, verify lock state again
MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getLockState());
```

`setUnLock` uses AES encryption (16-byte padded password). See `OrderTaskAssembler.setUnLock` / `setLockState` for password change.

### Manual disconnect

```java
MokoSupport.getInstance().disConnectBle();
```

---

## 3. Reading and Writing Parameters

### Task queue

All reads/writes are wrapped as `OrderTask`, created by `OrderTaskAssembler`, and sent via `sendOrder` **in queue order**. Default timeout per task is 3 seconds.

```java
// Single task
MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getSlotType());

// Multiple tasks (executed in order)
List<OrderTask> tasks = new ArrayList<>();
tasks.add(OrderTaskAssembler.setSlot(SlotEnum.SLOT_1));
tasks.add(OrderTaskAssembler.getSlotData());
tasks.add(OrderTaskAssembler.getAdvInterval());
MokoSupport.getInstance().sendOrder(tasks.toArray(new OrderTask[]{}));
```

See `OrderTaskAssembler.java` for the full list of `getXxx` / `setXxx` methods (lock, slots, device info, triggers, sensors, alarms, etc.).

### Protocol frame format

Parameter channel (`CHAR_PARAMS`) frame layout:

```
EA [cmd] [len_hi] [len_lo] [data...]
```

| Field | Description |
|-------|-------------|
| `0xEA` | Frame header |
| `cmd` | 1 byte, maps to `ParamsKeyEnum` (read keys `0x2x`, write keys `0x3x` / `0x5x`) |
| `len` | 2-byte payload length |
| `data` | Payload; write ACK uses response from device |

GATT characteristics (non-protocol) are accessed via dedicated tasks — e.g. `getBattery()`, `getSlotType()`, `getRssi()` map to `OrderCHAR` directly. Branch on `OrderCHAR` in `ACTION_ORDER_RESULT`.

### Command results (EventBus)

```java
@Subscribe(threadMode = ThreadMode.MAIN)
public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
    String action = event.getAction();
    OrderTaskResponse response = event.getResponse();

    if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
        // Timeout; check response.orderCHAR for which task
    }
    if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
        // All queued tasks finished
    }
    if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
        OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
        byte[] value = response.responseValue;
        // Parse value ...
    }
    if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
        // Device-initiated Notify (T&H, storage, 3-Axis, light sensor, disconnect, lock)
    }
}
```

### Example 1: Read slot types

```java
MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getSlotType());

// In callback for CHAR_SLOT_TYPE: value[0..5] = SLOT1~SLOT6 frame type
```

### Example 2: Read/write slot 1 configuration

```java
List<OrderTask> tasks = new ArrayList<>();
tasks.add(OrderTaskAssembler.setSlot(SlotEnum.SLOT_1));
tasks.add(OrderTaskAssembler.getSlotData());
tasks.add(OrderTaskAssembler.getTrigger());
tasks.add(OrderTaskAssembler.getRssi());           // adv TX power
tasks.add(OrderTaskAssembler.getRadioTxPower());
tasks.add(OrderTaskAssembler.getAdvInterval());
MokoSupport.getInstance().sendOrder(tasks.toArray(new OrderTask[]{}));
```

### Example 3: Batch read device info

```java
List<OrderTask> tasks = new ArrayList<>();
tasks.add(OrderTaskAssembler.getManufacturer());     // GATT 0x2A29
tasks.add(OrderTaskAssembler.getDeviceModel());      // GATT 0x2A24
tasks.add(OrderTaskAssembler.getProductDate());      // GATT 0x2A25
tasks.add(OrderTaskAssembler.getHardwareVersion());  // GATT 0x2A27
tasks.add(OrderTaskAssembler.getFirmwareVersion());  // GATT 0x2A26
tasks.add(OrderTaskAssembler.getSoftwareVersion());  // GATT 0x2A28
tasks.add(OrderTaskAssembler.getBattery());
tasks.add(OrderTaskAssembler.getLockState());
MokoSupport.getInstance().sendOrder(tasks.toArray(new OrderTask[]{}));
```

Newer firmware may omit standard GATT characteristics; use protocol keys such as `getNewManufacturer()`, `getNewDeviceModel()`, etc. The demo detects this via presence of `CHAR_MODEL_NUMBER` after connect (`MokoSupport.isNewVersion`).

### Example 4: Sync device time

```java
MokoSupport.getInstance().sendOrder(
        OrderTaskAssembler.setDeviceTime(year, month, day, hour, minute, second));
```

### Example 5: Factory reset / power off

```java
MokoSupport.getInstance().sendOrder(OrderTaskAssembler.resetDevice());
MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setClose());
```

---

## 4. Real-Time Sensor Notify

T&H, 3-Axis, storage, and light-sensor data are pushed via Notify. Enable/disable in `MokoSupport`:

```java
MokoSupport.getInstance().enableTHNotify();
MokoSupport.getInstance().disableTHNotify();

MokoSupport.getInstance().enableThreeAxisNotify();
MokoSupport.getInstance().disableThreeAxisNotify();

MokoSupport.getInstance().enableStoreNotify();
MokoSupport.getInstance().disableStoreNotify();

MokoSupport.getInstance().enableLightSensorNotify();
MokoSupport.getInstance().disableLightSensorNotify();

MokoSupport.getInstance().enableLightSensorCurrentNotify();
MokoSupport.getInstance().disableLightSensorCurrentNotify();
```

Receive data in `ACTION_CURRENT_DATA` and branch on `OrderCHAR`:

| Notify characteristic | Data |
|----------------------|------|
| `CHAR_TH_NOTIFY` | Real-time temperature & humidity |
| `CHAR_THREE_AXIS_NOTIFY` | Real-time 3-Axis samples |
| `CHAR_STORE_NOTIFY` | Stored T&H / sensor records |
| `CHAR_LIGHT_SENSOR_NOTIFY` | Stored light-sensor records |
| `CHAR_LIGHT_SENSOR_CURRENT` | Current light level |

Demo pages: `THDataActivity`, `AxisDataActivity`, `ExportDataActivity`, `LightSensorDataActivity`.

---

## 5. Disconnect Notifications

Handle two kinds of disconnect events separately.

### 5.1 BLE link disconnect (`ConnectStatusEvent`)

Triggered when the device powers off, goes out of range, connection fails, or you call `disConnectBle()`:

```java
if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
    // Close config UI, return to scan page, restart startScanDevice
}
```

**During DFU**, suppress disconnect dialogs (demo uses `isUpgrading` / `isUpgradeDisconnected` flags in `DeviceInfoActivity`).

### 5.2 Device-initiated disconnect Notify (`CHAR_DISCONNECT`)

The device may push a byte before disconnecting; receive it in `ACTION_CURRENT_DATA`:

```java
if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
    OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
    if (orderCHAR == OrderCHAR.CHAR_DISCONNECT) {
        int type = value[0] & 0xFF;
        // 1 = password changed successfully (reconnect required)
        // 2 = factory reset successful (reconnect required)
    }
}
```

`ACTION_DISCONNECTED` usually follows. See `DeviceInfoActivity` for dialog handling.

### 5.3 Lock timeout Notify (`CHAR_LOCKED_NOTIFY`)

When the device re-locks due to inactivity, frame `EB 63 00 01 00` is pushed:

```java
if (orderCHAR == OrderCHAR.CHAR_LOCKED_NOTIFY) {
    String hex = MokoUtils.bytesToHexString(value);
    if ("eb63000100".equals(hex.toLowerCase())) {
        // Device locked — exit config page and reconnect with password
    }
}
```

---

## 6. DFU Firmware Update

The demo uses the **Nordic Android DFU Library** (transitive dependency via `MKBXPUILib`). UI entry: **Settings → DFU**, or connect to an OTA advertisement (`MK_OTA`).

Register the service in `AndroidManifest.xml`:

```xml
<service android:name="com.moko.bxp.nordic.service.DfuServiceNordic" />
```
`DfuService` extends `DfuBaseService` (see `app/.../service/DfuService.java`).


### Flow

1. Connected (normal mode) or scan OTA device (`info.isOTA == true`) and connect
2. User selects a **`.zip`** firmware package
3. Disconnect normal GATT session if needed, then start DFU with MAC
4. Show progress via `DfuProgressListener`
5. Return to scan page and reconnect

### Code example

```java
DfuServiceListenerHelper.registerProgressListener(context, mDfuProgressListener);

DfuServiceInitiator starter = new DfuServiceInitiator(deviceMac)
        .setKeepBond(false)
        .setDisableNotification(true);
starter.setZip(null, firmwareFilePath);
starter.start(context, DfuServiceNordic.class);

private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
    @Override
    public void onProgressChanged(String address, int percent, float speed,
            float avgSpeed, int currentPart, int partsTotal) {
        // Progress: percent%
    }

    @Override
    public void onDfuCompleted(String deviceAddress) {
        // Success — prompt user to scan and reconnect
    }

    @Override
    public void onError(String deviceAddress, int error, int errorType, String message) {
        // Upgrade failed
    }
};

@Override
protected void onDestroy() {
    DfuServiceListenerHelper.unregisterProgressListener(context, mDfuProgressListener);
    super.onDestroy();
}
```

Notes:

- Firmware must be a valid non-empty **ZIP** file
- Call `disConnectBle()` before upgrading when already connected in normal mode
- Abort DFU if connection retries exceed 3 times (see `DfuActivity` / `DeviceInfoActivity`)

---

## 7. Typical Flow

```
Scan page (NordicMainActivity)
  ├─ MokoBleScanner.startScanDevice
  ├─ BeaconXInfoParseableImpl → device list (UID/URL/TLM/iBeacon/T&H/Axis)
  ├─ connDevice(mac)
  ├─ getLockState → [optional] getUnLock / setUnLock
  └─ DeviceInfoActivity
       ├─ Slot tab — 6 slots, frame types, triggers (T&H / tap / move / light)
       ├─ Device tab — model, version, battery, MAC
       ├─ Setting tab — connectable, password, HW reset, DFU, power off, factory reset
       ├─ SensorConfigActivity — T&H period, 3-Axis params, storage conditions
       ├─ QuickSwitchActivity — slot quick switch
       ├─ THDataActivity / AxisDataActivity / ExportDataActivity / LightSensorDataActivity
       ├─ RemoteReminderActivity — remote LED / buzzer alarm
       ├─ SlotDataActivity — per-slot detail editing
       ├─ ACTION_CURRENT_DATA → sensor Notify / disconnect / lock timeout
       ├─ ACTION_DISCONNECTED → link lost
       └─ Settings → DFU → DfuActivity → back to scan and reconnect
```

---

## 8. Core Classes Quick Reference

| Stage | Class | Role |
|-------|-------|------|
| Scan | `MokoBleScanner` | Scan control |
| Scan | `MokoScanDeviceCallback` | Scan callbacks |
| Scan | `BeaconXInfoParseableImpl` | Parse advertisements |
| Scan | `BeaconXParser` | Frame payload parser |
| Connect | `MokoSupport` | Connect, send commands, Notify control, Bluetooth on/off |
| Comm | `OrderTaskAssembler` | Build read/write tasks |
| Comm | `ParamsKeyEnum` | Protocol parameter keys |
| Comm | `OrderCHAR` | GATT characteristic mapping |
| Event | `ConnectStatusEvent` | Connected / disconnected |
| Event | `OrderTaskResponseEvent` | Command results, Notify data |

---

## 9. Notes

1. **Permissions**: Android 6.0+ requires runtime location for scanning; Android 12+ needs `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT`.
2. **EventBus**: The SDK posts events internally. To use LiveData/RxJava instead, change `orderFinish` / `orderTimeout` / `orderResult` / `orderNotify` in `MokoSupport`.
3. **Logging**: The SDK uses `XLog` with file output and storage permission. To disable file logging, keep only `XLog.init(config)` in `BaseApplication`.
4. **Firmware variants**: Older devices expose standard GATT Device Information characteristics; newer firmware uses `ParamsKeyEnum` `GET_NEW_*` keys. Check `CHAR_MODEL_NUMBER` after connect to choose the read path (see demo `DeviceInfoActivity`).
5. **Demo references**: Scan/connect — `NordicMainActivity`; device info & tabs — `DeviceInfoActivity`, `DeviceFragment`, `SlotFragment`, `SettingFragment`; slot editing — `SlotDataActivity`; sensors — `SensorConfigActivity`, `THDataActivity`, `AxisDataActivity`, `LightSensorDataActivity`, `ExportDataActivity`; DFU — `DfuActivity`, `DeviceInfoActivity`.

---

## Changelog

| Date | Version | Notes |
|------|---------|-------|
| 2020.01.18 | mokosupport 1.0 | Initial release |
| 2021.03.11 | mokosupport 2.0 | Restructure SDK; support Android API 29; androidx; optimize docs |
| 2021.11.30 | mokosupport 3.0 | Change SDK package name; support light sensor data |
| — | mokosupport 4.0 | compileSdk 35, minSdk 28 |
