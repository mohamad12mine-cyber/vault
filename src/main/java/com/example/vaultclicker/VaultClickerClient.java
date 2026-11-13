package com.example.vaultclicker;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class VaultClickerClient implements ClientModInitializer {
    public static boolean enabled = true;
    public static final Set<Item> targets = new HashSet<>();

    private static KeyMapping openKey;
    private static int cooldown = 0;

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("vaultclicker.properties");
    }

    public static void load() {
        targets.clear();
        Path p = configPath();
        if (!Files.exists(p)) {
            targets.add(Items.HEAVY_CORE);
            targets.add(Items.ENCHANTED_GOLDEN_APPLE);
            return;
        }
        try (InputStream in = Files.newInputStream(p)) {
            Properties props = new Properties();
            props.load(in);
            enabled = Boolean.parseBoolean(props.getProperty("enabled", "true"));
            for (String s : props.getProperty("items", "").split(",")) {
                s = s.trim();
                if (s.isEmpty()) continue;
                Identifier id = Identifier.tryParse(s);
                if (id != null && BuiltInRegistries.ITEM.containsKey(id)) {
                    targets.add(BuiltInRegistries.ITEM.getValue(id));
                }
            }
        } catch (IOException ignored) {
        }
    }

    public static void save() {
        Properties props = new Properties();
        props.setProperty("enabled", String.valueOf(enabled));
        StringBuilder sb = new StringBuilder();
        for (Item item : targets) {
            if (sb.length() > 0) sb.append(",");
            sb.append(BuiltInRegistries.ITEM.getKey(item).toString());
        }
        props.setProperty("items", sb.toString());
        try (OutputStream out = Files.newOutputStream(configPath())) {
            props.store(out, "Vault Clicker");
        } catch (IOException ignored) {
        }
    }

    @Override
    public void onInitializeClient() {
        load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("vaultclicker", "main"));

        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vaultclicker.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) {
                client.gui.setScreen(new ConfigScreen());
            }

            if (cooldown > 0) cooldown--;
            if (!enabled || cooldown > 0) return;
            if (client.player == null || client.level == null || client.gameMode == null) return;
            if (client.gui.screen() != null) return;

            ItemStack held = client.player.getMainHandItem();
            if (!held.is(Items.OMINOUS_TRIAL_KEY) && !held.is(Items.TRIAL_KEY)) return;

            if (!(client.hitResult instanceof BlockHitResult hit)) return;
            BlockEntity be = client.level.getBlockEntity(hit.getBlockPos());
            if (!(be instanceof

VaultBlockEntity vault)) return;

            ItemStack shown = vault.getSharedData().getDisplayItem();
            if (shown.isEmpty() || !targets.contains(shown.getItem())) return;

            client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hit);
            client.player.swing(InteractionHand.MAIN_HAND);
            cooldown = 20;
        });
    }
}
