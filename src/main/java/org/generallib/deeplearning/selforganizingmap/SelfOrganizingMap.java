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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class SelfOrganizingMap<T extends Node> {
    private static boolean isDrawing = false;

    public static void main(String[] ar) {
        SelfOrganizingMap map = new SelfOrganizingMap(3, 40, 40, 1.0, new NodeConstructor() {
            @Override
            public Node create(int dimension, double x, double y) {
                return new Node(dimension, x, y) {
                    @Override
                    public String toString() {
                        return getX() + ", " + getY();
                    }
                };
            }
        });

        final Queue<Node> nodesForDraw = new LinkedList<Node>();
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(1280, 1280);
            }

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                while (!nodesForDraw.isEmpty()) {
                    Node node = nodesForDraw.poll();
                    if (node == null)
                        continue;

                    double[] weights = node.getWeights();
                    if (weights.length != 3)
                        continue;

                    int red = (int) (weights[0] * 255);
                    int green = (int) (weights[1] * 255);
                    int blue = (int) (weights[2] * 255);

                    g.setColor(new Color(red, green, blue));
                    g.fillRect((int) node.getX() * 10, (int) node.getY() * 10, 10, 10);
                }

                synchronized (frame) {
                    isDrawing = false;
                    frame.notifyAll();
                }
            }
        };
        frame.setLayout(new BorderLayout());
        frame.setPreferredSize(new Dimension(1280, 1280));
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        map.setTracker(new IterationTracker() {
            @Override
            public void onInteration(Node[] map, int currernt) {
                for (Node node : map)
                    nodesForDraw.add(node);
                isDrawing = true;
                frame.validate();
                frame.repaint();

                try {
                    Thread.sleep(10L);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

                while (isDrawing) {
                    synchronized (frame) {
                        try {
                            frame.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        map.train(new double[][] { { Math.random(), Math.random(), Math.random() },
                { Math.random(), Math.random(), Math.random() }, { Math.random(), Math.random(), Math.random() },
                { Math.random(), Math.random(), Math.random() }, { Math.random(), Math.random(), Math.random() },
                { Math.random(), Math.random(), Math.random() }, { Math.random(), Math.random(), Math.random() },
                { Math.random(), Math.random(), Math.random() }, { Math.random(), Math.random(), Math.random() },
                { Math.random(), Math.random(), Math.random() }, { Math.random(), Math.random(), Math.random() },
                { Math.random(), Math.random(), Math.random() }, { Math.random(), Math.random(), Math.random() },
                { Math.random(), Math.random(), Math.random() }, { Math.random(), Math.random(), Math.random() },
                { Math.random(), Math.random(), Math.random() }, { Math.random(), Math.random(), Math.random() },
                { Math.random(), Math.random(), Math.random() }, { Math.random(), Math.random(), Math.random() } },
                500);

        System.out.println("red: " + map.findBMU(new double[] { 1, 0, 0 }, 0.1));
        System.out.println("green: " + map.findBMU(new double[] { 0, 1, 0 }, 0.1));
        System.out.println("blue: " + map.findBMU(new double[] { 0, 0, 1 }, 0.1));
        System.out.println("black: " + map.findBMU(new double[] { 0, 0, 0 }, 0.1));
        System.out.println("white: " + map.findBMU(new double[] { 1, 1, 1 }, 0.1));
        System.out.println("?: " + map.findBMU(new double[] { 1, 0, 1 }, 0.1));
        System.out.println("?: " + map.findBMU(new double[] { 1, 0.005, 1 }, 0.1));
    }

    private Node[] nodeMap;
    private int clientWidth, clientHeight;

    private double radius;
    private int iteration = 0;
    private final double initLearningRate;
    private double learningRate = 1.0;

    private IterationTracker tracker;

    public SelfOrganizingMap(int dimension, int clientWidth, int clientHeight, double initLearningRate,
            NodeConstructor nodeConstructor) {
        this.clientWidth = clientWidth;
        this.clientHeight = clientHeight;
        this.nodeMap = new Node[clientWidth * clientHeight];
        this.initLearningRate = initLearningRate;
        this.learningRate = initLearningRate;

        for (int i = 0; i < clientWidth * clientHeight; i++) {
            nodeMap[i] = nodeConstructor.create(dimension, i % clientWidth, i / clientWidth);
        }

        radius = Math.max(clientWidth, clientHeight) / 2.0;
    }

    IterationTracker getTracker() {
        return tracker;
    }

    void setTracker(IterationTracker tracker) {
        this.tracker = tracker;
    }

    public void train(double[][] input, int interationCount) {
        double timeConstant = interationCount / Math.log(radius);
        for (iteration = 0; iteration < interationCount; iteration++) {
            train(input, timeConstant);

            learningRate = initLearningRate * Math.exp(-(double) iteration / interationCount);

            if (tracker != null)
                tracker.onInteration(nodeMap, iteration);
        }
    }

    private Random rand = new Random();

    private void train(double[][] input, double timeConstant) {
        int randVal = rand.nextInt(input.length);

        Node winningNode = findBMU(input[randVal]);

        double neighbourhood = radius * Math.exp(-(double) iteration / timeConstant);

        for (int i = 0; i < nodeMap.length; i++) {
            double distSquared = (winningNode.getX() - nodeMap[i].getX()) * (winningNode.getX() - nodeMap[i].getX())
                    + (winningNode.getY() - nodeMap[i].getY()) * (winningNode.getY() - nodeMap[i].getY());

            double radSquared = neighbourhood * neighbourhood;

            if (distSquared < radSquared) {
                double influence = Math.exp(-(distSquared) / (2 * radSquared));

                nodeMap[i].adjustWeight(input[randVal], learningRate, influence);
            }
        }
    }

    private Node findBMU(double[] vector) {
        Node BMU = nodeMap[0];
        double minDist = BMU.eculideanDistance(vector);

        for (int i = 1; i < nodeMap.length; i++) {
            double eculidean = nodeMap[i].eculideanDistance(vector);
            if (eculidean < minDist) {
                BMU = nodeMap[i];
                minDist = eculidean;
            }
        }
        return BMU;
    }

    /**
     * 
     * @param vector
     *            input vector
     * @param accuracy
     *            limit of distance. If the distance is bigger than this value,
     *            return value will be null.
     * @return
     */
    public Node findBMU(double[] vector, double accuracy) {
        Node BMU = nodeMap[0];
        double minDist = BMU.eculideanDistance(vector);

        for (int i = 1; i < nodeMap.length; i++) {
            double eculidean = nodeMap[i].eculideanDistance(vector);
            if (eculidean < minDist) {
                BMU = nodeMap[i];
                minDist = eculidean;
            }
        }

        if (minDist < accuracy)
            return BMU;
        else
            return null;
    }

    public interface IterationTracker {
        void onInteration(Node[] map, int currernt);
    }

    public interface NodeConstructor {
        Node create(int dimension, double x, double y);
    }
}
