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
package org.generallib.main;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.generallib.nms.entity.INmsEntityManager;
import org.generallib.nms.particle.INmsParticleSender;
import org.generallib.nms.world.BlockFilter;
import org.generallib.nms.world.INmsWorldManager;

//just a fake plugin
public class FakePlugin extends JavaPlugin{
	public static Plugin instance;

	public static INmsWorldManager nmsWorldManager;
	public static INmsEntityManager nmsEntityManager;
	public static INmsParticleSender nmsParticleSender;

	@Override
	public void onEnable() {
		instance = this;

		String packageName = getServer().getClass().getPackage().getName();
		String version = packageName.substring(packageName.lastIndexOf('.') + 1);

		try {
			initWorldNms(version);
			initEntityrNms(version);
			initParticleNms(version);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			getLogger().severe("Version ["+version+"] is not supported by this plugin.");
			this.setEnabled(false);
		}
	}

	private static final String packageName = "org.generallib.nms";
	private void initWorldNms(String version) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		Class<?> clazz = Class.forName(packageName+".world."+version+"."+"NmsChunkManager");
		nmsWorldManager = (INmsWorldManager) clazz.newInstance();
	}

	private void initEntityrNms(String version) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		Class<?> clazz = Class.forName(packageName+".entity."+version+"."+"NmsEntityProvider");
		nmsEntityManager = (INmsEntityManager) clazz.newInstance();
	}

	private void initParticleNms(String version) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		Class<?> clazz = Class.forName(packageName+".particle."+version+"."+"NmsParticleSender");
		nmsParticleSender = (INmsParticleSender) clazz.newInstance();
	}

	private static Set<Integer> ores = new HashSet<Integer>(){{
		for(Material mat : Material.values())
			if(mat.name().endsWith("_ORE"))
				add(mat.getId());
	}};
	private static UUID temp = UUID.randomUUID();
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player && !((Player) sender).isOp())
			return true;

		if(!label.equals("glib"))
			return true;

		try{
			if(args.length == 1){
				if(args[0].equalsIgnoreCase("glow")){
					Player player = (Player) sender;
					int x = player.getLocation().getBlockX();
					int y = player.getLocation().getBlockY();
					int z = player.getLocation().getBlockZ();

					nmsParticleSender.showGlowingBlock(new Player[]{player}, -700, temp, x, y, z);
				} else if (args[0].equalsIgnoreCase("del")) {
					Player player = (Player) sender;

					nmsEntityManager.destroyEntity(new Player[] { player }, new int[] { -700 });
				} else if (args[0].equalsIgnoreCase("color")) {
					Player player = (Player) sender;

					nmsEntityManager.sendTeamColor(new Player[] { player }, "temp", ChatColor.RED + "",
							new HashSet<String>() {
								{
									add(temp.toString());
								}
							}, 2);
				}
			}else if(args.length == 3){
				if(args[0].equalsIgnoreCase("chunk")){
					Player player = (Player) sender;
					int i = Integer.parseInt(args[1]);
					int j = Integer.parseInt(args[2]);

					nmsWorldManager.regenerateChunk(player.getWorld(), i, j, new BlockFilter(){
						@Override
						public boolean allow(int blockID, byte data) {
							return ores.contains(blockID);
						}
					});
				}
			}else if(args.length == 4){
				if(args[0].equalsIgnoreCase("chunk")){
					World world = Bukkit.getWorld(args[3]);
					int i = Integer.parseInt(args[1]);
					int j = Integer.parseInt(args[2]);

					nmsWorldManager.regenerateChunk(world, i, j, new BlockFilter(){
						@Override
						public boolean allow(int blockID, byte data) {
							return ores.contains(blockID);
						}
					});
				}
			}
		}catch(Exception e){
			sender.sendMessage(ChatColor.RED+e.getMessage());
			return true;
		}

		return true;
	}
}
