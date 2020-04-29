package org.ucdm.launcher.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.ucdm.common.CommonDialog;
import org.ucdm.fd3d.option.SourceOption;
import org.ucdm.launcher.LauncherFrame;

import edu.mines.jtk.util.RandomFloat;

public class ManipulateMTDialog extends CommonDialog {
	LauncherFrame 		_frame 			= null;
	
	private JTextField _srcMechCombineTF 	= null;
	private int 		_isFileEvent 		= 0;
	private int 		_iFunction 			= 0;
	
	private String [] _rowLabel 		= null;
	private double [][] _minMaxValues 	= null;
	private JTextField [][]	_minMaxTF 	= null;
	private int []		_iDistributions = null; // 0 uniform 1 gaussian
	
	private JTextField 	_numOfEventsTF 	= null;
	private RandomFloat _randomFloat 	= new RandomFloat();
	private int 		_iVelModel	 	= 0; //0 moment tensor 1 velocity model
	private int 		_iUnit 			= 1; // 1 meter 2 feet
	
	private double [][] _p1p2Values 	= null;
	private JTextField [][]	_p1p2TF 	= null; //[min][p_northing]	
	
	public ManipulateMTDialog(JFrame aParent, String aTitle, boolean modal, int iVelModel) {
		super(aParent, aTitle, modal);
		if(iVelModel==0) {
			setDialogWindowSize(750, 500);
		} else {
			setDialogWindowSize(750, 600);
		}
		setOKButtonLabel("Save As...");
		
		_frame 		= (LauncherFrame)aParent;
		_iVelModel 	= iVelModel;
		if(iVelModel==0) {			
			_rowLabel 		= new String[] { "Strike(0 - 360) degree", "Dip(0 - 90) degree", 
					"Rake(-180 - 180) degree", "Moment(1.0E5 - 1.0E8) N-M",
					"Alpha Angle (-45 - 45) degree", "Keppa(lamda/mu>=-2/3)", "Peak Frequency (Hz)"};
			_minMaxValues 	= new double[][] { {10, 80}, {0, 90}, {-180, 180}, {100000, 100000000}, 
					{-45, 45}, {-0.5, 0.8}, {100, 400} };
			//_iDistributions = new int[]{0, 0, 0, 0, 0, 0, 0}; // 0 uniform 1 gaussian			
		} else if (iVelModel==1) {
			_rowLabel 		= new String[] { "Vp", "Vs", "Density (Kg/m^3)", "Delta", "Epsilon", "Gamma", "Qp", "Qs"};
			_minMaxValues 	= new double[][] { {2000, 6000}, {1500, 4000}, {2000, 2300}, {0.0, 0.2}, 
					{0, 0.3}, {0, 0.3}, {1.0E9, 1.0E9}, {1.0E9, 1.0E9} };
			
			_p1p2Values 	= new double[][] { {0, 0, 0}, {1500, 1500, 2000} };
			_p1p2TF 		= new JTextField[_p1p2Values.length][_p1p2Values[0].length];
		}
		
		_iDistributions = new int[_rowLabel.length];
		if (iVelModel==1) for(int i=0; i<_iDistributions.length; i++) _iDistributions[i] = 2;
		_minMaxTF 	= new JTextField[_minMaxValues.length][_minMaxValues[0].length];
	}

	protected JPanel createContents() {
		JPanel innerPanel = new JPanel();

		innerPanel.setLayout(new GridBagLayout());
		Insets insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gbc= null;

		gbc= new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0);

		String cwd = null;
		if(_iVelModel==0) {
			cwd  = _frame.getSyntheticMTCwd();			
		} else {
			cwd  = _frame.getSyntheticVelCwd();
		}
		LoadFilePanel loadFilePanel = new LoadFilePanel(cwd);
		innerPanel.add(loadFilePanel, gbc);

		return innerPanel;
	}
	
	private double getRandomNumber(int iRow, int numOfEvents, int iLayer) {
		double min = _minMaxValues[iRow][0];
		double max = _minMaxValues[iRow][1];
		double diff = max - min;
		if(diff==0) return max;		
	    
		if(_iDistributions[iRow]==0) {
			return min + diff * (_randomFloat.uniform());
		} else if(_iDistributions[iRow]==1) {
			double aMean = 0.5*(max+min); 
		    double aVariance = diff/3;
		    return aMean + _randomFloat.normal() * aVariance;
		}  else  { //linear increase
			double dv = (max-min)/(numOfEvents-1); 
		    return min + dv * iLayer;
		}
	}
	
	protected boolean okAction() {		
		int numOfEvents = Integer.parseInt(_numOfEventsTF.getText().trim());
		
		if(_iVelModel==0) {
			if(numOfEvents<1) {
				String message = "Error: Number of events should >= 1";
				String title = "Error";
				JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} else {
			if(numOfEvents<2) {
				String message = "Error: Number of layers should >= 2";
				String title = "Error";
				JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		for(int i=0; i<_minMaxTF.length; i++) {
			for(int j=0; j<_minMaxTF[i].length; j++) {
				_minMaxValues[i][j] = Double.parseDouble(_minMaxTF[i][j].getText().trim());
				//System.out.println(_minMaxValues[i][j]+"");
			}
			double diff = _minMaxValues[i][1] - _minMaxValues[i][0];
			if(diff<0) {
				String message = "Error: Max < min for "+_rowLabel[i];
				String title = "Error";
				JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}

		double [][] data = new double[numOfEvents][];
		if(_iVelModel==0) {
			SourceOption sourceOption = new SourceOption(null, null);
			double freq = 0;
			for(int i=0; i<numOfEvents; i++) { 
				int iRow = 0;
				sourceOption.setStrike(getRandomNumber(iRow++, numOfEvents, i));
				sourceOption.setDip(getRandomNumber(iRow++, numOfEvents, i));
				sourceOption.setRake(getRandomNumber(iRow++, numOfEvents, i));
				sourceOption.setMagnitude(getRandomNumber(iRow++, numOfEvents, i));
				sourceOption.setAlpha(getRandomNumber(iRow++, numOfEvents, i));
				sourceOption.setK(getRandomNumber(iRow++, numOfEvents, i));
				freq = getRandomNumber(iRow++, numOfEvents, i);
				sourceOption.angleToMomentTensor();
				double [][] mt = sourceOption.getMomentTensor();

				data[i] = new double[]{freq, mt[0][0], mt[1][1], mt[2][2], mt[0][1], mt[0][2], mt[1][2]};
			}
			String columnName = new String("Peak Frequency(Hz), M11(N-M), M22, M33, M12, M13, M23");
			saveToCsv(columnName, data);	
		} else {
			int nLayers = numOfEvents;
			for(int i=0; i<nLayers; i++) { 
				int iRow = 0;
				double vp = getRandomNumber(iRow++, nLayers, i);
				double vs = getRandomNumber(iRow++, nLayers, i);
				double den = getRandomNumber(iRow++, nLayers, i);
				double delta = getRandomNumber(iRow++, nLayers, i);
				double epsilon = getRandomNumber(iRow++, nLayers, i);
				double gamma = getRandomNumber(iRow++, nLayers, i);
				double qp = getRandomNumber(iRow++, nLayers, i);
				double qs = getRandomNumber(iRow++, nLayers, i);				

				data[i] = new double[]{vp, vs, den, delta, epsilon, gamma, qp, qs};
			}
			
			for(int i=0; i<_p1p2TF.length; i++) {
				for(int j=0; j<_p1p2TF[i].length; j++) {
					_p1p2Values[i][j] = Double.parseDouble(_p1p2TF[i][j].getText().trim());
					//System.out.println(_minMaxValues[i][j]+"");
				}
				double diff = _p1p2Values[i][1] - _p1p2Values[i][0];
				if(diff<0) {
					String message = "Error: Max < min for cube size";
					String title = "Error";
					JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
			
			String columnName = new String("Northing/Strike_Degree, Easting/Dip_Degree, TVD/None,"+
			"Vp/X0, Vs/Nx, Density_kg_m3/Dx, Delta/Y0, Epsilon/Ny, Gamma/Dy, Qp/Nz, Qs/unit_1_m_2_ft");
			
			saveToCsv(columnName, data);
		}
		return true;
	}
	
	public void saveToCsv(String columnName, double[][] data)	{
		String selectedFileName = null;
		if(_iVelModel==0) selectedFileName = _frame.getSyntheticMTCwd();
		else selectedFileName = _frame.getSyntheticVelCwd();
		String ext = "csv";
		FileNameExtensionFilter exts[] = new FileNameExtensionFilter [] { new FileNameExtensionFilter("CSV (*.csv)", ext) }; 		
		
		String fileName = _frame.saveFileUsingJFileChooser(exts, selectedFileName); 
		if(fileName==null) return;
		else {
			String selectedExt = FilenameUtils.getExtension(fileName);
			if(selectedExt==null||selectedExt.isEmpty()) fileName = fileName+"."+ext;			
			saveToCsv(columnName, data, fileName);	
			
			if(_iVelModel==0) _frame.setSyntheticMTCwd(fileName);
			else _frame.setSyntheticVelCwd(fileName);
			_frame.savePreferences();
		}
	}
	public boolean saveToCsv(String columnName, double [][] data, String selectedFile ) {
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFile, false));
			bufferedWriter.write(columnName); bufferedWriter.newLine();
			
			if(_iVelModel==0) {
				for(int i=0; i<data.length; i++) {
					bufferedWriter.write(data[i][0]+"");
					for(int j=1; j<data[i].length; j++) bufferedWriter.write(", "+data[i][j]);
					bufferedWriter.newLine();
				} 
			} else {
				int nLayers = data.length;
				double x0 = _p1p2Values[0][0];
				double x1 = _p1p2Values[1][0];
				double dx = 50.0;
				int nx = (int)((x1-x0)/dx)+1;
				double y0 = _p1p2Values[0][1];
				double y1 = _p1p2Values[1][1];
				double dy = 50.0;
				int ny = (int)((y1-y0)/dy)+1;
				double z0 = _p1p2Values[0][2];
				double z1 = _p1p2Values[1][2];
				double dz = (z1-z0)/(nLayers-1);
				int nz = (int)((z1-z0)/dz)+1;
				bufferedWriter.write("0, 0, 0, "+x0+", "+nx+", "+dx+", "+y0+", "+ny+", "+dy+", "+nz+", "+_iUnit);
				bufferedWriter.newLine();
				
				for(int i=0; i<nLayers; i++) { 
					bufferedWriter.write("0, 0, "+(z0+i*dz));
					for(int j=0; j<data[i].length; j++) bufferedWriter.write(", "+data[i][j]);
					System.out.println("l="+data[i].length);
					bufferedWriter.newLine();
				}
			}
			
			bufferedWriter.close();
		} catch (IOException ioexception) {
			ioexception.printStackTrace();
		}
		return true;
	}
	private class LoadFilePanel extends JPanel {

		public LoadFilePanel(String srcMechCombineCwd) {
			setLayout(new GridBagLayout());
			Insets insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gbc;

			String [] moduleString = null;
			JPanel modulePanel = null;
			String label = null;
			
			int nRow = _minMaxTF.length+1;
			int nCol = _minMaxTF[0].length+2;			
			modulePanel = new JPanel( new GridLayout(nRow, nCol, 2, 2));

			for(int i=0; i<_minMaxTF.length; i++) {
				for(int j=0; j<_minMaxTF[i].length; j++) {
					_minMaxTF[i][j] = new JTextField (_minMaxValues[i][j]+"");
				}
			}
			
			String [] distribution = new String[]{"Uniform", "Gaussian"};
			if(_iVelModel==0) distribution = new String[]{"Uniform", "Gaussian"};
			else distribution = new String[]{"Uniform", "Gaussian", "Linear-Increase"};
			JComboBox [] jComboBoxs = new JComboBox[_iDistributions.length];
			for(int i=0; i<jComboBoxs.length; i++) {
				final int j = i;
				//jComboBoxs[i]= new JComboBox(distribution[_iDistributions[i]]);
				jComboBoxs[i]= new JComboBox(distribution);
				//jComboBoxs[i].setToolTipText("");
				jComboBoxs[i].setSelectedIndex(_iDistributions[i]);
				//_minCombo.setEnabled(false);
				jComboBoxs[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JComboBox jcmbType = (JComboBox) e.getSource();
						_iDistributions[j] = jcmbType.getSelectedIndex();
						//String percentage = (String) jcmbType.getSelectedItem();
					}
				});
			}
			
			moduleString = new String[]{" ", "Minimun", "Maximum", "Distribution"};			
			
			for(int i=0; i<nRow; i++) {
				for(int j=0; j<nCol; j++) {
					if(i==0) modulePanel.add(new JLabel(moduleString[j], JLabel.CENTER));
					else {
						if(j==0) modulePanel.add(new JLabel(_rowLabel[i-1], JLabel.LEFT));
						else if(j==nCol-1) modulePanel.add(jComboBoxs[i-1]);
						else modulePanel.add(_minMaxTF[i-1][j-1]);
					}
				}
			}
			

			if(_iVelModel==0) label = "Parameters to generate moment tensor matrix:";
			else label = "Parameters to generate a velocity model:";
			Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
			modulePanel.setBorder(BorderFactory.createTitledBorder(loweredetched, label));
			int iRow = 0;
			gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, insets, 0, 0);
			add(modulePanel, gbc);
			
			if(_iVelModel==0) {
				label = "Number Of Synthetic Events(>0):";
				_numOfEventsTF = new JTextField ("100");
			} else {
				label = "Number Of Synthetic Layers(>=2):";
				_numOfEventsTF = new JTextField ("2");
			}
			iRow++;
			gbc= new GridBagConstraints(0, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, insets, 0, 0);
			add(new JLabel(label, JLabel.LEFT), gbc);			
			
			gbc= new GridBagConstraints(1, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, insets, 0, 0);
			add(_numOfEventsTF, gbc);
			
			if(_iVelModel==0) {
			} else {
				gbc= new GridBagConstraints(2, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, insets, 0, 0);
				add(new JLabel("Unit:", JLabel.RIGHT), gbc);
				
				ButtonGroup moduleRadioGroup = null;
				JRadioButton [] moduleRadioButton = null;
				moduleString = new String[]{"meters", "feet"};
				int n = moduleString.length;
				moduleRadioGroup = new ButtonGroup();
				moduleRadioButton = new JRadioButton[n];
				for(int i=0; i<n; i++) {
					final int j = i;
					moduleRadioButton[i] = new JRadioButton(moduleString[i], _iUnit==(i+1));
					moduleRadioButton[i].addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							if(((JRadioButton)event.getSource()).isSelected()) _iUnit = (j+1); 
						}
					});
					moduleRadioGroup.add(moduleRadioButton[i]);
					gbc= new GridBagConstraints(3+i, iRow, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, insets, 0, 0);
					add(moduleRadioButton[i], gbc);
				}
				
				nRow = _p1p2TF.length+1;
				nCol = _p1p2TF[0].length+1;			
				modulePanel = new JPanel( new GridLayout(nRow, nCol, 2, 2));
				for(int i=0; i<_p1p2TF.length; i++) {
					for(int j=0; j<_p1p2TF[i].length; j++) {
						_p1p2TF[i][j] = new JTextField (_p1p2Values[i][j]+"");
					}
				}
				
				moduleString = new String[]{"Cube size", "Northing", "Easting", "TVD"};	
				String [] labelString = new String[]{"Minimum", "Maximum"};
				for(int i=0; i<nRow; i++) {
					for(int j=0; j<nCol; j++) {
						if(i==0) modulePanel.add(new JLabel(moduleString[j], JLabel.CENTER));
						else {
							if(j==0) modulePanel.add(new JLabel(labelString[i-1], JLabel.LEFT));
							else modulePanel.add(_p1p2TF[i-1][j-1]);
						}
					}
				}
				modulePanel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Model Cube Size"));
				iRow++;
				gbc= new GridBagConstraints(0, iRow, 5, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, insets, 0, 0);
				add(modulePanel, gbc);				
			}
		}
		
	}
	
}
