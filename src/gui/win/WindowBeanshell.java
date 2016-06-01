package gui.win;

import gui.BoardFrame;
import gui.GuiSubWindowSavable;
import gui.varie.JComponentProvider;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import main.Stat;
import bsh.Interpreter;
import bsh.util.JConsole;


/*
 *  Copyright (C) 2014  Damiano Bolla  website www.engidea.com
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License at <http://www.gnu.org/licenses/> 
 *   for more details.
 *
 */


/**
 * This is used to manage giving commands to databse and see results.
 * It sohuld be instantiated later since it makes use of bits of stat.
 */
public class WindowBeanshell extends GuiSubWindowSavable implements JComponentProvider 
  {  
  private static final long serialVersionUID = 1L;
  private static final String classname="BshPanel.";
  private static final String source_file="config.bsh";
  
  private final Stat  stat;

  private final JFrame      workFrame;  
  private final JPanel      workPanel;  
  private final Interpreter bshInterp;
  private final JConsole    bshConsole;

  private JButton helpBtn;
  
  /**
   * What will be available is a panel where lanuages are managed.
   */
  public WindowBeanshell(BoardFrame p_board_frame)
    {
    super(p_board_frame);
    
    stat = board_frame.stat;
  
    workFrame = new JFrame ("Ammi Client Beanshell");
//    ImageIcon icon = new ImageIcon (getClass().getResource("log.gif"));
//    feedbackFrame.setIconImage(icon.getImage());

    workPanel = (JPanel)workFrame.getContentPane();

    bshConsole = new JConsole();
    workPanel.add(bshConsole,BorderLayout.CENTER); 
    workPanel.add(newButtons(),BorderLayout.NORTH);
    workFrame.setSize(500,500);
    
    bshInterp = new Interpreter(bshConsole);
    }

  /**
   * Init MUST be done in a different thread since it may hang up...
   */
  public void initialize ()
    {
    Thread athread = new Thread(new InitializeThread());
    athread.setName(classname+"initialize");
    athread.setPriority(Thread.MIN_PRIORITY);
    athread.start();
    }


  /**
   * Source a default configBsh script
   */
  private void sourceBeanshelllScript (Interpreter inter) throws Exception
    {
    File scriptFile = new File(source_file);
    if ( ! scriptFile.canRead() ) 
      {
      stat.log.userPrintln(classname+"sourceBeanshelllScript: "+source_file+" not available");
      return;
      }
      
    stat.log.userPrintln(classname+"sourceBeanshelllScript: reding "+source_file);
    inter.source(scriptFile.getCanonicalPath());
    }

  private JPanel newButtons ()
    {
    JPanel aPanel = new JPanel();
    helpBtn = new JButton("Help");
    helpBtn.addActionListener(new HelpClass());
    
    aPanel.add(helpBtn);
    
    return aPanel;
    }
    
    
    
  public Interpreter getInterpreter ()
    {
    return bshInterp;
    }

  public Object eval (String method)
    {
    try
      {
      return bshInterp.eval(method+"()");
      }
    catch ( Exception exc )
      {
      stat.log.exceptionShow(classname+"eval("+method+")",exc);
      return null;
      }
    }


  public Object eval (String method, String param)
    {
    try
      {
      return bshInterp.eval(method+"(\""+param+"\")");
      }
    catch ( Exception exc )
      {
      stat.log.exceptionShow(classname+"eval() method="+method+" param="+param,exc);
      return null;
      }
    }


  /**
   * Returns the panel to display.
   */
  public JComponent getComponentToDisplay ()
    {
    return workPanel;
    }
    
  /**
   * Display this console in a separate frame.
   */
  public void setVisible ( boolean visible )
    {
    workFrame.setVisible(visible);
    }
    
private final class InitializeThread implements Runnable
  {
  public void run()
    {
    try
      {
      bshInterp.set("stat",stat);
      bshInterp.set("board_frame",board_frame);
        
      sourceBeanshelllScript(bshInterp);
      }
    catch ( Exception exc )
      {
      stat.log.exceptionShow(classname+"BshPanel",exc);  
      }
    
    Thread aThread = new Thread(bshInterp);
    aThread.setName("BeanShell");
    aThread.setPriority(Thread.MIN_PRIORITY);

    // You NEED to provide the correct class loader to beanshell to see the classes correctly !
    // The following does NOT work
    //  BshClassManager mgr = bshInterp.getClassManager();
    //  mgr.setClassLoader(aLoader);
    aThread.setContextClassLoader(getClass().getClassLoader());

    aThread.start();
    }
  }

private final class HelpClass implements ActionListener
  {
  public void actionPerformed(ActionEvent e)
    {
    String msg = "oggetti disponibili\n"+
    "stat\n"+
    "board_frame\n";
    
    board_frame.showMessageDialog(msg,"Help");
    }
  }

  } 