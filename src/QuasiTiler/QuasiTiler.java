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
