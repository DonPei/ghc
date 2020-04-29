package com.geohammer.vc.dialog;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.geohammer.common.CommonDialog;
import com.geohammer.common.CommonFrame;
import com.geohammer.common.CommonMouseEditingMode;
import com.geohammer.common.CommonPointPanel;
import com.geohammer.core.acquisition.VCPair;
import com.geohammer.core.planarmodel.DipLayer1D;

public class PlotDialog extends CommonDialog {
	
	private CommonFrame 		_frame 		= null;
	private CommonPointPanel 	_plot 		= null;
	private DipLayer1D 			_dipLayer1D = null;
	public VCPair 				_vcPW 		= null;
	private String 				_title 		= null;
	
	private int 				_iH 		= 0;
	private int 				_iV 		= 1;
	
	public float [][] 			_curves		= null;
	
	public double 				_dv0 		= 0.0;
	public double 				_dv1 		= 0.0;
	public double 				_dh0 		= 0.0;
	public double 				_dh1 		= 0.0;
	
	public PlotDialog(JFrame aParent, String aTitle, boolean modal, DipLayer1D dipLayer1D,
			int iType, boolean isDepthPlot, String catalog, String hLabel, String vLabel) {
		this(1, 1, aParent, aTitle, modal, dipLayer1D,
				iType, isDepthPlot, catalog, hLabel, vLabel);
	}
	public PlotDialog(int nRow, int nCol, JFrame aParent, String aTitle, boolean modal, DipLayer1D dipLayer1D,
			int iType, boolean isDepthPlot, String catalog, String hLabel, String vLabel) {
		super(aParent, aTitle, modal);
		setBackground(Color.white);
		setDialogWindowSize(800, 800);		
		_frame 		= (CommonFrame)aParent;
		_dipLayer1D = dipLayer1D;
		_title 		= aTitle;
		
		_plot 		= new CommonPointPanel(nRow, nCol, iType, isDepthPlot, catalog, hLabel, vLabel);
		//_plot.setBorder(new EmptyBorder(3, 1, 3, 100));
		_plot.setBackground(Color.white);
		_plot.setFrame(_frame);		

		_plot.setEnableTracking(false);
		_plot.setEnableEditing(true);
		_plot.setEnableZoom(true);
		
		_plot.addModeManager();
		CommonMouseEditingMode mouseEditingMode = _plot.getMouseEditingMode();
		//mouseEditingMode.setShowAutoAxisLimit(false);
		mouseEditingMode.setShowManualAxisLimit(true);
		mouseEditingMode.setJComponent(genJMenuItem());	
	}
	
	public void setIH(int iH) 					{ _iH = iH; }
	public void setIV(int iV) 					{ _iV = iV; }
	public void setVcPair(VCPair vcPW) 			{ _vcPW = vcPW; }
	public void setCurves(float [][] curves) 	{ _curves = curves; }
	public void setAdjustAxislimit(double dv0, double dv1, double dh0, double dh1) {
		_dv0 = dv0; _dv1 = dv1; _dh0 = dh0; _dh1 = dh1;
	}	
	
	public CommonPointPanel getCommonPointPanel() { return _plot; }
	protected JComponent getCommandRow() { return null; }
	protected boolean okAction() {	return true;	}
	protected JPanel createContentsOver100() {		
		int iType = _plot.getIType();
		if(iType<100) return null;
		
		float [] hValue = null;
		float [] vValue = null;
		String hLabel = null;
		String vLabel = null; 
		String style = null;
		String name = null;
		float markSize =12.0f;
		float lineWidth = 2.0f;
		
		if(iType==100||iType==101) {
			if(iType==100) setDialogWindowSize(800, 500);
			else if(iType==101) setDialogWindowSize(400, 900);
			lineWidth = 1.0f;
			String [] styles = new String[]{"r-", "g-", "b-"};
			int n = _curves.length/2;
			for(int i=0; i<n; i++) {
				name = "Wavelet"+i; 	style = styles[i]; 		vValue = _curves[2*i]; hValue = _curves[2*i+1];
				_plot.add(hValue, vValue, 0, hLabel, vLabel, style, name, markSize, lineWidth);
				//_plot.adjustAxislimit(0.02, 0.02, 0.0, 0.0);				
				_plot.adjustAxislimit(_dv0, _dv1, _dh0, _dh1);
			}
		} else if(iType==110) {
			setDialogWindowSize(600, 600);
			lineWidth = 1.0f;
			String [] styles = new String[]{"ro", "go", "bo"};
			int n = _curves.length/2;
			for(int i=0; i<n; i++) {
				name = "Data"+i; 	style = styles[i]; 		vValue = _curves[2*i]; hValue = _curves[2*i+1];
				_plot.add(hValue, vValue, 0, hLabel, vLabel, style, name, markSize, lineWidth);
				_plot.adjustAxislimit(0.02, 0.02, 0.02, 0.02);
			}
		} else if(iType==120||iType==121||iType==122||iType==123) {
			setDialogWindowSize(800, 800);
			lineWidth = 1.0f;
			name = "Curve"+iType;  			hValue = _curves[0]; vValue = _curves[1];
			if(iType==123) {
				name = "Curve"+iType+"_1"; 	style = "k-";
				_plot.add(hValue, vValue, 0, hLabel, vLabel, style, name, markSize, lineWidth);
				name = "Curve"+iType+"_2"; 	style = "g-"; hValue = _curves[2]; vValue = _curves[3];
				_plot.add(hValue, vValue, 0, hLabel, vLabel, style, name, markSize, lineWidth);
				name = "Curve"+iType+"_3"; 	style = "b-"; hValue = _curves[4]; vValue = _curves[5];
				_plot.add(hValue, vValue, 0, hLabel, vLabel, style, name, markSize, lineWidth);

			} else {
				if(iType==120) style = "k-";
				else if(iType==121) style = "g-";
				else if(iType==122) style = "b-";
				_plot.add(hValue, vValue, 0, hLabel, vLabel, style, name, markSize, lineWidth);
			}
			_plot.adjustAxislimit(0.05, 0.05, 0.05, 0.05);
		} else if(iType==130) {
			setDialogWindowSize(600, 600);
			lineWidth = 2.5f;
			String [] styles = new String[]{"r-", "g-", "b-"};
			int n = _curves.length/2;
			for(int i=0; i<n; i++) {
				name = "Curve"+i; 	style = styles[i]; 		vValue = _curves[2*i]; hValue = _curves[2*i+1];
				_plot.add(hValue, vValue, 0, hLabel, vLabel, style, name, markSize, lineWidth);
				_plot.adjustAxislimit(0.02, 0.05, 0.01, 0.02);
			}
		} else if(iType==140) {
			setDialogWindowSize(600, 600);
			lineWidth = 2.5f;
			String [] styles = new String[]{"r-", "g-", "b-"};
			int n = _curves.length/2;
			for(int i=0; i<n; i++) {
				name = "Curve"+i; 		vValue = _curves[2*i]; hValue = _curves[2*i+1];
				_plot.add(hValue, vValue, 0, hLabel, vLabel, "bo", name, markSize, lineWidth);
				_plot.adjustAxislimit(0.02, 0.05, 0.01, 0.02);
			}
		} else if(iType==141) {
			setDialogWindowSize(600, 600);
			lineWidth = 2.5f;
			String [] styles = new String[]{"r-", "g-", "b-"};
			int n = _curves.length/2;
			for(int i=0; i<n; i++) {
				name = "Curve"+i; 		vValue = _curves[2*i]; hValue = _curves[2*i+1];
				_plot.add(hValue, vValue, 0, hLabel, vLabel, "r-", name, markSize, lineWidth);
				_plot.adjustAxislimit(0.02, 0.05, 0.01, 0.02);
			}
		} else if(iType==150) {
			setDialogWindowSize(600, 600);
			lineWidth = 2.5f;
			String [] styles = new String[]{"r--", "r-", "g--", "g-", "b--", "b-", "k--", "k-",
					"r--", "r-", "g--", "g-", "b--", "b-", "k--", "k-"};
			int n = _curves.length/2;
			for(int i=0; i<n; i++) {
				if(styles[i].contains("--")) name = "Loaded ";
				else name = "Memory ";
				name += "Curve"+i; 	
				
				vValue = _curves[2*i]; hValue = _curves[2*i+1];
				_plot.add(hValue, vValue, 0, hLabel, vLabel, styles[i], name, markSize, lineWidth);
				_plot.adjustAxislimit(0.0, 0.0, 0.02, 0.02);
			}
		} else if(iType==151) {
			setDialogWindowSize(600, 600);
			lineWidth = 2.5f;
			String [] styles = new String[]{"r-", "r--", "g-", "g--", "b-", "b--", "k-", "k--",
					"r-", "r--", "g-", "g--", "b-", "b--", "k-", "k--"};
			int n = _curves.length/2;
			for(int i=0; i<n; i++) {
				if(styles[i].contains("--")) name = "Memory ";
				else name = "Loaded ";
				name += "Curve"+i; 	
				
				vValue = _curves[2*i]; hValue = _curves[2*i+1];
				_plot.add(0, i/2, hValue, vValue, 0, hLabel, vLabel, styles[i], name, 
						markSize, lineWidth, true, true);
				_plot.adjustAxislimit(0.0, 0.0, 0.05, 0.05);
			}
			_plot.setHLabel(0, "Delta");
			_plot.setHLabel(1, "Epsilon");
			_plot.setHLabel(2, "Gamma");
		} else if(iType==152) {
			setDialogWindowSize(600, 600);
			lineWidth = 1.5f;
			String [] styles = new String[]{"k-", "r--", "b-."};
			String [] names = new String[]{"True ", "TestA ", "TestB "};
			float [] lineWidths = new float[]{2.5f, 2.5f, 2.5f};
			int n = _curves.length/2;
			for(int i=0; i<n; i++) {				
				vValue = _curves[2*i]; hValue = _curves[2*i+1];
				_plot.add(0, i/3, hValue, vValue, 0, hLabel, vLabel, styles[i%3], 
						names[i%3]+i, markSize, lineWidths[i%3], true, true);
				//_plot.adjustAxislimit(0.0, 0.0, 0.0, 0.0);
			}
			_plot.setHLabel(0, "P-wave Velocity (ft/s)");
			_plot.setHLabel(1, "S-wave Velocity (ft/s)");
			//_plot.setHLabel(0, "Delta");
			//_plot.setHLabel(1, "Epsilon");
			//_plot.setHLabel(2, "Gamma");
		} else if(iType==160) {
			setDialogWindowSize(800, 500);
			lineWidth = 1.0f;
			name = "Curve"+iType;  			hValue = _curves[0]; vValue = _curves[1];
			style = "k-";
			_plot.add(hValue, vValue, 0, hLabel, vLabel, style, name, markSize, lineWidth);
			//_plot.adjustAxislimit(0.05, 0.05, 0.05, 0.05);
		} 
		return _plot;
		
	}
		
	protected JPanel createContents() {		
		int iType = _plot.getIType();
		if(iType>=100) return createContentsOver100();
		int legendY = 20;
		
		double [] hValue = null;
		double [] vValue = null;
		String hLabel = null;
		String vLabel = null; 
		String style = null;
		String name = null;
		float markSize =12.0f;
		float lineWidth = 2.0f;
		
		
		double [][] data = getModelParameters(iType<50, _dipLayer1D);
		vValue = data[0];
		_plot.addBackgroundGrid();
		if(iType==0) {
			_plot.setLegendOn(true);
			CommonMouseEditingMode mouseEditingMode = _plot.getMouseEditingMode();
			mouseEditingMode.setShowLegend(true);
			name = "Vp"; 			style = "r-"; 			hValue = data[1]; 
			_plot.setLegendLabel(name);
			_plot.setLegendY(legendY);			    
			_plot.add(hValue, vValue, 1, hLabel, vLabel, style, name, markSize, lineWidth);
			
			name = "Vs"; 			style = "b-"; 			hValue = data[2]; 
			_plot.setLegendLabel(name);
			legendY += 20;
			_plot.setLegendY(legendY);
			_plot.add(hValue, vValue, 1, hLabel, vLabel, style, name, markSize, lineWidth);	

			_plot.adjustAxislimit(0, 0, 0.5, 0.05);
		} else if(iType==1) {
			name = "Vp/Vs"; 			style = "r-";
			hValue = new double[data[1].length];
			for(int i=0; i<hValue.length; i++) hValue[i] = data[1][i]/data[2][i];
			_plot.add(hValue, vValue, 0, hLabel, vLabel, style, name, markSize, lineWidth);

			_plot.adjustAxislimit(0, 0, 0.1, 0.1);
		} else if(iType==2) {
			_plot.setLegendOn(true);
			name = "Epsilon"; 			style = "r-"; 			hValue = data[5]; 
			_plot.setLegendLabel(name);
			_plot.setLegendY(legendY);
			_plot.add(hValue, vValue, 1, hLabel, vLabel, style, name, markSize, lineWidth);
			name = "Delta"; 			style = "g-"; 			hValue = data[6]; 
			_plot.setLegendLabel(name);
			legendY += 20;
			_plot.setLegendY(legendY);
			_plot.add(hValue, vValue, 1, hLabel, vLabel, style, name, markSize, lineWidth);
			name = "Gamma"; 			style = "b-"; 			hValue = data[7]; 
			_plot.setLegendLabel(name);
			legendY += 20;
			_plot.setLegendY(legendY);
			_plot.add(hValue, vValue, 1, hLabel, vLabel, style, name, markSize, lineWidth);
			
			_plot.adjustAxislimit(0, 0, 0.1, 0.1);
		} else if(iType==11) {
			name = "Density"; 			style = "r-"; 			hValue = data[4];
			_plot.add(hValue, vValue, 0, hLabel, vLabel, style, name, markSize, lineWidth);
			_plot.adjustAxislimit(0, 0, 0.1, 0.1);
		} else if(iType>=12&&iType<=16) {
			style = "k-"; 		
			String [] names = new String[]{"E(h)", "E(v)", "P(h)", "P(h, v)", "P(v, h)"};
			for(int i=0; i<names.length; i++) {
				if(i+12==iType) {
					name = names[i]; 
					hValue = data[8+i];
					_plot.add(hValue, vValue, 0, hLabel, vLabel, style, name, markSize, lineWidth);
					_plot.adjustAxislimit(0, 0, 0.1, 0.1);
				}
			}
		} 	
		
		else if(iType==20) {
			_plot.setLegendOn(true);
			CommonMouseEditingMode mouseEditingMode = _plot.getMouseEditingMode();
			mouseEditingMode.setShowLegend(true);
			String [] names = new String [] {"Obs P ", "Cal P ", "Obs S ", "Cal S "}; 
			String [] styles = new String [] {"ro", "r+", "bo", "b+" };
			double [][] times = new double[][] {_vcPW.getObsPT(), _vcPW.getCalPT(), _vcPW.getObsST(), _vcPW.getCalST() };
			
			float [] depth = null;
			float [] time = null;
			for(int i=0, k=0; i<_vcPW.getNEvents(); i++) {
				legendY = 0;
				
				for(int ik=0; ik<4; ik++) {
					int jk = k;
					int nPoint = 0;
					for(int j=0; j<_vcPW.getFlag(i); j++) {
						double a = times[ik][jk];
						if(a>0) nPoint++;
						jk++;
					}
					if(nPoint==0) continue;
					depth = new float[nPoint];
					time = new float[nPoint];
					
					jk = k;
					nPoint = 0;
					for(int j=0; j<_vcPW.getFlag(i); j++) {
						double a = times[ik][jk];
						if(a>0) {
							depth[nPoint] = (float)_vcPW.getRD(jk);
							time[nPoint] = (float)(1000.0*a);
							nPoint++;
						}
						jk++;
					}
					
					_plot.setLegendLabel(names[ik]);
					legendY += 20;
					_plot.setLegendY(legendY);			    
					_plot.add(time, depth, 0, hLabel, vLabel, styles[ik], "Shot "+i+" "+names[ik], markSize, lineWidth);
				}
				
				k += _vcPW.getFlag(i);
			}

			_plot.adjustAxislimit(0.05, 0.05, 0.05, 0.05);
		}
		
		
		else if(iType==50) {
			name = "Vp-Vs"; 		style = "ro"; 			vValue = data[1]; hValue = data[2];
			_plot.add(hValue, vValue, 0, hLabel, vLabel, style, name, markSize, lineWidth);
			_plot.adjustAxislimit(0.05, 0.05, 0.05, 0.05);
			//_plot.setEqualAxis(0.05, 0.05);
		} else if(iType==51) {
			name = "Epsilon-Gamma"; style = "ro"; 			vValue = data[5]; hValue = data[7];
			_plot.add(hValue, vValue, 0, hLabel, vLabel, style, name, markSize, lineWidth);
			_plot.adjustAxislimit(0.05, 0.05, 0.05, 0.05);
			//_plot.setEqualAxis(0.05, 0.05);
		}
		else if(iType==52) {
			style = "ro"; 		
			String [] names = new String[] {"Vp", "Vs", "Vp/Vs", "Density", "Delta", "Epsilon", "Gamma", 
					"E(h)", "E(v)", "P(h)", "P(h, v)", "P(v, h)"};

			name = names[_iH]+"-"+names[_iV]; 	vValue = data[_iV+1]; hValue = data[_iH+1];
			_plot.add(hValue, vValue, 0, hLabel, vLabel, style, name, markSize, lineWidth);
			_plot.adjustAxislimit(0.05, 0.05, 0.05, 0.05);
			//_plot.setEqualAxis(0.05, 0.05);
		}		
		
		else if(iType==80||iType==81||iType==82) {
			_plot.setLegendOn(true);
			CommonMouseEditingMode mouseEditingMode = _plot.getMouseEditingMode();
			mouseEditingMode.setShowLegend(true);
			float [] hValueF = new float[_vcPW.getNEvents()];
			float [] vValueF = new float[_vcPW.getNEvents()];
			if(iType==80) {
				for(int i=0; i<hValueF.length; i++) {
					vValueF[i] = (float)_vcPW.getEN(i);
					hValueF[i] = (float)_vcPW.getEE(i);
				}
			} else if(iType==81) {
				for(int i=0; i<hValueF.length; i++) {
					vValueF[i] = (float)_vcPW.getED(i);
					hValueF[i] = (float)_vcPW.getEE(i);
				}
			} else if(iType==82) {
				for(int i=0; i<hValueF.length; i++) {
					vValueF[i] = (float)_vcPW.getED(i);
					hValueF[i] = (float)_vcPW.getEN(i);
				}
			} 
			style = "rO"; 
			name = "source"; 	
			markSize = 10;
			_plot.setLegendLabel(name);
			_plot.setLegendY(legendY);
			_plot.add(hValueF, vValueF, 1, hLabel, vLabel, style, name, markSize, lineWidth);
			
			hValueF = new float[_vcPW.getFlag(0)];
			vValueF = new float[_vcPW.getFlag(0)];
			if(iType==80) {
				for(int i=0; i<hValueF.length; i++) {
					vValueF[i] = (float)_vcPW.getRN(i);
					hValueF[i] = (float)_vcPW.getRE(i);
				}
			} else if(iType==81) {
				for(int i=0; i<hValueF.length; i++) {
					vValueF[i] = (float)_vcPW.getRD(i);
					hValueF[i] = (float)_vcPW.getRE(i);
				}
			} else if(iType==82) {
				for(int i=0; i<hValueF.length; i++) {
					vValueF[i] = (float)_vcPW.getRD(i);
					hValueF[i] = (float)_vcPW.getRN(i);
				}
			}
			style = "gO"; 
			name = "receiver"; 	
			markSize = 10;
			_plot.setLegendLabel(name);
			legendY += 20;
			_plot.setLegendY(legendY);
			_plot.add(hValueF, vValueF, 1, hLabel, vLabel, style, name, markSize, lineWidth);
			_plot.adjustAxislimit(0.05, 0.05, 0.05, 0.05);
		}
		
		return _plot;
	}
	
	public void add(double hx, double vy) {
		float [] hValueF = new float[]{(float)hx};
		float [] vValueF = new float[]{(float)vy};
		String style = "bS"; 
		String name = "Current"; 	
		float markSize = 16;
		_plot.setLegendLabel(name);
		_plot.addLegendY(20);
		_plot.add(hValueF, vValueF, 1, null, null, style, name, markSize, 2.0f);
		_plot.adjustAxislimit(0.05, 0.05, 0.05, 0.05);
		_plot.addLegendY(-20);
	}
	
	private JMenuItem[] genJMenuItem() {
		int iType = _plot.getIType();
		JMenuItem jMenuItem1 = new JMenuItem("Data");		
		jMenuItem1.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent e)   {
				HtmlDialog dialog = new HtmlDialog(_frame, _title, false);
				dialog.addTable(_plot.getHList(), _plot.getVList());
				dialog.showDialog();
			}
		});
				
		return new JMenuItem[] { jMenuItem1 };	
	}
	public void refresh() {
		SwingUtilities.getWindowAncestor( this ).repaint(); 
	}
	
	public double [] calLayerProperty(int id, DipLayer1D init) {
		int n = init.getNumOfBoundaries();
		double [] vel = new double[n];
		for(int i=0; i<n; i++) { vel[i] = init.getLayer(i).calLayerProperty(id); }
		return vel;
	}
	
	private double [][] getModelParameters(boolean isDepth, DipLayer1D init) {
		if(init==null) return null;
		for(int i=0; i<init.getNumOfBoundaries(); i++) { init.getLayer(i).toCij(1.0, 1.0); }
		
		double [][] initData = new double[][] {
				init.getTop(), init.getLayerProperty(1), init.getLayerProperty(2), init.getLayerProperty(3), 
				init.getLayerProperty(4), init.getLayerProperty(21), 
				init.getLayerProperty(20), init.getLayerProperty(22),
				calLayerProperty(1, init), calLayerProperty(2, init),
				calLayerProperty(3, init), calLayerProperty(4, init), calLayerProperty(5, init)};
		if(!isDepth) return initData;
		
		int nBoundary = init.getNumOfBoundaries();
		double [][] data = new double[13][2*(nBoundary-1)];
		for(int i=0; i<nBoundary-1; i++) {
			int k = 2*i;

			data[0][k] = initData[0][i];
			data[0][k+1] = initData[0][i+1];
			
			for(int j=1; j<13; j++) {
				data[j][k] = initData[j][i];
				data[j][k+1] = initData[j][i];
			}
		}
		return data;
	}

}
