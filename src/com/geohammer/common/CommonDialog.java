package com.geohammer.common;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import com.geohammer.common.util.UiUtil;

public abstract class CommonDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	public int 			_width 				= 400;
	public int 			_height 			= 500;
	public boolean 		_enableOKButton 	= true;
	public boolean 		_enableApplyButton 	= false;
	public boolean 		_enableSaveButton 	= false;
	public String 		_applyButtonLabel 	= "Apply";
	public String 		_okButtonLabel 		= "OK";
	public String 		_saveButtonLabel 	= "Save";
	
	public CommonDialog(JFrame aParent, String aTitle, boolean modal) {
		super(aParent, aTitle, modal);
	}
	public void setEnableOKButton(boolean enableOKButton) 		{ _enableOKButton = enableOKButton; }
	public void setEnableApplyButton(boolean enableApplyButton) { _enableApplyButton = enableApplyButton; }
	public void setEnableSaveButton(boolean enableSaveButton) 	{ _enableSaveButton = enableSaveButton; }
	public void setApplyButtonLabel(String applyButtonLabel)  	{ _applyButtonLabel = applyButtonLabel; }
	public void setOKButtonLabel(String okButtonLabel)  		{ _okButtonLabel = okButtonLabel; }
	public void setSaveButtonLabel(String saveButtonLabel)  	{ _saveButtonLabel = saveButtonLabel; }
	
	public void showDialog(){
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setResizable(true);
		addCancelByEscapeKey();

		JPanel panel = new JPanel();
		panel.setLayout( new BorderLayout() );
		panel.setBorder( UiUtil.getStandardBorder() );

		JComponent jc = createContents();
		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		jc.setBorder(loweredetched);

		panel.add(jc, BorderLayout.CENTER);
		JComponent command = getCommandRow();
		if(command!=null) panel.add( getCommandRow(), BorderLayout.SOUTH );

		getContentPane().add( panel );

		setSize(_width, _height);		
		UiUtil.centerOnParentAndShow( this );
		setVisible(true);
	}
	
	protected abstract JComponent createContents();
	protected abstract boolean okAction();
	public void setDialogWindowSize(int width, int height) {
		_width 		= width;
		_height 	= height;
	}
	public boolean saveAction() { return true; }

	protected JComponent getCommandRow() {
		JButton ok = new JButton(_okButtonLabel);
		ok.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(okAction()) dispose();
			}
		});
		JButton save = new JButton(_saveButtonLabel);
		save.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(saveAction()) dispose();
			}
		});
		JButton apply = new JButton(_applyButtonLabel);
		apply.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				okAction();
			}
		});
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});
		
		this.getRootPane().setDefaultButton( ok );
		List<JComponent> buttons = new ArrayList<JComponent>();
		if(_enableApplyButton) buttons.add( apply );
		if(_enableSaveButton) buttons.add( save );
		if(_enableOKButton) buttons.add( ok );
		buttons.add( cancel );
		return UiUtil.getCommandRow( buttons );
	}
	
	private void addCancelByEscapeKey(){
		String CANCEL_ACTION_KEY = "CANCEL_ACTION_KEY";
		int noModifiers = 0;
		KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, noModifiers, false);
		InputMap inputMap = this.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.put(escapeKey, CANCEL_ACTION_KEY);
		AbstractAction cancelAction = new AbstractAction(){
			public void actionPerformed(ActionEvent e){
				dispose();
			}
		}; 
		this.getRootPane().getActionMap().put(CANCEL_ACTION_KEY, cancelAction);
	}

}
