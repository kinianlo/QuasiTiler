/*
*				VertexList.java
*
*  Copyright ( c ) 1994 by Eugenio Durand and The Geometry Center.
*  Distributed under the terms of the GNU General Public License.
*
*  Java conversion copyright ( c ) 1999 by Pierre Baillargeon.
*/

package QuasiTiler;

public class VertexList {
    public static final int INIT_CAPACITY = 128;

    public VertexList() {
        count = 0;
        if (null == array || array.length < 1) {
            array = new int[INIT_CAPACITY];
        }
    }

    public int[] store(Tiling theTiling, int[] aPoint) {
        if (count >= array.length - Tiling.MAX_DIM) {
            int[] new_array = new int[array.length * 2];
            System.arraycopy(array, 0, new_array, 0, array.length);
            array = new_array;
        }
        System.arraycopy(aPoint, 0, array, count, theTiling.ambient_dim);
        count += theTiling.ambient_dim;

        return array;
    }

    public int array[];
    public int count;
}
