package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.generallib.pluginbase.manager.RegionManager;

public class HandleProjectileLaunch extends DefaultHandle implements RegionManager.EventHandle<ProjectileLaunchEvent> {
    public HandleProjectileLaunch(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(ProjectileLaunchEvent e) {
        return (Entity) e.getEntity().getShooter();
    }

    @Override
    public Location getLocation(ProjectileLaunchEvent e) {
        return e.getEntity().getLocation();
    }
}
