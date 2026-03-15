package adris.altoclef.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.movement.MLGBucketTask;
import adris.altoclef.tasksystem.ITaskOverridesGrounded;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;

import java.util.Optional;

/**
 * MLG水桶降落链 - 自动处理高空降落时的水桶接水和使用
 * 当玩家掉落时自动使用水桶进行缓冲，防止摔伤
 */
@SuppressWarnings("UnnecessaryLocalVariable")
public class MLGBucketFallChain extends SingleTaskChain implements ITaskOverridesGrounded {

    private final TimerGame tryCollectWaterTimer = new TimerGame(4); // 尝试收集水的计时器
    private final TimerGame pickupRepeatTimer = new TimerGame(0.25); // 拾取重复计时器
    private MLGBucketTask lastMLG = null; // 最后一次MLG任务
    private boolean wasPickingUp = false; // 标记是否正在拾取
    private boolean doingChorusFruit = false; // 标记是否正在使用紫颂果

    public MLGBucketFallChain(TaskRunner runner) {
        super(runner);
    }

    @Override
    protected void onTaskFinish(AltoClef mod) {
        //_lastMLG = null;
    }

    @Override
    public float getPriority() {
        if (!AltoClef.inGame()) return Float.NEGATIVE_INFINITY;

        AltoClef mod = AltoClef.getInstance();

        if (isFalling(mod)) {
            tryCollectWaterTimer.reset();
            setTask(new MLGBucketTask());
            lastMLG = (MLGBucketTask) mainTask;
            return 100;
        } else if (!tryCollectWaterTimer.elapsed()) { // 为什么是-0.5？因为比-0.7慢
            // 我们刚放置了水，尝试收集它
            if (mod.getItemStorage().hasItem(Items.BUCKET) && !mod.getItemStorage().hasItem(Items.WATER_BUCKET)) {
                if (lastMLG != null) {
                    BlockPos placed = lastMLG.getWaterPlacedPos();
                    boolean isPlacedWater;
                    try {
                        isPlacedWater = mod.getWorld().getBlockState(placed).getBlock() == Blocks.WATER;
                    } catch (Exception e) {
                        isPlacedWater = false;
                    }
                    //Debug.logInternal("PLACED: " + placed);
                    if (placed != null && placed.isWithinDistance(mod.getPlayer().getPos(), 5.5) && isPlacedWater) {
                        BlockPos toInteract = placed;
                        // 允许观察流体
                        mod.getBehaviour().push();
                        mod.getBehaviour().setRayTracingFluidHandling(RaycastContext.FluidHandling.SOURCE_ONLY);
                        Optional<Rotation> reach = LookHelper.getReach(toInteract, Direction.UP);
                        if (reach.isPresent()) {
                            mod.getClientBaritone().getLookBehavior().updateTarget(reach.get(), true);
                            if (mod.getClientBaritone().getPlayerContext().isLookingAt(toInteract)) {
                                if (mod.getSlotHandler().forceEquipItem(Items.BUCKET)) {
                                    if (pickupRepeatTimer.elapsed()) {
                                        // 拾取
                                        pickupRepeatTimer.reset();
                                        mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                                        wasPickingUp = true;
                                    } else if (wasPickingUp) {
                                        // 停止拾取，等待并重试
                                        wasPickingUp = false;
                                    }
                                }
                            }
                        } else {
                            // 如果所有方法都失败了，尝试正常使用方式收集水
                            setTask(TaskCatalogue.getItemTask(Items.WATER_BUCKET, 1));
                        }
                        mod.getBehaviour().pop();
                        return 60;
                    }
                }
            }
        }
        if (wasPickingUp) {
            wasPickingUp = false;
            lastMLG = null;
        }
        if (mod.getPlayer().hasStatusEffect(StatusEffects.LEVITATION) &&
                !mod.getPlayer().getItemCooldownManager().isCoolingDown(Items.CHORUS_FRUIT) &&
                mod.getPlayer().getActiveStatusEffects().get(StatusEffects.LEVITATION).getDuration() <= 70 &&
                mod.getItemStorage().hasItemInventoryOnly(Items.CHORUS_FRUIT) &&
                !mod.getItemStorage().hasItemInventoryOnly(Items.WATER_BUCKET)) {
            doingChorusFruit = true;
            mod.getSlotHandler().forceEquipItem(Items.CHORUS_FRUIT);
            mod.getInputControls().hold(Input.CLICK_RIGHT);
            mod.getExtraBaritoneSettings().setInteractionPaused(true);
        } else if (doingChorusFruit) {
            doingChorusFruit = false;
            mod.getInputControls().release(Input.CLICK_RIGHT);
            mod.getExtraBaritoneSettings().setInteractionPaused(false);
        }
        lastMLG = null;
        return Float.NEGATIVE_INFINITY;
    }

    @Override
    public String getName() {
        return "MLG水桶降落链";
    }

    @Override
    public boolean isActive() {
        // 我们始终在检查MLG
        return true;
    }

    /**
     * 检查是否已完成MLG
     * @return 如果已完成MLG则返回true
     */
    public boolean doneMLG() {
        return lastMLG == null;
    }

    /**
     * 检查是否正在使用紫颂果
     * @return 如果正在使用紫颂果则返回true
     */
    public boolean isChorusFruiting() {
        return doingChorusFruit;
    }

    /**
     * 检查玩家是否正在掉落
     * @param mod AltoClef实例
     * @return 如果正在掉落则返回true
     */
    public boolean isFalling(AltoClef mod) {
        if (!mod.getModSettings().shouldAutoMLGBucket()) {
            return false;
        }
        if (mod.getPlayer().isSwimming() || mod.getPlayer().isTouchingWater() || mod.getPlayer().isOnGround() || mod.getPlayer().isClimbing()) {
            // 我们着地了
            return false;
        }
        double ySpeed = mod.getPlayer().getVelocity().y;
        return ySpeed < -0.7;
    }
}
