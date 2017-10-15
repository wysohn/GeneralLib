package org.generallib.pluginbase.constants;

import java.util.HashSet;
import java.util.Set;

public class Area {
    final SimpleLocation smallest;
    final SimpleLocation largest;

    public Area(SimpleLocation smallest, SimpleLocation largest) {
        this.smallest = smallest;
        this.largest = largest;
    }

    public SimpleLocation getSmallest() {
        return smallest;
    }

    public SimpleLocation getLargest() {
        return largest;
    }

    public boolean isInThisArea(SimpleLocation sloc) {
        if (smallest.getX() <= sloc.getX() && sloc.getX() <= largest.getX() && smallest.getY() <= sloc.getY()
                && sloc.getY() <= largest.getY() && smallest.getZ() <= sloc.getZ() && sloc.getZ() <= largest.getZ())
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((largest == null) ? 0 : largest.hashCode());
        result = prime * result + ((smallest == null) ? 0 : smallest.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Area other = (Area) obj;
        if (largest == null) {
            if (other.largest != null)
                return false;
        } else if (!largest.equals(other.largest))
            return false;
        if (smallest == null) {
            if (other.smallest != null)
                return false;
        } else if (!smallest.equals(other.smallest))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return smallest.world + "@" + smallest.x + "," + smallest.y + "," + smallest.z + "," + largest.x + ","
                + largest.y + "," + largest.z;
    }

    /**
     *
     * @param str
     * @return
     * @throws AreaFormatException
     * @throws NumberFormatException
     */
    public static Area fromString(String str) {
        String[] splitw = str.split("@", 2);
        if (splitw.length < 2)
            throw new AreaFormatException(str + " does not contains @.");

        String world = splitw[0];

        String[] splitl = splitw[1].split(",", 6);
        if (splitl.length < 6)
            throw new AreaFormatException(str + " does not contains enough coordinates.");

        int smallX, smallY, smallZ, largeX, largeY, largeZ;
        smallX = Integer.parseInt(splitl[0]);
        smallY = Integer.parseInt(splitl[1]);
        smallZ = Integer.parseInt(splitl[2]);
        largeX = Integer.parseInt(splitl[3]);
        largeY = Integer.parseInt(splitl[4]);
        largeZ = Integer.parseInt(splitl[5]);

        return new Area(new SimpleLocation(world, smallX, smallY, smallZ),
                new SimpleLocation(world, largeX, largeY, largeZ));
    }

    /**
     * Check wether the area1 and area2 conflicts. Conflict means at least a
     * block in the area is duplicating with another block in the second area.
     *
     * @param area1
     *            area1
     * @param area2
     *            area2
     * @return true if found conflict; false otherwise
     */
    public static boolean isConflicting(Area area1, Area area2) {
        if (!area1.smallest.getWorld().equals(area2.smallest.getWorld()))
            return false;

        int xs1 = area1.smallest.getX(), xs2 = area2.smallest.getX();
        int ys1 = area1.smallest.getY(), ys2 = area2.smallest.getY();
        int zs1 = area1.smallest.getZ(), zs2 = area2.smallest.getZ();

        int xl1 = area1.largest.getX(), xl2 = area2.largest.getX();
        int yl1 = area1.largest.getY(), yl2 = area2.largest.getY();
        int zl1 = area1.largest.getZ(), zl2 = area2.largest.getZ();

        boolean xConflict = false;
        boolean zConflict = false;
        // compare x
        if (Math.abs(xl1 - xs1) > Math.abs(xl2 - xs2)) {// sec1 is longer so
                                                        // check if one of the
                                                        // points in sec2 within
                                                        // the range
            if ((xs1 <= xs2 && xs2 <= xl1) || (xs1 <= xl2 && xl2 <= xl1)) {
                xConflict = true;
            }
        } else {// sec2 is longer so check if one of the points in sec1 within
                // the range
            if ((xs2 <= xs1 && xs1 <= xl2) || (xs2 <= xl1 && xl1 <= xl2)) {
                xConflict = true;
            }
        }

        // compare z
        if (Math.abs(zl1 - zs1) > Math.abs(zl2 - zs2)) {// sec1 is longer so
                                                        // check if one of the
                                                        // points in sec2 within
                                                        // the range
            if ((zs1 <= zs2 && zs2 <= zl1) || (zs1 <= zl2 && zl2 <= zl1)) {
                zConflict = true;
            }
        } else {// sec2 is longer so check if one of the points in sec1 within
                // the range
            if ((zs2 <= zs1 && zs1 <= zl2) || (zs2 <= zl1 && zl1 <= zl2)) {
                zConflict = true;
            }
        }

        // compare y
        if (xConflict && zConflict) {
            if (ys1 > ys2) {// sec1 on sec2
                int yFloor = ys1;
                int yCeiling = yl2;

                if (yFloor - yCeiling <= 0)
                    return true;
            } else if (yl1 < yl2) {// sec2 on sec1
                int yFloor = ys2;
                int yCeiling = yl1;

                if (yFloor - yCeiling <= 0)
                    return true;
            } else {// sec2 bot == sec1 bot
                return true;
            }
        }

        return false;
    }

    /**
     * Retrieve all the chunks that contains the 'area'
     * 
     * @param area
     *            the area
     * @return list of chunks that holds the area.
     */
    public static Set<SimpleChunkLocation> getAllChunkLocations(Area area) {
        SimpleLocation smallest = area.smallest;
        SimpleLocation largest = area.largest;

        Set<SimpleChunkLocation> set = new HashSet<>();

        for (int z = smallest.z; z <= largest.z; z += 16) {
            for (int x = smallest.x; x <= largest.x; x += 16) {
                int chunkX = x >> 4;
                int chunkZ = z >> 4;

                set.add(new SimpleChunkLocation(smallest.world, chunkX, chunkZ));
            }

            set.add(new SimpleChunkLocation(smallest.world, largest.x >> 4, z >> 4));
        }

        int z = largest.getZ();
        int chunkZ = z >> 4;
        for (int x = smallest.getX(); x <= largest.getX(); x += 16) {
            int chunkX = x >> 4;
            set.add(new SimpleChunkLocation(smallest.world, chunkX, chunkZ));
        }

        set.add(new SimpleChunkLocation(smallest.world, largest.x >> 4, z >> 4));

        return set;
    }

    public static Area formAreaBetweenTwoPoints(SimpleLocation sloc1, SimpleLocation sloc2) {
        return new Area(
                new SimpleLocation(sloc1.world, Math.min(sloc1.x, sloc2.x), Math.min(sloc1.y, sloc2.y),
                        Math.min(sloc1.z, sloc2.z)),
                new SimpleLocation(sloc2.world, Math.max(sloc1.x, sloc2.x), Math.max(sloc1.y, sloc2.y),
                        Math.max(sloc1.z, sloc2.z)));
    }

    public static class AreaFormatException extends RuntimeException {

        public AreaFormatException(String message, Throwable cause) {
            super(message, cause);
        }

        public AreaFormatException(String message) {
            super(message);
        }

        public AreaFormatException(Throwable cause) {
            super(cause);
        }

    }
}