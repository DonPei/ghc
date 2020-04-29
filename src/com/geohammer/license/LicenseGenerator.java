package com.geohammer.license;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.apache.commons.io.FilenameUtils;
import com.geohammer.common.util.UiUtil;

public class LicenseGenerator  extends JDialog {
	JFrame 		_frame 				= null;
	
	JLabel _licenseLabel 	= null;
	String _licenseFileName	= null;
	int 	_iType 			= 1;
	boolean _encoded 		= true;
	LicenseManager _licenseManager = null;
	
	private JTextField _moduleTF 		= null;
	private JTextField _emailTF 		= null;
	private JTextField _expirationTF 	= null;
	private JTextField _versionTF 		= null;
	private JTextField _licenseNumTF 	= null;
	private JTextField _macAddressTF 	= null;
	
	public LicenseGenerator(JFrame frame, String aTitle) {
		this(frame, aTitle, false, 1);
	}

	public LicenseGenerator(JFrame frame, String aTitle, boolean modal, int iType) {
		super(frame, aTitle, modal);
		_iType 	= iType;
		_frame 	= frame;
		
		_licenseManager = new LicenseManager();
		if(_iType==1){
			License license = new License();
			String licenseText = license.getLicenseInput();
			_licenseLabel = new JLabel(_licenseManager.encoding(licenseText));
		} else if (_iType==2){
			_licenseLabel = new JLabel("Load license input file.");
		} else if(_iType==11){
			String selectedFileName = "C:\\prowess\\geo\\org.ucdm\\aa.txt";
			String email = "trial@license.com";
			LicenseType licenseType = LicenseType.ONEMONTH;
			_licenseFileName = _licenseManager.getLicenseFileName(selectedFileName, email, licenseType);
			String licenseText = _licenseManager.getLicense(selectedFileName, email, licenseType);
			_licenseLabel = new JLabel(_licenseManager.encoding(licenseText));
		} else if (_iType==12){
			_licenseLabel = new JLabel("Load license file.");
		} 
	}

	public final void showDialog(){
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setResizable(true);
		addCancelByEscapeKey();

		JPanel panel = new JPanel();
		panel.setLayout( new BorderLayout() );
		panel.setBorder( UiUtil.getStandardBorder() );

		//		Font font = new Font("Serif", Font.PLAIN, 60);
		//		panel.setFont(font);

		JPanel jc = createContents();
		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		jc.setBorder(loweredetched);

		String tipsText = "<html>This is the computer-generated information. DO NOT MODIFY." +
				"<UL>" +
				"  <LI>Save it as a text file" +
				"  <LI>Email the text file to DonghongPei@gmail.com" +
				"  <LI>Within 24 hours, A license file will be emailed back to you" +
				"  <LI>After received, put the emailed file in license folder" +
				"</UL>";

		JLabel fancyTipsLabel = new JLabel(tipsText, UIManager.getIcon("OptionPane.informationIcon"), JLabel.CENTER);
		fancyTipsLabel.setBorder(BorderFactory.createTitledBorder("Tips"));


		panel.add(fancyTipsLabel, BorderLayout.NORTH);
		panel.add(jc, BorderLayout.CENTER);
		panel.add( getCommandRow(), BorderLayout.SOUTH );

		getContentPane().add( panel );

		UiUtil.centerOnParentAndShow( this );
		if(_iType==1) setSize(850, 340);
		else setSize(850, 540);
		setVisible(true);
	}

	private JPanel createContents() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		Insets insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gbc= null;
		
		_licenseNumTF 	= new JTextField(_licenseManager.generateLicenseNo(), 50);
		_licenseNumTF.setEnabled(false);
		_macAddressTF 	= new JTextField(_licenseManager.getLocalhostMacAddress(), 50);
		_macAddressTF.setEnabled(false);
		_moduleTF 		= new JTextField("All", 50);
		_versionTF 		= new JTextField("0-0-0-0", 50);
		_expirationTF 	= new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 50);
		_emailTF 		= new JTextField("you@millionaire.com", 50);
		
		int k = 0;
		gbc= new GridBagConstraints(0, k, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		panel.add(new JLabel("License Number:", JLabel.RIGHT), gbc);
		gbc= new GridBagConstraints(1, k++, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		panel.add(_licenseNumTF, gbc);
		
		gbc= new GridBagConstraints(0, k, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		panel.add(new JLabel("MAC Address:", JLabel.RIGHT), gbc);
		gbc= new GridBagConstraints(1, k++, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		panel.add(_macAddressTF, gbc);
		if(_iType==1) return panel;
		
		gbc= new GridBagConstraints(0, k, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		panel.add(new JLabel("Module:", JLabel.RIGHT), gbc);
		gbc= new GridBagConstraints(1, k++, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		panel.add(_moduleTF, gbc);

		gbc= new GridBagConstraints(0, k, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		panel.add(new JLabel("Version:", JLabel.RIGHT), gbc);
		gbc= new GridBagConstraints(1, k++, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		panel.add(_versionTF, gbc);
		
		gbc= new GridBagConstraints(0, k, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		panel.add(new JLabel("Expiration:", JLabel.RIGHT), gbc);
		gbc= new GridBagConstraints(1, k++, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		panel.add(_expirationTF, gbc);
		
		gbc= new GridBagConstraints(0, k, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		panel.add(new JLabel("Email:", JLabel.RIGHT), gbc);
		gbc= new GridBagConstraints(1, k++, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		panel.add(_emailTF, gbc);
		
		gbc= new GridBagConstraints(0, k++, 2, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		panel.add(_licenseLabel, gbc);
		return panel;
	}
	
	protected void generateAction() { 
		_licenseNumTF.setText(_licenseManager.generateLicenseNo()); 
		_macAddressTF.setText(_licenseManager.getLocalhostMacAddress()); 
		_expirationTF.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date())); 
	}
	protected void toggleAction() { 
		String licenseText = readInput();
		if(_encoded) {_licenseLabel.setText(licenseText); }
		else { _licenseLabel.setText(_licenseManager.encoding(licenseText)); }
		_encoded = !_encoded;
	}
	protected void saveAsAction() { 		
		String licenseText = readInput();
		_licenseLabel.setText(_licenseManager.encoding(licenseText));
		
		String initFileName = System.getProperty("user.dir")+File.separator+"license-"+_expirationTF.getText().trim()+".lic";
		String selectedFileName = _licenseManager.saveFileUsingJFileChooser(initFileName);
		_licenseManager.write(_licenseManager.encoding(licenseText), selectedFileName);
	}
	private String readInput() {
		return _licenseNumTF.getText().trim()+","+
				_macAddressTF.getText().trim()+","+
				_moduleTF.getText().trim()+","+
				_versionTF.getText().trim()+","+
				_expirationTF.getText().trim()+","+
				_emailTF.getText().trim();
	}
	protected void openAction() {
		String selectedFileName = _licenseManager.openFileUsingJFileChooser(System.getProperty("user.dir"));
		License license = _licenseManager.read(selectedFileName);
		_licenseManager.setLicense(license);
		_licenseManager.setPath(selectedFileName);
		
		_licenseNumTF.setText(license.getLicenseNumber()); 
		_macAddressTF.setText(license.getMacAddress()); 
		_moduleTF.setText(license.getModuleName()); 
		_versionTF.setText(license.getVersion()); 
		_expirationTF.setText(new SimpleDateFormat("yyyy-MM-dd").format(license.getExpiration())); 
		_emailTF.setText(license.getEmail()); 
		_licenseLabel.setText(license.toString());
	}

	/**
	 * Return a standardized row of command buttons, right-justified and 
	 * all of the same size, with OK as the default button, and no mnemonics used, 
	 * as per the Java Look and Feel guidelines. 
	 */
	private JComponent getCommandRow() {
		JButton open = new JButton("Open...");
		open.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				openAction();
			}
		});
		JButton saveAs = new JButton("Save As...");
		saveAs.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				saveAsAction();
			}
		});
		JButton toggle = new JButton("Toggle");
		toggle.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				toggleAction();
			}
		});
		JButton generate = new JButton("Generate");
		generate.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				generateAction();
			}
		});

		List<JComponent> buttons = new ArrayList<JComponent>();
		if(_iType==1) {
			buttons.add( saveAs );
			getRootPane().setDefaultButton( saveAs );
		} else if(_iType==2) {
			buttons.add( generate );
			buttons.add( toggle );
			buttons.add( open );
			buttons.add( saveAs );
		}

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
	
	public static void main(String s[]) {
		JFrame frame = new JFrame("License");
		frame.setVisible(true);
		frame.setSize(400, 200);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try { 
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());     
		} catch (Exception e) {e.printStackTrace();     }
		LicenseGenerator dialog = new LicenseGenerator(frame, "License", false, 1);
		dialog.showDialog();
		
//		USERDOMAIN=HALAMERICA,
//				USERDNSDOMAIN=CORP.HALLIBURTON.COM
//				ALLUSERSPROFILE=C:\ProgramData
//				USERNAME=hx40914
//				COMPUTERNAME=MWKS272045
//				USERPROFILE=C:\Users\hx40914
//				PROCESSOR_REVISION=2a07
//				NUMBER_OF_PROCESSORS=4
//				Map<String, String> env = System.getenv();
//		for (String envName : env.keySet()) {
//			System.out.format("%s=%s%n", envName, env.get(envName));
//		}
	}
}
