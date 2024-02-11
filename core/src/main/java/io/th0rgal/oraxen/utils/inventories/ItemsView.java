package io.th0rgal.oraxen.utils.inventories;

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
import org.bukkit.entity.Player;
import io.th0rgal.oraxen.utils.Utils;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class ItemsView {

    private final YamlConfiguration settings = OraxenPlugin.get().getResourceManager().settings();
    PaginatedGui mainGui;

    public PaginatedGui create() {
        final SortedMap<File, PaginatedGui> files = new TreeMap<>(Comparator.comparing(File::getName));
        for (final File file : OraxenItems.getMap().keySet()) {
            final List<ItemBuilder> unexcludedItems = OraxenItems.getUnexcludedItems(file);
            if (!unexcludedItems.isEmpty())
                files.put(file, createSubGUI(file.getName(), unexcludedItems));
        }
        int rows = (int) Settings.ORAXEN_INV_ROWS.getValue();
        mainGui = Gui.paginated().pageSize(9 * (Settings.ORAXEN_INV_ROWS.toInt() - 1)).rows(rows).title(Settings.ORAXEN_INV_TITLE.toComponent()).create();
        mainGui.disableAllInteractions();

        // Make a list of all slots to allow using mainGui.addItem easier
        List<GuiItem> pageItems = new ArrayList<>(Collections.nCopies(files.size(), null));
        // Make a list of all used slots to avoid using them later
        for (Map.Entry<File, PaginatedGui> entry : files.entrySet()) {
            int slot = getItemStack(entry.getKey()).getRight();
            if (slot == -1) continue;
            GuiItem guiItem = new GuiItem(getItemStack(entry.getKey()).getLeft(), e -> {
                entry.getValue().open(e.getWhoClicked());
                if(Settings.ORAXEN_INV_PLAY_BUTTON_SOUND.toBool())
                    ((Player)e.getWhoClicked()).playSound(e.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            });
            pageItems.add(slot, guiItem);
        }

        // Add all items without a specified slot to the earliest available slot
        for (Map.Entry<File, PaginatedGui> entry : files.entrySet()) {
            if (getItemStack(entry.getKey()).getRight() != -1) continue;
            pageItems.add(new GuiItem(getItemStack(entry.getKey()).getLeft(), e -> {
                entry.getValue().open(e.getWhoClicked());
                if(Settings.ORAXEN_INV_PLAY_BUTTON_SOUND.toBool())
                    ((Player)e.getWhoClicked()).playSound(e.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }));
        }

        mainGui.addItem(pageItems.stream().filter(Objects::nonNull).toArray(GuiItem[]::new));

        //page selection
        if (mainGui.getPagesNum() > 1) {
            mainGui.setItem(6, 2, new GuiItem((OraxenItems.exists("arrow_previous_icon")
                    ? OraxenItems.getItemById("arrow_previous_icon")
                    : new ItemBuilder(Material.ARROW).setDisplayName(AdventureUtils.parseMiniMessage("<gray>Previous page"))
            ).build(), event -> {
                mainGui.previous();
                if(Settings.ORAXEN_INV_PLAY_BUTTON_SOUND.toBool())
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }));

            mainGui.setItem(6, 8, new GuiItem((OraxenItems.exists("arrow_next_icon")
                    ? OraxenItems.getItemById("arrow_next_icon")
                    : new ItemBuilder(Material.ARROW).setDisplayName(AdventureUtils.parseMiniMessage("<gray>Next page"))
            ).build(), event -> {
                mainGui.next();
                if(Settings.ORAXEN_INV_PLAY_BUTTON_SOUND.toBool())
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }));
//            mainGui.setItem(Settings.ORAXEN_INV_PREVIOUS_PAGE_ROW.toInt(), Settings.ORAXEN_INV_PREVIOUS_PAGE_COLUMN.toInt(),
//                    new GuiItem(
//                            (OraxenItems.exists(Settings.ORAXEN_INV_PREVIOUS_PAGE_ICON.toString())
//                                    ? OraxenItems.getItemById(Settings.ORAXEN_INV_PREVIOUS_PAGE_ICON.toString())
//                                    : new ItemBuilder(Material.ARROW).setDisplayName(AdventureUtils.parseMiniMessage(
//                                        Settings.ORAXEN_INV_PREVIOUS_PAGE_NAME.toString()
//                                    )
//                            )).build(),
//                            event -> {
//                                mainGui.previous();
//                                if(Settings.ORAXEN_INV_PLAY_BUTTON_SOUND.toBool())
//                                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1f, 1f);
//                            }
//                    )
//            );
//
//            mainGui.setItem(Settings.ORAXEN_INV_NEXT_PAGE_ROW.toInt(), Settings.ORAXEN_INV_NEXT_PAGE_COLUMN.toInt(),
//                    new GuiItem(
//                            (OraxenItems.exists(Settings.ORAXEN_INV_NEXT_PAGE_ICON.toString())
//                                    ? OraxenItems.getItemById(Settings.ORAXEN_INV_NEXT_PAGE_ICON.toString())
//                                    : new ItemBuilder(Material.ARROW).setDisplayName(AdventureUtils.parseMiniMessage(
//                                        Settings.ORAXEN_INV_NEXT_PAGE_NAME.toString()
//                                    )
//                            )).build(),
//                            event -> {
//                                mainGui.next();
//                                if(Settings.ORAXEN_INV_PLAY_BUTTON_SOUND.toBool())
//                                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1f, 1f);
//                            }
//                    )
//            );
        }

        mainGui.setItem(6, 5, new GuiItem((OraxenItems.exists("exit_icon")
                ? OraxenItems.getItemById("exit_icon").setDisplayName(AdventureUtils.parseMiniMessage("<red>Exit"))
                : new ItemBuilder(Material.BARRIER).setDisplayName(AdventureUtils.parseMiniMessage("<red>Exit"))
        ).build(), event -> {
            event.getWhoClicked().closeInventory();
            ((Player)event.getWhoClicked()).playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }));
        /*mainGui.setItem(Settings.ORAXEN_INV_EXIT_ROW.toInt(), Settings.ORAXEN_INV_EXIT_COLUMN.toInt(),
                new GuiItem(
                        (OraxenItems.exists(Settings.ORAXEN_INV_EXIT_ICON.toString())
                                ? OraxenItems.getItemById(Settings.ORAXEN_INV_EXIT_ICON.toString())
                                : new ItemBuilder(Material.BARRIER).setDisplayName(AdventureUtils.parseMiniMessage(
                                        Settings.ORAXEN_INV_EXIT_NAME.toString()
                                )
                        )).build(),
                        event -> {
                            event.getWhoClicked().closeInventory();
                            if(Settings.ORAXEN_INV_PLAY_BUTTON_SOUND.toBool())
                                ((Player)event.getWhoClicked()).playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                        }
                )
        );*/

        return mainGui;
    }

    private PaginatedGui createSubGUI(final String fileName, final List<ItemBuilder> items) {
        final PaginatedGui gui = Gui.paginated().rows(6).pageSize(45).title(AdventureUtils.MINI_MESSAGE.deserialize(settings.getString(
                String.format("oraxen_inventory.menu_layout.%s.title", Utils.removeExtension(fileName)),
                Settings.ORAXEN_INV_TITLE.toString()
        ).replace("<main_menu_title>", Settings.ORAXEN_INV_TITLE.toString()))).create();
        gui.disableAllInteractions();

        for (ItemBuilder builder : items) {
            if (builder == null) continue;
            ItemStack itemStack = builder.build();
            if (itemStack == null || itemStack.getType().isAir()) continue;

            GuiItem guiItem = new GuiItem(itemStack);
            guiItem.setAction(e -> {
                e.getWhoClicked().getInventory().addItem(ItemUpdater.updateItem(guiItem.getItemStack()));
                if(Settings.ORAXEN_INV_PLAY_BUTTON_SOUND.toBool())
                    ((Player)e.getWhoClicked()).playSound(e.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            });
            gui.addItem(guiItem);
        }

        //page selection
        if (gui.getPagesNum() > 1) {
            gui.setItem(6, 2, new GuiItem((OraxenItems.exists("arrow_previous_icon")
                    ? OraxenItems.getItemById("arrow_previous_icon")
                    : new ItemBuilder(Material.ARROW).setDisplayName(AdventureUtils.parseMiniMessage("<gray>Previous page"))
            ).build(), event -> {
                gui.previous();
                if(Settings.ORAXEN_INV_PLAY_BUTTON_SOUND.toBool())
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }));

            gui.setItem(6, 8, new GuiItem((OraxenItems.exists("arrow_next_icon")
                    ? OraxenItems.getItemById("arrow_next_icon")
                    : new ItemBuilder(Material.ARROW).setDisplayName(AdventureUtils.parseMiniMessage("<gray>Next page"))
            ).build(), event -> {
                gui.next();
                if(Settings.ORAXEN_INV_PLAY_BUTTON_SOUND.toBool())
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            }));
        }

        gui.setItem(6, 5, new GuiItem((OraxenItems.exists("exit_icon")
                ? OraxenItems.getItemById("exit_icon").setDisplayName(AdventureUtils.parseMiniMessage("<red>Back to main menu"))
                : new ItemBuilder(Material.BARRIER).setDisplayName(AdventureUtils.parseMiniMessage("<red>Back to main menu"))
        ).build(), event -> {
            mainGui.open(event.getWhoClicked());
            if(Settings.ORAXEN_INV_PLAY_BUTTON_SOUND.toBool())
                ((Player)event.getWhoClicked()).playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }));

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

        if (itemStack == null)
            // avoid possible bug if isOraxenItems is available but can't be an itemstack
            itemStack = new ItemBuilder(Material.PAPER).setDisplayName(displayName).build();

        return Pair.of(itemStack, Math.max(settings.getInt(String.format("oraxen_inventory.menu_layout.%s.slot", Utils.removeExtension(file.getName())), -1) - 1, -1));
    }
}
