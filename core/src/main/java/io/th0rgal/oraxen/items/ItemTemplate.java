package io.th0rgal.oraxen.items;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ItemTemplate {

    private static final Map<String, ItemParser> itemTemplates = new HashMap<>();
    private final ItemParser templateParser;

    public ItemTemplate(ConfigurationSection section) {
        section.set("injectID", false);
        templateParser = new ItemParser(section);
        itemTemplates.put(section.getName(), templateParser);
    }

    public static Map<String, ItemParser> getItemTemplates() {
        return itemTemplates;
    }

    @Nullable
    public static ItemParser getParserTemplate(String id) {
        return itemTemplates.get(id);
    }

    public static boolean isTemplate(String id) {
        return itemTemplates.containsKey(id);
    }
}