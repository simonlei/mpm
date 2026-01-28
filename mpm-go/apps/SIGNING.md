# Android应用签名配置指南

## 📋 概述

本文档说明如何为MPM Android应用配置自动签名，以便构建可发布的Release APK。

## 🔐 步骤1：生成签名密钥

在项目根目录执行以下命令生成签名密钥：

```bash
cd D:/work/mpm/mpm-go/apps
keytool -genkey -v -keystore mpm-release.keystore -alias mpm -keyalg RSA -keysize 2048 -validity 10000
```

### 需要输入的信息：

1. **密钥库口令（Keystore Password）**：输入一个强密码，**请务必记住**
2. **确认密钥库口令**：再次输入相同的密码
3. **姓名（CN）**：可以填写你的名字或公司名
4. **组织单位（OU）**：可选，可以直接回车跳过
5. **组织（O）**：可选，可以直接回车跳过
6. **城市（L）**：可选，可以直接回车跳过
7. **省份（ST）**：可选，可以直接回车跳过
8. **国家代码（C）**：如 CN，可选
9. **密钥口令（Key Password）**：可以直接回车使用与密钥库相同的密码

### 示例输出：

```
正在生成 2,048 位RSA密钥对和自签名证书 (SHA256withRSA) (有效期为 10,000 天)
        对于: CN=Your Name, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=CN
[正在存储mpm-release.keystore]
```

执行完成后，会在当前目录生成 `mpm-release.keystore` 文件。

## 📝 步骤2：配置签名密码

### 方式1：使用gradle.properties（推荐）

1. 复制示例配置文件：
   ```bash
   cp gradle.properties.example gradle.properties
   ```

2. 编辑 `gradle.properties` 文件，填写你的密码：
   ```properties
   KEYSTORE_PASSWORD=你的密钥库密码
   KEY_PASSWORD=你的密钥密码
   ```

3. 保存文件（此文件已在.gitignore中，不会被提交到Git）

### 方式2：使用环境变量

在系统环境变量中设置：

**Windows PowerShell：**
```powershell
$env:KEYSTORE_PASSWORD="你的密钥库密码"
$env:KEY_PASSWORD="你的密钥密码"
```

**Windows CMD：**
```cmd
set KEYSTORE_PASSWORD=你的密钥库密码
set KEY_PASSWORD=你的密钥密码
```

**Linux/Mac：**
```bash
export KEYSTORE_PASSWORD="你的密钥库密码"
export KEY_PASSWORD="你的密钥密码"
```

## 🏗️ 步骤3：构建签名的Release APK

配置完成后，执行以下命令构建签名的APK：

```bash
./gradlew assembleRelease
```

构建成功后，签名的APK位于：
```
app/build/outputs/apk/release/app-release.apk
```

## ✅ 验证签名

使用以下命令验证APK签名：

```bash
# 使用apksigner验证（推荐）
apksigner verify --verbose app/build/outputs/apk/release/app-release.apk

# 或使用jarsigner验证
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

## 📦 文件说明

- `mpm-release.keystore` - 签名密钥文件（**请妥善保管，不要泄露**）
- `gradle.properties` - 签名密码配置（已在.gitignore中）
- `gradle.properties.example` - 配置示例文件（可以提交到Git）
- `app/build.gradle.kts` - 包含签名配置的构建脚本

## 🔒 安全注意事项

1. **密钥文件安全**：
   - `mpm-release.keystore` 已在 `.gitignore` 中，不会被提交到Git
   - 请将密钥文件备份到安全的地方
   - 如果密钥丢失，将无法更新已发布的应用

2. **密码安全**：
   - `gradle.properties` 已在 `.gitignore` 中，不会被提交到Git
   - 不要在代码中硬编码密码
   - 不要将密码分享给他人
   - 使用强密码（至少8位，包含大小写字母、数字、特殊字符）

3. **CI/CD环境**：
   - 在CI/CD环境中使用环境变量配置密码
   - 不要在CI/CD配置文件中明文存储密码
   - 使用CI/CD平台的密钥管理功能（如GitHub Secrets）

## 🚀 发布流程

1. **构建签名APK**：
   ```bash
   ./gradlew assembleRelease
   ```

2. **验证签名**：
   ```bash
   apksigner verify app/build/outputs/apk/release/app-release.apk
   ```

3. **测试APK**：
   - 在真实设备上安装测试
   - 验证所有功能正常工作

4. **发布**：
   - 上传到应用商店（Google Play、华为应用市场等）
   - 或通过内部渠道分发

## 🛠️ 故障排除

### 问题1：找不到keystore文件

**错误信息**：
```
Keystore file 'D:\work\mpm\mpm-go\apps\mpm-release.keystore' not found
```

**解决方案**：
- 确认已执行步骤1生成keystore文件
- 检查文件路径是否正确
- 确认文件名为 `mpm-release.keystore`

### 问题2：密码错误

**错误信息**：
```
Keystore was tampered with, or password was incorrect
```

**解决方案**：
- 检查 `gradle.properties` 中的密码是否正确
- 确认密码与生成keystore时设置的密码一致
- 注意密码区分大小写

### 问题3：环境变量未生效

**解决方案**：
- 确认环境变量已正确设置
- 重启终端或IDE
- 优先使用 `gradle.properties` 方式配置

## 📚 相关资源

- [Android官方文档 - 为应用签名](https://developer.android.com/studio/publish/app-signing)
- [Gradle官方文档 - 签名配置](https://developer.android.com/studio/build/gradle-tips#sign-your-app)
- [keytool命令参考](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html)

## 📞 支持

如有问题，请联系开发团队或查阅项目文档。

---

*最后更新时间: 2026-01-28*