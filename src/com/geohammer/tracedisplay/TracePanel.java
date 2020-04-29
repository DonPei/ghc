package org.ucdm.tracedisplay;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.ucdm.common.CommonMouseEditingMode;
import org.ucdm.common.CommonPanel;
import org.ucdm.common.UcdmUtils;
import org.ucdm.component.LabelTextCombo;
import org.ucdm.component.StatusBar;
import org.ucdm.common.TextViewDialog;
import org.ucdm.core.SeismicTraceComponent;
import org.ucdm.dsp.DSP;
import org.ucdm.vc.dialog.PlotDialog;

import edu.mines.jtk.awt.Mode;
import edu.mines.jtk.awt.ModeManager;
import edu.mines.jtk.mosaic.Projector;
import edu.mines.jtk.mosaic.Tile;
import edu.mines.jtk.mosaic.Transcaler;
import edu.mines.jtk.util.ArrayMath;

public class TracePanel extends CommonPanel {
	private static final long serialVersionUID = 1L;

	JTextField _beginTF 		= new JTextField("0");
	JTextField _endTF 			= new JTextField("0");
	JTextField _beginWTF 		= new JTextField("0");
	JTextField _endWTF 			= new JTextField("0");

	JTextField _sWinTF 		= new JTextField("50");
	JTextField _lWinTF 			= new JTextField("200");
	JTextField _smoothTF 		= new JTextField("3");
	
	public MouseTrackInfoMode _mouseTrackInfoMode = null;
	protected TraceView 	_traceView = null;

	//private InputMap mapInput;
    //private ActionMap mapAction;
    
	public TracePanel(int nrow, int ncol, Orientation orientation, AxesPlacement axesPlacement, StatusBar statusBar) {
		super(nrow,ncol,orientation, axesPlacement, statusBar);
		createKeyBindings();
	}
	private void createKeyBindings() {
		InputMap mapInput = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        mapInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0, false), "P_Wave");
        mapInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "SH_Wave");
        mapInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0, false), "SV_Wave");
        mapInput.put(KeyStroke.getKeyStroke(KeyEvent.VK_K, 0, false), "Remove_Picks");
        
        ActionMap mapAction = getActionMap();
        mapAction.put("P_Wave", new HotKeys(0));
        mapAction.put("SH_Wave", new HotKeys(1));
        mapAction.put("SV_Wave", new HotKeys(2));
        mapAction.put("Remove_Picks", new HotKeys(3));
    }
	final class HotKeys extends AbstractAction	{
		int _id = 0;
		public HotKeys(int id) {
			_id = id;
		}
		public void actionPerformed(ActionEvent e) {
			if(_id==0) {
				_mouseTrackInfoMode.setPickP();
				//System.out.println("In HotKeys.actionPerformed1().");
			} else if(_id==1) {
				_mouseTrackInfoMode.setPickSH();
				//System.out.println("In HotKeys.actionPerformed2().");
			} else if(_id==2) {
				_mouseTrackInfoMode.setPickSV();
				//System.out.println("In HotKeys.actionPerformed3().");
			} else if(_id==3) {
				_mouseTrackInfoMode.removePick();
				//System.out.println("In HotKeys.actionPerformed0().");
			} 
			
		}
	}


	public TraceView addTraceView(int irow, int icol, TraceView traceView) {
		if (_orientation==Orientation.X1RIGHT_X2UP) {
			traceView.setOrientation(TraceView.Orientation.X1RIGHT_X2UP);
		} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
			traceView.setOrientation(TraceView.Orientation.X1DOWN_X2RIGHT);
		}
		addTiledView(irow,icol,traceView);
		_traceView = traceView;
		return traceView;
	}
	
	public TraceView getTraceView() { return _traceView; }
	
	public void initModeManager() {
		setEnableTracking(false);
		setEnableEditing(true);
		setEnableZoom(true);

		addModeManager();
		CommonMouseEditingMode mouseEditingMode = getMouseEditingMode();
		mouseEditingMode.setShowVisibility(false);
		mouseEditingMode.setShowAutoAxisLimit(false);
		mouseEditingMode.setJComponent(genJMenuItem(mouseEditingMode));

		_mouseTrackInfoMode = new MouseTrackInfoMode(getModeManager());
		_mouseTrackInfoMode.setActive(true);
		_mouseTrackInfoMode.setTrackingLabel(_trackingLabel);		
	}
	
	private JComponent[] genJMenuItem(CommonMouseEditingMode mouseEditingMode) {		
		JMenuItem jMenuItem1 = new JMenuItem("View Trace Header");
		jMenuItem1.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent e)   {
//				if(_traceView.getSelectedComp()==null) return;
//				SeismicTraceComponent  [] comps = _traceView.getSelectedComp0();
//				//SEG2Trace trace = comps[0].getSEG2Trace();
//				String text = comps[0].getSEG2Trace().toString(1);
//				for(int i=1; i<comps.length; i++) {
//					text += comps[i].getSEG2Trace().toString(1);
//				}
//				TextViewDialog dialog = new TextViewDialog(_traceView.getFrame(), "Trace Header", false, text);
//				dialog.showDialog();
			}
		});

		JMenuItem jMenuItem2 = new JMenuItem("View Trace Data");
		jMenuItem2.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent e)   {
				if(_traceView.getSelectedComp()==null) return;
				SeismicTraceComponent  [] comps = _traceView.getSelectedComp0();
				
				String [] header = new String[comps.length];
				float [][] dataF = new float[comps.length][];
				for(int i=0; i<comps.length; i++) {
					header[i] = "Component_"+(i+1);
					dataF[i] = comps[i].getData();
				}
				float [][] data = UcdmUtils.transposeArray(dataF);
				//String text = textString(true, header, dataF, null);
				String text = "";
				for(int i=0; i<header.length; i++) text += header[i]+" \t";
				text += " \n";
				for(int i=0; i<data.length; i++) {
					for(int j=0; j<data[i].length; j++) {
						text += data[i][j]+" \t";
					}
					text += " \n";
				}
				TextViewDialog dialog = new TextViewDialog(_traceView.getFrame(), "Trace Data", false, text);
				dialog.showDialog();
			}
		});
		
		JMenu calSNRMenu = new JMenu("Cal SNR");
		calSNRMenu.add(new LabelTextCombo("Short Win", _sWinTF));
		calSNRMenu.add(new LabelTextCombo("Long Win  ", _lWinTF));
		JMenuItem calSNRMenuItem  	= new JMenuItem("Calculate"); 
		calSNRMenuItem.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent e)   {
				SeismicTraceComponent  [] orgComps = _traceView.getSelectedComp0();
				if(orgComps==null) return;
				
				String phrase = _trackingLabel.getText().trim();
				String delims = "[:,]+";
				String[] tokens = phrase.split(delims);
				//for (int i = 0; i < tokens.length; i++) System.out.println(tokens[i]);
				int indexP = (int)Double.parseDouble(tokens[1].trim());		
				int sLen = Integer.parseInt(_sWinTF.getText().trim());
				int lLen = Integer.parseInt(_lWinTF.getText().trim());

				double SNR = calSNR(indexP, indexP-sLen, indexP+lLen, orgComps[0].getData());
				
				TextViewDialog dialog = new TextViewDialog(_traceView.getFrame(), "SNR", false, 
						"indexP="+indexP+" P1="+(indexP-sLen)+" P2="+(indexP+lLen)+" SNR="+SNR);
				dialog.showDialog();
			}
		});
		calSNRMenu.add(calSNRMenuItem);
		
		JMenu smooth = new JMenu("Smooth");
		smooth.add(new LabelTextCombo("Half Width", _smoothTF));
		JMenuItem jMenuItem10  	= new JMenuItem("Smooth");
		smooth.add(jMenuItem10);
		jMenuItem10.addActionListener(new ActionListener() 	{
			public void actionPerformed(ActionEvent e)   {
				if(_traceView.getSelectedComp()==null) return;
				displayWaveform(5);
			}
		});
		
		JMenu plotWaveform = new JMenu("Plot Waveform");
		plotWaveform.add(new LabelTextCombo("From", _beginWTF));
		plotWaveform.add(new LabelTextCombo("To    ", _endWTF));
		
		String [] label = new String[] {"Component 1", "Component 2", "Component 3", "All Components"};
		JMenu comps  	= new JMenu("Component");
		for(int i=0; i<label.length; i++) {
			final int j = i;
			JMenuItem jMenuItem  	= new JMenuItem(label[i]);  
			if(j==3) plotWaveform.add(jMenuItem);
			else comps.add(jMenuItem);
			jMenuItem.addActionListener(new ActionListener() 	{
				public void actionPerformed(ActionEvent e)   {
					if(_traceView.getSelectedComp()==null) return;
					displayWaveform(j);
				}
			});
		}
		
		plotWaveform.add(comps);
		
		JMenu spectrum = new JMenu("Spectrum");		
		spectrum.add(new LabelTextCombo("From", _beginTF));
		spectrum.add(new LabelTextCombo("To    ", _endTF));
		
		label = new String[] {"Comp 1", "Comp 2", "Comp 3", "All Comps"};
		comps  	= new JMenu("Comp");
		for(int i=0; i<label.length; i++) {
			final int j = i;
			JMenuItem jMenuItem  	= new JMenuItem(label[i]);  
			if(j==3) spectrum.add(jMenuItem);
			else comps.add(jMenuItem);
			jMenuItem.addActionListener(new ActionListener() 	{
				public void actionPerformed(ActionEvent e)   {
					if(_traceView.getSelectedComp()==null) return;
					displaySpectrum(0, j);
				}
			});
		}
		spectrum.add(comps);
		
		

		return new JComponent[] { calSNRMenu, jMenuItem2, new JLabel("S"), smooth, plotWaveform, spectrum };
	}
	
	private double calSNR(int indexP, int p1, int p2, float [] V) {
		int len1 = indexP-p1+1;
		int len2 = p2-indexP+1;
		if(len1<2||len2<2) return 0.0f;
		
		double noiseSum = 0.0;
		double signalSum = 0.0;
		for(int i=p1; i<indexP; i++) noiseSum += V[i]*V[i];
		for(int i=indexP; i<=p2; i++) signalSum += V[i]*V[i];
		double SNR = 0.0;
		if(noiseSum>0.0)  	SNR = 10.0 * Math.log10( (signalSum/len2) / (noiseSum/len1) );
		//System.out.println("p1="+p1);
		return SNR;
	}
	
	private void displayWaveform(int iComp) {
		//SeismicTraceComponent [] comps0 = _traceView.getSeismicTraceComponent0();
		//int iReceiver = _traceView.getSelectedReceiverIndex();
		
		SeismicTraceComponent  [] orgComps = _traceView.getSelectedComp0();
		int beginIndex = Integer.parseInt(_beginWTF.getText().trim());
		int endIndex = Integer.parseInt(_endWTF.getText().trim());

		SeismicTraceComponent  [] comps = null;
		if(iComp<3) {
			comps = new SeismicTraceComponent  [] { orgComps[iComp] };
		} else comps = orgComps;
		
		if(endIndex>beginIndex) {
			for(int i=0; i<comps.length; i++) comps[i] = comps[i].copy(beginIndex, endIndex);
		}
		if(iComp==5) {
			int halfWidth = Integer.parseInt(_smoothTF.getText());
			for(int i=0; i<comps.length; i++) {
				comps[i] = comps[i].copy();
				comps[i].exponentiallySmooth(halfWidth);
			}
		}
		
		TraceDisplayDialog dialog = new TraceDisplayDialog(_frame, "Trace Waveform", false, 3, 1, comps);
		dialog.showDialog();
	}
	private void displaySpectrum(int iMethod, int iComp) {
		ArrayList<float[]> axList = new ArrayList<float[]>();
		ArrayList<float[]> ayList = new ArrayList<float[]>();

		if(iComp==3) {
			for(int i=0; i<3; i++) {
				SeismicTraceComponent comp = _traceView.getSelectedComp0()[i];
				int beginIndex = Integer.parseInt(_beginTF.getText().trim());
				int endIndex = Integer.parseInt(_endTF.getText().trim());
				float [][] a = DSP.fft(new int [] {beginIndex, endIndex}, comp);
				//DSP.exponentialFilterEndingWithZeroSlope(0.35f, a[1], null);
				axList.add(ArrayMath.copy(a[0]));
				ayList.add(ArrayMath.copy(a[1]));
			}
		} else {
			SeismicTraceComponent comp = _traceView.getSelectedComp0()[iComp];
			int beginIndex = Integer.parseInt(_beginTF.getText().trim());
			int endIndex = Integer.parseInt(_endTF.getText().trim());
			float [][] a = DSP.fft(new int [] {beginIndex, endIndex}, comp);
			DSP.exponentialFilterEndingWithZeroSlope(0.35f, a[1], null);
			axList.add(ArrayMath.copy(a[0]));
			ayList.add(ArrayMath.copy(a[1]));
		}
			
		float [][] ax = new float[axList.size()][];
		float [][] ay = new float[axList.size()][];
		for(int i=0; i<axList.size(); i++) {
			ax[i] = axList.get(i);
			ay[i] = ayList.get(i);
		}
		
		if(iMethod==0) {
			PlotDialog dialog = new PlotDialog(_traceView.getFrame(), "Frequency Spectrum - Component "+(iComp+1), false, 
					null, 120+iComp, false, "Frequency", "Frequency (Hz)", "Power Spectrum");
			if(iComp==3) dialog.setCurves(new float [][] {ax[0], ay[0], ax[1], ay[1], ax[2], ay[2]});
			else dialog.setCurves(new float [][] {ax[0], ay[0]});
			dialog.showDialog();
		} else {
			 float [][] curve = new  float [2*ax.length][];
			float [] a = null;
			for(int i=0, k=0; i<axList.size(); i++) {
				a = ax[i];
				float [] b = new float[a.length];
				for(int j=0; j<b.length; j++) b[j] = (float)Math.log10(a[j]);
				curve[k++] = b;
				a = ay[i];
				b = new float[a.length];
				for(int j=0; j<b.length; j++) b[j] = (float)Math.log10(a[j]);
				curve[k++] = b;
			}
			
			PlotDialog dialog = new PlotDialog(_traceView.getFrame(), "Frequency Spectrum - Component "+(iComp+1), false, 
					null, 120+iComp, false, "Frequency", "Frequency (Hz)", "Displacement Spectrum (m)");
			dialog.setCurves(curve);
			dialog.showDialog();
		}		
	}
	
	public class MouseTrackInfoMode extends Mode {
		private static final long serialVersionUID = 1L;
		private JLabel 	_trackingLabel 		= null;

		private double _vx = 0;
		private double _vy = 0;

		private Tile _tile; // tile in which tracking; null, if not tracking.
		private int _xmouse; // x coordinate where mouse last tracked
		private int _ymouse; // y coordinate where mouse last tracked 

		private int _ymouse0; // y coordinate where mouse last tracked
		private int _ymouse1; // y coordinate where mouse last tracked

		public MouseTrackInfoMode(ModeManager modeManager) {
			super(modeManager);
			setName("Track");
			setIcon(loadIcon(MouseTrackInfoMode.class,"Track24.gif"));
			setMnemonicKey(KeyEvent.VK_Z);
			setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_T,0));
			setShortDescription("Track mouse in tile");
		}

		public boolean isExclusive() { return false; }
		public void setTrackingLabel( JLabel trackingLabel) { _trackingLabel = trackingLabel; }

		protected void setActive(Component component, boolean active) {
			if ((component instanceof Tile)) {
				if (active) {
					component.addMouseListener(_ml);
					//component.setFocusable(true);
					//component.requestFocusInWindow();
					//component.addKeyListener(_kl);
				} else {
					component.removeMouseListener(_ml);
					//component.setFocusable(false);
					//component.removeKeyListener(_kl);
				}
			}
		}
		
		private KeyListener _kl = new KeyAdapter() {
			public void keyTyped(KeyEvent e) { 
				System.out.println("p=");
			}
			public void keyReleased(KeyEvent e) { }
			public void keyPressed(KeyEvent e) {
				System.out.println("q=");
			}
		};
		public void removePick() {
			TraceView lv = _traceView;
			if(lv==null) return;
			int p = lv.getPicks(0, _vy);
			int sh = lv.getPicks(1, _vy);

			double dp = Math.abs(_vx-p);
			double dsh = Math.abs(_vx-sh);
			int p1 = dp<dsh?0:1;
			//System.out.println("p="+p+" sh="+sh+" dp="+dp+" dsh="+dsh+" p1="+p1);
			//lv.setPicks(p1, 0, _vy);
			//lv.getSEG2InfTxt().setPickEdited(true);	
		}
		public void setPickP() {
			TraceView lv = _traceView;
			if(lv==null) return;
			//lv.setPicks(0, _vx, _vy);
			//lv.getSEG2InfTxt().setPickEdited(true);	
		}
		public void setPickSH() {
			TraceView lv = _traceView;
			if(lv==null) return;
			//lv.setPicks(1, _vx, _vy);
			//lv.getSEG2InfTxt().setPickEdited(true);	
		}
		public void setPickSV() {
			TraceView lv = _traceView;
			if(lv==null) return;
			//lv.setPicks(2, _vx, _vy);
			//lv.getSEG2InfTxt().setPickEdited(true);	
		}
		private void setPicks(MouseEvent evt) {
			if(evt.getButton() == MouseEvent.BUTTON1) {
				//label.setText("Detected Mouse Left Click!");
				_tile = (Tile)evt.getSource();
				TraceView lv = _traceView;
				if(lv==null) return;
				if(evt.isShiftDown()&&evt.isAltDown()) return;
				if(lv.getSEG2InfTxt()==null) return;
				//System.out.println(String.format("t:%.2f, z:%.2f", _vx, _vy));
				if(evt.isControlDown()) { //P wave
					//lv.setPicks(0, _vx, _vy);
					//lv.getSEG2InfTxt().setPickEdited(true);
				} else if(evt.isShiftDown()) { //SH wave
					//lv.setPicks(1, _vx, _vy);
					//lv.getSEG2InfTxt().setPickEdited(true);
				} else if(evt.isAltDown()) { //SV wave
					//lv.setPicks(2, _vx, _vy);
					//lv.getSEG2InfTxt().setPickEdited(true);
				} else { }
			} else if(evt.getButton() == MouseEvent.BUTTON3) {
				//label.setText("Detected Mouse Right Click!");
			}
		}
		private MouseListener _ml = new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				beginTracking(e);
			}
			public void mouseExited(MouseEvent e) {
				endTracking();
			}
			public void mousePressed(MouseEvent evt) {					
				setPicks(evt); 
				Projector hp = _tile.getHorizontalProjector();
				Projector vp = _tile.getVerticalProjector();
				Transcaler ts = _tile.getTranscaler();

				int id = (int)(_vy-0.5);
				double clickY = id+0.5;

				double y = vp.u(clickY);
				_ymouse0 = ts.y(y);
				y = vp.u(clickY+1.0);
				_ymouse1 = ts.y(y);
			}
			public void mouseDragged(MouseEvent evt)  {	 }
			public void mouseReleased(MouseEvent evt) {  }
		};

		private MouseListener _ml2 = new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				TraceView lv = _traceView;
				if(lv==null) return;

				
				if((!evt.isShiftDown())&& (!evt.isAltDown())) lv.mousePressed(evt);

				if((!evt.isControlDown())&&(!evt.isAltDown())&&(!evt.isShiftDown())&&evt.isMetaDown()) { //Press mouse right button
					int iComp = lv.getSelectedCompIndex();
					if(iComp<0) return;
					int sh = lv.getP2(iComp, 1);				
//					if(sh>10) {
//						_beginTF = new JTextField((sh-50)+"");
//						_endTF = new JTextField((sh+100)+"");
//					} else {
//						_beginTF = new JTextField("0");
//						_endTF = new JTextField("0");
//					}
//					_beginWTF = new JTextField("0");
//					_endWTF = new JTextField("0");
//					_popupMenu = new JPopupMenu();
//					setPopupMenu(lv);
//					_popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
				}
			}

			public void mouseReleased(MouseEvent evt) { 
				if((!evt.isShiftDown())&&(!evt.isAltDown())) {
					TraceView lv = _traceView;
					if(lv==null) return;
					lv.mouseReleased(evt);
//					if((!evt.isControlDown())&&(!evt.isAltDown())&&(!evt.isShiftDown())&&(!evt.isMetaDown())) { 
//						lv.getFrame().updateHodogramDialog();
//					}
				}
			}			
		};
		
		private MouseMotionListener _mml = new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent evt)  {
				duringTracking(evt);
				if((!evt.isShiftDown())&& (!evt.isAltDown())) {
					TraceView lv = _traceView;
					if(lv==null) return;
					lv.mouseDragged(evt);
				}
			}

			public void mouseMoved(MouseEvent e) {
				duringTracking(e);
			}
		};

		private void beginTracking(MouseEvent e) {
			_xmouse = e.getX();
			_ymouse = e.getY();
			_tile = (Tile)e.getSource();
			fireTrack();
			_tile.addMouseListener(_ml2);
			_tile.addMouseMotionListener(_mml);
		}

		private void endTracking() {
			_tile.removeMouseListener(_ml2);
			_tile.removeMouseMotionListener(_mml);
			fireTrack();
			_tile = null;
		}

		private void duringTracking(MouseEvent e) {
			_xmouse = e.getX();
			_ymouse = e.getY();
			_tile = (Tile)e.getSource();
			fireTrack();
			if(_ymouse>_ymouse0&&_ymouse<_ymouse1) setPicks(e);
		}

		private void fireTrack() {
			if (_tile==null) {
			} else {
				Projector hp = _tile.getHorizontalProjector();
				Projector vp = _tile.getVerticalProjector();
				Transcaler ts = _tile.getTranscaler();
				double ux = ts.x(_xmouse);
				double uy = ts.y(_ymouse);
				_vx = hp.v(ux);
				_vy = vp.v(uy);
				if(_trackingLabel!=null) {
					_trackingLabel.setText(String.format("x:%.2f, y:%.2f", _vx, _vy));
				}
			}
		}
	}
}

