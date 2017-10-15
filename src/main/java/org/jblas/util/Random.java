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
package org.jblas.util;

/**
 * Created by IntelliJ IDEA. User: mikio Date: 6/24/11 Time: 10:45 AM To change
 * this template use File | Settings | File Templates.
 */

public class Random {
    private static java.util.Random r = new java.util.Random();

    public static void seed(long newSeed) {
        r = new java.util.Random(newSeed);
    }

    public static double nextDouble() {
        return r.nextDouble();
    }

    public static float nextFloat() {
        return r.nextFloat();
    }

    public static int nextInt(int max) {
        return r.nextInt(max);
    }

    public static double nextGaussian() {
        return r.nextGaussian();
    }
}
