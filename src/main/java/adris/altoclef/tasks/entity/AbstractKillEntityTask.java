package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

import java.util.List;

/**
 * 攻击实体的任务，但必须指定目标实体。
 */
public abstract class AbstractKillEntityTask extends AbstractDoToEntityTask {
    // 其他实体力场范围
    private static final double OTHER_FORCE_FIELD_RANGE = 2;

    // 不是"攻击"距离，而是"我们足够接近，降低对其他生物的戒备，专注于这个实体"的范围。
    private static final double CONSIDER_COMBAT_RANGE = 10;

    /**
     * 构造函数，使用默认战斗范围和力场半径
     */
    protected AbstractKillEntityTask() {
        this(CONSIDER_COMBAT_RANGE, OTHER_FORCE_FIELD_RANGE);
    }

    /**
     * 构造函数，指定战斗守卫降低范围和战斗守卫降低力场半径
     * @param combatGuardLowerRange 战斗守卫降低范围
     * @param combatGuardLowerFieldRadius 战斗守卫降低力场半径
     */
    protected AbstractKillEntityTask(double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
        super(combatGuardLowerRange, combatGuardLowerFieldRadius);
    }

    /**
     * 构造函数，指定保持距离、战斗守卫降低范围和战斗守卫降低力场半径
     * @param maintainDistance 保持距离
     * @param combatGuardLowerRange 战斗守卫降低范围
     * @param combatGuardLowerFieldRadius 战斗守卫降低力场半径
     */
    protected AbstractKillEntityTask(double maintainDistance, double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
        super(maintainDistance, combatGuardLowerRange, combatGuardLowerFieldRadius);
    }

    /**
     * 获取最佳武器
     * @param mod AltoClef实例
     * @return 返回最佳武器
     */
    public static Item bestWeapon(AltoClef mod) {
        List<ItemStack> invStacks = mod.getItemStorage().getItemStacksPlayerInventory(true);

        Item bestItem = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot()).getItem();
        float bestDamage = Float.NEGATIVE_INFINITY;

        if (bestItem instanceof SwordItem handToolItem) {
            bestDamage = handToolItem.getMaterial().getAttackDamage();
        }

        for (ItemStack invStack : invStacks) {
            if (!(invStack.getItem() instanceof SwordItem item)) continue;

            float itemDamage = item.getMaterial().getAttackDamage();

            if (itemDamage > bestDamage) {
                bestItem = item;
                bestDamage = itemDamage;
            }
        }

        return bestItem;
    }

    /**
     * 装备武器
     * @param mod AltoClef实例
     * @return 如果更换了武器则返回true
     */
    public static boolean equipWeapon(AltoClef mod) {
        Item bestWeapon = bestWeapon(mod);
        Item equipedWeapon = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot()).getItem();
        if (bestWeapon != null && bestWeapon != equipedWeapon) {
            mod.getSlotHandler().forceEquipItem(bestWeapon);
            return true;
        }
        return false;
    }

    @Override
    protected Task onEntityInteract(AltoClef mod, Entity entity) {
        // 装备武器
        if (!equipWeapon(mod)) {
            float hitProg = mod.getPlayer().getAttackCooldownProgress(0);
            if (hitProg >= 1 && (mod.getPlayer().isOnGround() || mod.getPlayer().getVelocity().getY() < 0 || mod.getPlayer().isTouchingWater())) {
                LookHelper.lookAt(mod, entity.getEyePos());
                mod.getControllerExtras().attack(entity);
            }
        }
        return null;
    }
}