package io.th0rgal.oraxen.utils.inventories;

import dev.triumphteam.gui.components.ScrollType;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.config.Settings;
import io.th0rgal.oraxen.items.ItemBuilder;
import io.th0rgal.oraxen.items.ItemParser;
import io.th0rgal.oraxen.items.ItemUpdater;
import io.th0rgal.oraxen.utils.AdventureUtils;
import io.th0rgal.oraxen.utils.Utils;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class ItemsView {

    private final YamlConfiguration settings = OraxenPlugin.get().getResourceManager().settings();
    BaseGui mainGui;

    public BaseGui create() {
        final SortedMap<File, PaginatedGui> files = new TreeMap<>(Comparator.comparing(File::getName));
        for (final File file : OraxenItems.getMap().keySet()) {
            final List<ItemBuilder> unexcludedItems = OraxenItems.getUnexcludedItems(file);
            if (!unexcludedItems.isEmpty())
                files.put(file, createSubGUI(file.getName(), unexcludedItems));
        }
        int rows = (int) Settings.ORAXEN_INV_ROWS.getValue();
        String invType = Settings.ORAXEN_INV_TYPE.toString();
        mainGui = (Objects.equals(invType, "PAGINATED")
                ? Gui.paginated().pageSize((rows - 1) * 9).title(Settings.ORAXEN_INV_TITLE_PAGINATED.toComponent())
                : (Objects.equals(invType, "SCROLL_HORIZONTAL")
                ? Gui.scrolling(ScrollType.HORIZONTAL).pageSize((rows - 1) * 9).title(Settings.ORAXEN_INV_TITLE_SCROLL.toComponent())
                : (Objects.equals(invType, "SCROLL_VERTICAL")
                ? Gui.scrolling(ScrollType.VERTICAL).pageSize((rows - 1) * 9).title(Settings.ORAXEN_INV_TITLE_SCROLL.toComponent())
                : Gui.gui()))).rows(rows).create();

        // Make a list of all slots to allow using mainGui.addItem easier
        List<GuiItem> pageItems = new ArrayList<>(Collections.nCopies(files.size(), null));
        // Make a list of all used slots to avoid using them later
        for (Map.Entry<File, PaginatedGui> entry : files.entrySet()) {
            int slot = getItemStack(entry.getKey()).getRight();
            if (slot == -1) continue;
            GuiItem guiItem = new GuiItem(getItemStack(entry.getKey()).getLeft(), e -> entry.getValue().open(e.getWhoClicked()));
            pageItems.add(slot, guiItem);
        }

        // Add all items without a specified slot to the earliest available slot
        for (Map.Entry<File, PaginatedGui> entry : files.entrySet()) {
            if (getItemStack(entry.getKey()).getRight() != -1) continue;
            pageItems.add(new GuiItem(getItemStack(entry.getKey()).getLeft(), e -> entry.getValue().open(e.getWhoClicked())));
        }

        mainGui.addItem(pageItems.stream().filter(Objects::nonNull).toArray(GuiItem[]::new));

        ItemStack nextPage = (Settings.ORAXEN_INV_NEXT_PAGE_ICON.getValue() == null
                ? new ItemBuilder(Material.ARROW).setDisplayName(ItemParser.parseComponentDisplayName(Settings.ORAXEN_INV_NEXT_PAGE_NAME.toString()))
                : OraxenItems.getItemById(Settings.ORAXEN_INV_NEXT_PAGE_ICON.toString())).build();
        ItemStack previousPage = (Settings.ORAXEN_INV_PREVIOUS_PAGE_ICON.getValue() == null
                ? new ItemBuilder(Material.ARROW).setDisplayName(ItemParser.parseComponentDisplayName(Settings.ORAXEN_INV_PREVIOUS_PAGE_NAME.toString()))
                : OraxenItems.getItemById(Settings.ORAXEN_INV_PREVIOUS_PAGE_ICON.toString())).build();
        ItemStack exitIcon = (Settings.ORAXEN_INV_EXIT_ICON.getValue() == null
                ? new ItemBuilder(Material.BARRIER).setDisplayName(ItemParser.parseComponentDisplayName(Settings.ORAXEN_INV_EXIT_NAME.toString()))
                : OraxenItems.getItemById(Settings.ORAXEN_INV_EXIT_ICON.toString())).build();

        if (mainGui instanceof PaginatedGui paginated) {
            if (paginated.getPagesNum() > 1) {
                paginated.setItem(6, Settings.ORAXEN_INV_PREVIOUS_PAGE_COLUMN.toInt(), new GuiItem(previousPage, event -> {
                    paginated.previous();
                    event.setCancelled(true);
                }));
                paginated.setItem(6, Settings.ORAXEN_INV_NEXT_PAGE_COLUMN.toInt(), new GuiItem(nextPage, event -> {
                    paginated.next();
                    event.setCancelled(true);
                }));
            }

            paginated.setItem(6, Settings.ORAXEN_INV_EXIT_COLUMN.toInt(), new GuiItem(exitIcon, event -> mainGui.open(event.getWhoClicked())));
        }

        return mainGui;
    }

    private PaginatedGui createSubGUI(final String fileName, final List<ItemBuilder> items) {
        final PaginatedGui gui = Gui.paginated().rows(6).pageSize(45).title(AdventureUtils.MINI_MESSAGE.deserialize(settings.getString(
                        String.format("oraxen_inventory.menu_layout.%s.title", Utils.removeExtension(fileName)), Settings.ORAXEN_INV_TITLE_PAGINATED.toString())
                .replace("<main_menu_title>", Settings.ORAXEN_INV_TITLE_PAGINATED.toString()))).create();

        for (ItemBuilder builder : items) {
            if (builder == null) continue;
            ItemStack itemStack = builder.build();
            if (itemStack == null || itemStack.getType().isAir()) continue;

            GuiItem guiItem = new GuiItem(itemStack);
            guiItem.setAction(e -> e.getWhoClicked().getInventory().addItem(ItemUpdater.updateItem(guiItem.getItemStack())));
            gui.addItem(guiItem);
        }

        ItemStack nextPage = (Settings.ORAXEN_INV_NEXT_PAGE_ICON.getValue() == null
                ? new ItemBuilder(Material.ARROW).setDisplayName(ItemParser.parseComponentDisplayName(Settings.ORAXEN_INV_NEXT_PAGE_NAME.toString()))
                : OraxenItems.getItemById(Settings.ORAXEN_INV_NEXT_PAGE_ICON.toString())).build();
        ItemStack previousPage = (Settings.ORAXEN_INV_PREVIOUS_PAGE_ICON.getValue() == null
                ? new ItemBuilder(Material.ARROW).setDisplayName(ItemParser.parseComponentDisplayName(Settings.ORAXEN_INV_PREVIOUS_PAGE_NAME.toString()))
                : OraxenItems.getItemById(Settings.ORAXEN_INV_PREVIOUS_PAGE_ICON.toString())).build();
        ItemStack exitIcon = (Settings.ORAXEN_INV_EXIT_ICON.getValue() == null
                ? new ItemBuilder(Material.BARRIER).setDisplayName(ItemParser.parseComponentDisplayName(Settings.ORAXEN_INV_EXIT_NAME.toString()))
                : OraxenItems.getItemById(Settings.ORAXEN_INV_EXIT_ICON.toString())).build();

        if (gui.getPagesNum() > 1) {
            gui.setItem(6, 2, new GuiItem(previousPage, event -> gui.previous()));
            gui.setItem(6, 8, new GuiItem(nextPage, event -> gui.next()));
        }

        gui.setItem(6, 5, new GuiItem(exitIcon, event -> mainGui.open(event.getWhoClicked())));

        return gui;
    }

    private Pair<ItemStack, Integer> getItemStack(final File file) {
        ItemStack itemStack;
        String fileName = Utils.removeExtension(file.getName());
        String material = settings.getString(String.format("oraxen_inventory.menu_layout.%s.icon", fileName), "PAPER");
        String displayName = ItemParser.parseComponentDisplayName(settings.getString(String.format("oraxen_inventory.menu_layout.%s.displayname", fileName), "<green>" + file.getName()));
        try {
            itemStack = new ItemBuilder(OraxenItems.getItemById(material).getReferenceClone())
                    .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                    .setDisplayName(displayName)
                    .setLore(new ArrayList<>())
                    .build();
        } catch (final Exception e) {
            try {
                itemStack = new ItemBuilder(Material.getMaterial(material.toUpperCase()))
                        .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                        .setDisplayName(displayName)
                        .build();
            } catch (final Exception ignored) {
                itemStack = new ItemBuilder(Material.PAPER)
                        .setDisplayName(displayName)
                        .build();
            }
            e.printStackTrace();
        }

        // avoid possible bug if isOraxenItems is available but can't be an itemstack
        if (itemStack == null) itemStack = new ItemBuilder(Material.PAPER).setDisplayName(displayName).build();
        int slot = settings.getInt(String.format("oraxen_inventory.menu_layout.%s.slot", Utils.removeExtension(file.getName())), -1) - 1;
        return Pair.of(itemStack, Math.max(slot, -1));
    }
}
