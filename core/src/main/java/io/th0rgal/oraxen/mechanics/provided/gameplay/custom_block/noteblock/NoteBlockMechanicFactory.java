package io.th0rgal.oraxen.mechanics.provided.gameplay.custom_block.noteblock;

import com.google.gson.JsonObject;
import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.mechanics.Mechanic;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import io.th0rgal.oraxen.mechanics.provided.gameplay.custom_block.noteblock.beacon.BeaconListener;
import io.th0rgal.oraxen.mechanics.provided.gameplay.custom_block.noteblock.beacon.BeaconTagDatapack;
import io.th0rgal.oraxen.mechanics.provided.gameplay.custom_block.noteblock.directional.DirectionalBlock;
import io.th0rgal.oraxen.mechanics.provided.gameplay.custom_block.noteblock.logstrip.LogStripListener;
import io.th0rgal.oraxen.nms.NMSHandlers;
import io.th0rgal.oraxen.utils.VersionUtil;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.apache.commons.lang3.Range;
import org.bukkit.Instrument;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NoteBlockMechanicFactory extends MechanicFactory {

    public static final NamespacedKey MINEABLE_PACKET_LISTENER = NamespacedKey.fromString("mineable_with_key", OraxenPlugin.get());
    private static JsonObject variants;
    private static final Integer MAX_PER_INSTRUMENT = 50;
    public static final Integer MAX_BLOCK_VARIATION = Instrument.values().length * MAX_PER_INSTRUMENT - 1;
    public static final Map<Integer, NoteBlockMechanic> BLOCK_PER_VARIATION = new HashMap<>();
    private static NoteBlockMechanicFactory instance;
    public final List<String> toolTypes;
    public final boolean customSounds;
    public final boolean reimplementNoteblockFeatures;
    private final boolean notifyOfDeprecation = true;

    public NoteBlockMechanicFactory(ConfigurationSection section) {
        super(section);
        instance = this;

        variants = new JsonObject();
        variants.add("instrument=harp,powered=false", getModelJson("block/note_block"));
        toolTypes = section.getStringList("tool_types");
        customSounds = OraxenPlugin.get().getConfigsManager().getMechanics().getBoolean("custom_block_sounds.noteblock", true);
        reimplementNoteblockFeatures = section.getBoolean("reimplement_noteblock_features", false);

        if (VersionUtil.isPaperServer()) new NoteBlockDatapack().generateDatapack();

        // this modifier should be executed when all the items have been parsed, just
        // before zipping the pack
        OraxenPlugin.get().getResourcePack().addModifiers(getMechanicID(), packFolder ->
                OraxenPlugin.get().getResourcePack().writeStringToVirtual(
                        "assets/minecraft/blockstates", "note_block.json", getBlockstateContent())
        );

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
            MechanicsManager.registerListeners(OraxenPlugin.get(), getMechanicID(), new NoteBlockMechanicPhysicsListener());
            Logs.logError("Papers block-updates.disable-noteblock-updates is not enabled.");
            if (reimplementNoteblockFeatures) Logs.logError("reimplement_noteblock_feature mechanic will not be enabled");
            Logs.logWarning("It is recommended to enable this setting for improved performance and prevent bugs with noteblocks");
            Logs.logWarning("Otherwise Oraxen needs to listen to very taxing events, which also introduces some bugs");
            Logs.logWarning("You can enable this setting in ServerFolder/config/paper-global.yml", true);
        } else if (reimplementNoteblockFeatures)
            MechanicsManager.registerListeners(OraxenPlugin.get(), getMechanicID(), new NoteBlockMechanicInstrumentListener());
    }

    public static String getInstrumentName(int id) {
        return switch ((id % 400) / 25) {
            case 1 -> "basedrum";
            case 2 -> "snare";
            case 3 -> "hat";
            case 4 -> "bass";
            case 5 -> "flute";
            case 6 -> "bell";
            case 7 -> "guitar";
            case 8 -> "chime";
            case 9 -> "xylophone";
            case 10 -> "iron_xylophone";
            case 11 -> "cow_bell";
            case 12 -> "didgeridoo";
            case 13 -> "bit";
            case 14 -> "banjo";
            case 15 -> "pling";
            default -> "harp";
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

    public static String getBlockstateVariantName(int id) {
        id += 26;
        return getBlockstateVariantName(getInstrumentName(id), id % 25, id >= 400);
    }

    public static String getBlockstateVariantName(String instrument, int note, boolean powered) {
        return "instrument=" + instrument + ",note=" + note + ",powered=" + powered;
    }

    public static NoteBlockMechanic getBlockMechanic(int customVariation) {
        return BLOCK_PER_VARIATION.get(customVariation);
    }

    public static boolean isEnabled() {
        return instance != null;
    }

    public static NoteBlockMechanicFactory get() {
        return instance;
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
        if (!Range.between(0, 775).contains(mechanic.customVariation())) {
            Logs.logError("The custom variation of the block " + mechanic.getItemID() + " is not between 0 and 775!");
            Logs.logWarning("The item has failed to build for now to prevent bugs and issues.");
            return null;
        }
        DirectionalBlock directional = mechanic.directional();
        String modelName = mechanic.getModel(itemMechanicConfiguration.getParent().getParent());

        if (mechanic.isDirectional() && !directional.isParentBlock()) {
            NoteBlockMechanic parentMechanic = directional.getParentMechanic();
            modelName = (parentMechanic.getModel(itemMechanicConfiguration.getParent().getParent()));
            variants.add(getBlockstateVariantName(mechanic.customVariation()),
                    getDirectionalModelJson(modelName, mechanic, parentMechanic));
        } else {
            variants.add(getBlockstateVariantName(mechanic.customVariation()),
                    getModelJson(modelName));
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
