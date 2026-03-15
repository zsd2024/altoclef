package adris.altoclef.tasks.resources.wood;

import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.resources.CraftWithMatchingPlanksTask;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Item;

/**
 * 收集木制栅栏任务
 * 该任务负责收集指定数量的木制栅栏，通过使用匹配的木板和木棍进行合成
 */
public class CollectFenceTask extends CraftWithMatchingPlanksTask {

    /**
     * 构造函数，指定目标木制栅栏类型、所需木板和数量
     * @param targets 目标木制栅栏物品数组
     * @param planks 用于合成的木板
     * @param count 需要收集的数量
     */
    public CollectFenceTask(Item[] targets, ItemTarget planks, int count) {
        super(targets, woodItems -> woodItems.fence, createRecipe(planks), new boolean[]{true, false, true, true, false, true, false, false, false}, count);
    }

    /**
     * 构造函数，指定单一目标木制栅栏类型、木板目录名和数量
     * @param target 目标木制栅栏物品
     * @param plankCatalogueName 木板在目录中的名称
     * @param count 需要收集的数量
     */
    public CollectFenceTask(Item target, String plankCatalogueName, int count) {
        this(new Item[]{target}, new ItemTarget(plankCatalogueName, 1), count);
    }

    /**
     * 构造函数，使用默认配置收集指定数量的木制栅栏
     * @param count 需要收集的数量
     */
    public CollectFenceTask(int count) {
        this(ItemHelper.WOOD_FENCE, TaskCatalogue.getItemTarget("planks", 1), count);
    }

    /**
     * 创建用于合成木制栅栏的配方
     * @param planks 用于合成的木板
     * @return 返回合成配方
     */
    private static CraftingRecipe createRecipe(ItemTarget planks) {
        ItemTarget p = planks; // 木板
        ItemTarget s = TaskCatalogue.getItemTarget("stick", 1); // 木棍
        return CraftingRecipe.newShapedRecipe(new ItemTarget[]{p, s, p, p, s, p, null, null, null}, 3); // 木板和木棍交替排列，制作3个栅栏
    }
}
