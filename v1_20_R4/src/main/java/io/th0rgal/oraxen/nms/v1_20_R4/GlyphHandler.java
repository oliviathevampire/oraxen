package io.th0rgal.oraxen.nms.v1_20_R4;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import io.th0rgal.oraxen.config.Settings;
import io.th0rgal.oraxen.nms.GlyphHandlers;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.protocol.game.*;

import java.util.Optional;

public class GlyphHandler implements io.th0rgal.oraxen.nms.GlyphHandler {

    public GlyphHandler() {
        if (ChannelInitializeListenerHolder.hasListener(GlyphHandlers.GLYPH_HANDLER_KEY)) return;

        ChannelInitializeListenerHolder.addListener(GlyphHandlers.GLYPH_HANDLER_KEY, (channel ->
                channel.pipeline().addBefore("packet_handler", GlyphHandlers.GLYPH_HANDLER_KEY.asString(), new ChannelDuplexHandler() {
                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                        if (msg instanceof ClientboundSetTitleTextPacket packet && Settings.FORMAT_TITLES.toBool()) {
                            msg = new ClientboundSetTitleTextPacket(PaperAdventure.asVanilla(GlyphHandlers.transformGlyphs(PaperAdventure.asAdventure(packet.text()))));
                        } else if (msg instanceof ClientboundSetSubtitleTextPacket packet && Settings.FORMAT_SUBTITLES.toBool()) {
                            msg = new ClientboundSetSubtitleTextPacket(PaperAdventure.asVanilla(GlyphHandlers.transformGlyphs(PaperAdventure.asAdventure(packet.text()))));
                        } else if (msg instanceof ClientboundSetActionBarTextPacket packet && Settings.FORMAT_ACTION_BAR.toBool()) {
                            msg = new ClientboundSetActionBarTextPacket(PaperAdventure.asVanilla(GlyphHandlers.transformGlyphs(PaperAdventure.asAdventure(packet.text()))));
                        } else if (msg instanceof ClientboundOpenScreenPacket packet && Settings.FORMAT_INVENTORY_TITLES.toBool()) {
                            msg = new ClientboundOpenScreenPacket(packet.getContainerId(), packet.getType(), PaperAdventure.asVanilla(GlyphHandlers.transformGlyphs(PaperAdventure.asAdventure(packet.getTitle()))));
                        } else if (msg instanceof ClientboundSetScorePacket packet) {
                            msg = new ClientboundSetScorePacket(packet.owner(), packet.objectiveName(), packet.score(), packet.display(), Settings.HIDE_SCOREBOARD_NUMBERS.toBool() ? Optional.of(BlankFormat.INSTANCE) : packet.numberFormat());
                        }

                        ctx.write(msg, promise);
                    }
                })));

    }
}