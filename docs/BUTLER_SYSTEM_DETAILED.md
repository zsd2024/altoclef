# AltoClef 助手系统详细说明文档

## 系统概述

AltoClef 助手系统是一个允许授权玩家向机器人发送指令以执行的系统。该系统有效地使机器人功能像一个仆人或管家，为授权用户提供服务。

## 核心组件

### 1. Butler 类 (Butler.java)

这是助手系统的核心类，主要功能包括：

- 处理私聊消息的接收和解析
- 用户认证检查
- 指令执行管理
- 与机器人系统的集成
- 消息发送功能

**主要功能：**
- `isUserAuthorized()`: 检查用户是否已授权
- `onLog()`: 记录日志消息并发送给当前用户
- `executeWhisper()`: 执行私聊指令
- `receiveWhisper()`: 接收和处理私聊消息

### 2. ButlerConfig 类 (ButlerConfig.java)

提供助手系统的所有配置选项：

**主要配置项：**
- `useButlerBlacklist`: 是否使用黑名单拒绝用户
- `useButlerWhitelist`: 是否使用白名单仅接受指定用户
- `whisperFormats`: 定义私聊消息格式
- `whisperFormatDebug`: 是否启用调试模式
- `sendAuthorizationResponse`: 是否向未授权用户发送响应
- `failedAuthorizationResponse`: 未授权时的响应消息
- `requirePrefixMsg`: 是否要求消息前缀

### 3. UserAuth 类 (UserAuth.java)

处理用户授权逻辑，包括：

- 黑名单检查（优先级最高）
- 白名单验证
- 用户列表文件管理

**授权逻辑：**
1. 首先检查黑名单 - 如果用户在黑名单中则拒绝
2. 如果启用白名单，检查用户是否在白名单中
3. 默认情况下接受所有用户

### 4. UserListFile 类 (UserListFile.java)

管理用户列表文件（黑/白名单），支持：
- 从文件加载用户列表
- 检查用户是否在列表中
- 配置文件格式解析

### 5. WhisperChecker 类 (WhisperChecker.java)

负责解析和验证私聊消息格式，确保消息符合预期格式。

**解析逻辑：**
- 支持 {from}、{to}、{message} 占位符
- 验证消息是否发送给当前用户
- 防止重复消息处理

## 工作流程

1. **消息接收**：通过事件总线接收聊天消息
2. **格式验证**：检查消息是否符合私聊格式
3. **用户认证**：验证发送者是否被授权
4. **指令执行**：执行用户发送的指令
5. **结果反馈**：将执行结果反馈给用户

## 安全特性

- **双重验证**：支持黑名单和白名单双重安全机制
- **格式验证**：确保接收的是私聊消息而非普通聊天
- **机器人检测**：防止其他机器人发送的指令
- **权限控制**：严格的用户权限验证

## 配置文件

助手系统使用以下配置文件：
- `configs/butler.json`: 主配置文件
- `altoclef_butler_blacklist.txt`: 黑名单文件
- `altoclef_butler_whitelist.txt`: 白名单文件

## 使用示例

授权用户可以通过私聊向机器人发送指令，例如：
```
<用户名> <机器人名> <指令>
```

机器人将验证用户权限并执行相应的指令。

## 调试功能

通过 `whisperFormatDebug` 配置可以启用调试模式，帮助用户设置正确的私聊格式。