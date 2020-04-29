package com.geohammer.core;

import static edu.mines.jtk.util.ArrayMath.randfloat;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

//import org.ucdm.ivc.dialog.XyPlot;

import edu.mines.jtk.dsp.Sampling;
import edu.mines.jtk.mosaic.PlotFrame;
import edu.mines.jtk.mosaic.PlotPanel;
import edu.mines.jtk.mosaic.PointsView;
import edu.mines.jtk.mosaic.SequenceView;


public class SeismicTraceComponentTest implements Runnable {
	public SeismicTraceComponentTest() {
		
	}
	public void run() {	
		test1();
		
		System.out.println("Successfully Done!");
	}
	public static void main(String[] args) {
		System.out.println("Executing SeisPTTest()");
		SwingUtilities.invokeLater( new SeismicTraceComponentTest() );
	}
	
	//http://www.cbmapps.com/docs/28
	private static void test1() {
		double rad = Math.PI/180.0;
		double m2 = 2*Math.PI;
	    //int nt = 10000;
	    int nt = 5000;
	    double A = Math.sqrt(2);
		double dt = 1.0/1400;
		//double dt = 0.0007;
	    double ph = 30.0*rad;
	    float [] t = new float[nt];
	    float [] acc = new float[nt]; 
	    double a = 0;
	    
	    for(int i=0; i<nt; i++) {
	    	a = i*dt;
	    	t[i] = (float)(a);
	    	a *= m2;
	    	a = 10*A*Math.sin(50*a)+5*A*Math.sin(120*a)+8*A*Math.sin(315*a)+2*A*Math.sin(500*a);
	    	acc[i] = (float)(a);
	    }
	    
	    SeismicTraceComponent comp = new  SeismicTraceComponent(dt, acc);
	    //comp.mean();
	    comp.cosineTaper(0.4);
	    comp.fft(); comp.calSpectrum();
	    float [] t1 = comp.getFrequency();
	    float [] acc1 = comp.calSpectrumDB();
	    
	    float [] vel = comp.convert(3, comp.getData(), comp.getData().length, comp.getSampleInterval(), 2);
	    for(int i=0; i<vel.length; i++) vel[i] *= 1000;
	    
	    SeismicTraceComponent compVel = new  SeismicTraceComponent(dt, vel);
	    compVel.fft(); compVel.calSpectrum();
	    float [] vel1 = compVel.calSpectrumDB();
		
	    //float [] amp = compVel.convert(2, compVel.getData(), compVel.getData().length, compVel.getSampleInterval(), 3);
	    float [] amp = comp.convert(3, comp.getData(), comp.getData().length, comp.getSampleInterval(), 1);
	    for(int i=0; i<amp.length; i++) amp[i] *= 1000000.0;
	    SeismicTraceComponent compAmp = new  SeismicTraceComponent(dt, amp);
	    compAmp.fft(); compAmp.calSpectrum();
	    float [] amp1 = compAmp.calSpectrumDB();
		
	    PlotPanel panel = new PlotPanel(3,1);
	    //PlotPanel panel = new PlotPanel(1,1);
//	    Sampling sx = new Sampling(t1.length,t1[1]-t1[0],t1[0]);
//	    SequenceView sv1 = panel.addSequence(0,0,sx,acc1);
//	    sv1.setColor(Color.RED);
	    
	    //panel.setLimits(-0.1,-0.1,1.1,1.1);
	    
	    PointsView pv = null;
	    panel.setHLimits(1.0, 1.2);
	    pv = panel.addPoints(0, 0, t,acc);
	    pv.setStyle("k-");
	    pv = panel.addPoints(1, 0, t,vel);
	    pv.setStyle("r-");
	    pv = panel.addPoints(2, 0, t,amp);
	    pv.setStyle("g-");
	    
//	    pv = panel.addPoints(0, 0, t1,acc1);
//	    pv.setStyle("k-");
//	    pv = panel.addPoints(1, 0, t1,vel1);
//	    pv.setStyle("r-");
//	    pv = panel.addPoints(2, 0, t1,amp1);
//	    pv.setStyle("g-");
	    
	    //pv = panel.addPoints(t,comp.getData());
	    //pv.setStyle("b-");
	    //pv.setLineStyle(PointsView.Line.NONE);
	    //pv.setMarkStyle(PointsView.Mark.FILLED_CIRCLE);
	    //pv.setTextFormat("%4.2f");
	    PlotFrame frame = new PlotFrame(panel);
	    frame.setSize(1400,800);
	    frame.setDefaultCloseOperation(PlotFrame.EXIT_ON_CLOSE);
	    frame.setVisible(true);
	    //frame.paintToPng(300,6,"junk.png");
	  }

}
