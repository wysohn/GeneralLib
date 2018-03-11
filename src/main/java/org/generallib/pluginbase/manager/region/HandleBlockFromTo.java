package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockFromToEvent;
import org.generallib.pluginbase.constants.ClaimInfo;
import org.generallib.pluginbase.manager.RegionManager;

public class HandleBlockFromTo extends DefaultHandle implements RegionManager.EventHandle<BlockFromToEvent> {
    public HandleBlockFromTo(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(BlockFromToEvent e) {
        return null;
    }

    @Override
    public Location getLocation(BlockFromToEvent e) {
        ClaimInfo from = getInfo(e.getBlock().getLocation());
        ClaimInfo to = getInfo(e.getToBlock().getLocation());

        if(from == null && to == null){
            return null;
        }else if(to != null){
            if(from == null)
                return null;

            if(to.getArea().equals(from.getArea()))
                return null;

            return e.getToBlock().getLocation();
        }

        return null;
    }
}
