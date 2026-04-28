package adris.altoclef.util;

import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Item;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 物品目标工具类
 * 定义一个物品及其数量。
 * <p>
 * 多个Minecraft物品可以满足一个"物品"的标准（例如，"木板"可以由橡木、金合欢木、云杉木、丛林木等满足）
 */
public class ItemTarget {

    /**
     * 将`Item`对象数组转换为`ItemTarget`对象数组。
     *
     * @param items 要转换的`Item`对象数组
     * @return `ItemTarget`对象数组
     */
    public static ItemTarget[] of(Item... items) {
        return Arrays.stream(items).map(ItemTarget::new).toArray(ItemTarget[]::new);
    }

    private static final int BASICALLY_INFINITY = 99999999;

    /** 空物品目标常量 */
    public static ItemTarget EMPTY = new ItemTarget(new Item[0], 0);
    /** 可匹配的物品数组 */
    private Item[] itemMatches;
    /** 目标数量 */
    private final int targetCount;
    /** 目录名称（如果适用） */
    private String catalogueName = null;
    /** 是否为无限数量 */
    private boolean infinite = false;

    /**
     * 构造函数
     * @param items 可匹配的物品数组
     * @param targetCount 目标数量
     */
    public ItemTarget(Item[] items, int targetCount) {
        itemMatches = items;
        this.targetCount = targetCount;
        infinite = false;
    }

    /**
     * 构造函数（使用目录名称）
     * @param catalogueName 目录名称
     * @param targetCount 目标数量
     */
    public ItemTarget(String catalogueName, int targetCount) {
        this.catalogueName = catalogueName;
        itemMatches = TaskCatalogue.getItemMatches(catalogueName);
        this.targetCount = targetCount;
    }

    /**
     * 构造函数（使用目录名称，默认数量为1）
     * @param catalogueName 目录名称
     */
    public ItemTarget(String catalogueName) {
        this(catalogueName, 1);
    }

    /**
     * 构造函数（单个物品）
     * @param item 物品
     * @param targetCount 目标数量
     */
    public ItemTarget(Item item, int targetCount) {
        this(new Item[]{item}, targetCount);
    }

    /**
     * 构造函数（多个物品，默认数量为1）
     * @param items 物品数组
     */
    public ItemTarget(Item... items) {
        this(items, 1);
    }

    /**
     * 构造函数（单个物品，默认数量为1）
     * @param item 物品
     */
    public ItemTarget(Item item) {
        this(item, 1);
    }

    /**
     * 复制构造函数
     * @param toCopy 要复制的物品目标
     * @param newCount 新的数量
     */
    public ItemTarget(ItemTarget toCopy, int newCount) {
        if (toCopy.itemMatches != null) {
            itemMatches = new Item[toCopy.itemMatches.length];
            System.arraycopy(toCopy.itemMatches, 0, itemMatches, 0, toCopy.itemMatches.length);
        }
        catalogueName = toCopy.catalogueName;
        targetCount = newCount;
        infinite = toCopy.infinite;
    }

    /**
     * 判断物品目标是否为空或null
     * @param target 要检查的物品目标
     * @return 如果为空或null返回true，否则返回false
     */
    public static boolean nullOrEmpty(ItemTarget target) {
        return target == null || target == EMPTY;
    }

    /**
     * 获取多个物品目标的所有匹配物品
     * @param targets 物品目标数组
     * @return 所有匹配的物品数组
     */
    public static Item[] getMatches(ItemTarget... targets) {
        Set<Item> result = new HashSet<>();
        for (ItemTarget target : targets) {
            result.addAll(Arrays.asList(target.getMatches()));
        }
        return result.toArray(Item[]::new);
    }

    /**
     * 设置为无限数量
     * @return 当前实例（用于链式调用）
     */
    public ItemTarget infinite() {
        infinite = true;
        return this;
    }

    /**
     * 获取可匹配的物品数组
     * @return 可匹配的物品数组
     */
    public Item[] getMatches() {
        return itemMatches != null ? itemMatches : new Item[0];
    }

    /**
     * 获取目标数量
     * @return 目标数量（如果是无限则返回极大值）
     */
    public int getTargetCount() {
        if (infinite) {
            return BASICALLY_INFINITY;
        }
        return targetCount;
    }

    /**
     * 判断指定物品是否匹配
     * @param item 要检查的物品
     * @return 如果匹配返回true，否则返回false
     */
    public boolean matches(Item item) {
        if (itemMatches != null) {
            for (Item match : itemMatches) {
                if (match == null) continue;
                if (match.equals(item)) return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为目录物品
     * @return 如果是目录物品返回true，否则返回false
     */
    public boolean isCatalogueItem() {
        return catalogueName != null;
    }

    /**
     * 获取目录名称
     * @return 目录名称
     */
    public String getCatalogueName() {
        return catalogueName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemTarget other) {
            if (infinite) {
                if (!other.infinite) return false;
            } else {
                // Neither are infinite
                if (targetCount != other.targetCount) return false;
            }
            if ((other.itemMatches == null) != (itemMatches == null)) return false;
            if (itemMatches != null) {
                if (itemMatches.length != other.itemMatches.length) return false;
                for (int i = 0; i < itemMatches.length; ++i) {
                    if (other.itemMatches[i] == null) {
                        if ((other.itemMatches[i] == null) != (itemMatches[i] == null)) return false;
                    } else {
                        if (!other.itemMatches[i].equals(itemMatches[i])) return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 判断是否为空
     * @return 如果为空返回true，否则返回false
     */
    public boolean isEmpty() {
        return itemMatches == null || itemMatches.length == 0;
    }

    @Override
    public String toString() {

        StringBuilder result = new StringBuilder();
        if (isEmpty()) {
            result.append("(空)");
        } else if (isCatalogueItem()) {
            result.append(catalogueName);
        } else {
            result.append("[");
            int counter = 0;
            if (itemMatches != null) {
                for (Item item : itemMatches) {
                    if (item == null) {
                        result.append("(空??)");
                    } else {
                        result.append(ItemHelper.trimItemName(item.getTranslationKey()));
                    }
                    if (++counter != itemMatches.length) {
                        result.append(",");
                    }
                }
            }
            result.append("]");
        }
        if (!infinite && !isEmpty() && targetCount > 1) {
            result.append(" x ").append(targetCount);
        } else if (infinite) {
            result.append(" x 无限");
        }

        return result.toString();
    }


}
