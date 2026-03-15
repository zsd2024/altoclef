package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.*;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;

/**
 * 收集金粒任务
 * 用于收集金粒，在不同维度采用不同策略：主世界通过合成金锭获得，下界直接挖掘金矿石或镀金黑石
 */
public class CollectGoldNuggetsTask extends ResourceTask {

    private final int count; // 目标金粒数量

    public CollectGoldNuggetsTask(int count) {
        super(Items.GOLD_NUGGET, count);
        this.count = count;
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        // 任务开始时的初始化
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        switch (WorldHelper.getCurrentDimension()) {
            case OVERWORLD -> {
                setDebugState("获取金锭以转换为金粒");
                int potentialNuggies = mod.getItemStorage().getItemCount(Items.GOLD_NUGGET) + mod.getItemStorage().getItemCount(Items.GOLD_INGOT) * 9;
                if (potentialNuggies >= count && mod.getItemStorage().hasItem(Items.GOLD_INGOT)) {
                    // 将金锭合成成金粒
                    return new CraftInInventoryTask(new RecipeTarget(Items.GOLD_NUGGET, count, CraftingRecipe.newShapedRecipe("golden_nuggets", new ItemTarget[]{new ItemTarget(Items.GOLD_INGOT, 1), null, null, null}, 9)));
                }
                // 获取金锭
                int nuggiesStillNeeded = count - potentialNuggies;
                return TaskCatalogue.getItemTask(Items.GOLD_INGOT, (int) Math.ceil((double) nuggiesStillNeeded / 9.0));
            }
            case NETHER -> {
                setDebugState("挖掘金粒");
                return new MineAndCollectTask(Items.GOLD_NUGGET, count, new Block[]{Blocks.NETHER_GOLD_ORE, Blocks.GILDED_BLACKSTONE}, MiningRequirement.WOOD);
            }
            case END -> {
                setDebugState("前往主世界");
                return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
            }
        }

        setDebugState("无效维度??: " + WorldHelper.getCurrentDimension());
        return null;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectGoldNuggetsTask;
    }

    @Override
    protected String toDebugStringName() {
        return "收集 " + count + " 个金粒";
    }
}
