package freert.main;

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

import gui.varie.JComponentProvider;
import gui.varie.LimitedTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


/**
 * This class handles Logging and MUST be started as early as possible
 * It IS essential that the logger do NOT use anything of the whole system.
 * NOTE: Logging IS different than debugging, if you wish to have selective debugging you MUST
 * implement your own class that does what is needed.
 */
public final class WindowEventsLog implements JComponentProvider
  {
  private static final int REDIRECT_OUT=1;
  private static final int REDIRECT_ERROR=2;
  private static final int LINES_MAX=1000;
  private static final int progress_max = 25;
  
  private final JFrame logFrame;

  private JFrame       feedbackFrame;       // Used to give some feedback on the loading
  private JProgressBar progressBar;
  private JTextField   progressField;       // You can write a number here to show the progress
  private boolean      exitOnWindowClose = true; 

  private LimitedTextArea outArea,errArea,userArea;
  private JTabbedPane tabbedPane;

  private volatile boolean bootComplete=false;
  private volatile int exitCode;      // This is the exit code that I should use to exit.
  
  /**
   * Constructor.
   * Package access.
   * There is an option to avoid out/err redirect since it may happen that the mechanism do not work due to
   * a swing thread lockup. (Basically there is the write but you never see)
   * In such extreme case (that it may happen) you start the program using the command line and ask NOT to redirect out/err
   */
  WindowEventsLog ( boolean redirectOutErr )
    {
    logFrame = newOutputFrame();

    if ( redirectOutErr ) 
      {
      RedirectMessage redirOut = new RedirectMessage(REDIRECT_OUT);
      redirOut.setName("stdout redirect");
      redirOut.start();
      
      RedirectMessage redirErr = new RedirectMessage(REDIRECT_ERROR);
      redirErr.setName("stderr redirect");
      redirErr.start();
      }

    newBootFeedbackFrame();
    }


  private JPanel newProgressStepPanel ()
    {
    JPanel risul = new JPanel(new FlowLayout(FlowLayout.LEFT,1,1));
    risul.add(new JLabel("Step"));
    progressField = new JTextField(5);
    risul.add(progressField);
    return risul;
    }

  /**
   * Creates the new feedback frame that will tell the user that something is happening.
   */
  private void newBootFeedbackFrame()
    {
    feedbackFrame = new JFrame ("Boot .. ");
    ImageIcon icon = new ImageIcon (getClass().getResource("log.gif"));
    feedbackFrame.setIconImage(icon.getImage());
    feedbackFrame.addWindowListener(new CloserListener());

    JPanel workPanel = (JPanel)feedbackFrame.getContentPane();
    progressBar = new JProgressBar();
    progressBar.setMinimum(0);
    progressBar.setMaximum(progress_max);
    
    workPanel.add(new JLabel("Program is starting"),BorderLayout.NORTH);
    workPanel.add(progressBar,BorderLayout.CENTER);
    workPanel.add(newProgressStepPanel(),BorderLayout.SOUTH);
    
    feedbackFrame.pack();
    feedbackFrame.setLocationRelativeTo(null);    
    feedbackFrame.setVisible(true);

    // Time to start the timer that will show the eal log if something goes wrong.    
    new Thread(new BootingProgress()).start();
    }

  /**
   * Setup a simple frame where I can redirect output and error when needed.
   * This also sets the Look and Feel
   */
  private JFrame newOutputFrame ()
    {
    outArea = new LimitedTextArea(LINES_MAX);
    errArea = new LimitedTextArea(LINES_MAX);
    userArea = new LimitedTextArea(LINES_MAX);
    
    // I have too many of them and are used not together, I hope :-)
    tabbedPane = new JTabbedPane();
    tabbedPane.add("User",userArea.getComponentToDisplay());
    tabbedPane.add("Output",outArea.getComponentToDisplay());
    tabbedPane.add("Error",errArea.getComponentToDisplay());

    JFrame aFrame = new JFrame ("Boot Log");
    ImageIcon icon = new ImageIcon (getClass().getResource("log.gif"));
    aFrame.setIconImage(icon.getImage());
    aFrame.addWindowListener(new CloserListener());
    aFrame.getContentPane().add(tabbedPane);

    aFrame.setSize (500, 400);
    aFrame.setLocationRelativeTo(null);    
    
    return aFrame;
    }

  /**
   * Returns the tabbed pane that holds all the info, if you want to put it
   * somewhere else than in the standard log window.
   */
  public JComponent getComponentToDisplay ()
    {
    return tabbedPane;
    }
    
  /**
   * Sets the exit code that boot should use to exit.
   * If you install a security manager that checks for System.exit you have to allow
   * this exit otherwise it will be impossible to exit on error.
   * Make It a random so libraries cannot try to sneak past an exit.
   */
  public void setExitCode ( int exitCode )
    {
    this.exitCode = exitCode;
    }
    
  /**
   * This is used to either hide or show the log window.
   * Since this is something that the main program does it also means that
   * the main program is alive and therefore the feedback window/timer must stop.
   */
  public void setVisible ( boolean showFrame )
    {
    logFrame.setVisible(showFrame);
    
    bootComplete = true;
    feedbackFrame.setVisible(false);
    exitOnWindowClose = false;
    }

  /**
   * Stems are numbers, they can even jump backward, it is duty of the programmer to
   * have meaningful steps...
   * @param step_index
   */
  public void setProgressStep ( int step_index )
    {
    progressField.setText(Integer.toString(step_index));
    }
  
  /**
   * If you want to print to the user area, this is for you.
   */
  public final void userPrintln ( String message )
    {
    userArea.println(message);
    }


  /**
   * Prints an exception.
   * It is printed using System.err.println(), so it will go to the "console" if you do not redired out/err to a gui.
   * The idea is that if you are tracing and want to see the exception than this MUST work !.
   */
  public final void exceptionPrint ( String message, Exception exc )
    {
    System.err.println(exceptionExpand(message,exc));
    }


  /**
   * This expands a message and an exception into something that has both
   * the message, the exception message and the stack trace.
   */
  public final String exceptionExpand ( String message, Exception exc )
    {
    StringBuffer aBuffer = new StringBuffer(1000);
    
    aBuffer.append("User Message=");
    aBuffer.append(message);
    aBuffer.append("\n");
    
    if ( exc == null )
      {
      aBuffer.append("Exception=(null)\n");
      }
    else
      {
      aBuffer.append("Exception Class="+exc.getClass());
      aBuffer.append("\n");

      aBuffer.append("--> toString=");
      aBuffer.append (exc.toString());
      aBuffer.append("\n");
      
      aBuffer.append("--> Stack=");
      fillStackTrace(aBuffer,exc.getStackTrace());
      aBuffer.append("\n");
      }

    return aBuffer.toString();
    }


  /**
   * Creates a printing stacktrace from the given elements.
   * @param elements the StacktraceElements to add to the buffer.
   * @param buffer the string buffer where to add the elements.
   */
  private void fillStackTrace (StringBuffer buffer,  StackTraceElement[] elements )
    {
    for ( int index=0; index<elements.length; index++ )
      {
      buffer.append(elements[index]);
      buffer.append("\n");
      }
    }


  /**
   * This will SHOW an exception in terms of popup window.
   * The user will be really aware about it !
   * It returns true if there was an exception to show, false othervise.
   * Note that if there is no exception then nothing will be done.
   */
  public final boolean exceptionShow ( String message, Exception exc )
    {
    if ( exc == null ) return false;

    String showMsg = exceptionExpand (message,exc);
    JTextArea msgArea = new JTextArea(showMsg,10,75);    // It it a text area so you can COPY the message !
    msgArea.setLineWrap(true);
    JScrollPane scrollPane = new JScrollPane(msgArea);
    
    if ( SwingUtilities.isEventDispatchThread() )
      JOptionPane.showMessageDialog(logFrame,scrollPane,"Copiate questo errore",JOptionPane.ERROR_MESSAGE);
    else
      SwingUtilities.invokeLater(new SwingShowMessage(scrollPane,JOptionPane.ERROR_MESSAGE));    
      
    return true;
    }

  /**
   * Shows the given message to the user in a reliable way.
   * @param message the message to show.
   * @return true meaning that a message was shown.
   */
  public boolean messageShow ( String message )
    {
    JTextArea msgArea = new JTextArea(message,10,75);    // It it a text area so you can COPY the message !
    msgArea.setLineWrap(true);
    JScrollPane scrollPane = new JScrollPane(msgArea);
    
    if ( SwingUtilities.isEventDispatchThread() )
      JOptionPane.showMessageDialog(logFrame,scrollPane,"Copiate questo messaggio",JOptionPane.INFORMATION_MESSAGE);
    else
      SwingUtilities.invokeLater(new SwingShowMessage(scrollPane,JOptionPane.INFORMATION_MESSAGE));    

    return true;
    }



/**
 * A simple utility class to do things properly.
 * The issue here is to queue all the message so they do not HANG the swing thread.
 * So, basically, do NOT use JOptionPane.showMessageDialog alone, use this wrapper.
 */
private final class SwingShowMessage implements Runnable
  {
  private final Object message;
  private final int messageType;
  
  SwingShowMessage ( Object message, int messageType )
    {
    this.message=message;
    this.messageType = messageType;
    }
  
  public void run ()
    {
    JOptionPane.showMessageDialog(logFrame,message,"(fix) Copiate questo messaggio",messageType);
    }
  }


/**
 * I use my handler so I am sure that I know when to close the program or not.
 */
private final class CloserListener extends WindowAdapter
  {
  public void windowClosing(WindowEvent event)
    {
    if ( exitOnWindowClose ) 
      {
      messageShow("Calling System.exit("+exitCode+")");
      System.exit(exitCode);
      }
    else
      {
      JFrame aFrame = (JFrame)event.getSource();
      aFrame.setVisible(false);
      }
    }
  }


private final class BootingProgress implements Runnable
  {
  public void run()
    {
    for (int index=0; index<progress_max; index++)
      {
      progressBar.setValue(index);

      try { Thread.sleep(500); } 
      catch ( Exception exc ) { exc.printStackTrace(); }
      
      if ( bootComplete ) return;
      }

    logFrame.setVisible(true);
    feedbackFrame.setVisible(false);
    }
  }

/**
 * This is the thread that copies all from what is written and sends it to the console.
 */
private final class RedirectMessage extends Thread
  {
  private final String classname="RedirectMessages.";
  private final int      redirectWhat;
  private BufferedReader inputReader;
  private LimitedTextArea  writeHere;
    
  RedirectMessage ( int redirectWhat )
    {
    this.redirectWhat = redirectWhat;
    }

  /**
   * The thread starts here.
   */
  public void run () 
    {
    for (;;)
      {
      bindStreams();
      copyData();
      }
    }

  /**
   * This tryes to bind the streams and the windows.
   * If this fails I need to know about it.
   */
  private void bindStreams()
    {
    try
      {
      PipedOutputStream sendDataHere = new PipedOutputStream();
      PipedInputStream  readDataHere = new PipedInputStream(sendDataHere);

      InputStreamReader readDataStream = new InputStreamReader(readDataHere);
      inputReader = new BufferedReader(readDataStream);

      PrintStream sendPrintHere = new PrintStream (sendDataHere);

      if ( redirectWhat == REDIRECT_OUT )
        {
        writeHere = outArea;
        System.setOut(sendPrintHere);
        }
        
      if ( redirectWhat == REDIRECT_ERROR )
        {
        writeHere = errArea;
        System.setErr(sendPrintHere);
        }
      }
    catch ( Exception exc )
      {
      // This is really something that shold never happen and therefore I want to know about it
      exceptionShow (classname+"bindStreams: Exception=",exc);  
      }
    }

  /**
   * This copies data, when some exception occours it will come out of it
   */
  private void copyData ()
    {
    try
      {
      String aLine;
      while ( (aLine=inputReader.readLine()) != null ) writeHere.println(aLine);
      }
    catch ( Exception exc )
      {
      // This is produced even by a simple "close" on stdout, so I cannot use exceptionShow()
      // No point to do a full stack trace, it is always the same and due to the close.
      // A simple message is enough.
//      exceptionPrint (classname+"copyData()",exc);  
      System.err.println(classname+"copyData() input closed");
      }
    }
  }

  } // END




