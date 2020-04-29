package com.geohammer.vc.run;

public class SaRandom {
	int _randomGenerator = 1;
	
	public SaRandom() { }
	public SaRandom(int randomGenerator) { _randomGenerator = randomGenerator; }

	// return a random number in a distribution of whichOne
	public double rn(String distribution, int method, double mu, double sigma, double lamda, double temp ){
		double y=0.0;

		if(distribution.equalsIgnoreCase("StdUniform"))
			y = frandom( );
		else if(distribution.equalsIgnoreCase("Uniform"))
			y = funiform( method, mu, sigma, temp);
		else if(distribution.equalsIgnoreCase("Gaussian"))
			y = fgaussian( method, mu, sigma, temp);
		else if(distribution.equalsIgnoreCase("Cauchy"))
			y = fcauchy( method, mu, sigma, temp);
		else if(distribution.equalsIgnoreCase("VFSA"))
			y = fVFSA( method, mu, sigma, temp);
		else { }

		return y;
	}

	// return a random number in a distribution of whichOne
	public double rn(int iDistribution, int method, double mu, double sigma, double lamda, double temp ){
		double y=0.0;

		if( iDistribution == 1 )
			y = frandom( );
		else if( iDistribution == 2 )
			y = funiform( method, mu, sigma, temp);
		else if( iDistribution == 3 )
			y = fgaussian( method, mu, sigma, temp);
		else if( iDistribution == 4 )
			y = fcauchy( method, mu, sigma, temp);
		else if( iDistribution == 5 )
			y = fVFSA( method, mu, sigma, temp);
		else { }

		return y;
	}
	// Compute idum = (IA * idum) % IM without overflows by Schrage's method
	//return a x within (0.0, 1.0) with E[x]=1/2 and var(x)=1/12
	public double frandom( ){
		double random = 0.0;

		if(_randomGenerator==1) {
			int IA=16807, IM=2147483647, IQ=127773, IR=2836, NTAB=32;
			int NDIV=(1+(IM-1)/NTAB);
			double AM=(1.0/IM), EPS=3.0e-16, RNMAX=(1.0-EPS);
			int iy=0;
			int [] iv = new int[32];
			int j, k;
			double temp;
			int idum=-57983;

			if((idum)<=0 || iy==0 ) {
				if((-(idum)) < 1) (idum)=1;
				else (idum)= -(idum);
				for(j=NTAB+7; j>=0; j-- ) {
					k=(idum)/IQ;
					(idum)=IA*((idum)-k*IQ)-IR*k;
					if((idum)<0) (idum)+=IM;
					if(j<NTAB) iv[j]=(idum);
				}
				iy=iv[0];
			}

			k = (idum) / IQ;
			(idum) = IA * ((idum) - k * IQ) - IR * k;
			if ((idum) < 0) (idum) += IM;

			j = iy / NDIV;   // will be in the range 0..NTAB-1
			iy = iv [j];    // Output previously stored value and refill shuffle table
			iv [j] = (idum);

			if ((temp = AM * iy) > RNMAX) {
				random = RNMAX;      // no endpiont values
			} else {
				random = temp;
			}
		} else if(_randomGenerator==2) {
			random = Math.random();  //between [0.0, 1.0)
		} else { }

		return random;
	}

	// return a x within (mu-sigma, mu+sigma) with E[x]=0 and var(x)=0
	public double funiform(int method, double mu, double sigma, double temp){
		double a, b;
		a = mu-sigma;
		b = mu+sigma;

		return a+(b-a)*frandom();
	}

	// If a random variable X has a uniform distribution on [0, 1], then the variable Y defined as:
	// Y = x0 + g * tan(pi * (X - 1/2))
	// will have a Cauchy distribution with median x0 and half width at half maximum of g.
	// Note that the mean of a Cauchy distribution is undefined, so the variance (about the mean) is undefined,
	// and that the variance about zero is infinite.
	// return a x with a cauchy distribution
	public double fcauchy(int method, double location, double scale, double temperature){
		double PI1=3.141592653589793;
		double halfPI1=1.5707963267948965;

		//return location + scale*tan( PI1*frandom(idum)-halfPI1);
		return scale*temperature*Math.tan( halfPI1*frandom());
	}

	public double fVFSA(int method, double mu, double sigma, double temperature){
		double a, u, v;

		u = frandom();
		if		(u-0.5>0.0 )	v=1.0;
		else if (u-0.5==0.0)	v=0.0;
		else					v=-1.0;

		a=Math.pow( (1.0+1.0/temperature), Math.abs(2.0*u-1.0) );

		return v*temperature*(a-1.0);
	}

	// Box-Muller returns a normally distributed deviate with specified mu and
	// variance, using frandom() as the source of uniform deviates.
	// Box-Muller method is (-inf, inf)
	// Central-Theorem is (-1, 1)
	// visit www.dspguru.com/howto/tech/wgn2.htm
	public double fgaussian( int method, double mu, double sigma, double temp){
		int i, N;
		int iset=0;
		double gset = 0;
		double fac, rsq, v1, v2, Y=0;

		//if (strcmp(method, "Box-Muller") == 0) {
		if (method == 0) {
			//if ((idum)<0) iset=0;
			if ( iset == 0 ) {
				do {
					v1 = 2.0*frandom() - 1.0;
					v2 = 2.0*frandom() - 1.0;
					rsq = v1*v1 + v2*v2;
				} while ( rsq >= 1.0 || rsq == 0.0 );
				fac = Math.sqrt( -2.0*Math.log(rsq) / rsq );
				gset = v1*fac;
				iset = 1;
				Y = v2*fac;
			}
			else {
				iset = 0;
				Y = gset;
			}
		}
		//if (strcmp(method, "Central-Theorem" ) == 0 )
		else if (method == 1 )
		{
			Y = 0; N = 30;
			for( i=0; i<30; i++) { Y += frandom(); }
			Y = Y / (double)N;
			Y = (Y-0.5) * Math.sqrt( 12.0*(double)N );
		}

		else { }

		return (mu + sigma * Y);
	}
}
