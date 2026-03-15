package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

/**
 * 收集床任务
 * 用于制作床，需要羊毛和木板
 */
public class CollectBedTask extends CraftWithMatchingWoolTask {

    public static final Block[] BEDS = ItemHelper.itemsToBlocks(ItemHelper.BED); // 床方块数组

    private final ItemTarget visualBedTarget; // 可视化床目标

    public CollectBedTask(Item[] beds, ItemTarget wool, int count) {
        // 上面3个是羊毛，必须是相同的颜色
        super(new ItemTarget(beds, count), colorfulItems -> colorfulItems.wool, colorfulItems -> colorfulItems.bed, createBedRecipe(wool), new boolean[]{true, true, true, false, false, false, false, false, false});
        visualBedTarget = new ItemTarget(beds, count);
    }

    public CollectBedTask(Item bed, String woolCatalogueName, int count) {
        this(new Item[]{bed}, new ItemTarget(woolCatalogueName, 1), count);
    }

    public CollectBedTask(int count) {
        this(ItemHelper.BED, TaskCatalogue.getItemTarget("wool", 1), count);
    }

    /**
     * 创建床的合成配方
     * @param wool 羊毛目标
     * @return 床的合成配方
     */
    private static CraftingRecipe createBedRecipe(ItemTarget wool) {
        ItemTarget w = wool;
        ItemTarget p = TaskCatalogue.getItemTarget("planks", 1);
        return CraftingRecipe.newShapedRecipe(new ItemTarget[]{w, w, w, p, p, p, null, null, null}, 1);
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }


    @Override
    protected Task onResourceTick(AltoClef mod) {
        // 如果可能，从世界中破坏床，这样会比较快
        if (mod.getBlockScanner().anyFound(BEDS)) {
            // 失败+黑名单封装在此任务中
            return new MineAndCollectTask(new ItemTarget(ItemHelper.BED, 1), BEDS, MiningRequirement.HAND);
        }
        return super.onResourceTick(mod);
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof CollectBedTask task) {
            return task.visualBedTarget.equals(visualBedTarget);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "制作床: " + visualBedTarget;
    }
}
