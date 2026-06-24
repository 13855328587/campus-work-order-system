export function statusText(status) {
  const map = {
    PENDING_REVIEW: '待审核',
    REJECTED: '已驳回',
    PENDING_PROCESS: '待处理',
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