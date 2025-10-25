# Keystore 设置说明

## 📋 概述

本项目使用Android签名配置来构建APK。为了成功构建项目，需要正确配置keystore文件。

## 🔧 本地开发设置

### 1. 使用调试keystore（推荐用于开发）

项目已经包含了调试keystore配置，可以直接使用：

```bash
# 创建调试keystore
keytool -genkey -v -keystore keystore.jks -alias stferyaFrpcandroid -keyalg RSA -keysize 2048 -validity 10000 -storepass 1145141919810 -keypass 1145141919810 -dname "CN=StfreyaFrpc,O=Stfreya,C=US"

# 确保keystore.properties文件存在
cp keystore.example.properties keystore.properties
```

### 2. 使用自定义keystore（用于发布）

如果需要使用自定义keystore进行发布：

1. **创建keystore文件**：
```bash
keytool -genkey -v -keystore your-release-key.jks -keyalg RSA -keysize 2048 -validity 10000
```

2. **更新keystore.properties**：
```properties
keyAlias=stferyaFrpcandroid
keyPassword=1145141919810
storeFile=keystore.jks
storePassword=1145141919810
```

3. **添加到.gitignore**：
确保keystore文件不会被提交到版本控制：
```gitignore
*.jks
keystore.properties
```

## 🚀 CI/CD 设置

### GitHub Actions

项目的工作流会自动处理keystore设置：

1. **自动构建**：使用调试keystore
2. **发布构建**：可以通过GitHub Secrets配置生产keystore

#### 配置生产keystore（可选）

1. 将keystore文件转换为Base64：
```bash
base64 -i your-release-key.jks | pbcopy
```

2. 在GitHub仓库中添加Secret：
   - `KEYSTORE_BASE64`: keystore文件的Base64编码
   - `KEY_ALIAS`: keystore别名
   - `KEY_PASSWORD`: keystore密码
   - `STORE_PASSWORD`: store密码

## 📁 文件说明

- `keystore.properties`: 本地keystore配置（不提交到版本控制）
- `keystore.example.properties`: keystore配置示例
- `keystore.jks`: 调试keystore文件（不提交到版本控制）

## ⚠️ 安全注意事项

1. **永远不要提交keystore文件到版本控制**
2. **使用强密码保护keystore**
3. **定期备份keystore文件**
4. **在生产环境中使用不同的keystore**

## 🔍 故障排除

### 常见问题

1. **keystore.properties not found**
   - 确保文件存在于项目根目录
   - 检查文件内容格式是否正确

2. **keystore file not found**
   - 确保keystore文件路径正确
   - 检查文件名是否匹配

3. **密码错误**
   - 验证keystore.properties中的密码
   - 确保密码与keystore文件匹配

### 验证设置

```bash
# 验证keystore文件
keytool -list -v -keystore keystore.jks

# 验证配置
cat keystore.properties
```

## 📚 相关链接

- [Android应用签名](https://developer.android.com/studio/publish/app-signing)
- [Gradle签名配置](https://developer.android.com/studio/build/building-cmdline#sign_apk)
- [GitHub Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
