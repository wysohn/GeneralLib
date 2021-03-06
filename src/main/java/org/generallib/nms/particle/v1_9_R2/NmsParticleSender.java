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
package org.generallib.nms.particle.v1_9_R2;

import java.lang.reflect.Field;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.generallib.nms.particle.INmsParticleSender;
import org.generallib.reflection.utils.ReflectionHelper;

import net.minecraft.server.v1_9_R2.DataWatcherObject;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityShulker;
import net.minecraft.server.v1_9_R2.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_9_R2.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.EnumParticle;
import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import net.minecraft.server.v1_9_R2.PacketPlayOutChat;
import net.minecraft.server.v1_9_R2.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_9_R2.PlayerConnection;
import net.minecraft.server.v1_9_R2.IChatBaseComponent.ChatSerializer;

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
        for (Player p : player) {
            CraftPlayer cp = (CraftPlayer) p;
            EntityPlayer ep = cp.getHandle();

            CraftWorld world = (CraftWorld) p.getWorld();
            EntityShulker shulker = new EntityShulker(world.getHandle());
            shulker.setPosition(x, y, z);
            shulker.positionChanged = false;
            shulker.glowing = true;

            Field Z = null;
            DataWatcherObject<Byte> bitmasks = null;
            try {
                Z = Entity.class.getDeclaredField("Z");
                Z.setAccessible(true);
                bitmasks = (DataWatcherObject<Byte>) Z.get(null);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }

            shulker.getDataWatcher().set(bitmasks, (byte) 0x40);
            // block spawn
            /////////////////////////////////////////////////////////
            PacketPlayOutSpawnEntityLiving pposel = new PacketPlayOutSpawnEntityLiving(shulker);
            ReflectionHelper.setPrivateField(pposel, "a", entityID);
            ReflectionHelper.setPrivateField(pposel, "b", uuid);

            ep.playerConnection.sendPacket(pposel);
            /////////////////////////////////////////////////////////
            PacketPlayOutEntityMetadata ppoem = new PacketPlayOutEntityMetadata(entityID, shulker.getDataWatcher(),
                    true);

            ep.playerConnection.sendPacket(ppoem);
        }
    }
}
