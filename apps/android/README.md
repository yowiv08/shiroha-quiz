# Shiroha Quiz — Android 工程

## 双入口架构

通过 `productFlavors` 实现两个独立构建：

| Flavor | 入口 Activity | 技术栈 | 图标 |
|--------|-------------|--------|------|
| `web` | WebShellActivity | WebView 加载本地 HTML/CSS/JS | `app-icon.png` |
| `native` | MainActivity | Kotlin + Jetpack Compose + Material3 | 原生图标 |

## 快速编译

```bash
bash ./gradlew assembleWebDebug      # Web 壳子
bash ./gradlew assembleNativeDebug   # 原生 Compose
```

APK 输出：`app/build/outputs/shiroha-quiz/`

## 原生 Compose 进度

已开发 6 个页面 + 完整导入链：

| 页面 | 功能 |
|------|------|
| HomeScreen | 题库概览、快捷入口 |
| ImportScreen | 文件选择、解析预览 |
| PracticeScreen | 题卡展示、答案提交 |
| ExamScreen | 计时交卷、得分统计 |
| BankDetailScreen | 题库题目列表 |
| MeScreen | 个人设置 |

导入模块：`parser/` (8 个解析器) → `score/` → `validate/` → `state/QuizRepository`

详见 [原生开发进度](../../docs/native/原生开发进度.md)

## 目录说明

- `app/`：Android 应用模块
- `app/src/main/assets/web/`：WebView 加载的 Web 资源
- `app/src/web/res/`：Web flavor 专属图标资源
- `gradle/`：Gradle Wrapper
