package io.th0rgal.oraxen.items;

import org.bukkit.Color;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class ItemMetaHandler {

    public static ItemMeta updateDamageableMeta(ItemMeta meta, int durability) {
        if (meta instanceof Damageable damageable) {
            damageable.setDamage(durability);
        }
        return meta;
    }

    public static ItemMeta updateColorMeta(ItemMeta meta, Color color) {
        if (meta instanceof LeatherArmorMeta leatherArmorMeta) {
            leatherArmorMeta.setColor(color);
        }
        // Other conditions for different meta types that support color
        return meta;
    }

    // Additional methods for other metadata types like color, enchantments, etc.
}