package io.th0rgal.oraxen.nms.v1_20_R4;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.IFurniturePacketManager;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.NoteBlockMechanicFactory;
import io.th0rgal.oraxen.nms.GlyphHandler;
import io.th0rgal.oraxen.nms.v1_20_R4.furniture.FurniturePacketManager;
import io.th0rgal.oraxen.utils.BlockHelpers;
import io.th0rgal.oraxen.utils.InteractionResult;
import io.th0rgal.oraxen.utils.VersionUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.NoteBlockMechanicFactory.MINEABLE_PACKET_LISTENER;

public class NMSHandler implements io.th0rgal.oraxen.nms.NMSHandler {

    private final GlyphHandler glyphHandler;
    private final FurniturePacketManager furniturePacketManager = new FurniturePacketManager();

    public NMSHandler() {
        this.glyphHandler = new io.th0rgal.oraxen.nms.v1_20_R4.GlyphHandler();

        // mineableWith tag handling
        ChannelInitializeListenerHolder.addListener(MINEABLE_PACKET_LISTENER, (channel ->
                channel.pipeline().addBefore("packet_handler", MINEABLE_PACKET_LISTENER.asString(), new ChannelDuplexHandler() {
                    TagNetworkSerialization.NetworkPayload payload = createPayload();

                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                        if (msg instanceof ClientboundUpdateTagsPacket updateTagsPacket) {
                            Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> tags = updateTagsPacket.getTags();
                            if (NoteBlockMechanicFactory.isEnabled() && NoteBlockMechanicFactory.get().removeMineableTag())
                                tags.put(Registries.BLOCK, payload);
                            msg = new ClientboundUpdateTagsPacket(tags);
                        }
                        ctx.write(msg, promise);
                    }
                })));
    }

    @Override
    public GlyphHandler glyphHandler() {
        return glyphHandler;
    }

    @Override
    public IFurniturePacketManager furniturePacketManager() {
        return furniturePacketManager;
    }

    private static Field configurationTasks;
    static {
        try {
            configurationTasks = ServerConfigurationPacketListenerImpl.class.getDeclaredField("configurationTasks");
            configurationTasks.setAccessible(true);
        } catch (Exception e) {

        }
    }

    @Override
    public boolean tripwireUpdatesDisabled() {
        return VersionUtil.isPaperServer() && GlobalConfiguration.get().blockUpdates.disableTripwireUpdates;
    }

    @Override
    public boolean noteblockUpdatesDisabled() {
        return VersionUtil.isPaperServer() && GlobalConfiguration.get().blockUpdates.disableNoteblockUpdates;
    }


    @Override //TODO Fix this
    public ItemStack copyItemNBTTags(@NotNull ItemStack oldItem, @NotNull ItemStack newItem) {
        net.minecraft.world.item.ItemStack newNmsItem = CraftItemStack.asNMSCopy(newItem);
        net.minecraft.world.item.ItemStack oldItemStack = CraftItemStack.asNMSCopy(oldItem);
        CraftItemStack.asNMSCopy(oldItem).getTags().forEach(tag -> {
            if (!tag.location().getNamespace().equals("minecraft")) return;
            if (vanillaKeys.contains(tag.location().getPath())) return;

            DataComponentType<Object> type = (DataComponentType<Object>) BuiltInRegistries.DATA_COMPONENT_TYPE.get(tag.location());
            if (type != null) newNmsItem.set(type, oldItemStack.get(type));
        });
        return CraftItemStack.asBukkitCopy(newNmsItem);
    }

    @Override
    @Nullable
    public InteractionResult correctBlockStates(Player player, EquipmentSlot slot, ItemStack itemStack) {
        InteractionHand hand = slot == EquipmentSlot.HAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        BlockHitResult hitResult = getPlayerPOVHitResult(serverPlayer.level(), serverPlayer, ClipContext.Fluid.NONE);
        BlockPlaceContext placeContext = new BlockPlaceContext(serverPlayer.level(), serverPlayer, hand, nmsStack, hitResult);

        if (!(nmsStack.getItem() instanceof BlockItem blockItem)) {
            InteractionResult result = InteractionResult.fromNms(nmsStack.getItem().useOn(new UseOnContext(serverPlayer, hand, hitResult)));
            return player.isSneaking() && player.getGameMode() != GameMode.CREATIVE ? result
                    : InteractionResult.fromNms(serverPlayer.gameMode.useItem(serverPlayer, serverPlayer.level(), nmsStack, hand));
        }

        InteractionResult result = InteractionResult.fromNms(blockItem.place(placeContext));
        if (result == InteractionResult.FAIL) return null;

        if (!player.isSneaking()) {
            World world = player.getWorld();
            BlockPos clickPos = placeContext.getClickedPos();
            Block block = world.getBlockAt(clickPos.getX(), clickPos.getY(), clickPos.getZ());
            SoundGroup sound = block.getBlockData().getSoundGroup();

            world.playSound(
                    BlockHelpers.toCenterBlockLocation(block.getLocation()), sound.getPlaceSound(),
                    SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F
            );
        }

        return result;
    }

    public BlockHitResult getPlayerPOVHitResult(Level world, net.minecraft.world.entity.player.Player player, ClipContext.Fluid fluidHandling) {
        float f = player.getXRot();
        float g = player.getYRot();
        Vec3 vec3 = player.getEyePosition();
        float h = Mth.cos(-g * ((float) Math.PI / 180F) - (float) Math.PI);
        float i = Mth.sin(-g * ((float) Math.PI / 180F) - (float) Math.PI);
        float j = -Mth.cos(-f * ((float) Math.PI / 180F));
        float k = Mth.sin(-f * ((float) Math.PI / 180F));
        float l = i * j;
        float n = h * j;
        double d = 5.0D;
        Vec3 vec32 = vec3.add((double) l * d, (double) k * d, (double) n * d);
        return world.clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, fluidHandling, player));
    }

    @Override
    public int playerProtocolVersion(Player player) {
        return ((CraftPlayer) player).getHandle().connection.connection.protocolVersion;
    }

    private TagNetworkSerialization.NetworkPayload createPayload() {
        Constructor<?> constructor = Arrays.stream(TagNetworkSerialization.NetworkPayload.class.getDeclaredConstructors()).findFirst().orElse(null);
        if (constructor == null) return null;
        constructor.setAccessible(true);
        try {
            return (TagNetworkSerialization.NetworkPayload) constructor.newInstance(tagRegistryMap);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private final Map<ResourceLocation, IntList> tagRegistryMap = createTagRegistryMap();

    private static Map<ResourceLocation, IntList> createTagRegistryMap() {
        return BuiltInRegistries.BLOCK.getTags().map(pair -> {
            IntArrayList list = new IntArrayList(pair.getSecond().size());
            if (pair.getFirst().location() == BlockTags.MINEABLE_WITH_AXE.location()) {
                pair.getSecond().stream()
                        .filter(block -> !block.value().getDescriptionId().endsWith("note_block"))
                        .forEach(block -> list.add(BuiltInRegistries.BLOCK.getId(block.value())));
            } else pair.getSecond().forEach(block -> list.add(BuiltInRegistries.BLOCK.getId(block.value())));

            return Map.of(pair.getFirst().location(), list);
        }).collect(HashMap::new, Map::putAll, Map::putAll);
    }

    @Override
    public boolean getSupported() {
        return true;
    }

    @NotNull
    @Override
    public @Unmodifiable Set<Material> itemTools() {
        return Arrays.stream(Material.values()).filter(Material::isItem).map(ItemStack::new).filter(ItemStack::hasItemMeta)
                .filter(i -> i.getItemMeta().hasTool()).map(ItemStack::getType).collect(Collectors.toSet());
    }

    @Override
    public void applyMiningEffect(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        Collection<AttributeInstance> attributes = serverPlayer.getAttributes().getSyncableAttributes();
        attributes.removeIf(a -> a.getAttribute().is(Attributes.BLOCK_BREAK_SPEED.unwrapKey().get()));
        attributes.add(new AttributeInstance(Attributes.BLOCK_BREAK_SPEED, (instance) -> {
            instance.setBaseValue(0.0);
        }));
        serverPlayer.connection.send(new ClientboundUpdateAttributesPacket(player.getEntityId(), attributes));
    }

    @Override
    public void removeMiningEffect(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        Collection<AttributeInstance> attributes = serverPlayer.getAttributes().getSyncableAttributes();
        serverPlayer.connection.send(new ClientboundUpdateAttributesPacket(player.getEntityId(), attributes));
    }

    @Override
    public String getNoteBlockInstrument(Block block) {
        return ((CraftBlock) block).getNMS().instrument().toString();
    }
}