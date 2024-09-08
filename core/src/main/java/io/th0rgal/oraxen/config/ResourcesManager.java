package io.th0rgal.oraxen.config;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.utils.OraxenYaml;
import io.th0rgal.oraxen.utils.ReflectionUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ResourcesManager {

    final JavaPlugin plugin;

    public ResourcesManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private Entry<File, YamlConfiguration> settings;
    private Entry<File, YamlConfiguration> mechanics;

    public YamlConfiguration settings() {
        return settingsEntry().getValue();
    }

    public Entry<File, YamlConfiguration> settingsEntry() {
        return settings != null ? settings : (settings = entry("settings.yml"));
    }

    public YamlConfiguration mechanics() {
        return mechanicsEntry().getValue();
    }

    public Entry<File, YamlConfiguration> mechanicsEntry() {
        return mechanics != null ? mechanics : (mechanics = entry("mechanics.yml"));
    }

    public Entry<File, YamlConfiguration> entry(String fileName) {
        File file = extractConfiguration(fileName);
        return new AbstractMap.SimpleEntry<>(file, OraxenYaml.loadConfiguration(file));
    }

    public File extractConfiguration(String fileName) {
        File file = new File(this.plugin.getDataFolder(), fileName);
        if (!file.exists())
            this.plugin.saveResource(fileName, false);
        return file;
    }

    public void extractConfigsInFolder(String folder, String fileExtension) {
        ZipInputStream zip = browse();
        try {
            extractConfigsInFolder(zip, folder, fileExtension);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void extractConfigsInFolder(ZipInputStream zip, String folder, String fileExtension) throws IOException {
        ZipEntry entry = zip.getNextEntry();
        while (entry != null) {
            extractFileAccordingToExtension(entry, folder, fileExtension);
            entry = zip.getNextEntry();
        }
        zip.closeEntry();
        zip.close();
    }

    public void extractFileIfTrue(ZipEntry entry, boolean isSuitable) {
        if (entry.isDirectory() || !isSuitable) return;
        if (entry.getName().startsWith("pack/textures/models/armor/")) {
            if (OraxenPlugin.get().getDataFolder().toPath().resolve(entry.getName()).toFile().exists()) return;
            plugin.saveResource(entry.getName(), false);
        }
        else plugin.saveResource(entry.getName(), true);
    }

    private void extractFileAccordingToExtension(ZipEntry entry, String folder, String fileExtension) {
        boolean isSuitable = entry.getName().startsWith(folder + "/") && entry.getName().endsWith("." + fileExtension);
        extractFileIfTrue(entry, isSuitable);
    }

    public static ZipInputStream browse() {
        return ReflectionUtils.getJarStream(OraxenPlugin.class).orElseThrow(() -> {
            Message.ZIP_BROWSE_ERROR.log();
            return new RuntimeException("OraxenResources not found!");
        });
    }

}
