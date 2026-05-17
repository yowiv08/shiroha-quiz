# parser — 题库解析接口

两端解析器独立实现，输入输出契约一致。

## 支持格式

| 格式 | 说明 |
|------|------|
| 标准文本 | 题号 + 选项 + 答案分行 |
| 紧凑格式 | 选项在一行，自动修复 |
| 分卷分区 | "一、单选题" 等标题继承题型 |
| 双文件 | 题目 + 答案分离导入 |
| 整卷兜底 | 格式混乱时尽力解析 |
| Excel 表格 | xlsx/xls/xlsm，自动提取表列 |
| docx | 提取 word/document.xml，含内嵌图片 |
| JSON | 结构化导入，支持 ZIP 含素材 |

## 解析流程

```
原始文本
  ↓
1. TextImportDecoder      — 格式判断（标准/紧凑/分卷），选策略
  ↓
2. QuestionTextNormalizer — 文本清洗（全角半角、多余空格、特殊字符）
  ↓
3. SectionTitleParser     — 分区识别（"一、单选题" 继承题型）
  ↓
4. QuestionBlockSplitter   — 按题号切块，每块 = 一道题
  ↓
5. StandardQuestionParser  — 逐题解析：题型、题干、选项、答案、解析
  ↓
6. DualFileMerger（可选） — 题目文件 + 答案文件合并，自动匹配题号
  ↓
7. convertEmbeddedDataImages — 题干中 base64 图片提取为本地文件
  ↓
8. ImportStrategyScorer    — 多种策略并行评分，选最优
  ↓
9. ImportValidator         — 校验：题型、选项、答案一致性、重复题号
  ↓
10. ImportResult           — 输出题目列表 + 警告 + 诊断
```

### 1. TextImportDecoder — 格式判断

检测文本特征，推荐解析策略：

| 特征 | 策略 |
|------|------|
| 有 "一、"、"二、" 等卷标题 | `volume` 分卷分区 |
| 选项紧凑在同一行 | `compact` 紧凑格式 |
| 无特殊特征 | `standard` 标准逐行 |

### 2. QuestionTextNormalizer — 文本清洗

- 全角字母数字 → 半角（Ａ → A、０ → 0）
- 全角标点 → 半角（，→ , 、。→ .）
- 中文括号 → 英文括号（（）→ ()）
- 统一换行符 `\r\n` → `\n`
- 移除不可见控制字符
- 数学/化学符号标准化（α、β、H₂O、CO₃²⁻ 等）

### 3. SectionTitleParser — 分区识别

识别行首的分区/分卷标题：

```
一、单选题
二、多选题
三、判断题
第一部分 基础知识
```

区域内所有题目自动继承该分区的默认题型。

### 4. QuestionBlockSplitter — 切块

按题号正则切分文本块：

```
^\s*(\d{1,4})\s*[.、．)）]
```

相邻题号之间为一个题目块。支持题号跨行、无空行分隔等非标准格式。

### 5. StandardQuestionParser — 逐题解析

#### 题型检测（优先级从高到低）

| 检测方式 | 示例 |
|---------|------|
| 题号后的显式标签 | `1.【单选题】...` `2. [多选题]` `3. 判断题` |
| 分区继承 | 标题"一、单选题"下所有题默认单选 |
| 选项数量推断 | 有 2+ 选项 → 单选/多选（依答案数） |
| 题干关键词 | 含"填空/空格/____" → 填空题；含"判断/正确/错误" → 判断题 |

#### 题型判断规则

- **单选题**：有 A-D 选项 + 答案只有 1 个字母
- **多选题**：有 A-D 选项 + 答案有 2+ 个字母，或题号含"多选"
- **判断题**：题号含"判断"/"对错"/"是非"，或选项为"对/错/正确/错误"
- **填空题**：题干含填空特征关键词（填空、填入、空白、空格、横线、括号内、____、（））
- **简答题**：无明显选项结构，非填空特征

#### 填空题检测关键词

```
填空 | 填入 | 补全 | 补充完整 | 空白处 | 空白 | 空格
横线上 | 横线 | 括号内 | 括号里 | ____ | （） | ( )
```

#### 选项解析

```
A. 选项文本
A) 选项文本
A、选项文本
```

支持：
- 多选项同行（`A. xxx B. xxx C. xxx`）
- 选项行首直接跟文本（`Axxx`）
- "选项："/"备选项：" 前缀自动剥离
- 选项文本尾部 `;` `；` 自动清理

#### 答案解析

识别行内结果（单项）：`[答案]`、`(D)`、`【D】`

识别单独答案行：

```
答案：A
正确答案：B
参考答案：ABCD
标准答案：正确
本题答案：D
第1题答案：A/B/C
```

答案分隔符：`/` `|` `、` `,` `;` 空格

答案标签：`答案` `正确答案` `参考答案` `标准答案` `正确选项` `本题答案`

#### 解析识别

```
解析：...
【解析】...
```

解析可以紧跟在答案后（同行回答行格式：`答案：A 解析：...`）。

#### 集中答案区

```
一、单选题
1-5：DABCA
6-10：BCDAA
```

按题号范围匹配，逐个分配答案。

### 6. DualFileMerger — 双文件合并

题目文件和答案文件分别上传时：

1. 答案区格式：`1-10：D A A B C D A B C D`（范围格式）
2. 逐题格式：`1.D 2.A 3.C`（配对格式）
3. 无题号答案：按顺序自动对应

### 7. convertEmbeddedDataImages — 内嵌图片提取

从题干中提取 Markdown 图片语法：

```
![alt](data:image/png;base64,iVBOR...)
```

- Base64 解码 → 写入 assets 目录
- 读取图片宽高注册到 Question.images
- 原占位符替换为 `【alt】`

### 8. ImportStrategyScorer — 策略评分

多条解析策略并行执行，按以下维度评分：

| 维度 | 权重 |
|------|------|
| 识别出的题目数量 | 高 |
| 题型分布合理性 | 中 |
| 答案完整度 | 高 |
| 无警告/异常比例 | 低 |

得分最高的策略自动入选。

### 9. ImportValidator — 校验

| 检查项 | 严重度 |
|--------|--------|
| 单选题只有 1 个答案 | ERROR |
| 多选题答案 ≥ 2 个 | WARNING |
| 选项数 ≥ 2 | ERROR（图片题除外） |
| 答案不匹配选项标签 | ERROR |
| 题号重复（同分区/题型内） | WARNING |
| 题干为空 | ERROR |
| 题型判定模糊 | INFO |

### 10. AnswerSectionParser — 集中答案区解析（双文件时）

在答案文件中提取集中答案区：

```
一、单选题
1－5：DABCA
6－10：BCDAA

二、多选题
1-3：ABD
4-5：BC
```

按题目索引顺序将答案注入已解析的题目。

---

## 文本编辑器

原生端支持：
- 全文查找/替换，支持正则表达式
- "选项：A. xxx B. xxx" 前缀自动剥离

## 图片处理

- 原生端：docx 内嵌图片提取 + JSON 中 base64 图片转为本地文件
- Web 端：ZIP 导入时 assets 图片转为内嵌 data URL
- 图片选项题不触发"缺少选项"警告

## 题号识别

支持 `1.` `1、` `【1】` `1)` `1 题干`，不强制题型前缀。

按分区/题型维度判断重复题号，不同分区内相同题号不视为重复。

## AI 清洗提示词

见 `docs/标准题库格式示例.md` 第十一节。

## 实现位置

### 原生版

```
apps/android/app/src/native/java/com/yiqiu/shirohaquiz/importer/
├── parser/
│   ├── TextImportDecoder.kt         — 格式判断
│   ├── QuestionTextNormalizer.kt     — 文本清洗 + 符号标准化
│   ├── SectionTitleParser.kt        — 分区识别
│   ├── QuestionBlockSplitter.kt     — 切块
│   ├── StandardQuestionParser.kt    — 逐题解析（核心）
│   ├── AnswerParser.kt              — 答案匹配
│   ├── AnswerSectionParser.kt       — 集中答案区
│   ├── DualFileMerger.kt            — 双文件合并
│   ├── FullPaperFallbackStrategy.kt — 整卷兜底
│   ├── CompactQuestionRepair.kt     — 紧凑格式修复
│   ├── QuestionTypeLabelParser.kt   — 题型标签解析
│   ├── ExcelQuestionTableParser.kt  — Excel 表格解析
│   └── QuizImportParser.kt          — 主入口
├── assets/
│   ├── QuestionImageBinder.kt       — 图片绑定
│   └── QuestionImportAssetExtractor.kt — 素材提取
├── model/
│   └── ImportModels.kt              — 数据结构
├── score/
│   └── ImportStrategyScorer.kt      — 策略评分
└── validate/
    └── ImportValidator.kt           — 校验
```

### Web 版

`apps/web/app.js`：
- `parseTextQuestions()` — 主解析入口
- `parseAnswerEntries()` — 答案匹配
- `normalizeQuestion()` — 标准化
- `guessType()` / `hasExplicitBlankPrompt()` — 题型推断
