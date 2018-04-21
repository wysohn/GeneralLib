package org.generallib.pluginbase.constants.structure.interfaces.trigger;

import org.bukkit.entity.Entity;
import org.generallib.pluginbase.PluginBase;
import org.generallib.pluginbase.constants.Area;
import org.generallib.pluginbase.constants.structure.interfaces.filter.EntityFilter;

public interface RegionTrigger extends Trigger {
    Area getArea();

    void onEnter(PluginBase base, Entity entity, EntityFilter filter);
    void onExit(PluginBase base, Entity entity, EntityFilter filter);
}
