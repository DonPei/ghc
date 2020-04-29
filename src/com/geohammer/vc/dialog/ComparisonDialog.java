package com.geohammer.vc.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import com.geohammer.common.CommonDialog;
import com.geohammer.common.CommonFrame;
import com.geohammer.core.planarmodel.DipLayer1D;
import com.geohammer.core.planarmodel.FlatLayer1D;
import com.geohammer.vc.VcFrame;

public class ComparisonDialog extends CommonDialog {

	private CommonFrame _frame = null;
	private String _fileName = "";
	private JTextField _fileNameTF = null;
	DipLayer1D _dipLayer1D0 = null;
	DipLayer1D _dipLayer1D1 = null;

	String[] _label = new String[] { "Vp", "Vs", "Vp/Vs", "Density", "Delta", "Epsilon", "Gamma" };
	boolean[] _selected = new boolean[_label.length];

	public ComparisonDialog(JFrame aParent, String aTitle, boolean modal) {
		super(aParent, aTitle, modal);
		setDialogWindowSize(800, 250);
		_frame = (CommonFrame) aParent;
	}

	public void setFileName(String fileName) {
		_fileName = fileName;
	}

	protected JPanel createContents() {
		JPanel jPanel = new JPanel(new GridBagLayout());
		Insets insets = new Insets(5, 5, 5, 5);
		GridBagConstraints gbc = null;

		_fileNameTF = new JTextField(_fileName);
		JButton jButton = new JButton("Browse");
		jButton.setToolTipText("Browse file");
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String inputFileName = _fileNameTF.getText();
				String ext = FilenameUtils.getExtension(inputFileName);
				FileNameExtensionFilter exts[] = null;
				if(ext==null||ext.isEmpty()) {
					exts = new FileNameExtensionFilter[] {
							new FileNameExtensionFilter("file (*.csv)", "csv"),
							new FileNameExtensionFilter("file (*.xml)", "xml")};
					inputFileName += ".csv";
				} else {
					exts = new FileNameExtensionFilter[] {
						new FileNameExtensionFilter("file (*" + "." + ext + ")", ext) };
				}

				String fileName = _frame.openFileUsingJFileChooser(exts, inputFileName);
				if (fileName == null) return;
				else {
					_fileNameTF.setText(fileName);
				}
			}
		});

		int iRow = 0;
		gbc = new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets,
				0, 0);
		jPanel.add(new JLabel("Load a Velocity Model (against model in memory):"), gbc);

		iRow++;
		gbc = new GridBagConstraints(0, iRow, 4, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets,
				0, 0);
		jPanel.add(_fileNameTF, gbc);
		gbc = new GridBagConstraints(4, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets,
				0, 0);
		jPanel.add(jButton, gbc);

		iRow++;
		gbc = new GridBagConstraints(0, iRow, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets,
				0, 0);
		jPanel.add(new JLabel("Attribute: "), gbc);

		int n = _label.length;
		for (int i = 0; i < n; i++)
			_selected[i] = false;
		_selected[0] = true;

		JPanel jPanel1 = new JPanel(new GridLayout(1, n));
		JCheckBox[] jCheckBox = new JCheckBox[n];
		for (int i = 0; i < n; i++) {
			final int j = i;
			jCheckBox[i] = new JCheckBox(_label[i], false);
			jCheckBox[i].setSelected(_selected[i]);
			jPanel1.add(jCheckBox[i]);
			jCheckBox[i].addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					int state = itemEvent.getStateChange();
					if (state == ItemEvent.SELECTED)
						_selected[j] = true;
					else
						_selected[j] = false;
				}
			});
		}

		gbc = new GridBagConstraints(1, iRow, 3, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets,
				0, 0);
		jPanel.add(jPanel1, gbc);

		return jPanel;
	}

	// protected JComponent getCommandRow() { return null; }
	protected boolean okAction() {
		VcFrame frame = (VcFrame) _frame;
		_dipLayer1D0 = frame.getProject().getDipLayer1D();
		String mdlFileName = _fileNameTF.getText().trim();
		if (mdlFileName.contains("mdl.xml")) {
			MdlXml mdlXml = new MdlXml(mdlFileName);
			// System.out.println(mdlXml.toString());
			_dipLayer1D1 = mdlXml.toDipLayer1D();
			// System.out.println(dlayer1D.toString());
		} else if (mdlFileName.contains("par.xml")) {
			MdlXml mdlXml = new MdlXml(mdlFileName);
			_dipLayer1D1 = mdlXml.toDipLayer1D();
			// System.out.println(dipLayer1D.toString());
		} else {
			_dipLayer1D1 = new DipLayer1D(mdlFileName);
		}

		plotSection();

		return true;

	}

	public double[] calLayerProperty(int id, FlatLayer1D init) {
		int n = init.getNumOfBoundaries();
		double[] vel = new double[n];
		for (int i = 0; i < n; i++) {
			vel[i] = init.getLayer(i).calLayerProperty(id);
		}
		return vel;
	}

	private double[][] getModelParameters(boolean isDepth, FlatLayer1D init) {
		if (init == null)
			return null;
		for (int i = 0; i < init.getNumOfBoundaries(); i++) {
			init.getLayer(i).toCij(1.0, 1.0);
		}

		double[][] initData = new double[][] { init.getTop(), init.getLayerProperty(1), init.getLayerProperty(2),
				init.getLayerProperty(3), init.getLayerProperty(4), init.getLayerProperty(21),
				init.getLayerProperty(20), init.getLayerProperty(22)};
		if (!isDepth) return initData;

		int nBoundary = init.getNumOfBoundaries();
		double[][] data = new double[initData.length][2 * (nBoundary - 1)];
		for (int i = 0; i < nBoundary - 1; i++) {
			int k = 2 * i;

			data[0][k] = initData[0][i];
			data[0][k + 1] = initData[0][i + 1];

			for (int j = 1; j <initData.length; j++) {
				data[j][k] = initData[j][i];
				data[j][k + 1] = initData[j][i];
			}
		}
		return data;
	}
	
	private float [] getColumn(int index, double[][] initData) {
		double [] col = initData[index];
		float [] v = new float[col.length];
		for(int i=0; i<col.length; i++) v[i] = (float)col[i];
		return v;		
	}

	private void plotSection() {

		double[][] initData = getModelParameters(true, (FlatLayer1D) _dipLayer1D1);
		double[][] memoryData = getModelParameters(true, (FlatLayer1D) _dipLayer1D0);

		String aTitle = new String("Depth Section");
		String catalog = "Curve";
		String unit = "";
		if(_frame.getProject().getIUnit()==1) 		unit = " (m"; 
		else if(_frame.getProject().getIUnit()==2) 	unit = " (ft"; 
		else if(_frame.getProject().getIUnit()==3) 	unit = " (km"; 
		
		final String vLabel = "Depth"+unit+")";
		String hLabel = "Final(-) Initial(--)";
		
		if(_selected[0]||_selected[1]) hLabel = "Velocity"+unit+"/s)";
		else if(_selected[4]||_selected[5]||_selected[6]) hLabel = "Anisotropy";
		else  hLabel = "Vp/Vs";
		
		int k = 0;
		for(int i=0; i<_selected.length; i++) {
			if(_selected[i]) k++;
		}
		
		float [] initDp = getColumn(0, initData);
		float [] memoryDp = getColumn(0, memoryData);
		
		float [][] curves = new float[4*k][];
		k = 0;
		for(int i=0; i<_selected.length; i++) {
			if(_selected[i]) {
				curves[k++] = initDp;
				curves[k++] = getColumn(i+1, initData);
				curves[k++] = memoryDp;
				curves[k++] = getColumn(i+1, memoryData);
			}
		}
		
		PlotDialog dialog = new PlotDialog(_frame, aTitle, false, null, 150, true, catalog, hLabel, vLabel);
		//PlotDialog dialog = new PlotDialog(1, curves.length/4, _frame, aTitle, false, null, 151, true, catalog, hLabel, vLabel);
		dialog.setCurves(curves);
		dialog.showDialog();
	}

}
