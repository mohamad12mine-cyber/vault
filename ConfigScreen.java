package com.example.vaultclicker;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigScreen extends Screen {
    private static final Map<String, Item> OPTIONS = new LinkedHashMap<>();

    static {
        OPTIONS.put("Heavy Core", Items.HEAVY_CORE);
        OPTIONS.put("Enchanted Golden Apple", Items.ENCHANTED_GOLDEN_APPLE);
        OPTIONS.put("Golden Apple", Items.GOLDEN_APPLE);
        OPTIONS.put("Diamond", Items.DIAMOND);
        OPTIONS.put("Emerald", Items.EMERALD);
        OPTIONS.put("Enchanted Book", Items.ENCHANTED_BOOK);
        OPTIONS.put("Diamond Axe", Items.DIAMOND_AXE);
        OPTIONS.put("Diamond Chestplate", Items.DIAMOND_CHESTPLATE);
        OPTIONS.put("Wind Charge", Items.WIND_CHARGE);
        OPTIONS.put("Ominous Bottle", Items.OMINOUS_BOTTLE);
    }

    public ConfigScreen() {
        super(Component.literal("Vault Clicker"));
    }

    private static Component label(String name, boolean on) {
        return Component.literal((on ? "[ON] " : "[OFF] ") + name);
    }

    @Override
    protected void init() {
        int w = 150;
        int h = 20;
        int gap = 4;
        int left = this.width / 2 - w - gap / 2;
        int right = this.width / 2 + gap / 2;
        int y = 40;

        addRenderableWidget(Button.builder(
                label("Vault Clicker", VaultClickerClient.enabled),
                b -> {
                    VaultClickerClient.enabled = !VaultClickerClient.enabled;
                    b.setMessage(label("Vault Clicker", VaultClickerClient.enabled));
                    VaultClickerClient.save();
                }).bounds(left, y, w * 2 + gap, h).build());
        y += h + 10;

        int i = 0;
        for (Map.Entry<String, Item> e : OPTIONS.entrySet()) {
            final String name = e.getKey();
            final Item item = e.getValue();
            int x = (i % 2 == 0) ? left : right;
            addRenderableWidget(Button.builder(
                    label(name, VaultClickerClient.targets.contains(item)),
                    b -> {
                        if (!VaultClickerClient.targets.remove(item)) {
                            VaultClickerClient.targets.add(item);
                        }
                        b.setMessage(label(name, VaultClickerClient.targets.contains(item)));
                        VaultClickerClient.save();
                    }).bounds(x, y, w, h).build());
            if (i % 2 == 1) y += h + gap;
            i++;
        }
        if (i % 2 == 1) y += h + gap;

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
                .bounds(this.width / 2 - 50, y + 10, 100, h).build());
    }
}
