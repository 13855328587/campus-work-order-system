<template>
  <div class="my-orders-page">
    <div class="page-card order-card">
      <div class="page-header">
        <div>
          <div class="page-title">我的工单</div>
          <div class="page-subtitle">查看你提交的维修工单，并按编号、标题、地点、状态和时间筛选。</div>
        </div>
      </div>

      <div class="search-panel">
        <el-input
          v-model="searchForm.orderNo"
          placeholder="工单编号"
          :maxlength="20"
          show-word-limit
          clearable
          class="search-item"
          @input="handleOrderNoInput"
        />

        <el-input
          v-model="searchForm.title"
          placeholder="标题"
          :maxlength="50"
          show-word-limit
          clearable
          class="search-item"
        />

        <el-input
          v-model="searchForm.location"
          placeholder="地点"
          :maxlength="100"
          show-word-limit
          clearable
          class="search-item"
        />

        <el-select
          v-model="searchForm.category"
          placeholder="类别"
          clearable
          class="search-select"
        >
          <el-option label="电器维修" value="ELECTRIC" />
          <el-option label="网络问题" value="NETWORK" />
          <el-option label="家具设施" value="FURNITURE" />
          <el-option label="其他问题" value="OTHER" />
        </el-select>

        <el-select
          v-model="searchForm.priority"
          placeholder="优先级"
          clearable
          class="search-select"
        >
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
        <el-tab-pane label="被拒绝" name="WORKER_REJECTED" />
        <el-tab-pane label="已取消" name="CANCELLED" />
      </el-tabs>

      <div class="table-header">
        <div class="table-title">工单列表</div>
        <div class="table-tools">
          <div class="table-count">共 {{ page.total }} 条</div>
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
        <template v-if="canBatchCancelStatus">
          <span class="batch-tip">已选 {{ selectedRows.length }} 项</span>
          <el-button
            type="danger"
            plain
            size="small"
            :disabled="!selectedRows.length"
            @click="handleBatchCancel"
          >
            批量取消
          </el-button>
        </template>
      </div>

      <el-table
        v-loading="loading"
        :data="orders"
        border
        stripe
        height="100%"
        :class="{ 'hide-selection-column': !canBatchCancelStatus }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          type="selection"
          width="48"
          :selectable="canSelectOrder"
        />

        <el-table-column
          prop="orderNo"
          label="工单编号"
          width="170"
        />
        <el-table-column label="标题" min-width="180">
          <template #default="{ row }">
            <el-tooltip
              :content="row.title || '暂无'"
              placement="top"
              :show-after="300"
              popper-class="full-text-tooltip"
            >
              <span class="table-ellipsis">{{ row.title || '暂无' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="地点" min-width="170">
          <template #default="{ row }">
            <el-tooltip
              :content="row.location || '暂无'"
              placement="top"
              :show-after="300"
              popper-class="full-text-tooltip"
            >
              <span class="table-ellipsis">{{ row.location || '暂无' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="类别" width="110">
          <template #default="{ row }">{{ categoryText(row.category) }}</template>
        </el-table-column>
        <el-table-column label="优先级" width="100">
          <template #default="{ row }">{{ priorityText(row.priority) }}</template>
        </el-table-column>

        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" class="status-tag">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="创建时间" width="240" />

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              plain
              @click="openDetail(row)"
            >
              详情
            </el-button>

            <el-button
              v-if="canCancelOrder(row)"
              size="small"
              type="danger"
              @click="handleCancel(row.id)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <el-dialog
      v-model="detailDialog"
      title="工单详情"
      width="650px"
    >
      <el-descriptions
        v-if="currentOrder"
        :column="2"
        border
        class="work-order-detail"
      >
        <el-descriptions-item label="工单编号">
          {{ currentOrder.orderNo }}
        </el-descriptions-item>

        <el-descriptions-item label="标题">
          <el-tooltip :content="currentOrder.title || '暂无'" placement="top" :show-after="300" popper-class="full-text-tooltip">
            <span class="detail-single-line">{{ currentOrder.title || '暂无' }}</span>
          </el-tooltip>
        </el-descriptions-item>

        <el-descriptions-item label="类别">
          {{ categoryText(currentOrder.category) }}
        </el-descriptions-item>

        <el-descriptions-item label="优先级">
          {{ priorityText(currentOrder.priority) }}
        </el-descriptions-item>

        <el-descriptions-item label="地点">
          <el-tooltip :content="currentOrder.location || '暂无'" placement="top" :show-after="300" popper-class="full-text-tooltip">
            <span class="detail-single-line">{{ currentOrder.location || '暂无' }}</span>
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

        <el-descriptions-item
          :label="currentOrder.status === 'WORKER_REJECTED' ? '拒绝原因' : '驳回原因'"
          :span="2"
        >
          <div class="detail-scroll-text">
            {{ currentOrder.rejectReason || (currentOrder.status === 'WORKER_REJECTED' ? '暂无拒绝原因' : '暂无驳回原因') }}
          </div>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { batchCancelWorkOrder, cancelWorkOrder, getMyOrders } from '../../api/workOrder'
import { categoryText, priorityText, statusText, statusType } from '../../utils/status'

const orders = ref([])
const selectedRows = ref([])
const detailDialog = ref(false)
const currentOrder = ref(null)
const loading = ref(false)

const page = reactive({
  pageNum: 1,
  pageSize: 5,
  total: 0
})

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

const canBatchCancelStatus = computed(() => {
  // 只有待审核、待处理、被拒绝的工单允许学生取消。
  return ['PENDING_REVIEW', 'PENDING_PROCESS', 'WORKER_REJECTED'].includes(searchForm.status)
})

async function loadData() {
  if (loading.value) return
  loading.value = true
  try {
    // 学生端只查询当前登录学生自己的工单，归属校验由后端保证。
    const res = await getMyOrders({
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      ...searchForm
    })

    orders.value = res.list
    selectedRows.value = []
    page.total = res.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  if (!validateTimeRange()) {
    return
  }

  page.pageNum = 1
  loadData()
}

function handleStatusChange() {
  if (!validateTimeRange()) {
    return
  }

  page.pageNum = 1
  loadData()
}

function validateTimeRange() {
  if (
    searchForm.startTime &&
    searchForm.endTime &&
    new Date(searchForm.startTime).getTime() > new Date(searchForm.endTime).getTime()
  ) {
    ElMessage.warning('开始时间不能晚于结束时间')
    return false
  }

  return true
}

function handleOrderNoInput(value) {
  searchForm.orderNo = value.replace(/[^a-zA-Z0-9]/g, '').toUpperCase()
}

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

function openDetail(row) {
  currentOrder.value = row
  detailDialog.value = true
}

async function handleCancel(id) {
  await ElMessageBox.confirm('确定取消该工单吗？', '提示')
  await cancelWorkOrder(id)
  ElMessage.success('取消成功')
  await loadData()
}

function canSelectOrder(row) {
  // 批量取消只能选择仍未进入处理中的工单。
  return canCancelOrder(row)
}

function canCancelOrder(row) {
  return ['PENDING_REVIEW', 'PENDING_PROCESS', 'WORKER_REJECTED'].includes(row.status)
}

function handleSelectionChange(rows) {
  selectedRows.value = rows.filter(canSelectOrder)
}

async function handleBatchCancel() {
  // 批量取消走后端批量接口，后端会再次校验工单归属和状态。
  const rows = selectedRows.value.filter(canSelectOrder)
  if (!rows.length) {
    ElMessage.warning('请选择可取消的工单')
    return
  }

  await ElMessageBox.confirm(
    `确定取消选中的 ${rows.length} 个工单吗？`,
    '批量取消',
    {
      confirmButtonText: '确定取消',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )

  await batchCancelWorkOrder(rows.map(row => row.id))
  ElMessage.success(`已取消 ${rows.length} 个工单`)
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.my-orders-page {
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.order-card {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 18px;
}

.page-title {
  font-size: 22px;
  font-weight: 800;
  color: #303133;
}

.page-subtitle {
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
}

.search-panel {
  display: flex;
  flex-wrap: nowrap;
  gap: 8px;
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
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
  color: #606266;
  line-height: 32px;
}

.search-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.status-tabs {
  margin-bottom: 10px;
}

.status-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.table-title {
  font-size: 16px;
  font-weight: 800;
  color: #303133;
}

.table-count {
  color: #909399;
  font-size: 13px;
}

.table-tools {
  display: flex;
  align-items: center;
  gap: 12px;
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

.table-ellipsis,
.detail-single-line {
  display: inline-block;
  width: 100%;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

:global(.full-text-tooltip) {
  max-width: 520px !important;
  white-space: normal !important;
  word-break: break-word !important;
  line-height: 1.6;
}

.detail-text {
  line-height: 1.7;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-word;
}

.detail-images {
  display: flex;
  flex-wrap: nowrap;
  gap: 10px;
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 6px;
}

.detail-image {
  width: 96px;
  height: 96px;
  border-radius: 6px;
  cursor: zoom-in;
  flex: 0 0 96px;
}

.pagination {
  margin-top: 10px;
  flex: 0 0 auto;
  display: flex;
  justify-content: flex-end;
}
</style>
