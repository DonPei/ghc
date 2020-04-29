package com.geohammer.vc.run;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.geohammer.common.CommonDialog;
import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.FlatLayer1D;

public class SaPlot extends CommonDialog {
	SaDialog 		_saDialog 	= null;
	VelPanel 		_velPanel 	= null;
	RmsPanel 		_rmsPanel 	= null;

	int 			_iType 		= 0;
	int 			_nPoints 	= 0;
	FlatLayer1D 	_layer1D   	= null;

	public SaPlot(SaDialog aParent, boolean modal, int iType , String aTitle, FlatLayer1D layer1D, VCPair vcPW, int nPoints) {
		super(aParent.getFrame(), aTitle, modal);
		setDialogWindowSize(800, 800);
		_saDialog 	= (SaDialog)aParent;
		_iType  	= iType;
		_nPoints  	= nPoints;
		_layer1D  	= layer1D;
	}
	protected JComponent getCommandRow() { return null; }
	protected boolean okAction() {	return true;	}
	protected JPanel createContents() {
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		_rmsPanel = new RmsPanel(_saDialog.getFrame(), 1, 1, _nPoints, null);
		
		boolean vsOn = _iType<10;
		boolean gammaOn = vsOn;
		_velPanel = new VelPanel(_layer1D, true, vsOn, true, true, gammaOn);

		panel.setLayout(new GridBagLayout());
		Insets insets = new Insets(1, 1, 1, 1);
		GridBagConstraints gbc;
		gbc= new GridBagConstraints(0, 0, 1, 1, 1.0, 0.7, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		panel.add(_velPanel, gbc);
		gbc= new GridBagConstraints(0, 1, 1, 1, 1.0, 0.3, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		panel.add(_rmsPanel, gbc);
		
		return panel;
	}
	
	public void update(FlatLayer1D layer1D, VCPair vcPW, int index, double [][] rms, String [] imageFileName) {
		if(_iType==0||_iType==1||_iType==2) {
			_velPanel.update(layer1D, 2, "Trial");
			//_anisoPanel.update(layer1D, 2, "Trial");
		} else {
			_velPanel.update(layer1D, 2, "Trial");
		}
		if(rms!=null) {
			_rmsPanel.update(rms, 2, "Trial", null);
			if(index>=0) {
				if(index==imageFileName.length-1) saveImage(imageFileName[index]);
				else {
					if(index%3==0) saveImage(imageFileName[index]);
					else imageFileName[index] = null;
				}
			}
		}
		
	}
	private void saveImage2(String imageFileName) {
//		int x2 = getWorld().getTile(0, 0).getX();
//		int y2 = getWorld().getTile(0, 0).getY();
//		int w2 = getWorld().getTile(0, 0).getWidth();
//		int h2 = getWorld().getTile(0, 0).getHeight();
		//System.out.println("x2="+x2+" y2="+y2+" w2="+w2+" h2="+h2);
		
		//double dip = 96.0;
		//double width = w1/dip;
		//_frame.paintLayerViewToImage(pngFileName);
		BufferedImage image = _rmsPanel.paintToImage(_rmsPanel.getWidth());
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
	private void saveImage(String imageFileName) {
//		int x2 = getWorld().getTile(0, 0).getX();
//		int y2 = getWorld().getTile(0, 0).getY();
//		int w2 = getWorld().getTile(0, 0).getWidth();
//		int h2 = getWorld().getTile(0, 0).getHeight();
		//System.out.println("x2="+x2+" y2="+y2+" w2="+w2+" h2="+h2);
		
		//double dip = 96.0;
		//double width = w1/dip;
		//_frame.paintLayerViewToImage(pngFileName);

		BufferedImage image1 = _velPanel.paintToImage(_velPanel.getWidth());
		int w1 = image1.getWidth();
		int h1 = image1.getHeight();
		BufferedImage image2 = _rmsPanel.paintToImage(_rmsPanel.getWidth());
		int w2 = image2.getWidth();
		int h2 = image2.getHeight();
		
		int w = w1>w2?w1:w2;
		int h = h1+h2;
		
		BufferedImage image = new BufferedImage(w, h+5, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = (Graphics2D)image.getGraphics();
		g2d.setBackground(Color.white);
		g2d.clearRect(0, 0, w, h+5);
		g2d.drawImage(image1, 0, 0, w, h1, null);
		g2d.drawImage(image2, 0, h1+5, w, h2, null);
//		int imageWidth	= w2;
//		int imageHeight	= h;
		//System.out.println("imageW="+image.getWidth()+" imageH="+image.getHeight());
		//BufferedImage subImage = image.getSubimage(0, 0, imageWidth, imageHeight);
		//BufferedImage subImage = image.getSubimage(0, 0, w, h);
		//_images[iItem] = subImage;
		
		File f = new File(imageFileName);
		try {
			ImageIO.write(image, "JPEG", f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//_frame.paintToPng(dip, width, pngFileName); 
		//_frame.refresh();
	}

	@Override
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

			int w = (int)(6.0*gcBounds.getWidth() / 10.0);
			int h = (int)(8.0*gcBounds.getHeight() / 10.0);
			//int h = (int)(w/2);
			//int h = (int)(gcBounds.getHeight()-40.0);
			//setSize((int)(gcBounds.getWidth()-10), (int)(gcBounds.getHeight()/2));
			if(_iType==0||_iType==1||_iType==2) {
				setSize(w, h);
			} else {
				int s = w<h?w:h;
				setSize(s, s);
			}
			//setSize(1000, 800);

			setLocationRelativeTo(null);
		}
	}
}
