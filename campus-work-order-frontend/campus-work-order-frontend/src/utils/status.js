export function statusText(status) {
  const map = {
    PENDING_REVIEW: '待审核',
    REJECTED: '已驳回',
    PENDING_PROCESS: '待处理',
    WORKER_REJECTED: '被拒绝',
    PROCESSING: '处理中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

export function statusType(status) {
  const map = {
    PENDING_REVIEW: 'warning',
    REJECTED: 'danger',
    PENDING_PROCESS: 'primary',
    WORKER_REJECTED: 'danger',
    PROCESSING: 'success',
    COMPLETED: 'info',
    CANCELLED: 'info'
  }
  return map[status] || ''
}

export function roleText(role) {
  const map = {
    SUPER_ADMIN: '超级管理员',
    ADMIN: '管理员',
    WORKER: '维修人员',
    STUDENT: '学生'
  }
  return map[role] || role
}

export function categoryText(category) {
  const map = {
    ELECTRIC: '电器维修',
    NETWORK: '网络问题',
    FURNITURE: '家具设施',
    OTHER: '其他问题'
  }
  return map[category] || category || '未分类'
}

export function priorityText(priority) {
  const map = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高',
    URGENT: '紧急'
  }
  return map[priority] || priority || '未设置'
}
