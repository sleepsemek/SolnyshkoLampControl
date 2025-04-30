# SolnyshkoLampControl
[ESP32 Firmware](https://github.com/ValeraDanger/Solnyshko_BLE)\
**BLE version. Stable release.**\
App parses POJO classes to JSON string commands and vice versa to communicate with the device.\
UUIDs are:
|Service UUID|Command characteriscic UUID|Notification UUID|
|-|-|-|
|_`4fafc201-1fb5-459e-8fcc-c5c9c331914b`_|_`beb5483e-36e1-4688-b7f5-ea07361b26a8`_|_`1fd32b0a-aa51-4e49-92b2-9a8be97473c9`_|
|This service contains two characteristics|Commands and read requests are sent here|App receives here _CHNG_ notifications <br> when something on the lamp side changes|   

To get lamp state app requests read from a command characteristic. Response is:
```
{
  "state":"0-4",
  "preheat":{
    "time_left":0
  },
  "timer":{
    "cycle_time":0,
    "cycles":0,
    "time_left":0
  }
}
```
There are 5 lamp states:
- 0 - `OFF`
- 1 - `ON`
- 2 - `PREHEATING`
- 3 - `ACTIVE`
- 4 - `PAUSED`

App uses provided values depending on the received state.

Sent command looks something like:
```
{
  "timer":{
    "action":"...",
    "time":0,
    "cycles":0
  }
}
```   
There are 4 actions:
- `set`
- `pause`
- `resume`
- `stop`

To set up timer this JSON should have all the fields correctly filled, so all POJO class fields should be initialized. To pause, resume or stop timer only the `action` field should be initialized, i.e.:
```
{
  "timer":{
    "action":"pause"
  }
}
```   

To enable or disable relay only the `relay` field should be initialized, i.e.:
```
{
  "relay": 0
}
```   
Relay states:
- 0 - `turn off`
- 1 - `turn on`

`SentCommand.java` class has the special constructors for all three usages.
  
  
