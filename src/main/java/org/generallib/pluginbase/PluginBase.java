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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.generallib.main.FakePlugin;
import org.generallib.pluginbase.PluginLanguage.Language;
import org.generallib.pluginbase.language.DefaultLanguages;
import org.generallib.pluginbase.manager.*;

/**
 * Always register commands, managers, APIs, and languages.
 *
 * @author wysohn
 *
 */
public abstract class PluginBase extends JavaPlugin {
    final Map<Class<? extends PluginManager>, PluginManager> pluginManagers = new HashMap<Class<? extends PluginManager>, PluginManager>();

    private PluginConfig config;
    public PluginLanguage lang;
    // backward compatible
    public PluginCommandExecutor executor;
    public Map<String, PluginCommandExecutor> executors;
    public PluginAPISupport APISupport;

    private String[] mainCommand;
    private String adminPermission;

    public static void runAsynchronously(Runnable run) {
        Bukkit.getScheduler().runTaskAsynchronously(FakePlugin.instance, run);
    }

    /**
     * Do not call this contstructor.
     */
    protected PluginBase() {
        throw new RuntimeException(
                "Please override default constructor in order to establish PluginBase. This overriden constructor"
                        + " should also call super constructor(PluginConfig, String, String).");
    }

    public PluginBase(final PluginConfig config, String mainCommand, String adminPermission) {
        this(config, new String[] { mainCommand }, adminPermission);
    }

    public PluginBase(final PluginConfig config, String[] mainCommand, String adminPermission) {
        this.config = config;
        this.mainCommand = mainCommand;
        this.adminPermission = adminPermission;

        registerManager(PlayerLocationManager.getSharedInstance(this));
        registerManager(new AreaSelectionManager(this, PluginManager.NORM_PRIORITY));
        registerManager(new VolatileTaskManager(this, PluginManager.NORM_PRIORITY));
        registerManager(new TargetBlockManager(this, PluginManager.NORM_PRIORITY));
        registerManager(new PropertyEditManager(this, PluginManager.NORM_PRIORITY));
    }

    private void initiatePluginProcedures() {
        try {
            if (this.isEnabled())
                lang.onEnable(this);
        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().severe("While loading lang:");
            this.getLogger().severe(e.getClass().getSimpleName() + "@" + e.getMessage());
            this.setEnabled(false);
        }

        try {
            if (this.isEnabled()) {
                for (PluginCommandExecutor executor : executors.values())
                    executor.onEnable(this);
            }

        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().severe("While loading command executor:");
            this.getLogger().severe(e.getClass().getSimpleName() + "@" + e.getMessage());
            this.setEnabled(false);
        }

        try {
            if (this.isEnabled())
                APISupport.onEnable(this);
        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().severe("While loading APISupport:");
            this.getLogger().severe(e.getClass().getSimpleName() + "@" + e.getMessage());
            this.setEnabled(false);
        }

        Map<Integer, Set<PluginManager>> map = new TreeMap<Integer, Set<PluginManager>>();
        for (int i = PluginManager.FASTEST_PRIORITY; i <= PluginManager.SLOWEST_PRIORITY; i++) {
            map.put(i, new HashSet<PluginManager>());
        }

        for (Entry<Class<? extends PluginManager>, PluginManager> entry : pluginManagers.entrySet()) {
            Set<PluginManager> set = map.get(entry.getValue().getLoadPriority());
            set.add(entry.getValue());
        }

        for (Entry<Integer, Set<PluginManager>> entry : map.entrySet()) {
            Set<PluginManager> managers = entry.getValue();

            for (PluginManager manager : managers) {
                try {
                    manager.onEnable();
                } catch (Exception e) {
                    e.printStackTrace();
                    this.getLogger().severe("While Enabling [" + manager.getClass().getSimpleName() + "]:");
                    this.getLogger().severe(e.getClass().getSimpleName() + "@" + e.getMessage());
                    this.setEnabled(false);

                    this.getLogger().info(lang.parseFirstString(DefaultLanguages.Plugin_WillBeDisabled));
                    return;
                }

                if (manager instanceof Listener) {
                    getServer().getPluginManager().registerEvents((Listener) manager, this);
                }
            }
        }
    }

    private void finalizeDisableProcedures() {
        try {
            if (config != null)
                config.onDisable(this);
        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().severe("While disabling config:");
            this.getLogger().severe(e.getClass().getSimpleName() + "@" + e.getMessage());
        }

        try {
            if (lang != null)
                lang.onDisable(this);
        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().severe("While disabling lang:");
            this.getLogger().severe(e.getClass().getSimpleName() + "@" + e.getMessage());
        }

        try {
            for (PluginCommandExecutor executor : executors.values())
                executor.onDisable(this);
        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().severe("While disabling command executor:");
            this.getLogger().severe(e.getClass().getSimpleName() + "@" + e.getMessage());
        }

        try {
            if (APISupport != null)
                APISupport.onDisable(this);
        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().severe("While disabling APISupport:");
            this.getLogger().severe(e.getClass().getSimpleName() + "@" + e.getMessage());
        }

        Map<Integer, Set<PluginManager>> map = new TreeMap<Integer, Set<PluginManager>>();
        for (int i = PluginManager.FASTEST_PRIORITY; i <= PluginManager.SLOWEST_PRIORITY; i++) {
            map.put(i, new HashSet<PluginManager>());
        }

        for (Entry<Class<? extends PluginManager>, PluginManager> entry : pluginManagers.entrySet()) {
            Set<PluginManager> set = map.get(entry.getValue().getLoadPriority());
            set.add(entry.getValue());
        }

        for (Entry<Integer, Set<PluginManager>> entry : map.entrySet()) {
            Set<PluginManager> managers = entry.getValue();

            for (PluginManager manager : managers) {
                try {
                    manager.onDisable();
                } catch (Exception e) {
                    e.printStackTrace();
                    this.getLogger().severe("While Enabling [" + manager.getClass().getSimpleName() + "]:");
                    this.getLogger().severe(e.getClass().getSimpleName() + "@" + e.getMessage());
                    this.setEnabled(false);

                    this.getLogger().info(lang.parseFirstString(DefaultLanguages.Plugin_WillBeDisabled));
                    return;
                }
            }
        }
    }

    public void reloadPluginProcedures() {
        try {
            config.onReload(this);
        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().severe("While reloading config:");
            this.getLogger().severe(e.getClass().getSimpleName() + "@" + e.getMessage());
        }

        try {
            lang.onReload(this);
        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().severe("While reloading lang:");
            this.getLogger().severe(e.getClass().getSimpleName() + "@" + e.getMessage());
        }

        try {
            for (PluginCommandExecutor executor : executors.values())
                executor.onReload(this);
        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().severe("While reloading command executor:");
            this.getLogger().severe(e.getClass().getSimpleName() + "@" + e.getMessage());
        }

        try {
            APISupport.onReload(this);
        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().severe("While reloading APISupport:");
            this.getLogger().severe(e.getClass().getSimpleName() + "@" + e.getMessage());
        }

        Map<Integer, Set<PluginManager>> map = new TreeMap<Integer, Set<PluginManager>>();
        for (int i = PluginManager.FASTEST_PRIORITY; i <= PluginManager.SLOWEST_PRIORITY; i++) {
            map.put(i, new HashSet<PluginManager>());
        }

        for (Entry<Class<? extends PluginManager>, PluginManager> entry : pluginManagers.entrySet()) {
            Set<PluginManager> set = map.get(entry.getValue().getLoadPriority());
            set.add(entry.getValue());
        }

        for (Entry<Integer, Set<PluginManager>> entry : map.entrySet()) {
            Set<PluginManager> managers = entry.getValue();

            for (PluginManager manager : managers) {
                try {
                    if (this.isEnabled())
                        manager.onReload();
                } catch (Exception e) {
                    e.printStackTrace();
                    this.getLogger().severe("While Enabling [" + manager.getClass().getSimpleName() + "]:");
                    this.getLogger().severe(e.getClass().getSimpleName() + "@" + e.getMessage());
                    this.setEnabled(false);

                    this.getLogger().info(lang.parseFirstString(DefaultLanguages.Plugin_WillBeDisabled));
                    return;
                }
            }
        }
    }

    @Override
    public void onEnable() {
        try {
            config.onEnable(this);
        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().severe("While loading config:");
            this.getLogger().severe(e.getClass().getSimpleName() + "@" + e.getMessage());
            this.setEnabled(false);
        }

        String def = config.Plugin_Language_Default;
        Set<String> list = new HashSet<String>();
        list.addAll(config.Plugin_Language_List);

        this.lang = new PluginLanguage(list, def);
        this.executors = new HashMap<>();
        for (int i = 0; i < mainCommand.length; i++)
            this.executors.put(mainCommand[i], new PluginCommandExecutor(mainCommand[i], adminPermission));
        this.executor = this.executors.get(mainCommand[0]);
        this.APISupport = new PluginAPISupport();

        preEnable();

        initiatePluginProcedures();
    }

    /**
     * This is good place to initialize all commands, managers, etc.
     */
    protected abstract void preEnable();

    @Override
    public void onDisable() {
        finalizeDisableProcedures();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        PluginCommandExecutor executor = executors.get(command.getName());
        if (executor == null)
            return true;

        return executor.onCommand(sender, command, label, args);
    }

    public void sendMessage(CommandSender sender, Language language) {
        if (sender == null)
            return;

        if (sender instanceof Player) {
            sendMessage((Player) sender, language);
        } else {
            for (String msg : lang.parseStrings(language)) {
                sendMessage(sender, msg, 1000);
            }
        }
    }

    public void sendMessage(Player player, Language language) {
        if (player == null)
            return;

        String localeSimplified = "en";
        try {
            localeSimplified = FakePlugin.nmsEntityManager.getLocale(player);
        } catch (Exception e) {
            // silently fail
        } finally {
            for (String msg : lang.parseStrings(player, language, localeSimplified))
                sendMessage(player, lang.colorize(getPluginConfig().Plugin_Prefix) + " " + msg, 1000);
        }
    }

    private static void sendMessage(CommandSender sender, String msg, int limit) {
        if (msg.length() <= limit) {
            sender.sendMessage(msg);
        } else {
            int count = (int) Math.ceil((double) msg.length() / limit);
            for (int i = 0; i < count; i++) {
                int index = i * limit;
                if (i < count - 1) {
                    sender.sendMessage(msg.substring(index, index + limit));
                } else {
                    sender.sendMessage(msg.substring(index, index + msg.length() % limit));
                }
            }
        }
    }

    public void broadcast(Language language) {
        broadcast(language, null, null);
    }

    public void broadcast(Language language, PlayerFilter filter) {
        broadcast(language, filter, null);
    }

    public void broadcast(Language language, PreParseHandle parseHandle) {
        broadcast(language, null, parseHandle);
    }

    public void broadcast(Language language, PlayerFilter filter, PreParseHandle parseHandle) {
        for(Player player : Bukkit.getOnlinePlayers()){
            if(filter != null && !filter.accept(player))
                continue;

            if(parseHandle != null)
                parseHandle.onParse(lang);

            sendMessage(player, language);
        }
    }

    @FunctionalInterface
    public interface PlayerFilter{
        boolean accept(Player player);
    }

    @FunctionalInterface
    public interface PreParseHandle{
        void onParse(PluginLanguage lang);
    }

    @SuppressWarnings("unchecked")
    public <T extends PluginConfig> T getPluginConfig() {
        return (T) config;
    }

    public Map<Class<? extends PluginManager>, PluginManager> getPluginManagers() {
        return pluginManagers;
    }

    public void registerManager(PluginManager manager) {
        pluginManagers.put(manager.getClass(), manager);
    }

    public <T extends PluginManager> T getManager(Class<? extends PluginManager> clazz) {
        return (T) pluginManagers.get(clazz);
    }

    public static void main(String[] ar) {
        String msg = "123";
        int limit = 2;
        if (msg.length() <= limit) {
            System.out.println(msg);
        } else {
            int count = (int) Math.ceil((double) msg.length() / limit);
            for (int i = 0; i < count; i++) {
                int index = i * limit;
                if (i < count - 1) {
                    System.out.println(msg.substring(index, index + limit));
                } else {
                    System.out.println(msg.substring(index, index + msg.length() % limit));
                }
            }
        }
    }
}
