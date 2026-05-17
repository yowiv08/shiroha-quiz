# ui — 设计令牌

原生 Compose 版设计系统。Web 版视觉对齐参考。

## 间距 (ShirohaSpacing)

| 令牌   | 值    |
| ---- | ---- |
| `Xs` | 4dp  |
| `Sm` | 8dp  |
| `Md` | 12dp |
| `Lg` | 16dp |
| `Xl` | 24dp |

## 颜色 (ShirohaColors)

| 令牌                 | 用途   |
| ------------------ | ---- |
| `BrandPrimarySoft` | 主色浅底 |
| `CardSoft`         | 卡片背景 |
| `CardMuted`        | 弱化背景 |
| `LineSoft`         | 分割线  |
| `LineSelected`     | 选中边框 |
| `TextSecondary`    | 次要文字 |

## 圆角 (ShirohaRadius)

| 令牌     | 值     |
| ------ | ----- |
| `Lg`   | 20dp  |
| `Pill` | 999dp |

## 核心组件

| 组件                     | 说明                 |
| ---------------------- | ------------------ |
| `GlassCard`            | 毛玻璃卡片，含入场动画        |
| `ActionPillButton`     | 胶囊按钮               |
| `IllustrationHeroCard` | 插画头图，跟随 Shiroha 模式 |
| `MetricGlassCard`      | 数据指标卡              |
| `NoticeCard`           | 提示卡片               |
| `ShirohaClick`         | 统一按压反馈组件           |

## Shiroha 模式

统一管控开屏图、页面插画、应用图标。可在更通用的场景下使用（二次元向）。关闭后所有角色插画不渲染，图标切回默认。

### 双图标系统

- `ShirohaLauncher`：Shiroha 角色图标（`ic_launcher_shiroha`）
- `DefaultLauncher`：简洁默认图标（`ic_launcher_default`）
- 通过 `LauncherIconSwitcher` 运行时切换，不杀进程
- 两套图标均位于 `native/` 源集内独立管理

### 素材

原生端统一使用 WebP 格式，压缩质量 q70，完整覆盖暗夜模式（无白色边框）。

## 实现

`apps/android/app/src/native/java/.../ui/theme/` + `ui/components/` + `ui/app/`
