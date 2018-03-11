package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.generallib.pluginbase.manager.RegionManager;

public class HandleExplosionPrime extends DefaultHandle implements RegionManager.EventHandle<ExplosionPrimeEvent> {
    public HandleExplosionPrime(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(ExplosionPrimeEvent e) {
        return null;
    }

    @Override
    public Location getLocation(ExplosionPrimeEvent e) {
        return e.getEntity().getLocation();
    }
}
