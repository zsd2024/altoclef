package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.movement.GetToEntityTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasks.resources.KillAndLootTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SlimeEntity;

import java.util.Optional;

/**
 * 英雄任务 - 自动寻找并消灭敌对生物，同时收集掉落物和经验球
 */
public class HeroTask extends Task {
    @Override
    protected void onStart() {
        // 启动时无需特殊处理
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        // 如果需要进食，优先处理
        if (mod.getFoodChain().needsToEat()) {
            setDebugState("先吃点东西。");
            return null;
        }
        // 查找最近的经验球
        Optional<Entity> experienceOrb = mod.getEntityTracker().getClosestEntity(ExperienceOrbEntity.class);
        if (experienceOrb.isPresent()) {
            setDebugState("获取经验。");
            return new GetToEntityTask(experienceOrb.get());
        }
        assert MinecraftClient.getInstance().world != null;
        // 获取所有实体
        Iterable<Entity> hostiles = MinecraftClient.getInstance().world.getEntities();
        if (hostiles != null) {
            for (Entity hostile : hostiles) {
                // 检查是否为敌对生物或史莱姆
                if (hostile instanceof HostileEntity || hostile instanceof SlimeEntity) {
                    Optional<Entity> closestHostile = mod.getEntityTracker().getClosestEntity(hostile.getClass());
                    if (closestHostile.isPresent()) {
                        setDebugState("消灭敌对生物或拾取敌对生物掉落物。");
                        return new KillAndLootTask(hostile.getClass(), new ItemTarget(ItemHelper.HOSTILE_MOB_DROPS));
                    }
                }
            }
        }
        // 检查是否有敌对生物的掉落物
        if (mod.getEntityTracker().itemDropped(ItemHelper.HOSTILE_MOB_DROPS)) {
            setDebugState("拾取敌对生物掉落物。");
            return new PickupDroppedItemTask(new ItemTarget(ItemHelper.HOSTILE_MOB_DROPS), true);
        }
        setDebugState("搜索敌对生物。");
        return new TimeoutWanderTask();
    }

    @Override
    protected void onStop(Task interruptTask) {
        // 停止时无需特殊处理
    }

    @Override
    protected boolean isEqual(Task other) {
        // 所有HeroTask实例都被视为相等
        return other instanceof HeroTask;
    }

    @Override
    protected String toDebugString() {
        return "消灭所有敌对生物。";
    }
}
