package org.ucdm.tracedisplay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ucdm.common.CommonDialog;
import org.ucdm.common.CommonFrame;
import org.ucdm.common.CommonMouseEditingMode;
import org.ucdm.common.CommonPanel;
import org.ucdm.common.UcdmUtils;
import org.ucdm.component.StatusBar;
import org.ucdm.core.SeismicTraceComponent;
import org.ucdm.mti.Seg2View;
import org.ucdm.seg2.SEG2;
import org.ucdm.segy.Gather;


@SuppressWarnings("serial")
public class TraceDisplayDialog  extends CommonDialog {
	private CommonFrame 	_frame 			= null;
	private SeismicTraceComponent [] _comps = null;
	private int 			_iFold 			= 3;
	private TracePanel 		_world 			= null;
	private int 			_id 			= 0; // 2 FD2D 3 FD3D 11 DWN bin
	private TraceDisplayToolBar _traceDisplayToolBar = null;

	public TraceDisplayDialog(CommonFrame aParent, String aTitle, boolean modal, 
			 int id, int iFold, String selectedFileName) {
		this(aParent, aTitle, modal, id, iFold, read(id, selectedFileName));
	}
//	public TraceDisplayDialog(CommonFrame aParent, String aTitle, boolean modal, 
//			 int id, int iFold, SeismicTraceComponent [][] comps) {
//		this(aParent, aTitle, modal, id, iFold, convertTo1D(comps));
//	}
	public TraceDisplayDialog(CommonFrame aParent, String aTitle, boolean modal, int id, 
			int iFold, SeismicTraceComponent [] comps) {
		super((JFrame)aParent, aTitle, modal);
		setBackground(Color.white);
		setDialogWindowSize(1400, 800);
		_frame 	= aParent;
		_id 	= id;
		_iFold  = iFold;
		_comps 	= comps;
		//if(_comps!=null) paddingZeros(_comps);
		
		StatusBar statusBar = new StatusBar(); 
		statusBar.setZoneBorder(BorderFactory.createLineBorder(Color.GRAY)); 

		statusBar.setZones( new String[] { "first_zone", "second_zone", "third_zone", "remaining_zones" },     
				new Component[] { new JLabel("first"),  new JLabel("second"),  new JLabel("third"),  new JLabel("remaining")     },     
				new String[] {"20%", "6%", "60%", "*"} );
		
		String usageText = "<html>To pick: Mouse Hover + P for <font color='red'>P,</font> "
				+ " + S for <font color='blue'>SH,</font> "
				+ " + V for <font color='green'>SV</font>"
				+ "; To kill picks: Mouse Hover + <font color='red'>K</font>"
				+ ";       To zoom in/out: Alt+Shift+drag/click</html>";
		JLabel usageLabel  = (JLabel) statusBar.getZone("third_zone");
		usageLabel.setText(usageText);
		usageLabel.setHorizontalAlignment(JLabel.RIGHT);
		
		_world = new TracePanel(1, 1, CommonPanel.Orientation.X1DOWN_X2RIGHT, CommonPanel.AxesPlacement.LEFT_TOP, statusBar);
		_world.setVLabel("Channel No.");
		_world.setBackground(Color.white);
		_world.setFrame(_frame);
		_frame.setCommonPanel(_world);	
		_world.initModeManager();
		
		
//		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(
//				new KeyEventDispatcher() {
//					public boolean dispatchKeyEvent(KeyEvent e) {
//						boolean keyHandled = false;
//						if (e.getID() == KeyEvent.KEY_PRESSED) {
//							System.out.println("I am here");
//							if (e.getKeyCode() == KeyEvent.VK_ENTER) {
//								//ok();
//								keyHandled = true;
//							} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//								//cancel();
//								keyHandled = true;
//							}
//						}
//						return keyHandled;
//					}
//				});
		addKeyListener(_kl);
		_world.addKeyListener(_kl);
		_frame.addKeyListener(_kl);
	}
	
	private KeyListener _kl = new KeyAdapter() {
		public void keyTyped(KeyEvent e) { 
			System.out.println("p1=");
		}
		public void keyReleased(KeyEvent e) { }
		public void keyPressed(KeyEvent e) {
			System.out.println("q1=");
		}
	};
	
//	private void paddingZeros(SeismicTraceComponent [] comps) {
//		int maxN = 0;
//		for(int i=0; i<comps.length; i++) {
//			int n = comps[i].getData().length;
//			maxN = maxN>n?maxN:n;
//		}
//		for(int i=0; i<comps.length; i++) {
//			comps[i].paddingZeros(maxN);
//		}			
//	}
	
	protected JComponent getCommandRow() { return null; }
	protected boolean okAction() {	return true;	}
	protected JPanel createContents() {
		_traceDisplayToolBar = new TraceDisplayToolBar(_world, 0, null);
		update();
		
		JPanel jPanel = new JPanel(new BorderLayout());
		jPanel.add(_traceDisplayToolBar, BorderLayout.NORTH);
		jPanel.add(_world, BorderLayout.CENTER);
		return jPanel;
	}
	
	public TracePanel getWorld() { return _world; }
	
	public void update() {
			TraceView view	 = new TraceView(_frame, _iFold, _comps, 
					_traceDisplayToolBar.getState(), false, null, 1.0f);
			view.setLineWidth(1.0f);
			view.setLineColor(Color.GRAY);
			view.setLineStyle(TraceView.Line.SOLID);
			view.setName("SEG2", "All");
			getWorld().removeView(view.getCategory(), view.getName());
			getWorld().removeView("SEG2", "All");
			getWorld().addTraceView(0, 0, view);
	}
	
	public void setFD2DData(Gather gather) {
		int k = 0;
		for(int i=0; i<gather.getNumOfReceivers(); i++) {
			k += gather.getSensor(i).getNumOfComps();
		}
		SeismicTraceComponent [] comps = new SeismicTraceComponent[k];
		k = 0;
		for(int i=0; i<gather.getNumOfReceivers(); i++) {
			int nComps = gather.getSensor(i).getNumOfComps();
			for(int j=0; j<nComps; j++) comps[k++] = gather.getSensor(i).getComp(j);
		}

		_comps = comps;
	}
	
//	public static SeismicTraceComponent [] convertTo1D(SeismicTraceComponent [][] compsOrig) {
//		SeismicTraceComponent [] comps = new SeismicTraceComponent[compsOrig.length*compsOrig[0].length];
//		for(int i=0, k=0; i<compsOrig.length; i++) { 
//			for(int j=0; j<compsOrig[i].length; j++) { 
//				comps[k++] = compsOrig[i][j];
//			}
//		}
//		return comps;
//	}
	
	public static SeismicTraceComponent [] read(int iType, String selectedFileName) {
		SeismicTraceComponent [] comps = null;
		if(iType==2 || iType==3) {
			SEG2 seg2 = new SEG2(selectedFileName);
			int k = seg2.getNumOfTraces();
			comps = new SeismicTraceComponent[k];
			int nReceiver = k/3;
			k = 0;
			for(int i=0; i<nReceiver; i++) {
				for(int j=0; j<3; j++) {
					comps[k] = new SeismicTraceComponent(0, i, j, seg2.getSEG2Trace(k));
					k++;
				}
			}		
		} if(iType==11) {
			comps = UcdmUtils.readBinaryFileToTrace(selectedFileName);
		}
		return comps;
	}
	
	

}

