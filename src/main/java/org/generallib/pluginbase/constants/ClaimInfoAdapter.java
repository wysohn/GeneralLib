package org.generallib.pluginbase.constants;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class ClaimInfoAdapter implements ClaimInfo {
    private transient Area area;

    private final String name;

    private UUID owner;
    private Set<UUID> trusts = new HashSet<>();

    public ClaimInfoAdapter(String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.generallib.pluginbase.constants.ClaimInfo#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.generallib.pluginbase.constants.ClaimInfo#setArea(org.generallib.
     * pluginbase.constants.Area)
     */
    @Override
    public void setArea(Area area) {
        this.area = area;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.generallib.pluginbase.constants.ClaimInfo#getArea()
     */
    @Override
    public Area getArea() {
        return area;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.generallib.pluginbase.constants.ClaimInfo#isPublic()
     */
    @Override
    public boolean isPublic() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.generallib.pluginbase.constants.ClaimInfo#getOwner()
     */
    @Override
    public UUID getOwner() {
        return owner;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.generallib.pluginbase.constants.ClaimInfo#setOwner(java.util.UUID)
     */
    @Override
    public void setOwner(UUID uuid) {
        this.owner = uuid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.generallib.pluginbase.constants.ClaimInfo#getTrusts()
     */
    @Override
    public Set<UUID> getTrusts() {
        return trusts;
    }
}
