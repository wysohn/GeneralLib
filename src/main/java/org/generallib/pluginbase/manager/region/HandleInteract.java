package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.generallib.pluginbase.manager.RegionManager;

public class HandleInteract extends DefaultHandle implements RegionManager.EventHandle<PlayerInteractEvent> {
    public HandleInteract(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(PlayerInteractEvent e) {
        return e.getPlayer();
    }

    @Override
    public Location getLocation(PlayerInteractEvent e) {
        if(e.getClickedBlock() == null)
            return null;

        return e.getClickedBlock().getLocation();
    }
}
