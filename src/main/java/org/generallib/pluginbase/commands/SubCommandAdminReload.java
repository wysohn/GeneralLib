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

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.generallib.pluginbase.PluginBase;
import org.generallib.pluginbase.PluginLanguage.Language;
import org.generallib.pluginbase.language.DefaultLanguages;

public class SubCommandAdminReload extends SubCommandAdmin {

    public SubCommandAdminReload(PluginBase base, String permission) {
        super(base, permission, DefaultLanguages.Command_Reload_Description,
                new Language[] { DefaultLanguages.Command_Reload_Usage }, 0, "reload");
    }

    @Override
    protected boolean executeConsole(CommandSender sender, String[] args) {
        base.reloadPluginProcedures();
        base.getLogger().info("Plugin is reloaded.");

        return true;
    }

    @Override
    protected boolean executeOp(Player op, String[] args) {
        base.reloadPluginProcedures();
        base.getLogger().info("Plugin is reloaded.");

        return true;
    }

}
