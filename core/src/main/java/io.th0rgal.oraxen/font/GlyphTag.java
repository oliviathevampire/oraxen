package io.th0rgal.oraxen.font;

import io.th0rgal.oraxen.OraxenPlugin;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.internal.serializer.Emitable;
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class GlyphTag {

    private static final String GLYPH = "glyph";

    public static final TagResolver RESOLVER = SerializableResolver.claimingComponent(GLYPH, (ArgumentQueue args, Context ctx) -> create(args, ctx, null), GlyphTag::emit);

    public static TagResolver getResolverForPlayer(Player player) {
        return SerializableResolver.claimingComponent(GLYPH, (ArgumentQueue args, Context ctx) -> create(args, ctx, player), GlyphTag::emit);
    }

    static Tag create(final ArgumentQueue args, final Context ctx, Player player) throws ParsingException {
        Glyph glyph = OraxenPlugin.get().getFontManager().getGlyphFromName(args.popOr("A glyph value is required").value());
        Component glyphComponent = Component.text(glyph.getCharacter()).font(Key.key("default")).style(Style.empty());
        if (!args.hasNext() || !args.peek().value().equals("colorable"))
            glyphComponent = glyphComponent.color(NamedTextColor.WHITE);
        return Tag.inserting(glyphComponent);
    }

    static @Nullable Emitable emit(final Component component) {
        return null;
    }
}
