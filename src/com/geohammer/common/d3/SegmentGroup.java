package com.geohammer.common.d3;

import java.nio.FloatBuffer;

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
public class SegmentGroup extends Group {

  /**
   * Constructs a line group with specified coordinates.
   * <p>
   * The (x,y,z) coordinates of points are packed into the specified 
   * array xyz. The number of points is np = xyz.length/3.
   * @param xyz array[3*np] of packed point coordinates.
   */
  public SegmentGroup(float[] xyz) {
    this(xyz,null);
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
  public SegmentGroup(float[] xyz, float[] rgb) {
    this.addChild(new SegmentNode(xyz,rgb));
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  // Constants for indexing packed arrays.
  private static final int X = 0,  Y = 1,  Z = 2;
  private static final int R = 0,  G = 1,  B = 2;

  /**
   * A line node in this group.
   */
  private class SegmentNode extends Node {

    public SegmentNode(float[] xyz, float[] rgb) {
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
    }

    protected BoundingSphere computeBoundingSphere(boolean finite) {
      return _bs;
    }

    protected void draw(DrawContext dc) {
    	if(!SegmentGroup.this.isVisible()) return;
      glEnableClientState(GL_VERTEX_ARRAY);
      glVertexPointer(3,GL_FLOAT,0,_vb);
      if (_cb!=null) {
        glEnableClientState(GL_COLOR_ARRAY);
        glColorPointer(3,GL_FLOAT,0,_cb);
      }
      glDrawArrays(GL_LINES,0,_np);
      if (_cb!=null)
        glDisableClientState(GL_COLOR_ARRAY);
      glDisableClientState(GL_VERTEX_ARRAY);
    }
    
    private BoundingSphere _bs; // pre-computed bounding sphere
    private int _np; // number of points
    private FloatBuffer _vb; // vertex buffer
    private FloatBuffer _cb; // color buffer
  }
}
