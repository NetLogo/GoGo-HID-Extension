
# The NetLogo GoGo Extension for sensors and robotics


This is the new extension for physical computing, using sensors, motors, etc in NetLogo. It interfaces with GoGo boards running Human Interface Driver (HID) firmware, and it replaces the old GoGo Extension, which used USB-serial communications. (The USB-serial GoGo extension is still available for NetLogo 5.3.1 and earlier [here](https://github.com/NetLogo/GoGo-Serial-Extension).)

This extension comes bundled with NetLogo 5.2 and later.  The source code is hosted online at
https://github.com/NetLogo/GoGo-HID-Extension.

## Building

Use the netlogo.jar.url environment variable to tell sbt which NetLogo.jar to compile against (defaults to NetLogo 5.3). For example:

    sbt -Dnetlogo.jar.url=file:///path/to/NetLogo/target/NetLogo.jar package

If compilation succeeds, `gogo.jar` and `gogo-daemon.jar` will be created.

## Usage

The GoGo Extension comes preinstalled when you download and install NetLogo. To use the extension in your model, add this line to the top of your Code tab:

```NetLogo
extensions [ gogo ]
```

If your model already uses other extensions, then it already has an extensions line in it, so just add gogo to the list.

After loading the extension, you can see whether one or more HID-based gogos are on and attached to the computer by typing the following into the command center:

```NetLogo
gogo:howmany-gogos
```

## Changes

Compared to previous versions of the GoGo extension, this version offers:

- **Improved robustness**.  With prior versions of the GoGo extension, crashes were fairly common due to problems in the USB-Serial stack across platforms.  The switch to HID improved robustness, and the new extension also uses a "daemon" architecture which shields NetLogo from any problems that may occur in direct communication with the GoGo board.  The result is a substantial reduction in the number of crashes of NetLogo.
- **No Installation of Drivers**. Because the new GoGo firmware presents the board as an HID device, the extension could be written so as not to require installing drivers.  This means there is no need for the user to have administrator rights on the computer.
- **Directionality for Motors**. The board now has polarity-ensuring output connectors, so that "counterclockwise" or "clockwise" can now be specified in code.


## Primitives

### Other Outputs

[`gogo:led`](#gogoled)
[`gogo:beep`](#gogobeep)

### Utilities

[`gogo:read-all`](#gogoread-all)

### General

[`gogo:primitives`](#gogoprimitives)
[`gogo:howmany-gogos`](#gogohowmany-gogos)

### Sensors

[`gogo:read-sensors`](#gogoread-sensors)
[`gogo:read-sensor`](#gogoread-sensor)

### Outputs and Servos

[`gogo:talk-to-output-ports`](#gogotalk-to-output-ports)
[`gogo:set-output-port-power`](#gogoset-output-port-power)
[`gogo:output-port-on`](#gogooutput-port-on)
[`gogo:output-port-off`](#gogooutput-port-off)
[`gogo:output-port-clockwise`](#gogooutput-port-clockwise)
[`gogo:output-port-counterclockwise`](#gogooutput-port-counterclockwise)
[`gogo:set-servo`](#gogoset-servo)



### `gogo:primitives`

```NetLogo
gogo:primitives
```

Returns a list of the primitives of this extension.


### `gogo:howmany-gogos`

```NetLogo
gogo:howmany-gogos
```


Reports the number of USB HID devices visible to the computer and having the correct vendor and product ID to be a GoGo board.  A board will only be detected if it is both connected and powered on.  Using this primitive is one way to determine quickly whether a GoGo board has the HID firmware loaded. (A USB-Serial version of the board will not be detected.).



### `gogo:talk-to-output-ports`

```NetLogo
gogo:talk-to-output-ports list-of-portnames
```

Establishes a list of output ports that will be controlled with subsequent output-port commands.  See below...


### `gogo:set-output-port-power`

```NetLogo
gogo:set-output-port-power power-level
```


`power-level` is a number between 0 and 100, reflecting the percentage of maximum power.
Sets the amount of power that will be fed to the output ports indicated in `talk-to-output-ports`.
This will not affect the on-off state of the output ports.
So, for example, if a motor is already connected to an output port and running, changing its power will change its speed.
If the motor is not running, changing the power level will not turn it on; instead, it will affect the speed at which the motor starts when it is turned on with `output-port-on`.



### `gogo:output-port-on`

```NetLogo
gogo:output-port-on
```


Turns on the output ports which have been indicated with talk-to-output-ports.
If none have been set with talk-to-output-ports, no ports will be turned on.



### `gogo:output-port-off`

```NetLogo
gogo:output-port-off
```


Turns off the output ports which have been indicated with talk-to-output-ports.
If none have been set with talk-to-output-ports, no ports will be turned off.



### `gogo:output-port-clockwise`

```NetLogo
gogo:output-port-clockwise
```


Sets the polarity of the output port(s) that have been specified with talk-to-output-ports,
so that a motor attached to one of these ports would turn clockwise.



### `gogo:output-port-counterclockwise`

```NetLogo
gogo:output-port-counterclockwise
```


Sets the polarity of the output port(s) that have been specified with `talk-to-output-ports`, so that a motor attached to one of these ports would turn counterclockwise.



### `gogo:set-servo`

```NetLogo
gogo:set-servo number
```


Sets the Pulse-Width Modulation (PWM) proportion of the output port(s) that have been specified with talk-to-output-ports.  Note that the servo connectors are the male pins next to the standard motor connectors.  Different servos respond to different PWM ranges, but all servos read PWM proportions and set the position of their main gear accordingly.



### `gogo:led`

```NetLogo
gogo:led on-or-off
```


Turns the user-LED on or off, depending on the argument.  gogo:led 1 turns the LED on; gogo:led 0 turns it off.



### `gogo:beep`

```NetLogo
gogo:beep
```

Causes the GoGo board to beep.


### `gogo:read-sensors`

```NetLogo
gogo:read-sensors
```

Reports a list containing the current readings of all eight sensors ports of the GoGo.


### `gogo:read-sensor`

```NetLogo
gogo:read-sensor which-sensor
```


Reports the value of sensor number *which-sensor*, where *which-sensor* is a number between 0-7.



### `gogo:read-all`

```NetLogo
gogo:read-all
```

Reports all data available from the board, in a raw-list form useful for debugging.


### `gogo:send-bytes`

```NetLogo
gogo:send-bytes list
```

Sends a list of bytes to the GoGo board.  Useful for debugging or for testing any new or future functionality that is added to the GoGo board with new firmware updates.

