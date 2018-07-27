/*
*				GenView.java
*
*  Copyright ( c ) 1994 by Eugenio Durand and The Geometry Center.
*  Distributed under the terms of the GNU General Public License.
*
*  Java conversion copyright ( c ) 1999 by Pierre Baillargeon.
*/

package QuasiTiler;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class GenView extends Canvas implements View, MouseListener, MouseMotionListener {
  public GenView(QuasiTiler aQuasi) {
    quasi = aQuasi;
    quasi.addView(this);
    addMouseListener(this);
    addMouseMotionListener(this);
  }

  /**
   *** Modifiers.
   **/

  public void setZoom(double aZoom) {
    zoom = aZoom;
  }

  /**
   *** Computers.
   **/

  public void paint(Graphics gfx) {
    if (gfx instanceof Graphics2D) {
      Graphics2D gfx2 = (Graphics2D) gfx;
      gfx2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    int w = gfx.getClipBounds().width;
    int h = gfx.getClipBounds().height;
    BufferedImage buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D gfx_buffer = buffer.createGraphics();
    gfx_buffer.setColor(Color.white);
    gfx_buffer.fillRect(0, 0, w, h);

    gfx_buffer.translate((int) (centerOffsetFactor * zoom), (int) (centerOffsetFactor * zoom));

    final Tiling tiling = quasi.getTiling();
    if (null == tiling)
      return;

    final int ambient_dim = tiling.ambient_dim;
    double factor = 1.0 / (double) Math.sqrt(ambient_dim / 2.0);

    // Draw circle with marks.

    gfx_buffer.setColor(Color.gray);
    {
      int corner = (int) (zoom * factor);
      int size = (int) (2 * zoom * factor);
      gfx_buffer.drawOval(-corner, -corner, size, size);
      corner = (int) (rotationThreshold * zoom);
      size = (int) (2 * rotationThreshold * zoom);
      gfx_buffer.drawOval(-corner, -corner, size, size);
    }

    for (int ind = -ambient_dim; ind < ambient_dim; ++ind) {
      final double x = (double) (zoom * factor * Math.cos(0.5 * Math.PI + ind * Math.PI / ambient_dim));
      final double y = (double) (zoom * factor * Math.sin(0.5 * Math.PI + ind * Math.PI / ambient_dim));
      gfx_buffer.drawLine((int) x, (int) y, (int) (0.9 * x), (int) (0.9 * y));
    }

    // Draw the small tile contours.

    TilingPoint[] handle = new TilingPoint[2 * Tiling.MAX_DIM];
    for (int ind = 0; ind < 2 * ambient_dim; ++ind) {
      int gen0 = ind % ambient_dim;
      int sgn = ind < ambient_dim ? +1 : -1;
      handle[ind] = new TilingPoint();
      handle[ind].x = sgn * tiling.generator[0][tiling.so[gen0]];
      handle[ind].y = sgn * tiling.generator[1][tiling.so[gen0]];
    }

    // Draw the tiles.

    gfx_buffer.setColor(Color.black);
    int[] quad_x = new int[4];
    int[] quad_y = new int[4];
    for (int gen1 = 0; gen1 < 2 * ambient_dim; ++gen1) {
      int gen0 = (gen1 == 0) ? (2 * ambient_dim - 1) : (gen1 - 1);
      quad_x[0] = (int) (zoom * (0));
      quad_y[0] = (int) (zoom * (0));
      quad_x[1] = (int) (zoom * (handle[gen0].x));
      quad_y[1] = (int) (zoom * (handle[gen0].y));
      quad_x[2] = (int) (zoom * (handle[gen0].x + handle[gen1].x));
      quad_y[2] = (int) (zoom * (handle[gen0].y + handle[gen1].y));
      quad_x[3] = (int) (zoom * (handle[gen1].x));
      quad_y[3] = (int) (zoom * (handle[gen1].y));
      gfx_buffer.drawPolygon(quad_x, quad_y, 4);
    }

    // Draw handles.

    for (int gen1 = 0; gen1 < 2 * ambient_dim; ++gen1) {
      gfx_buffer.fillOval((int) (zoom * handle[gen1].x) - 2, (int) (zoom * handle[gen1].y) - 2, 4, 4);
    }

    // Mouse dragging to change generators.

    if (capturing) {
      gfx_buffer.setColor(Color.red);
      gfx_buffer.drawLine(0, 0, (int) (zoom * phantomGenerator.x), (int) (zoom * phantomGenerator.y));
      if (rotating) {
        gfx_buffer.setColor(Color.gray);
        gfx_buffer.drawLine(0, 0, (int) (zoom * start_x), (int) (zoom * start_y));
      }
    }
    gfx.drawImage(buffer, 0, 0, null);
  }

  /**
   *** Accessors.
   **/

  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  public Dimension getPreferredSize() {
    return new Dimension((int) (rotationThreshold * 2.5 * zoom), (int) (rotationThreshold * 2.5 * zoom));
  }

  /**
   *** MouseListener implementation.
   **/

  public void mouseClicked(MouseEvent ev) {
    capturing = false;
    repaint();
  }

  public void mousePressed(MouseEvent ev) {
    final double x = transformCoord(ev.getX());
    final double y = transformCoord(ev.getY());

    if (x * x + y * y > rotationThreshold * rotationThreshold) {
      rotating = true;
      phantomGenerator.x = start_x = x;
      phantomGenerator.y = start_y = y;
      start_angle = toDegree(x, y);
    } else {
      rotating = false;

      // Find the closest handle to the mouse down event.

      double min = 0;
      int sel = 0;
      final Tiling tiling = quasi.getTiling();
      for (int ind = 0; ind < tiling.ambient_dim; ++ind) {
        // Try the positive side first
        double cx = x - tiling.generator[0][ind];
        double cy = y - tiling.generator[1][ind];
        if (min > cx * cx + cy * cy || ind < 1) {
          // Update or initialize search
          min = cx * cx + cy * cy;
          sel = ind;
        }

        // Try the negative side second
        cx = x + tiling.generator[0][ind];
        cy = y + tiling.generator[1][ind];
        if (min > cx * cx + cy * cy) {
          // Update search
          min = cx * cx + cy * cy;
          sel = ind;
        }
      }
      selected = sel;
      calcGenerator(x, y);
    }

    capturing = true;
    repaint();

  }

  public void mouseReleased(MouseEvent ev) {
    if (capturing) {
      final double x = transformCoord(ev.getX());
      final double y = transformCoord(ev.getY());

      capturing = false;

      // Change generators, and notify tiling of the change.

      final Tiling tiling = quasi.getTiling();

      if (rotating) {
        final double end_angle = toDegree(x, y);
        tiling.rotateGenerators(end_angle - start_angle);
      } else {
        if (!calcGenerator(x, y)) {
          repaint();
          return;
        }

        selected %= tiling.ambient_dim;
        if (phantomGenerator.x < 0) {
          phantomGenerator.x *= -1;
          phantomGenerator.y *= -1;
        }
        tiling.setGenerator(phantomGenerator, selected);
      }
      quasi.recalc();
      quasi.repaint();
    }
  }

  public void mouseEntered(MouseEvent ev) {
  }

  public void mouseExited(MouseEvent ev) {
  }

  /**
   *** MouseMotionListener implementation.
   **/

  public void mouseDragged(MouseEvent ev) {
    if (capturing) {
      if (rotating) {
        phantomGenerator.x = transformCoord(ev.getX());
        phantomGenerator.y = transformCoord(ev.getY());
      } else {
        calcGenerator(transformCoord(ev.getX()), transformCoord(ev.getY()));
      }
      repaint();
    }
  }

  public void mouseMoved(MouseEvent ev) {
    if (capturing) {
      capturing = false;
      repaint();
    }
  }

  /**
   *** Private computers.
   **/

  private double transformCoord(double c) {
    return c / zoom - centerOffsetFactor;
  }

  private boolean calcGenerator(double x, double y) {
    // Constrains the generator to the maximum radius.

    double norm = (double) Math.sqrt(x * x + y * y);

    if (norm > rotationThreshold) {
      phantomGenerator.x = rotationThreshold * x / norm;
      phantomGenerator.y = rotationThreshold * y / norm;
    } else {
      phantomGenerator.x = x;
      phantomGenerator.y = y;
    }

    // Checks that the user is not dragging over the view origin.

    return norm >= 0.05;
  }

  private static double toDegree(double x, double y) {
    final double norm = Math.sqrt(x * x + y * y);
    return (double) (y >= 0 ? Math.acos(x / norm) : 2 * Math.PI - Math.acos(x / norm));
  }

  /**
   *** Data.
   **/

  private static final double rotationThreshold = 1.5;
  private static final double centerOffsetFactor = rotationThreshold + 0.1;

  private QuasiTiler quasi;
  private double zoom = 30;

  private boolean capturing;
  private boolean rotating;
  private int selected;
  private double start_angle;
  private double start_x;
  private double start_y;
  private TilingPoint phantomGenerator = new TilingPoint();
}
