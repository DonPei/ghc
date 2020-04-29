package com.geohammer.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.apache.commons.io.FilenameUtils;
import com.geohammer.resource.Resource;
import com.geohammer.vc.VcApp;
import com.geohammer.common.LayerPreferences;
import com.geohammer.license.LicenseManager;
import com.geohammer.common.CommonFrame;
import com.geohammer.component.StatusBar;

import edu.mines.jtk.mosaic.IPanel;
import edu.mines.jtk.mosaic.Mosaic;


/**
 * LauncherFrame starts a GUI interface. Users can access a collection of 
 * independent modules to do daily chores.  
 * <p>
 * LauncherFrame will store user input, check license for each modules, and 
 * passing a process ID to each module
 * <p>
 * @author Don Pei, Halliburton
 * @version 2013.12.31
 */
public class LauncherFrame extends CommonFrame {
	public static int processIDVc = 0;
	
	private LauncherMenuBar menuBar		= null;
	private VcApp 			vcApp 			= null;
	
	private JButton 		vcButton 		= null;
	private JButton 		pckButton 		= null;
	private JButton 		tomoButton 		= null;
	
	private JLabel 			rightLabel		= null;
	private String 			vcString 		= null;
	private String 			pckString 		= null;
	private String 			tomoString 		= null;
	
	private int 			iApp			= 1; 
	
	public int 				nCwd			= 28; 
	public String []		cwd 			= null;
	
	private StatusBar 		statusBar 		= null;
	private JLabel 			infoLabel 		= null;
	
	/**
	   * Constructs a GUI.
	   * @param iApp  application type 1-UCDM 2-MTV 3-splashFrame 4-UCDM research 5-VSP 6-pressure
	   */
	public LauncherFrame(int iApp) {
		this.iApp = iApp;
		menuBar = new LauncherMenuBar(this, iApp);
		setJMenuBar(menuBar);
		setLayout(new BorderLayout());

		add(new LauncherPanel(), BorderLayout.CENTER);
		
		preferences = new LayerPreferences("launcher");
		preferences.readPreferences();
		ArrayList<String> list = preferences.getRecentFileList();
		cwd = new String[nCwd];
		for(int i=0; i<cwd.length; i++) cwd[i] = System.getProperty("user.dir"); 
		if(list!=null) 	{
			int N = list.size();
			N = N<nCwd?N:nCwd;
			for(int i=0; i<N; i++)  cwd[i] = list.get(i); 
		}
		
		statusBar = new StatusBar(); 
		statusBar.setZoneBorder(BorderFactory.createLineBorder(Color.GRAY)); 
		statusBar.setZones( new String[] { "first_zone", "remaining_zones" },     
				new Component[] { new JLabel("first"),  new JLabel("remaining")},     
				new String[] {"30%", "70%"} );
		infoLabel = (JLabel) statusBar.getZone("remaining_zones");
		JLabel label = (JLabel)statusBar.getZone("first_zone");
		label.setText("Copyright (C) GeoHammer Consulting ");
		
		add(statusBar, BorderLayout.SOUTH);
		setTitle("GeoHammer Toolbox " + LauncherApp.VERSION);
		
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent windowEvent) { 
				savePreferences();
				exit(); 
			}
		});
		setResizable(false);
	}
	public void exit() {	
		if(vcApp!=null) 			{ vcApp.exit(); }
		
		System.exit(0);
	}
	public void savePreferences() {
		if(cwd!=null) {
			ArrayList<String> list = new ArrayList<String>();
			for(int i=0; i<cwd.length; i++) list.add(cwd[i]);
			preferences.setRecentFileList(list);
			preferences.writePreferences();
		}
	}
	public void startApp(JButton jButton) {
		String html = jButton.getText();
		String plain = html.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ").trim();
		if(jButton==vcButton) {if(checkLicense()) vcApp 		= new VcApp(this, 2, 2, processIDVc++, plain);}
		else {
			JOptionPane.showMessageDialog(this, "The module will be availabe by the end of this year", 
					"Error", JOptionPane.ERROR_MESSAGE );
			
		}
		
	}
	
	private boolean checkLicense() {	
		boolean noCheck = false;
		if(noCheck) return true;
		
		String appDir =  System.getProperty("geohammer.dir");
		if(appDir==null) return false;
		String path = FilenameUtils.separatorsToWindows(appDir);
		path += File.separator+"license";
		//System.out.println(path);
		LicenseManager licenseManager = new LicenseManager(path);
		//System.out.println("checking license: ");
		if(!licenseManager.isExpired()) return true;		

		int daysAfterExpiration = licenseManager.daysAfterExpiration();
		if(daysAfterExpiration<=15) {
			SimpleDateFormat ddMMMyyFormat = new SimpleDateFormat("MMM-dd-yyyy");
			String text =  "License had expired on " + ddMMMyyFormat.format(licenseManager.getLicense().getExpiration());
			text +=  ". You have a grace peiod of " + (15-daysAfterExpiration) + " days remaining.";
			text +=  " Download a new license from Houston server.";
			JOptionPane.showMessageDialog(this, text, "Expired License", JOptionPane.INFORMATION_MESSAGE);
			return true;
		} else {
			SimpleDateFormat ddMMMyyFormat = new SimpleDateFormat("MMM-dd-yyyy");
			String text =  "License had expired on " + ddMMMyyFormat.format(licenseManager.getLicense().getExpiration());
			text +=  ". Your grace peiod of 15 days has passed. ";
			text +=  " Download a new license from Houston server.";
			JOptionPane.showMessageDialog(this, text, "Expired License", JOptionPane.ERROR_MESSAGE);	
			return false;
		}
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);

		if(visible) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice gd = ge.getDefaultScreenDevice();
			GraphicsConfiguration [] gc = gd.getConfigurations();
			Rectangle gcBounds = gc[0].getBounds();
			setSize(1050, 600);

			setLocationRelativeTo(null);
		}
	}
	
	private MouseListener ml = new MouseAdapter() {
		Color 	defaultColor  	= null;
		Color 	mouseOverColor  = null;
		public void mousePressed(MouseEvent evt) {
			JButton jButton = (JButton)evt.getSource();
			new Thread(() -> { startApp(jButton); }).start();
		}

	    public void mouseReleased(MouseEvent evt) { }
		public void mouseEntered(MouseEvent evt) { 
			JButton jButton = (JButton)evt.getSource();
	    	jButton.setBackground(mouseOverColor);
			if(jButton==vcButton) {
				rightLabel.setText(vcString);
			} if(jButton==pckButton) {
				rightLabel.setText(pckString);
			} if(jButton==tomoButton) {
				rightLabel.setText(tomoString);
			} 
		}
	    public void mouseExited(MouseEvent evt) { 
	    	JButton jButton = (JButton)evt.getSource();
	    	jButton.setBackground(defaultColor);
	    }
	    public void mouseClicked(MouseEvent evt) { }
	};
	
	private class LauncherPanel extends JPanel {
		public LauncherPanel() {

			int k = 0;
			String base = "org/ucdm/resource/icon/";
			URL url = null;
			String htmlText = null;
			JButton jButton = null;
			
			htmlText = "<html>" + "<font size=\"5\" color=\"maroon\"><b>2D/3D Anisotropic Velocity Optimization</b></font>";
			jButton = new JButton(htmlText);
			jButton.setIcon(Resource.loadImageIcon("cube.png"));
			jButton.setHorizontalAlignment(SwingConstants.LEFT);
			jButton.setIconTextGap(30);
			jButton.addMouseListener(ml);
			jButton.setEnabled(true);
			jButton.setFocusable(false);
			vcButton = jButton;
			vcString = htmlText +
					"<UL>" +
					"  <LI>Isotropic and anisotropic 2D/3D seismic ray tracing" +
					"  <LI>Isotropic and anisotropic 2D/3D seismic velocity optimization by " +
					"  very fast simulated annealing (<font size=\"4\"><b>VFSA</b></font>) algorithm" +
					"</UL>";
			
			htmlText = "<html>" + "<font size=\"5\" color=\"maroon\"><b>Automatic First Arrival Time Picker</b></font>";
			jButton = new JButton(htmlText);
			jButton.setIcon(Resource.loadImageIcon("pck.png"));
			jButton.setHorizontalAlignment(SwingConstants.LEFT);
			jButton.setIconTextGap(30);
			jButton.addMouseListener(ml);
			jButton.setEnabled(true);
			jButton.setFocusable(false);
			pckButton = jButton;
			pckString = htmlText +
					"<UL>" +
					"  <LI>Automatically pick first arrival using artificial intelligence "
					+ "(<font size=\\\"4\\\"><b>AI</b></font>) algorithm" +
					"  <LI>Enhance signal SNR by noise removal filters" +
					"</UL>";
			
			htmlText = "<html>" + "<font size=\"5\" color=\"maroon\"><b>Time-Based Tomography for VSP</b></font>";
			jButton = new JButton(htmlText);
			jButton.setIcon(Resource.loadImageIcon("tomo.png"));
			jButton.setHorizontalAlignment(SwingConstants.LEFT);
			jButton.setIconTextGap(30);
			jButton.addMouseListener(ml);
			jButton.setEnabled(true);
			jButton.setFocusable(false);
			tomoButton = jButton;
			tomoString = htmlText +
					"<UL>" +
					"  <LI>Shoot seismic ray through geological formation to find out velocity anomalies"+
					"  <LI>Simultaneous iterative reconstruction technique "
					+ "(<font size=\\\"4\\\"><b>SIRT</b></font>) algorithm" +
					"  <LI>Conjugate gradient least squares "
					+ "(<font size=\\\"4\\\"><b>CGLS</b></font>) algorithm" +
					"  <LI>Least square QR "
					+ "(<font size=\\\"4\\\"><b>LSQR</b></font>) algorithm" +
					"</UL>";
			
			setLayout(new GridBagLayout());
			Insets insets = new Insets(1, 5, 1, 5);
			GridBagConstraints gbc = null;
			JPanel left = null;
			
			int nRow = 3+1;
			left = new JPanel(new GridLayout(nRow, 1, 1, 1));
			left.add(vcButton);
			left.add(pckButton);
			left.add(tomoButton);
			htmlText = "<html>" + "<font size=\"5\" color=\"maroon\"><b>More Modules Coming Soon</b></font>";
			JLabel jLabel = new JLabel(htmlText, null, JLabel.CENTER);
			//left.add(jLabel);
			left.add(new JLabel(""));

			gbc= new GridBagConstraints(0, 0, 1, 1, 100.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 1, 1);
			add(left, gbc);
			
			htmlText = "<html>" + "<font size=\"5\" color=\"maroon\"><b>GeoHammer Toolbox (GT)</b></font>";
			
			String aString = htmlText +
					"<P>" + "A collection of applications that extract subsurface information" +
					"<UL>" +
					"  <LI>Ctrl+C and Ctrl+V to copy and paste file directory from OS" +
					"</UL>";
			
			rightLabel = new JLabel(aString, null, JLabel.LEFT);
			//Dimension d = _jLabel.getPreferredSize();  
			rightLabel.setPreferredSize(new Dimension(500,600));
			rightLabel.setMinimumSize(new Dimension(500,600));
			//_jLabel.setPreferredSize(new Dimension(450,560));
			//_jLabel.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, Color.red));
			//_jLabel.setBorder(BorderFactory.createTitledBorder("Introduction"));
			Border raisedbevel = BorderFactory.createRaisedBevelBorder();
			Border loweredbevel = BorderFactory.createLoweredBevelBorder();
			//_jLabel.setBorder(BorderFactory.createCompoundBorder(raisedbevel, loweredbevel));
			Border paddingBorder = BorderFactory.createEmptyBorder(5,0,5,2);
			Border outside = BorderFactory.createCompoundBorder(paddingBorder, raisedbevel);
			paddingBorder = BorderFactory.createEmptyBorder(10,5,10,5);
			Border inside = BorderFactory.createCompoundBorder(loweredbevel, paddingBorder);			
			rightLabel.setBorder(BorderFactory.createCompoundBorder(outside, inside));
			
			gbc= new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, insets, 0, 0);
			add(rightLabel, gbc);
		}
	}

	@Override
	public IPanel getBaseWorld() { return null; }
	@Override
	public Mosaic getMosaic() { return null; }
	
	public String getProjectFullName() 			{ return System.getProperty("user.dir")+File.separator+"junk.txt"; }

	public StatusBar getStatusBar() 			{ return statusBar; }
	public JLabel getInfoLabel() 				{ return infoLabel; }
	
	public String getCwd(int index) 			{ return cwd[index]; }
	public void setCwd(int index, String cwd) 	{ this.cwd[index]=cwd; }
	
	
//	public class Html2Text extends HTMLEditorKit.ParserCallback {  
//		 StringBuffer s;  
//		  
//		 public Html2Text() {}  
//		  
//		 public void parse(Reader in) throws IOException {  
//		   s = new StringBuffer();  
//		   ParserDelegator delegator = new ParserDelegator();  
//		   // the third parameter is TRUE to ignore charset directive  
//		   delegator.parse(in, this, Boolean.TRUE);  
//		 }  
//		  
//		 public void handleText(char[] text, int pos) {  
//		   s.append(text);  
//		 }  
//		  
//		 public String getText() {  
//		   return s.toString();  
//		 }  
//		  
//		}

}
