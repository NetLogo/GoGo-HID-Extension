# The NetLogo GoGo Extension for sensors and robotics

This is the new extension for physical computing, using sensors, motors, etc in NetLogo. It interfaces with GoGo boards running HID-driver-compatible firmware, and it replaces the old GoGo Extension that used USB-serial communications. (The old GoGo extension is [here](https://github.com/NetLogo/GoGo-Serial-Extension).)  

This extension comes bundled with NetLogo as of NetLogo 5.2.  The source code is hosted online at
https://github.com/NetLogo/GoGo-HID-Extension.


## Index of Primitives

[General](#general)

- [primitives](#primitives), [howmany-gogos](#howmany-gogos)

[Motors and Servos](#motors-and-servos)

- [talk-to-output-ports](#talk-to-output-ports), [motor-set-power](#motor-set-power), [motor-on](#motor-on), [motor-off](#motor-off), [motor-clockwise](#motor-clockwise), [motor-counterclockwise](#motor-counterclockwise), [set-servo](#set-servo)

[Other Outputs](#other-outputs)

- [led](#v), [beep](#beep)

[Sensors](#sensors)

- [read-sensors](#read-sensors), [read-sensor](#read-sensor)

[Utilities](#utilities)

- [read-all](#read-all), [send-bytes](#send-bytes)


## Changes

Compared to the previous extension, this new version offers:

- **Improved robustness**: with prior versions of the GoGo extension, crashes were fairly common due to problems in the USB-Serial stack across platforms.
- **No Installation of Drivers**: Because the new GoGo firmware presents the board as an HID device, the extension could be written so as not to require installing drivers.  This means there is no need for the user to have administrator rights on the computer.
- **Directionality for Motors**: The board now has polarity-ensuring motor connectors, so that "counterclockwise" or "clockwise" can now be specified in code.


## Usage

The GoGo Extension comes preinstalled when you download and install NetLogo. To use the extension in your model, add this line to the top of your Code tab:

    extensions [ gogo ]
   
If your model already uses other extensions, then it already has an extensions line in it, so just add gogo to the list.

After loading the extension, see whether one or more gogos are on and attached to the computer by typing the following into the command center:

	gogo:howmany-gogos
	
### Example: Controlling a Car

Imagine that we want to control a car with four wheels and two motors attached to the back wheels. We will assume that you have built such a car and connected the motors to the output ports "a" and "b" on the GoGo board. One very simple approach could be to create two buttons for each motor, "on" and "off":

![carex][carex]

{}{}{}More stuff here, from the Old Manual.

## Primitives

### General

#### primitives

`gogo:primitives`

Returns a list of the primitives of this extension.

#### howmany-gogos

`gogo:howmany-gogos`

Reports the number of USB HID devices visible to the computer and having the correct vendor and product ID to be a GoGo board.  A board will only be detected if it is both connected and powered on.  Using this primitive is one way to determine quickly whether a GoGo board has the HID firmware loaded. (A USB-Serial version of the board will not be detected.).

### Motors and Servos


#### talk-to-output-ports 

`gogo:talk-to-output-ports _list-of-portnames_`

Establishes the context of 


#### motor-set-power

`gogo:motor-set-power _power-level_`

Sets the amount of power that will be fed to the motor ports indicated in talk-to-output-ports.  This will not affect the on-off state of the output ports.  So, if a motor is already running, changing its power will change its speed.  If a motor is not running, changing the power level will not turn it on; instead, it will affect the speed at which the motor starts when it is turned on with motor-on.

#### motor-on

`gogo:motor-on`

Turns on the motor ports which have been indicated with talk-to-output-ports. If none have been set with talk-to-output-ports, no motor ports will be turned on.

#### motor-off

`gogo:motor-off`

Turns off the motor ports which have been indicated with talk-to-output-ports. If none have been set with talk-to-output-ports, no motor ports will be turned off.

#### motor-clockwise

`gogo:motor-clockwise`


#### motor-counterclockwise

`gogo:motor-counterclockwise`


#### set-servo

`gogo:set-servo`



### Other Outputs

#### led
`gogo:led _on-or-off_`


#### beep
`gogo:beep`



### Sensors

#### read-sensors
`gogo:read-sensors`

Reports a list containing the current readings of all eight sensors.

#### read-sensor
`gogo:read-sensor _which-sensor_`

Reports the value of sensor number _which sensor_, where _which sensor_ is a number between 0-7.




### Utilities

#### read-all
`gogo:read-all`

Reports all data available from the board, in raw-list form.


#### send-bytes
`gogo:send-bytes _list-of-bytes_`

check argument




[carex]: http://ccl.northwestern.edu/netlogo/docs/images/gogo/netlogolab-example1-on_off_buttons.png "Car Example"
[turtle]: https://github.com/NetLogo/NW-Extension/raw/master/turtle.gif  "Turtle"
[link]: https://github.com/NetLogo/NW-Extension/raw/master/link.gif  "Link"