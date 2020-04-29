package com.geohammer.rt.pseudo3d;

import javax.swing.SwingUtilities;

import com.geohammer.core.planarmodel.Point2D;

public class Geometry2D {
	private SeismicVel 	_vel 	= null;

	private boolean 	_iDynamicXdir	= true;	// 1- dynamically search x-dir, 0- a constant x-dir;
	private int 		_iDebug 		= 0;		

	private double 		_EPSILON 		= 0.01; // tolerance to determine if a point is in a line
	private double 		_incidentAngle	= 1.0e-4;
	private double 		_halfPi			= 2.0*Math.atan(1.0);

	private int 		_idx 		= 0;
	private double 		_len 		= 1.0;
	

	public Geometry2D(SeismicVel vel, double minLayerThickness) {
		_vel	= vel;
		_len 	= minLayerThickness;
	}

	public double getIncidentAngle() { return _incidentAngle; }
	public int getIDx() { return _idx; }
	
	/**
	 * Compute the transmitted point. 
	 * Line p1-p2 is base and the p1 is always at left of p2
	 * @param p1    First point of the segment 1
	 * @param p2    Second point of the segment 1
	 * @param p0    shooting point
	 * @param rayParameter   ray parameter
	 * @param len   the segment length between p1 and the calculated point
	 * @param p     the calculated transmitted point
	 * @return code ID
	 */
	public Point2D calTransmittedPoint(int iVTI, double derivative, Point2D p1, Point2D p2, Point2D m1, Point2D m2, 
			int k1, int k2, int b1, int b2, double rayParameter, int idx, int iDynamic, double tiltedAngle) {
		if(p1.equal(p2)) return new Point2D(p2.getE(), p2.getD());
		if(m1.equal(m2)) return new Point2D(m1.getE(), m1.getD());
		
		double Ax, Az, Bx, Bz, Cx, Cz;
		double distAB, theCos, theSin;
		double Ex, Ez, newX;
		
		if(_iDebug==1) System.out.println(String.format("p1=%s p2=%s m1=%s m2=%s iVTI=%d k1=%d k2=%d b1=%d b2=%d rp=%f", 
				p1.toString(), p2.toString(), m1.toString(), m2.toString(), iVTI, k1, k2, b1, b2, rayParameter));
		Ax = m1.getE();	Az = m1.getD();
		Bx = m2.getE();	Bz = m2.getD();
		Cx = p1.getE();	Cz = p1.getD();
		//  (1) Translate the system so that point A is on the origin.
		Bx-=Ax; Bz-=Az;
		Cx-=Ax; Cz-=Az;
		
		//  Discover the length of segment A-B.
		distAB=Math.sqrt(Bx*Bx+Bz*Bz);

		//  (2) Rotate the system so that point B is on the positive X axis.
		theCos=Bx/distAB;
		theSin=Bz/distAB;
		newX=Cx*theCos+Cz*theSin;
		Cz  =Cz*theCos-Cx*theSin; Cx=newX;
		
		//  (3) Create a transmitted point .
		//double phaseAngle = _vel.getPhaseAngleFromRayParsTable(k2, rayParameter);
		double groupAngle = _vel.getTableValue(1, k2, 3, (float)rayParameter);
		//double transmittedAngle = getTransmittedAngle(iVTI, p1, p2, derivative, groupAngle, tiltedAngle);
		double transmittedAngle = groupAngle;
//		if(_iDebug==1) System.out.println(String.format("phaseAngle=%f groupAngle=%f transmittedAngle=%f ", 
//				phaseAngle*180.0/3.1415926, groupAngle*180.0/3.1415926, transmittedAngle*180.0/3.1415926));
		//double transmittedAngle = groupAngle;
		//Ex = distAB*Math.sin(theta);
		//Ez = dx*dz*distAB*Math.cos(theta);
		
		//Ex = 300.0*Math.sin(theta);
		//Ez = dx*dz*300.0*Math.cos(theta);
//		if(iDynamic==1) {
//			int jdx = 1;
//			if(Cx>0) jdx = -1;
//			int jdz = 1;
//			if(Cz>0) jdz = -1;
//			Ex = jdx*_len*Math.sin(transmittedAngle);
//			Ez = jdz*_len*Math.cos(transmittedAngle);
//		} else {
//			Ex = _len*Math.sin(transmittedAngle);
//			Ez = idx*idz*_len*Math.cos(transmittedAngle);
//		}
		int idz = 1;
		if(b2<b1) idz = -1;
		Ex = _len*Math.sin(transmittedAngle);
		Ez = idx*idz*_len*Math.cos(transmittedAngle);
		if(_iDebug==1) System.out.println(String.format("ex=%f ez=%f len=%f", Ex, Ez, _len));
		//Ex = _idx*300.0*Math.sin(theta);
		//Ez = dz*300.0*Math.cos(theta);
		//System.out.println(String.format("theta=%E ex=%f ez=%f theSin=%f theCos=%f theSinA=%f theCosA=%f Cx=%f Cz=%f", 
		//theta*180.0/3.1415926, Ex, Ez, theSin, theCos, Math.asin(theSin)*180.0/3.1415926, Math.acos(theCos)*180.0/3.1415926, Cx, Cz));
		//System.out.println(String.format("distAB=%f dx=%d dz=%d theta=%f ex=%f ez=%f  theSin=%f theCos=%f  Ax=%f Az=%f", 
		//		distAB, dx, dz, theta*180.0/3.1415926, Ex, Ez, theSin, theCos, Ax, Az));
		//  (4) Apply the discovered position to line A-B in the original coordinate system.
		//reverse rotation
		newX = Ex*theCos - Ez*theSin;
		Ez = Ex*theSin + Ez*theCos; Ex = newX;
		if(_iDebug==1) System.out.println(String.format("ex=%f ez=%f ", Ex, Ez));
				
		if(Math.abs(Ex)<_EPSILON) Ex = 0.0;
		if(Math.abs(Ez)<_EPSILON) Ez = 0.0;
		return new Point2D(Ex+Ax, Ez+Az);
	}

	public double getTransmittedAngle(int iVTI, Point2D p1, Point2D p2, double derivative, double groupAngle, double tiltedAngle) {
		double transmittedAngle = groupAngle;
		//group angle relative to titled angle to group angle relative to a vertical line
		if(iVTI==3) {				//TTI			
			if(p2.getE()>p1.getE()) {
				if(p2.getN()>p1.getN()) groupAngle += tiltedAngle;
				else groupAngle -= tiltedAngle;
			} else {
				if(p2.getN()>p1.getN()) groupAngle -= tiltedAngle;
				else groupAngle += tiltedAngle;
			}
		} 
		
		//group angle relative to a vertical line to group angle relative to the norm
		if(iVTI==1||iVTI==3) {
			if(p2.getE()>p1.getE()) {
				if(p2.getN()>p1.getN()) transmittedAngle += Math.atan(derivative);
				else transmittedAngle -= Math.atan(derivative);
			} else {
				if(p2.getN()>p1.getN()) transmittedAngle -= Math.atan(derivative);
				else transmittedAngle += Math.atan(derivative);
			}
		} 
		
		return transmittedAngle; // iso, STI, TTI
	}
	/**
	 * Compute the Incident angle when ray p3-p4 shoots on line p1-p2 
	 * Line p1-p2 is base and the p1 is always at left of p2
	 * two line segment must intersect
	 * @param p1    First point of the segment 1
	 * @param p2    Second point of the segment 1
	 * @param p3    First point of the segment 2
	 * @param p4    Second point of the segment 2
	 * @return the angel between 0 and 180 degree
	 */
	public double lineSegmentAcuteAngle(Point2D p1, Point2D p2, Point2D p3, Point2D p4) {
		double Ax, Az, Bx, Bz, Cx, Cz, Dx, Dz;
		double distAB, distCD, theCos, theSin, newX;

		Ax = p1.getE();	Az = p1.getN();
		Bx = p2.getE();	Bz = p2.getN();
		Cx = p3.getE();	Cz = p3.getN();
		Dx = p4.getE();	Dz = p4.getN();
		
		//  (1) Translate the system so that point A is on the origin.
		Bx-=Ax; Bz-=Az;
		Cx-=Ax; Cz-=Az;
		Dx-=Ax; Dz-=Az;

		//  Discover the length of segment A-B.
		distAB=Math.sqrt(Bx*Bx+Bz*Bz);
		distCD = p3.distance(p4);

		//  (2) Rotate the system so that point B is on the positive X axis.
		theCos=Bx/distAB;
		theSin=Bz/distAB;
		newX=Cx*theCos+Cz*theSin;
		Cz  =Cz*theCos-Cx*theSin; Cx=newX;
		newX=Dx*theCos+Dz*theSin;
		Dz  =Dz*theCos-Dx*theSin; Dx=newX;

		return Math.acos(Math.abs(Cx-Dx)/distCD);
	}

	public int pointCurvePosition(int idz, Point2D p, Point2D A, Point2D B, Point2D C){
		//System.out.println(String.format("p=%s A=%s B=%s C=%s", p.toString(), A.toString(), B.toString(), C.toString()));
		if ((B.getN()< p.getN() && C.getN()>=p.getN())|| (C.getN()< p.getN() && B.getN()>=p.getN())){
			return pointLinePosition(p, B, C);
		} else {
			int idx = 1;
			if(C.getE()>B.getE()) idx = 1;
			else idx = -1;
			return pointCurvePosition(idx, idz, p, A, B, C);
		}
	}
	
	public int pointCurvePosition1(int idz, Point2D p, Point2D A, Point2D B, Point2D C){
		//System.out.println(String.format("p=%s A=%s B=%s C=%s", p.toString(), A.toString(), B.toString(), C.toString()));
		if((A.getN()< p.getN() && C.getN()>=p.getN())|| (C.getN()< p.getN() && A.getN()>=p.getN())) {
			//B-C
			if ((B.getN()< p.getN() && C.getN()>=p.getN())|| (C.getN()< p.getN() && B.getN()>=p.getN())){
				return pointLinePosition(p, B, C);
			} else {
				return pointLinePosition(p, A, B);
			}
		} else {
			int idx = 1;
			if(C.getE()>B.getE()) idx = 1;
			else idx = -1;
			return pointCurvePosition(idx, idz, p, A, B, C);
		}
	}
	public int pointCurvePosition(int idx, int idz, Point2D p, Point2D A, Point2D B, Point2D C){
		//System.out.println(String.format("p=%s A=%s B=%s C=%s", p.toString(), A.toString(), B.toString(), C.toString()));
		
		int leftC = pointLinePosition(p, B, C);
		int leftA = pointLinePosition(p, A, B);
		//System.out.println("leftC="+leftC+" leftA="+leftA);
		if(idz==1) {
			if(idx==1) {
				if(leftC==-1 && leftA==-1) {
					return -1;
				} else {
					return 1; // right
				}
			} else {
				if(leftC==1 && leftA==1) {
					return 1;
				} else {
					return -1; // left
				}
			}
		} else {
			if(idx==1) {
				if(leftC==1 && leftA==1) {
					return 1;
				} else {
					return -1; // left
				}
			} else {
				if(leftC==-1 && leftA==-1) {
					return -1;
				} else {
					return 1; // right
				}
			}
		}
	}
	// isLeft(): test if a point is Left|On|Right of an infinite 2D line.
	//  Input:  three points P0, P1, and P2
	//  Return: >0 for P2 left of the line through P0 to P1
	//        =0 for P2 on the line
	//        <0 for P2 right of the line
	// cross product
	public int pointLinePosition(Point2D p, Point2D A, Point2D B){
		return pointLinePosition(p.getE(), p.getN(), A.getE(), A.getN(), B.getE(), B.getN());
	}
	
	//cross-product AxB=ABsin() where A=(bx-ax,by-ay) and B=(cx-ax,cy-ay)
	/**
	 * Create geometry for an arrow along the specified line and with
	 * tip at x1,y1. See general method above.
	 * 
	 * @param cx      X coordinate of an isolated point being tested.
	 * @param cy      Y coordinate of an isolated point being tested.
	 * @param ax      X coordinate of tail point of a line segment.
	 * @param ay      Y coordinate of tail point of a line segment. 
	 * @param bx      X coordinate of head point of a line segment.
	 * @param by      Y coordinate of head point of a line segment.  
	 * @return        0 is on the line, 1 is right side of the line segment, -1 is left side. 
	 */
	public static int pointLinePosition(double cx, double cy, double ax, double ay, double bx, double by){
		//System.out.println(String.format("cx=%f cy=%f Ax=%f Ay=%f Bx=%f By=%f ", cx, cy, ax, ay, bx, by));
		double d = ((bx - ax)*(cy - ay) - (by - ay)*(cx - ax));
		if(Almost.DOUBLE.zero(d)) 	return 0; 	// on
		else if(d<0.0) 				return -1; 	// left
		else 						return 1; 	// right
	}
	
	public boolean isLeft1(Point2D p0, Point2D p1, Point2D p2) {
		double Ax, Az, Bx, Bz, Cx, Cz;
		double distAB, theCos, theSin, theta;
		double Ex, Ez, newX;
		
		//System.out.println(String.format("p0=%s p1=%s p2=%s", p0.toString(), p1.toString(), p2.toString()));
		Ax = p1.getE();	Az = p1.getN();
		Bx = p2.getE();	Bz = p2.getN();
		Cx = p0.getE();	Cz = p0.getN();
		//  (1) Translate the system so that point A is on the origin.
		Bx-=Ax; Bz-=Az;
		Cx-=Ax; Cz-=Az;
		
		//  Discover the length of segment A-B.
		distAB=Math.sqrt(Bx*Bx+Bz*Bz);

		//  (2) Rotate the system so that point B is on the positive X axis.
		theCos=Bx/distAB;
		theSin=Bz/distAB;
		newX=Cx*theCos+Cz*theSin;
		Cz  =Cz*theCos-Cx*theSin; Cx=newX;
		
		//  (3) Create a transmitted point .
		if(Cz>0.0) return true;
		else return false;
	}
	
	

	
//  Globals which should be set before calling this function:
//
//  int    polySides  =  how many corners the polygon has
//  float  polyX[]    =  horizontal coordinates of corners
//  float  polyY[]    =  vertical coordinates of corners
//  float  x, y       =  point to be tested
//
//  (Globals are used in this example for purposes of speed.  Change as
//  desired.)
//
//  The function will return YES if the point x,y is inside the polygon, or
//  NO if it is not.  If the point is exactly on the edge of the polygon,
//  then the function may return YES or NO.
//
//  Note that division by zero is avoided because the division is protected
//  by the "if" clause which surrounds it.
// http://alienryderflex.com/polygon/
//	public boolean pointInPolygon(int polySides, double [] polyX, double [] polyY, double x, double y) {
//		int   i, j=polySides-1;
//		boolean  oddNodes=false;
//
//		for (i=0; i<polySides; i++) {
//			if ((polyY[i]< y && polyY[j]>=y  ||   polyY[j]< y && polyY[i]>=y)
//					&&  (polyX[i]<=x || polyX[j]<=x)) {
//				if (polyX[i]+(y-polyY[i])/(polyY[j]-polyY[i])*(polyX[j]-polyX[i])<x) {
//					oddNodes=!oddNodes; 
//				}
//			}
//			j=i;
//		}
//
//		return oddNodes; 
//	}
	
	
//	public boolean clockWise(Point2D A, Point2D B, Point2D C) {
//		//Point2D BA 	= new Point2D(B.getE()-A.getE(), B.getN()-A.getN());
//		//Point2D BC 	= new Point2D(B.getE()-C.getE(), B.getN()-C.getN());
//		//cross-product
//		if( ((B.getE()-A.getE()) * (B.getN()-C.getN())- (B.getE()-C.getE()) * B.getN()-A.getN())<0.0) 
//			return true;
//		else return false;
//	}
//	
//	public boolean pointInsideTriangle(Point2D A, Point2D B, Point2D C, double x, double y) {
//		boolean  oddNodes = false;
//
//		//A-B
//		if ((A.getN()< y && B.getN()>=y  ||   B.getN()< y && A.getN()>=y)
//				&&  (A.getE()<=x || B.getE()<=x)) {
//			if (A.getE()+(y-A.getN())/(B.getN()-A.getN())*(B.getE()-A.getE())<x) {
//				oddNodes=!oddNodes; 
//			}
//		}
//		
//		//B-C
//		if ((B.getN()< y && C.getN()>=y  ||   C.getN()< y && B.getN()>=y)
//				&&  (B.getE()<=x || C.getE()<=x)) {
//			if (B.getE()+(y-B.getN())/(C.getN()-B.getN())*(C.getE()-B.getE())<x) {
//				oddNodes=!oddNodes; 
//			}
//		}
//		
//		//C-A
//		if ((C.getN()< y && A.getN()>=y  ||   A.getN()< y && C.getN()>=y)
//				&&  (C.getE()<=x || A.getE()<=x)) {
//			if (C.getE()+(y-C.getN())/(A.getN()-C.getN())*(A.getE()-C.getE())<x) {
//				oddNodes=!oddNodes; 
//			}
//		}
//
//		return oddNodes; 
//	}
	
//	public static void main(String[] args) {		
//		//ZeltModel mdl1 = new ZeltModel();
//		//Vector<VCNode> nvector = mdl1.readNodeModel("C:\\HalWeb\\frsJobArchieve\\NORSAR\\vecon\\isoFlatModel.csv", 1);
//		//final ZeltModel mdl = new ZeltModel( nvector );
//		//System.out.println(mdl.toDeepString());
//		//final VCPair dmPW = VCUtil.readPckLocFromFile("C:\\HalWeb\\frsJobArchieve\\NORSAR\\vecon\\isoFlatPair.csv");
//		//System.out.println(dmPW.toDeepString());
//
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run(){
//				//System.out.println(dmPW.toDeepString());
//				//new TraceLayerModelField(null, mdl, dmPW, 1, 0, 1, 0, 0, -1);
//				new Geometry2D();
//			}
//		});
//	}
}

