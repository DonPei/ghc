package com.geohammer.common;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

import com.geohammer.component.PointsViewWithLegend;
import com.geohammer.component.StatusBar;
import com.geohammer.component.TransparentPixelsView;

import static java.lang.Math.*;
import edu.mines.jtk.awt.ColorMap;
import edu.mines.jtk.awt.ColorMapped;
import edu.mines.jtk.awt.ColorMapListener;
import edu.mines.jtk.awt.ModeManager;
import edu.mines.jtk.dsp.Sampling;
import edu.mines.jtk.mosaic.BarsView;
import edu.mines.jtk.mosaic.ColorBar;
import edu.mines.jtk.mosaic.ContoursView;
import edu.mines.jtk.mosaic.ContoursViewDouble;
import edu.mines.jtk.mosaic.GridView;
import edu.mines.jtk.mosaic.IPanel;
import edu.mines.jtk.mosaic.Mosaic;
import edu.mines.jtk.mosaic.PixelsView;
import edu.mines.jtk.mosaic.PixelsViewDouble;
import edu.mines.jtk.mosaic.PointsView;
import edu.mines.jtk.mosaic.Projector;
import edu.mines.jtk.mosaic.SequenceView;
import edu.mines.jtk.mosaic.Tile;
import edu.mines.jtk.mosaic.TiledView;
import edu.mines.jtk.sgl.Group;
import edu.mines.jtk.sgl.OrbitViewMode;
import edu.mines.jtk.sgl.World;
import edu.mines.jtk.util.Check;


public class CommonPanel extends IPanel {
	private static final long serialVersionUID = 1L;

	public enum AxesPlacement {
		LEFT_TOP,
		LEFT_BOTTOM,
		LEFT_BOTTOM_RIGHT,
		LEFT_BOTTOM_RIGHT_TOP,
		BOTTOM,
		NONE
	}

	public enum Orientation {
		X1RIGHT_X2UP,
		X1DOWN_X2RIGHT
	}

	public JLabel 			_trackingLabel 	= null;	
	public String 			_trackingLabelFormat 	= "x:%.2f, y:%.2f";	
	private double 			_vx 			= 0;
	private double 			_vy 			= 0;

	private boolean 		_enableTracking	= false;
	private boolean 		_enableEditing	= false;
	private boolean 		_enableZoom		= false;

	public CommonFrame 		_frame  		= null;
	private CommonMouseEditingMode _mouseEditingMode = null;
	private CommonMouseTrackInfoMode _mouseTrackInfoMode = null;
	
	private double 	[]	_minMax 	= new double[4];	
	private String 		_hLabel 	= null;
	private String 		_vLabel 	= null;
	
	private ModeManager _modeManager 	= null;
	
	protected ArrayList<String> 	_cList = new ArrayList<String>();
	protected ArrayList<String[]> 	_nList = new ArrayList<String[]>();
	protected ArrayList<Boolean[]> 	_vList = new ArrayList<Boolean[]>();
	
	
	private static AxesPlacement axesPlacement(Orientation orientation) {
		AxesPlacement axesPlacement;
		if (orientation==Orientation.X1DOWN_X2RIGHT) {
			axesPlacement = AxesPlacement.LEFT_TOP;
		} else {
			axesPlacement = AxesPlacement.LEFT_BOTTOM;
		}
		return axesPlacement;
	}

	public CommonPanel() 						{ this(1,1,Orientation.X1RIGHT_X2UP); }
	public CommonPanel(int nrow, int ncol) 		{ this(nrow,ncol,Orientation.X1RIGHT_X2UP); }
	public CommonPanel(Orientation orientation) { this(1,1,orientation); }
	public CommonPanel(int nrow, int ncol, Orientation orientation) {
		this(nrow,ncol,orientation,axesPlacement(orientation), null);
	}
	public CommonPanel(int nrow, int ncol, Orientation orientation, AxesPlacement axesPlacement, StatusBar statusBar) {
		this(nrow, ncol, orientation, axesPlacement, false, statusBar);
	}
	public CommonPanel(int nrow, int ncol, Orientation orientation, AxesPlacement axesPlacement, boolean isTop, StatusBar statusBar) {
		super();
		setBackground(Color.white);
		_orientation = orientation;
		_axesPlacement = axesPlacement;
		setLayout(new GridBagLayout());
		Set<Mosaic.AxesPlacement> axesPlacementSet;
		if (axesPlacement==AxesPlacement.LEFT_TOP) {
			axesPlacementSet = EnumSet.of(
					Mosaic.AxesPlacement.LEFT,
					Mosaic.AxesPlacement.TOP
					);
		} else if (axesPlacement==AxesPlacement.LEFT_BOTTOM) {
			axesPlacementSet = EnumSet.of(
					Mosaic.AxesPlacement.LEFT,
					Mosaic.AxesPlacement.BOTTOM
					);
		} else if (axesPlacement==AxesPlacement.LEFT_BOTTOM_RIGHT) {
			axesPlacementSet = EnumSet.of(
					Mosaic.AxesPlacement.LEFT,
					Mosaic.AxesPlacement.RIGHT, 
					Mosaic.AxesPlacement.BOTTOM
					);
		} else if (axesPlacement==AxesPlacement.LEFT_BOTTOM_RIGHT_TOP) {
			axesPlacementSet = EnumSet.of(
					Mosaic.AxesPlacement.LEFT,
					Mosaic.AxesPlacement.BOTTOM, 
					Mosaic.AxesPlacement.RIGHT, 
					Mosaic.AxesPlacement.TOP
					);
		} else if (axesPlacement==AxesPlacement.BOTTOM) {
			axesPlacementSet = EnumSet.of(
					Mosaic.AxesPlacement.BOTTOM
					);
		} else {
			axesPlacementSet = EnumSet.noneOf(Mosaic.AxesPlacement.class);
		}
		_mosaic = new Mosaic(nrow,ncol,axesPlacementSet);
		_colorMapHandler = new ColorMapHandler();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 100;
		gbc.weighty = 100;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(2, 0, 2, 4);
		add(_mosaic,gbc);

		if(statusBar!=null) {
			int iRow = 2;
			if(isTop) iRow = 0;
			_trackingLabel = (JLabel)statusBar.getZone("remaining_zones");
			Insets insets = new Insets(5, 10, 1, 1);
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0);			
			add(statusBar,gbc);
		}
		setPreferredSize(new Dimension(200+300*ncol,100+300*nrow));

		revalidate();
	}
	
	public void addStatusBar(boolean isTop, StatusBar statusBar) {
		if(statusBar==null) return;
		int iRow = 2;
		if(isTop) iRow = 0;
		_trackingLabel = (JLabel)statusBar.getZone("remaining_zones");
		Insets insets = new Insets(5, 10, 1, 1);
		GridBagConstraints gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0);			
		add(statusBar,gbc);
		revalidate();
	}
	
	public void updateVisibility() {
		String [] categories = getWorldUniqueCategory();

		ArrayList<String> 		cList = new ArrayList<String>();
		ArrayList<String[]> 	nList = new ArrayList<String[]>();
		ArrayList<Boolean[]> 	vList = new ArrayList<Boolean[]>();
		for(int i=0; i<categories.length; i++) {
			cList.add(categories[i]);
			vList.add(getEntityVisibility(categories[i]));
			nList.add(getEntityName(categories[i]));
		}
		
		_cList = cList;
		_nList = nList;
		_vList = vList;
	}	
	
	public boolean isPreviouslyVisible(String category, String name) {
		if(_vList==null||_vList.size()<=0) return true;
		for(int i=0; i<_cList.size(); i++) {
			if(_cList.get(i).equalsIgnoreCase(category.trim())) {
				String[] 	nameList = _nList.get(i);
				Boolean[] 	valueList = _vList.get(i);
				//return valueList[0].booleanValue();
				for(int j=0; j<nameList.length; j++) {
					if(nameList[j].equalsIgnoreCase(name.trim())) return valueList[j].booleanValue();
				}
			}
		}
		return true;
	}
	
	 public BufferedImage paintToImage(boolean saveToFile) {
		 int wpanel = getWidth();
		 int hpanel = getHeight();
		 int type = BufferedImage.TYPE_INT_RGB;
		 BufferedImage image = new BufferedImage(wpanel,hpanel,type);
		 paintToImage(image);
		 //if(_statusBarHeight>1000)
		//	 image = image.getSubimage(0, 0, image.getWidth(), image.getHeight()+_statusBarHeight); 

		 if(saveToFile) {
			 FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] {
					 new FileNameExtensionFilter("PNG (*.png)", "png")
			 };
			 String tmpName = _frame.getProject().getProjectFileName();
			 tmpName = FilenameUtils.getFullPath(tmpName)+"tmp.png";
			 String fileName = _frame.saveFileUsingJFileChooser(exts, tmpName); 
			 if(fileName==null) return null;
			 else {
				 File f = new File(fileName);
				 try { ImageIO.write(image, "png", f);
				 } catch (IOException e1) { e1.printStackTrace(); }
			 }
		 } else {
			 ImageSelection imgSel = new ImageSelection(image);
			 Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null); 
		 }

		 return image;
	 }
	 
	public void setFrame(CommonFrame frame) 				{ _frame = frame; }
	
	//public void setTrackingLabel(JLabel trackingLabel) 		{ _trackingLabel = trackingLabel; }
	public void setEnableTracking(boolean enableTracking) 	{ _enableTracking = enableTracking; }
	public void setTrackingLabelFormat(String trackingLabelFormat) { _trackingLabelFormat = trackingLabelFormat; }
	public void setEnableEditing(boolean enableEditing) 	{ _enableEditing = enableEditing; }
	public void setEnableZoom(boolean enableZoom) 			{ _enableZoom = enableZoom; }
	public void setDataLimits(double hmin, double vmin, double hmax, double vmax) { 
		_minMax[0] = hmin; _minMax[1] = vmin; _minMax[2] = hmax; _minMax[3] = vmax;
	}
	
	public double getDataLimits(int index) 					{ return _minMax[index];  }
	public CommonFrame getFrame() 							{ return _frame; }
	public CommonMouseEditingMode getMouseEditingMode() 	{ return _mouseEditingMode; }
	public CommonMouseTrackInfoMode getMouseTrackInfoMode() { return _mouseTrackInfoMode; }
	public ModeManager getModeManager() 					{ return _modeManager; }
	
	public void addModeManager() {
		_modeManager = new ModeManager();
		_mosaic.setModeManager(_modeManager);
		CommonMouseZoomMode mouseZoomMode = new CommonMouseZoomMode(_modeManager);
		mouseZoomMode.setActive(_enableZoom);

		if(_enableTracking) {
			_mouseTrackInfoMode = new CommonMouseTrackInfoMode(_modeManager);
			//if(_trackingLabel==null) _enableTracking = false;
			_mouseTrackInfoMode.setActive(_enableTracking);
			_mouseTrackInfoMode.setTrackingLabel(_trackingLabel);
			_mouseTrackInfoMode.setTrackingLabelFormat(_trackingLabelFormat);
		}
		if(_enableEditing) {
			_mouseEditingMode = new CommonMouseEditingMode(_modeManager);
			_mouseEditingMode.setCommonPanel(this);
			_mouseEditingMode.setActive(_enableEditing);
		}
	}

	public Mosaic getMosaic() { return _mosaic; }
	public Orientation getOrientation() { return _orientation; }
	public void mousePressedOn(MouseEvent evt) {  }
	
	public Tile getTile(int irow, int icol) {
		return _mosaic.getTile(irow,icol);
	}

	public ColorBar addColorBar() {
		return addColorBar(null,null);
	}

	/**
	 * Adds the color bar with specified label.
	 * @param label the label; null, if none.
	 * @return the color bar.
	 */
	public ColorBar addColorBar(String label) {
		return addColorBar(null,label);
	}

	/**
	 * Adds a color bar with a specified color mapped object and no label.
	 * @param cm the specified color mapped.
	 * @return the color bar.
	 */
	public ColorBar addColorBar(ColorMapped cm) {
		return addColorBar(cm,null);
	}

	/**
	 * Adds a color bar with a specified color mapped object and label.  
	 * If the specified color mapped object is null, then this plot panel
	 * will try to find the best color map to display in the color bar.
	 * @param cm the color mapped object.
	 * @param label the label.
	 * @return the color bar.
	 */
	public ColorBar addColorBar(ColorMapped cm, String label) {
		if (cm!=null) {
			_autoColorMapped = false;
			_colorMapped = cm;
		} else {
			_colorMapped = findBestColorMapped();
		}
		if (_colorBar==null) {
			_colorBar = new ColorBar(label);
			_colorBar.setFont(getFont());
			_colorBar.setForeground(getForeground());
			_colorBar.setBackground(getBackground());
			if (_colorBarFormat!=null)
				_colorBar.setFormat(_colorBarFormat);
			if (_colorBarWidthMinimum!=0)
				_colorBar.setWidthMinimum(_colorBarWidthMinimum);
			if (_colorMapped!=null)
				_colorMapped.getColorMap().addListener(_colorBar);
			add(_colorBar,makeColorBarConstraints());
		} else {
			_colorBar.setLabel(label);
		}
		_colorBar.getTile().getMosaic().setWidthMinimum(0,12);
		//_colorBar.setWidthMinimum(100);
		//_colorBar.setValueRange(double vmin, double vmax)
		revalidate();
		return _colorBar;
	}

	/**
	 * Sets a minimum width (in pixels) for a color bar.
	 * This method is useful when attempting to construct multiple plot 
	 * panels with the same layout. In this scenario, set this minimum
	 * equal to the width of the widest color bar. Then all color bars
	 * will have the same width. Those widths might otherwise vary as tic 
	 * and axes labels vary for the different panels.
	 * @param widthMinimum the minimum width.
	 */
	public void setColorBarWidthMinimum(int widthMinimum) {
		_colorBarWidthMinimum = widthMinimum;
		if (_colorBar!=null) {
			_colorBar.setWidthMinimum(widthMinimum);
			revalidate();
		}
	}

	/**
	 * Sets the format for major tic annotation of the color bar.
	 * The default format is "%1.4G", which yields a minimum of 1 digit,
	 * with up to 4 digits of precision. Any trailing zeros and decimal
	 * point are removed from tic annotation.
	 * @param format the format.
	 */
	public void setColorBarFormat(String format) {
		_colorBarFormat = format;
		if (_colorBar!=null) {
			_colorBar.setFormat(format);
			revalidate();
		}
	}

	/**
	 * Removes the color bar.
	 */
	public void removeColorBar() {
		if (_colorBar!=null) {
			remove(_colorBar);
			revalidate();
			_colorBar = null;
		}
	}

	/**
	 * Adds the plot title. Equivalent to {@link #setTitle(String)}.
	 * The title font is 1.5 times larger than the font of this panel.
	 * @param title the title; null, if none.
	 */
	public void addTitle(String title) {
		setTitle(title);
	}

	/**
	 * Sets the plot title. Equivalent to {@link #addTitle(String)}.
	 * @param title the title; null, for no title.
	 */
	public void setTitle(String title) {
		if (title!=null) {
			if (_title==null) {
				_title = new Title(title);
				Font font = getFont();
				font.deriveFont(1.5f*font.getSize2D());
				_title.setFont(getFont());
				_title.setForeground(getForeground());
				_title.setBackground(getBackground());
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.gridwidth = 2;
				gbc.gridheight = 1;
				gbc.weightx = 0;
				gbc.weighty = 0;
				gbc.fill = GridBagConstraints.NONE;
				gbc.insets.top = 0;
				gbc.insets.bottom = 0;
				gbc.insets.left = 0;
				gbc.insets.right = 0;
				//gbc.insets.left = _mosaic.getWidthAxesLeft();
				//gbc.insets.right = _mosaic.getWidthAxesRight();
				add(_title,gbc);
				revalidate();
			} else {
				_title.set(title);
			}
		} else if (_title!=null) {
			remove(_title);
			revalidate();
			_title = null;
		}
	}

	/**
	 * Removes the plot title. Equivalent to calling the method
	 * {@link #setTitle(String)} with a null title.
	 */
	public void removeTitle() {
		setTitle(null);
	}

	/**
	 * Sets limits for the both horizontal and vertical axes.
	 * By default, limits are computed automatically by tiled graphical views.
	 * This method can be used to override those default limits.
	 * @param hmin the minimum value.
	 * @param vmin the minimum value.
	 * @param hmax the maximum value.
	 * @param vmax the maximum value.
	 */
	public void setLimits(double hmin, double vmin, double hmax, double vmax) {
		setHLimits(hmin,hmax);
		setVLimits(vmin,vmax);
	}

	/**
	 * Sets limits for the horizontal axis.
	 * By default, limits are computed automatically by tiled graphical views.
	 * This method can be used to override those default limits.
	 * @param hmin the minimum value.
	 * @param hmax the maximum value.
	 */
	public void setHLimits(double hmin, double hmax) {
		setHLimits(0,hmin,hmax);
	}

	/**
	 * Sets limits for the vertical axis.
	 * By default, limits are computed automatically by tiled graphical views.
	 * This method can be used to override those default limits.
	 * @param vmin the minimum value.
	 * @param vmax the maximum value.
	 */
	public void setVLimits(double vmin, double vmax) {
		setVLimits(0,vmin,vmax);
	}

	  /**
	   * Sets limits for the horizontal axis in the specified column.
	   * By default, limits are computed automatically by tiled graphical views.
	   * This method can be used to override those default limits.
	   * @param icol the column index.
	   * @param hmin the minimum value.
	   * @param hmax the maximum value.
	   */
	  public void setHLimits(int icol, double hmin, double hmax) {
	    Check.argument(hmin<hmax,"hmin<hmax");
	    int nrow = getMosaic().countRows();
	    for (int irow=0; irow<nrow; ++irow)
	      getTile(irow,icol).setHLimits(hmin,hmax);
	  }

	  /**
	   * Sets limits for the vertical axis in the specified row.
	   * By default, limits are computed automatically by tiled graphical views.
	   * This method can be used to override those default limits.
	   * @param irow the row index.
	   * @param vmin the minimum value.
	   * @param vmax the maximum value.
	   */
	  public void setVLimits(int irow, double vmin, double vmax) {
	    Check.argument(vmin<vmax,"vmin<vmax");
	    int ncol = getMosaic().countColumns();
	    for (int icol=0; icol<ncol; ++icol)
	      getTile(irow,icol).setVLimits(vmin,vmax);
	  }

	/**
	 * Sets default limits for horizontal and vertical axes. This method may
	 * be used to restore default limits after they have been set explicitly.
	 */
	public void setLimitsDefault() {
		setHLimitsDefault();
		setVLimitsDefault();
	}

	/**
	 * Sets default limits for the horizontal axis. This method may be used 
	 * to restore default limits after they have been set explicitly.
	 */
	public void setHLimitsDefault() {
		setHLimitsDefault(0);
	}

	/**
	 * Sets default limits for the vertical axis. This method may be used 
	 * to restore default limits after they have been set explicitly.
	 */
	public void setVLimitsDefault() {
		setVLimitsDefault(0);
	}

	  /**
	   * Sets default limits for the horizontal axis in the specified column.
	   * This method may be used to restore default limits after they have 
	   * been set explicitly.
	   * @param icol the column index.
	   */
	  public void setHLimitsDefault(int icol) {
	    int nrow = getMosaic().countRows();
	    for (int irow=0; irow<nrow; ++irow)
	      getTile(irow,icol).setHLimitsDefault();
	  }

	  /**
	   * Sets default limits for the vertical axis in the specified column.
	   * This method may be used to restore default limits after they have 
	   * been set explicitly.
	   * @param irow the row index.
	   */
	  public void setVLimitsDefault(int irow) {
	    int ncol = getMosaic().countColumns();
	    for (int icol=0; icol<ncol; ++icol)
	      getTile(irow,icol).setVLimitsDefault();
	  }

	/**
	 * Sets the label for the horizontal axis.
	 * @param label the label.
	 */
	public void setHLabel(String label) {
		setHLabel(0,label);
	}

	/**
	 * Sets the label for the vertical axis.
	 * @param label the label.
	 */
	public void setVLabel(String label) {
		setVLabel(0,label);
	}

	/**
	 * Sets the label for the horizontal axis in the specified column.
	 * @param icol the column index.
	 * @param label the label.
	 */
	public void setHLabel(int icol, String label) {
		_hLabel = label;
		if (_axesPlacement==AxesPlacement.LEFT_TOP) {
			_mosaic.getTileAxisTop(icol).setLabel(label);
			adjustColorBar();
		} else if (_axesPlacement==AxesPlacement.LEFT_BOTTOM) {
			_mosaic.getTileAxisBottom(icol).setLabel(label);
			adjustColorBar();
		} else if (_axesPlacement==AxesPlacement.LEFT_BOTTOM_RIGHT) {
			_mosaic.getTileAxisBottom(icol).setLabel(label);
			adjustColorBar();
		}
	}
	public String getHLabel() { return _hLabel; }

	public void setHLabel2(int icol, String label) {
		_hLabel = label;
		if (_axesPlacement==AxesPlacement.LEFT_BOTTOM_RIGHT_TOP) {
			_mosaic.getTileAxisTop(icol).setLabel(label);
			adjustColorBar();
		}
	}
	
	/**
	 * Sets the label for the vertical axis in the specified row.
	 * @param irow the row index.
	 * @param label the label.
	 */
//	public void setVLabel(int irow, String label) {
//		_vLabel = label;
//		if (_axesPlacement!=AxesPlacement.NONE) {
//			_mosaic.getTileAxisLeft(irow).setLabel(label);
//		}
//	}
	public void setVLabel(int irow, String label) {
		_vLabel = label;
		if (_axesPlacement==AxesPlacement.LEFT_TOP) {
			_mosaic.getTileAxisLeft(irow).setLabel(label);
		} else if (_axesPlacement==AxesPlacement.LEFT_BOTTOM) {
			_mosaic.getTileAxisLeft(irow).setLabel(label);
		} else if (_axesPlacement==AxesPlacement.LEFT_BOTTOM_RIGHT) {
			_mosaic.getTileAxisLeft(irow).setLabel(label);
		}
	}
	public void setVLabel2(int irow, String label) {
		_vLabel = label;
		if (_axesPlacement==AxesPlacement.LEFT_BOTTOM_RIGHT) {
			_mosaic.getTileAxisRight(irow).setLabel(label);
		}
	}

	public String getVLabel() { return _vLabel; }
	
	/**
	 * Sets the format for the horizontal axis.
	 * @param format the format.
	 */
	public void setHFormat(String format) {
		setHFormat(0,format);
	}

	/**
	 * Sets the format for the vertical axis.
	 * @param format the format.
	 */
	public void setVFormat(String format) {
		setVFormat(0,format);
	}

	/**
	 * Sets the format for the horizontal axis in the specified column.
	 * @param icol the column index.
	 * @param format the format.
	 */
	public void setHFormat(int icol, String format) {
		if (_axesPlacement==AxesPlacement.LEFT_TOP) {
			_mosaic.getTileAxisTop(icol).setFormat(format);
		} else if (_axesPlacement==AxesPlacement.LEFT_BOTTOM) {
			_mosaic.getTileAxisBottom(icol).setFormat(format);
		}
	}

	/**
	 * Sets the format for the vertical axis in the specified row.
	 * @param irow the row index.
	 * @param format the format.
	 */
	public void setVFormat(int irow, String format) {
		if (_axesPlacement!=AxesPlacement.NONE) {
			_mosaic.getTileAxisLeft(irow).setFormat(format);
		}
	}

	/**
	 * Sets tic label rotation for the vertical axis in the specifie row.
	 * If true, tic labels in the vertical axis are rotated 90 degrees 
	 * counter-clockwise. The default is false, not rotated.
	 * @param irow the row index.
	 * @param rotated true, if rotated; false, otherwise.
	 */
	public void setVRotated(int irow, boolean rotated) {
		if (_axesPlacement!=AxesPlacement.NONE) {
			_mosaic.getTileAxisLeft(irow).setVerticalAxisRotated(rotated);
		}
	}

	/**
	 * Sets the tic interval for the horizontal axis.
	 * @param interval the major labeled tic interval.
	 */
	public void setHInterval(double interval) {
		setHInterval(0,interval);
	}

	/**
	 * Sets the tic interval for the vertical axis.
	 * @param interval the major labeled tic interval.
	 */
	public void setVInterval(double interval) {
		setVInterval(0,interval);
	}

	/**
	 * Sets the tic interval for the horizontal axis in the specified column.
	 * @param icol the column index.
	 * @param interval the major labeled tic interval.
	 */
	public void setHInterval(int icol, double interval) {
		if (_axesPlacement==AxesPlacement.LEFT_TOP) {
			_mosaic.getTileAxisTop(icol).setInterval(interval);
		} else if (_axesPlacement==AxesPlacement.LEFT_BOTTOM) {
			_mosaic.getTileAxisBottom(icol).setInterval(interval);
		}
	}

	/**
	 * Sets the tic interval for the vertical axis in the specified column.
	 * @param irow the row index.
	 * @param interval the major labeled tic interval.
	 */
	public void setVInterval(int irow, double interval) {
		if (_axesPlacement!=AxesPlacement.NONE) {
			_mosaic.getTileAxisLeft(irow).setInterval(interval);
		}
	}

	/**
	 * Adds a grid view.
	 * @return the grid view.
	 */
	public GridView addGrid() {
		return addGrid(0,0);
	}

	/**
	 * Adds a grid view with specified parameters string. 
	 * For the format of the parameters string, see 
	 * {@link edu.mines.jtk.mosaic.GridView#setParameters(String)}.
	 * @param parameters the parameters string.
	 * @return the grid view.
	 */
	public GridView addGrid(String parameters) {
		return addGrid(0,0,parameters);
	}

	/**
	 * Adds a grid view.
	 * @param irow the tile row index.
	 * @param icol the tile column index.
	 * @return the grid view.
	 */
	public GridView addGrid(int irow, int icol) {
		GridView gv = new GridView();
		return addGridView(irow,icol,gv);
	}

	/**
	 * Adds a grid view with specified parameters string.
	 * For the format of the parameters string, see 
	 * {@link edu.mines.jtk.mosaic.GridView#setParameters(String)}.
	 * @param irow the tile row index.
	 * @param icol the tile column index.
	 * @param parameters the parameters string.
	 * @return the grid view.
	 */
	public GridView addGrid(int irow, int icol, String parameters) {
		GridView gv = new GridView(parameters);
		return addGridView(irow,icol,gv);
	}
	/**
	   * Adds a bars view of the array x2.
	   * @param x2 array of x2 values.
	   * @return the bars view.
	   */
	  public BarsView addBars(float[] x2) {
	    return addBars(0,0,x2);
	  }

	  /**
	   * Adds a view of bars (x1,x2) for a sampled function x2(x1).
	   * @param s1 the sampling of the variable x1; must be uniform.
	   * @param x2 array of x2 coordinates.
	   * @return the bars view.
	   */
	  public BarsView addBars(Sampling s1, float[] x2) {
	    return addBars(0,0,s1,x2);
	  }

	  /**
	   * Adds a view of bars of the array x2 containing x2.length plot segments.
	   * @param x2 array of array of x2 values.
	   * @return the bars view.
	   */
	  public BarsView addBars(float[][] x2) {
	    return addBars(0,0,x2);
	  }

	  /**
	   * Adds a view of bars (x1,x2) for a sampled function x2(x1) and x2.length
	   * plot segments.
	   * @param s1 the sampling of the variable x1; must be uniform.
	   * @param x2 array of array of x2 values.
	   */
	  public BarsView addBars(Sampling s1, float[][] x2) {
	    return addBars(0,0,s1,x2);
	  }

	  /**
	   * Adds a view of bars x2 for a sampled function x2(x1).
	   * @param irow the tile row index.
	   * @param icol the tile column index.
	   * @param s1 the sampling of the variable x1; must be uniform.
	   * @param x2 array of x2 values.
	   * @return the bars view.
	   */
	  public BarsView addBars(int irow, int icol, Sampling s1, float[] x2) {
	    BarsView bv = new BarsView(s1,x2);
	    return addBarsView(irow,icol,bv);
	  }

	  /**
	   * Adds a bars view of the array x2 of bar values.
	   * @param irow the tile row index.
	   * @param icol the tile column index.
	   * @param x2 array of x2 values.
	   * @return the bars view.
	   */
	  public BarsView addBars(int irow, int icol, float[] x2) {
	    BarsView bv = new BarsView(x2);
	    return addBarsView(irow,icol,bv);
	  }

	  /**
	   * Adds a bars view of the array of arrays x2 of bar values and x2.length
	   * plot segments.
	   * @param irow the tile row index.
	   * @param icol the tile column index.
	   * @param x2 array of arrays of x2 values.
	   * @return the bars view.
	   */
	  public BarsView addBars(int irow, int icol, float[][] x2) {
	    BarsView bv = new BarsView(x2);
	    return addBarsView(irow,icol,bv);
	  }

	  /**
	   * Adds a bars view of the array of arrays x2 of bar values and x2.length
	   * plot segments for a sample function x2(x1).
	   * @param irow the tile row index.
	   * @param icol the tile column index.
	   * @param s1 the sampling of the variable x1; must be uniform.
	   * @param x2 array of array of x2 values.
	   * @return the bars view.
	   */
	  public BarsView addBars(int irow, int icol, Sampling s1, float[][] x2) {
	    BarsView bv = new BarsView(s1,x2);
	    return addBarsView(irow,icol,bv);
	  }

	/**
	 * Adds a pixels view of the specified sampled function f(x1,x2).
	 * Assumes zero first sample values and unit sampling intervals.
	 * @param f array[n2][n1] of sampled function values f(x1,x2), where 
	 *  n1 = f[0].length and n2 = f.length.
	 * @return the pixels view.
	 */
	public PixelsView addPixels(float[][] f) {
		return addPixels(0,0,f);
	}

	/**
	 * Adds a pixels view of the specified sampled function f(x1,x2).
	 * @param s1 the sampling of the variable x1; must be uniform.
	 * @param s2 the sampling of the variable x2; must be uniform.
	 * @param f array[n2][n1] of sampled function values f(x1,x2), where 
	 *  n1 = f[0].length and n2 = f.length.
	 * @return the pixels view.
	 */
	public PixelsView addPixels(Sampling s1, Sampling s2, float[][] f) {
		return addPixels(0,0,s1,s2,f);
	}

	/**
	 * Adds a pixels view of the specified sampled function f(x1,x2).
	 * Assumes zero first sample values and unit sampling intervals.
	 * @param irow the tile row index.
	 * @param icol the tile column index.
	 * @param f array[n2][n1] of sampled function values f(x1,x2), 
	 *  where n1 = f[0].length and n2 = f.length.
	 * @return the pixels view.
	 */
	public PixelsView addPixels(int irow, int icol, float[][] f) {
		PixelsView pv = new PixelsView(f);
		return addPixelsView(irow,icol,pv);
	}

	/**
	 * Adds a pixels view of the specified sampled function f(x1,x2).
	 * @param irow the tile row index.
	 * @param icol the tile column index.
	 * @param s1 the sampling of the variable x1; must be uniform.
	 * @param s2 the sampling of the variable x2; must be uniform.
	 * @param f array[n2][n1] of sampled function values f(x1,x2), 
	 *  where n1 = f[0].length and n2 = f.length.
	 * @return the pixels view.
	 */
	public PixelsView addPixels(
			int irow, int icol, Sampling s1, Sampling s2, float[][] f) {
		PixelsView pv = new PixelsView(s1,s2,f);
		return addPixelsView(irow,icol,pv);
	}

	/**
	 * Adds a pixels view of the specified sampled function f(x1,x2).
	 * Assumes zero first sample values and unit sampling intervals.
	 * @param f array[n2][n1] of sampled function values f(x1,x2), where 
	 *  n1 = f[0][0].length, n2 = f[0].length, and nc is the number 
	 *  of components.
	 * @return the pixels view.
	 */
	public PixelsView addPixels(float[][][] f) {
		return addPixels(0,0,f);
	}

	/**
	 * Adds a pixels view of the specified sampled function f(x1,x2).
	 * @param s1 the sampling of the variable x1; must be uniform.
	 * @param s2 the sampling of the variable x2; must be uniform.
	 * @param f array[n2][n1] of sampled function values f(x1,x2), where 
	 *  n1 = f[0][0].length, n2 = f[0].length, and nc is the number 
	 *  of components.
	 * @return the pixels view.
	 */
	public PixelsView addPixels(Sampling s1, Sampling s2, float[][][] f) {
		return addPixels(0,0,s1,s2,f);
	}

	/**
	 * Adds a pixels view of the specified sampled function f(x1,x2).
	 * Assumes zero first sample values and unit sampling intervals.
	 * @param irow the tile row index.
	 * @param icol the tile column index.
	 * @param f array[n2][n1] of sampled function values f(x1,x2), 
	 *  n1 = f[0][0].length, n2 = f[0].length, and nc is the number 
	 *  of components.
	 * @return the pixels view.
	 */
	public PixelsView addPixels(int irow, int icol, float[][][] f) {
		PixelsView pv = new PixelsView(f);
		return addPixelsView(irow,icol,pv);
	}

	/**
	 * Adds a pixels view of the specified sampled function f(x1,x2).
	 * @param irow the tile row index.
	 * @param icol the tile column index.
	 * @param s1 the sampling of the variable x1; must be uniform.
	 * @param s2 the sampling of the variable x2; must be uniform.
	 * @param f array[n2][n1] of sampled function values f(x1,x2), 
	 *  n1 = f[0][0].length, n2 = f[0].length, and nc is the number 
	 *  of components.
	 * @return the pixels view.
	 */
	public PixelsView addPixels(
			int irow, int icol, Sampling s1, Sampling s2, float[][][] f) {
		PixelsView pv = new PixelsView(s1,s2,f);
		return addPixelsView(irow,icol,pv);
	}

	/**
	 * Adds a points view of the arrays x1 and x2 of point (x1,x2) coordinates.
	 * @param x1 array of x1 coordinates.
	 * @param x2 array of x2 coordinates.
	 * @return the points view.
	 */
	public PointsView addPoints(float[] x1, float[] x2) {
		return addPoints(0,0,x1,x2);
	}

	/**
	 * Adds a points view of arrays x1, x2 and x3 of point (x1,x2,x3) coordinates.
	 * @param x1 array of x1 coordinates.
	 * @param x2 array of x2 coordinates.
	 * @param x3 array of x3 coordinates.
	 * @return the points view.
	 */
	public PointsView addPoints(float[] x1, float[] x2, float[] x3) {
		return addPoints(0,0,x1,x2,x3);
	}

	/**
	 * Adds a points view of (x1,x2) with specified x2 coordinates.
	 * The corresponding coordinates x1 are assumed to be 0, 1, 2, ....
	 * @param x2 array of x2 coordinates.
	 * @return the points view.
	 */
	public PointsView addPoints(float[] x2) {
		return addPoints(0,0,x2);
	}

	/**
	 * Adds a view of points (x1,x2) for a sampled function x2(x1).
	 * @param s1 the sampling of x1 coordinates.
	 * @param x2 array of x2 coordinates.
	 * @return the points view.
	 */
	public PointsView addPoints(Sampling s1, float[] x2) {
		return addPoints(0,0,s1,x2);
	}

	/**
	 * Adds a view of arrays of (x1,x2) coordinates for multiple plot segments.
	 * The lengths of the specified arrays x1 and x2 must be equal.
	 * @param x1 array of arrays of x1 coordinates.
	 * @param x2 array of arrays of x2 coordinates.
	 */
	public PointsView addPoints(float[][] x1, float[][] x2) {
		return addPoints(0,0,x1,x2);
	}

	/**
	 * Adds a points view of the arrays x1 and x2 of point (x1,x2) coordinates.
	 * @param irow the tile row index.
	 * @param icol the tile column index.
	 * @param x1 array of x1 coordinates.
	 * @param x2 array of x2 coordinates.
	 * @return the points view.
	 */
	public PointsView addPoints(int irow, int icol, float[] x1, float[] x2) {
		PointsView pv = new PointsView(x1,x2);
		return addPointsView(irow,icol,pv);
	}

	/**
	 * Adds a points view of arrays x1, x2 and x3 of point (x1,x2,x3) coordinates.
	 * @param irow the tile row index.
	 * @param icol the tile column index.
	 * @param x1 array of x1 coordinates.
	 * @param x2 array of x2 coordinates.
	 * @param x3 array of x3 coordinates.
	 * @return the points view.
	 */
	public PointsView addPoints(
			int irow, int icol, float[] x1, float[] x2, float[] x3) 
	{
		PointsView pv = new PointsView(x1,x2,x3);
		return addPointsView(irow,icol,pv);
	}

	/**
	 * Adds a points view of (x1,x2) with specified x2 coordinates.
	 * The corresponding coordinates x1 are assumed to be 0, 1, 2, ....
	 * @param irow the tile row index.
	 * @param icol the tile column index.
	 * @param x2 array of x2 coordinates.
	 * @return the points view.
	 */
	public PointsView addPoints(int irow, int icol, float[] x2) {
		PointsView pv = new PointsView(x2);
		return addPointsView(irow,icol,pv);
	}

	/**
	 * Adds a view of points (x1,x2) for a sampled function x2(x1).
	 * @param irow the tile row index.
	 * @param icol the tile column index.
	 * @param s1 the sampling of x1 coordinates.
	 * @param x2 array of x2 coordinates.
	 * @return the points view.
	 */
	public PointsView addPoints(int irow, int icol, Sampling s1, float[] x2) {
		PointsView pv = new PointsView(s1,x2);
		return addPointsView(irow,icol,pv);
	}

	/**
	 * Adds a view of arrays of (x1,x2) coordinates for multiple plot segments.
	 * The lengths of the specified arrays x1 and x2 must be equal.
	 * @param irow the tile row index.
	 * @param icol the tile column index.
	 * @param x1 array of arrays of x1 coordinates.
	 * @param x2 array of arrays of x2 coordinates.
	 */
	public PointsView addPoints(int irow, int icol, float[][] x1, float[][] x2) {
		PointsView pv = new PointsView(x1,x2);
		return addPointsView(irow,icol,pv);
	}

	/**
	 * Adds a contours view with the function f(x1,x2).
	 * Function f(x1,x2) assumed to have uniform sampling.
	 * @param f array[n2][n1] of sampled function values f(x1,x2), where
	 * n1 = f[0].length and n2 = f.length.
	 */
	public ContoursView addContours(float[][] f) {
		return addContours(0,0,f);
	}

	/**
	 * Adds a contours view of the specified sampled function f(x1,x2).
	 * @param s1 the sampling of the variable x1; must be uniform.
	 * @param s2 the sampling of the variable x2; must be uniform.
	 * @param f array[n2][n1] of sampled function values f(x1,x2), where 
	 *  n1 = f[0].length and n2 = f.length.
	 * @return the contours view.
	 */
	public ContoursView addContours(Sampling s1, Sampling s2, float[][] f) {
		return addContours(0,0,s1,s2,f);
	}

	/** 
	 * Adds a contours view with the function f(x1,x2).
	 * Function f(x1,x2) assumed to have uniform sampling.
	 * @param irow the tile row index.
	 * @param icol the tile column index.
	 * @param f array[n2][n1] of sampline function values f(x1,x2), where
	 * n1 = f[0].length and n2 = f.length.
	 */
	public ContoursView addContours(int irow, int icol, float[][] f) {
		ContoursView cv = new ContoursView(f);
		return addContoursView(irow,icol,cv);
	}

	/**
	 * Adds a contours view of the specified sampled function f(x1,x2).
	 * @param irow the tile row index.
	 * @param icol the tile column index.
	 * @param s1 the sampling of the variable x1; must be uniform.
	 * @param s2 the sampling of the variable x2; must be uniform.
	 * @param f array[n2][n1] of sampled function values f(x1,x2), 
	 *  where n1 = f[0].length and n2 = f.length.
	 * @return the contours view.
	 */
	public ContoursView addContours(
			int irow, int icol, Sampling s1, Sampling s2, float[][] f) {
		ContoursView cv = new ContoursView(s1,s2,f);
		return addContoursView(irow,icol,cv);
	}

	/**
	 * Adds a sequence view with specified values f(x).
	 * Uses default sampling of x = 0, 1, 2, ....
	 * @param f array of sampled function values f(x).
	 * @return the sequence view.
	 */
	public SequenceView addSequence(float[] f) {
		return addSequence(0,0,f);
	}

	/**
	 * Adds a sequence view with specified sampling and values f(x).
	 * @param sx the sampling of the variable x.
	 * @param f array of sampled function values f(x).
	 * @return the sequence view.
	 */
	public SequenceView addSequence(Sampling sx, float[] f) {
		return addSequence(0,0,sx,f);
	}

	/**
	 * Adds a sequence view with specified values f(x).
	 * Uses default sampling of x = 0, 1, 2, ....
	 * @param irow the tile row index.
	 * @param icol the tile column index.
	 * @param f array of sampled function values f(x).
	 * @return the sequence view.
	 */
	public SequenceView addSequence(int irow, int icol, float[] f) {
		SequenceView sv = new SequenceView(f);
		return addSequenceView(irow,icol,sv);
	}

	/**
	 * Adds a sequence view with specified sampling and values f(x).
	 * @param irow the tile row index.
	 * @param icol the tile column index.
	 * @param sx the sampling of the variable x.
	 * @param f array of sampled function values f(x).
	 * @return the sequence view.
	 */
	public SequenceView addSequence(int irow, int icol, Sampling sx, float[] f) {
		SequenceView sv = new SequenceView(sx,f);
		return addSequenceView(irow,icol,sv);
	}

	/**
	 * Adds the specified tiled view to this plot panel. If the tiled view 
	 * is already in this panel, it is first removed, before adding it again.
	 * @param tv the tiled view.
	 * @return true, if this panel did not already contain the specified
	 *  tiled view; false, otherwise.
	 */
	public boolean addTiledView(TiledView tv) {
		return addTiledView(0,0,tv);
	}

	/**
	 * Adds the specified tiled view to this plot panel. If the tiled view 
	 * is already in the specified tile, it is first removed, before adding 
	 * it again.
	 * @param irow the tile row index.
	 * @param icol the tile column index.
	 * @param tv the tiled view.
	 * @return true, if the tile did not already contain the specified
	 *  tiled view; false, otherwise.
	 */
	public boolean addTiledView(int irow, int icol, TiledView tv) {
		if (tv instanceof ColorMapped) {
			ColorMapped cm = (ColorMapped)tv;
			if(cm.getColorMap()!=null)  cm.getColorMap().addListener(_colorMapHandler); 
		}
		return getTile(irow,icol).addTiledView(tv);
	}

	/**
	 * Removes the specified tiled view from this plot panel.
	 * @param tv the tiled view.
	 * @return true, if this panel contained the specified tiled view; 
	 *  false, otherwise.
	 */
	public boolean remove(TiledView tv) {
		if (tv instanceof ColorMapped) {
			ColorMapped cm = (ColorMapped)tv;
			cm.getColorMap().removeListener(_colorMapHandler);
		}
		int nrow = _mosaic.countRows();
		int ncol = _mosaic.countColumns();
		for (int irow=0; irow<nrow; ++irow) {
			for (int icol=0; icol<ncol; ++icol) {
				if (getTile(irow,icol).removeTiledView(tv)) 
					return true;
			}
		}
		return false;
	}
	public void removeViews(int irow, int icol, String category) { removeViews(irow, icol, category, null); }
	public void removeViews(int irow, int icol, String category, String name) {
		ArrayList<TiledView> list = null;
		Tile tile = getTile(irow,icol);
		//System.out.println("n="+tile.countTiledViews());
		for(int k=0; k<tile.countTiledViews(); k++) {
			TiledView tv = tile.getTiledView(k);
			boolean removeIt = false;
			if(category==null) removeIt = true;
			else {
				if (tv.getCategory().equalsIgnoreCase(category)) {
					if(name==null) removeIt = true;
					else {
						if (tv.getName().equalsIgnoreCase(name)) { removeIt = true; }
						else removeIt = false;
					} 
				} else removeIt = false;
			}

			if(removeIt) {
				if(list==null) list = new ArrayList<TiledView>();
				list.add(tv);
			}
		}

		if(list==null || list.size()==0) return;		
		for(int i=0; i<list.size(); i++) { tile.removeTiledView(list.get(i)); }
	}
	public void removeAllViews() { 
		int nrow = _mosaic.countRows();
		int ncol = _mosaic.countColumns();
		for (int irow=0; irow<nrow; ++irow) {
			for (int icol=0; icol<ncol; ++icol) {
				removeAllViews(irow, icol);
			}
		}
	}
	public void removeAllViews(int irow, int icol) { removeViews(irow, icol, null); }
	public void removeViews(String category) { removeView(category, null); }
	public void removeView(String category, String name) {
		int nrow = _mosaic.countRows();
		int ncol = _mosaic.countColumns();
		for (int irow=0; irow<nrow; ++irow) {
			for (int icol=0; icol<ncol; ++icol) {
				removeViews(irow, icol, category, name);
			}
		}
	}
	public TiledView getViews(int iRow, int iCol, String category) { return getViews(iRow, iCol, category, null); }
	public TiledView getViews(int iRow, int iCol, String category, String name) {
		int nrow = _mosaic.countRows();
		int ncol = _mosaic.countColumns();
		for (int irow=0; irow<nrow; ++irow) {
			if(iRow!=irow) continue;
			for (int icol=0; icol<ncol; ++icol) {
				if(iCol!=icol) continue;
				Tile tile = getTile(irow,icol);
				for(int k=0; k<tile.countTiledViews(); k++) {
					TiledView tv = tile.getTiledView(k);
					if (tv.getCategory().equalsIgnoreCase(category)) {
						if(name==null) {
							return tv;
						} else {
							if(tv.getName().equalsIgnoreCase(name.trim()))  return tv;
						}
					}
				}
			}
		}
		return null;
	}

	public TiledView getView(String category) { return getView(category, null); }
	public TiledView getView(String category, String name) {
		int nrow = _mosaic.countRows();
		int ncol = _mosaic.countColumns();
		for (int irow=0; irow<nrow; ++irow) {
			for (int icol=0; icol<ncol; ++icol) {
				TiledView tv = getViews(irow, icol, category, name);
				if(tv!=null) return tv;
			}
		}
		return null;
	}

	public boolean getVisible(String category, String name) {
		int nrow = _mosaic.countRows();
		int ncol = _mosaic.countColumns();
		for (int irow=0; irow<nrow; ++irow) {
			for (int icol=0; icol<ncol; ++icol) {
				Tile tile = getTile(irow,icol);
				for(int k=0; k<tile.countTiledViews(); k++) {
					TiledView tv = tile.getTiledView(k);
					if (tv.getCategory().equalsIgnoreCase(category)) {
						if (tv.getName().equalsIgnoreCase(name)) {
							return tv.isVisible();
						}
					}
				}
			}
		}
		return true;
	}

	public void setVisible(String category, String name, boolean visible) {
		int nrow = _mosaic.countRows();
		int ncol = _mosaic.countColumns();
		for (int irow=0; irow<nrow; ++irow) {
			for (int icol=0; icol<ncol; ++icol) {
				Tile tile = getTile(irow,icol);
				for(int k=0; k<tile.countTiledViews(); k++) {
					TiledView tv = tile.getTiledView(k);
					if (tv.getCategory().equalsIgnoreCase(category)) {
						if (tv.getName().equalsIgnoreCase(name)) {
							tv.setVisible(visible);
						}
					}
				}
			}
		}
	}
	
	
	public void removeDuplicateWithOrder(ArrayList<String> arlList) {    
		Set<String> set 			= new HashSet<String>();    
		ArrayList<String> newList 	= new ArrayList<String>();    
		for (String element : arlList) {      
			if (set.add(element))  {
				newList.add(element);  
			}
		}    
		arlList.clear();    
		arlList.addAll(newList); 
	}

	public String [] getWorldUniqueCategory() {
		String [] category = getWorldCategory();
		ArrayList<String> pvector = new ArrayList<String>();
		for(int i=0; i<category.length; i++) {
			pvector.add(category[i]);
		}
		removeDuplicateWithOrder(pvector);

		String [] newString = new String[pvector.size()];
		for(int i=0; i<pvector.size(); i++) {
			newString[i] = pvector.get(i).trim();
		}
		return newString;
	}
	public String [] getWorldCategory() {
		int nrow = getMosaic().countRows();
		int ncol = getMosaic().countColumns();
		ArrayList<String> 	list = new ArrayList<String>();
		for (int irow=0; irow<nrow; ++irow) {
			for (int icol=0; icol<ncol; ++icol) {
				Tile tile = getTile(irow,icol);
				for(int k=0; k<tile.countTiledViews(); k++) {
					TiledView tv = tile.getTiledView(k);
					list.add(tv.getCategory());
				}
			}
		}
		if(list.size()==0) return null;
		String [] name = new String[list.size()];
		for(int i=0; i<list.size(); i++) {
			name[i] = list.get(i);
		}
		return name;
	}
	
	public String [] getEntityName(String category) {
		int nrow = getMosaic().countRows();
		int ncol = getMosaic().countColumns();
		ArrayList<String> 	list = new ArrayList<String>();
		for (int irow=0; irow<nrow; ++irow) {
			for (int icol=0; icol<ncol; ++icol) {
				Tile tile = getTile(irow,icol);
				for(int k=0; k<tile.countTiledViews(); k++) {
					TiledView tv = tile.getTiledView(k);
					if (tv.getCategory().equalsIgnoreCase(category)) {
						 list.add(tv.getName());
					}
				}
			}
		}
		if(list.size()==0) return null;
		String [] name = new String[list.size()];
		for(int i=0; i<list.size(); i++) {
			name[i] = list.get(i);
		}
		return name;
	}
	public String [] getEntityName(int irow, int icol, String category) {
		ArrayList<String> 	list = new ArrayList<String>();
		Tile tile = getTile(irow,icol);
		for(int k=0; k<tile.countTiledViews(); k++) {
			TiledView tv = tile.getTiledView(k);
			if (tv.getCategory().equalsIgnoreCase(category)) {
				list.add(tv.getName());
			}
		}
		if(list.size()==0) return null;
		String [] name = new String[list.size()];
		for(int i=0; i<list.size(); i++) {
			name[i] = list.get(i);
		}
		return name;
	}
	public Boolean [] getEntityVisibility(String category) {
		int nrow = getMosaic().countRows();
		int ncol = getMosaic().countColumns();
		ArrayList<Boolean> 	list = new ArrayList<Boolean>();
		for (int irow=0; irow<nrow; ++irow) {
			for (int icol=0; icol<ncol; ++icol) {
				Tile tile = getTile(irow,icol);
				for(int k=0; k<tile.countTiledViews(); k++) {
					TiledView tv = tile.getTiledView(k);
					if (tv.getCategory().equalsIgnoreCase(category)) {
						list.add(new Boolean(tv.isVisible()));
					}
				}
			}
		}
		
		Boolean [] visibility = new Boolean[list.size()];
		for(int i=0; i<list.size(); i++) {
			visibility[i] = list.get(i);
		}

		return visibility;
	}
	public Boolean [] getEntityVisibility(int irow, int icol, String category) {
		ArrayList<Boolean> 	list = new ArrayList<Boolean>();
		Tile tile = getTile(irow,icol);
		for(int k=0; k<tile.countTiledViews(); k++) {
			TiledView tv = tile.getTiledView(k);
			if (tv.getCategory().equalsIgnoreCase(category)) {
				list.add(new Boolean(tv.isVisible()));
			}
		}

		Boolean [] visibility = new Boolean[list.size()];
		for(int i=0; i<list.size(); i++) {
			visibility[i] = list.get(i);
		}

		return visibility;
	}
	public void setVisibility(String category, boolean [] visible) {
		setVisibility(category, null, visible);
	}
	public void setVisibility(int irow, int icol, String category, String name, boolean [] visible) {
		boolean needRefresh = false;
		Tile tile = getTile(irow,icol);
		for(int k=0, q=0; k<tile.countTiledViews(); k++) {
			TiledView tv = tile.getTiledView(k);
			if (tv.getCategory().equalsIgnoreCase(category)) {
				if(name!=null) {
					if (tv.getName().equalsIgnoreCase(name)) {
						tv.setVisible(visible[q++]); 
					}
				} else { tv.setVisible(visible[q++]); }
				needRefresh = true;
			}
		}
		if(needRefresh) repaint();
	}
	public void setVisibility(String category, String name, boolean [] visible) {
		int nrow = getMosaic().countRows();
		int ncol = getMosaic().countColumns();
		boolean needRefresh = false;
		for (int irow=0, q=0; irow<nrow; ++irow) {
			for (int icol=0; icol<ncol; ++icol) {
				Tile tile = getTile(irow,icol);
				for(int k=0; k<tile.countTiledViews(); k++) {
					TiledView tv = tile.getTiledView(k);
					if (tv.getCategory().equalsIgnoreCase(category)) {
						if(name!=null) {
							if (tv.getName().equalsIgnoreCase(name)) {
								tv.setVisible(visible[q++]); 
							}
						} else { tv.setVisible(visible[q++]); }
						needRefresh = true;
					}
				}
			}
		}
		if(needRefresh) repaint();
	}
	
	
	
	

	/**
	 * Sets the font in all components of this panel.
	 * Sets the title font to be 1.5 times larger than the specified font.
	 * @param font the font.
	 */
	public void setFont(Font font) {
		super.setFont(font);
		if (_mosaic!=null)
			_mosaic.setFont(font);
		if (_colorBar!=null)
			_colorBar.setFont(font);
		if (_title!=null)
			_title.setFont(font.deriveFont(1.5f*font.getSize2D()));
		adjustColorBar();
		revalidate();
	}

	/**
	 * Sets the foreground color in all components of this panel.
	 * @param color the foreground color.
	 */
	public void setForeground(Color color) {
		super.setForeground(color);
		if (_mosaic!=null)
			_mosaic.setForeground(color);
		if (_colorBar!=null)
			_colorBar.setForeground(color);
		if (_title!=null)
			_title.setForeground(color);
	}

	/**
	 * Sets the background color in all components of this panel.
	 * @param color the background color.
	 */
	public void setBackground(Color color) {
		super.setBackground(color);
		if (_mosaic!=null)
			_mosaic.setBackground(color);
		if (_colorBar!=null)
			_colorBar.setBackground(color);
		if (_title!=null)
			_title.setBackground(color);
	}

	///////////////////////////////////////////////////////////////////////////
	// private

	private Mosaic _mosaic;
	private ColorBar _colorBar;
	private String _colorBarFormat;
	private int _colorBarWidthMinimum = 0;
	private Title _title;
	protected Orientation _orientation;
	protected AxesPlacement _axesPlacement;
	protected ColorMapped _colorMapped;
	private boolean _autoColorMapped = true;
	private ColorMapHandler _colorMapHandler;

	/**
	 * Internal class for plot title.
	 */
	private class Title extends IPanel {
		private static final long serialVersionUID = 1L;
		String text;

		Title(String text) {
			this.text = text;
		}

		void set(String text) {
			this.text = text;
			repaint();
		}

		public void paintToRect(Graphics2D g2d, int x, int y, int w, int h) {
			g2d = createGraphics(g2d,x,y,w,h);
			g2d.setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			Font font = g2d.getFont();
			FontMetrics fm = g2d.getFontMetrics();
			FontRenderContext frc = g2d.getFontRenderContext();
			LineMetrics lm = font.getLineMetrics(text,frc);
			//int fh = round(lm.getHeight());
			//int fa = round(lm.getAscent());
			int fd = round(lm.getDescent());
			int wt = fm.stringWidth(text);
			int xt = max(0,min(w-wt,(w-wt)/2));
			int yt = h-1-2*fd;
			g2d.drawString(text,xt,yt);
			g2d.dispose();
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			paintToRect((Graphics2D)g,0,0,getWidth(),getHeight());
		}

		public Dimension getMinimumSize() {
			if (isMinimumSizeSet()) {
				return super.getMinimumSize();
			} else {
				Font font = getFont();
				FontMetrics fm = getFontMetrics(font);
				int fh = fm.getHeight();
				int fd = fm.getDescent();
				int wt = fm.stringWidth(text);
				return new Dimension(wt,fd+fh);
			}
		}
		public Dimension getPreferredSize() {
			if (isPreferredSizeSet()) {
				return super.getPreferredSize();
			} else {
				return getMinimumSize();
			}
		}
	}

	public GridView addGridView(int irow, int icol, GridView gv) {
		addTiledView(irow, icol, gv);
		return gv;
	}

	public BarsView addBarsView(int irow, int icol, BarsView bv) {
		if (_orientation==Orientation.X1RIGHT_X2UP) {
			bv.setOrientation(BarsView.Orientation.X1RIGHT_X2UP);
		} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
			bv.setOrientation(BarsView.Orientation.X1DOWN_X2RIGHT);
		}
		addTiledView(irow,icol,bv);
		return bv;
	}
	
	public PixelsView addPixelsView(int irow, int icol, PixelsView pv) {
		pv.getColorMap().addListener(_colorMapHandler);
		if (_orientation==Orientation.X1RIGHT_X2UP) {
			pv.setOrientation(PixelsView.Orientation.X1RIGHT_X2UP);
		} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
			pv.setOrientation(PixelsView.Orientation.X1DOWN_X2RIGHT);
		}
		addTiledView(irow,icol,pv);
		return pv;
	}
	public PixelsViewDouble addPixelsViewDouble(int irow, int icol, PixelsViewDouble pv) {
		pv.getColorMap().addListener(_colorMapHandler);
		if (_orientation==Orientation.X1RIGHT_X2UP) {
			pv.setOrientation(PixelsViewDouble.Orientation.X1RIGHT_X2UP);
		} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
			pv.setOrientation(PixelsViewDouble.Orientation.X1DOWN_X2RIGHT);
		}
		addTiledView(irow,icol,pv);
		return pv;
	}
	public TransparentPixelsView addTransparentPixelsView(int irow, int icol, TransparentPixelsView pv) {
		pv.getColorMap().addListener(_colorMapHandler);
		if (_orientation==Orientation.X1RIGHT_X2UP) {
			pv.setOrientation(TransparentPixelsView.Orientation.X1RIGHT_X2UP);
		} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
			pv.setOrientation(TransparentPixelsView.Orientation.X1DOWN_X2RIGHT);
		}
		addTiledView(irow,icol,pv);
		return pv;
	}

	public PointsView addPointsView(int irow, int icol, PointsView pv) {
		if (_orientation==Orientation.X1RIGHT_X2UP) {
			pv.setOrientation(PointsView.Orientation.X1RIGHT_X2UP);
		} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
			pv.setOrientation(PointsView.Orientation.X1DOWN_X2RIGHT);
		}
		addTiledView(irow,icol,pv);
		return pv;
	}
	public PointsViewWithLegend addPointsViewWithLegend(int irow, int icol, PointsViewWithLegend pv) {
		if (_orientation==Orientation.X1RIGHT_X2UP) {
			pv.setOrientation(PointsViewWithLegend.Orientation.X1RIGHT_X2UP);
		} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
			pv.setOrientation(PointsViewWithLegend.Orientation.X1DOWN_X2RIGHT);
		}
		addTiledView(irow,icol,pv);
		return pv;
	}

	private ContoursView addContoursView(int irow, int icol, ContoursView cv) {
		if (_orientation==Orientation.X1RIGHT_X2UP) {
			cv.setOrientation(ContoursView.Orientation.X1RIGHT_X2UP);
		} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
			cv.setOrientation(ContoursView.Orientation.X1DOWN_X2RIGHT);
		}
		addTiledView(irow,icol,cv);
		return cv;
	}
	
	public ContoursViewDouble addContoursViewDouble(int irow, int icol, ContoursViewDouble cv) {
		if (_orientation==Orientation.X1RIGHT_X2UP) {
			cv.setOrientation(ContoursViewDouble.Orientation.X1RIGHT_X2UP);
		} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
			cv.setOrientation(ContoursViewDouble.Orientation.X1DOWN_X2RIGHT);
		}
		addTiledView(irow,icol,cv);
		return cv;
	}

	public SequenceView addSequenceView(int irow, int icol, SequenceView sv) {
		addTiledView(irow,icol,sv);
		return sv;
	}

	public void setBestHorizontalClips(int icol, double v0, double v1) {
		int nrow = getMosaic().countRows();
		for (int irow=0; irow<nrow; ++irow)
			getTile(irow,icol).setBestHorizontalProjector(new Projector(v0, v1));
	}
	public void setBestVerticalClips(int irow, double v0, double v1) {
		int ncol = getMosaic().countRows();
		for (int icol=0; icol<ncol; ++icol)
			getTile(irow,icol).setBestVerticalProjector(new Projector(v0, v1));
	}

	private void setBestHorizontalProjector(int icol, Projector p) {
		int nrow = getMosaic().countRows();
		for (int irow=0; irow<nrow; ++irow)
			getTile(irow,icol).setBestHorizontalProjector(p);
	}

	private void setBestVerticalProjector(int irow, Projector p) {
		int ncol = getMosaic().countColumns();
		for (int icol=0; icol<ncol; ++icol)
			getTile(irow,icol).setBestVerticalProjector(p);
	}

	/**
	 * Called when the color bar in this panel may need resizing.
	 * This implementation simply removes and adds any existing color bar.
	 * This method is necessary because we want the colorbar height to equal
	 * that of the tiles in the mosaic, but not including any tile axes.
	 */
	private void adjustColorBar() {
		if (_colorBar!=null) {
			GridBagLayout gbl = (GridBagLayout)getLayout();
			gbl.setConstraints(_colorBar,makeColorBarConstraints());
			revalidate();
			//remove(_colorBar);
			//add(_colorBar,makeColorBarConstraints());
		}
	}

	private GridBagConstraints makeColorBarConstraints() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0;
		gbc.weighty = 100;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets.top = _mosaic.getHeightAxesTop();
		gbc.insets.left = 10;
		gbc.insets.bottom = _mosaic.getHeightAxesBottom();
		gbc.insets.right = 0;
		return gbc;
	}

	/**
	 * Searches for the best color mapped through each tiled view.
	 * The panels are searched top to bottom, right to left.
	 */
	private ColorMapped findBestColorMapped() {
		if (_autoColorMapped) {
			int rows = _mosaic.countRows();
			int cols = _mosaic.countColumns();
			ColorMapped cmBest = null;
			ColorMapped cmSolid = null;
			for (int ncol=cols-1; ncol>=0; --ncol) {
				for (int nrow=0; nrow<rows; ++nrow) {
					Tile t = getTile(nrow,ncol);
					int ntv = t.countTiledViews();
					for (int itv=ntv-1; itv>=0 && cmBest==null; --itv) {
						TiledView tv = t.getTiledView(itv);
						if (tv instanceof ColorMapped) {
							ColorMapped cm = (ColorMapped)tv;
							if (isMultiColor(cm)) {
								cmBest = cm;
							} else if (cmSolid==null) {
								cmSolid = cm;
							}
						}
					}
				}
			}
			if (cmBest==null)
				cmBest = cmSolid;
			return cmBest;
		} else {
			return _colorMapped;
		}
	}

	/**
	 * Determines if a specified color map has more than one color.
	 * Note that we ignore any variation in alpha.
	 */
	private static boolean isMultiColor(ColorMapped cm) {
		ColorMap cmap = cm.getColorMap();
		IndexColorModel icm = cmap.getColorModel();
		int n = icm.getMapSize();
		int rgb = icm.getRGB(0)&0x00ffffff;
		for (int i=1; i<n; ++i) 
			if (rgb!=(icm.getRGB(i)&0x00ffffff))
				return true;
		return false;
	}

	/**
	 * Verifies the correct color mapped is being used.
	 */
	private void updateColorMapped() {
		ColorMapped cm = findBestColorMapped();
		if (cm!=_colorMapped && _colorBar!=null) {
			if (_colorMapped!=null)
				_colorMapped.getColorMap().removeListener(_colorBar);
			_colorMapped = cm;
			_colorMapped.getColorMap().addListener(_colorBar);
			revalidate();
		}
	}

	/**
	 * Private inner class that handles a change in the color mapped by
	 * listening to the color map.
	 */
	private class ColorMapHandler implements ColorMapListener {

		/**
		 * Called whenever the color map within PlotPanel is changed.
		 * @param cm the color map.
		 */
		public void colorMapChanged(ColorMap cm) {
			updateColorMapped();
		}
	}	
	
	public BufferedImage paintScreenToImage() {
		BufferedImage image = null;
		try {
			Point pt = this.getLocationOnScreen();
			Robot robot = new Robot();
			Dimension d = this.getSize();
			Rectangle bounds = new Rectangle(pt.x, pt.y, d.width, d.height);
			image = robot.createScreenCapture(bounds);
		} catch (AWTException e1) { e1.printStackTrace(); }
		return image;
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

