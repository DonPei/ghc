package com.geohammer.vc.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.geohammer.common.CommonDialog;
import com.geohammer.vc.VcFrame;

public class RayTracerDialog extends CommonDialog {
	private VcFrame 	frame 		= null;

	JTextField 	rayCodeTF 			= null;
	JTextField 	psCodeTF 			= null;
	JTextField 	tolTF 				= null;
	double 		tol 				= 0.1;
	int 		iWave 				= 1;
	int 		iVTI 				= 0;
	int 		iR 					= 1;
	int 		iT0 				= 1;
	int 		iApprox 			= 1;
	int []		rayCodes 			= null;
	int 		nBoundaries			= 1;
	
	public RayTracerDialog(JFrame aParent, String aTitle, boolean modal) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(700, 500);
		setEnableApplyButton(true);
		frame 		= (VcFrame)aParent;
		if(frame.getProject().is2D()) nBoundaries = frame.getProject().getLayer2D().getNumOfBoundaries();
		else nBoundaries = frame.getProject().getLayer3D().getNumOfBoundaries();
		if(frame.getRayTracer()==null) frame.newRayTracer();
	}
	
	protected boolean okAction() {	
		String line = rayCodeTF.getText();
		if(line.contains(".")) { 
			JOptionPane.showMessageDialog(frame, "\nRay code must be integers!", 
					"Error", JOptionPane.ERROR_MESSAGE); 
			return false;
		}
		
		StringTokenizer st = new StringTokenizer(line, " ,\t");
		
		int [] rayCodes = null;
		if(iWave==3&&st.countTokens()>0) {
			rayCodes = new int [st.countTokens()];
			int iMax = -1;
			int iMin = 10000;
			for(int i=0; i<rayCodes.length; i++) {
				rayCodes[i] = Integer.parseInt(st.nextToken().trim());
				iMax = iMax>rayCodes[i] ? iMax:rayCodes[i];
				iMin = iMin<rayCodes[i] ? iMin:rayCodes[i];
			}
			if(iMin<0) return false;
			if(iMax>nBoundaries-1) return false;
		}
		
		line = psCodeTF.getText();
		if(line.contains(".")) { 
			JOptionPane.showMessageDialog(frame, "\nWave code must be integers!", 
					"Error", JOptionPane.ERROR_MESSAGE); 
			return false;	
		}
		
		st = new StringTokenizer(line, " ,\t");
		
		int [] psCodes = null;
		if(iWave==3&&st.countTokens()>1) {
			psCodes = new int [st.countTokens()];
			for(int i=0; i<psCodes.length; i++) {
				psCodes[i] = Integer.parseInt(st.nextToken().trim());
				if(psCodes[i]==1||psCodes[i]==2||psCodes[i]==3) {
				} else return false;
			}
			if(psCodes.length<=rayCodes.length) { 
				JOptionPane.showMessageDialog(frame, 
						"\nWave code should have one more element than ray code!", 
						"Error", JOptionPane.ERROR_MESSAGE); 
				return false;	
			}
		}
		
		if(iWave==3&&rayCodes==null) iWave = 1;
		
		frame.getRayTracer().setIWave(iWave);
		if(iWave==3) 	{
			frame.getRayTracer().setReflectorIndex(rayCodes);
			frame.getRayTracer().setPsIndex(psCodes);
		} else 			{
			frame.getRayTracer().setReflectorIndex(null);
			frame.getRayTracer().setPsIndex(null);
		}

		tol = Double.parseDouble(tolTF.getText().trim());
		frame.getRayTracer().setIVTI(iVTI);
		frame.getRayTracer().setIT0(iT0);
		frame.getRayTracer().setIR(iR);
		frame.getRayTracer().setTolerance(tol);
		frame.getRayTracer().setIApproximation(iApprox);

		frame.getRayTracer().setConfiguration();
		frame.startRayTracing(1);		

		//_frame.setIVp(_iVp);
		//_frame.updateWorld();
//		if(_frame.getRayTracer()!=null) _frame.getRayTracer().setIVp(iVp);
		
		//System.out.println("iT0="+_iT0+"\n"+vcPW.toString());
		//System.out.println(vcPW.toString(false, 13));
		//_frame.updateTime(0);
		return true;	
	}
	
	protected JPanel createContents() {	
		iVTI = frame.getRayTracer().getIVTI();
		iT0 = frame.getRayTracer().getIT0();
		iR = frame.getRayTracer().getIR();
		tol = frame.getRayTracer().getTolerance();
		iApprox = frame.getRayTracer().getIApproximation();
		
		int [] rayCodes = frame.getRayTracer().getReflectorIndex();
		int [] psCodes = frame.getRayTracer().getPsIndex();
		
		JPanel jPanel = new JPanel(new BorderLayout());
		
		jPanel.setLayout(new GridBagLayout());
		Insets insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gbc;

		if(rayCodes==null) 	rayCodeTF = new JTextField ("1");
		else {
			String line = " "+rayCodes[0];
			for(int i=1; i<rayCodes.length; i++) line = new String(line.concat(", "+rayCodes[i]));
			rayCodeTF = new JTextField (line);
		}
		rayCodeTF.setEnabled(iWave==3);
		
		if(psCodes==null) 	psCodeTF = new JTextField ("1, 1");
		else {
			String line = " "+psCodes[0];
			for(int i=1; i<psCodes.length; i++) line = new String(line.concat(", "+psCodes[i]));
			psCodeTF = new JTextField (line);
		}
		psCodeTF.setEnabled(iWave==3);
		
		String [] moduleString = new String[] { "Direct Arrivals", 
				"First Arrivals (including possible head waves)", "Reflected And Multiple Waves"
		};
		int n = moduleString.length;
		ButtonGroup moduleRadioGroup = new ButtonGroup();
		JRadioButton [] moduleRadioButton = new JRadioButton[n];

		for(int i=0; i<n; i++) {
			final int j = i;
			moduleRadioButton[i] = new JRadioButton(moduleString[i], iWave==(i+1) );
			moduleRadioButton[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					iWave = (j+1); 
					rayCodeTF.setEnabled(iWave==3);
					psCodeTF.setEnabled(iWave==3);
				}
			});
			moduleRadioGroup.add(moduleRadioButton[i]);
			gbc= new GridBagConstraints(i%2, i/2, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			jPanel.add(moduleRadioButton[i], gbc);
		}

		int iRow = 2;
		gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(rayCodeTF, gbc);
		gbc= new GridBagConstraints(1, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(new JLabel("Reflector Index (very top interface is 0)"), gbc);
		iRow++;
		gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(psCodeTF, gbc);
		gbc= new GridBagConstraints(1, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(new JLabel("Wave Mode (=1 P, =2 fast S (SH), =3 slow S (SV)"), gbc);
		
		iRow++;
		gbc= new GridBagConstraints(0, iRow, 4, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);

		moduleString = new String[] { "ISO Ray Tracer", "VTI Ray Tracer" };
		n = moduleString.length;
		moduleRadioGroup = new ButtonGroup();
		moduleRadioButton = new JRadioButton[n];
		iRow++;
		for(int i=0; i<n; i++) {
			final int j = i;
			moduleRadioButton[i] = new JRadioButton(moduleString[i], iVTI==i );
			moduleRadioButton[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					iVTI = j; 
				}
			});
			moduleRadioGroup.add(moduleRadioButton[i]);
			gbc= new GridBagConstraints(i, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			jPanel.add(moduleRadioButton[i], gbc);
		}
		
		iRow++;
		gbc= new GridBagConstraints(0, iRow, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);

		tolTF = new JTextField (tol+"");
		iRow++;
		gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(tolTF, gbc);
		gbc= new GridBagConstraints(1, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(new JLabel("Ray Searching Stop Limit"), gbc);			

		iRow++;
		gbc= new GridBagConstraints(0, iRow, 4, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
		
		moduleString = new String[] { "Do Not Apply The Origin Time (T0)", "Apply The Origin Time (T0)" };
		n = moduleString.length;
		moduleRadioGroup = new ButtonGroup();
		moduleRadioButton = new JRadioButton[n];
		iRow++;
		for(int i=0; i<n; i++) {
			final int j = i;
			moduleRadioButton[i] = new JRadioButton(moduleString[i], iT0>0 );
			moduleRadioButton[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					iT0 = j; 
					if(j==1) iT0 = 111;
				}
			});
			moduleRadioGroup.add(moduleRadioButton[i]);
			gbc= new GridBagConstraints(i, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
			jPanel.add(moduleRadioButton[i], gbc);
		}
		
		iRow++;
		gbc= new GridBagConstraints(0, iRow, 4, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
		
		JCheckBox checkBox = new JCheckBox("Calculate Ray Path", iR==1 );
		checkBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (((JCheckBox)event.getSource()).isSelected()) { iR = 1; }
				else {iR = 0; }
			}
		});

		iRow++;
		gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);
		jPanel.add(checkBox, gbc);
		
		return jPanel;
	}

}
