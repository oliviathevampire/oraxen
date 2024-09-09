package io.th0rgal.oraxen.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;

public class EntityUtils {
    public static boolean isUnderWater(Entity entity) {
        return entity.isUnderWater();
    }

    public static boolean isFixed(ItemDisplay itemDisplay) {
        return itemDisplay.getItemDisplayTransform() == ItemDisplay.ItemDisplayTransform.FIXED;
    }

    public static boolean isNone(ItemDisplay itemDisplay) {
        return itemDisplay.getItemDisplayTransform() == ItemDisplay.ItemDisplayTransform.NONE;
    }

    public static void customName(Entity entity, Component customName) {
        entity.customName(customName);
    }

}

