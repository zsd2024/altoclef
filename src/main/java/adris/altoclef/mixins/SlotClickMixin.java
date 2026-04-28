package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.SlotClickChangedEvent;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 槽位点击混入
 * 用于监控和跟踪物品槽位的变化事件
 */
@Mixin(ScreenHandler.class)
public abstract class SlotClickMixin {

    //#if MC >= 11701
    /**
     * 重定向槽位点击处理方法
     * 在槽位发生变化时记录变化前后的物品状态，并发布事件
     * @param self ScreenHandler 实例
     * @param slotIndex 被点击的槽位索引
     * @param button 点击的按钮类型
     * @param actionType 操作类型
     * @param player 玩家实体
     */
    @Redirect(
            method = "internalOnSlotClick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;internalOnSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V")
    )
    private void slotClick(ScreenHandler self, int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        // TODO: "self" is misleading, reread Mixin docs to understand the implications here.

        // 记录槽位变化前的状态
        List<Slot> afterSlots = self.slots;
        List<ItemStack> beforeStacks = new ArrayList<>(afterSlots.size());
        for (Slot slot : afterSlots) {
            beforeStacks.add(slot.getStack().copy());
        }
        // 执行槽位点击操作，可能会改变槽位内容
        self.onSlotClick(slotIndex, button, actionType, player);
        // 检查槽位变化并发布事件
        for (int i = 0; i < beforeStacks.size(); ++i) {
            ItemStack before = beforeStacks.get(i);
            ItemStack after = afterSlots.get(i).getStack();
            if (!ItemStack.areEqual(before, after)) {
                adris.altoclef.util.slots.Slot slot = adris.altoclef.util.slots.Slot.getFromCurrentScreen(i);
                EventBus.publish(new SlotClickChangedEvent(slot, before, after));
            }
        }
    }
    //#endif

}
