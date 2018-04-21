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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.generallib.pluginbase.constants.ArenaState;
import org.generallib.serializetools.Utf8YamlConfiguration;

/**
 * <p>
 * Let automatically make public fields to be saved and loaded.
 * </p>
 *
 * Child class only need to declare <b>public field with '_'</b> as _ will be
 * used to indicate the path. Fields with other than public modifier will be
 * ignored.
 *
 * <p>
 * For example) test_test field is equivalent to test.test in config
 * </p>
 *
 * @author wysohn
 *
 */
public abstract class PluginConfig implements PluginProcedure {
    /*
     * public static void main(String[] ar){
     * System.out.println(convertToConfigName("test_test_test"));
     * System.out.println(converToFieldName("test.test.test")); }
     */

    protected PluginBase base;

    private FileConfiguration config;
    private File file;

    public int Command_Help_SentencePerPage = 6;

    public int Languages_Double_DecimalPoints = 4;

    public boolean Plugin_Enabled = true;
    public boolean Plugin_Debugging = false;
    public String Plugin_Language_Default = "en";
    public List<String> Plugin_Language_List = new ArrayList<String>() {
        {
            add("en");
            add("ko");
        }
    };
    public String Plugin_Prefix = "&6[&5?&6]";

    public String Plugin_ServerName = "yourServer";

    public boolean MySql_Enabled = false;
    public String MySql_DBAddress = "localhost:3306";
    public String MySql_DBName = "somedb";
    public String MySql_DBUser = "root";
    public String MySql_DBPassword = "1234";

    public List<String> ArenaManager_Sign_Format = new ArrayList<String>(){{
        add("&e&l●&7&l <plugin> &e&l●");
        add("&0<name>");
        add("&a<state>");
        add("&0<users>/<total>");
    }};

    public Location MainLobbyLocation = null;

    public String ArenaManager_Sign_StateTranslate_WAITING = ArenaState.WAITING.name();
    public String ArenaManager_Sign_StateTranslate_STARTING = ArenaState.STARTING.name();
    public String ArenaManager_Sign_StateTranslate_PLAYING = ArenaState.PLAYING.name();
    public String ArenaManager_Sign_StateTranslate_ENDING = ArenaState.ENDING.name();
    public String ArenaManager_Sign_StateTranslate_CLEANING = ArenaState.CLEANING.name();

    public String ArenaManager_Sign_StateBlock_WAITING = "${Material.HARD_CLAY}:5";
    public String ArenaManager_Sign_StateBlock_STARTING = "${Material.HARD_CLAY}:4";
    public String ArenaManager_Sign_StateBlock_PLAYING = "${Material.HARD_CLAY}:14";
    public String ArenaManager_Sign_StateBlock_ENDING = "${Material.HARD_CLAY}:11";
    public String ArenaManager_Sign_StateBlock_CLEANING = "${Material.HARD_CLAY}:15";

    public String ArenaManager_Waiting_GameMode = GameMode.ADVENTURE.name();

    public int ArenaManager_Starting_Delay_Seconds = 30;
    public String ArenaManager_Starting_Delay_Messages_30 = "&a&l<count>;;10;10;10";
    public String ArenaManager_Starting_Delay_Messages_15 = "&a&l<count>;;10;10;10";
    public String ArenaManager_Starting_Delay_Messages_10 = "&a&l<count>;;10;10;10";
    public String ArenaManager_Starting_Delay_Messages_5 = "&a&l<count>;;10;10;10";
    public String ArenaManager_Starting_Delay_Messages_4 = "&a&l<count>;;10;10;10";
    public String ArenaManager_Starting_Delay_Messages_3 = "&a&l<count>;;10;10;10";
    public String ArenaManager_Starting_Delay_Messages_2 = "&a&l<count>;;10;10;10";
    public String ArenaManager_Starting_Delay_Messages_1 = "&a&l<count>;;10;10;10";

    public int ArenaManager_Ending_Delay_Seconds = 9;
    public long ArenaManager_Ending_Firework_PerTick = 30L;
    public boolean ArenaManager_Ending_Firework_Flicker = true;
    public boolean ArenaManager_Ending_Firework_Trail = true;
    public String ArenaManager_Ending_Firework_Type = FireworkEffect.Type.BALL_LARGE.name();
    public List<Color> ArenaManager_Ending_Firework_Color = new ArrayList<Color>()
    {
        {
            add(Color.RED);
            add(Color.GREEN);
            add(Color.BLUE);
        }
    };
    public List<Color> ArenaManager_Ending_Firework_Fade = new ArrayList<Color>() {
        {
            add(Color.RED);
            add(Color.GREEN);
            add(Color.BLUE);
        }
    };

    public String ArenaManager_onUserLeaveArena_GameMode = GameMode.SURVIVAL.name();

    @Override
    public void onEnable(final PluginBase base) throws Exception {
        this.base = base;
        config = new Utf8YamlConfiguration();

        if (!base.getDataFolder().exists())
            base.getDataFolder().mkdirs();

        file = new File(base.getDataFolder(), "config.yml");
        if (!file.exists())
            file.createNewFile();

        config.load(file);

        validateAndLoad();
    }

    @Override
    public void onDisable(PluginBase base) throws Exception {

    }

    @Override
    public void onReload(PluginBase base) throws Exception {
        reload();
    }

    private static String convertToConfigName(String fieldName) {
        return fieldName.replaceAll("_", ".");
    }

    private static String converToFieldName(String configKey) {
        return configKey.replaceAll("\\.", "_");
    }

    /**
     * check all the config and add necessary/remove unnecessary configs.
     */
    protected void validateAndLoad() {
        base.getLogger().info("Validating config...");

        Field[] fields = this.getClass().getFields();

        int addedNew = 0;
        // fill empty config
        for (Field field : fields) {
            try {
                String configName = convertToConfigName(field.getName());
                Object obj = field.get(this);

                if (!config.contains(configName)) {
                    config.set(configName, obj);
                    addedNew++;
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                base.getLogger().severe(e.getMessage());
            }
        }

        base.getLogger().info("Added [" + addedNew + "] new configs with default value.");

        int loaded = 0;
        // delete non existing config or set value with existing config
        Configuration root = config.getRoot();
        Set<String> keys = root.getKeys(true);
        for (String key : keys) {
            try {
                if (config.isConfigurationSection(key))
                    continue;

                if (key.contains("_COMMENT_"))
                    continue;

                String fieldName = converToFieldName(key);

                Field field = this.getClass().getField(fieldName);

                field.set(this, config.get(key));
                loaded++;
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                base.getLogger().severe(e.getMessage());
            }
        }

        try {
            if(addedNew != 0)
                save();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        base.getLogger().info("Loaded [" + loaded + "] configs.");

        base.getLogger().info("Validation and Loading complete!");
    }

    /**
     * save current values into the config file
     * 
     * @throws IOException
     */
    public void save() throws IOException {
        base.getLogger().info("Saving to [" + file.getName() + "]...");

        Field[] fields = this.getClass().getFields();
        for (Field field : fields) {
            try {
                Object obj = field.get(this);
                if (obj != null) {
                    config.set(convertToConfigName(field.getName()), obj);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        FileOutputStream stream = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(stream, Charset.forName("UTF-8"));

        String output = config.saveToString();
        String[] split = output.split("\n");
        for (String s : split) {
            if (s.contains("_COMMENT_")) {
                writer.write("#" + s.replaceAll("'", "").substring(s.indexOf(':') + 1) + "\n");
            } else {
                writer.write(s + "\n");
            }
        }

        writer.close();
        stream.close();

        base.getLogger().info("Complete!");
    }

    /**
     * Override all current values using values in config file
     * 
     * @throws IOException
     * @throws InvalidConfigurationException
     */
    public void reload() throws IOException, InvalidConfigurationException {
        base.getLogger().info("Loading [" + file.getName() + "]...");
        config.load(file);
        base.getLogger().info("Complete!");

        validateAndLoad();
    }

    public ConfigurationSection getSection(String key){
        return config.getConfigurationSection(convertToConfigName(key));
    }
}
