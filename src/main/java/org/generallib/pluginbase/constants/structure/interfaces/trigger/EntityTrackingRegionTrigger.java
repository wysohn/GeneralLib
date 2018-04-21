package org.generallib.pluginbase.constants.structure.interfaces.trigger;

import java.lang.ref.WeakReference;

import org.bukkit.entity.Entity;

public interface EntityTrackingRegionTrigger extends RegionTrigger {
    void addEntity(WeakReference<Entity> entity);
    void removeEntity(WeakReference<Entity> entity);
}
