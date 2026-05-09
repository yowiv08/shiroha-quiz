# Shiroha Quiz

Shiroha Quiz 是一个轻量、开源、可扩展的通用刷题项目，面向多题库、多题型和多端使用场景。

它当前的目标不是只做一个网页工具，而是逐步发展成一套完整的刷题系统：

- Web 端：用于题库导入、题库管理、格式验证和能力迭代
- Android 端：用于原生、高质感、重体验的刷题与考试
- 共享层：后续沉淀题库解析、刷题核心逻辑和通用数据结构

## 项目定位

Shiroha Quiz 希望提供一个稳定、通用、可持续扩展的刷题环境，支持：

- 多题库管理
- 题库导入
- 题型识别
- 刷题练习
- 错题复习
- 多端扩展

当前项目仍处于早期测试与快速迭代阶段。

## 当前能力

**刷题与考试**
- 完整练习模式：上一题/下一题、答题卡跳转、练习结束统计
- 考试模式：限时、自动交卷、题型分值、及格线判定
- 错题本：自动记录错题、掌握状态标记、错题重练
- 刷题记录：练习/考试明细、作答详情展开

**题库导入**
- 智能识别：自动检测题型、选项、答案、解析
- 多格式支持：txt、json、docx、文字层 pdf
- 双文件导入：题目文件 + 答案文件分离导入
- 识别预览：异常题标记、待确认列表、手动编辑

**多端支持**
- Web 端完整功能
- Android 端通过 WebView 壳运行（过渡方案，无需联网）
- 批量导出与备份 JSON 导入/恢复
- 采用 monorepo 结构

## 当前状态

已经完成：

- [x] 初始 monorepo 仓库结构整理
- [x] Web 工程收口到 `apps/web`，Android 工程收口到 `apps/android`
- [x] 中文产品、UI、线框、组件、执行计划文档
- [x] WebView 壳方案落地，加载本地 Web 资源
- [x] 完整练习模式（答题卡、切题、统计）
- [x] 考试模式（限时、自动交卷、及格线）
- [x] 错题本（自动记录、掌握标记、错题重练）
- [x] 刷题记录（练习/考试明细、作答详情）
- [x] PDF 文本提取（PDF.js + 轻量回退）
- [x] docx 文本提取
- [x] 双文件导入、答案解析合并
- [x] 看板娘插画素材（9 种状态场景）
- [x] 批量导出与备份 JSON 恢复

尚未完成：

- [ ] csv、doc 格式导入
- [ ] 原生 Android Compose 页面迁移
- [ ] 数据统计与学习进度面板
- [ ] 稳定版发布

## 支持的导入格式

已支持：

- `txt` — 纯文本题库
- `json` — Shiroha Quiz 备份 JSON
- `docx` — Word 文档
- 文字层 `pdf` — PDF.js 提取 + 轻量回退
- 双文件导入 — 题目文件 + 答案文件分离

计划支持：

- `csv`
- `doc`

当前阶段暂不做 OCR PDF。

## 默认示例题库

当前默认示例题库为 `C1` 驾考题库，用于：

- 导入逻辑验证
- 默认演示
- 功能测试

相关说明见：

- [docs/web/C1题库来源与说明.md](./docs/web/C1题库来源与说明.md)

## 仓库结构

```text
shiroha-quiz/
├─ apps/
│  ├─ web/        # 通用 Web 参考实现
│  └─ android/    # Android 原生工程
├─ packages/
│  ├─ core/       # 规划中的刷题核心逻辑
│  ├─ parser/     # 规划中的题库解析逻辑
│  ├─ types/      # 规划中的通用数据结构
│  ├─ shared/     # 规划中的通用工具
│  └─ ui/         # 规划中的通用 UI 规范
├─ docs/          # 产品、设计、架构、执行计划文档
├─ assets/        # 图标、截图、设计素材
├─ package.json
├─ pnpm-workspace.yaml
├─ CHANGELOG.md
├─ CONTRIBUTING.md
├─ LICENSE
└─ README.md
```

## Web 端运行方式

`apps/web/` 为纯静态页面，无需安装依赖或构建：

- 直接用浏览器打开 `apps/web/index.html`
- 或使用任意静态文件服务（如 `npx serve apps/web`）

## Web 端说明

`apps/web` 当前是通用网页版参考实现，主要承担：

- 题库导入能力验证
- 数据结构参考
- 题型识别规则验证
- 同时作为 Android WebView 壳的内容来源

## Android 端状态

`apps/android` 当前采用 WebView 壳方案作为过渡：

- 启动入口为 `WebShellActivity`，直接加载内置的 Web 端页面
- Web 资源（HTML / JS / CSS）存放在 `assets/web/` 目录下
- 无需联网，本地即可运行

这是过渡方案，后续会逐步替换为原生页面：

- 优势：Web 端和 Android 端共用同一套核心代码，迭代快
- 后续：Web 功能稳定后，逐步用 Compose 原生页面替换关键流程

## 开发路线

v25-v27（刷题体验、错题本、移动端优化）已完成。下一步：

1. **v28** — 抽离 parser、schema、核心数据结构，为原生 Android 迁移做准备
2. **v29** — 数据统计与学习进度面板
3. **v30** — 稳定版前整理：文档、示例题库、回归测试
4. 后续 — 逐步用 Compose 原生页面替换 WebView 关键流程

## 下载与使用

最新版本：**v0.3.0-alpha**（2026-05-09）

GitHub Releases 页面：

- [https://github.com/reiqr/shiroha-quiz/releases](https://github.com/reiqr/shiroha-quiz/releases)

每次发布包含：

- Android APK（调试安装包）
- Web 端离线包（`shiroha-quiz-vX.X.X-alpha.zip`，可直接在浏览器运行）
- GitHub 自动生成的 Source code (zip / tar.gz)

> 当前为 alpha 测试版本，不建议用于正式考试场景。

## 参与贡献

欢迎通过 Issue 提交：

- Bug 反馈
- 功能建议
- 题库格式兼容问题
- UI / 交互优化建议
- 文档改进建议

详细说明见：

- [CONTRIBUTING.md](./CONTRIBUTING.md)

## 更新日志

- [CHANGELOG.md](./CHANGELOG.md)

## 许可证

本项目采用 `GPL-3.0` 开源。

你可以自由使用、学习、修改和分发本项目代码；如果你分发修改后的版本或基于本项目的衍生作品，需要继续遵守 GPL-3.0 的相关要求。
