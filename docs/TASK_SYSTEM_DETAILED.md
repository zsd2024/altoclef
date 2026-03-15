# AltoClef 任务系统详细文档

## 概述

AltoClef 的任务系统是一个功能强大的自动化任务执行框架，用于在 Minecraft 环境中执行各种复杂的行动。任务系统基于状态机的设计模式，能够处理从简单的移动到复杂的速通任务。

## 任务系统核心结构

### 1. 任务基础类 (Task)

所有任务的基类，定义了任务的基本结构和生命周期方法：
- `onStart()` - 任务开始时调用
- `onTick()` - 任务每 tick 执行时调用
- `onStop()` - 任务结束时调用
- `isFinished()` - 检查任务是否完成
- `getPriority()` - 获取任务优先级

### 2. 任务执行流程

任务系统采用优先级驱动的方式，优先级最高的任务会被执行。

## Movement 任务模块

Movement 模块包含用于在世界中移动和导航的各种任务。

### 基础移动任务

- `GetToBlockTask` - 前往指定方块位置
- `GetToEntityTask` - 前往实体（包含解卡机制）
- `GetToXZTask` - 前往指定XZ坐标
- `GetToYTask` - 前往指定Y坐标
- `GetWithinRangeOfBlockTask` - 进入方块指定范围内

### 高级移动任务

- `FastTravelTask` - 快速传送任务，通过下界传送门快速移动长距离
- `FollowPlayerTask` - 跟随指定玩家
- `GetCloseToBlockTask` - 尽可能接近方块（即使无法到达）
- `GetOutOfWaterTask` - 从水中出来（使用MLG技术）
- `RunAwayFrom*Task` 系列 - 远离危险的各类任务

### 探索和搜索任务

- `SearchChunksExploreTask` - 探索连续区块
- `SearchChunkForBlockTask` - 在区块中搜索特定方块
- `SearchWithinBiomeTask` - 在指定生物群系中搜索
- `Locate*Task` 系列 - 位置定位任务

### 特殊移动任务

- `MLGBucketTask` - 水桶缓冲任务（MLG：通过水桶缓冲掉落）
- `TimeoutWanderTask` - 超时漫步任务（用于摆脱困境）
- `SafeRandomShimmyTask` - 安全随机摆动任务（用于解卡）

## SpeedRun 任务模块

SpeedRun 模块专注于Minecraft速通模式，包含各种优先级任务。

### 优先级计算系统

- `PriorityCalculator` - 优先级计算器接口
- `ItemPriorityCalculator` - 基于物品数量的优先级计算器
- `DistancePriorityCalculator` - 基于距离的优先级计算器
- `CollectFoodPriorityCalculator` - 食物收集优先级计算器

### 优先级任务系统

- `PriorityTask` - 优先级任务抽象基类
- `ActionPriorityTask` - 动作优先级任务（最通用的优先级任务）
- `CraftItemPriorityTask` - 制作物品优先级任务
- `MineBlockPriorityTask` - 挖掘方块优先级任务
- `ResourcePriorityTask` - 资源收集优先级任务
- `RecraftableItemPriorityTask` - 可重制物品优先级任务

## 任务系统特性

### 进度检查器

- `MovementProgressChecker` - 移动进度检查器，防止任务卡死
- 各种自定义进度检查器用于特定场景

### 互斥锁和同步

- 使用 mutex 和同步机制防止任务冲突
- 处理并发访问和资源共享

### 自定义目标系统

- `CustomBaritoneGoalTask` - 基于 Baritone 路径系统的自定义目标任务
- 支持复杂的路径规划和导航

### 解卡机制

- 自动检测和处理卡住情况
- 包含多种解卡策略（摆动、随机移动等）

## TaskCatalogue 任务注册表

TaskCatalogue 是任务的统一注册和管理中心，提供：
- 任务的统一创建接口
- 任务的缓存和重用
- 任务的标准化参数处理

## 任务系统最佳实践

### 创建新任务的步骤

1. 确定任务类型（移动、制作、收集等）
2. 选择合适的基类
3. 实现必要的生命周期方法
4. 添加适当的进度检查
5. 考虑解卡机制
6. 处理边界情况和异常

### 任务设计原则

- 任务应该是可重入的
- 任务应有明确的完成条件
- 任务应优雅地处理中断
- 任务应有适当的优先级

### 任务间的协调

- 使用互斥锁防止资源冲突
- 通过状态变量协调任务状态
- 适当的任务依赖管理

## 速通特定功能

### 优先级驱动系统

- 动态计算任务优先级
- 根据当前状态和需求调整优先级
- 支持条件触发的优先级调整

### 资源管理

- 智能物品收集
- 库存管理
- 制作需求分析

### 无用物品过滤

- UselessItems 类定义了速通中无用的物品
- 避免收集无用物品浪费时间

### 示例任务 (Example Tasks)

- `ExampleTask` - 示例任务
  - 演示如何创建复合任务
  - 包括获取物品和放置方块的流程
  - 展示任务间的协调机制

- `ExampleTask2` - 示例任务2
  - 演示高级行为管理
  - 包括避免破坏树木的保护机制
  - 演示区块扫描和定位功能

### 建筑任务 (Construction Tasks)

#### 基础建筑任务

- `PutOutFireTask` - 熄灭指定位置的火焰
  - 通过左键点击火焰方块来熄灭火焰
  - 使用空手进行互动
  - 完成条件：目标位置不再有火焰或灵魂火

- `PlaceBlockTask` - 在指定位置放置方块
  - 支持放置特定类型方块或可丢弃方块
  - 使用 Baritone 进行方块放置
  - 包含替代放置策略（如从上方放置）
  - 完成条件：目标位置放置了指定方块

- `PlaceBlockNearbyTask` - 在附近位置放置方块
  - 也称为"熊策略"任务
  - 优先选择平坦区域放置方块
  - 支持自定义放置位置筛选条件
  - 完成条件：在附近成功放置了方块

- `DestroyBlockTask` - 破坏指定位置的方块
  - 处理各种障碍方块（藤蔓、梯子等）
  - 避免破坏重要结构（如掠夺者的羊毛）
  - 包含解困机制
  - 完成条件：目标位置变为空气方块

- `ClearLiquidTask` - 清除液体
  - 优先使用桶收集液体源
  - 如无桶则使用方块阻挡
  - 完成条件：目标位置没有液体

- `ClearRegionTask` - 清理指定区域
  - 使用 Baritone 的清除区域功能
  - 遍历整个区域确保所有方块被清除
  - 完成条件：整个区域都变为空气方块

#### 高级建筑任务

- `PlaceObsidianBucketTask` - 使用桶技术放置黑曜石
  - 使用岩浆桶和水桶生成黑曜石
  - 构建铸造框架确保成功生成
  - 自动收集所需材料
  - 完成条件：目标位置生成黑曜石且无多余水

- `ProjectileProtectionWallTask` - 投射物防护墙
  - 检测敌对骷髅实体
  - 在玩家和实体之间放置方块阻挡投射物
  - 使用可丢弃方块快速构建
  - 完成条件：防护墙已建立或敌对实体消失

- `PlaceStructureBlockTask` - 放置结构方块
  - 在指定位置放置可丢弃方块
  - 用于支持其他建筑任务
  - 完成条件：目标位置放置了方块

#### 复合建筑任务

- `ConstructIronGolemTask` - 建造铁傀儡
  - 通过放置4个铁块和1个雕刻南瓜来建造铁傀儡
  - 自动寻找合适建造位置
  - 完成条件：铁傀儡成功生成

- `ConstructNetherPortalBucketTask` - 用桶建造下界传送门
  - 使用水和岩浆桶浇筑技术建造传送门
  - 目前最可靠的传送门建造方法
  - 完成条件：下界传送门成功建造

- `ConstructNetherPortalObsidianTask` - 用黑曜石建造下界传送门
  - 使用黑曜石方块手动建造传送门框架
  - 更直接的建造方法
  - 完成条件：下界传送门成功建造

- `ConstructNetherPortalSpeedrunTask` - 速通方式建造下界传送门
  - 使用结构框架和浇筑技术快速建造
  - 由于水溢出问题成功率较低
  - 完成条件：下界传送门成功建造

### 物品栏任务 (Slot Tasks)

#### 基础物品栏操作

- `ClickSlotTask` - 点击槽位
  - 支持不同鼠标按钮和操作类型
  - 提供灵活的槽位交互接口
  - 完成条件：槽位点击操作完成

- `ThrowCursorTask` - 丢弃光标物品
  - 将光标中的物品丢弃到世界中
  - 完成条件：光标为空

- `EnsureFreeCursorSlotTask` - 确保光标槽空闲
  - 释放光标槽以便进行其他操作
  - 优先将物品移至背包
  - 完成条件：光标槽为空

- `EnsureFreeInventorySlotTask` - 确保背包有空槽
  - 通过丢弃可丢弃物品来腾出空间
  - 完成条件：背包有空槽可用

- `EnsureFreePlayerCraftingGridTask` - 确保合成网格空闲
  - 清空玩家2x2合成网格中的物品
  - 完成条件：合成网格为空

#### 物品移动任务

- `MoveItemToSlotTask` - 物品移动基类
  - 提供物品移动的核心逻辑
  - 支持数量控制和最佳槽位选择

- `MoveItemToSlotFromContainerTask` - 从容器移动物品
  - 从容器中移动物品到指定槽位
  - 完成条件：物品成功移动到目标槽位

- `MoveItemToSlotFromInventoryTask` - 从背包移动物品
  - 从玩家背包移动物品到指定槽位
  - 完成条件：物品成功移动到目标槽位

- `MoveInaccessibleItemToInventoryTask` - 将不可访问物品移回背包
  - 处理在容器中无法访问的物品
  - 完成条件：目标物品在背包中可访问

#### 合成输出处理

- `ReceiveCraftingOutputSlotTask` - 接收合成输出
  - 从合成输出槽获取合成结果
  - 支持一次性获取或分批获取
  - 完成条件：获得所需数量的合成结果

### 合成任务 (Crafting Tasks)

- `CraftGenericManuallyTask` - 手动合成
  - 通过手动放置材料到合成网格
  - 适用于无配方书可用的场景
  - 自动管理合成材料和输出

- `CraftGenericWithRecipeBooksTask` - 用配方书合成
  - 使用配方书直接发送合成请求
  - 更高效但需要配方书支持
  - 自动清理合成网格

### 资源任务 (Resource Tasks)

- `CollectFoodTask` - 收集食物
  - 收集各种食物来源，包括击杀动物、收获作物等
  - 支持生食烹饪和原材料制作
  - 完成条件：收集到足够的食物单位

### 速通任务 (Speedrun Tasks)

- `BeatMinecraftTask` - 完成游戏任务
  - 执行完整Minecraft游戏流程
  - 包括获取末影之眼、进入末地、击败末影龙
  - 完成条件：击败末影龙并完成游戏

- `KillEnderDragonTask` - 击败末影龙
  - 击败末地中的末影龙
  - 处理龙的攻击和战斗策略
  - 完成条件：末影龙死亡

- `KillEnderDragonWithBedsTask` - 用床击败末影龙
  - 使用床爆炸击败末影龙的策略
  - 包括床的放置和引爆时机
  - 完成条件：末影龙死亡

- `WaitForDragonAndPearlTask` - 等待龙并投掷珍珠
  - 在末地等待龙停靠并投掷末影珍珠
  - 处理龙息和火球躲避
  - 完成条件：龙被击败或珍珠成功投掷

## 任务系统特性

### 任务调度
- 任务按优先级执行
- 支持任务中断和恢复
- 自动处理任务依赖关系

### 行为管理
- 任务可以推送/弹出行为栈
- 支持避免破坏/放置特定方块
- 防护物品不会被意外丢弃

### 容错机制
- 内置进度检查器防止卡死
- 自动重试失败的操作
- 多重替代策略

### 性能优化
- 智能缓存机制
- 并行处理可能的子任务
- 预测性物品管理

### 任务使用示例

任务系统通过 `TaskCatalogue` 进行统一管理，用户可以通过简单的接口获取复杂任务：

```java
// 获取获取物品的任务
Task getItemTask = TaskCatalogue.getItemTask(Items.DIAMOND_PICKAXE, 1);

// 获取合成任务
Task craftTask = TaskCatalogue.getMakeItemTask(Items.STICK, 32);

// 获取移动任务
Task moveTask = TaskCatalogue.getGoToDimensionTask(Dimension.NETHER);
```

任务可以被组合成更复杂的流程，系统会自动处理依赖关系和执行顺序。

## 总结

AltoClef 的任务系统是一个高度模块化和可扩展的框架，支持从基础移动到复杂速通任务的执行。其设计重点在于可靠性、效率和可维护性，通过优先级系统和进度检查器确保任务的正常执行。