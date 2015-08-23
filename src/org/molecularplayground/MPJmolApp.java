/*
 * Copyright 2011 University of Massachusetts
 *
 * File: MPJmolApp.java
 * Description: Molecular Playground Jmol interface component/application
 * Author: Adam Williams
 *
 * See http://molecularplayground.org/
 * 
 * A Java application that listens over a port on the local host for 
 * instructions on what to display. Instructions come in over the port as JSON strings.
 * 
 * This class uses the Naga asynchronous socket IO package, the JSON.org JSON package and Jmol.
 * 
 * Adapted by Bob Hanson for Jmol 12.2
 *  
 * see JsonNioService for details.
 *   
 */
package org.molecularplayground;

import java.awt.Dimension;
import java.awt.Graphics;

import javajs.util.PT;

import javax.swing.JPanel;

import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolCallbackListener;
import org.jmol.api.JmolViewer;
import org.jmol.c.CBK;
import org.jmol.util.Logger;
import org.jmol.viewer.Viewer;
import org.openscience.jmol.app.jmolpanel.JmolPanel;
import org.openscience.jmol.app.jsonkiosk.BannerFrame;
import org.openscience.jmol.app.jsonkiosk.JsonNioClient;
import org.openscience.jmol.app.jsonkiosk.JsonNioServer;
import org.openscience.jmol.app.jsonkiosk.KioskFrame;

/*
 * Jmol 12 implementation of the Molecular Playground
 * 
 * includes message "banner:xxxxx" intercept to 
 * display xxxxx on the banner, thus allowing that to 
 * be modified by a running script. (in JsonNioService.java)

version=12.3.3_dev

# new feature: MolecularPlayground now accepts messages to the banner:
#   message banner: xxxxxxx
# new feature: MolecularPlayground fully functional. 
#              This application (MPJmolApp) is part of a three-part suite 
#              that runs on a Mac mini involving:
#                 "Hub" [name]   for overall control (Mac app; not open source yet)
#                 MPKinectDriver for obtaining motion events (Mac app; not open source yet)
#                 MPJmolApp      for displaying the results (source here)
#              The Hub and MPJmolApp communicate over local port 31416, sending 
#              JSON messages back and forth. (See note in org.jmol.app.jsonkiosk.JsonNioService.java)
#              For demonstrations of the installation at St. Olaf College,
#              see the following YouTube videos:
#                 http://www.youtube.com/watch?v=iQRkuku8ry0
#                 http://www.youtube.com/watch?v=XCRrRZe1j6g
#                 http://www.youtube.com/watch?v=FTTIVWGtFD0
#              For details relating to the original Molecular Playground
#              installation at U. Mass.-Amherst, see
#                 http://molecularPlayground.org
#              Note that all of the functionality of the original MP are
#              present in MPJmolApp 
# new feature: MolecularPLayground can now ignore all Hub requests for
#              commands and content changes, thus allowing its own configuration
#              script to drive the presentation instead of the Hub's. So the Hub
#              can be used simply as an interface to the Kinect driver. This just
#              allows a simpler development interface -- a simple three-column Excel file can
#              be used to drive a presentation. (see org.jmol.molecularplayground.biophysics.xlsx)
#   -- MPJmolApp looks for the file MpJmolAppConfig.spt
#   -- This file can override MPJmolApp's default parameters:
#          NIOContentPath 
#             -- default: System.getProperty("user.dir").replace('\\', '/') 
#                            + "/Content-Cache/%ID%/%ID%.json"
#             -- ignored if NIOcontentDisabled ends up true (see below)
#          NIOterminatorMessage
#             -- default: "MP_DONE"
#          NIOresetMessage
#             -- default: "MP_RESET"
#          NIObannerEnabled
#             -- default: true
#          NIOcontentScript
#             -- default: (not present, setting NIOcontentDisabled=false)
#          NIOcontentDisabled
#             -- default: true if NIOcontentScript is present; false if not
#          NIOmotionDisabled
#             -- default: false
#   -- The script in MpJmolAppConfig.spt is run, along with whatever
#      default settings are generated by the above checks.
#   -- Parameters are set by querying the Viewer for those Jmol variables. 
#   -- If NIOcontentDisabled is true, then all JSON messages from the Hub
#      of types "content", "command", and "banner" are ignored. It is still
#      important that the running script send "MP_DONE" messages periodically
#      (within every 6 minutes) so that the Hub knows that MPJmolApp is still
#      alive and does not try to restart it.
#   -- If NIOmotionDisabled is true, then all JSON messages from the Hub
#      of types "move", "sync", and "touch" are ignored. 
#   -- These are checked every time a JSON command is received, so the
#      running script can specifically turn off motion detection if that
#      or content detection if that is desired.
#   -- Note that MPJmolApp has a full console and menu that are available
#      on the operator's screen, (which is just mirrored to the projector).
#      This allows for parameter setting and adjustments on the fly.
#

 * 
 */
public class MPJmolApp implements JsonNioClient {

  protected Viewer viewer;

  private static int MP_VERSION = 1; // SET TO 2 if using Version 2 (AW 12/2011) 
  
  public static void main(String args[]) {
    new MPJmolApp(args.length > 0 ? Integer.parseInt(args[0]) : 31416);
  }

  public MPJmolApp() {
    this(31416);
  }
  
  public MPJmolApp(int port) {
    startJsonNioKiosk(port);
  }

  protected JsonNioServer service;
  private BannerFrame bannerFrame;
  private KioskFrame kioskFrame;
  private boolean contentDisabled;
  
  private void startJsonNioKiosk(int port) {
    KioskPanel kioskPanel = new KioskPanel();
    bannerFrame = new BannerFrame(1024, 75);
    kioskFrame = new KioskFrame(0, 75, 1024, 768 - 75, kioskPanel);
    try {
      setBannerLabel("click below and type exitJmol[enter] to quit");
      String defaultScript = "set allowgestures;set allowKeyStrokes;set zoomLarge false;set frank off;set antialiasdisplay off;";
      
      String script = "cd \"\"; " + viewer.getFileAsString3("MPJmolAppConfig.spt", false, null) + ";";
      Logger.info("startJsonNioKiosk on port " + port);
      Logger.info(script);
      if (script.indexOf("java.io") >= 0)
        script = "";
      String s = PT.rep(script.toLowerCase(), " ", "");
      if (s.indexOf("niocontentpath=") < 0) {
        String path = System.getProperty("user.dir").replace('\\', '/')
        + "/Content-Cache/%ID%/%ID%.json";
        script += "NIOcontentPath=\"" + path + "\";";
      }
      if (s.indexOf("nioterminatormessage=") < 0) {
        script += "NIOterminatorMessage='MP_DONE';";
      }
      if (s.indexOf("nioresetmessage=") < 0) {
        script += "NIOresetMessage='MP_RESET';";
      }
      if (s.indexOf("niobannerenabled=") < 0) {
        script += "NIObannerEnabled=true;";
      }
      if (s.indexOf("niocontentscript=") >= 0) {
        script += "NIOcontentDisabled=true;";
      } else {
        script += "NIOcontentScript='';NIOcontentDisabled=false;";
      }
      if (s.indexOf("niomotiondisabled=") < 0) {
        script += "NIOmotionDisabled=false;";
      }
      Logger.info("startJsonNioKiosk: " + defaultScript + script);
      viewer.scriptWait(defaultScript + script);
      contentDisabled = JmolViewer.getJmolValueAsString(viewer, "NIOcontentDisabled").equals("true");
      Logger.info("startJsonNioKiosk: contentDisabled=" + contentDisabled);
      
      service = JmolPanel.getJsonNioServer();
      if (service == null) {
        Logger.info("Cannot start JsonNioServer");
        System.exit(1);
      }
      service.startService(port, this, viewer, "-MP", MP_VERSION);

      // Bob's demo model -- verifies that system is working and networked properly
      viewer.script("load $caffeine");

    } catch (Throwable e) {
      e.printStackTrace();
      if (service == null)
        nioClosed(null);
      else
        service.close();
    }
  }

  /// JsonNiosClient ///

  private boolean haveStarted = false;
  @Override
  public synchronized void nioRunContent(JsonNioServer jns) {
    if (contentDisabled && (jns == null || !haveStarted)) {
      // needs to be run from the NIO thread, just once.
      String script = (jns == null ? "; message testing nioRun2; cd \"\"; script \"" + JmolViewer.getJmolValueAsString(viewer, "NIOcontentScript") + "\"" : "");
      haveStarted = true;
      script += ";cd \"\";cd;script \"" + JmolViewer.getJmolValueAsString(viewer, "NIOcontentScript") + "\"";
      System.out.println("nioRunContent " + Thread.currentThread() + " " + script);
      viewer.script(script);
      System.out.println("nioRunContent done");
    }
  }
  
  @Override
  public void setBannerLabel(String label) {
    bannerFrame.setLabel(label);
  }

  @Override
  public void nioClosed(JsonNioServer jns) {
    try {
      viewer.dispose();
      bannerFrame.dispose();
      kioskFrame.dispose();
    } catch (Throwable e) {
      //
    }
    System.exit(0);
  }


  ////////////////////////

  class KioskPanel extends JPanel implements JmolCallbackListener {

    private final Dimension currentSize = new Dimension();

    KioskPanel() {
      viewer = (Viewer) JmolViewer.allocateViewer(this, new SmarterJmolAdapter(),
          null, null, null, ""/*-multitouch-mp"*/, null);
      viewer.setJmolCallbackListener(this);
      // turn off all file-writing capabilities
      viewer.setBooleanProperty("isKiosk", true);
    }

    @Override
    public void paint(Graphics g) {
      getSize(currentSize);
      viewer.renderScreenImage(g, currentSize.width, currentSize.height);
    }

    // / JmolCallbackListener interface ///
    @Override
    public boolean notifyEnabled(CBK type) {
      switch (type) {
      case SCRIPT:
      case ECHO:
      case MESSAGE:
        return true;
      case ANIMFRAME:
      case APPLETREADY:
      case ATOMMOVED:
      case CLICK:
      case ERROR:
      case EVAL:
      case HOVER:
      case LOADSTRUCT:
      case MEASURE:
      case MINIMIZATION:
      case PICK:
      case RESIZE:
      case SYNC:
      case STRUCTUREMODIFIED:
      case DRAGDROP:
        break;
      }
      return false;
    }

    @Override
    public void notifyCallback(CBK type, Object[] data) {
      if (service == null || viewer == null)
        return;
      String strInfo = (data == null || data[1] == null ? null : data[1]
          .toString());
      switch (type) {
      case SCRIPT:
      case MESSAGE:
      case ECHO:
        // could be terminator or message banner:...
        service.scriptCallback(strInfo);
        JmolCallbackListener appConsole = (JmolCallbackListener) viewer
            .getProperty("DATA_API", "getAppConsole", null);
        if (appConsole != null)
          appConsole.notifyCallback(type, data);
        break;
      default:
        break;
      }
    }

    @Override
    public void setCallbackFunction(String callbackType, String callbackFunction) {
      // ignore
    }

  }

}