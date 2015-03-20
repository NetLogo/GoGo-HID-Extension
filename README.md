# The NetLogo GoGo Extension for sensors and robotics

This is the new extension for physical computing, using sensors, motors, etc in NetLogo. It interfaces with GoGo boards running Human Interface Driver (HID) firmware, and it replaces the old GoGo Extension, which used USB-serial communications. (The USB-serial GoGo extension is still available  [here](https://github.com/NetLogo/GoGo-Serial-Extension).)  

This extension comes bundled with NetLogo as of NetLogo 5.2.  The source code is hosted online at
https://github.com/NetLogo/GoGo-HID-Extension.


## Index of Primitives

[General](#general)

- [primitives](#primitives), [howmany-gogos](#howmany-gogos)

[Outputs and Servos](#outputs-and-servos)

- [talk-to-output-ports](#talk-to-output-ports), [set-output-port-power](#set-output-port-power), [output-port-on](#output-port-on), [output-port-off](#output-port-off), [output-port-clockwise](#output-port-clockwise), [output-port-counterclockwise](#output-port-counterclockwise), [set-servo](#set-servo)

[Other Outputs](#other-outputs)

- [led](#led), [beep](#beep)

[Sensors](#sensors)

- [read-sensors](#read-sensors), [read-sensor](#read-sensor)

[Utilities](#utilities)

- [read-all](#read-all), [send-bytes](#send-bytes)


## Changes

Compared to previous versions of the GoGo extension, this version offers:

- **Improved robustness**.  With prior versions of the GoGo extension, crashes were fairly common due to problems in the USB-Serial stack across platforms.  The switch to HID improved robustness, and the new extension also uses a "daemon" architecture which shields NetLogo from any problems that may occur in direct communication with the GoGo board.  The result is a substantial reduction in the number of crashes of NetLogo.
- **No Installation of Drivers**. Because the new GoGo firmware presents the board as an HID device, the extension could be written so as not to require installing drivers.  This means there is no need for the user to have administrator rights on the computer.
- **Directionality for Motors**. The board now has polarity-ensuring output connectors, so that "counterclockwise" or "clockwise" can now be specified in code.


## Usage

The GoGo Extension comes preinstalled when you download and install NetLogo. To use the extension in your model, add this line to the top of your Code tab:

    extensions [ gogo ]
   
If your model already uses other extensions, then it already has an extensions line in it, so just add gogo to the list.

After loading the extension, you can see whether one or more HID-based gogos are on and attached to the computer by typing the following into the command center:

	gogo:howmany-gogos
	

## Primitives

### General

#### primitives

`gogo:primitives`

Returns a list of the primitives of this extension.

#### howmany-gogos

`gogo:howmany-gogos`

Reports the number of USB HID devices visible to the computer and having the correct vendor and product ID to be a GoGo board.  A board will only be detected if it is both connected and powered on.  Using this primitive is one way to determine quickly whether a GoGo board has the HID firmware loaded. (A USB-Serial version of the board will not be detected.).

### Outputs and Servos


#### talk-to-output-ports 

`gogo:talk-to-output-ports _list-of-portnames_`

Establishes a list of output ports that will be controlled with subsequent "output-port" commands.  See below...


#### set-output-port-power

`gogo:set-output-port-power _power-level_`

The argument is a number between 0 and 100, reflecting the percentage of maximum power.  Sets the amount of power that will be fed to the output ports indicated in talk-to-output-ports. This will not affect the on-off state of the output ports.  So, for example, if a motor is already connected to an output port and running, changing its power will change its speed.  If the motor is not running, changing the power level will not turn it on; instead, it will affect the speed at which the motor starts when it is turned on with output-port-on.

#### output-port-on

`gogo:output-port-on`

Turns on the output ports which have been indicated with talk-to-output-ports. If none have been set with talk-to-output-ports, no ports will be turned on.

#### output-port-off

`gogo:output-port-off`

Turns off the output ports which have been indicated with talk-to-output-ports. If none have been set with talk-to-output-ports, no ports will be turned off.

#### output-port-clockwise

`gogo:output-port-clockwise`

Sets the polarity of the output port(s) that have been specified with talk-to-output-ports, so that a motor attached to one of these ports would turn clockwise.


#### output-port-counterclockwise

`gogo:output-port-counterclockwise`

Sets the polarity of the output port(s) that have been specified with talk-to-output-ports, so that a motor attached to one of these ports would turn counterclockwise.


#### set-servo

`gogo:set-servo`

Sets the Pulse-Width Modulation (PWM) proportion of the output port(s) that have been specified with talk-to-output-ports.  Note that the servo connectors are the male pins next to the standard motor connectors.  Different servos respond to different PWM ranges, but all servos read PWM proportions and set the position of their main gear accordingly.



### Other Outputs

#### led
`gogo:led _on-or-off_`

Turns the user-LED on or off, depending on the argument.  gogo:led 1 turns the LED on; gogo:led 0 turns it off.


#### beep
`gogo:beep`

Causes the GoGo board to beep.


### Sensors

#### read-sensors
`gogo:read-sensors`

Reports a list containing the current readings of all eight sensors ports of the GoGo.

#### read-sensor
`gogo:read-sensor _which-sensor_`

Reports the value of sensor number _which sensor_, where _which sensor_ is a number between 0-7.




### Utilities

#### read-all
`gogo:read-all`

Reports all data available from the board, in a raw-list form useful for debugging.


#### send-bytes
`gogo:send-bytes _list-of-bytes_`

Sends a list of bytes to the GoGo board.  Useful for debugging or for testing any new or future functionality that is added to the GoGo board with new firmware updates.




[carex]: http://ccl.northwestern.edu/netlogo/docs/images/gogo/netlogolab-example1-on_off_buttons.png "Car Example"
[turtle]: https://github.com/NetLogo/NW-Extension/raw/master/turtle.gif  "Turtle"
[link]: https://github.com/NetLogo/NW-Extension/raw/master/link.gif  "Link"