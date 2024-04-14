package io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock;

import com.google.gson.JsonObject;
import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.directional.DirectionalBlock;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.farmblock.FarmBlockTask;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.logstrip.LogStripListener;
import io.th0rgal.oraxen.nms.NMSHandlers;
import io.th0rgal.oraxen.utils.VersionUtil;
import io.th0rgal.oraxen.utils.breaker.ToolTypeSpeedModifier;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.apache.commons.lang3.Range;
import org.bukkit.Instrument;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class NoteBlockMechanicFactory extends MechanicFactory {

    private static final Integer MAX_PER_INSTRUMENT = 50;
    public static final Integer MAX_BLOCK_VARIATION = Instrument.values().length * MAX_PER_INSTRUMENT - 1;
    public static final Map<Integer, NoteBlockMechanic> BLOCK_PER_VARIATION = new HashMap<>();
    private static JsonObject variants;
    private static NoteBlockMechanicFactory instance;
    public static final Set<ToolTypeSpeedModifier> toolTypeSpeedModifiers = ToolTypeSpeedModifier.VANILLA;
    public final List<String> toolTypes;
    private boolean farmBlock;
    private static FarmBlockTask farmBlockTask;
    public final int farmBlockCheckDelay;
    public final boolean customSounds;
    private final boolean removeMineableTag;

    public NoteBlockMechanicFactory(ConfigurationSection section) {
        super(section);
        instance = this;
        variants = new JsonObject();
        variants.add("instrument=harp,powered=false,note=0", getModelJson("block/note_block"));
        toolTypes = section.getStringList("tool_types");

        farmBlockCheckDelay = section.getInt("farmblock_check_delay");
        farmBlock = false;
        customSounds = OraxenPlugin.get().getConfigsManager().getMechanics().getConfigurationSection("custom_block_sounds").getBoolean("noteblock_and_block", true);
        removeMineableTag = section.getBoolean("remove_mineable_tag", false);

        OraxenPlugin.get().getResourcePack().addModifiers(getMechanicID(), packFolder ->
                OraxenPlugin.get().getResourcePack().writeStringToVirtual(
                        "assets/minecraft/blockstates", "note_block.json", getBlockstateContent())
        );
        MechanicsManager.registerListeners(OraxenPlugin.get(), getMechanicID(),
                new NoteBlockMechanicListener(),
                new LogStripListener()
        );
        if (customSounds) MechanicsManager.registerListeners(OraxenPlugin.get(), getMechanicID(), new NoteBlockSoundListener());

        // Physics-related stuff
        if (VersionUtil.isPaperServer())
            MechanicsManager.registerListeners(OraxenPlugin.get(), getMechanicID(), new NoteBlockMechanicListener.NoteBlockMechanicPaperListener());
        if (!VersionUtil.isPaperServer() || !NMSHandlers.isNoteblockUpdatesDisabled())
            MechanicsManager.registerListeners(OraxenPlugin.get(), getMechanicID(), new NoteBlockMechanicListener.NoteBlockMechanicPhysicsListener());
        if (VersionUtil.isPaperServer() && VersionUtil.atOrAbove("1.20.1") && !NMSHandlers.isNoteblockUpdatesDisabled()) {
            Logs.logError("Papers block-updates.disable-noteblock-updates is not enabled.");
            Logs.logWarning("It is recommended to enable this setting for improved performance and prevent bugs with noteblocks");
            Logs.logWarning("Otherwise Oraxen needs to listen to very taxing events, which also introduces some bugs");
            Logs.logWarning("You can enable this setting in ServerFolder/config/paper-global.yml", true);
        }
    }

    public static String instrumentName(Instrument instrument) {
        return switch (instrument) {
            case BASS_DRUM -> "basedrum";
            case PIANO -> "harp";
            case SNARE_DRUM -> "snare";
            case STICKS -> "hat";
            case BASS_GUITAR -> "bass";
            default -> instrument.name().toLowerCase();
        };
    }

    public static JsonObject getModelJson(String modelName) {
        JsonObject content = new JsonObject();
        content.addProperty("model", modelName);

        return content;
    }

    public static JsonObject getDirectionalModelJson(String modelName, NoteBlockMechanic mechanic, NoteBlockMechanic parentMechanic) {
        String itemId = mechanic.getItemID();
        JsonObject content = new JsonObject();
        DirectionalBlock parent = parentMechanic.getDirectional();
        String subBlockModel = mechanic.getDirectional().getDirectionalModel(mechanic);
        content.addProperty("model", subBlockModel != null ? subBlockModel : modelName);
        // If subModel is specified and is different from parent we don't want to rotate it
        if (subBlockModel != null && !Objects.equals(subBlockModel, modelName)) return content;

        if (Objects.equals(parent.getYBlock(), itemId))
            return content;
        else if (Objects.equals(parent.getXBlock(), itemId)) {
            content.addProperty("x", 90);
            content.addProperty("z", 90);
        } else if (Objects.equals(parent.getZBlock(), itemId)) {
            content.addProperty("y", 90);
            content.addProperty("x", 90);
        } else if (Objects.equals(parent.getNorthBlock(), itemId))
            return content;
        else if (Objects.equals(parent.getEastBlock(), itemId)) {
            content.addProperty("y", 90);
        } else if (Objects.equals(parent.getSouthBlock(), itemId))
            content.addProperty("y", 180);
        else if (Objects.equals(parent.getWestBlock(), itemId)) {
            content.addProperty("z", 90);
            content.addProperty("y", 270);
        } else if (Objects.equals(parent.getUpBlock(), itemId))
            content.addProperty("y", 270);
        else if (Objects.equals(parent.getDownBlock(), itemId))
            content.addProperty("x", 180);

        return content;
    }

    @Nullable
    public static NoteBlockMechanic getBlockMechanic(NoteBlock blockData) {
        return BLOCK_PER_VARIATION.values().stream().filter(m -> m.blockData().equals(blockData)).findFirst().orElse(null);
    }

    public static boolean isEnabled() {
        return instance != null;
    }

    public static NoteBlockMechanicFactory getInstance() {
        return instance;
    }

    public boolean removeMineableTag() {
        return removeMineableTag;
    }

    /**
     * Attempts to set the block directly to the model and texture of an Oraxen item.
     *
     * @param block  The block to update.
     * @param itemId The Oraxen item ID.
     */
    public static void setBlockModel(Block block, String itemId) {
        NoteBlockMechanic mechanic = OraxenBlocks.getNoteBlockMechanic(itemId);
        if (mechanic != null) block.setBlockData(mechanic.blockData());
    }

    private String getBlockstateContent() {
        JsonObject noteblock = new JsonObject();
        noteblock.add("variants", variants);
        return noteblock.toString();
    }

    @Override
    public Mechanic parse(ConfigurationSection itemMechanicConfiguration) {
        NoteBlockMechanic mechanic = new NoteBlockMechanic(this, itemMechanicConfiguration);
        if (!Range.between(1, MAX_BLOCK_VARIATION).contains(mechanic.getCustomVariation())) {
            Logs.logError("The custom variation of the block " + mechanic.getItemID() + " is not between 1 and " + MAX_BLOCK_VARIATION + "!");
            Logs.logWarning("The item has failed to build for now to prevent bugs and issues.");
            return null;
        }
        DirectionalBlock directional = mechanic.getDirectional();
        String modelName = mechanic.getModel();

        String variantName = "instrument=" + instrumentName(mechanic.blockData().getInstrument()) + ",note=" + mechanic.blockData().getNote().getId() + ",powered=" + mechanic.blockData().isPowered();
        if (mechanic.isDirectional() && !directional.isParentBlock()) {
            NoteBlockMechanic parentMechanic = directional.getParentMechanic();
            modelName = (parentMechanic.getModel());
            variants.add(variantName, getDirectionalModelJson(modelName, mechanic, parentMechanic));
        } else {
            variants.add(variantName, getModelJson(modelName));
        }

        BLOCK_PER_VARIATION.put(mechanic.getCustomVariation(), mechanic);
        addToImplemented(mechanic);
        return mechanic;
    }

    @Override
    public NoteBlockMechanic getMechanic(String itemID) {
        return (NoteBlockMechanic) super.getMechanic(itemID);
    }

    @Override
    public NoteBlockMechanic getMechanic(ItemStack itemStack) {
        return (NoteBlockMechanic) super.getMechanic(itemStack);
    }

    public NoteBlock getNoteBlockData(String itemId) {
        return instance.getMechanic(itemId).blockData();
    }

    public void registerFarmBlock() {
        if (farmBlock) return;
        if (farmBlockTask != null) farmBlockTask.cancel();

        farmBlockTask = new FarmBlockTask(farmBlockCheckDelay);
        BukkitTask task = farmBlockTask.runTaskTimer(OraxenPlugin.get(), 0, farmBlockCheckDelay);
        MechanicsManager.registerTask(getMechanicID(), task);
        farmBlock = true;
    }

    public float getSpeedMultiplier(Player player, Block block) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        AtomicReference<Float> speedMultiplier = new AtomicReference<>((float) 1);
        List<ToolTypeSpeedModifier> validToolTypes = toolTypeSpeedModifiers.stream().filter(t -> t.getToolType().contains(itemInMainHand.getType()))
                .sorted(Comparator.comparingDouble(ToolTypeSpeedModifier::getSpeedModifier))
                .toList();

        // Find first validToolTypes that contains the block material
        // If none found, use the first validToolTypes
        validToolTypes.stream().filter(t -> t.getMaterials().contains(block.getType()))
                .findFirst().ifPresentOrElse(toolTypeSpeedModifier -> speedMultiplier.set(toolTypeSpeedModifier.getSpeedModifier()), () ->
                        speedMultiplier.set(validToolTypes.stream().findFirst().get().getSpeedModifier()));

        float multiplier = speedMultiplier.get();
        if (itemInMainHand.containsEnchantment(Enchantment.DIG_SPEED))
            multiplier *= 1f + (itemInMainHand.getEnchantmentLevel(Enchantment.DIG_SPEED) ^ 2 + 1);

        PotionEffect haste = player.getPotionEffect(PotionEffectType.FAST_DIGGING);
        if (haste != null) multiplier *= 1f + (0.2F * haste.getAmplifier() + 1);

        // Whilst the player has this when they start digging, period is calculated before it is applied
        PotionEffect miningFatigue = player.getPotionEffect(PotionEffectType.SLOW_DIGGING);
        if (miningFatigue != null) multiplier *= 1f - (0.3F * miningFatigue.getAmplifier() + 1);

        ItemStack helmet = player.getEquipment().getHelmet();
        if (player.isInWater() && (helmet == null || !helmet.containsEnchantment(Enchantment.WATER_WORKER)))
            multiplier /= 5;

        if (!player.isOnGround()) multiplier /= 5;

        return multiplier;
    }

}
