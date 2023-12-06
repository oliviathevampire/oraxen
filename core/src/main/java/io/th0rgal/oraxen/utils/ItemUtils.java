package io.th0rgal.oraxen.utils;

import io.th0rgal.oraxen.utils.drops.Drop;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

public class ItemUtils {

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
     * @param player the player that broke the OraxenBlock or OraxenFurniture
     * @param drop the Drop that will be dropped
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
        damage = Tag.ITEMS_TOOLS.isTagged(itemStack.getType()) ? damage : 0;

        if (damage == 0) return itemStack;
        try {
            return player.damageItemStack(itemStack, damage);
        } catch (Exception e) {
            int finalDamage = damage;
            return editItemMeta(itemStack, meta -> {
                if (meta instanceof Damageable damageable && EventUtils.callEvent(new PlayerItemDamageEvent(player, itemStack, finalDamage))) {
                    damageable.setDamage(damageable.getDamage() + 1);
                }
            });
        }
    }
}
