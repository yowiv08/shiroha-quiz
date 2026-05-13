# core — 刷题核心逻辑

答题判断、计分、状态管理。

## 答题判断

| 题型 | 规则 |
|------|------|
| 单选 | 完全匹配 |
| 多选 | 集合相等，忽略顺序 |
| 判断 | 答案标准化后匹配 |
| 填空 | 文本匹配，支持 `||` 分隔等价答案 |
| 简答 | 不自动判对错 |

## 分值（原生版）

| 题型 | 分值 |
|------|------|
| 单选 | 1 |
| 多选 | 2 |
| 判断 | 1 |
| 填空 | 2 |
| 简答 | 5 |

## 数据持久化

- 原生版：SharedPreferences + JSON
- Web 版：localStorage + JSON

备份 JSON Schema 见 `packages/shared/`。

## 实现位置

- **原生版**：`apps/android/app/src/native/java/.../state/QuizRepository.kt`
- **Web 版**：`apps/web/app.js` 的 `submitAnswer()`、`checkMCAnswer()`
