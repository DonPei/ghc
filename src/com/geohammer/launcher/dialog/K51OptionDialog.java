package org.ucdm.launcher.dialog;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.ucdm.common.CommonDialog;
import org.ucdm.launcher.LauncherFrame;
import org.ucdm.launcher.dialog.K51Dialog.K51Panel;

public class K51OptionDialog  extends CommonDialog {

	LauncherFrame 		_frame 			= null;
	K51Option 			_k51Option 		= null;
	K51Panel 			_k51Panel 		= null;

	//JTextField 			_nTF			= null;
	JTextField 			_x1TF			= null;
	JTextField 			_x2TF			= null;	
	JTextField 			_a1TF			= null;
	JTextField 			_a2TF			= null;
	
	JLabel 				_trackingLabel	= new JLabel(" ", JLabel.CENTER);

	public K51OptionDialog(JFrame aParent, String aTitle, boolean modal, 
			K51Panel k51Panel, K51Option k51Option) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(650, 300);
		setEnableApplyButton(true);
		_frame		= (LauncherFrame)aParent;
		_k51Option	= k51Option;
		_k51Panel 	= k51Panel;
	}

	protected JPanel createContents() {
		return new K51OptionPanel();
	}
	protected boolean okAction() {
		getInput();

		if(_k51Panel!=null) _k51Panel.updateDialog();
		return true;
	}

	private class K51OptionPanel extends JPanel {

		public K51OptionPanel() {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 3, 5);
			GridBagConstraints gbc;

			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			Font myFont = new Font("SansSerif", Font.PLAIN, 12);
			Color myColor = Color.BLUE;

			int n = 0;
			JPanel modulePanel = null;
			String [] moduleString = null;
			ButtonGroup moduleRadioGroup = null;
			JRadioButton [] moduleRadioButton = null;

			//step 1 extract
			int iRow = 0;
			modulePanel = new JPanel	( new GridLayout(1, 3, 2, 2));
			_x1TF = new JTextField(_k51Option.getX1()+"");
			_x2TF = new JTextField(_k51Option.getX2()+"");
			modulePanel.add(_x1TF); 
			modulePanel.add(new JLabel("<=   X   <=", JLabel.CENTER)); 
			modulePanel.add(_x2TF); 

			modulePanel.setBorder(BorderFactory.createTitledBorder(null, "Domain ", 
					TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, myFont, myColor));
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);

			//step 2 a
			iRow++;
			modulePanel = new JPanel(new GridLayout(3, 2, 2, 2));

			_a1TF = new JTextField(_k51Option.getA1()+"");
			_a2TF = new JTextField(_k51Option.getA2()+"");

			JPanel jPanel = new JPanel	( new GridLayout(1, 3, 2, 2));
			jPanel.add(_a1TF); 
			jPanel.add(new JLabel("<=   A   <=", JLabel.CENTER)); 
			jPanel.add(_a2TF);
			modulePanel.add(jPanel);
			
			JSlider hJSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
			hJSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					JSlider jSlider = (JSlider)e.getSource();
					int v = jSlider.getValue();
					float a1 = Float.parseFloat(_a1TF.getText().trim());
					float a2 = Float.parseFloat(_a2TF.getText().trim());
					float a = a1+(a2-a1)*(v-0)/(100-0);
					_k51Option.setA1(a1);
					_k51Option.setA2(a2);
					_k51Option.setA(a);
					_k51Panel.updateDialog();
					
					float b = ((int)(a*100.0f))/100.0f;
					String htmlText =  "<html>" + "<font size=\"4\" color=\"black\"><b>a="+ b +
							" &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp "+b+" &lt= X &lt= " +(b+4)+"</b></font>  ";
					_trackingLabel.setText(htmlText);
					//System.out.println(value+" "+zoomX);
				}
			});
			hJSlider.setMinorTickSpacing(5);
			hJSlider.setMajorTickSpacing(10);
			hJSlider.setPaintTicks(false);
			hJSlider.setPaintLabels(false);
			modulePanel.add(hJSlider);
			
			modulePanel.add(_trackingLabel);
			
			modulePanel.setBorder(BorderFactory.createTitledBorder(null, "Set A (a <= X <= a+4)", 
					TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, myFont, myColor));
			gbc= new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			add(modulePanel, gbc);	
		}
	}	

	public void getInput() {		
		//_k51Option.setNPoints(Integer.parseInt(_nTF.getText().trim()));
		_k51Option.setX1(Float.parseFloat(_x1TF.getText().trim()));
		_k51Option.setX2(Float.parseFloat(_x2TF.getText().trim()));

		_k51Option.setA1(Float.parseFloat(_a1TF.getText().trim()));
		_k51Option.setA2(Float.parseFloat(_a2TF.getText().trim()));
	}

	

}
