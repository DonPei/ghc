package com.geohammer.resource;

import java.awt.Graphics;
import javax.swing.JPanel;
import javax.swing.JFrame;

public class Test2 extends JPanel
{
	Vec2 pt[];
	Test2()
	{
		pt = new Vec2[3];
		pt[0] = new Vec2(0.1,0.1);
		pt[1] = new Vec2(0.5, 0.8);
		pt[2] = new Vec2(0.8,0.1);
		System.out.println("Vec2's:" + pt[0] + pt[1] + pt[2]);
	}
	public void paintComponent(Graphics g)  {
		super.paintComponent( g );
		Vec2.xoffset = getWidth()/2;
		Vec2.yoffset = getHeight()/2;
		int resx = getWidth()/4;
		int resy = getHeight()/3;
		Vec2.Resolution = (resx<resy? resx: resy);
		for (int i=0; i<pt.length; i++) pt[i].draw(g);
		Vec2 origin = new Vec2(0,0);
		int [] iv = origin.convert();
		g.drawLine(iv[0]-2,iv[1]-2,iv[0]+2,iv[1]+2);
		g.drawLine(iv[0]-2,iv[1]+2,iv[0]+2,iv[1]-2);
	}

	public static void main(String args[] ) {
		Test2 panel = new Test2();
		JFrame application = new JFrame("Test2");
		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		application.add(panel);
		application.setSize(500,200);
		application.setVisible(true);
	}
}
