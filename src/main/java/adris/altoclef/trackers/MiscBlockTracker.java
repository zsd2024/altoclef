package adris.altoclef.trackers;

import adris.altoclef.AltoClef;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 杂项方块跟踪器 - 有时我们想跟踪特定的方块相关事物，比如我们使用的最后一个下界传送门。
 * 我不想为了添加这些功能而污染其他跟踪器。
 */
public class MiscBlockTracker {

    private final AltoClef mod;

    private final Map<Dimension, BlockPos> lastNetherPortalsUsed = new HashMap<>(); // 上次使用的下界传送门位置

    // 确保我们只关心我们通过进入的下界传送门
    private Dimension lastDimension; // 上一个维度
    private boolean newDimensionTriggered; // 新维度触发标志

    public MiscBlockTracker(AltoClef mod) {
        this.mod = mod;
    }

    /**
     * 每个游戏刻度执行一次，更新杂项方块跟踪器
     */
    public void tick() {
        if (WorldHelper.getCurrentDimension() != lastDimension) {
            lastDimension = WorldHelper.getCurrentDimension();
            newDimensionTriggered = true;
        }

        if (AltoClef.inGame() && newDimensionTriggered) {
            for (BlockPos check : WorldHelper.scanRegion(mod.getPlayer().getBlockPos().add(-1,-1,-1), mod.getPlayer().getBlockPos().add(1,1,1))) {
                Block currentBlock = mod.getWorld().getBlockState(check).getBlock();
                if (currentBlock == Blocks.NETHER_PORTAL) {
                    // 确保我们获取最低的下界传送门，因为我们只能从底部进入。
                    while (check.getY() > 0) {
                        if (mod.getWorld().getBlockState(check.down()).getBlock() == Blocks.NETHER_PORTAL) {
                            check = check.down();
                        } else {
                            break;
                        }
                    }
                    BlockPos below = check.down();
                    if (WorldHelper.isSolidBlock(below)) {
                        lastNetherPortalsUsed.put(WorldHelper.getCurrentDimension(), check);
                        newDimensionTriggered = false;
                    }
                    break;
                }
            }
        }
    }

    /**
     * 重置杂项方块跟踪器
     */
    public void reset() {
        lastNetherPortalsUsed.clear();
    }

    /**
     * 获取指定维度中最后使用的下界传送门位置
     * @param dimension 维度
     * @return 传送门位置的可选值
     */
    public Optional<BlockPos> getLastUsedNetherPortal(Dimension dimension) {
        if (lastNetherPortalsUsed.containsKey(dimension)) {
            BlockPos portalPos = lastNetherPortalsUsed.get(dimension);
            // 检查我们的下界传送门位置是否无效。
            if (mod.getChunkTracker().isChunkLoaded(portalPos)) {
                if (!mod.getBlockScanner().isBlockAtPosition(portalPos, Blocks.NETHER_PORTAL)) {
                    lastNetherPortalsUsed.remove(dimension);
                    return Optional.empty();
                }
            }
            return Optional.ofNullable(portalPos);
        }
        return Optional.empty();
    }
}
