package org.generallib.pluginbase.manager.region;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.generallib.pluginbase.constants.ClaimInfo;
import org.generallib.pluginbase.manager.RegionManager;

public class HandleHopperPickupItem extends DefaultHandle implements RegionManager.EventHandle<InventoryPickupItemEvent> {
    public HandleHopperPickupItem(RegionManager rmanager) {
        super(rmanager);
    }

    @Override
    public Entity getCause(InventoryPickupItemEvent e) {
        return null;
    }

    @Override
    public Location getLocation(InventoryPickupItemEvent e) {
        ClaimInfo claimItem = getInfo(e.getItem().getLocation());
        ClaimInfo claimInv = getInfo(e.getInventory().getLocation());

        if (claimItem != null && claimInv != null) {
            if (claimItem.getArea().equals(claimInv.getArea())) {
                return null;
            }
        }

        return e.getInventory().getLocation();
    }
}
