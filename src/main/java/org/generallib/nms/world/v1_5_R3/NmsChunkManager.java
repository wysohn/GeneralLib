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
package org.generallib.nms.world.v1_5_R3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_5_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_5_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_5_R3.generator.NormalChunkGenerator;
import org.bukkit.entity.Player;
import org.generallib.main.FakePlugin;
import org.generallib.nms.world.BlockFilter;
import org.generallib.nms.world.INmsWorldManager;

import net.minecraft.server.v1_5_R3.ChunkProviderServer;
import net.minecraft.server.v1_5_R3.IChunkLoader;
import net.minecraft.server.v1_5_R3.Packet51MapChunk;
import net.minecraft.server.v1_5_R3.WorldServer;

public class NmsChunkManager implements INmsWorldManager {
    private static Map<String, ChunkProviderServer> _serv = new ConcurrentHashMap<String, ChunkProviderServer>();

    private void initNatural(World w) {
        if (!_serv.containsKey(w.getName())) {
            CraftWorld cw = (CraftWorld) w;
            WorldServer ws = cw.getHandle();
            IChunkLoader loader = ws.getDataManager().createChunkLoader(ws.worldProvider);
            NormalChunkGenerator _gen = new NormalChunkGenerator(ws, w.getSeed());
            _serv.put(w.getName(), new ChunkProviderServer(ws, loader, _gen));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.generallib.nms.chunk.IChunkGenerator#regenerateChunk(org.bukkit.
     * World, int, int, org.generallib.nms.chunk.ChunkRegenerator.BlockFilter)
     */
    @Override
    public void regenerateChunk(World w, int i, int j, BlockFilter filter) {
        initNatural(w);

        Chunk c = _serv.get(w.getName()).getChunkAt(i, j).bukkitChunk;
        Chunk chunk = w.getChunkAt(i, j);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 128; y++) {
                    final Block block = c.getBlock(x, y, z);
                    if (!filter.allow(block.getTypeId(), block.getData()))
                        continue;

                    final Block target = chunk.getBlock(x, y, z);

                    Bukkit.getScheduler().runTask(FakePlugin.instance, new Runnable() {
                        @Override
                        public void run() {
                            target.setTypeId(block.getTypeId());
                            target.setData(block.getData());
                        }
                    });

                }
            }
        }
    }

    @Override
    public void sendChunkMapPacket(Player player, Chunk chunk) {
        CraftPlayer cp = (CraftPlayer) player;
        CraftChunk cc = (CraftChunk) chunk;

        Packet51MapChunk packet = new Packet51MapChunk(cc.getHandle(), true, 0);

        cp.getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public void sendUnloadChunkPacket(Player player, int x, int z) {

    }
}
