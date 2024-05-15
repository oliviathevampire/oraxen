package io.th0rgal.oraxen.utils;

import io.th0rgal.oraxen.utils.drops.Drop;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class ItemUtils {

    public static boolean isEmpty(ItemStack itemStack) {
        return itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() == 0;
    }

    public static void subtract(ItemStack itemStack, int amount) {
        itemStack.setAmount(Math.max(0, itemStack.getAmount() - amount));
    }

    public static void displayName(ItemStack itemStack, Component component) {
        editItemMeta(itemStack, itemMeta -> {
            if (VersionUtil.isPaperServer()) itemMeta.displayName(component);
            else itemMeta.setDisplayName(AdventureUtils.LEGACY_SERIALIZER.serialize(component));
        });
    }

    public static void displayName(ItemMeta itemMeta, Component component) {
        if (VersionUtil.isPaperServer()) itemMeta.displayName(component);
        else itemMeta.setDisplayName(AdventureUtils.LEGACY_SERIALIZER.serialize(component));
    }

    public static void displayName(ItemMeta itemMeta, ItemMeta otherMeta) {
        if (itemMeta == null) return;
        if (VersionUtil.isPaperServer()) itemMeta.displayName(otherMeta.displayName());
        else itemMeta.setDisplayName(otherMeta.getDisplayName());
    }

    public static void lore(ItemStack itemStack, List<Component> components) {
        if (VersionUtil.isPaperServer()) itemStack.lore(components);
        else itemStack.setLore(components.stream().map(AdventureUtils.LEGACY_SERIALIZER::serialize).toList());
    }

    public static void lore(ItemStack itemStack, Collection<String> strings) {
        if (VersionUtil.isPaperServer()) itemStack.lore(strings.stream().map(AdventureUtils.MINI_MESSAGE::deserialize).toList());
        else itemStack.setLore(strings.stream().toList());
    }

    public static void lore(ItemMeta itemMeta, List<Component> components) {
        if (VersionUtil.isPaperServer()) itemMeta.lore(components);
        else itemMeta.setLore(components.stream().map(AdventureUtils.LEGACY_SERIALIZER::serialize).toList());
    }

    public static void lore(ItemMeta itemMeta, Collection<String> strings) {
        if (VersionUtil.isPaperServer()) itemMeta.lore(strings.stream().map(AdventureUtils.MINI_MESSAGE::deserialize).toList());
        else itemMeta.setLore(strings.stream().toList());
    }

    public static void lore(ItemMeta itemMeta, ItemMeta otherMeta) {
        if (VersionUtil.isPaperServer()) itemMeta.lore(otherMeta.lore());
        else itemMeta.setLore(otherMeta.getLore());
    }

    public static void dyeItem(ItemStack itemStack, Color color) {
        editItemMeta(itemStack, meta -> {
            if (meta instanceof LeatherArmorMeta leatherArmorMeta) {
                leatherArmorMeta.setColor(color);
            } else if (meta instanceof PotionMeta potionMeta) {
                potionMeta.setColor(color);
            } else if (meta instanceof MapMeta mapMeta) {
                mapMeta.setColor(color);
            }
        });
    }

    /**
     * @param itemStack The ItemStack to edit the ItemMeta of
     * @param function  The function-block to edit the ItemMeta in
     * @return The original ItemStack with the new ItemMeta
     */
    public static ItemStack editItemMeta(ItemStack itemStack, Consumer<ItemMeta> function) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return itemStack;
        function.accept(meta);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * Used to correctly damage the item in the player's hand based on broken block
     * Only handles it if the block is a OraxenBlock or OraxenFurniture
     *
     * @param player    the player that broke the OraxenBlock or OraxenFurniture
     * @param drop      the Drop that will be dropped
     * @param itemStack the item in the player's hand
     * @return the itemStack with the correct damage applied
     */
    public static ItemStack damageItem(Player player, Drop drop, ItemStack itemStack) {

        // If all are null this is not something Oraxen should handle
        // If the block/furniture has no drop, it returns Drop.emptyDrop() which is handled by the caller
        if (drop == null) return itemStack;

        int damage;
        boolean isToolEnough = drop.isToolEnough(itemStack);
        damage = isToolEnough ? 1 : 2;
        // If the item is not a tool, it will not be damaged, example flint&steel should not be damaged
        damage = isTool(itemStack) ? damage : 0;

        if (damage == 0) return itemStack;
        if (VersionUtil.isPaperServer() && VersionUtil.atOrAbove("1.19"))
            return player.damageItemStack(itemStack, damage);
        else {
            int finalDamage = damage;
            return editItemMeta(itemStack, meta -> {
                if (meta instanceof Damageable damageable && EventUtils.callEvent(new PlayerItemDamageEvent(player, itemStack, finalDamage))) {
                    damageable.setDamage(damageable.getDamage() + 1);
                }
            });
        }
    }

    public static boolean isTool(ItemStack itemStack) {
        return isTool(itemStack.getType());
    }

    public static boolean isTool(Material material) {
        if (VersionUtil.atOrAbove("1.19.4"))
            return Tag.ITEMS_TOOLS.isTagged(material);
        else return material.toString().endsWith("_AXE")
                || material.toString().endsWith("_PICKAXE")
                || material.toString().endsWith("_SHOVEL")
                || material.toString().endsWith("_HOE")
                || material.toString().endsWith("_SWORD")
                || material == Material.TRIDENT;
    }

    public static boolean isSkull(Material material) {
        return switch (material) {
            case PLAYER_HEAD, PLAYER_WALL_HEAD, SKELETON_SKULL, SKELETON_WALL_SKULL, WITHER_SKELETON_SKULL, WITHER_SKELETON_WALL_SKULL, ZOMBIE_HEAD, ZOMBIE_WALL_HEAD, CREEPER_HEAD, CREEPER_WALL_HEAD, DRAGON_HEAD, DRAGON_WALL_HEAD, PIGLIN_HEAD, PIGLIN_WALL_HEAD ->
                    true;
            default -> false;
        };
    }

    public static boolean hasInventoryParent(Material material) {
        return Tag.WALLS.isTagged(material) || Tag.FENCES.isTagged(material) || Tag.BUTTONS.isTagged(material) || material == Material.PISTON || material == Material.STICKY_PISTON || (VersionUtil.atOrAbove("1.20") && material == Material.CHISELED_BOOKSHELF) || material == Material.BROWN_MUSHROOM_BLOCK || material == Material.RED_MUSHROOM_BLOCK || material == Material.MUSHROOM_STEM;
    }
}
