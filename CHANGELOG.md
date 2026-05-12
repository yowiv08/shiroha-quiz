# Changelog

## Unreleased

## v0.2.5-native

- 页面插画 PNG 全面迁移至 WebP，资源体积降低 98%
- 新增 AboutScreen、RecordDetailScreen、StandardImportFormatScreen
- 导入读取与解析后台化，减少大文本导入卡顿
- 练习设置面板紧凑化，组题方式切换为等宽按钮
- 练习提交后选项锁定，完成后显示总结
- 考试新增答题卡与未答交卷提醒
- 学习记录升级为一轮一条，支持逐题复盘
- 设置页增强数据导入/导出/备份功能
- 题库管理支持重命名，题库详情支持完整二次核对
- 启用 R8 代码压缩与资源收缩（Release 构建）
- 标准导入格式文档新增 AI 清洗章节与 Prompt 模板

## v0.2.3-native

- 新增题库重命名
- 二次核对功能
- 优化核对页面排版

## v0.4.3-alpha

- 优化答题体验
- 题库可以二次编辑

## v0.2.2-native

- 优化原生 APP 功能
- 增加导入导出
- 刷题体验优化
- 卡片布局优化

## v0.2.0-native

- 优化原生 APP UI 设计
- 功能实现
- 版本更新：native 0.2.0

## v0.4.2-alpha

- 增强对考公题目解析
- 优化 UI 布局
- 考试模式完善答题卡
- 原生 APP 框架基本落实
- 版本更新：web 0.4.2-alpha / native 0.1.6

## v0.3.8-alpha

- 修导航栏显示 bug

## v0.3.7-alpha

- Web 壳子构建，独立版本号管理
- 自适应图标 Safe Zone 修复
- 项目宣传图 + README 全面更新
- 图片素材 PNG → WebP 全面替换

## v0.3.6-alpha

- 双 productFlavors 架构：Web 壳子 + 原生 Compose 独立构建
- Web 版使用 `app-icon.png` 作为桌面图标
- 原生版 Compose 6 页面 + 完整导入链路
- 源码上传助手脚本

## v0.3.5-alpha

- 接入原生导入链、考试页与题库状态管理
- 清除冗余图片素材，换源 WebP
- 优化 Web UI 设计

## v0.3.4-alpha

- 修复 AndroidManifest 合并冲突
- 轻量开屏动画
- APP 卡片体积优化

## v0.2.0-alpha

- 安卓端切换为 WebView 壳方案，WebShellActivity 加载本地 web 资源作为过渡
- Web 端版本迭代至 v24：界面与交互优化修复
- 新增完整练习模式（上一题/下一题、答题卡、练习结束统计）
- 新增考试模式（限时、自动交卷、题型分值、及格线）
- 新增错题本（自动记录、掌握状态标记、错题重练）
- 新增刷题记录（练习/考试明细、作答详情展开）
- 新增双文件导入、答案解析区合并解析
- 新增 PDF 本地文本提取（PDF.js + 轻量回退）
- 新增 docx 本地文本提取
- 拆分素材图片并更新应用图标资源
- 更新 README 反映 WebView 壳过渡方案

## v0.1.1-alpha

- 优化 Web 端题库批量导出与备份 JSON 导入流程
- 新增题库批量选择导出、复制备份文本兜底、备份 JSON 合并/覆盖恢复
- 优化 README、仓库说明和文档目录结构
- 清理本地发布包、对比文件和临时产物的忽略规则

## v0.1.0-alpha

- 完成首次公开仓库整理
- 建立 monorepo 基础结构
- 收口 Web 工程到 `apps/web`
- 收口 Android 原生工程到 `apps/android`
- 增加中文产品、UI、页面和架构文档
- 增加 Android Compose 导航壳和首批原型页面
- 清理旧专有题库相关敏感内容

## v1.0.0-test

- 首次本地公开测试版本整理
- 验证 Git 仓库结构和首批代码组织方式
