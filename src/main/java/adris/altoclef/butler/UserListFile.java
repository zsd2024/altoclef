package adris.altoclef.butler;

import adris.altoclef.util.helpers.ConfigHelper;
import adris.altoclef.util.serialization.IListConfigFile;

import java.util.HashSet;
import java.util.function.Consumer;

/**
 * 用户列表文件类，提供用户列表的配置文件处理
 */
public class UserListFile implements IListConfigFile {

    private final HashSet<String> _users = new HashSet<>(); // 存储用户名的集合

    /**
     * 从指定路径加载用户列表文件
     * @param path 文件路径
     * @param onLoad 加载完成后的回调函数
     */
    public static void load(String path, Consumer<UserListFile> onLoad) {
        ConfigHelper.loadListConfig(path, UserListFile::new, onLoad);
    }

    /**
     * 检查列表中是否包含指定用户
     * @param username 用户名
     * @return 是否包含该用户
     */
    public boolean containsUser(String username) {
        return _users.contains(username);
    }

    @Override
    /**
     * 配置文件加载开始时的处理
     */
    public void onLoadStart() {
        _users.clear();
    }

    @Override
    /**
     * 添加一行到配置文件
     * @param line 要添加的行
     */
    public void addLine(String line) {
        _users.add(line);
    }
}
