# AltoClef 跟踪器系统详细文档

## 概述

AltoClef 跟踪器系统是 Minecraft 自动化机器人项目的核心组件之一，负责跟踪游戏中的各种状态、物品、实体等信息，为任务系统提供实时数据支持。该系统采用了模块化设计，通过不同的跟踪器类处理不同类型的信息。

## 跟踪器系统架构

### 基础架构

- **Tracker (src/main/java/adris/altoclef/trackers/Tracker.java)**
  - 跟踪器基类，定义了所有跟踪器的基本功能
  - 提供了脏状态管理机制，通过 `setDirty()` 和 `isDirty()` 方法管理更新时机
  - 通过 `ensureUpdated()` 方法确保状态已更新
  - 抽象方法 `updateState()` 和 `reset()` 由子类实现

- **TrackerManager (src/main/java/adris/altoclef/trackers/TrackerManager.java)**
  - 跟踪器管理器，集中管理所有跟踪器的生命周期
  - 负责调用所有跟踪器的 `tick()` 方法，统一管理更新时机
  - 在玩家离开世界时重置所有跟踪器状态
  - 通过 `addTracker()` 方法注册跟踪器

## 核心跟踪器组件

### 实体跟踪器

- **EntityTracker (src/main/java/adris/altoclef/trackers/EntityTracker.java)**
  - 跟踪游戏世界中的所有实体，包括物品掉落、生物、玩家等
  - 维护物品掉落位置映射 (`itemDropLocations`) 和实体类型映射 (`entityMap`)
  - 提供查找最近掉落物品、最近实体等功能
  - 跟踪投射物、敌对生物和玩家信息
  - 包含碰撞检测和可达性检测功能

### 方块跟踪器

- **BlockScanner (src/main/java/adris/altoclef/trackers/BlockScanner.java)**
  - 负责扫描和跟踪方块位置
  - 维护已追踪和已扫描的方块位置 (`trackedBlocks`, `scannedBlocks`)
  - 实现区域扫描和距离计算功能
  - 提供可达性检查和黑名单机制
  - 支持定期重新扫描以更新数据

- **SimpleChunkTracker (src/main/java/adris/altoclef/trackers/SimpleChunkTracker.java)**
  - 跟踪当前已加载的区块状态
  - 通过事件总线监听区块加载和卸载事件
  - 提供区块加载状态检查和区块扫描功能

- **MiscBlockTracker (src/main/java/adris/altoclef/trackers/MiscBlockTracker.java)**
  - 跟踪特殊方块相关的信息，如下界传送门
  - 记录维度切换时的传送门位置
  - 提供传送门位置的验证和获取功能

### 合成配方跟踪器

- **CraftingRecipeTracker (src/main/java/adris/altoclef/trackers/CraftingRecipeTracker.java)**
  - 跟踪和管理合成配方信息
  - 维护物品到配方的映射 (`itemRecipeMap`) 和配方结果映射 (`recipeResultMap`)
  - 提供配方查询和目标生成功能
  - 自动从 Minecraft 配方管理器加载合成配方

## 黑名单系统

### 抽象黑名单机制

- **AbstractObjectBlacklist (src/main/java/adris/altoclef/trackers/blacklisting/AbstractObjectBlacklist.java)**
  - 提供黑名单基础功能，用于标记不可达的对象
  - 通过失败次数和距离阈值判断对象可达性
  - 维护最佳距离和工具信息以决定是否重置黑名单状态

- **EntityLocateBlacklist (src/main/java/adris/altoclef/trackers/blacklisting/EntityLocateBlacklist.java)**
  - 专门用于实体的黑名单，继承自 AbstractObjectBlacklist

- **WorldLocateBlacklist (src/main/java/adris/altoclef/trackers/blacklisting/WorldLocateBlacklist.java)**
  - 专门用于世界坐标（方块位置）的黑名单，继承自 AbstractObjectBlacklist

## 存储跟踪器系统

### 存储跟踪器主类

- **ItemStorageTracker (src/main/java/adris/altoclef/trackers/storage/ItemStorageTracker.java)**
  - 统一管理所有存储相关的信息
  - 整合玩家物品栏和容器内容跟踪
  - 提供物品数量查询、槽位检查等功能
  - 支持多种存储类型的统一接口

### 子跟踪器组件

- **InventorySubTracker (src/main/java/adris/altoclef/trackers/storage/InventorySubTracker.java)**
  - 专门跟踪玩家物品栏和当前界面中的物品
  - 维护玩家物品栏和容器物品的映射关系
  - 提供槽位定位和容量检查功能

- **ContainerSubTracker (src/main/java/adris/altoclef/trackers/storage/ContainerSubTracker.java)**
  - 跟踪各种容器（箱子、熔炉、酿造台等）中的物品
  - 监听方块交互和界面打开事件
  - 维护多维度的容器缓存系统

### 容器类型系统

- **ContainerType (src/main/java/adris/altoclef/trackers/storage/ContainerType.java)**
  - 定义不同类型的容器（箱子、末影箱、潜影盒、熔炉、酿造台等）
  - 提供容器类型检测和匹配功能
  - 支持界面处理器和槽位类型的验证

- **ContainerCache (src/main/java/adris/altoclef/trackers/storage/ContainerCache.java)**
  - 缓存容器内容信息
  - 维护物品数量统计和空槽数量
  - 提供容器状态检查功能

## 系统特性

### 性能优化
- 脏状态管理：只有在需要时才更新跟踪器状态
- 数据缓存：使用哈希表快速查找和访问数据
- 区块扫描：按区块进行高效扫描

### 鲁棒性
- 黑名单机制：自动标记不可达的方块和实体
- 数据验证：定期检查缓存数据的有效性
- 错误恢复：在数据不一致时自动重置

### 多维度支持
- 维度感知：根据不同维度维护独立的数据
- 跨维度缓存：支持在不同维度间切换时保持数据

## 使用场景

### 任务系统集成
跟踪器系统为任务系统提供实时的游戏状态信息，包括：
- 物品位置和数量
- 实体位置和类型
- 可用配方和合成路径
- 存储容器内容

### 路径规划支持
- 提供可达性信息用于路径规划
- 实时更新世界状态用于动态路径调整

### 资源管理
- 跟踪玩家和容器中的物品
- 管理合成配方和材料需求
- 优化资源分配和存储

## 扩展性

跟踪器系统设计具有良好的扩展性：
- 新的跟踪器可以轻松继承 `Tracker` 基类
- 系统自动管理所有注册的跟踪器
- 模块化设计便于添加新的跟踪功能
- 事件驱动架构支持实时数据更新

跟踪器系统是 AltoClef 项目的核心组件，为自动化机器人提供了稳定、高效的游戏状态感知能力，是实现复杂自动化任务的基础。