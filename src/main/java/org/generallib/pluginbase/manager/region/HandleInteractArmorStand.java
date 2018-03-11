package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.generallib.pluginbase.manager.RegionManager;

public class HandleInteractArmorStand extends DefaultHandle implements RegionManager.EventHandle<PlayerArmorStandManipulateEvent> {
    public HandleInteractArmorStand(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(PlayerArmorStandManipulateEvent e) {
        return e.getPlayer();
    }

    @Override
    public Location getLocation(PlayerArmorStandManipulateEvent e) {
        return e.getRightClicked().getLocation();
    }
}
