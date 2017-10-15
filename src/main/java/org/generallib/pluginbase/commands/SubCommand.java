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
package org.generallib.pluginbase.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.generallib.pluginbase.PluginBase;
import org.generallib.pluginbase.PluginLanguage.Language;
import org.generallib.pluginbase.language.DefaultLanguages;

public abstract class SubCommand{
	protected final String name;
	protected final String[] aliases;
	protected final String permission;
	protected Language permissionDeniedMessage = DefaultLanguages.General_NotEnoughPermission;
	protected final Language description;
	protected final Language[] usage;
	
	protected final PluginBase base;
	protected ChatColor commandColor = ChatColor.GOLD;
	
	private int arguments  = -1;

	public SubCommand(PluginBase base, String permission, Language description,
			Language[] usage, int arguments, String name, String... aliases) {
		this.base = base;
		this.name = name;
		this.aliases = aliases;
		this.permission = permission;
		this.description = description;
		this.usage = usage;
		this.arguments = arguments;
	}

	public Language getPermissionDeniedMessage() {
		return permissionDeniedMessage;
	}

	public void setPermissionDeniedMessage(Language permissionDeniedMessage) {
		this.permissionDeniedMessage = permissionDeniedMessage;
	}

	public ChatColor getCommandColor() {
		return commandColor;
	}

	public int getArguments() {
		return arguments;
	}

	protected void setArguments(int arguments) {
		this.arguments = arguments;
	}

	public boolean execute(CommandSender sender, String commandLabel, String[] args) {	
		if(arguments != -1 && args.length != arguments)
			return false;
		
		if (sender == null || sender instanceof ConsoleCommandSender) {
			return executeConsole(sender, args);
		} else {
			Player player = (Player) sender;
			if (player.isOp()) {
				return executeOp(player, args);
			} else {
				return executeUser(player, args);
			}
		}
	}
	
	protected boolean executeConsole(CommandSender sender, String[] args){
		base.getLogger().info("Not allowed to execute from Console.");
		return true;
	}
	protected boolean executeOp(Player op, String[] args){
		op.sendMessage(ChatColor.RED+"Not allowed to execute from OP.");
		return true;
	}
	protected boolean executeUser(Player player, String[] args){
		player.sendMessage(ChatColor.RED+"Not allowed to execute from User.");
		return true;
	}
	
	public boolean testPermission(CommandSender sender){
		if(permission == null)
			return true;
		
		if(!testPermissionSilent(sender)){
			base.sendMessage(sender, permissionDeniedMessage);
			return false;
		}
		
		return true;
	}
	
	public boolean testPermissionSilent(CommandSender sender){
		if(permission == null)
			return true;
		
		return sender.hasPermission(permission);
	}
	
	
	
/*
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubCommand other = (SubCommand) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}
*/

	public String getName() {
		return name;
	}

	public String[] getAliases() {
		return aliases;
	}

	public String getPermission() {
		return permission;
	}

	public Language getDescription() {
		return description;
	}

	public Language[] getUsage() {
		return usage;
	}

	@Override
	public String toString() {
		return name;
	}
}
