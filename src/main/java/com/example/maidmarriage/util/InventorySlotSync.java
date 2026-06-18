package com.example.maidmarriage.util;

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * 玩家背包槽位同步工具。
 *
 * <p>送礼、眷恋供奉这类 UI 交互会从客户端提交背包槽位号。
 * 服务端必须从真实背包重新取栈、扣除、写回空栈并广播，否则客户端容易看到物品数量“弹回去”。
 */
public final class InventorySlotSync {
    private InventorySlotSync() {
    }

    public static ItemStack getPlayerInventoryStack(ServerPlayer player, int slotIndex) {
        if (!isValidPlayerInventorySlot(player, slotIndex)) {
            return ItemStack.EMPTY;
        }
        return player.getInventory().items.get(slotIndex);
    }

    public static boolean consumeOnePlayerInventoryItem(ServerPlayer player, int slotIndex) {
        if (player == null) {
            return false;
        }
        if (player.getAbilities().instabuild) {
            syncPlayerInventorySlot(player, slotIndex);
            return true;
        }
        ItemStack stack = getPlayerInventoryStack(player, slotIndex);
        if (stack.isEmpty()) {
            syncPlayerInventorySlot(player, slotIndex);
            return false;
        }
        stack.shrink(1);
        if (stack.isEmpty()) {
            player.getInventory().items.set(slotIndex, ItemStack.EMPTY);
        }
        syncPlayerInventorySlot(player, slotIndex);
        return true;
    }

    public static void syncPlayerInventorySlot(ServerPlayer player, int slotIndex) {
        if (!isValidPlayerInventorySlot(player, slotIndex)) {
            return;
        }
        ItemStack stack = player.getInventory().items.get(slotIndex);
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
        player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, slotIndex, stack.copy()));
    }

    private static boolean isValidPlayerInventorySlot(ServerPlayer player, int slotIndex) {
        return player != null && slotIndex >= 0 && slotIndex < player.getInventory().items.size();
    }
}
