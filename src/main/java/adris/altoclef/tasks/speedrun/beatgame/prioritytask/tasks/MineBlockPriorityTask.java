package adris.altoclef.tasks.speedrun.beatgame.prioritytask.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.resources.MineAndCollectTask;
import adris.altoclef.tasks.speedrun.beatgame.prioritytask.prioritycalculators.DistancePriorityCalculator;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.StorageHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

/**
 * 挖掘方块优先级任务 - 为获取特定物品而挖掘指定方块
 */
public class MineBlockPriorityTask extends PriorityTask{

    public final Block[] toMine; // 要挖掘的方块
    public final Item[] droppedItem; // 掉落物品
    public final ItemTarget[] droppedItemTargets; // 掉落物品目标
    private final MiningRequirement miningRequirement; // 挖掘需求
    private final DistancePriorityCalculator prioritySupplier; // 优先级提供器

    public MineBlockPriorityTask(Block[] toMine, Item[] droppedItem, MiningRequirement miningRequirement, DistancePriorityCalculator prioritySupplier) {
        this(toMine, droppedItem, miningRequirement, prioritySupplier, false, true, false);
    }

    public MineBlockPriorityTask(Block[] toMine, Item[] droppedItem, MiningRequirement miningRequirement, DistancePriorityCalculator prioritySupplier, Function<AltoClef, Boolean> canCall) {
        this(toMine, droppedItem, miningRequirement, prioritySupplier,canCall , false, true, false);
    }

    public MineBlockPriorityTask(Block[] toMine, Item[] droppedItem, MiningRequirement miningRequirement, DistancePriorityCalculator prioritySupplier, boolean shouldForce, boolean canCache, boolean bypassForceCooldown) {
        this(toMine, droppedItem, miningRequirement, prioritySupplier, mod -> true, shouldForce, canCache, bypassForceCooldown);
    }

    public MineBlockPriorityTask(Block[] toMine, Item[] droppedItem, MiningRequirement miningRequirement, DistancePriorityCalculator prioritySupplier, Function<AltoClef, Boolean> canCall, boolean shouldForce, boolean canCache, boolean bypassForceCooldown) {
        super(canCall, shouldForce, canCache, bypassForceCooldown);
        this.toMine = toMine;
        this.droppedItem = droppedItem;
        this.droppedItemTargets = ItemTarget.of(droppedItem);
        this.miningRequirement = miningRequirement;
        this.prioritySupplier = prioritySupplier;
    }

    @Override
    public Task getTask(AltoClef mod) {
        return new MineAndCollectTask(droppedItemTargets, toMine, miningRequirement);
    }

    @Override
    public String getDebugString() {
        return "收集资源: "+ Arrays.toString(droppedItem);
    }

    @Override
    protected double getPriority(AltoClef mod) {
        if (!StorageHelper.miningRequirementMet(miningRequirement)) return Double.NEGATIVE_INFINITY;

        double closestDist = getClosestDist(mod);
        int itemCount = mod.getItemStorage().getItemCount(droppedItem);

        prioritySupplier.update(itemCount);
        return prioritySupplier.getPriority(closestDist);
    }


    /**
     * 获取最近距离
     * @param mod AltoClef实例
     * @return 最近距离
     */
    private double getClosestDist(AltoClef mod) {
        Vec3d pos = mod.getPlayer().getPos();

        Pair<Double, Optional<BlockPos>> closestBlock = MineAndCollectTask.MineOrCollectTask.getClosestBlock(mod,pos,  toMine);
        Pair<Double, Optional<ItemEntity>> closestDrop = MineAndCollectTask.MineOrCollectTask.getClosestItemDrop(mod,pos, droppedItemTargets);

        return Math.min(closestBlock.getLeft(), closestDrop.getLeft());
    }

}
