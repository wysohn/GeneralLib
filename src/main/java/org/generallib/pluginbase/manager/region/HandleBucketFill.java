package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.generallib.pluginbase.manager.RegionManager;

public class HandleBucketFill extends DefaultHandle implements RegionManager.EventHandle<PlayerBucketFillEvent> {
    public HandleBucketFill(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(PlayerBucketFillEvent e) {
        return e.getPlayer();
    }

    @Override
    public Location getLocation(PlayerBucketFillEvent e) {
        return e.getBlockClicked().getLocation();
    }
}
