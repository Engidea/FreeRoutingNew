package gui.varie;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;

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
 * The editor button that brings up the dialog. We extend DefaultCellEditor for convenience, even though it mean we have to
 * create a dummy check box. Another approach would be to copy the implementation of TableCellEditor methods from the source code
 * for DefaultCellEditor.
 */
public class TableColorEditor extends DefaultCellEditor
   {

   private static final long serialVersionUID = 1L;

   public Color currentColor = null;

   public TableColorEditor(JButton b)
      {
      super(new JCheckBox()); // Unfortunately, the constructor
      // expects a check box, combo box,
      // or text field.
      editorComponent = b;
      setClickCountToStart(1); // This is usually 1 or 2.

      // Must do this so that editing stops when appropriate.
      b.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
               {
               fireEditingStopped();
               }
         });
      }

   protected void fireEditingStopped()
      {
      super.fireEditingStopped();
      }

   public Object getCellEditorValue()
      {
      return currentColor;
      }

   public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
      {
      ((JButton) editorComponent).setText(value.toString());
      currentColor = (Color) value;
      return editorComponent;
      }
   }
