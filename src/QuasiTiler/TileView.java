/*
*				TileView.java
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
import java.awt.BasicStroke;
import java.awt.RenderingHints;

public class TileView extends Canvas implements View {
    /**
     *** Constructors.
     **/

    public TileView(QuasiTiler aQuasi) {
        quasi = aQuasi;
        quasi.addView(this);
    }

    /**
     *** Modifiers.
     **/

    public void displayTiles(boolean displayed) {
        tilesDisplayed = displayed;
    }

    public void displayEdges(boolean displayed) {
        edgesDisplayed = displayed;
    }

    public void displayVertices(boolean displayed) {
        verticesDisplayed = displayed;
    }

    public void setZoom(double aZoom) {
        zoom = aZoom;
    }

    public void setEdgeWidth(double aWidth) {
        edge_width = aWidth;
    }

    /**
     *** Computers.
     **/

    public void paint(Graphics gfx) {
        if (gfx instanceof Graphics2D) {
            Graphics2D gfx2 = (Graphics2D) gfx;
            gfx2.setStroke(new BasicStroke((float) edge_width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            gfx2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        // Get local for speed.

        final Color edgeColor = quasi.getColor(-1, -1);
        final Tiling tiling = quasi.getTiling();
        final Drawing drawing = quasi.getDrawing();
        if (null == tiling || null == drawing)
            return;

        final int dim = tiling.ambient_dim;
        final int vertex_count = drawing.vertex_storage.count / dim;

        // Compute the projection of the lattice vertices.

        TilingPoint[] vertices = new TilingPoint[vertex_count];
        for (int ind = 0; ind < vertex_count; ++ind) {
            vertices[ind] = new TilingPoint();
        }
        int[] lp = drawing.vertex_storage.array;
        for (int ind = 0; ind < vertex_count; ind++) {
            drawing.lattice_to_tiling(lp, ind * dim, vertices[ind]);
        }

        Exporter.saveVertices(drawing.vertex_storage, tiling.ambient_dim);
        Exporter.saveTiles(vertices);
        Exporter.saveGenerator(tiling.generator, tiling.ambient_dim);
        Exporter.saveOffset(tiling.offset, tiling.ambient_dim);

        // Premultiply the edge generators by their correct sign.

        double[][] generator = new double[Tiling.MAX_DIM][2];
        for (int ind = 0; ind < dim; ++ind) {
            generator[ind][0] = tiling.sgn[ind] * tiling.generator[0][ind];
            generator[ind][1] = tiling.sgn[ind] * tiling.generator[1][ind];
        }

        // Display the tiles.

        if (tilesDisplayed || edgesDisplayed || verticesDisplayed) {
            int[] quad_x = new int[4];
            int[] quad_y = new int[4];
            TileList[] tile_storage = drawing.tile_storage;
            for (int comb = tiling.tile_count; --comb >= 0;) {
                final int gen0 = tiling.tile_generator[comb][0];
                final int gen1 = tiling.tile_generator[comb][1];
                final Color tileColor = quasi.getTileColor(comb);

                for (int ind = 0; ind < tile_storage[comb].count; ++ind) {
                    final int vertex_index = tile_storage[comb].array[ind];
                    quad_x[0] = (int) (zoom * (vertices[vertex_index].x));
                    quad_y[0] = (int) (zoom * (vertices[vertex_index].y));
                    quad_x[1] = (int) (zoom * (vertices[vertex_index].x + generator[gen0][0]));
                    quad_y[1] = (int) (zoom * (vertices[vertex_index].y + generator[gen0][1]));
                    quad_x[2] = (int) (zoom * (vertices[vertex_index].x + generator[gen0][0] + generator[gen1][0]));
                    quad_y[2] = (int) (zoom * (vertices[vertex_index].y + generator[gen0][1] + generator[gen1][1]));
                    quad_x[3] = (int) (zoom * (vertices[vertex_index].x + generator[gen1][0]));
                    quad_y[3] = (int) (zoom * (vertices[vertex_index].y + generator[gen1][1]));

                    if (tilesDisplayed) {
                        gfx.setColor(tileColor);
                        gfx.fillPolygon(quad_x, quad_y, 4);
                    }

                    if (edgesDisplayed) {
                        gfx.setColor(edgeColor);
                        gfx.drawPolygon(quad_x, quad_y, 4);
                    }

                    if (verticesDisplayed) {
                        gfx.setColor(Color.black);
                        gfx.drawLine(quad_x[0], quad_y[0], quad_x[0], quad_y[0]);
                    }
                }

                // Check if the user wants to stop right now

                // if ( interrupted ( ) )
                // return;
            }
        }
    }

    /**
     *** Accessors.
     **/

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getPreferredSize() {
        int x_min = 0;
        int x_max = 0;
        int y_min = 50;
        int y_max = 50;

        final Tiling tiling = quasi.getTiling();
        final Drawing drawing = quasi.getDrawing();
        if (null != tiling && null != drawing) {
            final int dim = tiling.ambient_dim;
            final int vertex_count = drawing.vertex_storage.count / dim;

            // Compute the projection of the lattice vertices.

            TilingPoint vertex = new TilingPoint();
            int[] lp = drawing.vertex_storage.array;
            for (int ind = 0; ind < vertex_count; ++ind) {
                drawing.lattice_to_tiling(lp, ind * dim, vertex);
                final int x = (int) (zoom * vertex.x);
                final int y = (int) (zoom * vertex.y);
                if (x < x_min) {
                    x_min = x;
                } else if (x > x_max) {
                    x_max = x;
                }
                if (y < y_min) {
                    y_min = y;
                } else if (y > y_max) {
                    y_max = y;
                }
            }
        }

        return new Dimension(x_max - x_min, y_max - y_min);
    }

    /**
     *** Data.
     **/

    private boolean tilesDisplayed = true;
    private boolean edgesDisplayed = true;
    private boolean verticesDisplayed = false;
    private double zoom = 30;
    private double edge_width = 2;

    private QuasiTiler quasi;
}
