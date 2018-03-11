package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.generallib.pluginbase.manager.RegionManager;

public class HandleHopperMoveItem extends DefaultHandle implements RegionManager.EventHandle<InventoryMoveItemEvent> {
    public HandleHopperMoveItem(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(InventoryMoveItemEvent e) {
        return null;
    }

    @Override
    public Location getLocation(InventoryMoveItemEvent e) {
        return e.getSource().getLocation();
    }
}
