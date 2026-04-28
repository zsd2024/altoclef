package adris.altoclef.multiversion;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

/**
 * 交互管理器版本适配器
 * 用于处理不同 Minecraft 版本中物品和方块交互方法参数差异
 * 在 1.19.4+ 版本中，interactItem 和 interactBlock 方法不需要传入世界参数
 * 在 1.19.3 及更早版本中，这些方法需要传入世界参数
 */
public class InteractionManagerVer {

    /**
     * 执行物品交互操作
     * 
     * @param interactionManager 客户端玩家交互管理器
     * @param player 玩家实体
     * @param hand 使用的手（主手或副手）
     * @return 交互操作的结果
     */
    @Pattern
    public ActionResult interactItem(ClientPlayerInteractionManager interactionManager, PlayerEntity player, Hand hand) {
        //#if MC >= 11904
        return interactionManager.interactItem(player,hand);
        //#else
        //$$ return interactionManager.interactItem(player,MinecraftClient.getInstance().world,hand);
        //#endif
    }

    /**
     * 执行方块交互操作
     * 
     * @param interactionManager 客户端玩家交互管理器
     * @param player 客户端玩家实体
     * @param hand 使用的手（主手或副手）
     * @param hitResult 方块命中结果
     * @return 交互操作的结果
     */
    @Pattern
    public ActionResult interactBlock(ClientPlayerInteractionManager interactionManager, ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        //#if MC >= 11904
        return interactionManager.interactBlock(player,hand, hitResult);
        //#else
        //$$ return interactionManager.interactBlock(player,MinecraftClient.getInstance().world, hand ,hitResult);
        //#endif
    }
}
