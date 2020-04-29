package com.geohammer.common.d3;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

//import com.sun.opengl.util.BufferUtil;
//import com.sun.opengl.util.j2d.TextRenderer;
//import com.jogamp.opengl.util.awt.TextRenderer;
//import com.jogamp.opengl.util.FPSAnimator; //JOGL2
import com.jogamp.opengl.util.GLBuffers; //JOGL2

import static edu.mines.jtk.ogl.Gl.*;
import edu.mines.jtk.sgl.BoundingBox;
import edu.mines.jtk.sgl.BoundingSphere;
import edu.mines.jtk.sgl.DrawContext;
import edu.mines.jtk.sgl.Group;
import edu.mines.jtk.sgl.Node;
import edu.mines.jtk.util.Direct;

/**
 * A group of connected line segments.
 * @author Dave Hale, Colorado School of Mines
 * @version 2010.01.10
 */
public class TextGroup extends Group {

	public TextGroup(ByteBuffer [] buf, float[] xyz, String [] label) {
		this(4, buf, xyz, null, label);
	}
	/**
	 * Constructs a line group with specified coordinates.
	 * <p>
	 * The (x,y,z) coordinates of points are packed into the specified 
	 * array xyz. The number of points is np = xyz.length/3.
	 * @param xyz array[3*np] of packed point coordinates.
	 */
	public TextGroup(int iAxis, ByteBuffer [] buf, float[] xyz) {
		this(iAxis, buf, xyz, null, null);
	}

	/**
	 * Constructs a line group with specified coordinates and colors.
	 * <p>
	 * The (x,y,z) coordinates of points are packed into the specified 
	 * array xyz. The number of points is np = xyz.length/3.
	 * <p>
	 * The (r,g,b) components of colors are packed into the specified 
	 * array rgb. The number of colors equals the number of points.
	 * @param xyz array[3*np] of packed point coordinates.
	 * @param rgb array[3*np] of packed color components.
	 */
	public TextGroup(int iAxis, ByteBuffer [] buf, float[] xyz, float[] rgb, String [] label) {
		this.addChild(new TextNode(xyz,rgb));
		_iAxis = iAxis;
		_buf = buf;
		_label = label;
	}

	public TextGroup() {
		this.addChild(new TextNode());
	}
	///////////////////////////////////////////////////////////////////////////
	// private
	int _iAxis = 1;
	ByteBuffer _buf[] = null;
	private String [] _label = null;
	// Constants for indexing packed arrays.
	private static final int X = 0,  Y = 1,  Z = 2;
	private static final int R = 0,  G = 1,  B = 2;

	/**
	 * A line node in this group.
	 */
	private class TextNode extends Node {
		private int fontOffset = -1;

		public TextNode() {

		}
		public TextNode(float[] xyz, float[] rgb) {
			BoundingBox bb = new BoundingBox(xyz);
			_bs = new BoundingSphere(bb);
			_np = xyz.length/3;
			int np = _np;
			int nv = np;
			int nc = np;
			_vb = Direct.newFloatBuffer(3*nv);
			_cb = (rgb!=null)?Direct.newFloatBuffer(3*nc):null;

			for (int ip=0,iv=0,ic=0; ip<np; ++ip) {
				int i = 3*ip;
				_vb.put(iv++,xyz[i+X]);
				_vb.put(iv++,xyz[i+Y]);
				_vb.put(iv++,xyz[i+Z]);
				if (_cb!=null) {
					_cb.put(ic++,rgb[i+R]);
					_cb.put(ic++,rgb[i+G]);
					_cb.put(ic++,rgb[i+B]);
				}
			}

			bufF = ByteBuffer.allocateDirect( rasterF.length );
			bufF.put( rasterF );
			bufF.rewind();
		}

		protected BoundingSphere computeBoundingSphere(boolean finite) {
			return _bs;
		}

		protected void draw(DrawContext dc) {
			if(!TextGroup.this.isVisible()) return;
			makeRasterFont();
			int np = _np;
			float r = 0   / 255f;
			float g = 255 / 255f;
			float b = 0  / 255f;
			float x = 0.0f;
			float y = 0.0f;
			float z = 0.0f;
			if(_iAxis==4) {
				for (int ip=0,iv=0,ic=0; ip<np; ++ip) {
					int i = 3*ip;
					x = _vb.get(iv++); //,xyz[i+X]);
					y = _vb.get(iv++); //,xyz[i+Y]);
					z = _vb.get(iv++); //,xyz[i+Z]);
					if (_cb!=null) {
						r = _cb.get(ic++); //,rgb[i+R]);
						g = _cb.get(ic++); //,rgb[i+G]);
						b = _cb.get(ic++); //,rgb[i+B]);
					}
					printString(_label[ip], x, y, z);
				}
			} else {
				for (int ip=0,iv=0,ic=0; ip<np; ++ip) {
					int i = 3*ip;
					x = _vb.get(iv++); //,xyz[i+X]);
					y = _vb.get(iv++); //,xyz[i+Y]);
					z = _vb.get(iv++); //,xyz[i+Z]);

					x = _vb.get(iv++); //,xyz[i+X]);
					y = _vb.get(iv++); //,xyz[i+Y]);
					z = _vb.get(iv++); //,xyz[i+Z]);
					++ip;
					if (_cb!=null) {
						r = _cb.get(ic++); //,rgb[i+R]);
						g = _cb.get(ic++); //,rgb[i+G]);
						b = _cb.get(ic++); //,rgb[i+B]);

						r = _cb.get(ic++); //,rgb[i+R]);
						g = _cb.get(ic++); //,rgb[i+G]);
						b = _cb.get(ic++); //,rgb[i+B]);
					}
					//glWindowPos2i(100, 100);
					//glRasterPos3f(x,y,z);
					//glBitmap(10, 12, 0.0f, 0.0f, 11.0f, 0.0f, bufF);
					String str;
					if(_iAxis==1) str = Integer.toString((int)(x));
					else if(_iAxis==2) str = Integer.toString((int)(y));
					else if(_iAxis==3) str = Integer.toString((int)(z));
					else  str = _label[ip];
					printString(str, x, y, z);
				}
			}


		}
		private void printString(String s, float x, float y, float z) {
			ByteBuffer str = GLBuffers.newDirectByteBuffer(s.length());
			//ByteBuffer str = BufferUtil.newByteBuffer(s.length());
			str.put(s.getBytes());
			str.rewind();

			glPushAttrib(GL_LIST_BIT);
			glListBase(fontOffset);
			glRasterPos3f(x,y,z);
			glCallLists(s.length(), GL_UNSIGNED_BYTE, str);
			glPopAttrib();
		}
		private void makeRasterFont() {
			if(fontOffset!=-1) return;
			glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

			fontOffset = glGenLists(128);
			for (int i = 32; i < 127; i++) {
				glNewList(i + fontOffset, GL_COMPILE);
				glBitmap(8, 13, 0.0f, 2.0f, 10.0f, 0.0f, _buf[i - 32] );
				glEndList();
			}
		}
		// Bitmap for the letter F.
		private byte rasterF[] = 
			{ 
				(byte) 0xc0, (byte) 0x00, (byte) 0xc0, (byte) 0x00, (byte) 0xc0,
				(byte) 0x00, (byte) 0xc0, (byte) 0x00, (byte) 0xc0, (byte) 0x00,
				(byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0xc0,
				(byte) 0x00, (byte) 0xc0, (byte) 0x00, (byte) 0xc0, (byte) 0x00,
				(byte) 0xff, (byte) 0xc0, (byte) 0xff, (byte) 0xc0 
			};
		ByteBuffer bufF;

		private BoundingSphere _bs = new BoundingSphere(0,0,5000,10000); // pre-computed bounding sphere
		private int _np; // number of points
		private FloatBuffer _vb; // vertex buffer
		private FloatBuffer _cb; // color buffer
	}
}
