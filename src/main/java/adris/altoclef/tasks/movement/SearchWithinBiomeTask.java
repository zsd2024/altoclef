package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.world.WorldVer;
import adris.altoclef.tasksystem.Task;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;

/**
 * 在生物群系中搜索/加载所有区块
 */
public class SearchWithinBiomeTask extends SearchChunksExploreTask {

    private final RegistryKey<Biome> _toSearch; // 要搜索的生物群系

    public SearchWithinBiomeTask(RegistryKey<Biome> toSearch) {
        _toSearch = toSearch;
    }

    @Override
    protected boolean isChunkWithinSearchSpace(AltoClef mod, ChunkPos pos) {
        // 检查位置是否为指定生物群系
        return WorldVer.isBiomeAtPos(mod.getWorld(),_toSearch,pos.getStartPos().add(1,1,1));
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof SearchWithinBiomeTask task) {
            return task._toSearch == _toSearch;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "搜索生物群系: " + _toSearch;
    }
}
