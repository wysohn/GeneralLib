package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.generallib.pluginbase.manager.RegionManager;

public class HandleHangingBreak extends DefaultHandle implements RegionManager.EventHandle<HangingBreakByEntityEvent> {
    public HandleHangingBreak(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(HangingBreakByEntityEvent e) {
        return e.getRemover();
    }

    @Override
    public Location getLocation(HangingBreakByEntityEvent e) {
        return e.getEntity().getLocation();
    }
}
