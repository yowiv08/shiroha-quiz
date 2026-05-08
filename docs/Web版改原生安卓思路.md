# Web版改原生安卓开发建议

## 一、结论

Shiroha Quiz 现在 Web 版中的题目导入逻辑，完全可以用原生安卓语言重写。

推荐路线是：

```text
Kotlin 原生重写解析核心
而不是直接把现有 app.js 逐行翻译
也不建议长期依赖 WebView 调用网页逻辑
```

原因是当前 Web 版导入逻辑已经比较复杂，包含：

```text
标准逐行解析
分卷分区三层解析
紧凑格式解析
答案解析区合并解析
判断题识别
双文件答案匹配
导入异常 / 错误校验
题型自动判断
候选策略评分
```

这些逻辑都能用 Kotlin 实现，但应该先整理成独立的解析模块，而不是和界面代码混在一起。

---

## 二、推荐技术栈

原生安卓建议使用：

```text
Kotlin
Jetpack Compose
Room 数据库
Material 3
DocumentFile / Storage Access Framework
```

其中：

```text
Kotlin：负责核心业务逻辑和导入解析
Jetpack Compose：负责界面
Room：负责题库、题目、做题记录本地存储
Storage Access Framework：负责文件选择、导入 txt / docx / pdf
```

---

## 三、项目结构建议

如果后续 Web 版和安卓 App 同时维护，建议仓库结构设计为：

```text
shiroha-quiz/
├─ web/                 # 当前 Web 版
├─ android/             # 原生安卓 App
├─ parser-spec/         # 题目解析规则和测试样例
├─ docs/                # 项目文档
└─ README.md
```

其中最重要的是：

```text
parser-spec/
```

它应该保存 Web 版和安卓 App 共用的解析规则与测试样例。

建议结构：

```text
parser-spec/
├─ import-rules.md
├─ samples/
│  ├─ standard_choice.txt
│  ├─ judge_trailing_answer.txt
│  ├─ two_option_single.txt
│  ├─ answer_analysis_2014.txt
│  ├─ answer_analysis_2015.txt
│  ├─ dual_file_questions.txt
│  └─ dual_file_answers.txt
└─ expected/
   ├─ standard_choice.expected.json
   ├─ judge_trailing_answer.expected.json
   ├─ answer_analysis_2014.expected.json
   └─ answer_analysis_2015.expected.json
```

这样以后安卓端重写解析器时，可以用同一批样例做对照测试。

---

## 四、三种开发路线对比

## 方案一：Kotlin 原生重写解析核心

这是最推荐的长期方案。

### 优点

```text
性能稳定
体验更原生
方便接入 Room 数据库
方便处理安卓文件导入
方便后续做离线使用
代码可维护性更好
不依赖 WebView
```

### 缺点

```text
前期工作量较大
需要把 JS 解析逻辑整理后重写成 Kotlin
需要重新搭建测试样例
```

### 适用场景

```text
正式开发安卓 App
后续长期维护
希望体验接近真正原生软件
```

---

## 方案二：WebView 包装 Web 版

这是最快的短期方案。

### 优点

```text
开发最快
可以直接复用当前 Web 版
导入逻辑几乎不用重写
适合快速做安卓测试版
```

### 缺点

```text
不是真正原生体验
文件选择、系统存储、数据库交互不够自然
后期功能扩展受 WebView 限制
长期维护可能麻烦
```

### 适用场景

```text
想快速做一个 Android 预览版
先验证手机端使用体验
暂时不追求原生架构
```

---

## 方案三：安卓内调用 JS 解析器

即安卓界面用原生实现，但解析核心仍然调用 JS。

### 优点

```text
Web 和安卓共用一套解析逻辑
短期不用维护两套解析规则
解析效果与 Web 版更容易保持一致
```

### 缺点

```text
调试复杂
和 Kotlin 数据结构交互麻烦
性能和兼容性不如纯 Kotlin
长期架构不够干净
```

### 适用场景

```text
过渡期
想先复用 app.js 的解析函数
后续再慢慢迁移到 Kotlin
```

---

## 五、最终推荐路线

建议分三步走。

### 第一阶段：稳定 Web 版解析逻辑

继续先把 Web 版导入模块做稳定。

重点包括：

```text
标准题库稳定导入
判断题尾部答案识别
两选项单选不误判判断题
答案解析区合并解析
双文件题目和答案匹配
异常 / 错误分级
```

当前 Web 版已经具备这些核心能力，但还需要继续通过真实题库测试完善。

---

### 第二阶段：整理 parser-spec

把当前解析规则从代码中抽离成文档和样例。

至少整理这些规则：

```text
题号识别规则
选项识别规则
答案识别规则
判断题识别规则
填空题 / 简答题识别规则
答案解析区识别规则
双文件答案匹配规则
异常 / 错误分级规则
候选策略评分规则
```

同时整理样例：

```text
标准选择题
两选项单选题
题尾判断题
无选项简答题
有答案解析区的整卷
双文件题库
分卷分区题库
紧凑格式题库
```

---

### 第三阶段：Kotlin 原生重写 parser core

安卓端建议拆成这些核心文件：

```text
QuizImportParser.kt
QuestionParser.kt
AnswerParser.kt
AnswerSectionParser.kt
DualFileMerger.kt
ImportValidator.kt
ImportStrategyScorer.kt
ImportModels.kt
```

职责如下：

```text
QuizImportParser.kt
导入总入口，负责调度各种候选解析策略。

QuestionParser.kt
负责题目区解析，例如标准题、紧凑题、分卷题。

AnswerParser.kt
负责普通答案表解析，例如 1.A / 2.B / 3.C。

AnswerSectionParser.kt
负责答案解析区解析，例如 1.【答案】A【解析】。

DualFileMerger.kt
负责双文件题目和答案合并。

ImportValidator.kt
负责正常 / 异常 / 错误分级。

ImportStrategyScorer.kt
负责候选策略评分和自动推荐。

ImportModels.kt
定义 Question、Option、ImportResult、ImportWarning 等数据结构。
```

---

## 六、Kotlin 数据模型建议

可以设计成：

```kotlin
data class Question(
    val id: String = UUID.randomUUID().toString(),
    val number: String = "",
    val type: QuestionType,
    val question: String,
    val options: List<Option> = emptyList(),
    val answer: List<String> = emptyList(),
    val analysis: String = "",
    val category: String = "",
    val score: Double? = null
)

data class Option(
    val key: String,
    val text: String
)

enum class QuestionType {
    SINGLE,
    MULTIPLE,
    JUDGE,
    BLANK,
    SHORT
}

data class ImportResult(
    val questions: List<Question>,
    val strategyName: String,
    val warnings: List<ImportWarning>,
    val diagnostics: ImportDiagnostics
)

data class ImportWarning(
    val level: WarningLevel,
    val questionNumber: String?,
    val message: String
)

enum class WarningLevel {
    NORMAL,
    WARNING,
    ERROR
}
```

---

## 七、安卓端必须保留的核心导入逻辑

## 1. 自动推荐策略

安卓端也应该保留 Web 版的思路：

```text
标准优先
复杂兜底
答案解析区作为结构化候选
```

不要一开始就让所有复杂策略乱跑。

推荐流程：

```text
导入文本
↓
文本标准化
↓
候选策略生成
↓
候选策略评分
↓
选择最优解析结果
↓
预览确认
↓
写入题库
```

---

## 2. 题目区和答案区分层

这是目前最关键的逻辑。

必须保留：

```text
题目区只解析题干、选项、题型
答案区只提取题号、答案、解析
最后再合并
```

不能让答案解析区继续进入普通题目解析器。

错误做法：

```text
把 1.【答案】A【解析】 当成一道新题解析
```

正确做法：

```text
把 1.【答案】A【解析】 当成第 1 题的答案和解析来源
```

---

## 3. 判断题边界

安卓端也要避免判断题误伤单选题。

规则：

```text
有 C/D/E/F/G 选项 → 优先选择题
3 个及以上选项 → 优先选择题
A. 是 / B. 否 → 默认单选题
A. 正确 / B. 错误 → 默认单选题
无选项 + 题尾 √/×/对/错 → 判断题
明确处于判断题分区 → 判断题
```

---

## 4. 文本答案保护

`splitAnswer()` 只应该处理客观题答案：

```text
A
AB
ACD
A、C、D
正确
错误
√
×
```

简答题、填空题答案应走文本答案逻辑。

例如：

```text
参考答案：检查设备、核对参数、进行安全交底。
```

不应被拆成多个选择题答案。

---

## 八、正则移植注意事项

JS 正则和 Kotlin 正则类似，但不能完全照搬。

JS：

```javascript
/答案[:：]\s*([A-D])/i
```

Kotlin：

```kotlin
Regex("答案[:：]\\s*([A-D])", RegexOption.IGNORE_CASE)
```

注意点：

```text
Kotlin 字符串里的反斜杠需要双重转义
建议使用 raw string："""..."""
中文标点要保留
RegexOption.IGNORE_CASE 替代 JS 的 i 标志
全局匹配使用 findAll()
```

示例：

```kotlin
val answerRegex = Regex(
    """(?:答案|正确答案|参考答案)[:：]\s*([A-G])""",
    RegexOption.IGNORE_CASE
)

val match = answerRegex.find(line)
val answer = match?.groupValues?.get(1)
```

---

## 九、文件导入建议

安卓端导入优先级：

```text
第一阶段：txt / 剪贴板
第二阶段：docx 文本提取
第三阶段：文字型 pdf
第四阶段：扫描版 pdf / 图片 OCR
```

不建议一开始就做 OCR。

原因：

```text
OCR 成本高
识别质量不稳定
图片题暂时也难自动结构化
会拖慢核心功能开发
```

建议先支持：

```text
txt
复制粘贴
docx
```

后续再考虑 pdf。

---

## 十、数据库设计建议

Room 表建议：

```text
question_banks
questions
options
practice_records
wrong_questions
favorites
import_logs
```

示例：

```text
question_banks
- id
- name
- createdAt
- updatedAt
- questionCount

questions
- id
- bankId
- number
- type
- question
- answer
- analysis
- category
- score

options
- id
- questionId
- key
- text

practice_records
- id
- questionId
- userAnswer
- isCorrect
- createdAt
```

---

## 十一、Web 版和安卓端如何保持一致

最重要的是不要靠“感觉一致”，而是靠测试样例一致。

推荐流程：

```text
Web 版导入样例
↓
导出 expected.json
↓
Kotlin parser 跑同样样例
↓
比较题数、题型、选项、答案、解析
↓
结果一致才算通过
```

比较项目：

```text
题目数量
题型分布
每题题干
选项数量
选项文本
答案
解析
异常提示
```

---

## 十二、开发顺序建议

推荐顺序：

```text
1. 整理 parser-spec
2. 搭建 Android 项目骨架
3. 设计 Room 数据库
4. 做题库列表 / 题目列表 / 刷题页
5. 实现 txt 文本导入
6. 移植标准逐行解析
7. 移植判断题和两选项单选规则
8. 移植答案解析区合并解析
9. 移植双文件导入
10. 支持 docx 文本提取
11. 做错题本、收藏、统计
12. 再考虑 pdf
```

---

## 十三、最终建议

最稳的路线是：

```text
Web 版继续作为功能验证版本
Android 版用 Kotlin 原生重写核心逻辑
用 parser-spec 保证两端解析结果一致
不要长期依赖 WebView
不要把 JS 逻辑直接硬塞进安卓界面
```

一句话总结：

```text
Web 版负责快速迭代解析规则；
Android 版负责原生体验；
parser-spec 负责让两端结果一致。
```
