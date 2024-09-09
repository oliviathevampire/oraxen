package io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock;

import com.google.gson.JsonObject;
import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.beacon.BeaconListener;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.beacon.BeaconTagDatapack;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.directional.DirectionalBlock;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.logstrip.LogStripListener;
import io.th0rgal.oraxen.nms.NMSHandlers;
import io.th0rgal.oraxen.utils.breaker.ToolTypeSpeedModifier;
import io.th0rgal.oraxen.utils.logs.Logs;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.Range;
import org.bukkit.Instrument;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NoteBlockMechanicFactory extends MechanicFactory {

    public static final NamespacedKey MINEABLE_PACKET_LISTENER = NamespacedKey.fromString("mineable_with_key", OraxenPlugin.get());
    private static final Integer MAX_PER_INSTRUMENT = 50;
    public static final Integer MAX_BLOCK_VARIATION = Instrument.values().length * MAX_PER_INSTRUMENT - 1;
    public static final Map<Integer, NoteBlockMechanic> BLOCK_PER_VARIATION = new HashMap<>();
    public static final Set<ToolTypeSpeedModifier> toolTypeSpeedModifiers = ToolTypeSpeedModifier.VANILLA;
    private static NoteBlockMechanicFactory instance;
    public final List<String> toolTypes;
    public final boolean customSounds;
    public final boolean reimplementNoteblockFeatures;
    private final boolean removeMineableTag;
    private boolean notifyOfDeprecation = true;
    private static JsonObject variants;

    public NoteBlockMechanicFactory(ConfigurationSection section) {
        super(section);
        instance = this;

        toolTypes = section.getStringList("tool_types");
        customSounds = OraxenPlugin.get().configsManager().getMechanics().getBoolean("custom_block_sounds.noteblock", true);
        removeMineableTag = section.getBoolean("remove_mineable_tag", false);
        reimplementNoteblockFeatures = section.getBoolean("reimplement_noteblock_features", false);

        MechanicsManager.registerListeners(OraxenPlugin.get(), getMechanicID(),
                new NoteBlockMechanicListener(),
                new LogStripListener(),
                new BeaconListener()
        );
        if (customSounds) MechanicsManager.registerListeners(OraxenPlugin.get(), getMechanicID(), new NoteBlockSoundListener());

        BeaconTagDatapack.generateDatapack();

        // Physics-related stuff
        MechanicsManager.registerListeners(OraxenPlugin.get(), getMechanicID(), new NoteBlockMechanicPaperListener());
        if (!NMSHandlers.isNoteblockUpdatesDisabled()) {
            Logs.logError("Papers block-updates.disable-noteblock-updates is not enabled.");
            if (reimplementNoteblockFeatures) Logs.logError("reimplement_noteblock_feature mechanic will not be enabled");
            Logs.logWarning("It is recommended to enable this setting for improved performance and prevent bugs with noteblocks");
            Logs.logWarning("Otherwise Oraxen needs to listen to very taxing events, which also introduces some bugs");
            Logs.logWarning("You can enable this setting in ServerFolder/config/paper-global.yml", true);
        } else if (reimplementNoteblockFeatures)
            MechanicsManager.registerListeners(OraxenPlugin.get(), getMechanicID(), new NoteBlockMechanicInstrumentListener());
    }

    public static boolean isEnabled() {
        return instance != null;
    }

    public static NoteBlockMechanicFactory get() {
        return instance;
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
        DirectionalBlock parent = parentMechanic.directional();
        String subBlockModel = mechanic.directional().getDirectionalModel(mechanic);
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

    @Override
    public Mechanic parse(ConfigurationSection itemMechanicConfiguration) {
        NoteBlockMechanic mechanic = new NoteBlockMechanic(this, itemMechanicConfiguration);
        if (!Range.between(1, MAX_BLOCK_VARIATION).contains(mechanic.customVariation())) {
            Logs.logError("The custom variation of the block " + mechanic.getItemID() + " is not between 1 and " + MAX_BLOCK_VARIATION + "!");
            Logs.logWarning("The item has failed to build for now to prevent bugs and issues.");
            return null;
        }
        DirectionalBlock directional = mechanic.directional();
        Key modelName = mechanic.model();

        String variantName = "instrument=" + instrumentName(mechanic.blockData().getInstrument()) + ",note=" + mechanic.blockData().getNote().getId() + ",powered=" + mechanic.blockData().isPowered();
        if (mechanic.isDirectional() && !directional.isParentBlock()) {
            NoteBlockMechanic parentMechanic = directional.getParentMechanic();
            modelName = (parentMechanic.model());
            variants.add(variantName, getDirectionalModelJson(modelName.value(), mechanic, parentMechanic));
        } else {
            variants.add(variantName, getModelJson(modelName.value()));
        }

        BLOCK_PER_VARIATION.put(mechanic.customVariation(), mechanic);
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
}
