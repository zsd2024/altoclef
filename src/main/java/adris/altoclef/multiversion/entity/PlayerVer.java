package adris.altoclef.multiversion.entity;

import adris.altoclef.multiversion.Pattern;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

/**
 * 玩家版本适配器
 * 提供跨 Minecraft 版本的玩家实体操作兼容层
 */
public class PlayerVer {


    /**
     * 发送聊天消息
     * 
     * @param player 玩家实体
     * @param content 消息内容
     */
    public static void sendChatMessage(ClientPlayerEntity player,String content) {
        //#if MC >= 11904
        player.networkHandler.sendChatMessage(content);
        //#else
        //$$ player.sendChatMessage(content);
        //#endif
    }

    /**
     * 发送聊天命令
     * 
     * @param player 玩家实体
     * @param content 命令内容（不包含前缀斜杠）
     */
    public static void sendChatCommand(ClientPlayerEntity player,String content) {
        //#if MC >= 11904
        player.networkHandler.sendChatCommand(content);
        //#else
        //$$ player.sendChatMessage("/"+content);
        //#endif
    }

    /**
     * 获取玩家光标上的物品堆栈
     * 
     * @param player 玩家实体
     * @return 光标上的物品堆栈
     */
    @Pattern
    private static ItemStack getCursorStack(PlayerEntity player) {
        //#if MC >= 11701
        return player.currentScreenHandler.getCursorStack();
        //#else
        //$$ return player.inventory.getCursorStack();
        //#endif
    }

    /**
     * 获取玩家的物品栏
     * 
     * @param player 玩家实体
     * @return 玩家的物品栏
     */
    @Pattern
    private static Inventory getInventory(PlayerEntity player) {
        //#if MC >= 11701
        return player.getInventory();
        //#else
        //$$ return player.inventory;
        //#endif
    }

    /**
     * 检查玩家是否在细雪中
     * 
     * @param player 玩家实体
     * @return 如果玩家在细雪中返回true，否则返回false（旧版本不支持此功能，始终返回false）
     */
    public static boolean inPowderedSnow(PlayerEntity player) {
        //#if MC >= 11701
        return player.inPowderSnow;
        //#else
        //$$ return false;
        //#endif
    }



}
