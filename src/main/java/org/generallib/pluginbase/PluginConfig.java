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

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.generallib.serializetools.Utf8YamlConfiguration;

/**
 * <p>
 * Let automatically make public fields to be saved and loaded.
 * </p>
 *
 * Child class only need to declare <b>public field with '_'</b> as _ will be
 * used to indicate the path. Fields with other than public modifier will be
 * ignored. Use {@link Comment} annotation to add comments. Comment annotation
 * has member <b>String[] comment</b>. ex) @Comment(comment = {"This","is","comment"})
 *
 * <p>
 * For example) test_test field is equivalent to test.test in config
 * </p>
 *
 * @author wysohn
 *
 */
public abstract class PluginConfig implements PluginProcedure{
/*	public static void main(String[] ar){
		System.out.println(convertToConfigName("test_test_test"));
		System.out.println(converToFieldName("test.test.test"));
	}*/

	protected PluginBase base;

	private FileConfiguration config;
	private File file;

	public int Command_Help_SentencePerPage = 6;

	public int Languages_Double_DecimalPoints = 4;

	public boolean Plugin_Enabled = true;
	public boolean Plugin_Debugging = false;
	public String Plugin_Language_Default = "en";
	public List<String> Plugin_Language_List = new ArrayList<String>(){{
		add("en");
		add("ko");
	}};
	public String Plugin_Prefix = "&6[&5?&6]";

	public String Plugin_ServerName = "yourServer";

	public boolean MySql_Enabled = false;
	public String MySql_DBAddress = "localhost:3306";
	public String MySql_DBName = "somedb";
	public String MySql_DBUser = "root";
	public String MySql_DBPassword = "1234";

	@Override
    public void onEnable(final PluginBase base) throws Exception {
		this.base = base;
		config = new Utf8YamlConfiguration();

        if(!base.getDataFolder().exists())
            base.getDataFolder().mkdirs();

        file = new File(base.getDataFolder(), "config.yml");
        if(!file.exists())
            file.createNewFile();

        config.load(file);

        validateAndLoad();
        save();
	}

	@Override
    public void onDisable(PluginBase base) throws Exception{
		save();
	}

	@Override
    public void onReload(PluginBase base) throws Exception {
		reload();
	}

	private static String convertToConfigName(String fieldName){
		return fieldName.replaceAll("_", ".");
	}

	private static String converToFieldName(String configKey){
		return configKey.replaceAll("\\.", "_");
	}

	/**
	 * check all the config and add necessary/remove unnecessary configs.
	 */
	protected void validateAndLoad(){
		base.getLogger().info("Validating config...");

		Field[] fields = this.getClass().getFields();

		int addedNew = 0;
		//fill empty config
		for(Field field : fields){
			try {
				String configName = convertToConfigName(field.getName());
				Object obj = field.get(this);

				if(!config.contains(configName)){
					config.set(configName, obj);
					addedNew++;
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				base.getLogger().severe(e.getMessage());
			}
		}

		base.getLogger().info("Added ["+addedNew+"] new configs with default value.");

		Set<String> fieldNames = new HashSet<String>();
		for(Field field : fields)
			fieldNames.add(field.getName());

		int deletedOld = 0;
		int loaded = 0;
		//delete non existing config or set value with existing config
		Configuration root = config.getRoot();
		Set<String> keys = root.getKeys(true);
		for(String key : keys){
			try {
				if(config.isConfigurationSection(key))
					continue;

				if(key.contains("_COMMENT_"))
					continue;

				String fieldName = converToFieldName(key);

				if(!fieldNames.contains(fieldName)){
					config.set(key, null);
					deletedOld++;
				}else{
					Field field = this.getClass().getField(fieldName);

					field.set(this, config.get(key));
					loaded++;
				}
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				base.getLogger().severe(e.getMessage());
			}
		}

		try {
			save();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		base.getLogger().info("Deleted ["+deletedOld+"] old configs and loaded ["+loaded+"] configs.");

		base.getLogger().info("Validation and Loading complete!");
	}

	/**
	 * save current values into the config file
	 * @throws IOException
	 */
	public void save() throws IOException{
		base.getLogger().info("Saving to ["+file.getName()+"]...");

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
		for(String s : split){
			if(s.contains("_COMMENT_")){
				writer.write("#"+s.replaceAll("'", "").substring(s.indexOf(':') + 1) + "\n");
			}else{
				writer.write(s + "\n");
			}
		}

		writer.close();
		stream.close();

		base.getLogger().info("Complete!");
	}

	/**
	 * Override all current values using values in config file
	 * @throws IOException
	 * @throws InvalidConfigurationException
	 */
	public void reload() throws IOException, InvalidConfigurationException{
		base.getLogger().info("Loading ["+file.getName()+"]...");
		config.load(file);
		base.getLogger().info("Complete!");

		validateAndLoad();
	}
}