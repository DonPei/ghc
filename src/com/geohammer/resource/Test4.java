package com.geohammer.resource;

//Test2.java
import java.awt.Graphics;
import javax.swing.JPanel;
import javax.swing.JFrame;


class Panel4 extends JPanel
{

	Line2 line[];
	Vec2 pts[];
	Line2 perp_line;

	public Panel4() {
		line = new Line2[4];
		line[0] = new Line2(-0.3,-0.5,-0.7,0.9);
		line[1] = new Line2();
		line[1].from(0.5,-0.5).away(0.8,60.0);
		Vec2 a = new Vec2(-0.4,0.5);
		Vec2 b = new Vec2(0.6, 0.8);
		line[2] = new Line2(a,b);
		pts = new Vec2[1];
		pts[0] = new Vec2(-1.4,-0.2);
		double s = line[0].getClosestPosition(pts[0]);
		perp_line = new Line2(pts[0], line[0].lerp(s));
		
		line[3] = new Line2(-1.0,0.0,1.0,0.0);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent( g );
		// world/screen transformation
		Vec2.xoffset = getWidth()/2;
		Vec2.yoffset = getHeight()/2;
		int resx = getWidth()/4;
		int resy = getHeight()/3;
		Vec2.Resolution = (resx<resy? resx: resy);
		// Here are our objects
		//for (int i=0; i<line.length; i++) line[i].draw(g);
		for (int i=0; i<line.length; i++)
			Arrow.draw(line[i],g,1.5,Arrow.AH_FILLED);
		for (int j=0; j<pts.length; j++) pts[j].draw(g);
		perp_line.draw(g);
		// Coordinate origin
		Vec2 origin = new Vec2(0,0);
		int [] iv = origin.convert();
		int size = 5;
		g.drawLine(iv[0]-size,iv[1]-size,iv[0]+size,iv[1]+size);
		g.drawLine(iv[0]-size,iv[1]+size,iv[0]+size,iv[1]-size);
	}
}

public class Test4
{

	public static void test1()
	{
		Vec2 a = new Vec2(14,20);
		Vec2 b = new Vec2(4,-3);
		Vec2 c = new Vec2(6,8);
		Vec2 d = new Vec2(-8,4);

		Line2 AB = new Line2(a,b);
		Line2 CD = new Line2(c,d);


		System.out.println( "line AB: " + AB );
		System.out.println( "line CD: " + CD );
		System.out.println( "line AB + Vec2(1,-1): " + AB.translate(1.0,-1.0) );

		System.out.println( "length AB : " + AB.length() );
		System.out.println( "angle(AB,CD) : " + Line2.angle(AB,CD) );

		Line2 XY = new Line2();
		System.out.println( "XY.from(0,1).to(3,2): " + XY.from(0,1).to(3,2) );
		System.out.println( "XY.to(a).from(b): " + XY.to(a).from(b) );

		Vec2 angled = new Vec2(30.0);
		System.out.println( "angled = Vec2(30.0): " + angled );
		System.out.println( "Line2(1,2,3,4): " + new Line2(1,2,3,4) );
		Line2 R1 = new Line2(angled);
		System.out.println( "R1 = Line2(30.0)): " + R1 );

		Line2 R2 = new Line2();
		R2.from(1,1).away(1.0,-90.0);
		System.out.println( "R2.from(1,1).away(1.0,-90.0) " + R2 );
		System.out.println("test 1 done.");

	}

	public static void main(String args[] )
	{
		Panel4 panel = new Panel4();
		for (int i=0; i<args.length; i++) {
			String str = String.format("arg[%d] %s",i,args[i]);
			System.out.println(str);
		}
		if (args.length>0 && args[0].equals("1"))
		{
			test1();
			return;
		}
		JFrame application = new JFrame("Test4");
		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		application.add(panel);
		application.setSize(200,200);
		application.setVisible(true);
	}
}


