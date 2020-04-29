
package com.geohammer.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

import com.geohammer.component.FileMenuEvent;

import edu.mines.jtk.mosaic.IPanel;
import edu.mines.jtk.mosaic.Mosaic;
import edu.mines.jtk.mosaic.Tile;
import edu.mines.jtk.mosaic.TiledView;


public class CommonFrame extends JFrame {
	//static final Logger logger = Logger.getLogger(ModelFrame.class);

	private static final long serialVersionUID = 1L;
	public static final int DEFAULT_WIDTH = 720;
	public static final int DEFAULT_HEIGHT = 550;
	public static final Font DEFAULT_FONT = new Font("Arial",Font.PLAIN,24);
	public static final Color DEFAULT_BACKGROUND = Color.WHITE;

	public boolean 		_fontSizeForPrint; 	// true if auto font size for print
	public boolean 		_fontSizeForSlide; 	// true if auto font size for slide
	public double 		_fracWidthSlide; 	// fraction of slide height available
	public double 		_fracHeightSlide; 	// fraction of slide width available
	public double 		_fontSizePrint; 	// font size for print
	public double 		_plotWidthPrint; 	// plot width for print

	protected int 				exitCode	= 0;
	protected int 				processID	= 0;
	protected CommonPanel 		panel 		= null;
	protected LayerPreferences 	preferences = null;

	public CommonFrame() { this(null); }
	public CommonFrame(String title) {
		super();
		if(title!=null) setTitle(title);
		setBackground(Color.white);
	}
	
	public CommonProject getProject() { return null; }
	public boolean openProject(String projectFileName) { return true; }
	public boolean saveProject(String selectedFileName) { return true; }
	public void savePreferences() { }
	
	public IPanel getBaseWorld() 					{ return panel; }
	public Mosaic getMosaic() 						{ return panel.getMosaic(); }
	public Tile getTile(int irow, int icol) 		{ return getMosaic().getTile(irow,icol); }
	
	public LayerPreferences getPreferences() 		{ return preferences; }
	public CommonPanel getCommonPanel() 			{ return panel; }
	public int getProcessID() 						{ return processID; }
	
	public String getCwd() 		{		
		String baseName = getProject().getBaseName();
		if(baseName.contains("untitled")) {
			return System.getProperty("user.dir")+File.separator;
		} else {
			return getProject().getFullPath(); 
		}
	}
	
	public void setCommonPanel(CommonPanel panel) 	{ this.panel = panel; }
	public void setProcessID(int processID) 		{ this.processID = processID; }

	public String getIconName(String iconName) 		{ return "com/vecon/resource/icon/" + iconName + ".png"; }
	public ArrayList<String> getRecentFileList()	{ return preferences.getRecentFileList(); }
	
	public void fileSelected( FileMenuEvent e ) {
		File file = e.file();
		if(!file.exists()) {
			JOptionPane.showMessageDialog(this, "project file does not exist", "Error", JOptionPane.ERROR_MESSAGE );
			return;
		}
		boolean success = openProject(file.getAbsolutePath() );
	}
	public class doYouWantSave extends WindowAdapter {		
		public void windowClosing( WindowEvent e ) {
			CommonProject project = getProject();
			if(project!=null) { 
				String baseName = project.getBaseName();
				if(baseName!=null&&!baseName.isEmpty()) {
					if(baseName.contains("untitled")) { 
						Object[] options = {"Save As","No","Cancel"};
						int userChoice = JOptionPane.showOptionDialog(
								CommonFrame.this, "Your work has not been saved. Do you want to save the project?", 
								"Exit Dialog", JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.WARNING_MESSAGE, null, options, options[0] );
						
						if( userChoice == 0 ) 		saveProjectAs(); 
						else if( userChoice == 1 ) 	exit(exitCode==1); 
						else 						return;
					}
				}
			} 
			exit(exitCode==1);
		}
	}
	
	public void exit(boolean global) {
		savePreferences();
		setVisible(false); 
		dispose();
		if(exitCode==1) System.exit(0);
	}
	
	public String openDirectoryUsingJFileChooser(String base)	{
		return usingJFileChooser(1, true, null, base);
	}
	public String openFileUsingJFileChooser(FileNameExtensionFilter [] exts, String base)	{
		return usingJFileChooser(0, true, exts, base);
	}
	public String openFileAndDirectoryUsingJFileChooser(FileNameExtensionFilter [] exts, String base)	{
		return usingJFileChooser(2, true, exts, base);
	}
	public String saveFileUsingJFileChooser(FileNameExtensionFilter [] exts, String base)	{
		return usingJFileChooser(0, false, exts, base);
	}
	private String usingJFileChooser(int isDir, boolean isOpen, FileNameExtensionFilter exts[], String base)	{
		File baseFile = new File(base);
		JFileChooser chooser = new JFileChooser(baseFile.isDirectory()?base:FilenameUtils.getPath(base));
		int index = FilenameUtils.indexOfExtension(base);
		if(index>0) chooser.setSelectedFile(baseFile);
		//chooser.setCurrentDirectory(baseFile);
		//chooser.setAcceptAllFileFilterUsed(false);
		String titleName = null;
		if(isDir==0) {
			if(exts!=null) 
				for(int i=0; i<exts.length; i++) chooser.addChoosableFileFilter( exts[i] );
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			titleName = " Files";
		} else if(isDir==1){
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			titleName = " Directories";
		} else {
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			titleName = " Files And Directories";
		}

		int returnVal = 0;
		if(isOpen) {
			chooser.setDialogTitle("Open"+titleName);
			returnVal = chooser.showOpenDialog( null );
		} else {
			chooser.setDialogTitle("Save"+titleName);
			returnVal = chooser.showSaveDialog( null );
		}
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			String fullFileName = null;
			//directory only
			if(isDir==1 || (exts==null)) {
				if(file.isFile()) {
					if(isDir==1) return null;
					else return file.getAbsolutePath();
				}
				if (file!= null) {
					if (file.exists()) {
						fullFileName = file.getAbsolutePath();
					} else {
						File parentFile = new File(file.getParent());
						fullFileName = parentFile.getAbsolutePath();
					}
				} 
				return fullFileName;
			}
			fullFileName = file.getAbsolutePath();
			if ((!isOpen)&&file.exists()) {
				int response = JOptionPane.showConfirmDialog (null,
						"Overwrite existing file? ","Confirm Overwrite",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.CANCEL_OPTION) { return null; }
			}

			String selectedExt = FilenameUtils.getExtension(fullFileName);

			if(selectedExt==null||selectedExt.isEmpty()) {
				if(!isOpen) fullFileName = fullFileName+"."+exts[0].getExtensions()[0];
			} else {
				boolean validExt = false;
				for(int i=0; i<exts.length; i++) {
					String [] possibleExt = exts[i].getExtensions();
					//System.out.println("selectedExt="+selectedExt+" "+Arrays.toString(possibleExt));
					for(int j=0; j<possibleExt.length; j++) {
						if(selectedExt.equalsIgnoreCase(possibleExt[j])) {
							validExt = true;
							j = possibleExt.length+1;
							i = exts.length+1;
						}
					}
				}
				if(!validExt) {
					String message = "File name extension is not valid: "+fullFileName;
					String title = "Alert";
					JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
					return null;
				}
			}
			return fullFileName;
		} else if (returnVal == JFileChooser.CANCEL_OPTION) { 
			return null;		 							
		} else { }
		return null;
	}
	
	public void openProject()	{
		String  recentFileName = preferences.getRecentFile();
		String ext = getProject().getExtension();
		FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] {
				new FileNameExtensionFilter("(*."+ext+")", ext) };
		String fileName = openFileUsingJFileChooser(exts, recentFileName); 
		if(fileName==null) return;
		else { openProject(fileName); }
	}
	public void openProjectWithExtension(FileNameExtensionFilter [] exts)	{
		String  recentFileName = preferences.getRecentFile();
		String fileName = openFileUsingJFileChooser(exts, recentFileName); 
		if(fileName==null) return;
		else { openProject(fileName); }
	}

	public boolean saveProject()	{ 
		String baseName = getProject().getBaseName();
		if(baseName.contains("untitled")) {
			return saveProjectAs();
		} else {
			return saveProject(getProject().getProjectFileName()); 
		}
	}
	public boolean saveProjectAs()	{ 
		String ext = getProject().getExtension();
		FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] {
				new FileNameExtensionFilter("(*."+ext+")", ext) };
		String base = getProject().getFullName()+"."+exts[0].getExtensions()[0];
		//if(base.contains("untitled")) base = _preferences.getRecentFile();
		//System.out.println("v="+getProject().getFullName()+" v1="+base);
		String fileName = saveFileUsingJFileChooser(exts, base); 
		if(fileName==null) return false;
		else {
			String selectedExt = FilenameUtils.getExtension(fileName);
			if(selectedExt==null||selectedExt.isEmpty()) fileName = fileName+"."+ext;
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
			if(k==0) 	saveProject(fileName); 
			else 		saveProject(fileName+"."+ext); 
		}
		return true;
	}

	public void fadeOut() {
		int delay = 500;   // delay for 1 sec.
		int period = 200;  // repeat every 3 sec.
		JDialog dialog = new JDialog(this);
		dialog.setUndecorated(true);
		JLabel label = new JLabel("saving...   ");

		dialog.setLocationRelativeTo(this);
		//dialog.setTitle(label.getText());
		//dialog.add(label);
		dialog.add(new JLabel("<html><font color='red' size='+3' >saving...   </font></html>"));
		dialog.pack();
		dialog.setVisible(true);		
		fadeOut(dialog, delay, period);	
	}
	public void fadeOut(final JDialog dialog, int delay, int period) {
		final Timer timer = new Timer(period, null);
		timer.setRepeats(true);
		timer.addActionListener(new ActionListener() {
			private float opacity = 1;
			@Override 
			public void actionPerformed(ActionEvent e) {
				opacity -= 0.15f;
				dialog.setOpacity(Math.max(opacity, 0));
				if (opacity <= 0) {
					timer.stop();
					dialog.dispose();
				}
			}
		});

		dialog.setOpacity(1);
		timer.setInitialDelay(delay);
		timer.start();
	}	

	public void setVisible(boolean visible) {
		super.setVisible(visible);

		if(visible) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice gd = ge.getDefaultScreenDevice();
			GraphicsConfiguration [] gc = gd.getConfigurations();
			Rectangle gcBounds = gc[0].getBounds();

			//Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
			//int w = dimension.width / 2;
			//int h = 3*dimension.height / 4;
			//System.out.println("w=" + w + " h=" + h + " 1=" + dimension.width + " 2=" + dimension.height);

			int w = (int)(8.0*gcBounds.getWidth() / 10.0);
			int h = (int)(8.0*gcBounds.getHeight() / 10.0);
			//int h = (int)(w/2);
			//int h = (int)(gcBounds.getHeight()-40.0);
			//setSize((int)(gcBounds.getWidth()-10), (int)(gcBounds.getHeight()/2));
			setSize(w, h);
			//setSize(1000, 800);

			setLocationRelativeTo(null);

			//			int cx = (int)(gcBounds.getWidth() / 2.0 -w/2);
			//			int cy = (int)(gcBounds.getHeight() / 2.0 -h/2);
			//			//setLocation(cx, cy); 
			//			setLocation(50, 50); 
			//			int minW = (int)(0.9*w / 3.0);
			//			int minW1 = (int)(1.9*w / 3.0);
			//			int minH = (int)(0.9*h / 2.0);
			//_mvp.setMinimumSize(new Dimension(minW1, minH));
			//_mvs.setMinimumSize(new Dimension(minW1, minH));
			//_world.setMinimumSize(new Dimension(minW, h));
		}
		//repaint();
	}

	public void to1to1Ratio() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration [] gc = gd.getConfigurations();
		Rectangle gcBounds = gc[0].getBounds();

		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int w1 = dimension.width / 2;
		int h1 = 3*dimension.height / 4;
		System.out.println("w1=" + w1 + " h1=" + h1 + " 1=" + dimension.width + " 2=" + dimension.height);

		int w = (int)(gcBounds.getWidth());
		int h = (int)(gcBounds.getHeight());
		//int h = (int)(w/2);
		setSize(w, h);
		//setSize(800, 600);
		//System.out.println("w=" + w + " h=" + h );

		setLocationRelativeTo(null);
	}

	//http://viralpatel.net/blogs/20-useful-java-code-snippets-for-java-developers/
	public void captureScreen(String fileName) throws Exception { 
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); 
		Rectangle screenRectangle = new Rectangle(screenSize); 
		Robot robot = new Robot(); 
		BufferedImage image = robot.createScreenCapture(screenRectangle); 
		ImageIO.write(image, "png", new File(fileName)); 
	}



	//	/**
	//	 * Paints this frame's panel(s) to a PNG image with specified parameters.
	//	 * The image height is computed so that the image has the same aspect 
	//	 * ratio as the panel(s) in this frame.
	//	 * @param dpi the image resolution, in dots per inch.
	//	 * @param win the image width, in inches.
	//	 * @param fileName the name of the file to contain the PNG image.  
	//	 */
	//	public void paintToPng(double dpi, double win, String fileName) {
	//
	//		// If our main panel is valid, this is the Swing thread, and
	//		// no tile axis has a revalidate pending, then paint the main 
	//		// panel to the specified PNG image file. Otherwise, invoke 
	//		// this method later on the Swing thread.
	//		if (canPaintToPngNow()) {
	//			try {
	//				getBaseWorld().paintToPng(dpi,win,fileName);
	//			} catch (IOException ioe) {
	//				throw new RuntimeException(ioe);
	//			}
	//		} else {
	//			final double fdpi = dpi;
	//			final double fwin = win;
	//			final String ffileName = fileName;
	//			SwingUtilities.invokeLater(new Runnable() {
	//				public void run() {
	//					paintToPng(fdpi,fwin,ffileName);
	//				}
	//			});
	//		}
	//	}
	//	private boolean canPaintToPngNow() {
	//		if (!getBaseWorld().isValid()) {
	//			//trace("PlotFrame: main panel not valid");
	//			return false;
	//		}
	//		if (!SwingUtilities.isEventDispatchThread()) {
	//			//trace("PlotFrame: not event dispatch thread");
	//			return false;
	//		}
	//		//    if (TileAxis.revalidatePending(this)) {
	//		//      //trace("PlotFrame: revalidate pending");
	//		//      return false;
	//		//    }
	//		//trace("PlotFrame: can paint to PNG");
	//		return true;
	//	}



	/**
	 * Sets the font in all components in this frame.
	 * @param font the font.
	 */
	public void setFont(Font font) {
		super.setFont(font);
		if (getBaseWorld()==null) return;
		getBaseWorld().setFont(font);
	}

	/**
	 * Sets the font size (in points) for all panels in this frame.
	 * The font size will not change as this frame is resized.
	 * @param size the size.
	 */
	public void setFontSize(float size) {
		setFontSizeInternal(size);
		_fontSizeForPrint = false;
		_fontSizeForSlide = false;
	}

	/**
	 * Sets font size automatically for a slide in presentations.
	 * This method causes the font size to change as this frame is resized,
	 * so that text height will be about 1/25 of slide height if this frame
	 * is saved to an image.
	 * <p>
	 * The width/height ratio of the slide is assumed to be 4/3. However,
	 * only fractions of the slide width and height may be available if,
	 * for example, more than one plot will be shown on the same slide.
	 * @param fracWidth the fraction of slide width available.
	 * @param fracHeight the fraction of slide height available.
	 */
	public void setFontSizeForSlide(double fracWidth, double fracHeight) {
		_fracWidthSlide = fracWidth;
		_fracHeightSlide = fracHeight;
		_fontSizeForSlide = true;
		_fontSizeForPrint = false;
	}

	/**
	 * Sets font size to automatically adjust for a printed manuscript.
	 * This method causes the font size to change as this frame is resized,
	 * so that font size will equal the specified size if this frame is saved
	 * to an image and that image is then scaled to fit the specified width.
	 * @param fontSize the printed font size (in points) for this plot.
	 * @param plotWidth the printed width (in points) of this plot.
	 */
	public void setFontSizeForPrint(double fontSize, double plotWidth) {
		_fontSizePrint = fontSize;
		_plotWidthPrint = plotWidth;
		_fontSizeForPrint = true;
		_fontSizeForSlide = false;
	}

	/**
	 * Sets the foreground color in all components in this frame.
	 * @param color the foreground color.
	 */
	public void setForeground(Color color) {
		super.setForeground(color);
		if (getBaseWorld()==null) return;
		getBaseWorld().setForeground(color);
	}

	/**
	 * Sets the background color in all components in this frame.
	 * @param color the background color.
	 */
	public void setBackground(Color color) {
		super.setBackground(color);
		if (getBaseWorld()==null) return;
		getBaseWorld().setBackground(color);
	}

	// Necessary because we have more than one way to set the font size.
	private void setFontSizeInternal(float size) {
		Font font = getFont();
		if (font==null)
			font = UIManager.getFont("Panel.font");
		if (font!=null) {
			font = font.deriveFont(size);
			setFont(font);
		}
	}

	// When this frame is resized, we may need to adjust the font size.
	public void addResizedShownListener() {
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				adjustFontSize();
			}
			public void componentShown(ComponentEvent e) {
				adjustFontSize();
			}
		});
	}
	public void adjustFontSize() {
		if (_fontSizeForPrint) {
			adjustFontSizeForPrint();
		} else if (_fontSizeForSlide) {
			adjustFontSizeForSlide();
		}
	}

	// Sets font size for a slide with width/height ratio = 4/3.
	private void adjustFontSizeForSlide() {

		// Width and height of the main panel for this frame.
		double panelWidth = getBaseWorld().getWidth();
		double panelHeight = getBaseWorld().getHeight();
		if (panelWidth==0.0 || panelHeight==0.0)
			return;

		// Part of the slide that is available for the main panel.
		// Here we assume that the slide has a 4:3 aspect ratio.
		double slideWidth = 4.0*_fracWidthSlide;
		double slideHeight = 3.0*_fracHeightSlide;

		// The aspect ratio of this panel will typically not match that of
		// the space available on the slide. Therefore, the panel height may
		// represent only a fraction of the height available on the slide.
		// This scale factor is that fraction.
		double scaleHeight = 1.0;
		if (panelWidth*slideHeight>panelHeight*slideWidth)
			scaleHeight = (panelHeight*slideWidth)/(panelWidth*slideHeight);

		// Font size equals 1/20 of panel height, adjusted for any
		// scaling and our use of only a fraction of slide height.
		double fontSize = panelHeight/scaleHeight/_fracHeightSlide/20.0;
		setFontSizeInternal((float)fontSize);
	}

	// Sets font size for plots in printed manuscripts.
	private void adjustFontSizeForPrint() {
		double panelWidth = getBaseWorld().getWidth();
		float fontSize = (float)(_fontSizePrint*panelWidth/_plotWidthPrint);
		setFontSizeInternal(fontSize);
	}

	
	public enum MENUID {
	    RT2D,
	    DFN2D,
	    MagR2D,
	    Dots3D;
	}
}
