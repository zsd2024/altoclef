package adris.altoclef.tasks.resources.wood;

import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.resources.CraftWithMatchingPlanksTask;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Item;

/**
 * 收集木制压力板任务
 * 该任务负责收集指定数量的木制压力板，通过使用匹配的木板进行合成
 */
public class CollectWoodenPressurePlateTask extends CraftWithMatchingPlanksTask {

    /**
     * 构造函数，指定目标木制压力板类型、所需木板和数量
     * @param targets 目标木制压力板物品数组
     * @param planks 用于合成的木板
     * @param count 需要收集的数量
     */
    public CollectWoodenPressurePlateTask(Item[] targets, ItemTarget planks, int count) {
        super(targets, woodItems -> woodItems.pressurePlate, createRecipe(planks), new boolean[]{true, true, false, false}, count);
    }

    /**
     * 构造函数，指定单一目标木制压力板类型、木板目录名和数量
     * @param target 目标木制压力板物品
     * @param plankCatalogueName 木板在目录中的名称
     * @param count 需要收集的数量
     */
    public CollectWoodenPressurePlateTask(Item target, String plankCatalogueName, int count) {
        this(new Item[]{target}, new ItemTarget(plankCatalogueName, 1), count);
    }

    /**
     * 构造函数，使用默认配置收集指定数量的木制压力板
     * @param count 需要收集的数量
     */
    public CollectWoodenPressurePlateTask(int count) {
        this(ItemHelper.WOOD_PRESSURE_PLATE, TaskCatalogue.getItemTarget("planks", 1), count);
    }


    /**
     * 创建用于合成木制压力板的配方
     * @param planks 用于合成的木板
     * @return 返回合成配方
     */
    private static CraftingRecipe createRecipe(ItemTarget planks) {
        ItemTarget p = planks; // 木板
        return CraftingRecipe.newShapedRecipe(new ItemTarget[]{p, p, null, null}, 1); // 2个木板横向排列，制作1个压力板
    }
}
