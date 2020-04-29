package org.ucdm.launcher.dialog;

import static org.monte.media.VideoFormatKeys.QualityKey;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.monte.media.Buffer;
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.MovieWriter;
import org.monte.media.Registry;
import org.monte.media.VideoFormatKeys;
import org.monte.media.avi.AVIWriter;
import org.monte.media.math.Rational;

import org.ucdm.common.CommonDialog;
import org.ucdm.launcher.LauncherFrame;

public class MovieDialog  extends CommonDialog {
	private LauncherFrame 	_frame 		= null;

	JTextField 	 _sourceFolderTF 	= null;
	private int 	_iFormat		= 0;  //0-mov 1-avi
	private int 	_iQuality		= 0;  //0-low 1-high

	private String  		_cwd 				= null;
	private JTextField 		_opsTF 				= null;
	private JTextField 		_fpsTF 				= null;

	public MovieDialog(JFrame aParent, String aTitle, boolean modal, String cwd) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(900, 280);
		_frame 	= (LauncherFrame)aParent;
		_cwd 	= cwd;
	}

	protected JPanel createContents() { return new LoadPanel(_cwd);}

	private class LoadPanel extends JPanel {

		public LoadPanel(String srcPath) {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;

			String [] moduleString = null;
			int n = 0;

			JPanel modulePanel = null;	
			ButtonGroup moduleRadioGroup = null;
			JRadioButton [] moduleRadioButton = null;

			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

			_sourceFolderTF = new JTextField (srcPath);
			JButton ssBB = new JButton("Browse");
			ssBB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String fileName = _frame.openDirectoryUsingJFileChooser(_sourceFolderTF.getText()); 
					if(fileName==null) return;
					else {
						_sourceFolderTF.setText(fileName);
					}
				}
			}); 

			int iRow = 0;
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("Image File (jpg, png) Directory:"), gbc);

			iRow++;
			gbc= new GridBagConstraints(0, iRow, 3, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_sourceFolderTF, gbc);
			gbc= new GridBagConstraints(3, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(ssBB, gbc);

			_opsTF = new JTextField("1");
			_fpsTF = new JTextField("2");
			modulePanel = new JPanel( new GridLayout(2, 2, 5, 5));
			modulePanel.add(new JLabel("Moving Step: "));
			modulePanel.add(_opsTF);
			modulePanel.add(new JLabel("# Of Frames Per Second: "));
			modulePanel.add(_fpsTF);

			iRow++;			
			modulePanel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Movie:"));
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);

			moduleString = new String[] { new String( "Low"), new String( "High")};
			n = moduleString.length;

			modulePanel = new JPanel( new GridLayout(n, 1, 5, 5));	
			moduleRadioGroup = new ButtonGroup();
			moduleRadioButton = new JRadioButton[n];
			for(int i=0; i<n; i++) {
				final int j = i;
				moduleRadioButton[i] = new JRadioButton(moduleString[i], i==(_iQuality));
				moduleRadioButton[i].addItemListener( new ItemListener() {
					public void itemStateChanged(ItemEvent itemEvent) {
						if (itemEvent.getStateChange() == ItemEvent.SELECTED) { _iQuality = j; } 
					}
				});

				moduleRadioGroup.add(moduleRadioButton[i]);
				modulePanel.add(moduleRadioButton[i]);
			}

			loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			modulePanel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Quality:"));
			gbc= new GridBagConstraints(1, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);			

			moduleString = new String[]{new String( "AVI"), new String( "MOV")};
			n = moduleString.length;

			modulePanel = new JPanel( new GridLayout(2, 1, 5, 5));	
			moduleRadioGroup = new ButtonGroup();
			moduleRadioButton = new JRadioButton[n];
			for(int i=0; i<n; i++) {
				final int j = i;
				moduleRadioButton[i] = new JRadioButton(moduleString[i], i==(_iFormat));
				moduleRadioButton[i].addItemListener( new ItemListener() {
					public void itemStateChanged(ItemEvent itemEvent) {
						if (itemEvent.getStateChange() == ItemEvent.SELECTED) { _iFormat = j; } 
					}
				});

				moduleRadioGroup.add(moduleRadioButton[i]);
				modulePanel.add(moduleRadioButton[i]);
			}

			modulePanel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Format:"));
			gbc= new GridBagConstraints(2, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);


//			iRow++;
//			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0);
//			add(new JLabel(" "), gbc);
		}
	}

	protected boolean okAction() {
		String sourceDir = _sourceFolderTF.getText().trim();
		//System.out.println(sourceDir);
		File directory = new File(sourceDir);
		MyFileFilter myFileFilter = new MyFileFilter(new String[] {"jpg", "png"});
		File[] imageFiles = directory.listFiles(myFileFilter);

		int fps = Integer.parseInt(_fpsTF.getText().trim());

		FileNameExtensionFilter [] exts = null;
		if(_iFormat==0) exts = new FileNameExtensionFilter [] { new FileNameExtensionFilter("Movie (*.avi)","avi") };
		else exts = new FileNameExtensionFilter [] { new FileNameExtensionFilter("Movie (*.mov)","mov") };

		String movFileName = null;
		if(_iQuality==1) movFileName = _frame.saveFileUsingJFileChooser(exts, sourceDir+File.separator+
				"S_HQ."+exts[exts.length-1].getExtensions()[0]); 
		else movFileName = _frame.saveFileUsingJFileChooser(exts, sourceDir+File.separator+
				"S_LQ."+exts[exts.length-1].getExtensions()[0]);

		if(movFileName==null) return false;
		else {
			try {
				if(_iFormat==0) writeMovieAVI(new File(movFileName), imageFiles, fps, _iQuality==0?0.5f:1.0f);
				else writeMovieMOV(new File(movFileName), imageFiles, fps, _iQuality==0?0.5f:1.0f);
				_frame.setFolderMoviePath(sourceDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return true;
	}


	private void writeMovieAVI(File file, File[] imageFiles, int fps, float movieQuality) throws IOException {
		BufferedImage frame = ImageIO.read(imageFiles[0]);
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
			for (int i = 0; i < imageFiles.length; i++) {
				buf.data = ImageIO.read(imageFiles[i]);
				out.write(track, buf);
			}
		} finally {
			if (out != null) { out.close(); }
		}
	}

	private void writeMovieMOV(File file, File[] imageFiles, int fps, float movieQuality) throws IOException {
		BufferedImage frame = ImageIO.read(imageFiles[0]);
		MovieWriter out = Registry.getInstance().getWriter(file);
		Format format = new Format(FormatKeys.MediaTypeKey, MediaType.VIDEO, //
				VideoFormatKeys.EncodingKey, VideoFormatKeys.ENCODING_QUICKTIME_JPEG,
				FormatKeys.FrameRateKey, new Rational(fps, 1),// bigger the first int, faster
				VideoFormatKeys.WidthKey, frame.getWidth(), //
				VideoFormatKeys.HeightKey, frame.getHeight(),//
				VideoFormatKeys.DepthKey, 24, 
				QualityKey, movieQuality
				);


		int track = out.addTrack(format);
		try {
			Buffer buf = new Buffer();
			buf.format = new Format(VideoFormatKeys.DataClassKey, BufferedImage.class);
			buf.sampleDuration = format.get(FormatKeys.FrameRateKey).inverse();
			for (int i = 0; i < imageFiles.length; i++) {
				buf.data = ImageIO.read(imageFiles[i]);;
				out.write(track, buf);
			}
		} catch(javax.imageio.IIOException e) {

		} finally {
			out.close();
		}
	}


	private final class MyFileFilter implements FileFilter {
		private final String [] _fileType;

		private MyFileFilter(String [] fileType)  {
			_fileType = fileType;
		}

		public boolean accept(File pathname) {
			String fileName = pathname.getName();
			int index = fileName.indexOf('.');
			if(index<=0 || index>=fileName.length()-2) return false;

			String fileExtension = fileName.substring(index+1);
			for(int i=0; i<_fileType.length; i++) {
				if(fileExtension.equalsIgnoreCase(_fileType[i])) return true;
			}
			//return pathname.getName().endsWith("."+_fileType);
			return false;
		}
	}


}
