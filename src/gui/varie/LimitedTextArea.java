package gui.varie;

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

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;


/**
 * It happens quite often that you need a text area that has a limit on the number of lines
 * you can put in it, it is useful when you have some sort of logging and you want to avoid it to fill up
 * the entire system memory
 */
public final class LimitedTextArea implements JComponentProvider
  {
  private final JScrollPane workPanel;
  private final JTextArea textArea;
  private final PlainDocument workDoc;
  private final int linesMax;

  /**
   * Construct a given limited text area that holds at max lineMax lines.
   * @param linesMax
   */
  public LimitedTextArea(int linesMax)
    {
    this.linesMax = linesMax;
    
    // This is for user activity
    textArea = new JTextArea(10,40);
    textArea.setLineWrap(true);
    // Do not setEditable(false), it is useful to be able to insert a few CR by hand
    workPanel = new JScrollPane(textArea);
    
    workDoc = (PlainDocument)textArea.getDocument();    
    }

  /**
   * implements ComponentProvider.
   * @return a component that is the visual part of this object.
   */
  public final JComponent getComponentToDisplay()
    {
    return workPanel;
    }
  
  public LimitedTextArea setRows ( int rows )
    {
    textArea.setRows(rows);
    return this;
    }
    
  public LimitedTextArea setColumns ( int columns )
    {
    textArea.setColumns(columns);
    return this;
    }
  
  /**
   * If you wish to add a listener for keystrokes or anything else you need the text area.
   * But you should behave nicely :-)
   * @return 
   */
  public final JTextArea getTextArea ()
    {
    return textArea;
    }
    
  /**
   * Print unprintable chars like /n or /r or something else that is < ' ' or > 'z'
   * in a (cal) form and also prints the actual char so you may "see" it.
   * @param value a string to print, if null is given nothing will be printed.
   */
  public final void printUnprintable ( String value )
    {
    if ( value == null ) return;
    
    byte [] byteVal = value.getBytes();
    int letti = byteVal.length;

    // We guess that the result should fit in a double size...
    StringBuffer risul = new StringBuffer(letti*2);
    
    for (int index=0; index<letti; index++ )
      {
      byte ch = byteVal[index];
  
      if ( ch < ' ' || ch > 'z' ) risul.append("("+ch+")");

      risul.append((char)ch);
      }
      
    print ( risul.toString() );
    }
    
  /**
   * Prints a string without adding a /n at the end.
   * @param value
   */
  public final void print ( String value )
    {
    try
      {
      Element rootElement = workDoc.getDefaultRootElement();
      // Shorten the document until is is shorter than linesMax lines
      int deleteLines = rootElement.getElementCount() - linesMax;
      for (int index=0; index<deleteLines; index++ )    
        {
        Element elem = rootElement.getElement(0);
        workDoc.remove(elem.getStartOffset(),elem.getEndOffset());
        }

      // Then insert the new message.    
      workDoc.insertString(workDoc.getLength(),value,null);
      textArea.setCaretPosition(workDoc.getLength());
      }
    catch ( Exception exc )
      {
      exc.printStackTrace();
      }
    }


  /**
   * Prints the given message in the text area.
   * Before printing it it makes sure that there are less linesMax in the current area.
   * After the printing there may be more than linesMax, but the next print will trim them.
   */
  public final void println ( String message )
    {
    try
      {
      Element rootElement = workDoc.getDefaultRootElement();
      // Shorten the document until is is shorter than linesMax lines
      int deleteLines = rootElement.getElementCount() - linesMax;
      for (int index=0; index<deleteLines; index++ )    
        {
        Element elem = rootElement.getElement(0);
        workDoc.remove(elem.getStartOffset(),elem.getEndOffset());
        }

      // Then insert the new message.    
      workDoc.insertString(workDoc.getLength(),message,null);
      textArea.setCaretPosition(workDoc.getLength());
      workDoc.insertString(workDoc.getLength(),"\n",null);
      }
    catch ( Exception exc )
      {
      exc.printStackTrace();
      }
    }
    
  }