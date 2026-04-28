# 配置文件详解

Alto Clef 的配置文件为 `altoclef_settings.json`，位于 Minecraft 的配置目录中。

## 配置文件位置

- **Windows**: `%appdata%\.minecraft\altoclef_settings.json`
- **macOS**: `~/Library/Application Support/minecraft/altoclef_settings.json`
- **Linux**: `~/.minecraft/altoclef_settings.json`

## 配置项说明

### 显示和界面

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `showDebugTickMs` | boolean | `false` | 显示 Alto Clef 每帧处理时间的调试条 |
| `showTaskChains` | boolean | `true` | 在屏幕左上角显示当前任务链 |
| `showTimer` | boolean | `true` | 显示计时器 |

### 命令和日志

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `commandPrefix` | string | `"@"` | 命令前缀（例如 `@get`） |
| `chatLogPrefix` | string | `"[Alto Clef] "` | 聊天日志前缀 |
| `logLevel` | string | `"NORMAL"` | 日志级别：`ALL`、`NORMAL`、`WARN`、`ERROR`、`NONE` |
| `hideAllWarningLogs` | boolean | `false` | 隐藏所有警告日志 |

### 资源收集

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `resourcePickupDropRange` | float | `-1` | 捡起掉落物品的最大距离（-1 表示禁用） |
| `resourceChestLocateRange` | float | `500` | 搜索箱子获取资源的最大范围 |
| `resourceMineRange` | float | `100` | 优先挖掘附近方块而非合成的距离 |
| `minimumFoodAllowed` | int | `0` | 背包中保留的最少食物数量 |
| `foodUnitsToCollect` | int | `0` | 食物不足时收集的食物数量 |
| `collectPickaxeFirst` | boolean | `true` | 优先获取镐子再执行其他任务 |

### 生存和防御

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `mobDefense` | boolean | `true` | 启用怪物防御系统 |
| `autoEat` | boolean | `true` | 自动进食 |
| `autoMLGBucket` | boolean | `true` | 自动水桶自救 |
| `replantCrops` | boolean | `true` | 收获作物后自动重新种植 |
| `avoidDrowning` | boolean | `true` | 避免溺水 |
| `extinguishSelfWithWater` | boolean | `true` | 着火时自动用水灭火 |

### 自动化行为

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `autoReconnect` | boolean | `true` | 断线后自动重连 |
| `autoRespawn` | boolean | `true` | 死亡后自动重生 |
| `idleCommand` | string | `""` | 空闲时执行的命令 |
| `deathCommand` | string | `""` | 死亡后执行的命令 |

### 物品管理

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `throwawayItems` | Item[] | `[大量基础方块]` | 优先丢弃的物品列表 |
| `importantItems` | Item[] | `[重要物品]` | 永远不会丢弃的物品 |
| `reservedBuildingBlockCount` | int | `64` | 保留的建筑方块数量 |
| `dontThrowAwayCustomNameItems` | boolean | `true` | 不丢弃有自定义名称的物品 |
| `dontThrowAwayEnchantedItems` | boolean | `true` | 不丢弃附魔物品 |

### 维度和传送

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `overworldToNetherBehaviour` | enum | `BUILD_PORTAL_VANILLA` | 主世界到下界的行为：`BUILD_PORTAL_VANILLA` 或 `GO_TO_HOME_BASE` |
| `netherFastTravelWalkingRange` | int | `600` | 下界快速旅行的步行范围 |
| `homeBasePosition` | BlockPos | `(0, 64, 0)` | 家园基地坐标 |

### 保护区域

```json
"areasToProtect": [
  {
    "start": "-10, 0, -10",
    "end": "10, 255, 10"
  }
]
```

`areasToProtect` 定义了不会被挖掘或破坏的区域，用于防止在出生点或其他重要区域造成破坏。

## 重载配置

在游戏中使用 `@reloadsettings` 命令可以重新加载配置文件，无需重启游戏。

> ⚠️ **警告**: 修改配置文件时请确保 JSON 格式正确，否则可能导致配置加载失败。