# 本地开发环境设置

## 克隆仓库

```bash
git clone https://github.com/MiranCZ/altoclef.git
cd altoclef
```

## 系统要求

- **Java 21** (项目使用 Java 21 编译，但会根据 Minecraft 版本自动降级到 Java 17 或 8)
- **Gradle 8.0+**
- **Minecraft 客户端** (用于测试)

## 构建项目

### 基本构建

```bash
./gradlew build
```

这将生成 `build/libs/altoclef-*.jar` 文件。

### 清理构建缓存

```bash
./gradlew clean
```

## 运行和调试

### 运行客户端进行测试

```bash
./gradlew runClient
```

这将启动带有 Alto Clef 的 Minecraft 客户端，便于本地测试。

### 启用开发模式

启用开发模式以使用本地构建的 Baritone：

```bash
./gradlew runClient -Paltoclef.development
```

> ⚠️ **注意**: 使用开发模式前需要先构建 [baritone-plus](https://github.com/TacoTheDank/baritone-plus) 项目。

## 多版本开发

Alto Clef 支持多个 Minecraft 版本，使用 ReplayMod 预处理器处理版本差异。

### 版本特定代码

- `src/main/java/`: 主要源代码（适用于所有版本）
- `versions/1.20.6/`: 1.20.6 特定代码
- `versions/1.18.2/`: 1.18.2 特定代码  
- `versions/1.16.5/`: 1.16.5 特定代码

### 构建特定版本

项目会根据目录名称自动构建对应版本。例如：

```bash
# 构建 1.20.6 版本
mv altoclef altoclef-1.20.6
./gradlew build

# 构建 1.18.2 版本  
mv altoclef altoclef-1.18.2
./gradlew build
```

## IDE 配置

### IntelliJ IDEA

1. 导入项目为 Gradle 项目
2. 确保使用 Java 21 SDK
3. 启用注解处理（Annotation Processing）

### VS Code

1. 安装 Java Extension Pack
2. 导入项目
3. 配置 Java 21 环境

## 调试技巧

### 启用详细日志

在 `altoclef_settings.json` 中设置：

```json
{
  "logLevel": "ALL",
  "showDebugTickMs": true
}
```

### 使用调试命令

- `@test`: 运行测试任务
- `@status`: 查看当前状态
- `@inventory`: 查看物品库存

### 断点调试

在 IDE 中设置断点，使用 `./gradlew runClient` 启动调试会话。

## 依赖管理

项目主要依赖：

- **Fabric Loader**: Minecraft 模组加载器
- **Fabric API**: Fabric 模组开发 API
- **Baritone**: 路径规划引擎
- **Jackson**: JSON 序列化
- **MixinExtras**: Mixin 扩展工具

依赖版本在 `build.gradle` 和 `gradle.properties` 中定义。

## 代码风格

- 使用 4 个空格缩进
- 类名使用 PascalCase
- 方法和变量使用 camelCase
- 常量使用 UPPER_SNAKE_CASE
- 遵循标准 Java 命名规范