package io.th0rgal.oraxen.config;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.font.FontEvents;
import io.th0rgal.oraxen.nms.GlyphHandlers;
import io.th0rgal.oraxen.utils.AdventureUtils;
import io.th0rgal.oraxen.utils.BlockHelpers;
import io.th0rgal.oraxen.utils.OraxenYaml;
import io.th0rgal.oraxen.utils.VersionUtil;
import io.th0rgal.oraxen.utils.customarmor.CustomArmorMaterial;
import io.th0rgal.oraxen.utils.customarmor.CustomArmorType;
import io.th0rgal.oraxen.utils.customarmor.ShaderArmorTextures;
import io.th0rgal.oraxen.utils.logs.Logs;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public enum Settings {
    // Generic Plugin stuff
    DEBUG("debug", false),
    PLUGIN_LANGUAGE("Plugin.language", "english"),
    KEEP_UP_TO_DATE("Plugin.keep_this_up_to_date", true),
    REPAIR_COMMAND_ORAXEN_DURABILITY("Plugin.commands.repair.oraxen_durability_only", false),
    GENERATE_DEFAULT_ASSETS("Plugin.generation.default_assets", true),
    GENERATE_DEFAULT_CONFIGS("Plugin.generation.default_configs", true),
    FORMAT_INVENTORY_TITLES("Plugin.formatting.inventory_titles", true),
    FORMAT_TITLES("Plugin.formatting.titles", true),
    FORMAT_SUBTITLES("Plugin.formatting.subtitles", true),
    FORMAT_ACTION_BAR("Plugin.formatting.action_bar", true),
    FORMAT_ANVIL("Plugin.formatting.anvil", true),
    FORMAT_SIGNS("Plugin.formatting.signs", true),
    FORMAT_CHAT("Plugin.formatting.chat", true),
    FORMAT_BOOKS("Plugin.formatting.books", true),

    // WorldEdit
    WORLDEDIT_NOTEBLOCKS("WorldEdit.noteblock_mechanic", false),
    WORLDEDIT_STRINGBLOCKS("WorldEdit.stringblock_mechanic", false),
    WORLDEDIT_FURNITURE("WorldEdit.furniture_mechanic", false),

    // Glyphs
    GLYPH_HANDLER("Glyphs.glyph_handler", GlyphHandlers.GlyphHandler.VANILLA.name()),
    SHOW_PERMISSION_EMOJIS("Glyphs.emoji_list_permission_only", true),
    UNICODE_COMPLETIONS("Glyphs.unicode_completions", true),
    GLYPH_HOVER_TEXT("Glyphs.chat_hover_text", "<glyph_placeholder>"),


    // Chat
    CHAT_HANDLER("Chat.chat_handler", VersionUtil.isPaperServer() ? FontEvents.ChatHandler.MODERN.name() : FontEvents.ChatHandler.LEGACY.name()),

    // Config Tools
    CONFIGS_VERSION("configs_version"),
    UPDATE_CONFIGS("ConfigsTools.enable_configs_updater", true),
    DISABLE_AUTOMATIC_MODEL_DATA("ConfigsTools.disable_automatic_model_data", false),
    DISABLE_AUTOMATIC_GLYPH_CODE("ConfigsTools.disable_automatic_glyph_code", false),
    SKIPPED_MODEL_DATA_NUMBERS("ConfigsTools.skipped_model_data_numbers", List.of()),
    ERROR_ITEM("ConfigsTools.error_item", Map.of("material", Material.PODZOL.name(), "excludeFromInventory", false, "injectID", false)),

    // Custom Armor
    CUSTOM_ARMOR_DEFAULT_TYPE("CustomArmor.default_type", CustomArmorType.TRIMS.name()),
    DISABLE_LEATHER_REPAIR_CUSTOM("CustomArmor.disable_leather_repair", true),
    CUSTOM_ARMOR_TRIMS_DEFAULT_MATERIAL("CustomArmor.trims_settings.default_material", CustomArmorMaterial.CHAINMAIL.name()),
    CUSTOM_ARMOR_TRIMS_ASSIGN("CustomArmor.trims_settings.auto_assign_settings", true),
    CUSTOM_ARMOR_SHADER_TYPE("CustomArmor.shader_settings.type", ShaderArmorTextures.ShaderType.FANCY.name()),
    CUSTOM_ARMOR_SHADER_RESOLUTION("CustomArmor.shader_settings.armor_resolution", 16),
    CUSTOM_ARMOR_SHADER_ANIMATED_FRAMERATE("CustomArmor.shader_settings.animated_armor_framerate", 24),
    CUSTOM_ARMOR_SHADER_GENERATE_FILES("CustomArmor.shader_settings.generate_armor_shader_files", true),
    CUSTOM_ARMOR_SHADER_GENERATE_CUSTOM_TEXTURES("CustomArmor.shader_settings.generate_custom_armor_textures", true),
    CUSTOM_ARMOR_SHADER_GENERATE_SHADER_COMPATIBLE_ARMOR("CustomArmor.shader_settings.generate_shader_compatible_armor", true),

    // Custom Blocks
    BLOCK_CORRECTION("CustomBlocks.block_correction", BlockHelpers.BlockCorrection.NMS.name()),
    LEGACY_NOTEBLOCKS("CustomBlocks.use_legacy_noteblocks", VersionUtil.atOrAbove("1.20")),
    LEGACY_STRINGBLOCKS("CustomBlocks.use_legacy_stringblocks"),

    // ItemUpdater
    UPDATE_ITEMS("ItemUpdater.update_items", true),
    UPDATE_ITEMS_ON_RELOAD("ItemUpdater.update_items_on_reload", true),
    OVERRIDE_RENAMED_ITEMS("ItemUpdater.override_renamed_items", false),
    OVERRIDE_ITEM_LORE("ItemUpdater.override_item_lore", false),

    // FurnitureUpdater
    UPDATE_FURNITURE("FurnitureUpdater.update_furniture", true),
    UPDATE_FURNITURE_ON_RELOAD("FurnitureUpdater.update_on_reload", false),
    UPDATE_FURNITURE_ON_LOAD("FurnitureUpdater.update_on_load", false),
    EXPERIMENTAL_FURNITURE_TYPE_UPDATE("FurnitureUpdater.experimental_furniture_type_update", false),
    EXPERIMENTAL_FIX_BROKEN_FURNITURE("FurnitureUpdater.experimental_fix_broken_furniture", false),

    //Misc
    RESET_RECIPES("Misc.reset_recipes", true),
    ADD_RECIPES_TO_BOOK("Misc.add_recipes_to_book", true),
    ARMOR_EQUIP_EVENT_BYPASS("Misc.armor_equip_event_bypass"),
    SHIELD_DISPLAY("Misc.shield_display"),
    BOW_DISPLAY("Misc.bow_display"),
    CROSSBOW_DISPLAY("Misc.crossbow_display"),
    HIDE_SCOREBOARD_NUMBERS("Misc.hide_scoreboard_numbers", false),
    HIDE_SCOREBOARD_BACKGROUND("Misc.hide_scoreboard_background", false),

    //Pack
    GENERATE("Pack.generation.generate", true),
    EXCLUDED_FILE_EXTENSIONS("Pack.generation.excluded_file_extensions", List.of(".zip")),
    FIX_FORCE_UNICODE_GLYPHS("Pack.generation.fix_force_unicode_glyphs", true),
    VERIFY_PACK_FILES("Pack.generation.verify_pack_files", true),
    GENERATE_ATLAS_FILE("Pack.generation.atlas.generate"),
    TEXTURE_SLICER("Pack.generation.texture_slicer"),
    EXCLUDE_MALFORMED_ATLAS("Pack.generation.atlas.exclude_malformed_from_atlas"),
    ATLAS_GENERATION_TYPE("Pack.generation.atlas.type"),
    GENERATE_MODEL_BASED_ON_TEXTURE_PATH("Pack.generation.auto_generated_models_follow_texture_path", false),
    COMPRESSION("Pack.generation.compression", "BEST_COMPRESSION"),
    PROTECTION("Pack.generation.protection", true),
    COMMENT("Pack.generation.comment", """
            The content of this resourcepack
            belongs to the owner of the Oraxen
            plugin and any complete or partial
            use must comply with the terms and
            conditions of Oraxen
            """.trim()
    ),
    MERGE_DUPLICATE_FONTS("Pack.import.merge_duplicate_fonts"),
    MERGE_DUPLICATES("Pack.import.merge_duplicates"),
    RETAIN_CUSTOM_MODEL_DATA("Pack.import.retain_custom_model_data"),
    MERGE_ITEM_MODELS("Pack.import.merge_item_base_models"),

    UPLOAD_TYPE("Pack.upload.type"),
    UPLOAD("Pack.upload.enabled"),
    UPLOAD_OPTIONS("Pack.upload.options"),

    POLYMATH_SERVER("Pack.upload.polymath.server"),
    POLYMATH_SECRET("Pack.upload.polymath.secret"),

    SEND_PACK("Pack.dispatch.send_pack", true),
    SEND_ON_RELOAD("Pack.dispatch.send_on_reload", true),
    SEND_PACK_DELAY("Pack.dispatch.delay", -1),
    SEND_PACK_MANDATORY("Pack.dispatch.mandatory", true),
    SEND_PACK_PROMPT("Pack.dispatch.prompt", "<#fa4943>Accept the pack to enjoy a full <b><gradient:#9055FF:#13E2DA>Oraxen</b><#fa4943> experience"),
    SEND_JOIN_MESSAGE("Pack.dispatch.join_message.enabled", false),
    JOIN_MESSAGE_DELAY("Pack.dispatch.join_message.delay", -1),

    RECEIVE_ENABLED("Pack.receive.enabled"),
    RECEIVE_ALLOWED_ACTIONS("Pack.receive.accepted.actions"),
    RECEIVE_LOADED_ACTIONS("Pack.receive.loaded.actions"),
    RECEIVE_FAILED_ACTIONS("Pack.receive.failed_download.actions"),
    RECEIVE_DENIED_ACTIONS("Pack.receive.denied.actions"),
    RECEIVE_FAILED_RELOAD_ACTIONS("Pack.receive.failed_reload.actions"),
    RECEIVE_DOWNLOADED_ACTIONS("Pack.receive.downloaded.actions"),
    RECEIVE_INVALID_URL_ACTIONS("Pack.receive.invalid_url.actions"),
    RECEIVE_DISCARDED_ACTIONS("Pack.receive.discarded.actions"),

    // Inventory
    ORAXEN_INV_LAYOUT("oraxen_inventory.menu_layout", Map.of(
            "armors", Map.of("icon", "emerald_chestplate", "displayname", "<green>Armors"),
            "blocks", Map.of("icon", "orax_ore", "displayname", "<green>Blocks"),
            "furniture", Map.of("icon", "chair", "displayname", "<green>Furniture"),
            "flowers", Map.of("icon", "dailily", "displayname", "<green>Flowers"),
            "hats", Map.of("icon", "crown", "displayname", "<green>Hats"),
            "items", Map.of("icon", "ruby", "displayname", "<green>Items"),
            "mystical", Map.of("icon", "legendary_hammer", "displayname", "<green>Mystical"),
            "plants", Map.of("icon", "weed_leaf", "displayname", "<green>Plants"),
            "tools", Map.of("icon", "iron_serpe", "displayname", "<green>Tools"),
            "weapons", Map.of("icon", "energy_crystal_sword", "displayname", "<green>Weapons")
    )),
    ORAXEN_INV_TITLE("oraxen_inventory.menu_info.title", "<shift:-18><glyph:menu_items><shift:-193>"),
    ORAXEN_INV_ROWS("oraxen_inventory.menu_info.rows", 6),
    ORAXEN_INV_SIZE("oraxen_inventory.menu_info.size", 45),

    ORAXEN_INV_PREVIOUS_PAGE_ICON("oraxen_inventory.menu_info.previous_page_button"),
    ORAXEN_INV_NEXT_PAGE_ICON("oraxen_inventory.menu_info.next_page_button"),
    ORAXEN_INV_EXIT_ICON("oraxen_inventory.menu_info.exit_button");

    private final String path;
    private final Object defaultValue;
    private List<String> comments = Collections.emptyList();
    private List<String> inlineComments = Collections.emptyList();
    private Component richComment = Component.empty();

    Settings(String path) {
        this.path = path;
        this.defaultValue = null;
    }

    Settings(String path, Object defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }

    Settings(String path, Object defaultValue, String... comments) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.comments = List.of(comments);
    }

    Settings(String path, Object defaultValue, List<String> comments, String... inlineComments) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.comments = comments;
        this.inlineComments = List.of(inlineComments);
    }

    Settings(String path, Object defaultValue, Component richComment) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.richComment = richComment;
    }

    public String getPath() {
        return path;
    }

    public Object defaultValue() {
        return defaultValue;
    }

    public List<String> comments() {
        return comments;
    }

    public List<String> inlineComments() {
        return inlineComments;
    }

    public Component richComment() {
        return richComment;
    }

    public Object getValue() {
        return OraxenPlugin.get().configsManager().getSettings().get(path);
    }

    public void setValue(Object value) {
        setValue(value, true);
    }

    public void setValue(Object value, boolean save) {
        YamlConfiguration settingFile = OraxenPlugin.get().configsManager().getSettings();
        settingFile.set(path, value);
        try {
            if (save) settingFile.save(OraxenPlugin.get().getDataFolder().toPath().resolve("settings.yml").toFile());
        } catch (Exception e) {
            Logs.logError("Failed to apply changes to settings.yml");
        }
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }

    public Component toComponent() {
        return AdventureUtils.MINI_MESSAGE.deserialize(getValue().toString());
    }

    public Boolean toBool() {
        return (boolean) getValue();
    }

    public int toInt() {
        return (int) getValue();
    }

    public List<String> toStringList() {
        return OraxenPlugin.get().configsManager().getSettings().getStringList(path);
    }

    public ConfigurationSection toConfigSection() {
        return OraxenPlugin.get().configsManager().getSettings().getConfigurationSection(path);
    }

    public static YamlConfiguration validateSettings() {
        File settingsFile = OraxenPlugin.get().getDataFolder().toPath().resolve("settings.yml").toFile();
        YamlConfiguration settings = settingsFile.exists() ? OraxenYaml.loadConfiguration(settingsFile) : new YamlConfiguration();
        settings.options().copyDefaults(true).indent(4).parseComments(true);
        YamlConfiguration defaults = defaultSettings();

        settings.addDefaults(defaults);

        try {
            settingsFile.createNewFile();
            settings.save(settingsFile);
        } catch (IOException e) {
            if (DEBUG.toBool()) e.printStackTrace();
        }

        return settings;
    }

    private static YamlConfiguration defaultSettings() {
        YamlConfiguration defaultSettings = new YamlConfiguration();
        defaultSettings.options().copyDefaults(true).indent(4).parseComments(true);

        for (Settings setting : Settings.values()) {
            defaultSettings.set(setting.getPath(), setting.defaultValue());
            defaultSettings.setComments(setting.getPath(), setting.comments());
            defaultSettings.setInlineComments(setting.getPath(), setting.inlineComments());
//            defaultSettings.setRichMessage(setting.getPath(), setting.richComment());
        }

        return defaultSettings;
    }
}
