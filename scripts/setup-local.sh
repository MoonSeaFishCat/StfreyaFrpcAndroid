#!/bin/bash

# 本地开发环境设置脚本

echo "🚀 设置本地开发环境..."

# 创建docs目录
mkdir -p docs

# 检查keystore.properties是否存在
if [ ! -f "keystore.properties" ]; then
    echo "📝 创建keystore.properties文件..."
    cp keystore.example.properties keystore.properties
    echo "✅ keystore.properties已创建"
else
    echo "✅ keystore.properties已存在"
fi

# 检查keystore.jks是否存在
if [ ! -f "keystore.jks" ]; then
    echo "🔑 创建调试keystore..."
    keytool -genkey -v -keystore keystore.jks -alias stferyaFrpcandroid -keyalg RSA -keysize 2048 -validity 10000 -storepass 1145141919810 -keypass 1145141919810 -dname "CN=StfreyaFrpc,O=Stfreya,C=US"
    echo "✅ 调试keystore已创建"
else
    echo "✅ keystore.jks已存在"
fi

# 给脚本添加执行权限
echo "🔧 设置脚本权限..."
chmod +x scripts/*.sh
chmod +x gradlew

# 检查FRP内核是否存在
echo "🔍 检查FRP内核..."
if [ ! -f "app/src/main/jniLibs/arm64-v8a/libfrpc.so" ]; then
    echo "📦 FRP内核不存在，开始构建..."
    ./scripts/build-frp-kernels.sh
else
    echo "✅ FRP内核已存在"
fi

echo "🎉 本地开发环境设置完成！"
echo ""
echo "📋 下一步："
echo "1. 运行 './gradlew assembleDebug' 构建调试APK"
echo "2. 运行 './gradlew assembleRelease' 构建发布APK"
echo "3. 查看 docs/KEYSTORE_SETUP.md 了解keystore配置"
