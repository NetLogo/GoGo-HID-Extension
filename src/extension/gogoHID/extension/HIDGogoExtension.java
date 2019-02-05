package gogoHID.extension;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.Command;
import org.nlogo.api.Reporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.core.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.PrimitiveManager;
import org.nlogo.api.ExtensionManager;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

import org.nlogo.awt.UserCancelException;

import org.nlogo.swing.FileDialog;

import java.util.List;
import java.util.Arrays;
import java.io.*;

public class HIDGogoExtension extends DefaultClassManager {

  static final String javaLocationPropertyKey = "netlogo.extensions.gogo.javaexecutable";

  private boolean shownErrorMessage = false;

  private class UnsuccessfulReadOperation extends Exception {};

  private void alertToNewGogoBoards(String primitiveName) {
    if(!shownErrorMessage) {
      while(true) {
        final int choice =
            org.nlogo.swing.OptionDialog.showMessage
                (org.nlogo.app.App.app().frame(),
                    "GoGo Extension has been updated!",
                    "This model is using a primitive (gogo:" + primitiveName + ") from the old version of the GoGo extension.  Use gogo-serial for older GoGo boards.",
                    new String[]{"More Information", "Close"});
        if (choice == 1) {
          break;
        }
        org.nlogo.swing.BrowserLauncher.openURL
          (org.nlogo.app.App.app().frame(), "https://github.com/NetLogo/NetLogo/wiki/GoGo-Upgrade", false);
      }
      System.getProperties().put("org.nlogo.gogo.shownErrorMessage", "");
      shownErrorMessage = true;
    }
  }

  private class OldReporter implements Reporter {
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

  private class OldCommand implements Command {
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

    shownErrorMessage = System.getProperties().containsKey("org.nlogo.gogo.shownErrorMessage");

    pm.addPrimitive("primitives", new Prims() );

    pm.addPrimitive("beep", new Beep() );

    pm.addPrimitive("read-sensors", new ReadSensors() );
    pm.addPrimitive("read-sensor", new ReadSensorNumber() );

    pm.addPrimitive("led", new LED() );

    pm.addPrimitive("talk-to-output-ports",new TalkToOutputPort() );
    pm.addPrimitive("set-output-port-power", new SetOutputPortPower() );
    pm.addPrimitive("output-port-on", new OutputPortOn() );
    pm.addPrimitive("output-port-off", new OutputPortOff() );
    pm.addPrimitive("output-port-clockwise", new OutputPortClockwise() );
    pm.addPrimitive("output-port-counterclockwise", new OutputPortCounterClockwise() );

    pm.addPrimitive("set-servo", new SetServo() );

    pm.addPrimitive("read-all", new ReadAll() );
    pm.addPrimitive("send-bytes", new SendBytes() );
    pm.addPrimitive("howmany-gogos", new Enumerate() );

    // Old Primitives that should let you know we've changed the extension
    pm.addPrimitive("ports", new OldReporter("ports", SyntaxJ.reporterSyntax(Syntax.ListType())));
    pm.addPrimitive("burst-value", new OldReporter("burst-value", SyntaxJ.reporterSyntax(new int[] {Syntax.NumberType()}, Syntax.NumberType())));
    pm.addPrimitive("close", new OldCommand("close", SyntaxJ.commandSyntax()));
    pm.addPrimitive("install", new OldCommand("install", SyntaxJ.commandSyntax()));
    pm.addPrimitive("led-off", new OldCommand("led-off", SyntaxJ.commandSyntax()));
    pm.addPrimitive("led-on", new OldCommand("led-on", SyntaxJ.commandSyntax()));
    pm.addPrimitive("open", new OldCommand("open", SyntaxJ.commandSyntax(new int[] {Syntax.StringType()})));
    pm.addPrimitive("open?", new OldReporter("open?", SyntaxJ.reporterSyntax(Syntax.BooleanType())));
    pm.addPrimitive("output-port-coast", new OldCommand("output-port-coast", SyntaxJ.commandSyntax()));
    pm.addPrimitive("output-port-reverse", new OldCommand("output-port-reverse", SyntaxJ.commandSyntax()));
    pm.addPrimitive("output-port-thatway", new OldCommand("output-port-thatway", SyntaxJ.commandSyntax()));
    pm.addPrimitive("output-port-thisway", new OldCommand("output-port-thisway", SyntaxJ.commandSyntax()));
    pm.addPrimitive("ping", new OldReporter("ping", SyntaxJ.reporterSyntax(Syntax.BooleanType())));
    pm.addPrimitive("sensor", new OldReporter("sensor", SyntaxJ.reporterSyntax(new int[] {Syntax.NumberType()}, Syntax.NumberType())));
    pm.addPrimitive("set-burst-mode", new OldCommand("set-burst-mode", SyntaxJ.commandSyntax(new int[] {Syntax.ListType(), Syntax.BooleanType()})));
    pm.addPrimitive("stop-burst-mode", new OldCommand("stop-burst-mode", SyntaxJ.commandSyntax()));

    try {
      bootHIDDaemon();
    } catch (Exception e) {
      System.err.println("FAILED TO BOOT DAEMON");
      e.printStackTrace();
      throw new ExtensionException("Error in loading HID services");
    }

  }

  @Override public void unload(ExtensionManager pm) throws ExtensionException {
    proc.destroy();
  }

  private OutputStream os = null;
  private InputStream is = null;
  private InputStream err = null;
  private Process proc = null;
  private boolean stillRunning = false;

  private String userJavaPath(String defaultJava) {
    if (org.nlogo.app.App$.MODULE$ != null && org.nlogo.app.App$.MODULE$.app() != null) {
      try {
        return FileDialog.showFiles(org.nlogo.app.App$.MODULE$.app().frame(),
            "Please locate your java executable", java.awt.FileDialog.LOAD);
      } catch (UserCancelException e) {
        System.out.println("User canceled java location, using default java");
      }
    } else {
      System.out.println("NetLogo is headless, using default java");
    }
    return defaultJava;
  }

  private String javaExecutablePath() {
    String osName = System.getProperty("os.name").toLowerCase();
    System.err.println(osName);
    if (osName.contains("mac")) {
      return userJavaPath("/usr/bin/java");
    } else if (osName.contains("windows")) {
      return userJavaPath("java.exe");
    } else {
      return userJavaPath("java");
    }
  }

  private void bootHIDDaemon() throws ExtensionException{
    System.out.println("looking for system java, override by setting property " + javaLocationPropertyKey);
    String executable = System.getProperty(javaLocationPropertyKey);
    if (executable == null) {
      executable = javaExecutablePath();
    }
    String gogoExtensionPath =
      System.getProperty("netlogo.extensions.dir", "extensions") + File.separator + "gogo" + File.separator;
    try {
      String classpath =
        new File(gogoExtensionPath + "gogo-daemon.jar").getCanonicalPath() + File.pathSeparator +
        new File(gogoExtensionPath + "hid4java.jar").getCanonicalPath() + File.pathSeparator +
        new File(gogoExtensionPath + "jna.jar").getCanonicalPath();
      List<String> command = Arrays.asList(executable, "-classpath", classpath, "-showversion", "gogoHID.daemon.HIDGogoDaemon");
      System.out.println("running: " + Arrays.toString(command.toArray()));
      proc = new ProcessBuilder(command).start();
      System.setProperty(javaLocationPropertyKey, executable);
      stillRunning = true;
      os = proc.getOutputStream();
      is = proc.getInputStream();
      err = proc.getErrorStream();
      System.out.println("Booted daemon!");

      new Thread() {
        public void run() {
          try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(err));
            String line = null;
            while((line = reader.readLine()) != null) {
              System.err.println(line);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }.start();
      new Thread() {
        public void run() {
          try {
            proc.waitFor();
            stillRunning = false;
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }.start();
    } catch(Exception e) {
      System.out.println("ERROR BOOTING DAEMON:");
      System.out.println(e.getClass());
      System.out.println(e.getMessage());
      e.printStackTrace();
      throw new ExtensionException("Couldn't boot daemon");
    }
  }

  private void sendMessage(byte[] bytes) throws ExtensionException {
    try {
      if(!stillRunning) {
        bootHIDDaemon();
      }
      os.write('S');
      os.write(bytes.length);
      os.write(bytes);
      os.flush();
    } catch(IOException e) {
      throw new ExtensionException("Couldn't write message to daemon");
    }
  }

  private byte[] receiveMessage(int numBytes) throws ExtensionException, UnsuccessfulReadOperation {
    try {
      if(!stillRunning) {
        bootHIDDaemon();
      }
      os.write('R');
      os.write(numBytes);
      os.flush();

      byte[] data = new byte[numBytes];
      is.read(data);
      return data;
    } catch(IOException e) {
      throw new ExtensionException("Couldn't write message to daemon");
    }
  }

  private int numAttached() throws ExtensionException {
    try {
      if(!stillRunning) {
        bootHIDDaemon();
      }
      os.write('N');
      os.flush();
      return is.read();
    } catch (IOException e) {
      throw new ExtensionException("Couldn't write message to daemon");
    }
  }

  private class Prims implements Reporter {
    @Override
    public Syntax getSyntax() {
        return SyntaxJ.reporterSyntax(Syntax.ListType());
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
      llb.add("talk-to-output-ports <List of output letter(s) to talk to> {e.g, [\"A\"] for port A, [\"A\" \"B\" \"D\" ] for ports A, B, and D}");
      llb.add("set-output-port-power <power value 0-100 for current output-port (see talk-to-output-ports)> {0 = full off; 100 = full on.  Sets board value to <power>% of 255.}");
      llb.add("output-port-on {turns current output-port (see talk-to-output-ports) on}");
      llb.add("output-port-off {turns current output-port (see talk-to-output-ports) off}");
      llb.add("output-port-clockwise {makes current output-port (see talk-to-output-ports) go clockwise}");
      llb.add("output-port-counterclockwise {makes current output-port (see talk-to-output-ports) go counterclockwise}");
      llb.add("set-servo <position> {makes current servo (see talk-to-output-ports) move to position <position> 0-255}");
      llb.add("----utility functions---");
      llb.add("read-all <returns entire byte sequence of HID device's status message> {converts to byte digits to hex strings}");
      llb.add("send-bytes <sends any byte sequence to the gogo> {list of integers will get padded out with zeroes.}");
      llb.add("howmany-gogos <returns number of HID gogos currently connected>");
      return llb.toLogoList();
    }
  }

  private class ReadSensors implements Reporter {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(Syntax.ListType());
    }

    @Override
    public Object report(Argument[] arg0, Context arg1)
        throws ExtensionException, LogoException {
      short[] sensors = new short[8];
      try {
        byte[] data = receiveMessage(64);
        for (int index = 0; index < 8; index++) {
          ByteBuffer bb = ByteBuffer.wrap(data,(2*index) + 1,2 );
          bb.order(ByteOrder.BIG_ENDIAN);
          sensors[index] = bb.getShort();
        }
      } catch(UnsuccessfulReadOperation e) {
        System.err.println("Couldn't read from gogo board");
      }
      LogoListBuilder llb = new LogoListBuilder();
      for (int i = 0; i< sensors.length; i++) {
        llb.add(Double.valueOf(sensors[i]) );
      }
      return llb.toLogoList();
    }
  }

  private class ReadSensorNumber implements Reporter {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(new int[] {Syntax.NumberType() }, Syntax.NumberType());
    }

    @Override
    public Object report(Argument[] args, Context arg1)
        throws ExtensionException, LogoException {
      int sensorNumber = args[0].getIntValue();
      if ((sensorNumber > 8) || (sensorNumber < 1)) {
        throw new ExtensionException("Sensor Number must be between 1 and 8 (inclusive).  You entered " + sensorNumber);
      }
      Short sensorReading = 0;
      try {
        byte[] data = receiveMessage(64);

        ByteBuffer bb = ByteBuffer.wrap(data,(2*(sensorNumber-1)) + 1,2 );
        bb.order(ByteOrder.BIG_ENDIAN);
        sensorReading = bb.getShort();
      } catch(UnsuccessfulReadOperation e) {
        System.err.println("Couldn't read from gogo board");
      }

      return sensorReading.doubleValue();
    }
  }

  private class Beep implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax();
    }

    @Override
    public void perform(Argument[] arg0, Context arg1)
        throws ExtensionException, LogoException {
      byte[] message = new byte[64];
      message[0] = (byte)0;
      message[1] = (byte)11;
      message[2] = (byte)0;
      message[3] = (byte)0;
      sendMessage(message);
    }
  }

  private class LED implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax(new int[] {Syntax.NumberType() });
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

      sendMessage(message);
    }
  }

  private class TalkToOutputPort implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax(new int[] {Syntax.ListType() });
    }

    @Override
    public void perform(Argument args[], Context context)
        throws ExtensionException, org.nlogo.api.LogoException {
      java.util.Iterator<?> iter = args[0].getList().javaIterator();

      int outputPortMask = 0;
      while (iter.hasNext()) {
        Object val = iter.next();
        switch (val.toString().toLowerCase().charAt(0)) {
          case 'a':
          case 'A':
            outputPortMask = outputPortMask  | 1;
            break;
          case 'b':
          case 'B':
            outputPortMask = outputPortMask  | 2;
            break;
          case 'c':
          case 'C':
            outputPortMask = outputPortMask  | 4;
            break;
          case 'd':
          case 'D':
            outputPortMask = outputPortMask  | 8;
            break;
        }
      }

      byte[] message = new byte[64];
      message[0] = (byte)0;
      message[1] = (byte)7;
      message[2] = (byte)outputPortMask;
      message[3] = (byte)0;
      sendMessage(message);
    }
  }

  private class SetOutputPortPower implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax(new int[] {Syntax.NumberType() });
    }

    @Override
    public void perform(Argument[] args, Context ctx)
        throws ExtensionException, LogoException {
      int outputPortPowerVal = args[0].getIntValue();
      byte[] message = new byte[64];
      message[0] = (byte)0;
      message[1] = (byte)6;
      message[2] = (byte)0;
      message[3] = (byte)0; //high byte
      message[4] = (byte)outputPortPowerVal;
      sendMessage(message);
    }
  }

  private class OutputPortOn implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax();
    }

    @Override
    public void perform(Argument[] args, Context ctx)
        throws ExtensionException, LogoException {
      byte[] message = new byte[64];
      message[0] = (byte)0;
      message[1] = (byte)2;
      message[2] = (byte)0;
      message[3] = (byte)1; //on
      sendMessage(message);
    }
  }

  private class OutputPortOff implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax();
    }

    @Override
    public void perform(Argument[] args, Context ctx)
        throws ExtensionException, LogoException {
      byte[] message = new byte[64];
      message[0] = (byte)0;
      message[1] = (byte)2;
      message[2] = (byte)0;
      message[3] = (byte)0; //off

      sendMessage(message);
    }
  }


  private class OutputPortClockwise implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax();
    }

    @Override
    public void perform(Argument[] arg0, Context arg1)
        throws ExtensionException, LogoException {
      byte[] message = new byte[64];
      message[0] = (byte)0;
      message[1] = (byte)3;
      message[2] = (byte)0;
      message[3] = (byte)1;  //cw

      sendMessage(message);
    }
  }

  private class OutputPortCounterClockwise implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax();
    }

    @Override
    public void perform(Argument[] arg0, Context arg1)
        throws ExtensionException, LogoException {
      byte[] message = new byte[64];
      message[0] = (byte)0;
      message[1] = (byte)3;
      message[2] = (byte)0;
      message[3] = (byte)0;  //ccw
      sendMessage(message);
    }
  }

  private class SetServo implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax(new int[] {Syntax.NumberType() });
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
      sendMessage(message);
    }
  }

  private class SendBytes implements Command {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.commandSyntax(new int[] { Syntax.ListType() });
    }

    @Override
    public void perform(Argument[] args, Context ctx)
        throws ExtensionException, LogoException {
      LogoList messageList = args[0].getList();
      byte[] message = new byte[64];
      for (int i = 0; i<messageList.size() && i < 64; i++) {
        Object val = messageList.get(i);
        int v = (Integer)val;
        message[i] = (byte)v;
      }
      sendMessage(message);
    }
  }

  private class ReadAll implements Reporter {
    @Override
    public Syntax getSyntax() {
        return SyntaxJ.reporterSyntax(Syntax.ListType());
      }

    @Override
    public Object report(Argument[] arg0, Context arg1)
        throws ExtensionException, LogoException {
      LogoListBuilder llb = new LogoListBuilder();
      try {
        byte[] data = receiveMessage(64);
        for (int i = 0; i< data.length; i++) {
          llb.add( byteToHex(data[i]) );
        }
      } catch(UnsuccessfulReadOperation e) {
        System.err.println("Couldn't read from gogo board");
      }
      return llb.toLogoList();
    }
  }

  private class Enumerate implements Reporter {
    @Override
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(Syntax.NumberType());
    }

    @Override
    public Object report(Argument[] arg0, Context arg1)
        throws ExtensionException, LogoException {
      return (double) numAttached();
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
}
