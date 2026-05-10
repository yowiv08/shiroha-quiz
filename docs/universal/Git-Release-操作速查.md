# Git & Release 操作速查

## 日常提交（每次改代码都要做）

```powershell
# 1. 查看改了什么
git status

# 2. 添加文件（三选一）
git add apps/                    # 添加指定文件夹
git add README.md app.js         # 添加指定文件
git add .                        # 添加所有改动

# 3. 提交
git commit -m "feat: 做了什么"

# 4. 推送
git push origin main
```

---

## Commit 类型速查

| 类型          | 用途       | 示例                            |
| ----------- | -------- | ----------------------------- |
| `feat:`     | 新功能      | `feat: Android WebView 壳方案落地` |
| `fix:`      | 修复 bug   | `fix: 修复选项错位`                 |
| `style:`    | UI/样式调整  | `style: 调整刷题页面布局`             |
| `docs:`     | 文档修改     | `docs: 更新 README`             |
| `chore:`    | 配置/忽略等杂项 | `chore: 更新 .gitignore`        |
| `refactor:` | 重构       | `refactor: 拆分题库解析模块`          |

---

## 版本号规则

| 版本号            | 含义          |
| -------------- | ----------- |
| `v0.1.0-alpha` | 首次公开测试版     |
| `v0.1.1-alpha` | 修复 bug、小幅调整 |
| `v0.2.0-alpha` | 新增功能        |
| `v0.3.0-beta`  | 基本可用，进入测试阶段 |
| `v1.0.0`       | 正式稳定版       |

---

## 创建标签（Tag）

Tag 是 Git 层面的版本标记，独立于 Release。

```powershell
# 创建标签
git tag -a v0.2.0-alpha -m "v0.2.0-alpha"

# 推送标签到远端
git push origin v0.2.0-alpha

# 查看已有标签
git tag -l
```

---

## 创建 Release（基于已有的 Tag）

Release 是 GitHub 层面的发布，可以附带说明和附件。

### 方式一：一步到位（推荐）

创建 tag + 创建 release + 上传附件，一条命令搞定：

```powershell
gh release create v0.2.0-alpha "./apps/压缩包.zip" "./apps/xxx.apk" -t "v0.2.0-alpha" -n "版本说明"
```

多个附件用空格分隔，全部写在同一行。

### 方式二：分步操作（先打 tag，再建 release）

```powershell
# 第一步：打 tag 并推送
git tag -a v0.2.0-alpha -m "v0.2.0-alpha"
git push origin v0.2.0-alpha

# 第二步：基于已有 tag 创建 release（带附件）
gh release create v0.2.0-alpha "./apps/压缩包.zip" -t "v0.2.0-alpha" -n "版本说明"
```

### 方式三：草稿模式（先创建草稿，上传完再发布）

> **注意：** Release 发布后无法补传附件。不确定附件是否齐全时用此方式。

```powershell
# 第一步：创建草稿
gh release create v0.2.0-alpha -t "v0.2.0-alpha" -n "版本说明" -d

# 第二步：上传附件（可多次执行）
gh release upload v0.2.0-alpha "./apps/压缩包.zip" --repo reiqr/shiroha-quiz
gh release upload v0.2.0-alpha "./apps/xxx.apk" --repo reiqr/shiroha-quiz

# 第三步：发布草稿
gh release edit v0.2.0-alpha --draft=false
```

---

## 修改 Release 说明

```powershell
gh release edit v0.2.0-alpha -n "新的版本说明"
```

---

## 补传附件

> **重要：** Release 发布后默认不可补传。只有草稿状态才能上传附件。

如果确实需要补传，只能删除重建：

```powershell
# 1. 删除旧 release（不会删除 tag）
gh release delete v0.2.0-alpha --yes

# 2. 重建（带附件）
gh release create v0.2.0-alpha "./apps/压缩包.zip" "./apps/xxx.apk" -t "v0.2.0-alpha" -n "版本说明"
```

---

## 查看信息

```powershell
git status                        # 当前改动
git log --oneline -10             # 最近 10 条提交
git tag -l                        # 本地标签列表
gh release list                   # Release 列表
gh release view v0.2.0-alpha      # 查看某个 Release
```

---

## 撤销操作

```powershell
git restore 文件名                  # 撤销还没 add 的改动
git restore --staged 文件名         # 取消暂存
git rm -r --cached 文件夹名/        # 取消跟踪（不删除本地文件）
git tag -d v0.2.0-alpha            # 删除本地标签
git push origin :refs/tags/v0.2.0-alpha   # 删除远端标签
```

---

## 忽略文件

编辑 `.gitignore`，每行一条规则：

```text
文件夹名/          # 忽略整个文件夹
*.log             # 忽略所有 .log 文件
文件名.txt         # 忽略指定文件
```

加入 `.gitignore` = 永久排除。Git 不会检测这些文件的改动。

---

## 关键规则

| 规则                 | 说明                                 |
| ------------------ | ---------------------------------- |
| Tag 和 Release 是两回事 | Tag 是 Git 标记，Release 是 GitHub 发布页  |
| Release 发布后不可补传    | 创建时直接带附件，或用草稿模式                    |
| 压缩包不进源码仓库          | 放 `.gitignore`，只上传到 Release Assets |
| 附件同名优先改名           | 不要轻易覆盖，避免中途失败丢失旧文件                 |
| 不要删除 tag 重建        | 仓库有 tag 保护规则，删除后无法重建同名 tag         |
