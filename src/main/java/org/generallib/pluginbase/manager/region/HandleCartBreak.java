package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.generallib.pluginbase.manager.RegionManager;

public class HandleCartBreak extends DefaultHandle implements RegionManager.EventHandle<VehicleDestroyEvent> {
    public HandleCartBreak(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(VehicleDestroyEvent e) {
        return e.getAttacker();
    }

    @Override
    public Location getLocation(VehicleDestroyEvent e) {
        return e.getVehicle().getLocation();
    }
}
