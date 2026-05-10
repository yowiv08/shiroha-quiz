![Shiroha Quiz](assets/promo.png)

# Shiroha Quiz

Shiroha Quiz 是一个轻量、开源、可扩展的通用刷题项目，面向多题库、多题型和多端使用场景。

当前定位：

- **Web 端**：题库导入、题库管理、格式验证和能力迭代
- **Android 端**：双入口架构 — Web 壳子（已发布）+ 原生 Compose（开发中）
- **共享层**：后续沉淀题库解析、刷题核心逻辑和通用数据结构

## 当前能力

**刷题与考试**
- 完整练习模式：上一题/下一题、答题卡跳转、练习结束统计
- 考试模式：限时、自动交卷、题型分值、及格线判定、得分报告
- 错题本：自动记录错题、掌握状态标记、错题重练
- 刷题记录：练习/考试明细、作答详情展开

**题库导入**
- 智能识别：自动检测题型、选项、答案、解析
- 多格式支持：txt、json、docx、文字层 pdf
- 双文件导入：题目文件 + 答案文件分离导入
- 识别预览：异常题标记、待确认列表、手动编辑
- 导入策略评分与验证

**多端支持**
- Web 端完整功能
- Android Web 壳子（WebView 加载本地资源，无需联网）
- Android 原生 Compose（6 页面 + 导入链 + 状态管理，独立版本号）
- 批量导出与备份 JSON 导入/恢复

## Android 双入口架构

两个入口通过 `productFlavors` 共存于同一工程，独立构建、独立打包：

| Flavor | 包名 | 入口 Activity | 图标 | 版本 |
|--------|------|-------------|------|------|
| `web` | `com.yiqiu.shirohaquiz` | WebShellActivity (WebView) | `app-icon.png` | `0.3.6-alpha` |
| `native` | `com.reqir.shirohaquiz` | MainActivity (Compose) | 原生图标 | `0.1.0` |

- 两者版本号独立管理（各 flavor 持有自己的 versionCode / versionName）
- Web 资源存放于 `assets/web/`，原生代码位于 `src/main/java/`

## 原生 Compose 进度

原生版已开发 6 个页面 + 完整导入链 + 状态管理：

| 页面 | 功能 |
|------|------|
| HomeScreen | 题库概览、快捷入口 |
| ImportScreen | 文件选择、文本解析、识别预览 |
| PracticeScreen | 题卡展示、选项交互、答案提交 |
| ExamScreen | 计时交卷、得分统计 |
| BankDetailScreen | 题库题目列表 |
| MeScreen | 个人设置 |

导入链路：`parser/` (8 个解析器) → `score/` (策略评分) → `validate/` (验证器) → `state/QuizRepository` (状态管理)

详见 [原生开发进度](docs/native/原生开发进度.md)

## 支持的导入格式

已支持：`txt`、`json`、`docx`、文字层 `pdf`、双文件导入

计划支持：`csv`、`doc`

## 仓库结构

```text
shiroha-quiz/
├── apps/
│   ├── web/              # Web 参考实现（也是 WebView 壳的内容源）
│   └── android/           # Android 工程 (productFlavors: web + native)
├── packages/              # 规划中的共享模块
├── docs/                  # 产品、设计、架构文档
├── assets/                # 设计素材、归档、备选图片
├── releases/              # 历史发布压缩包
├── build-release.ps1      # Web 壳子发布脚本
├── build-release-native.ps1  # 原生版发布脚本
├── source-upload.ps1      # 源码上传助手
└── CHANGELOG.md
```

## Web 端运行方式

`apps/web/` 为纯静态页面，无需构建：
- 浏览器直接打开 `apps/web/index.html`
- 或用 `npx serve apps/web`

## 构建与发布

**快速测试编译（不改版本号）：**
```bash
cd apps/android
bash ./gradlew assembleWebDebug     # Web 壳子
bash ./gradlew assembleNativeDebug  # 原生 Compose
```

**正式发布（更新版本号 + 编译 + 创建 Draft Release）：**
```powershell
.\build-release.ps1          # Web 壳子
.\build-release-native.ps1   # 原生 Compose
```

发布脚本流程：输入版本号 → 更新 build.gradle.kts → 编译 APK → 打包 ZIP → 创建 GitHub Draft Release

APK 输出：`apps/android/app/build/outputs/shiroha-quiz/`

## 下载与使用

最新 Web 版：**v0.3.6-alpha** / 原生版：**v0.1.0**

- [GitHub Releases](https://github.com/reiqr/shiroha-quiz/releases)
- [在线体验](https://reiqr.github.io/shiroha-quiz)

每次发布包含 Android APK 和 Web 离线包。

> 当前为 alpha 测试版本，不建议用于正式考试场景。

## 参与贡献

欢迎通过 Issue 提交 Bug 反馈、功能建议、题库格式兼容问题、UI/交互优化建议。

详见 [CONTRIBUTING.md](./CONTRIBUTING.md)

## 更新日志

[CHANGELOG.md](./CHANGELOG.md)

## 许可证

本项目采用 `GPL-3.0` 开源。
