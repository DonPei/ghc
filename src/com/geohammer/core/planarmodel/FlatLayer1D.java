package com.geohammer.core.planarmodel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import com.geohammer.rt.pseudo3d.Almost;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlatLayer1D implements LayersI {
	private double [] 	anchors 	= null; 	//the connect point for extracting from layer2D
	public int 			iUnit 		= 2;		//1 m; 2 ft; 3 km
	
	protected ArrayList<SingleFlatLayer> layerList = null;

	public FlatLayer1D() { }
	public FlatLayer1D(int nBoundaries) {
		layerList = new ArrayList<SingleFlatLayer>(nBoundaries);
	}
	public FlatLayer1D(String selectedFileName)	{
		read(selectedFileName);
	}
	public FlatLayer1D(String [] layerNames, double [] top, double [] vp, double [] vs, double [] den, 
			double [] delta, double [] epsilon, double [] gamma, 
			double [] qp, double [] qs, double [] theta, double [] phi) {
		this(top.length);
		for(int i=0; i<top.length; i++) {  
			SingleFlatLayer layer = new SingleFlatLayer();
			layer.setId(i);
			if(layerNames!=null) layer.setLayerName(layerNames[i]);
			if(top!=null) 		layer.setTopDepth(top[i]);
			if(vp!=null) 		layer.setVp(vp[i]);
			if(vs!=null) 		layer.setVs(vs[i]);
			if(den!=null) 		layer.setDen(den[i]);
			if(delta!=null) 	layer.setDelta(delta[i]);
			if(epsilon!=null) 	layer.setEpsilon(epsilon[i]);
			if(gamma!=null) 	layer.setGamma(gamma[i]);
			if(theta!=null) 	layer.setTheta(theta[i]);
			if(phi!=null) 		layer.setPhi(phi[i]);
			if(qp!=null) 		layer.setQp(qp[i]);
			if(qs!=null) 		layer.setQs(qs[i]);

			add(layer); 
		}
		updateDepth();
		SingleFlatLayer lastLayer = getLayer(top.length-1);
		lastLayer.setBottomDepth(lastLayer.getTopDepth());
		updateH();
	}
	
	public void setSize(int nx, int ny, int nz,
			double dx, double dy, double dz, double x0, double y0, double z0) {
		for(int i=0; i<layerList.size(); i++) {
			layerList.get(i).setSize(nx, ny, nz, dx, dy, dz, x0, y0, z0);
		}
	}

	public int getLayerIndex(double sz, double rz) {
		checkDepth(sz);
		checkDepth(rz);
		if(Almost.DOUBLE.cmp(sz,rz)==0) return getLayerIndex(sz);
		for(int i=0; i<getNumOfBoundaries(); i++) { 
			if(layerList.get(i).inside(sz)) return i;
			else if(layerList.get(i).onTDepth(sz)) {if(rz<sz) return i-1; else return i;}
			else if(layerList.get(i).onBDepth(sz)) {if(rz<sz) return i; else return i+1;}
			else { }
		}
		return 0;
	}
	public int getLayerIndex(double z) {
		checkDepth(z);
		for(int i=0; i<getNumOfBoundaries(); i++) { if(layerList.get(i).include(z)) return i; }
		return 0;
	}	
	
	public FlatLayer1D extractModel(double top, double bot) {	
		double a = 0;
		if(top>bot) { a=top; top=bot; bot=a; }
		
		int iTop = getLayerTopIndex(top);
		int iBot = getLayerTopIndex(bot);
		FlatLayer1D other = new FlatLayer1D(1);
		for(int i=iTop; i<=iBot+1; i++) {  other.add(layerList.get(i).copy()); }
		return other; 
	}
	
	public int getLayerTopIndex(double z) {
		checkDepth(z);
		for(int i=0; i<getNumOfBoundaries(); i++) {
			if(layerList.get(i).inside(z)) return i;  
			else if(layerList.get(i).onTDepth(z)) return i;
			else if(layerList.get(i).onBDepth(z)) {
				if(i==getNumOfBoundaries()-1) return i;
				else return i+1;
			}
			else { }
		}
		return 0;
	}

	public int getNumOfBoundaries() 				{ return layerList.size(); }
	public SingleFlatLayer getBoundary(int index) 	{ return layerList.get(index); }
	public ArrayList<SingleFlatLayer> getLayers() 	{ return layerList; }
	public SingleFlatLayer getLayer(int index) 		{ return layerList.get(index); }
	public ArrayList<SingleFlatLayer> getLayer() 	{ return layerList; }
	public void setModelSize(double x0, double x1) { 
		for(int i=0; i<layerList.size(); i++) layerList.get(i).setLayerSize(x0, x1);
	}
	public void setModelSize(double x0, double y0, double x1, double y1) {
		for(int i=0; i<layerList.size(); i++) layerList.get(i).setLayerSize(x0, y0, x1, y1); 
	}
	public boolean isIso() {
		for(int i=0; i<layerList.size(); i++) { 
			if(!layerList.get(i).isIso()) return false; 
		}
		return true;
	}
	
	public void trimAnisotropicParameters() {
		for(int i=0; i<layerList.size(); i++) { layerList.get(i).trimAnisotropicParameters(); }
	}
	public void toCij() {
		for(int i=0; i<layerList.size(); i++) { layerList.get(i).toCij(); }
	}
	
	public FlatLayer1D copy() { 
		FlatLayer1D other = new FlatLayer1D(layerList.size());
		for(int i=0; i<layerList.size(); i++) {  other.add(layerList.get(i).copy()); }
		return other; 
	}
	public void shiftZ(double z) {
		for(int i=0; i<layerList.size(); i++) {  layerList.get(i).shiftZ(z); }
	}

	public void ftToMeter() { 
		iUnit = 1; 
		for(int i=0; i<getNumOfBoundaries(); i++) layerList.get(i).convertUnitSystem(0.3048, 1.0); 
	}
	public void meterToFt() { 
		iUnit = 2; 
		for(int i=0; i<getNumOfBoundaries(); i++) layerList.get(i).convertUnitSystem(3.28084, 1.0); 
	}
	public void convertUnitSystem(double velScalor, double denScalor) {
		for(int i=0; i<layerList.size(); i++) {  layerList.get(i).convertUnitSystem(velScalor, denScalor); }
	}

	public double [] getTop() { 
		double [] top = new double[layerList.size()]; 
		for(int i=0; i<layerList.size(); i++) { top[i] = layerList.get(i).getTopDepth(); }
		return top; 
	}
	public double [] getLayerThickness() {
		double [] vel = new double[layerList.size()];
		for(int i=0; i<layerList.size(); i++) { vel[i] = getLayer(i).getThickness(); }
		return vel;
	}
	public double [] getLayerProperty(int id) {
		double [] vel = new double[layerList.size()];
		for(int i=0; i<layerList.size(); i++) { vel[i] = getLayer(i).getLayerProperty(id); }
		return vel;
	}
	public double getLayerPropertyMean(int id) {
		double sum = 0.0;
		for(int i=0; i<layerList.size(); i++) { sum += getLayer(i).getLayerProperty(id); }
		return sum/layerList.size();
	}
	
	public boolean isVsZero() {
		for(int i=0; i<layerList.size(); i++) { 
			if(getLayer(i).getVs()==0) return true; 
		}
		return false;
	}
	public String validate() {
		String errorMsg = null;
		for(int i=0; i<layerList.size(); i++) { 
			errorMsg = getLayer(i).validate(); 
			if(errorMsg!=null) {
				return "Layer "+i+" "+errorMsg+" ";
			}
		}
		
		for(int i=0; i<layerList.size()-1; i++) { 
			double h = getLayer(i).getThickness(); 
			if(h==0) {
				return "Layer "+i+" Thickness is ZERO ";
			}
		}
		
		return null;
	}
	
	
	public double getVel(int iVp, int iBoundary) 	{ if(iVp==1||iVp==11) return layerList.get(iBoundary).getVp(); 
													else return layerList.get(iBoundary).getVs();}

	public boolean inside(double z) 	{ return Almost.DOUBLE.cmp(z,layerList.get(0).getTopDepth())==1 
										&& Almost.DOUBLE.cmp(z,layerList.get(getNumOfBoundaries()-1).getTopDepth())==-1; }
	public boolean onTDepth(double z) 	{ return Almost.DOUBLE.cmp(layerList.get(getNumOfBoundaries()-1).getTopDepth(),z)==0; }
	public boolean onBDepth(double z) 	{ return Almost.DOUBLE.cmp(layerList.get(getNumOfBoundaries()-1).getTopDepth(),z)==0; }
	public boolean include(double z) 	{ return inside(z)||onTDepth(z)||onBDepth(z); }
	public boolean exclude(double z) 	{ return !include(z); }
	public void checkDepth(double z) 	{ assert exclude(z) : "The depth is outside of model range"; }
	public boolean onTBoundary(double z) 	{ 
		for(int i=0; i<getNumOfBoundaries(); i++) { if(layerList.get(i).onTDepth(z)) return true; }
		return false;
	}

	public void remove(int index) 				 		{ layerList.remove(index); }
	public void add(SingleFlatLayer layer) 			 	{ layerList.add(layer); }
	public void add(int index, SingleFlatLayer layer) 	{ layerList.add(index, layer); }
	public void set(int index, SingleFlatLayer layer) 	{ layerList.set(index, layer); }

	public void split(double z) { 
		int index = getLayerIndex(z);
		SingleFlatLayer lowerPart = layerList.get(index).split(z);
		if(lowerPart==null) return;
		add(index+1, lowerPart);
	}
	public void split(double sz, double rz) { 
		split(sz);
		split(rz);
		updateDepth();
		updateH();
	}
	public void clip(double z, double minGap) {
		for(int i=0; i<getNumOfBoundaries()-1; i++) { 
			double zh = layerList.get(i).getTopDepth()-z;
			if(Math.abs(zh)<=minGap) {
				layerList.get(i).setTopDepth(z);
			}
		} 
		updateDepth();
		updateH();
	}
	// remove zero thickness or layers less than minThickness thick
	public void trim(double minThickness) {
		FlatLayer1D other = copy();
		for(int i=0; i<getNumOfBoundaries()-1; i++) { 
			if(layerList.get(i).getThickness()<=minThickness) {
				remove(i);
			}
		} 
		
	}
	public void remove(double z) { 
		int index = getLayerIndex(z);
		remove(index);
		updateDepth();
		updateH();
	}

//	public void adjustLayerThickness(double dip) {
//		double theCos = Math.cos(dip*Math.PI/180.0);
//		SingleFlatLayer sl = layerList.get(0);
//		double top = sl.getBottomDepth();
//		for(int i=1; i<getNumOfBoundaries()-1; i++) { 
//			sl = layerList.get(i);
//			sl.setTopDepth(top);
//			double h = sl.getThickness()*theCos;
//			top += h;
//		} 
//		
//		for(int i=0; i<getNumOfBoundaries()-1; i++) { 
//			layerList.get(i).setBottomDepth(layerList.get(i+1).getTopDepth());
//		} 
//		layerList.get(getNumOfBoundaries()-1).setBottomDepth(layerList.get(getNumOfBoundaries()-1).getTopDepth());
//		
//		for(int i=0; i<getNumOfBoundaries(); i++) { 
//			layerList.get(i).updateH();
//		} 
//	}
	public void updateH() { for(int i=0; i<getNumOfBoundaries(); i++) { layerList.get(i).updateH();} }
	public void updateDepth() { 
		for(int i=0; i<getNumOfBoundaries()-1; i++) { 
			if(Almost.DOUBLE.cmp(layerList.get(i).getBottomDepth(),layerList.get(i+1).getTopDepth())!=0) {
				layerList.get(i).setBottomDepth(layerList.get(i+1).getTopDepth());
			}
		} 
		layerList.get(getNumOfBoundaries()-1).setBottomDepth(layerList.get(getNumOfBoundaries()-1).getTopDepth());
	}
	public void updateTop(int firstTop) { 
		for(int i=firstTop; i<getNumOfBoundaries(); i++) { 
			layerList.get(i).setTopDepth(layerList.get(i-1).getBottomDepth());
		}
	}
	// head wave(only from the layer which has velocity higher than all above layers)
	public boolean headWavePossible(double[] z, double [] max) {
		return headWavePossible(1, z, max);
	}
	public boolean headWavePossible(int iVp, double[] z, double [] max) { 
		assert z.length != 3 : "Error: only three element array needed";
		int ke = getLayerTopIndex(z[0]);
		int kr = getLayerTopIndex(z[2]);
		int k = getLayerTopIndex(z[1]);
		double vMax = max[0];
		if(k>ke && k>kr) {
			if(k==getNumOfBoundaries()-1) return false;
			for(int i=ke; i<k; i++) { if(getVel(iVp, i)>vMax) return false; }
			for(int i=kr; i<k; i++) { if(getVel(iVp, i)>vMax) return false; }
		} else if(k<ke && k<kr) {
			if(k==0) return false;
			vMax = getVel(iVp, k-1);
			for(int i=k; i<ke; i++) { if(getVel(iVp, i)>vMax) return false; }
			for(int i=k; i<kr; i++) { if(getVel(iVp, i)>vMax) return false; }
		} else { return false; }
		max[0] = vMax;
		return true;
	}


	public int [][] calRayCodesIdz(double sz, double rz, int iPS) { 
		int ke = getLayerTopIndex(sz);
		int kr = getLayerTopIndex(rz);
		int N = Math.abs(ke-kr);

		int [] rayCodes = new int[N];
		int [] rayIdz = new int[N];
		if(ke>kr) {
			for(int i=ke-1, j=0; i>=kr; i--, j++) {rayCodes[j] = i; rayIdz[j] = -1;}
		} else {
			for(int i=ke, j=0; i<kr; i++, j++) {rayCodes[j] = i; rayIdz[j] = 1;}
		}

		if(iPS<0) for(int i=0; i<N; i++) { rayCodes[i] *= -1; }

		//System.out.println("ke="+ke+" kr="+kr+" "+Arrays.toString(rayCodes)+" "+Arrays.toString(rayIdz)); 
		return new int [][] {rayCodes, rayIdz};
	}
	public int [][] calRayCodesIdz(double[] z, int [] ps) { 
		assert z.length<2 : "provide at least two points";
		int [][][] rayCodesIdzs = new int[z.length-1][][];
		if(ps==null) {
			for(int i=0, j=0; i<z.length-1; i++, j++) rayCodesIdzs[i] = calRayCodesIdz(z[j], z[j+1], 1);
		} else {
			for(int i=0, j=0; i<z.length-1; i++, j++) rayCodesIdzs[i] = calRayCodesIdz(z[j], z[j+1], ps[j]);
		}

		int N = 0;
		for(int i=0; i<z.length-1; i++) N+=rayCodesIdzs[i][0].length;

		int [][] rayCodes = new int[2][N];
		for(int i=0, k=0; i<z.length-1; i++) {
			for(int j=0; j<rayCodesIdzs[i][0].length; j++, k++) {
				rayCodes[0][k] = rayCodesIdzs[i][0][j]; 
				rayCodes[1][k] = rayCodesIdzs[i][1][j];
			}
		}

		return rayCodes;
	}
	public int [] calLayerIndex() {
		int nLayers = getNumOfBoundaries()-1;
		int [] index = new int[nLayers];
		for(int i=0; i<nLayers; i++) { index[i] = layerList.get(i).getId(); }
		return index;
	}
	public double [] calHPrime() {
		int nLayers = getNumOfBoundaries()-1;
		double [] h = new double[nLayers];
		for(int i=0; i<nLayers; i++) { h[i] = layerList.get(i).getThickness(); }
		return h;
	}
	public double [] calVPrime(int iVp) {
		int nLayers = getNumOfBoundaries()-1;
		double [] v = new double[nLayers];
		for(int i=0; i<nLayers; i++) { 
			if(iVp==1||iVp==11) v[i] = layerList.get(i).getVp(); 
			else v[i] = layerList.get(i).getVs();
		}
		return v;
	}
	public double [] calZPrime(int [][] rayCodesIdz) {
		int [] rayCodes = rayCodesIdz[0];
		int [] down = rayCodesIdz[1];
		//System.out.println("down= "+ Arrays.toString(down));
		double [] z = new double[rayCodes.length];
		for(int i=0, k=0; i<rayCodes.length; i++) {
			k = Math.abs(rayCodes[i]);
			if(down[i]>0) z[i] = layerList.get(k).getBottomDepth(); // going down;
			else  z[i] = layerList.get(k).getTopDepth(); 		// going up
			//System.out.println("k="+k+" z="+z[i]);
		}
		return z;
	}
	public int [] calRayIdz(int idz, int [] rayCodes) {
		int [] derivative 	= new int[rayCodes.length];
		derivative[0] = idz;
		for(int i=1; i<rayCodes.length; i++) { derivative[i] = (int)(Math.abs(rayCodes[i])-Math.abs(rayCodes[i-1])); }

		for(int i=0; i<rayCodes.length; i++) {
			if(derivative[i] ==0) { derivative[i] = -derivative[i-1]; } 
		}
		return derivative;
	}

	public FlatLayer1D toLayer1DPrime(int [] rayCodes) {
		FlatLayer1D other = new FlatLayer1D(rayCodes.length+1);
		int k = 0;
		for(int i=0; i<rayCodes.length; i++) {
			k = Math.abs(rayCodes[i]);
			SingleFlatLayer layer = layerList.get(k).copy();
			if(rayCodes[i]<0) layer.setVp(layer.getVs());
			other.getLayers().add(layer);
		}
		SingleFlatLayer layer1 = layerList.get(k).copy();
		layer1.setTopDepth(layer1.getBottomDepth());
		layer1.updateH();
		other.getLayers().add(layer1);

		return other;
	}

	public double [][] to2DArray() {
		double [][] initData = new double[][] {
				getTop(), getLayerProperty(1), getLayerProperty(2), getLayerProperty(3), 
				getLayerProperty(4), getLayerProperty(21), 
				getLayerProperty(20), getLayerProperty(22)};
		
		int nBoundary = getNumOfBoundaries();
		double [][] data = new double[initData.length][2*(nBoundary-1)];
		for(int i=0; i<nBoundary-1; i++) {
			int k = 2*i;

			data[0][k] 		= initData[0][i];
			data[0][k+1] 	= initData[0][i+1];
			
			for(int j=1; j<data.length; j++) {
				data[j][k] 		= initData[j][i];
				data[j][k+1] 	= initData[j][i];
			}
		}
		return data;
	}
	public float [][] to2DArrayFloat() {
		double [][] initData = new double[][] {
				getTop(), getLayerProperty(1), getLayerProperty(2), getLayerProperty(3), 
				getLayerProperty(4), getLayerProperty(21), 
				getLayerProperty(20), getLayerProperty(22)};
		
		int nBoundary = getNumOfBoundaries();
		float [][] data = new float[initData.length][2*(nBoundary-1)];
		for(int i=0; i<nBoundary-1; i++) {
			int k = 2*i;

			data[0][k] 		= (float)initData[0][i];
			data[0][k+1] 	= (float)initData[0][i+1];
			
			for(int j=1; j<data.length; j++) {
				data[j][k] 		= (float)initData[j][i];
				data[j][k+1] 	= (float)initData[j][i];
			}
		}
		return data;
	}
	
	public Layer2D toLayer2D(double x1, double x2) {
		
		Layer2D layer2D = new Layer2D(layerList.size());
		for(int i=0; i<layerList.size(); i++) {
			ArrayList<Point2D> 	mp = new ArrayList<Point2D>(2);
			mp.add(new Point2D(x1, layerList.get(i).getTopDepth()));
			mp.add(new Point2D(x2, layerList.get(i).getTopDepth()));
			SingleLayer2D singleLayer = new SingleLayer2D("layer_"+i, i, 
					layerList.get(i).getVp(), layerList.get(i).getVs(), layerList.get(i).getDen(), 
					0,0,0,0,  
					layerList.get(i).getDelta(), layerList.get(i).getEpsilon(), layerList.get(i).getGamma(), 
					layerList.get(i).getQp(), layerList.get(i).getQs(), 
					layerList.get(i).getTheta(), layerList.get(i).getPhi(),
					mp, null);
			singleLayer.setC11(layerList.get(i).getC11());
			singleLayer.setC13(layerList.get(i).getC13());
			singleLayer.setC33(layerList.get(i).getC33());
			singleLayer.setC55(layerList.get(i).getC55());
			singleLayer.setC66(layerList.get(i).getC66());
			singleLayer.setC12(layerList.get(i).getC12());
			singleLayer.setC44(layerList.get(i).getC44());			
			
			singleLayer.setSize(1, 2, 2, 1, x2-x1, 1, 0, x1, 0);
			//singleLayer.setSize(2, 2, 2, x2-x1, 1, 1, x1, 0, 0);
			singleLayer.setCurve();
			
			layer2D.getLayer().add(singleLayer);
		}
		return layer2D;
	}
	
	public void read(String selectedFileName) {
		try{
			BufferedReader reader = new BufferedReader(new FileReader(selectedFileName));
			StringTokenizer st = null;
			String line = reader.readLine(); // 1st line Title
			int nBoundaries = getNumOfValidLines(selectedFileName);
			layerList = new ArrayList<SingleFlatLayer>(nBoundaries);

			int k = 0;
			while((line = reader.readLine())!=null) {
				st = new StringTokenizer(line, " ,\t");
				if(st.countTokens()<=1) continue;
				SingleFlatLayer layer = new SingleFlatLayer();
				layer.setId(k);
				layer.setTopDepth(Double.parseDouble(st.nextToken().trim()));
				layer.setVp(Double.parseDouble(st.nextToken().trim()));
				if(st.hasMoreTokens()) layer.setVs(Double.parseDouble(st.nextToken().trim()));
				if(st.hasMoreTokens()) layer.setDen(Double.parseDouble(st.nextToken().trim()));
				if(st.hasMoreTokens()) layer.setDelta(Double.parseDouble(st.nextToken().trim()));
				if(st.hasMoreTokens()) layer.setEpsilon(Double.parseDouble(st.nextToken().trim()));
				if(st.hasMoreTokens()) layer.setGamma(Double.parseDouble(st.nextToken().trim()));
				if(st.hasMoreTokens()) layer.setTheta(Double.parseDouble(st.nextToken().trim()));
				if(st.hasMoreTokens()) layer.setPhi(Double.parseDouble(st.nextToken().trim()));
				if(st.hasMoreTokens()) layer.setQp(Double.parseDouble(st.nextToken().trim()));
				if(st.hasMoreTokens()) layer.setQs(Double.parseDouble(st.nextToken().trim()));

				layerList.add(layer);
				k++;
			}
			updateDepth();
			layerList.get(nBoundaries-1).setBottomDepth(layerList.get(nBoundaries-1).getTopDepth());
			updateH();
			reader.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}	
	}

	public int getNumOfValidLines(String selectedFileName) {
		int nLines = 0;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(selectedFileName));
			String line = reader.readLine(); // 1st line Title
			StringTokenizer st = null;
			while((line = reader.readLine())!=null) {
				st = new StringTokenizer(line, " ,\t");
				if(st.countTokens()>1) nLines++;
			}
			reader.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		return nLines;
	}
	public void write(String selectedFileName) {
		try{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(selectedFileName, false));
			bufferedWriter.write(toString());
			bufferedWriter.close();
		} catch (IOException ioexception) {
			JOptionPane.showMessageDialog(null, "\nFile Format is not right!", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	public String toString() {
		int nBoundaries = getNumOfBoundaries();
		String separator = System.getProperty( "line.separator" );
		StringBuilder lines = new StringBuilder("Top_Depth, Vp,   Vs,   Density,   Delta,   Epsilon,   Gamma");
		lines.append( separator );
		for(int i=0; i<nBoundaries; i++) {
			SingleFlatLayer layer = layerList.get(i);
			String a =layer.getTopDepth()+ ", "+layer.getVp()+ ", "+layer.getVs()+ ", "+layer.getDen()+ ", "+
					layer.getDelta()+ ", "+layer.getEpsilon()+ ", "+layer.getGamma(); 
			lines.append(a);
			if(i!=nBoundaries) lines.append( separator );
		}
		return lines.toString( );
	}
	
	public String toDeepString() {
		int nBoundaries = getNumOfBoundaries();
		String b ="Id Top_Depth Bot_Depth H  Vp   Vs   Density   Delta   Epsilon   Gamma   Theta   Phi Qp   Qs   "+"\n";
		String a = null;
		for(int i=0; i<nBoundaries; i++) {
			SingleFlatLayer layer = layerList.get(i);
			a =layer.getId()+ " "+layer.getTopDepth()+ " "+layer.getBottomDepth()+ " "+layer.getThickness()+ " "+
					layer.getVp()+ " "+layer.getVs()+ " "+layer.getDen()+ " "+ 
					layer.getDelta()+ " "+layer.getEpsilon()+ " "+layer.getGamma()+ " "+layer.getTheta()+ " "+layer.getPhi()+" "+
					layer.getQp()+ " "+layer.getQs(); 
			b = b.concat(a+"\n");
		}

		return b;
	}
}
