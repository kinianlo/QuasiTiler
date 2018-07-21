/*
*				Tiling.java
*
*  Copyright ( c ) 1994 by Eugenio Durand and The Geometry Center.
*  Distributed under the terms of the GNU General Public License.
*
*  Java conversion copyright ( c ) 1999 by Pierre Baillargeon.
*/

package QuasiTiler;

/*
**  Tiling.
*/

public class Tiling {
      /**
       *** Constants.
       **/

      public static final int MAX_DIM = 15;
      public static final int MAX_TILE_COMB = MAX_DIM * (MAX_DIM - 1) / 2;
      public static final int MAX_CYLR_COMB = MAX_DIM * (MAX_DIM - 1) * (MAX_DIM - 2) / 6;
      public static final int TARGET_DIM = 2;
      public static final double EPSILON = 0.000001;
      public static final double M_PI = (double) Math.PI;

      /**
       *** Constructors. Calls init_default().
       **/

      public Tiling() {
            init_default(5);
      }

      public Tiling(int dimension) {
            init_default(dimension);
      }

      /*
       ** Tiling data. Must have package-level protection because they are accessed
       * directly by the Drawing class. Oooh, evil.
       */

      int ambient_dim = 5;

      double[] offset = new double[MAX_DIM];
      double[][] generator = new double[MAX_DIM][MAX_DIM];

      int crit_count;
      double[][] cylinder_criteria = new double[MAX_CYLR_COMB][MAX_DIM];

      int[] co = new int[MAX_DIM]; /* the coordinate order */

      double[][] parametrization = new double[TARGET_DIM][TARGET_DIM];

      int[] so = new int[MAX_DIM]; /* the slope order */
      int[] sgn = new int[MAX_DIM];

      int tile_count;
      int[][] tile_index = new int[MAX_DIM][MAX_DIM];
      int[][] tile_generator = new int[MAX_TILE_COMB][2];

      /**
       *** Utility functions.
       **/

      public static final double my_abs(double f) {
            return f < 0 ? -f : f;
      }

      public static final int my_sgn(double f) {
            return f < 0 ? -1 : f > 0 ? 1 : 0;
      }

      public static final int e_compare(double x, double y) {
            return (x < y - EPSILON) ? -1 : (x > y + EPSILON) ? 1 : 0;
      }

      /**
       *** The floor ( ) and ceil ( ) defined in java.Math return double, an overkill
       * for us.
       **/

      public static final int my_floor(double x) {
            int i = (int) x;
            return i <= x ? i : i - 1;
      }

      public static final int my_ceil(double x) {
            int i = (int) x;
            return i >= x ? i : i + 1;
      }

      /**
       *** Now we define elementary vector operations.
       **/

      /* Computes the dot product */

      public double dot_product(double[] x, double[] y) {
            double prod = 0.0;

            for (int ind = ambient_dim; --ind >= 0;)
                  prod += x[ind] * y[ind];
            return prod;
      }

      /* Computes x = s*y */

      public void scalar_mult(double[] x, double s, double[] y) {
            for (int ind = ambient_dim; --ind >= 0;)
                  x[ind] = s * y[ind];
      }

      /* Computes x = x + s*y */

      public void add_to(double[] x, double s, double[] y) {
            for (int ind = ambient_dim; --ind >= 0;)
                  x[ind] += s * y[ind];
      }

      /**
       *** Definition of the functions for initializing the tiling.
       **/

      public boolean normalize() {
            // Complete the generator array to a basis
            // of the ambient space ( hopefully, change latter )

            // Initialize the first generator of the orthogonal space
            // to ( 1, -1, 1, -1,... )

            for (int ind2 = 0; ind2 < ambient_dim; ++ind2)
                  generator[TARGET_DIM][ind2] = ((ind2 % 2) != 0 ? -1.0 : 1.0);

            // Initialize the second generator of the orthogonal space
            // to ( 1, 1, 1, ... )

            if (TARGET_DIM + 1 < ambient_dim)
                  for (int ind2 = 0; ind2 < ambient_dim; ++ind2)
                        generator[TARGET_DIM + 1][ind2] = 1.0;

            // Initialize the rest with the canonical basis, which may not
            // give a linearly independent basis, but it is highly improbable

            for (int ind = TARGET_DIM + 1; ind < ambient_dim; ++ind)
                  for (int ind2 = 0; ind2 < ambient_dim; ++ind2)
                        generator[ind][ind2] = (ind == ind2 ? 1.0 : 0.0);

            // Grahm-Schmitt on the generators of tiling's plane

            for (int ind = 0; ind < ambient_dim; ++ind) {
                  double[] sum = new double[MAX_DIM];
                  for (int ind2 = 0; ind2 < ambient_dim; ++ind2)
                        sum[ind2] = 0.0;
                  for (int ind2 = 0; ind2 < ind; ++ind2) {
                        double scalar = dot_product(generator[ind], generator[ind2]);
                        add_to(sum, scalar, generator[ind2]);
                  }
                  add_to(generator[ind], -1.0, sum);

                  double scalar = (double) Math.sqrt(dot_product(generator[ind], generator[ind]));

                  if (e_compare(scalar, 0) == 0)
                        return false;

                  scalar_mult(generator[ind], 1 / scalar, generator[ind]);
            }

            // Try to make the first generator of the orthogonal space to have
            // integer coords. Probably this should be somewhere else, so
            // change latter

            /*
             * scalar_mult ( theTiling, theTiling->generator[TARGET_DIM], sqrt (
             * theTiling->ambient_dim ) , theTiling->generator[TARGET_DIM] );
             */

            // And check that the tiling's plane doesn't contain nor is
            // perpendicular to any of the lattice directions

            for (int ind = 0; ind < ambient_dim; ++ind) {
                  double[] projection = new double[MAX_DIM];
                  for (int ind2 = 0; ind2 < ambient_dim; ++ind2)
                        projection[ind2] = 0.0;
                  /* Project the ind-th generator into theTiling */
                  for (int ind2 = 0; ind2 < TARGET_DIM; ++ind2)
                        add_to(projection, generator[ind2][ind], generator[ind2]);
                  double scalar = (double) Math.sqrt(dot_product(projection, projection));
                  if (e_compare(scalar, 0) == 0)
                        return false;
                  if (e_compare(scalar, 1) == 0)
                        return false;
            }

            return true;
      }

      /**
       *** We go over all the combinations of 3 vectors out of ambient_dim of vectors
       * from the canonical basis. With each combination we find the orthogonal vector
       * to the tiling plane contained in the space generated by the current choice.
       * This vector will determine one face of the cylinder.
       *** 
       *** For efficiency, we "randomize" the order of the hyperplane criteria. This
       * increases the likelihood that we can reject points quickly when they lie far
       * from the cylinder.
       *** 
       *** We strongly assume that TARGET_DIM is 2 when we use the cross product to
       * compute the orthogonal vector.
       **/

      private static int[] primes = { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 0 };

      public boolean compute_cylinder() {

            /* Initialize the indexing of the combinations */

            int[] choice = new int[TARGET_DIM + 1];
            for (int ind = 0; ind <= TARGET_DIM; ++ind)
                  choice[ind] = ind;

            /* Randomize order of criteria to increase chances of rejection. */
            int randomizer = 1;
            int max_abs_res = 1;
            for (int ind = 0; primes[ind] > 1; ++ind) {
                  if (0 == (crit_count % primes[ind]))
                        continue;
                  int res = primes[ind] % crit_count;
                  if (res > crit_count / 2)
                        res -= crit_count;
                  int abs_res = (int) my_abs(res);
                  if (abs_res > max_abs_res) {
                        max_abs_res = abs_res;
                        randomizer = primes[ind];
                  }
            }

            /* Loop through all the combinations */
            boolean[] choosen = new boolean[MAX_DIM];
            for (int local_crit_count = 0; local_crit_count < crit_count; ++local_crit_count) {
                  /* Fill criteria in "random" order */
                  int crit_index = ((randomizer * local_crit_count) % crit_count);

                  /* Turn flags for the current combination */
                  for (int ind = 0; ind < ambient_dim; ++ind)
                        choosen[ind] = false;
                  for (int ind = 0; ind <= TARGET_DIM; ++ind)
                        choosen[choice[ind]] = true;

                  /*
                   * Find the vector orthogonal to the current choice of coordinate axis and
                   * theTiling subspace
                   */
                  double[] x = new double[3];
                  double[] y = new double[3];
                  double[] z = new double[3];
                  for (int ind = 0, dim = 0; ind < ambient_dim; ++ind)
                        if (choosen[ind]) {
                              x[dim] = generator[0][ind];
                              y[dim] = generator[1][ind];
                              ++dim;
                        }

                  /* Compute the cross product in R^3 */
                  z[0] = x[1] * y[2] - y[1] * x[2];
                  z[1] = x[2] * y[0] - y[2] * x[0];
                  z[2] = x[0] * y[1] - y[0] * x[1];

                  /* Put back the orthogonal vector back in the ambient space */
                  for (int ind = 0, dim = 0; ind < ambient_dim; ++ind) {
                        if (choosen[ind]) {
                              cylinder_criteria[crit_index][ind] = z[dim];
                              ++dim;
                        } else {
                              cylinder_criteria[crit_index][ind] = 0.0;
                        }
                  }

                  // Now find the extreme values for the criteria.

                  double scalar = 0.0;
                  int sign = 0;
                  for (int ind = 0; ind < ambient_dim; ++ind) {
                        scalar += my_abs(cylinder_criteria[crit_index][ind]);
                        if (0 == sign)
                              sign = my_sgn(cylinder_criteria[crit_index][ind]);
                  }

                  // It is against the hypothesis for the tiling to have a
                  // singular projection of a face of the cell, so we report
                  // this. The cylinder would be OK, it is only one less face to
                  // check.

                  if (e_compare(scalar, 0) == 0)
                        return false;

                  // Multiply by 2.0 since the cell is centered at 0 an has
                  // diameter 1/2, divide by scalar so 1.0 is the criteria, and
                  // make the first nonzero coordinate positive so we can
                  // distinguish between opposite corners.

                  scalar_mult(cylinder_criteria[crit_index], sign * 2.0 / scalar, cylinder_criteria[crit_index]);

                  // Increment to the next combination. Find first which choice
                  // can be incremented next.

                  int ind = TARGET_DIM;
                  while (ind > 0 && choice[ind] >= ambient_dim - 3 + ind)
                        --ind;
                  choice[ind++]++;
                  for (; ind <= TARGET_DIM; ++ind)
                        choice[ind] = choice[ind - 1] + 1;
            }
            return true;
      }

      /**
       *** Initialize the Tiling co[]. Sort co[] wrt the size of the projections of the
       * lattice generators to each of the plane generators.
       **/

      public void sort_coordinates() {
            // Initialize the Tiling co[].

            for (int ind0 = 0; ind0 < ambient_dim; ++ind0)
                  co[ind0] = ind0;

            // Sort co[] wrt the size of the projections of the lattice
            // generators to each of the plane generators.

            double[] aux = new double[MAX_DIM];

            for (int ind0 = 0; ind0 < TARGET_DIM; ++ind0) {
                  // Fill the auxiliary array with the projection lengths.
                  for (int ind1 = ind0; ind1 < ambient_dim; ++ind1)
                        aux[co[ind1]] = my_abs(generator[ind0][co[ind1]]);
                  // Find maximum.
                  for (int ind1 = ind0; ind1 < ambient_dim; ++ind1)
                        if (aux[co[ind0]] < aux[co[ind1]]) {
                              int temp = co[ind0];
                              co[ind0] = co[ind1];
                              co[ind1] = temp;
                        }
            }

            // Sort the directions so the projections have the proper
            // orientation to find the tiles.

            for (int ind0 = 0; ind0 < ambient_dim; ++ind0) {
                  so[ind0] = ind0;
                  sgn[ind0] = e_compare(generator[0][ind0], 0);
                  if (sgn[ind0] == 0)
                        sgn[ind0] = 1;
                  aux[ind0] = (double) (sgn[ind0] * generator[1][ind0] / Math
                              .sqrt(generator[0][ind0] * generator[0][ind0] + generator[1][ind0] * generator[1][ind0]));
            }

            for (int ind0 = 0; ind0 < ambient_dim - 1; ++ind0)
                  for (int ind1 = ind0; ind1 < ambient_dim; ++ind1)
                        if (aux[so[ind0]] > aux[so[ind1]]) {
                              int temp = so[ind0];
                              so[ind0] = so[ind1];
                              so[ind1] = temp;
                        }

            // We generate the tables with all the possible combinations
            // choosing two generators out of ambient_dim. The combinations
            // are generated so that tiles of the same size ( in the
            // simetrical case ) are consecutive in the table.

            int comb = 0;
            for (int size = 1; size <= ambient_dim / 2; ++size)
                  for (int frst = 0; frst < ambient_dim - 1; ++frst) {
                        int gen0 = so[frst];

                        if (frst + size < ambient_dim) {
                              int gen1 = so[frst + size];
                              tile_index[gen0][gen1] = comb;
                              tile_generator[comb][0] = gen0;
                              tile_generator[comb][1] = gen1;
                              ++comb;
                        }

                        if (frst - size < 0 && size != ambient_dim - size) {
                              int gen2 = so[frst + ambient_dim - size];
                              tile_index[gen0][gen2] = comb;
                              tile_generator[comb][0] = gen0;
                              tile_generator[comb][1] = gen2;
                              ++comb;
                        }
                  }

            return;
      }

      /**
       *** Compute the parametrization of the tiling plane with respect to the major
       * directions, and the lengths of the diagonals in each direction.
       **/

      public boolean init_parametrization() {
            double[][] a = new double[TARGET_DIM][TARGET_DIM];

            // Assuming that TARGET_DIM is 2, find the inverse matrix.

            a[0][0] = generator[0][co[0]];
            a[0][1] = generator[1][co[0]];
            a[1][0] = generator[0][co[1]];
            a[1][1] = generator[1][co[1]];

            double det = a[0][0] * a[1][1] - a[1][0] * a[0][1];
            if (e_compare(det, 0) == 0)
                  return false;

            parametrization[0][0] = a[1][1] / det;
            parametrization[0][1] = -a[0][1] / det;
            parametrization[1][0] = -a[1][0] / det;
            parametrization[1][1] = a[0][0] / det;

            return true;
      }

      /**
       *** init_default() initilize the tiling to the values corresponding to the most
       * symmetrical tiles. The function should be called every time the dimension
       * changes
       ***
       *** init_default() returns false if it cannot finish the computation for any
       * reason.
       **/

      public boolean init_default(int dimension) {
            ambient_dim = dimension;
            for (int ind = 0; ind < ambient_dim; ++ind) {
                  double theta = M_PI * ((double) ind / (double) ambient_dim - 0.5);
                  generator[0][ind] = (double) Math.cos(theta);
                  generator[1][ind] = (double) Math.sin(theta);
            }

            return true;
      }

      /**
       *** init() makes the preliminary computations; the main computation is finding
       * all the hyperplanes that bound the cylinder or region around the generating
       * plane. Also notice that we use relative offset, so the tiling must have
       * sensible values, for example the ones provided by init_default().
       ***
       *** init() returns false if it cannot finish the computation for any reason.
       **/

      public boolean init(double[] relative_offset) {
            crit_count = ambient_dim * (ambient_dim - 1) * (ambient_dim - 2) / 6;
            tile_count = ambient_dim * (ambient_dim - 1) / 2;

            // Check the input and complete the generators to an orthonormal
            // basis of the ambient space.

            if (!normalize())
                  return false;

            // Express the relative offset ( which is expressed in the
            // generator basis ) in the canonical basis. We ignore the first
            // two components of the relative offset, since without loss of
            // generality we assume that the offset is orthogonal to the
            // tiling plane.

            for (int ind = 0; ind < ambient_dim; ++ind)
                  offset[ind] = 0.0;
            for (int ind = TARGET_DIM; ind < ambient_dim; ++ind)
                  // for ( int ind = 0 ; ind < ambient_dim ; ++ind )
                  add_to(offset, relative_offset[ind], generator[ind]);

            if (!compute_cylinder())
                  return false;

            // Sort the usual coordinate basis wrt this tiling.

            sort_coordinates();

            // Compute the parametrization of the tiling with respect to the
            // major directions, and the lengths of the diagonals in each
            // direction.

            if (!init_parametrization())
                  return false;

            return true;
      }

      /**
       *** Changes a generator.
       **/

      public boolean setGenerator(TilingPoint point, int index) {
            generator[0][index] = point.x;
            generator[1][index] = point.y;
            return init(offset);
      }

      public boolean rotateGenerators(double degrees) {
            double cos = Math.cos(degrees);
            double sin = Math.sin(degrees);
            for (int ind = 0; ind < ambient_dim; ++ind) {
                  final double x = generator[0][ind];
                  final double y = generator[1][ind];
                  double x2 = (double) (x * cos - y * sin);
                  double y2 = (double) (y * cos + x * sin);
                  if (x2 < 0) {
                        x2 *= -1;
                        y2 *= -1;
                  }
                  generator[0][ind] = x2;
                  generator[1][ind] = y2;
            }
            return init(offset);
      }

      /**
       *** Find a bounding box for the cylinder in the ambient space. bounds[] is a
       * bounding box of the plane in generators coords.
       **/

      public void compute_ambient_bounds(double[][] tiling_bounds, int[][] bounds) {
            double[][][] corner = new double[2][2][MAX_DIM];

            for (int ind0 = 0; ind0 < 2; ++ind0)
                  for (int ind1 = 0; ind1 < 2; ++ind1) {
                        // Compute the corners of the tiling plane in the ambient space.

                        for (int ind = 0; ind < ambient_dim; ++ind)
                              corner[ind0][ind1][ind] = offset[ind];
                        add_to(corner[ind0][ind1], tiling_bounds[ind0][0], generator[0]);
                        add_to(corner[ind0][ind1], tiling_bounds[ind1][1], generator[1]);
                  }

            // Now we find the max and min.

            double[][] ambient_bounds = new double[2][MAX_DIM];

            for (int ind = 0; ind < ambient_dim; ++ind) {
                  ambient_bounds[0][ind] = corner[0][0][ind];
                  ambient_bounds[1][ind] = corner[0][0][ind];
                  for (int ind0 = 0; ind0 < 2; ++ind0)
                        for (int ind1 = 0; ind1 < 2; ++ind1) {
                              if (ambient_bounds[0][ind] > corner[ind0][ind1][ind])
                                    ambient_bounds[0][ind] = corner[ind0][ind1][ind];
                              if (ambient_bounds[1][ind] < corner[ind0][ind1][ind])
                                    ambient_bounds[1][ind] = corner[ind0][ind1][ind];
                        }

                  // Add/substract sqrt of dim to acomodate for the cylinder
                  // thickness.

                  double thick = (double) Math.sqrt(ambient_dim);
                  bounds[0][ind] = my_floor(ambient_bounds[0][ind] - thick);
                  bounds[1][ind] = my_ceil(ambient_bounds[1][ind] + thick);
            }
            return;
      }

      /**
       *** Find a point in the tiling plane. The plane is parametrized by the two main
       * canonical directions. Use these two main directions in scan_index to
       * determine the point in the plane. Return the result both in the ambient space
       * coords and the plane coords
       **/

      public void parametrization(int[] scan_index, double[] plane_point, TilingPoint tiling_point) {
            for (int ind0 = 0; ind0 < ambient_dim; ++ind0)
                  plane_point[ind0] = offset[ind0];

            for (int ind0 = 0; ind0 < TARGET_DIM; ++ind0) {
                  // Change coordinates in the plane, from canonical main coords,
                  // to the generators coords.

                  double scalar = 0.0;
                  for (int ind1 = 0; ind1 < TARGET_DIM; ++ind1)
                        scalar += parametrization[ind0][ind1] * (scan_index[co[ind1]] - offset[co[ind1]]);

                  // Compute the projection.

                  if (0 != ind0)
                        tiling_point.y = scalar;
                  else
                        tiling_point.x = scalar;

                  add_to(plane_point, scalar, generator[ind0]);
            }
            return;
      }

      /**
       *** Returns false if the point is not EPSILON inside the cylinder, true
       * otherwise.
       **/

      public boolean in_cylinder(int[] point) {

            // Translate by the offset.

            double[] trans_point = new double[MAX_DIM];
            for (int ind1 = 0; ind1 < ambient_dim; ++ind1)
                  trans_point[ind1] = point[ind1] - offset[ind1];

            // Now check if the point is inside all the faces of the cylinder.

            for (int ind = 0; ind < crit_count; ++ind) {
                  // Compute the dot product. For efficiency, no function call.

                  double dot_p = 0.0;
                  double[] current_criteria = cylinder_criteria[ind];
                  for (int ind1 = 0; ind1 < ambient_dim; ++ind1)
                        dot_p += current_criteria[ind1] * trans_point[ind1];
                  double ans = 1.0 - my_abs(dot_p);
                  if (ans < EPSILON)
                        return false; // outside.
            }
            return true;
      }

      /**
       *** generate() computes the vertices of the tiling that fit inside the
       * tiling_bounds, plus some more to guarantee that all the tiles partialy
       *** intersecting the rectagle given by tiling_bounds are computed.
       ***
       *** For each tiling vertex found, the function report_point() is called with
       *** target, theTiling and the corresponding latice point as parameters;
       *** report_point() should take proper action with the vertex, e.g. storing it,
       * printing it, etc.
       ***
       *** call_back() is called periodically (if it isn't null) to provide a way of
       *** stoping the computation; should return nonzero for the computation to stop.
       ***
       *** generate() returns false if it cannot finish the computation for any reason.
       **/

      public boolean generate(double[][] tiling_bounds, Reporter reporter, Interruptor interruptor) {
            // Find the bounds relative to the ambient space, for the bounds
            // in the tiling subspace in.

            int[][] bounds = new int[2][MAX_DIM];
            compute_ambient_bounds(tiling_bounds, bounds);

            // Initialize the indices for the scaning of this tiling.

            int[] scan_index = new int[MAX_DIM];
            for (int ind = 0; ind < TARGET_DIM; ++ind)
                  scan_index[co[ind]] = bounds[0][co[ind]];

            // Scaning this tiling.

            double diag = (double) Math.sqrt(2.0);
            double[] plane_point = new double[MAX_DIM];
            TilingPoint tilingPoint = new TilingPoint();
            while (scan_index[co[0]] <= bounds[1][co[0]]) {
                  // Find the next point in the tiling parametrization.

                  parametrization(scan_index, plane_point, tilingPoint);

                  // Do some preliminary clipping here.

                  if (tilingPoint.x > (tiling_bounds[0][0] - 2.0) && tilingPoint.x < (tiling_bounds[1][0] + 2.0)
                              && tilingPoint.y > (tiling_bounds[0][1] - 2.0)
                              && tilingPoint.y < (tiling_bounds[1][1] + 2.0)) {
                        // Find the bounds for the intersection of the tiling's
                        // plane with the remaining coordinates.

                        int[][] local_bounds = new int[2][MAX_DIM];
                        for (int dim = TARGET_DIM; dim < ambient_dim; ++dim) {
                              local_bounds[0][co[dim]] = my_ceil(plane_point[co[dim]] - diag);
                              local_bounds[1][co[dim]] = my_floor(plane_point[co[dim]] + diag);
                        }

                        // Scan for all the intersecting points above the current
                        // plane_point.

                        for (int ind = TARGET_DIM; ind < ambient_dim; ++ind)
                              scan_index[co[ind]] = local_bounds[0][co[ind]];

                        // Scaning.

                        while (scan_index[co[TARGET_DIM]] <= local_bounds[1][co[TARGET_DIM]]) {
                              if (in_cylinder(scan_index))
                                    reporter.report_point(this, scan_index);

                              // Increment the scan_index to the next point.

                              int ind = ambient_dim - 1;
                              while ((++(scan_index[co[ind]])) > local_bounds[1][co[ind]] && ind > TARGET_DIM) {
                                    scan_index[co[ind]] = local_bounds[0][co[ind]];
                                    ind--;
                              }
                        }
                  }

                  // Find the next point in the scaning in the parametrization.

                  int ind = TARGET_DIM - 1;
                  while ((++(scan_index[co[ind]])) > bounds[1][co[ind]] && ind > 0) {
                        scan_index[co[ind]] = bounds[0][co[ind]];
                        ind--;
                  }

                  // Should we abort the computation.

                  if (null != interruptor && interruptor.interrupted())
                        return false;
            }

            return true;
      }
}
