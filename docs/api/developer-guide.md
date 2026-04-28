# 开发者指南

## 项目结构

```
src/main/java/adris/altoclef/
├── AltoClef.java          # 主入口点和核心控制器
├── Settings.java          # 配置系统
├── BotBehaviour.java      # 机器人行为控制
├── TaskCatalogue.java     # 任务注册表
├── AltoClefCommands.java  # 命令初始化
├── commands/             # 具体命令实现
├── chains/               # 任务链系统
├── tasks/                # 具体任务实现
├── trackers/             # 跟踪器系统
├── util/                 # 工具类
└── control/              # 控制系统
```

## 核心组件

### AltoClef (主控制器)

`AltoClef` 类是整个模组的中心枢纽，提供以下关键功能：

```java
// 获取核心组件
AltoClef.getInstance()           // 获取单例实例
AltoClef.getCommandExecutor()    // 获取命令执行器
mod.getTaskRunner()              // 获取任务运行器
mod.getUserTaskChain()           // 获取用户任务链
mod.getItemStorage()             // 获取物品存储跟踪器
mod.getEntityTracker()           // 获取实体跟踪器
mod.getBlockScanner()            // 获取方块扫描器
```

### 任务系统

#### 创建自定义任务

继承 `ResourceTask` 类并实现必要的方法：

```java
public class CustomResourceTask extends ResourceTask {
    public CustomResourceTask(ItemTarget target) {
        super(target);
    }
    
    @Override
    protected void onTick(AltoClef mod) {
        // 任务逻辑
    }
    
    @Override
    protected boolean isFinished(AltoClef mod) {
        return false; // 任务完成条件
    }
}
```

#### 注册新资源

在 `TaskCatalogue.init()` 中添加新的资源定义：

```java
// 简单资源
simple("custom_item", Items.CUSTOM_ITEM, count -> new CustomResourceTask(new ItemTarget(Items.CUSTOM_ITEM, count)));

// 挖掘资源  
mine("custom_ore", MiningRequirement.IRON, Blocks.CUSTOM_ORE, Items.CUSTOM_ITEM);

// 合成资源
shapedRecipe3x3("custom_tool", Items.CUSTOM_TOOL, 1, "stick", "custom_item", "stick", ...);
```

### 命令系统

#### 创建新命令

继承 `Command` 类并实现 `call` 方法：

```java
public class CustomCommand extends Command {
    public CustomCommand() {
        super("custom", "执行自定义操作");
    }
    
    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        // 命令逻辑
        String param = parser.getString(); // 获取参数
        mod.log("执行自定义命令: " + param);
    }
}
```

#### 注册命令

在 `AltoClefCommands.init()` 中注册：

```java
AltoClef.getCommandExecutor().registerNewCommand(new CustomCommand());
```

### 跟踪器系统

跟踪器用于监控游戏状态：

- `ItemStorageTracker`: 跟踪背包和箱子中的物品
- `EntityTracker`: 跟踪加载的实体
- `BlockScanner`: 跟踪方块位置和类型
- `CraftingRecipeTracker`: 跟踪可用的合成配方

### 行为控制

`BotBehaviour` 类允许动态修改机器人的行为：

```java
// 临时修改行为
mod.getBehaviour().push();
mod.getBehaviour().setEscapeLava(false);
mod.getBehaviour().addProtectedItems(Items.DIAMOND);
// ... 执行任务 ...
mod.getBehaviour().pop(); // 恢复之前的行为
```

## 构建和测试

### 本地构建

```bash
./gradlew build
```

### 开发模式

启用本地开发模式以使用本地构建的 Baritone：

```bash
./gradlew runClient -Paltoclef.development
```

### 多版本支持

项目使用 ReplayMod 预处理器支持多个 Minecraft 版本。版本特定代码放在 `versions/` 目录中。

## 扩展 API

### 事件系统

Alto Clef 使用自定义事件总线：

```java
// 订阅事件
EventBus.subscribe(ClientTickEvent.class, evt -> {
    // 每帧执行的逻辑
});

// 发布事件
EventBus.publish(new CustomEvent());
```

### 工具类

常用工具类位于 `util/helpers/` 目录：

- `ItemHelper`: 物品相关操作
- `WorldHelper`: 世界相关操作  
- `MathsHelper`: 数学计算
- `CraftingHelper`: 合成相关操作

## 贡献指南

1. Fork 项目仓库
2. 创建特性分支 (`git checkout -b feature/your-feature`)
3. 提交更改 (`git commit -am 'Add some feature'`)
4. 推送到分支 (`git push origin feature/your-feature`)
5. 创建 Pull Request

确保遵循项目的代码风格和命名约定。