package org.ucdm.tracedisplay;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import org.ucdm.common.CommonFrame;
import org.ucdm.core.MSEvent;
import org.ucdm.core.ObsWell;
import org.ucdm.core.SeismicTraceComponent;
import org.ucdm.core.SeismicTraceSensor;
import org.ucdm.core.Sensor;
import org.ucdm.dsp.DSP;
import org.ucdm.mti.run.dialog.MtiOption;
import org.ucdm.seg2.SEG2InfTxt;

import edu.mines.jtk.dsp.BandPassFilter;
import edu.mines.jtk.dsp.ButterworthFilter;
import edu.mines.jtk.dsp.Sampling;
import edu.mines.jtk.mosaic.Projector;
import edu.mines.jtk.mosaic.TiledView;
import edu.mines.jtk.mosaic.Transcaler;
import edu.mines.jtk.util.Check;
import static edu.mines.jtk.util.ArrayMath.*;


public class TraceView extends TiledView {
//	public class Seg2View extends TiledView implements KeyListener {
	/**
	 * Orientation of axes x1 and x2. For example, the default orientation 
	 * X1RIGHT_X2UP corresponds to x1 increasing horizontally from left to 
	 * right, and x2 increasing vertically from bottom to top.
	 */
	public enum Orientation {
		X1RIGHT_X2UP,
		X1DOWN_X2RIGHT
	}

	/**
	 * The style of mark plotted at points (x1,x2).
	 * The default mark style is none.
	 */
	public enum Mark {
		NONE,
		POINT,
		PLUS,
		CROSS,
		ASTERISK,
		HOLLOW_CIRCLE,
		HOLLOW_SQUARE,
		FILLED_CIRCLE,
		FILLED_SQUARE,
	}

	/**
	 * The style of line plotted between consecutive points (x1,x2).
	 * The default line style is solid.
	 */
	public enum Line {
		NONE,
		SOLID,
		DASH,
		DOT,
		DASH_DOT,
	}

	public int 		_mouseX = -1; // x coordinate where mouse last tracked
	public int 		_mouseY = -1; // y coordinate where mouse last tracked
	public double 	_xmouseCoord; 
	public double 	_ymouseCoord; 

	public boolean [] 	_kLayer = null;
	//public int 		_pixelTol 	= 6;		//pixels

	private boolean 	_mousePressed		= false;
	private boolean 	_mouseSelection		= false;
	private boolean 	_mouseReleased		= false;
	private boolean 	_mouseDragged		= false;
	private boolean 	_controlDown		= false;
	private boolean 	_enableLineSelection= true;
	private boolean 	_enableArrivalPicking= false;	

	private boolean 	_compZSelected		= true;
	private boolean 	_compYSelected		= true;
	private boolean 	_compXSelected		= true;
	private boolean 	_pickPSelected		= true;
	private boolean 	_pickSHSelected		= true;
	private boolean 	_pickSVSelected		= true;
	private boolean 	_pickPcSelected		= false;
	private boolean 	_pickSHcSelected	= false;
	private boolean 	_pickSVcSelected	= false;

	private boolean 	_equilibriumComp	= false;
	private boolean 	_equilibriumReceiver= true;
	private boolean 	_equilibriumEvent	= false;

	private boolean [][] _state 			= null;

	private int 				_fold		= 3;

	private int 				_currPos 	= 0;
	private boolean 			_currPosOn 	= false;
	private ArrayList<Integer> _idp 		= null;
	private float [] 			_orgX2 		= null;

	private float [][] 			_pX1		= null;
	private float [][] 			_pX2		= null;
	private float [][] 			_shX1		= null;
	private float [][] 			_shX2		= null;
	private float [][] 			_svX1		= null;
	private float [][] 			_svX2		= null;
	private float [][] 			_pcX2		= null;
	private float [][] 			_shcX2		= null;
	private float [][] 			_svcX2		= null;

	private float [][] 			_hodogramXY		= null;
	private int 				_hodogramReceiverIndex 	= 0;
	
	private CommonFrame 				_frame 		= null;
	private SeismicTraceComponent [] 	_compsZYX	= null;
	private SeismicTraceComponent [] 	_comps 		= null;
	private int [][] 					_pick 		= null;
	private int [][] 					_pickT 		= null;

	private boolean [] 			_pickSelection 	= null;
	
	private SeismicTraceSensor 	_sensors[] 	= null;
	private SeismicTraceSensor 	_sensorsZYX[] 	= null;
	private SEG2InfTxt 			_seg2InfTxt = null;
	private MSEvent 			_msEvent 	= null;
	private MtiOption 			_mtiOption 	= null;
	
	private float 				_ampScale 	= 1.0f;
	
	private double 			_INVALID_D 		= -999999.9999;
	
	public void gc() {
		_kLayer 	= null;
		_state 		= null;
		_idp 		= null;
		_orgX2 		= null;

		_pX1		= null;
		_pX2		= null;
		_shX1		= null;
		_shX2		= null;
		_svX1		= null;
		_svX2		= null;
		_pcX2		= null;
		_shcX2		= null;
		_svcX2		= null;

		_hodogramXY	= null;
		_compsZYX	= null;
		_comps 		= null;
		_pick 		= null;
		_pickT 		= null;

		_pickSelection 	= null;
		_sensors 		= null;
		_sensorsZYX 	= null;
		_seg2InfTxt 	= null;
		
		_nx = null; 
		_x1 = null; 
		_x2 = null;
		_x3 = null;
	}
	
	public TraceView(CommonFrame frame, int fold, SeismicTraceComponent [] compZYX, boolean [][] state, 
			boolean enableArrivalPicking, SEG2InfTxt seg2InfTxt, float ampScale) {
		_frame = frame;
		_fold = fold;
		_state =  state;
		_enableArrivalPicking = enableArrivalPicking;
		_seg2InfTxt = seg2InfTxt;

		if(state!=null) {
			_compZSelected	= state[0][0];
			_compYSelected	= state[0][1];
			_compXSelected	= state[0][2];
			_pickPSelected	= state[1][0];
			_pickSHSelected	= state[1][1];
			_pickSVSelected	= state[1][2];
			_pickPcSelected	= state[5][0];
			_pickSHcSelected= state[5][1];
			_pickSVcSelected= state[5][2];

			_enableLineSelection	= state[3][0];
			_equilibriumComp 		= state[4][0];
			_equilibriumReceiver 	= state[4][1];
			_equilibriumEvent 		= state[4][2];
		}

		if(fold==1) {
			_equilibriumComp 		= true;
			_equilibriumReceiver 	= false;
			_equilibriumEvent 		= false;
		}
		setFold(fold, compZYX, seg2InfTxt, ampScale);

		if(state!=null) { if(!state[2][0]) setFold(1); }
	}
	public int getFold() { return _fold; }
	public void setEquilibrium(boolean equilibriumComp, boolean equilibriumReceiver, boolean equilibriumEvent) { 
		_equilibriumComp 		= equilibriumComp; 
		_equilibriumReceiver 	= equilibriumReceiver;
		_equilibriumEvent 		= equilibriumEvent;
		if(!_state[2][0]) setFold(1);
		else setFold(getFold());
	}
//	public void setAmpScale(float ampScale) { 
//		_ampScale = ampScale;
//		setFold(_fold, _compsZYX, (_dasInfTxt==null)?null:_dasInfTxt, ampScale); 
//	}
	
	
	public void setAmpScale(float ampScale) {
		_ampScale = ampScale;
		for(int i=0, is=0; i<_sensors.length; i++) {
			float h = i+1;
			for(int j=0; j<_sensors[i].getNumOfComps(); j++, is++) {
				int n = _nx.get(is);
				float[] x1 = _x1.get(is);
				float[] x2 = _x2.get(is);
				for(int k=0; k<n; k++) x1[k] = h + (x1[k]-h)*ampScale;
			}
		}
		repaint();
	}
	public void setFold(int fold) { setFold(fold, _compsZYX, (_seg2InfTxt==null)?null:_seg2InfTxt, _ampScale); }
	public void setFold(int fold, SeismicTraceComponent [] compZYX, SEG2InfTxt seg2InfTxt, float ampScale) {
		if(compZYX!=null) {
			_comps = new SeismicTraceComponent[compZYX.length];
			for(int i=0; i<_comps.length; i++) { _comps[i] = compZYX[i].copy(); }
			for(int i=0; i<_comps.length; i++) { _comps[i].multiply(-1.0f); }
			for(int i=0; i<_comps.length; i++) { _comps[i].changeNan(0.0f); }

			_compsZYX 		= compZYX;
		}
		int k = _comps.length/fold;
		_sensors = new SeismicTraceSensor[k];
		_sensorsZYX = new SeismicTraceSensor[k];
		_ampScale = ampScale;
		//System.out.println("k="+k);
		float a1 = 0;
		k = 0;
		for(int i=0; i<_sensors.length; i++) {
//			float r = 0;
//			if(vcPW.getTotalNumOfReceivers()==_sensors.length) r = (float)vcPW.distance(0, i);
//			else r = (float)vcPW.distance(0, i/3);
			SeismicTraceComponent [] sensorComp = new SeismicTraceComponent[fold];
			SeismicTraceComponent [] sensorCompZYX = new SeismicTraceComponent[fold];
			for(int j=0; j<fold; j++) {
				_comps[k].add(-_comps[k].getAverageAmplitude());
				//_comps[k].multiply(r);
				if(_equilibriumComp) {
					a1 = _comps[k].normalize();
					//if(j==2) System.out.println("i="+i+" k="+k+" "+a1);
					_comps[k].multiply((float)(0.5*ampScale));
				}
				//System.out.println("i="+i+" min="+min(_comps[k].getData())+" max="+max(_comps[k].getData()));
				sensorCompZYX[j] = _compsZYX[k];
				sensorComp[j] = _comps[k++];
			}
			_sensors[i] = new SeismicTraceSensor(0, i, sensorComp);
			_sensorsZYX[i] = new SeismicTraceSensor(0, i, sensorCompZYX);
			if(_equilibriumReceiver) {
				_sensors[i].normalize();  //1 to -1
				_sensors[i].multiply((float)(0.5*ampScale));  
			}
			//_sensors[i].centerToAverage();
		}

		//System.out.println("equilibriumComp="+_equilibriumComp+" 2="+_equilibriumReceiver+" 3="+_equilibriumEvent);

		if(_equilibriumEvent) {
			//normalize
			float max = _sensors[0].getMaxAbsAmp();
			for(int i=1; i<_sensors.length; i++) {
				float a = _sensors[i].getMaxAbsAmp();
				max = max>a?max:a;
			}
			//for(int i=0; i<_sensors.length; i++) System.out.println(i+" "+_sensors[i].getMaxAbsAmp()/max+" ");
			float scalor = 1.0f;
			if(max!=0.0) scalor = (float)(0.5/max);
			scalor *= ampScale;
			//System.out.println("max="+max+" 4="+scalor);
			for(int i=0; i<_sensors.length; i++) { _sensors[i].multiply(scalor); }			
			//System.out.println("equilibriumComp="+_equilibriumComp+" 2="+_equilibriumReceiver+" 3="+_equilibriumEvent+" 4="+scalor);
		}
		
		float min = -0.5f;
		float max = 0.5f;
		_pX1 = new float[_comps.length][2]; 
		_shX1 = new float[_comps.length][2]; 
		_svX1 = new float[_comps.length][2]; 
		_pX2 = new float[_comps.length][2]; 
		_pcX2 = new float[_comps.length][2]; 
		_shX2 = new float[_comps.length][2]; 
		_shcX2 = new float[_comps.length][2]; 
		_svX2 = new float[_comps.length][2]; 
		_svcX2 = new float[_comps.length][2]; 

		setP1(_pX1, min, max);
		setP1(_shX1, min, max);
		setP1(_svX1, min, max);
		
		setPickFromComp();
		
		k = 0;
		float height = 1;
		for(int i=0; i<_sensors.length; i++) {
			for(int j=0; j<fold; j++, k++) {
				_sensors[i].getComp()[j].add(height);
				_pX1[k][0] += height; _pX1[k][1] += height;
				_shX1[k][0] += height; _shX1[k][1] += height;
				_svX1[k][0] += height; _svX1[k][1] += height;

				//System.out.println("i="+i+" j="+j+" min="+_sensors[i].getComp()[j].getAbsMin()+" max="+_sensors[i].getComp()[j].getAbsMax());
			}
			height += 1;
		}
		
		float [][] x1 = new float[_comps.length][];
		float [][] x2 = new float[_comps.length][];
		float [] x = new float[_comps[0].getNumOfSamples()];
		for(int i=0; i<x.length; i++) { x[i] = i; }

		for(int i=0; i<x2.length; i++) { 
			x1[i] = _comps[i].getData(); 
			//System.out.println("i="+i+" min="+min(x1[i])+" max="+max(x1[i]));
			x2[i] = x; 
		}
		set(x1, x2);
		
		_kLayer = new boolean[_comps.length];
		setKLayer(false);
		repaint();
	}
	public void setPickFromComp() {		
		for(int i=0; i<_pX1.length; i++) {
			int index = (int)_comps[i].getPpick(); 
			if(index<=10) index = 0;
			//System.out.println("indexc="+indexc);
			_pX2[i][0] = index;
			_pX2[i][1] = index;
			
			index = (int)_comps[i].getSHpick();
			if(index<=10) index = 0;
			_shX2[i][0] = index;
			_shX2[i][1] = index;

			index = (int)_comps[i].getSVpick();
			if(index<=10) index = 0;
			_svX2[i][0] = index;
			_svX2[i][1] = index;

			//System.out.println("p="+_pX2[i][0]+" sh="+_shX2[i][0]+" sv="+_svX2[i][0]);
		}
		for(int i=0; i<_pX1.length; i++) {
			int indexc = 0;
			indexc = (int)_comps[i].getPcpick(); 
			if(indexc<=10) indexc = 0;
			_pcX2[i][0] = indexc;
			_pcX2[i][1] = indexc;

			indexc = (int)_comps[i].getSHcpick();
			if(indexc<=10) indexc = 0;
			_shcX2[i][0] = indexc;
			_shcX2[i][1] = indexc;

			indexc = (int)_comps[i].getSVcpick();
			if(indexc<=10) indexc = 0;
			_svcX2[i][0] = indexc;
			_svcX2[i][1] = indexc;

			//System.out.println("pc="+_pcX2[i][0]+" shc="+_shcX2[i][0]+" svc="+_svcX2[i][0]);
		}
	}
//	public void setPicks(int iCol, double vx) { 
//		for(int i=0; i<_sensors.length; i++) { setPicks(iCol, vx, i+0.6, false);  }
//	}
//	
//	public void setPicks(int iCol, double vx, double vy) { setPicks(iCol, vx, vy, true); }
//	public void setPicks(int iCol, double vx, double vy, boolean refresh) {
//		int iSensor = (int)(vy-0.5);
//		int index = (int)vx;
//		
//		for(int i=0, k=0; i<_sensors.length; i++) {
//			if(iSensor==i) {
//				for(int j=0; j<_sensors[i].getNumOfComps(); j++) {
//					setP2(k, iCol, index, _seg2InfTxt);
//					k++;
//				}
//			} else {
//				k += _sensors[i].getNumOfComps();
//			}
//		}
//		if(refresh) repaint();
//	}
	public int getPicks(int iCol, double vy) {
		int iSensor = (int)(vy-0.5);
		
		for(int i=0, k=0; i<_sensors.length; i++) {
			if(iSensor==i) {
				for(int j=0; j<_sensors[i].getNumOfComps(); j++) {
					if(iCol==0) { // P-wave
						return (int)_pX2[k][0];
					} else if(iCol==1) { //SH-wave
						return (int)_shX2[k][0];
					} else if(iCol==2) { //SV-wave
						return (int)_svX2[k][0];
					} else { }
					k++;
				}
			} else {
				k += _sensors[i].getNumOfComps();
			}
		}

		return 0;
	}
	public MSEvent getMSEvent() 				{ return _msEvent; }
	public void setMSEvent(MSEvent msEvent) 	{ _msEvent = msEvent; }
	
	public SeismicTraceSensor [] getSensorsZYX() { return _sensorsZYX; }
	
	private void setP1(float [][] pX1, float min, float max) {
		for(int i=0; i<pX1.length; i++) {
			pX1[i][0] = max;
			pX1[i][1] = min;
		}
	}
//	public void setP2(SEG2InfTxt seg2InfTxt) {
//		setP2(_pX2, 0, seg2InfTxt);
//		setP2(_shX2, 1, seg2InfTxt);
//		setP2(_svX2, 2, seg2InfTxt);
//		setP2(_pcX2, 3, seg2InfTxt);
//		setP2(_shcX2, 4, seg2InfTxt);
//		setP2(_svcX2, 5, seg2InfTxt);
//	}
//	public void setP2(SEG2InfTxt seg2InfTxt) {
//		setP2(_pX2, 0, seg2InfTxt);
//		setP2(_shX2, 1, seg2InfTxt);
//		setP2(_svX2, 2, seg2InfTxt);
//		setP2(_pcX2, 3, seg2InfTxt);
//		setP2(_shcX2, 4, seg2InfTxt);
//		setP2(_svcX2, 5, seg2InfTxt);
//	}
//	private void setP2(float [][] pX2, int iCol, SEG2InfTxt seg2InfTxt) {
//		int index = 0;
//		for(int i=0; i<pX2.length; i++) {
//			index = seg2InfTxt.getData(i, iCol);
//			//System.out.print(index+" ");
//			if(index<=10) index = 0;
//			pX2[i][0] = index;
//			pX2[i][1] = index;
//		}
//	}
	public int getP2(int iComp, int iCol) {
		if(iCol==0) { // P-wave
			return (int)_pX2[iComp][0];
		} else if(iCol==1) { //SH-wave
			return (int)_shX2[iComp][0];
		} else if(iCol==2) { //SV-wave
			return (int)_svX2[iComp][0];
		} else { return 0; }
	}
//	private void setP2(int iRow, int iCol, int index, SEG2InfTxt seg2InfTxt) {
//		if(index<=10) index = 0;
//		if(seg2InfTxt!=null) seg2InfTxt.setData(iRow, iCol, index);
//		//seg2InfTxt.setPickEdited(true);
//		if(iCol==0) { // P-wave
//			_pX2[iRow][0] = index; 		_pX2[iRow][1] = index;
//		} else if(iCol==1) { //SH-wave
//			_shX2[iRow][0] = index; 	_shX2[iRow][1] = index;
//		} else if(iCol==2) { //SV-wave
//			_svX2[iRow][0] = index; 	_svX2[iRow][1] = index;
//		} else { }
//	}
	
	public SEG2InfTxt getSEG2InfTxt() 		{ return _seg2InfTxt; }

	public void calSNR() { calSNR(_seg2InfTxt); }
	public void calSNR(SEG2InfTxt seg2InfTxt) {
		float [] v = new float[_comps[0].getData().length];
		for(int i=0, k=0; i<_comps.length; i++) {
			k = i/3;
			for(int j=0; j<v.length; j++) v[j] = _comps[i].getData(j)-(k+1);
			
			int windowLen = 30;
			int index = 0;
			for(int iCol=3; iCol<5; iCol++) {
				if(iCol==3) index = (int)_pX2[i][0];
				else if(iCol==4) index = (int)_shX2[i][0];
				else if(iCol==5) index = (int)_svX2[i][0];
				double snr = 0.0;
				if(index>10) snr = calSNR(index, v, windowLen);
				seg2InfTxt.setData(i, iCol, (int)(snr*1.0e6));
			}
		}
	}
	private double calSNR(int index, float [] v, int windowLen) {
		double signalSum = 0.0;
		double noiseSum = 0.0;
		
		int n1 = index-windowLen;
		int p1 = n1<0?0:n1;
		int n2 = index+windowLen;
		int p2 = n2>v.length?v.length:n2;
		
		int len1 = index-p1;
		int len2 = p2-index;
		int len = len1<len2?len1:len2;
		
		for(int i=1, k=0; i<len; i++) {
			k = index+i; signalSum += Math.pow(v[k], 2);
			k = index-i; noiseSum += Math.pow(v[k], 2);
		}
		double SNR = 10.0 * Math.log10(signalSum / noiseSum);
		//double SNR = signalSum / noiseSum;
		return SNR;
	}
	public double calSNR2_1() {
		double SNR = 0.0;
		int windowLen = 40;
		int len1 = windowLen;
		int len2 = windowLen;
		int nSample = _comps[0].getData().length;
		int a = 0;
		for(int i=0; i<_comps.length; i++) {
			int index = (int)_shX2[i][0];
			if(index>10) {
				a = index;
				if(a<len1) len1 = a;
				
				a = nSample-index;
				if(a<len2) len2 = a;
			}
		}
		
		float [] v = new float[_comps[0].getData().length];
		double noiseSum = 0.0;
		double signalSum = 0.0;
		for(int i=0, k=0; i<_comps.length; i++) {
			int index = (int)_shX2[i][0];
			if(index>10) {
				k = i/3;
				for(int j=0; j<v.length; j++) v[j] = _comps[i].getData(j)-(k+1);
				for(int j=0; j<len1; j++)  noiseSum += v[index-j]*v[index-j]; 
				for(int j=0; j<len2; j++)  signalSum += v[index+j]*v[index+j];
			}
		}
		
		if(noiseSum==0.0) {
			SNR = 0.0;
		} else {
			SNR = 10.0 * Math.log10( (signalSum/len2) / (noiseSum/len1) );
		}
		return SNR;
	}
	
	public double calSNR3() {
		double SNR = 0.0;
		int len1 = 40;
		int len2 = 100;
		
		double noiseSum = 0.0;
		double signalSum = 0.0;
		float [] v = null;
		for(int i=0, is=0; i<_sensors.length; i++) {
			for(int j=0; j<_sensors[i].getNumOfComps(); j++, is++) {
				v = _compsZYX[is].getData();
				int sPickIndex = (int)_shX2[is][0];
				int k1 = sPickIndex - len1;
				int k2 = sPickIndex + len2;
				if(k1>10 && k1<v.length && k2>10 && k2<v.length) {						
					for(int k=k1; k<=sPickIndex; k++)  noiseSum += v[k]*v[k]; 
					for(int k=sPickIndex; k<=k2; k++)  signalSum += v[k]*v[k]; 
				}					
			}
		}
		if(noiseSum==0.0)  	SNR = 0.0;
		else 				SNR = 10.0 * Math.log10( (signalSum/len2) / (noiseSum/len1) );
		return SNR;
	}
	

	public CommonFrame getFrame() 					{ return _frame; }
	public int getMouseX() 							{ return _mouseX; }
	public int getMouseY() 							{ return _mouseY; }
	public int getMouseXTime() { 
		Projector hp = getHorizontalProjector();
		Transcaler ts = getTranscaler();
		double ux = ts.x(_mouseX);
		double vx = hp.v(ux);
		//System.out.println(vx);
		return (int)vx; 
	}
	
	private void setKLayer(boolean on) 				{ for(int i=0; i<_comps.length; i++) {_kLayer[i] = on;} }
	public void setMouseX(int mouseX) 				{ _mouseX = mouseX; }
	public void setMouseY(int mouseY) 				{ _mouseY = mouseY; }
	
	public SeismicTraceComponent [] getSeismicTraceComponent0() { return _compsZYX; }
	public SeismicTraceComponent [] getSeismicTraceComponent() 	{ return _comps; }
	public SeismicTraceSensor [] getSeismicTraceSensor() 		{ return _sensors; }

	public boolean getCompZSelected() 		{ return _compZSelected; }
	public boolean getCompYSelected() 		{ return _compYSelected; }
	public boolean getCompXSelected() 		{ return _compXSelected; }
	public boolean getPickPSelected() 		{ return _pickPSelected; }
	public boolean getPickSHSelected() 		{ return _pickSHSelected; }
	public boolean getPickSVSelected() 		{ return _pickSVSelected; }
	public boolean getPickPcSelected() 		{ return _pickPcSelected; }
	public boolean getPickSHcSelected() 	{ return _pickSHcSelected; }
	public boolean getPickSVcSelected() 	{ return _pickSVcSelected; }

	public void setCompZSelected(boolean selected) 		{ _compZSelected = selected; repaint(); }
	public void setCompYSelected(boolean selected) 		{ _compYSelected = selected; repaint(); }
	public void setCompXSelected(boolean selected) 		{ _compXSelected = selected; repaint(); }
	public void setPickPSelected(boolean selected) 		{ _pickPSelected = selected; repaint(); }
	public void setPickSHSelected(boolean selected) 	{ _pickSHSelected = selected; repaint(); }
	public void setPickSVSelected(boolean selected) 	{ _pickSVSelected = selected; repaint(); }
	public void setPickPcSelected(boolean selected) 	{ _pickPcSelected = selected; repaint(); }
	public void setPickSHcSelected(boolean selected) 	{ _pickSHcSelected = selected; repaint(); }
	public void setPickSVcSelected(boolean selected) 	{ _pickSVcSelected = selected; repaint(); }

	public void enableLineSelection(boolean enabled) 	{ _enableLineSelection = enabled; repaint(); }

	public int getSelectedCompIndex() {
		for(int i=0; i<_kLayer.length; i++) { if(_kLayer[i]) return i; }
		return -1;
	}
	public int getSelectedReceiverIndex() {
		int k = getSelectedCompIndex();
		if(k<0) return -1;
		return k/_fold;
	}
	public SeismicTraceComponent [] getSelectedComp0() 		{ return getSelectedComp(0); }
	public SeismicTraceComponent [] getSelectedComp() 		{ return getSelectedComp(1); }
	public SeismicTraceComponent [] getSelectedComp(int id) {
		int k = getSelectedCompIndex();
		if(k<0) return null;
		int n = 0;
		for(int i=0; i<_kLayer.length; i++) { if(_kLayer[i]) n++; }
		SeismicTraceComponent [] comps = new SeismicTraceComponent[n];
		n = 0;
		for(int i=0; i<_kLayer.length; i++) { 
			if(_kLayer[i]) {
				if(id==0) comps[n] = _compsZYX[i];
				else comps[n] = _comps[i];
				n++; 
			}
		}
		return comps;	
	}
	
	public int getFirstValidPwavePickIndex() {
		for(int i=0; i<_pX2.length; i++) { if(_pX2[i][0]>0.0f) return i; }
		return -1;
	}
	public float [][] getHodogramXY() 				{ return _hodogramXY; }
	public int getHodogramReceiverIndex() 			{ return _hodogramReceiverIndex; }
	public void updateHodogramXY(int radius, boolean useFirstValidTrace) {
		int k = getSelectedCompIndex();
		if(k<0) {
			if(useFirstValidTrace) {
				k = getFirstValidPwavePickIndex();
				if(k<0) return;
			}
			else return; 
		}
		_hodogramReceiverIndex = k/_fold;
		
		int iPickSample = (int)_pX2[k][0];
		int index0 = iPickSample-radius;
		int index1 = iPickSample+radius;
		if(index0<0) return;
		
		int nSamples = index1-index0+1;
		//System.out.println("index0="+index0+" index1="+index1+" n="+nSamples+" k="+k+" radius="+radius);
		if(_hodogramXY==null || _hodogramXY.length!=nSamples) _hodogramXY = new float[2][nSamples];
		for(int i=index0, j=0; i<=index1; i++, j++) {
			_hodogramXY[0][j] = _compsZYX[k+2].getSample(i);
			_hodogramXY[1][j] = _compsZYX[k+1].getSample(i);
		}
		
		//normalize
		float absMax = -1;
		float a = 0;
		for(int i=0; i<_hodogramXY.length; i++) {
			for(int j=0; j<_hodogramXY[i].length; j++) {
				a = Math.abs(_hodogramXY[i][j]);
				absMax = absMax>a?absMax:a;
			}
		}
		if(absMax==0.0f) return;
		for(int i=0; i<_hodogramXY.length; i++) {
			for(int j=0; j<_hodogramXY[i].length; j++) {
				_hodogramXY[i][j] /= absMax;
			}
		}
	}
	
	public void exponentiallySmooth(boolean global, boolean realData,  int halfWidth) {
		int m = halfWidth; //half-width m of a boxcar filter
		float sigma = sqrt(m*(m+1)/3.0f);  //a Gaussian filter with a specified half-width sigma
		float ss = sigma*sigma;
		float a = (1.0f+ss-sqrt(1.0f+2.0f*ss))/ss;
		boolean enable = false;
		float[] x = null;
		
		for(int i=0, is=0; i<_sensors.length; i++) {
			for(int j=0; j<_sensors[i].getNumOfComps(); j++, is++) {
				enable = false;
				if(global) enable = true;
				else {
					if(_kLayer[is]) { enable = true; }
				}
				
				if(enable) {
					if(realData) x = _compsZYX[is].getData();
					else x = _x1.get(is);
					DSP.exponentialFilterEndingWithZeroSlope(a, x, null);
				}
			}
		}
		if(realData) setFold(getFold(), _compsZYX, _seg2InfTxt, _ampScale);
		else repaint();
	}
//	public void denoising(boolean global) {
//		boolean enable = false;
//		
//		for(int i=0, is=0; i<_sensors.length; i++) {
//			for(int j=0; j<_sensors[i].getNumOfComps(); j++, is++) {
//				enable = false;
//				if(global) enable = true;
//				else { if(_kLayer[is]) { enable = true; } }
//				
//				if(enable) {
//					_compsZYX[is].denoising();
//				}
//			}
//		}
//		setFold(getFold(), _compsZYX, _seg2InfTxt);
//	}
	public void multiply(float a) {
		for(int i=0; i<_compsZYX.length; i++) _compsZYX[i].multiply(a);
	}
	
	public void toVelocity(boolean global, boolean fromHeader, double a) {
		boolean enable = false;
//		% ControlPara.DataCalib = 10^6/104000; %Converts amplitude scale from minivolts to micron/sec for PinnTech tools.
//		ControlPara.DataCalib = 1/104000; %Converts amplitude scale from minivolts to micron/sec for PinnTech tools.
//		% ControlPara.DataCalib = ControlPara.DataCalib * 10^(-6);    % convert to m/s
//		% ControlPara.DataCalib = 2.443/1000; %Mike's numbers to Converts amplitude scale from volts to cm/sec for PinnTech tools.
		//System.out.println("begin="+a);
		for(int i=0, is=0; i<_sensors.length; i++) {
			for(int j=0; j<_sensors[i].getNumOfComps(); j++, is++) {
				enable = false;
				if(global) enable = true;
				else { if(_kLayer[is]) { enable = true; } }
				
				if(enable) {
					//if(is==0) for(int ip=0; ip<10; ip++)  System.out.println(String.format("%.4f ", _compsZYX[is].getSample(ip)));
					//float[] x1 = _compsZYX[is].getData();
					//DSP.multiply((float)a, x1, null);
					//String text = _compsZYX[is].getSEG2Trace().toString(1);
					if(fromHeader) a = _compsZYX[is].getSEG2Trace().getStringBlock().getTRANSDUCTION_CONSTANT();
					//System.out.print(" is="+is+" "+(float)a);
					_compsZYX[is].multiply((float)(a));
				}
			}
		}
		//System.out.println("end="+a);
		setFold(getFold(), _compsZYX, _seg2InfTxt, _ampScale);
	}
	public void velocityToAmplitude(boolean global, int iMethod) {
		boolean enable = false;
		
		for(int i=0, is=0; i<_sensors.length; i++) {
			for(int j=0; j<_sensors[i].getNumOfComps(); j++, is++) {
				enable = false;
				if(global) enable = true;
				else { if(_kLayer[is]) { enable = true; } }
				
				if(enable) {
					//float[] x1 = _compsZYX[is].getData();
					//DSP.cumSum(x1, null);
					_compsZYX[is].velocityToAmplitude(iMethod);
				}
			}
		}
		setFold(getFold(), _compsZYX, _seg2InfTxt, _ampScale);
	}
	
	public void flipPolarity(boolean global) {
		if(_msEvent==null) return;
		boolean enable = false;
		
		for(int i=0, k=0, is=0; i<_msEvent.getNumOfObsWells(); i++) {
			ObsWell well 	= _msEvent.getObsWells(i);
			for(int j=0; j<well.getNumOfSensors(); j++) {
				enable = false;
				if(global) enable = true;
				else { if(_kLayer[is]) { enable = true; } }
				
				if(enable) {
					_sensorsZYX[k].flipPolarity();
				}
				
				k++;
				is += 3;
			}
		}
		setFold(getFold(), _compsZYX, _seg2InfTxt, _ampScale);
	}
	public void swapChannelOrderToZYX(boolean global, int inputChannelOrder) {
		if(_msEvent==null) return;
		boolean enable = false;
		
		for(int i=0, k=0, is=0; i<_msEvent.getNumOfObsWells(); i++) {
			ObsWell well 	= _msEvent.getObsWells(i);
			for(int j=0; j<well.getNumOfSensors(); j++) {
				enable = false;
				if(global) enable = true;
				else { if(_kLayer[is]) { enable = true; } }
				
				if(enable) {
					_sensorsZYX[k].swapChannelOrderToZYX(inputChannelOrder);
				}
				
				k++;
				is += 3;
			}
		}
		setFold(getFold(), _compsZYX, _seg2InfTxt, _ampScale);
	}
	public void swapChannelOrderToXYZ(boolean global, int inputChannelOrder) {
		if(_msEvent==null) return;
		boolean enable = false;
		
		for(int i=0, k=0, is=0; i<_msEvent.getNumOfObsWells(); i++) {
			ObsWell well 	= _msEvent.getObsWells(i);
			for(int j=0; j<well.getNumOfSensors(); j++) {
				enable = false;
				if(global) enable = true;
				else { if(_kLayer[is]) { enable = true; } }
				
				if(enable) {
					_sensorsZYX[k].swapChannelOrderToXYZ(inputChannelOrder);
				}
				
				k++;
				is += 3;
			}
		}
		setFold(getFold(), _compsZYX, _seg2InfTxt, _ampScale);
	}
	
	public boolean headerExists(int iComp, String headerKey) {
		String value = _compsZYX[iComp].getSEG2Trace().getStringBlock().getValue(headerKey);
		//System.out.println("key="+headerKey+" value="+value);
		return value!=null;
	}
	
	public void toENU(boolean global, int iRotation) {
		if(_msEvent==null) return;
		boolean enable = false;
		
		for(int i=0, k=0, is=0; i<_msEvent.getNumOfObsWells(); i++) {
			ObsWell well 	= _msEvent.getObsWells(i);
			for(int j=0; j<well.getNumOfSensors(); j++) {
				Sensor 	sensor 	= well.getSensor(j);
				sensor.calRotationMatrixXYZ2ENUp();
				double [][] rm = sensor.getRotationMatrix();  //{{l1, m1, n1}, {l2, m2 , n2}, {l3, m3, n3}}
				enable = false;
				if(global) enable = true;
				else { if(_kLayer[is]) { enable = true; } }
				
				if(enable) {
					//_sensorsZYX[k].toENU(rm);
					_sensorsZYX[k].toENU(iRotation, rm);
				}
				
				k++;
				is += 3;
			}
		}
		setFold(getFold(), _compsZYX, _seg2InfTxt, _ampScale);
	}
	public void toP_SV_SH(boolean global) {
		if(_msEvent==null) return;
		boolean enable = false;
		
		for(int i=0, k=0, is=0; i<_msEvent.getNumOfObsWells(); i++) {
			ObsWell well 	= _msEvent.getObsWells(i);
			for(int j=0; j<well.getNumOfSensors(); j++) {
				Sensor 	sensor 	= well.getSensor(j);
				//double [][] rm = sensor.getRotationMatrix();  //{{l1, m1, n1}, {l2, m2 , n2}, {l3, m3, n3}}
				enable = false;
				if(global) enable = true;
				else { if(_kLayer[is]) { enable = true; } }
				
				if(enable) {
					//all coordinates are in ENU order
					double [] src= new double [] {_msEvent.getY(), _msEvent.getX(), _msEvent.getZ()};         
					double [] rec= new double [] {sensor.getY(), sensor.getX(), sensor.getZ()}; 
					double [] rm = new double[] { rec[0]-src[0], rec[1]-src[1], (src[2]-rec[2]) };
					
					double rxy = Math.sqrt(rm[0]*rm[0]+rm[1]*rm[1]);
					double theCosK = rm[0]/rxy;
					double theSinK = rm[1]/rxy;
					double [][] rz = new double[][]{ 
							{theCosK, 	theSinK, 	0},
							{-theSinK, 	theCosK, 	0},
							{0, 		0, 			1}							
					};
					
					double r = Math.sqrt(rm[0]*rm[0]+rm[1]*rm[1]+rm[2]*rm[2]);
					//double [] directionCos = new double[] { rm[0]/r, rm[1]/r, rm[2]/r };
					
					double theCosP = rm[2]/r;
					double theSinP = rxy/r;
					double [][] ry = new double[][]{ 
							{theCosP, 	0, 	-theSinP},
							{0, 		1, 	0},
							{theSinP, 	0, 	theCosP}							
					};
					
					//ry*rz
					double [][] rotationMatrix = new double[3][3];
					for(int i1=0; i1<3; i1++) {
						for(int i2=0; i2<3; i2++) {
							double sum = 0;
							for(int i3=0; i3<3; i3++) sum += ry[i1][i3]*rz[i3][i2];
							rotationMatrix[i1][i2] = sum;
						}
					}
					
					_sensorsZYX[k].applyRotation(3, rotationMatrix);
				}
				
				k++;
				is += 3;
			}
		}
		setFold(getFold(), _compsZYX, _seg2InfTxt, _ampScale);
	}
	
	public void applyBandPassFilter(boolean global, BandPassFilter bandPassFilter, ButterworthFilter butterworthFilter) {
		if(_msEvent==null) return;
		boolean enable = false;
		
		for(int i=0, k=0, is=0; i<_msEvent.getNumOfObsWells(); i++) {
			ObsWell well 	= _msEvent.getObsWells(i);
			for(int j=0; j<well.getNumOfSensors(); j++) {
				enable = false;
				if(global) enable = true;
				else { if(_kLayer[is]) { enable = true; } }
				
				if(enable) {
					if(bandPassFilter!=null) {
					_compsZYX[is].applyBandPassFilter(bandPassFilter);
					_compsZYX[is+1].applyBandPassFilter(bandPassFilter);
					_compsZYX[is+2].applyBandPassFilter(bandPassFilter);
					} else if(butterworthFilter!=null) {
						_compsZYX[is].applyButterworthFilter(butterworthFilter);
						_compsZYX[is+1].applyButterworthFilter(butterworthFilter);
						_compsZYX[is+2].applyButterworthFilter(butterworthFilter);
					} 
				}
				
				k++;
				is += 3;
			}
		}
		setFold(getFold(), _compsZYX, _seg2InfTxt, _ampScale);
	}
	

	public void print(double [][] M) {
		for(int i=0; i<M.length; i++) {
			for(int j=0; j<M[i].length; j++) {
				System.out.print(M[i][j]+" ");
			}
			System.out.println(" ");
		}
	}
	
	private boolean allPicksInside(int nPointAhead, int nPointAfter) {
		int nSample = _compsZYX[0].getNumOfSamples();
		//System.out.println("nSample="+nSample);
		int k = 0;
		for(int i=0; i<_compsZYX.length; i++) { 
			if(_pX2[i][0]>10) {
				k = (int)_pX2[i][0]-nPointAhead; 		if(k<0) 		return false;
				k = (int)_pX2[i][0]+nPointAfter; 		if(k>=nSample) 	return false;
			}
			if(_shX2[i][0]>10) {
				k = (int)_shX2[i][0]-nPointAhead; 		if(k<0) 		return false;
				k = (int)_shX2[i][0]+nPointAfter; 		if(k>=nSample) 	return false;
			}
			//k = (int)_svX2[i][0]-nPointAhead; 		if(k<0) 		return false;
			//k = (int)_svX2[i][0]+nPointAfter; 		if(k>=nSample) 	return false;
		}
		return true;
	}
	public void sampleDisplacement(boolean enable, double [] D, int offset) {
		int indexP = 0;
		int indexSH = 0;
		for(int i=0; i<D.length; i++) D[i] = _INVALID_D;
		if(!enable) return;
		
		//Pz SHz Py SHy Px SHx
//		for(int is=0, i=0, k=0; is<_sensors.length; is++, i=i+3) {
//			indexP = (int)_pX2[i][0];
//			indexSH = (int)_shX2[i][0];
//			D[k++] = indexP<=10 ? _INVALID_D:_sensorsZYX[is].getCompZ().getData(indexP);
//			D[k++] = indexSH<=10 ? _INVALID_D:_sensorsZYX[is].getCompZ().getData(indexSH);
//			D[k++] = indexP<=10 ? _INVALID_D:_sensorsZYX[is].getCompY().getData(indexP);
//			D[k++] = indexSH<=10 ? _INVALID_D:_sensorsZYX[is].getCompY().getData(indexSH);
//			D[k++] = indexP<=10 ? _INVALID_D:_sensorsZYX[is].getCompX().getData(indexP);
//			D[k++] = indexSH<=10 ? _INVALID_D:_sensorsZYX[is].getCompX().getData(indexSH);
//			//System.out.println("is="+is+" i="+i+" k="+k+"in="+indexSH+" a="+_sensorsZYX[is].getCompY().getData(indexSH));
//		}

		int numOfSamples 	= _sensorsZYX[0].getCompX().getNumOfSamples();
		//PE SHE PN SHN PD SHD
		for(int is=0, i=0, k=0; is<_sensors.length; is++, i=i+3) {
			indexP = (int)_pX2[i][0]+offset;
			indexP = indexP<=10 ? -1:indexP;
			indexP = indexP>=numOfSamples ? -1:indexP;
			
			indexSH = (int)_shX2[i][0]+offset;
			indexSH = indexSH<=10 ? -1:indexSH;
			indexSH = indexSH>=numOfSamples ? -1:indexSH;
			
			D[k++] = indexP<=10 ? _INVALID_D:_sensorsZYX[is].getCompX().getData(indexP);
			D[k++] = indexSH<=10 ? _INVALID_D:_sensorsZYX[is].getCompX().getData(indexSH);
			D[k++] = indexP<=10 ? _INVALID_D:_sensorsZYX[is].getCompY().getData(indexP);
			D[k++] = indexSH<=10 ? _INVALID_D:_sensorsZYX[is].getCompY().getData(indexSH);
			D[k++] = indexP<=10 ? _INVALID_D:_sensorsZYX[is].getCompZ().getData(indexP);
			D[k++] = indexSH<=10 ? _INVALID_D:_sensorsZYX[is].getCompZ().getData(indexSH);
			//System.out.println("is="+is+" i="+i+" k="+k+"in="+indexSH+" a="+_sensorsZYX[is].getCompY().getData(indexSH));
		}
	}
	/**
	 * Constructs a view of points (x1,x2) with specified x2 coordinates.
	 * The corresponding coordinates x1 are assumed to be 0, 1, 2, ....
	 * @param x2 array of x2 coordinates.
	 */
	public TraceView(float[] x2) {
		float[] x1 = rampfloat(0.0f,1.0f,x2.length);
		set(x1,x2);
	}

	/**
	 * Constructs a view of points (x1,x2) for a sampled function x2(x1).
	 * @param s1 the sampling of x1 coordinates.
	 * @param x2 array of x2 coordinates.
	 */
	public TraceView(Sampling s1, float[] x2) {
		set(s1,x2);
	}

	public TraceView(CommonFrame frame, float[] x1, float[] x2) {
		this(x1,x2);
		_frame = frame;
	}
	/**
	 * Constructs a view of points (x1,x2) with a single plot segment.
	 * The lengths of the specified arrays x1 and x2 must be equal.
	 * @param x1 array of x1 coordinates.
	 * @param x2 array of x2 coordinates.
	 */
	public TraceView(float[] x1, float[] x2) {
		set(x1,x2);
	}

	public TraceView(float[] x1, float[] x2, float[] x3, float[] orgX2) {
		this(x1,x2,x3);
		_orgX2 = orgX2;
	}
	/**
	 * Constructs a view of points (x1,x2) with multiple plot segments.
	 * The lengths of the specified arrays x1 and x2 must be equal.
	 * @param x1 array of arrays of x1 coordinates.
	 * @param x2 array of arrays of x2 coordinates.
	 */
	public TraceView(float[][] x1, float[][] x2) {
		set(x1,x2);
	}

	/**
	 * Constructs a view of points (x1,x2,x3) with a single plot segment.
	 * The lengths of the specified arrays x1 and x2 must be equal.
	 * If x3 is not null, its length must equal that of x1 and x2.
	 * @param x1 array of x1 coordinates.
	 * @param x2 array of x2 coordinates.
	 * @param x3 array of x3 coordinates; null, if none.
	 */
	public TraceView(float[] x1, float[] x2, float[] x3) {
		set(x1,x2,x3);
	}

	/**
	 * Constructs a view of points (x1,x2,x3) with multiple plot segments.
	 * The lengths of the specified arrays x1 and x2 must be equal.
	 * If x3 is not null, its length must equal that of x1 and x2.
	 * @param x1 array of arrays of x1 coordinates.
	 * @param x2 array of arrays of x2 coordinates.
	 * @param x3 array of arrays of x3 coordinates.
	 */
	public TraceView(float[][] x1, float[][] x2, float[][] x3) {
		set(x1,x2,x3);
	}

	/**
	 * Sets (x1,x2) coordinates for a sampled function x2(x1).
	 * @param s1 the sampling of x1 coordinates.
	 * @param x2 array of x2 coordinates.
	 */
	public void set(Sampling s1, float[] x2) {
		Check.argument(s1.getCount()==x2.length,"s1 count equals x2 length");
		int n1 = x2.length;
		float[] x1 = new float[n1];
		for (int i1=0; i1<n1; ++i1)
			x1[i1] = (float)s1.getValue(i1);
		set(x1,x2);
	}

	/**
	 * Sets arrays of (x1,x2) coordinates for a single plot segment.
	 * The lengths of the specified arrays x1 and x2 must be equal.
	 * @param x1 array of x1 coordinates.
	 * @param x2 array of x2 coordinates.
	 */
	public void set(float[] x1, float[] x2) {
		set(x1,x2,null);
	}

	/**
	 * Sets arrays of (x1,x2,x3) coordinates for a single plot segment.
	 * The lengths of the specified arrays x1 and x2 must be equal.
	 * If x3 is not null, its length must equal that of x1 and x2.
	 * @param x1 array of x1 coordinates.
	 * @param x2 array of x2 coordinates.
	 * @param x3 array of x3 coordinates; null, if none.
	 */
	public void set(float[] x1, float[] x2, float[] x3) {
		Check.argument(x1.length==x2.length,"x1.length equals x2.length");
		if (x3!=null)
			Check.argument(x1.length==x3.length,"x1.length equals x3.length");
		_ns = 1;
		_nx.clear();
		_x1.clear();
		_x2.clear();
		_x3.clear();
		_nxmax = x1.length;
		_nx.add(x1.length);
		_x1.add(copy(x1));
		_x2.add(copy(x2));
		if (x3!=null)
			_x3.add(copy(x3));
		updateBestProjectors();
		repaint();
	}

	/**
	 * Sets array of arrays of (x1,x2) coordinates for multiple plot segments.
	 * The lengths of the specified arrays x1 and x2 must be equal.
	 * @param x1 array of arrays of x1 coordinates.
	 * @param x2 array of arrays of x2 coordinates.
	 */
	public void set(float[][] x1, float[][] x2) {
		set(x1,x2,null);
	}

	/**
	 * Sets array of arrays of (x1,x2,x3) coordinates for multiple plot segments.
	 * The lengths of the specified arrays x1 and x2 must be equal.
	 * If x3 is not null, its length must equal that of x1 and x2.
	 * @param x1 array of arrays of x1 coordinates.
	 * @param x2 array of arrays of x2 coordinates.
	 * @param x3 array of arrays of x3 coordinates; null, if none.
	 */
	public void set(float[][] x1, float[][] x2, float[][] x3) {
		Check.argument(x1.length==x2.length,"x1.length equals x2.length");
		if (x3!=null)
			Check.argument(x1.length==x3.length,"x1.length equals x3.length");
		_ns = x1.length;
		_nx.clear();
		_x1.clear();
		_x2.clear();
		_x3.clear();
		_nxmax = 0;
		for (int is=0; is<_ns; ++is) {
			Check.argument(x1[is].length==x2[is].length,
					"x1[i].length equals x2[i].length");
			_nxmax = max(_nxmax,x1[is].length);
			_nx.add(x1[is].length);
			_x1.add(copy(x1[is]));
			_x2.add(copy(x2[is]));
			if (x3!=null)
				_x3.add(copy(x3[is]));
		}
		updateBestProjectors();
		repaint();
	}

	/**
	 * Sets the orientation of (x1,x2) axes.
	 * @param orientation the orientation.
	 */
	public void setOrientation(Orientation orientation) {
		if (_orientation!=orientation) {
			_orientation = orientation;
			updateBestProjectors();
			repaint();
		}
	}

	/**
	 * Gets the orientation of (x1,x2) axes.
	 * @return the orientation.
	 */
	public Orientation getOrientation() {
		return _orientation;
	}

	/**
	 * Sets the color, line style, and mark style from a style string.
	 * This method provides a convenient way to set the most commonly
	 * specified attributes of lines and marks painted by this view.
	 * <p>
	 * To specify a color, the style string may contain one of "r" for red,
	 * "g" for green, "b" for blue, "c" for cyan, "m" for magenta, "y" for
	 * yellow, "k" for black, or "w" for white. If the style string contains 
	 * none of these colors, then the default color is used.
	 * <p>
	 * To specify a line style, the style string may contain one of "-" for 
	 * solid lines, "--" for dashed lines, "-." for dotted lines, or "--."
	 * for dash-dotted lines. If the style string contains none of these
	 * line styles, then no lines are painted.
	 * <p>
	 * To specify a mark style, the style string may contain one of "." for
	 * point, "+" for plus, "x" for cross, "o" for hollow circle", "O" for
	 * filled circle, "s" for hollow square, or "S" for filled square. If
	 * the style string contains none of these mark styles, then no marks
	 * are painted.
	 * @param style the style string.
	 */
	public void setStyle(String style) {

		// Color.
		if (style.contains("r")) {
			setLineColor(Color.RED);
			setMarkColor(Color.RED);
		} else if (style.contains("g")) {
			setLineColor(Color.GREEN);
			setMarkColor(Color.GREEN);
		} else if (style.contains("b")) {
			setLineColor(Color.BLUE);
			setMarkColor(Color.BLUE);
		} else if (style.contains("c")) {
			setLineColor(Color.CYAN);
			setMarkColor(Color.CYAN);
		} else if (style.contains("m")) {
			setLineColor(Color.MAGENTA);
			setMarkColor(Color.MAGENTA);
		} else if (style.contains("y")) {
			setLineColor(Color.YELLOW);
			setMarkColor(Color.YELLOW);
		} else if (style.contains("k")) {
			setLineColor(Color.BLACK);
			setMarkColor(Color.BLACK);
		} else if (style.contains("w")) {
			setLineColor(Color.WHITE);
			setMarkColor(Color.WHITE);
		} else {
			setLineColor(null);
			setMarkColor(null);
		}

		// Line style.
		if (style.contains("--.")) {
			setLineStyle(Line.DASH_DOT);
		} else if (style.contains("--")) {
			setLineStyle(Line.DASH);
		} else if (style.contains("-.")) {
			setLineStyle(Line.DOT);
		} else if (style.contains("-")) {
			setLineStyle(Line.SOLID);
		} else {
			setLineStyle(Line.NONE);
		}

		// Mark style.
		if (style.contains("+")) {
			setMarkStyle(Mark.PLUS);
		} else if (style.contains("x")) {
			setMarkStyle(Mark.CROSS);
		} else if (style.contains("o")) {
			setMarkStyle(Mark.HOLLOW_CIRCLE);
		} else if (style.contains("O")) {
			setMarkStyle(Mark.FILLED_CIRCLE);
		} else if (style.contains("s")) {
			setMarkStyle(Mark.HOLLOW_SQUARE);
		} else if (style.contains("S")) {
			setMarkStyle(Mark.FILLED_SQUARE);
		} else if (style.contains(".")) {
			int i = style.indexOf(".");
			if (i==0 || style.charAt(i-1)!='-')
				setMarkStyle(Mark.POINT);
		} else {
			setMarkStyle(Mark.NONE);
		}
	}

	/**
	 * Sets the line style.
	 * The default style is solid.
	 * @param style the line style.
	 */
	public void setLineStyle(Line style) {
		_lineStyle = style;
		repaint();
	}

	public Line getLineStyle() {
		return _lineStyle;
	}
	/**
	 * Sets the line width.
	 * The default width is zero, for the thinnest lines.
	 * @param width the line width.
	 */
	public void setLineWidth(float width) {
		if (_lineWidth!=width) {
			_lineWidth = width;
			updateBestProjectors();
			repaint();
		}
	}
	public float getLineWidth() {
		return _lineWidth;
	}

	/**
	 * Sets the line color.
	 * The default line color is the tile foreground color. 
	 * That default is used if the specified line color is null.
	 * @param color the line color; null, for tile foreground color.
	 */
	public void setLineColor(Color color) {
		if (!equalColors(_lineColor,color)) {
			_lineColor = color;
			repaint();
		}
	}
	public Color getLineColor() {
		return _lineColor;
	}

	/**
	 * Sets the mark style.
	 * The default mark style is none, for no marks.
	 * @param style the mark style.
	 */
	public void setMarkStyle(Mark style) {
		if (_markStyle!=style) {
			_markStyle = style;
			updateBestProjectors();
			repaint();
		}
	}

	public Mark getMarkStyle() {
		return _markStyle;
	}

	/**
	 * Sets the mark size.
	 * The default mark size is half the tile font size.
	 * The default is used if the specified mark size is zero.
	 * @param size the mark size.
	 */
	public void setMarkSize(float size) {
		if (_markSize!=size) {
			_markSize = size;
			updateBestProjectors();
			repaint();
		}
	}

	public float getMarkSize() {
		return _markSize;
	}

	/**
	 * Sets the mark color.
	 * The default mark color is the tile foreground color. 
	 * That default is used if the specified mark color is null.
	 * @param color the mark color.
	 */
	public void setMarkColor(Color color) {
		if (!equalColors(_markColor,color)) {
			_markColor = color;
			repaint();
		}
	}
	public Color getMarkColor() {
		return _markColor;
	}
	/**
	 * Sets the format used for text labels.
	 * The default format is "%1.4G".
	 * @param format the text format.
	 */
	public void setTextFormat(String format) {
		_textFormat = format;
		repaint();
	}

	private void aveArray(int i1, int i2) {
		float sum = 0.0f;
		for(int i=i1; i<=i2; i++) sum += _orgX2[i];
		float ave = (float)(sum/(i2-i1+1));
		float [] x2 = _x2.get(0);
		for(int i=i1; i<=i2; i++) x2[i] = ave;
	}
	private void updateArray(ArrayList<Integer> idp) {
		int i1 = 0;
		int i2 = 0;
		for(int i=1; i<idp.size(); i++) {
			i1 = idp.get(i-1).intValue();
			i2 = idp.get(i).intValue();
			//System.out.println("i1="+i1+" i2="+i2);
			aveArray(i1, i2);
		}
		repaint();
	}
	public void addBoundary() {
		if(_idp==null) {
			_idp = new ArrayList<Integer>(2);
			_idp.add(new Integer(0));
			_idp.add(new Integer(_orgX2.length-1));
		}

		for(int i=0; i<_idp.size()-1; i++) {
			if(_currPos==_idp.get(i).intValue()) return;
			if(_currPos>_idp.get(i).intValue() && _currPos<_idp.get(i+1).intValue()) {
				_idp.add(i+1, new Integer(_currPos));
				break;
			}
		}
		System.out.println(_currPos+" ");
		//for(int i=0; i<_idp.size(); i++) { System.out.print(_idp.get(i).intValue()+" "); }
		updateArray(_idp);
	}
	public void removeBoundary() {
		if(_idp==null) {
			_idp = new ArrayList<Integer>(2);
			_idp.add(new Integer(0));
			_idp.add(new Integer(_orgX2.length));
		}
		System.out.println(_currPos+" ");
		for(int i=0; i<_idp.size(); i++) { System.out.print(_idp.get(i).intValue()+" "); }
		for(int i=0; i<_idp.size()-1; i++) {
			if(_currPos>=_idp.get(i).intValue() && _currPos<_idp.get(i+1).intValue()) {
				_idp.remove(i+1);
				break;
			}
		}
		updateArray(_idp);
	}
	public ArrayList<Integer> getCurrPosArray()		{return _idp;}
	public boolean getEnableCurrPos()				{return _currPosOn;}
	public void enableCurrPos(boolean currPosOn)	{ _currPosOn = currPosOn; repaint();}
	public void move(int nStep)						{ setPos(_currPos+nStep); }
	public int getPos(int pos)						{ return _currPos; }
	public void setPos(int pos)		{ 
		if(pos<0) return;
		if(pos>=_nxmax) return;
		_currPos = pos; 
		repaint(); 
	}

	public void printArray(int[] x, int n) {
		for(int i=0; i<n; i=i+30) System.out.print(" "+x[i]);
	}

	public void refresh() { repaint(); }
	public void paint(Graphics2D g2d) {
		if(!isVisible()) return;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Projector hp = getHorizontalProjector();
		Projector vp = getVerticalProjector();
		Transcaler ts = getTranscaler();

		// Font size and line width from graphics context.
		float fontSize = g2d.getFont().getSize2D();
		float lineWidth = 1.0f;
		Stroke stroke = g2d.getStroke();
		if (stroke instanceof BasicStroke) {
			BasicStroke bs = (BasicStroke)stroke;
			lineWidth = bs.getLineWidth();
		}

		// Graphics context for lines.
		Graphics2D gline = null;
		Graphics2D glineCoarse 	= null;
		Graphics2D gRect 	= null;
		gRect = (Graphics2D)g2d.create();
		Color myColour = new Color(0.3f, 0.3f, 0.3f, 0.3f);
		gRect.setColor(myColour);
		
		if (_lineStyle!=Line.NONE) {
			gline = (Graphics2D)g2d.create();
			float width = lineWidth;
			if (_lineWidth!=0.0f)
				width *= _lineWidth;
			float[] dash = null;
			if (_lineStyle!=Line.SOLID) {
				float dotLength = 0.5f*width;
				float dashLength = 2.0f*width;
				float gapLength = 2.0f*dotLength+dashLength;
				if (_lineStyle==Line.DASH) {
					dash = new float[]{dashLength,gapLength};
				} else if (_lineStyle==Line.DOT) {
					dash = new float[]{dotLength,gapLength};
				} else if (_lineStyle==Line.DASH_DOT) {
					dash = new float[]{dashLength,gapLength,dotLength,gapLength};
				}
			}
			BasicStroke bs;
			BasicStroke bsCoarse;
			if (dash!=null) {
				int cap = BasicStroke.CAP_ROUND;
				int join = BasicStroke.JOIN_ROUND;
				float miter = 10.0f;
				float phase = 0.0f;
				bs = new BasicStroke(width,cap,join,miter,dash,phase);
				bsCoarse = new BasicStroke(width,cap,join,miter,dash,phase);
			} else {
				bs = new BasicStroke(width);
				bsCoarse = new BasicStroke(1.5f*width);
			}
			gline.setStroke(bs);
			if (_lineColor!=null) gline.setColor(_lineColor);

			glineCoarse = (Graphics2D)gline.create();
			glineCoarse.setStroke(bsCoarse);
		}

		// Graphics context for marks.
		Graphics2D gmark = null;
		int markSize = round(fontSize/2.0f);
		if (_markStyle!=Mark.NONE) {
			gmark = (Graphics2D)g2d.create();
			if (_markSize>=0.0f)
				markSize = round(_markSize*lineWidth);
			if (_markColor!=null)
				gmark.setColor(_markColor);
			float width = lineWidth;
			if (_lineWidth!=0.0f)
				width *= _lineWidth;
			BasicStroke bs = new BasicStroke(width);
			gmark.setStroke(bs);
		}

		//		Graphics2D gmarkP = (Graphics2D)g2d.create();
		//		Graphics2D gmarkSH = (Graphics2D)g2d.create();
		//		Graphics2D gmarkSV = (Graphics2D)g2d.create();
		//		
		//		float width1 = lineWidth;
		//		if (_lineWidth!=0.0f)
		//			width1 *= _lineWidth;
		//		BasicStroke bs = new BasicStroke(width1);
		//		gmarkP.setStroke(bs);
		//		gmarkSH.setStroke(bs);
		//		gmarkSV.setStroke(bs);
		//		
		//		gmarkP.setColor(Color.RED);
		//		gmarkSH.setColor(Color.BLUE);
		//		gmarkSV.setColor(Color.GREEN);

		//		int markSize1 = 8;

		// Graphics context for text labels.
		Graphics2D gtext = null;
		if (_x3.size()>0)
			gtext = (Graphics2D)g2d.create();

		// Arrays for (x,y) coordinates.
		int[] x = new int[_nxmax];
		int[] y = new int[_nxmax];

		int[] mx = new int[2];
		int[] my = new int[2];

		setKLayer(false);
		for(int i=0, is=0; i<_sensors.length; i++) {
			for(int j=0; j<_sensors[i].getNumOfComps(); j++, is++) {
				int n = _nx.get(is);
				float[] x1 = _x1.get(is);
				float[] x2 = _x2.get(is);
				computeXY(hp,vp,ts,n,x1,x2,x,y);

				if(_mouseSelection&&_enableLineSelection) {
					computeXY(hp,vp,ts,2,_pX1[is],_pX2[is],mx,my);
					if(_mouseY<=my[0]&&_mouseY>=my[1]) {
						_kLayer[is] = true;
					} 
				}

				// Draw lines between consecutive points.
				if (gline!=null) {
					if(is%3==0) {
						if(_compZSelected) {
							if(_kLayer[is]) {
								gline.setColor(Color.RED);
								gline.drawPolyline(x,y,n);
							} else {
								gline.setColor(Color.BLACK);
								gline.drawPolyline(x,y,n); 
							}
						}
					} else if(is%3==1) {
						if(_compYSelected) {
							if(_kLayer[is]) {
								gline.setColor(Color.RED);
								gline.drawPolyline(x,y,n);
							} else {
								//gline.setColor(Color.MAGENTA);
								if(_fold!=3) gline.setColor(Color.BLACK);
								else gline.setColor(new Color(139, 0, 0)); //dark red
								gline.drawPolyline(x,y,n); 
							}
						}
					} else if(is%3==2) {
						if(_compXSelected) {
							if(_kLayer[is]) {
								gline.setColor(Color.RED);
								gline.drawPolyline(x,y,n);
							} else {
								if(_fold!=3) gline.setColor(Color.BLACK);
								else gline.setColor(Color.BLUE);
								gline.drawPolyline(x,y,n); 
							}
						}
					}
					if(_pickPcSelected) {
						glineCoarse.setColor(Color.RED);
						computeXY(hp,vp,ts,2,_pX1[is],_pcX2[is],mx,my);
						if(mx[0]>0) glineCoarse.drawPolyline(mx,my,2);
					} 
					if(_pickSHcSelected) {
						glineCoarse.setColor(Color.MAGENTA);
						computeXY(hp,vp,ts,2,_shX1[is],_shcX2[is],mx,my);
						if(mx[0]>0) glineCoarse.drawPolyline(mx,my,2);
					} 
					if(_pickSVcSelected) {
						glineCoarse.setColor(Color.ORANGE);
						computeXY(hp,vp,ts,2,_svX1[is],_svcX2[is],mx,my);
						if(mx[0]>0) glineCoarse.drawPolyline(mx,my,2);
					}
					if(_pickPSelected) {
						glineCoarse.setColor(Color.BLACK);
						computeXY(hp,vp,ts,2,_pX1[is],_pX2[is],mx,my);
						if(mx[0]>0) glineCoarse.drawPolyline(mx,my,2);
					} 
					if(_pickSHSelected) {
						glineCoarse.setColor(Color.BLUE);
						computeXY(hp,vp,ts,2,_shX1[is],_shX2[is],mx,my);
						if(mx[0]>0) glineCoarse.drawPolyline(mx,my,2);
					} 
					if(_pickSVSelected) {
						glineCoarse.setColor(Color.GREEN);
						computeXY(hp,vp,ts,2,_svX1[is],_svX2[is],mx,my);
						if(mx[0]>0) glineCoarse.drawPolyline(mx,my,2);
					}
				}

				// Draw marks at points.
				if (gmark!=null) {
					if (_markStyle==Mark.POINT) {
						paintPoint(gmark,n,x,y);
					} else if (_markStyle==Mark.PLUS) {
						paintPlus(gmark,markSize,n,x,y);
					} else if (_markStyle==Mark.CROSS) {
						paintCross(gmark,markSize,n,x,y);
					} else if (_markStyle==Mark.FILLED_CIRCLE) {
						paintFilledCircle(gmark,markSize,n,x,y);
					} else if (_markStyle==Mark.HOLLOW_CIRCLE) {
						paintHollowCircle(gmark,markSize,n,x,y);
					} else if (_markStyle==Mark.FILLED_SQUARE) {
						paintFilledSquare(gmark,markSize,n,x,y);
					} else if (_markStyle==Mark.HOLLOW_SQUARE) {
						paintHollowSquare(gmark,markSize,n,x,y);
					}
				}

				// Draw text labels.
				//				if (gtext!=null) {
				//					float[] z = _x3.get(is);
				//					paintLabel(gtext,markSize,n,x,y,z);
				//				}
			}
		}
		//System.out.println("len="+_sensors.length);
		for(int i=0, is=0; i<_sensors.length; i++) {
			// Draw lines between consecutive points.
			if (gline!=null) {
				if(_pickPcSelected) {
					glineCoarse.setColor(Color.RED);
					computeXY(hp,vp,ts,2,_pX1[is],_pcX2[is],mx,my);
					if(mx[0]>0) glineCoarse.drawPolyline(mx,my,2);
				} 
				if(_pickSHcSelected) {
					glineCoarse.setColor(Color.MAGENTA);
					computeXY(hp,vp,ts,2,_shX1[is],_shcX2[is],mx,my);
					if(mx[0]>0) glineCoarse.drawPolyline(mx,my,2);
				} 
				if(_pickSVcSelected) {
					glineCoarse.setColor(Color.ORANGE);
					computeXY(hp,vp,ts,2,_svX1[is],_svcX2[is],mx,my);
					if(mx[0]>0) glineCoarse.drawPolyline(mx,my,2);
				}
				if(_pickPSelected) {
					glineCoarse.setColor(Color.RED);
					computeXY(hp,vp,ts,2,_pX1[is],_pX2[is],mx,my);
					if(mx[0]>0) {
						glineCoarse.drawPolyline(mx,my,2);
						if(_mtiOption!=null && _mtiOption.getShowBruneWindow()) {
							int iWave = _mtiOption.getISpdWave();
							float q1 = -1;
							float q2 = -1;
							if(iWave==0) {
								q1 = _pX2[is][0]-_mtiOption.getBruneWinLenRange(iWave, 0);
								q2 = _pX2[is][1]+_mtiOption.getBruneWinLenRange(iWave, 1);
							} else if(iWave==2) {
								q2 = _pX2[is][0]-_mtiOption.getBruneWinLenRange(0, 0);
								q1 = q2-(_mtiOption.getBruneWinLenRange(0, 0)+_mtiOption.getBruneWinLenRange(0, 1));
							} 
							if(q1>=0 && q2>=0) {
								computeXY(hp,vp,ts,2,_pX1[is], new float[]{q1, q2},mx,my);
								gRect.fillRect(mx[0], my[1], mx[1]-mx[0], my[0]-my[1]);
							}
						}
					}
				} 
				if(_pickSHSelected) {
					glineCoarse.setColor(Color.BLUE);
					computeXY(hp,vp,ts,2,_shX1[is],_shX2[is],mx,my);
					if(mx[0]>0) {
						glineCoarse.drawPolyline(mx,my,2);
						if(_mtiOption!=null && _mtiOption.getShowBruneWindow()) {
							int iWave = _mtiOption.getISpdWave();
							float q1 = -1;
							float q2 = -1;
							if(iWave==1) {
								q1 = _shX2[is][0]-_mtiOption.getBruneWinLenRange(iWave, 0);
								q2 = _shX2[is][1]+_mtiOption.getBruneWinLenRange(iWave, 1);
							} else if(iWave==3) {
								q2 = _shX2[is][0]-_mtiOption.getBruneWinLenRange(1, 0);
								q1 = q2-(_mtiOption.getBruneWinLenRange(1, 0)+_mtiOption.getBruneWinLenRange(1, 1));
							} 
							if(q1>=0 && q2>=0) {
								computeXY(hp,vp,ts,2,_shX1[is], new float[]{q1, q2},mx,my);
								gRect.fillRect(mx[0], my[1], mx[1]-mx[0], my[0]-my[1]);
							}
						}
					}
				} 
				if(_pickSVSelected) {
					glineCoarse.setColor(Color.GREEN);
					computeXY(hp,vp,ts,2,_svX1[is],_svX2[is],mx,my);
					if(mx[0]>0) glineCoarse.drawPolyline(mx,my,2);
				}
			}
			is += _sensors[i].getNumOfComps();
		}
	} 

	private int getArrayMin(int n, int [] v) {
		int min = v[0];
		for(int i=1; i<n; i++) min = min<v[i]? min:v[i];
		return min;
	}
	private int getArrayMax(int n, int [] v) {
		int max = v[0];
		for(int i=1; i<v.length; i++) max = max>v[i]? max:v[i];
		return max;
	}

	///////////////////////////////////////////////////////////////////////////
	// private

	int _ns; // number of segments
	ArrayList<Integer> _nx = new ArrayList<Integer>(); // numbers of (x1,x2,x3)
	ArrayList<float[]> _x1 = new ArrayList<float[]>(); // arrays of x1
	ArrayList<float[]> _x2 = new ArrayList<float[]>(); // arrays of x2
	ArrayList<float[]> _x3 = new ArrayList<float[]>(); // arrays of x3
	int _nxmax; // maximum number of points in a segment
	private Orientation _orientation = Orientation.X1RIGHT_X2UP;
	private Line _lineStyle = Line.SOLID;
	private float _lineWidth = 0.0f;
	private Color _lineColor = null;
	private Mark _markStyle = Mark.NONE;
	private float _markSize = -1.0f;
	private Color _markColor = null;
	private String _textFormat = "%1.4G";

	/**
	 * Called when we might new realignment.
	 */
	private void updateBestProjectors() {

		// Min and max (x1,x2) values.
		float x1min =  FLT_MAX;
		float x2min =  FLT_MAX;
		float x1max = -FLT_MAX;
		float x2max = -FLT_MAX;
		for (int is=0; is<_ns; ++is) {
			int nx = _nx.get(is);
			float[] x1 = _x1.get(is);
			float[] x2 = _x2.get(is);
			for (int ix=0; ix<nx; ++ix) {
				float x1i = x1[ix];
				float x2i = x2[ix];
				x1min = min(x1min,x1i);
				x2min = min(x2min,x2i);
				x1max = max(x1max,x1i);
				x2max = max(x2max,x2i);
			}
		}

		// Ensure x1min<x1max and x2min<x2max.
		if (x1min==x1max) {
			x1min -= ulp(x1min);
			x1max += ulp(x1max);
		}
		if (x2min==x2max) {
			x2min -= ulp(x2min);
			x2max += ulp(x2max);
		}

		// Assume mark sizes and line widths less than 2% of plot dimensions.
		// The goal is to avoid clipping big marks and wide lines. The problem
		// is that mark sizes and line widths are specified in screen pixels
		// (or points), but margins u0 and u1 are specified in normalized 
		// coordinates, fractions of our tile's width and height. Here, we do 
		// not know those dimensions.
		double u0 = 0.0;
		double u1 = 1.0;
		if (_markStyle!=Mark.NONE || _lineWidth>1.0f) {
			u0 = 0.01;
			u1 = 0.99;
		}

		// Best projectors.
		Projector bhp = null;
		Projector bvp = null;
		if (_orientation==Orientation.X1RIGHT_X2UP) {
			bhp = (x1min<x1max)?new Projector(x1min,x1max,u0,u1):null;
			bvp = (x2min<x2max)?new Projector(x2max,x2min,u0,u1):null;
		} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
			bhp = (x2min<x2max)?new Projector(x2min,x2max,u0,u1):null;
			bvp = (x1min<x1max)?new Projector(x1min,x1max,u0,u1):null;
		}
		if(bhp!=null && bvp!=null) setBestProjectors(bhp,bvp);
	}

	public void mousePressed(MouseEvent e) {
		setMouseStatus(e, true, false, false, e.isControlDown());
		repaint();
	}
	public void mouseDragged(MouseEvent e)  {	
		setMouseStatus(e, false, false, true, e.isControlDown());
		repaint();
	}
	public void mouseReleased(MouseEvent e) {
//		HodogramDialog hodogramDialog = _frame.getMtiMenuBar().getHodogramDialog();
//		if(hodogramDialog==null) return;
//		if(!hodogramDialog.isVisible()) return;
	}

	public void mouseMoved(MouseEvent e) { 
		_mouseX 		= e.getX();
		_mouseY 		= e.getY();
	}

	public void mouseClicked(MouseEvent e) { 
		//		  boolean isAltDown()     // true if Alt key middle mouse button
		//		  boolean isControlDown() // true if Control key is pressed
		//		  boolean isShiftDown()   // true if Shift key is pressed
		//		  boolean isAltGraphDown()// true if Alt Graphics key (found on some keyboards) is pressed
		//		  boolean isMetaDown()    // true if Meta key or right mouse button
	}

	public void setMouseStatus(MouseEvent e, boolean mousePressed, boolean mouseReleased, 
			boolean mouseDragged, boolean controlDown) {
		_mousePressed	= mousePressed;
		_mouseReleased	= mouseReleased;
		_mouseDragged	= mouseDragged;
		_mouseX 		= e.getX();
		_mouseY 		= e.getY();
		_controlDown 	= controlDown;
		
		_mouseSelection = false;
		if(_mousePressed) {
			_mouseSelection = !e.isControlDown()&&!e.isShiftDown()&&!e.isAltDown(); 
		} 

//		if(_mousePressed&&_enableLineSelection) {
//			System.out.println(_mouseY);
//		}
		repaint();
	}

	private boolean equalColors(Color ca, Color cb) {
		return (ca==null)?cb==null:ca.equals(cb);
	}

	private void computeXY(Projector hp, Projector vp, Transcaler ts,
			int n, float[] x1, float[] x2, int[] x, int[] y){
		ts = ts.combineWith(hp,vp);
		float[] xv = null;
		float[] yv = null;
		if (_orientation==Orientation.X1RIGHT_X2UP) {
			xv = x1;
			yv = x2;
		} else if (_orientation==Orientation.X1DOWN_X2RIGHT) {
			xv = x2;
			yv = x1;
		}
		for (int i=0; i<n; ++i) {
			x[i] = ts.x(xv[i]);
			y[i] = ts.y(yv[i]);
		}
	}

	private void paintPoint(Graphics2D g2d, int n, int[] x, int[] y) {
		for (int i=0; i<n; ++i) {
			int xi = x[i];
			int yi = y[i];
			g2d.drawLine(xi,yi,xi,yi);
		}
	}

	private void paintPlus(Graphics2D g2d, int s, int n, int[] x, int[] y) {
		int wh = 2*(s/2);
		int xy = wh/2;
		for (int i=0; i<n; ++i) {
			int xi = x[i];
			int yi = y[i];
			g2d.drawLine(xi-xy,yi,xi+xy,yi);
			g2d.drawLine(xi,yi-xy,xi,yi+xy);
		}
	}

	private void paintCross(Graphics2D g2d, int s, int n, int[] x, int[] y) {
		int wh = 2*(s/2);
		int xy = wh/2;
		for (int i=0; i<n; ++i) {
			int xi = x[i];
			int yi = y[i];
			g2d.drawLine(xi-xy,yi-xy,xi+xy,yi+xy);
			g2d.drawLine(xi+xy,yi-xy,xi-xy,yi+xy);
		}
	}

	private void paintFilledCircle(Graphics2D g2d, int s, int n, int[] x, int[] y) {
		int wh = 1+2*(s/2);
		int xy = wh/2;
		for (int i=0; i<n; ++i)
			g2d.fillOval(x[i]-xy,y[i]-xy,wh,wh);
	}

	private void paintHollowCircle(Graphics2D g2d, int s, int n, int[] x, int[] y) {
		int wh = 1+2*(s/2);
		int xy = wh/2;
		for (int i=0; i<n; ++i)
			g2d.drawOval(x[i]-xy,y[i]-xy,wh-1,wh-1);
	}

	private void paintFilledSquare(Graphics2D g2d, int s, int n, int[] x, int[] y) {
		int wh = 1+2*(s/2);
		int xy = wh/2;
		for (int i=0; i<n; ++i)
			g2d.fillRect(x[i]-xy,y[i]-xy,wh,wh);
	}

	private void paintHollowSquare(Graphics2D g2d, int s, int n, int[] x, int[] y) {
		int wh = 1+2*(s/2);
		int xy = wh/2;
		for (int i=0; i<n; ++i)
			g2d.drawRect(x[i]-xy,y[i]-xy,wh-1,wh-1);
	}

	private void paintLabel(Graphics2D g2d, int s, int n, int[] x, int[] y, float[] z) {
		s /= 2;
		for (int i=0; i<n; ++i) {
			int xi = x[i];
			int yi = y[i];
			g2d.drawString(String.format(_textFormat,z[i]),xi+s,yi-s);
		}
	}
}
