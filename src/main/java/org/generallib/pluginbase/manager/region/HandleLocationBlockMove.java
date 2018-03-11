package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.generallib.location.utils.LocationUtil;
import org.generallib.pluginbase.constants.ClaimInfo;
import org.generallib.pluginbase.manager.RegionManager;
import org.generallib.pluginbase.manager.event.PlayerBlockLocationEvent;

public class HandleLocationBlockMove extends DefaultHandle implements RegionManager.EventHandle<PlayerBlockLocationEvent> {
    public HandleLocationBlockMove(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(PlayerBlockLocationEvent e) {
        return e.getPlayer();
    }

    @Override
    public Location getLocation(PlayerBlockLocationEvent e) {
        ClaimInfo from = getInfo(e.getFrom());
        ClaimInfo to = getInfo(e.getTo());

        if(from != null){//prevent area exit
            if(to == null)
                return LocationUtil.convertToBukkitLocation(e.getFrom());

            //both direction
            if(!from.getArea().equals(to.getArea()))
                return LocationUtil.convertToBukkitLocation(e.getFrom());

            return null;
        }else if(to != null){//prevent area enter
            //from is always null
            return LocationUtil.convertToBukkitLocation(e.getTo());
        }else{//nothing
            return null;
        }
    }
}
