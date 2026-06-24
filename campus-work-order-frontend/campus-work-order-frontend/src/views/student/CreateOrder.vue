<template>
  <div class="create-order-page">
    <div class="page-card order-card">
      <div class="page-header">
        <div>
          <div class="page-title">新建工单</div>
          <div class="page-subtitle">
            请填写维修问题的基本信息，描述越清楚，管理员和维修人员处理越高效。
          </div>
        </div>
      </div>

      <el-row :gutter="22">
        <el-col :span="15">
          <div class="form-panel">
            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-width="90px"
            >
              <el-form-item label="工单标题" prop="title">
                <el-input
                  v-model="form.title"
                  placeholder="例如：宿舍空调无法启动"
                  :maxlength="50"
                  show-word-limit
                  clearable
                />
              </el-form-item>

              <el-form-item label="问题描述" prop="description">
                <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="5"
                  :maxlength="500"
                  show-word-limit
                  placeholder="请详细描述问题，例如：什么时候出现、具体表现、是否影响正常使用等"
                />
              </el-form-item>

              <el-form-item label="地点" prop="location">
                <el-input
                  v-model="form.location"
                  placeholder="例如：3号宿舍楼502"
                  :maxlength="100"
                  show-word-limit
                  clearable
                />
              </el-form-item>

              <el-form-item label="类型" prop="category">
                <el-select
                  v-model="form.category"
                  placeholder="请选择类型"
                  clearable
                  style="width: 100%"
                >
                  <el-option label="电器维修" value="ELECTRIC" />
                  <el-option label="网络问题" value="NETWORK" />
                  <el-option label="家具设施" value="FURNITURE" />
                  <el-option label="其他问题" value="OTHER" />
                </el-select>
              </el-form-item>

              <el-form-item label="优先级" prop="priority">
                <el-select
                  v-model="form.priority"
                  placeholder="请选择优先级"
                  style="width: 100%"
                >
                  <el-option label="低" value="LOW" />
                  <el-option label="中" value="MEDIUM" />
                  <el-option label="高" value="HIGH" />
                  <el-option label="紧急" value="URGENT" />
                </el-select>
              </el-form-item>

              <el-form-item label="现场图片">
                <el-upload
                  ref="uploadRef"
                  v-model:file-list="uploadFileList"
                  action="#"
                  list-type="picture-card"
                  accept="image/jpeg,image/png,image/gif,image/webp"
                  :http-request="handleImageUpload"
                  :before-upload="beforeImageUpload"
                  :on-remove="handleImageRemove"
                  :on-exceed="handleExceed"
                  :limit="5"
                >
                  <span class="upload-plus">+</span>
                  <template #tip>
                    <div class="upload-tip">最多 5 张，支持 JPG、PNG、GIF、WEBP，单张不超过 5MB</div>
                  </template>
                </el-upload>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" :loading="submitting" :disabled="submitting" @click="submit">
                  提交工单
                </el-button>
                <el-button @click="reset">
                  重置
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-col>

        <el-col :span="9">
          <div class="side-panel">
            <div class="side-card highlight-card">
              <div class="side-icon">📝</div>
              <div>
                <div class="side-title">填写建议</div>
                <div class="side-desc">
                  建议写清楚问题位置、故障现象和影响范围，方便管理员快速审核并分配维修人员。
                </div>
              </div>
            </div>

            <div class="tips-list">
              <div class="tip-item">
                <div class="tip-num blue-step">1</div>
                <div>
                  <div class="tip-title">标题要简洁</div>
                  <div class="tip-desc">例如“宿舍空调无法启动”“校园网无法连接”。</div>
                </div>
              </div>

              <div class="tip-item">
                <div class="tip-num orange-step">2</div>
                <div>
                  <div class="tip-title">地点要准确</div>
                  <div class="tip-desc">尽量写到楼栋、楼层、房间号或具体区域。</div>
                </div>
              </div>

              <div class="tip-item">
                <div class="tip-num green-step">3</div>
                <div>
                  <div class="tip-title">描述要完整</div>
                  <div class="tip-desc">说明出现时间、具体现象、是否已经尝试处理。</div>
                </div>
              </div>
            </div>

            <div class="status-box">
              <div class="status-title">提交后流程</div>
              <div class="flow-row">
                <span>待审核</span>
                <span>→</span>
                <span>待处理</span>
                <span>→</span>
                <span>处理中</span>
                <span>→</span>
                <span>已完成</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createWorkOrder } from '../../api/workOrder'
import { uploadWorkOrderImage } from '../../api/file'

const formRef = ref(null)
const uploadRef = ref(null)
const uploadFileList = ref([])
const imageUrls = ref([])
const uploadingCount = ref(0)
const submitting = ref(false)
const idempotencyKey = ref(crypto.randomUUID())

const form = reactive({
  title: '',
  description: '',
  location: '',
  category: '',
  priority: 'MEDIUM'
})

const rules = {
  title: [
    { required: true, whitespace: true, message: '请输入工单标题', trigger: 'blur' },
    { min: 2, max: 50, message: '工单标题长度应为 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, whitespace: true, message: '请输入问题描述', trigger: 'blur' },
    { min: 5, max: 500, message: '问题描述长度应为 5 到 500 个字符', trigger: 'blur' }
  ],
  location: [
    { required: true, whitespace: true, message: '请输入地点', trigger: 'blur' },
    { min: 2, max: 100, message: '地点长度应为 2 到 100 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择类型', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ]
}

async function submit() {
  if (submitting.value) return
  await formRef.value.validate()

  if (uploadingCount.value > 0) {
    ElMessage.warning('图片正在上传，请稍候再提交')
    return
  }

  submitting.value = true
  try {
    await createWorkOrder({
      title: form.title.trim(),
      description: form.description.trim(),
      location: form.location.trim(),
      category: form.category,
      priority: form.priority,
      imageUrls: imageUrls.value
    }, idempotencyKey.value)

    ElMessage.success('工单提交成功')
    reset()
  } finally {
    submitting.value = false
  }
}

function reset() {
  form.title = ''
  form.description = ''
  form.location = ''
  form.category = ''
  form.priority = 'MEDIUM'
  imageUrls.value = []
  uploadFileList.value = []
  uploadRef.value?.clearFiles()
  idempotencyKey.value = crypto.randomUUID()

  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

function beforeImageUpload(file) {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('仅支持 JPG、PNG、GIF、WEBP 图片')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('单张图片不能超过 5MB')
    return false
  }
  return true
}

async function handleImageUpload(options) {
  uploadingCount.value++
  try {
    const url = await uploadWorkOrderImage(options.file)
    imageUrls.value.push(url)
    options.onSuccess(url)
  } catch (error) {
    options.onError(error)
  } finally {
    uploadingCount.value--
  }
}

function handleImageRemove(file) {
  const url = typeof file.response === 'string' ? file.response : file.url
  const index = imageUrls.value.indexOf(url)
  if (index >= 0) imageUrls.value.splice(index, 1)
}

function handleExceed() {
  ElMessage.warning('工单图片最多上传 5 张')
}
</script>

<style scoped>
.create-order-page {
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.order-card {
  height: 100%;
  min-height: 0;
  border-radius: 16px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
}

.page-header {
  margin-bottom: 10px;
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

.form-panel {
  padding: 12px 14px 2px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
}

.upload-plus {
  font-size: 28px;
  color: #8c939d;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
}

.side-panel {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.side-card {
  display: flex;
  gap: 14px;
  padding: 12px;
  border-radius: 16px;
  border: 1px solid #eef2f7;
  background: #ffffff;
}

.highlight-card {
  background: linear-gradient(135deg, #eef6ff, #f8fbff);
}

.side-icon {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  background: #409eff;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.side-title {
  font-weight: 800;
  color: #303133;
  margin-bottom: 6px;
}

.side-desc {
  color: #606266;
  font-size: 13px;
  line-height: 1.7;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.tip-item {
  display: flex;
  gap: 12px;
  padding: 9px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
}

.tip-num {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 13px;
  flex-shrink: 0;
}

.blue-step {
  background: #409eff;
}

.orange-step {
  background: #e6a23c;
}

.green-step {
  background: #67c23a;
}

.tip-title {
  font-weight: 800;
  color: #303133;
  margin-bottom: 4px;
}

.tip-desc {
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
}

.status-box {
  padding: 10px;
  border-radius: 16px;
  background: #ffffff;
  border: 1px solid #eef2f7;
}

:deep(.el-form-item) {
  margin-bottom: 13px;
}

:deep(.el-upload--picture-card),
:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 82px;
  height: 82px;
}

.status-title {
  font-weight: 800;
  color: #303133;
  margin-bottom: 12px;
}

.flow-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  color: #409eff;
  font-weight: 700;
  font-size: 13px;
}
</style>
