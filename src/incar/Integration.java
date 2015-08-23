/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package incar;

/**
 *
 * @author aniket
 */

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolViewer;
import org.jmol.util.Logger;
import org.openscience.jmol.app.jmolpanel.console.AppConsole;

public class Integration {
    
    private String src;
    private String strScript;
    JmolPanel jmolPanel = new JmolPanel();

    int cnt;
    MainPanel parent;
    public Integration(String s, MainPanel par, int counter)
    {
        src = s;
        parent = par;
        cnt = counter;
    }
    
    public void exec(String cmd)
    {
        strScript = cmd;
        jmolPanel.viewer.evalString(strScript);

    }
    
    public void myJmolViewer() {
        JFrame frame = new JFrame("Welcome");
        frame.addWindowListener(new ApplicationCloser());
        frame.setSize(500, 700);
        Container contentPane = frame.getContentPane();
        jmolPanel.setPreferredSize(new Dimension(400, 400));

        // main panel -- Jmol panel on top

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(jmolPanel);

        // main panel -- console panel on bottom

        JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout());
        panel2.setPreferredSize(new Dimension(400, 300));
        AppConsole console = new AppConsole(jmolPanel.viewer, panel2,
            "History State Clear");

        // You can use a different JmolStatusListener or JmolCallbackListener interface
        // if you want to, but AppConsole itself should take care of any console-related callbacks
        jmolPanel.viewer.setJmolCallbackListener(console);

        panel.add("South", panel2);

        contentPane.add(panel);
        frame.setVisible(true);

        // sample start-up script

        String strError = jmolPanel.viewer
            .openFile(src);
        //viewer.openStringInline(strXyzHOH);
        if (strError == null)
        {}
        else
          Logger.error(strError);
  }

    
    
    
    
    
    
    
    
    
    
    
    
    
    


  class ApplicationCloser extends WindowAdapter {
    @Override
    public void windowClosing(WindowEvent e) {
      parent.Reset2(cnt);
      return;
    }
  }

    class JmolPanel extends JPanel {

    JmolViewer viewer;
    
    private final Dimension currentSize = new Dimension();
    
    JmolPanel() {
      viewer = JmolViewer.allocateViewer(this, new SmarterJmolAdapter(), 
          null, null, null, null, null);
    }

    @Override
    public void paint(Graphics g) {
      getSize(currentSize);
      viewer.renderScreenImage(g, currentSize.width, currentSize.height);
    }
  }

}
