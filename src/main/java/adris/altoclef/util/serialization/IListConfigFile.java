package adris.altoclef.util.serialization;

/**
 * 列表配置文件接口
 * 定义了处理基于行的配置文件的方法
 */
public interface IListConfigFile {
    /**
     * 在开始加载配置文件时调用
     */
    void onLoadStart();

    /**
     * 向配置文件添加一行内容
     * 
     * @param line 要添加的行内容
     */
    void addLine(String line);
}
