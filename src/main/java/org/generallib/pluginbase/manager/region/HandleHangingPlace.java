package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.generallib.pluginbase.manager.RegionManager;

public class HandleHangingPlace extends DefaultHandle implements RegionManager.EventHandle<HangingPlaceEvent> {
    public HandleHangingPlace(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(HangingPlaceEvent e) {
        return e.getPlayer();
    }

    @Override
    public Location getLocation(HangingPlaceEvent e) {
        return e.getBlock().getLocation();
    }
}
