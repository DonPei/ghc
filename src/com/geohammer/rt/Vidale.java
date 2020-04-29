package com.geohammer.rt;

public class Vidale {



#include <stdio.h>
#include <math.h>

double VelocityCalibrationImpl::GetAverageVelFromGrid( double *slow, double top, double bottom, VCINPUT *vcI )
{
	int i, j, k, s, z1, z2;
	double a, sum, v;

	a = (top-vcI->z0)/vcI->h;
	z1 = (int)(a + 0.5);
	a = (bottom-vcI->z0)/vcI->h;
	z2 = (int)(a + 0.5);

	sum=0.0;
	if( vcI->dimension==2 ) {
		s=z1*vcI->nx; 
		for( k=z1; k<z2; k++ ) {
			for( j=0; j<vcI->nx; j++ ) {
				sum += slow[s];
				s++;
			}
		}
	}
	else if( vcI->dimension==3 ) {
		s=z1*vcI->nx*vcI->ny; 
		for( k=z1; k<z2; k++ ) {
			for( j=0; j<vcI->ny; j++ ) {
				for( i=0; i<vcI->nx; i++ ) {
					sum += slow[s];
					s++;
				}
			}
		}
	}
	else { }
	v = (s*vcI->h)/sum;

	return v;
}

void VelocityCalibrationImpl::UpdateGridFromModel( int vp, double *slow, int dimension, VCINPUT *vcI, VCMODEL *mdl )
{
	int i, j, k, s, nx, ny, nz, fl;
	double h;
	double *v, *z, dv, z0, z1, *vz;

	fl = mdl->nLayers;
	h = vcI->h; nx = vcI->nx; ny = vcI->ny; nz = vcI->nz;

	vz = dvector( nz );
	v = dvector( fl+4 );
	z = dvector( fl+4 );

	if( vp==0 ) { for(i=0; i<fl; i++) v[i+2] = mdl->vp[i]; }
	else if( vp==1 ) { for(i=0; i<fl; i++) v[i+2] = mdl->vs[i];	}
	else if( vp==100 ) { for(i=0; i<fl; i++) v[i+2] = mdl->minVp[i];	}
	else if( vp==101 ) { for(i=0; i<fl; i++) v[i+2] = mdl->maxVp[i];	}
	else if( vp==200 ) { for(i=0; i<fl; i++) v[i+2] = mdl->minVs[i];	}
	else if( vp==201 ) { for(i=0; i<fl; i++) v[i+2] = mdl->maxVs[i];	}
	else { }
	v[fl+2] = v[fl+1];
	v[fl+3] = v[fl+2];
	v[1] = v[2];
	v[0] = v[1];

	z[0] = vcI->z0;
	z[1] = mdl->dp[0] - mdl->h[0];
	for(i=0; i<fl; i++) z[i+2] = mdl->dp[i];
	z[fl+2] = vcI->z1;
	z[fl+3] = vcI->z1+h;

	z0 = z[0];
	j=0;
	for( i=0; i<nz; i++ ) {
		z1 = h*i + z0;
		while( z1 >= z[j] ) j++;
		vz[i] = v[j-1] + (z1-z[j-1])*(v[j]-v[j-1])/(z[j]-z[j-1]);
		//fprintf(rp, "i=%d %f v=%f\n", i, z, v[i]); fflush( rp );
	}

	s=0;
	if( dimension==2 ) {
		for( k=0; k<nz; k++ ) {
			for( j=0; j<nx; j++ ) {
				slow[s] = h / vz[k];
				s++;
			}
		}
	}
	else if( dimension==3 ) {
		for( k=0; k<nz; k++ ) {
			for( j=0; j<ny; j++ ) {
				for( i=0; i<nx; i++ ) {
					slow[s] = h / vz[k];
					s++;
				}
			}
		}
	}
	else { }

	free_dvector( vz );
	free_dvector( v );
	free_dvector( z );
}


double * VelocityCalibrationImpl::GenerateGridAndUpdateVCI( int dimension, double b, VCINPUT *vcI, VCMODEL *mdl, VCPAIR *vcPW )
{

	int i, nx, ny, nz, fl, fp;
	double h;
	double minX, maxX, minY, maxY, minZ, maxZ, a; 
	double *slow=NULL;

	fl = mdl->nLayers;
	h = vcI->h;

	minX = 1.0e20; maxX = -1.0e20;
	minY = 1.0e20; maxY = -1.0e20;
	minZ = 1.0e20; maxZ = -1.0e20;

	fp=0; for(i=0; i<vcPW->nEvents; i++) fp = fp + vcPW->flag[i];

	for(i=0; i<fp; i++)
	{
		a = vcPW->rE[i];
		minX = minX < a ? minX : a;
		maxX = maxX > a ? maxX : a;
		a = vcPW->rN[i];
		minY = minY < a ? minY : a;
		maxY = maxY > a ? maxY : a;
		a = vcPW->rD[i];
		minZ = minZ < a ? minZ : a;
		maxZ = maxZ > a ? maxZ : a;

		a = vcPW->eE[i];
		minX = minX < a ? minX : a;
		maxX = maxX > a ? maxX : a;
		a = vcPW->eN[i];
		minY = minY < a ? minY : a;
		maxY = maxY > a ? maxY : a;
		a = vcPW->eD[i];
		minZ = minZ < a ? minZ : a;
		maxZ = maxZ > a ? maxZ : a;
	}

	a = mdl->dp[0] - mdl->h[0];
	minZ = minZ < a ? minZ : a;
	maxZ = maxZ > a ? maxZ : a;
	a = mdl->dp[fl-1];
	minZ = minZ < a ? minZ : a;
	maxZ = maxZ > a ? maxZ : a;

	nx = (int) (maxX-minX)/h + 3;
	ny = (int) (maxY-minY)/h + 3;
	nz = (int) (maxZ-minZ)/h + 3;
	
	vcI->nx = nx; vcI->ny = ny; vcI->nz = nz;  
	vcI->x0 = minX-h; vcI->y0 = minY-h; vcI->z0 = minZ-h;
	vcI->x1 = vcI->x0+(nx-1)*h; vcI->y1 = vcI->y0+(ny-1)*h; vcI->z1 = vcI->z0+(nz-1)*h;

	if( dimension==2 ) {
		slow = dvector( nx*nz );
		for(i=0; i<nx*nz; i++) slow[i] = b;
	}
	else if( dimension==3 ) {
		slow = dvector( nx*ny*nz );
		for(i=0; i<nx*ny*nz; i++) slow[i] = b;
	}
	else { }

	return slow;
}

int VelocityCalibrationImpl::gridGetTravelTime( const int method, const int nx, const int ny, const int nz, const double h, 
											   const int srctype, const int floatsrc, const int ncube, 
											   double x0, double y0, double z0, double fxs, double fys, double fzs, 
											   const double maxoff, double *slow, double *time )
{
	int errorID = 0;

	if( method==10 ) errorID = vidale2DTime(nx, nz, h, srctype, floatsrc, ncube, x0, z0, fxs, fzs, slow, time);

	if( errorID > 0 ) return errorID;

	return OPERATION_SUCCESS;
}

int VelocityCalibrationImpl::gridGetArrivalTime( const int method, const int nx, const int ny, const int nz, const double h, 
												const int srctype, const int floatsrc, const int ncube, const double maxoff, double *slow,
												const int nEvents, const int * flag, double x0, double y0, double z0, 
												const double *rx, const double *ry, const double *rz, 
												const double *sx, const double *sy, const double *sz, double *calT, const double *obsT, double *orT, 
												const int spMethod, const double *pTravelTime, const int oMethod, double *originTime)
{
	int i, j, s, t, tis, tiz, tjx, tjz, fp, errorID;
	double originT, minT, maxT, t0, t1, t2, t3;
	int fa, fb;
	double *time;

	double fxs, fys, fzs;
	int xs, ys, zs;

	if( method==10 ) time = dvector( nx * nz ); 

	fp=0;
	for(i=0; i<nEvents; i++ ) {
		fxs=sx[fp]; fys=sy[fp]; fzs=sz[fp];	
		errorID = gridGetTravelTime( method, nx, ny, nz, h, srctype, floatsrc, ncube, x0, y0, z0, fxs, fys, fzs, maxoff, slow, time );
		if( errorID > 0 ) return errorID;

		if( method==10 ) { 
			for(j=0; j<flag[i]; j++ ) {
				fxs=rx[fp+j]; fys=ry[fp+j]; fzs=rz[fp+j];
				fxs = (fxs-x0)/h;
				fys = (fys-y0)/h;
				fzs = (fzs-z0)/h;
				xs = (int)(fxs + 0.5);
				ys = (int)(fys + 0.5);
				zs = (int)(fzs + 0.5);
				calT[fp+j] = time[xs+zs*nx]; 
			}
		}

		fp += flag[i]; 
	}

	if( oMethod==0 ) { fa=0; fb=1; } // arrival time is travel time
	else if( oMethod==1 ) { fa=1; fb=1; }// origin time is calculated from Nelson
	else if( oMethod==2 ) { fa=2; fb=1; }// origin time is manually given
	else if( oMethod==3 ) { fa=3; fb=0; }// origin time is inverted
	else { }

	s=0; t=0;
	for(i=0; i<nEvents; i++ ) {
		originT = 0.0;
		for(j=0; j<flag[i]; j++) {
			originT = originT + (obsT[s]-calT[s]);
			s++;
		}
		if( fa==0 ) originT = 0.0;
		else if( fa==1 ) originT = originT / (double)(flag[i]);
		else if( fa==2 ) originT = originTime[t];
		else { }
		for(j=0; j<flag[i]; j++) {
			if(fb) 	orT[t] = originT;
			else { }
			calT[t] = orT[t] + calT[t];
			t++;
		}
	}

	//Good thing about using vs-vp to locating is the origin time is not needed
	//pTravelTime contains the provided p wave travel time
	if( spMethod==0 ) { 	}
	else {
		for(i=0; i<fp; i++ ) {
			calT[i] = calT[i]-pTravelTime[i];
		}
	}

	free_dvector( time );

	return OPERATION_SUCCESS;
}

/*

				if( fxs==(double)xs ) {
					if( zs==nz-1 ) {

					}
					else {
						t1 = time[xs+zs*nx];
						t2 = time[xs+(zs+1)*nx];
						t3 = t1*(1.0-fabs(h*sz-fzs)/h) + t2*(1.0-fabs(h*(sz+1)-fzs)/h)
					}

				}
				else if (north edge) {

				}
				else { }
				xs = (int)(fxs + 0.5);
				ys = (int)(fys + 0.5);
				zs = (int)(fzs + 0.5);

				calT[fp+j] = time[xs+zs*nx]; 
			}


			if( fxs==0 ) { tix=0; tjx=0; }
			else {
				for(j=1; j<nx; j++ ) {
					if( fxs==j*h ) { tix=j; j=nx; }
				}
			}

			else if( fxs==h*(nx-1) ) { tix=nx-1; tjx=nx-1; }
			else { tix=-1; tjx=-1; }

			if( fxz==0 ) { tiz=0; tjz=0; }
			else if( fxz==h*(nz-1) ) { tiz=nz-1; tjz=nz-1; }
			else { tiz=-1; tjz=-1; }



			for(j=0; j<nx-1; j++ ) {
				a = j*h; b=(j+1)*h; 
				if( fxs>a && fxs<b ) { tix=j; j=nx; }
			}
			for(j=0; j<nz-1; j++ ) {
				a = j*h; b=(j+1)*h; 
				if( fxz>a && fxz<b ) { tiz=j; j=nz; }
			}

			if( xs>0 && xs<nx-1 && xz>0 && xz<nz-1) { 
				t0 = time[xs+zs*nx];

			}
			else {

			}

			//for(j=0; j<flag[i]; j++ ) calT[fp+j] = time[xs+zs*nx]; 
			calT[fp+j] = t3;	
		}

int VelocityCalibrationImpl::GetResidual ( char * Mtype, const double sigma, const VCINPUT *vcI, const VCPAIR *vcPW, double *slow, 
		double *error, const int spMethod, const double *pTravelTime, const int oMethod, double *originTime )
{
	int i, j, s, nPairs, errorID;
	double dum1, eValue;

	int nx, ny, nz, srctype, floatsrc, ncube, method;
	double x0, y0, z0, h, maxoff; 

	nx = vcI->nx; ny = vcI->ny; nz = vcI->nz; 
	srctype = vcI->srctype; floatsrc = vcI->floatsrc; ncube = vcI->ncube; 
	h = vcI->h; maxoff = vcI->maxoff; 
	x0 = vcI->x0; y0 = vcI->y0; z0 = vcI->z0; 

	if( oMethod==3 ) {
		s=0;
		for(i=0; i<vcPW->nEvents; i++) {
			for(j=0; j<vcPW->flag[i]; j++) {
				originTime[s] = vcPW->originT[s];
				s++;
			}
		}
	}

	method=10;
	errorID = gridGetArrivalTime( method, nx, ny, nz, h, srctype, floatsrc, ncube, maxoff, slow,
		vcPW->nEvents, vcPW->flag, x0, y0, z0, vcPW->rE, vcPW->rN, vcPW->rD, vcPW->eE, vcPW->eN, vcPW->eD,  
		vcPW->calT, vcPW->obsT, vcPW->originT, spMethod, pTravelTime, oMethod, originTime );
	if( errorID>0 ) return errorID;

	nPairs = 0;
	for(i=0; i<vcPW->nEvents; i++) { nPairs += vcPW->flag[i]; }

	eValue = 0.0;
	if ( strcmp(Mtype, "KaiSquare") == 0) {
		for(i=0; i<nPairs; i++) {
			dum1 = (vcPW->calT[i]-vcPW->obsT[i]) / (sigma);
			//dum1 = (vcPW->calT[i]-vcPW->obsT[i]) / (vcPW->obsT[i]*0.03);
			eValue += dum1*dum1;
		}
	}
	else if ( strcmp(Mtype, "RMS") == 0) {
		for(i=0; i<nPairs; i++) {
			dum1 = (vcPW->calT[i]-vcPW->obsT[i]);
			eValue += dum1*dum1;
		}
		eValue = sqrt(eValue/(double)(nPairs));
	}
	else if ( strcmp(Mtype, "L1") == 0) {
		for(i=0; i<nPairs; i++) eValue += fabs(vcPW->calT[i]-vcPW->obsT[i]);
	}
	else if ( strcmp(Mtype, "L2") == 0) {
		for(i=0; i<nPairs; i++) {
			dum1 = (vcPW->calT[i]-vcPW->obsT[i]);
			eValue += dum1*dum1;
		}
		eValue = sqrt(eValue);
	}
	else if ( strcmp(Mtype, "None") == 0) {

	}
	else {  }

	*error=eValue;
	return OPERATION_SUCCESS;
}
*/
/* GetTimes defines for array access */
/* ( v(ix,iz) = v[ ix + iz*nx ] in 1D array ) */
/* ( s(ix,iz) = h * slowness averaged between ix, ix+1, iz, and iz+1) */

//#define dt(x,z)	timing[(x) + (z)*nx]
//#define ds(x,z)	 slow[s0 + zs*nx]

/* PROGRAM TO CALCULATE TRAVEL TIMES IN 2D MEDIA        */
/*      AUTHOR: John E. Vidale */
/*   UCSC and Caltech, all rights reserved      */
/*     Revamped by RWC, 2-14-88  */
/*     Edges ordered in relative chronological order by JEV, 3-11-88 */
/*     Source not restricted to fall on a node, JEV, 10-28-88 */
/* OVERHAULED IN 11-90 TO ALLOW RECURSIVE CORRECTION (SEE VIDALE, 1991) */

/*int   floatsrc=0;      IF 0, SOURCE IS ON A GRID POINT */
/*int   nx=20;           X-DIMENSION OF MESH */
/*int   nz=20;          Z-DIMENSION OF MESH */
/*double h=1;             SPATIAL MESH INTERVAL (units consistant with vel)*/
/*int   s0=18;           SHOT POSITION IN X (if in grid points)*/
/*int           zs=1;    SHOT POSITION IN Z (if in grid points)*/
/*double         fs0;            SHOT POSITION IN X (if in real units)*/
/*double fzs;     SHOT POSITION IN Z (if in real units)*/
/*int sbox=1;	                   RADIUS OF INNER STRAIGHT RAY */
/*int   tfint;*/
/*char  velfile[40];    INPUT VELOCITY FILE */
/*double v1=1.0, v2=3.0;  VELOCITIES IN THE TOP AND BOTTOM LAYERS */
/*char  timefile[40];   OUTPUT TRAVELTIME FILE */
/* ARRAY ORDERING ( x(ix,iz) = x[ ix + iz*nx ] IN 1D ARRAY ) */
/* VELOCITY AND TRAVELTIME IS IN SAME ORDER */


int VelocityCalibrationImpl::vidale2DTime( const int nx, const int nz, const double h, const int srctype, const int floatsrc, 
										  const int sbox, double x0, double z0, double fs0, double fzs, double *slow, double *timing )
{
	int i, j, k, nxz;
	int s0, zs;		/* source location grid points */

	int mzup, mzdn, zup, zdn, xlt, xrt, nbox, ix, iz;
	double dist;
	double rtt, junk1, junk2;

	//FILE *tfint;

	if(floatsrc==0){
		s0 = (int)fs0;
		zs = (int)fzs;
	}
	else{
		fs0 = (fs0-x0)/h;
		fzs = (fzs-z0)/h;
		s0 = (int)(fs0 + 0.5);
		zs = (int)(fzs + 0.5);
	}
	if(s0 > nx-1){
		//fprintf(stderr,"Source off grid to the right, try again.\n");
		//exit(-1);
	}
	if(zs > nz-1){
		//fprintf(stderr,"Source off bottom of grid, try again.\n");
		//exit(-1);
	}


	/* INITIALIZE ALL POINTS TO -1, (NEGATIVE WILL MEAN POINT IS SO FAR */
	/* UNTIMED, SO AVOID TINKERING TO SOLVE FOR NEGATIVE TIMES */

	nxz = nx*nz;
	for(i=0; i<nxz; i++ ) timing[i]=-1.0;

	/* CONVERT VELOCITIES TO SLOWNESSES */
	//for(k=0; k<nxz; k++) slow[k] = h/slow[k];

	/* TIME POINTS NEAR SOURCE WITH STRAIGHT RAYS */
	for(ix = -sbox ; ix <= sbox ; ix++) {
		for(iz= -sbox; iz <= sbox ; iz++){
			if((s0+ix>=0) && (s0+ix<nx) && (zs+iz>=0) && (zs+iz<nz)) {
				if(ix != 0 || iz != 0){
					dist  = (s0-fs0+ix) * (s0-fs0+ix); 
					dist += (zs-fzs+iz) * (zs-fzs+iz); 
					dist = sqrt( dist );
					timing[s0+ix + (zs+iz)*nx] = 0.5*(slow[s0 + zs*nx] + slow[s0+ix + (zs+iz)*nx]) * dist;
				}
			}
		}
	}				
	timing[s0+zs*nx] = 0;

	/* SET POINTERS TO NEXT CONCENTRIC BOX, AND COUNT BOXES */
	zup = (zs-1-sbox >=   0  ? zs-1-sbox : -1 );
	zdn = (zs+1+sbox <= nz-1 ? zs+1+sbox : nz );
	xlt = (s0-1-sbox >=    0 ? s0-1-sbox : -1 );
	xrt = (s0+1+sbox <= nx-1 ? s0+1+sbox : nx );
	if(zup == -1) mzup = 0;
	else mzup = zup;
	if(zdn == nz) mzdn = nz-1;
	else mzdn = zdn;
	nbox= 0;
	if(s0-1-sbox > nbox) nbox= s0-2;
	if(zs-1-sbox > nbox) nbox= zs-2;
	if(nx-s0-1-sbox > nbox) nbox= nx-s0-2;
	if(nz-zs-1-sbox > nbox) nbox= nz-zs-2;

	/* LOOP OVER BOXES */
	for(k=0; k<=nbox; k++)
	{
		/*if( k%100 == 0)
		fprintf(stderr,"box=%d out of %d xlt=%d xrt=%d zup=%d zdn=%d\n",
		k, nbox, xlt, xrt, zup, zdn);*/

		/* TOP EDGE, Z=zup */
		if(zup >= 0)
			timerow(xrt-xlt-1,nx,1,&timing[xlt+1+nx*mzup],&slow[xlt+1+nx*mzup]);
		/* BOTTOM EDGE, z=zdn */
		if(zdn < nz)
			timerow(xrt-xlt-1,-nx,1,&timing[xlt+1+nx*mzdn],&slow[xlt+1+nx*mzdn]);
		/* LEFT EDGE, x=xlt */
		if(xlt >= 0)
			timerow(mzdn-mzup+1,1,nx,&timing[xlt+nx*mzup],&slow[xlt+nx*mzup]);
		/* RIGHT EDGE, x=xrt */
		if(xrt < nx)
			timerow(mzdn-mzup+1,-1,nx,&timing[xrt+nx*mzup],&slow[xrt+nx*mzup]);

		/* EXPAND THE BOX */
		if(xlt >= 0) xlt--;
		if(zup >= 0) zup--;
		if(xrt < nx) xrt++;
		if(zdn < nz) zdn++;

		/* KEEP UPPER AND LOWER LIMITS ON COLUMN CALCULATION CORRECT */
		if(zup == -1) mzup = 0;
		else mzup = zup;
		if(zdn == nz) mzdn = nz-1;
		else mzdn = zdn;
	}
	/* PRINT OUT ANSWERS AND ERRORS */
	/*write(tfint,timing,sizeof(double)*nx*nz);
	close(tfint);
	*/
/*
	tfint=fopen("time2D.txt", "w");
	for(i=0;i<nxz;i++) fprintf(tfint, "%lf    ", timing[i]);

	int rz[12];
	for(i=0;i<12;i++) rz[i]=3+i;
	for(i=0;i<12;i++) {
		fprintf(tfint, "\n%lf    ", dt(18, rz[i]));
		dist = sqrt((13.0*13.0+(10-rz[i])*(10-rz[i])));
		fprintf(tfint, "%lf    ", dist*ds(18,rz[i]));
	}
	fclose( tfint );
*/
	return OPERATION_SUCCESS;
}

double VelocityCalibrationImpl::edge(double ta, double tb, double tc, double hs)
{
	double td, diff, arg;
	diff= 0.5*(tc- tb);
	arg= hs*hs - diff*diff;
	/* PATHOLOGICAL CASE, ASSUME SOMETHING SIMPLE */
	if(arg < 0.0)
	{
		td = ta + hs;
		return(td);
	}
	td= ta + sqrt( (double)(arg));
	return(td);
}

/* EXTRAPOLATE TRAVELTIMES TO NEXT ROW OR COLUMN */
void VelocityCalibrationImpl::timerow(int len, int norm, int tang, double *t, double *s)
//double *t, *s; /* ARRAYS WITH SLOWNESS AND TRAVELTIME */
//int len;	/* NUMBER OF POINTS IN ROW */
//int norm;	/* INCREMENT IN DIRECTION NORMAL TO ROW (POSITIVE INWARD) */
//int tang;	/* INCREMENT IN TANGENTIAL DIRECTION */
{
	int i, j, fredo, bredo, update, updatd, go_on;
	double guess, diff, avs, argu, *ss, *tt, *ch;
	//double fabs();
	fredo = bredo = 0;
	/* CHECK WHETHER UPPER OR LEFT END HAS NORMAL REFRACTION */
	if(t[norm] <= t[norm+tang])
		t[0] = t[norm] + 0.5*(s[0] + s[norm]);
	/* SWEEP FORWARD (TANGENTIALLY) ACROSS INTERIOR OF ROW */
	for(i = 0; i< len-1; i++){
		t += tang;
		s += tang;
		if((t[norm] >= t[norm-tang]) || (i == len-2)){
			/* TIME INCREASING WITH SWEEP */
			/* NO BACK CORRECTION NECESSARY */
			if(fredo==0){
				if(t[-tang] < 0 || (t[norm] < t[norm-tang])){
					/* NORMAL DIFFRACTION MUST BE ASSUMED */
					t[0] = t[norm] + 0.5*(s[0] + s[norm]);
				} else{
					diff = t[norm] - t[-tang];
					avs = 0.25*(s[0]+s[-tang]+s[norm]+s[norm-tang]);
					argu = 2.0*avs*avs - diff*diff;
					if(argu >= 0){
						/* BASIC CORNER CALCULATION */
						t[0] = t[norm-tang] + sqrt(argu);
					} else{
						/* TANG. DIFFRACTION MUST BE ASSUMED */
						t[0] = t[-tang] + 0.5*(s[0] + s[-tang]);
						if(t[0] < t[norm]) fredo = 2;
					}
				}
			}
			/* BACK CORRECTION NECESSARY */
			if(fredo != 0){
				/* SEE IF NORMAL CALCULATION WILL WORK */
				/* (THEN PERHAPS fredo WILL RETURN TO 0) */
				if(t[-tang] < 0 || (t[norm] < t[norm-tang])){
					/* NORMAL DIFFRACTION MUST BE ASSUMED */
					t[0] = t[norm] + 0.5*(s[0] + s[norm]);
				} else{
					diff = t[norm] - t[-tang];
					avs = 0.25*(s[0]+s[-tang]+s[norm]+s[norm-tang]);
					argu = 2.0*avs*avs - diff*diff;
					if(argu >= 0)
						/* BASIC CORNER CALCULATION */
						t[0] = t[norm-tang] + sqrt(argu);
					else
						/* TANG. DIFFRACTION MUST BE ASSUMED */
						t[0] = t[-tang] + 0.5*(s[0] + s[-tang]);
				}
				/* STORE OLD TIMES TO SEE WHICH CHANGE MUCH */
				/* IN RECURSIVE CORRECTION */
				if(fredo <= 0){
					//fprintf(stderr,"timerow: fredo=%d\n", fredo);
					//return -1;
				}
				ch = (double *) malloc (sizeof(double)*fredo);
				for(j=0, tt=t; j<fredo; j++, tt += norm)
					ch[j] = tt[0];
				/* DO BACK CORRECTION */
				rtimerow(fredo,-tang,norm,&t[0],&s[0]);
				tt = t + fredo*norm;
				ss = s + fredo*norm;
				/* SEE WHETHER BACK CORRECTION IS NECESSARY FURTHER IN */
				update = 1;
				updatd = 0;
				while((tt[-tang] >= tt[-norm-tang]) && update == 1){
					diff = tt[-norm] - t[-tang];
					avs = 0.25*(ss[0]+ss[-tang]+ss[-norm]+ss[-norm-tang]);
					argu = 2.0*avs*avs - diff*diff;
					if(argu >= 0)
						guess = tt[-norm-tang] + sqrt(argu);
					else
						guess = tt[-norm] + 0.5*(ss[0]+ss[-norm]);
					update = 0;
					if(guess <= tt[0]){
						tt[0] = guess;
						fredo++;
						tt += norm;
						ss += norm;
						update = 1;
						updatd = 1;
					}
				}
				/*
				fprintf(logFile,"Forward sweep recurs. corr. of %d points, tang=%d, norm=%d, len=%d, i=%d\n",
				fredo, tang, norm, len, i);
				*/
				/* SEE WHETHER BACK CORRECTION LENGTH CAN BE SHORTENED */
				if(updatd == 0){
					/* CHECK FROM INNERMOST POINT BACK OUT */
					tt=&t[fredo * norm];
					ss=&s[fredo * norm];
					go_on = 1;
					for(j=(fredo-1); j >= 0 && go_on;j--){
						tt -= norm;
						ss -= norm;
						/* CHANGE IS IGNORABLE IF < 10% OF SPACING*SLOWNESS */
						if(fabs(ch[j] - tt[0]) > 0.10*ss[0]) go_on = 0;
						else{
							/*
							fprintf(logFile,"Dropping fredo from %d to %d\n",fredo,j);
							*/
							fredo = j;
						}
					}
					/* fredo == 1 DOESN'T MAKE MUCH SENSE */
					if(fredo==1) fredo=2;
				}
				free(ch);
			}
		}
		else if(t[norm] <= t[norm+tang])
			/* EDGE CALCULATION */
			t[0] = edge(t[norm],t[norm-tang],t[norm+tang],
			0.5*(s[0]+s[norm]));
	}
	/* SWEEP BACK ACROSS INTERIOR OF ROW */
	for(i=0;i<len-1;i++){
		t -= tang;
		s -= tang;
		if(t[norm] >= t[norm+tang]){
			/* TIME INCREASING WITH SWEEP */
			if(bredo == 0){
				/* NO BACK CORRECTION NECESSARY */
				diff = t[norm] - t[tang];
				avs = 0.25*(s[0]+s[tang]+s[norm]+s[norm+tang]);
				argu = 2.0*avs*avs - diff*diff;
				/* ALLOW POSSIBILITY THAT POINT IS TIMED FROM BOTH SIDES */
				if(argu >= 0)
					/* BASIC CORNER CALCULATION */
					guess = t[norm+tang] + sqrt(argu);
				else
					/* TANGENTIAL DIFFRACTION MUST BE ASSUMED */
					guess = t[tang] + 0.5*(s[0] + s[tang]);
				if(guess < t[0] || t[0] < 0)
					t[0] = guess;
				if(t[0] < t[norm]) bredo = 2;
			}
			/* BACK CORRECTION NECESSARY */
			if(bredo != 0){
				/* SEE IF NORMAL CALCULATION WILL WORK */
				/* (THEN PERHAPS bredo WILL RETURN TO 0) */
				diff = t[norm] - t[tang];
				avs = 0.25*(s[0]+s[tang]+s[norm]+s[norm+tang]);
				argu = 2.0*avs*avs - diff*diff;
				/* ALLOW POSSIBILITY THAT POINT IS TIMED FROM BOTH SIDES */
				if(argu >= 0)
					/* BASIC CORNER CALCULATION */
					guess = t[norm+tang] + sqrt(argu);
				else
					/* TANGENTIAL DIFFRACTION MUST BE ASSUMED */
					guess = t[tang] + 0.5*(s[0] + s[tang]);
				if(guess < t[0] || t[0] < 0)
					t[0] = guess;
				/* STORE OLD TIMES TO SEE WHICH CHANGE MUCH */
				/* IN RECURSIVE CORRECTION */
				if(bredo <= 0) {
					//fprintf(stderr,"timerow: bredo=%d\n", bredo);
					//return -1;
				}
				ch = (double *) malloc (sizeof(double)*bredo);
				for(j=0, tt=t; j<bredo; j++, tt += norm)
					ch[j] = tt[0];
				/* DO BACK CORRECTION */
				rtimerow(bredo,tang,norm,&t[0],&s[0]);
				tt = t + bredo*norm;
				ss = s + bredo*norm;
				/* SEE WHETHER BACK CORRECTION IS NECESSARY FURTHER IN */
				update = 1;
				updatd = 0;
				while((tt[tang] >= tt[-norm+tang]) && update == 1){
					diff = tt[-norm] - t[tang];
					avs = 0.25*(ss[0]+ss[tang]+ss[-norm]+ss[-norm+tang]);
					argu = 2.0*avs*avs - diff*diff;
					if(argu >= 0)
						guess = tt[-norm+tang] + sqrt(argu);
					else
						guess = tt[-norm] + 0.5*(ss[0]+ss[-norm]);
					update = 0;
					if(guess <= tt[0]){
						tt[0] = guess;
						bredo++;
						tt += norm;
						ss += norm;
						update = 1;
						updatd = 1;
					}
				}
				/*
				fprintf(logFile,"Reverse sweep recurs. corr. of %d points, tang=%d, norm=%d, len=%d, i=%d\n",
				bredo, tang, norm, len, i);
				*/
				/* SEE WHETHER BACK CORRECTION LENGTH CAN BE SHORTENED */
				if(updatd == 0){
					/* CHECK FROM INNERMOST POINT BACK OUT */
					tt=&t[bredo * norm];
					ss=&s[bredo * norm];
					go_on = 1;
					for(j=(bredo-1); j >= 0 && go_on;j--){
						tt -= norm;
						ss -= norm;
						/* CHANGE IS IGNORABLE IF < 10% OF SPACING*SLOWNESS */
						if(fabs(ch[j] - tt[0]) > 0.10*ss[0]) go_on = 0;
						else{
							/*
							fprintf(logFile,"Dropping bredo from %d to %d\n",bredo,j);
							*/
							bredo = j;
						}
					}
					/* bredo == 1 DOESN'T MAKE MUCH SENSE */
					if(bredo==1) bredo=2;
				}
				free(ch);
			}		
		}
	}
}

/* RECURSIVELY CORRECT TRAVELTIMES BACK INTO INTERIOR */
void VelocityCalibrationImpl::rtimerow(int len, int norm, int tang, double *t, double *s)
//double *t, *s; /* ARRAYS WITH SLOWNESS AND TRAVELTIME */
//int len;	/* NUMBER OF POINTS IN ROW */
//int norm;	/* INCREMENT IN DIRECTION NORMAL TO ROW (POSITIVE INWARD) */
//int tang;	/* INCREMENT IN TANGENTIAL DIRECTION */
{
	int i;
	double guess, diff, avs, argu;
	/* START WITH NORMAL REFRACTION */
	/* BUT ONLY USE TIMES THAT ARE EARLIER THAN EXISTING TIMES */
	guess = t[norm] + 0.5*(s[0] + s[norm]);
	if((guess < t[0]) || (t[0] < 0)) t[0] = guess;
	/* SWEEP FORWARD ACROSS INTERIOR OF ROW */
	for(i=0;i< len-1;i++){
		t += tang;
		s += tang;
		if(t[norm] >= t[norm-tang]){
			/* TIME INCREASING WITH SWEEP */
			/* BASIC CORNER CALCULATION */
			diff = t[norm] - t[-tang];
			avs = 0.25*(s[0]+s[-tang]+s[norm]+s[norm-tang]);
			argu = 2.0*avs*avs - diff*diff;
			if(argu > 0)
				guess = t[norm-tang] + sqrt(argu);
			else
				guess = t[-tang] + 0.5*(s[0] + s[-tang]);
			if(guess < t[0] || t[0] < 0)
				t[0] = guess;
		}
		else if(t[norm] < t[norm+tang]){
			/* EDGE CALCULATION */
			guess = edge(t[norm],t[norm-tang],t[norm+tang],
				0.5*(s[0]+s[norm]));
			if(guess < t[0] || t[0] < 0)
				t[0] = guess;
		}
	}
	/* SWEEP BACK ACROSS INTERIOR OF ROW */
	for(i=0;i<len-1;i++){
		t -= tang;
		s -= tang;
		if(t[norm] >= t[norm+tang]){
			/* TIME INCREASING WITH SWEEP */
			/* BASIC CORNER CALCULATION */
			diff = t[norm] - t[tang];
			avs = 0.25*(s[0]+s[tang]+s[norm]+s[norm+tang]);
			argu = 2.0*avs*avs - diff*diff;
			if(argu > 0)
				guess = t[norm+tang] + sqrt(argu);
			else
				guess = t[norm] + 0.5*(s[0] + s[norm]);
			if(guess < t[0] || t[0] < 0)
				t[0] = guess;
		}
	}
}
}