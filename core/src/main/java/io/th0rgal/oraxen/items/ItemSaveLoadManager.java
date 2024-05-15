package io.th0rgal.oraxen.items;

import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.config.Settings;
import io.th0rgal.oraxen.utils.OraxenYaml;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;

import java.io.File;
import java.io.IOException;

public class ItemSaveLoadManager {

    /**
     * Saves item configuration to the specified file.
     * @param itemBuilder The itemBuilder containing the item data.
     * @param file The configuration file to save to.
     */
    public void saveItemConfiguration(ItemBuilder itemBuilder, File file) {
        YamlConfiguration yamlConfiguration = OraxenYaml.loadConfiguration(file);
        // Assuming the itemBuilder has methods to retrieve the necessary data:
        if (itemBuilder.hasColor()) {
            String color = itemBuilder.getColor().getRed() + "," + itemBuilder.getColor().getGreen() + "," + itemBuilder.getColor().getBlue();
            yamlConfiguration.set(OraxenItems.getIdByItem(itemBuilder.build()) + ".color", color);
        }
        if (itemBuilder.hasTrimPattern()) {
            String trimPattern = itemBuilder.getTrimPatternKey().asString();
            yamlConfiguration.set(OraxenItems.getIdByItem(itemBuilder.build()) + ".trim_pattern", trimPattern);
        }
        if (!itemBuilder.getItemFlags().isEmpty()) {
            yamlConfiguration.set(OraxenItems.getIdByItem(itemBuilder.build()) + ".ItemFlags", itemBuilder.itemFlags.stream().map(ItemFlag::name).toList());
        }
        try {
            yamlConfiguration.save(file);
        } catch (IOException e) {
            if (Settings.DEBUG.toBool()) e.printStackTrace();
        }
    }
}