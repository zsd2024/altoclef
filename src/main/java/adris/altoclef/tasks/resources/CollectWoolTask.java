package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.entity.ShearSheepTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;

import java.util.Arrays;
import java.util.HashSet;

/**
 * 收集羊毛任务
 * 用于收集指定颜色的羊毛，通过挖掘羊毛块、剪羊毛或击杀羊获得
 */
public class CollectWoolTask extends ResourceTask {

    private final int _count; // 目标羊毛数量

    private final HashSet<DyeColor> _colors; // 需要的颜色集合
    private final Item[] _wools; // 目标羊毛物品数组

    public CollectWoolTask(DyeColor[] colors, int count) {
        super(new ItemTarget(ItemHelper.WOOL, count));
        _colors = new HashSet<>(Arrays.asList(colors));
        _count = count;
        _wools = getWoolColorItems(colors);
    }

    public CollectWoolTask(DyeColor color, int count) {
        this(new DyeColor[]{color}, count);
    }

    public CollectWoolTask(int count) {
        this(DyeColor.values(), count);
    }

    /**
     * 根据染料颜色获取对应的羊毛物品
     * @param colors 染料颜色数组
     * @return 对应颜色的羊毛物品数组
     */
    private static Item[] getWoolColorItems(DyeColor[] colors) {
        Item[] result = new Item[colors.length];
        for (int i = 0; i < result.length; ++i) {
            result[i] = ItemHelper.getColorfulItems(colors[i]).wool;
        }
        return result;
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

        // TODO: 如果我们找不到合适颜色的羊毛方块
        // 且我们找不到合适颜色的羊:
        // 使用染料+普通羊毛合成所需颜色的羊毛!!

        // 如果我们找到羊毛方块，破坏它
        Block[] woolBlocks = ItemHelper.itemsToBlocks(_wools);
        if (mod.getBlockScanner().anyFound(woolBlocks)) {
            return new MineAndCollectTask(new ItemTarget(_wools), woolBlocks, MiningRequirement.HAND);
        }

        // 如果我们有剪刀，右键点击最近的羊
        // 否则，击杀+拾取羊毛

        // 维度检查
        if (isInWrongDimension(mod) && !mod.getEntityTracker().entityFound(SheepEntity.class)) {
            return getToCorrectDimensionTask(mod);
        }

        if (mod.getItemStorage().hasItem(Items.SHEARS)) {
            // 剪羊毛
            return new ShearSheepTask();
        }

        // 唯一剩下的选择就是击杀羊
        return new KillAndLootTask(SheepEntity.class, entity -> {
            if (entity instanceof SheepEntity sheep) {
                // 狩猎相同颜色的羊
                return _colors.contains(sheep.getColor()) && !sheep.isSheared();
            }
            return false;
        }, new ItemTarget(_wools, _count));
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectWoolTask && ((CollectWoolTask) other)._count == _count;
    }

    @Override
    protected String toDebugStringName() {
        return "收集 " + _count + " 个羊毛。";
    }

}
