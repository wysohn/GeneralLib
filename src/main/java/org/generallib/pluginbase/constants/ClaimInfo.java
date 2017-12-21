package org.generallib.pluginbase.constants;

import java.util.Set;
import java.util.UUID;

import org.generallib.pluginbase.manager.ElementCachingManager.NamedElement;

public interface ClaimInfo extends NamedElement {

    @Override
    String getName();

    void setArea(Area area);

    Area getArea();

    void setPublic(boolean bool);

    boolean isPublic();

    UUID getOwner();

    void setOwner(UUID uuid);

    Set<UUID> getTrusts();

}