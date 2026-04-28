package adris.altoclef.tasks.misc;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.container.LootContainerTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 掠夺废弃传送门任务
 * 此任务负责寻找并掠夺废弃传送门中的宝箱
 */
public class RavageRuinedPortalsTask extends Task {
    /**
     * 废弃传送门中可能获得的战利品列表
     */
    public final Item[] LOOT = {
            Items.IRON_NUGGET,
            Items.FLINT,
            Items.OBSIDIAN,
            Items.FIRE_CHARGE,
            Items.FLINT_AND_STEEL,
            Items.GOLD_NUGGET,
            Items.GOLDEN_APPLE,
            Items.GOLDEN_AXE,
            Items.GOLDEN_HOE,
            Items.GOLDEN_PICKAXE,
            Items.GOLDEN_SHOVEL,
            Items.GOLDEN_SWORD,
            Items.GOLDEN_HELMET,
            Items.GOLDEN_CHESTPLATE,
            Items.GOLDEN_LEGGINGS,
            Items.GOLDEN_BOOTS,
            Items.GLISTERING_MELON_SLICE,
            Items.GOLDEN_CARROT,
            Items.GOLD_INGOT,
            Items.CLOCK,
            Items.LIGHT_WEIGHTED_PRESSURE_PLATE,
            Items.GOLDEN_HORSE_ARMOR,
            Items.GOLD_BLOCK,
            Items.BELL,
            Items.ENCHANTED_GOLDEN_APPLE
    };
    /**
     * 不是废弃传送门的宝箱位置列表（用于排除）
     */
    private List<BlockPos> notRuinedPortalChests = new ArrayList<>();
    /**
     * 当前执行的掠夺任务
     */
    private Task lootTask;

    /**
     * 构造函数
     */
    public RavageRuinedPortalsTask() {

    }

    @Override
    protected void onStart() {
        // 保存当前行为设置
        AltoClef.getInstance().getBehaviour().push();
    }

    @Override
    protected Task onTick() {
        // 如果正在掠夺宝箱且任务未完成，继续执行掠夺任务
        if (lootTask != null && lootTask.isActive() && !lootTask.isFinished()) {
            return lootTask;
        }
        // 寻找最近的未打开的废弃传送门宝箱
        Optional<BlockPos> closest = locateClosestUnopenedRuinedPortalChest(AltoClef.getInstance());
        if (closest.isPresent()) {
            lootTask = new LootContainerTask(closest.get(), List.of(LOOT));
            return lootTask;
        }
        // 未找到宝箱，随机游荡以寻找新的废弃传送门
        return new TimeoutWanderTask();
    }

    @Override
    protected void onStop(Task task) {
        // 恢复之前的行为设置
        AltoClef.getInstance().getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task task) {
        return task instanceof RavageRuinedPortalsTask;
    }

    @Override
    public boolean isFinished() {
        // 此任务永远不会自动完成，需要手动停止
        return false;
    }

    @Override
    protected String toDebugString() {
        return "掠夺废弃传送门";
    }

    /**
     * 判断指定位置的宝箱是否可能是废弃传送门的宝箱
     *
     * @param mod      AltoClef实例
     * @param blockPos 宝箱位置
     * @return 如果是废弃传送门的宝箱则返回true，否则返回false
     */
    private boolean canBeLootablePortalChest(AltoClef mod, BlockPos blockPos) {
        // 如果宝箱上方是水或Y坐标低于50，则不是废弃传送门的宝箱
        if (mod.getWorld().getBlockState(blockPos.up(1)).getBlock() == Blocks.WATER || blockPos.getY() < 50) {
            return false;
        }
        // 在宝箱周围区域扫描是否有地狱岩，如果有则是废弃传送门
        for (BlockPos check : WorldHelper.scanRegion(blockPos.add(-4,-2,-4), blockPos.add(4,2,4))) {
            if (mod.getWorld().getBlockState(check).getBlock() == Blocks.NETHERRACK) {
                return true;
            }
        }
        // 记录不是废弃传送门的宝箱，避免重复检查
        notRuinedPortalChests.add(blockPos);
        return false;
    }

    /**
     * 寻找最近的未打开的废弃传送门宝箱
     *
     * @param mod AltoClef实例
     * @return 最近的废弃传送门宝箱位置（如果存在）
     */
    private Optional<BlockPos> locateClosestUnopenedRuinedPortalChest(AltoClef mod) {
        // 只能在主世界寻找废弃传送门
        if (WorldHelper.getCurrentDimension() != Dimension.OVERWORLD) {
            return Optional.empty();
        }
        return mod.getBlockScanner().getNearestBlock(blockPos -> !notRuinedPortalChests.contains(blockPos) && WorldHelper.isUnopenedChest(blockPos) && canBeLootablePortalChest(mod, blockPos), Blocks.CHEST);
    }
}
