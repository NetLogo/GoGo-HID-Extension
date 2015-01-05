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
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.PrimitiveManager;
import org.nlogo.api.Syntax;

public class HIDGogoExtension extends DefaultClassManager implements HidServicesListener {
	
	private HidServices hidServices;
	private int numInitialDevices = 0;
	private HidDevice gogoBoard;
	

	@Override
	public void load(PrimitiveManager pm) throws ExtensionException {
		pm.addPrimitive("test", new Test() );
		pm.addPrimitive("beep", new Beep() );
		pm.addPrimitive("read-sensors", new ReadSensors() );
		pm.addPrimitive("led", new LED() );
		pm.addPrimitive("primitives", new Prims() );
		pm.addPrimitive("lame",new Lame() );
		try { 
			loadUpHIDServices();
		} catch (Exception e) {
			System.err.println("HID EXCEPTION");
			e.printStackTrace();
		}
	}
	
	
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
	
	private class Lame extends DefaultCommand {
		@Override
		public void perform(Argument[] arg0, Context arg1)
				throws ExtensionException, LogoException {
			byte[] message = new byte[64];
			message[0] = (byte)0;
		    message[1] = (byte)7;  
		    message[2] = (byte)1;
		    message[3] = (byte)0;  
		    
		    byte[] message2 = new byte[64];
			message2[0] = (byte)0;
		    message2[1] = (byte)9;  
		    message2[2] = (byte)0;
		    message2[3] = (byte)0;  
		    message2[4] = (byte)30;  
			
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
				gogoBoard.read(data, 100);
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
	
	
	private class Prims extends DefaultReporter {
		@Override
		public Syntax getSyntax() {
	      return Syntax.reporterSyntax(Syntax.ListType());
	    }
		
		@Override
		public Object report(Argument[] arg0, Context arg1)
				throws ExtensionException, LogoException {
			LogoListBuilder llb = new LogoListBuilder();
			llb.add("beep");
			llb.add("led <0 off 1 on>");
			llb.add("read-sensors <returns all 8 sensor values in a list>");
			llb.add("test <returns number of HID gogos currently connected>");
			llb.add("primitives <this command, returns prims listing>");
			return llb.toLogoList();
		}
	}
	
	
	private class Test extends DefaultReporter {
		@Override
		public Object report(Argument[] arg0, Context arg1)
				throws ExtensionException, LogoException {
			numInitialDevices = 0;
			for (HidDeviceInfo hidDeviceInfo : hidServices.getAttachedHidDevices()) {
		      if (hidDeviceInfo.getVendorId() == 0x461 && hidDeviceInfo.getProductId() == 0x20) {
		    	  numInitialDevices++;
		      }
		    }
			return "Hello.  There are " + numInitialDevices + " attached HID device(s) with the right vendor / product ids for a GoGo...";
		}
	}
	

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

	
	//just for testing w/o NetLogo
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
