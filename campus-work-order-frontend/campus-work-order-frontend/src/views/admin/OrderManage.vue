<template>
  <div class="order-manage-page">
    <div class="page-card order-card">

      <!-- 标题 -->
      <div class="page-header">
        <div>
          <div class="page-title">工单管理</div>
          <div class="page-subtitle">
            管理学生提交的工单，可进行审核、驳回、分配维修人员和查看详情。
          </div>
        </div>
      </div>

      <!-- 搜索 -->
      <div class="search-panel">

        <el-input v-model="searchForm.orderNo" placeholder="工单编号" clearable class="search-item" />
        <el-input v-model="searchForm.title" placeholder="标题" clearable class="search-item" />
        <el-input v-model="searchForm.location" placeholder="地点" clearable class="search-item" />

        <el-select v-model="searchForm.category" placeholder="类别" clearable class="search-select">
          <el-option label="电器维修" value="ELECTRIC" />
          <el-option label="网络问题" value="NETWORK" />
          <el-option label="家具设施" value="FURNITURE" />
          <el-option label="其他问题" value="OTHER" />
        </el-select>

        <el-select v-model="searchForm.priority" placeholder="优先级" clearable class="search-select">
          <el-option label="低" value="LOW" />
          <el-option label="中" value="MEDIUM" />
          <el-option label="高" value="HIGH" />
          <el-option label="紧急" value="URGENT" />
        </el-select>

        <el-date-picker
          v-model="searchForm.startTime"
          type="datetime"
          placeholder="开始时间"
          value-format="YYYY-MM-DDTHH:mm:ss"
          class="time-picker"
        />

        <span class="time-separator">至</span>

        <el-date-picker
          v-model="searchForm.endTime"
          type="datetime"
          placeholder="结束时间"
          value-format="YYYY-MM-DDTHH:mm:ss"
          class="time-picker"
        />

        <div class="search-actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>

      </div>

      <el-tabs
        v-model="searchForm.status"
        class="status-tabs"
        @tab-change="handleStatusChange"
      >
        <el-tab-pane label="待审核" name="PENDING_REVIEW" />
        <el-tab-pane label="待处理" name="PENDING_PROCESS" />
        <el-tab-pane label="处理中" name="PROCESSING" />
        <el-tab-pane label="已完成" name="COMPLETED" />
        <el-tab-pane label="已驳回" name="REJECTED" />
        <el-tab-pane label="已取消" name="CANCELLED" />
      </el-tabs>

      <div class="table-header">
        <div class="table-title">工单列表</div>
        <div class="table-tools">
          <span class="table-count">共 {{ page.total }} 条</span>
          <el-tooltip content="刷新" placement="top">
            <el-icon
              class="table-refresh"
              :class="{ 'is-loading': loading }"
              role="button"
              tabindex="0"
              aria-label="刷新工单列表"
              @click="loadData"
              @keydown.enter="loadData"
            ><Refresh /></el-icon>
          </el-tooltip>
        </div>
      </div>

      <div class="batch-toolbar">
        <template v-if="searchForm.status === 'PENDING_REVIEW'">
          <span class="batch-tip">已选 {{ selectedRows.length }} 项</span>
          <el-button
            type="success"
            plain
            size="small"
            :disabled="!selectedRows.length"
            @click="batchApprove"
          >
            批量通过
          </el-button>
        </template>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="orders"
        border
        stripe
        height="100%"
        :class="{ 'hide-selection-column': searchForm.status !== 'PENDING_REVIEW' }"
        @selection-change="handleSelectionChange"
      >

        <el-table-column
          type="selection"
          width="48"
          :selectable="canSelectOrder"
        />

        <el-table-column prop="orderNo" label="工单编号" width="170" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="location" label="地点" min-width="170" show-overflow-tooltip />
        <el-table-column label="类别" width="110">
          <template #default="{ row }">{{ categoryText(row.category) }}</template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100" />

        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="维修人员" width="100">
          <template #default="{ row }">
            {{ getWorkerName(row.handlerId) }}
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="创建时间" width="200" />

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">

            <el-button size="small" type="primary" plain @click="openDetail(row)">
              详情
            </el-button>

            <el-button
              v-if="row.status === 'PENDING_REVIEW'"
              size="small"
              type="success"
              @click="approve(row.id)"
            >
              通过
            </el-button>

            <el-button
              v-if="row.status === 'PENDING_REVIEW'"
              size="small"
              type="danger"
              @click="reject(row.id)"
            >
              驳回
            </el-button>

            <el-button
              v-if="row.status === 'PENDING_PROCESS'"
              size="small"
              type="primary"
              @click="openAssign(row)"
            >
              分配
            </el-button>

          </template>
        </el-table-column>

      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="page.pageNum"
          v-model:page-size="page.pageSize"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="page.total"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>

    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialog" title="工单详情" width="650px">
      <el-descriptions v-if="currentOrder" :column="2" border class="work-order-detail">
        <el-descriptions-item label="工单编号">
          {{ currentOrder.orderNo }}
        </el-descriptions-item>

        <el-descriptions-item label="标题">
          <el-tooltip :content="currentOrder.title" placement="top" :show-after="300" popper-class="detail-value-tooltip">
            <span class="detail-single-line">{{ currentOrder.title }}</span>
          </el-tooltip>
        </el-descriptions-item>

        <el-descriptions-item label="类别">
          {{ categoryText(currentOrder.category) }}
        </el-descriptions-item>

        <el-descriptions-item label="优先级">
          {{ currentOrder.priority }}
        </el-descriptions-item>

        <el-descriptions-item label="地点">
          <el-tooltip :content="currentOrder.location" placement="top" :show-after="300" popper-class="detail-value-tooltip">
            <span class="detail-single-line">{{ currentOrder.location }}</span>
          </el-tooltip>
        </el-descriptions-item>

        <el-descriptions-item label="创建时间">
          {{ currentOrder.createdAt || '暂无' }}
        </el-descriptions-item>

        <el-descriptions-item label="现场图片" :span="2">
          <div v-if="currentOrder.imageUrls?.length" class="detail-images">
            <el-image
              v-for="(url, index) in currentOrder.imageUrls"
              :key="url"
              :src="url"
              :preview-src-list="currentOrder.imageUrls"
              :initial-index="index"
              fit="cover"
              class="detail-image"
              preview-teleported
            />
          </div>
          <span v-else>暂无图片</span>
        </el-descriptions-item>

        <el-descriptions-item label="问题描述" :span="2">
          <div class="detail-scroll-text">
            {{ currentOrder.description || '暂无描述' }}
          </div>
        </el-descriptions-item>

        <el-descriptions-item label="处理结果" :span="2">
          <div class="detail-scroll-text">
            {{ currentOrder.finishResult || '暂无处理结果' }}
          </div>
        </el-descriptions-item>

        <el-descriptions-item label="驳回原因" :span="2">
          <div class="detail-scroll-text">
            {{ currentOrder.rejectReason || '暂无驳回原因' }}
          </div>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 分配弹窗 -->
    <el-dialog v-model="assignDialog" title="分配维修人员" width="420px">

      <el-form label-width="110px">

        <el-form-item label="工单编号">
          <el-input :model-value="currentOrder?.orderNo" disabled />
        </el-form-item>

        <el-form-item label="维修人员">
          <el-select
            v-model="assignForm.handlerId"
            placeholder="请选择维修人员"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="w in workers"
              :key="w.id"
              :label="w.id + ' - ' + w.realName"
              :value="w.id"
            />
          </el-select>
        </el-form-item>

      </el-form>

      <template #footer>
        <el-button @click="assignDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAssign">确定</el-button>
      </template>

    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

import {
  getOrderPage,
  getWorkOrderDetail,
  approveWorkOrder,
  batchApproveWorkOrder,
  rejectWorkOrder,
  assignWorkOrder
} from '../../api/workOrder'

import { getWorkers } from '../../api/user'
import { categoryText, statusText, statusType } from '../../utils/status'

/* 数据 */
const orders = ref([])
const workers = ref([])
const selectedRows = ref([])
const loading = ref(false)

/* 分页 */
const page = reactive({
  pageNum: 1,
  pageSize: 5,
  total: 0
})

/* 弹窗 */
const detailDialog = ref(false)
const assignDialog = ref(false)
const currentOrder = ref(null)

const assignForm = reactive({
  handlerId: ''
})

/* 搜索 */
const searchForm = reactive({
  orderNo: '',
  title: '',
  location: '',
  category: '',
  priority: '',
  status: 'PENDING_REVIEW',
  startTime: '',
  endTime: ''
})

/* 加载数据（核心） */
async function loadData() {
  if (loading.value) return
  loading.value = true
  try {
    // 管理员端按当前标签状态分页查询工单，后端负责时间范围和条件过滤。
    const res = await getOrderPage({
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      ...searchForm
    })

    orders.value = [...res.list].sort((a, b) => {
      const timeDiff = new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      return timeDiff || Number(b.id) - Number(a.id)
    })
    selectedRows.value = []
    page.total = res.total
  } finally {
    loading.value = false
  }
}

/* 查询 */
function handleSearch() {
  page.pageNum = 1
  loadData()
}

function handleStatusChange() {
  page.pageNum = 1
  loadData()
}

/* 重置 */
function resetSearch() {
  Object.assign(searchForm, {
    orderNo: '',
    title: '',
    location: '',
    category: '',
    priority: '',
    status: 'PENDING_REVIEW',
    startTime: '',
    endTime: ''
  })

  handleSearch()
}

/* 维修人员 */
function getWorkerName(id) {
  const w = workers.value.find(i => i.id === id)
  return w ? `${w.id} - ${w.realName}` : '未分配'
}

/* 查看详情 */
async function openDetail(row) {
  currentOrder.value = await getWorkOrderDetail(row.id)
  detailDialog.value = true
}

/* 加载维修人员 */
async function loadWorkers() {
  workers.value = await getWorkers()
}

/* 分配 */
function openAssign(row) {
  currentOrder.value = row
  assignForm.handlerId = row.handlerId || ''
  assignDialog.value = true
}

async function submitAssign() {
  await assignWorkOrder(currentOrder.value.id, {
    handlerId: assignForm.handlerId
  })

  ElMessage.success('分配成功')
  assignDialog.value = false
  loadData()
}

/* 审核 */
async function approve(id) {
  await approveWorkOrder(id)
  ElMessage.success('审核通过')
  loadData()
}

function canSelectOrder(row) {
  // 批量通过只允许选择待审核工单，其它状态不能跨状态操作。
  return row.status === 'PENDING_REVIEW'
}

function handleSelectionChange(rows) {
  selectedRows.value = rows.filter(canSelectOrder)
}

async function batchApprove() {
  // 批量通过走后端事务接口，保证状态校验和操作日志一致。
  const rows = selectedRows.value.filter(canSelectOrder)
  if (!rows.length) {
    ElMessage.warning('请选择待审核工单')
    return
  }

  await ElMessageBox.confirm(
    `确定通过选中的 ${rows.length} 个待审核工单吗？`,
    '批量通过',
    {
      confirmButtonText: '确定通过',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )

  await batchApproveWorkOrder(rows.map(row => row.id))
  ElMessage.success(`已通过 ${rows.length} 个工单`)
  loadData()
}

async function reject(id) {
  const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回')
  await rejectWorkOrder(id, { rejectReason: value })
  ElMessage.success('已驳回')
  loadData()
}

/* 初始化 */
onMounted(() => {
  loadData()
  loadWorkers()
})
</script>

<style scoped>
.order-manage-page {
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.order-card {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.search-panel {
  display: flex;
  gap: 8px;
  flex-wrap: nowrap;
  margin: 10px 0;
  overflow: hidden;
}

.search-item {
  width: 155px;
  flex: 1 1 130px;
}

.search-select {
  width: 110px;
  flex: 0 1 110px;
}

.time-picker {
  width: 165px;
  flex: 0 1 165px;
}

.time-separator {
  align-self: center;
  color: #909399;
}

.search-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.pagination {
  margin-top: 10px;
  flex: 0 0 auto;
  display: flex;
  justify-content: flex-end;
}

.status-tabs {
  margin-bottom: 10px;
}

.status-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.table-header,
.table-tools {
  display: flex;
  align-items: center;
}

.table-header {
  justify-content: space-between;
  margin-bottom: 10px;
}

.table-title {
  font-size: 16px;
  font-weight: 800;
}

.table-tools {
  gap: 12px;
}

.table-count {
  color: #909399;
  font-size: 13px;
}

.batch-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 34px;
  padding: 6px 10px;
  margin-bottom: 8px;
  border: 1px solid #eef2f7;
  border-radius: 10px;
  background: #fbfdff;
}

.batch-tip {
  margin-right: 4px;
  color: #64748b;
  font-size: 13px;
}

.hide-selection-column :deep(.el-table-column--selection .cell) {
  visibility: hidden;
}

.detail-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.detail-image {
  width: 96px;
  height: 96px;
  border-radius: 6px;
  cursor: zoom-in;
}
</style>
