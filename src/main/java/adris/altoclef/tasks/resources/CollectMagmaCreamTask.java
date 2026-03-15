package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.item.Items;

/**
 * 收集岩浆膏任务
 * 用于收集岩浆膏，需要击杀岩浆立方怪或合成（烈焰粉+粘液球）
 */
public class CollectMagmaCreamTask extends ResourceTask {
    private final int count; // 目标岩浆膏数量

    public CollectMagmaCreamTask(int count) {
        super(Items.MAGMA_CREAM, count);
        this.count = count;
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        // 任务开始时的初始化
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        /*
         * 如果在下界:
         *      如果找到岩浆立方怪，猎杀岩浆立方怪
         *      如果烈焰粉潜力不足，猎杀烈焰人
         * 如果在主世界:
         *      如果粘液球潜力不足，猎杀史莱姆
         *      否则，前往下界
         * 如果在末地:
         *      前往主世界
         */
        int currentCream = mod.getItemStorage().getItemCount(Items.MAGMA_CREAM);
        int neededCream = count - currentCream;
        switch (WorldHelper.getCurrentDimension()) {
            case NETHER -> {
                // 如果找到了岩浆立方怪，击杀它
                if (mod.getEntityTracker().entityFound(MagmaCubeEntity.class)) {
                    setDebugState("击杀岩浆立方怪");
                    return new KillAndLootTask(MagmaCubeEntity.class, new ItemTarget(Items.MAGMA_CREAM));
                }
                // 计算烈焰粉的潜力（包括烈焰棒）
                int currentBlazePowderPotential = mod.getItemStorage().getItemCount(Items.BLAZE_POWDER) + mod.getItemStorage().getItemCount(Items.BLAZE_ROD);
                if (neededCream > currentBlazePowderPotential) {
                    // 没有找到岩浆立方怪，击杀烈焰人以获取烈焰粉
                    setDebugState("获取烈焰粉");
                    return TaskCatalogue.getItemTask(Items.BLAZE_POWDER, neededCream - currentCream);
                }
                setDebugState("返回主世界击杀史莱姆，我们有足够的烈焰粉且附近没有岩浆立方怪。");
                return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
            }
            case OVERWORLD -> {
                // 检查粘液球数量
                int currentSlime = mod.getItemStorage().getItemCount(Items.SLIME_BALL);
                if (neededCream > currentSlime) {
                    setDebugState("获取粘液球");
                    return TaskCatalogue.getItemTask(Items.SLIME_BALL, neededCream - currentCream);
                }
                setDebugState("前往下界获取烈焰粉和/或击杀岩浆立方怪");
                return new DefaultGoToDimensionTask(Dimension.NETHER);
            }
            case END -> {
                setDebugState("前往主世界，此地不存在岩浆膏材料。");
                return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
            }
        }

        setDebugState("无效维度??: " + WorldHelper.getCurrentDimension());
        return null;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务结束时的清理
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectMagmaCreamTask;
    }

    @Override
    protected String toDebugStringName() {
        return "收集 " + count + " 个岩浆膏。";
    }
}
