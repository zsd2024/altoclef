package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ChunkLoadEvent;
import adris.altoclef.eventbus.events.ChunkUnloadEvent;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC <= 11701
//$$ import net.minecraft.world.biome.source.BiomeArray;
//#endif

import java.util.BitSet;
import java.util.function.Consumer;

/**
 * 客户端区块管理器混入类
 * 监听区块加载和卸载事件，并发布相应事件
 */
@Mixin(ClientChunkManager.class)
public class LoadChunkMixin {

    /**
     * 从数据包加载区块并执行必要操作
     *
     * @param x        区块的x坐标
     * @param z        区块的z坐标
     * @param buf      包含区块数据的数据包
     * @param nbt      区块的NBT复合标签
     * @param consumer 用于访问区块中方块实体的消费者
     * @param cir      可返回回调信息对象
     */
    @Inject(
            method = "loadChunkFromPacket",
            at = @At("RETURN")
    )
    //#if MC >= 11800
    private void onLoadChunk(int x, int z, PacketByteBuf buf, NbtCompound nbt, Consumer<net.minecraft.network.packet.s2c.play.ChunkData.BlockEntityVisitor> consumer, CallbackInfoReturnable<WorldChunk> cir) {
    //#elseif MC >= 11701
    //$$ private void onLoadChunk(int x, int z, net.minecraft.world.biome.source.BiomeArray biomes, PacketByteBuf buf, NbtCompound nbt, BitSet bitSet, CallbackInfoReturnable<WorldChunk> cir) {
    //#else
    //$$ private void onLoadChunk(int x, int z, BiomeArray biomes, PacketByteBuf buf, NbtCompound tag, int verticalStripBitmask, boolean complete, CallbackInfoReturnable<WorldChunk> cir) {
    //#endif
        // 使用方法的返回值作为参数发布区块加载事件
        EventBus.publish(new ChunkLoadEvent(cir.getReturnValue()));
    }

    /**
     * 当区块被卸载时发布区块卸载事件
     *
     * @param pos 卸载的区块位置
     * @param ci  回调信息对象
     */
    @Inject(
            method = "unload",
            at = @At("TAIL")
    )
    //#if MC > 12001
    private void onChunkUnload(ChunkPos pos, CallbackInfo ci) {
        EventBus.publish(new ChunkUnloadEvent(pos));
    }
    //#else
    //$$ private void onChunkUnload(int x, int z, CallbackInfo ci) {
    //$$     EventBus.publish(new ChunkUnloadEvent(new ChunkPos(x,z)));
    //$$ }
    //#endif
}
