package adris.altoclef.tasks.resources.wood;

import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.resources.CraftWithMatchingPlanksTask;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Item;

/**
 * 收集木制告示牌任务
 * 该任务负责收集指定数量的木制告示牌，通过使用匹配的木板和木棍进行合成
 */
public class CollectSignTask extends CraftWithMatchingPlanksTask {

    /**
     * 构造函数，指定目标木制告示牌类型、所需木板和数量
     * @param targets 目标木制告示牌物品数组
     * @param planks 用于合成的木板
     * @param count 需要收集的数量
     */
    public CollectSignTask(Item[] targets, ItemTarget planks, int count) {
        // 前6个位置是木板，必须是相同的木板类型
        super(targets, woodItems -> woodItems.sign, createRecipe(planks), new boolean[]{true, true, true, true, true, true, false, false, false}, count);
    }

    /**
     * 构造函数，指定单一目标木制告示牌类型、木板目录名和数量
     * @param target 目标木制告示牌物品
     * @param plankCatalogueName 木板在目录中的名称
     * @param count 需要收集的数量
     */
    public CollectSignTask(Item target, String plankCatalogueName, int count) {
        this(new Item[]{target}, new ItemTarget(plankCatalogueName, 1), count);
    }

    /**
     * 构造函数，使用默认配置收集指定数量的木制告示牌
     * @param count 需要收集的数量
     */
    public CollectSignTask(int count) {
        this(ItemHelper.WOOD_SIGN, TaskCatalogue.getItemTarget("planks", 1), count);
    }


    /**
     * 创建用于合成木制告示牌的配方
     * @param planks 用于合成的木板
     * @return 返回合成配方
     */
    private static CraftingRecipe createRecipe(ItemTarget planks) {
        ItemTarget p = planks; // 木板
        ItemTarget stick = TaskCatalogue.getItemTarget("stick", 1); // 木棍
        return CraftingRecipe.newShapedRecipe(new ItemTarget[]{p, p, p, p, p, p, null, stick, null}, 3); // 顶部6格为木板，中间为木棍，可制作3个告示牌
    }
}
