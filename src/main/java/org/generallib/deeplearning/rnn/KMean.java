package org.generallib.deeplearning.rnn;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import org.jblas.util.Random;

public class KMean {
    public static void main(String[] ar) {
        int K = 6;
        int dimension = 2;

        double[][] vectors = generateSamples(dimension, 300);
        KMean kmean = new KMean(K, new DoubleMatrix(vectors));

        for(int i = 0; i < 100; i++)
            kmean.doTraining(true);

        int[] c = kmean.getClusterIndexes();

        Drawer drawer = new Drawer(new DrawHandle() {

            @Override
            public void onDraw(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;

                for (int i = 0; i < c.length; i++) {
                    int clusterIndex = c[i]*100 % 256;
                    double[] vector = vectors[i];

                    g2d.setPaint(new Color(clusterIndex, (clusterIndex + 50) % 256, (clusterIndex + 100) % 256));

                    g2d.fillOval((int) vector[0], (int) vector[1], 8, 8);
                }
            }

        });
        drawer.setSize(400, 400);
        drawer.setLocationRelativeTo(null);
        drawer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        drawer.setVisible(true);

/*        List<List<double[]>> groups = new ArrayList<>();

        for(int i = 0; i < c.length; i++) {
            int index = c[i];

            if(index >= groups.size())
                groups.add(new ArrayList<>());
            List<double[]> group = groups.get(index);

            group.add(vectors[i]);
        }

        for(int gid = 0; gid < groups.size(); gid++) {
            System.out.println("GROUP "+gid+" :\n");
            for(double[] v : groups.get(gid))
                System.out.print(Arrays.toString(v)+", ");

            System.out.print("\n\n");
        }*/
    }

    private static double[][] generateSamples(int dimension, int limit) {
        int numVectors = 40;

        double[][] vectors = new double[numVectors][];

        for(int i = 0; i < numVectors; i++) {
            vectors[i] = new double[dimension];
            for(int j = 0; j < dimension; j++)
                vectors[i][j] = Random.nextInt(limit);
        }

        return vectors;
    }

    private int[] c; // centroid indexes for dataset
    private DoubleMatrix u; // centroid location
    private DoubleMatrix x; // dataset

    public KMean(int k, DoubleMatrix dataset) {
        if(k < 2)
            throw new RuntimeException("k should be at least 2!");

        if(dataset.length == 0)
            throw new RuntimeException("dataset is empty!");

        int dimension = dataset.columns;
        if(dimension < 1)
            throw new RuntimeException("Dimension of dataset should be larger than 0!");
        int m = dataset.rows;

        int minValue = (int) dataset.min();
        int maxValue = (int) dataset.max();

        c = new int[m];
        x = dataset;
        u = randomInitialCentroids(k, dimension, minValue, maxValue);
    }

    private DoubleMatrix randomInitialCentroids(int k, int dimension, int min, int max) {
        DoubleMatrix matrix = new DoubleMatrix(k, dimension);
        for(int i = 0; i < matrix.length; i++)
            matrix.data[i] = Random.nextInt(max - min) + min;
        return matrix;
    }

    public double doTraining(boolean getCost) {
        for (int m = 0; m < x.rows; m++) {
            double distance = Double.MAX_VALUE;

            // data vector
            DoubleMatrix vector_x = x.getRow(m);

            for (int k = 0; k < u.rows; k++) {
                // centroid vector
                DoubleMatrix vector_c = u.getRow(k);

                double newDistance = getDistance(vector_c, vector_x);
                if (newDistance < distance) {
                    distance = newDistance;

                    // update
                    c[m] = k;
                }
            }
        }

        u = getMeans(u.rows, u.columns);

        if(getCost)
            return getCost();
        else
            return -0.0;
    }

    private DoubleMatrix getMeans(int k, int n) {
        DoubleMatrix centroids = DoubleMatrix.zeros(k, n);
        DoubleMatrix counts = DoubleMatrix.zeros(k);

        for(int m = 0; m < x.rows; m++) {
            DoubleMatrix vector_x = x.getRow(m);
            int k_id = c[m];

            centroids.putRow(k_id, centroids.getRow(k_id).add(vector_x));
            counts.put(k_id, counts.get(k_id) + 1);
        }

        return centroids.divColumnVector(counts);
    }

    public double getCost() {
        double sum = 0.0;

        for(int m = 0; m < c.length; m++) {
            DoubleMatrix vector_x = x.getRow(m);
            int k_id = c[m];

            sum += getDistance(vector_x, u.getRow(k_id));
        }

        return sum / c.length;
    }

    private double getDistance(DoubleMatrix vec1, DoubleMatrix vec2) {
        double top = vec1.mul(vec2).sum();
        double bottom = Math.sqrt(MatrixFunctions.pow(vec1, 2).sum())*Math.sqrt(MatrixFunctions.pow(vec2, 2).sum());

        return Math.acos(top / bottom);
    }

    public int[] getClusterIndexes() {
        return c;
    }

    private static class Drawer extends JFrame{
        private DrawHandle handle;
        private Drawer(DrawHandle handle) {
            super();
            this.handle = handle;
        }

        @Override
        public void paint(Graphics g) {
            handle.onDraw(g);
        }
    }

    private interface DrawHandle{
        void onDraw(Graphics g);
    }
}
