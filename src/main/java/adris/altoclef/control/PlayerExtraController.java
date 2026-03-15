package adris.altoclef.control;

import adris.altoclef.AltoClef;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.BlockBreakingCancelEvent;
import adris.altoclef.eventbus.events.BlockBreakingEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/**
 * 玩家额外控制器
 * 负责处理玩家的额外控制操作，如挖掘方块、攻击实体等
 */
public class PlayerExtraController {

    private final AltoClef mod;
    // 正在挖掘的方块位置
    private BlockPos blockBreakPos;
    // 挖掘进度
    private double blockBreakProgress;

        /**
     * 构造函数，初始化玩家额外控制器
     * @param mod AltoClef主模块实例
     */
    public PlayerExtraController(AltoClef mod) {
        this.mod = mod;

        EventBus.subscribe(BlockBreakingEvent.class, evt -> onBlockBreak(evt.blockPos, evt.progress));
        EventBus.subscribe(BlockBreakingCancelEvent.class, evt -> onBlockStopBreaking());
    }

        /**
     * 处理方块挖掘事件
     * @param pos 挖掘的方块位置
     * @param progress 挖掘进度
     */
    private void onBlockBreak(BlockPos pos, double progress) {
        blockBreakPos = pos;
        blockBreakProgress = progress;
    }

        /**
     * 处理停止挖掘方块事件
     */
    private void onBlockStopBreaking() {
        blockBreakPos = null;
        blockBreakProgress = 0;
    }

        /**
     * 获取正在挖掘的方块位置
     * @return 正在挖掘的方块位置，如果未在挖掘则返回null
     */
    public BlockPos getBreakingBlockPos() {
        return blockBreakPos;
    }

        /**
     * 检查是否正在挖掘方块
     * @return 如果正在挖掘方块返回true，否则返回false
     */
    public boolean isBreakingBlock() {
        return blockBreakPos != null;
    }

        /**
     * 获取当前挖掘方块的进度
     * @return 挖掘进度值，范围从0.0到1.0
     */
    public double getBreakingBlockProgress() {
        return blockBreakProgress;
    }

        /**
     * 检查实体是否在玩家的攻击范围内
     * @param entity 要检查的实体
     * @return 如果实体在范围内返回true，否则返回false
     */
    public boolean inRange(Entity entity) {
        return mod.getPlayer().isInRange(entity, mod.getModSettings().getEntityReachRange());
    }

        /**
     * 攻击指定实体
     * @param entity 要攻击的实体
     */
    public void attack(Entity entity) {
        if (inRange(entity)) {
            mod.getController().attackEntity(mod.getPlayer(), entity);
            mod.getPlayer().swingHand(Hand.MAIN_HAND);
        }
    }
}
