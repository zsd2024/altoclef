package adris.altoclef.tasks.resources.wood;

import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.resources.CraftWithMatchingPlanksTask;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Item;

/**
 * 收集木制按钮任务
 * 该任务负责收集指定数量的木制按钮，通过使用匹配的木板进行合成
 */
public class CollectWoodenButtonTask extends CraftWithMatchingPlanksTask {

    /**
     * 构造函数，指定目标木制按钮类型、所需木板和数量
     * @param targets 目标木制按钮物品数组
     * @param planks 用于合成的木板
     * @param count 需要收集的数量
     */
    public CollectWoodenButtonTask(Item[] targets, ItemTarget planks, int count) {
        super(targets, woodItems -> woodItems.button, createRecipe(planks), new boolean[]{true, true, false, false}, count);
    }

    /**
     * 构造函数，指定单一目标木制按钮类型、木板目录名和数量
     * @param target 目标木制按钮物品
     * @param plankCatalogueName 木板在目录中的名称
     * @param count 需要收集的数量
     */
    public CollectWoodenButtonTask(Item target, String plankCatalogueName, int count) {
        this(new Item[]{target}, new ItemTarget(plankCatalogueName, 1), count);
    }

    /**
     * 构造函数，使用默认配置收集指定数量的木制按钮
     * @param count 需要收集的数量
     */
    public CollectWoodenButtonTask(int count) {
        this(ItemHelper.WOOD_BUTTON, TaskCatalogue.getItemTarget("planks", 1), count);
    }


    /**
     * 创建用于合成木制按钮的配方
     * @param planks 用于合成的木板
     * @return 返回合成配方
     */
    private static CraftingRecipe createRecipe(ItemTarget planks) {
        ItemTarget p = planks; // 木板
        return CraftingRecipe.newShapedRecipe(new ItemTarget[]{p, null, null, null}, 1); // 使用1块木板制作1个按钮
    }
}
