package adris.altoclef.multiversion.world;

import adris.altoclef.multiversion.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

//#if MC >= 11802
import net.minecraft.registry.entry.RegistryEntry;
//#endif

/**
 * 世界版本适配器
 * 
 * 此类提供了跨多个 Minecraft 版本的世界相关功能的统一接口。
 * 使用 ReplayMod 预处理器注解来处理不同版本之间的 API 差异。
 */
public class WorldVer {



    /**
     * 检查指定位置是否属于给定的生物群系
     * 
     * @param world 世界实例
     * @param biome 生物群系键
     * @param pos 位置
     * @return 如果位置属于指定生物群系返回 true，否则返回 false
     */
    public static boolean isBiomeAtPos(World world, RegistryKey<Biome> biome, BlockPos pos) {
        //#if MC >= 11802
        RegistryEntry<Biome> b = world.getBiome(pos);
        return b.matchesKey(biome);
        //#else
        //$$ Biome b = world.getBiome(pos);
        //$$ return world.getRegistryManager().get(Registry.BIOME_KEY).get(biome) == b;
        //#endif
    }


    //#if MC >= 11802
    /**
     * 检查两个生物群系是否相同
     * 
     * @param biome1 第一个生物群系（RegistryEntry 形式）
     * @param biome2 第二个生物群系（RegistryKey 形式）
     * @return 如果两个生物群系相同返回 true，否则返回 false
     */
    public static boolean isBiome(RegistryEntry<Biome> biome1, RegistryKey<Biome> biome2) {
        return biome1.matchesKey(biome2);
    }
    //#else
    //$$ /**
    //$$  * 检查两个生物群系是否相同
    //$$  * 
    //$$  * @param biome1 第一个生物群系（Biome 对象）
    //$$  * @param biome2 第二个生物群系（RegistryKey 形式）
    //$$  * @return 如果两个生物群系相同返回 true，否则返回 false
    //$$  */
    //$$ public static boolean isBiome(Biome biome1, RegistryKey<Biome> biome2) {
    //$$     World world = MinecraftClient.getInstance().world;
    //$$     return world.getRegistryManager().get(Registry.BIOME_KEY).get(biome2) == biome1;
    //$$ }
    //#endif


    /**
     * 获取世界的底部 Y 坐标
     * 
     * @param world 世界实例
     * @return 底部 Y 坐标
     */
    @Pattern
    public static int getBottomY(World world) {
        //#if MC >= 11701
        return world.getBottomY();
        //#else
        //$$ return adris.altoclef.multiversion.world.WorldHelper.getBottomY(world);
        //#endif
    }

    /**
     * 获取世界的顶部 Y 坐标
     * 
     * @param world 世界实例
     * @return 顶部 Y 坐标
     */
    @Pattern
    public static int getTopY(World world) {
        //#if MC >= 11701
        return world.getTopY();
        //#else
        //$$ return adris.altoclef.multiversion.world.WorldHelper.getTopY(world);
        //#endif
    }

    /**
     * 检查指定位置是否超出世界高度限制
     * 
     * @param world 世界实例
     * @param pos 位置
     * @return 如果位置超出高度限制返回 true，否则返回 false
     */
    @Pattern
    private static boolean isOutOfHeightLimit(World world,BlockPos pos) {
        //#if MC >= 11701
        return world.isOutOfHeightLimit(pos);
        //#else
        //$$ return adris.altoclef.multiversion.world.WorldHelper.isOutOfHeightLimit(world,pos);
        //#endif
    }

}
