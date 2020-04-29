package com.geohammer.launcher.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.geohammer.common.CommonDialog;
import com.geohammer.common.CommonMouseEditingMode;
import com.geohammer.common.CommonPanel;
import com.geohammer.common.util.ColorUtils;
import com.geohammer.component.StatusBar;
import com.geohammer.launcher.LauncherFrame;
import com.geohammer.launcher.dialog.K51Dialog.K51Panel;
import com.geohammer.launcher.dialog.K51Dialog.K51Panel.MouseTrackInfoMode;
import com.geohammer.resource.Resource;

import edu.mines.jtk.awt.ColorMap;
import edu.mines.jtk.awt.ModeManager;
import edu.mines.jtk.mosaic.GridView;
import edu.mines.jtk.mosaic.Mosaic;
import edu.mines.jtk.mosaic.PointsView;

public class ColorGalleryDialog extends CommonDialog {

	LauncherFrame 		frame 			= null;
	int iMethod = 0;

	public ColorGalleryDialog(JFrame aParent, String aTitle, boolean modal, int iMethod) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(800, 800);
		this.frame 	= (LauncherFrame)aParent;
		this.iMethod = iMethod;
	}

	@Override
	protected JComponent createContents() {
		if(iMethod==0) {
			GalleryPanel galleryPanel = new GalleryPanel();
			return  galleryPanel;
		} else if(iMethod==1) {
			ColorUtilPanel colorUtilPanel = new ColorUtilPanel();
			return  colorUtilPanel;
		} 
		else return null;
	}
	@Override
	protected boolean okAction() { return true; }
	@Override
	protected JComponent getCommandRow() { return null; }

	private class ColorUtilPanel extends JPanel {

		public ColorUtilPanel() {
			
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 3, 5);
			GridBagConstraints gbc;

			int iRow = 0;
			JPanel jpanel = null;
			
			int m = 10;
			int n = 10;
			jpanel = new JPanel();
			jpanel.setLayout(new GridLayout(m, n, 2, 2));
			for(int i=0, k=0; i<m; i++) {
				for(int j=0; j<n; j++) {
					Color c = ColorUtils.getColor(k++);
					JLabel label = new JLabel(k+"");
					label.setOpaque(true);
					label.setBackground(c);
					jpanel.add(label);
				}
			}
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(jpanel, gbc);
			
			iRow++;
			Color[] cs = ColorUtils.KELLY_COLORS;
			m = 2;
			n = cs.length/m;
			jpanel = new JPanel();
			jpanel.setLayout(new GridLayout(m, n, 2, 2));
			for(int i=0; i<cs.length; i++) {
				JLabel label = new JLabel("kelly"+i);
				label.setOpaque(true);
				label.setBackground(cs[i]);
				jpanel.add(label);
			}
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(jpanel, gbc);
			
			iRow++;
			int ncolors = 20;
			cs = ColorUtils.generateVisuallyDistinctColors(ncolors, 0, 1);
			m = 2;
			n = ncolors/m;
			jpanel = new JPanel();
			jpanel.setLayout(new GridLayout(m, n, 2, 2));
			for(int i=0; i<cs.length; i++) {
				JLabel label = new JLabel("Dist"+i);
				label.setOpaque(true);
				label.setBackground(cs[i]);
				jpanel.add(label);
			}
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(jpanel, gbc);
		}
	}

	private class GalleryPanel extends JPanel {

		public GalleryPanel() {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 3, 5);
			GridBagConstraints gbc;

			int iRow = 0;		

			int w = 2*256;
			int h = 30;
			BufferedImage bufferedImage = null;
			String htmlTextTime = null;
									
			gbc= new GridBagConstraints(0, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			htmlTextTime = "<html> <font size=\"4\" color=\"black\"><b> GRAY </b></font>";
			add(new JLabel(htmlTextTime, JLabel.RIGHT), gbc);
			bufferedImage = createIndexedImage(w, h, ColorMap.GRAY);
			gbc= new GridBagConstraints(1, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel(new ImageIcon(bufferedImage)), gbc);
			
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			htmlTextTime = "<html> <font size=\"4\" color=\"black\"><b> Jet </b></font>";
			add(new JLabel(htmlTextTime, JLabel.RIGHT), gbc);
			bufferedImage = createIndexedImage(w, h, ColorMap.JET);
			gbc= new GridBagConstraints(1, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel(new ImageIcon(bufferedImage)), gbc);

			iRow++;						
			gbc= new GridBagConstraints(0, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			htmlTextTime = "<html> <font size=\"4\" color=\"black\"><b> GMT_JET </b></font>";
			add(new JLabel(htmlTextTime, JLabel.RIGHT), gbc);
			bufferedImage = createIndexedImage(w, h, ColorMap.GMT_JET);
			gbc= new GridBagConstraints(1, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel(new ImageIcon(bufferedImage)), gbc);
			
			iRow++;						
			gbc= new GridBagConstraints(0, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			htmlTextTime = "<html> <font size=\"4\" color=\"black\"><b> HUE </b></font>";
			add(new JLabel(htmlTextTime, JLabel.RIGHT), gbc);
			bufferedImage = createIndexedImage(w, h, ColorMap.HUE);
			gbc= new GridBagConstraints(1, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel(new ImageIcon(bufferedImage)), gbc);
			
			iRow++;						
			gbc= new GridBagConstraints(0, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			htmlTextTime = "<html> <font size=\"4\" color=\"black\"><b> HUE_RED_TO_BLUE </b></font>";
			add(new JLabel(htmlTextTime, JLabel.RIGHT), gbc);
			bufferedImage = createIndexedImage(w, h, ColorMap.HUE_RED_TO_BLUE);
			gbc= new GridBagConstraints(1, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel(new ImageIcon(bufferedImage)), gbc);
			
			iRow++;						
			gbc= new GridBagConstraints(0, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			htmlTextTime = "<html> <font size=\"4\" color=\"black\"><b> HUE_BLUE_TO_RED </b></font>";
			add(new JLabel(htmlTextTime, JLabel.RIGHT), gbc);
			bufferedImage = createIndexedImage(w, h, ColorMap.HUE_BLUE_TO_RED);
			gbc= new GridBagConstraints(1, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel(new ImageIcon(bufferedImage)), gbc);
			
			iRow++;						
			gbc= new GridBagConstraints(0, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			htmlTextTime = "<html> <font size=\"4\" color=\"black\"><b> PRISM </b></font>";
			add(new JLabel(htmlTextTime, JLabel.RIGHT), gbc);
			bufferedImage = createIndexedImage(w, h, ColorMap.PRISM);
			gbc= new GridBagConstraints(1, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel(new ImageIcon(bufferedImage)), gbc);
			
			iRow++;						
			gbc= new GridBagConstraints(0, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			htmlTextTime = "<html> <font size=\"4\" color=\"black\"><b> RED_WHITE_BLUE </b></font>";
			add(new JLabel(htmlTextTime, JLabel.RIGHT), gbc);
			bufferedImage = createIndexedImage(w, h, ColorMap.RED_WHITE_BLUE);
			gbc= new GridBagConstraints(1, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel(new ImageIcon(bufferedImage)), gbc);
			
			iRow++;						
			gbc= new GridBagConstraints(0, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			htmlTextTime = "<html> <font size=\"4\" color=\"black\"><b> BLUE_WHITE_RED </b></font>";
			add(new JLabel(htmlTextTime, JLabel.RIGHT), gbc);
			bufferedImage = createIndexedImage(w, h, ColorMap.BLUE_WHITE_RED);
			gbc= new GridBagConstraints(1, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel(new ImageIcon(bufferedImage)), gbc);			

			iRow++;						
			gbc= new GridBagConstraints(0, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			htmlTextTime = "<html> <font size=\"4\" color=\"black\"><b> BLUE_WHITE_WHITE_RED </b></font>";
			add(new JLabel(htmlTextTime, JLabel.RIGHT), gbc);
			bufferedImage = createIndexedImage(w, h, ColorMap.BLUE_WHITE_WHITE_RED);
			gbc= new GridBagConstraints(1, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel(new ImageIcon(bufferedImage)), gbc);
			
			iRow++;						
			gbc= new GridBagConstraints(0, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			htmlTextTime = "<html> <font size=\"4\" color=\"black\"><b> GRAY_YELLOW_RED </b></font>";
			add(new JLabel(htmlTextTime, JLabel.RIGHT), gbc);
			bufferedImage = createIndexedImage(w, h, ColorMap.GRAY_YELLOW_RED);
			gbc= new GridBagConstraints(1, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel(new ImageIcon(bufferedImage)), gbc);
			
			iRow++;						
			gbc= new GridBagConstraints(0, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			htmlTextTime = "<html> <font size=\"4\" color=\"black\"><b> SPECTRAL </b></font>";
			add(new JLabel(htmlTextTime, JLabel.RIGHT), gbc);
			bufferedImage = createIndexedImage(w, h, ColorMap.SPECTRAL);
			gbc= new GridBagConstraints(1, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel(new ImageIcon(bufferedImage)), gbc);
			
			iRow++;						
			gbc= new GridBagConstraints(0, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			htmlTextTime = "<html> <font size=\"4\" color=\"black\"><b> PLUS_MINUS </b></font>";
			add(new JLabel(htmlTextTime, JLabel.RIGHT), gbc);
			bufferedImage = createIndexedImage(w, h, ColorMap.PLUS_MINUS);
			gbc= new GridBagConstraints(1, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel(new ImageIcon(bufferedImage)), gbc);
			
		}

		protected BufferedImage createIndexedImage(int w, int h, IndexColorModel icm) {
			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED, icm);

			int mapSize = icm.getMapSize();
			int width = w / mapSize;
			//System.out.println("mapSize="+mapSize+" with="+width);

			WritableRaster wr = img.getRaster();
			for (int i = 0; i < mapSize; i++) {
				for (int y = 0; y < h; y++) {
					for (int x = 0; x < width; x++) {
						wr.setSample(i * width + x, y, 0, i);
					}
				}
			}
			return img;
		}

	}
}

