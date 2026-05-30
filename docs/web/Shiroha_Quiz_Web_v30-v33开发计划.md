# Shiroha Quiz Web 端 v30-v33 开发交接文档

> 版本口径：用户内部版本号  
> 当前主线：Web 版 / 网页端  
> 固定目录：`apps/web/`  
> 当前最新有效状态：内部 `Web v33 / v30 富文本增强版`  
> 助手交付包流水号：`v54 → v57`

---

## 一、当前有效基线

当前 Web 端后续开发以用户最新上传的 `apps.zip` 为源码基线，并已叠加以下补丁：

```text
历史重复函数定义清理
内部 Web v30 / 助手包 v54：DOCX 富文本块识别
内部 Web v31 / 助手包 v55：DOCX 表格与图片显示增强
内部 Web v32 / 助手包 v56：MathJax 公式显示支持
内部 Web v33 / 助手包 v57：富文本 JSON 兼容与 v30 系列收口
```

当前最新有效交付包为：

```text
shiroha_web_v57_rich_json_v30_release_min.zip
```

该包只包含：

```text
apps/web/app.js
```

后续继续开发时，必须基于当前 `Web v33 / v30 富文本增强版` 继续叠加，不要回退到旧 v48、v49、纯 v53、v54、v55 或 v56 中间状态。

---

## 二、目录边界

### 2.1 本轮 Web 主线固定目录

```text
apps/web/
```

### 2.2 禁止误改目录

除非用户明确切换任务，否则不要修改：

```text
apps/android/app/src/main/assets/web/
apps/android/app/src/native/
```

三套代码含义如下：

```text
apps/web/                                  Web 版 / 网页端
apps/android/app/src/main/assets/web/      Android WebView 壳内置 Web
apps/android/app/src/native/               原生 Android / Compose 版
```

后续 Web 端开发、修复、交付默认只处理 `apps/web/`。

---

## 三、版本号口径

用户内部版本号与助手交付包流水号分开管理。

| 用户内部版本 | 功能阶段 | 助手交付包 |
|---|---|---|
| Web v30 | DOCX 富文本块识别 | `shiroha_web_v54_docx_rich_blocks_min.zip` |
| Web v31 | DOCX 表格与图片显示增强 | `shiroha_web_v55_docx_table_image_display_min.zip` |
| Web v32 | MathJax 公式显示支持 | `shiroha_web_v56_mathjax_formula_display_min.zip` |
| Web v33 | 富文本 JSON 兼容与 v30 系列收口 | `shiroha_web_v57_rich_json_v30_release_min.zip` |

后续说明建议统一写法：

```text
用户内部版本：Web v33
助手交付包：shiroha_web_v57_rich_json_v30_release_min.zip
```

---

## 四、本轮开发总目标

本轮 Web v30-v33 的目标是增强 DOCX 富文本题库导入与显示能力，重点面向 Word 统计学题库中的：

```text
Word 表格
图片 / 折线图 / 统计图
OMML 公式
LaTeX 文本公式
Unicode 数学字符
富文本 JSON 兼容
```

整体路线：

```text
DOCX 文档流结构识别
        ↓
段落 / 表格 / 图片 / OMML公式 / LaTeX文本 分流
        ↓
网页端稳定显示
        ↓
导出 JSON 保留富文本能力，同时保留旧字段安全降级
```

---

## 五、开发边界

### 5.1 本轮明确要做的方向

```text
DOCX 表格结构识别
DOCX 表格 HTML 显示
DOCX 图片位置与显示增强
OMML 公式来源识别
LaTeX 文本显示调度
MathJax 公式渲染
富文本 JSON 导入 / 导出兼容
```

### 5.2 本轮明确不做的内容

```text
不增强导入文本框粘贴图片
不增强 textarea 表格粘贴
不做 OCR
不从纯文本里强行还原表格
不把所有公式强制转成 LaTeX
不污染标准纯文本题库解析主线
不改 WebView 壳
不改原生 Compose
```

### 5.3 长期解析原则

```text
标准优先，复杂兜底。
```

普通纯文本题库、标准题库、选择题、判断题、答案集中区、解析集中区、紧凑选项等主线不能被 DOCX 富文本逻辑污染。

---

## 六、历史重复函数定义清理

在 Web v30-v33 之前，已完成一次 `apps/web/app.js` 历史重复函数定义清理。

### 目标

清理 IIFE 顶层历史重复 `function` 声明，避免后续维护时出现：

```text
修改了前面的旧函数，但运行时实际走后面的最终函数
```

### 结果

```text
node --check apps/web/app.js 通过
IIFE 顶层重复 function 检查：0 处
```

### 说明

后续修改必须保留该去重结果，不要用旧包覆盖导致重复函数回退。

---

## 七、Web v30：DOCX 富文本块识别

### 7.1 对应交付包

```text
shiroha_web_v54_docx_rich_blocks_min.zip
```

### 7.2 修改文件

```text
apps/web/app.js
```

### 7.3 核心目标

将 DOCX 提取从原来的“全局 `<w:p>` 段落抽取”，调整为按 Word document body 文档流识别块。

### 7.4 主要改动

新增识别：

```text
普通段落
Word 表格 <w:tbl>
图片引用
OMML 公式 <m:oMath> / <m:oMathPara>
LaTeX 文本
Unicode 数学字符
```

### 7.5 表格处理

Word 表格先转换为稳定文本块：

```text
【DOCX表格开始】
| 列1 | 列2 |
| --- | --- |
| 内容1 | 内容2 |
【DOCX表格结束】
```

此阶段目标是：

```text
表格不直接丢
行列关系比原来稳定
不再只是把表格单元格散成普通段落
```

### 7.6 公式处理

OMML 公式先转换为可读占位：

```text
【DOCX公式OMML：...】
```

支持基础文本化方向：

```text
分式
上标
下标
上下标
根号
求和
普通 m:t 文本
```

### 7.7 LaTeX 处理

原本就是 LaTeX 文本的内容保持原样，不强制转换，例如：

```text
\alpha
\frac{C}{k!}
e^{-2}
\begin{cases}
```

### 7.8 保留能力

继续保留：

```text
DOCX 图片提取
DOCX 上标 / 下标
v53 JSON 题型大小写互通
V83 图片字段兼容
历史重复函数定义清理
```

### 7.9 自检结果

```text
node --check apps/web/app.js 通过
IIFE 顶层重复 function 检查：0 处
压缩包只包含 apps/web/app.js
```

### 7.10 提交信息

```text
增强 Web 端 DOCX 富文本块识别
```

---

## 八、Web v31：DOCX 表格与图片显示增强

### 8.1 对应交付包

```text
shiroha_web_v55_docx_table_image_display_min.zip
```

### 8.2 修改文件

```text
apps/web/app.js
```

### 8.3 核心目标

把 Web v30 识别出的 DOCX 表格块和图片内容，在显示层稳定渲染出来。

### 8.4 表格显示

将以下块：

```text
【DOCX表格开始】
| 列1 | 列2 |
| --- | --- |
| 内容1 | 内容2 |
【DOCX表格结束】
```

在显示层转换为真实 HTML table。

显示策略：

```text
表格外层横向滚动
不靠空格 / Tab 对齐
不强行压缩列宽
避免移动端撑破题目卡片
```

### 8.5 图片显示

继续复用原有能力：

```text
DOCX 图片提取
data:image Markdown 图片
V83 图片字段兼容
```

并增强富文本渲染覆盖范围：

```text
题干
选项
练习反馈解析
考试结果解析
```

### 8.6 富文本渲染入口

题干、选项、解析等富文本内容统一走：

```text
renderQuestionContent
```

这样 DOCX 表格、图片 Markdown 可以在更多显示场景里稳定渲染。

### 8.7 本阶段未做

```text
未接 MathJax
未做 OMML 最终公式渲染
未做富文本 JSON 收口
未增强文本框
未做 OCR
未改 WebView 壳或原生 Compose
```

### 8.8 自检结果

```text
node --check apps/web/app.js 通过
IIFE 顶层重复 function 检查：0 处
压缩包只包含 apps/web/app.js
```

### 8.9 提交信息

```text
增强 Web 端 DOCX 表格与图片显示
```

---

## 九、Web v32：MathJax 公式显示支持

### 9.1 对应交付包

```text
shiroha_web_v56_mathjax_formula_display_min.zip
```

### 9.2 修改文件

```text
apps/web/app.js
```

### 9.3 核心目标

在富文本显示入口中增加公式分流渲染，让题干、选项、解析中的常见数学公式能够显示。

### 9.4 MathJax 加载策略

新增 MathJax v3 懒加载：

```text
优先尝试本地：
./libs/mathjax/tex-mml-chtml.js

本地缺失时回退 CDN：
https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js
```

如需完全离线使用，可后续将 MathJax 本地文件放入：

```text
apps/web/libs/mathjax/
```

### 9.5 LaTeX 文本支持

支持常见 LaTeX 文本显示调度，例如：

```text
\alpha
\theta
\mu
\sigma
\frac{...}{...}
e^{-2}
\begin{cases}...\end{cases}
```

### 9.6 OMML 占位显示增强

识别 Web v30 生成的 OMML 占位：

```text
【DOCX公式OMML：...】
```

并转换为可交给 MathJax 的 TeX 片段。

### 9.7 显示策略

```text
Unicode 数学字符直接显示
图片公式按图片显示
LaTeX 文本交给 MathJax
OMML 占位尽量转换后交给 MathJax
```

### 9.8 移动端适配

新增公式显示样式与横向滚动策略，避免：

```text
公式撑破题目卡片
页面横向溢出
移动端按钮被挤压
```

### 9.9 本阶段未做

```text
未做 OCR
未增强文本框
未改 WebView 壳
未改原生 Compose
未做完整 OMML 全结构转换
未做富文本 JSON 收口
```

### 9.10 自检结果

```text
node --check apps/web/app.js 通过
IIFE 顶层重复 function 检查：0 处
压缩包只包含 apps/web/app.js
```

### 9.11 提交信息

```text
接入 Web 端 MathJax 公式显示
```

---

## 十、Web v33：富文本 JSON 兼容与 v30 系列收口

### 10.1 对应交付包

```text
shiroha_web_v57_rich_json_v30_release_min.zip
```

### 10.2 修改文件

```text
apps/web/app.js
```

### 10.3 核心目标

在 DOCX 富文本块识别、表格图片显示、公式显示调度稳定后，补充 JSON 富文本兼容字段，同时保留旧字段安全降级。

### 10.4 新增 JSON 元数据

导出 JSON 时新增：

```text
richContentVersion: "shiroha-web-rich-v1"
richContentCapabilities
```

题目级新增：

```text
richContent
```

用于标记：

```text
docx_table
docx_omml_formula
latex_formula
image
```

### 10.5 保留安全降级字段

导出 JSON 继续保留普通字段：

```text
question
options
analysis
images
type
answer
```

目的：

```text
原生端或旧版本暂时不识别 richContent 时，也不影响基础字段读取。
```

### 10.6 导入兼容

导入 JSON 时兼容：

```text
richContent.fields.question
richContent.fields.analysis
richContent.fields.options
```

支持以下回退字段：

```text
text
markdown
sourceText
fallbackText
plainText
```

### 10.7 版本口径收口

更新：

```text
APP_VERSION = Web v33 / v30 富文本增强版
```

### 10.8 本阶段未做

```text
未新增 DOCX 解析大改
未做 OCR
未增强文本框
未改 WebView 壳
未改原生 Compose
未做完整 OMML 全结构转换
```

### 10.9 自检结果

```text
node --check apps/web/app.js 通过
IIFE 顶层重复 function 检查：0 处
压缩包只包含 apps/web/app.js
```

### 10.10 提交信息

```text
完善 Web 端富文本 JSON 兼容并整理 v30 版本
```

---

## 十一、当前必须继续保留的能力

后续 Web 端修改不能回退以下能力：

```text
DOCX 图片提取
DOCX 图片题绑定
浏览器端图片压缩 WebP
图片真题 + 后置答案解析
资料分析分块
紧凑选项拆分
图形推理图片题自动补 A/B/C/D
结束语清理
解析文本自动补“答案：X。”
xlsx / csv 表格题库导入
DOCX 上标 / 下标还原
导入文本查找替换
简答题 / 填空题判型优化
Web / 原生 JSON 题型大小写互通
V83 图片字段兼容
DOCX 富文本块识别
DOCX 表格 HTML 显示
DOCX 图片 Markdown 显示
MathJax 公式显示调度
富文本 JSON 兼容
历史重复函数定义清理
```

尤其要保留 v29.9 已验证的 DOCX 真题解析优化，不要因为统计学题库、富文本公式或 JSON 兼容继续污染 DOCX 主线。

---

## 十二、JSON 互通规则

### 12.1 Web 内部题型

Web 内部继续使用小写：

```text
single
multiple
judge
blank
short
```

不要全站改成大写。

### 12.2 跨端 JSON 导出

跨端 JSON 导出使用原生大写枚举：

```text
SINGLE
MULTIPLE
JUDGE
BLANK
SHORT
```

### 12.3 JSON 导入

导入时大小写都兼容，并统一归一为 Web 内部小写：

```text
single / SINGLE     → single
multiple / MULTIPLE → multiple
judge / JUDGE       → judge
blank / BLANK       → blank
short / SHORT       → short
```

### 12.4 富文本 JSON 原则

富文本字段只作为增强字段，不破坏旧字段：

```text
richContent 是增强字段
question / options / analysis / images 是安全降级字段
```

原生端或旧版本暂不支持 `richContent` 时，不应导致基础导入失败。

---

## 十三、公式显示路线

当前 Web v32-v33 的公式路线为：

```text
OMML 公式 → 识别为 DOCX OMML 来源 → 转成可交给 MathJax 的片段
LaTeX 文本 → MathJax TeX 输入显示
Unicode 数学字符 → 直接显示
图片公式 → 按图片显示
```

当前不是“所有公式都强转 LaTeX”。

继续坚持：

```text
OMML 是 OMML 来源
LaTeX 才走 LaTeX 显示
普通数学字符不强制公式化
图片公式不做 OCR
```

---

## 十四、表格显示路线

当前 Web v30-v33 的表格路线为：

```text
DOCX <w:tbl>
        ↓
按文档流识别为表格块
        ↓
转换为 Markdown 表格稳定块
        ↓
显示层转为真实 HTML table
        ↓
移动端横向滚动显示
```

表格不再依赖空格 / Tab 手动对齐。

复杂边界仍需注意：

```text
复杂合并单元格
多级表头
表格内嵌套图片
表格内复杂公式
Office 图表对象
```

这些可作为后续增强，不应影响当前基础表格显示主线。

---

## 十五、图片显示路线

当前图片路线为：

```text
DOCX 图片关系表
        ↓
提取图片
        ↓
转换 / 压缩为可显示 data:image
        ↓
Markdown 图片或结构化图片字段
        ↓
renderQuestionContent 显示
        ↓
JSON 导出保留 images / richContent 能力
```

不做 OCR。

如果 Word 里的折线图、柱状图是普通嵌入图片，当前路线应尽量显示。  
如果是 Office 图表对象而非图片，短期不保证完整解析，可后续按安全降级处理。

---

## 十六、后续开发建议

当前 Web v33 已完成本轮富文本增强收口。后续如果继续开发，建议优先做实际样本回归，而不是继续盲目扩展解析器。

优先级建议：

### 16.1 用真实统计学 DOCX 样本回归

重点检查：

```text
表格是否显示为 table
图片是否显示在正确题目
OMML 公式是否可读或能被 MathJax 显示
LaTeX 文本是否正确渲染
导入预览、练习、考试、错题本是否一致
```

### 16.2 再按样本问题补丁化修复

如果出现问题，建议按最小范围修复：

```text
表格问题 → 修 DOCX 表格块或表格显示
图片问题 → 修图片绑定或 renderQuestionContent
公式问题 → 修 OMML 文本化或 MathJax 调度
JSON 问题 → 修导入 / 导出边界
```

### 16.3 不建议马上做的事

```text
不建议马上重构整个解析器
不建议把所有题目结构改成富文本块数组
不建议一口气补完整 OMML 转换器
不建议把 WebView 壳和原生一起改
```

---

## 十七、交付规则

后续继续按最小替换包交付。

### 17.1 压缩包内容

只包含实际修改文件，例如：

```text
apps/web/app.js
apps/web/styles.css
apps/web/index.html
```

不要打包：

```text
build/
node_modules/
.git/
.idea/
.gradle/
local.properties
APK / AAB
无关素材
无关文档
```

### 17.2 每版交付说明

每次交付说明包含：

```text
用户内部版本号
助手交付包名
覆盖路径
改动要点
自检结果
中文提交信息
```

### 17.3 轻量检查

至少做：

```text
node --check apps/web/app.js
JSON 文件有效性检查
修改范围检查
IIFE 顶层重复 function 检查
```

不默认做完整构建，不声称浏览器 / 移动端实机验证通过。

---

## 十八、当前结论

当前 Web 端最新有效版本为：

```text
内部 Web v33 / v30 富文本增强版
```

当前最新有效交付包为：

```text
shiroha_web_v57_rich_json_v30_release_min.zip
```

后续开发必须基于该状态继续叠加。

最终路线概括：

```text
Web v30-v33 已完成 DOCX 富文本题库增强主线：
先识别 DOCX 富文本块，再稳定显示表格和图片，再接入 MathJax 公式显示，最后补齐富文本 JSON 兼容与版本收口。
```
