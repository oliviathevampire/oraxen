package io.th0rgal.oraxen.font;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.internal.serializer.Emitable;
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import javax.annotation.Nullable;

public class LangTag {
    private static final String LANG = "lang";

    public static final TagResolver RESOLVER = SerializableResolver.claimingComponent(LANG, LangTag::create, LangTag::emit);
    public static final TagResolver RESOLVER_SHORT = SerializableResolver.claimingComponent("l", LangTag::create, LangTag::emit);

    static Tag create(final ArgumentQueue args, final Context ctx) throws ParsingException {
        Component langComponent = Component.translatable(args.popOr("A lang value is required").value()).font(Key.key("default"));
        if (!args.hasNext() || !args.peek().value().equals("colorable"))
            langComponent = langComponent.color(NamedTextColor.WHITE);
        return Tag.inserting(langComponent);
    }

    static @Nullable Emitable emit(final Component component) {
        return null;
    }
}
