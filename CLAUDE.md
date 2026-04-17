# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

FuckLocation 是一个 Xposed 模块，用于在系统层面控制位置权限。它通过 Hook Android 框架层，向指定应用返回自定义/虚假位置信息，无需 root 级别的模拟位置设置。

**目标版本**: Android 10 (Q) 到 Android 12 (S)，针对不同版本有独立实现。

## 构建命令

```bash
# 构建调试版 APK
./gradlew assembleDebug

# 构建发布版 APK
./gradlew assembleRelease

# 清理构建
./gradlew clean

# 运行单元测试
./gradlew test

# 运行设备测试（需连接设备）
./gradlew connectedAndroidTest
```

APK 输出路径: `app/build/outputs/apk/debug/app-debug.apk`

## 架构

### Xposed 模块结构

- **入口点**: `HookEntry.kt` 实现 `IXposedHookZygoteInit` 和 `IXposedHookLoadPackage`
- **Xposed 作用域**: 定义在 `res/values/arrays.xml` - 目标进程为 `android` 和 `com.android.phone`

### 按进程划分的 Hook 目标

1. **`android` 进程** (system_server): Hook Android 框架中的位置服务
2. **`com.android.phone` 进程**: Hook 电话/基站位置 API
3. **模块自身**: Hook 用于报告模块激活状态

### 按版本划分的定位 Hooker

针对不同 Android 版本维护独立的 Hook 实现：

| 版本 | Hooker 类 | API 级别 |
|------|----------|---------|
| Android 12+ | `LocationHookerAfterS` | 31+ |
| Android 11 | `LocationHookerR` | 30 |
| Android 10 及更早 | `LocationHookerPreQ` | ≤29 |

### 核心组件

**ConfigGateway** (`xposed/helpers/ConfigGateway.kt`):
- 单例模式，作为用户界面 App 与运行在 system_server 中的 Xposed Hook 之间的通信桥梁
- 使用"魔数"通过隐藏 API 识别跨进程调用
- 配置存储在 `/data/system/fuck_location_<随机>/` (whiteList.json, fakeLocation.json)
- 提供白名单检查和虚假位置数据获取

**Location Hookers** (`xposed/location/`):
- Hook `LocationManagerService`、`LocationProviderManager` 方法
- 拦截 `getLastLocation`、`getCurrentLocation`、`onReportLocation`
- 为白名单应用修改 `Location` 对象

**GNSS Hookers** (`xposed/location/gnss/`):
- 阻止或修改白名单应用的 GPS/GNSS 回调

**基站 Hookers** (`xposed/cellar/`):
- Hook `PhoneInterfaceManager` 和 `TelephonyRegistry` 实现基站定位拦截

**厂商适配** (`xposed/helpers/workround/`):
- MIUI、Oplus/ColorOS、LG 等厂商的特殊位置实现适配

### 数据流

1. 用户通过 App 界面设置虚假位置 → 通过 ConfigGateway 保存
2. ConfigGateway 使用隐藏 API 调用（带魔数）写入 system_server 的数据目录
3. 定位 Hooker 在应用请求位置时检查白名单
4. 白名单应用收到修改后的 Location 对象，包含虚假坐标

## 关键依赖

- **EzXHelper** (`com.github.kyuubiran:EzXHelper`): Xposed 辅助库，简化 Hook 操作
- **Xposed API** (`de.robv.android.xposed:api`): Xposed 框架核心
- **HiddenApiBypass** (`org.lsposed.hiddenapibypass`): 访问 Android 隐藏 API
- **Moshi** (`com.squareup.moshi`): JSON 序列化，用于配置文件
- **OneAdapter** (`com.github.idanatz:OneAdapter`): RecyclerView 适配器库
- **SmartRefreshLayout** (`io.github.scwang90`): 下拉刷新组件

## CI/CD

项目使用 GitHub Actions 进行持续集成，配置文件位于 `.github/workflows/build.yaml`。

触发条件：
- 推送到 main/master 分支
- Pull Request 到 main/master 分支
- 手动触发 (workflow_dispatch)

构建产物会作为 Artifact 上传，可在 Actions 页面下载。

## 测试

此模块需要已安装 LSPosed/EdXposed 框架的已 root 设备。无法在标准 Android 模拟器中测试。
