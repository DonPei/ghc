package com.geohammer.rt;

public class Punch {

	/*    punch   */

	/*  from Vidale's slug3d
	edited by j.hole 02-08-90    add ability to accept as
	source some input values of travel times on the faces
	of the data volume  
	edited by j.hole 06-09-90    fix bugs in Vidale's code
	remove edges and corners, including
	them in the sides calculation        add ability to choose
	rate of speed of box expansion in each of 6 directions
	**** RENAMED slugjah.c
	edited by j.hole 17-09-90    replace source cube of constant
	velocity with a source cube of linear velocity gradient
	edited by j.hole 11-91       new source option: input a tt field
	that has already been calculated, start calculations on any
	wall of volume, change tt only if a smaller value is found
	(allows rays to travel back into expanding cube)
	MAJOR edits by j.hole 11/91  new fd stencils and stencil logic
	to attempt to allow head waves to travel parallel the faces 
	of the expanding cube (and at some other angles too)
	**** RENAMED bull3d.c
	MAJOR edits by j.hole 01/93  extended bull3d.c logic to 
	systematically test all possible operators
	**** RENAMED bully.c
	MAJOR edits by j.hole 12/93  removed bug in bully.c;  only use
	"new face" and "new edge" stencils on new faces and edges!
	**** RENAMED punch.c
	edited by j.hole 01/95  added ability to automatically detect 
	head waves travelling on the faces of the expanding cube 
	of already-calculated values; then restarts calculations 
	at appropriate wall of the model to allow the waves to 
	travel back into the (previous) expanding cube
	****Hole, J.A., and B.C. Zelt, 1995.  3-D finite-difference reflection 
	traveltimes.  Geophys. J. Int., 121, 427-434.
	This reference describes the modifications for head waves 
	made 11-91, 01/93, 12/93, and 01/95.  
	edited by j.hole spring/98   allowed an x0,y0,z0 origin to be input
	edited by j.hole 05/99   double precision on source cube calculation
	edited by Donghong Pei 1/22/09 fix bugs( index x1, x2,.. are -1 at edges)  
	*/


	/*  RECEIVED IN E-MAIL 12-02-90  */
	/* 
	Message inbox:3 -  Read
	From:    <vid@flatte.ucsc.edu>
	To:      <hole@geop.ubc.ca>, <vid@flatte.ucsc.edu>
	Cc:      <vid@rupture.ucsc.edu>
	*** NOTE THAT THESE ADDRESSES ARE OUTDATED ***
	Subject: Re:  3-D Travel Times

	I can send the code, but this week I am too busy to send sample input and 
	output files.
	The code is included below.
	No special compiling options are necessary.
	Two input files are necessary.
	One, in ascii, has lines that look like:

	nx=100
	ny=100
	nz=100
	xs=10
	ys=10
	zs=10
	h=1
	timefile=run0.times
	velfile=input.vel

	(Notice that there are no spaces.)
	The other file is the velocity field (an nx by ny by nz array) that is
	written in C binary, and with the parameter file above would have the
	name "input.vel".
	When the program is finished, the traveltimes to all the grid points will
	appear in a file called run0.times.
	If the C code was compiled with the command "cc time3d.c -o time3d", and
	the parameter file is named "time.par", the
	program would run with the command "time3d par=time.par".
	If the program gives the correct answer for a uniform wholespace, 
	it is probably working.
	Send me some mail if something doesn't work, and give me a call (408 459-4585)
	if I don't answer the e-mail (our computers are in a state of flux).   */

	/* PROGRAM TO CALCULATE TRAVEL TIMES IN 3D MEDIA */
	/* author: John E. Vidale, started Sept. 11, 1986*/
	/*  restarted, 8-11-88, with more accurate scheme */
	/*  last revised, 2-89, by Glenn Nelson for 5x5x5 source */
	/* UC Santa Cruz, all rights reserved */

	#define PI			3.141592654
	#define SQR2		1.414213562
	#define SQR3		1.732050808
	#define SQR6		2.449489743

	#define	SQR(x)					((x) * (x))
	#define	DIST(x,y,z,x1,y1,z1)	sqrt(SQR(x-(x1))+SQR(y-(y1)) + SQR(z-(z1)))

	/* FUNCTION DECLARATIONS	*/
	int compar(const void *a, const void *b);
	float fd5(float t1, float t2,float t4,float t3,float t5,float t6,float t7,float slo);
	float fd6(float t1,float t2,float t3,float t4,float t5,float t6,float t7,float slo);
	float ex0(int nx,int ny,int nz,int xs,int ys,int zs,int index);
	float fd7(float t1,float t2,float t3,float t4,float t5,float slo);
	float fd8(float t3,float t4,float t1,float t2,float t5,float slo);
	float fdh3d(float t1,float t2,float t3,float t4,float t5,float t6,float t7,float ss0,
				float s1,float s2,float s3,float s4,float s5,float s6,float s7);
	float fdhne(float t1,float t2,float t3,float t4,float t5,float ss0,float s1,float s2,float s3);
	float fdh2d(float t1,float t2,float t3,float ss0,float s1,float s2,float s3);
	float fdhnf(float t1,float t2,float t3,float t4,float t5,float ss0,float s1);

	int Get3DTT(const int nx, const int ny, const int nz, const float h, const int srctype, const int NCUBE, 
				float x0, float y0, float z0, float fxs, float fys, float fzs, 
				int nwall, struct sorted *sort, float *slow0, float *wall, float *time0 )
	{
	#define t0(x,y,z)				time0[nxy*(z) + nx*(y) + (x)]
	#define s0(x,y,z)				slow0[nxy*(z) + nx*(y) + (x)]

//		 note that several variables must be specified in par=xxx file,
//		while others are optional:  if a mstpar statement reads the
//		variable below, then the variable is required;  if a getpar
//		statement is used below, then the variable is optional
		int
			iplus=1,		/* rate of expansion of "cube" in the */
			iminus=1,		/*    plus/minus x/y/z direction */
			jplus=1, jminus=1, kplus=1, kminus=1,
			igrow,			/* counter for "cube" growth */
			floatsrc,		/* if 0, source must be on a grid node; 1, source can lie between grid nodes */
			srcwall,        /* if 1, source on x=0 wall, if 2, on x=nx-1 wall */
			/* if 3, source on y=0 wall, if 4, on y=ny-1 wall */
			/* if 5, source on z=0 wall, if 6, on z=nz-1 wall */
			xs,				/* shot x position (in grid points) */
			ys,				/* shot y position */
			zs,				/* shot depth */
			xx, yy, zz,		/* Used to loop around xs, ys, zs coordinates	*/
			X1, X2, lasti, index, ii, i, j, k, radius, nxy, nyz, nxz, nxyz,
			x1, x2, y1, y2, z1, z2,								/* counters for the position of the sides of current cube */
			dx1=1, dx2=1, dy1=1, dy2=1, dz1=1, dz2=1, rad0=1,	/* flags set to 1 until a side has been reached */
			maxrad,			/* maximum radius to compute used in linear velocity gradient cube source */
			reverse=1,		/* will automatically do up to this number of reverse propagation steps to fix waves that travel back into expanding cube */
			headpref=6,		/* if headpref starts > 0, will determine model wall closest to source and will prefer to start */
			/* reverse calculations on opposite wall counters for detecting head waves on sides of current cube */
			head, headw[7];
		float
			a, guess, tryit,
			maxoff = -1.,			/* maximum offset (real units) to compute */
			fhead, headtest=1.e-3;	/* used to detect head waves:  if headwave operator decreases the previously-computed traveltime by at least */
			/* headtest*<~time_across_cell> then the headwave counter is triggered */
		double
			/* used in linear velocity gradient cube source */
			rx, ry, rz, dvz, dv, v0, rzc, rxyc, rz1, rxy1, rho, theta1, theta2;

		FILE *fpv;
		//FILE *fplog, *fpv, *fidt;
		//fplog = fopen("Get3Dlog.out", "w");
		//fidt = fopen("time.3d", "wb");

		nxyz = nx*ny*nz; nxy = nx*ny; nxz = nx*nz; nyz = ny*nz; 
		// Start loop over receivers
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
		}
		else{
			fxs = (fxs-x0)/h;
			fys = (fys-y0)/h;
			fzs = (fzs-z0)/h;
			xs = (int) (fxs+0.5);
			ys = (int) (fys+0.5);
			zs = (int) (fzs+0.5);
		}

	/*
		if(xs<2 || ys<2 || zs<2 || xs>nx-3 || ys>ny-3 || zs>nz-3){
			fprintf(fplog,"Source near an edge, beware of traveltime errors\n");
			fprintf(fplog,"for raypaths that travel parallel to edge \n");
			fprintf(fplog,"while wavefronts are strongly curved, (JV, 8/17/88)\n");
		}
	*/
		/* SET MAXIMUM RADIUS TO COMPUTE */
		if (maxoff > 0.) {
			maxrad = maxoff/h + 1;
			//fprintf(fplog,"WARNING: Computing only to max radius = %d\n",maxrad);
		}
		else   maxrad = 99999999;

		for(i=0;i<nxyz;i++) {
			slow0[i] = h*slow0[i];
			time0[i] = 1.0e10;		/* SET TIMES TO DUMMY VALUE ***** JAH ***** BUG IN VIDALE'S CODE */
		}

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
						dv = fabs(dvz);
						if (dv == 0.)  {
							t0(xx,yy,zz) = s0(xs,ys,zs)*DIST(fxs,fys,fzs,xx,yy,zz);
							continue;
						}
						rzc = -v0/dv;
						rx = h*(xx - fxs);
						ry = h*(yy - fys);
						rz = h*(zz - fzs);
						rz1 = rz*dvz/dv;
						rxy1 = sqrt(rx*rx+ry*ry);
						/*rxy1 = sqrt(rx*rx+ry*ry+rz*rz-rz1*rz1);*/
						if (rxy1<=h/1.e6)
							t0(xx,yy,zz) = fabs(log((v0+dv*rz1)/v0)/dv);
						else {
							rxyc = (rz1*rz1+rxy1*rxy1-2*rz1*rzc)/(2*rxy1);
							rho = sqrt(rzc*rzc+rxyc*rxyc);
							theta1 = asin(-rzc/rho);
							/* can't handle asin(1.) ! */
							if (fabs(rz1-rzc)>=rho)  rho=1.0000001*fabs(rz1-rzc);
							theta2 = asin((rz1-rzc)/rho);
							if (rxyc<0) theta1=PI-theta1;
							if (rxyc<rxy1) theta2=PI-theta2;
							t0(xx,yy,zz) = log(tan(theta2/2)/tan(theta1/2)) / dv;
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
			/*
			read (wfint,wall,4*nwall);	// READ X=0 WALL
			if (wall[0]>-1.e-20) {
			ii = 0;
			for (k=0; k<nz; k++) {
			for (j=0; j<ny; j++) {
			t0(0,j,k) = wall[ii];
			ii++;
			}
			}
			}
			read (wfint,wall,4*nwall);	// READ X=NX-1 WALL
			if (wall[0]>-1.e-20) {
			ii = 0;
			for (k=0; k<nz; k++) {
			for (j=0; j<ny; j++) {
			t0(nx-1,j,k) = wall[ii];
			ii++;
			}
			}
			}
			read (wfint,wall,4*nwall);	// READ Y=0 WALL
			if (wall[0]>-1.e-20) {
			ii = 0;
			for (k=0; k<nz; k++) {
			for (i=0; i<nx; i++) {
			t0(i,0,k) = wall[ii];
			ii++;
			}
			}
			}
			read (wfint,wall,4*nwall);	// READ Y=NY-1 WALL
			if (wall[0]>-1.e-20) {
			ii = 0;
			for (k=0; k<nz; k++) {
			for (i=0; i<nx; i++) {
			t0(i,ny-1,k) = wall[ii];
			ii++;
			}
			}
			}
			read (wfint,wall,4*nwall);	// READ Z=0 WALL
			if (wall[0]>-1.e-20) {
			ii = 0;
			for (j=0; j<ny; j++) {
			for (i=0; i<nx; i++) {
			t0(i,j,0) = wall[ii];
			ii++;
			}
			}
			}
			read (wfint,wall,4*nwall);	// READ Z=NZ-1 WALL
			if (wall[0]>-1.e-20) {
			ii = 0;
			for (j=0; j<ny; j++) {
			for (i=0; i<nx; i++) {
			t0(i,j,nz-1) = wall[ii];
			ii++;
			}
			}
			}
			// SET LOCATIONS OF SIDES OF THE CUBE SO THAT CUBE IS A FACE
			radius = 1;
			if (srcwall == 1)	x2=1;
			else	{  x2=nx;	dx2=0;  }
			if (srcwall == 2)	x1=nx-2;
			else	{  x1= -1;	dx1=0;  }
			if (srcwall == 3)	y2=1;
			else	{  y2=ny;	dy2=0;  }
			if (srcwall == 4)	y1=ny-2;
			else	{  y1= -1;	dy1=0;  }
			if (srcwall == 5)	z2=1;
			else	{  z2=nz;	dz2=0;  }
			if (srcwall == 6)	z1=nz-2;
			else	{  z1= -1;	dz1=0;  }
			*/
		}   
		else if (srctype == 3) {                //  HOLE'S REDO OLD TIMES
			/* READ IN OLD TIME FILE */
			//	        if (srctype == 3)  read(ofint,time0,nxyz*4);
			if(srctype == 3) fread(time0, 4, nxyz, fpv);
			/* SET LOCATIONS OF SIDES OF THE CUBE SO THAT CUBE IS A FACE */
			radius = 1;
			if (srcwall == 1)	x2=1;
			else	{  x2=nx;	dx2=0;  }
			if (srcwall == 2)	x1=nx-2;
			else	{  x1= -1;	dx1=0;  }
			if (srcwall == 3)	y2=1;
			else	{  y2=ny;	dy2=0;  }
			if (srcwall == 4)	y1=ny-2;
			else	{  y1= -1;	dy1=0;  }
			if (srcwall == 5)	z2=1;
			else	{  z2=nz;	dz2=0;  }
			if (srcwall == 6)	z1=nz-2;
			else	{  z1= -1;	dz1=0;  }
		}
		else  {
			//fprintf(fplog,"incorrect value of srctype = %d\n",srctype);
			exit(-1);
		}

		if (headpref>0) {	/* HOLE - PREFERRED REVERSE DIRECTION */
			head = nx*ny*nz;
			if (nx>5 && x2<=head)   {headpref=2;  head=x2;}
			if (nx>5 && (nx-1-x1)<=head)   {headpref=1;  head=nx-1-x1;}
			if (ny>5 && y2<=head)   {headpref=4;  head=y2;}
			if (ny>5 && (ny-1-y1)<=head)   {headpref=3;  head=ny-1-y1;}
			if (nz>5 && z2<=head)   {headpref=6;  head=z2;}
			if (nz>5 && (nz-1-z1)<=head)   {headpref=5;  head=nz-1-z1;}
		}

		/* BIGGER LOOP - HOLE - ALLOWS AUTOMATIC REVERSE PROPAGATION IF 
		HEAD WAVES ARE ENCOUNTERED ON FACES OF EXPANDING CUBE, 
		ALLOWING WAVES TO TRAVEL BACK INTO THE CUBE */
		while ( reverse > -1 )  {
			headw[1]=0; headw[2]=0; headw[3]=0; headw[4]=0;
			headw[5]=0; headw[6]=0;

			/* BIG LOOP */
			while(rad0 && (dx1 || dx2 || dy1 || dy2 || dz1 || dz2))  {

				/* CALCULATE ON PRIMARY (time0) GRID */

				/* TOP SIDE */
				for (igrow=1;igrow<=kminus;igrow++) {
					if(dz1){
						ii = 0;
						for(j=y1+1; j<=y2-1; j++){
							for(i=x1+1; i<=x2-1; i++){
								sort[ii].time = t0(i,j,z1+1);
								sort[ii].i1 = i;
								sort[ii].i2 = j;
								ii++;
							}
						}
						qsort((void *)sort, ii, sizeof(sort[0]), compar);
						for(i=0;i<ii;i++){
							X1 = sort[i].i1;
							X2 = sort[i].i2;
							index = z1*nxy + X2*nx + X1;
							lasti = (z1+1)*nxy + X2*nx + X1;
							fhead = 0.;
							guess = time0[index];
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
							tryit = t0(X1,X2,z1+1) + .5*(s0(X1,X2,z1)+s0(X1,X2,z1+1));
							if (tryit<guess)  guess = tryit;
							if ( time0[index+1]<1.e9 && X1<nx-1 )  {
								tryit = t0(X1+1,X2,z1) + .5*(s0(X1,X2,z1)+s0(X1+1,X2,z1));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-1]<1.e9 && X1>0 )  {
								tryit = t0(X1-1,X2,z1) + .5*(s0(X1,X2,z1)+s0(X1-1,X2,z1));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index+nx]<1.e9 && X2<ny-1 )  {
								tryit = t0(X1,X2+1,z1) + .5*(s0(X1,X2,z1)+s0(X1,X2+1,z1));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-nx]<1.e9 && X2>0 )  {
								tryit = t0(X1,X2-1,z1) + .5*(s0(X1,X2,z1)+s0(X1,X2-1,z1));
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
					if(dz2){
						ii = 0;
						for(j=y1+1; j<=y2-1; j++){
							for(i=x1+1; i<=x2-1; i++){
								sort[ii].time = t0(i,j,z2-1);
								sort[ii].i1 = i;
								sort[ii].i2 = j;
								ii++;
							}
						}
						qsort((char *)sort,ii,sizeof(struct sorted),compar);
						for(i=0;i<ii;i++){
							X1 = sort[i].i1;
							X2 = sort[i].i2;
							index = z2*nxy + X2*nx + X1;
							lasti = (z2-1)*nxy + X2*nx + X1;
							fhead = 0.;
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
							tryit = t0(X1,X2,z2-1) + .5*(s0(X1,X2,z2)+s0(X1,X2,z2-1));
							if (tryit<guess)  guess = tryit;
							if ( time0[index+1]<1.e9 && X1<nx-1 )  {
								tryit = t0(X1+1,X2,z2) + .5*(s0(X1,X2,z2)+s0(X1+1,X2,z2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-1]<1.e9 && X1>0 )  {
								tryit = t0(X1-1,X2,z2) + .5*(s0(X1,X2,z2)+s0(X1-1,X2,z2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index+nx]<1.e9 && X2<ny-1 )  {
								tryit = t0(X1,X2+1,z2) + .5*(s0(X1,X2,z2)+s0(X1,X2+1,z2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-nx]<1.e9 && X2>0 )  {
								tryit = t0(X1,X2-1,z2) + .5*(s0(X1,X2,z2)+s0(X1,X2-1,z2));
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
					if(dy1){
						ii = 0;
						for(k=z1+1; k<=z2-1; k++){
							for(i=x1+1; i<=x2-1; i++){
								sort[ii].time = t0(i,y1+1,k);
								sort[ii].i1 = i;
								sort[ii].i2 = k;
								ii++;
							}
						}
						qsort((char *)sort,ii,sizeof(struct sorted),compar);
						for(i=0;i<ii;i++){
							X1 = sort[i].i1;
							X2 = sort[i].i2;
							index = X2*nxy + y1*nx + X1;
							lasti = X2*nxy + (y1+1)*nx + X1;
							fhead = 0.;
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
							tryit = t0(X1,y1+1,X2) + .5*(s0(X1,y1,X2)+s0(X1,y1+1,X2));
							if (tryit<guess)  guess = tryit;
							if ( time0[index+1]<1.e9 && X1<nx-1 )  {
								tryit = t0(X1+1,y1,X2) + .5*(s0(X1,y1,X2)+s0(X1+1,y1,X2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-1]<1.e9 && X1>0 )  {
								tryit = t0(X1-1,y1,X2) + .5*(s0(X1,y1,X2)+s0(X1-1,y1,X2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index+nxy]<1.e9 && X2<nz-1 )  {
								tryit = t0(X1,y1,X2+1) + .5*(s0(X1,y1,X2)+s0(X1,y1,X2+1));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-nxy]<1.e9 && X2>0 )  {
								tryit = t0(X1,y1,X2-1) + .5*(s0(X1,y1,X2)+s0(X1,y1,X2-1));
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
					if(dy2){
						ii = 0;
						for(k=z1+1; k<=z2-1; k++){
							for(i=x1+1; i<=x2-1; i++){
								sort[ii].time = t0(i,y2-1,k);
								sort[ii].i1 = i;
								sort[ii].i2 = k;
								ii++;
							}
						}
						qsort((char *)sort,ii,sizeof(struct sorted),compar);
						for(i=0;i<ii;i++){
							X1 = sort[i].i1;
							X2 = sort[i].i2;
							index = X2*nxy + y2*nx + X1;
							lasti = X2*nxy + (y2-1)*nx + X1;
							fhead = 0.;
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
							tryit = t0(X1,y2-1,X2) + .5*(s0(X1,y2,X2)+s0(X1,y2-1,X2));
							if (tryit<guess)  guess = tryit;
							if ( time0[index+1]<1.e9 && X1<nx-1 )  {
								tryit = t0(X1+1,y2,X2) + .5*(s0(X1,y2,X2)+s0(X1+1,y2,X2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-1]<1.e9 && X1>0 )  {
								tryit = t0(X1-1,y2,X2) + .5*(s0(X1,y2,X2)+s0(X1-1,y2,X2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index+nxy]<1.e9 && X2<nz-1 )  {
								tryit = t0(X1,y2,X2+1) + .5*(s0(X1,y2,X2)+s0(X1,y2,X2+1));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-nxy]<1.e9 && X2>0 )  {
								tryit = t0(X1,y2,X2-1) + .5*(s0(X1,y2,X2)+s0(X1,y2,X2-1));
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
					if(dx1){
						ii = 0;
						for(k=z1+1; k<=z2-1; k++){
							for(j=y1+1; j<=y2-1; j++){
								sort[ii].time = t0(x1+1,j,k);
								sort[ii].i1 = j;
								sort[ii].i2 = k;
								ii++;
							}
						}
						qsort((char *)sort,ii,sizeof(struct sorted),compar);
						for(i=0;i<ii;i++){
							X1 = sort[i].i1;
							X2 = sort[i].i2;
							index = X2*nxy + X1*nx + x1;
							lasti = X2*nxy + X1*nx + (x1+1);
							fhead = 0.;
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
							tryit = t0(x1+1,X1,X2) + .5*(s0(x1,X1,X2)+s0(x1+1,X1,X2));
							if (tryit<guess)  guess = tryit;
							if ( time0[index+nx]<1.e9 && X1<ny-1 )  {
								tryit = t0(x1,X1+1,X2) + .5*(s0(x1,X1,X2)+s0(x1,X1+1,X2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-nx]<1.e9 && X1>0 )  {
								tryit = t0(x1,X1-1,X2) + .5*(s0(x1,X1,X2)+s0(x1,X1-1,X2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index+nxy]<1.e9 && X2<nz-1 )  {
								tryit = t0(x1,X1,X2+1) + .5*(s0(x1,X1,X2)+s0(x1,X1,X2+1));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-nxy]<1.e9 && X2>0 )  {
								tryit = t0(x1,X1,X2-1) + .5*(s0(x1,X1,X2)+s0(x1,X1,X2-1));
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
					if(dx2){
						ii = 0;
						for(k=z1+1; k<=z2-1; k++){
							for(j=y1+1; j<=y2-1; j++){
								sort[ii].time = t0(x2-1,j,k);
								sort[ii].i1 = j;
								sort[ii].i2 = k;
								ii++;
							}
						}
						qsort((char *)sort,ii,sizeof(struct sorted),compar);
						for(i=0;i<ii;i++){
							X1 = sort[i].i1;
							X2 = sort[i].i2;
							index = X2*nxy + X1*nx + x2;
							lasti = X2*nxy + X1*nx + (x2-1);
							fhead = 0.;
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
							tryit = t0(x2-1,X1,X2) + .5*(s0(x2,X1,X2)+s0(x2-1,X1,X2));
							if (tryit<guess)  guess = tryit;
							if ( time0[index+nx]<1.e9 && X1<ny-1 )  {
								tryit = t0(x2,X1+1,X2) + .5*(s0(x2,X1,X2)+s0(x2,X1+1,X2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-nx]<1.e9 && X1>0 )  {
								tryit = t0(x2,X1-1,X2) + .5*(s0(x2,X1,X2)+s0(x2,X1-1,X2));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index+nxy]<1.e9 && X2<nz-1 )  {
								tryit = t0(x2,X1,X2+1) + .5*(s0(x2,X1,X2)+s0(x2,X1,X2+1));
								if (tryit<guess)  {fhead=(guess-tryit)/slow0[index]; guess=tryit;}
							}
							if ( time0[index-nxy]<1.e9 && X2>0 )  {
								tryit = t0(x2,X1,X2-1) + .5*(s0(x2,X1,X2)+s0(x2,X1,X2-1));
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
				}
				else	{  x2=nx;	dx2=0;  }
				if (srcwall == 2)	{ x1=nx-2;
				//fprintf(fplog,"RESTART at right side of model\n");  
				}
				else	{  x1= -1;	dx1=0;  }
				if (srcwall == 3)	{ y2=1;
				//fprintf(fplog,"RESTART at front side of model\n");  
				}
				else	{  y2=ny;	dy2=0;  }
				if (srcwall == 4)	{ y1=ny-2;
				//fprintf(fplog,"RESTART at back side of model\n");  
				}
				else	{  y1= -1;	dy1=0;  }
				if (srcwall == 5)	{ z2=1;
				//fprintf(fplog,"RESTART at top side of model\n");  
				}
				else	{  z2=nz;	dz2=0;  }
				if (srcwall == 6)	{ z1=nz-2;
				//fprintf(fplog,"RESTART at bottom side of model\n");  
				}
				else	{  z1= -1;	dz1=0;  }
				//if (reverse == 0)  
					//fprintf(fplog,"WARNING:  RESTART CANCELLED by choice of input parameter 'reverse'\n");
			}
			reverse--;

		}	/* END BIGGER LOOP - HOLE */

		/* OUTPUT COMPLETED WAVEFRONT */
		//fwrite(time0, sizeof(time0[0]), nxyz, fidt);
		//fprintf(fplog,"wavefront done \n");
		//fclose(fpt);

		/*
		FILE *fid;
		fid = fopen("time3D.txt", "w");
		for(j=0; j<ny; j++) {
			for(k=0; k<nx; k++) {
				for(i=0; i<nz; i++ ) {
					fprintf(fid, "%f ", time0[i*ny*nx+j*nx+k]);
				}
				fprintf(fid, "\n");
			}
		}
		fclose(fid);
		*/
		//printf( "\n\ntime0=%f  \n ", time0[9*ny*nx+10*nx+10] );

		//fclose(fplog);
		//fclose(fidt);
		//delete time0;
		//delete slow0;
		//delete wall;
		//delete sort; 

	#undef t0(x,y,z)
	#undef s0(x,y,z)

		return 0;
	}

	/* -------------------------------------------------------------------------- */

	int compar(const void *a, const void *b)
	//struct sorted *a, *b;
	{
		if( ((struct sorted *)a)->time > ((struct sorted *)b)->time) return(1);
		if( ((struct sorted *)b)->time > ((struct sorted *)a)->time) return(-1);
		else return(0);
	}


	/* RICHARD'S EXTENDED STENCIL */
	float fd5(float t1,float t2,float t4,float t3,float t5,float t6,float t7,float slo)
	{
		float x, inc1;
		//	double sqrt();
		x = 6.0*slo*slo - (t1-t2)*(t1-t2) - (t2-t3)*(t2-t3) - (t3-t1)*(t3-t1);
		x -= (t4-t5)*(t4-t5) + (t5-t6)*(t5-t6) + (t6-t4)*(t6-t4);
		if( x < 0 ) {
			/*	fprintf(fplog,"Warning: x<0 in fd5: Richard: x= %f\n",x);
			fprintf(fplog,"      slo= %f\n",slo);*/
			x = 0.0;
		}
		inc1 = sqrt(x)/1.41428;
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
	float fd6(float t1,float t2,float t3,float t4,float t5,float t6,float t7,float slo)
	{
		float x, inc1;
		//	double sqrt();
		x = 6.0*slo*slo - (t1-t2)*(t1-t2) - (t2-t3)*(t2-t3) - (t3-t1)*(t3-t1);
		x -= (t4-t5)*(t4-t5) + (t5-t6)*(t5-t6) + (t6-t4)*(t6-t4);
		if( x < 0 ) {
			/*		fprintf(fplog,"Warning: x<0 in fd6: different: x= %f\n",x);
			fprintf(fplog,"      slo= %f\n",slo); */
			x = 0.0;
		}
		inc1 = sqrt(x)/1.41428;
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
	float ex0(int nx,int ny,int nz,int xs,int ys,int zs,int index)
	{
		int nxr, nyr, nzr;
		float tryit;
		//	double sqrt();
		nxr = ((index%(nx*ny))%nx);
		nyr = (index%(nx*ny))/nx;
		nzr = index/(nx*ny);
		tryit = sqrt((float)((xs-nxr)*(xs-nxr) + (ys-nyr)*(ys-nyr) + (zs-nzr)*(zs-nzr)));
		return(tryit);
	}


	/* NEW FACE STENCIL, JEV, 11-15-88 */
	float fd7(float t1,float t2,float t3,float t4,float t5,float slo)
	{
		float x;
		//	double sqrt();
		x = slo*slo - 0.25*((t1-t3)*(t1-t3) + (t2-t4)*(t2-t4));
		if( x < 0 ) {
			/*	fprintf(fplog,"Warning: x<0 in fd7: new face \n");*/
			x = 0.0;
		}
		x = t5 + sqrt(x);
		/* FOR STABILITY, ENSURE THAT NEW POINT IS LATER THAN OLD POINTS */
		if( x < t1 ) x = t1;
		if( x < t2 ) x = t2;
		if( x < t3 ) x = t3;
		if( x < t4 ) x = t4;
		return(x);
	}


	/* NEW EDGE STENCIL, JEV, 11-15-88 */
	float fd8(float t3,float t4,float t1,float t2,float t5,float slo)
	{
		float x;
		//	double sqrt();
		x = slo*slo*2.0 - (t1-t2)*(t1-t2)*0.5 - (t3-t4)*(t3-t4);
		if( x < 0 ) {
			/*	fprintf(fplog,"Warning: x<0 in fd8: new edge \n");*/
			x = 0.0;
		}
		x = t5 + sqrt(x);
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
	float fdh3d(float t1,float t2,float t3,float t4,float t5,float t6,float t7,float ss0,float s1,float s2,float s3,float s4,float s5,float s6,float s7)
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
		slo = .125*(ss0+s1+s2+s3+s4+s5+s6+s7);
		x = 6.*slo*slo - (t4-t2)*(t4-t2) - (t2-t6)*(t2-t6) - (t6-t4)*(t6-t4)
			- (t7-t5)*(t7-t5) - (t5-t1)*(t5-t1) - (t1-t7)*(t1-t7);
		if (x>=0.)  {
			x = t3 + sqrt(.5*x);
			if ( (x<t1) || (x<t2) || (x<t4) || (x<t5) || (x<t6) || (x<t7) )  
				x = 1.e11;   /* ACAUSAL; ABORT */
		}
		else  x = 1.e11;   /* SQRT IMAGINARY; ABORT */
		return(x);
	}


	/* 3D STENCIL FOR NEW EDGE
	STENCIL FROM VIDALE; CONDITIONS AND OTHER OPTIONS FROM HOLE
	JAH 11/91 */
	float fdhne(float t1,float t2,float t3,float t4,float t5,float ss0,float s1,float s2,float s3)
	/* ss0 at newpoint; s1,t1 adjacent on oldface;
	s2,t2 diagonal on oldface; s3,t3 adjacent on newface;
	t4,t5 beside t2 on old face opposite each other */
	{
		float x,slo;
		//  double sqrt();
		slo = .25*(ss0+s1+s2+s3);
		x = 2.*slo*slo - (t3-t1)*(t3-t1) - .5*(t5-t4)*(t5-t4);
		if (x>=0.)  {
			x = t2 + sqrt(x);
			if ( (x<t1) || (x<t3) || (x<t4) || (x<t5) )     /* ACAUSAL; ABORT */
				x = 1.e11;
		}
		else  x = 1.e11;   /* SQRT IMAGINARY; ABORT */
		return(x);
	}


	/* 2D TRANSMISSION STENCIL (FOR HEAD WAVES ON FACES OF GRID CELLS)
	STENCIL FROM VIDALE (1988 2D PAPER); CONDITIONS AND OTHER OPTIONS FROM HOLE
	JAH 11/91 */
	float fdh2d(float t1,float t2,float t3,float ss0,float s1,float s2,float s3)
	/* ss0 at newpoint; s1,t1 & s3,t3 adjacent; s2,t2 diagonal
	*/
	{
		float x,slo;
		//  double sqrt();
		slo = .25*(ss0+s1+s2+s3);
		x = 2.*slo*slo - (t3-t1)*(t3-t1);
		if (x>=0.)  {
			x = t2 + sqrt(x);
			if ( (x<t1) || (x<t3) )  x = 1.e11;   /* ACAUSAL; ABORT */
		}
		else  x = 1.e11;   /* SQRT IMAGINARY; ABORT */
		return(x);
	}


	/* 3D STENCIL FOR NEW FACE
	STENCIL FROM VIDALE; CONDITIONS AND OTHER OPTIONS FROM HOLE
	JAH 11/91 */
	float fdhnf(float t1,float t2,float t3,float t4,float t5,float ss0,float s1)
	/* ss0 at newpoint; s1,t1 adjacent on old face;
	t2,t4 beside t1 on old face and opposite each other;
	t3,t5 beside t1 on old face and opposite each other
	*/
	{
		float x,slo;
		//  double sqrt();
		slo = .5*(ss0+s1);
		x = slo*slo - .25*( (t4-t2)*(t4-t2) + (t5-t3)*(t5-t3) );
		if (x>=0.)  {
			x = t1 + sqrt(x);
			if ( (x<t2) || (x<t3) || (x<t4) || (x<t5) )     /* ACAUSAL; ABORT */
				x = 1.e11;
		}
		else  x = 1.e11;   /* SQRT IMAGINARY; ABORT */
		return(x);
	}





}
