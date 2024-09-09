package io.th0rgal.oraxen.mechanics.provided.gameplay.custom_block.stringblock;

import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.mechanics.provided.gameplay.custom_block.CustomBlockMechanic;
import io.th0rgal.oraxen.mechanics.provided.gameplay.custom_block.stringblock.sapling.SaplingMechanic;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class StringBlockMechanic extends CustomBlockMechanic {
    private final List<String> randomPlace;
    private final SaplingMechanic sapling;
    private final boolean isTall;
    private final boolean placeableOnWater;

    @SuppressWarnings("unchecked")
    public StringBlockMechanic(MechanicFactory mechanicFactory, ConfigurationSection section) {
        // Creates an instance of CustomBlockMechanic and applies the below
        super(mechanicFactory, section);

        isTall = section.getBoolean("is_tall");
        placeableOnWater = section.getBoolean("placeable_on_water");
        randomPlace = section.getStringList("random_place");

        ConfigurationSection saplingSection = section.getConfigurationSection("sapling");
        sapling = saplingSection != null ? new SaplingMechanic(getItemID(), saplingSection) : null;
    }

    @Override
    public Tripwire blockData() {
        return (Tripwire) super.blockData();
    }

    @Override
    public Tripwire createBlockData() {
        Tripwire tripwire = ((Tripwire) Bukkit.createBlockData(Material.TRIPWIRE));
        if (Settings.LEGACY_NOTEBLOCKS.toBool()) {
            int i = 0;
            for (BlockFace face : new BlockFace[]{BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH})
                tripwire.setFace(face, (customVariation() & 0x1 << i++) != 0);
            tripwire.setAttached((customVariation() & 0x1 << i++) != 0);
            tripwire.setDisarmed((customVariation() & 0x1 << i++) != 0);
            tripwire.setPowered((customVariation() & 0x1 << i) != 0);
        } else {
            tripwire.setFace(BlockFace.NORTH, (customVariation() & 0x1) != 0);
            tripwire.setFace(BlockFace.SOUTH, (customVariation() & 0x2) != 0);
            tripwire.setFace(BlockFace.EAST, (customVariation() & 0x4) != 0);
            tripwire.setFace(BlockFace.WEST, (customVariation() & 0x8) != 0);
            tripwire.setAttached((customVariation() & 0x10) != 0);
            tripwire.setDisarmed((customVariation() & 0x20) != 0);
            tripwire.setPowered((customVariation() & 0x40) != 0);
        }

        return tripwire;
    }

    public boolean isSapling() {
        return sapling != null;
    }

    public SaplingMechanic sapling() {
        return sapling;
    }

    public boolean isTall() {
        return isTall;
    }

    public boolean isPlaceableOnWater() {
        return placeableOnWater;
    }

    public boolean hasRandomPlace() {
        return !randomPlace.isEmpty();
    }

    public List<String> randomPlace() {
        return randomPlace;
    }

}
