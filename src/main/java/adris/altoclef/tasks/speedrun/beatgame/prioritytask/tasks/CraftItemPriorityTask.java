package adris.altoclef.tasks.speedrun.beatgame.prioritytask.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.container.CraftInTableTask;
import adris.altoclef.tasks.speedrun.beatgame.BeatMinecraftTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.helpers.CraftingHelper;

import java.util.function.Function;

/**
 * 制作物品优先级任务 - 以恒定优先级制作物品
 */
public class CraftItemPriorityTask extends PriorityTask{

    public final double priority; // 优先级
    public final RecipeTarget recipeTarget; // 配方目标
    private boolean satisfied = false; // 是否已满足

    // 注意：bypassForceCooldown设置为true，因为我们在制作时通常不想等待
    public CraftItemPriorityTask(double priority, RecipeTarget toCraft) {
        this(priority, toCraft, mod -> true);
    }

    public CraftItemPriorityTask(double priority, RecipeTarget toCraft, Function<AltoClef, Boolean> canCall) {
        this(priority, toCraft, canCall, false, true, true);
    }

    public CraftItemPriorityTask(double priority, RecipeTarget toCraft, boolean shouldForce, boolean canCache, boolean bypassForceCooldown) {
        this(priority, toCraft, mod -> true, shouldForce, canCache, bypassForceCooldown);
    }

    public CraftItemPriorityTask(double priority, RecipeTarget toCraft, Function<AltoClef, Boolean> canCall, boolean shouldForce, boolean canCache, boolean bypassForceCooldown) {
        super(canCall, shouldForce, canCache, bypassForceCooldown);
        this.priority = priority;
        this.recipeTarget = toCraft;
    }

    @Override
    public Task getTask(AltoClef mod) {
        if (recipeTarget.getRecipe().isBig()) {
            // 如果是大配方（需要工作台），使用工作台制作任务
            return new CraftInTableTask(recipeTarget);
        }

        return new CraftInInventoryTask(recipeTarget);
    }

    @Override
    public String getDebugString() {
        return "制作 "+recipeTarget;
    }

    @Override
    protected double getPriority(AltoClef mod) {
        if (BeatMinecraftTask.hasItem(mod, recipeTarget.getOutputItem())) {
            Debug.logInternal("这已满足 "+recipeTarget.getOutputItem());
            satisfied = true;
        }
        Debug.logInternal("未满足");

        if (satisfied) return Double.NEGATIVE_INFINITY;

        return priority;
    }


    @Override
    public boolean needCraftingOnStart(AltoClef mod) {
        return CraftingHelper.canCraftItemNow(mod, recipeTarget.getOutputItem());
    }

    /**
     * 检查是否已满足
     * @return 是否已满足
     */
    public boolean isSatisfied() {
        return satisfied;
    }
}

    public CraftItemPriorityTask(double priority, RecipeTarget toCraft, Function<AltoClef, Boolean> canCall) {
        this(priority, toCraft, canCall, false, true, true);
    }

    public CraftItemPriorityTask(double priority, RecipeTarget toCraft, boolean shouldForce, boolean canCache, boolean bypassForceCooldown) {
        this(priority, toCraft, mod -> true, shouldForce, canCache, bypassForceCooldown);
    }

    public CraftItemPriorityTask(double priority, RecipeTarget toCraft, Function<AltoClef, Boolean> canCall, boolean shouldForce, boolean canCache, boolean bypassForceCooldown) {
        super(canCall, shouldForce, canCache, bypassForceCooldown);
        this.priority = priority;
        this.recipeTarget = toCraft;
    }

    @Override
    public Task getTask(AltoClef mod) {
        if (recipeTarget.getRecipe().isBig()) {
            return new CraftInTableTask(recipeTarget);
        }

        return new CraftInInventoryTask(recipeTarget);
    }

    @Override
    public String getDebugString() {
        return "Crafting "+recipeTarget;
    }

    @Override
    protected double getPriority(AltoClef mod) {
        if (BeatMinecraftTask.hasItem(mod, recipeTarget.getOutputItem())) {
            Debug.logInternal("THIS IS SATISFIED "+recipeTarget.getOutputItem());
            satisfied = true;
        }
        Debug.logInternal("NOT SATISFIED");

        if (satisfied) return Double.NEGATIVE_INFINITY;

        return priority;
    }


    @Override
    public boolean needCraftingOnStart(AltoClef mod) {
        return CraftingHelper.canCraftItemNow(mod, recipeTarget.getOutputItem());
    }

    public boolean isSatisfied() {
        return satisfied;
    }
}
