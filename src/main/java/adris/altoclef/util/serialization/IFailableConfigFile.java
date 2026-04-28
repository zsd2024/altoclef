package adris.altoclef.util.serialization;

/**
 * 可失败配置文件接口
 * 定义了配置文件加载失败时的处理方法
 */
public interface IFailableConfigFile {
    /**
     * 当配置文件加载失败时调用
     */
    void onFailLoad();

    /**
     * 检查配置文件是否加载失败
     * 
     * @return 如果加载失败返回true，否则返回false
     */
    boolean failedToLoad();
}
