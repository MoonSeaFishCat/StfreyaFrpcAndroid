# FRP Kernels Build Scripts

这些脚本用于自动从 [frp 官方 release](https://github.com/fatedier/frp/releases) 下载对应 ABI 的 Linux 版本，重命名为 `libfrpc.so` 和 `libfrps.so`，并放置到正确的 Android 项目目录中。

## 脚本说明

### 1. `build-frp-kernels.sh` - 完整构建脚本
- 支持所有 Android 架构 (arm64-v8a, armeabi-v7a, x86_64)
- 自动验证文件完整性
- 更新 `build.gradle.kts` 中的 FRP 版本
- 创建压缩包
- 详细的日志输出

### 2. `quick-build.sh` - 快速构建脚本
- 简化的构建流程
- 适合本地开发使用
- 最小化的输出

## 使用方法

### 手动构建

```bash
# 使用默认版本 (0.65.0)
./scripts/build-frp-kernels.sh

# 指定版本
./scripts/build-frp-kernels.sh 0.65.0

# 快速构建
./scripts/quick-build.sh 0.65.0
```

### GitHub Actions 自动构建

1. **手动触发**：
   - 进入 GitHub Actions 页面
   - 选择 "Build FRP Kernels" 工作流
   - 点击 "Run workflow"
   - 输入 FRP 版本号

2. **自动触发**：
   - 推送到 `main` 分支
   - 修改脚本文件
   - 每周一自动检查新版本

## 构建流程

1. **下载**：从 GitHub releases 下载对应平台的 frp 压缩包
2. **解压**：解压到临时目录
3. **重命名**：
   - `frpc` → `libfrpc.so`
   - `frps` → `libfrps.so`
4. **放置**：复制到 `app/src/main/jniLibs/{arch}/` 目录
5. **验证**：检查文件大小和类型
6. **更新**：更新 `build.gradle.kts` 中的版本号
7. **打包**：创建压缩包供分发

## 架构映射

| Linux ABI | Android ABI | 说明 |
|-----------|-------------|------|
| `linux_amd64` | `x86_64` | 64位 x86 设备 |
| `linux_arm64` | `arm64-v8a` | 64位 ARM 设备 |
| `linux_arm` | `armeabi-v7a` | 32位 ARM 设备 |

## 输出文件

构建完成后会生成：

```
app/src/main/jniLibs/
├── arm64-v8a/
│   ├── libfrpc.so
│   └── libfrps.so
├── armeabi-v7a/
│   ├── libfrpc.so
│   └── libfrps.so
└── x86_64/
    ├── libfrpc.so
    └── libfrps.so
```

## 注意事项

1. **文件大小**：正常的 `.so` 文件应该大于 1MB
2. **权限**：确保 `.so` 文件有执行权限
3. **版本同步**：构建后会自动更新 `build.gradle.kts` 中的版本号
4. **清理**：脚本会自动清理临时文件

## 故障排除

### 下载失败
- 检查网络连接
- 确认 FRP 版本号正确
- 查看 GitHub releases 页面确认版本存在

### 文件过小
- 检查下载是否完整
- 确认解压过程无错误
- 验证源文件完整性

### 权限问题
- 确保脚本有执行权限：`chmod +x scripts/*.sh`
- 检查目标目录的写入权限

## 自动化

### GitHub Actions 工作流特性

- **多触发方式**：手动、推送、定时
- **版本管理**：支持指定版本或自动检测
- **产物上传**：自动创建 Release 和 Artifacts
- **代码提交**：自动提交更新到仓库
- **缓存优化**：使用 Gradle 缓存加速构建

### 定时检查

工作流配置了每周一自动检查新版本，确保内核保持最新。

## 相关链接

- [frp 官方仓库](https://github.com/fatedier/frp)
- [frp releases](https://github.com/fatedier/frp/releases)
- [Android ABI 文档](https://developer.android.com/ndk/guides/abis)
