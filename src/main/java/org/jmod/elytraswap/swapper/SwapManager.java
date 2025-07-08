package org.jmod.elytraswap.swapper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.SlotActionType;
import org.jmod.elytraswap.client.ElytraswapClient;

import java.util.Objects;

public class SwapManager {
    public static ClientPlayerEntity clientPlayer = null;

    private static ItemStack chestPlate;

    private static ItemStack elytra;

    private static boolean swapped;

    public static boolean playerExists() {
        return clientPlayer != null;
    }

    public static ItemStack getChestPlate() {
        return chestPlate;
    }

    public static ItemStack getElytra() {
        return elytra;
    }

    public static void applyChestplate(ItemStack chestPlate){
        SwapManager.chestPlate = chestPlate.copy();
    }

    public static void applyElytra(ItemStack elytra) {
        SwapManager.elytra = elytra.copy();
    }

    public static void clearItems(){
        chestPlate = null;
        elytra = null;
    }

    public static void tick(){
        if (!playerExists()) {
            if (MinecraftClient.getInstance().player == null) return;
            clientPlayer = MinecraftClient.getInstance().player;
        }
        if (clientPlayer.getHealth() <= 0) {
            clientPlayer = null;
            return;
        }
        if ((clientPlayer.isOnGround() || clientPlayer.isTouchingWater()) && swapped) swapped = false;

        swap();
    }

    public static void enableSwap() {
        swapped = true;
    }

    private static void swap() {
        if (!playerExists()) return;

        ItemStack el = elytra != null ? elytra : findFirstElytraStack();
        if (el.isEmpty()) return;

        ItemStack cp = chestPlate != null ? chestPlate : findFirstChestplateStack();

        PlayerInventory inv = clientPlayer.getInventory();
        MinecraftClient client = MinecraftClient.getInstance();
        ItemStack equipped = clientPlayer.getEquippedStack(EquipmentSlot.CHEST);

        boolean isElytraEquipped = lazyAreItemsAndComponentsEqual(equipped, el);
        boolean isChestPlateEquipped = lazyAreItemsAndComponentsEqual(equipped, cp);


        if ((!isElytraEquipped && !isChestPlateEquipped)) {
            return;
        }

        ItemStack target = swapped ? el : cp;

        if (ItemStack.areItemsAndComponentsEqual(equipped, target)){
            return;
        }

        int targetSlot = lazilyGetSlotWithStack(inv, target);
        if (targetSlot == -1) {
            return;
        }

        // Convert logical slot to ScreenHandler slot index
        int windowSlot = convertInventorySlotToScreenHandlerSlot(targetSlot);
        try {
            client.interactionManager.clickSlot(0, windowSlot, 0, SlotActionType.PICKUP, clientPlayer);
            client.interactionManager.clickSlot(0, 6, 0, SlotActionType.PICKUP, clientPlayer);
            client.interactionManager.clickSlot(0, windowSlot, 0, SlotActionType.PICKUP, clientPlayer);
            if (target.equals(el)) clientPlayer.checkGliding();
            if (target.equals(cp)) clientPlayer.stopGliding();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static boolean lazyAreItemsAndComponentsEqual(ItemStack stack, ItemStack otherStack) {
        if (!stack.isOf(otherStack.getItem())) return false;
        if (stack.isEmpty() && otherStack.isEmpty()) return true;

        ItemStack stackCopy = stack.copy();
        ItemStack otherStackCopy = otherStack.copy();
        stackCopy.remove(DataComponentTypes.DAMAGE);
        otherStackCopy.remove(DataComponentTypes.DAMAGE);

        return Objects.equals(stackCopy.getComponents(), otherStackCopy.getComponents());
    }

    public static int findFirstElytraSlot() {
        if (!playerExists()) return -1;
        PlayerInventory inv = clientPlayer.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.isOf(Items.ELYTRA)) {
                return i;
            }
        }
        return -1;
    }

    public static int findFirstChestplateSlot() {
        if (!playerExists()) return -1;
        PlayerInventory inv = clientPlayer.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.isIn(ItemTags.CHEST_ARMOR) && !stack.isOf(Items.ELYTRA)) {
                return i;
            }
        }
        return -1;
    }

    public static ItemStack findFirstElytraStack() {
        int slot = findFirstElytraSlot();
        if (slot == -1) return ItemStack.EMPTY;
        return clientPlayer.getInventory().getStack(slot);
    }

    public static ItemStack findFirstChestplateStack() {
        int slot = findFirstChestplateSlot();
        if (slot == -1) return ItemStack.EMPTY;
        return clientPlayer.getInventory().getStack(slot);
    }



    private static int convertInventorySlotToScreenHandlerSlot(int slot) {
        if (slot >= 0 && slot <= 8) return 36 + slot; // hotbar
        if (slot >= 9 && slot <= 35) return slot; // main inventory
        if (slot == 36) return 8; // boots
        if (slot == 37) return 7; // leggings
        if (slot == 38) return 6; // chestplate ✅
        if (slot == 39) return 5; // helmet
        if (slot == 40) return 45; // offhand
        return -1; // invalid
    }

    public static int lazilyGetSlotWithStack(PlayerInventory inv, ItemStack match) {
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && lazyAreItemsAndComponentsEqual(stack, match)) {
                if (!ItemStack.areItemsEqual(stack,clientPlayer.getEquippedStack(EquipmentSlot.CHEST))) {
                    return i;
                }
            }
        }
        return -1;
    }


    public static boolean shouldApplyChestplate() {
        if (!playerExists()) return false;

        ItemStack handStack = clientPlayer.getMainHandStack();

        // Ensure chestPlate is not null
        boolean holdingSomethingElse;
        if (!(chestPlate == null || chestPlate.isEmpty())) {
            holdingSomethingElse = !handStack.isEmpty() && !lazyAreItemsAndComponentsEqual(chestPlate, handStack);
        } else {
            holdingSomethingElse = !handStack.isEmpty();
        }

        // Check if the held item can be equipped in the chest slot
        boolean canEquip = clientPlayer.canEquip(handStack, EquipmentSlot.CHEST);


        boolean notElytra;
        if (elytra != null) {
            notElytra = !lazyAreItemsAndComponentsEqual(handStack, elytra);
        } else {
            notElytra = true;
        }

        return ElytraswapClient.APPLY_CHESTPLATE.isPressed() && holdingSomethingElse && canEquip && notElytra;
    }

    public static boolean shouldApplyElytra() {
        if (!playerExists()) return false;

        ItemStack handStack = clientPlayer.getMainHandStack();

        // Main hand must not be empty, and must not already be the elytra
        boolean holdingSomethingElse;
        if (!(elytra == null || elytra.isEmpty())) {
            holdingSomethingElse = !handStack.isEmpty() && !lazyAreItemsAndComponentsEqual(elytra, handStack);
        } else {
            holdingSomethingElse = !handStack.isEmpty();
        }
        

        // Check if the held item can be equipped in the chest slot
        boolean canEquip = clientPlayer.canEquip(handStack, EquipmentSlot.CHEST);

        boolean notChestplate;
        if (chestPlate != null) {
            notChestplate = !lazyAreItemsAndComponentsEqual(handStack, chestPlate);
        } else {
            notChestplate = true;
        }

        return ElytraswapClient.APPLY_ELYTRA.isPressed() && holdingSomethingElse && canEquip && notChestplate;
    }
}
