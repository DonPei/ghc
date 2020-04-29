package com.geohammer.common.d3;

import static org.monte.media.VideoFormatKeys.QualityKey;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.mines.jtk.sgl.OrbitView;
import edu.mines.jtk.sgl.OrbitViewMode;
import edu.mines.jtk.sgl.ViewCanvas;

import org.apache.commons.io.FileUtils;
import org.monte.media.Buffer;
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.VideoFormatKeys;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.avi.AVIWriter;
import org.monte.media.math.Rational;
import com.geohammer.common.CommonDialog;
import com.geohammer.resource.MathArrowIcon;
import com.geohammer.common.util.ArrowIcon;
import com.geohammer.common.util.ArrowIcon.ArrowButton;

public class Common3DMobilityDialog  extends CommonDialog {
	private Common3DPanel 	_panel 		= null;
	private OrbitViewMode 	_ovm		= null;
	private OrbitView 		_view 		= null;
	private ViewCanvas 		_canvas 	= null;
	
	private boolean 		_savable 	= false; 
	private JTextField 		_movieDirTF = null;
	private int 			_imageIndex = 0;

	public Common3DMobilityDialog(Common3DPanel panel, String aTitle, boolean modal, String movieDir) {
		super(panel.getFrame(), aTitle, modal);
		setDialogWindowSize(800, 650);
		_panel 	= panel;
		_ovm 	= panel.getObitViewMode();
		_view 	= panel.getOrbitView();
		_canvas = panel.getViewCanvas();
		
		//String a 	= System.getProperty("user.dir");
		//String movieDir = a+File.separator+"movie";
		_movieDirTF = new JTextField(movieDir);
		_imageIndex = 0;
	}
	
	protected JPanel createContents() {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());
		Insets insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gbc= new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);

		SliderPanelPlayer slider = new SliderPanelPlayer("Azimuth (degree)", -180, 180, 90);
		topPanel.add(slider, gbc);
		
		slider = new SliderPanelPlayer("Elevation (degree)", -90, 90, 45);
		gbc= new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		topPanel.add(slider, gbc);
		
		slider = new SliderPanelPlayer("Bounding Box (1/percentage)", 50, 200, 50);
		gbc= new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		topPanel.add(slider, gbc);
			
		gbc= new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		topPanel.add(new TransferPanel("Move/Transfer"), gbc);
		
		gbc= new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		topPanel.add(new ScalePanel("Scale"), gbc);
		
		gbc= new GridBagConstraints(0, 5, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		topPanel.add(new ResetPanel("Reset"), gbc);
		
		gbc= new GridBagConstraints(0, 6, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		topPanel.add(new MoviePanel("Movie"), gbc);
		
		return topPanel;
	}
	
	protected boolean okAction() { return true; }
	public JComponent getCommandRow() { return null;	}
	
	private class SliderPanelPlayer extends JPanel {
		String 		_name			= null;
		int 		_min			= 0;
		int 		_max 			= 1;
		double 		_value 			= 1.0;

		JTextField 	_incrementTF 	= null;
		JSlider  	_slider 		= null;
		int _id = 1;

		ActionListener listener = new ActionListener(){
			public void actionPerformed(ActionEvent event){
				move(_id);
			}
		};
		Timer _timer = null;

		public SliderPanelPlayer(String name, int min, int max, int tickSpacing) {
			_name = name;
			_min = min;
			_max = max;

			setLayout(new GridBagLayout());
			Insets insets = new Insets(1, 1, 1, 1);
			GridBagConstraints gbc;

			int size = 20;
			JButton moveRight = new JButton(new ArrowIcon(ArrowIcon.EAST, size));
			moveRight.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					move(1);
					if(_timer!=null) _timer.stop();
				}
			});
			moveRight.setToolTipText("Play Forward");

			JButton moveLeft = new JButton(new ArrowIcon(ArrowIcon.WEST, size));
			moveLeft.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					move(-1);
					if(_timer!=null) _timer.stop();
				}
			});
			moveLeft.setToolTipText("Play Backward");

			ArrowButton moveRight2 = new ArrowButton(SwingConstants.EAST, 2, 8);
			moveRight2.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					startTimer(1);
				}
			});
			moveRight2.setToolTipText("Auto Play Forward");

			ArrowButton moveLeft2 = new ArrowButton(SwingConstants.WEST, 2, 8);
			moveLeft2.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					startTimer(-1);
				}
			});
			moveLeft2.setToolTipText("Auto Play Backward");

			JButton plus = new JButton(getMathArrowIcon(SwingConstants.NORTH, 7));
			plus.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					if(_timer==null) return;
					int delay = _timer.getDelay();
					_timer.setDelay(delay/2);
				}
			});
			plus.setToolTipText("faster");


			JButton minus = new JButton(getMathArrowIcon(SwingConstants.EAST, 8));
			minus.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					if(_timer==null) return;
					int delay = _timer.getDelay();
					_timer.setDelay(delay*2);
				}
			});
			minus.setToolTipText("slower");

			JButton pauseButton = new JButton(getPauseIcon(SwingConstants.NORTH, 8));
			pauseButton.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					if(_timer!=null) {
						_timer.stop();
					}
				}
			});
			pauseButton.setToolTipText("pause");			

			_incrementTF = new JTextField("5.0", 10);

			int idx = 0;
			gbc = new GridBagConstraints(idx++, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(new JLabel("Increment:"), gbc);

			gbc = new GridBagConstraints(idx++, 0, 1, 1, 0.1, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_incrementTF, gbc);

			gbc = new GridBagConstraints(idx++, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(moveLeft2, gbc);

			gbc = new GridBagConstraints(idx++, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(moveLeft, gbc);

			if(_name.equalsIgnoreCase("Azimuth (degree)")) {
				_value = _ovm.getAzimuth(_canvas, _view);
			} else if(_name.equalsIgnoreCase("Elevation (degree)")) {
				_value = _ovm.getElevation(_canvas, _view);
			} else if(_name.equalsIgnoreCase("Bounding Box (1/percentage)")) {
				_value = 100;
			} else { 
				_value = 0.0;
			}
			_slider = genSlider(min, max, (int)_value, tickSpacing);
			_slider.setToolTipText(_name);
			gbc = new GridBagConstraints(idx++, 0, 1, 1, 0.9, 0.7,
					GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_slider, gbc);

			gbc = new GridBagConstraints(idx++, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(moveRight, gbc);
			gbc = new GridBagConstraints(idx++, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(moveRight2, gbc);

			gbc = new GridBagConstraints(idx++, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(plus, gbc);
			gbc = new GridBagConstraints(idx++, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(minus, gbc);
			gbc = new GridBagConstraints(idx++, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(pauseButton, gbc);

			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			TitledBorder title = BorderFactory.createTitledBorder(loweredetched, name);
			setBorder(title);
		}


		public void setupTimer() {
			int delay = 3000;   // delay for 5 sec.
			int period = 1000;  // repeat every sec.
			_timer = new Timer(period, listener);
			_timer.setInitialDelay(delay);
		}

		public void startTimer(int id) {
			_id = id;
			if(_timer==null) {
				int delay = 500;   // delay for .5 sec.
				int period = 1000;  // repeat every sec.
				_timer = new Timer(period, new ActionListener(){
					public void actionPerformed(ActionEvent event){
						move(_id);
					}
				});
				_timer.setInitialDelay(delay);
				_timer.start();
			} else {
				if(_timer.isRunning()) {
					_timer.stop();
				} else {
					_timer.restart();
				}
			}
		}

		public void move(int id) {
			double increment = Double.parseDouble(_incrementTF.getText());;
			if(increment<=0.0) return;
			_value += id*increment;
			//			if(_value >= _max) _value = _max;
			//			if(_value <= _min) _value = _min;
			if(_value > _max) _value = _min;
			if(_value < _min) _value = _max;			
			_slider.setValue((int)_value);

			if(_savable) {
				String 	movieDir 	= _movieDirTF.getText().trim();
				String fileName = String.format("dfn%04d.jpg" , ++_imageIndex);
				saveScreenImageToFile(movieDir+File.separator+fileName);
			}
		}

		protected void saveScreenImageToFile(String fileName) {
			BufferedImage image = null;
			try {
				ViewCanvas canvas = _canvas;
				Point pt = canvas.getLocationOnScreen();
				Robot robot = new Robot();
				Dimension d = canvas.getSize();
				Rectangle bounds = new Rectangle(pt.x, pt.y, d.width, d.height);
				image = robot.createScreenCapture(bounds);
			} catch (AWTException e1) { e1.printStackTrace(); }

			//System.getProperty("java.io.tmpdir")

			if(fileName==null) return;
			else {
				File f = new File(fileName);
				try { ImageIO.write(image, "jpg", f);
				} catch (IOException e1) { e1.printStackTrace(); }
			}
		}

		public void rotate(){
			//System.out.println(_value);
			if(_name.equalsIgnoreCase("Azimuth (degree)")) {
				_ovm.rotateToAzimuth(_value, _canvas, _view);
			} else if(_name.equalsIgnoreCase("Elevation (degree)")) {
				_ovm.rotateToElevation(_value, _canvas, _view);
			} else if(_name.equalsIgnoreCase("Bounding Box (1/percentage)")) {
				//_frame.setWorldSphere(_value/100.0);            	
				_ovm.scaleUpDown(2, _value/100.0, _canvas, _view);
			} else { }
		}
		public JSlider genSlider(int min, int max, int value, int tickSpacing){
			JSlider slider = new JSlider(min, max, value);
			// Show tick marks
			slider.setPaintTrack(true);
			slider.setPaintTicks(true);
			// Set major tick marks every 25 units
			//int tickSpacing = 90;
			slider.setMajorTickSpacing(tickSpacing);

			// Set minor tick marks every 5 units
			//tickSpacing = 5;
			//_azimuthSlide.setMinorTickSpacing(tickSpacing);
			// Paint labels at the major ticks - 0, 25, 50, 75, and 100
			slider.setPaintLabels(true);

			// Register a change listener
			slider.addChangeListener(new ChangeListener() {
				// This method is called whenever the slider's value is changed
				public void stateChanged(ChangeEvent evt) {
					JSlider slider = (JSlider)evt.getSource();
					if (!slider.getValueIsAdjusting()) {
						// Get new value
						_value = slider.getValue();
						rotate();
					}
				}
			});
			return slider;
		}
		public MathArrowIcon getPauseIcon(int direction, int type) {
			int sizeW = 16;
			int sizeH = 16;
			int style = 3;
			double headSize = -1.5;
			double arrowAngle = 1.5;
			int lineWidth = 60;
			Color color = Color.red;

			return new MathArrowIcon(direction, sizeW, sizeH, 
					style, type, arrowAngle, headSize, lineWidth, color);
		}

		public MathArrowIcon getMathArrowIcon(int direction, int type) {
			int sizeW = 16;
			int sizeH = 16;
			int style = 3;
			double headSize = -1.5;
			double arrowAngle = 1.5;
			int lineWidth = 6;
			Color color = new Color(75, 125,0);
			//Color color = Color.red;

			return new MathArrowIcon(direction, sizeW, sizeH, 
					style, type, arrowAngle, headSize, lineWidth, color);
		}
	}

	private class ScalePanel extends JPanel {
		int size = 16;
		public ScalePanel(String name) {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;

			JButton moveUp = new JButton(getMathArrowIcon(ArrowIcon.NORTH, 3));
			moveUp.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					_ovm.stretchZ(_canvas, _view);
				}
			});

			JButton moveDown = new JButton(getMathArrowIcon(ArrowIcon.NORTH, 4));
			moveDown.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					_ovm.shrinkZ(_canvas, _view);
				}
			});

			JButton moveXLeft = new JButton(getMathArrowIcon(ArrowIcon.EAST, 3));
			moveXLeft.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					_ovm.stretchX(_canvas, _view);
				}
			});

			JButton moveXRight = new JButton(getMathArrowIcon(ArrowIcon.EAST, 4));
			moveXRight.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					_ovm.shrinkX(_canvas, _view);
				}
			});

			JButton moveYLeft = new JButton(getMathArrowIcon(ArrowIcon.EAST, 5));
			moveYLeft.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					_ovm.stretchY(_canvas, _view);
				}
			});

			JButton moveYRight = new JButton(getMathArrowIcon(ArrowIcon.NORTH, 5));
			moveYRight.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					_ovm.shrinkY(_canvas, _view);
				}
			});

			gbc = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(moveUp, gbc);

			gbc = new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(moveDown, gbc);

			gbc = new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(moveXLeft, gbc);

			gbc = new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(moveXRight, gbc);

			gbc = new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(moveYLeft, gbc);

			gbc = new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(moveYRight, gbc);
			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			TitledBorder title = BorderFactory.createTitledBorder(loweredetched, name);
			setBorder(title);
		}

		public MathArrowIcon getMathArrowIcon(int direction, int type) {
			int sizeW = 24;
			int sizeH = 24;
			int style = 2;
			double headSize = 1.5;
			double arrowAngle = 1.5;
			int lineWidth = 2;
			Color color = new Color(75, 125,0);

			return new MathArrowIcon(direction, sizeW, sizeH, 
					style, type, arrowAngle, headSize, lineWidth, color);
		}
	}
	
	private class TransferPanel extends JPanel {
		
		public MathArrowIcon getMathArrowIcon(int direction) {
			int sizeW = 24;
			int sizeH = 24;
			int style = 2;
			int type = 1;
			double headSize = 1.5;
			double arrowAngle = 3.0;
			int lineWidth = 2;
			Color color = Color.red;
			
			return new MathArrowIcon(direction, sizeW, sizeH, 
			style, type, arrowAngle, headSize, lineWidth, color);
		}
		
		public TransferPanel(String name) {
			int size = 16;
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;
			JButton moveLeft = new JButton(getMathArrowIcon(SwingConstants.WEST));
			moveLeft.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					_ovm.moveLeft(_canvas, _view);
				}
			});
			
			JButton moveRight = new JButton(getMathArrowIcon(ArrowIcon.EAST));
			moveRight.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					_ovm.moveRight(_canvas, _view);
				}
			});

			JButton moveUp = new JButton(getMathArrowIcon(ArrowIcon.NORTH));
			moveUp.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					_ovm.moveUp(_canvas, _view);
				}
			});
			
			JButton moveDown = new JButton(getMathArrowIcon(ArrowIcon.SOUTH));
			moveDown.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					_ovm.moveDown(_canvas, _view);
				}
			});
			
			gbc = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(moveLeft, gbc);
			
			gbc = new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(moveRight, gbc);
			
			gbc = new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(moveUp, gbc);
			
			gbc = new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(moveDown, gbc);
			
			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			TitledBorder title = BorderFactory.createTitledBorder(loweredetched, name);
			setBorder(title);
		}
	}	

	private class ResetPanel extends JPanel {
		int size = 16;
		public ResetPanel(String name) {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;
			
			JButton oneToOne = new JButton("1:1");
			oneToOne.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					//System.out.println("1:1");
					_ovm.setOneToOne(_canvas, _view);
				}
			});
			
			JButton reset = new JButton("Reset");
			reset.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					_ovm.reset(_canvas, _view);
				}
			});
			
			gbc = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(oneToOne, gbc);
			
			gbc = new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(reset, gbc);
			
			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			TitledBorder title = BorderFactory.createTitledBorder(loweredetched, name);
			setBorder(title);
		}
	}
	private class MoviePanel extends JPanel {

		private JTextField 		_fpsTF 			= new JTextField("1");
		
		public MoviePanel(String name) {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;

			JCheckBox jCheckBox = new JCheckBox( "Record Steps Into Images",  _savable);
			jCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					_savable = ((JCheckBox)event.getSource()).isSelected();
				}
			});

			JButton iJButton = new JButton("Initilization");
			iJButton.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					String movieDir = _movieDirTF.getText().trim();
					File file = new File(movieDir);
					if (file.exists()) {
						try {
							FileUtils.cleanDirectory(file);
						} catch (IOException evt) {
							evt.printStackTrace();
						}
					} else {
						if (file.mkdir()) {
							System.out.println("Directory is created! "+movieDir); 
						}
					}

					_imageIndex = 0;
				}
			});

			JButton movie1JButton = new JButton("Make A High Quality Movie");
			movie1JButton.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					int fps = Integer.parseInt(_fpsTF.getText().trim());
					makeMovie(fps, 1.0f);
				}
			});	
			JButton movie2JButton = new JButton("Make A Low Quality Movie");
			movie2JButton.addActionListener(new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					int fps = Integer.parseInt(_fpsTF.getText().trim());
					makeMovie(fps, 0.5f);
				}
			});

			gbc = new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(jCheckBox, gbc);

			gbc = new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(iJButton, gbc);
			
			gbc = new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(new JLabel("# Of Frames Per Second: "), gbc);

			gbc = new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
					GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_fpsTF, gbc);
			

			gbc = new GridBagConstraints(2, 1, 1, 1, 1.0, 1.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(movie1JButton, gbc);
			gbc = new GridBagConstraints(3, 1, 1, 1, 1.0, 1.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(movie2JButton, gbc);

			gbc = new GridBagConstraints(0, 2, 4, 1, 1.0, 1.0,
					GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_movieDirTF, gbc);

			JButton browserButton = new JButton("Browse"); 
			browserButton.addActionListener(new ActionListener()	{
				public void actionPerformed(ActionEvent e)  {
					JFrame jframe = new JFrame("Open A Directory");
					//System.out.println("Selected file1: " + _mtvTF.getText().trim());
					JFileChooser chooser = new JFileChooser(new File(_movieDirTF.getText().trim()));
					FileNameExtensionFilter extension = new FileNameExtensionFilter("*.mtv", "MTV");

					//chooser.setFileFilter( extension );
					chooser.addChoosableFileFilter( extension );
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					//chooser.setDialogTitle("Open A File");

					int returnVal = chooser.showOpenDialog( jframe );
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = chooser.getSelectedFile();
						String path = file.getAbsolutePath();
						//System.out.println("Selected file: " + path);
						_movieDirTF.setText(path.trim());						
					} else if (returnVal == JFileChooser.CANCEL_OPTION) { 							
					} else {
					}
				}
			});

			gbc = new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NORTH, insets, 0, 0);
			add(browserButton, gbc);

			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			TitledBorder title = BorderFactory.createTitledBorder(loweredetched, name);
			setBorder(title);
		}
		
		protected void makeMovie(int fps, float movieQuality) {
			String movieDir = _movieDirTF.getText().trim();
			//String targetPath = snapDir+File.separator+"movie";
			String targetPath = movieDir;
			FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] {
					new FileNameExtensionFilter("Movie (*.avi)","avi")
			};
			String movFileName = null;
			if(movieQuality==1.0f) movFileName = _panel.getFrame().saveFileUsingJFileChooser(exts, targetPath+File.separator+
					"movieHQ."+exts[exts.length-1].getExtensions()[0]); 
			else movFileName = _panel.getFrame().saveFileUsingJFileChooser(exts, targetPath+File.separator+
					"movieLQ."+exts[exts.length-1].getExtensions()[0]); 
			if(movFileName==null) return;
			else {
				try {
					JpgFileFilter jpgFileFilter = new JpgFileFilter("jpg");				
					String [] fileNames = getAllFileName(movieDir, jpgFileFilter);
					writeMovieAVI(new File(movFileName), fileNames, fps, movieQuality);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void writeMovieAVI(File file, String[] imageFileNames, int fps, float movieQuality) throws IOException {
			BufferedImage frame = ImageIO.read(new File(imageFileNames[0]));
			Format format = new Format(FormatKeys.MediaTypeKey, MediaType.VIDEO, //
					VideoFormatKeys.EncodingKey, VideoFormatKeys.ENCODING_AVI_MJPG,
					FormatKeys.FrameRateKey, new Rational(fps, 1),// 
					VideoFormatKeys.WidthKey, frame.getWidth(), //
					VideoFormatKeys.HeightKey, frame.getHeight(),//
					VideoFormatKeys.DepthKey, 24,
					QualityKey, movieQuality
					);
			AVIWriter out = null;
	        try {
	            out = new AVIWriter(file);

	            int track = out.addTrack(format);
				Buffer buf = new Buffer();
				buf.format = new Format(VideoFormatKeys.DataClassKey, BufferedImage.class);
				buf.sampleDuration = format.get(FormatKeys.FrameRateKey).inverse();
				for (int i = 0; i < imageFileNames.length; i++) {
					buf.data = ImageIO.read(new File(imageFileNames[i]));;
					out.write(track, buf);
				}
	        } finally {
	            if (out != null) { out.close(); }
	        }
		}
		
		public String [] getAllFileName(String path, JpgFileFilter jpgFileFilter) {
			File srcDir = new File(path);
			File[] files = srcDir.listFiles(jpgFileFilter);
			
			int k = 0;
			for (File mfile : files) {
				if (mfile.isDirectory()) {
				} else {
					k++;
				}
			}
			if(k==0) return null;
			String [] dir = new String[k];
			k = 0;
			for (File mfile : files) {
				if (mfile.isDirectory()) {
				} else {
					dir[k] = path+File.separator+mfile.getName();
					k++;
				}
			}
			return dir;
		}
		
		private final class JpgFileFilter implements FileFilter {
			private final String _fileType;

			private JpgFileFilter(String fileType)  {
				_fileType = fileType;
			}

			public boolean accept(File pathname) {
				return pathname.getName().endsWith("."+_fileType) || pathname.isDirectory();
			}
		}
	}
	
	
}
