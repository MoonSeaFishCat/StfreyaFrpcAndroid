# StfreyaFrpc

一个现代化的 Android FRP 客户端应用，采用 Material 3 设计语言和 Jetpack Compose 构建。

## 特性

### 🎨 现代 UI 设计
- **Material 3 设计语言** - 遵循最新的 Google 设计规范
- **Jetpack Compose** - 现代化的声明式 UI 框架
- **深色/浅色主题** - 自动适配系统主题
- **响应式布局** - 适配各种屏幕尺寸

### ⚡ 核心功能
- **FRPC 支持** - 运行 FRP 客户端配置
- **FRPS 支持** - 运行 FRP 服务端配置
- **多配置管理** - 支持多个配置文件的创建、编辑和管理
- **开机自启动** - 支持选择性开机自启动
- **后台服务** - 可靠的后台执行，确保配置持续运行
- **实时日志** - 查看 FRP 运行日志和状态

### 🔧 技术特性
- **原生 FRP 内核** - 直接使用官方 FRP 二进制文件
- **多架构支持** - 支持 ARM64、ARMv7、x86_64
- **现代架构** - MVVM 模式，使用 StateFlow 进行状态管理
- **类型安全** - 100% Kotlin 代码
- **性能优化** - 高效的内存和 CPU 使用
- **智能缓存** - 配置文件和状态的高效缓存机制
- **网络监控** - 实时网络状态检测和连接类型识别
- **配置验证** - 自动验证 FRP 配置文件的正确性
- **错误处理** - 完善的错误处理和用户反馈系统
- **性能监控** - 内置性能监控和内存泄漏检测

## 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **设计系统**: Material 3
- **架构模式**: MVVM
- **状态管理**: StateFlow
- **最低 SDK**: 23 (Android 6.0)
- **目标 SDK**: 35 (Android 15)

## 项目结构

```
app/src/main/java/com/stfreya/frpc/
├── MainActivity.kt              # 主界面
├── ConfigActivity.kt            # 配置编辑界面
├── AboutActivity.kt             # 关于页面
├── ShellService.kt              # 后台服务
├── ShellThread.kt               # 进程管理
├── FrpConfig.kt                 # 配置数据模型
├── FrpType.kt                   # FRP 类型枚举
├── AutoStartBroadReceiver.kt    # 开机自启动广播接收器
├── IntentExtraKey.kt            # Intent 键常量
├── PreferencesKey.kt            # 偏好设置键常量
├── ShellServiceAction.kt        # 服务操作常量
└── ui/theme/                    # UI 主题
    ├── Color.kt                 # 颜色定义
    ├── Theme.kt                 # 主题配置
    └── Type.kt                  # 字体配置

app/src/test/java/com/stfreya/frpc/
└── ExampleUnitTest.kt           # 单元测试

app/src/androidTest/java/com/stfreya/frpc/
└── ExampleInstrumentedTest.kt   # 集成测试
```

## 构建说明

### 环境要求
- Android Studio Hedgehog 或更高版本
- JDK 17 或更高版本
- Android SDK 35

### 构建步骤
1. 克隆项目
2. 在 Android Studio 中打开项目
3. 同步 Gradle 文件
4. 构建并运行

### 签名配置
项目支持两种签名方式：

#### 方式一：本地签名
1. 在项目根目录创建 `keystore.properties` 文件
2. 参考 `keystore.example.properties` 填写签名信息

#### 方式二：GitHub Actions 签名
1. 将签名文件转换为 base64
2. 在 GitHub Secrets 中配置签名信息

## 使用说明

### 创建配置
1. 点击主界面的 "+" 按钮
2. 选择 FRPC 或 FRPS 类型
3. 编辑配置文件内容
4. 保存配置

### 运行配置
1. 在主界面找到要运行的配置
2. 打开配置右侧的开关
3. 配置将在后台运行

### 查看日志
1. 点击主界面右上角的日志图标
2. 查看实时运行日志
3. 支持复制和清除日志

## 许可证

本项目采用 Apache License 2.0 许可证。

FRP 项目同样采用 Apache License 2.0 许可证。

## 致谢

- [FRP 项目](https://github.com/fatedier/frp) - 提供核心功能
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - UI 框架
- [Material 3](https://m3.material.io/) - 设计系统

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

- 网站: https://stfreya.com
- GitHub: https://github.com/stfreya/StfreyaFrpc

---

**StfreyaFrpc** - 让 FRP 在 Android 上更简单、更美观、更强大！