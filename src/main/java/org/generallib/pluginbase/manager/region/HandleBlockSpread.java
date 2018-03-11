package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.generallib.pluginbase.manager.RegionManager;

public class HandleBlockSpread extends DefaultHandle implements RegionManager.EventHandle {
    public HandleBlockSpread(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(Event e) {
        return null;
    }

    @Override
    public Location getLocation(Event e) {
        return null;
    }
}
