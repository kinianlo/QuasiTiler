package QuasiTiler;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class Exporter {
    private String file_path;

    public Exporter(String file_path) {
        this.file_path = file_path;
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

    public static void saveTiles(TilingPoint[] tilingPoints) {
        try {
            FileWriter write = new FileWriter("export/tiles.txt", false);
            PrintWriter print_line = new PrintWriter(write);
            for (int i = 0; i < tilingPoints.length; i++) {
                print_line.printf("%f\t%f\n", tilingPoints[i].x, tilingPoints[i].y);
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
                    print_line.printf("%f\t", generator[d][i]);
                }
                print_line.printf("\n");
            }
            print_line.close();
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
    }
}