package gogoHID;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.hid4java.HidDevice;
import org.hid4java.HidDeviceInfo;
import org.hid4java.HidException;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.hid4java.HidServicesListener;
import org.hid4java.event.HidServicesEvent;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.PrimitiveManager;
import org.nlogo.api.Syntax;

public class HIDGogoExtension extends DefaultClassManager implements HidServicesListener {
	
	private HidServices hidServices;
	private HidDevice gogoBoard;
	
  private boolean shownErrorMessage = false;

	private void loadUpHIDServices() throws HidException  {
		// Get HID services and store
	    hidServices = HidManager.getHidServices();
	    hidServices.addHidServicesListener(this);

	    // Provide a list of attached devices
	    for (HidDeviceInfo hidDeviceInfo : hidServices.getAttachedHidDevices()) {
	      System.out.println(hidDeviceInfo);
	      if (hidDeviceInfo.getVendorId() == 0x461 && hidDeviceInfo.getProductId() == 0x20) {
	    	  //store a gogo as gogo (limitation of current implementation -- only allow one gogo)
	    	  gogoBoard = hidServices.getHidDevice(0x461, 0x20, null);
	      }
	    }
	}

  private void alertToNewGogoBoards(String primitiveName) {
    if(!shownErrorMessage) {
      while(true) {
        final int choice =
            org.nlogo.swing.OptionDialog.show
                (org.nlogo.app.App.app().frame(),
                    "GoGo Extension has been updated!",
                    "This model is using a primitive (gogo:" + primitiveName + ") from the old version of the GoGo extension.  Use gogo-serial for older GoGo boards.  Your model will likely throw errors.",
                    new String[]{"More Information", "Close"});
        if (choice == 1) {
          break;
        }
        org.nlogo.swing.BrowserLauncher.openURL
          (org.nlogo.app.App.app().frame(), "https://github.com/NetLogo/NetLogo/wiki/GoGo-Upgrade", false);
      }
      shownErrorMessage = true;
    }
  }

  private class OldReporter extends DefaultReporter {
    private Syntax syntax;
    private String primitiveName;

    private OldReporter(String primitiveName, Syntax syntax) {
      this.syntax = syntax;
      this.primitiveName = primitiveName;
    }

    public Syntax getSyntax() { return syntax; }
    public Object report(Argument args[], Context context) {
      alertToNewGogoBoards(primitiveName);
      return null;
    }
  }

  private class OldCommand extends DefaultCommand {
    private Syntax syntax;
    private String primitiveName;

    private OldCommand(String primitiveName, Syntax syntax) {
      this.syntax = syntax;
      this.primitiveName = primitiveName;
    }

    public Syntax getSyntax() { return syntax; }
    public void perform(Argument args[], Context context) {
      alertToNewGogoBoards(primitiveName);
    }
  }

	@Override
	public void load(PrimitiveManager pm) throws ExtensionException {
		pm.addPrimitive("primitives", new Prims() );
		
		pm.addPrimitive("read-sensors", new ReadSensors() );
		pm.addPrimitive("read-sensor", new ReadSensorNumber() );
		
		pm.addPrimitive("beep", new Beep() );
		pm.addPrimitive("led", new LED() );
		
		pm.addPrimitive("talk-to-output-ports",new TalkToMotor() );
		pm.addPrimitive("motor-set-power", new SetMotorPower() );
		pm.addPrimitive("motor-on", new MotorOn() );
		pm.addPrimitive("motor-off", new MotorOff() );
		pm.addPrimitive("motor-clockwise", new MotorClockwise() );
		pm.addPrimitive("motor-counterclockwise", new MotorCounterClockwise() );
		
		pm.addPrimitive("set-servo", new SetServo() );
		
		pm.addPrimitive("read-all", new ReadAll() );
		pm.addPrimitive("send-bytes", new SendBytes() );
		pm.addPrimitive("howmany-gogos", new Enumerate() );

    // Old Primitives that should let you know we've changed the extension
    pm.addPrimitive("ports", new OldReporter("ports", Syntax.reporterSyntax(Syntax.ListType())));
    pm.addPrimitive("burst-value", new OldReporter("burst-value", Syntax.reporterSyntax(new int[] {Syntax.NumberType()}, Syntax.NumberType())));
    pm.addPrimitive("close", new OldCommand("close", Syntax.commandSyntax()));
    pm.addPrimitive("install", new OldCommand("install", Syntax.commandSyntax()));
    pm.addPrimitive("led-off", new OldCommand("led-off", Syntax.commandSyntax()));
    pm.addPrimitive("led-on", new OldCommand("led-on", Syntax.commandSyntax()));
    pm.addPrimitive("open", new OldCommand("open", Syntax.commandSyntax(new int[] {Syntax.StringType()})));
    pm.addPrimitive("open?", new OldReporter("open?", Syntax.reporterSyntax(Syntax.BooleanType())));
    pm.addPrimitive("output-port-coast", new OldCommand("output-port-coast", Syntax.commandSyntax()));
    pm.addPrimitive("output-port-off", new OldCommand("output-port-off", Syntax.commandSyntax()));
    pm.addPrimitive("output-port-on", new OldCommand("output-port-on", Syntax.commandSyntax()));
    pm.addPrimitive("output-port-reverse", new OldCommand("output-port-reverse", Syntax.commandSyntax()));
    pm.addPrimitive("output-port-thatway", new OldCommand("output-port-thatway", Syntax.commandSyntax()));
    pm.addPrimitive("output-port-thisway", new OldCommand("output-port-thisway", Syntax.commandSyntax()));
    pm.addPrimitive("ping", new OldReporter("ping", Syntax.reporterSyntax(Syntax.BooleanType())));
    pm.addPrimitive("ports", new OldReporter("ports", Syntax.reporterSyntax(Syntax.ListType())));
    pm.addPrimitive("sensor", new OldReporter("sensor", Syntax.reporterSyntax(new int[] {Syntax.NumberType()}, Syntax.NumberType())));
    pm.addPrimitive("set-burst-mode", new OldCommand("set-burst-mode", Syntax.commandSyntax(new int[] {Syntax.ListType(), Syntax.BooleanType()})));
    pm.addPrimitive("set-output-port-power", new OldCommand("set-output-port-power", Syntax.commandSyntax(new int[] {Syntax.NumberType()})));
    pm.addPrimitive("stop-burst-mode", new OldCommand("stop-burst-mode", Syntax.commandSyntax()));

		try { 
			loadUpHIDServices();
		} catch (Exception e) {
			System.err.println("HID EXCEPTION");
			e.printStackTrace();
			throw new ExtensionException("Error in loading HID services");
		}
	}
	
	
	private class Prims extends DefaultReporter {
		@Override
		public Syntax getSyntax() {
	      return Syntax.reporterSyntax(Syntax.ListType());
	    }
		
		@Override
		public Object report(Argument[] arg0, Context arg1)
				throws ExtensionException, LogoException {
			LogoListBuilder llb = new LogoListBuilder();
			llb.add("primitives <this command, returns prims listing>");
			llb.add("----reading (inputs)---");
			llb.add("read-sensors {returns all 8 sensor values in a list}");
			llb.add("read-sensor <number> {number should be between 1 and 8 inclusive}");
			llb.add("----writing (outputs)---");
			llb.add("beep");
			llb.add("led <0 off 1 on> {refers to the user LED (yellow)");
			llb.add("talk-to-output-ports <output letter(s) to talk to> {e.g, A for port A, ABD for ports A, B, and D}");
			llb.add("motor-set-power <power value 0-100 for current motor (see talk-to-output-ports)> {0 = full off; 100 = full on.  Sets board value to <power>% of 255.}");
			llb.add("motor-on {turns current motor (see talk-to-output-ports) on}");
			llb.add("motor-off {turns current motor (see talk-to-output-ports) off}");
			llb.add("motor-clockwise {makes current motor (see talk-to-output-ports) go clockwise}");
			llb.add("motor-counterclockwise {makes current motor (see talk-to-output-ports) go counterclockwise}");
			llb.add("set-servo <position> {makes current servo (see talk-to-output-ports) move to position <position> 0-255}");
			llb.add("----utility functions---");
			llb.add("read-all <returns entire byte sequence of HID device's status message> {converts to byte digits to hex strings}");
			llb.add("send-bytes <sends any byte sequence to the gogo> {list of integers will get padded out with zeroes.}");
			llb.add("howmany-gogos <returns number of HID gogos currently connected>");
			return llb.toLogoList();
		}
	}
	
	private class ReadSensors extends DefaultReporter {
		@Override
		public Syntax getSyntax() {
	      return Syntax.reporterSyntax(Syntax.ListType());
	    }
		
		@Override
		public Object report(Argument[] arg0, Context arg1)
				throws ExtensionException, LogoException {
			short[] sensors = new short[8];
			if (gogoBoard != null) {
				byte[] data = new byte[64];
				gogoBoard.read(data, 200);
				for (int index = 0; index < 8; index++) {
					ByteBuffer bb = ByteBuffer.wrap(data,(2*index) + 1,2 );
					bb.order(ByteOrder.BIG_ENDIAN);
					sensors[index] = bb.getShort();
				}
			}
			LogoListBuilder llb = new LogoListBuilder();
			for (int i = 0; i< sensors.length; i++) {
				llb.add( Double.valueOf(sensors[i]) );
			}
			return llb.toLogoList();
		}
	}
	
	private class ReadSensorNumber extends DefaultReporter {
		@Override
		public Syntax getSyntax() {
	      return Syntax.reporterSyntax(new int[] {Syntax.NumberType() }, Syntax.NumberType());
	    }
		
		@Override
		public Object report(Argument[] args, Context arg1)
				throws ExtensionException, LogoException {
			int sensorNumber = args[0].getIntValue();
			if ((sensorNumber > 8) || (sensorNumber < 1)) {
				throw new ExtensionException("Sensor Number must be between 1 and 8 (inclusive).  You entered " + sensorNumber);
			}
			Short sensorReading = 0;
			if (gogoBoard != null) {
				byte[] data = new byte[64];
				gogoBoard.read(data, 200);
				
				ByteBuffer bb = ByteBuffer.wrap(data,(2*(sensorNumber-1)) + 1,2 );
				bb.order(ByteOrder.BIG_ENDIAN);
				sensorReading = bb.getShort();
			}
			
			return sensorReading.doubleValue();
		}
	}
		
	
	private class Beep extends DefaultCommand {
		@Override
		public void perform(Argument[] arg0, Context arg1)
				throws ExtensionException, LogoException {
			byte[] message = new byte[64];
			message[0] = (byte)0;
		    message[1] = (byte)11;  
		    message[2] = (byte)0;
		    message[3] = (byte)0;  
			
			if (gogoBoard != null) {
				gogoBoard.write(message,64, (byte)0);
			}
		}
	}
	
	
	private class LED extends DefaultCommand {
		@Override
		public Syntax getSyntax() {
			return Syntax.commandSyntax(new int[] {Syntax.NumberType() });
		}
		
		@Override
		public void perform(Argument[] args, Context arg1)
				throws ExtensionException, LogoException {
			byte[] message = new byte[64];
			boolean turnOn = ( args[0].getIntValue() == 1);
			message[0] = (byte)0;
		    message[1] = (byte)10; 
		    message[2] = (byte)0;
		    if (turnOn) {
		    	message[3] = (byte)1;
		    } else {
		    	message[3] = (byte)0;  
		    }
			
			if (gogoBoard != null) {
				gogoBoard.write(message,64, (byte)0);
			}
		}
	}
	
	
	private class TalkToMotor extends DefaultCommand {
		@Override
		public Syntax getSyntax() {
			return Syntax.commandSyntax(new int[] {Syntax.ListType() });
		}
		
		@Override
    public void perform(Argument args[], Context context)
        throws ExtensionException, org.nlogo.api.LogoException {
      java.util.Iterator<?> iter = args[0].getList().iterator();
      int outputPortMask = 0;

			int motorMask = 0;
      while (iter.hasNext()) {
        Object val = iter.next();
        switch (val.toString().toLowerCase().charAt(0)) {
          case 'a':
          case 'A':
				    motorMask = motorMask  | 1;
            break;
          case 'b':
          case 'B':
				    motorMask = motorMask  | 2;
            break;
          case 'c':
          case 'C':
				    motorMask = motorMask  | 4;
            break;
          case 'd':
          case 'D':
				    motorMask = motorMask  | 8;
            break;
        }
      }
			
			byte[] message = new byte[64];
			message[0] = (byte)0;
		    message[1] = (byte)7;  
		    message[2] = (byte)motorMask;
		    message[3] = (byte)0;   
			if (gogoBoard != null) {
				gogoBoard.write(message,64, (byte)0);
			}
		}
	}
	
	
	private class SetMotorPower extends DefaultCommand {
		@Override
		public Syntax getSyntax() {
			return Syntax.commandSyntax(new int[] {Syntax.NumberType() });
		}
		
		@Override
		public void perform(Argument[] args, Context ctx)
				throws ExtensionException, LogoException {
			int motorPowerVal = args[0].getIntValue();
			byte[] message = new byte[64];
			message[0] = (byte)0;
		    message[1] = (byte)6;
		    message[2] = (byte)0;
		    message[3] = (byte)0; //high byte   
		    message[4] = (byte)motorPowerVal; 
		    if (gogoBoard != null) {
				gogoBoard.write(message,64, (byte)0);
			}
		}
	}
	
	
	private class MotorOn extends DefaultCommand {
		@Override
		public void perform(Argument[] args, Context ctx)
				throws ExtensionException, LogoException {
			byte[] message = new byte[64];
			message[0] = (byte)0;
		    message[1] = (byte)2;
		    message[2] = (byte)0;
		    message[3] = (byte)1; //on  
		    
			if (gogoBoard != null) {
				gogoBoard.write(message,64, (byte)0);
			}
		}
	}
	
	
	private class MotorOff extends DefaultCommand {
		@Override
		public void perform(Argument[] args, Context ctx)
				throws ExtensionException, LogoException {
			byte[] message = new byte[64];
			message[0] = (byte)0;
		    message[1] = (byte)2;
		    message[2] = (byte)0;
		    message[3] = (byte)0; //off  
		    
			if (gogoBoard != null) {
				gogoBoard.write(message,64, (byte)0);
			}
		}
	}
	

	private class MotorClockwise extends DefaultCommand {
		@Override
		public void perform(Argument[] arg0, Context arg1)
				throws ExtensionException, LogoException {
			byte[] message = new byte[64];
			message[0] = (byte)0;
		    message[1] = (byte)3;  
		    message[2] = (byte)0;
		    message[3] = (byte)1;  //cw
			
			if (gogoBoard != null) {
				gogoBoard.write(message,64, (byte)0);
			}
		}
	}
	
	private class MotorCounterClockwise extends DefaultCommand {
		@Override
		public void perform(Argument[] arg0, Context arg1)
				throws ExtensionException, LogoException {
			byte[] message = new byte[64];
			message[0] = (byte)0;
		    message[1] = (byte)3;  
		    message[2] = (byte)0;
		    message[3] = (byte)0;  //ccw
			
			if (gogoBoard != null) {
				gogoBoard.write(message,64, (byte)0);
			}
		}
	}
	
	private class SetServo extends DefaultCommand {
		@Override
		public Syntax getSyntax() {
			return Syntax.commandSyntax(new int[] {Syntax.NumberType() });
		}
		
		@Override
		public void perform(Argument[] args, Context ctx)
				throws ExtensionException, LogoException {
			int dutyLevel = args[0].getIntValue();
			byte[] message = new byte[64];
			message[0] = (byte)0;
		    message[1] = (byte)9;  
		    message[2] = (byte)0;
		    message[3] = (byte)0; 
		    message[4] = (byte)dutyLevel;
			if (gogoBoard != null) {
				gogoBoard.write(message,64, (byte)0);
			}
		}
	}
	
	private class SendBytes extends DefaultCommand {
		@Override
		public Syntax getSyntax() {
			return Syntax.commandSyntax(new int[] { Syntax.ListType() });
		}
		
		@Override
		public void perform(Argument[] args, Context ctx)
				throws ExtensionException, LogoException {
			LogoList messageList = args[0].getList();
			byte[] message = new byte[64];
			for (int i = 0; i<messageList.size(); i++) {
				Object val = messageList.get(i);
				int v = (Integer)val;
				message[i] = (byte)v;
			}
			if (gogoBoard != null) {
				gogoBoard.write(message,64, (byte)0);
			}
		}
	}
	
	
	private class ReadAll extends DefaultReporter {
		@Override
		public Syntax getSyntax() {
	      return Syntax.reporterSyntax(Syntax.ListType());
	    }
		
		@Override
		public Object report(Argument[] arg0, Context arg1)
				throws ExtensionException, LogoException {
			byte[] data = new byte[64];
			if (gogoBoard != null) {
				gogoBoard.read(data, 200);
			}
			LogoListBuilder llb = new LogoListBuilder();
			for (int i = 0; i< data.length; i++) {
				llb.add( byteToHex(data[i]) );
			}
			return llb.toLogoList();
		}
	}
	

	
	private class Enumerate extends DefaultReporter {
		@Override
		public Object report(Argument[] arg0, Context arg1)
				throws ExtensionException, LogoException {
			Integer numDevices = 0;
			for (HidDeviceInfo hidDeviceInfo : hidServices.getAttachedHidDevices()) {
		      if (hidDeviceInfo.getVendorId() == 0x461 && hidDeviceInfo.getProductId() == 0x20) {
		    	  numDevices++;
		      }
		    }
			return numDevices.doubleValue();
		}
	}
	

	//utility.  converts a single byte to a 2-digit hex string representation.
	final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public String byteToHex(byte b) {
	    char[] hexChars = new char[2];

        int v = b & 0xFF;
        hexChars[0] = hexArray[v >>> 4];
        hexChars[1] = hexArray[v & 0x0F];
    
	    return new String(hexChars);
	}
	
	
	//Attach and detach events
	@Override
	public void hidDeviceAttached(HidServicesEvent event) {
		System.out.println("Device attached: " + event);
	    if (event.getHidDeviceInfo().getVendorId() == 0x461 &&
	      event.getHidDeviceInfo().getProductId() == 0x20) {
	      gogoBoard = hidServices.getHidDevice(0x461, 0x20, null);
	    }
	}


	@Override
	public void hidDeviceDetached(HidServicesEvent event) {
		System.err.println("device detached");
		System.err.println(event.getHidDeviceInfo().toString());
		System.err.println(event.toString());
		if (event.getHidDeviceInfo().getVendorId() == 0x461 &&
	      event.getHidDeviceInfo().getProductId() == 0x20) {
	      gogoBoard = null;
	    }
	}


	@Override
	public void hidFailure(HidServicesEvent arg0) {
		System.err.println("HID failure");
	}

	
}
