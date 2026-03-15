# AltoClef 项目开发指南

## 构建与测试命令

### 构建命令
- `./gradlew build` - 构建项目
- `./gradlew runClient` - 运行客户端进行测试
- `./gradlew clean` - 清理构建缓存

### 测试命令
- `./gradlew test` - 运行所有测试（如果存在）
- 单个测试运行需通过IDE配置或Gradle测试任务指定

### 开发模式
- `-Paltoclef.development` - 启用本地开发模式，使用本地构建的baritone

## 代码规范

### Java版本
- 项目使用Java 21编译
- 源代码兼容性: Java 21
- 根据Minecraft版本可能降级到Java 17或Java 8

### 代码风格
- 使用UTF-8编码
- 遵循标准Java命名规范
- 使用4个空格缩进
- 类名使用PascalCase
- 方法和变量使用camelCase
- 常量使用UPPER_SNAKE_CASE

### 导入规范
- 使用完整的包名导入
- 避免使用"*"进行批量导入
- 按java标准库、第三方库、项目内部包的顺序分组

### 错误处理
- 遵循Java标准异常处理机制
- 自定义异常应继承适当的异常基类
- 记录关键错误日志以便调试

### 命名约定
- 任务类以"Task"结尾
- 链条类以"Chain"结尾
- 命令类以"Command"结尾
- 配置类以"Config"结尾

### 多版本支持
- 项目使用ReplayMod预处理器支持多个Minecraft版本
- 版本特定代码使用@Pattern注解标记
- 不同版本的兼容性代码应保持一致的接口

## 项目结构
- src/main/java/adris/altoclef - 主要源代码
- src/main/resources - 资源文件
- build.gradle - 构建配置
- gradle.properties - Gradle属性配置
- gradlew - Gradle包装器

## 模块说明
- TaskCatalogue - 任务注册表
- tasks - 任务实现
- chains - 各种功能链
- commands - 指令系统
- trackers - 跟踪器系统
- util - 工具类
- control - 控制系统

## 文档要求
- 所有公共API必须有Javadoc注释
- 复杂逻辑需要添加行内注释说明
- 类和方法功能应清晰描述

## 特殊配置
- 使用定制的baritonefork提供更精确的控制
- 支持多Minecraft版本编译
- 使用ShadowJar打包依赖