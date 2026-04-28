package adris.altoclef.commandsystem;

import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.exception.CommandNotFinishedException;

import java.util.ArrayList;
import java.util.List;

/**
 * 字符串读取器
 * 用于解析和读取命令字符串，支持按单词或字符进行读取
 */
public class StringReader {


    /** 存储分割后的字符串片段 */
    private final List<String> parts;
    /** 存储每个片段在原字符串中的起始索引 */
    private final List<Integer> indexStarts;

    /**
     * 构造函数
     * 将输入字符串按空格分割，并记录每个片段的起始位置
     * @param line 输入的字符串行
     */
    public StringReader(String line){
        parts = new ArrayList<>();
        indexStarts = new ArrayList<>();

        String[] tokens = line.split(" ", -1);

        int from = 0;
        for (String tok : tokens) {
            int start = line.indexOf(tok, from);
            parts.add(tok);
            indexStarts.add(start);

            from = start + tok.length() + 1;
        }

        indexStarts.add(line.length());
    }

    /**
     * 私有构造函数，用于复制对象
     * @param parts 字符串片段列表
     * @param indexStarts 起始索引列表
     */
    private StringReader(List<String> parts, List<Integer> indexStarts) {
        this.parts = parts;
        this.indexStarts = indexStarts;
    }

    /**
     * 获取下一个字符串片段
     * @return 下一个字符串片段
     * @throws CommandException 当字符串已被完全消耗时抛出异常
     */
    public String next() throws CommandException {
        if (parts.isEmpty()) {
            throw new CommandNotFinishedException("字符串已被完全消耗！");
        }

        indexStarts.removeFirst();
        return parts.removeFirst();
    }

    /**
     * 获取下一个字符串片段，如果已无片段则返回空字符串
     * @return 下一个字符串片段或空字符串
     */
    public String nextOrEmpty() {
        if (parts.isEmpty()) return "";

        try {
            return next();
        } catch (CommandException e) {
            throw new IllegalStateException();
        }
    }

    /**
     * 获取剩余字符串片段的数量
     * @return 剩余片段数量
     */
    public int size() {
        return parts.size();
    }


    /**
     * 预览下一个字符串片段（不消耗）
     * @return 下一个字符串片段
     * @throws CommandException 当字符串已被完全消耗时抛出异常
     */
    public String peek()  throws CommandException {
        if (parts.isEmpty()) {
            throw new CommandNotFinishedException("字符串已被完全消耗！");
        }

        return parts.getFirst();
    }

    /**
     * 获取下一个字符
     * @return 下一个字符
     * @throws CommandException 当字符串已被完全消耗时抛出异常
     */
    public Character nextChar() throws CommandException {
        if (parts.isEmpty()) {
            throw new CommandNotFinishedException("字符串已被完全消耗！");
        }

        String part = parts.getFirst();

        // 消耗整个字符串片段
        if (part.length() == 1) {
            indexStarts.removeFirst();
            return parts.removeFirst().charAt(0);
        }

        // 消耗单个字符
        parts.set(0, part.substring(1));

        return part.charAt(0);
    }

    /**
     * 检查是否还有剩余的字符串片段
     * @return 如果还有剩余片段返回true，否则返回false
     */
    public boolean hasNext() {
        return !parts.isEmpty();
    }

    /**
     * 获取当前读取位置的索引
     * @return 当前索引位置
     */
    public int getIndex() {
        return indexStarts.getFirst();
    }

    /**
     * 创建当前读取器的副本
     * @return 新的StringReader对象
     */
    public StringReader copy() {
        return new StringReader(new ArrayList<>(this.parts), new ArrayList<>(indexStarts));
    }

    /**
     * 将当前读取器设置为另一个读取器的状态
     * @param other 另一个StringReader对象
     */
    public void set(StringReader other) {
        parts.clear();
        parts.addAll(other.parts);

        indexStarts.clear();
        indexStarts.addAll(other.indexStarts);
    }

}
