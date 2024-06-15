package io.th0rgal.oraxen;

import com.comphenix.protocol.ProtocolLibrary;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.api.events.OraxenItemsLoadedEvent;
import io.th0rgal.oraxen.commands.CommandsManager;
import io.th0rgal.oraxen.compatibilities.CompatibilitiesManager;
import io.th0rgal.oraxen.config.*;
import io.th0rgal.oraxen.font.FontManager;
import io.th0rgal.oraxen.font.packets.InventoryPacketListener;
import io.th0rgal.oraxen.font.packets.TitlePacketListener;
import io.th0rgal.oraxen.hud.HudManager;
import io.th0rgal.oraxen.items.ItemUpdater;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureFactory;
import io.th0rgal.oraxen.nms.GlyphHandlers;
import io.th0rgal.oraxen.nms.NMSHandlers;
import io.th0rgal.oraxen.pack.generation.ResourcePack;
import io.th0rgal.oraxen.pack.upload.UploadManager;
import io.th0rgal.oraxen.recipes.RecipesManager;
import io.th0rgal.oraxen.sound.SoundManager;
import io.th0rgal.oraxen.utils.*;
import io.th0rgal.oraxen.utils.actions.ClickActionManager;
import io.th0rgal.oraxen.utils.armorequipevent.ArmorEquipEvent;
import io.th0rgal.oraxen.utils.breaker.BreakerSystem;
import io.th0rgal.oraxen.utils.customarmor.CustomArmorListener;
import io.th0rgal.oraxen.utils.inventories.InvManager;
import io.th0rgal.oraxen.utils.logs.Logs;
import io.th0rgal.protectionlib.ProtectionLib;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.jar.JarFile;

public class OraxenPlugin extends JavaPlugin {

    private static OraxenPlugin oraxen;
    private ConfigsManager configsManager;
    private ResourcesManager resourceManager;
    private BukkitAudiences audience;
    private UploadManager uploadManager;
    private FontManager fontManager;
    private HudManager hudManager;
    private SoundManager soundManager;
    private InvManager invManager;
    private ResourcePack resourcePack;
    private ClickActionManager clickActionManager;
    public static boolean supportsDisplayEntities;

    public OraxenPlugin() {
        oraxen = this;
    }

    public static OraxenPlugin get() {
        return oraxen;
    }

    @Nullable
    public static JarFile getJarFile() {
        try {
            return new JarFile(oraxen.getFile());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(true));
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();
        ProtectionLib.init(this);
        audience = BukkitAudiences.create(this);
        clickActionManager = new ClickActionManager(this);
        supportsDisplayEntities = VersionUtil.atOrAbove("1.19.4");
        reloadConfigs();

        if (Settings.KEEP_UP_TO_DATE.toBool())
            new SettingsUpdater().handleSettingsUpdate();
        final PluginManager pluginManager = Bukkit.getPluginManager();
        if (PluginUtils.isEnabled("ProtocolLib")) {
            new BreakerSystem().registerListener();
            if (Settings.FORMAT_INVENTORY_TITLES.toBool())
                ProtocolLibrary.getProtocolManager().addPacketListener(new InventoryPacketListener());
            ProtocolLibrary.getProtocolManager().addPacketListener(new TitlePacketListener());
        } else Logs.logWarning("ProtocolLib is not on your server, some features will not work");
        Bukkit.getPluginManager().registerEvents(new CustomArmorListener(), this);
        NMSHandlers.setup();

        resourcePack = new ResourcePack();
        MechanicsManager.registerNativeMechanics();
        //CustomBlockData.registerListener(this); //Handle this manually
        hudManager = new HudManager(configsManager);
        fontManager = new FontManager(configsManager);
        soundManager = new SoundManager(configsManager.getSound());
        OraxenItems.loadItems();
        fontManager.registerEvents();
        fontManager.verifyRequired(); // Verify the required glyph is there
        hudManager.registerEvents();
        hudManager.registerTask();
        hudManager.parsedHudDisplays = hudManager.generateHudDisplays();
        Bukkit.getPluginManager().registerEvents(new ItemUpdater(), this);
        resourcePack.generate();
        RecipesManager.load(this);
        invManager = new InvManager();
        ArmorEquipEvent.registerListener(this);
        new CommandsManager().loadCommands();
        postLoading();
        try {
            Message.PLUGIN_LOADED.log(AdventureUtils.tagResolver("os", OS.getOs().getPlatformName()));
        } catch (Exception ignore) {
        }
        CompatibilitiesManager.enableNativeCompatibilities();
        if (VersionUtil.isCompiled()) NoticeUtils.compileNotice();
        if (VersionUtil.isLeaked()) NoticeUtils.leakNotice();
//        if (VersionUtil.isFoliaServer()) NoticeUtils.foliaNotice();
    }

    private void postLoading() {
        new Metrics(this, 5371);
        new LU().l();
        Bukkit.getScheduler().runTask(this, () ->
                Bukkit.getPluginManager().callEvent(new OraxenItemsLoadedEvent()));
    }

    @Override
    public void onDisable() {
        unregisterListeners();
        FurnitureFactory.unregisterEvolution();
        for (Player player : Bukkit.getOnlinePlayers())
            if (GlyphHandlers.isNms()) NMSHandlers.getHandler().glyphHandler().uninject(player);

        CompatibilitiesManager.disableCompatibilities();
        CommandAPI.onDisable();
        Message.PLUGIN_UNLOADED.log();
    }

    private void unregisterListeners() {
        fontManager.unregisterEvents();
        hudManager.unregisterEvents();
        MechanicsManager.unloadListeners();
        HandlerList.unregisterAll(this);
    }

    public ResourcesManager resourceManager() {
        return resourceManager;
    }

    public BukkitAudiences getAudience() {
        return audience;
    }

    public void reloadConfigs() {
        resourceManager = new ResourcesManager(this);
        configsManager = new ConfigsManager(this);
        configsManager.validatesConfig();
    }

    public ConfigsManager configsManager() {
        return configsManager;
    }

    public UploadManager getUploadManager() {
        return uploadManager;
    }

    public void uploadManager(final UploadManager uploadManager) {
        this.uploadManager = uploadManager;
    }

    public FontManager getFontManager() {
        return fontManager;
    }

    public void fontManager(final FontManager fontManager) {
        this.fontManager.unregisterEvents();
        this.fontManager = fontManager;
        fontManager.registerEvents();
    }

    public HudManager getHudManager() {
        return hudManager;
    }

    public void setHudManager(final HudManager hudManager) {
        this.hudManager.unregisterEvents();
        this.hudManager = hudManager;
        hudManager.registerEvents();
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public void soundManager(final SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    public InvManager invManager() {
        return invManager;
    }

    public ResourcePack getResourcePack() {
        return resourcePack;
    }

    public ClickActionManager getClickActionManager() {
        return clickActionManager;
    }
}
