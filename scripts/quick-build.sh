#!/bin/bash

# Quick FRP Kernels Build Script
# 快速构建脚本，用于本地开发

set -e

FRP_VERSION=${1:-"0.65.0"}
echo "🚀 Quick building FRP kernels v$FRP_VERSION"

# 创建jniLibs目录结构
mkdir -p app/src/main/jniLibs/{arm64-v8a,armeabi-v7a,x86_64}

# 下载并处理每个架构
download_and_process() {
    local abi=$1
    local target_abi=$2
    local url="https://github.com/fatedier/frp/releases/download/v${FRP_VERSION}/frp_${FRP_VERSION}_${abi}.tar.gz"
    
    echo "📦 Processing $abi -> $target_abi"
    
    # 下载
    wget -q "$url" -O "temp-${abi}.tar.gz"
    
    # 解压
    tar -xzf "temp-${abi}.tar.gz"
    
    # 复制文件
    local frp_dir="frp_${FRP_VERSION}_${abi}"
    if [ -d "$frp_dir" ]; then
        cp "$frp_dir/frpc" "app/src/main/jniLibs/${target_abi}/libfrpc.so"
        cp "$frp_dir/frps" "app/src/main/jniLibs/${target_abi}/libfrps.so"
        chmod +x "app/src/main/jniLibs/${target_abi}/"*.so
        echo "✅ Copied files for $target_abi"
        
        # 清理
        rm -rf "$frp_dir"
        rm "temp-${abi}.tar.gz"
    fi
}

# 处理各架构
download_and_process "linux_arm64" "arm64-v8a"
download_and_process "linux_arm" "armeabi-v7a"  
download_and_process "linux_amd64" "x86_64"

echo "🎉 Quick build completed!"
echo "📁 Files created in app/src/main/jniLibs/"
ls -la app/src/main/jniLibs/*/
