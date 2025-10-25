#!/bin/bash

# FRP Kernels Build Script
# 自动从frp官方release下载并构建Android所需的so文件

set -e

# 默认FRP版本
DEFAULT_FRP_VERSION="0.65.0"
FRP_VERSION=${1:-$DEFAULT_FRP_VERSION}

echo "🚀 Building FRP kernels for version: $FRP_VERSION"

# 创建临时目录
TEMP_DIR="temp/frp-build"
mkdir -p "$TEMP_DIR"

# 支持的ABI映射
declare -A ABI_MAP
ABI_MAP["linux_amd64"]="x86_64"
ABI_MAP["linux_arm64"]="arm64-v8a"
ABI_MAP["linux_arm"]="armeabi-v7a"

# 支持的ABI列表
ABIS=("linux_amd64" "linux_arm64" "linux_arm")

# 下载并处理每个ABI
for abi in "${ABIS[@]}"; do
    echo "📦 Processing ABI: $abi"
    
    # 构建下载URL - 确保版本号格式正确
    if [[ "$FRP_VERSION" =~ ^[0-9]+\.[0-9]+$ ]]; then
        # 如果版本号只有主版本.次版本，添加.0补丁版本
        FRP_VERSION="${FRP_VERSION}.0"
    fi
    FRP_URL="https://github.com/fatedier/frp/releases/download/v${FRP_VERSION}/frp_${FRP_VERSION}_${abi}.tar.gz"
    echo "⬇️  Downloading from: $FRP_URL"
    
    # 下载文件
    if ! wget -q "$FRP_URL" -O "$TEMP_DIR/frp-${abi}.tar.gz"; then
        echo "❌ Failed to download $abi"
        continue
    fi
    
    # 解压文件
    if ! tar -xzf "$TEMP_DIR/frp-${abi}.tar.gz" -C "$TEMP_DIR/"; then
        echo "❌ Failed to extract $abi"
        continue
    fi
    
    # 查找解压后的目录
    FRP_DIR=$(find "$TEMP_DIR" -name "frp_${FRP_VERSION}_${abi}" -type d | head -1)
    
    if [ -d "$FRP_DIR" ]; then
        echo "✅ Found FRP directory: $FRP_DIR"
        
        # 获取目标ABI
        TARGET_ABI=${ABI_MAP[$abi]}
        TARGET_DIR="app/src/main/jniLibs/${TARGET_ABI}"
        
        # 创建目标目录
        mkdir -p "$TARGET_DIR"
        echo "📁 Created target directory: $TARGET_DIR"
        
        # 复制并重命名frpc
        if [ -f "$FRP_DIR/frpc" ]; then
            cp "$FRP_DIR/frpc" "$TARGET_DIR/libfrpc.so"
            chmod +x "$TARGET_DIR/libfrpc.so"
            echo "✅ Copied frpc to $TARGET_DIR/libfrpc.so"
        else
            echo "⚠️  Warning: frpc not found in $FRP_DIR"
        fi
        
        # 复制并重命名frps
        if [ -f "$FRP_DIR/frps" ]; then
            cp "$FRP_DIR/frps" "$TARGET_DIR/libfrps.so"
            chmod +x "$TARGET_DIR/libfrps.so"
            echo "✅ Copied frps to $TARGET_DIR/libfrps.so"
        else
            echo "⚠️  Warning: frps not found in $FRP_DIR"
        fi
        
        # 清理当前ABI的临时文件
        rm -rf "$FRP_DIR"
        rm "$TEMP_DIR/frp-${abi}.tar.gz"
    else
        echo "❌ Error: Could not find FRP directory for $abi"
    fi
done

# 验证构建结果
echo "🔍 Verifying built kernels..."
echo "📊 File sizes:"
find app/src/main/jniLibs -name "*.so" -exec ls -lh {} \;

# 检查文件完整性
echo "🔍 Checking file integrity..."
for so_file in $(find app/src/main/jniLibs -name "*.so"); do
    size=$(stat -c%s "$so_file")
    echo "📄 File: $so_file, Size: $size bytes"
    
    if [ $size -lt 1000000 ]; then
        echo "⚠️  Warning: $so_file seems too small ($size bytes)"
    fi
    
    # 检查文件类型
    if file "$so_file" | grep -q "ELF"; then
        echo "✅ $so_file is a valid ELF file"
    else
        echo "❌ $so_file is not a valid ELF file"
    fi
done

# 更新build.gradle.kts中的FRP版本
echo "📝 Updating build.gradle.kts with FRP version: $FRP_VERSION"
if [ -f "app/build.gradle.kts" ]; then
    # 备份原文件
    cp app/build.gradle.kts app/build.gradle.kts.backup
    
    # 更新FRP版本
    sed -i "s/buildConfigField(\"String\", \"FrpVersion\", \".*\")/buildConfigField(\"String\", \"FrpVersion\", \"\\\"$FRP_VERSION\\\"\")/" app/build.gradle.kts
    
    echo "✅ Updated build.gradle.kts"
    echo "📋 New FrpVersion setting:"
    grep "FrpVersion" app/build.gradle.kts
else
    echo "⚠️  Warning: build.gradle.kts not found"
fi

# 创建压缩包
echo "📦 Creating FRP kernels archive..."
cd app/src/main/jniLibs
tar -czf "../../../frp-kernels-${FRP_VERSION}.tar.gz" .
cd ../../../
echo "✅ Created frp-kernels-${FRP_VERSION}.tar.gz"

# 显示压缩包信息
echo "📊 Archive info:"
ls -lh frp-kernels-*.tar.gz

# 清理临时目录
echo "🧹 Cleaning up..."
rm -rf "$TEMP_DIR"

echo "🎉 FRP kernels build completed successfully!"
echo "📦 Archive: frp-kernels-${FRP_VERSION}.tar.gz"
echo "📁 Kernels location: app/src/main/jniLibs/"
