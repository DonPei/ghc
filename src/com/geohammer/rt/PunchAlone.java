package com.geohammer.rt;

import java.util.Arrays;

public class PunchAlone {

	private float t0(int x, int y, int z) 			 { return _time0[_nxy*z + _nx*y + x]; }
	private float s0(int x, int y, int z) 			 { return _slow0[_nxy*z + _nx*y + x]; }
	private void setT0(int x, int y, int z, float v) { _time0[_nxy*z + _nx*y + x] = v; }
	//private void setS0(int x, int y, int z, float v) { _slow0[_nxy*z + _nx*y + x] = v; }

	public float [] getTime0() 						{ return _time0; }
	public float [] getSlow0() 						{ return _slow0; }
	public void setTime0(float [] time0) 			{ _time0 = time0; }
	public void setSlow0(float [] slow0) 			{ _slow0 = slow0; }

	private float dist(float x, float y, float z, float x1, float y1, float z1) {
		double a = x-x1, b = y-y1, c = z-z1;			
		return (float)Math.sqrt(a*a+b*b+c*c); 
	}

	private int 		_nx, _ny, _nz, _nxy;
	private float 		_h 		= 0;
	private float [] 	_slow0 	= null;
	private float [] 	_time0 	= null;
	private float 		_x0, _y0, _z0;
	private float 		_fxs, _fys, _fzs;

	public PunchAlone(int nx, int ny, int nz, float h, float x0, float y0, float z0, 
			float fxs, float fys, float fzs, float [] slow0, float [] time0) {
		_nx 	= nx;
		_ny 	= ny;
		_nz 	= nz;
		_nxy 	= nx*ny;
		_h 		= h;
		_x0 	= x0;
		_y0 	= y0;
		_z0 	= z0;
		_fxs 	= fxs;
		_fys 	= fys;
		_fzs 	= fzs;
		_slow0 	= slow0;
		_time0 	= time0;
		//int nxyz 	= nx*ny*nz;
		//for(int i=0;i<nxyz;i++)  slow0[i] = h*slow0[i]; 
	}

	public int start() {
		int nx 		= _nx;
		int ny 		= _ny;
		int nz 		= _nz;
		int nxy 	= _nx*_ny;
		int nxz 	= _nx*_nz;
		int nyz 	= _nz*_ny;
		int nxyz 	= _nx*_ny*_nz;
		float h 	= _h;
		float x0 	= _x0;
		float y0 	= _y0;
		float z0 	= _z0;
		float fxs 	= _fxs;
		float fys 	= _fys;
		float fzs 	= _fzs;
		float [] slow0 = _slow0;
		float [] time0 = _time0;

		int iplus, iminus, jplus, jminus, kplus, kminus;
		int X1, X2, lasti, index, ii, i, j, k, radius;
		int igrow,			/* counter for "cube" growth */
		floatsrc,		/* if 0, source must be on a grid node; 1, source can lie between grid nodes */
		srcwall,        /* if 1, source on x=0 wall, if 2, on x=nx-1 wall */
		/* if 3, source on y=0 wall, if 4, on y=ny-1 wall */
		/* if 5, source on z=0 wall, if 6, on z=nz-1 wall */
		xs,				/* shot x position (in grid points) */
		ys,				/* shot y position */
		zs,				/* shot depth */
		xx, yy, zz,		/* Used to loop around xs, ys, zs coordinates	*/				
		x1=0, x2=0, y1=0, y2=0, z1=0, z2=0,	/* counters for the position of the sides of current cube */
		dx1=1, dx2=1, dy1=1, dy2=1, dz1=1, dz2=1, rad0=1,	/* flags set to 1 until a side has been reached */
		maxrad,			/* maximum radius to compute used in linear velocity gradient cube source */
		reverse=1,		/* will automatically do up to this number of reverse propagation steps to fix waves that travel back into expanding cube */
		headpref=6,		/* if headpref starts > 0, will determine model wall closest to source and will prefer to start */
		/* reverse calculations on opposite wall counters for detecting head waves on sides of current cube */
		head;
		int [] headw = new int[7];
		float a, guess, tryit,
		maxoff = -1,			/* maximum offset (real units) to compute */
		fhead, headtest=1.e-3f;	/* used to detect head waves:  if headwave operator decreases the previously-computed traveltime by at least */
		/* headtest*<~time_across_cell> then the headwave counter is triggered */
		double
		/* used in linear velocity gradient cube source */
		rx, ry, rz, dvz, dv, v0, rzc, rxyc, rz1, rxy1, rho, theta1, theta2;
		int srctype = 1;
		int NCUBE = 2;
		radius = NCUBE;
		Sorted [] sort = null;
		int nwall = 0;
		float [] wall = null;

		iplus = iminus = jplus = jminus = kplus = kminus = 1;
		floatsrc = 1;
		srcwall = 10;
		maxoff = -1;
		headpref = 6;
		reverse = 1;
		dx1 = dx2 = dy1 = dy2 = dz1 =dz2 = rad0 = 1;

		i = 0; j = 0; k = 0; 
		for(ii=0; ii<nx; ii++ ) {
			a = x0 + ii*h;
			if( a==fxs ) { i = 999; ii = nx; }
		}
		for(ii=0; ii<ny; ii++ ) {
			a = y0 + ii*h;
			if( a==fys ) { j = 999; ii = nx; }
		}
		for(ii=0; ii<nz; ii++ ) {
			a = z0 + ii*h;
			if( a==fzs ) { k = 999; ii = nx; }
		}

		if( i==999 && j==999 && k==999 ) floatsrc = 0; // IF 0, SOURCE IS ON A GRID POINT 
		else floatsrc = 1;

		if(floatsrc==0){
			fxs = (fxs-x0)/h;
			fys = (fys-y0)/h;
			fzs = (fzs-z0)/h;
			xs = (int) fxs;
			ys = (int) fys;
			zs = (int) fzs;
		} else{
			fxs = (fxs-x0)/h;
			fys = (fys-y0)/h;
			fzs = (fzs-z0)/h;
			xs = (int) (fxs+0.5);
			ys = (int) (fys+0.5);
			zs = (int) (fzs+0.5);
		}

		if(nx <= ny && nx <= nz)  {
			sort = new Sorted[ny*nz];
			nwall = nyz;
		} else if(ny <= nx && ny <= nz)  {
			sort = new Sorted[nx*nz];
			nwall = nxz;
		}else  {
			sort = new Sorted[nx*ny];
			nwall = nxy;
		}
		for(int ik=0; ik<sort.length; ik++) sort[ik] = new Sorted();
		wall = new float[nwall];

		/* SET MAXIMUM RADIUS TO COMPUTE */
		if (maxoff > 0.0f)  maxrad = (int)(maxoff/h + 1); 
		else   				maxrad = 99999999;

		for(i=0;i<nxyz;i++)  time0[i] = 1.0e10f; 

		if (srctype == 1) {			/* POINT SOURCE */
			/* HOLE'S NEW LINEAR VELOCITY GRADIENT CUBE (APRIL 1991)*/
			v0 = h/s0(xs,ys,zs);
			for (xx = xs-NCUBE; xx <= xs+NCUBE; xx++) {
				if (xx < 0 || xx >= nx)	continue; 
				for (yy = ys-NCUBE; yy <= ys+NCUBE; yy++) {
					if (yy < 0 || yy >= ny)	continue;
					for (zz = zs-NCUBE; zz <= zs+NCUBE; zz++) {
						if (zz < 0 || zz >= nz)	continue;
						if (zz == zs)
							dvz = 1/s0(xx,yy,zz+1)-1/s0(xs,ys,zs);
						else
							dvz = (1/s0(xx,yy,zz)-1/s0(xs,ys,zs))/(zz-zs);
						dv = Math.abs(dvz);
						if (dv == 0.)  {
							setT0(xx,yy,zz,s0(xs,ys,zs)*dist(fxs,fys,fzs,xx,yy,zz));
							continue;
						}
						rzc = -v0/dv;
						rx = h*(xx - fxs);
						ry = h*(yy - fys);
						rz = h*(zz - fzs);
						rz1 = rz*dvz/dv;
						rxy1 = Math.sqrt(rx*rx+ry*ry);
						/*rxy1 = sqrt(rx*rx+ry*ry+rz*rz-rz1*rz1);*/
						if (rxy1<=h/1.e6)
							setT0(xx,yy,zz, (float)Math.abs(Math.log((v0+dv*rz1)/v0)/dv));
						else {
							rxyc = (rz1*rz1+rxy1*rxy1-2*rz1*rzc)/(2*rxy1);
							rho = Math.sqrt(rzc*rzc+rxyc*rxyc);
							theta1 = Math.asin(-rzc/rho);
							/* can't handle asin(1.) ! */
							if (Math.abs(rz1-rzc)>=rho)  rho=1.0000001*Math.abs(rz1-rzc);
							theta2 = Math.asin((rz1-rzc)/rho);
							if (rxyc<0) theta1=3.141592654-theta1;
							if (rxyc<rxy1) theta2=3.141592654-theta2;
							setT0(xx,yy,zz, (float)(Math.log(Math.tan(theta2/2)/Math.tan(theta1/2))/dv));
						}
					}
				}
			}

			/* SETS LOCATION OF THE SIDES OF THE CUBE	*/
			radius = NCUBE;
			if(xs > NCUBE) x1 = xs - (NCUBE + 1);
			else{ x1 = -1; dx1 = 0;}
			if(xs < nx-(NCUBE + 1)) x2 = xs + (NCUBE + 1);
			else{ x2 = nx; dx2 = 0;}
			if(ys > NCUBE) y1 = ys - (NCUBE + 1);
			else{ y1 = -1; dy1 = 0;}
			if(ys < ny-(NCUBE + 1)) y2 = ys + (NCUBE + 1);
			else{ y2 = ny; dy2 = 0;}
			if(zs > NCUBE) z1 = zs - (NCUBE + 1);
			else{ z1 = -1; dz1 = 0;}
			if(zs < nz-(NCUBE + 1)) z2 = zs + (NCUBE + 1);
			else{ z2 = nz; dz2 = 0;}
		}
		else if (srctype == 2) {		//  HOLE'S EXTERNAL SOURCE
			// FILL IN WALLS' TIMES FROM EXTERNAL DATAFILE
		}   
		else if (srctype == 3) {                //  HOLE'S REDO OLD TIMES
		}
		else  {
			return -1;
		}

		if (headpref>0) {	/* HOLE - PREFERRED REVERSE DIRECTION */
			head = nx*ny*nz;
			if (nx>5 && x2<=head)  			{ headpref=2;  head=x2;}
			if (nx>5 && (nx-1-x1)<=head)   	{ headpref=1;  head=nx-1-x1;}
			if (ny>5 && y2<=head)   		{ headpref=4;  head=y2;}
			if (ny>5 && (ny-1-y1)<=head)   	{ headpref=3;  head=ny-1-y1;}
			if (nz>5 && z2<=head)   		{ headpref=6;  head=z2;}
			if (nz>5 && (nz-1-z1)<=head)   	{ headpref=5;  head=nz-1-z1;}
		}

		/* BIGGER LOOP - HOLE - ALLOWS AUTOMATIC REVERSE PROPAGATION IF 
			HEAD WAVES ARE ENCOUNTERED ON FACES OF EXPANDING CUBE, 
			ALLOWING WAVES TO TRAVEL BACK INTO THE CUBE */
		while ( reverse > -1 )  {
			headw[1]=0; headw[2]=0; headw[3]=0; headw[4]=0; headw[5]=0; headw[6]=0;

			/* BIG LOOP */
			while(rad0!=0 && (dx1!=0 || dx2!=0 || dy1!=0 || dy2!=0 || dz1!=0 || dz2!=0))  {

				/* TOP SIDE */
				for (igrow=1;igrow<=kminus;igrow++) {
					if(dz1!=0){
						ii = 0;
						for(j=y1+1; j<=y2-1; j++){
							for(i=x1+1; i<=x2-1; i++){
								sort[ii].setTime(t0(i,j,z1+1));
								sort[ii].setI1(i);
								sort[ii].setI2(j);
								ii++;
							}
						}
						Arrays.sort(sort, 0, ii);
						for(i=0;i<ii;i++){
							X1 = sort[i].getI1();
							X2 = sort[i].getI2();
							index = z1*nxy + X2*nx + X1;
							lasti = (z1+1)*nxy + X2*nx + X1;
							fhead = 0.0f;
							guess = time0[index];
							// {{ top
							if(time0[index+1] < 1.e9 && time0[index+nx+1] < 1.e9
									&& time0[index+nx] < 1.e9 && X2<ny-1  && X1<nx-1 ) {
								tryit = fdh3d(              t0(X1,X2,z1+1),
										t0(X1+1,X2,z1+1),t0(X1+1,X2+1,z1+1),t0(X1,X2+1,z1+1),
										t0(X1+1,X2,z1  ),t0(X1+1,X2+1,z1  ),t0(X1,X2+1,z1  ),
										s0(X1,X2,z1), s0(X1,X2,z1+1),
										s0(X1+1,X2,z1+1),s0(X1+1,X2+1,z1+1),s0(X1,X2+1,z1+1),
										s0(X1+1,X2,z1  ),s0(X1+1,X2+1,z1  ),s0(X1,X2+1,z1  ));
								if (tryit<guess) guess = tryit;
							}
							if(time0[index-1] < 1.e9 && time0[index+nx-1] < 1.e9
									&& time0[index+nx] < 1.e9 && X2<ny-1  && X1>0 ) {
								tryit = fdh3d(              t0(X1,X2,z1+1),
										t0(X1-1,X2,z1+1),t0(X1-1,X2+1,z1+1),t0(X1,X2+1,z1+1),
										t0(X1-1,X2,z1  ),t0(X1-1,X2+1,z1  ),t0(X1,X2+1,z1  ),
										s0(X1,X2,z1), s0(X1,X2,z1+1),
										s0(X1-1,X2,z1+1),s0(X1-1,X2+1,z1+1),s0(X1,X2+1,z1+1),
										s0(X1-1,X2,z1  ),s0(X1-1,X2+1,z1  ),s0(X1,X2+1,z1  ));
								if (tryit<guess) guess = tryit;
							}
							if(time0[index+1] < 1.e9 && time0[index-nx+1] < 1.e9
									&& time0[index-nx] < 1.e9 && X2>0  && X1<nx-1 ) {
								tryit = fdh3d(              t0(X1,X2,z1+1),
										t0(X1+1,X2,z1+1),t0(X1+1,X2-1,z1+1),t0(X1,X2-1,z1+1),
										t0(X1+1,X2,z1  ),t0(X1+1,X2-1,z1  ),t0(X1,X2-1,z1  ),
										s0(X1,X2,z1), s0(X1,X2,z1+1),
										s0(X1+1,X2,z1+1),s0(X1+1,X2-1,z1+1),s0(X1,X2-1,z1+1),
										s0(X1+1,X2,z1  ),s0(X1+1,X2-1,z1  ),s0(X1,X2-1,z1  ));
								if (tryit<guess) guess = tryit;
							}
							if(time0[index-1] < 1.e9 && time0[index-nx-1] < 1.e9
									&& time0[index-nx] < 1.e9 && X2>0  && X1>0 ) {
								tryit = fdh3d(              t0(X1,X2,z1+1),
										t0(X1-1,X2,z1+1),t0(X1-1,X2-1,z1+1),t0(X1,X2-1,z1+1),
										t0(X1-1,X2,z1  ),t0(X1-1,X2-1,z1  ),t0(X1,X2-1,z1  ),
										s0(X1,X2,z1), s0(X1,X2,z1+1),
										s0(X1-1,X2,z1+1),s0(X1-1,X2-1,z1+1),s0(X1,X2-1,z1+1),
										s0(X1-1,X2,z1  ),s0(X1-1,X2-1,z1  ),s0(X1,X2-1,z1  ));
								if (tryit<guess) guess = tryit;
							}
							if(guess > 1.0e9){ 
								if(time0[index+1] < 1.e9 && X1<nx-1 && X2>y1+1 && X2<y2-1 )  {
									tryit = fdhne(t0(X1,X2,z1+1),t0(X1+1,X2,z1+1),t0(X1+1,X2,z1),
											t0(X1+1,X2-1,z1+1),t0(X1+1,X2+1,z1+1),
											s0(X1,X2,z1),
											s0(X1,X2,z1+1),s0(X1+1,X2,z1+1),s0(X1+1,X2,z1) );
									if (tryit<guess)  guess = tryit;
								}
								if(time0[index-1] < 1.e9 && X1>0 && X2>y1+1 && X2<y2-1 )  {
									tryit = fdhne(t0(X1,X2,z1+1),t0(X1-1,X2,z1+1),t0(X1-1,X2,z1),
											t0(X1-1,X2-1,z1+1),t0(X1-1,X2+1,z1+1),
											s0(X1,X2,z1),
											s0(X1,X2,z1+1),s0(X1-1,X2,z1+1),s0(X1-1,X2,z1) );
									if (tryit<guess)  guess = tryit;
								}
								if(time0[index+nx] < 1.e9 && X2<ny-1 && X1>x1+1 && X1<x2-1 )  {
									tryit = fdhne(t0(X1,X2,z1+1),t0(X1,X2+1,z1+1),t0(X1,X2+1,z1),
											t0(X1-1,X2+1,z1+1),t0(X1+1,X2+1,z1+1),
											s0(X1,X2,z1),
											s0(X1,X2,z1+1),s0(X1,X2+1,z1+1),s0(X1,X2+1,z1) );
									if (tryit<guess)  guess = tryit;
								}
								if(time0[index-nx] < 1.e9 && X2>0 && X1>x1+1 && X1<x2-1 )  {
									tryit = fdhne(t0(X1,X2,z1+1),t0(X1,X2-1,z1+1),t0(X1,X2-1,z1),
											t0(X1-1,X2-1,z1+1),t0(X1+1,X2-1,z1+1),
											s0(X1,X2,z1),
											s0(X1,X2,z1+1),s0(X1,X2-1,z1+1),s0(X1,X2-1,z1) );
									if (tryit<guess)  guess = tryit;
								}
							} 
							if(time0[index+1] < 1.e9 && X1<nx-1 )  {
								tryit = fdh2d(t0(X1,X2,z1+1),t0(X1+1,X2,z1+1),t0(X1+1,X2,z1),
										s0(X1,X2,z1),
										s0(X1,X2,z1+1),s0(X1+1,X2,z1+1),s0(X1+1,X2,z1) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index-1] < 1.e9 && X1>0 )  {
								tryit = fdh2d(t0(X1,X2,z1+1),t0(X1-1,X2,z1+1),t0(X1-1,X2,z1),
										s0(X1,X2,z1),
										s0(X1,X2,z1+1),s0(X1-1,X2,z1+1),s0(X1-1,X2,z1) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index+nx] < 1.e9 && X2<ny-1 )  {
								tryit = fdh2d(t0(X1,X2,z1+1),t0(X1,X2+1,z1+1),t0(X1,X2+1,z1),
										s0(X1,X2,z1),
										s0(X1,X2,z1+1),s0(X1,X2+1,z1+1),s0(X1,X2+1,z1) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index-nx] < 1.e9 && X2>0 )  {
								tryit = fdh2d(t0(X1,X2,z1+1),t0(X1,X2-1,z1+1),t0(X1,X2-1,z1),
										s0(X1,X2,z1),
										s0(X1,X2,z1+1),s0(X1,X2-1,z1+1),s0(X1,X2-1,z1) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index+1] < 1.e9 && time0[index+nx+1] < 1.e9
									&& time0[index+nx] < 1.e9 && X2<ny-1  && X1<nx-1 ) {
								tryit = fdh2d(t0(X1+1,X2,z1),t0(X1+1,X2+1,z1),t0(X1,X2+1,z1),
										s0(X1,X2,z1),
										s0(X1+1,X2,z1),s0(X1+1,X2+1,z1),s0(X1,X2+1,z1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(time0[index+1] < 1.e9 && time0[index-nx+1] < 1.e9
									&& time0[index-nx] < 1.e9 && X2>0  && X1<nx-1 ) {
								tryit = fdh2d(t0(X1+1,X2,z1),t0(X1+1,X2-1,z1),t0(X1,X2-1,z1),
										s0(X1,X2,z1),
										s0(X1+1,X2,z1),s0(X1+1,X2-1,z1),s0(X1,X2-1,z1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(time0[index-1] < 1.e9 && time0[index+nx-1] < 1.e9
									&& time0[index+nx] < 1.e9 && X2<ny-1  && X1>0 ) {
								tryit = fdh2d(t0(X1-1,X2,z1),t0(X1-1,X2+1,z1),t0(X1,X2+1,z1),
										s0(X1,X2,z1),
										s0(X1-1,X2,z1),s0(X1-1,X2+1,z1),s0(X1,X2+1,z1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(time0[index-1] < 1.e9 && time0[index-nx-1] < 1.e9
									&& time0[index-nx] < 1.e9 && X2>0  && X1>0 ) {
								tryit = fdh2d(t0(X1-1,X2,z1),t0(X1-1,X2-1,z1),t0(X1,X2-1,z1),
										s0(X1,X2,z1),
										s0(X1-1,X2,z1),s0(X1-1,X2-1,z1),s0(X1,X2-1,z1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							// }}
							if(guess > 1.0e9){ 
								if ( X1>x1+1 && X1<x2-1 && X2>y1+1 && X2<y2-1 ) {
									tryit = fdhnf(t0(X1,X2,z1+1),
											t0(X1+1,X2,z1+1),t0(X1,X2+1,z1+1),
											t0(X1-1,X2,z1+1),t0(X1,X2-1,z1+1),
											s0(X1,X2,z1),
											s0(X1,X2,z1+1) );
									if (tryit<guess)  guess = tryit;
								}
							} 
							tryit = t0(X1,X2,z1+1) + 0.5f*(s0(X1,X2,z1)+s0(X1,X2,z1+1));
							if (tryit<guess)  guess = tryit;
							if ( time0[index+1]<1.e9 && X1<nx-1 )  {
								tryit = t0(X1+1,X2,z1) + 0.5f*(s0(X1,X2,z1)+s0(X1+1,X2,z1));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-1]<1.e9 && X1>0 )  {
								tryit = t0(X1-1,X2,z1) + 0.5f*(s0(X1,X2,z1)+s0(X1-1,X2,z1));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index+nx]<1.e9 && X2<ny-1 )  {
								tryit = t0(X1,X2+1,z1) + 0.5f*(s0(X1,X2,z1)+s0(X1,X2+1,z1));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-nx]<1.e9 && X2>0 )  {
								tryit = t0(X1,X2-1,z1) + 0.5f*(s0(X1,X2,z1)+s0(X1,X2-1,z1));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if (guess<time0[index])  {
								time0[index] = guess;
								if (fhead>headtest)  headw[5]++;
							}
						}
						if(z1 == 0) dz1 = 0;
						z1--;
					}
				}
				z1 = (z1<0) ? (0):(z1);
				/* BOTTOM SIDE */
				for (igrow=1;igrow<=kplus;igrow++) {  							
					if(dz2!=0){
						ii = 0;
						for(j=y1+1; j<=y2-1; j++){
							for(i=x1+1; i<=x2-1; i++){
								sort[ii].setTime(t0(i,j,z2-1));
								sort[ii].setI1(i);
								sort[ii].setI2(j);
								ii++;
							}
						}
						Arrays.sort(sort, 0, ii);
						for(i=0;i<ii;i++){
							X1 = sort[i].getI1();
							X2 = sort[i].getI2();
							index = z2*nxy + X2*nx + X1;
							lasti = (z2-1)*nxy + X2*nx + X1;
							fhead = 0.0f;
							guess = time0[index];
							if(time0[index+1] < 1.e9 && time0[index+nx+1] < 1.e9
									&& time0[index+nx] < 1.e9 && X2<ny-1  && X1<nx-1 ) {
								tryit = fdh3d(              t0(X1,X2,z2-1),
										t0(X1+1,X2,z2-1),t0(X1+1,X2+1,z2-1),t0(X1,X2+1,z2-1),
										t0(X1+1,X2,z2  ),t0(X1+1,X2+1,z2  ),t0(X1,X2+1,z2  ),
										s0(X1,X2,z2), s0(X1,X2,z2-1),
										s0(X1+1,X2,z2-1),s0(X1+1,X2+1,z2-1),s0(X1,X2+1,z2-1),
										s0(X1+1,X2,z2  ),s0(X1+1,X2+1,z2  ),s0(X1,X2+1,z2  ));
								if (tryit<guess) guess = tryit;
							}
							if(time0[index-1] < 1.e9 && time0[index+nx-1] < 1.e9
									&& time0[index+nx] < 1.e9 && X2<ny-1  && X1>0 ) {
								tryit = fdh3d(              t0(X1,X2,z2-1),
										t0(X1-1,X2,z2-1),t0(X1-1,X2+1,z2-1),t0(X1,X2+1,z2-1),
										t0(X1-1,X2,z2  ),t0(X1-1,X2+1,z2  ),t0(X1,X2+1,z2  ),
										s0(X1,X2,z2), s0(X1,X2,z2-1),
										s0(X1-1,X2,z2-1),s0(X1-1,X2+1,z2-1),s0(X1,X2+1,z2-1),
										s0(X1-1,X2,z2  ),s0(X1-1,X2+1,z2  ),s0(X1,X2+1,z2  ));
								if (tryit<guess) guess = tryit;
							}
							if(time0[index+1] < 1.e9 && time0[index-nx+1] < 1.e9
									&& time0[index-nx] < 1.e9 && X2>0  && X1<nx-1 ) {
								tryit = fdh3d(              t0(X1,X2,z2-1),
										t0(X1+1,X2,z2-1),t0(X1+1,X2-1,z2-1),t0(X1,X2-1,z2-1),
										t0(X1+1,X2,z2  ),t0(X1+1,X2-1,z2  ),t0(X1,X2-1,z2  ),
										s0(X1,X2,z2), s0(X1,X2,z2-1),
										s0(X1+1,X2,z2-1),s0(X1+1,X2-1,z2-1),s0(X1,X2-1,z2-1),
										s0(X1+1,X2,z2  ),s0(X1+1,X2-1,z2  ),s0(X1,X2-1,z2  ));
								if (tryit<guess) guess = tryit;
							}
							if(time0[index-1] < 1.e9 && time0[index-nx-1] < 1.e9
									&& time0[index-nx] < 1.e9 && X2>0  && X1>0 ) {
								tryit = fdh3d(              t0(X1,X2,z2-1),
										t0(X1-1,X2,z2-1),t0(X1-1,X2-1,z2-1),t0(X1,X2-1,z2-1),
										t0(X1-1,X2,z2  ),t0(X1-1,X2-1,z2  ),t0(X1,X2-1,z2  ),
										s0(X1,X2,z2), s0(X1,X2,z2-1),
										s0(X1-1,X2,z2-1),s0(X1-1,X2-1,z2-1),s0(X1,X2-1,z2-1),
										s0(X1-1,X2,z2  ),s0(X1-1,X2-1,z2  ),s0(X1,X2-1,z2  ));
								if (tryit<guess) guess = tryit;
							}
							if(guess > 1.0e9){ 
								if(time0[index+1] < 1.e9 && X1<nx-1 && X2>y1+1 && X2<y2-1 )  {
									tryit = fdhne(t0(X1,X2,z2-1),t0(X1+1,X2,z2-1),t0(X1+1,X2,z2),
											t0(X1+1,X2-1,z2-1),t0(X1+1,X2+1,z2-1),
											s0(X1,X2,z2),
											s0(X1,X2,z2-1),s0(X1+1,X2,z2-1),s0(X1+1,X2,z2) );
									if (tryit<guess)  guess = tryit;
								}
								if(time0[index-1] < 1.e9 && X1>0 && X2>y1+1 && X2<y2-1 )  {
									tryit = fdhne(t0(X1,X2,z2-1),t0(X1-1,X2,z2-1),t0(X1-1,X2,z2),
											t0(X1-1,X2-1,z2-1),t0(X1-1,X2+1,z2-1),
											s0(X1,X2,z2),
											s0(X1,X2,z2-1),s0(X1-1,X2,z2-1),s0(X1-1,X2,z2) );
									if (tryit<guess)  guess = tryit;
								}
								if(time0[index+nx] < 1.e9 && X2<ny-1 && X1>x1+1 && X1<x2-1 )  {
									tryit = fdhne(t0(X1,X2,z2-1),t0(X1,X2+1,z2-1),t0(X1,X2+1,z2),
											t0(X1-1,X2+1,z2-1),t0(X1+1,X2+1,z2-1),
											s0(X1,X2,z2),
											s0(X1,X2,z2-1),s0(X1,X2+1,z2-1),s0(X1,X2+1,z2) );
									if (tryit<guess)  guess = tryit;
								}
								if(time0[index-nx] < 1.e9 && X2>0 && X1>x1+1 && X1<x2-1 )  {
									tryit = fdhne(t0(X1,X2,z2-1),t0(X1,X2-1,z2-1),t0(X1,X2-1,z2),
											t0(X1-1,X2-1,z2-1),t0(X1+1,X2-1,z2-1),
											s0(X1,X2,z2),
											s0(X1,X2,z2-1),s0(X1,X2-1,z2-1),s0(X1,X2-1,z2) );
									if (tryit<guess)  guess = tryit;
								}
							}
							if(time0[index+1] < 1.e9 && X1<nx-1 )  {
								tryit = fdh2d(t0(X1,X2,z2-1),t0(X1+1,X2,z2-1),t0(X1+1,X2,z2),
										s0(X1,X2,z2),
										s0(X1,X2,z2-1),s0(X1+1,X2,z2-1),s0(X1+1,X2,z2) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index-1] < 1.e9 && X1>0 )  {
								tryit = fdh2d(t0(X1,X2,z2-1),t0(X1-1,X2,z2-1),t0(X1-1,X2,z2),
										s0(X1,X2,z2),
										s0(X1,X2,z2-1),s0(X1-1,X2,z2-1),s0(X1-1,X2,z2) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index+nx] < 1.e9 && X2<ny-1 )  {
								tryit = fdh2d(t0(X1,X2,z2-1),t0(X1,X2+1,z2-1),t0(X1,X2+1,z2),
										s0(X1,X2,z2),
										s0(X1,X2,z2-1),s0(X1,X2+1,z2-1),s0(X1,X2+1,z2) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index-nx] < 1.e9 && X2>0 )  {
								tryit = fdh2d(t0(X1,X2,z2-1),t0(X1,X2-1,z2-1),t0(X1,X2-1,z2),
										s0(X1,X2,z2),
										s0(X1,X2,z2-1),s0(X1,X2-1,z2-1),s0(X1,X2-1,z2) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index+1] < 1.e9 && time0[index+nx+1] < 1.e9
									&& time0[index+nx] < 1.e9 && X2<ny-1  && X1<nx-1 ) {
								tryit = fdh2d(t0(X1+1,X2,z2),t0(X1+1,X2+1,z2),t0(X1,X2+1,z2),
										s0(X1,X2,z2),
										s0(X1+1,X2,z2),s0(X1+1,X2+1,z2),s0(X1,X2+1,z2) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(time0[index+1] < 1.e9 && time0[index-nx+1] < 1.e9
									&& time0[index-nx] < 1.e9 && X2>0  && X1<nx-1 ) {
								tryit = fdh2d(t0(X1+1,X2,z2),t0(X1+1,X2-1,z2),t0(X1,X2-1,z2),
										s0(X1,X2,z2),
										s0(X1+1,X2,z2),s0(X1+1,X2-1,z2),s0(X1,X2-1,z2) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(time0[index-1] < 1.e9 && time0[index+nx-1] < 1.e9
									&& time0[index+nx] < 1.e9 && X2<ny-1  && X1>0 ) {
								tryit = fdh2d(t0(X1-1,X2,z2),t0(X1-1,X2+1,z2),t0(X1,X2+1,z2),
										s0(X1,X2,z2),
										s0(X1-1,X2,z2),s0(X1-1,X2+1,z2),s0(X1,X2+1,z2) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(time0[index-1] < 1.e9 && time0[index-nx-1] < 1.e9
									&& time0[index-nx] < 1.e9 && X2>0  && X1>0 ) {
								tryit = fdh2d(t0(X1-1,X2,z2),t0(X1-1,X2-1,z2),t0(X1,X2-1,z2),
										s0(X1,X2,z2),
										s0(X1-1,X2,z2),s0(X1-1,X2-1,z2),s0(X1,X2-1,z2) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(guess > 1.0e9){ 
								if ( X1>x1+1 && X1<x2-1 && X2>y1+1 && X2<y2-1 ) {
									tryit = fdhnf(t0(X1,X2,z2-1),
											t0(X1+1,X2,z2-1),t0(X1,X2+1,z2-1),
											t0(X1-1,X2,z2-1),t0(X1,X2-1,z2-1),
											s0(X1,X2,z2),
											s0(X1,X2,z2-1) );
									if (tryit<guess)  guess = tryit;
								}
							} 
							tryit = t0(X1,X2,z2-1) + 0.5f*(s0(X1,X2,z2)+s0(X1,X2,z2-1));
							if (tryit<guess)  guess = tryit;
							if ( time0[index+1]<1.e9 && X1<nx-1 )  {
								tryit = t0(X1+1,X2,z2) + 0.5f*(s0(X1,X2,z2)+s0(X1+1,X2,z2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-1]<1.e9 && X1>0 )  {
								tryit = t0(X1-1,X2,z2) + 0.5f*(s0(X1,X2,z2)+s0(X1-1,X2,z2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index+nx]<1.e9 && X2<ny-1 )  {
								tryit = t0(X1,X2+1,z2) + 0.5f*(s0(X1,X2,z2)+s0(X1,X2+1,z2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-nx]<1.e9 && X2>0 )  {
								tryit = t0(X1,X2-1,z2) + 0.5f*(s0(X1,X2,z2)+s0(X1,X2-1,z2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if (guess<time0[index]) {
								time0[index] = guess;
								if (fhead>headtest)  headw[6]++;
							}
						}
						if(z2 == nz-1) dz2 = 0;
						z2++;
					}
				}
				z2 = (z2>nz-1) ? (nz-1):(z2);
				/* FRONT SIDE */
				for (igrow=1;igrow<=jminus;igrow++) {  
					if(dy1!=0){
						ii = 0;
						for(k=z1+1; k<=z2-1; k++){
							for(i=x1+1; i<=x2-1; i++){
								sort[ii].setTime(t0(i,y1+1,k));
								sort[ii].setI1(i);
								sort[ii].setI2(k);
								ii++;
							}
						}
						Arrays.sort(sort, 0, ii);
						for(i=0;i<ii;i++){
							X1 = sort[i].getI1();
							X2 = sort[i].getI2();
							index = X2*nxy + y1*nx + X1;
							lasti = X2*nxy + (y1+1)*nx + X1;
							fhead = 0.0f;
							guess = time0[index];
							if(time0[index+1] < 1.e9 && time0[index+nxy+1] < 1.e9
									&& time0[index+nxy] < 1.e9 && X2<nz-1  && X1<nx-1 ) {
								tryit = fdh3d(              t0(X1,y1+1,X2),
										t0(X1+1,y1+1,X2),t0(X1+1,y1+1,X2+1),t0(X1,y1+1,X2+1),
										t0(X1+1,y1  ,X2),t0(X1+1,y1  ,X2+1),t0(X1,y1  ,X2+1),
										s0(X1,y1,X2), s0(X1,y1+1,X2),
										s0(X1+1,y1+1,X2),s0(X1+1,y1+1,X2+1),s0(X1,y1+1,X2+1),
										s0(X1+1,y1  ,X2),s0(X1+1,y1  ,X2+1),s0(X1,y1  ,X2+1));
								if (tryit<guess) guess = tryit;
							}
							if(time0[index-1] < 1.e9 && time0[index+nxy-1] < 1.e9
									&& time0[index+nxy] < 1.e9 && X2<nz-1  && X1>0 ) {
								tryit = fdh3d(              t0(X1,y1+1,X2),
										t0(X1-1,y1+1,X2),t0(X1-1,y1+1,X2+1),t0(X1,y1+1,X2+1),
										t0(X1-1,y1  ,X2),t0(X1-1,y1  ,X2+1),t0(X1,y1  ,X2+1),
										s0(X1,y1,X2), s0(X1,y1+1,X2),
										s0(X1-1,y1+1,X2),s0(X1-1,y1+1,X2+1),s0(X1,y1+1,X2+1),
										s0(X1-1,y1  ,X2),s0(X1-1,y1  ,X2+1),s0(X1,y1  ,X2+1));
								if (tryit<guess) guess = tryit;
							}
							if(time0[index+1] < 1.e9 && time0[index-nxy+1] < 1.e9
									&& time0[index-nxy] < 1.e9 && X2>0  && X1<nx-1 ) {
								tryit = fdh3d(              t0(X1,y1+1,X2),
										t0(X1+1,y1+1,X2),t0(X1+1,y1+1,X2-1),t0(X1,y1+1,X2-1),
										t0(X1+1,y1  ,X2),t0(X1+1,y1  ,X2-1),t0(X1,y1  ,X2-1),
										s0(X1,y1,X2), s0(X1,y1+1,X2),
										s0(X1+1,y1+1,X2),s0(X1+1,y1+1,X2-1),s0(X1,y1+1,X2-1),
										s0(X1+1,y1  ,X2),s0(X1+1,y1  ,X2-1),s0(X1,y1  ,X2-1));
								if (tryit<guess) guess = tryit;
							}
							if(time0[index-1] < 1.e9 && time0[index-nxy-1] < 1.e9
									&& time0[index-nxy] < 1.e9 && X2>0  && X1>0 ) {
								tryit = fdh3d(              t0(X1,y1+1,X2),
										t0(X1-1,y1+1,X2),t0(X1-1,y1+1,X2-1),t0(X1,y1+1,X2-1),
										t0(X1-1,y1  ,X2),t0(X1-1,y1  ,X2-1),t0(X1,y1  ,X2-1),
										s0(X1,y1,X2), s0(X1,y1+1,X2),
										s0(X1-1,y1+1,X2),s0(X1-1,y1+1,X2-1),s0(X1,y1+1,X2-1),
										s0(X1-1,y1  ,X2),s0(X1-1,y1  ,X2-1),s0(X1,y1  ,X2-1));
								if (tryit<guess) guess = tryit;
							}
							if(guess > 1.0e9){ 
								if(time0[index+1] < 1.e9 && X1<nx-1 && X2>z1+1 && X2<z2-1 )  {
									tryit = fdhne(t0(X1,y1+1,X2),t0(X1+1,y1+1,X2),t0(X1+1,y1,X2),
											t0(X1+1,y1+1,X2-1),t0(X1+1,y1+1,X2+1),
											s0(X1,y1,X2),
											s0(X1,y1+1,X2),s0(X1+1,y1+1,X2),s0(X1+1,y1,X2) );
									if (tryit<guess)  guess = tryit;
								}
								if(time0[index-1] < 1.e9 && X1>0 && X2>z1+1 && X2<z2-1 )  {
									tryit = fdhne(t0(X1,y1+1,X2),t0(X1-1,y1+1,X2),t0(X1-1,y1,X2),
											t0(X1-1,y1+1,X2-1),t0(X1-1,y1+1,X2+1),
											s0(X1,y1,X2),
											s0(X1,y1+1,X2),s0(X1-1,y1+1,X2),s0(X1-1,y1,X2) );
									if (tryit<guess)  guess = tryit;
								}
								if(time0[index+nxy] < 1.e9 && X2<nz-1 && X1>x1+1 && X1<x2-1 )  {
									tryit = fdhne(t0(X1,y1+1,X2),t0(X1,y1+1,X2+1),t0(X1,y1,X2+1),
											t0(X1-1,y1+1,X2+1),t0(X1+1,y1+1,X2+1),
											s0(X1,y1,X2),
											s0(X1,y1+1,X2),s0(X1,y1+1,X2+1),s0(X1,y1,X2+1) );
									if (tryit<guess)  guess = tryit;
								}
								if(time0[index-nxy] < 1.e9 && X2>0 && X1>x1+1 && X1<x2-1 )  {
									tryit = fdhne(t0(X1,y1+1,X2),t0(X1,y1+1,X2-1),t0(X1,y1,X2-1),
											t0(X1-1,y1+1,X2-1),t0(X1+1,y1+1,X2-1),
											s0(X1,y1,X2),
											s0(X1,y1+1,X2),s0(X1,y1+1,X2-1),s0(X1,y1,X2-1) );
									if (tryit<guess)  guess = tryit;
								}
							} 
							if(time0[index+1] < 1.e9 && X1<nx-1 )  {
								tryit = fdh2d(t0(X1,y1+1,X2),t0(X1+1,y1+1,X2),t0(X1+1,y1,X2),
										s0(X1,y1,X2),
										s0(X1,y1+1,X2),s0(X1+1,y1+1,X2),s0(X1+1,y1,X2) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index-1] < 1.e9 && X1>0 )  {
								tryit = fdh2d(t0(X1,y1+1,X2),t0(X1-1,y1+1,X2),t0(X1-1,y1,X2),
										s0(X1,y1,X2),
										s0(X1,y1+1,X2),s0(X1-1,y1+1,X2),s0(X1-1,y1,X2) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index+nxy] < 1.e9 && X2<nz-1 )  {
								tryit = fdh2d(t0(X1,y1+1,X2),t0(X1,y1+1,X2+1),t0(X1,y1,X2+1),
										s0(X1,y1,X2),
										s0(X1,y1+1,X2),s0(X1,y1+1,X2+1),s0(X1,y1,X2+1) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index-nxy] < 1.e9 && X2>0 )  {
								tryit = fdh2d(t0(X1,y1+1,X2),t0(X1,y1+1,X2-1),t0(X1,y1,X2-1),
										s0(X1,y1,X2),
										s0(X1,y1+1,X2),s0(X1,y1+1,X2-1),s0(X1,y1,X2-1) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index+1] < 1.e9 && time0[index+nxy+1] < 1.e9
									&& time0[index+nxy] < 1.e9 && X2<nz-1  && X1<nx-1 ) {
								tryit = fdh2d(t0(X1+1,y1,X2),t0(X1+1,y1,X2+1),t0(X1,y1,X2+1),
										s0(X1,y1,X2),
										s0(X1+1,y1,X2),s0(X1+1,y1,X2+1),s0(X1,y1,X2+1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(time0[index+1] < 1.e9 && time0[index-nxy+1] < 1.e9
									&& time0[index-nxy] < 1.e9 && X2>0  && X1<nx-1 ) {
								tryit = fdh2d(t0(X1+1,y1,X2),t0(X1+1,y1,X2-1),t0(X1,y1,X2-1),
										s0(X1,y1,X2),
										s0(X1+1,y1,X2),s0(X1+1,y1,X2-1),s0(X1,y1,X2-1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(time0[index-1] < 1.e9 && time0[index+nxy-1] < 1.e9
									&& time0[index+nxy] < 1.e9 && X2<nz-1  && X1>0 ) {
								tryit = fdh2d(t0(X1-1,y1,X2),t0(X1-1,y1,X2+1),t0(X1,y1,X2+1),
										s0(X1,y1,X2),
										s0(X1-1,y1,X2),s0(X1-1,y1,X2+1),s0(X1,y1,X2+1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(time0[index-1] < 1.e9 && time0[index-nxy-1] < 1.e9
									&& time0[index-nxy] < 1.e9 && X2>0  && X1>0 ) {
								tryit = fdh2d(t0(X1-1,y1,X2),t0(X1-1,y1,X2-1),t0(X1,y1,X2-1),
										s0(X1,y1,X2),
										s0(X1-1,y1,X2),s0(X1-1,y1,X2-1),s0(X1,y1,X2-1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(guess > 1.0e9){ 
								if ( X1>x1+1 && X1<x2-1 && X2>z1+1 && X2<z2-1 ) {
									tryit = fdhnf(t0(X1,y1+1,X2),
											t0(X1+1,y1+1,X2),t0(X1,y1+1,X2+1),
											t0(X1-1,y1+1,X2),t0(X1,y1+1,X2-1),
											s0(X1,y1,X2),
											s0(X1,y1+1,X2) );
									if (tryit<guess)  guess = tryit;
								}
							} 
							tryit = t0(X1,y1+1,X2) + 0.5f*(s0(X1,y1,X2)+s0(X1,y1+1,X2));
							if (tryit<guess)  guess = tryit;
							if ( time0[index+1]<1.e9 && X1<nx-1 )  {
								tryit = t0(X1+1,y1,X2) + 0.5f*(s0(X1,y1,X2)+s0(X1+1,y1,X2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-1]<1.e9 && X1>0 )  {
								tryit = t0(X1-1,y1,X2) + 0.5f*(s0(X1,y1,X2)+s0(X1-1,y1,X2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index+nxy]<1.e9 && X2<nz-1 )  {
								tryit = t0(X1,y1,X2+1) + 0.5f*(s0(X1,y1,X2)+s0(X1,y1,X2+1));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-nxy]<1.e9 && X2>0 )  {
								tryit = t0(X1,y1,X2-1) + 0.5f*(s0(X1,y1,X2)+s0(X1,y1,X2-1));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if (guess<time0[index]) {
								time0[index] = guess;
								if (fhead>headtest)  headw[3]++;
							}
						}
						if(y1 == 0) dy1 = 0;
						y1--;
					}
				}
				y1 = (y1<0) ? (0):(y1);
				/* BACK SIDE */
				for (igrow=1;igrow<=jplus;igrow++) {  
					if(dy2!=0){
						ii = 0;
						for(k=z1+1; k<=z2-1; k++){
							for(i=x1+1; i<=x2-1; i++){
								sort[ii].setTime(t0(i,y2-1,k));
								sort[ii].setI1(i);
								sort[ii].setI2(k);
								ii++;
							}
						}
						Arrays.sort(sort, 0, ii);
						for(i=0;i<ii;i++){
							X1 = sort[i].getI1();
							X2 = sort[i].getI2();
							index = X2*nxy + y2*nx + X1;
							lasti = X2*nxy + (y2-1)*nx + X1;
							fhead = 0.0f;
							guess = time0[index];
							if(time0[index+1] < 1.e9 && time0[index+nxy+1] < 1.e9
									&& time0[index+nxy] < 1.e9 && X2<nz-1  && X1<nx-1 ) {
								tryit = fdh3d(              t0(X1,y2-1,X2),
										t0(X1+1,y2-1,X2),t0(X1+1,y2-1,X2+1),t0(X1,y2-1,X2+1),
										t0(X1+1,y2  ,X2),t0(X1+1,y2  ,X2+1),t0(X1,y2  ,X2+1),
										s0(X1,y2,X2), s0(X1,y2-1,X2),
										s0(X1+1,y2-1,X2),s0(X1+1,y2-1,X2+1),s0(X1,y2-1,X2+1),
										s0(X1+1,y2  ,X2),s0(X1+1,y2  ,X2+1),s0(X1,y2  ,X2+1));
								if (tryit<guess) guess = tryit;
							}
							if(time0[index-1] < 1.e9 && time0[index+nxy-1] < 1.e9
									&& time0[index+nxy] < 1.e9 && X2<nz-1  && X1>0 ) {
								tryit = fdh3d(              t0(X1,y2-1,X2),
										t0(X1-1,y2-1,X2),t0(X1-1,y2-1,X2+1),t0(X1,y2-1,X2+1),
										t0(X1-1,y2  ,X2),t0(X1-1,y2  ,X2+1),t0(X1,y2  ,X2+1),
										s0(X1,y2,X2), s0(X1,y2-1,X2),
										s0(X1-1,y2-1,X2),s0(X1-1,y2-1,X2+1),s0(X1,y2-1,X2+1),
										s0(X1-1,y2  ,X2),s0(X1-1,y2  ,X2+1),s0(X1,y2  ,X2+1));
								if (tryit<guess) guess = tryit;
							}
							if(time0[index+1] < 1.e9 && time0[index-nxy+1] < 1.e9
									&& time0[index-nxy] < 1.e9 && X2>0  && X1<nx-1 ) {
								tryit = fdh3d(              t0(X1,y2-1,X2),
										t0(X1+1,y2-1,X2),t0(X1+1,y2-1,X2-1),t0(X1,y2-1,X2-1),
										t0(X1+1,y2  ,X2),t0(X1+1,y2  ,X2-1),t0(X1,y2  ,X2-1),
										s0(X1,y2,X2), s0(X1,y2-1,X2),
										s0(X1+1,y2-1,X2),s0(X1+1,y2-1,X2-1),s0(X1,y2-1,X2-1),
										s0(X1+1,y2  ,X2),s0(X1+1,y2  ,X2-1),s0(X1,y2  ,X2-1));
								if (tryit<guess) guess = tryit;
							}
							if(time0[index-1] < 1.e9 && time0[index-nxy-1] < 1.e9
									&& time0[index-nxy] < 1.e9 && X2>0  && X1>0 ) {
								tryit = fdh3d(              t0(X1,y2-1,X2),
										t0(X1-1,y2-1,X2),t0(X1-1,y2-1,X2-1),t0(X1,y2-1,X2-1),
										t0(X1-1,y2  ,X2),t0(X1-1,y2  ,X2-1),t0(X1,y2  ,X2-1),
										s0(X1,y2,X2), s0(X1,y2-1,X2),
										s0(X1-1,y2-1,X2),s0(X1-1,y2-1,X2-1),s0(X1,y2-1,X2-1),
										s0(X1-1,y2  ,X2),s0(X1-1,y2  ,X2-1),s0(X1,y2  ,X2-1));
								if (tryit<guess) guess = tryit;
							}
							if(guess > 1.0e9){ 
								if(time0[index+1] < 1.e9 && X1<nx-1 && X2>z1+1 && X2<z2-1 )  {
									tryit = fdhne(t0(X1,y2-1,X2),t0(X1+1,y2-1,X2),t0(X1+1,y2,X2),
											t0(X1+1,y2-1,X2-1),t0(X1+1,y2-1,X2+1),
											s0(X1,y2,X2),
											s0(X1,y2-1,X2),s0(X1+1,y2-1,X2),s0(X1+1,y2,X2) );
									if (tryit<guess)  guess = tryit;
								}
								if(time0[index-1] < 1.e9 && X1>0 && X2>z1+1 && X2<z2-1 )  {
									tryit = fdhne(t0(X1,y2-1,X2),t0(X1-1,y2-1,X2),t0(X1-1,y2,X2),
											t0(X1-1,y2-1,X2-1),t0(X1-1,y2-1,X2+1),
											s0(X1,y2,X2),
											s0(X1,y2-1,X2),s0(X1-1,y2-1,X2),s0(X1-1,y2,X2) );
									if (tryit<guess)  guess = tryit;
								}
								if(time0[index+nxy] < 1.e9 && X2<nz-1 && X1>x1+1 && X1<x2-1 )  {
									tryit = fdhne(t0(X1,y2-1,X2),t0(X1,y2-1,X2+1),t0(X1,y2,X2+1),
											t0(X1-1,y2-1,X2+1),t0(X1+1,y2-1,X2+1),
											s0(X1,y2,X2),
											s0(X1,y2-1,X2),s0(X1,y2-1,X2+1),s0(X1,y2,X2+1) );
									if (tryit<guess)  guess = tryit;
								}
								if(time0[index-nxy] < 1.e9 && X2>0 && X1>x1+1 && X1<x2-1 )  {
									tryit = fdhne(t0(X1,y2-1,X2),t0(X1,y2-1,X2-1),t0(X1,y2,X2-1),
											t0(X1-1,y2-1,X2-1),t0(X1+1,y2-1,X2-1),
											s0(X1,y2,X2),
											s0(X1,y2-1,X2),s0(X1,y2-1,X2-1),s0(X1,y2,X2-1) );
									if (tryit<guess)  guess = tryit;
								}
							} 
							if(time0[index+1] < 1.e9 && X1<nx-1 )  {
								tryit = fdh2d(t0(X1,y2-1,X2),t0(X1+1,y2-1,X2),t0(X1+1,y2,X2),
										s0(X1,y2,X2),
										s0(X1,y2-1,X2),s0(X1+1,y2-1,X2),s0(X1+1,y2,X2) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index-1] < 1.e9 && X1>0 )  {
								tryit = fdh2d(t0(X1,y2-1,X2),t0(X1-1,y2-1,X2),t0(X1-1,y2,X2),
										s0(X1,y2,X2),
										s0(X1,y2-1,X2),s0(X1-1,y2-1,X2),s0(X1-1,y2,X2) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index+nxy] < 1.e9 && X2<nz-1 )  {
								tryit = fdh2d(t0(X1,y2-1,X2),t0(X1,y2-1,X2+1),t0(X1,y2,X2+1),
										s0(X1,y2,X2),
										s0(X1,y2-1,X2),s0(X1,y2-1,X2+1),s0(X1,y2,X2+1) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index-nxy] < 1.e9 && X2>0 )  {
								tryit = fdh2d(t0(X1,y2-1,X2),t0(X1,y2-1,X2-1),t0(X1,y2,X2-1),
										s0(X1,y2,X2),
										s0(X1,y2-1,X2),s0(X1,y2-1,X2-1),s0(X1,y2,X2-1) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index+1] < 1.e9 && time0[index+nxy+1] < 1.e9
									&& time0[index+nxy] < 1.e9 && X2<nz-1  && X1<nx-1 ) {
								tryit = fdh2d(t0(X1+1,y2,X2),t0(X1+1,y2,X2+1),t0(X1,y2,X2+1),
										s0(X1,y2,X2),
										s0(X1+1,y2,X2),s0(X1+1,y2,X2+1),s0(X1,y2,X2+1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(time0[index+1] < 1.e9 && time0[index-nxy+1] < 1.e9
									&& time0[index-nxy] < 1.e9 && X2>0  && X1<nx-1 ) {
								tryit = fdh2d(t0(X1+1,y2,X2),t0(X1+1,y2,X2-1),t0(X1,y2,X2-1),
										s0(X1,y2,X2),
										s0(X1+1,y2,X2),s0(X1+1,y2,X2-1),s0(X1,y2,X2-1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(time0[index-1] < 1.e9 && time0[index+nxy-1] < 1.e9
									&& time0[index+nxy] < 1.e9 && X2<nz-1  && X1>0 ) {
								tryit = fdh2d(t0(X1-1,y2,X2),t0(X1-1,y2,X2+1),t0(X1,y2,X2+1),
										s0(X1,y2,X2),
										s0(X1-1,y2,X2),s0(X1-1,y2,X2+1),s0(X1,y2,X2+1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(time0[index-1] < 1.e9 && time0[index-nxy-1] < 1.e9
									&& time0[index-nxy] < 1.e9 && X2>0  && X1>0 ) {
								tryit = fdh2d(t0(X1-1,y2,X2),t0(X1-1,y2,X2-1),t0(X1,y2,X2-1),
										s0(X1,y2,X2),
										s0(X1-1,y2,X2),s0(X1-1,y2,X2-1),s0(X1,y2,X2-1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(guess > 1.0e9){ 
								if ( X1>x1+1 && X1<x2-1 && X2>z1+1 && X2<z2-1 ) {
									tryit = fdhnf(t0(X1,y2-1,X2),
											t0(X1+1,y2-1,X2),t0(X1,y2-1,X2+1),
											t0(X1-1,y2-1,X2),t0(X1,y2-1,X2-1),
											s0(X1,y2,X2),
											s0(X1,y2-1,X2) );
									if (tryit<guess)  guess = tryit;
								}
							} 
							tryit = t0(X1,y2-1,X2) + 0.5f*(s0(X1,y2,X2)+s0(X1,y2-1,X2));
							if (tryit<guess)  guess = tryit;
							if ( time0[index+1]<1.e9 && X1<nx-1 )  {
								tryit = t0(X1+1,y2,X2) + 0.5f*(s0(X1,y2,X2)+s0(X1+1,y2,X2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-1]<1.e9 && X1>0 )  {
								tryit = t0(X1-1,y2,X2) + 0.5f*(s0(X1,y2,X2)+s0(X1-1,y2,X2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index+nxy]<1.e9 && X2<nz-1 )  {
								tryit = t0(X1,y2,X2+1) + 0.5f*(s0(X1,y2,X2)+s0(X1,y2,X2+1));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-nxy]<1.e9 && X2>0 )  {
								tryit = t0(X1,y2,X2-1) + 0.5f*(s0(X1,y2,X2)+s0(X1,y2,X2-1));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if (guess<time0[index]) {
								time0[index] = guess;
								if (fhead>headtest)  headw[4]++;
							}
						}
						if(y2 == ny-1) dy2 = 0;
						y2++;
					}
				}
				y2 = (y2>ny-1) ? (ny-1):(y2);
				/* LEFT SIDE */
				for (igrow=1;igrow<=iminus;igrow++) {  
					if(dx1!=0){
						ii = 0;
						for(k=z1+1; k<=z2-1; k++){
							for(j=y1+1; j<=y2-1; j++){
								sort[ii].setTime(t0(x1+1,j,k));
								sort[ii].setI1(j);
								sort[ii].setI2(k);
								ii++;
							}
						}
						Arrays.sort(sort, 0, ii);
						for(i=0;i<ii;i++){
							X1 = sort[i].getI1();
							X2 = sort[i].getI2();
							index = X2*nxy + X1*nx + x1;
							lasti = X2*nxy + X1*nx + (x1+1);
							fhead = 0.0f;
							guess = time0[index];
							if(time0[index+nx] < 1.e9 && time0[index+nxy+nx] < 1.e9
									&& time0[index+nxy] < 1.e9 && X2<nz-1  && X1<ny-1 ) {
								tryit = fdh3d(              t0(x1+1,X1,X2),
										t0(x1+1,X1+1,X2),t0(x1+1,X1+1,X2+1),t0(x1+1,X1,X2+1),
										t0(x1  ,X1+1,X2),t0(x1  ,X1+1,X2+1),t0(x1  ,X1,X2+1),
										s0(x1,X1,X2), s0(x1+1,X1,X2),
										s0(x1+1,X1+1,X2),s0(x1+1,X1+1,X2+1),s0(x1+1,X1,X2+1),
										s0(x1  ,X1+1,X2),s0(x1  ,X1+1,X2+1),s0(x1  ,X1,X2+1));
								if (tryit<guess) guess = tryit;
							}
							if(time0[index-nx] < 1.e9 && time0[index+nxy-nx] < 1.e9
									&& time0[index+nxy] < 1.e9 && X2<nz-1  && X1>0 ) {
								tryit = fdh3d(              t0(x1+1,X1,X2),
										t0(x1+1,X1-1,X2),t0(x1+1,X1-1,X2+1),t0(x1+1,X1,X2+1),
										t0(x1  ,X1-1,X2),t0(x1  ,X1-1,X2+1),t0(x1  ,X1,X2+1),
										s0(x1,X1,X2), s0(x1+1,X1,X2),
										s0(x1+1,X1-1,X2),s0(x1+1,X1-1,X2+1),s0(x1+1,X1,X2+1),
										s0(x1  ,X1-1,X2),s0(x1  ,X1-1,X2+1),s0(x1  ,X1,X2+1));
								if (tryit<guess) guess = tryit;
							}
							if(time0[index+nx] < 1.e9 && time0[index-nxy+nx] < 1.e9
									&& time0[index-nxy] < 1.e9 && X2>0  && X1<ny-1 ) {
								tryit = fdh3d(              t0(x1+1,X1,X2),
										t0(x1+1,X1+1,X2),t0(x1+1,X1+1,X2-1),t0(x1+1,X1,X2-1),
										t0(x1  ,X1+1,X2),t0(x1  ,X1+1,X2-1),t0(x1  ,X1,X2-1),
										s0(x1,X1,X2), s0(x1+1,X1,X2),
										s0(x1+1,X1+1,X2),s0(x1+1,X1+1,X2-1),s0(x1+1,X1,X2-1),
										s0(x1  ,X1+1,X2),s0(x1  ,X1+1,X2-1),s0(x1  ,X1,X2-1));
								if (tryit<guess) guess = tryit;
							}
							if(time0[index-nx] < 1.e9 && time0[index-nxy-nx] < 1.e9
									&& time0[index-nxy] < 1.e9 && X2>0  && X1>0 ) {
								tryit = fdh3d(              t0(x1+1,X1,X2),
										t0(x1+1,X1-1,X2),t0(x1+1,X1-1,X2-1),t0(x1+1,X1,X2-1),
										t0(x1  ,X1-1,X2),t0(x1  ,X1-1,X2-1),t0(x1  ,X1,X2-1),
										s0(x1,X1,X2), s0(x1+1,X1,X2),
										s0(x1+1,X1-1,X2),s0(x1+1,X1-1,X2-1),s0(x1+1,X1,X2-1),
										s0(x1  ,X1-1,X2),s0(x1  ,X1-1,X2-1),s0(x1  ,X1,X2-1));
								if (tryit<guess) guess = tryit;
							}
							if(guess > 1.0e9){ 
								if(time0[index+nx] < 1.e9 && X1<ny-1 && X2>z1+1 && X2<z2-1 )  {
									tryit = fdhne(t0(x1+1,X1,X2),t0(x1+1,X1+1,X2),t0(x1,X1+1,X2),
											t0(x1+1,X1+1,X2-1),t0(x1+1,X1+1,X2+1),
											s0(x1,X1,X2),
											s0(x1+1,X1,X2),s0(x1+1,X1+1,X2),s0(x1,X1+1,X2) );
									if (tryit<guess)  guess = tryit;
								}
								if(time0[index-nx] < 1.e9 && X1>0 && X2>z1+1 && X2<z2-1 )  {
									tryit = fdhne(t0(x1+1,X1,X2),t0(x1+1,X1-1,X2),t0(x1,X1-1,X2),
											t0(x1+1,X1-1,X2-1),t0(x1+1,X1-1,X2+1),
											s0(x1,X1,X2),
											s0(x1+1,X1,X2),s0(x1+1,X1-1,X2),s0(x1,X1-1,X2) );
									if (tryit<guess)  guess = tryit;
								}
								if(time0[index+nxy] < 1.e9 && X2<nz-1 && X1>y1+1 && X1<y2-1 )  {
									tryit = fdhne(t0(x1+1,X1,X2),t0(x1+1,X1,X2+1),t0(x1,X1,X2+1),
											t0(x1+1,X1-1,X2+1),t0(x1+1,X1+1,X2+1),
											s0(x1,X1,X2),
											s0(x1+1,X1,X2),s0(x1+1,X1,X2+1),s0(x1,X1,X2+1) );
									if (tryit<guess)  guess = tryit;
								}
								if(time0[index-nxy] < 1.e9 && X2>0 && X1>y1+1 && X1<y2-1 )  {
									tryit = fdhne(t0(x1+1,X1,X2),t0(x1+1,X1,X2-1),t0(x1,X1,X2-1),
											t0(x1+1,X1-1,X2-1),t0(x1+1,X1+1,X2-1),
											s0(x1,X1,X2),
											s0(x1+1,X1,X2),s0(x1+1,X1,X2-1),s0(x1,X1,X2-1) );
									if (tryit<guess)  guess = tryit;
								}
							} 
							if(time0[index+nx] < 1.e9 && X1<ny-1 )  {
								tryit = fdh2d(t0(x1+1,X1,X2),t0(x1+1,X1+1,X2),t0(x1,X1+1,X2),
										s0(x1,X1,X2),
										s0(x1+1,X1,X2),s0(x1+1,X1+1,X2),s0(x1,X1+1,X2) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index-nx] < 1.e9 && X1>0 )  {
								tryit = fdh2d(t0(x1+1,X1,X2),t0(x1+1,X1-1,X2),t0(x1,X1-1,X2),
										s0(x1,X1,X2),
										s0(x1+1,X1,X2),s0(x1+1,X1-1,X2),s0(x1,X1-1,X2) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index+nxy] < 1.e9 && X2<nz-1 )  {
								tryit = fdh2d(t0(x1+1,X1,X2),t0(x1+1,X1,X2+1),t0(x1,X1,X2+1),
										s0(x1,X1,X2),
										s0(x1+1,X1,X2),s0(x1+1,X1,X2+1),s0(x1,X1,X2+1) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index-nxy] < 1.e9 && X2>0 )  {
								tryit = fdh2d(t0(x1+1,X1,X2),t0(x1+1,X1,X2-1),t0(x1,X1,X2-1),
										s0(x1,X1,X2),
										s0(x1+1,X1,X2),s0(x1+1,X1,X2-1),s0(x1,X1,X2-1) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index+nx] < 1.e9 && time0[index+nxy+nx] < 1.e9
									&& time0[index+nxy] < 1.e9 && X2<nz-1  && X1<ny-1 ) {
								tryit = fdh2d(t0(x1,X1+1,X2),t0(x1,X1+1,X2+1),t0(x1,X1,X2+1),
										s0(x1,X1,X2),
										s0(x1,X1+1,X2),s0(x1,X1+1,X2+1),s0(x1,X1,X2+1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(time0[index+nx] < 1.e9 && time0[index-nxy+nx] < 1.e9
									&& time0[index-nxy] < 1.e9 && X2>0  && X1<ny-1 ) {
								tryit = fdh2d(t0(x1,X1+1,X2),t0(x1,X1+1,X2-1),t0(x1,X1,X2-1),
										s0(x1,X1,X2),
										s0(x1,X1+1,X2),s0(x1,X1+1,X2-1),s0(x1,X1,X2-1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(time0[index-nx] < 1.e9 && time0[index+nxy-nx] < 1.e9
									&& time0[index+nxy] < 1.e9 && X2<nz-1  && X1>0 ) {
								tryit = fdh2d(t0(x1,X1-1,X2),t0(x1,X1-1,X2+1),t0(x1,X1,X2+1),
										s0(x1,X1,X2),
										s0(x1,X1-1,X2),s0(x1,X1-1,X2+1),s0(x1,X1,X2+1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(time0[index-nx] < 1.e9 && time0[index-nxy-nx] < 1.e9
									&& time0[index-nxy] < 1.e9 && X2>0  && X1>0 ) {
								tryit = fdh2d(t0(x1,X1-1,X2),t0(x1,X1-1,X2-1),t0(x1,X1,X2-1),
										s0(x1,X1,X2),
										s0(x1,X1-1,X2),s0(x1,X1-1,X2-1),s0(x1,X1,X2-1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(guess > 1.0e9){ 
								if ( X1>y1+1 && X1<y2-1 && X2>z1+1 && X2<z2-1 ) {
									tryit = fdhnf(t0(x1+1,X1,X2),
											t0(x1+1,X1+1,X2),t0(x1+1,X1,X2+1),
											t0(x1+1,X1-1,X2),t0(x1+1,X1,X2-1),
											s0(x1,X1,X2),
											s0(x1+1,X1,X2) );
									if (tryit<guess)  guess = tryit;
								}
							} 
							tryit = t0(x1+1,X1,X2) + 0.5f*(s0(x1,X1,X2)+s0(x1+1,X1,X2));
							if (tryit<guess)  guess = tryit;
							if ( time0[index+nx]<1.e9 && X1<ny-1 )  {
								tryit = t0(x1,X1+1,X2) + 0.5f*(s0(x1,X1,X2)+s0(x1,X1+1,X2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-nx]<1.e9 && X1>0 )  {
								tryit = t0(x1,X1-1,X2) + 0.5f*(s0(x1,X1,X2)+s0(x1,X1-1,X2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index+nxy]<1.e9 && X2<nz-1 )  {
								tryit = t0(x1,X1,X2+1) + 0.5f*(s0(x1,X1,X2)+s0(x1,X1,X2+1));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-nxy]<1.e9 && X2>0 )  {
								tryit = t0(x1,X1,X2-1) + 0.5f*(s0(x1,X1,X2)+s0(x1,X1,X2-1));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if (guess<time0[index]) {
								time0[index] = guess;
								if (fhead>headtest)  headw[1]++;
							}
						}
						if(x1 == 0) dx1 = 0;
						x1--;
					}
				}
				x1 = (x1<0) ? (0):(x1);
				/* RIGHT SIDE */
				for (igrow=1;igrow<=iplus;igrow++) {  
					if(dx2!=0){
						ii = 0;
						for(k=z1+1; k<=z2-1; k++){
							for(j=y1+1; j<=y2-1; j++){
								sort[ii].setTime(t0(x2-1,j,k));
								sort[ii].setI1(j);
								sort[ii].setI2(k);
								ii++;
							}
						}
						Arrays.sort(sort, 0, ii);
						for(i=0;i<ii;i++){
							X1 = sort[i].getI1();
							X2 = sort[i].getI2();
							index = X2*nxy + X1*nx + x2;
							lasti = X2*nxy + X1*nx + (x2-1);
							fhead = 0.0f;
							guess = time0[index];
							if(time0[index+nx] < 1.e9 && time0[index+nxy+nx] < 1.e9
									&& time0[index+nxy] < 1.e9 && X2<nz-1  && X1<ny-1 ) {
								tryit = fdh3d(              t0(x2-1,X1,X2),
										t0(x2-1,X1+1,X2),t0(x2-1,X1+1,X2+1),t0(x2-1,X1,X2+1),
										t0(x2  ,X1+1,X2),t0(x2  ,X1+1,X2+1),t0(x2  ,X1,X2+1),
										s0(x2,X1,X2), s0(x2-1,X1,X2),
										s0(x2-1,X1+1,X2),s0(x2-1,X1+1,X2+1),s0(x2-1,X1,X2+1),
										s0(x2  ,X1+1,X2),s0(x2  ,X1+1,X2+1),s0(x2  ,X1,X2+1));
								if (tryit<guess) guess = tryit;
							}
							if(time0[index-nx] < 1.e9 && time0[index+nxy-nx] < 1.e9
									&& time0[index+nxy] < 1.e9 && X2<nz-1  && X1>0 ) {
								tryit = fdh3d(              t0(x2-1,X1,X2),
										t0(x2-1,X1-1,X2),t0(x2-1,X1-1,X2+1),t0(x2-1,X1,X2+1),
										t0(x2  ,X1-1,X2),t0(x2  ,X1-1,X2+1),t0(x2  ,X1,X2+1),
										s0(x2,X1,X2), s0(x2-1,X1,X2),
										s0(x2-1,X1-1,X2),s0(x2-1,X1-1,X2+1),s0(x2-1,X1,X2+1),
										s0(x2  ,X1-1,X2),s0(x2  ,X1-1,X2+1),s0(x2  ,X1,X2+1));
								if (tryit<guess) guess = tryit;
							}
							if(time0[index+nx] < 1.e9 && time0[index-nxy+nx] < 1.e9
									&& time0[index-nxy] < 1.e9 && X2>0  && X1<ny-1 ) {
								tryit = fdh3d(              t0(x2-1,X1,X2),
										t0(x2-1,X1+1,X2),t0(x2-1,X1+1,X2-1),t0(x2-1,X1,X2-1),
										t0(x2  ,X1+1,X2),t0(x2  ,X1+1,X2-1),t0(x2  ,X1,X2-1),
										s0(x2,X1,X2), s0(x2-1,X1,X2),
										s0(x2-1,X1+1,X2),s0(x2-1,X1+1,X2-1),s0(x2-1,X1,X2-1),
										s0(x2  ,X1+1,X2),s0(x2  ,X1+1,X2-1),s0(x2  ,X1,X2-1));
								if (tryit<guess) guess = tryit;
							}
							if(time0[index-nx] < 1.e9 && time0[index-nxy-nx] < 1.e9
									&& time0[index-nxy] < 1.e9 && X2>0  && X1>0 ) {
								tryit = fdh3d(              t0(x2-1,X1,X2),
										t0(x2-1,X1-1,X2),t0(x2-1,X1-1,X2-1),t0(x2-1,X1,X2-1),
										t0(x2  ,X1-1,X2),t0(x2  ,X1-1,X2-1),t0(x2  ,X1,X2-1),
										s0(x2,X1,X2), s0(x2-1,X1,X2),
										s0(x2-1,X1-1,X2),s0(x2-1,X1-1,X2-1),s0(x2-1,X1,X2-1),
										s0(x2  ,X1-1,X2),s0(x2  ,X1-1,X2-1),s0(x2  ,X1,X2-1));
								if (tryit<guess) guess = tryit;
							}
							if(guess > 1.0e9){ 
								if(time0[index+nx] < 1.e9 && X1<ny-1 && X2>z1+1 && X2<z2-1 )  {
									tryit = fdhne(t0(x2-1,X1,X2),t0(x2-1,X1+1,X2),t0(x2,X1+1,X2),
											t0(x2-1,X1+1,X2-1),t0(x2-1,X1+1,X2+1),
											s0(x2,X1,X2),
											s0(x2-1,X1,X2),s0(x2-1,X1+1,X2),s0(x2,X1+1,X2) );
									if (tryit<guess)  guess = tryit;
								}
								if(time0[index-nx] < 1.e9 && X1>0 && X2>z1+1 && X2<z2-1 )  {
									tryit = fdhne(t0(x2-1,X1,X2),t0(x2-1,X1-1,X2),t0(x2,X1-1,X2),
											t0(x2-1,X1-1,X2-1),t0(x2-1,X1-1,X2+1),
											s0(x2,X1,X2),
											s0(x2-1,X1,X2),s0(x2-1,X1-1,X2),s0(x2,X1-1,X2) );
									if (tryit<guess)  guess = tryit;
								}
								if(time0[index+nxy] < 1.e9 && X2<nz-1 && X1>y1+1 && X1<y2-1 )  {
									tryit = fdhne(t0(x2-1,X1,X2),t0(x2-1,X1,X2+1),t0(x2,X1,X2+1),
											t0(x2-1,X1-1,X2+1),t0(x2-1,X1+1,X2+1),
											s0(x2,X1,X2),
											s0(x2-1,X1,X2),s0(x2-1,X1,X2+1),s0(x2,X1,X2+1) );
									if (tryit<guess)  guess = tryit;
								}
								if(time0[index-nxy] < 1.e9 && X2>0 && X1>y1+1 && X1<y2-1 )  {
									tryit = fdhne(t0(x2-1,X1,X2),t0(x2-1,X1,X2-1),t0(x2,X1,X2-1),
											t0(x2-1,X1-1,X2-1),t0(x2-1,X1+1,X2-1),
											s0(x2,X1,X2),
											s0(x2-1,X1,X2),s0(x2-1,X1,X2-1),s0(x2,X1,X2-1) );
									if (tryit<guess)  guess = tryit;
								}
							} 
							if(time0[index+nx] < 1.e9 && X1<ny-1 )  {
								tryit = fdh2d(t0(x2-1,X1,X2),t0(x2-1,X1+1,X2),t0(x2,X1+1,X2),
										s0(x2,X1,X2),
										s0(x2-1,X1,X2),s0(x2-1,X1+1,X2),s0(x2,X1+1,X2) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index-nx] < 1.e9 && X1>0 )  {
								tryit = fdh2d(t0(x2-1,X1,X2),t0(x2-1,X1-1,X2),t0(x2,X1-1,X2),
										s0(x2,X1,X2),
										s0(x2-1,X1,X2),s0(x2-1,X1-1,X2),s0(x2,X1-1,X2) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index+nxy] < 1.e9 && X2<nz-1 )  {
								tryit = fdh2d(t0(x2-1,X1,X2),t0(x2-1,X1,X2+1),t0(x2,X1,X2+1),
										s0(x2,X1,X2),
										s0(x2-1,X1,X2),s0(x2-1,X1,X2+1),s0(x2,X1,X2+1) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index-nxy] < 1.e9 && X2>0 )  {
								tryit = fdh2d(t0(x2-1,X1,X2),t0(x2-1,X1,X2-1),t0(x2,X1,X2-1),
										s0(x2,X1,X2),
										s0(x2-1,X1,X2),s0(x2-1,X1,X2-1),s0(x2,X1,X2-1) );
								if (tryit<guess)  guess = tryit;
							}
							if(time0[index+nx] < 1.e9 && time0[index+nxy+nx] < 1.e9
									&& time0[index+nxy] < 1.e9 && X2<nz-1  && X1<ny-1 ) {
								tryit = fdh2d(t0(x2,X1+1,X2),t0(x2,X1+1,X2+1),t0(x2,X1,X2+1),
										s0(x2,X1,X2),
										s0(x2,X1+1,X2),s0(x2,X1+1,X2+1),s0(x2,X1,X2+1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(time0[index+nx] < 1.e9 && time0[index-nxy+nx] < 1.e9
									&& time0[index-nxy] < 1.e9 && X2>0  && X1<ny-1 ) {
								tryit = fdh2d(t0(x2,X1+1,X2),t0(x2,X1+1,X2-1),t0(x2,X1,X2-1),
										s0(x2,X1,X2),
										s0(x2,X1+1,X2),s0(x2,X1+1,X2-1),s0(x2,X1,X2-1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(time0[index-nx] < 1.e9 && time0[index+nxy-nx] < 1.e9
									&& time0[index+nxy] < 1.e9 && X2<nz-1  && X1>0 ) {
								tryit = fdh2d(t0(x2,X1-1,X2),t0(x2,X1-1,X2+1),t0(x2,X1,X2+1),
										s0(x2,X1,X2),
										s0(x2,X1-1,X2),s0(x2,X1-1,X2+1),s0(x2,X1,X2+1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(time0[index-nx] < 1.e9 && time0[index-nxy-nx] < 1.e9
									&& time0[index-nxy] < 1.e9 && X2>0  && X1>0 ) {
								tryit = fdh2d(t0(x2,X1-1,X2),t0(x2,X1-1,X2-1),t0(x2,X1,X2-1),
										s0(x2,X1,X2),
										s0(x2,X1-1,X2),s0(x2,X1-1,X2-1),s0(x2,X1,X2-1) );
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if(guess > 1.0e9){ 
								if ( X1>y1+1 && X1<y2-1 && X2>z1+1 && X2<z2-1 ) {
									tryit = fdhnf(t0(x2-1,X1,X2),
											t0(x2-1,X1+1,X2),t0(x2-1,X1,X2+1),
											t0(x2-1,X1-1,X2),t0(x2-1,X1,X2-1),
											s0(x2,X1,X2),
											s0(x2-1,X1,X2) );
									if (tryit<guess)  guess = tryit;
								}
							} 
							tryit = t0(x2-1,X1,X2) + 0.5f*(s0(x2,X1,X2)+s0(x2-1,X1,X2));
							if (tryit<guess)  guess = tryit;
							if ( time0[index+nx]<1.e9 && X1<ny-1 )  {
								tryit = t0(x2,X1+1,X2) + 0.5f*(s0(x2,X1,X2)+s0(x2,X1+1,X2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-nx]<1.e9 && X1>0 )  {
								tryit = t0(x2,X1-1,X2) + 0.5f*(s0(x2,X1,X2)+s0(x2,X1-1,X2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index+nxy]<1.e9 && X2<nz-1 )  {
								tryit = t0(x2,X1,X2+1) + 0.5f*(s0(x2,X1,X2)+s0(x2,X1,X2+1));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-nxy]<1.e9 && X2>0 )  {
								tryit = t0(x2,X1,X2-1) + 0.5f*(s0(x2,X1,X2)+s0(x2,X1,X2-1));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if (guess<time0[index]) {
								time0[index] = guess;
								if (fhead>headtest)  headw[2]++;
							}
						}
						if(x2 == nx-1) dx2 = 0;
						x2++;
					}
				}
				x2 = (x2>nx-1) ? (nx-1):(x2);

				/* UPDATE RADIUS */
				radius++;
				//if(radius%10 == 0) fprintf(fplog,"Completed radius = %d\n",radius);
				if(radius == maxrad) rad0 = 0;

			}	/* END BIG LOOP */

			/* TEST IF REVERSE PROPAGATION IS NEEDED */

			if (headw[1]==0 && headw[2]==0 && headw[3]==0 && headw[4]==0 
					&& headw[5]==0 && headw[6]==0)
				reverse=0;
			else {
				head=0;
				if (headw[1]>0) {
					//fprintf(fplog,"Head waves found on left: %d\n",headw[1]);
					if (headw[1]>head)  {
						head = headw[1];
						srcwall = 1;
					}
				}
				if (headw[2]>0) {
					//fprintf(fplog,"Head waves found on right: %d\n",headw[2]);
					if (headw[2]>head)  {
						head = headw[2];
						srcwall = 2;
					}
				}
				if (headw[3]>0) {
					//fprintf(fplog,"Head waves found on front: %d\n",headw[3]);
					if (headw[3]>head)  {
						head = headw[3];
						srcwall = 3;
					}
				}
				if (headw[4]>0) {
					//fprintf(fplog,"Head waves found on back: %d\n",headw[4]);
					if (headw[4]>head)  {
						head = headw[4];
						srcwall = 4;
					}
				}
				if (headw[5]>0) {
					//fprintf(fplog,"Head waves found on top: %d\n",headw[5]);
					if (headw[5]>head)  {
						head = headw[5];
						srcwall = 5;
					}
				}
				if (headw[6]>0) {
					//fprintf(fplog,"Head waves found on bottom: %d\n",headw[6]);
					if (headw[6]>head)  {
						head = headw[6];
						srcwall = 6;
					}
				}
				if (headpref>0 && headw[headpref]>0) {
					//fprintf(fplog,"Preference to restart on wall opposite source\n");
					srcwall = headpref;
				}
				/* SET LOCATIONS OF SIDES OF THE CUBE SO THAT CUBE IS A FACE */
				dx1=1; dx2=1; dy1=1; dy2=1; dz1=1; dz2=1; rad0=1;
				radius = 1;
				if (srcwall == 1)	{  x2=1;
				//fprintf(fplog,"RESTART at left side of model\n");  
				} else	{  x2=nx;	dx2=0;  }
				if (srcwall == 2)	{ x1=nx-2;
				//fprintf(fplog,"RESTART at right side of model\n");  
				} else	{  x1= -1;	dx1=0;  }
				if (srcwall == 3)	{ y2=1;
				//fprintf(fplog,"RESTART at front side of model\n");  
				} else	{  y2=ny;	dy2=0;  }
				if (srcwall == 4)	{ y1=ny-2;
				//fprintf(fplog,"RESTART at back side of model\n");  
				} else	{  y1= -1;	dy1=0;  }
				if (srcwall == 5)	{ z2=1;
				//fprintf(fplog,"RESTART at top side of model\n");  
				} else	{  z2=nz;	dz2=0;  }
				if (srcwall == 6)	{ z1=nz-2;
				//fprintf(fplog,"RESTART at bottom side of model\n");  
				} else	{  z1= -1;	dz1=0;  }
				//if (reverse == 0)  
				//fprintf(fplog,"WARNING:  RESTART CANCELLED by choice of input parameter 'reverse'\n");
			}
			reverse--;

		}	/* END BIGGER LOOP - HOLE */

		return 0;
	}


	/* -------------------------------------------------------------------------- */


	/* RICHARD'S EXTENDED STENCIL */
	private float fd5(float t1,float t2,float t4,float t3,float t5,float t6,float t7,float slo)
	{
		float x, inc1;
		//	double sqrt();
		x = 6.0f*slo*slo - (t1-t2)*(t1-t2) - (t2-t3)*(t2-t3) - (t3-t1)*(t3-t1);
		x -= (t4-t5)*(t4-t5) + (t5-t6)*(t5-t6) + (t6-t4)*(t6-t4);
		if( x < 0 ) {
			/*	fprintf(fplog,"Warning: x<0 in fd5: Richard: x= %f\n",x);
				fprintf(fplog,"      slo= %f\n",slo);*/
			x = 0.0f;
		}
		inc1 = (float)(Math.sqrt(x)/1.41428);
		x = t7 + inc1;
		/* FOR STABILITY, ENSURE THAT NEW POINT IS LATER THAN OLD POINTS */
		if( x < t1 ) x = t1;
		if( x < t2 ) x = t2;
		if( x < t3 ) x = t3;
		if( x < t4 ) x = t4;
		if( x < t5 ) x = t5;
		if( x < t6 ) x = t6;
		if( x < t7 ) x = t7;
		return(x);
	}


	/* DIFFERENT ORDER USED IN SIDES, STUPID MISTAKE */
	private float fd6(float t1,float t2,float t3,float t4,float t5,float t6,float t7,float slo)
	{
		float x, inc1;
		//	double sqrt();
		x = 6.0f*slo*slo - (t1-t2)*(t1-t2) - (t2-t3)*(t2-t3) - (t3-t1)*(t3-t1);
		x -= (t4-t5)*(t4-t5) + (t5-t6)*(t5-t6) + (t6-t4)*(t6-t4);
		if( x < 0 ) {
			/*		fprintf(fplog,"Warning: x<0 in fd6: different: x= %f\n",x);
				fprintf(fplog,"      slo= %f\n",slo); */
			x = 0.0f;
		}
		inc1 = (float)(Math.sqrt(x)/1.41428);
		x = t7 + inc1;
		/* FOR STABILITY, ENSURE THAT NEW POINT IS LATER THAN OLD POINTS */
		if( x < t1 ) x = t1;
		if( x < t2 ) x = t2;
		if( x < t3 ) x = t3;
		if( x < t4 ) x = t4;
		if( x < t5 ) x = t5;
		if( x < t6 ) x = t6;
		if( x < t7 ) x = t7;
		return(x);
	} 


	/* FIND EXACT SOLUTION ON MAIN GRID */
	private float ex0(int nx,int ny,int nz,int xs,int ys,int zs,int index)
	{
		int nxr, nyr, nzr;
		float tryit;
		//	double sqrt();
		nxr = ((index%(nx*ny))%nx);
		nyr = (index%(nx*ny))/nx;
		nzr = index/(nx*ny);
		tryit = (float)Math.sqrt((float)((xs-nxr)*(xs-nxr) + (ys-nyr)*(ys-nyr) + (zs-nzr)*(zs-nzr)));
		return(tryit);
	}


	/* NEW FACE STENCIL, JEV, 11-15-88 */
	private float fd7(float t1,float t2,float t3,float t4,float t5,float slo)
	{
		float x;
		//	double sqrt();
		x = slo*slo - 0.25f*((t1-t3)*(t1-t3) + (t2-t4)*(t2-t4));
		if( x < 0 ) {
			/*	fprintf(fplog,"Warning: x<0 in fd7: new face \n");*/
			x = 0.0f;
		}
		x = (float)(t5 + Math.sqrt(x));
		/* FOR STABILITY, ENSURE THAT NEW POINT IS LATER THAN OLD POINTS */
		if( x < t1 ) x = t1;
		if( x < t2 ) x = t2;
		if( x < t3 ) x = t3;
		if( x < t4 ) x = t4;
		return(x);
	}


	/* NEW EDGE STENCIL, JEV, 11-15-88 */
	private float fd8(float t3,float t4,float t1,float t2,float t5,float slo)
	{
		float x;
		//	double sqrt();
		x = slo*slo*2.0f - (t1-t2)*(t1-t2)*0.5f - (t3-t4)*(t3-t4);
		if( x < 0 ) {
			/*	fprintf(fplog,"Warning: x<0 in fd8: new edge \n");*/
			x = 0.0f;
		}
		x = (float)(t5 + Math.sqrt(x));
		/* FOR STABILITY, ENSURE THAT NEW POINT IS LATER THAN OLD POINTS */
		if( x < t1 ) x = t1;
		if( x < t2 ) x = t2;
		if( x < t3 ) x = t3;
		if( x < t4 ) x = t4;
		return(x);
	}


	/* 3D TRANSMISSION STENCIL
		STENCIL FROM VIDALE; CONDITIONS AND OTHER OPTIONS FROM HOLE
		JAH 11/91 */
	private float fdh3d(float t1,float t2,float t3,float t4,float t5,float t6,float t7,float ss0,float s1,float s2,float s3,float s4,float s5,float s6,float s7)
	/* ss0 at newpoint; s1,t1 adjacent on oldface;
		s2,t2 and s4,t4 on oldface adjacent to s1;
		s3,t3 on oldface diametrically opposite newpoint;
		s5,t5 on newface adjacent to newpoint AND to s2;
		s6,t6 on newface diagonal to newpoint (adjacent to s3);
		s7,t7 on newface adjacent to newpoint AND to s4
	 */
	{
		float x,slo;
		//  double sqrt();
		slo = 0.125f*(ss0+s1+s2+s3+s4+s5+s6+s7);
		x = 6.0f*slo*slo - (t4-t2)*(t4-t2) - (t2-t6)*(t2-t6) - (t6-t4)*(t6-t4)
				- (t7-t5)*(t7-t5) - (t5-t1)*(t5-t1) - (t1-t7)*(t1-t7);
		if (x>=0.)  {
			x = (float)(t3 + Math.sqrt(0.5f*x));
			if ( (x<t1) || (x<t2) || (x<t4) || (x<t5) || (x<t6) || (x<t7) )  
				x = 1.e11f;   /* ACAUSAL; ABORT */
		}
		else  x = 1.e11f;   /* SQRT IMAGINARY; ABORT */
		return(x);
	}


	/* 3D STENCIL FOR NEW EDGE
		STENCIL FROM VIDALE; CONDITIONS AND OTHER OPTIONS FROM HOLE
		JAH 11/91 */
	private float fdhne(float t1,float t2,float t3,float t4,float t5,float ss0,float s1,float s2,float s3)
	/* ss0 at newpoint; s1,t1 adjacent on oldface;
		s2,t2 diagonal on oldface; s3,t3 adjacent on newface;
		t4,t5 beside t2 on old face opposite each other */
	{
		float x,slo;
		//  double sqrt();
		slo = 0.25f*(ss0+s1+s2+s3);
		x = 2.0f*slo*slo - (t3-t1)*(t3-t1) - 0.5f*(t5-t4)*(t5-t4);
		if (x>=0.)  {
			x = (float)(t2 + Math.sqrt(x));
			if ( (x<t1) || (x<t3) || (x<t4) || (x<t5) )     /* ACAUSAL; ABORT */
				x = 1.e11f;
		}
		else  x = 1.e11f;   /* SQRT IMAGINARY; ABORT */
		return(x);
	}


	/* 2D TRANSMISSION STENCIL (FOR HEAD WAVES ON FACES OF GRID CELLS)
		STENCIL FROM VIDALE (1988 2D PAPER); CONDITIONS AND OTHER OPTIONS FROM HOLE
		JAH 11/91 */
	private float fdh2d(float t1,float t2,float t3,float ss0,float s1,float s2,float s3)
	/* ss0 at newpoint; s1,t1 & s3,t3 adjacent; s2,t2 diagonal
	 */
	{
		float x,slo;
		//  double sqrt();
		slo = 0.25f*(ss0+s1+s2+s3);
		x = 2.0f*slo*slo - (t3-t1)*(t3-t1);
		if (x>=0.0f)  {
			x = (float)(t2 + Math.sqrt(x));
			if ( (x<t1) || (x<t3) )  x = 1.e11f;   /* ACAUSAL; ABORT */
		}
		else  x = 1.e11f;   /* SQRT IMAGINARY; ABORT */
		return(x);
	}


	/* 3D STENCIL FOR NEW FACE
		STENCIL FROM VIDALE; CONDITIONS AND OTHER OPTIONS FROM HOLE
		JAH 11/91 */
	private float fdhnf(float t1,float t2,float t3,float t4,float t5,float ss0,float s1)
	/* ss0 at newpoint; s1,t1 adjacent on old face;
		t2,t4 beside t1 on old face and opposite each other;
		t3,t5 beside t1 on old face and opposite each other
	 */
	{
		float x,slo;
		//  double sqrt();
		slo = 0.5f*(ss0+s1);
		x = slo*slo - 0.25f*( (t4-t2)*(t4-t2) + (t5-t3)*(t5-t3) );
		if (x>=0.)  {
			x = (float)(t1 + Math.sqrt(x));
			if ( (x<t2) || (x<t3) || (x<t4) || (x<t5) )     /* ACAUSAL; ABORT */
				x = 1.e11f;
		}
		else  x = 1.e11f;   /* SQRT IMAGINARY; ABORT */
		return(x);
	}


	private class Sorted implements Comparable<Sorted> {
		private int _i1, _i2;
		private float _time;

		public Sorted() { this(0, 0, 0);  }
		public Sorted(int i1, int i2, float time) {
			_i1 = i1;
			_i2 = i2;
			_time = time;
		}

		public int getI1() 					{ return _i1; }
		public int getI2() 					{ return _i2; }
		public float getTime() 				{ return _time; }

		public void setI1(int i1) 			{ _i1 = i1; }
		public void setI2(int i2) 			{ _i2 = i2; }
		public void setTime(float time) 	{ _time = time; }

		@Override
		public int compareTo(Sorted emp) {
			//let's sort the employee based on id in ascending order
			//returns a negative integer, zero, or a positive integer as this employee id
			//is less than, equal to, or greater than the specified object.
			if((_time - emp._time)<0) return -1;
			else if((_time - emp._time)>0) return 1;
			else return 0;
		}
	}

}

