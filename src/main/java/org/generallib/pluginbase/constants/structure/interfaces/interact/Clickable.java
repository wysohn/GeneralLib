package org.generallib.pluginbase.constants.structure.interfaces.interact;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.generallib.pluginbase.PluginBase;
import org.generallib.pluginbase.constants.structure.interfaces.filter.EntityFilter;

public interface Clickable {
    void onClick(PluginBase base, Action action, Player player, EntityFilter<Player> filter);
}
