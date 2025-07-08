package org.jmod.elytraswap.client;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.jmod.elytraswap.swapper.SwapManager;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

public class ElytraswapClient implements ClientModInitializer {

    public final static Logger LOGGER = LogUtils.getLogger();


    public static final KeyBinding APPLY_CHESTPLATE = new KeyBinding(
            "key.elytraswap.set_chesplate",                  // Translation key (for localization)
            InputUtil.Type.KEYSYM,                      // Input type (keyboard key)
            GLFW.GLFW_KEY_V,                            // Default key (V in this example)
            "category.elytraswap.controls"                 // Category in Controls screen
    );

    public static final KeyBinding APPLY_ELYTRA = new KeyBinding(
            "key.elytraswap.set_elytra",                  // Translation key (for localization)
            InputUtil.Type.KEYSYM,                      // Input type (keyboard key)
            GLFW.GLFW_KEY_C,                            // Default key (V in this example)
            "category.elytraswap.controls"                 // Category in Controls screen
    );

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(APPLY_ELYTRA);
        KeyBindingHelper.registerKeyBinding(APPLY_CHESTPLATE);
        ClientTickEvents.END_CLIENT_TICK.register((minecraftClient) -> {
            if (SwapManager.shouldApplyChestplate()) {
                SwapManager.applyChestplate(SwapManager.clientPlayer.getMainHandStack());
                minecraftClient.player.sendMessage(Text.translatable("elytraswap.equip.chestplate.success"), false);
            }
            if (SwapManager.shouldApplyElytra()) {
                SwapManager.applyElytra(SwapManager.clientPlayer.getMainHandStack());
                minecraftClient.player.sendMessage(Text.translatable("elytraswap.equip.elytra.success"), false);
            }
            SwapManager.tick();
        });
    }


}
