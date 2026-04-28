package adris.altoclef.tasks.misc;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.container.LootContainerTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.List;

/**
 * 掠夺沙漠神殿任务
 * 此任务负责掠夺沙漠神殿中的宝箱，包括移除压力板和打开四个宝箱
 */
public class LootDesertTempleTask extends Task {
    /**
     * 沙漠神殿中四个宝箱相对于中心位置的相对坐标
     */
    public final Vec3i[] CHEST_POSITIONS_RELATIVE = {
            new Vec3i(2, 0, 0),
            new Vec3i(-2, 0, 0),
            new Vec3i(0, 0, 2),
            new Vec3i(0, 0, -2)
    };
    /**
     * 沙漠神殿的中心位置（压力板位置）
     */
    private final BlockPos temple;
    /**
     * 需要获取的物品列表
     */
    private final List<Item> wanted;
    /**
     * 当前执行的掠夺任务
     */
    private Task lootTask;
    /**
     * 已经掠夺的宝箱数量
     */
    private short looted = 0;

    /**
     * 构造函数
     *
     * @param temple 沙漠神殿的位置（压力板位置）
     * @param wanted 需要获取的物品列表
     */
    public LootDesertTempleTask(BlockPos temple, List<Item> wanted) {
        this.temple = temple;
        this.wanted = wanted;
    }

    @Override
    protected void onStart() {
        // 将石质压力板添加到避免行走的方块列表中，防止触发陷阱
        AltoClef.getInstance().getClientBaritoneSettings().blocksToAvoid.value.add(Blocks.STONE_PRESSURE_PLATE);
    }

    @Override
    protected Task onTick() {
        if (lootTask != null) {
            if (!lootTask.isFinished()) {
                setDebugState("正在掠夺沙漠神殿宝箱");
                return lootTask;
            }
            looted++;
        }
        // 如果神殿中心有石质压力板，先破坏它
        if (AltoClef.getInstance().getWorld().getBlockState(temple).getBlock() == Blocks.STONE_PRESSURE_PLATE) {
            setDebugState("破坏压力板");
            return new DestroyBlockTask(temple);
        }
        // 如果还有未掠夺的宝箱，继续掠夺
        if (looted < 4) {
            setDebugState("正在掠夺沙漠神殿宝箱");
            lootTask = new LootContainerTask(temple.add(CHEST_POSITIONS_RELATIVE[looted]), wanted);
            return lootTask;
        }
        setDebugState("此任务仍在运行？请报告此问题");
        return null;
    }

    @Override
    protected void onStop(Task task) {
        // 移除石质压力板的避免行走设置
        AltoClef.getInstance().getClientBaritoneSettings().blocksToAvoid.value.remove(Blocks.STONE_PRESSURE_PLATE);
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof LootDesertTempleTask && ((LootDesertTempleTask) other).getTemplePos() == temple;
    }

    @Override
    public boolean isFinished() {
        // 当四个宝箱都被掠夺后，任务完成
        return looted == 4;
    }

    @Override
    protected String toDebugString() {
        return "掠夺沙漠神殿";
    }

    /**
     * 获取沙漠神殿的位置
     *
     * @return 沙漠神殿的位置
     */
    public BlockPos getTemplePos() {
        return temple;
    }
}