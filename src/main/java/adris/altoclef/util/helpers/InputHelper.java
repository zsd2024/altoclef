package adris.altoclef.util.helpers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

/**
 * 输入帮助器类
 * 提供与输入相关的实用方法
 */
public class InputHelper {

    /**
     * 检查指定键码是否被按下
     * @param code 键码
     * @return 如果键被按下返回true，否则返回false
     */
    public static boolean isKeyPressed(int code) {
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), code);
    }
}
