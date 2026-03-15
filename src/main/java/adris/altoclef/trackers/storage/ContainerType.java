package adris.altoclef.trackers.storage;

import adris.altoclef.util.slots.ChestSlot;
import adris.altoclef.util.slots.FurnaceSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.*;
import org.apache.commons.lang3.NotImplementedException;

/**
 * 容器类型枚举 - 定义不同类型的存储容器
 */
public enum ContainerType {
    CHEST, ENDER_CHEST, SHULKER, FURNACE, BREWING, MISC, EMPTY;

    /**
     * 从方块获取容器类型
     * @param block 方块
     * @return 容器类型
     */
    public static ContainerType getFromBlock(Block block) {
        if (block instanceof ChestBlock) {
            return CHEST;
        }
        if (block instanceof AbstractFurnaceBlock) {
            return FURNACE;
        }
        if (block.equals(Blocks.ENDER_CHEST)) {
            return ENDER_CHEST;
        }
        if (block instanceof ShulkerBoxBlock) {
            return SHULKER;
        }
        if (block instanceof BrewingStandBlock) {
            return BREWING;
        }
        if (block instanceof BarrelBlock || block instanceof DispenserBlock || block instanceof HopperBlock) {
            return MISC;
        }
        return EMPTY;
    }

    /**
     * 检查屏幕处理器是否匹配指定容器类型
     * @param type 容器类型
     * @param handler 屏幕处理器
     * @return 是否匹配
     */
    public static boolean screenHandlerMatches(ContainerType type, ScreenHandler handler) {
        switch (type) {
            case CHEST, ENDER_CHEST -> {
                return handler instanceof GenericContainerScreenHandler;
            }
            case SHULKER -> {
                return handler instanceof ShulkerBoxScreenHandler;
            }
            case FURNACE -> {
                return handler instanceof AbstractFurnaceScreenHandler;
            }
            case BREWING -> {
                return handler instanceof BrewingStandScreenHandler;
            }
            case MISC -> {
                return handler instanceof Generic3x3ContainerScreenHandler || handler instanceof GenericContainerScreenHandler;
            }
            case EMPTY -> {
                return false;
            }
            default -> throw new NotImplementedException("遗漏了此容器类型: " + type);
        }
    }

    /**
     * 检查屏幕处理器是否匹配指定容器类型
     * @param type 容器类型
     * @return 是否匹配
     */
    public static boolean screenHandlerMatches(ContainerType type) {
        if (MinecraftClient.getInstance().player != null) {
            ScreenHandler h = MinecraftClient.getInstance().player.currentScreenHandler;
            if (h != null)
                return screenHandlerMatches(type, h);
        }
        return false;
    }

    /**
     * 检查是否匹配任何容器类型
     * @return 是否匹配任何容器类型
     */
    public static boolean screenHandlerMatchesAny() {
        return screenHandlerMatches(CHEST) ||
                screenHandlerMatches(SHULKER) ||
                screenHandlerMatches(FURNACE);
    }

    /**
     * 检查槽位类型是否匹配容器类型
     * @param type 容器类型
     * @param slot 槽位
     * @return 是否匹配
     */
    public static boolean slotTypeMatches(ContainerType type, Slot slot) {
        switch (type) {
            case CHEST, ENDER_CHEST, SHULKER -> {
                return slot instanceof ChestSlot;
            }
            case FURNACE -> {
                return slot instanceof FurnaceSlot;
            }
            case BREWING -> throw new NotImplementedException("酿造槽位尚未实现。");
            case MISC -> {
                return true;
            }
            default -> throw new NotImplementedException("遗漏了此容器类型: " + type);
        }
    }
}
