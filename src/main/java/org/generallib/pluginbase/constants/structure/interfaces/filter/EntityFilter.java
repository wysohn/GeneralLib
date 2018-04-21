package org.generallib.pluginbase.constants.structure.interfaces.filter;

import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.generallib.pluginbase.constants.structure.Structure;

public interface EntityFilter<E extends Entity> {
    boolean isPermitted(Structure structure, E entity);
}
