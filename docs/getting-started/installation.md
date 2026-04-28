# 安装指南

## 系统要求

- **Minecraft 版本**: 1.16.5 - 1.21.1
- **Fabric Loader**: 0.16.2 或更高版本
- **Java 版本**: 
  - Minecraft 1.20.6+ 需要 Java 21
  - Minecraft 1.17+ 需要 Java 17  
  - Minecraft 1.16.5 需要 Java 8

## 安装步骤

### 1. 安装 Fabric Loader

1. 访问 [Fabric 官方网站](https://fabricmc.net/use/)
2. 下载对应 Minecraft 版本的 Fabric Installer
3. 运行安装程序并选择你的 Minecraft 版本和 Fabric Loader 版本 (0.16.2+)

### 2. 安装 Alto Clef

1. 从 [GitHub Releases](https://github.com/MiranCZ/altoclef/releases) 下载最新版本的 Alto Clef JAR 文件
2. 将下载的 JAR 文件放入 Minecraft 的 `mods` 文件夹中：
   - Windows: `%appdata%\.minecraft\mods`
   - macOS: `~/Library/Application Support/minecraft/mods`
   - Linux: `~/.minecraft/mods`

### 3. 启动游戏

启动 Minecraft 并选择安装了 Fabric 的配置文件。Alto Clef 应该会自动加载。

## 加载器兼容性

Alto Clef 目前仅支持 **Fabric** 加载器，不支持 Forge、NeoForge 或 Quilt。

## 多版本支持

Alto Clef 使用 ReplayMod 预处理器支持多个 Minecraft 版本。项目源码中的 `versions/` 目录包含了不同 Minecraft 版本的特定代码。

| Minecraft 版本 | 源码位置 |
|---------------|----------|
| 1.20.6 | `versions/1.20.6/` |
| 1.18.2 | `versions/1.18.2/` |
| 1.16.5 | `versions/1.16.5/` |

> 💡 **提示**: 如果你需要为特定版本构建 mod，确保在构建时指定正确的 Minecraft 版本。