package io.th0rgal.oraxen.items;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ItemStackBuilder {

    private final ItemStack stack;

    public ItemStackBuilder(Material material) {
        this.stack = new ItemStack(material);
    }

    public ItemStackBuilder(ItemStack material) {
        this.stack = material;
    }

    public ItemStackBuilder setAmount(int amount) {
        stack.setAmount(Math.min(amount, stack.getType().getMaxStackSize()));
        return this;
    }

    public ItemStackBuilder setColor(Color color) {
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof LeatherArmorMeta leatherArmorMeta) {
            leatherArmorMeta.setColor(color);
        }
        // Assume other types of Meta also support color
        stack.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder setUnbreakable(boolean unbreakable) {
        ItemMeta meta = stack.getItemMeta();
        meta.setUnbreakable(unbreakable);
        stack.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder setLore(List<Component> lore) {
        ItemMeta meta = stack.getItemMeta();
        meta.lore(lore);
        stack.setItemMeta(meta);
        return this;
    }


    public ItemStackBuilder setDisplayName(final Component displayName) {
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(displayName);
        stack.setItemMeta(meta);
        return this;
    }

    public <T, Z> ItemStackBuilder setCustomTag(final NamespacedKey namespacedKey, final PersistentDataType<T, Z> dataType, final Z data) {
//        persistentDataMap.put(new PersistentDataSpace(namespacedKey, dataType), data); //TODO
        return this;
    }

    public ItemStack build() {
        return stack;
    }

    // Additional setter methods for other item attributes.
}