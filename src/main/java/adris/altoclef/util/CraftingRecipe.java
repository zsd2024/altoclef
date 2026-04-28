package adris.altoclef.util;

import adris.altoclef.Debug;
import net.minecraft.item.Item;

import java.util.Arrays;

/**
 * 合成配方工具类
 * 表示一个Minecraft合成配方，包括配方槽位、尺寸、形状等信息
 */
public class CraftingRecipe {

    /** 配方槽位中的物品目标 */
    private ItemTarget[] slots;

    /** 配方的宽度 */
    private int width, height;

    /** 是否为无序合成 */
    private boolean shapeless;

    /** 配方的简短名称 */
    private String shortName;

    /** 合成输出的数量 */
    private int outputCount;

    // Every item in this list MUST match.
    // Used for beds where the wood can be anything
    // but the wool MUST be the same color.
    //private final Set<Integer> _mustMatch = new HashSet<>();

    // 私有构造函数，用于内部创建实例
    private CraftingRecipe() {
    }

    /**
     * 创建一个新的有序合成配方（无名称）
     * @param items 二维物品数组，表示合成配方
     * @param outputCount 合成输出的数量
     * @return 创建的合成配方实例
     */
    public static CraftingRecipe newShapedRecipe(Item[][] items, int outputCount) {
        return newShapedRecipe(null, items, outputCount);
    }

    /**
     * 创建一个新的有序合成配方（无名称）
     * @param slots 物品目标数组，表示合成配方槽位
     * @param outputCount 合成输出的数量
     * @return 创建的合成配方实例
     */
    public static CraftingRecipe newShapedRecipe(ItemTarget[] slots, int outputCount) {
        return newShapedRecipe(null, slots, outputCount);
    }

    /**
     * 创建一个新的有序合成配方（带名称）
     * @param shortName 配方的简短名称
     * @param items 二维物品数组，表示合成配方
     * @param outputCount 合成输出的数量
     * @return 创建的合成配方实例
     */
    public static CraftingRecipe newShapedRecipe(String shortName, Item[][] items, int outputCount) {
        return newShapedRecipe(shortName, createSlots(items), outputCount);
    }

    /**
     * 创建一个新的有序合成配方（带名称）
     * @param shortName 配方的简短名称
     * @param slots 物品目标数组，表示合成配方槽位
     * @param outputCount 合成输出的数量
     * @return 创建的合成配方实例
     */
    public static CraftingRecipe newShapedRecipe(String shortName, ItemTarget[] slots, int outputCount) {
        if (slots.length != 4 && slots.length != 9) {
            Debug.logError("无效的有序合成配方，大小必须为4或9。给定大小: " + slots.length);
            return null;
        }

        CraftingRecipe result = new CraftingRecipe();
        result.shortName = shortName;
        // 移除null值
        result.slots = Arrays.stream(slots).map(target -> target == null ? ItemTarget.EMPTY : target).toArray(ItemTarget[]::new);
        result.outputCount = outputCount;
        if (slots.length == 4) {
            result.width = 2;
            result.height = 2;
        } else {
            result.width = 3;
            result.height = 3;
        }
        result.shapeless = false;

        return result;
    }

    /**
     * 从物品目标数组创建槽位数组
     * @param slots 物品目标数组
     * @return 槽位数组的副本
     */
    private static ItemTarget[] createSlots(ItemTarget[] slots) {
        ItemTarget[] result = new ItemTarget[slots.length];
        System.arraycopy(slots, 0, result, 0, slots.length);
        return result;
    }

    /**
     * 从二维物品数组创建槽位数组
     * @param slots 二维物品数组
     * @return 槽位数组
     */
    private static ItemTarget[] createSlots(Item[][] slots) {
        ItemTarget[] result = new ItemTarget[slots.length];
        for (int i = 0; i < slots.length; ++i) {
            if (slots[i] == null) {
                result[i] = ItemTarget.EMPTY;
            } else {
                result[i] = new ItemTarget(slots[i]);
            }
        }
        return result;
    }

    /**
     * 获取指定索引的槽位物品目标
     * @param index 槽位索引
     * @return 槽位的物品目标，如果为空则返回EMPTY
     */
    public ItemTarget getSlot(int index) {
        ItemTarget result = slots[index];
        return result != null ? result : ItemTarget.EMPTY;
    }

    /**
     * 获取槽位数量
     * @return 槽位数量
     */
    public int getSlotCount() {
        return slots.length;
    }

    /**
     * 获取所有槽位的物品目标数组
     * @return 槽位的物品目标数组
     */
    public ItemTarget[] getSlots() {
        return slots;
    }

    /**
     * 获取配方的宽度
     * @return 配方的宽度
     */
    public int getWidth() {
        return width;
    }

    /**
     * 获取配方的高度
     * @return 配方的高度
     */
    public int getHeight() {
        return height;
    }

    /**
     * 判断是否为无序合成
     * @return 如果是无序合成返回true，否则返回false
     */
    public boolean isShapeless() {
        return shapeless;
    }

    /**
     * 判断是否为大型合成（3x3）
     * @return 如果是大型合成返回true，否则返回false
     */
    public boolean isBig() {
        return slots.length > 4;
    }

    /**
     * 获取合成输出的数量
     * @return 合成输出的数量
     */
    public int outputCount() {
        return outputCount;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CraftingRecipe other) {
            if (other.shapeless != shapeless) return false;
            if (other.outputCount != outputCount) return false;
            if (other.height != height) return false;
            if (other.width != width) return false;
            //if (other._mustMatch.size() != _mustMatch.size()) return false;
            if (other.slots.length != slots.length) return false;
            for (int i = 0; i < slots.length; ++i) {
                if ((other.slots[i] == null) != (slots[i] == null)) return false;
                if (other.slots[i] != null && !other.slots[i].equals(slots[i])) return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String name = "CraftingRecipe{";
        if (shortName != null) {
            name += "craft " + shortName;
        } else {
            name += "_slots=" + Arrays.toString(slots) +
                    ", _width=" + width +
                    ", _height=" + height +
                    ", _shapeless=" + shapeless;
        }
        name += "}";
        return name;
    }
}
