package adris.altoclef.tasks.speedrun.beatgame.prioritytask.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.util.RecipeTarget;

import java.util.function.Function;

/**
 * 可重制物品优先级任务 - 可以重新制作的物品的优先级任务
 */
public class RecraftableItemPriorityTask extends CraftItemPriorityTask{


    private final double recraftPriority; // 重制优先级

    public RecraftableItemPriorityTask(double priority, double recraftPriority, RecipeTarget toCraft, Function<AltoClef, Boolean> canCall ) {
        super(priority, toCraft, canCall);
        this.recraftPriority = recraftPriority;
    }


    @Override
    protected double getPriority(AltoClef mod) {
        if (isSatisfied()) return recraftPriority; // 如果已满足，返回重制优先级

        return super.getPriority(mod); // 否则返回常规优先级
    }
}
