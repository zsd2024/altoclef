package adris.altoclef.tasksystem;

import adris.altoclef.AltoClef;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * 某些任务如果在空中被中断可能会导致严重问题。
 * 例如，如果我们在进行跑酷时Baritone任务被停止，
 * 玩家会掉落到下方的任何地方，可能导致死亡。
 */
public interface ITaskRequiresGrounded extends ITaskCanForce {
    @Override
    default boolean shouldForce(Task interruptingCandidate) {
        if (interruptingCandidate instanceof ITaskOverridesGrounded)
            return false;

        ClientPlayerEntity player = AltoClef.getInstance().getPlayer();
        return !(player.isOnGround() || player.isSwimming() || player.isTouchingWater() || player.isClimbing());
    }
}
