/*
*				ColorView.java
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

public class ColorView extends Canvas implements View {
    /**
     *** Constructors.
     **/

    public ColorView(QuasiTiler aQuasi) {
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

    private static final double TILE_RAD = 0.75;

    public void paint(Graphics gfx) {
        final Tiling tiling = quasi.getTiling();
        if (null == tiling)
            return;

        final int[] quad_x = new int[4];
        final int[] quad_y = new int[4];

        for (int ind = 0; ind < tiling.tile_count; ++ind) {
            final int row = ind / tiling.ambient_dim;
            final int col = ind % tiling.ambient_dim;

            // Top row in even dimension has half as many tiles.

            final double col_adj;

            if (row >= tiling.ambient_dim / 2 - 1 && tiling.ambient_dim % 2 == 0) {
                col_adj = 1.0 + (double) tiling.ambient_dim / ((double) tiling.ambient_dim - 2.0);
            } else {
                col_adj = 1.0;
            }

            final int gen0 = tiling.tile_generator[ind][0];
            final int gen1 = tiling.tile_generator[ind][1];

            final double x = col * col_adj + 0.5
                    - TILE_RAD * (tiling.generator[0][gen0] + tiling.generator[0][gen1]) / 2.0;
            final double y = row + 0.5 - TILE_RAD * (tiling.generator[1][gen0] + tiling.generator[1][gen1]) / 2.0;

            quad_x[0] = (int) (zoom + zoom * (x));
            quad_y[0] = (int) (zoom + zoom * (y));
            quad_x[1] = (int) (zoom + zoom * (x + TILE_RAD * tiling.generator[0][gen0]));
            quad_y[1] = (int) (zoom + zoom * (y + TILE_RAD * tiling.generator[1][gen0]));
            quad_x[2] = (int) (zoom
                    + zoom * (x + TILE_RAD * tiling.generator[0][gen0] + TILE_RAD * tiling.generator[0][gen1]));
            quad_y[2] = (int) (zoom
                    + zoom * (y + TILE_RAD * tiling.generator[1][gen0] + TILE_RAD * tiling.generator[1][gen1]));
            quad_x[3] = (int) (zoom + zoom * (x + TILE_RAD * tiling.generator[0][gen1]));
            quad_y[3] = (int) (zoom + zoom * (y + TILE_RAD * tiling.generator[1][gen1]));

            gfx.setColor(quasi.getTileColor(row * col + col));
            gfx.fillPolygon(quad_x, quad_y, 4);
        }
    }

    /**
     *** Accessors.
     **/

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getPreferredSize() {
        final Tiling tiling = quasi.getTiling();
        if (null != tiling) {
            final int row = tiling.tile_count / tiling.ambient_dim;
            final int col = tiling.ambient_dim;
            return new Dimension((int) ((col + 2) * zoom), (int) ((row + 2) * zoom));
        } else {
            return new Dimension(50, 50);
        }
    }

    /**
     *** Data.
     **/

    private QuasiTiler quasi;
    private double zoom = 30;
}
