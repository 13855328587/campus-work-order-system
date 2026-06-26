from pathlib import Path
from xml.sax.saxutils import escape
from zipfile import ZIP_DEFLATED, ZipFile


ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "04_AI使用记录"
OUT_PATH = OUT_DIR / "AI使用记录.docx"


def x(text):
    return escape(text, {"'": "&apos;", '"': "&quot;"})


def p(text="", style=None, bold=False, color=None, size=22, align=None, spacing_after=120):
    p_pr = []
    if style:
        p_pr.append(f'<w:pStyle w:val="{style}"/>')
    if align:
        p_pr.append(f'<w:jc w:val="{align}"/>')
    p_pr.append(f'<w:spacing w:after="{spacing_after}"/>')
    r_pr = [
        '<w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:eastAsia="Microsoft YaHei"/>',
        f'<w:sz w:val="{size}"/>',
        f'<w:szCs w:val="{size}"/>',
    ]
    if bold:
        r_pr.append("<w:b/>")
        r_pr.append("<w:bCs/>")
    if color:
        r_pr.append(f'<w:color w:val="{color}"/>')
    return (
        "<w:p>"
        f"<w:pPr>{''.join(p_pr)}</w:pPr>"
        "<w:r>"
        f"<w:rPr>{''.join(r_pr)}</w:rPr>"
        f"<w:t xml:space=\"preserve\">{x(text)}</w:t>"
        "</w:r>"
        "</w:p>"
    )


def bullet(text):
    return (
        "<w:p>"
        '<w:pPr><w:pStyle w:val="ListParagraph"/>'
        '<w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr>'
        '<w:spacing w:after="80"/></w:pPr>'
        "<w:r><w:rPr>"
        '<w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:eastAsia="Microsoft YaHei"/>'
        '<w:sz w:val="22"/><w:szCs w:val="22"/>'
        "</w:rPr>"
        f"<w:t>{x(text)}</w:t>"
        "</w:r></w:p>"
    )


def code_block(text):
    lines = text.splitlines() or [text]
    body = "".join(
        "<w:p><w:pPr><w:spacing w:after=\"0\"/></w:pPr>"
        "<w:r><w:rPr>"
        '<w:rFonts w:ascii="Consolas" w:hAnsi="Consolas" w:eastAsia="Microsoft YaHei"/>'
        '<w:sz w:val="19"/><w:szCs w:val="19"/>'
        "</w:rPr>"
        f"<w:t xml:space=\"preserve\">{x(line)}</w:t>"
        "</w:r></w:p>"
        for line in lines
    )
    return (
        '<w:tbl><w:tblPr><w:tblW w:w="9360" w:type="dxa"/>'
        '<w:tblBorders><w:top w:val="single" w:sz="4" w:color="D9E2F3"/>'
        '<w:left w:val="single" w:sz="4" w:color="D9E2F3"/>'
        '<w:bottom w:val="single" w:sz="4" w:color="D9E2F3"/>'
        '<w:right w:val="single" w:sz="4" w:color="D9E2F3"/>'
        '<w:insideH w:val="single" w:sz="4" w:color="D9E2F3"/>'
        '<w:insideV w:val="single" w:sz="4" w:color="D9E2F3"/></w:tblBorders>'
        '<w:tblCellMar><w:top w:w="120" w:type="dxa"/><w:left w:w="160" w:type="dxa"/>'
        '<w:bottom w:w="120" w:type="dxa"/><w:right w:w="160" w:type="dxa"/></w:tblCellMar>'
        "</w:tblPr><w:tblGrid><w:gridCol w:w=\"9360\"/></w:tblGrid>"
        "<w:tr><w:tc><w:tcPr><w:tcW w:w=\"9360\" w:type=\"dxa\"/>"
        '<w:shd w:fill="F4F6F9"/></w:tcPr>'
        f"{body}</w:tc></w:tr></w:tbl>"
    )


def meta_table(rows):
    tr_xml = []
    for label, value in rows:
        tr_xml.append(
            "<w:tr>"
            '<w:tc><w:tcPr><w:tcW w:w="2160" w:type="dxa"/><w:shd w:fill="F2F4F7"/></w:tcPr>'
            f'{p(label, bold=True, size=21, spacing_after=0)}</w:tc>'
            '<w:tc><w:tcPr><w:tcW w:w="7200" w:type="dxa"/></w:tcPr>'
            f'{p(value, size=21, spacing_after=0)}</w:tc>'
            "</w:tr>"
        )
    return (
        '<w:tbl><w:tblPr><w:tblW w:w="9360" w:type="dxa"/>'
        '<w:tblBorders><w:top w:val="single" w:sz="4" w:color="BFBFBF"/>'
        '<w:left w:val="single" w:sz="4" w:color="BFBFBF"/>'
        '<w:bottom w:val="single" w:sz="4" w:color="BFBFBF"/>'
        '<w:right w:val="single" w:sz="4" w:color="BFBFBF"/>'
        '<w:insideH w:val="single" w:sz="4" w:color="BFBFBF"/>'
        '<w:insideV w:val="single" w:sz="4" w:color="BFBFBF"/></w:tblBorders>'
        '<w:tblCellMar><w:top w:w="80" w:type="dxa"/><w:left w:w="120" w:type="dxa"/>'
        '<w:bottom w:w="80" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tblCellMar>'
        "</w:tblPr><w:tblGrid><w:gridCol w:w=\"2160\"/><w:gridCol w:w=\"7200\"/></w:tblGrid>"
        f"{''.join(tr_xml)}</w:tbl>"
    )


def record(title, prompt, process, result, note=None):
    parts = [p(title, "Heading2", bold=True, color="2E74B5", size=26, spacing_after=120)]
    parts.append(p("用户提示词：", bold=True, spacing_after=60))
    parts.append(code_block(prompt))
    parts.append(p("AI 处理过程：", bold=True, spacing_after=60))
    parts.extend(bullet(item) for item in process)
    parts.append(p("产出结果：", bold=True, spacing_after=60))
    parts.extend(bullet(item) for item in result)
    if note:
        parts.append(p("当前记录：", bold=True, spacing_after=60))
        parts.extend(bullet(item) for item in note)
    return "".join(parts)


def styles_xml():
    return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:style w:type="paragraph" w:default="1" w:styleId="Normal">
    <w:name w:val="Normal"/>
    <w:pPr><w:spacing w:after="120" w:line="264" w:lineRule="auto"/></w:pPr>
    <w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:eastAsia="Microsoft YaHei"/><w:sz w:val="22"/></w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading1">
    <w:name w:val="heading 1"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/>
    <w:pPr><w:keepNext/><w:spacing w:before="320" w:after="160"/><w:outlineLvl w:val="0"/></w:pPr>
    <w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:eastAsia="Microsoft YaHei"/><w:b/><w:color w:val="2E74B5"/><w:sz w:val="32"/></w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Heading2">
    <w:name w:val="heading 2"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/>
    <w:pPr><w:keepNext/><w:spacing w:before="240" w:after="120"/><w:outlineLvl w:val="1"/></w:pPr>
    <w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:eastAsia="Microsoft YaHei"/><w:b/><w:color w:val="2E74B5"/><w:sz w:val="26"/></w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="ListParagraph">
    <w:name w:val="List Paragraph"/><w:basedOn w:val="Normal"/>
    <w:pPr><w:ind w:left="720"/></w:pPr>
  </w:style>
</w:styles>"""


def numbering_xml():
    return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:numbering xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:abstractNum w:abstractNumId="0">
    <w:multiLevelType w:val="hybridMultilevel"/>
    <w:lvl w:ilvl="0"><w:start w:val="1"/><w:numFmt w:val="bullet"/><w:lvlText w:val="•"/>
      <w:lvlJc w:val="left"/><w:pPr><w:tabs><w:tab w:val="num" w:pos="720"/></w:tabs><w:ind w:left="720" w:hanging="360"/></w:pPr>
      <w:rPr><w:rFonts w:ascii="Symbol" w:hAnsi="Symbol"/></w:rPr>
    </w:lvl>
  </w:abstractNum>
  <w:num w:numId="1"><w:abstractNumId w:val="0"/></w:num>
</w:numbering>"""


def document_xml():
    body = []
    body.append(p("AI 使用记录", bold=True, color="0B2545", size=44, align="center", spacing_after=60))
    body.append(p("校园工单管理系统 | Codex 协作过程归档 | 2026-06-25", color="666666", size=20, align="center", spacing_after=240))
    body.append(
        meta_table(
            [
                ("项目名称", "校园工单管理系统"),
                ("记录日期", "2026-06-25"),
                ("AI 工具", "Codex"),
                ("记录范围", "项目开发、部署、性能验证、前端功能协作与 AI 使用记录归档"),
            ]
        )
    )
    body.append(p("1. 使用目的", "Heading1", bold=True, color="2E74B5", size=32))
    body.append(
        p(
            "在校园工单管理系统开发过程中，使用 AI 辅助完成项目结构检查、功能实现建议、部署配置整理、"
            "性能测试分析、前端页面改造以及开发记录归档。AI 主要承担代码阅读、实现方案梳理、文件修改、"
            "命令执行、测试验证和文档生成等工作。"
        )
    )
    body.append(p("2. 主要对话与提示词记录", "Heading1", bold=True, color="2E74B5", size=32))
    body.append(
        record(
            "2.1 项目环境与结构检查",
            "查看当前项目结构，确认前后端目录和部署文件。",
            [
                "读取项目根目录文件结构。",
                "识别后端目录、前端目录、性能测试目录和脚本目录。",
                "检查 .env、.env.example、docker-compose.yml、DEPLOYMENT.md 等部署相关文件。",
            ],
            ["明确项目采用前后端分离结构。", "确认项目中已经包含部署配置和性能测试相关目录。"],
        )
    )
    body.append(
        record(
            "2.2 部署配置与说明文档整理",
            "帮我完善项目部署说明和环境变量配置。",
            [
                "检查 Docker Compose 配置。",
                "对比 .env 与 .env.example。",
                "整理服务启动、数据库连接、端口配置、后端接口地址等信息。",
                "补充部署文档中的启动步骤、配置说明和常见检查项。",
            ],
            ["完善部署说明文件 DEPLOYMENT.md。", "补充环境变量示例文件 .env.example。", "使项目具备更清晰的本地或服务器部署指引。"],
        )
    )
    body.append(
        record(
            "2.3 性能测试与压力验证",
            "验证校园工单系统是否能满足每分钟 1000 次请求的性能要求。",
            [
                "使用项目中的性能测试能力检查接口压测方案。",
                "分析 k6 压测脚本、接口路径、并发模型和阈值设置。",
                "根据测试目标关注请求成功率、响应时间、吞吐量和错误率。",
            ],
            ["形成以 1000 requests/min 为目标的性能验证思路。", "明确性能测试应关注 HTTP 请求失败率、平均响应时间、P95/P99 响应时间以及服务稳定性。"],
        )
    )
    body.append(
        record(
            "2.4 前端用户管理页面改造",
            "加一个新增用户 @UserManage.vue",
            [
                "定位目标文件 campus-work-order-frontend/campus-work-order-frontend/src/views/admin/UserManage.vue。",
                "计划在管理员用户管理页面中增加新增用户入口。",
                "预期实现内容包括新增按钮、弹窗表单、表单字段校验和提交逻辑。",
            ],
            ["该需求已被提出并纳入开发记录。"],
            [
                "后续实现时应确认项目现有 API 封装方式、用户字段结构、角色枚举或权限字段。",
                "新增成功后应刷新列表，并保持与现有编辑、删除、查询功能的 UI 风格一致。",
            ],
        )
    )
    body.append(
        record(
            "2.5 AI 使用记录文档生成",
            "├── 04_AI使用记录/\n│   ├── 提示词记录.md\n│   └── (可选) skill代码/，跟我总结咱们对话文档给我",
            [
                "检查项目根目录是否已有 04_AI使用记录。",
                "发现该目录不存在后，新建目录。",
                "根据当前项目协作内容生成提示词记录.md。",
                "将主要提示词、AI 操作过程和产出结果整理为 Markdown 文档。",
            ],
            ["生成 04_AI使用记录/提示词记录.md。", "继续根据用户要求生成 Word 版 AI 使用记录。"],
        )
    )
    body.append(p("3. AI 辅助内容总结", "Heading1", bold=True, color="2E74B5", size=32))
    body.extend(
        bullet(item)
        for item in [
            "辅助理解项目目录和模块职责。",
            "根据需求定位可能需要修改的代码文件。",
            "生成或完善部署、环境变量和性能测试相关文档。",
            "梳理前端功能开发方案。",
            "将开发过程中的提示词和 AI 响应整理成规范记录。",
        ]
    )
    body.append(p("4. 使用 AI 的收益", "Heading1", bold=True, color="2E74B5", size=32))
    body.extend(
        bullet(item)
        for item in [
            "提高项目结构梳理和文件定位效率。",
            "降低部署文档、测试记录、开发记录等重复性文档工作的成本。",
            "在功能开发前帮助明确实现边界和风险点。",
            "便于后续复盘项目开发过程，说明 AI 在软件工程实践中的具体使用方式。",
        ]
    )
    body.append(p("5. 注意事项", "Heading1", bold=True, color="2E74B5", size=32))
    body.extend(
        bullet(item)
        for item in [
            "AI 生成的代码和文档需要结合实际项目运行结果进行验证。",
            "涉及新增、修改、删除等核心业务功能时，应通过前端页面操作和后端接口测试共同确认。",
            "性能测试结果应以实际压测输出为准，不能只依赖理论分析。",
            "项目最终提交前，应检查 AI 生成内容是否与真实实现一致。",
        ]
    )
    body.append(
        '<w:sectPr><w:pgSz w:w="12240" w:h="15840"/>'
        '<w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="708" w:footer="708" w:gutter="0"/>'
        "</w:sectPr>"
    )
    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:wpc="http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas"
 xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"
 xmlns:o="urn:schemas-microsoft-com:office:office"
 xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
 xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math"
 xmlns:v="urn:schemas-microsoft-com:vml"
 xmlns:wp14="http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing"
 xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
 xmlns:w10="urn:schemas-microsoft-com:office:word"
 xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
 xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml"
 xmlns:wpg="http://schemas.microsoft.com/office/word/2010/wordprocessingGroup"
 xmlns:wpi="http://schemas.microsoft.com/office/word/2010/wordprocessingInk"
 xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml"
 xmlns:wps="http://schemas.microsoft.com/office/word/2010/wordprocessingShape"
 mc:Ignorable="w14 wp14"><w:body>{''.join(body)}</w:body></w:document>"""


def write_docx():
    OUT_DIR.mkdir(exist_ok=True)
    content_types = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
  <Override PartName="/word/numbering.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml"/>
</Types>"""
    rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""
    doc_rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering" Target="numbering.xml"/>
</Relationships>"""
    with ZipFile(OUT_PATH, "w", ZIP_DEFLATED) as z:
        z.writestr("[Content_Types].xml", content_types)
        z.writestr("_rels/.rels", rels)
        z.writestr("word/_rels/document.xml.rels", doc_rels)
        z.writestr("word/document.xml", document_xml())
        z.writestr("word/styles.xml", styles_xml())
        z.writestr("word/numbering.xml", numbering_xml())
    return OUT_PATH


if __name__ == "__main__":
    print(write_docx())
