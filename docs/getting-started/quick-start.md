# 5分钟上手指南

## 基础命令使用

Alto Clef 使用 `@` 作为默认命令前缀。你可以在聊天框中输入以下命令：

### 🎯 资源获取命令

```bash
# 获取指定数量的物品
@get <物品名> <数量>

# 示例：获取 64 个钻石
@get diamond 64

# 获取多种物品
@get iron_ingot 32, gold_ingot 16, diamond 8
```

### 🏃‍♂️ 移动和导航命令

```bash
# 前往指定坐标
@goto <x> <y> <z>

# 示例：前往坐标 (100, 64, 200)
@goto 100 64 200

# 跟随特定玩家
@follow <玩家名>

# 示例：跟随玩家 "Steve"
@follow Steve
```

### ⚙️ 系统控制命令

```bash
# 停止当前所有任务
@stop

# 暂停/恢复机器人
@pause
@unpause

# 查看当前状态
@status
```

## 常用物品名称

Alto Clef 支持 Minecraft 的标准物品名称，包括：

- **基础资源**: `cobblestone`, `dirt`, `log`, `coal`, `iron_ore`, `diamond`
- **合成物品**: `stick`, `planks`, `crafting_table`, `furnace`
- **工具**: `wooden_pickaxe`, `stone_pickaxe`, `iron_pickaxe`, `diamond_pickaxe`
- **食物**: `wheat`, `carrot`, `potato`, `bread`, `cooked_beef`

> 💡 **提示**: 使用 `@list` 命令查看所有支持的物品和任务。

## 自动化生存示例

```bash
# 开始全自动生存模式（收集食物、防御怪物、自动进食）
@gamer

# 或者设置空闲命令，在没有其他任务时自动执行
# 在 altoclef_settings.json 中设置 idleCommand: "gamer"
```

## 快捷键

- **Ctrl + K**: 立即停止所有任务

## 查看进度

启用任务链显示后，屏幕左上角会显示当前正在执行的任务：

1. 打开 `altoclef_settings.json`
2. 确保 `showTaskChains` 设置为 `true`
3. 保存文件并重新加载设置 (`@reloadsettings`)

现在你可以在游戏界面看到 Alto Clef 的实时进度！