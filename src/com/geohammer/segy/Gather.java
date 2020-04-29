package com.geohammer.segy;

import com.geohammer.core.SeismicTraceComponent;
import com.geohammer.core.SeismicTraceSensor;

public class Gather {

	SEGYTraceHeader [] 		_headers 	= null;
	SeismicTraceSensor 	[]	_sensors 	= null;

	public Gather() {
	}

	public Gather(String selectedFileName) {
		//readBinaryFile(selectedFileName);
	}
	public Gather(int nRcvs, int nCompPerReceiver, int nt, float dt, 
			int iShot, float sx, float sy, float sz, float [] rx, float [] ry, float [] rz) {
		this(nRcvs, nCompPerReceiver, nt, dt, 
				iShot, 0, sx, sy, sz, rx, ry, rz, true);
	}

	public Gather(int nRcvs, int nCompPerReceiver, int nt, float dt, 
			int iShot, float datum, float sx, float sy, float sz, float [] rx, float [] ry, float [] rz, boolean allocate) {
		this();
		_sensors = new SeismicTraceSensor[nRcvs];
		for(int i=0; i<nRcvs; i++) {
			_sensors[i] = new SeismicTraceSensor(0, i, nCompPerReceiver);
			_sensors[i].setX(rx[i]);
			_sensors[i].setY(ry[i]);
			_sensors[i].setZ(rz[i]);
			for(int j=0; j<nCompPerReceiver; j++) {
				SeismicTraceComponent comp = null;
				if(allocate) {
					comp = new SeismicTraceComponent(0, i, j, dt, new float [nt]);
				} else {
					comp = new SeismicTraceComponent(0, i, j, dt, null);
				}
				comp.setX(rx[i]);
				comp.setY(ry[i]);
				comp.setZ(rz[i]);
				_sensors[i].setComp(j, comp);
			}
		}

		int nHeaders = nRcvs*nCompPerReceiver;
		_headers 		= new SEGYTraceHeader[nHeaders] ;
		for(int i=0, k=0; i<nRcvs; i++) {
			for(int j=0; j<nCompPerReceiver; j++, k++) {
				_headers[k] = new SEGYTraceHeader();

				_headers[k].setInt(1, 4, (k+1));
				_headers[k].setInt(5, 4, (k+1));
				_headers[k].setInt(9, 4, iShot);
				_headers[k].setInt(13, 4, (k+1));
				
				_headers[k].setInt(21, 4, 1);
				_headers[k].setInt(25, 4, (k+1));
				_headers[k].setInt(29, 2, 1);
				
				_headers[k].setInt(31, 2, 1);
				_headers[k].setInt(33, 2, 1);
				_headers[k].setInt(35, 2, 1);
				
				_headers[k].setInt(41, 4, (int)(1000*rz[i]));
				_headers[k].setInt(45, 4, (int)(1000*sz));
				_headers[k].setInt(49, 4, 0);
				_headers[k].setInt(53, 4, (int)(1000*datum));
				_headers[k].setInt(57, 4, (int)(1000*datum));
				_headers[k].setInt(61, 4, 0);
				_headers[k].setInt(65, 4, 0);
				_headers[k].setInt(69, 2, -1000);
				
				_headers[k].setInt(71, 2, -1000);
				
				_headers[k].setInt(73, 4, (int)(1000*sx));
				_headers[k].setInt(77, 4, (int)(1000*sy));
				
				_headers[k].setInt(81, 4, (int)(1000*rx[i])); //Easting
				_headers[k].setInt(85, 4, (int)(1000*ry[i]));
				
				_headers[k].setInt(89, 2, 1);

				_headers[k].setInt(115, 2, nt);
				_headers[k].setInt(117, 2, (int)(1000000*dt));
				
				_headers[k].setInt(171, 2, (i+1)); // receiver group number
				_headers[k].setInt(173, 2, (j+1)); // component/channel number
			}
		}
	}
	public void setHeaderInt(int byteNo, int len, int value) {
		for(int i=0; i<_headers.length; i++) {
			_headers[i].setInt(byteNo, len, value);
		}
	}
	public void setHeaderInt(int byteNo, int len, int [] values) {
		for(int i=0; i<_headers.length; i++) {
			_headers[i].setInt(byteNo, len, values[i]);
		}
	}
	public SEGYTraceHeader [] getHeader() { return _headers; }
	public SEGYTraceHeader getHeader(int index) { return _headers[index]; }
	
	public SeismicTraceSensor [] getSensor() { return _sensors; }
	public SeismicTraceSensor getSensor(int index) { return _sensors[index]; }
	
	public int getNumOfReceivers() 			{ return _sensors.length; }	
	public int getNumOfCompPerReceiver() 	{ return _sensors[0].getNumOfComps(); }
	

	//public SeismicTraceComponent [] getSeismicTraceComponent() { return _sensors; }


}

