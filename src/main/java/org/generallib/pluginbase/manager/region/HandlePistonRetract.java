package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.generallib.pluginbase.constants.ClaimInfo;
import org.generallib.pluginbase.manager.RegionManager;

import java.util.Iterator;

public class HandlePistonRetract extends DefaultHandle implements RegionManager.EventHandle<BlockPistonRetractEvent> {
    public HandlePistonRetract(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(BlockPistonRetractEvent e) {
        return null;
    }

    @Override
    public Location getLocation(BlockPistonRetractEvent e) {
        for(Iterator<Block> iter = e.getBlocks().iterator(); iter.hasNext();){
            Location location = iter.next().getLocation();
            ClaimInfo info = getInfo(location);
            if(info != null)
                return location;
        }
        return null;
    }
}
