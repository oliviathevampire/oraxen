package io.th0rgal.oraxen.mechanics.provided.gameplay.furniture;

import io.th0rgal.oraxen.api.OraxenFurniture;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface IFurniturePacketManager {

    BlockData BARRIER_DATA = Material.BARRIER.createBlockData();
    BlockData AIR_DATA = Material.AIR.createBlockData();

    Set<FurnitureBaseEntity> furnitureBaseMap = new HashSet<>();
    Map<UUID, Set<BlockLocation>> barrierHitboxPositionMap = new HashMap<>();
    Map<UUID, Set<BlockLocation>> lightMechanicPositionMap = new HashMap<>();
    Set<FurnitureSubEntity> interactionHitboxIdMap = new HashSet<>();

    int nextEntityId();

    default Optional<FurnitureBaseEntity> furnitureBaseFromBaseEntity(@NotNull Entity baseEntity) {
        return furnitureBaseMap.stream().filter(f -> f.baseUUID() == baseEntity.getUniqueId()).findFirst();
    }

    @Nullable
    default Entity baseEntityFromFurnitureBase(int furnitureBaseId) {
        return furnitureBaseMap.stream().filter(f -> f.baseId() == furnitureBaseId)
                .map(FurnitureBaseEntity::baseEntity).findFirst().orElse(null);
    }

    @Nullable
    default ItemDisplay baseEntityFromHitbox(int interactionId) {
        return interactionHitboxIdMap.stream().filter(h -> h.entityIds().contains(interactionId))
                .map(FurnitureSubEntity::baseEntity).findFirst().orElse(null);
    }

    @Nullable
    default ItemDisplay baseEntityFromHitbox(BlockLocation barrierLocation) {
        for (Map.Entry<UUID, Set<BlockLocation>> entry : barrierHitboxPositionMap.entrySet()) {
            if (entry.getValue().stream().anyMatch(barrierLocation::equals)) return (ItemDisplay) Bukkit.getEntity(entry.getKey());
        }
        return null;
    }

    default void sendFurnitureEntityPacket(@NotNull ItemDisplay baseEntity, @NotNull FurnitureMechanic mechanic, @NotNull Player player) {}
    default void removeFurnitureEntityPacket(@NotNull ItemDisplay baseEntity, @NotNull FurnitureMechanic mechanic) {}
    default void removeFurnitureEntityPacket(@NotNull ItemDisplay baseEntity, @NotNull FurnitureMechanic mechanic, @NotNull Player player) {}

    void sendInteractionEntityPacket(@NotNull ItemDisplay baseEntity, @NotNull FurnitureMechanic mechanic, @NotNull Player player);
    void removeInteractionHitboxPacket(@NotNull ItemDisplay baseEntity, @NotNull FurnitureMechanic mechanic);
    void removeInteractionHitboxPacket(@NotNull ItemDisplay baseEntity, @NotNull FurnitureMechanic mechanic, @NotNull Player player);

    void sendBarrierHitboxPacket(@NotNull ItemDisplay baseEntity, @NotNull FurnitureMechanic mechanic, @NotNull Player player);
    void removeBarrierHitboxPacket(@NotNull ItemDisplay baseEntity, @NotNull FurnitureMechanic mechanic);
    void removeBarrierHitboxPacket(@NotNull ItemDisplay baseEntity, @NotNull FurnitureMechanic mechanic, @NotNull Player player);

    default void sendLightMechanicPacket(@NotNull ItemDisplay baseEntity, @NotNull FurnitureMechanic mechanic, @NotNull Player player) {}
    default void removeLightMechanicPacket(@NotNull ItemDisplay baseEntity, @NotNull FurnitureMechanic mechanic) {}
    default void removeLightMechanicPacket(@NotNull ItemDisplay baseEntity, @NotNull FurnitureMechanic mechanic, @NotNull Player player) {}

    default void removeAllFurniturePackets() {
        for (World world : Bukkit.getWorlds()) for (ItemDisplay entity : world.getEntitiesByClass(ItemDisplay.class)) {
            FurnitureMechanic mechanic = OraxenFurniture.getFurnitureMechanic(entity);
            if (mechanic == null) continue;
            removeFurnitureEntityPacket(entity, mechanic);
            removeLightMechanicPacket(entity, mechanic);
            removeInteractionHitboxPacket(entity, mechanic);
            removeBarrierHitboxPacket(entity, mechanic);
        }
    }
}