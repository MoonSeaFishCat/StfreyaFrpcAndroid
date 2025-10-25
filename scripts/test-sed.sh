#!/bin/bash

# 测试sed命令的引号处理
FRP_VERSION="0.61.0"

echo "原始内容:"
echo '        buildConfigField("String", "FrpVersion", "\"0.65.0\"")'

echo ""
echo "测试sed命令:"
echo '        buildConfigField("String", "FrpVersion", "\"0.65.0\"")' | sed "s/buildConfigField(\"String\", \"FrpVersion\", \".*\")/buildConfigField(\"String\", \"FrpVersion\", \"\\\\\"$FRP_VERSION\\\\\"\")/"

echo ""
echo "期望结果:"
echo '        buildConfigField("String", "FrpVersion", "\"0.61.0\"")'
