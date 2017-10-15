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

import java.util.UUID;

import org.bukkit.entity.Player;

public abstract class PlayerSession {
    private final UUID uuid;
    private transient Player player = null;

    public PlayerSession(UUID uuid) {
        this.uuid = uuid;
    }

    public PlayerSession(Player player) {
        this(player.getUniqueId());
        this.player = player;
    }

    public boolean isOnline() {
        return player != null && player.isOnline();
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public UUID getUuid() {
        return uuid;
    }

}
