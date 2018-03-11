package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.generallib.pluginbase.constants.ClaimInfo;
import org.generallib.pluginbase.manager.RegionManager;

import java.util.Iterator;

public class HandlePistonExtend extends DefaultHandle implements RegionManager.EventHandle<BlockPistonExtendEvent> {
    public HandlePistonExtend(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(BlockPistonExtendEvent e) {
        return null;
    }

    @Override
    public Location getLocation(BlockPistonExtendEvent e) {
        for(Iterator<Block> iter = e.getBlocks().iterator(); iter.hasNext();){
            Location location = iter.next().getLocation();
            ClaimInfo info = getInfo(location);
            if(info != null)
                return location;
        }
        return null;
    }
}
