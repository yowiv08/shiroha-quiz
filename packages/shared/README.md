# shared — 通用约定

## 备份 JSON Schema

```json
{
  "app": "Shiroha Quiz",
  "schemaVersion": 1,
  "exportType": "all_data",
  "exportedAt": "2026-05-12T00:00:00.000Z",
  "banks": [
    {
      "id": "uuid",
      "name": "题库名",
      "questions": [ /* 见 packages/types */ ]
    }
  ]
}
```

## 版本号

- Web 壳版：`v0.4.x-alpha`
- 原生版：`v0.2.x-native`
- 统一发布：`v1.0.0-beta`

## 跨端兼容

- Web 导出 JSON 可由原生版导入，反之亦然
- `schemaVersion` 变化向后兼容

## 导入格式文档

- [Markdown](../../docs/标准题库格式示例.md)
- [Word](../../docs/标准题库格式示例.docx)
- [PDF](../../docs/标准题库格式示例.pdf)
- App 内：`StandardImportFormatScreen`
