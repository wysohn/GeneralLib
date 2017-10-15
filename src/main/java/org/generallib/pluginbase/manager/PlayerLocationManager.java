/*******************************************************************************
 *     Copyright (C) 2017 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.generallib.pluginbase.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.generallib.location.utils.LocationUtil;
import org.generallib.pluginbase.PluginBase;
import org.generallib.pluginbase.PluginManager;
import org.generallib.pluginbase.constants.SimpleChunkLocation;
import org.generallib.pluginbase.constants.SimpleLocation;
import org.generallib.pluginbase.manager.event.PlayerBlockLocationEvent;
import org.generallib.pluginbase.manager.event.PlayerChunkLocationEvent;

public class PlayerLocationManager<T extends PluginBase> extends PluginManager<T> implements Listener {
    private transient Map<UUID, SimpleLocation> locations = new ConcurrentHashMap<>();

    public PlayerLocationManager(T base, int loadPriority) {
        super(base, loadPriority);
    }

    @Override
    protected void onDisable() throws Exception {

    }

    @Override
    protected void onEnable() throws Exception {

    }

    @Override
    protected void onReload() throws Exception {

    }

    /**
     * get location of player
     * 
     * @param uuid
     *            uuid of player
     * @return the location. If the player just logged in, it might be null.
     */
    public SimpleLocation getCurrentBlockLocation(UUID uuid) {
        return locations.get(uuid);
    }

    /**
     * set current location of the player
     * 
     * @param uuid
     *            the player's uuid
     * @param sloc
     *            the location where player is at
     */
    protected void setCurrentBlockLocation(UUID uuid, SimpleLocation sloc) {
        locations.put(uuid, sloc);
    }

    /**
     * remove the current location of the player.
     * 
     * @param uuid
     *            the player's uuid
     */
    protected void removeCurrentBlockLocation(UUID uuid) {
        locations.remove(uuid);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Location loc = player.getLocation();
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        setCurrentBlockLocation(player.getUniqueId(), sloc);
    }

    @EventHandler
    public void onSpawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        Location loc = player.getLocation();
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        setCurrentBlockLocation(player.getUniqueId(), sloc);
    }

    @EventHandler
    public void onTeleport(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();
        Location loc = player.getLocation();
        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(loc);
        setCurrentBlockLocation(player.getUniqueId(), sloc);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        removeCurrentBlockLocation(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo() == e.getFrom())
            return;

        Player player = e.getPlayer();

        SimpleLocation from = getCurrentBlockLocation(player.getUniqueId());
        SimpleLocation to = LocationUtil.convertToSimpleLocation(e.getTo());

        if (from.equals(to))
            return;

        SimpleChunkLocation fromChunk = new SimpleChunkLocation(from);
        SimpleChunkLocation toChunk = new SimpleChunkLocation(to);

        boolean cancelled = false;
        if (!fromChunk.equals(toChunk)) {
            PlayerChunkLocationEvent pcle = new PlayerChunkLocationEvent(player, fromChunk, toChunk);
            Bukkit.getPluginManager().callEvent(pcle);
            if (pcle.isCancelled())
                cancelled = true;
        }

        PlayerBlockLocationEvent pble = new PlayerBlockLocationEvent(player, from, to);
        Bukkit.getPluginManager().callEvent(pble);
        if (pble.isCancelled())
            cancelled = true;

        if (cancelled) {
            e.setCancelled(true);

            Location loc = LocationUtil.convertToBukkitLocation(from);
            loc.setPitch(e.getPlayer().getLocation().getPitch());
            loc.setYaw(e.getPlayer().getLocation().getPitch());
            e.setFrom(loc);
            e.setTo(loc);
        } else {
            setCurrentBlockLocation(player.getUniqueId(), to);
        }
    }

}
