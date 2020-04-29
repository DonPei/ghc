package org.ucdm.launcher.dialog;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.io.FilenameUtils;
import org.ucdm.common.CommonDialog;
import org.ucdm.common.CommonFrame;
import org.ucdm.common.CommonPanel;
import org.ucdm.common.d3.Common3DPanel;
import org.ucdm.common.d3.Common3DVisibilityDialog;
import org.ucdm.component.LabelTextCombo;
import org.ucdm.component.StatusBar;
import org.ucdm.core.planarmodel.Grid3D;
//import org.ucdm.mtv.d3.SelectDragPopMode;

import edu.mines.jtk.awt.Mode;
import edu.mines.jtk.awt.ModeManager;
import edu.mines.jtk.dsp.Sampling;
import edu.mines.jtk.mosaic.PixelsView;
import edu.mines.jtk.sgl.AxesOrientation;
import edu.mines.jtk.sgl.Axis;
import edu.mines.jtk.sgl.BoundingBox;
import edu.mines.jtk.sgl.BoundingSphere;
import edu.mines.jtk.sgl.ImagePanel;
import edu.mines.jtk.sgl.ImagePanelGroup;
import edu.mines.jtk.sgl.OrbitView;
import edu.mines.jtk.sgl.OrbitViewMode;
import edu.mines.jtk.sgl.ViewCanvas;
import edu.mines.jtk.sgl.World;
import edu.mines.jtk.util.ArrayMath;

public class Grid3DPanel3D  extends Common3DPanel{
	private static final long serialVersionUID = 1L;
	
	public ModeManager 			_mm					= null;
	public Grid3DMouseEditingMode _mouseEditingMode = null; 		// mouse track mode
	//public SelectDragPopMode 	_sdm				= null;
	
	public Grid3D 				_grid3D 			= null;
	public StatusBar 			_statusBar			= null;
	public 	JLabel 				_resultsLabel		= null;
	protected ImagePanelGroup 	_ipg 				= null;
	
	private int 				_cI 				= 0;
	private int 				_cJ 				= 0;
	private int 				_cK 				= 0;
	
	private boolean   			_axisX 				= true;
	private boolean   			_axisY 				= true;
	private boolean   			_axisZ 				= true;

	public Grid3DColorMap 		_grid3DColorMap 	= new Grid3DColorMap();
	
	public Grid3DPanel3D(CommonFrame frame, Grid3D grid3D) {
		super(100, frame);
		_grid3D 		= grid3D; 	
		BoundingBox globalBox = new BoundingBox(grid3D.getX0(), grid3D.getY0(), grid3D.getZ0(), 
				grid3D.getX1(), grid3D.getY1(), grid3D.getZ1());
		
		World world3d = new World();
		OrbitView view = (world3d!=null)?new OrbitView(world3d):new OrbitView();
		view.setAxesOrientation(AxesOrientation.XRIGHT_YOUT_ZDOWN);
		view.setWorldSphere(new BoundingSphere(globalBox));
		view.setAzimuth(-90.0);

		ViewCanvas canvas = new ViewCanvas(view);
		canvas.setView(view);
		canvas.setBackground(Color.white);
		setLayout(new BorderLayout(2, 2));
		add(canvas, BorderLayout.CENTER);
		add(_statusBar=genStatusBar(), BorderLayout.SOUTH);

		OrbitView orbitView 	= (OrbitView)canvas.getView();
		//orbitView.setElevation(45);
		orbitView.setAzimuthAndElevation(-45, 30);
		
		//orbitView.setAxesScale(1.0, 1.0, 0.5);
		
		ModeManager mm = new ModeManager();
		mm.add(canvas);
		
		_mouseEditingMode = new Grid3DMouseEditingMode(mm);
		_mouseEditingMode.setPanel(this);
		_mouseEditingMode.setActive(true);	
		
		OrbitViewMode ovm = new OrbitViewMode(mm);
		ovm.setOriginalScale(orbitView.getScale());
		//_sdm = new SelectDragPopMode(frame, _mm);
		ovm.setActive(true);
				
		setParameters(canvas, orbitView, ovm, world3d, globalBox);
		
		constructWorld(_world, _globalBox);
		revalidate();
	}
	
	public void constructWorld(World world, BoundingBox globalBox) {
		if(world!=null) 			removeAllViews();
		if(globalBox!=null) 		drawBox(world, globalBox);
		
		_cI = _grid3D.getNz()/2;
		_cJ = _grid3D.getNy()/2;
		_cK = _grid3D.getNx()/2;
		Sampling s1 = new Sampling(_grid3D.getNz(),_grid3D.getDz(),_grid3D.getZ0());
	    Sampling s2 = new Sampling(_grid3D.getNy(),_grid3D.getDy(),_grid3D.getY0());
	    Sampling s3 = new Sampling(_grid3D.getNx(),_grid3D.getDx(),_grid3D.getX0());
	    _ipg = new ImagePanelGroup(s1,s2,s3,_grid3D.getData(),new Axis[]{Axis.X,Axis.Y,Axis.Z});
	    _ipg.setPercentiles(1,99);
	    _ipg.setName("Slide", "All");
		remove(getWorld(), _ipg.getCategory(), _ipg.getName());
		_world.addChild(_ipg);
		
		update();
	}
	
	public Grid3D getGrid3D() 		{ return _grid3D; }
	public boolean getAxisX() 		{ return _axisX; }
	public boolean getAxisY() 		{ return _axisY; }
	public boolean getAxisZ() 		{ return _axisZ; }
	
	public int getCI() 				{ return _cI; }
	public int getCJ() 				{ return _cJ; }
	public int getCK() 				{ return _cK; }
	
	public void setAxisX(boolean axisX) 		{ _axisX = axisX; }
	public void setAxisY(boolean axisY) 		{ _axisY = axisY; }
	public void setAxisZ(boolean axisZ) 		{ _axisZ = axisZ; }
	
	public void setCI(int cI) 		{ _cI = cI; }
	public void setCJ(int cJ) 		{ _cJ = cJ; }
	public void setCK(int cK) 		{ _cK = cK; }
	
	public void updateSection() {				
		Sampling s1 = new Sampling(_grid3D.getNz(),_grid3D.getDz(),_grid3D.getZ0());
	    Sampling s2 = new Sampling(_grid3D.getNy(),_grid3D.getDy(),_grid3D.getY0());
	    Sampling s3 = new Sampling(_grid3D.getNx(),_grid3D.getDx(),_grid3D.getX0());
	    
	    ArrayList<Axis> list = new ArrayList<Axis>();
	    if(_axisX) list.add(Axis.X);
	    if(_axisY) list.add(Axis.Y);
	    if(_axisZ) list.add(Axis.Z);
	    
	    Axis [] axises = new Axis[list.size()];
	    for(int i=0; i<list.size(); i++) axises[i] = list.get(i);	    
	    
	    _ipg = new ImagePanelGroup(s1,s2,s3,_grid3D.getData(),axises);
	    _ipg.setPercentiles(1,99);
	    _ipg.setName("Slide", "All");
		remove(getWorld(), _ipg.getCategory(), _ipg.getName());
		_world.addChild(_ipg);
		updateIntersectionPoint();
	}
	
	public void updateIntersectionPoint() {
	    _ipg.setSlices(_cI, _cJ, _cK);
	    update();
	}
	
	public void update() {
		if(_grid3DColorMap.getAutoColor()) {
			float minT = ArrayMath.min(_grid3D.getData());
			float maxT = ArrayMath.max(_grid3D.getData());
			_grid3DColorMap.setMinColor(minT);
			_grid3DColorMap.setMaxColor(maxT);
			_grid3DColorMap.getMinColorTF().setText(minT+"");
			_grid3DColorMap.getMaxColorTF().setText(maxT+"");
			//System.out.println("min1="+_grid3DColorMap.getMinColor()+" max="+_grid3DColorMap.getMaxColor());
		}
		//_resultsLabel.setText(label);
	    _ipg. setColorModel(_grid3DColorMap.getIndexColorModel());
	    _ipg.setClips(_grid3DColorMap.getMinColor(), _grid3DColorMap.getMaxColor());
		Iterator<ImagePanel> ipListItr = _ipg.getImagePanels();
	    while(ipListItr.hasNext()) {
	    	ImagePanel ip = ipListItr.next();
	    	ip.update();
	    }
	}

	public StatusBar getStatusBar() 					{ return _statusBar; }
	private StatusBar genStatusBar() {
		StatusBar statusBar = new StatusBar(); 
		statusBar.setZoneBorder(BorderFactory.createLineBorder(Color.GRAY)); 
		statusBar.setZones( new String[] { "first_zone", "second_zone", "remaining_zones" },     
				new Component[] { new JLabel("first"), new JLabel("second"), new JLabel("remaining")},     
				new String[] {"15%", "40%", "*"} );
		
		String usageText = "<html> "
				+ "To menu: <b>Right-click</b></html>";
		_resultsLabel  = (JLabel) statusBar.getZone("second_zone");
		_resultsLabel.setText(usageText);	
		
		return statusBar;
	}
	
	private class ControlDialog  extends CommonDialog {
		private Grid3DPanel3D 		_plot3D			= null;		
		
		public ControlDialog(Grid3DPanel3D plot3D, String aTitle, boolean modal) {
			super(plot3D.getFrame(), aTitle, modal);
			setDialogWindowSize(800, 400);
			setBackground(Color.white);	
			_plot3D 	= plot3D;
		}
		
		protected JComponent getCommandRow() 	{ return null; }
		protected boolean okAction() 			{ return true; }
		
		protected JPanel createContents() {
			JPanel jPanel = new JPanel(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;

			String [] moduleString = null;
			int n = 0;

			JPanel modulePanel = null;	
			JCheckBox [] moduleCheckBox = null;
			JSlider [] moduleSlider = null;
			
			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			
			moduleString = new String[] { new String( "X"), new String( "Y"), new String( "Z")};
			n = moduleString.length;

			modulePanel = new JPanel( new GridLayout(1, n+2, 2, 2));	
			moduleCheckBox = new JCheckBox[n];
			for(int i=0; i<n; i++) {
				final int j = i;
				moduleCheckBox[i] = new JCheckBox(moduleString[i], true);
				moduleCheckBox[i].addItemListener( new ItemListener() {
					public void itemStateChanged(ItemEvent itemEvent) {
						JCheckBox cb = (JCheckBox) itemEvent.getSource();
						if(j==0) _plot3D.setAxisX(cb.isSelected());
						if(j==1) _plot3D.setAxisY(cb.isSelected());
						if(j==2) _plot3D.setAxisZ(cb.isSelected());
				        _plot3D.updateSection();
					}
				});

				modulePanel.add(moduleCheckBox[i]);
			}
			
			JButton dump = new JButton("Dump To Memory");
			modulePanel.add(dump);
			dump.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent event) {
//					String fileName = _imageFolder+"plot3D_"+label+".jpg";
//					_plot3D.getViewCanvas().paintToFile(fileName);
					BufferedImage image = null;
					try {
						ViewCanvas canvas = _plot3D.getViewCanvas();
						Point pt = canvas.getLocationOnScreen();
						Robot robot = new Robot();
						Dimension d = canvas.getSize();
						Rectangle bounds = new Rectangle(pt.x, pt.y, d.width, d.height);
						image = robot.createScreenCapture(bounds);
					} catch (AWTException e1) { e1.printStackTrace(); }
					if(image!=null) {
						ImageSelection imgSel = new ImageSelection(image);
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
					}
				}
			});
			dump = new JButton("Dump To File");
			modulePanel.add(dump);
			dump.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					String a = _grid3D.getFileName();					
					String fileName = FilenameUtils.getFullPath(a)+ FilenameUtils.getBaseName(a)+".jpg";
					//_plot3D.getViewCanvas().paintToFile(fileName);
					BufferedImage image = null;
					try {
						ViewCanvas canvas = _plot3D.getViewCanvas();
						Point pt = canvas.getLocationOnScreen();
						Robot robot = new Robot();
						Dimension d = canvas.getSize();
						Rectangle bounds = new Rectangle(pt.x, pt.y, d.width, d.height);
						image = robot.createScreenCapture(bounds);
						
						File outputfile = new File(fileName);
						if(image!=null)  ImageIO.write(image, "jpg", outputfile); 
					} catch (AWTException e1) { e1.printStackTrace(); } 
					catch (IOException e) { e.printStackTrace(); }					
				}
			});

			int iRow = 0;
			loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			modulePanel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Image Section:"));
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
			jPanel.add(modulePanel, gbc);	
			
			n = 3;
			modulePanel = new JPanel( new GridLayout(n, 1, 5, 10));	
			Grid3D grid3D = _plot3D.getGrid3D();
			int [] maxValue = new int[] { grid3D.getNx(), grid3D.getNy(), grid3D.getNz() };
			int [] currValue = new int[] { _plot3D.getCI(), _plot3D.getCJ(), _plot3D.getCK() };
			moduleSlider = new JSlider[n];
			for(int i=0; i<n; i++) {
				final int j = i;
				moduleSlider[i] = new JSlider(JSlider.HORIZONTAL, 0, maxValue[i], currValue[i]);
				moduleSlider[i].addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						JSlider jSlider = (JSlider)e.getSource();
						if (!jSlider.getValueIsAdjusting()) {
						} else {
							int value = jSlider.getValue();
							if(j==0) 		_plot3D.setCI(value);
							else if(j==1) 	_plot3D.setCJ(value);
							else if(j==2) 	_plot3D.setCK(value);
							_plot3D.updateIntersectionPoint();
						}
					}
				});
				moduleSlider[i].setMinorTickSpacing(5);
				moduleSlider[i].setMajorTickSpacing(10);
				moduleSlider[i].setPaintTicks(true);
				moduleSlider[i].setPaintLabels(true); 
				moduleSlider[i].setPaintTrack(true);
				modulePanel.add(moduleSlider[i]);
			}

			iRow++;
			loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			modulePanel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Intersection Location:"));
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
			jPanel.add(modulePanel, gbc);	

//			iRow++;
//			gbc= new GridBagConstraints(0, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
//			jPanel.add(new JLabel(), gbc);
		
			return jPanel;
		}
	}

	private class Grid3DMouseEditingMode extends Mode {
		private static final long serialVersionUID = 1L;
		private Grid3DPanel3D 	_panel 		= null;
		
		private JPopupMenu 		_popupMenu 				= new JPopupMenu();

		private JTextField 		_rFactorTF 				= null;
		private JTextField 		_heightTF 				= null;
	
		public Grid3DMouseEditingMode(ModeManager modeManager) {
			super(modeManager);
			setName("Track");
			//setIcon(loadIcon(MouseTrackInfoMode.class,"Track24.gif"));
			setMnemonicKey(KeyEvent.VK_Z);
			setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_T,0));
			setShortDescription("Track mouse in tile");
		}
		
		public void setPanel(Grid3DPanel3D panel) 	{ _panel = panel; }		
		public boolean isExclusive() { return false; }

		protected void setActive(Component component, boolean active) {
			if (active) {
				component.addMouseListener(_ml);
			} else {
				component.removeMouseListener(_ml);
			}
		}

		private MouseListener _ml = new MouseAdapter() {
			
			public void mousePressed(MouseEvent evt) {
				//int px = evt.getXOnScreen();
				int px = evt.getX();
				int py = evt.getY();
				if((!evt.isShiftDown())&& (!evt.isAltDown()) && evt.isMetaDown()) {
					_popupMenu = new JPopupMenu();
					setPopupMenu();
					_popupMenu.show(evt.getComponent(), px, py);
				}
			}

			public void mouseReleased(MouseEvent evt) { 
				if((!evt.isShiftDown())&&(!evt.isAltDown())) { }
			}
		};

		public void setPopupMenu() {
			JMenuItem jMenuItem = null;
			
			JCheckBoxMenuItem defaultColorMenuItem = new JCheckBoxMenuItem("Default Color", _grid3DColorMap.getAutoColor());
			_popupMenu.add(defaultColorMenuItem);
			defaultColorMenuItem.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					_grid3DColorMap.setAutoColor(defaultColorMenuItem.isSelected());
				}
			});			
			
			JMenu clipColorMap = new JMenu("Clip ColorMap");
			clipColorMap.add(new LabelTextCombo("Min Value: ", _grid3DColorMap.getMinColorTF()));
			clipColorMap.add(new LabelTextCombo("Max Value:", _grid3DColorMap.getMaxColorTF()));
			jMenuItem  	= new JMenuItem("    Clip  ");  
			clipColorMap.add(jMenuItem);
			jMenuItem.addActionListener(new ActionListener() 	{
				public void actionPerformed(ActionEvent e)   {
					float minColor = Float.parseFloat(_grid3DColorMap.getMinColorTF().getText().trim());
					float maxColor = Float.parseFloat(_grid3DColorMap.getMaxColorTF().getText().trim());
					if(maxColor>minColor) {
						_grid3DColorMap.setMinColor(minColor);
						_grid3DColorMap.setMaxColor(maxColor);
						_grid3DColorMap.getMinColorTF().setText(minColor+"");
						_grid3DColorMap.getMaxColorTF().setText(maxColor+"");
						update();
					}
				}
			});
			_popupMenu.add(clipColorMap);
					
			String[] indexColorModelString = _grid3DColorMap.getIndexColorModelString();
			JMenu indexColorModel = new JMenu("Color Model");
			for(int i=0; i<indexColorModelString.length; i++) {
				final int j = i;
				jMenuItem  	= new JMenuItem(indexColorModelString[i]);  
				indexColorModel.add(jMenuItem);
				jMenuItem.addActionListener(new ActionListener() 	{
					public void actionPerformed(ActionEvent e)   {
						_grid3DColorMap.setIndexColorModelId(j);
						update();
					}
				});
			}
			_popupMenu.add(indexColorModel);			
			
			_popupMenu.addSeparator();
			
			jMenuItem = new JMenuItem("Control Panel");
			_popupMenu.add(jMenuItem);
			jMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {					
					ControlDialog dialog = new ControlDialog(_panel, "Control", false);
					dialog.showDialog();
				}
			});
			
			jMenuItem = new JMenuItem("Visibility");
			_popupMenu.add(jMenuItem);
			jMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String [] categories = _panel.getWorldUniqueCategory();

					ArrayList<String> 		cList = new ArrayList<String>();
					ArrayList<String[]> 	nList = new ArrayList<String[]>();
					ArrayList<Boolean[]> 	vList = new ArrayList<Boolean[]>();
					for(int i=0; i<categories.length; i++) {
						cList.add(categories[i]);
						vList.add(_panel.getEntityVisibility(categories[i]));
						nList.add(_panel.getUniqueName(_panel.getEntityName(categories[i])));
					}
					
					Common3DVisibilityDialog dialog = new Common3DVisibilityDialog(_panel, "Visibility", false);
					dialog.setParameters(cList, nList, vList);
					dialog.showDialog();
				}
			});
			
			_popupMenu.addSeparator();
			
			jMenuItem = new JMenuItem("Color Legend");
			_popupMenu.add(jMenuItem);
			jMenuItem.addActionListener(new ActionListener() 	{
				public void actionPerformed(ActionEvent e)   {
					Grid3DLegendDialog dialog = new Grid3DLegendDialog(_frame, "Legend", false, 
							_grid3DColorMap.getMinColor(), _grid3DColorMap.getMaxColor());
					dialog.showDialog();
				}
			});
			
			jMenuItem = new JMenuItem("Refresh");
			_popupMenu.add(jMenuItem);
			jMenuItem.addActionListener(new ActionListener() 	{
				public void actionPerformed(ActionEvent e)   {
					update();
				}
			});			
		}
	}
	
	private class Grid3DLegendDialog  extends CommonDialog {
		private CommonPanel 		_world 	= null;
		private float 				_minT 	= 0;
		private float 				_maxT 	= 0;

		public Grid3DLegendDialog(JFrame aParent, String aTitle, boolean modal, float minT, float maxT) {	
			super(aParent, aTitle, modal);
			setDialogWindowSize(50, 400);
			_minT 		= minT;
			_maxT 		= maxT;

			_world = new CommonPanel(1, 1, CommonPanel.Orientation.X1RIGHT_X2UP);
			_world.setBackground(Color.white);
			_world.setFrame((CommonFrame)aParent);
			Font font = new Font ("Arial", Font.BOLD, 12); //Monospaced, Serif, Dialog, Sanserif
			_world.setFont(font);
			//_world.getMosaic().getTileAxisBottom(0).setShowTimeFormat(false);
			_world.getMosaic().getTileAxisBottom(0).setVisible(false);
		}

		protected JComponent getCommandRow() { return null; }
		protected boolean okAction() {	return true;	}
		protected JPanel createContents() {
			float minT = _minT;
			float maxT = _maxT;
			int nSamples = 255;
			float dt = (maxT-minT)/(nSamples-1);
			float [][] legendData = new float[nSamples][3];
			for(int i=0; i<nSamples; i++) {
				float a = minT+i*dt;
				for(int j=0; j<3; j++) legendData[i][j] = a;
			}
			Sampling samplingZ = new Sampling(legendData.length, dt, minT);
			Sampling samplingX = new Sampling(legendData[0].length, 1, 0);
				
			PixelsView pv = new PixelsView(samplingX, samplingZ, legendData);
			//pv.setColorModel(ColorMap.HUE_BLUE_TO_RED);
			//pv.setColorModel(ColorMap.JET);
			pv.setColorModel(_grid3DColorMap.getIndexColorModel());
			//pv.setClips(-1.0f, 1.0f);
			pv.setName("Legend", "Pixel");
			_world.removeView(pv.getCategory(), pv.getName());
			_world.addPixelsView(0, 0, pv);	
			
			return _world;
		}

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
