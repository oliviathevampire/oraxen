package io.th0rgal.oraxen.nms;

import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.IFurniturePacketManager;
import io.th0rgal.oraxen.utils.InteractionResult;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectTypeWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

public interface NMSHandler {
    default boolean isEmpty() {
        return this.equals(new io.th0rgal.oraxen.nms.EmptyNMSHandler());
    }
    default IFurniturePacketManager furniturePacketManager() {
        return new EmptyFurniturePacketManager();
    }

    GlyphHandler glyphHandler();

    /**
     * Registers a PacketListener to handle ResourcePack-dispatching in Configuration-phase
     * <p>
     * Note: Only works for 1.20.3+ Paper-Servers
     */
    default void registerConfigPhaseListener() {}
    default void unregisterConfigPhaseListener() {}

    boolean noteblockUpdatesDisabled();

    boolean tripwireUpdatesDisabled();

    int playerProtocolVersion(Player player);

    /**
     * Copies over all NBT-Tags from oldItem to newItem
     * Useful for plugins that might register their own NBT-Tags outside
     * the ItemStacks PersistentDataContainer
     *
     * @param oldItem The old ItemStack to copy the NBT-Tags from
     * @param newItem The new ItemStack to copy the NBT-Tags to
     * @return The new ItemStack with the copied NBT-Tags
     */
    ItemStack copyItemNBTTags(@NotNull ItemStack oldItem, @NotNull ItemStack newItem);

    /**
     * Corrects the BlockData of a placed block.
     * Mainly fired when placing a block against an OraxenNoteBlock due to vanilla behaviour requiring Sneaking
     *
     * @param player          The player that placed the block
     * @param slot            The hand the player placed the block with
     * @param itemStack       The ItemStack the player placed the block with
     * @return The enum interaction result
     */
    @Nullable InteractionResult correctBlockStates(Player player, EquipmentSlot slot, ItemStack itemStack);

    /**
     * Keys that are used by vanilla Minecraft and should therefore be skipped
     * Some are accessed through API methods, others are just used internally
     */
    Set<String> vanillaKeys = Set.of("PublicBukkitValues", "display", "CustomModelData", "Damage", "AttributeModifiers",
            "Unbreakable", "CanDestroy", "slot", "count", "HideFlags", "CanPlaceOn", "Enchantments", "StoredEnchantments",
            "RepairCost", "CustomPotionEffects", "Potion", "CustomPotionColor", "Trim", "EntityTag",
            "pages", "filtered_pages", "filtered_title", "resolved", "generation", "author", "title",
            "BucketVariantTag", "Items", "LodestoneTracked", "LodestoneDimension", "LodestonePos",
            "ChargedProjectiles", "Charged", "DebugProperty", "Fireworks", "Explosion", "Flight",
            "map", "map_scale_direction", "map_to_lock", "Decorations", "SkullOwner", "Effects", "BlockEntityTag", "BlockStateTag");

    default boolean getSupported() {
        return false;
    }

    class EmptyNMSHandler implements NMSHandler {

        @Override
        public GlyphHandler glyphHandler() {
            return new GlyphHandler.EmptyGlyphHandler();
        }

        @Override
        public boolean noteblockUpdatesDisabled() {
            return false;
        }

        @Override
        public boolean tripwireUpdatesDisabled() {
            return false;
        }

        @Override
        public int playerProtocolVersion(Player player) {
            return 0;
        }

        @Override
        public ItemStack copyItemNBTTags(@NotNull ItemStack oldItem, @NotNull ItemStack newItem) {
            return newItem;
        }

        @Nullable
        @Override
        public InteractionResult correctBlockStates(Player player, EquipmentSlot slot, ItemStack itemStack) {
            return null;
        }

        @NotNull
        @Override
        public @Unmodifiable Set<Material> itemTools() {
            return Set.of();
        }

        @Override
        public void applyMiningEffect(Player player) {
            player.addPotionEffect(new PotionEffect(PotionEffectTypeWrapper.MINING_FATIGUE, -1, Integer.MAX_VALUE, false, false, false));
        }

        @Override
        public void removeMiningEffect(Player player) {
            player.removePotionEffect(PotionEffectTypeWrapper.MINING_FATIGUE);
        }

        @Override
        public String getNoteBlockInstrument(Block block) {
            return "block.note_block.harp";
        }
    }

    @NotNull
    @Unmodifiable
    Set<Material> itemTools();


    default void applyMiningEffect(Player player) {}

    default void removeMiningEffect(Player player) {}

    String getNoteBlockInstrument(Block block);
}
