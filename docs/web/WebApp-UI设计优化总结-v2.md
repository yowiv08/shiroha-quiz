# Shiroha Quiz WebApp UI 设计优化总结 v2

> 说明：本文是阶段性规划 / 历史设计记录，部分版本号和待办项可能已经被后续实现覆盖。  
> 当前项目状态请优先参考根目录 `README.md`、`CHANGELOG.md` 和 `docs/native/原生开发进度.md`。

> 基于当前实际资源包 `main.zip`、项目内历史讨论，以及最新手机截图重新整理。  
> 本版不包含 Codex 提示词，只保留设计判断、问题定位、修改方向和可执行的代码级建议。  
> 适用范围：当前 Android Web 壳版本，即 `apps/web/index.html`、`apps/web/styles.css`、`apps/web/app.js`、`apps/android/app/src/main/java/com/codex/shirohaquiz/WebShellActivity.kt`、`apps/android/app/src/main/AndroidManifest.xml`。

---

## 1. 当前项目定位

Shiroha Quiz 现在不是普通刷题网站，也不是传统题库 App。它最核心的差异点是：

**用户可以自己导入题库，并在本地 Web / Android WebView 壳中完成练习、考试、错题复习、记录查看和数据备份。**

所以 UI 设计主线应该固定为：

```text
导入题库 → 核对识别结果 → 开始练习 / 考试 → 形成错题与记录 → 导出 / 备份数据
```

后续宣传图、首页文案、导入页设计、练习页设计，都应该围绕“自导入题库”展开，而不是泛泛写成“刷题练习工具”。

当前限制也要保留：

1. 目前没有云同步，不要在 UI 和宣传中暗示“云端题库”“多端同步”“账号体系”。
2. 当前 App 是 WebView 壳，主要体验由 `assets/web` 里的网页决定。
3. 短期目标是把 Web UI 做得更像手机 App，不是立刻重写原生 Android。
4. 不要为了 UI 好看破坏 v23 已经补上的数据导入、批量导出、备份恢复功能。

---

## 2. 当前资源包实际情况

项目当前结构重点如下：

```text
apps/
├─ android/app/src/main/
│  ├─ AndroidManifest.xml
│  ├─ java/com/codex/shirohaquiz/
│  │  ├─ WebShellActivity.kt
│  │  ├─ MainActivity.kt
│  │  └─ ui/... 原生 Compose 雏形
│  └─ res/...
├─ web/
│  ├─ index.html
│  ├─ styles.css
│  ├─ app.js
│  ├─ question-bank.js
│  ├─ app-icon.png
│  └─ libs/pdf.min.mjs / pdf.worker.min.mjs
```

当前启动入口是 `WebShellActivity`，核心代码是：

```kotlin
loadUrl("file:///android_asset/web/index.html")
```

> 注：`apps/web/` 下的文件在构建时被复制到 Android 的 `assets/web/` 目录中。

因此当前 Android App 的真实页面主要来自：

```text
apps/web/index.html
apps/web/styles.css
apps/web/app.js
```

已有优点：

1. 功能完整度已经比较高：导入题库、解析预览、题库管理、练习、考试、错题本、刷题记录、设置/导出都有。
2. v23 已经补上数据闭环：题库管理页可批量导出，导入页可导入配置/备份 JSON，设置页可导出/恢复全部数据。
3. 已经有移动端适配代码：`styles.css` 中存在 `Android Web shell mobile polish`。
4. 已经有练习页沉浸模式：`body.practice-focus` 会隐藏侧边栏和顶部栏，方向是对的。
5. WebView 已经支持文件选择，导入题库需要的文件选择流程有基础能力。

主要问题：

1. 页面仍然有 PC 后台工具感，手机 App 感不足。
2. 底部导航入口过多，当前有 8 个入口，手机端认知负担大。
3. 导入页功能堆叠严重，单文件导入、双文件导入、备份 JSON、格式说明、识别预览都在同一长页。
4. 表格在手机端仍依赖横向滚动，功能可用但不像移动 App。
5. 练习页已经进入沉浸模式，但选项布局、标题、按钮层级还需要修。
6. `app.js` 里动态注入 v23 数据工具面板和样式，短期可用，长期维护不利。
7. Android WebView 侧还缺下载处理、分享导出、错误页、返回逻辑细化等 App 壳体验。

---

## 3. 最新截图中的两个明确问题

截图中最明显的问题有两个：

1. 顶部显示 **“Shiroha Quiz Web Shell”**，太像开发调试名，不像正式 App。
2. 刷题选项布局错乱：单选圆点被撑到卡片中间，`A. / B. / C. / D.` 和选项文字被挤到右侧，甚至发生不自然换行。

这两个问题不是审美问题，而是需要优先修复的 UI bug。

---

## 4. 问题一：顶部 “Shiroha Quiz Web Shell” 应改掉

### 4.1 现象

手机截图顶部出现：

```text
Shiroha Quiz Web Shell
```

这会让用户感觉 App 是一个“网页壳测试版”，而不是正式产品。

### 4.2 可能来源

当前 `AndroidManifest.xml` 中有：

```xml
<activity
    android:name=".WebShellActivity"
    android:exported="true"
    android:label="Shiroha Quiz Web Shell"
    android:theme="@style/Theme.ShirohaQuiz.Starting">
```

这个 label 应该改成正式应用名。

### 4.3 建议修改

改为：

```xml
<activity
    android:name=".WebShellActivity"
    android:exported="true"
    android:label="@string/app_name"
    android:theme="@style/Theme.ShirohaQuiz.Starting">
```

`strings.xml` 中已经有：

```xml
<string name="app_name">Shiroha Quiz</string>
```

所以直接引用即可。

### 4.4 如果仍然显示标题栏

当前主题是：

```xml
<style name="Theme.ShirohaQuiz" parent="Theme.Material3.DayNight.NoActionBar">
```

理论上不应该显示传统 ActionBar。如果修改 label 后仍然出现类似标题栏，需要检查：

1. 启动页主题 `Theme.ShirohaQuiz.Starting` 切换后是否正确进入 `Theme.ShirohaQuiz`。
2. 是否存在系统或外层 Activity 标题栏。
3. 是否截图中的标题来自网页内部，而不是 Android 标题栏。

如果标题来自网页，则检查 `index.html`：

```html
<p class="kicker">Shiroha Quiz Web</p>
<h1 id="page-title">首页</h1>
```

移动端正式版建议把 `Shiroha Quiz Web` 改为更产品化的文案，例如：

```text
自导入题库
本地练习
```

或者在练习沉浸模式下完全隐藏网页顶部栏。

---

## 5. 问题二：刷题选项布局错乱的根因

### 5.1 现象

截图中每个选项卡片大致表现为：

```text
[很大的空白]    ○      A.   钢直尺
```

正确的布局应该是：

```text
○  A. 钢直尺
```

或者更 App 化一些：

```text
A  钢直尺                                      ○
```

目前截图中的布局会造成三个问题：

1. 选项左侧有大量无意义空白。
2. 单选圆点脱离选项文字，用户不容易判断点选区域。
3. 选项文字被挤窄，短词也会换行，例如“钢直尺”被拆成两行。

### 5.2 根因定位

当前 `questionHtml()` 中选项结构是：

```js
<label class="option" data-key="A">
  <input type="radio" name="q_xxx" value="A">
  <span class="option-key">A.</span>
  <span>钢直尺</span>
</label>
```

这个结构本身没有问题。

真正的问题在 `styles.css` 的移动端样式中：

```css
@media(max-width:720px){
  .top-actions select,.top-actions button,.actions button,.row-actions button,.form-grid label,input,select,textarea{width:100%}
}
```

这里的 `input` 是全局选择器，会把所有输入框都设置成：

```css
input { width: 100%; }
```

这会影响 `radio` 和 `checkbox`。在 `.option` 的 flex 布局中，单选圆点变成一个宽度接近 100% 的 flex 项，于是把 `A.` 和选项文字挤到右边，最终形成截图中的错位。

### 5.3 优先修复方式

不要在移动端全局写：

```css
input,select,textarea{width:100%}
```

应该把它限制在表单区域，例如：

```css
@media(max-width:720px){
  .top-actions select,
  .top-actions button,
  .actions button,
  .row-actions button,
  .form-grid label,
  .form-grid input,
  .form-grid select,
  .form-grid textarea,
  .split-grid textarea,
  .modal-panel input:not([type="radio"]):not([type="checkbox"]),
  .modal-panel select,
  .modal-panel textarea {
    width:100%;
  }
}
```

同时为选项里的单选和多选控件单独补规则：

```css
.option input[type="radio"],
.option input[type="checkbox"]{
  width:20px;
  min-width:20px;
  height:20px;
  flex:0 0 20px;
  padding:0;
  margin:2px 0 0;
  accent-color:var(--blue);
}

.option span:last-child{
  flex:1;
  min-width:0;
  overflow-wrap:anywhere;
  line-height:1.55;
}
```

这样可以立即解决截图中的布局问题。

---

## 6. 刷题页推荐重构方向

当前练习页已经有沉浸模式，但仍然偏“网页表单”。手机 App 中刷题页应该是全项目 UI 优化优先级最高的页面，因为这是用户停留时间最长的页面。

### 6.1 顶部区域

当前顶部是：

```text
刷题练习       1 / 50       退出练习
```

方向是对的，但建议再优化为：

```text
退出        1 / 50
[进度条]
```

原因：

1. “刷题练习”在沉浸模式下不是必要信息。
2. 当前题号和进度比标题更重要。
3. 进度条比单纯 `1 / 50` 更有 App 感。
4. 退出按钮不应该过大，避免误触；但也不能隐藏太深。

建议结构：

```html
<div class="practice-appbar">
  <button class="practice-exit">退出</button>
  <div class="practice-progress-wrap">
    <div class="practice-count">1 / 50</div>
    <div class="practice-progress-bar"><span style="width:2%"></span></div>
  </div>
</div>
```

### 6.2 题型标签

截图里有两个重复的“单选题”：

```text
单选题    单选题
```

这通常是因为一个标签来自题型，另一个标签来自分类/分组，而分类刚好也是“单选题”。

建议规则：

1. 第一个标签固定显示题型：单选 / 多选 / 判断 / 填空 / 简答。
2. 第二个标签只有在分类与题型不重复时显示。
3. 如果分类为空或与题型同义，不显示第二个标签。

例如：

```js
const typeLabel = label(q.type);
const category = q.category || '';
const showCategory = category && normalizeText(category) !== normalizeText(typeLabel);
```

最终显示：

```text
单选题
```

而不是：

```text
单选题    单选题
```

### 6.3 题干排版

截图中的题干：

```text
测绘零件的角度时一般使用（）
```

目前题干是普通文本。建议优化为：

1. 字号：18px 左右。
2. 行高：1.65。
3. 颜色：深色但不要纯黑。
4. 与选项之间保持 18px 到 22px 间距。
5. 长题干要支持自然换行，不要挤压。

建议样式：

```css
.quiz-card .question-title{
  font-size:18px;
  line-height:1.65;
  color:#172033;
  font-weight:700;
  margin:12px 0 18px;
}
```

### 6.4 选项卡片

手机端选项推荐使用“整行可点”的卡片形式。

建议视觉结构：

```text
┌────────────────────────────┐
│ A.  钢直尺                 │
└────────────────────────────┘
```

如果保留单选圆点，则建议：

```text
┌────────────────────────────┐
│ ○  A.  钢直尺              │
└────────────────────────────┘
```

不建议出现：

```text
┌────────────────────────────┐
│        ○          A. 钢直尺 │
└────────────────────────────┘
```

建议 CSS：

```css
.option{
  display:flex;
  align-items:flex-start;
  gap:10px;
  width:100%;
  padding:15px 14px;
  min-height:58px;
  border:1.5px solid var(--line);
  border-radius:16px;
  background:#fff;
}

.option-key{
  flex:0 0 auto;
  min-width:28px;
  font-weight:900;
  color:#25324b;
}

.option span:last-child{
  flex:1;
  min-width:0;
  color:#334155;
  font-weight:700;
}
```

更推荐的 App 化方案是隐藏原生 radio，用卡片状态表达选择：

```css
.option input[type="radio"],
.option input[type="checkbox"]{
  position:absolute;
  opacity:0;
  pointer-events:none;
}

.option::after{
  content:"";
  width:18px;
  height:18px;
  border-radius:50%;
  border:2px solid #cbd5e1;
  margin-left:auto;
  flex:0 0 18px;
}

.option.selected{
  background:#eff6ff;
  border-color:#2563eb;
}

.option.selected::after{
  border-color:#2563eb;
  box-shadow:inset 0 0 0 4px #fff;
  background:#2563eb;
}
```

这种方案更像正式 App，但修改范围稍大。短期先修复 `input width:100%` 即可。

### 6.5 底部按钮

截图中底部按钮是：

```text
提交答案
看答案
下一题
```

目前三个按钮垂直排列，功能清楚，但可以进一步优化：

未提交前：

```text
[提交答案]
[看答案]
```

提交后：

```text
[下一题]
[查看解析 / 已显示答案]
```

建议：

1. `提交答案` 作为主按钮，保持蓝色。
2. `看答案` 作为次按钮，降低视觉权重。
3. `下一题` 未提交前不要占太大空间，可以隐藏或放灰。
4. 提交后 `下一题` 变成主按钮。
5. 按钮区可以做成底部 sticky，长题目时仍方便操作。

---

## 7. 移动端导航优化

当前 `index.html` 中导航有 8 个入口：

```text
首页
导入题库
题库管理
刷题练习
考试模式
错题本
刷题记录
设置/导出
```

手机底部导航不适合放 8 个入口。建议改成 5 个主入口：

```text
首页
导入
练习
题库
我的
```

具体合并方式：

| 当前入口 | 建议归属 |
|---|---|
| 首页 | 首页 |
| 导入题库 | 导入 |
| 题库管理 | 题库 |
| 刷题练习 | 练习 |
| 考试模式 | 练习里的二级模式 |
| 错题本 | 我的 / 练习二级入口 |
| 刷题记录 | 我的 |
| 设置/导出 | 我的 |

这样手机端主导航更像 App，也不会出现底部横向滚动。

PC 端可以保留完整侧边栏，移动端单独压缩为 5 个主入口。

建议 CSS 思路：

```css
@media(max-width:720px){
  .side{
    display:grid;
    grid-template-columns:repeat(5,1fr);
    overflow:visible;
  }

  .nav.mobile-secondary{
    display:none;
  }
}
```

需要在 HTML 或 JS 中给二级入口增加标识，例如：

```html
<button class="nav mobile-secondary" data-view="exam">考试模式</button>
<button class="nav mobile-secondary" data-view="wrongbook">错题本</button>
<button class="nav mobile-secondary" data-view="records">刷题记录</button>
<button class="nav mobile-secondary" data-view="settings">设置/导出</button>
```

然后在“我的”页中用卡片入口进入这些功能。

---

## 8. 首页优化方向

当前首页文案偏版本说明：

```text
v23：数据导入导出优化版。
```

正式 App 首页不建议把版本号作为主视觉。用户打开 App 时最需要知道的是：

1. 当前有没有题库。
2. 能不能继续练习。
3. 能不能导入新题库。
4. 最近错题和记录情况。

建议首页改为：

```text
Shiroha Quiz
自导入题库刷题工具

[导入题库]
[开始练习]

当前题库：xxx
题目数量：225
错题：12
最近练习：今天 20 题
```

首页卡片优先级：

1. 主行动：导入题库 / 开始练习。
2. 当前题库概览。
3. 最近记录。
4. 错题提醒。
5. 数据备份提示。

版本号可以放到设置页，不应该放到首页首屏。

---

## 9. 导入题库页优化方向

导入题库是项目核心卖点，应该从“工具面板”改成“流程页面”。

当前导入页问题：

1. 表单项多。
2. 文本框大。
3. 帮助说明长。
4. 单文件、双文件、备份 JSON 混在一起。
5. 用户不知道下一步是什么。

建议改成三步：

```text
第 1 步：选择导入方式
第 2 步：识别并预览
第 3 步：确认保存
```

### 9.1 第 1 步：选择导入方式

用卡片而不是下拉框：

```text
[智能导入题库]
支持 TXT / Word / PDF / JSON，自动识别题目、选项、答案。

[题目 + 答案分离导入]
适合题目文件和答案文件分开的情况。

[导入备份 JSON]
导入从 Shiroha Quiz 导出的题库或全部数据备份。
```

### 9.2 第 2 步：识别结果预览

识别后优先展示：

```text
识别到 225 道题
单选 120，多选 60，判断 30，填空 15
有 3 条需要核对
```

比直接给大表格更适合手机。

### 9.3 第 3 步：确认保存

确认导入前显示：

```text
题库名称：安全培训题库
题目数量：225
保存方式：新建题库 / 合并到当前题库
```

按钮：

```text
[确认导入]
[返回修改]
```

---

## 10. 题库管理页优化方向

题库管理页目前有批量选择和导出能力，这是对的，但手机端不应该像表格后台。

建议每个题库作为卡片：

```text
安全培训题库
225 题 · 更新于 2026-05-08

[开始练习] [编辑] [导出]
```

批量模式单独开启：

```text
[批量管理]
```

进入批量模式后：

```text
□ 安全培训题库    225 题
□ 测绘题库        50 题
□ HSE 题库        120 题

已选择 2 个题库，共 275 题
[导出选中题库]
```

不要长期在普通卡片上显示太多批量操作，否则会让页面显得像管理后台。

---

## 11. 设置/导出页优化方向

设置/导出页现在承担了很多功能：

1. 保存数据。
2. 导出全部数据。
3. 复制备份文本。
4. 导入全部数据/备份 JSON。
5. 清空本地数据。

建议重新分组：

```text
数据备份
- 导出全部数据
- 复制备份文本
- 导入备份 JSON

题库迁移
- 导出当前题库
- 导入单题库 JSON

危险操作
- 清空本地数据
```

文案要明确区分：

| 类型 | 说明 |
|---|---|
| 单题库 JSON | 只包含一个或多个题库，主要用于题库迁移 |
| 全部数据备份 JSON | 包含题库、错题、记录、设置，主要用于整机恢复 |
| 复制备份文本 | 解决手机 WebView 下载不弹窗的问题 |

---

## 12. 表格移动端处理

当前移动端 `table{min-width:720px}`，会导致横向滚动。

对于后台工具这是可接受的，但对 App 不友好。建议：

1. PC 端继续使用表格。
2. 手机端改为卡片列表。
3. 只有复杂明细报告保留横向滚动。

例如识别预览移动端：

```text
第 1 题 · 单选
测绘零件的角度时一般使用（）
A. 钢直尺
B. 百分表
C. 千分尺
D. 万能角度尺
答案：D
状态：正常
```

比横向滚动表格更容易检查。

---

## 13. Android WebView 壳优化方向

当前 `WebShellActivity.kt` 已经支持：

1. JavaScript。
2. localStorage / DOM Storage。
3. 文件访问。
4. 文件选择器。
5. Android 返回键基础处理。

建议继续补：

### 13.1 下载处理

当前 Web 端 `download()` 在普通浏览器中可用，但 Android WebView 里经常没有明显响应。应在 WebView 侧增加 `DownloadListener` 或用 JavaScript bridge 做分享/保存。

最低限度也要保留现在已有的“复制备份文本”作为降级方案。

### 13.2 返回键逻辑

当前返回键逻辑是：

```kotlin
if (webView.canGoBack()) {
    webView.goBack()
} else {
    onBackPressedDispatcher.onBackPressed()
}
```

但单页应用内部切换页面不一定产生 WebView 历史记录。建议 Web 页面内部维护当前 view，Android 返回键优先触发网页内返回：

```text
练习页 → 弹出确认退出
二级页 → 返回上一主页面
首页 → 退出 App
```

### 13.3 加载失败页

即使是本地 asset，也建议准备简单错误页：

```text
页面加载失败
请重新打开 App
[重试]
```

### 13.4 状态栏和导航栏

当前主题已经设置透明状态栏和浅色导航栏。后续要检查：

1. 刘海屏 / 挖孔屏状态栏是否遮挡内容。
2. 底部导航是否和系统导航条重叠。
3. 横屏时是否出现布局挤压。

---

## 14. 视觉风格建议

Shiroha Quiz 适合走干净、轻量、学习工具感的路线，不建议太花。

建议设计关键词：

```text
清爽
轻量
题库感
专注刷题
本地可靠
```

建议主色：

```text
蓝色系继续保留
```

但可以降低大面积渐变和阴影，让界面更稳定。

建议组件风格：

| 组件 | 建议 |
|---|---|
| 卡片 | 白底、浅边框、轻阴影 |
| 主按钮 | 蓝色实心，圆角 14-16px |
| 次按钮 | 浅灰底或白底描边 |
| 标签 | 小胶囊，但不要重复显示 |
| 列表 | 手机端卡片化 |
| 弹窗 | 手机端全屏底部页或全屏面板 |
| 提示 | Toast 保留，但不要遮挡主按钮 |

---

## 15. 优先级排序

### P0：必须先修

1. 修改 `AndroidManifest.xml` 中 `WebShellActivity` 的 label：

```xml
android:label="@string/app_name"
```

2. 修复移动端全局 `input{width:100%}` 导致的单选/多选布局错乱。
3. 去掉刷题页重复题型标签，例如“单选题 单选题”。
4. 检查手机端导出全部数据无响应时的降级提示，确保用户知道可以复制备份文本。

### P1：核心体验优化

1. 刷题页改成更完整的 App 沉浸式布局。
2. 导入题库页改为三步流程。
3. 手机端底部导航压缩为 5 个主入口。
4. 题库管理页移动端卡片化。
5. 设置/导出页重新分组。

### P2：质量提升

1. 表格移动端卡片化。
2. 练习结果页优化。
3. 错题本复习入口强化。
4. 加入空状态、加载状态、错误状态。
5. 优化深色模式或至少保证系统深色模式下不崩。

### P3：长期方向

1. 把 `app.js` 中动态注入的 v23 面板整理回结构化 HTML / 模块化 JS。
2. 为未来原生 Android 复刻导入逻辑整理 UI 状态机。
3. 增加数据迁移版本号和备份兼容提示。
4. 加入更完整的本地文件保存 / 分享导出能力。

---

## 16. 推荐的阶段性改造路线

### 第一阶段：修 bug，让手机端可正常用

目标：解决截图中的明显问题。

需要改：

1. `AndroidManifest.xml`：改 Activity label。
2. `styles.css`：修复移动端全局 input 宽度。
3. `styles.css`：补 `.option input[type=radio]` 和 `.option input[type=checkbox]` 规则。
4. `app.js`：题型标签去重。

验收标准：

1. 顶部不再显示 `Web Shell`。
2. 选项显示为正常横向布局。
3. `A. 钢直尺` 不再被挤到右侧或拆成两行。
4. 单选、多选、判断题都能正常选择和提交。

### 第二阶段：刷题页 App 化

目标：让最高频页面像正式 App。

需要改：

1. 练习页顶部增加进度条。
2. 选项卡片统一高度、边框、选中态、正确/错误态。
3. 提交前后按钮状态重排。
4. 答案解析区域改为卡片。
5. 长题目下按钮区不丢失。

验收标准：

1. 360px、390px、412px 宽度下无横向滚动。
2. 单手操作时主按钮容易点击。
3. 提交后正确/错误反馈明确。
4. 连续刷 50 题不出现布局跳动或遮挡。

### 第三阶段：导入流程重构

目标：把核心卖点做清楚。

需要改：

1. 导入方式卡片化。
2. 识别结果摘要前置。
3. 预览列表手机端卡片化。
4. 异常题目优先展示。
5. 确认导入页明确保存方式。

验收标准：

1. 新用户能理解“该点哪里导入”。
2. 用户能看出识别到了多少题。
3. 用户能知道哪些题需要核对。
4. 导入普通题库 JSON 和全部备份 JSON 不再混淆。

### 第四阶段：整体导航和页面整理

目标：从网页工具变成 App。

需要改：

1. 手机端底部导航压缩为 5 个主入口。
2. “我的”页承载错题、记录、设置、导出。
3. 首页去掉版本号主视觉，改为用户任务入口。
4. 题库管理页卡片化。

验收标准：

1. 底部导航不横向滚动。
2. 主要功能两步内可达。
3. 首页首屏能体现“自导入题库”。
4. 页面整体不像后台管理面板。

---

## 17. 建议立即改动的最小补丁范围

如果现在只想快速解决截图问题，不做大改，最小改动是：

### 17.1 修改 `AndroidManifest.xml`

把：

```xml
android:label="Shiroha Quiz Web Shell"
```

改成：

```xml
android:label="@string/app_name"
```

### 17.2 修改 `styles.css` 移动端全局 input 规则

把移动端这类规则：

```css
.top-actions select,.top-actions button,.actions button,.row-actions button,.form-grid label,input,select,textarea{width:100%}
```

改成限定版：

```css
.top-actions select,
.top-actions button,
.actions button,
.row-actions button,
.form-grid label,
.form-grid input,
.form-grid select,
.form-grid textarea,
.split-grid textarea{
  width:100%;
}
```

### 17.3 追加选项控件修复

在 `styles.css` 末尾追加：

```css
.option input[type="radio"],
.option input[type="checkbox"]{
  width:20px;
  min-width:20px;
  height:20px;
  flex:0 0 20px;
  padding:0;
  margin:2px 0 0;
  accent-color:var(--blue);
}

.option span:last-child{
  flex:1;
  min-width:0;
  overflow-wrap:anywhere;
  line-height:1.55;
}

@media(max-width:720px){
  .option{
    align-items:flex-start;
    min-height:58px;
  }
}
```

### 17.4 去除重复题型标签

当前 `questionHtml()` 里直接显示：

```js
<span class="pill">${label(q.type)}</span>
<span class="pill">${esc(q.category||'未分类')}</span>
```

建议改为：

```js
const typeText = label(q.type);
const categoryText = q.category || '';
const showCategory = categoryText && normalizeText(categoryText) !== normalizeText(typeText);
const meta = `<div class="qmeta">
  <span class="pill">${typeText}</span>
  ${showCategory ? `<span class="pill">${esc(categoryText)}</span>` : ''}
  ${examMode ? `<span class="pill">${scoreOf(q)}分</span>` : ''}
</div><div class="question-title">${examMode ? idx + '. ' : ''}${esc(q.question)}</div>`;
```

如果担心 `normalizeText` 对中文题型映射不够，可以先用简单排除：

```js
const sameCategory = ['单选题','单选','多选题','多选','判断题','判断','填空题','填空','简答题','简答'].includes(categoryText);
```

---

## 18. 最终验收清单

### 手机尺寸

至少检查：

```text
360 × 800
390 × 844
412 × 915
```

### 页面

必须检查：

1. 首页。
2. 导入题库。
3. 题库管理。
4. 刷题练习。
5. 考试模式。
6. 错题本。
7. 刷题记录。
8. 设置/导出。

### 刷题题型

必须检查：

1. 单选题。
2. 多选题。
3. 判断题。
4. 填空题。
5. 简答题。

### 状态

必须检查：

1. 未选择答案。
2. 已选择答案。
3. 提交正确。
4. 提交错误。
5. 查看答案。
6. 下一题。
7. 退出练习。
8. 本轮完成。

### 数据操作

必须检查：

1. 单文件导入。
2. 双文件导入。
3. 题库批量导出。
4. 导入批量导出的 JSON。
5. 导出全部数据。
6. 复制全部数据备份文本。
7. 覆盖恢复。
8. 合并导入。

---

## 19. 结论

当前版本的功能基础已经够用了，问题主要不是“功能不够”，而是：

1. 手机端 CSS 有明显 bug，导致刷题选项布局错乱。
2. Android Activity label 仍带 `Web Shell`，影响正式感。
3. 页面组织仍然像 PC 工具面板，不像移动 App。
4. 导入题库这个核心卖点没有在首页和流程设计中被足够突出。

最应该先做的是：

```text
先修截图里的 P0 问题 → 再优化刷题页 → 再重构导入流程 → 最后整理导航和设置页
```

不要一开始就大规模重写。当前最小修复只需要改 `AndroidManifest.xml`、`styles.css` 和 `app.js` 的一小段逻辑，就能明显提升手机端观感和可用性。
