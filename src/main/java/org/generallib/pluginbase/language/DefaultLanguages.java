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
package org.generallib.pluginbase.language;

import org.generallib.pluginbase.PluginLanguage.Language;

public enum DefaultLanguages implements Language {
	Plugin_NotEnabled("Plugin is not enabled. "),
	Plugin_SetEnableToTrue("Please check your setting at config.yml to make sure it's enabled."),
	Plugin_WillBeDisabled("Plugin will be disabled."),

	General_NotANumber("&c${string} is not a number!"),
	General_OutOfBound("&c${string} is out of bound!"),
	General_OutOfBound_RangeIs("&crange: &6${integer} &7< &fvalue &7< &6${integer}"),
	General_Header("&7======== &6${string}&7 ========"),
	General_InvalidType("&c${string} is not a valid type!"),
	General_NoSuchPlayer("&cNo such player named ${string}!"),
	General_NoSuchCommand("&cNo such command ${string}!"),
	General_Allow("&aAllow"),
	General_Deny("&cDeny"),
	General_NotABoolean("&c${string} is not a boolean!"),
	General_NotEnoughPermission("&cYou don't have enough permission!"),

	Economy_NotEnoughMoney("&cNot enough money! Required:[&6${double}&c]"),
	Economy_TookMoney("&aTook [&6${double}&a] from your account!"),

	Command_Format_Description("&6/${string} ${string} &5- &7${string}"),
	Command_Format_Aliases("  &5Aliases&7: &a${string}"),
	Command_Format_Usage("  &7${string}"),

	Command_Help_PageDescription("&6Page &7${integer}/${integer}"),
	Command_Help_TypeHelpToSeeMore("&6Type &6/${string} help &7<page> &6to see next pages."),
	Command_Help_Description("Show all commands and its desriptions of this plugin."),
	Command_Help_Usage("<page> for page to see."),

	Command_Reload_Description("reload config"),
	Command_Reload_Usage("to reload config"),

	Command_Import_Description("DB types: ${dbtype}"),
	Command_Import_Usage("<dbtype> to import data from <dbtype>."),
	;

	private final String[] englishDefault;
	private DefaultLanguages(String... englishDefault){
		this.englishDefault = englishDefault;
	}

	@Override
	public String[] getEngDefault() {
		return englishDefault;
	}
}
