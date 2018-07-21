/*
*				TileList.java
*
*  Copyright ( c ) 1994 by Eugenio Durand and The Geometry Center.
*  Distributed under the terms of the GNU General Public License.
*
*  Java conversion copyright ( c ) 1999 by Pierre Baillargeon.
*/

package QuasiTiler;

public class TileList {
    public static final int INIT_CAPACITY = 128;

    public TileList() {
        count = 0;
        if (null == array || array.length < 1) {
            array = new int[INIT_CAPACITY];
        }
    }

    public int[] store(int aVertex) {
        if (count >= array.length - 2) {
            int[] new_array = new int[array.length * 2];
            System.arraycopy(array, 0, new_array, 0, array.length);
            array = new_array;
        }
        array[count++] = aVertex;

        return array;
    }

    public int array[];
    public int count;
}
