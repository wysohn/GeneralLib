package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.generallib.pluginbase.manager.RegionManager;

public class HandleBlockPlace extends DefaultHandle implements RegionManager.EventHandle<BlockPlaceEvent> {
    public HandleBlockPlace(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(BlockPlaceEvent e) {
        return e.getPlayer();
    }

    @Override
    public Location getLocation(BlockPlaceEvent e) {
        return e.getBlock().getLocation();
    }
}
