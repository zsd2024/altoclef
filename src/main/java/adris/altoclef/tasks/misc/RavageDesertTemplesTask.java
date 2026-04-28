package adris.altoclef.tasks.misc;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.movement.SearchWithinBiomeTask;
import adris.altoclef.tasks.squashed.CataloguedResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.BiomeKeys;

import java.util.List;

/**
 * 掠夺沙漠神庙任务
 * 此任务负责在沙漠生物群系中寻找并掠夺沙漠神庙
 */
public class RavageDesertTemplesTask extends Task {
    /**
     * 沙漠神庙中可能获得的战利品列表
     */
    public final Item[] LOOT = {
            Items.BONE,
            Items.ROTTEN_FLESH,
            Items.GUNPOWDER,
            Items.SAND,
            Items.STRING,
            Items.SPIDER_EYE,
            Items.ENCHANTED_BOOK,
            Items.SADDLE,
            Items.GOLDEN_APPLE,
            Items.GOLD_INGOT,
            Items.IRON_INGOT,
            Items.EMERALD,
            Items.IRON_HORSE_ARMOR,
            Items.GOLDEN_HORSE_ARMOR,
            Items.DIAMOND,
            Items.DIAMOND_HORSE_ARMOR,
            Items.ENCHANTED_GOLDEN_APPLE
    };
    /**
     * 当前找到的沙漠神庙位置
     */
    private BlockPos currentTemple;
    /**
     * 当前执行的掠夺任务
     */
    private Task lootTask;
    /**
     * 获取镐子的任务
     */
    private Task pickaxeTask;

    /**
     * 构造函数
     */
    public RavageDesertTemplesTask() {

    }

    @Override
    protected void onStart() {
        // 保存当前行为设置
        AltoClef.getInstance().getBehaviour().push();
    }

    @Override
    protected Task onTick() {
        // 如果需要获取镐子且任务未完成，先执行获取镐子任务
        if (pickaxeTask != null && !pickaxeTask.isFinished()) {
            setDebugState("需要先获取镐子");
            return pickaxeTask;
        }
        // 如果正在掠夺神庙且任务未完成，继续执行掠夺任务
        if (lootTask != null && !lootTask.isFinished()) {
            setDebugState("正在掠夺找到的神庙");
            return lootTask;
        }
        // 如果没有足够的挖掘工具，先获取木镐
        if (StorageHelper.miningRequirementMetInventory(MiningRequirement.WOOD)) {
            setDebugState("需要先获取镐子");
            pickaxeTask = new CataloguedResourceTask(new ItemTarget(Items.WOODEN_PICKAXE, 2));
            return pickaxeTask;
        }
        // 尝试获取一个沙漠神庙位置
        currentTemple = WorldHelper.getADesertTemple();
        if (currentTemple != null) {
            // 找到神庙，开始掠夺任务
            lootTask = new LootDesertTempleTask(currentTemple, List.of(LOOT));
            setDebugState("正在掠夺找到的神庙");
            return lootTask;
        }
        // 未找到神庙，在沙漠生物群系中搜索
        return new SearchWithinBiomeTask(BiomeKeys.DESERT);
    }

    @Override
    protected void onStop(Task task) {
        // 恢复之前的行为设置
        AltoClef.getInstance().getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof RavageDesertTemplesTask;
    }

    @Override
    public boolean isFinished() {
        // 此任务永远不会自动完成，需要手动停止
        return false;
    }

    @Override
    protected String toDebugString() {
        return "掠夺沙漠神庙";
    }
}
