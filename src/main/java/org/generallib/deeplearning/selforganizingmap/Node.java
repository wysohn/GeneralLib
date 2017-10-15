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
package org.generallib.deeplearning.selforganizingmap;

public abstract class Node {
    private double x, y;

    private double[] weights;

    protected Node(int dimension, double x, double y) {
        this.x = x;
        this.y = y;
        weights = new double[dimension];

        for (int i = 0; i < dimension; i++) {
            weights[i] = Math.random();
        }
    }

    double getX() {
        return x;
    }

    double getY() {
        return y;
    }

    double[] getWeights() {
        return weights;
    }

    double eculideanDistance(double[] input) {
        double sum = 0.0;

        for (int i = 0; i < weights.length; i++) {
            sum += (input[i] - weights[i]) * (input[i] - weights[i]);
        }

        return sum;
    }

    void adjustWeight(double[] input, double learningRate, double influence) {
        double[] diff = sub.calc(input, weights);
        double[] decayed = mult.calc(influence, mult.calc(learningRate, diff));
        weights = sum.calc(weights, decayed);
    }

    private static Operation sum = new Operation() {
        @Override
        public double[] calc(double[] left, double[] right) {
            if (left.length != right.length)
                throw new RuntimeException("Dimension does not match.");

            double[] result = new double[left.length];
            for (int i = 0; i < left.length; i++) {
                result[i] = left[i] + right[i];
            }
            return result;
        }
    };

    private static Operation sub = new Operation() {
        @Override
        public double[] calc(double[] left, double[] right) {
            if (left.length != right.length)
                throw new RuntimeException("Dimension does not match.");

            double[] result = new double[left.length];
            for (int i = 0; i < left.length; i++) {
                result[i] = left[i] - right[i];
            }
            return result;
        }
    };

    private static Operation2 mult = new Operation2() {
        @Override
        public double[] calc(double factor, double[] input) {
            double[] result = new double[input.length];
            for (int i = 0; i < input.length; i++) {
                result[i] = factor * input[i];
            }
            return result;
        }
    };

    interface Operation {
        double[] calc(double[] left, double[] right);
    }

    interface Operation2 {
        double[] calc(double factor, double[] input);
    }
}
