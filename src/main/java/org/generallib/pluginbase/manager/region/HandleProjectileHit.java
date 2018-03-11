package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.generallib.pluginbase.manager.RegionManager;

public class HandleProjectileHit extends DefaultHandle implements RegionManager.EventHandle<ProjectileHitEvent> {
    public HandleProjectileHit(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(ProjectileHitEvent e) {
        return (Entity) e.getEntity().getShooter();
    }

    @Override
    public Location getLocation(ProjectileHitEvent e) {
        return e.getEntity().getLocation();
    }
}
