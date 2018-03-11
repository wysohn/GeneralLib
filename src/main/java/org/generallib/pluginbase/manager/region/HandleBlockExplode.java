package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockExplodeEvent;
import org.generallib.pluginbase.constants.ClaimInfo;
import org.generallib.pluginbase.manager.RegionManager;

import java.util.Iterator;

public class HandleBlockExplode extends DefaultHandle implements RegionManager.EventHandle<BlockExplodeEvent> {
    public HandleBlockExplode(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(BlockExplodeEvent e) {
        return null;
    }

    @Override
    public Location getLocation(BlockExplodeEvent e) {
        for(Iterator<Block> iter = e.blockList().iterator(); iter.hasNext();){
            Location location = iter.next().getLocation();
            ClaimInfo claimInfo = getInfo(location);
            if(claimInfo != null)
                return location;
        }

        return null;
    }
}
