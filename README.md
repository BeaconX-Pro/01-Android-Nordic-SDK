# BeaconXPro Android SDK Guide（English）

## Intro

Please read the part of this document which you need.

* We will explain the important classes in the SDK.

* will help developers to get started.

* will explain notes in your developing progress.


## Design instructions

We divide the communications between SDK and devices into three stages: Scanning stage, Connection stage, Communication stage. For ease of understanding, let's take a look at the related classes and the relationships between them.

### 1.Scanning stage

**`com.moko.support.nordic.MokoBleScanner`**

Scanning processing class, support to open scan, close scan and get the raw data of the scanned device.

**`com.moko.support.nordic.callback.MokoScanDeviceCallback`**

Scanning callback interface,this interface can be used to obtain the scan status and device data.

**`com.moko.support.nordic.service.DeviceInfoParseable`**

Parsed data interface,this interface can parsed the device broadcast frame, get the specific data. the implementation can refer to `BeaconXInfoParseableImpl` in the project,the `DeviceInfo` will be parsed to `BeaconXInfo`.

**`com.moko.bxp.nordic.utils.BeaconXParser`**

Parsed data utils class, use this class to convert `BeaconXInfo.ValidData` to UID, URL, TLM, IBeacon, T&H, 3-Axis.

### 2.Connection stage

**`com.moko.support.nordic.MokoSupport`**

BLE operation core class, extends from `Mokoblelib`.It can connect the device, disconnect the device, send the device connection status, turn on Bluetooth, turn off Bluetooth, judge whether Bluetooth is on or not, receive data from the device and send data to the device, notify the page data update, turn on and off characteristic notification.

### 3.Communication stage

**`com.moko.support.nordic.OrderTaskAssembler`**

We assemble read data and write data to `OrderTask`, send the task to the device through `MokoSupport`, and receive the resopnse.

**`com.moko.ble.lib.event.ConnectStatusEvent`**

The connection status is notified by `EventBus`, the device connection status and disconnection status are obtained from this event.

**`com.moko.ble.lib.event.OrderTaskResponseEvent`**

The response is notified by `EventBus`, we can get result when we send task to device from this event,distinguish between function via `OrderTaskResponse`.

## Get Started

### Prepare

**Development environment:**

* Android Studio 3.6.+

* minSdkVersion 18

**Import to Project**

Copy the module mokosupport into the project root directory and add dependencies in build.gradle. As shown below:

```
dependencies {
    ...
    implementation project(path: ':mokosupport')
}
```

add mokosupport in settings.gradle.As shown below:

```
include ':app', ':mokosupport'
```

### Start Developing

**Initialize**

First of all, you should initialize the MokoSupport.We recommend putting it in Application.

```
MokoSupport.getInstance().init(getApplicationContext());
```

**Scan devices**

Before operating the Bluetooth scanning device, we need to apply for permission, which we have added in mokosupport `AndroidManifest.xml`

```
...
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-feature
    android:name="android.hardware.bluetooth_le"
    android:required="true" />
...
```

Start scanning task to find devices around you, then you can get their advertisement content, connect to device and change parameters.

```
MokoBleScanner mokoBleScanner = new MokoBleScanner(this);
mokoBleScanner.startScanDevice(new MokoScanDeviceCallback() {
    @Override
    public void onStartScan() {
    }

    @Override
    public void onScanDevice(DeviceInfo device) {
    }

    @Override
    public void onStopScan() {
    }
});
```

at the sometime, you can stop the scanning task in this way:

```
mokoBleScanner.stopScanDevice();
```

You can use BeaconXInfoParseImpl and BeaconXParser to parsed advertisement data to the frame data, such as iBeacon, URL, UID and etc...

```
BeaconXInfoParseableImpl beaconXInfoParseable = new BeaconXInfoParseableImpl();
BeaconXInfo beaconXInfo = beaconXInfoParseable.parseDeviceInfo(deviceInfo);
ArrayList<BeaconXInfo.ValidData> validDatas = new ArrayList<>(beaconXInfo.validDataHashMap.values());
for (BeaconXInfo.ValidData validData : validDatas) {
    if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_UID) {
        BeaconXUID beaconXUID = BeaconXParser.getUID(validData.data);
    }
    if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_URL) {
        BeaconXURL beaconXURL = BeaconXParser.getURL(validData.data);
    }
    if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_TLM) {
        BeaconXTLM beaconXTLM = BeaconXParser.getTLM(validData.data);
    }
    if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_IBEACON) {
        BeaconXiBeacon beaconXiBeacon = BeaconXParser.getiBeacon(beaconXInfo.rssi, validData.data);
    }
    if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_TH) {
        BeaconXTH beaconXTH = BeaconXParser.getTH(validData.data);
    }
    if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_AXIS) {
        BeaconXAxis beaconXAxis = BeaconXParser.getAxis(validData.data);
    }
}
```

**Connect to devices**

Connect to the device in order to do more operations(change parameter, OTA),the only parameter required is the MAC address.

```
MokoSupport.getInstance().connDevice(beaconXInfo.mac);
```

You can get the connection status through `ConnectStatusEvent`,remember to register `EventBus`

```
@Subscribe(threadMode = ThreadMode.MAIN)
public void onConnectStatusEvent(ConnectStatusEvent event) {
    String action = event.getAction();
    if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
    // connect failed
    ...
    }
    if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
    // connect success
    ...
    }
}
```

You will find that when connect to device password may need, so ,we need to read the lock state of the device first.

```
MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getLockState());

```

You can get the response result from device through `OrderTaskResponseEvent`,

```
@Subscribe(threadMode = ThreadMode.MAIN)
public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
    final String action = event.getAction();
    if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
    // the task timout
    }
    if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
    // finish all task
    }
    if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
    // get the task result
        OrderTaskResponse response = event.getResponse();
        OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
        int responseType = response.responseType;
        byte[] value = response.responseValue;
        ...
    }
    if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
    // notify data
    }
}
```

> `ACTION_ORDER_RESULT`
>
> After the task is sent to the device, the data returned by the device can be obtained by using the `OrderTaskResponse`, and you can determine which task is being returned as a resultis according to the `response.orderCHAR`. The `response.responseValue` is the returned data.

> `ACTION_ORDER_TIMEOUT`
>
> Every task has a default timeout of 3 seconds to prevent the device from failing to return data due to a fault and the fail will cause other tasks in the queue can not execute normally. You can determine which task is being returned as a resultis according to the `response.orderCHAR` function and then the next task continues.

> `ACTION_ORDER_FINISH`
>
> When the task in the queue is empty, `onOrderFinish` will be called back.

> `ACTION_CURRENT_DATA`
>
> The data from device notify.

**Communication with the device**

All the read data and write data is encapsulated into `OrderTask` in `OrderTaskAssembler`, and sent to the device in a **QUEUE** way.
SDK gets task status from task callback `OrderTaskResponse` after sending tasks successfully.

For example, if you want to get the type of each Slot, please refer to the code example below.

```
// read Slot type
MokoSupport.getInstance().sendOrder(derTaskAssembler.getSlotType());
...
// get result
@Subscribe(threadMode = ThreadMode.MAIN)
public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
    final String action = event.getAction();
    if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
        OrderTaskResponse response = event.getResponse();
        OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
        int responseType = response.responseType;
        byte[] value = response.responseValue;
        switch (orderCHAR) {
	        case CHAR_SLOT_TYPE:
	            if (value.length >= 6) {
	                // value[0]:SLOT1 type;
	                // value[1]:SLOT2 type;
	                // value[2]:SLOT3 type;
	                // value[3]:SLOT4 type;
	                // value[4]:SLOT5 type;
	                // value[5]:SLOT6 type;
	            }
	            break;
    	 }
    }
}
// read data of Slot1
ArrayList<OrderTask> orderTasks = new ArrayList<>();
// change slot
orderTasks.add(OrderTaskAssembler.setSlot(SlotEnum.SLOT_1));
orderTasks.add(OrderTaskAssembler.getSlotData());
orderTasks.add(OrderTaskAssembler.getTrigger());
orderTasks.add(OrderTaskAssembler.getAdvTxPower());
orderTasks.add(OrderTaskAssembler.getRadioTxPower());
orderTasks.add(OrderTaskAssembler.getAdvInterval());
MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));

```
How to parse the returned results, please refer to the code of the sample project and documentation.

The current data of T&H, 3-Axes,storage and light sensor are sent to APP by notification. you need to turn on and off the notification function of characteristic

```
MokoSupport.getInstance().enableTHNotify();
MokoSupport.getInstance().disableTHNotify();
MokoSupport.getInstance().enableStoreNotify();
MokoSupport.getInstance().disableStoreNotify();
MokoSupport.getInstance().enableThreeAxisNotify();
MokoSupport.getInstance().disableThreeAxisNotify();
MokoSupport.getInstance().enableLightSensorNotify();
MokoSupport.getInstance().disableLightSensorNotify();
```

**OTA**

We used the Nordic DFU for the OTA,dependencies have been added to build.gradle.

```
dependencies {
    api 'no.nordicsemi.android:dfu:0.6.2'
}
```

The OTA requires three important parameters:the path of firmware file,the adv name of device and the mac address of device.You can use it like this:

```
DfuServiceInitiator starter = new DfuServiceInitiator(deviceMac)
    .setDeviceName(deviceName)
    .setKeepBond(false)
    .setDisableNotification(true);
starter.setZip(null, firmwareFilePath);
starter.start(this, DfuService.class);
```
you can get progress of OTA through `DfuProgressListener`,the examples can be referred to demo project.

At the end of this part, you can refer all code above to develop. If there is something new, we will update this document.

## Notes

1.In Android-6.0 or later, Bluetooth scanning requires dynamic application for location permissions, as follows:

```
if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
!= PackageManager.PERMISSION_GRANTED) {
ActivityCompat.requestPermissions(this,
                                  new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
} 
```

2.`EventBus` is used in the SDK and can be modified in `MokoSupport` if you want to use other communication methods.

```
@Override
public void orderFinish() {
    OrderTaskResponseEvent event = new OrderTaskResponseEvent();
    event.setAction(MokoConstants.ACTION_ORDER_FINISH);
    EventBus.getDefault().post(event);
}

@Override
public void orderTimeout(OrderTaskResponse response) {
    OrderTaskResponseEvent event = new OrderTaskResponseEvent();
    event.setAction(MokoConstants.ACTION_ORDER_TIMEOUT);
    event.setResponse(response);
    EventBus.getDefault().post(event);
}

@Override
public void orderResult(OrderTaskResponse response) {
    OrderTaskResponseEvent event = new OrderTaskResponseEvent();
    event.setAction(MokoConstants.ACTION_ORDER_RESULT);
    event.setResponse(response);
    EventBus.getDefault().post(event);
}

@Override
public boolean orderNotify(BluetoothGattCharacteristic characteristic, byte[] value) {
    ...
    OrderTaskResponseEvent event = new OrderTaskResponseEvent();
    event.setAction(MokoConstants.ACTION_CURRENT_DATA);
    event.setResponse(response);
    EventBus.getDefault().post(event);
    ...
}
```
3.In order to record log files, `XLog` is used in the SDK, and the permission `WRITE_EXTERNAL_STORAGE` is applied. If you do not want to use it, you can modify it in `BaseApplication`, and only keep `XLog.init(config)`.


## Change log

* 2021.11.30 mokosupport version:3.0
	*  Change the SDK package name
	*  support light sensor data
* 2021.03.11 mokosupport version:2.0
	* Change the SDK structure
    * Support Android API 29
    * Support androidx
	* Optimize document content
* 2020.01.18 mokosupport version:1.0
	* First commit
