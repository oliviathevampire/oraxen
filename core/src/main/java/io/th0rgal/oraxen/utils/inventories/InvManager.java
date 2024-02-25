package io.th0rgal.oraxen.utils.inventories;

import dev.triumphteam.gui.guis.BaseGui;
import io.th0rgal.oraxen.recipes.CustomRecipe;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InvManager {

    private final Map<UUID, BaseGui> itemsViews = new HashMap<>();

    public InvManager() {
        regen();
    }

    public void regen() {
        itemsViews.clear();
    }

    public BaseGui getItemsView(Player player) {
        return itemsViews.computeIfAbsent(player.getUniqueId(), uuid -> new ItemsView().create());
    }

    public PaginatedGui getRecipesShowcase(Player player, final int page, final List<CustomRecipe> filteredRecipes) {
        return new RecipesView().create(page, filteredRecipes);
    }
}
