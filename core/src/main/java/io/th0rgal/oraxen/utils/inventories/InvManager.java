package io.th0rgal.oraxen.utils.inventories;

import dev.triumphteam.gui.guis.PaginatedGui;
import io.th0rgal.oraxen.recipes.CustomRecipe;

import java.util.List;

public class InvManager {

    private RecipesView recipesView;

    public InvManager() {
        regen();
    }

    public void regen() {
        recipesView = new RecipesView();
    }

    public PaginatedGui getRecipesShowcase(final int page, final List<CustomRecipe> filteredRecipes) {
        return recipesView.create(page, filteredRecipes);
    }
}
