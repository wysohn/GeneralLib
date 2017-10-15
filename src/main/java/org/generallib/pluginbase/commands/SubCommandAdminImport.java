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

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.generallib.database.tasks.DatabaseTransferTask;
import org.generallib.database.tasks.DatabaseTransferTask.TransferPair;
import org.generallib.pluginbase.PluginBase;
import org.generallib.pluginbase.language.DefaultLanguages;
import org.generallib.pluginbase.PluginLanguage.Language;
import org.generallib.pluginbase.PluginManager;

public class SubCommandAdminImport extends SubCommandAdmin {
    public SubCommandAdminImport(PluginBase base, String permission) {
        super(base, permission, DefaultLanguages.Command_Import_Description,
                new Language[] { DefaultLanguages.Command_Import_Usage }, 1, "import");
    }

    @Override
    protected boolean executeConsole(CommandSender sender, String[] args) {
        String fromName = args[0];

        Set<TransferPair> pairs = new HashSet<TransferPair>();

        for (Entry<Class<? extends PluginManager>, PluginManager> entry : base.getPluginManagers().entrySet()) {
            Set<String> allowedTypes = entry.getValue().getValidDBTypes();
            if (!allowedTypes.contains(fromName)) {
                base.getLogger().severe(entry.getKey().getSimpleName() + "@Invalid db type -- " + fromName);
                return false;
            }

            Set<TransferPair> pair = entry.getValue().getTransferPair(fromName);
            if (pair != null)
                pairs.addAll(pair);
        }

        new Thread(new DatabaseTransferTask(base, pairs)).start();

        return true;
    }
}
