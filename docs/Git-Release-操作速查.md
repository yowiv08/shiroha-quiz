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

| 类型          | 用途       | 示例                          |
| ----------- | -------- | --------------------------- |
| `feat:`     | 新功能      | `feat: Android WebView 壳方案` |
| `fix:`      | 修复 bug   | `fix: 修复选项错位`               |
| `style:`    | UI/样式调整  | `style: 调整刷题页面布局`           |
| `docs:`     | 文档修改     | `docs: 更新 README`           |
| `chore:`    | 配置/忽略等杂项 | `chore: 更新 .gitignore`      |
| `refactor:` | 重构       | `refactor: 拆分题库解析模块`        |

---

## 发布新版本（一步到位）

```powershell
# 带附件（一步创建 tag + release + 上传）
gh release create v0.2.0-alpha ./apps/文件名.zip -t "v0.2.0-alpha" -n "版本说明"

# 不带附件
gh release create v0.2.0-alpha -t "v0.2.0-alpha" -n "版本说明"
```

版本号规则：

| 版本号            | 含义          |
| -------------- | ----------- |
| `v0.1.0-alpha` | 首次公开测试版     |
| `v0.1.1-alpha` | 修复 bug、小幅调整 |
| `v0.2.0-alpha` | 新增功能        |
| `v0.3.0-beta`  | 基本可用，进入测试阶段 |
| `v1.0.0`       | 正式稳定版       |

---

## 修改 Release（不删除重建）

```powershell
# 修改说明
gh release edit v0.1.0-alpha -n "新的版本说明"

# 补上传附件
gh release upload v0.1.0-alpha ./apps/文件名.zip
```

---

## 查看信息

```powershell
git status                        # 当前改动
git log --oneline -10             # 最近 10 条提交
gh release list                   # Release 列表
gh release view v0.1.0-alpha      # 查看某个 Release
```

---

## 撤销操作

```powershell
git restore 文件名                 # 撤销还没 add 的改动
git restore --staged 文件名        # 取消暂存
git rm -r --cached 文件夹名/      # 取消跟踪（不删除本地文件）
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
