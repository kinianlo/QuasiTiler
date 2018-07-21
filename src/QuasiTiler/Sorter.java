/*
*				Sorter.java
*
*  Copyright ( c ) 1994 by Eugenio Durand and The Geometry Center.
*  Distributed under the terms of the GNU General Public License.
*
*  Java conversion copyright ( c ) 1999 by Pierre Baillargeon.
*/

package QuasiTiler;

public class Sorter {
    public static void qsort(int[] a, int count, int size, Comparator c) {
        int aux[] = (int[]) a.clone();
        mergeSort(aux, a, 0, count, size, c);
    }

    public static boolean bsearch(int[] el, int[] array, int count, int size, Comparator comp) {
        return binarySearch(array, count, el, size, comp) >= 0;
    }

    private static void swap(int[] a, int i, int j, int size) {
        i *= size;
        j *= size;
        while (--size >= 0) {
            final int temp = a[i + size];
            a[i + size] = a[j + size];
            a[j + size] = temp;
        }
    }

    private static void mergeSort(int src[], int dest[], int low, int high, int size, Comparator c) {
        int length = high - low;

        // Insertion sort on smallest arrays
        if (length < 7) {
            for (int i = low; i < high; i++)
                for (int j = i; j > low && c.compare(dest, (j - 1) * size, dest, j * size) > 0; j--)
                    swap(dest, j, j - 1, size);
            return;
        }

        // Recursively sort halves of dest into src
        int mid = (low + high) / 2;
        mergeSort(dest, src, low, mid, size, c);
        mergeSort(dest, src, mid, high, size, c);

        // If list is already sorted, just copy from src to dest. This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (c.compare(src, (mid - 1) * size, src, mid * size) <= 0) {
            System.arraycopy(src, low * size, dest, low * size, length * size);
            return;
        }

        // Merge sorted halves (now in src) into dest
        for (int i = low, p = low, q = mid; i < high; i++) {
            if (q >= high || p < mid && c.compare(src, p * size, src, q * size) <= 0) {
                for (int s = size; --s >= 0;)
                    dest[i * size + s] = src[p * size + s];
                p++;
            } else {
                for (int s = size; --s >= 0;)
                    dest[i * size + s] = src[q * size + s];
                q++;
            }
        }
    }

    /**
     * Searches the specified array for the specified object using the binary search
     * algorithm. The array must be sorted into ascending order according to the
     * specified comparator (as by the <tt>Sort(Object[],
     * Comparator)</tt> method, above), prior to making this call. If it is not
     * sorted, the results are undefined. If the array contains multiple elements
     * equal to the specified object, there is no guarantee which one will be found.
     *
     * @param a   the array to be searched.
     * @param key the value to be searched for.
     * @param c   the comparator by which the array is ordered.
     * @return index of the search key, if it is contained in the list; otherwise,
     *         <tt>(-(<i>insertion point</i>) - 1)</tt>. The <i>insertion point</i>
     *         is defined as the point at which the key would be inserted into the
     *         list: the index of the first element greater than the key, or
     *         <tt>list.size()</tt>, if all elements in the list are less than the
     *         specified key. Note that this guarantees that the return value will
     *         be &gt;= 0 if and only if the key is found.
     * @throws ClassCastException if the array contains elements that are not
     *                            <i>mutually comparable</i> using the specified
     *                            comparator, or the search key in not mutually
     *                            comparable with the elements of the array using
     *                            this comparator.
     * @see Comparable
     * @see #sort(Object[], Comparator)
     */
    private static int binarySearch(int[] a, int count, int[] key, int size, Comparator c) {
        int low = 0;
        int high = count; // a.length-1;

        while (low <= high) {
            int mid = (low + high) / 2;
            int cmp = c.compare(a, mid * size, key, 0);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1); // key not found.
    }

}
