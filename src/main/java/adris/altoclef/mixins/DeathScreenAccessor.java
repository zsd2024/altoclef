package adris.altoclef.mixins;

import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 死亡屏幕访问器接口
 * 
 * 此接口用于通过Mixin的Accessor功能访问DeathScreen类中的私有message字段，
 * 允许AltoClef获取玩家死亡时显示的消息文本。
 */
@Mixin(DeathScreen.class)
public interface DeathScreenAccessor {
    /**
     * 获取死亡屏幕中显示的消息文本
     * 
     * @return 死亡消息文本
     */
    @Accessor("message")
    Text getMessage();
}