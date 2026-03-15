# AltoClef 项目整体架构概述

## 项目简介
AltoClef 是一个基于 Baritone 的 Minecraft 自动化客户端机器人，能够自主完成游戏任务。该项目支持多个 Minecraft 版本（1.16.5 到 1.21.1），使用 ReplayMod 预处理器实现多版本兼容。

## 核心模块结构

### 主要入口
- AltoClef.java - 主要入口点和核心控制器
- AltoClefCommands.java - 命令系统入口
- TaskCatalogue.java - 任务注册表

### 任务系统 (tasks/)
- 任务实现的核心目录，包含各种自动化任务
- 涵盖从基础挖掘、制作到复杂生存任务的完整实现
- 任务之间可以组合形成更复杂的自动化流程

### 功能链 (chains/)
- DeathMenuChain - 死亡菜单处理
- FoodChain - 食物管理
- MobDefenseChain - 怪物防御
- PreEquipItemChain - 预装备物品
- WorldSurvivalChain - 世界生存逻辑
- 其他功能链模块

### 命令系统 (commands/)
- 包含各种用户可调用的命令实现
- 提供与机器人交互的接口

### 跟踪器系统 (trackers/)
- 负责跟踪游戏状态、物品、实体等信息
- 为任务系统提供实时数据支持

### 控制系统 (control/)
- 机器人核心控制逻辑
- 状态管理和行为决策

### 工具类 (util/)
- 通用工具函数
- 数据结构和辅助功能

### 但ler系统 (butler/)
- 机器人助手功能
- 用户认证和权限管理

### UI系统 (ui/)
- 用户界面相关功能
- 交互和显示组件

## 关键特性
- 多版本兼容：支持从1.16.5到1.21.1的多个Minecraft版本
- 自主导航：利用Baritone进行路径规划和移动
- 任务自动化：可执行复杂的游戏内任务序列
- 状态管理：实时跟踪游戏状态和物品
- 命令接口：提供用户交互命令系统

## 构建系统
- 使用 Gradle 构建
- Fabric Loom 插件支持
- 支持开发模式下的本地 Baritone 集成
- JVM Downgrader 实现版本兼容性