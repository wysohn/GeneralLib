package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.generallib.pluginbase.manager.RegionManager;

public class HandleBlockBreak extends DefaultHandle implements RegionManager.EventHandle<BlockBreakEvent> {
    public HandleBlockBreak(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(BlockBreakEvent e) {
        return e.getPlayer();
    }

    @Override
    public Location getLocation(BlockBreakEvent e) {
        return e.getBlock().getLocation();
    }
}
