/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package incar;
import java.awt.Color;
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


/**
 *
 * @author aniket
 */
public class INCAR {

    /**
     * @param    args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try
        {
           Thread.sleep(2000);
        }
        catch(Exception ex)
        {}
        MainPanel mainpanel = new MainPanel();
        mainpanel.setVisible(true);
        mainpanel.setLocationRelativeTo(null);
    }
}
