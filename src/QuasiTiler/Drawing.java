/*
*				Drawing.java
*
*  Copyright ( c ) 1994 by Eugenio Durand and The Geometry Center.
*  Distributed under the terms of the GNU General Public License.
*
*  Java conversion copyright ( c ) 1999 by Pierre Baillargeon.
*/

package QuasiTiler;

import java.io.IOException;

public class Drawing implements Reporter, Comparator {
    Tiling theTiling;
    VertexList vertex_storage = new VertexList();
    TileList[] tile_storage = new TileList[Tiling.MAX_TILE_COMB];

    public Drawing(Tiling tiling) {
        theTiling = tiling;
        for (int ind = 0; ind < Tiling.MAX_TILE_COMB; ++ind) {
            tile_storage[ind] = new TileList();
        }
    }

    public void report_point(Tiling theTiling, int[] point) {
        vertex_storage.store(theTiling, point);
    }

    /*
     * compare ( ) is used by qsort ( ) and bsearch ( ).
     */

    public int compare(int[] x, int ix, int[] y, int iy) {
        int i = 0;
        while (x[ix + i] == y[iy + i] && i < theTiling.ambient_dim)
            ++i;
        return (i == theTiling.ambient_dim) ? 0 : x[ix + i] - y[iy + i];
    }

    /*
     * locate_tiles ( ) goes over each vertex in vertex_storage, and finds
     * neighboring vertices also in the list. This determines the tiles. The
     * coordinates of the tiles are stored in tile_storage.
     *
     * call_back ( ) is called periodically ( if it isn't null ) to provide a way of
     * stoping the computation; should return nonzero for the computation to stop.
     *
     * locate_tiles ( ) returns false if it cannot finish the computation for any
     * reason.
     */
    public boolean locate_tiles(Interruptor interruptor) {
        /* Set static variable ambient_dim */
        int vertex_count = vertex_storage.count / theTiling.ambient_dim;

        /* Sort the array of vertices */
        Sorter.qsort(vertex_storage.array, vertex_count, theTiling.ambient_dim, this);

        /*
         * Go over each vertex and find its neighbors; form the list of tiles
         * accordingly
         */
        int[] neighbor = new int[Tiling.MAX_DIM];
        for (int vertex_index = 0, vertex_array_index = 0; vertex_index < vertex_count; vertex_index++, vertex_array_index += theTiling.ambient_dim) {
            /* Initialize the auxiliary variable */
            for (int ind = 0; ind < theTiling.ambient_dim; ++ind)
                neighbor[ind] = vertex_storage.array[vertex_array_index + ind];

            /* Initialize the tile search loop */
            int gen0 = -1;

            /* Check all the neighbors in each direction */
            for (int ind = 0; ind < theTiling.ambient_dim; ++ind) {
                /* Compute the next neighbor */
                int gen1 = theTiling.so[ind];
                neighbor[gen1] += theTiling.sgn[gen1];

                /* Is the neighbor in the tiling? */
                /*
                 * Notice that I only need to know yes or no, I don't care for its position in
                 * the array
                 */
                if (Sorter.bsearch(neighbor, vertex_storage.array, vertex_count, theTiling.ambient_dim, this)) {
                    if (gen0 >= 0)
                        /*
                         * We have a new tile, so store in the appropiate array; we could instead draw
                         * the tile at this point
                         */
                        tile_storage[theTiling.tile_index[gen0][gen1]].store(vertex_index);
                    gen0 = gen1;
                }

                /* Get ready for the next nghb */
                neighbor[gen1] = vertex_storage.array[vertex_array_index + gen1];
            }
            /* Check if the user wants to stop right now */
            if (0 == (vertex_index % 100) && null != interruptor && interruptor.interrupted())
                return false;
        }

        return true;
    }

    public void lattice_to_tiling(int[] l_point, int offset, TilingPoint t_point) {
        t_point.x = t_point.y = 0.0;
        for (int ind = 0; ind < theTiling.ambient_dim; ++ind) {
            t_point.x += l_point[offset + ind] * theTiling.generator[0][ind];
            t_point.y += l_point[offset + ind] * theTiling.generator[1][ind];
        }
    }

    public void lattice_to_orthogonal(int[] l_point, int offset, TilingPoint o_point) {
        o_point.x = o_point.y = 0.0;
        for (int ind = 0; ind < theTiling.ambient_dim; ++ind) {
            o_point.y += (l_point[ind] - theTiling.offset[ind]) * theTiling.generator[Tiling.TARGET_DIM][ind];
            o_point.x += (l_point[ind] - theTiling.offset[ind]) * theTiling.generator[Tiling.TARGET_DIM + 1][ind];
        }
    }
}
