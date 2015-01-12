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
	private int numInitialDevices = 0;
	private HidDevice gogoBoard;
	

	private void loadUpHIDServices() throws HidException  {
		// Get HID services and store
	    hidServices = HidManager.getHidServices();
	    hidServices.addHidServicesListener(this);

	    numInitialDevices = 0;
	    // Provide a list of attached devices
	    for (HidDeviceInfo hidDeviceInfo : hidServices.getAttachedHidDevices()) {
	      System.out.println(hidDeviceInfo);
	      if (hidDeviceInfo.getVendorId() == 0x461 && hidDeviceInfo.getProductId() == 0x20) {
	    	  numInitialDevices++;
	    	  //store a gogo as gogo (limitation of current implementation -- only allow one gogo)
	    	  gogoBoard = hidServices.getHidDevice(0x461, 0x20, null);
	      }
	    }
	}

	@Override
	public void load(PrimitiveManager pm) throws ExtensionException {
		pm.addPrimitive("primitives", new Prims() );
		
		pm.addPrimitive("read-sensors", new ReadSensors() );
		
		pm.addPrimitive("beep", new Beep() );
		pm.addPrimitive("led", new LED() );
		
		pm.addPrimitive("talk-to-motor",new TalkToMotor() );
		pm.addPrimitive("motor-set-power", new SetMotorPower() );
		pm.addPrimitive("motor-on", new MotorOn() );
		pm.addPrimitive("motor-off", new MotorOff() );
		pm.addPrimitive("motor-clockwise", new MotorClockwise() );
		pm.addPrimitive("motor-counterclockwise", new MotorCounterClockwise() );
		
		pm.addPrimitive("set-servo", new SetServo() );
		
		pm.addPrimitive("read-all", new ReadAll() );
		pm.addPrimitive("send-bytes", new SendBytes() );
		pm.addPrimitive("howmany-gogos", new Enumerate() );
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
			llb.add("read-sensors <returns all 8 sensor values in a list>");
			llb.add("----writing (inputs)---");
			llb.add("beep");
			llb.add("led <0 off 1 on> {refers to the user LED (yellow)");
			llb.add("talk-to-motor <motor number(s) to talk to -- 1,2,4,8 and bitwise or> {also activates associated servo}");
			llb.add("motor-set-power <power value 0-100%>");
			llb.add("motor-on {turns current motor (see talk-to-motor) on}");
			llb.add("motor-off {turns current motor (see talk-to-motor) off}");
			llb.add("motor-clockwise {makes current motor (see talk-to-motor) go clockwise}");
			llb.add("motor-counterclockwise {makes current motor (see talk-to-motor) go counterclockwise}");
			llb.add("set-servo <position> {makes current servo (see talk-to-motor) move to position}");
			llb.add("----utility functions---");
			llb.add("read-all <returns entire byte sequence of HID device's status message>");
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
			return Syntax.commandSyntax(new int[] {Syntax.NumberType() });
		}
		
		@Override
		public void perform(Argument[] args, Context ctx)
				throws ExtensionException, LogoException {
			int motorNum = args[0].getIntValue();
			byte[] message = new byte[64];
			message[0] = (byte)0;
		    message[1] = (byte)7;  
		    message[2] = (byte)motorNum;
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
				llb.add( Double.valueOf(data[i]) );
			}
			return llb.toLogoList();
		}
	}
	

	
	private class Enumerate extends DefaultReporter {
		@Override
		public Object report(Argument[] arg0, Context arg1)
				throws ExtensionException, LogoException {
			numInitialDevices = 0;
			for (HidDeviceInfo hidDeviceInfo : hidServices.getAttachedHidDevices()) {
		      if (hidDeviceInfo.getVendorId() == 0x461 && hidDeviceInfo.getProductId() == 0x20) {
		    	  numInitialDevices++;
		      }
		    }
			return "There are " + numInitialDevices + " attached HID device(s) with the correct vendor / product ids for a GoGo...";
		}
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

	
	//For testing connectivity without NetLogo
	public static void main(String[] args) {
		HIDGogoExtension hge = new HIDGogoExtension();
		try {
			hge.loadUpHIDServices();
		} catch (HidException e) {
			System.err.println("error in loading up services:");
			e.printStackTrace();
		}
		System.err.println("THERE IS / ARE " + hge.numInitialDevices + " GoGo device(s). ");
	}
}
