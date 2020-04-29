package com.geohammer.rt;

public class Punch1 {

	/*
	c cover.synpick: create a fake travel-time pick file from
	c	results of Vidale "punch.c" travel-time modeling.
	c Hole's "cover" has a 3-D trilinear spatial interpolation for calculating
	c	the residual travel time for a location that is inside a
	c	cell defined by four grid points.  
	c This program is a modification of Hole's cover in order to take advantage
	c	of the 3-D spatial interpolation
	c
	c               o--------------o
	c              /:             /|
	c             / :        *   / |	* at (fsx,fsy,fsz)
	c            o--------------o  | 
	c            |  :           |  |
	c            |  o  -  -  -  |- o
	c            | /            | /
	c            |/             |/
	c            o--------------o
	c
	c Internal gridding:
	c   Real world (x,y,z) mapped into internal grid:
	c	(x,y,z) = (x_real,y_real,z_real) - (x0,y0,z0)
	c       (ix,iy,iz) = (x,y,z) / h
	c  	nodes (cell corners) "o" at integral units of h
	c
	c -------------------------------------------------------------
	c -------------------------------------------------------------
	c edited HOLE cover comments:
	c     Given shotpoint location within a sampled 3d traveltime grid,
	c        find the ray from the shotpoint to the receiver(source),
	c        and find how the traveltime residual affects each cell.
	c        Input:   3d travel time field:   nx,ny,nz,t(i,j,k),h,x0,y0,z0,
	c                                         xs,ys,zs
	c                 shotpoints:   line,spn,xsh,ysh,zsh
	c                 traveltime picks:   line,spn,tpick
	c                 optional:  ray coverage from previous receivers(sources)
	c                               to be added to this rec.:   nrays(i,j,k)
	c                            slowness purturbation from previous:   du(i,j,k)
	c
	c -------------------------------------------------------------
	*/

	int Get3DCoverage( const int method, FILE *fid_raypath, const int nx, const int ny, const int nz, const double h, const int srctype, const int NCUBE, float *dus, int *nrays, 
					  const double x0, const double y0, const double z0, float *slow, VCPAIR *vcPW )
	{
		int i, j, k, s, t, errorID;
		int nwall, nxyz, fa, fb, dunxyz;
		float *time, *wall;
		double fxs, fys, fzs, originT, a;
		struct sorted *sort;

		int itp[2]; // dum array
		int row[2], col[2]; //dum arrays
		float G[2], D[2]; //dum arrays

		nxyz = nx*ny*nz;
		dunxyz = (nx-1)*(ny-1)*(nz-1);

		if(nx <= ny && nx <= nz)  {
			sort = (struct sorted *) malloc (sizeof(struct sorted)*ny*nz);
			nwall = ny*nz;
		}
		else if(ny <= nx && ny <= nz)  {
			sort = (struct sorted *) malloc (sizeof(struct sorted)*nx*nz);
			nwall = nx*nz;
		}
		else  {
			sort = (struct sorted *) malloc (sizeof(struct sorted)*nx*ny);
			nwall = nx*ny;
		}
		wall = fvector( nwall );
		time = fvector( nxyz );
		for(i=0; i<dunxyz; i++ ) { dus[i]=0.0; nrays[i]=0; }

		k = 0; itp[0] = 0; itp[1] = 0;
		for(i=0; i<vcPW->nEvents; i++ ) {
			fxs = vcPW->eE[k];
			fys = vcPW->eN[k];
			fzs = vcPW->eD[k];

			errorID = Get3DTT(nx, ny, nz, h, srctype, NCUBE, x0, y0, z0, fxs, fys, fzs, nwall, sort, slow, wall, time );
			if( errorID > 0 ) return errorID;
			for(j=0;j<nxyz;j++) slow[j] = slow[j]/h;

			//vcPW->flag[i] = 1;
			//printf(".");
			j = -(i+1);
			if(method==100) fwrite(&j, sizeof(int), 1, fid_raypath);
			errorID = cover( method, fid_raypath, nx, ny, nz, h, NCUBE, x0, y0, z0, fxs, fys, fzs, vcPW->flag[i], &vcPW->rE[k], &vcPW->rN[k], &vcPW->rD[k], &vcPW->obsT[k], 
				time, slow, dus, nrays, itp, G, row, col, D );
			if( errorID > 0 ) return errorID;
			//i = vcPW->nEvents;
			//exit( 1 );

			k += vcPW->flag[i];
		}

		free_fvector( time );
		free_fvector( wall );
		free( sort );

		return 0;
	}

	//original from John Hole and later adopted from Haijiang Zhang 2008-1-20
	int cover( const int method, FILE *fid, const int nx, const int ny, const int nz, const double h, const int NCUBE,  
			  double x0, double y0, double z0, double xs, double ys, double zs, 
			  int nPairs, double *rx, double *ry, double *rz, double * tpick, 
			  float *t, float *slow, float *dus, int *nrays, int *itp, float *G, int *row, int *col, float *D )
	{
		int i, j, k, ia, ib, ic, is, js, ks, iscell, jscell, kscell;
		double x, y, z, xi, yj, zk, x1, y1, z1, xx, yy, zz, fx, fy, fz;
		double dt, dist, d, gradt[3], dd[3], maxGrad, a, b, c, t_tol;
		int errorID = 300;
		int dumi;
		double dum1, dum;

		int inp, bp, bpMax, md, goodRay, MAXSEG;
		int nxyz, nxy, dunxyz, dunxy, dunx, nseg, mseg, iseg, isegm1, isegm2;
		double *rayXYZ, *tcal;
		int *rayijk;

		nxyz	= nx*ny*nz;
		nxy		= nx*ny;
		dunx	= nx-1;
		dunxy	= dunx*(ny-1);
		dunxyz	= dunxy*(nz-1);
		x1		= x0 + h*(nx-1);
		y1		= y0 + h*(ny-1);
		z1		= z0 + h*(nz-1);

		MAXSEG	= 6*(nx+ny+nz);

		rayXYZ = dvector( MAXSEG+3 );
		rayijk = ivector( MAXSEG+3 );

		maxGrad	= 0.0; //1.0e-20 / h;
		t_tol = 1.0e-6;

		//source check should be done already
		//FIND the model node of SOURCE POINT
		is = (int)((xs-x0)/h+0.5);
		js = (int)((ys-y0)/h+0.5);
		ks = (int)((zs-z0)/h+0.5);

		//FIND THE CELL CONTAINING THE SOURCE POINT
		iscell = (int)((xs-x0)/h);
		jscell = (int)((ys-y0)/h);
		kscell = (int)((zs-z0)/h);
		goodRay = 0;

		tcal = dvector( nPairs );
		Trilinear3DTT(2, nx, ny, nz, h, x0, y0, z0, nPairs, rx, ry, rz, t, tcal );

		for ( inp=0; inp<nPairs; inp++ ) {
			iseg	= 0;
			isegm1	= 0;
			isegm2	= 0;
			a		= 0.0;
			b		= 0.0;
			c		= 0.0;

			//receiver should be checked already
			x = rx[inp];
			y = ry[inp];
			z = rz[inp];

			//Locate receiver in model by finding cell
			i = (int)((x-x0)/h);
			j = (int)((y-y0)/h);
			k = (int)((z-z0)/h);
			
			nseg = 0; 
			mseg = 3*nseg;
			rayXYZ[mseg] = x; rayXYZ[mseg+1] = y; rayXYZ[mseg+2] = z; 
			rayijk[mseg] = i; rayijk[mseg+1] = j; rayijk[mseg+2] = k;

			//find location of the cell left-down-back corner
			xi = h*i + x0;
			yj = h*j + y0;
			zk = h*k + z0;

			dt = tpick[inp] - tcal[inp];
			if( abs(dt)>60 && abs(dt)<=t_tol ) { inp++; bp=bpMax+2; }
			//fprintf(fidrp, "%d, %lf, %lf \n", inp, tpick[inp], dt);

			//start BACK-PROPAGATE RAY 
			bp=1; bpMax=1000;
			while( bp<bpMax ) {
				xi = h*i + x0;
				yj = h*j + y0;
				zk = h*k + z0;	
				dist = (xs-x)*(xs-x)+(ys-y)*(ys-y)+(zs-z)*(zs-z);
				//if( inp==94) printf("\nk=%d %d xi=%lf yj=%lf zk=%lf", k, bp, xi, yj, zk);

				//ray is approximated by a straight line if in the source cell
				if( (i==iscell && j==jscell && k==kscell) || ( dist<h*h*1.0e-6 ) ) {
					nseg++; mseg=3*nseg;
					if( mseg<0 )			{ bp = bpMax+250; break; }
					else if( mseg>=MAXSEG )	{ bp = bpMax+260; break; }
					else { 
						rayXYZ[mseg] = xs; rayXYZ[mseg+1] = ys; rayXYZ[mseg+2] = zs; 
						rayijk[mseg] = i; rayijk[mseg+1] = j; rayijk[mseg+2] = k;
					}

					GramSum( method, fid, nx, ny, nz, nseg+1, dt, rayXYZ, rayijk, slow, dus, nrays, itp, G, row, col, D );

					goodRay++;
					bp = bpMax+1; 
				}
				else {
					//FIND THE RAY GRAD(T)
					rayGrad(2, nx, ny, nz, h, x0, y0, z0,  x, y, z, t, gradt );

					//IF RAY IN SOURCE CUBE, USE STRAIGHT RAY FROM SOURCE
					if  (i>=(is-NCUBE)&&i<(is+NCUBE) && j>=(js-NCUBE)&&j<(js+NCUBE) && k>=(ks-NCUBE)&&k<(ks+NCUBE)){
						gradt[0] = (x - xs);
						gradt[1] = (y - ys);
						gradt[2] = (z - zs);
					}
					if( 0 ) {
					//if(gradt[0]==maxGrad && gradt[1]==maxGrad && gradt[2]==maxGrad) { 
						line3D( h, x, y, z, xs, ys, zs, &fx, &fy, &fz );
						i = (int)((fx-x0)/h);
						j = (int)((fy-y0)/h);
						k = (int)((fz-z0)/h);

						x = h*i + x0;
						y = h*j + y0;
						z = h*k + z0;

						if( x>x1 || x<x0 )	{ bp = bpMax+210; break; }
						if( y>y1 || y<y0 )	{ bp = bpMax+220; break; }
						if( z>z1 || z<z0 )	{ bp = bpMax+230; break; }
					}
					else {
						//calculate intersection point of ray with cell boundary
						if( gradt[0]>0.0 ) {
							dd[0] = (xi-x)/gradt[0];
							if( dd[0]>-(1.0e-10) ) {
								if((iseg==1) || (((iseg+isegm1+isegm2)==1) && (nseg>=3))) 
								{ gradt[0] = 0.0; dd[0] = -1.0e20; }
							}
						}
						else if( gradt[0]<0.0 ) {
							dd[0] = (xi+h-x)/gradt[0];
							if( dd[0]>-(1.0e-10) ) {
								if((iseg==-1) || (((iseg+isegm1+isegm2)==-1) && (nseg>=3))) 
								{ gradt[0] = 0.0; dd[0] = -1.0e20; }
							}
						}
						else dd[0] = -1.0e20;

						if( gradt[1]>0.0 ) {
							dd[1] = (yj-y)/gradt[1];
							if( dd[1]>-(1.0e-10) ) {
								if((iseg==20) || (((iseg+isegm1+isegm2)==20) && (nseg>=3))) 
								{ gradt[1] = 0.0; dd[1] = -1.0e20; }
							}
						}
						else if( gradt[1]<0.0 ) {
							dd[1] = (yj+h-y)/gradt[1];
							if( dd[1]>-(1.0e-10) ) {
								if((iseg==-20) || (((iseg+isegm1+isegm2)==-20) && (nseg>=3)))  
								{ gradt[1] = 0.0; dd[1] = -1.0e20; }
							}
						}
						else dd[1] = -1.0e20;

						if( gradt[2]>0.0 ) {
							dd[2] = (zk-z)/gradt[2];
							if( dd[2]>-(1.0e-10) ) {
								if((iseg==300) || (((iseg+isegm1+isegm2)==300) && (nseg>=3)))  
								{ gradt[2] = 0.0; dd[2] = -1.0e20; }
							}
						}
						else if( gradt[2]<0.0 ) {
							dd[2] = (zk+h-z)/gradt[2];
							if( dd[2]>-(1.0e-10) ) {
								if((iseg==-300) || (((iseg+isegm1+isegm2)==-300) && (nseg>=3))) 
								{ gradt[2] = 0.0; dd[2] = -1.0e20; 	}
							}
						}
						else dd[2] = -1.0e20;

						//DETERMINE WHICH IS DESIRED (SMALLEST ABSOLUTE VALUE; ALL SHOULD 
						//BE NEGATIVE, BUT NEAR-ZERO MIGHT BE POSITIVE)
						if( fabs(dd[0])<=fabs(dd[1]) && fabs(dd[0])<=fabs(dd[2]) )	{ md = 1; d = dd[0]; }
						else if( fabs(dd[1])<=fabs(dd[2]) )						{ md = 2; d = dd[1]; }
						else													{ md = 3; d = dd[2]; }

						//length = length - d*sqrt(gradt[0]*gradt[0]+gradt[1]*gradt[1]+gradt[2]*gradt[2]);

						x = x + d*gradt[0];
						y = y + d*gradt[1];
						z = z + d*gradt[2];
	/*
						//randomly choose direction in the case of homogeneous time field
						if(gradt[0]==0.0 && gradt[1]==0.0 && gradt[2]==0.0) {
							dum = frandom ( &dumi );
							if( dum<0.33 )		md=1;
							else if( dum<0.66 )	md=2;
							else				md=3;

							dum = frandom ( &dumi );
							if( dum<0.5 )	dum1 = 1.0;
							else			dum1 = -1.0;
							gradt[md-1] = dum1*1.0e-10;

							if( md==1 )			x = x + dum1*h;
							else if( md==2 )	y = y + dum1*h;
							else				z = z + dum1*h;
						}
	*/
						if( x>x1 || x<x0 )	{ bp = bpMax+210; break; }
						if( y>y1 || y<y0 )	{ bp = bpMax+220; break; }
						if( z>z1 || z<z0 )	{ bp = bpMax+230; break; }

						isegm2 = isegm1; isegm1 = iseg;

						//check to see if ray has reached the edge of the model
						if ( md==1 ) {
							if( gradt[0]>=0.0 ) {
								i = i-1;
								if( i<0 )  { bp = bpMax+30; break; }
								iseg = -1;
							}
							else {
								i = i+1;
								if( i>nx-2 )  { bp = bpMax+40; break; }
								iseg = 1;
							}
						}
						else if ( md==2 ) {
							if( gradt[1]>=0.0 ) {
								j = j-1;
								if( j<0 )  { bp = bpMax+50; break; }
								iseg = -20;
							}
							else {
								j = j+1;
								if( j>ny-2 )  { bp = bpMax+60; break; }
								iseg = 20;
							}
						}
						else {
							if( gradt[2]>=0.0 ) {
								k = k-1;
								if( k<0 )  { bp = bpMax+70; break; }
								iseg = -300;
							}
							else {
								k = k+1;
								if( k>nz-2 )  { bp = bpMax+80; break; }
								iseg = 300;
							}	
						} 
					}

					nseg++; mseg=3*nseg;
					if( mseg<0 )			{ bp = bpMax+250; break; }
					else if( mseg>=MAXSEG )	{ bp = bpMax+260; break; }
					else { 
						rayXYZ[mseg] = x; rayXYZ[mseg+1] = y; rayXYZ[mseg+2] = z; 
						rayijk[mseg] = i; rayijk[mseg+1] = j; rayijk[mseg+2] = k;
					}

				} //bigger if-else sentence

			} //iteration of blocks of one raypath 

			//printf("%d ", bp);
		}
		//printf("with good ray %d %d / %d ", goodRay, bp, nPairs);
		//getchar();

		free_dvector( rayXYZ );
		free_ivector( rayijk );

		free_dvector( tcal );

		return 0;

	}

	void GramSum( const int method, FILE *fid, const int nx, const int ny, const int nz, const int npt, double dt, 
				 double *rayXYZ, int *rayijk, float *slow, float *dus, int *nrays, int *itp, float *G, int *row, int *col, float *D )
	{
	#define nrays0(i,j,k)			nrays[dunxy*(k) + dunx*(j) + (i)]
	#define dus0(i,j,k)				dus[dunxy*(k) + dunx*(j) + (i)]
	#define s0(i,j,k)				slow[nxy*(k) + nx*(j) + (i)]

		int ia, ib, ic, i, mpt, nxy, dunx, dunxy, itpt;
		double prevX, prevY, prevZ, x, y, z;
		double dist, length, minL, maxL, aLength, eLength, dua, weight;
		double sum, a, b, c; 
		double *rayL;

		rayL = dvector( npt );
		nxy = nx*ny;
		dunx = (nx-1);
		dunxy = dunx*(ny-1);
		itpt = itp[1];

		length = 0.0;
		i=0; mpt=3*i;
		prevX = rayXYZ[mpt];
		prevY = rayXYZ[mpt+1];
		prevZ = rayXYZ[mpt+2];
		for(i=1; i<npt; i++) {
			mpt=3*i;
			x = rayXYZ[mpt];
			y = rayXYZ[mpt+1];
			z = rayXYZ[mpt+2];

			dist = sqrt( (prevX-x)*(prevX-x)+(prevY-y)*(prevY-y)+(prevZ-z)*(prevZ-z) );
			rayL[i] = dist;
			length += dist;

			prevX = x;
			prevY = y;
			prevZ = z;
		}

		minL = 1.0e10; 	maxL = -minL;
		for(i=1; i<npt; i++) {
			a = rayL[i];
			minL = minL<a ? minL:a;
			maxL = maxL>a ? maxL:a;
		}

		if(length!=0.0) dua = dt/length;
		else dua = 0.0;	
		aLength = length / (npt-1);

		for(i=0; i<npt; i++) {
			mpt=3*i;
			ia = rayijk[mpt];
			ib = rayijk[mpt+1];
			ic = rayijk[mpt+2];

			//if(aLength!=0.0) weight = rayL[iseg]/aLength;
			//else weight = 1.0;
			//dua = eLength*s0(ia, ib, ic);

			nrays0(ia,ib,ic) += 1; //(int)(weight);
			if(method==0)				
				dus0(ia,ib,ic) += dua; //*weight;
			else if( method==200) {
				if(length!=0.0) dua = rayL[i]/length;
				else dua = 0.0;	
				dus0(ia,ib,ic) += dua;
			}
			else { }
		}
		if( method==100 ) {
			fwrite(&npt, sizeof(int), 1, fid);
			fwrite(rayXYZ, sizeof(rayXYZ[0]), 3*npt, fid);
		}
		else if( method==300) {
			for(i=1; i<npt; i++) {
				mpt=3*i;
				ia = rayijk[mpt];
				ib = rayijk[mpt+1];
				ic = rayijk[mpt+2];

				row[itpt]	= itp[0];
				col[itpt]	= ic*nxy+ib*nx+ia;
				G[itpt]		= (float)(rayL[i]);
				itpt++;
			}
			D[ itp[0] ] = dt;
			itp[0]++;
			itp[1] = itpt;
		}
		else { }


		/*
		
		//effective ray length
		if( method==10 ) {
			sum = 0.0;
			for(iseg=0; iseg<=nseg; iseg++) 
				sum += rayL[iseg]*s0(rayi[iseg], rayj[iseg], rayk[iseg]);
			if(sum!=0.0) eLength = dt/sum;
			else eLength = 0.0;
		}
		sum = 0.0;
		for(iseg=0; iseg<=nseg; iseg++) 
		sum += rayL[iseg]*dus0(rayi[iseg], rayj[iseg], rayk[iseg]);
		if(sum==0.0) c = 1.0;
		else {
		a = dt*sum;
		b = sum*sum;
		*alphaU = a;
		*alphaD = b;
		c = a/b;
		}
		for(iseg=0; iseg<=nseg; iseg++) 
		dus0(rayi[iseg], rayj[iseg], rayk[iseg]) *= c;
		*/

		free_dvector( rayL );
		//fclose( fid );

	#undef nrays0(i,j,k)
	#undef dus0(i,j,k)	
	#undef s0(i,j,k)

	}

	void rayGrad(const int method, const int nx, const int ny, const int nz, const double h, double x0, double y0, double z0, 
				 double x, double y, double z, float *t, double *gradt )
	{
		double rx[6], ry[6], rz[6], tcal[6];
		int i, j, k, nxy, nt;
		double h1;

		nxy = nx*ny;
		nt = 6;
		h1 = 0.5*h;

		if( method==1 ) {
			#define t0(i,j,k)				t[nxy*(k) + nx*(j) + (i)]
			
			i = (int)((x-x0)/h);
			j = (int)((y-y0)/h);
			k = (int)((z-z0)/h);

			//FIND THE RAY GRAD(T)
			gradt[0] = (t0(i+1,j,k)+t0(i+1,j+1,k)+t0(i+1,j+1,k+1)+t0(i+1,j,k+1)
				-t0(i,j+1,k)-t0(i,j+1,k)-t0(i,j+1,k+1)-t0(i,j,k+1)) /(4.0*h);
			gradt[1] = (t0(i,j+1,k)+t0(i+1,j+1,k)+t0(i+1,j+1,k+1)+t0(i,j+1,k+1)
				-t0(i,j,k)-t0(i+1,j,k)-t0(i+1,j,k+1)-t0(i,j,k+1)) /(4.0*h);
			gradt[2] = (t0(i,j,k+1)+t0(i+1,j,k+1)+t0(i+1,j+1,k+1)+t0(i,j+1,k+1)
				-t0(i,j,k)-t0(i+1,j,k)-t0(i+1,j+1,k)-t0(i,j+1,k)) /(4.0*h);

			#undef t0(i,j,k)
		}
		else if( method==2 ) {
			//left face
			i = 0;
			rx[i] = x - h1;
			ry[i] = y;
			rz[i] = z;
			//right face
			i = 1;
			rx[i] = x + h1;
			ry[i] = y;
			rz[i] = z;
			//front face
			i = 2;
			rx[i] = x;
			ry[i] = y - h1;
			rz[i] = z;
			//back face
			i = 3;
			rx[i] = x ;
			ry[i] = y + h1;
			rz[i] = z;
			//bottom face
			i = 4;
			rx[i] = x;
			ry[i] = y;
			rz[i] = z - h1;
			//top face
			i = 5;
			rx[i] = x;
			ry[i] = y;
			rz[i] = z + h1;

			Trilinear3DTT(2, nx, ny, nz, h, x0, y0, z0, nt, rx, ry, rz, t, tcal );

			gradt[0] = tcal[1] - tcal[0];
			gradt[1] = tcal[3] - tcal[2];
			gradt[2] = tcal[5] - tcal[4];
		}
		else {

		}
	}

	/*
	void intersect(const int nx, const int ny, const int nz, const double h, double x0, double y0, double z0, 
				double x, double y, double z, float *t, double *gradt )
	{
		double a[6];
		int i, j, k, s;

		i = (int)((x-x0)/h);
		j = (int)((y-y0)/h);
		k = (int)((z-z0)/h);

		//find location of the cell left-down-back corner
		xi = h*i + x0;
		yj = h*j + y0;
		zk = h*k + z0;

		for(s=0; s<6; s++) a[s]=1.0e21;

		if( gradt[0]<0.0 ) {
			//left face
			a[0] = (xi-x)/gradt[0];
		}
		else if( gradt[0]>0.0 ){
			//right face
			a[1] = (xi+h-x)/gradt[0];
		}
		else { }

		if( gradt[1]<0.0 ) {
			//left face
			a[0] = (xi-x)/gradt[1];
		}
		else if( gradt[1]>0.0 ){
			//right face
			a[1] = (xi+h-x)/gradt[1];
		}
		else { }

		if( gradt[0]<0.0 ) {
			//left face
			a[0] = (xi-x)/gradt[0];
		}
		else if( gradt[0]>0.0 ){
			//right face
			a[1] = (xi+h-x)/gradt[0];
		}
		else { }



			//left face
			i = 0;
			rx[i] = x - h1;
			ry[i] = y;
			rz[i] = z;
			//right face
			i = 1;
			rx[i] = x + h1;
			ry[i] = y;
			rz[i] = z;
			//front face
			i = 2;
			rx[i] = x;
			ry[i] = y - h1;
			rz[i] = z;
			//back face
			i = 3;
			rx[i] = x ;
			ry[i] = y + h1;
			rz[i] = z;
			//top face
			i = 4;
			rx[i] = x;
			ry[i] = y;
			rz[i] = z - h1;
			//bottom face
			i = 5;
			rx[i] = x;
			ry[i] = y;
			rz[i] = z + h1;

			Trilinear3DTT(2, nx, ny, nz, h, x0, y0, z0, 5, rx, ry, rz, t, tcal );

			gradt[0] = tcal[1] - tcal[0];
			gradt[1] = tcal[3] - tcal[2];
			gradt[2] = tcal[5] - tcal[4];

	}

	*/
}
