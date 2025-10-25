# FRP Kernels Build Script for Windows
# 自动从frp官方release下载并构建Android所需的so文件

param(
    [string]$FrpVersion = "0.65.0"
)

Write-Host "🚀 Building FRP kernels for version: $FrpVersion" -ForegroundColor Green

# 创建临时目录
$TempDir = "temp\frp-build"
if (Test-Path $TempDir) {
    Remove-Item -Recurse -Force $TempDir
}
New-Item -ItemType Directory -Path $TempDir -Force | Out-Null

# 支持的ABI映射
$AbiMap = @{
    "linux_amd64" = "x86_64"
    "linux_arm64" = "arm64-v8a"
    "linux_arm" = "armeabi-v7a"
}

# 支持的ABI列表
$Abis = @("linux_amd64", "linux_arm64", "linux_arm")

# 下载并处理每个ABI
foreach ($abi in $Abis) {
    Write-Host "📦 Processing ABI: $abi" -ForegroundColor Yellow
    
    # 构建下载URL
    $FrpUrl = "https://github.com/fatedier/frp/releases/download/v$FrpVersion/frp_${FrpVersion}_${abi}.tar.gz"
    Write-Host "⬇️  Downloading from: $FrpUrl" -ForegroundColor Cyan
    
    # 下载文件
    $DownloadPath = "$TempDir\frp-${abi}.tar.gz"
    try {
        Invoke-WebRequest -Uri $FrpUrl -OutFile $DownloadPath -UseBasicParsing
        Write-Host "✅ Downloaded $abi" -ForegroundColor Green
    }
    catch {
        Write-Host "❌ Failed to download $abi`: $($_.Exception.Message)" -ForegroundColor Red
        continue
    }
    
    # 解压文件 (需要7zip或tar命令)
    $ExtractPath = "$TempDir\extract-$abi"
    New-Item -ItemType Directory -Path $ExtractPath -Force | Out-Null
    
    try {
        # 尝试使用tar命令 (Windows 10+)
        tar -xzf $DownloadPath -C $ExtractPath
        Write-Host "✅ Extracted $abi" -ForegroundColor Green
    }
    catch {
        Write-Host "❌ Failed to extract $abi`: $($_.Exception.Message)" -ForegroundColor Red
        continue
    }
    
    # 查找解压后的目录
    $FrpDir = Get-ChildItem -Path $ExtractPath -Directory -Name "frp_${FrpVersion}_${abi}" | Select-Object -First 1
    $FullFrpDir = Join-Path $ExtractPath $FrpDir
    
    if (Test-Path $FullFrpDir) {
        Write-Host "✅ Found FRP directory: $FullFrpDir" -ForegroundColor Green
        
        # 获取目标ABI
        $TargetAbi = $AbiMap[$abi]
        $TargetDir = "app\src\main\jniLibs\$TargetAbi"
        
        # 创建目标目录
        New-Item -ItemType Directory -Path $TargetDir -Force | Out-Null
        Write-Host "📁 Created target directory: $TargetDir" -ForegroundColor Cyan
        
        # 复制并重命名frpc
        $FrpcSource = Join-Path $FullFrpDir "frpc"
        $FrpcTarget = Join-Path $TargetDir "libfrpc.so"
        if (Test-Path $FrpcSource) {
            Copy-Item $FrpcSource $FrpcTarget -Force
            Write-Host "✅ Copied frpc to $FrpcTarget" -ForegroundColor Green
        }
        else {
            Write-Host "⚠️  Warning: frpc not found in $FullFrpDir" -ForegroundColor Yellow
        }
        
        # 复制并重命名frps
        $FrpsSource = Join-Path $FullFrpDir "frps"
        $FrpsTarget = Join-Path $TargetDir "libfrps.so"
        if (Test-Path $FrpsSource) {
            Copy-Item $FrpsSource $FrpsTarget -Force
            Write-Host "✅ Copied frps to $FrpsTarget" -ForegroundColor Green
        }
        else {
            Write-Host "⚠️  Warning: frps not found in $FullFrpDir" -ForegroundColor Yellow
        }
        
        # 清理当前ABI的临时文件
        Remove-Item -Recurse -Force $ExtractPath
        Remove-Item $DownloadPath
    }
    else {
        Write-Host "❌ Error: Could not find FRP directory for $abi" -ForegroundColor Red
    }
}

# 验证构建结果
Write-Host "🔍 Verifying built kernels..." -ForegroundColor Yellow
Write-Host "📊 File sizes:" -ForegroundColor Cyan
Get-ChildItem -Path "app\src\main\jniLibs" -Recurse -Filter "*.so" | ForEach-Object {
    $size = $_.Length
    $sizeKB = [math]::Round($size / 1KB, 2)
    Write-Host "📄 File: $($_.FullName), Size: $sizeKB KB" -ForegroundColor White
    
    if ($size -lt 1MB) {
        Write-Host "⚠️  Warning: $($_.Name) seems too small ($sizeKB KB)" -ForegroundColor Yellow
    }
}

# 更新build.gradle.kts中的FRP版本
Write-Host "📝 Updating build.gradle.kts with FRP version: $FrpVersion" -ForegroundColor Yellow
if (Test-Path "app\build.gradle.kts") {
    # 备份原文件
    Copy-Item "app\build.gradle.kts" "app\build.gradle.kts.backup" -Force
    
    # 读取文件内容
    $Content = Get-Content "app\build.gradle.kts" -Raw
    
    # 替换FRP版本
    $NewContent = $Content -replace 'buildConfigField\("String", "FrpVersion", ".*"\)', "buildConfigField(`"String`", `"FrpVersion`", `"$FrpVersion`")"
    
    # 写回文件
    Set-Content -Path "app\build.gradle.kts" -Value $NewContent -NoNewline
    
    Write-Host "✅ Updated build.gradle.kts" -ForegroundColor Green
    Write-Host "📋 New FrpVersion setting:" -ForegroundColor Cyan
    Select-String -Path "app\build.gradle.kts" -Pattern "FrpVersion"
}
else {
    Write-Host "⚠️  Warning: build.gradle.kts not found" -ForegroundColor Yellow
}

# 创建压缩包
Write-Host "📦 Creating FRP kernels archive..." -ForegroundColor Yellow
$ArchiveName = "frp-kernels-${FrpVersion}.zip"
Compress-Archive -Path "app\src\main\jniLibs\*" -DestinationPath $ArchiveName -Force
Write-Host "✅ Created $ArchiveName" -ForegroundColor Green

# 显示压缩包信息
Write-Host "📊 Archive info:" -ForegroundColor Cyan
Get-ChildItem -Path $ArchiveName | ForEach-Object {
    $size = $_.Length
    $sizeMB = [math]::Round($size / 1MB, 2)
    Write-Host "📦 $($_.Name): $sizeMB MB" -ForegroundColor White
}

# 清理临时目录
Write-Host "🧹 Cleaning up..." -ForegroundColor Yellow
Remove-Item -Recurse -Force $TempDir

Write-Host "🎉 FRP kernels build completed successfully!" -ForegroundColor Green
Write-Host "📦 Archive: $ArchiveName" -ForegroundColor Cyan
Write-Host "📁 Kernels location: app\src\main\jniLibs\" -ForegroundColor Cyan
