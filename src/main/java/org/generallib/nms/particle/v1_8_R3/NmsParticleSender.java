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
package org.generallib.nms.particle.v1_8_R3;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.generallib.nms.particle.INmsParticleSender;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;

public class NmsParticleSender implements INmsParticleSender {
    @Override
    public void sendPlayerOutParticle(Player[] player, int id, boolean distance, Location loc, int red, int green,
            int blue, int speed, int count) {
        sendPlayerOutParticle(player, id, distance, loc.getX(), loc.getY(), loc.getZ(), red, green, blue, speed, count);
    }

    @Override
    public void sendPlayerOutParticle(Player[] player, int id, boolean distance, double x, double y, double z, int red,
            int green, int blue, int speed, int count) {
        if (player.length == 0)
            return;

        int view = Bukkit.getServer().getViewDistance();

        for (Player p : player) {
            if (p == null)
                continue;

            if (y > p.getWorld().getMaxHeight())
                continue;
            Location loc = p.getLocation();

            int centerX = loc.getBlockX();
            int centerZ = loc.getBlockZ();

            if (!(centerX - view * 16 <= x && x <= centerX + view * 16))
                continue;
            if (!(centerZ - view * 16 <= z && z <= centerZ + view * 16))
                continue;

            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.a(id), // 30
                                                                                                     // -
                                                                                                     // red
                                                                                                     // dust
                    distance, (float) x, (float) y, (float) z, red / 255, green / 255, blue / 255, speed, // 1
                    count);// 0

            CraftPlayer cp = (CraftPlayer) p;
            EntityPlayer ep = cp.getHandle();
            PlayerConnection conn = ep.playerConnection;
            conn.sendPacket(packet);
        }
    }

    @Override
    public void showGlowingBlock(Player[] player, int entityID, UUID uuid, int x, int y, int z) {
        // TODO Auto-generated method stub

    }

}
