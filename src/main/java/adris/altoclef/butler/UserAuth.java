package adris.altoclef.butler;

import adris.altoclef.AltoClef;
import adris.altoclef.util.helpers.ConfigHelper;

/**
 * 用户认证类，处理助手系统的用户授权
 */
public class UserAuth {
    private static final String BLACKLIST_PATH = "altoclef_butler_blacklist.txt"; // 黑名单文件路径
    private static final String WHITELIST_PATH = "altoclef_butler_whitelist.txt"; // 白名单文件路径
    private final AltoClef _mod; // AltoClef主模块
    private UserListFile _blacklist; // 黑名单
    private UserListFile _whitelist; // 白名单

    /**
     * 构造函数，初始化用户认证系统
     * @param mod AltoClef主模块
     */
    public UserAuth(AltoClef mod) {
        _mod = mod;

        ConfigHelper.ensureCommentedListFileExists(BLACKLIST_PATH, """
                在此处添加助手黑名单玩家。
                确保在设置文件中将useButlerBlacklist设置为真。
                井号（#）之后的任何内容将被忽略。""");
        ConfigHelper.ensureCommentedListFileExists(WHITELIST_PATH, """
                在此处添加助手白名单玩家。
                确保在设置文件中将useButlerWhitelist设置为真。
                井号（#）之后的任何内容将被忽略。""");

        UserListFile.load(BLACKLIST_PATH, newList -> _blacklist = newList);
        UserListFile.load(WHITELIST_PATH, newList -> _whitelist = newList);
    }

/**
 * 检查用户是否已授权
     * @param username 用户名
     * @return 是否授权
     */
    public boolean isUserAuthorized(String username) {

        // 黑名单优先级最高。
        if (ButlerConfig.getInstance().useButlerBlacklist && _blacklist.containsUser(username)) {
            return false;
        }
        if (ButlerConfig.getInstance().useButlerWhitelist) {
            return _whitelist.containsUser(username);
        }

        // 默认接受所有人。
        return true;
    }
}
        if (ButlerConfig.getInstance().useButlerWhitelist) {
            return _whitelist.containsUser(username);
        }

        // By default accept everyone.
        return true;
    }

}
