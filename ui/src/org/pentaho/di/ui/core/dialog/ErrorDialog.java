/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.core.dialog;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.trans.step.BaseStepDialog;




/**
 * Dialog to display an error generated by a Kettle Exception.
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class ErrorDialog extends Dialog
{
	private static Class<?> PKG = ErrorDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private Label        wlDesc;
	private Text         wDesc;
    private FormData     fdlDesc, fdDesc;
		
	private Button wOK, wDetails, wCancel;

	private Shell  shell;
	private SelectionAdapter lsDef;
	private PropsUI props;

	private boolean	cancelled;

	// private LogChannelInterface	log;

	public ErrorDialog(Shell parent, String title, String message, Throwable throwable)
	{
		super(parent, SWT.NONE);
		
		throwable.printStackTrace();

		// this.log = new LogChannel("ErrorDialog");
		//  log.logError(message, throwable);
		
		if (throwable instanceof Exception) {
			showErrorDialog(parent, title, message, (Exception)throwable, false);
		} else {
			// not optimal, but better then nothing
			showErrorDialog(parent, title, message + Const.CR + Const.getStackTracker(throwable), null, false);
		}
	}

	public ErrorDialog(Shell parent, String title, String message, Exception exception)
	{
		super(parent, SWT.NONE);
		showErrorDialog(parent, title, message, exception, false);
	}
	
	public ErrorDialog(Shell parent, String title, String message, Exception exception, boolean showCancelButton)
	{
		super(parent, SWT.NONE);
		showErrorDialog(parent, title, message, exception, showCancelButton);
	}

	
	private void showErrorDialog(Shell parent, String title, String message, Exception exception, boolean showCancelButton)
	{
		if (parent.isDisposed()) {
			exception.printStackTrace();
			return;
		}
		this.props = PropsUI.getInstance();

		Display display  = parent.getDisplay();
        final Font largeFont = GUIResource.getInstance().getFontBold();
		final Color gray = GUIResource.getInstance().getColorDemoGray();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN | SWT.APPLICATION_MODAL | SWT.SHEET);
 		props.setLook(shell);
 		shell.setImage(GUIResource.getInstance().getImageShowErrorLines());

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(title);
	
		int margin = Const.MARGIN;

		// From step line
		wlDesc=new Label(shell, SWT.NONE);
		wlDesc.setText(message);
 		props.setLook(wlDesc);
		fdlDesc=new FormData();
		fdlDesc.left = new FormAttachment(0, 0);
		fdlDesc.top  = new FormAttachment(0, margin);
		wlDesc.setLayoutData(fdlDesc);
        wlDesc.setFont(largeFont);
		
        wDesc=new Text(shell, SWT.MULTI  | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
        
        final StringBuffer text = new StringBuffer();
        final StringBuffer details = new StringBuffer();
        
		if (exception!=null) 
		{
			if (exception instanceof KettleException) // Normal error
			{
				KettleException ke = (KettleException) exception;
				Throwable cause = ke.getCause();
				if (cause != null){
					text.append(ke.getCause().getMessage());	
				}
				else{
					text.append(ke.getMessage());
				}
				
			}
			else
            // Error from somewhere else, what is the cause?
			if (exception instanceof InvocationTargetException) 
			{
				Throwable cause = exception.getCause();
				if (cause instanceof KettleException)
				{
					KettleException ke = (KettleException)cause;
                    text.append(ke.getMessage());
				}
				else
				{
                    text.append(Const.NVL(cause.getMessage(), cause.toString()));
					while (text.length()==0 && cause!=null)
					{
						cause = cause.getCause();
						if (cause!=null) 
						{
                            text.append(Const.NVL(cause.getMessage(), cause.toString()));
						}
					}
				}
			}
			else // Error from somewhere else...
			{
                if (exception.getMessage()==null)
                {
                    text.append(message);
                }
                else
                {
                    text.append(exception.getMessage());
                }
			}

			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			exception.printStackTrace(pw);
			
			details.append(sw.getBuffer());
			
			wDesc.setText( text.toString() );
		} else {
			text.append(message);
			wDesc.setText( text.toString() );
		}
		wDesc.setBackground(gray);
		fdDesc=new FormData();
		fdDesc.left  = new FormAttachment(0, 0);
		fdDesc.top   = new FormAttachment(wlDesc, margin);
		fdDesc.right = new FormAttachment(100, 0);
		fdDesc.bottom= new FormAttachment(100, -50);
		wDesc.setLayoutData(fdDesc);
		wDesc.setEditable(false);

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		if (showCancelButton) {
  		wCancel=new Button(shell, SWT.PUSH);
  		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
		}
        wDetails=new Button(shell, SWT.PUSH);
        wDetails.setText(BaseMessages.getString(PKG, "System.Button.Details"));

        Button[] buttons;
        if (showCancelButton) {
        	buttons = new Button[] { wOK, wCancel, wDetails, };
        } else {
        	buttons = new Button[] { wOK, wDetails, };
        }
        
        BaseStepDialog.positionBottomButtons(shell, buttons, margin, null);

		// Add listeners
		wOK.addListener     (SWT.Selection, new Listener() { public void handleEvent(Event e) { ok(); } });
		if (showCancelButton) {
		  wCancel.addListener     (SWT.Selection, new Listener() { public void handleEvent(Event e) { cancel(); } });
		}
		wDetails.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { showDetails(details.toString()); } });
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wDesc.addSelectionListener(lsDef);
		
		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { ok(); } } );
		// Clean up used resources!
		shell.addDisposeListener(new DisposeListener() 
			{
				public void widgetDisposed(DisposeEvent arg0) 
				{
				}
			}
		);
		
		BaseStepDialog.setSize(shell);
        
        // Set the focus on the "OK" button
        wOK.setFocus();

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
	}

	protected void showDetails(String details)
    {
        EnterTextDialog dialog = new EnterTextDialog(shell, BaseMessages.getString(PKG, "ErrorDialog.ShowDetails.Title"),
            BaseMessages.getString(PKG, "ErrorDialog.ShowDetails.Message"), details);
        dialog.setReadOnly();
        dialog.open();
    }

    public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	private void ok()
	{
		dispose();
	}
	
	private void cancel()
	{
		cancelled=true;
		dispose();
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
}