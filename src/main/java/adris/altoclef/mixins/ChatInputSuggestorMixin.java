package adris.altoclef.mixins;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandExecutor;
import adris.altoclef.commandsystem.StringReader;
import adris.altoclef.commandsystem.args.Arg;
import adris.altoclef.commandsystem.args.ListArg;
import adris.altoclef.commandsystem.exception.BadCommandSyntaxException;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.commandsystem.exception.CommandNotFinishedException;
import adris.altoclef.commandsystem.exception.RuntimeCommandException;
import adris.altoclef.util.Pair;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * 聊天输入建议器混入类，负责高亮显示自定义命令。
 * 此类代码结构复杂，但由于命令系统不常修改，暂时保持现状。
 */
@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin {

    /**
     * 分号样式：浅紫色
     */
    @Unique
    private static final Style SEMICOLOMN_STYLE = Style.EMPTY.withColor(Formatting.LIGHT_PURPLE);
    
    /**
     * 错误文本样式（通过Mixin注入）
     */
    @Shadow
    @Final
    private static Style ERROR_STYLE;
    
    /**
     * 信息文本样式（通过Mixin注入）
     */
    @Shadow
    @Final
    private static Style INFO_STYLE;
    
    /**
     * 高亮样式列表（通过Mixin注入）
     */
    @Shadow
    @Final
    private static List<Style> HIGHLIGHT_STYLES;
    
    /**
     * 文本输入框（通过Mixin注入）
     */
    @Shadow
    @Final
    private TextFieldWidget textField;
    
    /**
     * 消息列表（通过Mixin注入）
     */
    @Shadow
    @Final
    private List<OrderedText> messages;

    /**
     * 消息宽度（通过Mixin注入）
     */
    @Shadow
    private int width;
    
    /**
     * 所属屏幕（通过Mixin注入）
     */
    @Shadow
    @Final
    private Screen owner;

    /**
     * 消息X坐标（通过Mixin注入）
     */
    @Shadow
    private int x;
    
    /**
     * 文本渲染器（通过Mixin注入）
     */
    @Shadow
    @Final
    private TextRenderer textRenderer;
    
    /**
     * 解析缓存，用于避免重复解析相同的文本
     */
    @Unique
    private final HashMap<Pair<String, Integer>, Pair<MutableText, Optional<Pair<MutableText, Integer>>>> parseCache = new HashMap<>();

    /**
     * 添加带样式的文本到列表中
     * 
     * @param styledText 带样式的文本对列表
     * @param original 原始字符串
     * @param currentStr 当前字符串
     * @param style 样式
     * @param reader 字符串读取器
     * @return 处理后的剩余字符串
     * @throws CommandException 命令异常
     */
    @Unique
    private static String addStyledText(List<Pair<String, Style>> styledText, String original, String currentStr, Style style, StringReader reader) throws CommandException {
        if (!reader.hasNext()) {
            styledText.add(new Pair<>(currentStr, style));
            return "";
        }

        int diff = original.length() - currentStr.length();
        int index = reader.getIndex() - diff;
        String processed = currentStr.substring(0, index);

        styledText.add(new Pair<>(processed, style));

        return currentStr.substring(index);
    }

    /**
     * 在刷新时清空解析缓存
     * 
     * @param ci 回调信息
     */
    @Inject(method = "refresh", at = @At("HEAD"))
    public void injectRefresh(CallbackInfo ci) {
        parseCache.clear();
    }

    /**
     * 注入提供渲染文本的方法，处理自定义命令的高亮显示
     * 
     * @param original 原始文本
     * @param firstCharacterIndex 第一个字符索引
     * @param cir 回调信息返回值
     */
    @Inject(method = "provideRenderText", at = @At("HEAD"), cancellable = true)
    public void inj(String original, int firstCharacterIndex, CallbackInfoReturnable<OrderedText> cir) {
        String full = this.textField.getText();

        // 如果不是以命令前缀开头，直接返回
        if (!full.startsWith(AltoClef.getCommandExecutor().getCommandPrefix())) return;

        Pair<String, Integer> key = new Pair<>(original, firstCharacterIndex);

        Pair<MutableText, Optional<Pair<MutableText, Integer>>> result;
        if (parseCache.containsKey(key)) {
            result = parseCache.get(key);
        } else {
            result = highlightText(original, firstCharacterIndex, full);
            parseCache.put(key, result);
        }

        if (result == null) return;

        messages.clear();
        if (result.getRight().isPresent()) {
            MutableText text = result.getRight().get().getLeft();
            int severity = result.getRight().get().getRight();

            messages.add(text.asOrderedText());

            if (severity == 1) {
                this.x = MathHelper.clamp(this.textField.getCharacterX(original.length()), 0, this.textField.getCharacterX(0) + this.textField.getInnerWidth());
                this.width = this.textRenderer.getWidth(text.getString());
            } else if (severity == 2) {
                this.width = this.owner.width;
                this.x = 0;
            }
        }
        cir.setReturnValue(result.getLeft().asOrderedText());
    }

    /**
     * 高亮显示文本
     * 
     * @param original 原始文本
     * @param firstCharacterIndex 第一个字符索引
     * @param full 完整文本
     * @return 包含高亮文本和错误信息的配对
     */
    @Unique
    private Pair<MutableText, Optional<Pair<MutableText, Integer>>> highlightText(String original, int firstCharacterIndex, String full) {
        MutableText text = Text.empty();
        MutableText errorMsg = null;

        Style splitColor = SEMICOLOMN_STYLE;
        int errorSeverity = 0;
        try {
            List<Pair<String, Style>> styledText = new ArrayList<>();

            String[] split = full.split(";", -1);
            int index = 0;
            for (int i = 0; i < split.length; i++) {
                String command = split[i];
                index += command.length();

                if (command.endsWith(" ") && (i+1) < split.length) {
                    errorSeverity = 2;
                    errorMsg = buildErrorMessage("意外的参数", full, index);
                } else if (command.isBlank()) {
                    errorSeverity = Math.max(errorSeverity, 1);
                }

                if (errorSeverity > 0) {
                    splitColor = this.ERROR_STYLE;

                    styledText.add(new Pair<>(command, this.ERROR_STYLE));
                    if (i + 1 < split.length) {
                        styledText.add(new Pair<>(";", splitColor));
                    }

                    continue;
                }


                Pair<List<Pair<String, Style>>, Pair<Integer, MutableText>> part = getText(
                        command.stripLeading(), original.length() + firstCharacterIndex, i + 1 < split.length
                );

                errorSeverity = part.getRight().getLeft();
                if (errorSeverity > 0) {
                    splitColor = this.ERROR_STYLE;
                    errorMsg = part.getRight().getRight();
                }

                List<Pair<String, Style>> styled = new ArrayList<>(part.getLeft());
                String leadingSpace = command.substring(0, command.length() - command.stripLeading().length());

                String first = styled.getFirst().getLeft();
                styled.set(0, new Pair<>(leadingSpace + first, styled.getFirst().getRight()));

                styledText.addAll(styled);

                if (i + 1 < split.length) {
                    styledText.add(new Pair<>(";", splitColor));
                }

            }

            int maxLen = firstCharacterIndex + original.length();

            int length = 0;

            for (Pair<String, Style> pair : styledText) {
                String str = pair.getLeft();

                int nextLength = length + str.length();

                if (nextLength <= firstCharacterIndex) {
                    length = nextLength;
                    continue;
                }

                int start = Math.max(firstCharacterIndex - length, 0);
                int end = Math.min(str.length(), maxLen - length + start);

                String segment = str.substring(start, end);
                text.append(Text.literal(segment).setStyle(pair.getRight()));

                if (length + end >= maxLen) break;

                length = nextLength;
            }

        } catch (CommandException e) {
            return null;
        }
        if (errorMsg != null) {
            return new Pair<>(text, Optional.of(new Pair<>(errorMsg, errorSeverity)));
        }
        return new Pair<>(text, Optional.empty());
    }

    /**
     * 获取带样式的文本
     * 
     * @param s 输入字符串
     * @param maxLen 最大长度
     * @param showUnfinishedErrors 是否显示未完成的错误
     * @return 包含样式文本列表和错误信息的配对
     * @throws CommandException 命令异常
     */
    @Unique
    private Pair<List<Pair<String, Style>>, Pair<Integer, MutableText>> getText(String s, int maxLen, boolean showUnfinishedErrors) throws CommandException {
        CommandExecutor executor = AltoClef.getCommandExecutor();
        StringReader reader = new StringReader(s);
        String original = s;
        MutableText errorMsg = null;

        if (s.isBlank() || reader.peek().isEmpty())
            return new Pair<>(List.of(new Pair<>(s, this.INFO_STYLE)), new Pair<>(0, null));

        String cmd = reader.next();
        boolean hasPrefix = false;
        if (cmd.startsWith(executor.getCommandPrefix())) {
            hasPrefix = true;
            cmd = cmd.substring(executor.getCommandPrefix().length());
        }

        Command command = executor.get(cmd);

        if (command == null) {
            MutableText error = Text.literal("未知命令 '" + cmd + "'");

            ArrayList<Pair<String, Style>> res = new ArrayList<>();
            if (hasPrefix) {
                res.add(new Pair<>(executor.getCommandPrefix(), this.INFO_STYLE));
                res.add(new Pair<>(s.substring(executor.getCommandPrefix().length()), this.ERROR_STYLE));
            } else {
                res.add(new Pair<>(s, this.ERROR_STYLE));
            }

            return new Pair<>(res, new Pair<>(2, error));
        }


        List<Pair<String, Style>> styledText = new ArrayList<>();

        s = addStyledText(styledText, original, s, this.INFO_STYLE, reader);

        Arg<?>[] args = command.getArgs();
        int styleIndex = 0;
        int errorSeverity = 0;

        for (int i = 0; i < args.length; i++) {
            Arg<?> arg = args[i];
            if (!reader.hasNext()) {
                if (!arg.hasDefault) {
                    errorMsg = buildErrorMessage("需要 " + arg.getTypeName(), original + " ", original.length() + 1);
                    errorSeverity = 2;
                }
                break;
            }
            Arg.ParseResult result = arg.consumeIfSupplied(reader);

            if (result == Arg.ParseResult.CONSUMED) {
                s = addStyledText(styledText, original, s, this.HIGHLIGHT_STYLES.get(styleIndex), reader);

                styleIndex++;

                if (styleIndex >= this.HIGHLIGHT_STYLES.size()) {
                    styleIndex = 0;
                }

                continue;
            }

            StringReader copy = reader.copy();
            try {
                arg.parseArg(copy);
            } catch (CommandNotFinishedException e) {
                if (showUnfinishedErrors) {
                    errorMsg = buildErrorMessage(e.getMessage(), original, copy.getIndex());
                    errorSeverity = 2;
                } else if ((original.endsWith(" ") || original.endsWith("[") || original.endsWith(",") || original.endsWith(";") || arg instanceof ListArg<?>)
                        && original.length() == maxLen
                ) {
                    String str = command.getHelpRepresentation(cmd, i);
                    errorMsg = Text.literal(str).setStyle(this.INFO_STYLE);

                    errorSeverity = 1;
                }

            } catch (BadCommandSyntaxException e) {
                errorMsg = buildErrorMessage(e.getMessage(), original, copy.getIndex());
                errorSeverity = 2;
            }

            styledText.add(new Pair<>(s, this.ERROR_STYLE));
            s = "";
            break;
        }

        if (!s.isEmpty() || original.endsWith(" ")) {
            styledText.add(new Pair<>(s, this.ERROR_STYLE));

            if (errorMsg == null) {
                errorMsg = buildErrorMessage("意外的参数", original, reader.getIndex());
            }
            errorSeverity = 2;
        }

        return new Pair<>(styledText, new Pair<>(errorSeverity, errorMsg));
    }

    /**
     * 构建错误消息
     * 
     * @param message 错误消息
     * @param original 原始字符串
     * @param index 错误位置索引
     * @return 错误消息文本
     */
    @Unique
    private MutableText buildErrorMessage(String message, String original, int index) {
        String substr = original.substring(0, index);

        int ind = substr.lastIndexOf(" ");
        if (ind == -1) {
            ind = substr.length();
        }
        substr = substr.substring(0, ind);

        int maxLen = 15;
        if (substr.length() > maxLen) {
            substr = "..." + substr.substring(substr.length() - (maxLen - 3));
        }

        return
                Text.literal(message)
                        .append(Text.literal(" 在位置 " + index + ": " + substr + " <--[此处]"));
    }

}
