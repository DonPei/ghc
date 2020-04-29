package com.geohammer.vc;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import com.geohammer.resource.Resource;

import com.geohammer.component.LabelTextCombo;
import com.geohammer.component.RecentFileMenu;
import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.DipLayer1D;
import com.geohammer.core.planarmodel.FlatLayer1D;
import com.geohammer.core.planarmodel.Layer2D;
import com.geohammer.core.planarmodel.Layer3D;
import com.geohammer.core.planarmodel.SingleFlatLayer;
import com.geohammer.core.planarmodel.SingleLayer2D;
import com.geohammer.rt.RayPaths;
import com.geohammer.rt.pseudo3d.RayTracerI;
import com.geohammer.tracedisplay.TraceDisplayDialog;
import com.geohammer.vc.dialog.ColorBarDialog;
import com.geohammer.vc.dialog.ComparisonDialog;
import com.geohammer.vc.dialog.ExportFileDialog;
import com.geohammer.vc.dialog.PlotDialog;
import com.geohammer.vc.dialog.PlotSelectionDialog;
import com.geohammer.vc.dialog.ProjectDialog;
import com.geohammer.vc.dialog.PropertyEditor;
import com.geohammer.vc.dialog.RayTracerDialog;
import com.geohammer.vc.dialog.Vel3dDialog;
import com.geohammer.vc.run.SaDialog;
import com.geohammer.vc.run.StrippingDialog;

@SuppressWarnings("serial")
public class VcMenuBar extends JMenuBar {
	private final static int MAX_RECENT_FILES = 20;
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	VcFrame 		frame 				= null;
	private RecentFileMenu 	menuRecentFiles = null;
	
	private JMenuItem 	openFile 			= null;
	private JMenuItem [] spaceJMenuItem 	= null;
	
	private JTextField [] minMaxRange 		= null;
	private JTextField [] numOfGrid 		= null;
	
	public VcMenuBar(VcFrame frame) {
		this.frame 				= frame;
		JMenu menuFile  		= new JMenu("File");
		JMenu menuMode			= new JMenu("   Mode");
		JMenu menuEdit			= new JMenu("   Edit");
		JMenu menuOptimization  = new JMenu("   Optimization");
		JMenu menuPlot			= new JMenu("   Plot");

		setFileMenu(menuFile);
//		setModeMenu(menuMode);
//		setEditMenu(menuEdit);
		setOptimizationMenu(menuOptimization);
//		setFdMenu(menuFd);
//		setDwnMenu(menuDwn);
		setPlotMenu(menuPlot);
		
		add(menuFile);
//		add(menuMode);
//		add(menuEdit);	
		add(menuOptimization);
		add(menuPlot);	
		
		add(genSpacer(200));

		String [] moduleString = null;
		ButtonGroup moduleRadioGroup = null;
		JRadioButton [] moduleRadioButton = null;
		moduleString = new String []{ "Vp  ", "Vs  "};
		int n = moduleString.length;
		moduleRadioGroup = new ButtonGroup();
		moduleRadioButton = new JRadioButton[n];
		for(int i=0; i<n; i++) {
			final int j = i+1;
			moduleRadioButton[i] = new JRadioButton(moduleString[i], i==0);
			//moduleRadioButton[i].setEnabled(false);
			moduleRadioButton[i].setOpaque(false);
			moduleRadioButton[i].addItemListener( new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
						frame.setIVp(j);
						frame.updateWorld();
					}
				}
			});
			moduleRadioGroup.add(moduleRadioButton[i]);
			//add(moduleRadioButton[i]);
		}	
		Dimension dBt = null;
		
		//JButton jButton = new JButton(Resource.loadImageIcon("colorWheel.png"));
		JButton jButton = new JButton("Legend");
		jButton.setToolTipText("Show Velocity Legend");
		add(jButton);
		dBt = new Dimension(80,30); //Sets the size of the button in the  JMenuBar
		jButton.setMinimumSize(dBt);
		jButton.setPreferredSize(dBt);
		jButton.setMaximumSize(dBt);
		//		jButton.setOpaque(true);
		jButton.setFocusable(false);
		jButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(frame.getProject()==null) return;
				String aTitle = new String("Velocity Legend");
				ColorBarDialog dialog = new ColorBarDialog(frame, aTitle, false);
				
				String unit = "";
				if(frame.getProject().getIUnit()==0) 		unit = " (ft/s)"; 
				else if(frame.getProject().getIUnit()==1) 	unit = " (m/s)"; 
				else if(frame.getProject().getIUnit()==2) 	unit = " (km/s)"; 
				
				if(frame.getIVp()==1) dialog.setName("P-wave Velocity"+unit);
				else dialog.setName("S-wave Velocity"+unit);
				dialog.showDialog();
			}
		});

		add(genSpacer(5));

		jButton = new JButton(Resource.loadImageIcon("screenshot.png"));
		jButton.setToolTipText("Screenshot to memory");
		add(jButton);
		dBt = new Dimension(35,30); //Sets the size of the button in the  JMenuBar
		jButton.setMinimumSize(dBt);
		jButton.setPreferredSize(dBt);
		jButton.setMaximumSize(dBt);
		//		jButton.setOpaque(true);
		jButton.setFocusable(false);
		jButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(frame.getProject()==null) return;
				BufferedImage image = null;
				if(frame.getProject().is2D()) {
					image = frame.getVel2dPanel().paintScreenToImage();
				} else {
					image = frame.getVel3dPanel().paintScreenToImage();
				}
				ImageSelection imgSel = new ImageSelection(image);
			    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);	
			}
		});
		jButton = new JButton(Resource.loadImageIcon("screenshotToFolder.png"));
		jButton.setToolTipText("Screenshot to file");
		add(jButton);
		dBt = new Dimension(35,30); //Sets the size of the button in the  JMenuBar
		jButton.setMinimumSize(dBt);
		jButton.setPreferredSize(dBt);
		jButton.setMaximumSize(dBt);
		//		jButton.setOpaque(true);
		jButton.setFocusable(false);
		jButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(frame.getProject()==null) return;
				BufferedImage image = null;
				if(frame.getProject().is2D()) {
					image = frame.getVel2dPanel().paintScreenToImage();
				} else {
					image = frame.getVel3dPanel().paintScreenToImage();
				}
				ImageSelection imgSel = new ImageSelection(image);
			    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);	
				
				if(image==null)  return;
				FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] {
						new FileNameExtensionFilter("PNG (*.png)", "png")
				};

				Timestamp timestamp = new Timestamp(System.currentTimeMillis());				
				String name = frame.getProject().getImagesCwd()+File.separator+
						"ss"+FORMAT.format(timestamp)+".png";
				String fileName = frame.saveFileUsingJFileChooser(exts, name); 
				if(fileName==null) return;
				else {
					String selectedExt = FilenameUtils.getExtension(fileName);
					int k = 0;
					for(int i=0; i<exts.length; i++) {
						String [] possibleExt = exts[i].getExtensions();
						for(int j=0; j<possibleExt.length; j++) {
							if(selectedExt.equals(possibleExt[j])) {
								k = i;
								j = possibleExt.length+1;
								i = exts.length+1;
							}
						}
					}
					File f = new File(fileName);
					try { ImageIO.write(image, "jpg", f);
					} catch (IOException e1) { e1.printStackTrace(); }
				}
			}
		});
		
	}

	private JMenu genSpacer(int width) {
		JMenu spacer = new JMenu();
		spacer.setEnabled(false);

		spacer.setMinimumSize(new Dimension(width, 1));
		spacer.setPreferredSize(new Dimension(width, 1));
		spacer.setMaximumSize(new Dimension(width, 1));
		return spacer;
	}
	
	public void setFrame(VcFrame frame) 			{ this.frame = frame; }
	public JMenuItem getJMenuItemOpenFile() 		{ return openFile; }
	
	public void removeRecentFile( String filename ) { menuRecentFiles.removeFile( filename ); }
	public void addRecentFile( String filename ) 	{ menuRecentFiles.addFile( filename ); }
	public ArrayList<String> getRecentFileList() 	{ return menuRecentFiles.getFileList(); }
	
	public void setFileMenu(JMenu jMenu) {
		JMenuItem jMenuItem = null;
		jMenuItem  	= new JMenuItem("Open...");
		jMenuItem.setToolTipText("Open a project File");
		jMenuItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK) );
		jMenu.add(jMenuItem);
		jMenuItem.addActionListener(e -> {
			FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] {
					new FileNameExtensionFilter("(*.prj2d)", "prj2d"),
					new FileNameExtensionFilter("(*.prj3d)", "prj3d")};
			frame.openProjectWithExtension(exts);
		});
		openFile = jMenuItem;
	
		jMenuItem  	= new JMenuItem("New...");
		jMenuItem.setToolTipText("Create a new project");
		jMenuItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK) );
		jMenu.add(jMenuItem);
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				String aTitle = new String("New Project");
				String lastPath = null;
				if(getRecentFileList()==null || getRecentFileList().isEmpty()) 
					lastPath = System.getProperty("user.dir")+File.separator;
				else {
					String projectFileName = getRecentFileList().get(0);
					lastPath = FilenameUtils.getFullPath(projectFileName);
				}
				ProjectDialog dialog = new ProjectDialog(frame, aTitle, true, lastPath);
				dialog.showDialog();
			}
		});
		jMenu.addSeparator();
		
		jMenuItem  	= new JMenuItem("Save...");
		jMenuItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK) );
		jMenu.add(jMenuItem);
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				frame.saveProject();
			}
		});
		
		jMenuItem  	= new JMenuItem("Save As...");
		jMenu.add(jMenuItem);
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				frame.saveProjectAs();
			}
		});
		jMenu.addSeparator();
		
//		jMenuItem  	= new JMenu("Load");
//		jMenu.add(jMenuItem);
//		
//		String [] label = new String[]{"Survey", "Layer Name"};
//		for(int i=0; i<label.length; i++) {
//			final int j = i;
//			JMenuItem subMenuItem  	= new JMenuItem(label[i]);
//			jMenuItem.add(subMenuItem);
//			subMenuItem.addActionListener( new ActionListener() {
//				public void actionPerformed( ActionEvent e ) {
//					if(j==0) {
//						String aTitle = "Import directional survey";
//						ExportFileDialog dialog = new ExportFileDialog(frame, aTitle, false, 100);
//						dialog.showDialog();
//					}
//				}
//			});
//		}	
//		jMenu.addSeparator();
		
		jMenuItem  	= new JMenu("Export");
		jMenu.add(jMenuItem);
		//label = new String[]{"SeisPT Velocity XML 2D", "SeisPT Velocity XML 3D", 
		//		"UCDM Velocity CSV", "UCDM Velocity Grid", "Sonic to VTI log", "VSP to VTI log"};
		String [] label = new String[]{"Velocity Model", "Travel Time"};
		for(int i=0; i<label.length; i++) {
			final int j = i;
			JMenuItem subMenuItem  	= new JMenuItem(label[i]);
			jMenuItem.add(subMenuItem);
			subMenuItem.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					String aTitle = "Export File";
					String fileName = null;
					if(j==0) {
						fileName = frame.getProject().getFullName()+"_exportedVelModel.xlsx";
					} else {
						fileName = frame.getProject().getFullName()+"_exportedTravelTime.xlsx";
					}
					
					ExportFileDialog dialog = new ExportFileDialog(frame, aTitle, false, j);
					dialog.setFileName(fileName);
					dialog.showDialog();
				}
			});
		}	
		jMenu.addSeparator();
		
		menuRecentFiles = new RecentFileMenu("Recent", MAX_RECENT_FILES);
		menuRecentFiles.addFileMenuListener(frame);
		jMenu.add(menuRecentFiles);
		
		jMenuItem  	= new JMenuItem("Edit Recent");
		//jMenu.add(jMenuItem);
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				String aTitle = new String("Edit Recent");				
//				EditRecentDialog dialog = new EditRecentDialog(_frame, aTitle,  
//						_frame.getPreferences(), _menuRecentFiles, false);
//				dialog.showDialog();
			}
		});

		jMenu.addSeparator();
		
		jMenuItem  	= new JMenuItem("Exit");
		jMenuItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK) );
		jMenu.add(jMenuItem);
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				frame.exit(false);
			}
		});
	}
	
	
	public void setModelMenu(JMenu jMenu) {		
		JMenuItem jMenuItem = null;
		jMenuItem  	= new JMenuItem("3D");
		jMenu.add(jMenuItem);
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				Vel3dDialog dialog = new Vel3dDialog(frame, "Try", false);
				dialog.showDialog();
			}
		});
	}

	public void setPlotMenu(JMenu jMenu) {		
		JMenuItem jMenuItem = null;
		String [] label = null;


		jMenuItem  	= new JMenuItem("Plot Travel Time");
		jMenu.add(jMenuItem);
		jMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {				
				if(rayTracerIsInvalid()) return;
				
				String aTitle = new String("Travel Time Plot");
				String catalog = "Time";
				String unit = "";
				if(frame.getProject().getIUnit()==1) 		unit = " (m)"; 
				else if(frame.getProject().getIUnit()==2) 	unit = " (ft)"; 
				else if(frame.getProject().getIUnit()==3) 	unit = " (km)"; 
				final String vLabel = "Receiver TVD"+unit;
				String hLabel = "Time (millisecond)";
				
				PlotDialog dialog = new PlotDialog(frame, aTitle, false, frame.getProject().getDipLayer1D(),
						20, true, catalog, hLabel, vLabel);
				dialog.setVcPair(frame.getProject().getVCPair());
				dialog.showDialog();
			}
		});
		
		jMenuItem  	= new JMenuItem("Geometry Map");
		jMenu.add(jMenuItem);
		jMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String aTitle = new String("Map Plot");
				String catalog = "map";
				String unit = "";
				if(frame.getProject().getIUnit()==1) 		unit = " (m)"; 
				else if(frame.getProject().getIUnit()==2) 	unit = " (ft)"; 
				else if(frame.getProject().getIUnit()==3) 	unit = " (km)"; 
				String vLabel = "Northing"+unit;
				String hLabel = "Easting"+unit;
				
				VCPair vcPW = frame.getProject().getVCPair();
				PlotDialog dialog = new PlotDialog(frame, aTitle, false, frame.getProject().getDipLayer1D(),
						80, false, catalog, hLabel, vLabel);
				dialog.setVcPair(vcPW);
				dialog.showDialog();
			}
		});
		
		jMenuItem  	= new JMenuItem("Velocity Legend");
		//jMenu.add(jMenuItem);
		jMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String aTitle = new String("Velocity Legend");
				ColorBarDialog dialog = new ColorBarDialog(frame, aTitle, false);
				
				String unit = "";
				if(frame.getProject().getIUnit()==0) 		unit = " (ft/s)"; 
				else if(frame.getProject().getIUnit()==1) 	unit = " (m/s)"; 
				else if(frame.getProject().getIUnit()==2) 	unit = " (km/s)"; 
				
				if(frame.getIVp()==1) dialog.setName("P-wave Velocity"+unit);
				else dialog.setName("S-wave Velocity"+unit);
				dialog.showDialog();
			}
		});
		jMenu.addSeparator();
		
		jMenuItem  	= new JMenu("Depth Plot");
		jMenu.add(jMenuItem);
		
		label = new String[]{"Velocity", "Vp/Vs", "Anisotropy"};
		for(int i=0; i<label.length; i++) {
			final int j = i;
			final String itemName = label[i];
			JMenuItem subMenuItem  	= new JMenuItem(itemName);
			jMenuItem.add(subMenuItem);
			subMenuItem.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					String aTitle = new String(itemName+" Depth Plot");
					String catalog = "Depth";
					String unit = "";
					if(frame.getProject().getIUnit()==1) 		unit = " (m"; 
					else if(frame.getProject().getIUnit()==2) 	unit = " (ft"; 
					else if(frame.getProject().getIUnit()==3) 	unit = " (km"; 
					final String vLabel = "TVD"+unit+")";
					String hLabel = null;
					if(j==0) 		hLabel = "Velocity"+unit+"/s)";
					else if(j==1) 	hLabel = "Vp/Vs";
					else if(j==2) 	hLabel = "Anisotropy";
					
					PlotDialog dialog = new PlotDialog(frame, aTitle, false, frame.getProject().getDipLayer1D(),
							j, true, catalog, hLabel, vLabel);
					dialog.showDialog();
				}
			});
		}	
		
		JMenu subMenuItem0  	= new JMenu("Other...");
		jMenuItem.add(subMenuItem0);
		String [] label1 = new String[]{"Density", "E(h)", "E(v)", "P(h)", "P(h, v)", "P(v, h)"};
		for(int i1=0; i1<label1.length; i1++) {
			final int j1 = i1;
			final String itemName1 = label1[i1];
			JMenuItem subMenuItem1  	= new JMenuItem(itemName1);
			subMenuItem0.add(subMenuItem1);
			subMenuItem1.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					String aTitle1 = new String(itemName1+" Depth Plot");
					String catalog1 = "Depth";
					String unit = "";
					if(frame.getProject().getIUnit()==1) 		unit = " (m)"; 
					else if(frame.getProject().getIUnit()==2) 	unit = " (ft)"; 
					else if(frame.getProject().getIUnit()==3) 	unit = " (km)"; 
					final String vLabel = "TVD"+unit;
					String hLabel1 = itemName1;
					PlotDialog dialog = new PlotDialog(frame, aTitle1, false, frame.getProject().getDipLayer1D(),
							j1+10, true, catalog1, hLabel1, vLabel);
					dialog.showDialog();
				}
			});
		}
		
		jMenuItem  	= new JMenu("Cross Plot");
		jMenu.add(jMenuItem);
		
		label = new String[]{"Vp-Vs", "Epsilon-Gamma", "Other..."};
		for(int i=0; i<label.length; i++) {
			final int j = 50+i;
			final String itemName = label[i];
			JMenuItem subMenuItem  	= new JMenuItem(itemName);
			jMenuItem.add(subMenuItem);
			subMenuItem.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					String aTitle = new String(itemName+" Cross Plot");
					String catalog = "Cross";
					String unit = "";
					if(frame.getProject().getIUnit()==1) 		unit = " (m/s)"; 
					else if(frame.getProject().getIUnit()==2) 	unit = " (ft/s)"; 
					else if(frame.getProject().getIUnit()==3) 	unit = " (km/s)"; 
					String vLabel = null;
					String hLabel = null;
					if(j==50) 		{ vLabel = "Vp"+unit; 		hLabel = "Vs"+unit; }
					else if(j==51) 	{ vLabel = "Epsilon"; 		hLabel = "Gamma"; }
					
					if(j==52) {						
						PlotSelectionDialog dialog = new PlotSelectionDialog(frame, aTitle, false, j, false, frame.getProject().getDipLayer1D());
						dialog.showDialog();
					} else {
						PlotDialog dialog = new PlotDialog(frame, aTitle, false, frame.getProject().getDipLayer1D(),
								j, false, catalog, hLabel, vLabel);
						dialog.showDialog();
					}
				}
			});
		}

		jMenu.addSeparator();
		
		jMenuItem  	= new JMenuItem("Model Comparison");
		//jMenu.add(jMenuItem);
		jMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {				
				String aTitle = "Model Comparison";
				String fileName = frame.getProject().getFullName();				
				ComparisonDialog dialog = new ComparisonDialog(frame, aTitle, false);
				dialog.setFileName(fileName);
				dialog.showDialog();
			}
		});
	}
	
	public boolean rayTracerIsInvalid() {
		RayTracerI rta = frame.getRayTracer();
		if(rta==null) {
			String message = "Must do ray tracing first!";
			String title = "Error";
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
			return true;
		} else {
			return false;
		}
	}
	
	private double statisticArray(double v1, double v2, double [][] angles) {
		double p = 0;
		for(int i=0; i<angles.length; i++) {
			for(int j=0; j<angles[i].length; j++) {
				if(angles[i][j]>=v1 &&angles[i][j]<v2) p += 1.0;
			}
		}
		return p;
	}
	
	public void setOptimizationMenu(JMenu jMenu) {		
		JMenuItem jMenuItem = null;
		jMenuItem  	= new JMenuItem("Ray Tracing");
		jMenu.add(jMenuItem);
		jMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				RayTracerDialog dialog = new RayTracerDialog(frame, "Ray Tracing Parameters", false);
				dialog.showDialog();
				//if(frame.getRayTracer()!=null) frame.getRayTracer().setIVp(_frame.getIVp());
			}			
		});
		
		//jMenu.addSeparator();
		
		jMenuItem  	= new JMenuItem("Emergency Angle");
		//jMenu.add(jMenuItem);
		jMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(!frame.getProject().is2D()) return;
				if(frame.rayPaths==null) return;
				RayPaths rayPaths 	= frame.rayPaths;
				//System.out.println(rayPaths.toString());
				int nShots = rayPaths.getNumOfEmsembles();
				double [][] angles = new double[nShots][];
				float [][] curves = new float[2*nShots][];
				int numOfRays = 0;
				//System.out.println("nShots="+nShots);
				for(int i=0, k=0; i<nShots; i++) {
					RayPaths rayPaths1 = rayPaths.getEmsemble(i);
					//System.out.println("shot="+i);
					//System.out.println(rayPaths1.toString());
					angles[i] = rayPaths1.calEmergentAngle();
					numOfRays += angles[i].length;
					
					float [] no = new float[angles[i].length];
					float [] v = new float[angles[i].length];
					for(int j=0; j<angles[i].length; j++) {
						no[j] = i+1;
						v[j] = (float)(angles[i][j]*180/Math.PI);
					}
					curves[k++] = v;
					curves[k++] = no;
				}
				double dn = 5.0;
				int n = (int)(90.0/dn)+1;
				float [] zones = new float[n];
				float [] p = new float[n];
				double a = 1.0;
				for(int i=0; i<n; i++) {
					zones[i] = (float)(i*dn);
					if(i==n-1) a = 1.1;
					p[i] = (float)(100*statisticArray(zones[i]*Math.PI/180, (i+a)*dn*Math.PI/180, angles)/numOfRays);
				}
				
				int m = 2*n+2;
				float [] blockX = new float[m];
				float [] blockY = new float[m];
				for(int i=0; i<n; i++) {
					int j=2*i+1;
					if(i==n-1){
						blockX[j] = zones[i];
						blockX[j+1] = 90;
						blockY[j] = p[i];
						blockY[j+1] = p[i];
					} else {
						blockX[j] = zones[i];
						blockX[j+1] = zones[i+1];
						blockY[j] = p[i];
						blockY[j+1] = p[i];
					}
				}
				blockX[m-1] = 90;
				blockY[m-1] = 0;
				
				
				
				String aTitle = new String("Emergent Angle");
				String catalog = "Angle";
				//String vLabel = "Emergent Angle (degree)";
				//String hLabel = "Shot No";
				String vLabel = "Number of Raypath Percentage (%)";
				String hLabel = "Ray Emergent Angle (degree) with 5 Degree Bin Size";
				
//				PlotDialog dialog = new PlotDialog(_frame, aTitle, false, null,
//						141, false, catalog, hLabel, vLabel);
//				//dialog.setCurves(curves);
//				dialog.setCurves(new float[][]{p, zones});
//				//dialog.setCurves(new float[][]{blockY, blockX});
//				dialog.showDialog();				
			}			
		});
		
		jMenu.addSeparator();
		
		final String title1 = "Velocity SA Optimization";
		jMenuItem  	= new JMenuItem(title1);
		jMenuItem.setToolTipText("Calibration velocity while fixing anisotropy");
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				if(rayTracerIsInvalid()) return;
				FlatLayer1D layer1D 	= frame.getRayTracer().getFlatLayer1D();
				boolean isVsZero = layer1D.isVsZero();
				VCPair vcPW = frame.getRayTracer().getVCPair();
				boolean isObsSTExist = vcPW.isObsSTExist();
				int iType = 1;
				if(isVsZero || !isObsSTExist) {
					iType += 10;
				}
				//System.out.println("isVsZero="+isVsZero+ " isObsSTExist="+isObsSTExist);
				SaDialog dialog = new SaDialog(frame, title1, false, iType, 0, null);
				dialog.showDialog();
			}
		});
		jMenu.add(jMenuItem);
		
		final String title2 = "Anisotropy SA Optimization";
		jMenuItem  	= new JMenuItem(title2);
		jMenuItem.setToolTipText("Calibration anisotropy while fixing velocity");
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				if(rayTracerIsInvalid()) return;
				FlatLayer1D layer1D 	= frame.getRayTracer().getFlatLayer1D();
				boolean isVsZero = layer1D.isVsZero();
				VCPair vcPW = frame.getRayTracer().getVCPair();
				boolean isObsSTExist = vcPW.isObsSTExist();
				int iType = 0;
				if(isVsZero || !isObsSTExist) {
					iType += 10;
				}
				SaDialog dialog = new SaDialog(frame, title2, false, iType, 0, frame.getSaCwd());
				dialog.showDialog();
			}
		});
		jMenu.add(jMenuItem);
		
		jMenu.addSeparator();
		
		final String title3 = "Simultaneous SA Optimization";
		jMenuItem  	= new JMenuItem(title3);
		jMenuItem.setToolTipText("Simultaneously calibrate both velocity and anisotropy");
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				if(rayTracerIsInvalid()) return;
				FlatLayer1D layer1D 	= frame.getRayTracer().getFlatLayer1D();
				boolean isVsZero = layer1D.isVsZero();
				VCPair vcPW = frame.getRayTracer().getVCPair();
				boolean isObsSTExist = vcPW.isObsSTExist();
				int iType = 2;
				if(isVsZero || !isObsSTExist) {
					iType += 10;
				}
				SaDialog dialog = new SaDialog(frame, title3, false, iType, 0, null);
				dialog.showDialog();
			}
		});
		jMenu.add(jMenuItem);
		
		jMenu.addSeparator();
		
		final String title5 = "Epsilon-Delta Overburden Stripping";
		jMenuItem  	= new JMenuItem(title5);
		jMenuItem.setToolTipText("");
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				if(rayTracerIsInvalid()) return;
				StrippingDialog dialog = new StrippingDialog(frame, title5, false, null, 1);
				dialog.showDialog();
			}
		});
		jMenu.add(jMenuItem);

		final String title4 = "Epsilon-Gamma Overburden Stripping";
		jMenuItem  	= new JMenuItem(title4);
		jMenuItem.setToolTipText("");
		jMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				if(rayTracerIsInvalid()) return;
				StrippingDialog dialog = new StrippingDialog(frame, title4, false, null, 0);
				dialog.showDialog();
			}
		});
		jMenu.add(jMenuItem);
//		
//		jMenuItem  	= new JMenuItem("Levenberg Marquardt Algorithm");
//		jMenuItem.setToolTipText("One of damped least-squares method is based on derivative");
//		jMenuItem.addActionListener( new ActionListener() {
//			public void actionPerformed( ActionEvent e ) {
//				if(rayTracerIsInvalid()) return;
//				SaDialog dialog = new SaDialog(_frame, "Anisotropy Simulated Annealing", false, 2, 1, null);
//				dialog.showDialog();
//			}
//		});
//		jMenu.add(jMenuItem);
//		
//		//jMenu.addSeparator();
//		
//		jMenuItem  	= new JMenuItem("Effective Velocity");
//		jMenuItem.setToolTipText("Calculate the effective velocity");
//		jMenuItem.addActionListener( new ActionListener() {
//			public void actionPerformed( ActionEvent e ) {
//				if(rayTracerIsInvalid()) return;
//				EffectiveVelocityDialog dialog = new EffectiveVelocityDialog(_frame, null, "Effective Velocity", false, 
//						_frame.getProjectFullPath());
//				dialog.showDialog();
//			}
//		});
//		jMenu.add(jMenuItem);
	}
	

	public void setModeMenu(JMenu jMenu) {		
		JMenuItem jMenuItem = null;
		String [] label = null;

		label = new String[] { "Display Vp", "Display Vs"};
		for(int i=0; i<label.length; i++) {
			jMenuItem  	= new JMenuItem(label[i]);
			jMenu.add(jMenuItem);
			final int iVp = i+1;
			jMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					frame.setIVp(iVp);
					frame.updateWorld(true);
				}
			});
		}
//		jMenu.addSeparator();
//		
//		String [] cardsLabel = new String[]{"2D", "3D", "Log", "VSP"};
//		for(int i=0; i<cardsLabel.length; i++) {
//			final String currentCardLabel = cardsLabel[i];
//			jMenuItem  	= new JMenuItem(currentCardLabel);
//			jMenu.add(jMenuItem);
//			jMenuItem.addActionListener( new ActionListener() {
//				public void actionPerformed( ActionEvent e ) {
//					VcCardPanel card = _frame.getCard();
//					int k = card.getIndex(currentCardLabel);
//					if(k>=0) card.show(currentCardLabel);
////					else {
////						card.show("Help");
////						add(_menuOptimization);
////						remove(_menuFd);
////						revalidate();
////						_frame.setVisible(true);
////					}
//					
//					boolean a = currentCardLabel.equalsIgnoreCase("2D")||currentCardLabel.equalsIgnoreCase("3D");
//					_menuOptimization.setEnabled(a);
//					_menuFd.setEnabled(a);
//					_menuPlot.getMenuComponent(0).setEnabled(a);
//					_menuPlot.getMenuComponent(1).setEnabled(a);
//					_menuPlot.getMenuComponent(2).setEnabled(a);
//					_menuEdit.getMenuComponent(0).setEnabled(a);
//					
//					_menuMode.getMenuComponent(0).setEnabled(a);
//					_menuMode.getMenuComponent(1).setEnabled(a);
//					
//					if(a) {
//						_menuFd.getMenuComponent(0).setEnabled(currentCardLabel.equalsIgnoreCase("2D"));
//						_menuFd.getMenuComponent(1).setEnabled(currentCardLabel.equalsIgnoreCase("3D"));
//					}
//					
//				}
//			});
//		}
	}
	//public void setEditMenu() { menuEdit.removeAll(); setEditMenu(menuEdit); }
	public void setEditMenu(JMenu jMenu) {
		JMenuItem jMenuItem  	= null;
		
		jMenuItem  	= new JMenuItem("Layer Property");
		jMenu.add(jMenuItem);
		jMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String aTitle = new String("Layer Info");
				PropertyEditor dialog = new PropertyEditor(frame, aTitle, false, true);
				dialog.showDialog();
			}
		});
		
		jMenu.addSeparator();
		
		JMenuItem subMenuItem  	= null;
		String [] label = null;
		double [] minMax = null;
		jMenuItem  	= new JMenu("Model Dimension");
		jMenu.add(jMenuItem);
		
		if(frame.getProject().is2D()) {
			label = new String[]{"Offset Min:  ", "Offset Max: ", "Depth Min:  ", "Depth Max: "};
			//System.out.println("x000="+getProject().getLayer().getX0()+" x1="+getProject().getLayer().getX1()+
			//	" z0="+getProject().getLayer().getZ0()+" z1="+getProject().getLayer().getZ1());
			if(frame.getProject().getLayer2D()==null) {
				minMax = new double[] { 0, 1, 0, 1 };
			} else {
				DipLayer1D dipLayer1D = frame.getProject().getDipLayer1D();
				ArrayList<SingleFlatLayer> layers = dipLayer1D.getLayer();
				double x0 = layers.get(0).getX0();
				double x1 = layers.get(0).getX1();
				double z0 = layers.get(0).getTopDepth();
				double z1 = layers.get(layers.size()-1).getTopDepth();
				//minMax = new double[] {x0, x1, z0, z1};
				minMax = new double[] {x0, x1};
			}				
		} else {
			Layer3D layer3D = frame.getProject().getLayer3D();
			
			label = new String[]{"Northing Min:  ", "Northing Max: ", "Easting Min:    ", "Easting Max:   ", 
					"Depth Min:     ", "Depth Max:    "};
			minMax = new double[] {
					layer3D.getX0(), layer3D.getX1(), 
					layer3D.getY0(), layer3D.getY1() 
					//,layer3D.getZ0(), layer3D.getZ1()
			};
		}
		minMaxRange 		= new JTextField [minMax.length];
		for(int i=0; i<minMaxRange.length; i++) {
			final String itemName = label[i];
			minMaxRange[i] = new JTextField(minMax[i]+"");
			LabelTextCombo subLabelTextCombo  	= new LabelTextCombo(itemName, minMaxRange[i]);
			jMenuItem.add(subLabelTextCombo);
		}
		subMenuItem  	= new JMenuItem("Set Model Dimension");
		jMenuItem.add(subMenuItem);
		subMenuItem.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				if(frame.getProject().is2D()) {
					double x0 = Double.parseDouble(minMaxRange[0].getText().trim());
					double x1 = Double.parseDouble(minMaxRange[1].getText().trim());
					//double z0 = Double.parseDouble(_minMaxRange[2].getText().trim());
					//double z1 = Double.parseDouble(_minMaxRange[3].getText().trim());
					DipLayer1D dipLayer1D = frame.getProject().getDipLayer1D();
					dipLayer1D.setModelSize(x0, x1);										
					Layer2D layer2D = dipLayer1D.toLayer2D();
					layer2D.setIUnit(dipLayer1D.getIUnit());
					frame.getProject().setLayer2D(layer2D);
					frame.updateWorld();
				} else {
					double x0 = Double.parseDouble(minMaxRange[0].getText().trim());
					double x1 = Double.parseDouble(minMaxRange[1].getText().trim());
					double y0 = Double.parseDouble(minMaxRange[2].getText().trim());
					double y1 = Double.parseDouble(minMaxRange[3].getText().trim());
					//double z0 = Double.parseDouble(_minMaxRange[4].getText().trim());
					//double z1 = Double.parseDouble(_minMaxRange[5].getText().trim());
					
					DipLayer1D dipLayer1D = frame.getProject().getDipLayer1D();
					dipLayer1D.setModelSize(x0, x1, y0, y1);										
					Layer3D layer3D = dipLayer1D.toLayer3D();
					layer3D.setIUnit(dipLayer1D.getIUnit());
					frame.getProject().setLayer3D(layer3D);
					frame.getVel3dPanel().updateLayer3D(layer3D);
					frame.updateWorld();
				}
			}
		});
	}

	private class ImageSelection implements Transferable {
		private Image image;

		public ImageSelection(Image image) {
			this.image = image;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { DataFlavor.imageFlavor };
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.imageFlavor.equals(flavor);
		}

		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
			if (!DataFlavor.imageFlavor.equals(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			return image;
		}
	}
}
