package io.th0rgal.oraxen.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.TextArgument;
import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.api.OraxenFurniture;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.api.events.OraxenItemsLoadedEvent;
import io.th0rgal.oraxen.config.Message;
import io.th0rgal.oraxen.config.Settings;
import io.th0rgal.oraxen.font.FontManager;
import io.th0rgal.oraxen.hud.HudManager;
import io.th0rgal.oraxen.items.ItemUpdater;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import io.th0rgal.oraxen.recipes.RecipesManager;
import io.th0rgal.oraxen.sound.SoundManager;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

public class ReloadCommand {

    public static void reloadItems(@Nullable CommandSender sender) {
        Message.ITEM_RELOAD.send(sender);
        OraxenItems.loadItems();
        Bukkit.getPluginManager().callEvent(new OraxenItemsLoadedEvent());

        if (Settings.UPDATE_ITEMS.toBool() && Settings.UPDATE_ITEMS_ON_RELOAD.toBool()) {
            Logs.logInfo("Updating all items in player-inventories...");
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                PlayerInventory inventory = player.getInventory();
                Bukkit.getScheduler().runTaskAsynchronously(OraxenPlugin.get(), () -> {
                    for (int i = 0; i < inventory.getSize(); i++) {
                        ItemStack oldItem = inventory.getItem(i);
                        ItemStack newItem = ItemUpdater.updateItem(oldItem);
                        if (oldItem == null || oldItem.equals(newItem)) continue;
                        inventory.setItem(i, newItem);
                    }
                });
            }
        }

        if (Settings.UPDATE_FURNITURE.toBool() && Settings.UPDATE_FURNITURE_ON_RELOAD.toBool()) {
            Logs.logInfo("Updating all placed furniture...");
            for (World world : Bukkit.getServer().getWorlds())
                world.getEntities().stream().filter(OraxenFurniture::isBaseEntity).forEach(OraxenFurniture::updateFurniture);
        }

    }

    public static void reloadPack(@Nullable CommandSender sender) {
        Message.PACK_REGENERATED.send(sender);
        OraxenPlugin.get().fontManager(new FontManager(OraxenPlugin.get().configsManager()));
        OraxenPlugin.get().soundManager(new SoundManager(OraxenPlugin.get().configsManager().getSound()));
        OraxenPlugin.get().getResourcePack().generate();
    }

    public static void reloadHud(@Nullable CommandSender sender) {
        Message.HUD_RELOAD.send(sender);
        OraxenPlugin.get().reloadConfigs();
        HudManager hudManager = new HudManager(OraxenPlugin.get().configsManager());
        OraxenPlugin.get().setHudManager(hudManager);
        hudManager.loadHuds(hudManager.getHudConfigSection());
        hudManager.parsedHudDisplays = hudManager.generateHudDisplays();
        hudManager.reregisterEvents();
        hudManager.restartTask();
    }

    public static void reloadRecipes(@Nullable CommandSender sender) {
        Message.RECIPE_RELOAD.send(sender);
        RecipesManager.reload();
    }

    public static void reloadConfigs(@Nullable CommandSender sender) {
        Message.CONFIG_RELOAD.send(sender);
        MechanicsManager.unloadListeners();
        MechanicsManager.registerNativeMechanics();
        MechanicsManager.unregisterTasks();
        OraxenPlugin.get().reloadConfigs();
        OraxenPlugin.get().invManager().regen();
    }

    CommandAPICommand getReloadCommand() {
        return new CommandAPICommand("reload")
                .withAliases("rl")
                .withPermission("oraxen.command.reload")
                .withArguments(new TextArgument("type").replaceSuggestions(
                        ArgumentSuggestions.strings("items", "pack", "hud", "recipes", "messages", "all", "configs")))
                .executes((sender, args) -> {
                    switch (((String) args.get("type")).toUpperCase()) {
                        case "HUD" -> reloadHud(sender);
                        case "ITEMS" -> reloadItems(sender);
                        case "PACK" -> reloadPack(sender);
                        case "RECIPES" -> reloadRecipes(sender);
                        case "CONFIGS" -> reloadConfigs(sender);
                        default -> {
                            reloadConfigs(sender);
                            reloadHud(sender);
                            reloadItems(sender);
                            reloadPack(sender);
                            reloadRecipes(sender);
                        }
                    }
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        OraxenPlugin.get().getFontManager().sendGlyphTabCompletion(player);
                    }
                });
    }

}
