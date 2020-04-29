package com.geohammer.rt;

import javax.swing.SwingUtilities;

import org.ucdm.core.planarmodel.Grid3D;

public class RtTest implements Runnable {
	public RtTest() {

	}
	public void run() {
		long startTime = System.currentTimeMillis();
		
		testPunchAlone1();
		
		long endTime = System.currentTimeMillis();
		float elapsedTimeMillis = endTime - startTime;
		float elapsedTimeSec = elapsedTimeMillis/1000F; 
		float elapsedTimeMin = elapsedTimeMillis/(60*1000F); 
		float elapsedTimeHour = elapsedTimeMillis/(60*60*1000F);
		float elapsedTimeDay = elapsedTimeMillis/(24*60*60*1000F);

		String aTime = new String("Successfully Done With Elapsed Time:\n " + (int)(elapsedTimeSec) + " second(s). \n");
		if( elapsedTimeMin>1.0 ) {
			elapsedTimeSec = elapsedTimeSec - (int)(elapsedTimeMin)*60;
			String bTime = new String(" Or " + (int)(elapsedTimeMin) + " minute(s) " + " and " + (int)(elapsedTimeSec) + " second(s). \n");
			aTime = new String(aTime.concat(bTime));
		}
		System.out.println(aTime);
		
		System.out.println("Successfully Done!");
	}
	public static void main(String[] args) {
		System.out.println("Executing RtTest()");
		SwingUtilities.invokeLater( new RtTest() );
	}

	public void testPunchAlone() {
		float x0 = 0;
		float y0 = 0;
		float z0 = 0;
		float h = 5;
		int nx = 200;
		int ny = 100;
		int nz = 80;
		int isx = 100;
		int isy = 20;
		int isz = 10;
		float fxs = isx*h;
		float fys = isy*h;
		float fzs = isz*h;
		
		int nxyz = nx*ny*nz;	
		int nxy = nx*ny;
		float [] time0 = new float[nxyz];
		float [] slow0 = new float[nxyz];
		
		float s0 = (float)(h/3500.0);
		for(int i=0; i<nxyz; i++)  slow0[i] = s0; 
		
		PunchAlone punchAlone = new PunchAlone(nx, ny, nz, h, x0, y0, z0, 
				fxs, fys, fzs, slow0, time0);
		punchAlone.start();
		
//		String selectedFileName = "C:\\PINN_DATA\\tmp\\junk.bin";
//		Grid3D grid3D = new Grid3D(2, nx, ny, nz, h, h, h, x0, y0, z0, time0);
//		grid3D.writeAsBinary(selectedFileName);
		
		int ix = 10;
		int iy = 20;
		int iz = 10;
		double x = ix*h-fxs;
		double y = iy*h-fys;
		double z = iz*h-fzs;
		//private float t0(int x, int y, int z) 			 { return _time0[_nxy*z + _nx*y + x]; }
		System.out.println("T0="+time0[nxy*iz + nx*iy + ix]+" T1="+Math.sqrt(x*x+y*y+z*z)/3500.0);
		
	}
	public void testPunchAlone1() {
		float x0 = 0;
		float y0 = 0;
		float z0 = 0;
		float h = 5;
		int nx = 100;
		int ny = 200;
		int nz = 80;
		int isx = 20;
		int isy = 100;
		int isz = 70;
		float fxs = isx*h;
		float fys = isy*h;
		float fzs = isz*h;
		
		int nxyz = nx*ny*nz;	
		int nxy = nx*ny;
		float [] time0 = new float[nxyz];
		float [] slow0 = new float[nxyz];
		
		float s0 = (float)(h/3500.0);
		for(int i=0; i<nxyz; i++)  slow0[i] = s0; 
		
		PunchAlone punchAlone = new PunchAlone(nx, ny, nz, h, x0, y0, z0, 
				fxs, fys, fzs, slow0, time0);
		punchAlone.start();
		
//		String selectedFileName = "C:\\PINN_DATA\\tmp\\junk.bin";
//		Grid3D grid3D = new Grid3D(2, nx, ny, nz, h, h, h, x0, y0, z0, time0);
//		grid3D.writeAsBinary(selectedFileName);
		
		int ix = 20;
		int iy = 10;
		int iz = 70;
		double x = ix*h-fxs;
		double y = iy*h-fys;
		double z = iz*h-fzs;
		//private float t0(int x, int y, int z) 			 { return _time0[_nxy*z + _nx*y + x]; }
		System.out.println("T0="+time0[nxy*iz + nx*iy + ix]+" T1="+Math.sqrt(x*x+y*y+z*z)/3500.0);
		
	}
	
}
