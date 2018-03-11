package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.generallib.pluginbase.manager.RegionManager;

public class HandleInteractEntity extends DefaultHandle implements RegionManager.EventHandle<PlayerInteractEntityEvent> {
    public HandleInteractEntity(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(PlayerInteractEntityEvent e) {
        return e.getPlayer();
    }

    @Override
    public Location getLocation(PlayerInteractEntityEvent e) {
        return e.getRightClicked().getLocation();
    }
}
