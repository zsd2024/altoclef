# AltoClef 核心模块详细文档

## 1. AltoClef - 主要入口点和核心控制器

### 概述
`AltoClef` 类是 AltoClef 项目的核心入口点和主要控制器，实现了 ModInitializer 接口。它是整个自动化机器人的中心访问点，负责初始化和协调所有主要组件。

### 主要功能

#### 核心管理器
- `CommandExecutor commandExecutor` - 命令执行器，处理所有用户命令
- `TaskRunner taskRunner` - 任务运行器，管理任务的执行
- `TrackerManager trackerManager` - 跟踪器管理器，管理各种跟踪器
- `BotBehaviour botBehaviour` - 机器人行为控制器，管理临时保护等行为
- `PlayerExtraController extraController` - 玩家额外控制器，处理额外的玩家控制

#### 任务链
- `UserTaskChain userTaskChain` - 用户任务链，执行用户命令（如获取钻石、通关游戏）
- `FoodChain foodChain` - 食物链，接管控制权以进食
- `MobDefenseChain mobDefenseChain` - 怪物防御链，接管控制权以防御怪物
- `MLGBucketFallChain mlgBucketChain` - MLG水桶链，接管控制权以执行水桶自救

#### 跟踪器
- `ItemStorageTracker storageTracker` - 物品存储跟踪器，跟踪背包和存储容器中的物品
- `EntityTracker entityTracker` - 实体跟踪器，跟踪已加载的实体
- `BlockScanner blockScanner` - 区块扫描器，跟踪方块及其位置
- `SimpleChunkTracker chunkTracker` - 简单区块跟踪器，跟踪区块加载/可见状态
- `CraftingRecipeTracker craftingRecipeTracker` - 合成配方跟踪器，管理可用配方列表

### 关键方法
- `onInitialize()` - 初始化方法，当Minecraft处于mod加载就绪状态时运行
- `onInitializeLoad()` - 实际启动点，由mixin控制
- `onClientTick()` - 客户端Tick，同步游戏循环
- `runUserTask(Task task)` - 运行用户任务
- `stopTasks()` - 停止当前任务
- `log(String message)` - 记录消息到控制台和向使用机器人作为管家的玩家发送消息

## 2. AltoClefCommands - 命令系统入口

### 概述
`AltoClefCommands` 类初始化 AltoClef 的内置命令系统。它负责注册和管理所有可用的命令。

### 注册的命令

#### 资源获取命令
- `GetCommand` - 获取命令，用于获取特定物品
- `ListCommand` - 列表命令，列出可用资源
- `EquipCommand` - 装备命令，装备物品

#### 存储命令
- `DepositCommand` - 存储命令，将物品存入容器
- `StashCommand` - 藏匿命令，快速存储物品

#### 导航命令
- `GotoCommand` - 前往命令，前往特定位置
- `CoordsCommand` - 坐标命令，显示当前位置

#### 状态和控制命令
- `StatusCommand` - 状态命令，显示机器人状态
- `InventoryCommand` - 背包命令，显示背包内容
- `StopCommand` - 停止命令，停止当前任务
- `PauseCommand` - 暂停命令，暂停机器人
- `UnPauseCommand` - 恢复命令，恢复机器人

#### 特殊命令
- `IdleCommand` - 空闲命令，设置空闲行为
- `FoodCommand` - 食物命令，处理食物需求
- `MeatCommand` - 肉类命令，获取肉类
- `HeroCommand` - 英雄命令，特殊功能
- `GamerCommand` - 游戏者命令，特殊功能
- `MarvionCommand` - Marvion命令，特殊功能

#### 结构和定位命令
- `LocateStructureCommand` - 定位结构命令，查找结构
- `FollowCommand` - 跟随命令，跟随玩家

#### 工具命令
- `TestCommand` - 测试命令，用于测试功能
- `ScanCommand` - 扫描命令，扫描环境
- `GiveCommand` - 给予命令，给予物品
- `SetGammaCommand` - 设置伽马值命令，调整亮度

### 初始化方法
- `init()` - 初始化所有命令并将其注册到命令执行器

## 3. TaskCatalogue - 任务注册表

### 概述
`TaskCatalogue` 类包含所有可获得资源的硬编码列表。大多数资源对应单个物品，但某些资源（如"log"或"door"）包含一系列物品。

### 核心组件

#### 数据结构
- `nameToItemMatches` - 资源名称到物品数组的映射
- `nameToResourceTask` - 资源名称到资源任务的映射
- `itemToResourceTask` - 物品到资源任务的映射
- `resourcesObtainable` - 可获得的资源集合

### 主要资源类别

#### 原始资源
- 木材类：原木、树叶、树苗
- 矿物类：煤炭、铁、金、铜、钻石、绿宝石、红石、青金石、下界石英
- 岩石类：圆石、砂岩、红砂岩、各种变种石头
- 生物资源：骨头、火药、末影珍珠、蜘蛛眼、皮革、羽毛、腐肉、粘液球

#### 材料
- 淬炼：石锭、铁锭、金锭、铜锭、下界合金碎片
- 块：各种金属块、矿物块、装饰块
- 合成材料：木板、木棍、玻璃、粘土块

#### 工具和装备
- 工具：各种等级的镐、锹、剑、斧、锄
- 装备：各种材质的盔甲
- 锻造：下界合金装备

#### 家具和建筑
- 结构：工作台、熔炉、箱子、桶、告示牌
- 建筑：楼梯、台阶、墙、栅栏、门、活板门
- 红石：活塞、中继器、比较器、拉杆、压力板

#### 食物
- 肉类：各种生肉和熟肉
- 农作物：小麦、胡萝卜、土豆、甜菜根
- 合成品：面包、蛋糕、曲奇

### 核心方法

#### 资源获取
- `getItemTask(String name, int count)` - 根据资源名称返回任务
- `getItemTask(Item item, int count)` - 根据物品返回任务
- `getItemMatches(String name)` - 获取资源名称匹配的物品
- `getSquashedItemTask(ItemTarget... targets)` - 返回获取多个资源的任务

#### 任务类型
- `simple()` - 简单任务（如收集）
- `mine()` - 挖掘任务（如挖掘方块）
- `smelt()` - 淬炼任务（如熔炉处理）
- `shapedRecipe2x2()` - 2x2合成配方
- `shapedRecipe3x3()` - 3x3合成配方
- `mob()` - 生物击杀任务
- `crop()` - 农作物采集任务

#### 工具助手
- `tools()` - 注册工具制造任务
- `armor()` - 注册盔甲制造任务
- `woodTasks()` - 注册木制物品任务
- `colorfulTasks()` - 注册彩色物品任务

### 初始化
- `init()` - 初始化所有预定义的资源任务
- `static{}` - 静态初始化块，定义所有资源和任务映射

## 总结

这三个核心模块共同构成了 AltoClef 的基础架构：

1. **AltoClef** - 提供主要入口点和核心控制逻辑
2. **AltoClefCommands** - 管理命令系统和用户交互
3. **TaskCatalogue** - 提供完整的资源获取和任务系统

这种设计使 AltoClef 能够灵活地处理复杂的 Minecraft 自动化任务，从资源收集到建筑，从食物管理到战斗。