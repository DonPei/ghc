package com.geohammer.resource;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Resource {

	public static Icon loadIcon(Class<?> cls, String res) {
		java.net.URL url = cls.getResource(res);
		//System.out.println(url);
		return (url!=null)?new ImageIcon(url):null;
	}

	public static Icon loadIcon(String res) {
		return loadImageIcon(res);
	}

	public static ImageIcon loadImageIcon(String res) { return loadImageIcon(res, 1.0); }
	public static ImageIcon loadImageIcon(String res, double percentage) {
		String base = "icon/";
		java.net.URL url = Resource.class.getResource(base+res);
		//System.out.println(url);
		if(url==null) return null;
		ImageIcon icon = new ImageIcon(url);
		if(percentage==1.0) return icon;
		int resizedWidth = (int)(percentage*icon.getIconWidth());
		int resizedHeight = (int)(percentage*icon.getIconHeight());
		return scaleImageIcon(icon, resizedWidth, resizedHeight);
	}
	
	public static ImageIcon scaleImageIcon(ImageIcon icon, int resizedWidth, int resizedHeight) {
		Image img = icon.getImage();  
	    Image resizedImage = img.getScaledInstance(resizedWidth, resizedHeight,  java.awt.Image.SCALE_SMOOTH);  
	    return new ImageIcon(resizedImage);
	}

	public static Icon loadIconFile(String res) {
		String base = "/icon/file/";
		java.net.URL url = Resource.class.getResource(base+res);
		//System.out.println(url);
		return (url!=null)?new ImageIcon(url):null;
	}
}
