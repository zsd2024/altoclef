package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.container.CraftInTableTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.RecipeTarget;
import net.minecraft.item.Item;

/**
 * 使用匹配材料制作任务抽象类
 * 用于处理需要使用统一材料进行合成的任务（如床、地毯等需要统一颜色的物品）
 */
public abstract class CraftWithMatchingMaterialsTask extends ResourceTask {

    // 目标物品
    private final ItemTarget target;
    // 合成配方
    private final CraftingRecipe recipe;
    // 同种材料掩码，标记配方中哪些位置需要使用相同材料
    private final boolean[] sameMask;

    // 同种资源目标
    private final ItemTarget sameResourceTarget;
    private final int sameResourceRequiredCount;
    private final int sameResourcePerRecipe;

    public CraftWithMatchingMaterialsTask(ItemTarget target, CraftingRecipe recipe, boolean[] sameMask) {
        super(target);
        this.target = target;
        this.recipe = recipe;
        this.sameMask = sameMask;
        int sameResourceRequiredCount = 0;
        ItemTarget sameResourceTarget = null;
        if (recipe.getSlotCount() != sameMask.length) {
            Debug.logError("CraftWithMatchingMaterialsTask 构造函数参数无效：配方大小必须等于 \"sameMask\" 大小。");
        }
        // 遍历配方中的每个位置，找出需要使用相同材料的位置
        for (int i = 0; i < recipe.getSlotCount(); ++i) {
            if (sameMask[i]) {
                sameResourceRequiredCount++;
                sameResourceTarget = recipe.getSlot(i);
            }
        }
        this.sameResourceTarget = sameResourceTarget;

        // 计算需要多少次合成操作
        // cant this be just replaced with `Math.ceil((double) target.getTargetCount() / recipe.outputCount())` ?
        int craftsNeeded = (int) (1 + Math.floor((double) target.getTargetCount() / recipe.outputCount() - 0.001));
        sameResourcePerRecipe = sameResourceRequiredCount;
        this.sameResourceRequiredCount = sameResourceRequiredCount * craftsNeeded;
    }

    /**
     * 生成使用相同材料的合成配方
     * @param diverseRecipe 原始配方
     * @param sameItem 相同的材料
     * @param sameMask 相同材料掩码
     * @return 新的配方
     */
    private static CraftingRecipe generateSameRecipe(CraftingRecipe diverseRecipe, Item sameItem, boolean[] sameMask) {
        ItemTarget[] result = new ItemTarget[diverseRecipe.getSlotCount()];
        for (int i = 0; i < result.length; ++i) {
            if (sameMask[i]) {
                // 使用相同材料
                result[i] = new ItemTarget(sameItem, 1);
            } else {
                // 保持原有材料
                result[i] = diverseRecipe.getSlot(i);
            }
        }
        return CraftingRecipe.newShapedRecipe(result, diverseRecipe.outputCount());
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        // 任务开始时的初始化
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {

        /*
         * 0) 确定"相同资源"物品目标
         * 1) 获取"相同资源"物品匹配数组
         * 2) 计算需要多少"相同资源"
         * 3) 获取该材料出现频率最高的类型
         * 4) 如果最高频率类型不满足要求，返回TaskCatalogue.getItemTask("相同资源"物品目标)
         * 5) 如果最高频率类型满足要求，使用自定义配方运行CraftInTable，该配方仅使用频率最高的材料。
         */

        // 对于每种"相同"物品：我们可以用它制作多少物品？
        // 例如，如果我们有7个红色羊毛，我们可以制作2张床
        // sameFullCraftsPermitted[Items.RED_WOOL] = 2;
        int canCraftTotal = 0;
        int majorityCraftCount = 0;
        Item majorityCraftItem = null;
        // 遍历所有可能的相同资源类型，计算每种类型能制作多少物品
        for (Item sameCheck : sameResourceTarget.getMatches()) {
            int count = getExpectedTotalCountOfSameItem(mod, sameCheck);
            int canCraft = (count / sameResourcePerRecipe) * recipe.outputCount();
            canCraftTotal += canCraft;
            if (canCraft > majorityCraftCount) {
                majorityCraftCount = canCraft;
                majorityCraftItem = sameCheck;
            }
        }

        // 如果我们已经有一些目标物品，我们需要更少的"相同"材料
        int currentTargetCount = mod.getItemStorage().getItemCount(target);
        int currentTargetsRequired = target.getTargetCount() - currentTargetCount;

        if (canCraftTotal >= currentTargetsRequired) {
            // 我们有足够的相同资源！！！
            // 正常处理合成

            // 我们可能需要将原材料转换为"匹配"材料
            int trueCanCraftTotal = 0;
            for (Item sameCheck : sameResourceTarget.getMatches()) {
                int trueCount = mod.getItemStorage().getItemCount(sameCheck);
                int trueCanCraft = (trueCount / sameResourcePerRecipe) * recipe.outputCount();
                trueCanCraftTotal += trueCanCraft;
            }
            if (trueCanCraftTotal < currentTargetsRequired) {
                // 获取特定相同资源任务
                return getSpecificSameResourceTask(mod, sameResourceTarget.getMatches());
            }

            // 生成使用主要材料的合成配方
            CraftingRecipe sameRecipe = generateSameRecipe(recipe, majorityCraftItem, sameMask);
            int toCraftTotal = majorityCraftCount;
            toCraftTotal = Math.min(toCraftTotal, target.getTargetCount());
            // 获取与主要材料相对应的输出物品
            Item output = getSpecificItemCorrespondingToMajorityResource(majorityCraftItem);

            toCraftTotal = Math.min(target.getTargetCount(),toCraftTotal+mod.getItemStorage().getItemCount(output));

            RecipeTarget recipeTarget = new RecipeTarget(output, toCraftTotal, sameRecipe);
            // 根据配方是否需要大工作台来选择合适的合成任务
            return recipe.isBig() ? new CraftInTableTask(recipeTarget) : new CraftInInventoryTask(recipeTarget);
        }
        // 首先收集相同的资源！！！
        return getAllSameResourcesTask(mod);
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        // 任务停止时的清理
    }


    protected Task getAllSameResourcesTask(AltoClef mod) {
        // 创建一个需要大量相同资源的目标（999999表示需要很多）
        ItemTarget infinityVersion = new ItemTarget(sameResourceTarget, 999999);
        return TaskCatalogue.getItemTask(infinityVersion);
    }

    // 虚方法
    protected int getExpectedTotalCountOfSameItem(AltoClef mod, Item sameItem) {
        return mod.getItemStorage().getItemCount(sameItem);
    }

    // 虚方法
    // 如果重写了'getExpectedTotalCountOfSameItem'，则应实现此方法
    protected Task getSpecificSameResourceTask(AltoClef mod, Item[] toGet) {
        Debug.logError("啊哦！！！ getSpecificSameResourceTask 应该被实现！！！ 现在我们卡住了。");
        return null;
    }

    protected abstract Item getSpecificItemCorrespondingToMajorityResource(Item majority);
}
