package adris.altoclef.tasks.construction;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.multiversion.versionedfields.Items;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import baritone.api.schematic.AbstractSchematic;
import baritone.api.schematic.ISchematic;
import baritone.api.utils.BlockOptionalMeta;
import baritone.api.utils.input.Input;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 在指定位置放置方块的任务
 */
public class PlaceBlockTask extends Task implements ITaskRequiresGrounded {

    // 最少需要的材料数量
    private static final int MIN_MATERIALS = 1;
    // 推荐的材料数量
    private static final int PREFERRED_MATERIALS = 32;
    // 目标放置位置
    private final BlockPos target;
    // 要放置的方块数组
    private final Block[] toPlace;
    // 是否使用可丢弃物品
    private final boolean useThrowaways;
    // 是否自动收集结构方块
    private final boolean autoCollectStructureBlocks;
    // 移动进度检查器
    private final MovementProgressChecker progressChecker = new MovementProgressChecker();
    // 超时徘徊任务（避免永久卡住）
    private final TimeoutWanderTask wanderTask = new TimeoutWanderTask(5);
    // 材料获取任务
    private Task materialTask;
    // 失败计数
    private int failCount = 0;

    /**
     * 构造函数
     * @param target 目标放置位置
     * @param toPlace 要放置的方块数组
     * @param useThrowaways 是否使用可丢弃物品
     * @param autoCollectStructureBlocks 是否自动收集结构方块
     */
    public PlaceBlockTask(BlockPos target, Block[] toPlace, boolean useThrowaways, boolean autoCollectStructureBlocks) {
        this.target = target;
        this.toPlace = toPlace;
        this.useThrowaways = useThrowaways;
        this.autoCollectStructureBlocks = autoCollectStructureBlocks;
    }

    /**
     * 构造函数（简化版本，不使用可丢弃物品）
     * @param target 目标放置位置
     * @param toPlace 要放置的方块
     */
    public PlaceBlockTask(BlockPos target, Block... toPlace) {
        this(target, toPlace, false, false);
    }

    /**
     * 获取可用材料数量
     * @param mod AltoClef主模块实例
     * @return 可用材料的总数
     */
    public int getMaterialCount(AltoClef mod) {
        int count = mod.getItemStorage().getItemCount(ItemHelper.blocksToItems(toPlace));

        if (useThrowaways) {
            // 如果允许使用可丢弃物品，添加可丢弃物品的数量
            count += mod.getItemStorage().getItemCount(mod.getClientBaritoneSettings().acceptableThrowawayItems.value.toArray(new Item[0]));
        }
        return count;
    }

    /**
     * 获取材料任务
     * @param count 需要的材料数量
     * @return 获取材料的任务
     */
    public static Task getMaterialTask(int count) {
        return TaskCatalogue.getSquashedItemTask(new ItemTarget(Items.DIRT, count), new ItemTarget(Items.COBBLESTONE,
                count), new ItemTarget(Items.NETHERRACK, count), new ItemTarget(Items.COBBLED_DEEPSLATE, count));
    }

    @Override
    protected void onStart() {
        progressChecker.reset();
        // 如果被其他任务中断，这可能会导致问题...
        //_wanderTask.resetWander();
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 检查是否在下界传送门中
        if (WorldHelper.isInNetherPortal()) {
            if (!mod.getClientBaritone().getPathingBehavior().isPathing()) {
                setDebugState("正在从下界传送门中退出");
                mod.getInputControls().hold(Input.SNEAK);
                mod.getInputControls().hold(Input.MOVE_FORWARD);
                return null;
            } else {
                mod.getInputControls().release(Input.SNEAK);
                mod.getInputControls().release(Input.MOVE_BACK);
                mod.getInputControls().release(Input.MOVE_FORWARD);
            }
        } else {
            if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
                mod.getInputControls().release(Input.SNEAK);
                mod.getInputControls().release(Input.MOVE_BACK);
                mod.getInputControls().release(Input.MOVE_FORWARD);
            }
        }
        // 执行超时徘徊
        if (wanderTask.isActive() && !wanderTask.isFinished()) {
            setDebugState("徘徊中。");
            progressChecker.reset();
            return wanderTask;
        }

        if (autoCollectStructureBlocks) {
            if (materialTask != null && materialTask.isActive() && !materialTask.isFinished()) {
                setDebugState("没有结构方块，收集圆石+泥土作为默认方块。");
                if (getMaterialCount(mod) < PREFERRED_MATERIALS) {
                    return materialTask;
                } else {
                    materialTask = null;
                }
            }

            //Item[] items = Util.toArray(Item.class, mod.getClientBaritoneSettings().acceptableThrowawayItems.value);
            if (getMaterialCount(mod) < MIN_MATERIALS) {
                // TODO: 挖掘物品，以某种方式提取其资源密钥。
                materialTask = getMaterialTask(PREFERRED_MATERIALS);
                progressChecker.reset();
                return materialTask;
            }
        }


        // 检查我们是否正在接近目标点。如果失败，稍微徘徊一下。
        if (!progressChecker.check(mod)) {
            failCount++;
            if (!tryingAlternativeWay()) {
                Debug.logMessage("放置失败，超时徘徊。");
                return wanderTask;
            } else {
                Debug.logMessage("尝试替代方法放置方块...");
            }
        }


        // 放置方块
        if (tryingAlternativeWay()) {
            setDebugState("替代方法：尝试到目标方块上方放置方块。");
            return new GetToBlockTask(target.up(), false);
        } else {
            setDebugState("让Baritone放置一个方块。");
            // 执行Baritone放置
            if (!mod.getClientBaritone().getBuilderProcess().isActive()) {
                Debug.logInternal("运行结构构建");
                ISchematic schematic = new PlaceStructureSchematic(mod);
                mod.getClientBaritone().getBuilderProcess().build("structure", schematic, target);
            }
        }
        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        AltoClef.getInstance().getClientBaritone().getBuilderProcess().onLostControl();
    }

    //TODO: 在叶子方块位置放置结构???? 如果不是空/空气/水，可能需要先删除方块。

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof PlaceBlockTask task) {
            // 比较目标位置、是否使用可丢弃物品和要放置的方块数组
            return task.target.equals(target) && task.useThrowaways == useThrowaways && Arrays.equals(task.toPlace, toPlace);
        }
        return false;
    }

    @Override
    public boolean isFinished() {
        assert MinecraftClient.getInstance().world != null;
        if (useThrowaways) {
            // 如果使用可丢弃物品，只要目标位置是固体方块就算完成
            return WorldHelper.isSolidBlock(target);
        }
        // 检查目标位置的方块是否在可接受的方块列表中
        BlockState state = AltoClef.getInstance().getWorld().getBlockState(target);
        return ArrayUtils.contains(toPlace, state.getBlock());
    }

    @Override
    protected String toDebugString() {
        return "在 " + target.toShortString() + " 处放置结构" + ArrayUtils.toString(toPlace);
    }

    /**
     * 检查是否尝试替代方法（根据失败次数判断）
     * @return 是否尝试替代方法
     */
    private boolean tryingAlternativeWay() {
        return failCount % 4 == 3;
    }

    /**
     * 放置结构的原理图类，用于Baritone构建过程
     */
    private class PlaceStructureSchematic extends AbstractSchematic {

        private final AltoClef _mod;

        public PlaceStructureSchematic(AltoClef mod) {
            super(1, 1, 1);
            _mod = mod;
        }

        @Override
        public BlockState desiredState(int x, int y, int z, BlockState blockState, List<BlockState> available) {
            if (x == 0 && y == 0 && z == 0) {
                // 放置目标方块！
                if (!available.isEmpty()) {
                    for (BlockState possible : available) {
                        if (possible == null) continue;
                        // 如果允许使用可丢弃物品且当前方块是可丢弃的，则使用它
                        if (useThrowaways && _mod.getClientBaritoneSettings().acceptableThrowawayItems.value.contains(possible.getBlock().asItem())) {
                            return possible;
                        }
                        // 如果当前方块在指定的可放置方块列表中，则使用它
                        if (Arrays.asList(toPlace).contains(possible.getBlock())) {
                            return possible;
                        }
                    }
                }
                Debug.logInternal("找不到可丢弃的方块");
                // 没有可丢弃的方块可用！
                return new BlockOptionalMeta(Blocks.COBBLESTONE).getAnyBlockState();
            }
            // 不关心其他位置
            return blockState;
        }
    }
}
