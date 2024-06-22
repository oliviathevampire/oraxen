package io.th0rgal.oraxen.utils.customarmor;

import io.th0rgal.oraxen.config.Settings;
import io.th0rgal.oraxen.utils.logs.Logs;

public enum CustomArmorType {
    NONE, SHADER, TRIMS;

    public static CustomArmorType getSetting() {
        return fromString(Settings.CUSTOM_ARMOR_DEFAULT_TYPE.toString());
    }

    public static CustomArmorType fromString(String type) {
        try {
			return CustomArmorType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            Logs.logError("Invalid custom armor type: " + type);
            Logs.logError("Defaulting to NONE.");
            return NONE;
        }
    }
}
