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
package org.generallib.pluginbase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.chatlib.main.ChatLibAPI;
import org.chatlib.utils.chat.JsonMessage;
import org.chatlib.utils.chat.JsonMessagePlain;
import org.chatlib.utils.chat.handlers.JsonMessageClickEvent;
import org.chatlib.utils.chat.handlers.JsonMessageClickEvent.ClickAction;
import org.chatlib.utils.chat.handlers.JsonMessageHoverEvent;
import org.chatlib.utils.chat.handlers.JsonMessageHoverEvent.HoverAction;
import org.generallib.pluginbase.PluginLanguage.Language;
import org.generallib.pluginbase.api.ChatLibSupport;
import org.generallib.pluginbase.api.ChatLibSupport.MessageBuilder;
import org.generallib.pluginbase.commands.SubCommand;
import org.generallib.pluginbase.commands.SubCommandAdminReload;
import org.generallib.pluginbase.language.DefaultLanguages;

public final class PluginCommandExecutor implements PluginProcedure {
    private PluginBase base;

    public final String mainCommand;
    public final String adminPermission;

    private SubCommandMap commandMap;

    private final Queue<Runnable> commandAddQueue = new LinkedList<Runnable>();

    protected PluginCommandExecutor(String mainCommand, String adminPermission) {
        this.mainCommand = mainCommand;
        this.adminPermission = adminPermission;
    }

    @Override
    public void onEnable(PluginBase base) throws Exception {
        this.base = base;
        this.commandMap = new SubCommandMap();

        addCommand(new SubCommandAdminReload(base, adminPermission));

        while (!commandAddQueue.isEmpty()) {
            Runnable run = commandAddQueue.poll();

            run.run();
        }
    }

    @Override
    public void onDisable(PluginBase base) throws Exception {

    }

    @Override
    public void onReload(PluginBase base) throws Exception {

    }

    public void addCommand(final SubCommand cmd) {
        commandAddQueue.add(new Runnable() {
            @Override
            public void run() {
                commandMap.register(null, cmd);
            }
        });

    }

    public boolean onCommand(CommandSender sender, Command arg0, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            int page = 0;
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]) - 1;
                } catch (NumberFormatException ex) {
                    base.lang.addString(args[1]);
                    base.sendMessage(sender, DefaultLanguages.General_NotANumber);
                    return true;
                }
            }

            this.showHelp(label, sender, page);

            return true;
        }

        String cmdLine = "";
        for (String str : args) {
            cmdLine += str + " ";
        }

        commandMap.dispatch(sender, cmdLine);

        return true;
    }

    /**
     *
     * @param sender
     * @param page
     *            0~size()
     */
    public void showHelp(String label, final CommandSender sender, int page) {
        List<SubCommand> list = new ArrayList<SubCommand>();
        for (Entry<String, SubCommand> entry : commandList.entrySet()) {
            SubCommand cmd = entry.getValue();
            if (!cmd.testPermissionSilent(sender)) {
                continue;
            }

            list.add(cmd);
        }

        base.lang.addString(base.getDescription().getName());
        base.sendMessage(sender, DefaultLanguages.General_Header);
        sender.sendMessage("");

        int max = base.getPluginConfig().Command_Help_SentencePerPage;

        int remainder = list.size() % base.getPluginConfig().Command_Help_SentencePerPage;
        int divided = list.size() / base.getPluginConfig().Command_Help_SentencePerPage;
        int outof = remainder == 0 ? divided : divided + 1;

        page = Math.max(page, 0);
        page = Math.min(page, outof - 1);

        int index;
        for (index = page * max; index >= 0 && index < (page + 1) * max; index++) {
            if (index >= list.size())
                break;

            final SubCommand c = list.get(index);
            ChatColor color = ChatColor.GOLD;
            color = c.getCommandColor();

            String desc = base.lang.parseFirstString(sender, c.getDescription());
            base.lang.addString(label);
            base.lang.addString(color + c.toString());
            base.lang.addString(desc);
            if (base.APISupport.isHooked("ChatLib")) {
                if (sender instanceof Player) {
                    ChatLibSupport api = base.APISupport.getAPI("ChatLib");

                    MessageBuilder jsonbuilder = new MessageBuilder(
                            base.lang.parseFirstString(sender, DefaultLanguages.Command_Format_Description));

                    StringBuilder builder = new StringBuilder();

                    StringBuilder builderAliases = new StringBuilder();
                    for (String alias : c.getAliases()) {
                        builderAliases.append(" " + alias);
                    }
                    base.lang.addString(builderAliases.toString());
                    builder.append(base.lang.parseFirstString(sender, DefaultLanguages.Command_Format_Aliases) + "\n");

                    for (Language lang : c.getUsage())
                        builder.append(base.lang.parseFirstString(sender, lang) + "\n");

                    jsonbuilder.withHoverShowText(builder.toString());
                    jsonbuilder.withClickRunCommand("/" + mainCommand + " " + c.toString() + " ");

                    api.send((Player) sender, jsonbuilder.build());
                } else {
                    base.sendMessage(sender, DefaultLanguages.Command_Format_Description);

                    StringBuilder builder = new StringBuilder();
                    for (String alias : c.getAliases()) {
                        builder.append(" " + alias);
                    }
                    base.lang.addString(builder.toString());
                    base.sendMessage(sender, DefaultLanguages.Command_Format_Aliases);

                    for (Language lang : c.getUsage()) {
                        String usage = base.lang.parseFirstString(sender, lang);
                        base.lang.addString(usage);
                        base.sendMessage(sender, DefaultLanguages.Command_Format_Usage);
                    }
                }
            } else {
                base.sendMessage(sender, DefaultLanguages.Command_Format_Description);
                for (Language lang : c.getUsage()) {
                    String usage = base.lang.parseFirstString(sender, lang);
                    base.lang.addString(usage);
                    base.sendMessage(sender, DefaultLanguages.Command_Format_Usage);
                }
            }
        }

        sender.sendMessage(ChatColor.LIGHT_PURPLE + "");

        base.lang.addInteger(page + 1);
        base.lang.addInteger(outof);
        base.sendMessage(sender, DefaultLanguages.Command_Help_PageDescription);

        base.lang.addString(mainCommand);
        base.sendMessage(sender, DefaultLanguages.Command_Help_TypeHelpToSeeMore);
        if (base.APISupport.isHooked("ChatLib") && sender instanceof Player) {
            String leftArrow = ChatColor.DARK_GRAY + "[" + ChatColor.GREEN + "<---" + ChatColor.DARK_GRAY + "]";
            String home = ChatColor.DARK_GRAY + "[" + ChatColor.YELLOW + "Home" + ChatColor.DARK_GRAY + "]";
            String rightArrow = ChatColor.DARK_GRAY + "[" + ChatColor.GREEN + "--->" + ChatColor.DARK_GRAY + "]";

            final String previousCmd = "/" + mainCommand + " help " + (page);
            final String homeCmd = "/" + mainCommand + " help ";
            final String nextCmd = "/" + mainCommand + " help " + (page + 2);

            ChatLibAPI.sendJsonMessage((Player) sender,
                    ChatLibAPI.toJsonString(new JsonMessage[] { new JsonMessagePlain(leftArrow) {
                        {
                            setHoverEvent(new JsonMessageHoverEvent(HoverAction.show_text, previousCmd));
                            setClickEvent(new JsonMessageClickEvent(ClickAction.run_command, previousCmd));
                        }
                    }, new JsonMessagePlain(home) {
                        {
                            setHoverEvent(new JsonMessageHoverEvent(HoverAction.show_text, homeCmd));
                            setClickEvent(new JsonMessageClickEvent(ClickAction.run_command, homeCmd));
                        }
                    }, new JsonMessagePlain(rightArrow) {
                        {
                            setHoverEvent(new JsonMessageHoverEvent(HoverAction.show_text, nextCmd));
                            setClickEvent(new JsonMessageClickEvent(ClickAction.run_command, nextCmd));
                        }
                    }, }));
        }
        sender.sendMessage(ChatColor.GRAY + "");
    }

    private final Map<String, SubCommand> commandList = new LinkedHashMap<String, SubCommand>();
    private final Map<String, String> aliasMap = new HashMap<String, String>();

    private class SubCommandMap {
        public void clearCommands() {
            commandList.clear();
            aliasMap.clear();
        }

        public boolean dispatch(CommandSender arg0, String arg1) throws CommandException {
            String[] split = arg1.split(" ");

            String cmd = split[0];
            if (aliasMap.containsKey(cmd)) {
                cmd = aliasMap.get(cmd);
            }

            String[] args = new String[split.length - 1];
            for (int i = 1; i < split.length; i++) {
                args[i - 1] = split[i];
            }

            SubCommand command = commandList.get(cmd);

            if (command != null) {
                if (command.getArguments() != -1 && command.getArguments() != args.length) {
                    ChatColor color = command.getCommandColor();
                    String desc = base.lang.parseFirstString(arg0, command.getDescription());
                    base.lang.addString(mainCommand);
                    base.lang.addString(color + command.toString());
                    base.lang.addString(desc);
                    base.sendMessage(arg0, DefaultLanguages.Command_Format_Description);

                    StringBuilder builder = new StringBuilder();
                    for (String alias : command.getAliases()) {
                        builder.append(" " + alias);
                    }
                    base.lang.addString(builder.toString());
                    base.sendMessage(arg0, DefaultLanguages.Command_Format_Aliases);

                    for (Language lang : command.getUsage()) {
                        String usage = base.lang.parseFirstString(arg0, lang);
                        base.lang.addString(usage);
                        base.sendMessage(arg0, DefaultLanguages.Command_Format_Usage);
                    }
                    return true;
                }

                if (command.getPermission() != null && !arg0.hasPermission(adminPermission)
                        && !arg0.hasPermission(command.getPermission())) {
                    base.sendMessage(arg0, DefaultLanguages.General_NotEnoughPermission);
                    return true;
                }

                boolean result = command.execute(arg0, cmd, args);
                if (!result) {
                    ChatColor color = command.getCommandColor();
                    String desc = base.lang.parseFirstString(arg0, command.getDescription());
                    base.lang.addString(mainCommand);
                    base.lang.addString(color + command.toString());
                    base.lang.addString(desc);
                    base.sendMessage(arg0, DefaultLanguages.Command_Format_Description);

                    StringBuilder builder = new StringBuilder();
                    for (String alias : command.getAliases()) {
                        builder.append(" " + alias);
                    }
                    base.lang.addString(builder.toString());
                    base.sendMessage(arg0, DefaultLanguages.Command_Format_Aliases);

                    for (Language lang : command.getUsage()) {
                        String usage = base.lang.parseFirstString(arg0, lang);
                        base.lang.addString(usage);
                        base.sendMessage(arg0, DefaultLanguages.Command_Format_Usage);
                    }
                }
                return true;
            } else if (cmd.equals("")) {
                return dispatch(arg0, "help");
            } else {
                base.lang.addString(cmd);
                base.sendMessage(arg0, DefaultLanguages.General_NoSuchCommand);
                return true;
            }
        }

        public SubCommand getCommand(String arg0) {
            return commandList.get(arg0);
        }

        public boolean register(String arg0, SubCommand arg1) {
            String[] aliases = arg1.getAliases();
            if (aliases != null) {
                for (String alias : aliases) {
                    aliasMap.put(alias, arg1.getName());
                }
            }

            if (commandList.containsKey(arg1.getName())) {
                return false;
            }

            commandList.put(arg1.getName(), arg1);
            return true;
        }

        public boolean register(String arg0, String arg1, SubCommand arg2) {
            if (commandList.containsKey(arg0))
                return false;

            commandList.put(arg0, arg2);
            return true;
        }

        public void registerAll(String arg0, List<SubCommand> arg1) {
            for (SubCommand cmd : arg1) {
                register(null, cmd);
            }
        }

        public List<String> tabComplete(CommandSender arg0, String arg1) throws IllegalArgumentException {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
