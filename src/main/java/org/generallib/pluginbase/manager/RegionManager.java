package org.generallib.pluginbase.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.generallib.location.utils.LocationUtil;
import org.generallib.pluginbase.PluginBase;
import org.generallib.pluginbase.constants.Area;
import org.generallib.pluginbase.constants.ClaimInfo;
import org.generallib.pluginbase.constants.SimpleChunkLocation;
import org.generallib.pluginbase.constants.SimpleLocation;
import org.generallib.pluginbase.language.DefaultLanguages;

public abstract class RegionManager<T extends PluginBase, V extends ClaimInfo> extends ElementCachingManager<Area, V>
        implements Listener {
    private final Map<SimpleChunkLocation, Set<Area>> regionsCache = new HashMap<>();
    private final Set<Class<? extends Event>> registeredEventTypes = new HashSet<>();

    private GeneralEventHandle generalEventHandle = (e, loc, entity) -> {
        return false;
    };

    public RegionManager(T base, int loadPriority) {
        super(base, loadPriority);
    }

    @Override
    protected void onEnable() throws Exception {
        super.onEnable();
    }

    @Override
    protected Area createKeyFromString(String str) {
        return Area.fromString(str);
    }

    @Override
    protected CacheUpdateHandle<Area, V> getUpdateHandle() {
        return updateHandle;
    }

    @Override
    protected CacheDeleteHandle<Area, V> getDeleteHandle() {
        return deleteHandle;
    }

    protected void setGeneralEventHandle(GeneralEventHandle generalEventHandle) {
        this.generalEventHandle = generalEventHandle;
    }

    public Set<Class<? extends Event>> getRegisteredEventTypes() {
        return registeredEventTypes;
    }

    protected void initEvent(Class<? extends Event> event, final EventHandle eventHandle) {
        registeredEventTypes.add(event);

        Bukkit.getPluginManager().registerEvent(event, this, EventPriority.NORMAL, new EventExecutor() {

            @Override
            public void execute(Listener arg0, Event arg1) throws EventException {
                Location loc = eventHandle.getLocation(arg1);
                if (loc == null)
                    return;

                Entity cause = eventHandle.getCause(arg1);

                // canceled
                if (generalEventHandle != null && generalEventHandle.preEvent(arg1, loc, cause)) {
                    return;
                }

                SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);

                V claim = RegionManager.this.getAreaInfo(sloc);
                if (claim == null)
                    return;

                // don't protect if chunk is public
                if (claim.isPublic())
                    return;

                if (cause != null && cause instanceof Player) {
                    Player p = (Player) cause;

                    if (p.isOp())
                        return;

                    UUID uuid = p.getUniqueId();

                    if (uuid.equals(claim.getOwner()))
                        return;

                    if (claim.getTrusts().contains(uuid))
                        return;
                }

                if (cause instanceof Player) {
                    base.sendMessage(cause, DefaultLanguages.General_NotEnoughPermission);
                }

                // canceled
                if (generalEventHandle != null) {
                    generalEventHandle.postEvent(arg1);
                }
            }

        }, base);
    }

    public V getAreaInfo(SimpleLocation sloc) {
        if (sloc == null)
            return null;

        SimpleChunkLocation scloc = new SimpleChunkLocation(sloc);
        synchronized (regionsCache) {
            if (!regionsCache.containsKey(scloc))
                return null;
        }

        synchronized (regionsCache) {
            Set<Area> areas = regionsCache.get(scloc);
            if (areas != null) {
                for (Area area : areas)
                    if (area.isInThisArea(sloc))
                        return this.get(area, false);
            }
        }

        return null;
    }

    public V getAreaInfo(String name) {
        if (name == null)
            return null;

        return this.get(name, false);
    }

    /**
     * Set info of area. If 'info' is null, the data connected with key 'area'
     * will be removed.
     *
     * @param area
     * @param info
     */
    public void setAreaInfo(Area area, V info) {
        // first schedule update task
        this.save(area, info);

        synchronized (regionsCache) {
            // clean up cache
            removeAreaCache(area);

            // don't cache again if deleting info
            if (info != null) {
                // re-cache claim info
                setAreaCache(area);
            }
        }
    }

    public void removeAreaInfo(Area area) {
        // first schedule update task
        this.save(area, null);

        synchronized (regionsCache) {
            // clean up cache
            removeAreaCache(area);
        }
    }

    /**
     *
     * @param before
     * @param after
     * @return false if area info of 'before' doesn't exist, or area info of
     *         'after' already exist; true otherwise.
     */
    public boolean resizeArea(Area before, Area after) {
        if (this.get(after, false) != null)
            return false;

        V info = this.get(before, false);
        if (info == null)
            return false;

        // first schedule update task
        this.save(before, null);
        this.save(after, info, new SaveHandle() {

            @Override
            public void preSave() {
                info.setArea(after);
            }

            @Override
            public void postSave() {
                synchronized (regionsCache) {
                    // clean up cache
                    removeAreaCache(before);

                    // re-cache claim info
                    setAreaCache(after);
                }
            }

        });

        return true;
    }

    /**
     * This method is not thread safe.
     *
     * @param area
     * @param info
     */
    private void setAreaCache(Area area) {
        for (SimpleChunkLocation scloc : Area.getAllChunkLocations(area)) {
            Set<Area> areas = regionsCache.get(scloc);
            if (areas == null) {
                areas = new HashSet<>();
                regionsCache.put(scloc, areas);
            }

            areas.add(area);
        }
    }

    /**
     * This method is not thread safe.
     *
     * @param area
     */
    private void removeAreaCache(Area area) {
        synchronized (regionsCache) {
            for (SimpleChunkLocation scloc : Area.getAllChunkLocations(area)) {
                Set<Area> areas = regionsCache.get(scloc);
                if (areas == null)
                    continue;

                areas.remove(area);
            }
        }
    }

    /**
     * get all the area that is conflicting with given area. This does not
     * include the area itself. It's quite a CPU intensive work; use it wisely
     *
     * @param area
     * @return never be null; can be empty if no conflicts are found
     */
    public Set<Area> getConflictingAreas(Area area) {
        Set<Area> conflicts = new HashSet<>();

        Set<SimpleChunkLocation> sclocs = Area.getAllChunkLocations(area);
        synchronized (regionsCache) {
            for (SimpleChunkLocation scloc : sclocs) {
                Set<Area> areas = this.regionsCache.get(scloc);
                if (areas == null)
                    continue;

                for (Area areaOther : areas) {
                    if (area.equals(areaOther))
                        continue;

                    if (Area.isConflicting(area, areaOther)) {
                        conflicts.add(areaOther);
                    }
                }
            }
        }

        return conflicts;
    }

    private final CacheUpdateHandle<Area, V> updateHandle = new CacheUpdateHandle<Area, V>() {

        @Override
        public V onUpdate(Area key, V original) {
            original.setArea(key);
            setAreaCache(key);
            return null;
        }

    };

    private final CacheDeleteHandle<Area, V> deleteHandle = new CacheDeleteHandle<Area, V>() {

        @Override
        public void onDelete(Area key, V value) {
            removeAreaCache(key);
        }

    };

    /**
     * The handle that is responsible for each Bukkit API events.
     *
     * @author wysohn
     *
     */
    protected interface EventHandle {
        Entity getCause(Event e);

        Location getLocation(Event e);
    }

    /**
     * This handle can be used to do something before events are passed to each
     * EventHandles. For example, it is tedious to check if a player has bypass
     * permission in every single EventHandles; if you were to use
     * GeneralEventHandle, you can simply check it before the events are passed
     * to the EventHandles.
     *
     * @author wysohn
     *
     */
    protected interface GeneralEventHandle {
        /**
         * This method will be invoked before any events will be hand over to
         * the EventHandles.
         *
         * @param e
         *            event to handle
         * @param cause
         *            the entity caused the event
         * @param loc
         *            location where event occur
         * @return true if event should not be received by all EventHandles;
         *         false otherwise.
         */
        public boolean preEvent(Event e, Location loc, Entity cause);

        /**
         * This method will be invoked after all events are handed over to the
         * EventHandles. Default behavior is canceling event if it's instance of
         * Cancellable
         *
         * @param e
         *            event to handle
         */
        default public void postEvent(Event e) {
            if (e instanceof Cancellable)
                ((Cancellable) e).setCancelled(true);
        }
    }
}
