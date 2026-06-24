$ErrorActionPreference = 'Stop'

$root = 'C:\Users\pc\Downloads\campus-work-order-frontend'
$outDir = Join-Path $root 'docs'
$outPath = Join-Path $outDir '校园工单管理系统前端迭代总结.docx'
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$wdCollapseEnd = 0
$wdPageBreak = 7
$wdAlignLeft = 0
$wdAlignCenter = 1
$wdAlignRight = 2
$wdLineSpaceMultiple = 5
$wdFormatDocumentDefault = 16
$wdAutoFitFixed = 0
$wdPreferredWidthPoints = 3

function Rgb([int]$r, [int]$g, [int]$b) { return $r + 256 * $g + 65536 * $b }
$blue = Rgb 46 116 181
$darkBlue = Rgb 31 77 120
$ink = Rgb 24 41 64
$muted = Rgb 100 116 139
$lightFill = Rgb 232 238 245
$softFill = Rgb 244 246 249
$white = Rgb 255 255 255

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0

try {
  $doc = $word.Documents.Add()
  $section = $doc.Sections.Item(1)
  $section.PageSetup.PageWidth = 612
  $section.PageSetup.PageHeight = 792
  $section.PageSetup.TopMargin = 72
  $section.PageSetup.BottomMargin = 72
  $section.PageSetup.LeftMargin = 72
  $section.PageSetup.RightMargin = 72
  $section.PageSetup.HeaderDistance = 35.4
  $section.PageSetup.FooterDistance = 35.4

  # -1 为 Word 内置“正文/Normal”样式，避免中英文 Office 名称差异。
  $normal = $doc.Styles.Item(-1)
  $normal.Font.Name = 'Calibri'
  $normal.Font.NameFarEast = '微软雅黑'
  $normal.Font.Size = 11
  $normal.Font.Color = $ink
  $normal.ParagraphFormat.SpaceAfter = 6
  $normal.ParagraphFormat.LineSpacingRule = $wdLineSpaceMultiple
  $normal.ParagraphFormat.LineSpacing = 13.75

  $header = $section.Headers.Item(1).Range
  $header.Text = '校园工单管理系统  |  前端迭代记录'
  $header.Font.Name = 'Calibri'
  $header.Font.NameFarEast = '微软雅黑'
  $header.Font.Size = 9
  $header.Font.Color = $muted
  $header.ParagraphFormat.Alignment = $wdAlignRight

  $footer = $section.Footers.Item(1)
  $footer.Range.Font.NameFarEast = '微软雅黑'
  $footer.Range.Font.Size = 9
  $footer.Range.Font.Color = $muted
  $footer.PageNumbers.Add(2) | Out-Null

  function Add-Paragraph {
    param(
      [string]$Text,
      [double]$Size = 11,
      [bool]$Bold = $false,
      [int]$Color = $ink,
      [int]$Alignment = $wdAlignLeft,
      [double]$Before = 0,
      [double]$After = 6,
      [bool]$Italic = $false
    )
    $range = $doc.Content
    $range.Collapse($wdCollapseEnd)
    $range.InsertAfter($Text)
    $range.InsertParagraphAfter()
    $p = $doc.Paragraphs.Item($doc.Paragraphs.Count - 1)
    $p.Range.Font.Name = 'Calibri'
    $p.Range.Font.NameFarEast = '微软雅黑'
    $p.Range.Font.Size = $Size
    $p.Range.Font.Bold = [int]$Bold
    $p.Range.Font.Italic = [int]$Italic
    $p.Range.Font.Color = $Color
    $p.ParagraphFormat.Alignment = $Alignment
    $p.ParagraphFormat.SpaceBefore = $Before
    $p.ParagraphFormat.SpaceAfter = $After
    $p.ParagraphFormat.LineSpacingRule = $wdLineSpaceMultiple
    $p.ParagraphFormat.LineSpacing = [Math]::Max(13.75, $Size * 1.25)
    return $p
  }

  function Add-Heading1([string]$Text) {
    $p = Add-Paragraph -Text $Text -Size 16 -Bold $true -Color $blue -Before 18 -After 10
    $p.KeepWithNext = $true
  }

  function Add-Heading2([string]$Text) {
    $p = Add-Paragraph -Text $Text -Size 13 -Bold $true -Color $blue -Before 14 -After 7
    $p.KeepWithNext = $true
  }

  function Add-Bullet([string]$Text) {
    $p = Add-Paragraph -Text $Text -Size 11 -After 4
    $p.Range.ListFormat.ApplyBulletDefault()
    $p.ParagraphFormat.LeftIndent = 27
    $p.ParagraphFormat.FirstLineIndent = -13.5
    return $p
  }

  function Add-Callout([string]$Label, [string]$Text) {
    $table = Add-Table -Headers @('重点结论') -Rows @(@("$Label：$Text")) -Widths @(468) -HeaderFill $blue -BodyFill $softFill
    $table.Range.ParagraphFormat.SpaceAfter = 4
  }

  function Add-Table {
    param(
      [string[]]$Headers,
      [object[]]$Rows,
      [double[]]$Widths,
      [int]$HeaderFill = $lightFill,
      [int]$BodyFill = $white
    )
    $range = $doc.Content
    $range.Collapse($wdCollapseEnd)
    $table = $doc.Tables.Add($range, $Rows.Count + 1, $Headers.Count)
    $table.AllowAutoFit = $false
    $table.AutoFitBehavior($wdAutoFitFixed)
    $table.PreferredWidthType = $wdPreferredWidthPoints
    $table.PreferredWidth = 468
    $table.LeftPadding = 6
    $table.RightPadding = 6
    $table.TopPadding = 4
    $table.BottomPadding = 4
    $table.Borders.Enable = 1

    for ($c = 1; $c -le $Headers.Count; $c++) {
      $table.Columns.Item($c).Width = $Widths[$c - 1]
      $cell = $table.Cell(1, $c)
      $cell.Range.Text = $Headers[$c - 1]
      $cell.Range.Font.Bold = 1
      $cell.Range.Font.Color = if ($HeaderFill -eq $blue) { $white } else { $ink }
      $cell.Shading.BackgroundPatternColor = $HeaderFill
      $cell.VerticalAlignment = 1
    }

    for ($r = 0; $r -lt $Rows.Count; $r++) {
      for ($c = 0; $c -lt $Headers.Count; $c++) {
        $cell = $table.Cell($r + 2, $c + 1)
        $cell.Range.Text = [string]$Rows[$r][$c]
        $cell.Range.Font.Color = $ink
        $cell.Shading.BackgroundPatternColor = $BodyFill
        $cell.VerticalAlignment = 1
      }
    }

    $table.Range.Font.Name = 'Calibri'
    $table.Range.Font.NameFarEast = '微软雅黑'
    $table.Range.Font.Size = 9.5
    $table.Range.ParagraphFormat.SpaceAfter = 0
    $table.Range.ParagraphFormat.LineSpacingRule = $wdLineSpaceMultiple
    $table.Range.ParagraphFormat.LineSpacing = 12
    $table.Rows.Item(1).HeadingFormat = -1
    $table.Range.InsertParagraphAfter()
    $after = $doc.Paragraphs.Item($doc.Paragraphs.Count)
    $after.ParagraphFormat.SpaceAfter = 8
    return $table
  }

  function Add-PageBreak {
    $range = $doc.Content
    $range.Collapse($wdCollapseEnd)
    $range.InsertBreak($wdPageBreak)
  }

  # 封面：editorial_cover
  Add-Paragraph -Text 'PROJECT ITERATION RECORD' -Size 10.5 -Bold $true -Color $blue -Alignment $wdAlignCenter -Before 116 -After 20 | Out-Null
  Add-Paragraph -Text '校园工单管理系统' -Size 30 -Bold $true -Color $ink -Alignment $wdAlignCenter -After 8 | Out-Null
  Add-Paragraph -Text '前端迭代总结' -Size 20 -Bold $true -Color $darkBlue -Alignment $wdAlignCenter -After 18 | Out-Null
  Add-Paragraph -Text '需求 · 页面改造 · 接口映射 · 故障排查' -Size 12 -Color $muted -Alignment $wdAlignCenter -After 92 | Out-Null
  Add-Paragraph -Text '整理日期：2026 年 6 月 24 日' -Size 10.5 -Bold $true -Color $ink -Alignment $wdAlignCenter -After 5 | Out-Null
  Add-Paragraph -Text '项目：campus-work-order-frontend' -Size 10 -Color $muted -Alignment $wdAlignCenter -After 0 | Out-Null

  Add-PageBreak

  Add-Heading1 '1. 文档概览'
  Add-Paragraph '本文档汇总本轮对校园工单管理系统前端提出的需求、已完成改动、接口调用关系、异常原因及处理方案。覆盖管理员工单管理、用户管理、学生工单、维修任务、个人信息、登录注册等页面。' | Out-Null
  Add-Callout '当前结果' '核心列表已统一采用状态标签、时间查询和分页交互；登录注册与个人信息页面完成视觉及校验优化。'

  Add-Heading2 '1.1 本轮目标'
  @(
    '补齐后端新增 createdAt 字段的前端展示与时间范围检索。',
    '将状态下拉查询改为更直观的状态标签页。',
    '为管理员、学生、维修人员的主要列表加入分页。',
    '修复详情、接口参数、注册校验等运行时问题。',
    '统一查询栏布局、输入限制、响应式展示和视觉风格。'
  ) | ForEach-Object { Add-Bullet $_ | Out-Null }

  Add-Heading2 '1.2 状态分类'
  Add-Table -Headers @('使用角色', '状态板块') -Rows @(
    @('管理员 / 学生', '待审核、待处理、处理中、已完成、已驳回、已取消'),
    @('维修人员', '待处理、处理中、已完成')
  ) -Widths @(126, 342) | Out-Null

  Add-Heading1 '2. 管理员端改动'
  Add-Heading2 '2.1 工单管理 OrderManage.vue'
  @(
    '列表增加“创建时间”列，展示后端 createdAt。',
    '增加开始时间与结束时间检索，使用 YYYY-MM-DDTHH:mm:ss 格式提交 startTime、endTime。',
    '状态查询由下拉框改为六个标签页，默认“待审核”，切换后重置到第一页。',
    '查询栏压缩为单行；窄屏时横向滚动，查询和重置按钮不换行。',
    '操作列宽由 320px 缩小为 220px。',
    '补齐 openDetail 方法、详情接口调用和详情弹窗，解决详情按钮运行时报错。',
    '详情弹窗展示状态、创建时间、维修人员、问题描述、处理结果和驳回原因。'
  ) | ForEach-Object { Add-Bullet $_ | Out-Null }

  Add-Heading2 '2.2 用户管理 UserManage.vue'
  @(
    '数据源由一次性请求 /api/users 改为后端分页 /api/users/page。',
    '增加 pageNum、pageSize、total 和分页控件，筛选条件交由后端处理。',
    '增加 createdAt 创建时间列及 startTime、endTime 时间范围查询。',
    '移除 ID 查询框，但保留用户列表中的 ID，便于管理员排查。',
    '查询栏保持单行，状态列移动到列表最后。'
  ) | ForEach-Object { Add-Bullet $_ | Out-Null }

  Add-PageBreak
  Add-Heading1 '3. 学生端改动'
  Add-Heading2 '3.1 我的工单 MyOrders.vue'
  @(
    '调用 /api/work-orders/my，并携带 pageNum、pageSize 及完整筛选参数。',
    '状态下拉改为六个状态标签页；默认显示待审核。',
    '表格使用后端返回的 list、total，支持每页 5、10、20、50 条。',
    '查询栏保持单行，增加创建时间范围检索。',
    '工单编号最多 20 字符，只允许英文字母和数字，并自动转为大写。',
    '标题最多 50 字符、地点最多 100 字符，并显示字数限制。',
    '开始时间晚于结束时间时阻止查询并给出提示。'
  ) | ForEach-Object { Add-Bullet $_ | Out-Null }

  Add-Heading2 '3.2 新建工单 CreateOrder.vue'
  Add-Table -Headers @('字段', '限制', '附加处理') -Rows @(
    @('工单标题', '2-50 字符', '显示字数；禁止纯空格'),
    @('问题描述', '5-500 字符', '显示字数；禁止纯空格'),
    @('地点', '2-100 字符', '显示字数；禁止纯空格')
  ) -Widths @(105, 126, 237) | Out-Null
  Add-Paragraph '提交前会去除标题、问题描述和地点两端的空格。后端仍应设置相同或更严格的长度校验，前端限制不能替代服务端校验。' -Color $muted -Italic $true | Out-Null

  Add-Heading1 '4. 维修人员端改动'
  Add-Heading2 '4.1 我的任务 MyTasks.vue'
  @(
    '取消“当前任务 / 历史任务”双页签，统一为待处理、处理中、已完成三个状态标签。',
    '待处理与处理中使用 /worker/tasks；已完成使用 /worker/history。',
    '接口提交分页、状态、文本查询和时间范围参数，并兼容数组或分页对象返回。',
    '列表增加 createdAt 创建时间；已完成列表保留 updatedAt 完成时间。',
    '处理结果仅在详情弹窗中展示，不占用列表列宽。',
    '接单成功后刷新当前状态；完成工单后自动切换至“已完成”。'
  ) | ForEach-Object { Add-Bullet $_ | Out-Null }

  Add-Heading1 '5. 个人信息与认证页面'
  Add-Heading2 '5.1 个人信息 Profile.vue'
  @(
    '移除普通用户无实际用途的数据库用户 ID。管理员用户管理页仍保留 ID。',
    '基本信息改为双列展示并增加移动端响应式布局。',
    '头像上传限制为 JPG、PNG、WebP，文件不超过 2MB。',
    '增加头像悬停提示、上传状态、错误处理和格式说明。'
  ) | ForEach-Object { Add-Bullet $_ | Out-Null }

  Add-Heading2 '5.2 登录与注册'
  @(
    '新建 AuthShell.vue 作为共享认证布局，登录和注册使用统一双栏视觉。',
    '加入校园智慧运维背景图、玻璃效果、渐变按钮、在线状态动效和移动端布局。',
    '登录支持记住用户名、回车提交、表单校验和防重复提交。',
    '注册增加确认密码、手机号格式校验、提交状态和完整错误处理。',
    '因原新增用户名规则过严导致无法注册，已放宽为非空且最多 20 字符，并捕获表单校验异常。'
  ) | ForEach-Object { Add-Bullet $_ | Out-Null }

  Add-PageBreak
  Add-Heading1 '6. 接口映射'
  Add-Table -Headers @('功能', '方法与接口', '关键参数 / 说明') -Rows @(
    @('管理员工单分页', 'GET /api/work-orders/page', 'pageNum、pageSize、status、startTime、endTime 等'),
    @('个人工单分页', 'GET /api/work-orders/my', '必须携带 pageNum、pageSize；返回 list、total'),
    @('工单详情', 'GET /api/work-orders/{id}', '后端 @PathVariable 必须显式写 "id"'),
    @('工单统计', 'GET /api/work-orders/statistics', '仪表盘状态数量'),
    @('全部工单', 'GET /api/work-orders', '管理员仪表盘提醒列表的旧数据源'),
    @('审核通过', 'POST /api/work-orders/{id}/approve', '管理员操作'),
    @('驳回工单', 'POST /api/work-orders/{id}/reject', '请求体包含 rejectReason'),
    @('分配人员', 'POST /api/work-orders/{id}/assign', '请求体包含 handlerId'),
    @('维修任务', 'GET /api/work-orders/worker/tasks', '待处理、处理中；支持分页参数'),
    @('维修历史', 'GET /api/work-orders/worker/history', '已完成；支持分页参数'),
    @('接单', 'POST /api/work-orders/{id}/accept', '维修人员操作'),
    @('完成工单', 'POST /api/work-orders/{id}/finish', '请求体包含 finishResult'),
    @('用户分页', 'GET /api/users/page', 'pageNum、pageSize、角色、状态、时间等'),
    @('维修人员列表', 'GET /api/users/workers', '管理员分配工单使用'),
    @('登录 / 注册', 'POST /api/auth/login；POST /api/auth/register', '认证表单')
  ) -Widths @(104, 206, 158) | Out-Null

  Add-Heading1 '7. 异常与解决办法'
  Add-Table -Headers @('异常', '原因', '解决办法') -Rows @(
    @('AggregateError: ECONNREFUSED', 'Vite 代理无法连接 localhost:8080', '启动后端或修改 vite.config.js 中的 target，并重启前端'),
    @('openDetail is not a function', '模板引用了未定义的方法', '补齐 getWorkOrderDetail 调用、openDetail 方法和详情弹窗'),
    @('Long 参数名不可反射', 'Spring 未保留参数名且注解未显式命名', '使用 @PathVariable("id") Long id，或为 @RequestParam 显式命名'),
    @('Missing pageNum', '后端已将接口改为强制分页', '前端请求携带 pageNum、pageSize，并处理 list、total'),
    @('注册 Uncaught in promise', '前端用户名规则过严且 validate 拒绝未捕获', '放宽用户名规则，validate().catch(() => false) 后再提交')
  ) -Widths @(135, 151, 182) | Out-Null

  Add-Heading1 '8. 当前注意事项与建议'
  @(
    '后端分页接口应为 pageSize 设置最大值（建议 50 或 100），防止绕过前端直接请求超大数据量。',
    'Dashboard.vue 为兼容分页接口，当前使用较大的 pageSize 汇总学生和维修人员数据。长期建议新增角色范围统计接口，避免拉取大量列表后在前端统计。',
    '后端字段长度、手机号格式、权限和状态流转必须再次校验；前端校验只负责用户体验。',
    '时间参数统一采用本地时间字符串 YYYY-MM-DDTHH:mm:ss，后端需明确时区为 Asia/Shanghai 或统一转换为 UTC。',
    '当前工作环境未配置 Node.js/npm，因此本轮无法执行 npm run build。交付前应在开发机执行构建和关键页面回归。'
  ) | ForEach-Object { Add-Bullet $_ | Out-Null }

  Add-Heading2 '8.1 建议回归清单'
  @(
    '登录、注册、退出和记住用户名。',
    '三类角色访问控制及导航菜单。',
    '各列表的状态切换、分页、时间范围和重置。',
    '审核、驳回、分配、取消、接单、完成等状态流转。',
    '详情弹窗字段完整性及空值显示。',
    '头像格式、大小限制与上传失败处理。'
  ) | ForEach-Object { Add-Bullet $_ | Out-Null }

  Add-Paragraph -Text '— 文档结束 —' -Size 10 -Color $muted -Alignment $wdAlignCenter -Before 24 -After 0 | Out-Null

  $doc.BuiltInDocumentProperties.Item('Title').Value = '校园工单管理系统前端迭代总结'
  $doc.BuiltInDocumentProperties.Item('Subject').Value = '需求、实现、接口与问题排查记录'
  $doc.BuiltInDocumentProperties.Item('Author').Value = '项目组'

  $doc.SaveAs2($outPath, $wdFormatDocumentDefault)
  $doc.Close()

  # 结构检查：重新打开，确认正文、表格和页数可读取。
  $check = $word.Documents.Open($outPath, $false, $true)
  $pageCount = $check.ComputeStatistics(2)
  $paragraphCount = $check.Paragraphs.Count
  $tableCount = $check.Tables.Count
  $check.Close()

  [PSCustomObject]@{
    Output = $outPath
    Pages = $pageCount
    Paragraphs = $paragraphCount
    Tables = $tableCount
  }
}
finally {
  if ($doc -and -not $doc.Saved) { $doc.Close($false) }
  $word.Quit()
}
