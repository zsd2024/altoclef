package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.PlaceBlockTask;
import adris.altoclef.tasks.movement.GetCloseToBlockTask;
import adris.altoclef.tasks.movement.SearchChunkForBlockTask;
import adris.altoclef.tasks.squashed.CataloguedResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

/**
 * 收集蜜脾任务
 * 用于收集蜜蜂巢中的蜜脾，可以选择是否使用营火来避免激怒蜜蜂
 */
public class CollectHoneycombTask extends ResourceTask {
    private final boolean campfire; // 是否使用营火来避免激怒蜜蜂
    private final int count; // 目标蜜脾数量
    private BlockPos nest; // 蜜蜂巢的位置

    public CollectHoneycombTask(int targetCount) {
        super(Items.HONEYCOMB, targetCount);
        campfire = true;
        count = targetCount;
    }

    public CollectHoneycombTask(int targetCount, boolean useCampfire) {
        super(Items.HONEYCOMB, targetCount);
        campfire = useCampfire;
        count = targetCount;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        mod.getBehaviour().push();
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        if (nest == null) {
            Optional<BlockPos> getNearestNest = mod.getBlockScanner().getNearestBlock(Blocks.BEE_NEST);
            if (getNearestNest.isPresent()) nest = getNearestNest.get();
        }
        // 如果我们仍然为空
        if (nest == null) {
            if (campfire && !mod.getItemStorage().hasItemInventoryOnly(Items.CAMPFIRE)) {
                // 不妨先获取一个营火
                setDebugState("找不到蜂巢，先获取营火...");
                return new CataloguedResourceTask(new ItemTarget(Items.CAMPFIRE, 1));
            }
            setDebugState("好吧，我们正在搜索");
            return new SearchChunkForBlockTask(Blocks.BEE_NEST);
        }
        if (campfire && !isCampfireUnderNest(mod, nest)) {
            if (!mod.getItemStorage().hasItemInventoryOnly(Items.CAMPFIRE)) {
                setDebugState("获取一个营火");
                return new CataloguedResourceTask(new ItemTarget(Items.CAMPFIRE, 1));
            }
            setDebugState("放置营火");
            return new PlaceBlockTask(nest.down(2), Blocks.CAMPFIRE);
        }
        if (!mod.getItemStorage().hasItemInventoryOnly(Items.SHEARS)) {
            setDebugState("获取剪刀");
            return new CataloguedResourceTask(new ItemTarget(Items.SHEARS, 1));
        }
        if (mod.getWorld().getBlockState(nest).get(Properties.HONEY_LEVEL) != 5) {
            if (!nest.isWithinDistance(mod.getPlayer().getPos(), 20)) {
                setDebugState("靠近蜂巢");
                return new GetCloseToBlockTask(nest);
            }
            setDebugState("等待蜂巢积累蜂蜜...");
            return null;
        }
        return new InteractWithBlockTask(Items.SHEARS, nest);
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectHoneycombTask;
    }

    @Override
    protected String toDebugStringName() {
        return "收集 " + count + " 个蜜脾 " + (campfire ? "和平方式" : "冒险方式");
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    /**
     * 检查蜂巢下方是否有营火
     * @param mod AltoClef实例
     * @param pos 蜂巢位置
     * @return 如果蜂巢下方有营火则返回true
     */
    private boolean isCampfireUnderNest(AltoClef mod, BlockPos pos) {
        for (BlockPos underPos : WorldHelper.scanRegion(pos.down(6), pos.down())) {
            if (mod.getWorld().getBlockState(underPos).getBlock() == Blocks.CAMPFIRE)
                return true;
        }
        return false;
    }
}
