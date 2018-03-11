package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.generallib.location.utils.LocationUtil;
import org.generallib.pluginbase.constants.ClaimInfo;
import org.generallib.pluginbase.constants.SimpleLocation;
import org.generallib.pluginbase.manager.RegionManager;

public abstract class DefaultHandle {
    private final RegionManager rmanager;

    protected DefaultHandle(RegionManager rmanager) {
        this.rmanager = rmanager;
    }

    protected ClaimInfo getInfo(SimpleLocation simpleLocation){
        return rmanager.getAreaInfo(simpleLocation);
    }

    protected ClaimInfo getInfo(Location location){
        return getInfo(LocationUtil.convertToSimpleLocation(location));
    }
}
