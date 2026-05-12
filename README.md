<div align="center">
  <img src="assets/promo.png" width="800" alt="Shiroha Quiz">
</div>

# Shiroha Quiz

Shiroha Quiz 是一个轻量、开源、可扩展的通用刷题项目，主要面向**自导入题库、练习、考试、错题复习和多端使用**场景。

项目想解决的问题很简单：

> 手里有 Word、PDF、TXT、JSON，或者题目和答案分开的文件，但真正想刷题前，还要先花大量时间整理格式。Shiroha Quiz 希望把这些题库整理成可以直接练习、考试和复盘的本地题库。

当前项目包含三条线：

- **Web 版**：主要功能完整，是题库导入、解析策略、题库管理和刷题体验的主要迭代入口。
- **Android WebView 壳版**：把 Web 资源打包进 Android App，离线加载本地页面，适合手机刷题使用。
- **Android 原生 Compose 版**：使用 Kotlin + Jetpack Compose + Material3 开发，正在逐步完善原生 App 体验。

---

## 当前能力

### 刷题与考试

- 练习模式：上一题 / 下一题、提交答案、查看解析、答题状态记录。
- 考试模式：题量设置、计时、自动交卷、得分统计、明细报告。
- 错题本：自动记录错题，可用于后续复习。
- 刷题记录：保存练习和考试记录，方便回顾。
- 多题型支持：单选题、多选题、判断题、填空题、简答题。

### 题库导入

- 普通题库导入：支持粘贴文本或上传文件。
- 多格式支持：`txt`、`json`、`docx`、文字层 `pdf`。
- 双文件导入：支持“题目文件 + 答案文件”分离导入。
- 识别预览：导入前查看题干、选项、答案、题型和异常提示。
- 手动修正：识别结果可在确认前检查和调整。
- 解析策略评分：多种解析方式自动比较，优先采用更可信的结果。
- 备份恢复：支持全部数据备份、批量题库 JSON 导出与恢复。

### 多端支持

- Web 端完整功能。
- Android WebView 壳版可离线加载本地 Web 资源。
- Android 原生 Compose 版持续开发中。
- 默认内置 C1 科目一题库，方便首次体验和测试。

---

## Android 双入口架构

Android 工程通过 `productFlavors` 同时维护 WebView 壳版和原生 Compose 版。两个入口独立包名、独立版本号、独立构建。

| Flavor | 包名 | 入口 | 技术路线 | 源码版本 |
|---|---|---|---|---|
| `web` | `com.yiqiu.shirohaquiz` | `WebShellActivity` | WebView 加载本地 Web 资源 | `0.4.3-alpha` |
| `native` | `com.reqir.shirohaquiz` | `MainActivity` | Kotlin + Jetpack Compose + Material3 | `0.2.5` |

> GitHub Releases 中的最新发布版本可能略低于源码中的开发版本，以 release 页面为准下载，以 `build.gradle.kts` 为准查看当前源码版本。

主要目录：

```text
apps/android/
├── app/src/main/assets/web/        # Android WebView 壳内置 Web 资源
├── app/src/main/java/              # WebView 壳入口、启动层、通用 Android 入口
└── app/src/native/java/            # 原生 Compose 版本代码
```

---

## 原生 Compose 当前状态

原生 Android 版正在开发中，目标是逐步形成独立于 WebView 的原生刷题体验。

当前原生版已经包含多个页面与模块，核心方向包括：

- `MainActivity`：原生 Compose 入口。
- `ShirohaAppShell`：原生页面外壳、底部导航和页面切换。
- `QuizRepository`：题库、练习、考试、错题、记录和持久化状态管理。
- `importer/`：题库解析、双文件合并、答案区识别、策略评分和结果校验。
- `ui/`：Compose 页面、组件、主题和设计令牌。

当前源码中原生页面包括但不限于：

| 页面文件 | 说明 |
|---|---|
| `HomeScreen.kt` | 首页、题库概览、快速入口 |
| `ImportScreen.kt` | 文件选择、文本解析、双文件导入、识别预览 |
| `PracticeScreen.kt` | 练习题卡、选项交互、答案提交、解析展示 |
| `ExamScreen.kt` | 考试设置、计时、交卷、得分统计 |
| `BankListScreen.kt` | 题库列表与题库管理 |
| `BankDetailScreen.kt` | 题库详情与题目查看 |
| `BankReviewScreen.kt` | 题库复查 / 题目检查相关页面 |
| `WrongBookScreen.kt` | 错题本相关页面 |
| `RecordsScreen.kt` | 练习 / 考试记录页面 |
| `MeScreen.kt` | 设置、关于、数据管理相关入口 |
| `AboutScreen.kt` | 版本信息与关于页面 |
| `RecordDetailScreen.kt` | 单轮练习/考试记录逐题复盘 |
| `StandardImportFormatScreen.kt` | 标准导入格式说明与 AI 清洗指引 |

> 原生 Compose 版仍在快速开发中，README 只描述当前主线结构；具体完成度以源码和实际构建结果为准。

---

## 使用说明

### Web 端快速上手

1. 打开 `apps/web/index.html`，或访问在线版。
2. 进入 **导入题库**，粘贴文本或上传文件。
3. 系统自动识别题型、选项、答案和解析。
4. 在识别预览中确认题目无误。
5. 进入 **刷题练习** 或 **考试模式** 开始使用。
6. 答错的题会进入 **错题本**。
7. 定期在 **设置/导出** 中导出备份。

### 数据备份建议

Shiroha Quiz 的题库和记录主要保存在本地浏览器或 App WebView 的本地存储中。

建议：

- 重要题库导入后，及时导出全部数据备份。
- 换设备、清理缓存、卸载 App 前，先导出备份 JSON。
- 从 Shiroha Quiz 导出的备份 JSON，应在 **设置/导出 → 导入配置 / 备份 JSON** 中导入。
- 备份 JSON、批量题库 JSON 不建议放进普通题库导入区解析。

---

## 导入格式与策略

支持：

```text
txt
json
docx
文字层 pdf
题目文件 + 答案文件双文件导入
```

系统会尝试识别：

```text
题号
题干
选项
答案
解析
题型
分区 / 分卷
```

导入后会进入识别结果预览。建议在确认导入前检查题型、答案、选项和异常提示。

详细说明：

- [题库导入策略与使用指南](docs/Shiroha%20Quiz%20题库导入策略与使用指南.md)
- [题目导入解析方法说明](docs/Shiroha%20Quiz%20题目导入解析方法说明.md)
- 标准题库格式示例：[Markdown](docs/标准题库格式示例.md) / [Word](docs/标准题库格式示例.docx) / [PDF](docs/标准题库格式示例.pdf)

如果原题库格式非常混乱，且题目没有保密需求，可以先使用 LLM 智能体进行数据清洗，例如豆包、深度求索、通义千问等。清洗目标不是改题，而是统一题号、选项、答案和解析格式。

---

## 仓库结构

```text
shiroha-quiz/
├── .github/                         # Issue 模板与 GitHub Actions
├── apps/
│   ├── web/                         # Web 版
│   │   ├── index.html
│   │   ├── app.js
│   │   ├── styles.css
│   │   ├── question-bank.js
│   │   ├── media/                   # Web 插画素材
│   │   ├── data/                    # 内置题库
│   │   └── libs/                    # PDF.js 等本地库
│   └── android/                     # Android 工程
│       └── app/
│           ├── src/main/assets/web/ # Android WebView 壳内置 Web 资源
│           ├── src/main/java/       # WebView 壳入口与 Android 原生入口
│           └── src/native/java/     # 原生 Compose 代码
├── docs/
│   ├── web/                         # Web 端相关文档
│   ├── native/                      # 原生 Android 相关文档
│   └── universal/                   # 通用架构建议
├── assets/                          # 宣传图与素材源文件
├── packages/                        # 规划中的共享模块
├── CHANGELOG.md
├── CONTRIBUTING.md
├── LICENSE
└── README.md
```

---

## 本地运行

### Web 端

`apps/web/` 是纯静态页面，无需构建。

```bash
# 方式一：直接打开
apps/web/index.html

# 方式二：本地静态服务
npx serve apps/web
```

在线版：

```text
https://reiqr.github.io/shiroha-quiz
```

### Android 端

进入 Android 工程目录：

```bash
cd apps/android
```

构建 WebView 壳版本：

```bash
./gradlew assembleWebRelease
```

构建原生 Compose 版本：

```bash
./gradlew assembleNativeRelease
```

Windows PowerShell 可使用：

```powershell
.\gradlew.bat assembleWebRelease
.\gradlew.bat assembleNativeRelease
```

构建输出通常位于：

```text
apps/android/app/build/outputs/
```

---

## 下载与使用

下载入口：

- [GitHub Releases](https://github.com/reiqr/shiroha-quiz/releases)
- [在线体验](https://reiqr.github.io/shiroha-quiz)

当前 GitHub Releases 最新发布版本为 `v0.4.3-alpha`；源码中的 Android flavor 版本可能已经进入下一轮开发版本。

每次发布通常包含：

- Android APK
- Web 离线包
- 相关说明文档

> 当前仍为 alpha 测试阶段，不建议用于正式考试、生产培训或高风险场景。

---

## 开发计划

近期重点：

- 稳定 Android 原生 Compose 版核心流程。
- 继续完善题库导入、双文件导入和识别预览。
- 优化移动端启动、导航切换和大题库操作流畅度。
- 完善错题本、刷题记录、备份恢复等核心功能。
- 增加核心流程测试，尤其是导入、考试、错题和备份恢复。
- 后续逐步沉淀共享解析层和通用数据结构。

---

## 参与贡献

欢迎通过 Issue 提交：

- Bug 反馈
- 题库格式兼容问题
- 导入失败样例
- UI / 交互优化建议
- Android 适配问题
- 文档补充建议

详见：

- [CONTRIBUTING.md](./CONTRIBUTING.md)
- [CHANGELOG.md](./CHANGELOG.md)

---

## 许可证

本项目采用 `GPL-3.0` 开源。
