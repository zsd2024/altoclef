package adris.altoclef.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.entity.AbstractKillEntityTask;
import adris.altoclef.tasksystem.TaskChain;
import adris.altoclef.tasksystem.TaskRunner;
import baritone.api.BaritoneAPI;
import baritone.api.pathing.calc.IPath;
import baritone.api.pathing.movement.IMovement;
import baritone.pathing.movement.Movement;
import baritone.utils.BlockStateInterface;

import java.util.Optional;

/**
 * 预装备物品链 - 自动预装备适当物品以备后续使用
 * 根据当前任务和路径规划，预先装备可能需要的物品
 */
public class PreEquipItemChain extends SingleTaskChain {


    public PreEquipItemChain(TaskRunner runner) {
        super(runner);
    }

    @Override
    protected void onTaskFinish(AltoClef mod) {
        // 任务完成时无需特殊处理
    }

    @Override
    public float getPriority() {
        update(AltoClef.getInstance());

        // 我们不关心抢占...只是在后台预装备物品
        return -1;
    }

    /**
     * 更新预装备状态
     * @param mod AltoClef实例
     */
    private void update(AltoClef mod) {
        if (mod.getFoodChain().isTryingToEat()) return;

        TaskChain currentChain = mod.getTaskRunner().getCurrentTaskChain();
        if (currentChain == null) return;

        // 我们需要放置或破坏一些方块，不预装备任何东西...
        Optional<IPath> pathOptional = mod.getClientBaritone().getPathingBehavior().getPath();
        if (pathOptional.isEmpty()) return;

        IPath path = pathOptional.get();

        // 真的需要每一刻都创建这个吗？
        BlockStateInterface bsi = new BlockStateInterface(BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext());
        for (IMovement iMovement : path.movements()) {
            Movement movement = (Movement) iMovement;
            if (movement.toBreak(bsi).stream().anyMatch(pos -> mod.getWorld().getBlockState(pos).getBlock().getHardness() > 0)
                    || !movement.toPlace(bsi).isEmpty()) return;
        }

        // 我们*可能*在尝试杀死某些东西，最好装备武器
        if (currentChain.getTasks().stream().anyMatch(task -> task instanceof AbstractKillEntityTask)) {
            AbstractKillEntityTask.equipWeapon(mod);
        }

    }

    @Override
    public String getName() {
        return "预装备物品链";
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
