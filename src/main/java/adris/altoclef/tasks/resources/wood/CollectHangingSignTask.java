package adris.altoclef.tasks.resources.wood;

import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.resources.CraftWithMatchingStrippedLogsTask;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Item;

/**
 * 收集悬挂式告示牌任务
 * 该任务负责收集指定数量的悬挂式告示牌，通过使用匹配的去皮原木和锁链进行合成
 */
public class CollectHangingSignTask extends CraftWithMatchingStrippedLogsTask {

    /**
     * 构造函数，指定目标悬挂式告示牌类型、所需去皮原木和数量
     * @param targets 目标悬挂式告示牌物品数组
     * @param strippedLogs 用于合成的去皮原木
     * @param count 需要收集的数量
     */
    public CollectHangingSignTask(Item[] targets, ItemTarget strippedLogs, int count) {
        // 底部6个位置是去皮原木，必须是相同的去皮原木类型
        super(targets, woodItems -> woodItems.hangingSign, createRecipe(strippedLogs), new boolean[]{false, false, false, true, true, true, true, true, true}, count);
    }

    /**
     * 构造函数，指定单一目标悬挂式告示牌类型、去皮原木目录名和数量
     * @param target 目标悬挂式告示牌物品
     * @param strippedLogCatalogueName 去皮原木在目录中的名称
     * @param count 需要收集的数量
     */
    public CollectHangingSignTask(Item target, String strippedLogCatalogueName, int count) {
        this(new Item[]{target}, new ItemTarget(strippedLogCatalogueName, 1), count);
    }

    /**
     * 构造函数，使用默认配置收集指定数量的悬挂式告示牌
     * @param count 需要收集的数量
     */
    public CollectHangingSignTask(int count) {
        this(ItemHelper.WOOD_HANGING_SIGN, TaskCatalogue.getItemTarget("stripped_logs", 1), count);


        //#if MC <= 11802
        //$$ throw new IllegalStateException("悬挂式告示牌尚不存在！");
        //#endif
    }


    /**
     * 创建用于合成悬挂式告示牌的配方
     * @param strippedLogs 用于合成的去皮原木
     * @return 返回合成配方
     */
    private static CraftingRecipe createRecipe(ItemTarget strippedLogs) {
        ItemTarget s = strippedLogs; // 去皮原木
        ItemTarget chain = TaskCatalogue.getItemTarget("chain", 1); // 锁链
        return CraftingRecipe.newShapedRecipe(new ItemTarget[]{chain, null, chain, s, s, s, s, s, s}, 6); // 顶部为锁链，底部为去皮原木，可制作6个悬挂式告示牌
    }
}
