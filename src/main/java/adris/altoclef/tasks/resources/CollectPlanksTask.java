package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ItemStorageTracker;
import adris.altoclef.util.*;
import adris.altoclef.util.helpers.ItemHelper;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 收集木板任务
 * 用于收集木板，优先使用原木合成，否则挖掘原木或木板
 */
public class CollectPlanksTask extends ResourceTask {

    private final Item[] _planks; // 木板类型数组
    private final Item[] _logs; // 原木类型数组
    private final int _targetCount; // 目标数量
    private boolean _logsInNether; // 原木是否在下界

    public CollectPlanksTask(Item[] planks, Item[] logs, int count, boolean logsInNether) {
        super(new ItemTarget(planks, count));
        _planks = planks;
        _logs = logs;
        _targetCount = count;
        _logsInNether = logsInNether;
    }

    public CollectPlanksTask(int count) {
        this(ItemHelper.PLANKS, ItemHelper.LOG, count, false);
    }

    public CollectPlanksTask(Item plank, Item log, int count) {
        this(new Item[]{plank}, new Item[]{log}, count, false);
    }

    public CollectPlanksTask(Item plank, int count) {
        this(plank, ItemHelper.planksToLog(plank), count);
    }

    /**
     * 生成木板合成配方
     * @param logs 原木类型数组
     * @return 木板合成配方
     */
    private static CraftingRecipe generatePlankRecipe(Item[] logs) {
        return CraftingRecipe.newShapedRecipe(
                "planks",
                new Item[][]{
                        logs, null,
                        null, null
                },
                4
        );
    }

    @Override
    protected double getPickupRange(AltoClef mod) {
        ItemStorageTracker storage = mod.getItemStorage();
        if (storage.getItemCount(ItemHelper.LOG)*4>_targetCount) return 10;

        return 50;
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

        // 当可以合成时进行合成
        int totalInventoryPlankCount = mod.getItemStorage().getItemCount(_planks);
        int potentialPlanks = totalInventoryPlankCount + mod.getItemStorage().getItemCount(_logs) * 4;
        if (potentialPlanks >= _targetCount) {
            for (Item logCheck : _logs) {
                int count = mod.getItemStorage().getItemCount(logCheck);
                if (count > 0) {
                    Item plankCheck = ItemHelper.logToPlanks(logCheck);
                    if (plankCheck == null) {
                        Debug.logError("无效/无法转换的原木: " + logCheck + " (找不到对应的木板)");
                    }
                    int plankCount = mod.getItemStorage().getItemCount(plankCheck);
                    int otherPlankCount = totalInventoryPlankCount - plankCount;
                    int targetTotalPlanks = Math.min(count * 4 + plankCount, _targetCount - otherPlankCount);
                    setDebugState("我们有 " + logCheck + "，合成 " + targetTotalPlanks + " 个木板。");
                    return new CraftInInventoryTask(new RecipeTarget(plankCheck, targetTotalPlanks, generatePlankRecipe(_logs)));
                }
            }
        }

        // 收集木板和原木
        ArrayList<ItemTarget> blocksTomine = new ArrayList<>(2);
        blocksTomine.add(new ItemTarget(_logs));
        // 如果被指示则忽略木板
        if (!mod.getBehaviour().exclusivelyMineLogs()) {
            // TODO: 将木板加回来，但使用启发式检查（这样我们不会去废弃矿井)
            //blocksTomine.add(new ItemTarget(ItemUtil.PLANKS));
        }

        ResourceTask mineTask = new MineAndCollectTask(blocksTomine.toArray(ItemTarget[]::new), MiningRequirement.HAND);
        // 有点笨拙
        if (_logsInNether) {
            mineTask.forceDimension(Dimension.NETHER);
        }
        return mineTask;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectPlanksTask;
    }

    @Override
    protected String toDebugStringName() {
        return "制作 " + _targetCount + " 个木板 " + Arrays.toString(_planks);
    }

    /**
     * 设置原木在下界
     * @return 当前任务实例
     */
    public CollectPlanksTask logsInNether() {
        _logsInNether = true;
        return this;
    }
}
