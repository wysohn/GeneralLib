package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.generallib.pluginbase.manager.RegionManager;

public class HandleBucketEmpty extends DefaultHandle implements RegionManager.EventHandle<PlayerBucketEmptyEvent> {
    public HandleBucketEmpty(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(PlayerBucketEmptyEvent e) {
        return e.getPlayer();
    }

    @Override
    public Location getLocation(PlayerBucketEmptyEvent e) {
        return e.getBlockClicked().getLocation();
    }
}
