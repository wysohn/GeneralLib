/*******************************************************************************
 *     Copyright (C) 2017 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.generallib.deeplearning.neuralnetwork;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

public class NeuralNetwork implements Serializable {
    private static final ActivationFunction defaultFunction = new ActivationFunction() {
        @Override
        public DoubleMatrix activate(DoubleMatrix matrix) {
            return MatrixFunctions.exp(matrix.mul(-1.0)).add(1.0).rdiv(1.0);
        }
    };

    public static void main(String[] ar) throws Exception {
        NeuralNetwork net = new NeuralNetwork(new int[] { 5, 6, 6, 3 });

        System.out.println(net);

        DoubleMatrix dataset = new DoubleMatrix(
                new double[][] { { 'H', 'E', 'L', 'L', 'O' }, { 'H', 'E', 'L', 'L', 'o' }, { 'H', 'E', 'L', 'l', 'O' },
                        { 'H', 'E', 'L', 'l', 'o' }, { 'H', 'E', 'l', 'L', 'O' }, { 'H', 'E', 'l', 'l', 'O' },
                        { 'H', 'E', 'l', 'L', 'o' }, { 'h', 'e', 'l', 'l', 'o' }, { 'h', 'i', ' ', ' ', ' ' },
                        { 'h', 'I', ' ', ' ', ' ' }, { 'H', 'i', ' ', ' ', ' ' }, { 'H', 'I', ' ', ' ', ' ' },
                        { 'h', 'i', ' ', ' ', ' ' }, { 'H', 'i', ' ', ' ', ' ' }, { 'h', 'I', ' ', ' ', ' ' },
                        { 'H', 'I', ' ', ' ', ' ' }, { 'h', ' ', 'I', ' ', ' ' }, });
        dataset = dataset.div(dataset.max() - dataset.min());

        double cost = 0.0;
        for (int count = 0; count < 10; count++) {
            net.resetLayers();

            double lambda = 0.001 * count * count;
            for (int i = 0; i < 1000; i++) {
                cost = net.trainNetwork(dataset,
                        new DoubleMatrix(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, }), lambda);
            }
            System.out.println("\nlambda [" + lambda + "] >>> " + cost + "\n");

            System.out
                    .println("hello: " + net.predict(new DoubleMatrix(new double[][] { { 'h', 'e', 'l', 'l', 'o' } })));
            System.out.println("hi: " + net.predict(new DoubleMatrix(new double[][] { { 'h', 'i', ' ', ' ', ' ' } })));
            System.out
                    .println("happy: " + net.predict(new DoubleMatrix(new double[][] { { 'h', 'a', 'p', 'p', 'y' } })));
        }

        File file = new File("test");
        net.saveToFile(file);

        System.out.println(net);

        NeuralNetwork loaded = NeuralNetwork.fromFile(file);
        System.out.println(loaded);
    }

    private int[] layerCounts;
    private DoubleMatrix[] theta;
    private int outputRange;

    private transient ActivationFunction act;

    private NeuralNetwork() {

    }

    /**
     * network with sigmoid activation
     *
     * @param layerCounts
     * @throws NeuralNetworkInitializeException
     */
    public NeuralNetwork(int[] layerCounts) throws NeuralNetworkInitializeException {
        this(layerCounts, defaultFunction);
    }

    /**
     * network with custom activation
     *
     * @param layerCounts
     * @param act
     * @throws NeuralNetworkInitializeException
     */
    public NeuralNetwork(int[] layerCounts, ActivationFunction act) throws NeuralNetworkInitializeException {
        this.act = act;

        if (layerCounts.length < 3)
            throw new InvalidLayerCountException();
        this.layerCounts = layerCounts;
        theta = new DoubleMatrix[layerCounts.length - 1];

        this.outputRange = layerCounts[layerCounts.length - 1];

        resetLayers();
    }

    public void resetLayers() {
        for (int i = 0; i < layerCounts.length - 1; i++) {
            theta[i] = DoubleMatrix.rand(layerCounts[i + 1], layerCounts[i]);
            theta[i] = DoubleMatrix.concatHorizontally(DoubleMatrix.ones(layerCounts[i + 1]), theta[i]);
        }
    }

    public double trainNetwork(DoubleMatrix X, DoubleMatrix y) {
        return trainNetwork(X, y, 0);
    }

    public double trainNetwork(DoubleMatrix X, DoubleMatrix y, double lambda) {
        int m = X.rows;
        //// foward prop

        // input
        DoubleMatrix[] forward = new DoubleMatrix[layerCounts.length];
        forward[0] = DoubleMatrix.concatHorizontally(DoubleMatrix.ones(X.rows), X);

        // hidden
        for (int i = 1; i < layerCounts.length - 1; i++) {
            DoubleMatrix z = forward[i - 1].mmul(theta[i - 1].transpose());
            forward[i] = DoubleMatrix.concatHorizontally(DoubleMatrix.ones(z.rows), act.activate(z));
        }

        // output
        DoubleMatrix z = forward[layerCounts.length - 2].mmul(theta[layerCounts.length - 2].transpose());
        forward[layerCounts.length - 1] = act.activate(z);

        //// cost function
        double cost = cost(X, y, theta, m, lambda);

        //// back prop
        DoubleMatrix y_mat = DoubleMatrix.eye(outputRange).getRows(y.toIntArray());

        // output
        DoubleMatrix[] deltas = new DoubleMatrix[layerCounts.length];
        deltas[layerCounts.length - 1] = forward[layerCounts.length - 1].sub(y_mat);

        // hidden(delta[0] is empty)
        for (int i = layerCounts.length - 2; i > 0; i--) {
            DoubleMatrix sigGrad = forward[i].mul(forward[i].rsub(1.0));
            sigGrad = sigGrad.getRange(0, sigGrad.rows, 1, sigGrad.columns);

            DoubleMatrix thetaTarget = theta[i].getRange(0, theta[i].rows, 1, theta[1].columns);
            deltas[i] = deltas[i + 1].mmul(thetaTarget).mul(sigGrad);
        }

        //// gradient update
        for (int i = 0; i < theta.length; i++) {
            DoubleMatrix delta = deltas[i + 1];
            DoubleMatrix thetaTemp = theta[i].mulColumn(0, 0);

            DoubleMatrix grad = delta.transpose().mmul(forward[i]).mul(1.0 / m).add(thetaTemp.mul(lambda / m));
            grad = grad.mulColumn(0, 0);

            theta[i] = theta[i].sub(grad);
        }

        return cost;
    }

    private double cost(DoubleMatrix X, DoubleMatrix y, DoubleMatrix[] theta, int m, double lambda) {
        DoubleMatrix output = predict(X, theta);

        // cost
        DoubleMatrix y_mat = DoubleMatrix.eye(outputRange).getRows(y.toIntArray()).mul(-1.0);

        DoubleMatrix left = new DoubleMatrix();
        left.copy(y_mat);
        left = left.mul(MatrixFunctions.log(output));

        DoubleMatrix right = new DoubleMatrix();
        right.copy(y_mat);
        right = right.add(1.0);
        right = right.mul(MatrixFunctions.log(output.rsub(1.0)));

        DoubleMatrix leftMright = left.sub(right);

        double normalSum = (1.0 / m) * leftMright.sum();

        double regularization = 0.0;
        // regularization
        for (int i = 0; i < theta.length; i++) {
            DoubleMatrix thetaTemp = new DoubleMatrix();
            thetaTemp.copy(theta[i]);
            thetaTemp.mulColumn(0, 0);
            regularization += MatrixFunctions.pow(thetaTemp, 2).sum();
        }
        regularization *= (lambda / (2.0 * m));

        return normalSum + regularization;
    }

    public DoubleMatrix predict(DoubleMatrix X) {
        return predict(X, theta);
    }

    private DoubleMatrix predict(DoubleMatrix X, DoubleMatrix[] theta) {
        // input
        DoubleMatrix[] forward = new DoubleMatrix[layerCounts.length];
        forward[0] = DoubleMatrix.concatHorizontally(DoubleMatrix.ones(X.rows), X);

        // hidden
        for (int i = 1; i < layerCounts.length - 1; i++) {
            DoubleMatrix z = forward[i - 1].mmul(theta[i - 1].transpose());
            forward[i] = DoubleMatrix.concatHorizontally(DoubleMatrix.ones(z.rows), act.activate(z));
        }

        // output
        DoubleMatrix z = forward[layerCounts.length - 2].mmul(theta[layerCounts.length - 2].transpose());
        return act.activate(z);
    }

    public void saveToFile(File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(this);

        oos.flush();
        oos.close();
        fos.close();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(layerCounts);
        out.writeObject(theta);
        out.writeObject(outputRange);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.layerCounts = (int[]) in.readObject();
        this.theta = (DoubleMatrix[]) in.readObject();
        this.outputRange = (int) in.readObject();
    }

    @Override
    public String toString() {
        String layerInfo = "[ ";
        for (int layer : layerCounts)
            layerInfo += layer + " ";
        layerInfo += "]";

        String thetaInfo = "";
        for (int i = 0; i < theta.length; i++) {
            thetaInfo += "Layer" + (i + 1) + " -> " + (i + 2) + " \n" + theta[i].toString("%.5f", "", "", " ", "\n")
                    + "\n";
        }

        return "NeuralNetwork -- " + layerInfo + "\n" + thetaInfo;
    }

    public static NeuralNetwork fromFile(File file) {
        return fromFile(file, defaultFunction);
    }

    public static NeuralNetwork fromFile(File file, ActivationFunction func) {

        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);

            NeuralNetwork net = (NeuralNetwork) ois.readObject();
            net.act = func;

            ois.close();
            fis.close();

            return net;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
