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

import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.permissions.Permission;
import org.generallib.pluginbase.PluginBase;
import org.generallib.pluginbase.PluginLanguage.Language;

public abstract class SubCommandAdmin extends SubCommand {

	public SubCommandAdmin(PluginBase base, String permission, Language description, Language[] usage, int arguments,
			String name, String... aliases) {
		super(base, permission, description, usage, arguments, name, aliases);
		commandColor = ChatColor.LIGHT_PURPLE;
	}


}
