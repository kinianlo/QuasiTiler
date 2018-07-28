/*
*				QuasiTiler.java
*
*  Copyright ( c ) 1994 by Eugenio Durand and The Geometry Center.
*  Distributed under the terms of the GNU General Public License.
*
*  Java conversion copyright ( c ) 1999 by Pierre Baillargeon.
*/

package QuasiTiler;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Color;
import java.io.IOException;

public class QuasiTiler implements View {
    /**
     *** Constructor.
     **/

    public QuasiTiler() {
    }

    /**
     *** Modifiers.
     **/

    public void setDimension(int aDim) {
        quasi_dim = aDim;
    }

    public void setBounds(double[][] someBounds) {
        for (int i = someBounds.length; --i >= 0;)
            for (int j = someBounds[i].length; --j >= 0;)
                quasi_bound[i][j] = someBounds[i][j];
    }

    public void setBound(double aBound, int i, int j) {
        quasi_bound[i][j] = aBound;
    }

    public void setOffsets(double[] someOffsets) {
        for (int i = someOffsets.length; --i >= 0;)
            quasi_offset[i] = someOffsets[i];
    }

    public void setOffset(double anOffset, int i) {
        quasi_offset[i] = anOffset;
    }

    public void addView(View aView) {
        views.addElement(aView);
    }

    public void removeView(View aView) {
        views.removeElement(aView);
    }

    public void setZoom(double aZoom) {
        // Not relevant (or zoom all views to same value?).
    }

    public void setInterruptor(Interruptor anInt) {
        interruptor = anInt;
    }

    /**
     *** Computers.
     **/

    public void recalc() {
        drawing = null;

        if (null == tiling || quasi_dim != tiling.ambient_dim) {
            tiling = null;

            System.gc();
            System.runFinalization();
            System.gc();
            System.runFinalization();

            tiling = new Tiling(quasi_dim);
            tiling.init_default(quasi_dim);
        }

        tiling.init(quasi_offset);

        drawing = new Drawing(tiling);

        tiling.generate(quasi_bound, drawing, interruptor);
        drawing.locate_tiles(interruptor);

    }

    public void repaint() {
        for (int i = 0; i < views.size(); ++i) {
            ((View) views.elementAt(i)).repaint();
        }
    }

    /**
     *** Accessors.
     **/

    public double[][] getTiles() {
        if (null == tiling || null == drawing)
            return null;

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

        // Premultiply the edge generators by their correct sign.

        double[][] generator = new double[Tiling.MAX_DIM][2];
        for (int ind = 0; ind < dim; ++ind) {
            generator[ind][0] = tiling.sgn[ind] * tiling.generator[0][ind];
            generator[ind][1] = tiling.sgn[ind] * tiling.generator[1][ind];
        }

        // Display the tiles.
        TileList[] tile_storage = drawing.tile_storage;
        int num_tiles = 0;
        for (int comb = tiling.tile_count; --comb >= 0;) {
            for (int ind = 0; ind < tile_storage[comb].count; ++ind) {
                num_tiles++;
            }
        }

        double[][] output = new double[num_tiles][3];

        int i = 0;
        for (int comb = tiling.tile_count; --comb >= 0;) {
            final int gen0 = tiling.tile_generator[comb][0];
            final int gen1 = tiling.tile_generator[comb][1];

            for (int ind = 0; ind < tile_storage[comb].count; ++ind) {
                final int vertex_index = tile_storage[comb].array[ind];

                double centroid_x = vertices[vertex_index].x + (generator[gen0][0] + generator[gen1][0]) / 2.0;
                double centroid_y = vertices[vertex_index].y + (generator[gen0][1] + generator[gen1][1]) / 2.0;
                output[i][0] = (double) comb;
                output[i][1] = centroid_x;
                output[i][2] = centroid_y;
                i++;
            }
        }
        return output;
    }

    public int getDimension() {
        return quasi_dim;
    }

    public double getBound(int i, int j) {
        return quasi_bound[i][j];
    }

    public double getOffset(int i) {
        return quasi_offset[i];
    }

    private void createColorTable() {
        if (null == color_table) {
            // Init color table.
            color_table = new Color[Tiling.MAX_DIM / 2][2];
            for (int row = 0; row < Tiling.MAX_DIM / 2; ++row) {
                int v = 255 - row * 15;
                switch (row % 3) {
                case 0:
                    color_table[row][0] = new Color(v, 80, 80);
                    color_table[row][1] = new Color(80, 80, v);
                    break;
                case 1:
                    color_table[row][0] = new Color(80, v, 80);
                    color_table[row][1] = new Color(80, v, v);
                    break;
                case 2:
                    color_table[row][0] = new Color(v, 80, v);
                    color_table[row][1] = new Color(v, v, 80);
                    break;
                }
            }
        }
    }

    public Color getColor(int index1, int index2) {
        if (index1 < 0) {
            return edge_color;
        }

        createColorTable();
        if (index1 < color_table.length && index2 < color_table[index1].length) {
            return color_table[index1][index2];
        }

        return new Color(0, 0, 0);
    }

    public void setColor(int index1, int index2, Color aColor) {
        if (index1 < 0) {
            edge_color = aColor;
            return;
        }

        createColorTable();
        if (index1 < color_table.length && index2 < color_table[index1].length) {
            color_table[index1][index2] = aColor;
        }
    }

    public Color getTileColor(int tile_index) {
        createColorTable();
        int row = tile_index / tiling.ambient_dim;
        int col = tile_index % tiling.ambient_dim;

        int row_count;
        if (row >= tiling.ambient_dim / 2 - 1 && tiling.ambient_dim % 2 == 0) {
            row_count = tiling.ambient_dim / 2 - 1;
        } else {
            row_count = tiling.ambient_dim - 1;
        }
        // Interpolate color.

        double cof = (double) col / (double) row_count;

        Color leftColor = color_table[row][0];
        Color rightColor = color_table[row][1];
        return new Color((int) ((1.0 - cof) * leftColor.getRed() + cof * rightColor.getRed()),
                (int) ((1.0 - cof) * leftColor.getGreen() + cof * rightColor.getGreen()),
                (int) ((1.0 - cof) * leftColor.getBlue() + cof * rightColor.getBlue()));

    }

    public Tiling getTiling() {
        return tiling;
    }

    public Drawing getDrawing() {
        return drawing;
    }

    /**
     *** Data.
     **/

    private Color[][] color_table;
    private Color edge_color = Color.gray;
    private Tiling tiling;
    private Drawing drawing;
    private Interruptor interruptor;

    private int quasi_dim;
    private double[][] quasi_bound = new double[2][Tiling.MAX_DIM];
    private double[] quasi_offset = new double[Tiling.MAX_DIM];

    /**
     *** Views.
     **/

    private java.util.Vector<View> views = new java.util.Vector<View>();
}
