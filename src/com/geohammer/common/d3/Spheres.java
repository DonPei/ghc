package com.geohammer.common.d3;

import static edu.mines.jtk.ogl.Gl.GL_AMBIENT_AND_DIFFUSE;
import static edu.mines.jtk.ogl.Gl.GL_COLOR_ARRAY;
import static edu.mines.jtk.ogl.Gl.GL_COMPILE;
import static edu.mines.jtk.ogl.Gl.GL_FILL;
import static edu.mines.jtk.ogl.Gl.GL_FLOAT;
import static edu.mines.jtk.ogl.Gl.GL_FRONT_AND_BACK;
import static edu.mines.jtk.ogl.Gl.GL_LIGHTING;
import static edu.mines.jtk.ogl.Gl.GL_LINE;
import static edu.mines.jtk.ogl.Gl.GL_NORMAL_ARRAY;
import static edu.mines.jtk.ogl.Gl.GL_POLYGON_OFFSET_FILL;
import static edu.mines.jtk.ogl.Gl.GL_QUADS;
import static edu.mines.jtk.ogl.Gl.GL_TRIANGLES;
import static edu.mines.jtk.ogl.Gl.GL_VERTEX_ARRAY;
import static edu.mines.jtk.ogl.Gl.glCallList;
import static edu.mines.jtk.ogl.Gl.glColor3d;
import static edu.mines.jtk.ogl.Gl.glColorPointer;
import static edu.mines.jtk.ogl.Gl.glDisable;
import static edu.mines.jtk.ogl.Gl.glDisableClientState;
import static edu.mines.jtk.ogl.Gl.glDrawArrays;
import static edu.mines.jtk.ogl.Gl.glEnable;
import static edu.mines.jtk.ogl.Gl.glEnableClientState;
import static edu.mines.jtk.ogl.Gl.glEndList;
import static edu.mines.jtk.ogl.Gl.glMultMatrixf;
import static edu.mines.jtk.ogl.Gl.glNewList;
import static edu.mines.jtk.ogl.Gl.glNormalPointer;
import static edu.mines.jtk.ogl.Gl.glPolygonMode;
import static edu.mines.jtk.ogl.Gl.glPolygonOffset;
import static edu.mines.jtk.ogl.Gl.glPopMatrix;
import static edu.mines.jtk.ogl.Gl.glPushMatrix;
import static edu.mines.jtk.ogl.Gl.glScalef;
import static edu.mines.jtk.ogl.Gl.glTranslatef;
import static edu.mines.jtk.ogl.Gl.glVertexPointer;
import static edu.mines.jtk.util.ArrayMath.sqrt;

import java.awt.Color;
import java.nio.FloatBuffer;

import edu.mines.jtk.ogl.GlDisplayList;
import edu.mines.jtk.sgl.BoundingBox;
import edu.mines.jtk.sgl.BoundingSphere;
import edu.mines.jtk.sgl.ColorState;
import edu.mines.jtk.sgl.DrawContext;
import edu.mines.jtk.sgl.Group;
import edu.mines.jtk.sgl.LightModelState;
import edu.mines.jtk.sgl.MaterialState;
import edu.mines.jtk.sgl.Node;
import edu.mines.jtk.sgl.PickContext;
import edu.mines.jtk.sgl.Point3;
import edu.mines.jtk.sgl.Segment;
import edu.mines.jtk.sgl.Selectable;
import edu.mines.jtk.sgl.StateSet;
import edu.mines.jtk.util.ArrayMath;
import edu.mines.jtk.util.Direct;

public class Spheres extends Group implements Selectable{
	
	public Spheres(float [] cx, float [] cy, float [] cz, float [] r) {
		this(4, cx, cy, cz, r);
	}
	public Spheres(int m, float [] cx, float [] cy, float [] cz, float [] r) {
		this(4, cx, cy, cz, r, true);
	}
	
	public Spheres(int m, float [] cx, float [] cy, float [] cz, float [] r, boolean wire) {
		setSelected(wire);
		_cx = cx;
		_cy = cy;
		_cz = cz;
		_r = r;
		
		makeTransformMatrix();
		makeUnitSphere(m);
		setDefaultStates();
		addChild(new SphereNode());
	}
	
	
	/**
	 * Returns the number of vertices used to approximate this glyph.
	 * @return the number of vertices.
	 */
	public int countVertices() {
		return _nv;
	}

	/**
	 * Gets the vertices of the unit sphere used to approximate this glyph.
	 * @return array of packed (x,y,z) coordinates of vertices; by reference,
	 *  not by copy. The array length is 3 times the number of vertices.
	 */
	public float[] getVertices() {
		return _xyz;
	}

	protected void selectedChanged() {
	    //System.out.println("TriangleGroup: "+this+" selected="+isSelected());
	    dirtyDraw();
	  }
	
	/**
	 * Draws a unit sphere centered at the origin.
	 */
	public void draw() {
		boolean selected = isSelected();
        if(!isVisible()) return;        
        
		if (_displayList==null) {
			FloatBuffer xyz = Direct.newFloatBuffer(3*_nv);
			xyz.put(_xyz); xyz.rewind();
			_displayList = new GlDisplayList();
			glEnableClientState(GL_VERTEX_ARRAY);
			glEnableClientState(GL_NORMAL_ARRAY);
			glNewList(_displayList.list(),GL_COMPILE);
			glVertexPointer(3,GL_FLOAT,0,xyz);
			glNormalPointer(GL_FLOAT,0,xyz);
			if (selected) {
				glPolygonMode(GL_FRONT_AND_BACK,GL_LINE);
				glDisable(GL_LIGHTING);
				glColor3d(1.0,1.0,1.0);
				glDrawArrays(GL_TRIANGLES,0,_nv);
			} else {
				glPolygonMode(GL_FRONT_AND_BACK,GL_FILL);
				glDrawArrays(GL_TRIANGLES,0,_nv);
			}
			glEndList();
			glDisableClientState(GL_NORMAL_ARRAY);
			glDisableClientState(GL_VERTEX_ARRAY);
		}
		glCallList(_displayList.list());
	}

	/**
	 * Draws a sphere centered at a specified point with specified radius.
	 * @param cx x coordinate of the center point.
	 * @param cy y coordinate of the center point.
	 * @param cz z coordinate of the center point.
	 * @param r radius of the sphere.
	 */
	public void draw(float [] cx, float [] cy, float [] cz, float [] r) {
		for(int i=0; i<cx.length; i++) draw(cx[i],cy[i],cz[i],r[i],r[i],r[i]);
	}

	/**
	 * Draws an axis-aligned ellipsoid centered at a specified point.
	 * The lengths of the specified semi-principal axes must be positive.
	 * @param cx x coordinate of the center point.
	 * @param cy y coordinate of the center point.
	 * @param cz z coordinate of the center point.
	 * @param dx semi-principal length in direction of x axis.
	 * @param dy semi-principal length in direction of y axis.
	 * @param dz semi-principal length in direction of z axis.
	 */
	public void draw(
			float cx, float cy, float cz,
			float dx, float dy, float dz)
	{
		glPushMatrix();
		glTranslatef(cx,cy,cz);
		glScalef(dx,dy,dz);
		draw();
		glPopMatrix();
	}

	/**
	 * Draws an arbitrary ellipsoid centered at a specified point.
	 * The semi-principal axes of the ellipsoid are represented by three 
	 * vectors u, v, and w. The lengths of these three vectors are the 
	 * semi-principal lengths of the ellipsoid, and must be non-zero.
	 * @param cx x coordinate of the center point.
	 * @param cy y coordinate of the center point.
	 * @param cz z coordinate of the center point.
	 * @param ux x component of vector u.
	 * @param uy y component of vector u.
	 * @param uz z component of vector u.
	 * @param vx x component of vector v.
	 * @param vy y component of vector v.
	 * @param vz z component of vector v.
	 * @param wx x component of vector w.
	 * @param wy y component of vector w.
	 * @param wz z component of vector w.
	 */
	public void draw(
			float cx, float cy, float cz,
			float ux, float uy, float uz,
			float vx, float vy, float vz,
			float wx, float wy, float wz)
	{
		// Ensure vectors u, v, and w form a right-handed coordinate system.
		// This is necessary to keep triangle vertices in counter-clockwise
		// order as viewed from outside the ellipsoid.
		if (ux*(vy*wz-vz*wy)+uy*(vz*wx-vx*wz)+uz*(vx*wy-vy*wx)<0.0) {
			ux = -ux; uy = -uy; uz = -uz;
			vx = -vx; vy = -vy; vz = -vz;
			wx = -wx; wy = -wy; wz = -wz;
		}

		// The transformation matrix.
		_m[ 0] = ux; _m[ 4] = vx; _m[ 8] = wx; _m[12] = cx;
		_m[ 1] = uy; _m[ 5] = vy; _m[ 9] = wy; _m[13] = cy;
		_m[ 2] = uz; _m[ 6] = vz; _m[10] = wz; _m[14] = cz;

		// Draw the transformed unit sphere.
		glPushMatrix();
		glMultMatrixf(_m,0);
		draw();
		glPopMatrix();
	}
	public BoundingSphere computeBoundingSphere(boolean finite) {
		float minx = ArrayMath.min(_cx);
		float miny = ArrayMath.min(_cy);
		float minz = ArrayMath.min(_cz);
		float maxx = ArrayMath.max(_cx);
		float maxy = ArrayMath.max(_cy);
		float maxz = ArrayMath.max(_cz);

		float minr = ArrayMath.min(_r);
		float maxr = ArrayMath.max(_r);
		
		return new BoundingSphere(new BoundingBox(minx-minr, miny-minr, minz-minr, maxx+maxr, maxy+maxr, maxz+maxr));
	}

	///////////////////////////////////////////////////////////////////////////
	// private
	private float [] _cx = null;
	private float [] _cy = null;
	private float [] _cz = null;
	private float [] _r = null;
	
//	private float _cx = 0;
//	private float _cy = 0;
//	private float _cz = 0;
//	private float _r = 0;
	
	private float[] _m; // transform matrix used when drawing
	private int _nv; // number of vertices for unit sphere
	private float[] _xyz; // vertices on the unit sphere
	private GlDisplayList _displayList; // draws unit sphere when called

	private void makeTransformMatrix() {
		_m = new float[16];
		_m[15] = 1.0f;
	}

	private void makeUnitSphere(int m) {

		// Buffers for vertices of triangles used to approximate the unit sphere. 
		// The initial octahedron has 8 triangular faces, each with 3 vertices. 
		// Each subdivision increases the number of triangles by a factor of 4.
		_nv = 8*3;
		for (int i=0; i<m; ++i)
			_nv *= 4;
		int n = _nv*3;
		_xyz = new float[n];

		// Compute vertices and unit normal vectors for the ellipsoid by 
		// recursively subdividing the eight triangular faces of the 
		// octahedron. The order of the three vertices in each triangle is 
		// counter-clockwise as viewed from outside the ellipsoid.
		float xm = -1.0f, x0 = 0.0f, xp = 1.0f;
		float ym = -1.0f, y0 = 0.0f, yp = 1.0f;
		float zm = -1.0f, z0 = 0.0f, zp = 1.0f;
		n = 0;
		n = addTri(xp,y0,z0,x0,yp,z0,x0,y0,zp,m,n);
		n = addTri(xm,y0,z0,x0,y0,zp,x0,yp,z0,m,n);
		n = addTri(xp,y0,z0,x0,y0,zp,x0,ym,z0,m,n);
		n = addTri(xm,y0,z0,x0,ym,z0,x0,y0,zp,m,n);
		n = addTri(xp,y0,z0,x0,y0,zm,x0,yp,z0,m,n);
		n = addTri(xm,y0,z0,x0,yp,z0,x0,y0,zm,m,n);
		n = addTri(xp,y0,z0,x0,ym,z0,x0,y0,zm,m,n);
		n = addTri(xm,y0,z0,x0,y0,zm,x0,ym,z0,m,n);
	}
	
	//http://www.glprogramming.com/red/chapter02.html#name8
	private int addTri(
			float xa, float ya, float za,
			float xb, float yb, float zb,
			float xc, float yc, float zc,
			int m, int n)
	{
		// If no longer subdividing, ...
		if (m==0) {

			// Append coordinates of vertices a, b, c of triangle abc.
			_xyz[n++] = xa; _xyz[n++] = ya; _xyz[n++] = za;
			_xyz[n++] = xb; _xyz[n++] = yb; _xyz[n++] = zb;
			_xyz[n++] = xc; _xyz[n++] = yc; _xyz[n++] = zc;
		} 

		// Else, if subdividing, ...
		else {

			// New vertices at midpoints ab, bc, and ca of triangle edges.
			float xab = 0.5f*(xa+xb), yab = 0.5f*(ya+yb), zab = 0.5f*(za+zb);
			float xbc = 0.5f*(xb+xc), ybc = 0.5f*(yb+yc), zbc = 0.5f*(zb+zc);
			float xca = 0.5f*(xc+xa), yca = 0.5f*(yc+ya), zca = 0.5f*(zc+za);

			// Distances from new vertices to origin.
			float dab = sqrt(xab*xab+yab*yab+zab*zab);
			float dbc = sqrt(xbc*xbc+ybc*ybc+zbc*zbc);
			float dca = sqrt(xca*xca+yca*yca+zca*zca);

			// Scale new vertices to put them on the sphere.
			float sab = 1.0f/dab;
			float sbc = 1.0f/dbc;
			float sca = 1.0f/dca;
			xab *= sab; yab *= sab; zab *= sab;
			xbc *= sbc; ybc *= sbc; zbc *= sbc;
			xca *= sca; yca *= sca; zca *= sca;

			// Recursively subdivide triangle abc into four triangles.
			m -= 1;
			n = addTri( xa, ya, za,xab,yab,zab,xca,yca,zca,m,n);
			n = addTri( xb, yb, zb,xbc,ybc,zbc,xab,yab,zab,m,n);
			n = addTri( xc, yc, zc,xca,yca,zca,xbc,ybc,zbc,m,n);
			n = addTri(xab,yab,zab,xbc,ybc,zbc,xca,yca,zca,m,n);
		}

		return n;
	}
	private class SphereNode extends Node {

	    public SphereNode() {
	    }

	    protected BoundingSphere computeBoundingSphere(boolean finite) {
	      return Spheres.this.computeBoundingSphere(finite);
	    }

	    protected void draw(DrawContext dc) {
	      if(!Spheres.this.isVisible()) return;
	      Spheres.this.draw(_cx,_cy,_cz,_r);
	    }

//		public void pick(PickContext pc) {
//			Segment ps = pc.getPickSegment();
//			int nTriangles = _nv/3;
//			for (int iq=0,j=0; iq<nTriangles; ++iq) {
//				double xi = _xyz[j++];
//				double yi = _xyz[j++];
//				double zi = _xyz[j++];
//				double xj = _xyz[j++];
//				double yj = _xyz[j++];
//				double zj = _xyz[j++];
//				double xk = _xyz[j++];
//				double yk = _xyz[j++];
//				double zk = _xyz[j++];
////				double xl = _xyz[j++];
////				double yl = _xyz[j++];
////				double zl = _xyz[j++];
//				Point3 p = ps.intersectWithTriangle(xi,yi,zi,xj,yj,zj,xk,yk,zk);
//				//if (p==null)
//					//p = ps.intersectWithTriangle(xk,yk,zk,xl,yl,zl,xi,yi,zi);
//				if (p!=null)
//					pc.addResult(p);
//			}
//		}
	}
	
	private static StateSet defaultStateSet(Color color) {
		StateSet states = new StateSet();
		ColorState cs = new ColorState();
		cs.setColor(color);
		LightModelState lms = new LightModelState();
		lms.setTwoSide(true);
		MaterialState ms = new MaterialState();
		ms.setColorMaterial(GL_AMBIENT_AND_DIFFUSE);
		ms.setSpecular(Color.WHITE);
		ms.setShininess(100.0f);
		states.add(cs);
		states.add(lms);
		states.add(ms);
		return states;
	}

	private void setDefaultStates() {
		setStates(defaultStateSet(Color.LIGHT_GRAY));
	}
	
}

