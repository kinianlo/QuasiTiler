/*
*				OffsetView.java
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
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class OffsetView extends Canvas implements View {
      /**
       *** Constructors.
       **/

      public OffsetView(QuasiTiler aQuasi) {
            quasi = aQuasi;
            quasi.addView(this);
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

      private static final double OFFSETVIEW_RADIUS = 1.6;

      public void paint(Graphics gfx) {
            if (gfx instanceof Graphics2D) {
                  Graphics2D gfx2 = (Graphics2D) gfx;
                  gfx2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }

            gfx.translate((int) (1.5 * zoom), (int) (1.5 * zoom));

            final Tiling tiling = quasi.getTiling();
            final Drawing drawing = quasi.getDrawing();
            if (null == tiling || null == drawing)
                  return;

            final double[] relative_offset = tiling.offset;

            // If the dimension is 3, there is not much to do
            double[] offset_coords = { 0.0, 0.0 };
            offset_coords[1] = relative_offset[0];
            // if ( tiling.ambient_dim > 3 )
            {
                  offset_coords[0] = relative_offset[1];
                  // Allocate memory for the array of vertices
                  int vertex_count = 1 << tiling.ambient_dim;
                  double[][] cell_vertex = new double[vertex_count][2];

                  // Compute the vertices of the cell projection
                  for (int ind = 0; ind < vertex_count; ind++) {
                        double dot_0 = 0.0;
                        double dot_1 = 0.0;
                        for (int ind1 = 0, mask = 1; ind1 < tiling.ambient_dim; ind1++, mask <<= 1) {
                              double vertex_coord = (0 != (ind & mask) ? 0.5 : -0.5);
                              dot_0 += vertex_coord * tiling.generator[Tiling.TARGET_DIM + 1][ind1];
                              dot_1 += vertex_coord * tiling.generator[Tiling.TARGET_DIM][ind1];
                        }
                        cell_vertex[ind][0] = dot_0;
                        cell_vertex[ind][1] = dot_1;
                  }

                  // Draw the edges of the projected cell.

                  int edge_count = tiling.ambient_dim * (1 << (tiling.ambient_dim - 1));

                  // Generate all the edges.

                  gfx.setColor(Color.gray);
                  for (int ind = 0; ind < vertex_count; ind++)
                        for (int ind1 = 0, mask = 1; ind1 < tiling.ambient_dim; ind1++, mask <<= 1)
                              if (0 == (ind & mask)) {
                                    gfx.drawLine((int) (zoom * cell_vertex[ind][0]), (int) (zoom * cell_vertex[ind][1]),
                                                (int) (zoom * cell_vertex[ind | mask][0]),
                                                (int) (zoom * cell_vertex[ind | mask][1]));
                              }

                  // Draw the projections of the tiling vertices.
                  // Make the points smaller as their density increases.

                  int cnt = drawing.vertex_storage.count / tiling.ambient_dim;
                  if (cnt == 0)
                        return;

                  gfx.setColor(Color.black);
                  double avg_density = cnt / (double) (OFFSETVIEW_RADIUS * OFFSETVIEW_RADIUS);
                  // if ( avg_density == 0) PSsetlinewidth(0.075);
                  // else PSsetlinewidth(sqrt(1/avg_density));

                  TilingPoint op = new TilingPoint();
                  int[] lp = drawing.vertex_storage.array;
                  for (int ind = 0; ind < cnt; ++ind) {
                        drawing.lattice_to_orthogonal(lp, ind * tiling.ambient_dim, op);
                        gfx.drawLine((int) op.x, (int) op.y, (int) op.x, (int) op.y);
                  }
            }

            // Draw a point at the value of the offset.

            gfx.setColor(Color.red);
            gfx.fillOval((int) (offset_coords[0] - 2), (int) (offset_coords[1] - 2), 4, 4);
      }

      /**
       *** Accessors.
       **/

      public Dimension getMinimumSize() {
            return getPreferredSize();
      }

      public Dimension getPreferredSize() {
            return new Dimension((int) (3 * zoom), (int) (3 * zoom));
      }

      /**
       *** Data.
       **/

      private QuasiTiler quasi;
      private double zoom = 30;
}
