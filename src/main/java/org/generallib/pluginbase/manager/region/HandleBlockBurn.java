package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBurnEvent;
import org.generallib.pluginbase.manager.RegionManager;

public class HandleBlockBurn extends DefaultHandle implements RegionManager.EventHandle<BlockBurnEvent> {
    public HandleBlockBurn(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(BlockBurnEvent e) {
        return null;
    }

    @Override
    public Location getLocation(BlockBurnEvent e) {
        return e.getBlock().getLocation();
    }
}
