package com.geohammer.vc.run;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.geohammer.common.CommonDialog;
import com.geohammer.common.CommonFrame;
import com.geohammer.common.CommonMouseEditingMode;
import com.geohammer.common.CommonPanel;
import com.geohammer.common.util.UiUtil;
import com.geohammer.core.planarmodel.FlatLayer1D;
import com.geohammer.common.TextViewDialog;

import edu.mines.jtk.mosaic.PointsView;
import edu.mines.jtk.util.ArrayMath;

public class RmsPanel  extends CommonPanel {
	CommonFrame 	_frame 		= null;
	FlatLayer1D 	_layer1D 	= null;

	boolean 		_vpOn 		= true;
	boolean 		_vsOn 		= true;

	boolean 		_deltaOn 	= true;
	boolean 		_epsilonOn 	= true;
	boolean 		_gammaOn 	= true;

	double [] 		_y 			= null;
	double [][] 	_rms 		= null;

	boolean 		_duplicated = false;
	public RmsPanel(CommonFrame frame, int orientation, int axis, int nPoints, double [][] rms) {
		super(1,1,CommonPanel.Orientation.X1RIGHT_X2UP, CommonPanel.AxesPlacement.LEFT_BOTTOM, null);
		
		setBackground(Color.white);
		
		Font font = new Font ("Arial", Font.BOLD, 12); //Monospaced, Serif, Dialog, Sanserif
		setFont(font);
		
		_frame = frame;
		init();
		
		if(rms==null) {
			_y 			= new double[nPoints];
			update(new double [][] {_y}, 1, "True", null);
		} else {
			update(rms, 2, "Trial", null);

			double minClip = 0.0;
			double maxClip = ArrayMath.max(rms);
			if(minClip>maxClip) {
				minClip = maxClip;
				maxClip = 0.0;
			}
			setVLimits(0, minClip, 1000.0*maxClip);
		}
	}

	public boolean isDuplicated() { return _duplicated; }
	public void setDuplicated(boolean duplicated) { _duplicated = duplicated; }
	
	private float[][] getData(double [][] rms) {

		int k=0; 
		for(int i=0; i<rms[0].length; i++) {
			if(rms[0][i]!=-99999.0) k++;
		}
		float [] x = new float[k];
		float [] y = new float[k];
		if(rms.length==1) {
			for(int i=0; i<x.length; i++) {
				x[i] = i+1;
				y[i] = (float)rms[0][i]*1000;
			}
		} else {
			for(int i=0; i<x.length; i++) {
				x[i] = (float)rms[0][i]*-1;
				y[i] = (float)rms[1][i]*1000;
			}
		}

		return new float[][] {x, y};
	}

	public void update(double [][] rms, int iType, String name, String imageFileName) {
		_rms = rms;
		float size = 12;
		setVLabel(0, "Rms (ms)");
		setHLabel(0, "Iteration #");

		float [][] data = getData(rms);

		PointsView pv = new PointsView(data[0], data[1]);
		pv.setName("Vel", "vp "+name);
		if(iType==1) pv.setStyle("r-");
		else pv.setStyle("b-");
		pv.setMarkSize(size);
		removeView(pv.getCategory(), pv.getName());
		addPointsView(0, 0, pv);
		
		if(imageFileName!=null) saveImage(imageFileName);
	}
	private void saveImage(String imageFileName) {
//		int x2 = getWorld().getTile(0, 0).getX();
//		int y2 = getWorld().getTile(0, 0).getY();
//		int w2 = getWorld().getTile(0, 0).getWidth();
//		int h2 = getWorld().getTile(0, 0).getHeight();
		//System.out.println("x2="+x2+" y2="+y2+" w2="+w2+" h2="+h2);
		
		//double dip = 96.0;
		//double width = w1/dip;
		//_frame.paintLayerViewToImage(pngFileName);
		BufferedImage image = paintToImage(getWidth());
		int w = image.getWidth();
		int h = image.getHeight();
//		int imageWidth	= w2;
//		int imageHeight	= h;
		//System.out.println("imageW="+image.getWidth()+" imageH="+image.getHeight());
		//BufferedImage subImage = image.getSubimage(0, 0, imageWidth, imageHeight);
		BufferedImage subImage = image.getSubimage(0, 0, w, h);
		//_images[iItem] = subImage;
		
		File f = new File(imageFileName);
		try {
			ImageIO.write(subImage, "JPEG", f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//_frame.paintToPng(dip, width, pngFileName); 
		//_frame.refresh();
	}
	
	public void init() {
		//setEnableDrawLine(false);
		setEnableTracking(true);
		setEnableEditing(true);
		setEnableZoom(true);
		
		addModeManager();
		CommonMouseEditingMode mouseEditingMode = getMouseEditingMode();
		mouseEditingMode.setShowAutoAxisLimit(false);
		mouseEditingMode.setJComponent(genJMenuItem());
	}
	
	private JMenuItem[] genJMenuItem() {
		
		JMenuItem jMenuItem1 = new JMenuItem("Duplicate Plot");		
		jMenuItem1.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent e)   {
				CommonFrame frame = new CommonFrame();
				frame.setLayout(new BorderLayout());
				RmsPanel rmsPanel = new RmsPanel(frame, 1, 1, 1, _rms);
				rmsPanel.setDuplicated(true);
			    frame.add(rmsPanel, BorderLayout.CENTER);
			    frame.setSize(600, 800);
			    frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});		
		
		JMenuItem jMenuItem2 = new JMenuItem("View/Save RMS Data");		
		jMenuItem2.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent e)   {
				double [] y = null;
				if(_rms.length==1) {
					y = _rms[0];
				} else {
					y = _rms[1];
				}
				
				String text = " ";
				for(int i=0; i<y.length; i++) {
					text += y[i]+" ";
					if(i%10==0) text += "\n";
				}
				//if(_frame==null) _frame = (JFrame)(getParent().getParent().getParent().getParent().getParent().getParent().getParent());
				TextViewDialog dialog = new TextViewDialog(_frame, "RMS Data", false, text);
				dialog.showDialog();
			}
		});
		
		return new JMenuItem[] { jMenuItem1, jMenuItem2};
	}
	
	public class RmsAxisDialog extends CommonDialog {
		
		private JTextField 	_minClipTF 	= null;
		private JTextField 	_maxClipTF 	= null;

		public RmsAxisDialog(JFrame aParent, String aTitle, int iAuto) {
			this(aParent, aTitle, iAuto, false);
		}
		public RmsAxisDialog(JFrame aParent, String aTitle, int iAuto, boolean modal) {
			super(aParent, aTitle, modal);
			setDialogWindowSize(800, 800);

			double v = getTile(0, 0).getVerticalProjector().v1();
			_minClipTF = new JTextField (v+" ");
			v = getTile(0, 0).getVerticalProjector().v0();
			_maxClipTF = new JTextField (v+" ");
			
			if(iAuto==1) setAxis(1);
		}
		
		protected boolean okAction() {	return true;	}
		protected JPanel createContents() {
			JPanel panel = new JPanel();
			panel.setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			Insets insets1 = new Insets(20, 5, 5, 5);
			GridBagConstraints gbc = null;
			
			
			gbc= new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.BOTH, insets, 0, 0);
			panel.add(new JLabel("Minimum Of Vertical Axis:",Label.RIGHT), gbc);
			gbc= new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			panel.add(_minClipTF, gbc);

			gbc= new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.BOTH, insets1, 0, 0);
			panel.add(new JLabel("Maximum Of Vertical Axis:",Label.RIGHT), gbc);
			gbc= new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			panel.add(_maxClipTF, gbc);
			
			gbc= new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
					GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			panel.add(new JLabel(" "), gbc);
			
			return panel;
		}
		
		public void setAxis(int iAuto) {
			if(iAuto==0) {
				double minClip = Double.parseDouble(_minClipTF.getText().trim());
				double maxClip = Double.parseDouble(_maxClipTF.getText().trim());
				if(minClip>maxClip) {
					JOptionPane.showMessageDialog( null, "Minimum value Must be less than the maximum value!", 
							"Error", JOptionPane.ERROR_MESSAGE );
					return;
				}
				setVLimits(0, minClip, maxClip);
			} else {
				setVLimitsDefault();
			}
		}

		protected JComponent getCommandRow() {
			JButton ok = new JButton("OK");
			ok.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					setAxis(0);
					dispose();
				}
			});
			JButton auto = new JButton("Auto Set");
			auto.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					setAxis(1);
					dispose();
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
			buttons.add( auto );
			buttons.add( ok );
			buttons.add( cancel );
			return UiUtil.getCommandRow( buttons );
		}

	}
}
