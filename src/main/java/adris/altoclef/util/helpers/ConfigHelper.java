package adris.altoclef.util.helpers;

import adris.altoclef.Debug;
import adris.altoclef.util.serialization.*;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 配置帮助器类
 * 用于加载和管理设置/配置文件
 */
public class ConfigHelper {

    private static final String ALTO_FOLDER = "altoclef";
    // 用于重新加载配置
    private static final HashMap<String, Runnable> loadedConfigs = new HashMap<>();

    /**
     * 获取配置文件的File对象
     * @param path 配置文件路径
     * @return File对象
     */
    private static File getConfigFile(String path) {
        return Paths.get(ALTO_FOLDER, path).toFile();
    }

    /**
     * 重新加载所有已加载的配置文件
     */
    public static void reloadAllConfigs() {
        for (Runnable reload : loadedConfigs.values()) {
            reload.run();
        }
    }

    /**
     * 获取配置对象
     * @param path 配置文件路径
     * @param getDefault 默认值提供者
     * @param classToLoad 要加载的类类型
     * @param <T> 配置类型
     * @return 配置对象
     */
    private static <T> T getConfig(String path, Supplier<T> getDefault, Class<T> classToLoad) {
        T result = getDefault.get();
        File loadFrom = getConfigFile(path);
        if (!loadFrom.exists()) {
            saveConfig(path, result);
            return result;
        }

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Vec3d.class, new Vec3dDeserializer());
        module.addDeserializer(ChunkPos.class, new ChunkPosDeserializer());
        module.addDeserializer(BlockPos.class, new BlockPosDeserializer());
        mapper.registerModule(module);

        boolean failed = false;
        try {
            result = mapper.readValue(loadFrom, classToLoad);
        } catch (JsonMappingException ex) {
            Debug.logError("解析类型为 " + classToLoad.getSimpleName() + " 的配置文件失败，路径: " + path + ". JSON错误信息: " + ex.getMessage() + ".\n JSON错误堆栈跟踪:\n\n",ex);
            if (result instanceof IFailableConfigFile failable)
                failable.failedToLoad();
            failed = true;
        } catch (IOException e) {
            Debug.logError("读取配置文件失败，路径: " + path + ".", e);
            if (result instanceof IFailableConfigFile failable)
                failable.failedToLoad();
            failed = true;
        }

        // 保存覆盖以包含新设置
        // 但仅在加载成功时才保存。不想覆盖用户设置！
        if (!failed) {
            saveConfig(path, result);
        }

        return result;
    }

    /**
     * 加载配置文件
     * @param path 配置文件路径
     * @param getDefault 默认值提供者
     * @param classToLoad 要加载的类类型
     * @param onReload 重新加载时的回调函数
     * @param <T> 配置类型
     */
    public static <T> void loadConfig(String path, Supplier<T> getDefault, Class<T> classToLoad, Consumer<T> onReload) {
        T result = getConfig(path, getDefault, classToLoad);
        loadedConfigs.put(path, () -> onReload.accept(getConfig(path, getDefault, classToLoad)));
        onReload.accept(result);
    }

    /**
     * 保存配置文件
     * @param path 配置文件路径
     * @param config 配置对象
     * @param <T> 配置类型
     */
    public static <T> void saveConfig(String path, T config) {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Vec3d.class, new Vec3dSerializer());
        module.addSerializer(BlockPos.class, new BlockPosSerializer());
        module.addSerializer(ChunkPos.class, new ChunkPosSerializer());
        mapper.registerModule(module);

        File toSave = getConfigFile(path);
        if (!toSave.getParentFile().exists()) {
            //noinspection ResultOfMethodCallIgnored
            toSave.getParentFile().mkdirs();
        }

        try {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            // 美化打印并缩进数组
            DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
            prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

            mapper.writer(prettyPrinter).writeValue(toSave, config);
        } catch (IOException e) {
            Debug.logError("保存配置失败!",e);
        }
    }

    /**
     * 获取列表配置
     * @param path 配置文件路径
     * @param getDefault 默认值提供者
     * @param <T> 列表配置类型
     * @return 列表配置对象
     */
    private static <T extends IListConfigFile> T getListConfig(String path, Supplier<T> getDefault) {
        T result = getDefault.get();

        result.onLoadStart();

        File loadFrom = getConfigFile(path);
        if (!loadFrom.exists()) {
            // 空文件
            Debug.logInternal("在 " + path + " 处未找到列表文件");
            return result;
        }

        try {
            FileInputStream fis = new FileInputStream(loadFrom);
            Scanner sc = new Scanner(fis);    //要扫描的文件
            //如果还有下一行可读则返回true
            while (sc.hasNextLine()) {
                String line = trimComment(sc.nextLine()).trim();
                if (line.isEmpty()) continue;
                result.addLine(line);
            }
            sc.close();
        } catch (IOException e) {
            Debug.logError("加载列表配置文件失败 ("+loadFrom+")",e);
            return null;
        }
        return result;
    }

    /**
     * 加载列表配置文件
     * @param path 配置文件路径
     * @param getDefault 默认值提供者
     * @param onReload 重新加载时的回调函数
     * @param <T> 列表配置类型
     */
    public static <T extends IListConfigFile> void loadListConfig(String path, Supplier<T> getDefault, Consumer<T> onReload) {
        T result = getListConfig(path, getDefault);
        loadedConfigs.put(path, () -> onReload.accept(getListConfig(path, getDefault)));
        onReload.accept(result);
    }

    /**
     * 去除行尾注释（#后面的内容）
     * @param line 输入行
     * @return 去除注释后的行
     */
    private static String trimComment(String line) {
        int pound = line.indexOf('#');
        if (pound == -1) {
            return line;
        }
        return line.substring(0, pound);
    }

    /**
     * 确保带注释的列表文件存在
     * @param path 文件路径
     * @param startingComment 初始注释内容
     */
    public static void ensureCommentedListFileExists(String path, String startingComment) {
        File loadFrom = getConfigFile(path);
        if (loadFrom.exists()) {
            // 文件已存在，不需要创建新的。
            return;
        }
        StringBuilder result = new StringBuilder();
        for (String line : startingComment.split("\\r?\\n")) {
            if (!line.isEmpty()) {
                result.append("# ").append(line).append("\n");
            }
        }
        try {
            Files.write(loadFrom.toPath(), result.toString().getBytes());
        } catch (IOException e) {
            Debug.logError("写入列表配置失败 ("+loadFrom+")",e);
        }
    }
}
