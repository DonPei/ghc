package org.ucdm.launcher.dialog;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.ucdm.common.CommonFrame;
import org.ucdm.common.CommonProject;
import org.ucdm.launcher.LauncherFrame;
import org.ucdm.soniclog.LasTree;
import org.ucdm.common.util.UiUtil;

import edu.mines.jtk.mosaic.IPanel;
import edu.mines.jtk.mosaic.Mosaic;


public class LasViewerDialog  extends JDialog {
	LauncherFrame 		_frame 		= null;
	public JTextField _srcTF 		= null;
	private boolean _lasOnly 		= true;
	private int _iMethod 			= 0; // 0- las viewer 1-arithmetic operation

	public LasViewerDialog(JFrame aParent, String aTitle) {
		this(aParent, aTitle, 0, false, true);
	}

	public LasViewerDialog(JFrame aParent, String aTitle, int iMethod, boolean modal, boolean lasOnly) {
		super(aParent, aTitle, modal);
		_frame 	= (LauncherFrame)aParent;
		_lasOnly = lasOnly;
		_iMethod = iMethod;
	}


	public final void showDialog(){
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setResizable(true);
		addCancelByEscapeKey();

		JPanel panel = new JPanel();
		panel.setLayout( new BorderLayout() );
		panel.setBorder( UiUtil.getStandardBorder() );

		//		Font font = new Font("Serif", Font.PLAIN, 60);
		//		panel.setFont(font);

		JPanel jc = createContents();
		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		jc.setBorder(loweredetched);

		String tipsText = "<html> Read and view SEG-2 seismograms " +
				"<UL>" +
				"  <LI>SEG-2 specifications at <FONT COLOR=BLUE><B>http://www.seg.org/documents/10161/77915/seg_2.pdf </B></FONT>   " +
				"  <LI>Ctrl+C and Ctrl+V to copy and paste file directory from OS" +
				"</UL>";

		JLabel fancyTipsLabel = new JLabel(tipsText, UIManager.getIcon("OptionPane.informationIcon"), JLabel.CENTER);
		fancyTipsLabel.setBorder(BorderFactory.createTitledBorder("Tips"));


		//panel.add(fancyTipsLabel, BorderLayout.NORTH);
		panel.add(jc, BorderLayout.CENTER);
		panel.add( getCommandRow(), BorderLayout.SOUTH );

		getContentPane().add( panel );

		UiUtil.centerOnParentAndShow( this );
		setSize(700, 190);
		setVisible(true);
	}

	private JPanel createContents() {
		JPanel innerPanel = new JPanel();

		innerPanel.setLayout(new GridBagLayout());
		Insets insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gbc= null;

		gbc= new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		innerPanel.add(new LoadFilePanel(_frame.getLasViewerCwd()), gbc);
		return innerPanel;
	}

	protected void okAction() {
		applyAction();
		dispose();
	}

	protected void applyAction() {
		String srcFileName = _srcTF.getText().trim();
		if(srcFileName==null||srcFileName.isEmpty()) { return; }

		if(_iMethod==0) {
			//Create and set up the window.
			CommonFrameInstant frame = new CommonFrameInstant("LAS Log Viewer");
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			//Create and set up the content pane.
			LasTree newContentPane = new LasTree(frame, srcFileName, _lasOnly);
			newContentPane.setOpaque(true); //content panes must be opaque
			frame.setContentPane(newContentPane);

			//Display the window.
			frame.pack();
			frame.setVisible(true);
			frame.setSize(1000, 800);
		} else if(_iMethod==1) {
			LasArithmeticDialog dialog = new LasArithmeticDialog(_frame, "Log Arithmetic", srcFileName, true);
			dialog.showDialog();
		}
		
		_frame.setLasViewerCwd(srcFileName);
		_frame.savePreferences();
	}
	/**
	 * Return a standardized row of command buttons, right-justified and 
	 * all of the same size, with OK as the default button, and no mnemonics used, 
	 * as per the Java Look and Feel guidelines. 
	 */
	private JComponent getCommandRow() {
		JButton ok = new JButton("OK");
		ok.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				okAction();
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
		buttons.add( ok );
		buttons.add( cancel );
		return UiUtil.getCommandRow( buttons );
	}
	/**
	 * Force the escape key to call the same action as pressing the Cancel button.
	 *
	 * <P>This does not always work. See class comment.
	 */
	private void addCancelByEscapeKey(){
		String CANCEL_ACTION_KEY = "CANCEL_ACTION_KEY";
		int noModifiers = 0;
		KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, noModifiers, false);
		InputMap inputMap = this.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.put(escapeKey, CANCEL_ACTION_KEY);
		AbstractAction cancelAction = new AbstractAction(){
			public void actionPerformed(ActionEvent e){
				dispose();
			}
		}; 
		this.getRootPane().getActionMap().put(CANCEL_ACTION_KEY, cancelAction);
	}
	private class CommonFrameInstant extends CommonFrame {
		public CommonFrameInstant(String title) { super(title); }
		public CommonProject getProject() {return null;}
		public IPanel getBaseWorld() {return null;}
		public Mosaic getMosaic() {return null;}
	}

	private class LoadFilePanel extends JPanel implements ActionListener {

		private JButton 	_srcBrowserButton = null;

		public LoadFilePanel(String srcCwd) {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;

			_srcTF = new JTextField (srcCwd, 5);

			_srcBrowserButton = new JButton("Browse"); 
			_srcBrowserButton.setToolTipText("Browse a LAS log file");
			_srcBrowserButton.addActionListener(this);

			int iRow = 0;
			gbc= new GridBagConstraints(0, iRow++, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(new JLabel("Select A LAS File:"), gbc);
			gbc= new GridBagConstraints(0, iRow, 2, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_srcTF, gbc);
			gbc= new GridBagConstraints(2, iRow++, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(_srcBrowserButton, gbc);
		}

		public void actionPerformed(ActionEvent actionevent) {
			if(actionevent.getSource() == _srcBrowserButton) 	{ 
				String fileName = _srcTF.getText().trim();
				if(fileName.isEmpty()) {
					fileName = System.getProperty("user.dir");
				} 

				FileNameExtensionFilter [] exts = new FileNameExtensionFilter [] { 
						new FileNameExtensionFilter("LAS (*.las)", "las") }; 

				String selectedFileName = null;
				if(_frame!=null) { 	
					selectedFileName = _frame.openFileUsingJFileChooser(exts, fileName); 
				}

				if(selectedFileName==null) return;
				else { _srcTF.setText(selectedFileName.trim()); }
			} 
		}	

	}

}

