package de.itsjxsper.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class Configuration {
    private final YamlConfiguration yamlConfiguration;
    private final File file;

    /**
     * Create a new ConfigurationFile
     * @param plugin The plugin which should own this file.
     * @param name The name (without extension) of the file
     */

    public Configuration(Plugin plugin, String name) throws IOException, InvalidConfigurationException {
        this.file = new File(plugin.getDataFolder(), name + ".yml");
        this.yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        if (!this.file.exists()) {
            this.file.createNewFile();
        }
        this.yamlConfiguration.load(this.file);
    }

    public YamlConfiguration getYamlConfiguration() {
        return this.yamlConfiguration;
    }

    public void save() throws IOException {
        yamlConfiguration.save(file);
    }
}
