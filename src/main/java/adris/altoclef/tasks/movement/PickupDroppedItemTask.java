package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.AbstractDoToClosestObjectTask;
import adris.altoclef.tasks.resources.SatisfyMiningRequirementTask;
import adris.altoclef.tasks.slot.EnsureFreeInventorySlotTask;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.StlHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import net.minecraft.block.*;
import adris.altoclef.multiversion.versionedfields.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 拾取掉落物品任务 - 拾取指定类型的掉落物品
 */
public class PickupDroppedItemTask extends AbstractDoToClosestObjectTask<ItemEntity> implements ITaskRequiresGrounded {
    private static final Task getPickaxeFirstTask = new SatisfyMiningRequirementTask(MiningRequirement.STONE);
    // 不是良好的实践，但我觉得这有助于保持事物自包含。
    private static boolean isGettingPickaxeFirstFlag = false;
    private final TimeoutWanderTask wanderTask = new TimeoutWanderTask(5, true); // 漫步任务
    private final MovementProgressChecker stuckCheck = new MovementProgressChecker(); // 卡住检查器
    private final MovementProgressChecker progressChecker = new MovementProgressChecker(); // 进度检查器
    private final ItemTarget[] itemTargets; // 物品目标数组

    // 这在矿井和沼泽/丛林中经常发生
    private final Set<ItemEntity> _blacklist = new HashSet<>(); // 黑名单
    private final boolean _freeInventoryIfFull; // 库存满时是否清空
    // 烦人的方块列表
    Block[] annoyingBlocks = new Block[]{
            Blocks.VINE, // 藤蔓
            Blocks.NETHER_SPROUTS, // 下界芽
            Blocks.CAVE_VINES, // 洞穴藤蔓
            Blocks.CAVE_VINES_PLANT, // 洞穴藤蔓植物
            Blocks.TWISTING_VINES, // 扭曲藤蔓
            Blocks.TWISTING_VINES_PLANT, // 扭曲藤蔓植物
            Blocks.WEEPING_VINES_PLANT, // 垂泪藤蔓植物
            Blocks.LADDER, // 梯子
            Blocks.BIG_DRIPLEAF, // 大型垂滴叶
            Blocks.BIG_DRIPLEAF_STEM, // 大型垂滴叶茎
            Blocks.SMALL_DRIPLEAF, // 小型垂滴叶
            Blocks.TALL_GRASS, // 高草
            Blocks.SHORT_GRASS // 矮草
    };
    private Task unstuckTask = null; // 解卡任务
    // 我开始后悔没有将其设为单例
    private AltoClef _mod; // AltoClef实例
    private boolean _collectingPickaxeForThisResource = false; // 是否正在为此资源收集镐子
    private ItemEntity _currentDrop = null; // 当前掉落物

    public PickupDroppedItemTask(ItemTarget[] itemTargets, boolean freeInventoryIfFull) {
        this.itemTargets = itemTargets;
        _freeInventoryIfFull = freeInventoryIfFull;
    }

    public PickupDroppedItemTask(ItemTarget target, boolean freeInventoryIfFull) {
        this(new ItemTarget[]{target}, freeInventoryIfFull);
    }

    public PickupDroppedItemTask(Item item, int targetCount, boolean freeInventoryIfFull) {
        this(new ItemTarget(item, targetCount), freeInventoryIfFull);
    }

    public PickupDroppedItemTask(Item item, int targetCount) {
        this(item, targetCount, true);
    }

    /**
     * 生成周围的8个方块位置
     * @param pos 中心位置
     * @return 周围的方块位置数组
     */
    private static BlockPos[] generateSides(BlockPos pos) {
        return new BlockPos[]{
                pos.add(1,0,0),   // 东
                pos.add(-1,0,0),  // 西
                pos.add(0,0,1),   // 南
                pos.add(0,0,-1),  // 北
                pos.add(1,0,-1),  // 东北
                pos.add(1,0,1),   // 东南
                pos.add(-1,0,-1), // 西北
                pos.add(-1,0,1)   // 西南
        };
    }

    /**
     * 检查是否正在拾取第一把镐子
     * @param mod AltoClef实例
     * @return 是否正在拾取第一把镐子
     */
    public static boolean isIsGettingPickaxeFirst(AltoClef mod) {
        return isGettingPickaxeFirstFlag && mod.getModSettings().shouldCollectPickaxeFirst();
    }

    /**
     * 检查指定位置的方块是否令人烦恼（可能导致卡住）
     * @param mod AltoClef实例
     * @param pos 位置
     * @return 是否为令人烦恼的方块
     */
    private boolean isAnnoying(AltoClef mod, BlockPos pos) {
        if (annoyingBlocks != null) {
            for (Block AnnoyingBlocks : annoyingBlocks) {
                return mod.getWorld().getBlockState(pos).getBlock() == AnnoyingBlocks ||
                        mod.getWorld().getBlockState(pos).getBlock() instanceof DoorBlock || // 门
                        mod.getWorld().getBlockState(pos).getBlock() instanceof FenceBlock || // 栅栏
                        mod.getWorld().getBlockState(pos).getBlock() instanceof FenceGateBlock || // 栅栏门
                        mod.getWorld().getBlockState(pos).getBlock() instanceof FlowerBlock; // 花
            }
        }
        return false;
    }

    /**
     * 检查玩家是否卡在烦人的方块中
     * 这在矿井和沼泽/丛林中经常发生
     * @param mod AltoClef实例
     * @return 卡住的方块位置，如果没有则返回null
     */
    private BlockPos stuckInBlock(AltoClef mod) {
        BlockPos p = mod.getPlayer().getBlockPos();
        // 检查玩家脚下和上方的方块
        if (isAnnoying(mod, p)) return p;
        if (isAnnoying(mod, p.up())) return p.up();
        // 检查周围方块
        BlockPos[] toCheck = generateSides(p);
        for (BlockPos check : toCheck) {
            if (isAnnoying(mod, check)) {
                return check;
            }
        }
        // 检查上方一层的周围方块
        BlockPos[] toCheckHigh = generateSides(p.up());
        for (BlockPos check : toCheckHigh) {
            if (isAnnoying(mod, check)) {
                return check;
            }
        }
        return null;
    }

    /**
     * 获取栅栏解卡任务
     * @return 解卡任务
     */
    private Task getFenceUnstuckTask() {
        return new SafeRandomShimmyTask(); // 安全随机摆动任务
    }

    /**
     * 检查是否正在为此资源收集镐子
     * @return 是否正在收集镐子
     */
    public boolean isCollectingPickaxeForThis() {
        return _collectingPickaxeForThisResource;
    }

    @Override
    protected void onStart() {
        wanderTask.reset();
        progressChecker.reset();
        stuckCheck.reset();
    }

    @Override
    protected void onStop(Task interruptTask) {

    }

    @Override
    protected Task onTick() {
        if (wanderTask.isActive() && !wanderTask.isFinished()) {
            setDebugState("漫步中。");
            return wanderTask;
        }
        AltoClef mod = AltoClef.getInstance();

        if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            progressChecker.reset();
        }
        if (unstuckTask != null && unstuckTask.isActive() && !unstuckTask.isFinished() && stuckInBlock(mod) != null) {
            setDebugState("从方块中解卡。");
            stuckCheck.reset();
            // 停止其他任务，我们只进行摆动
            mod.getClientBaritone().getCustomGoalProcess().onLostControl();
            mod.getClientBaritone().getExploreProcess().onLostControl();
            return unstuckTask;
        }
        if (!progressChecker.check(mod) || !stuckCheck.check(mod)) {
            BlockPos blockStuck = stuckInBlock(mod);
            if (blockStuck != null) {
                unstuckTask = getFenceUnstuckTask();
                return unstuckTask;
            }
            stuckCheck.reset();
        }
        _mod = mod;

        // 如果我们正在获取这个资源的镐子...
        if (isIsGettingPickaxeFirst(mod) && _collectingPickaxeForThisResource && !StorageHelper.miningRequirementMetInventory(MiningRequirement.STONE)) {
            progressChecker.reset();
            setDebugState("首先收集镐子");
            return getPickaxeFirstTask;
        } else {
            if (StorageHelper.miningRequirementMetInventory(MiningRequirement.STONE)) {
                isGettingPickaxeFirstFlag = false;
            }
            _collectingPickaxeForThisResource = false;
        }

        if (!progressChecker.check(mod)) {
            mod.getClientBaritone().getPathingBehavior().forceCancel();
            if (_currentDrop != null && !_currentDrop.getStack().isEmpty()) {
                // 我们可能想先获得一个镐子。
                if (!isGettingPickaxeFirstFlag && mod.getModSettings().shouldCollectPickaxeFirst() && !StorageHelper.miningRequirementMetInventory(MiningRequirement.STONE)) {
                    Debug.logMessage("拾取掉落物失败，将尝试先收集一个石镐再试一次！");
                    _collectingPickaxeForThisResource = true;
                    isGettingPickaxeFirstFlag = true;
                    return getPickaxeFirstTask;
                }
                Debug.logMessage(StlHelper.toString(_blacklist, element -> element == null ? "(空)" : element.getStack().getItem().getTranslationKey()));
                Debug.logMessage("拾取掉落物失败，推测无法到达。");
                _blacklist.add(_currentDrop);
                mod.getEntityTracker().requestEntityUnreachable(_currentDrop);
                return wanderTask;
            }
        }

        return super.onTick();
    }


    @Override
    protected boolean isEqual(Task other) {
        // 相同目标物品
        if (other instanceof PickupDroppedItemTask task) {
            return Arrays.equals(task.itemTargets, itemTargets) && task._freeInventoryIfFull == _freeInventoryIfFull;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        StringBuilder result = new StringBuilder();
        result.append("拾取掉落物品: [");
        int c = 0;
        for (ItemTarget target : itemTargets) {
            result.append(target.toString());
            if (++c != itemTargets.length) {
                result.append(", ");
            }
        }
        result.append("]");
        return result.toString();
    }

    @Override
    protected Vec3d getPos(AltoClef mod, ItemEntity obj) {
        if (!obj.isOnGround() && !obj.isTouchingWater()) {
            // 假设我们会从这里向下落一到两个方块。我们可能做得更高级但无所谓。
            BlockPos p = obj.getBlockPos();
            if (!WorldHelper.isSolidBlock(p.down(3))) {
                return obj.getPos().subtract(0, 2, 0);
            }
            return obj.getPos().subtract(0, 1, 0);
        }
        return obj.getPos();
    }

    @Override
    protected Optional<ItemEntity> getClosestTo(AltoClef mod, Vec3d pos) {
        return mod.getEntityTracker().getClosestItemDrop(
                pos,
                itemTargets);
    }

    @Override
    protected Vec3d getOriginPos(AltoClef mod) {
        return mod.getPlayer().getPos();
    }

    @Override
    protected Task getGoalTask(ItemEntity itemEntity) {
        if (!itemEntity.equals(_currentDrop)) {
            _currentDrop = itemEntity;
            progressChecker.reset();
            if (isGettingPickaxeFirstFlag && _collectingPickaxeForThisResource) {
                Debug.logMessage("新目标，不再收集镐子。");
                _collectingPickaxeForThisResource = false;
                isGettingPickaxeFirstFlag = false;
            }
        }
        // 如果我们接近，确保我们的库存是空的
        boolean touching = _mod.getEntityTracker().isCollidingWithPlayer(itemEntity);
        if (touching) {
            if (_freeInventoryIfFull) {
                if (_mod.getItemStorage().getSlotsThatCanFitInPlayerInventory(itemEntity.getStack(), false).isEmpty()) {
                    return new EnsureFreeInventorySlotTask();
                }
            }
        }
        return new GetToEntityTask(itemEntity);
    }

    @Override
    protected boolean isValid(AltoClef mod, ItemEntity obj) {
        return obj.isAlive() && !_blacklist.contains(obj);
    }

}
