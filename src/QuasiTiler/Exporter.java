package QuasiTiler;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class Exporter {
    private String file_path;
    private PrintWriter print;

    public Exporter(String file_path) {
        try {
            this.file_path = file_path;
            this.print = new PrintWriter(new FileWriter(file_path, false));
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
    }

    public void close() {
        this.print.close();
    }

    public void writeTile(int tile_index, double centroid_x, double centroid_y, int dim) {
        int row = tile_index / dim;
        int col = tile_index % dim;

        this.print.printf("%d\t%d\t%.20e\t%.20e\n", row, col, centroid_x, centroid_y);
    }

    public static void saveVertices(VertexList vertexList, int dim) {
        try {
            FileWriter write = new FileWriter("export/vertices.txt", false);
            PrintWriter print_line = new PrintWriter(write);
            for (int i = 0; i < vertexList.count; i++) {
                print_line.printf("%d\t", vertexList.array[i]);
                if (i % dim == dim - 1) {
                    print_line.printf("\n");
                }
            }
            print_line.close();
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
    }

    public static void saveProjectedVectices(TilingPoint[] tilingPoints) {
        try {
            FileWriter write = new FileWriter("export/projected_vertices.txt", false);
            PrintWriter print_line = new PrintWriter(write);
            for (int i = 0; i < tilingPoints.length; i++) {
                print_line.printf("%.20e\t%.20e\n", tilingPoints[i].x, tilingPoints[i].y);
            }
            print_line.close();
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
    }

    public static void saveGenerator(double[][] generator, int dim) {
        try {
            FileWriter write = new FileWriter("export/generator.txt", false);
            PrintWriter print_line = new PrintWriter(write);
            for (int d = 0; d < 2; d++) {
                for (int i = 0; i < dim; i++) {
                    print_line.printf("%.20e\t", generator[d][i]);
                }
                print_line.printf("\n");
            }
            print_line.close();
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
    }

    public static void saveOffset(double[] offset, int dim) {
        try {
            FileWriter write = new FileWriter("export/offset.txt", false);
            PrintWriter print_line = new PrintWriter(write);
            for (int i = 0; i < dim; i++) {
                print_line.printf("%.20e\t", offset[i]);
            }
            print_line.close();
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
    }
}