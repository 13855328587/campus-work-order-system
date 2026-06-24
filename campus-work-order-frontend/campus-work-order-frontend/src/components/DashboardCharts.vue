<template>
  <section class="analytics-section">
    <div class="analytics-header">
      <div>
        <h3>工单数据分析</h3>
        <p>根据当前账号可访问的真实工单实时计算</p>
      </div>
      <div class="live-badge"><span></span> REAL DATA</div>
    </div>

    <div class="analytics-grid">
      <div class="chart-card trend-card">
        <div class="chart-title-row">
          <div>
            <h4>近 7 日工单趋势</h4>
            <p>按工单创建时间统计</p>
          </div>
          <strong>{{ recentTotal }}</strong>
        </div>

        <div class="trend-chart">
          <div class="y-guides">
            <span v-for="guide in 4" :key="guide"></span>
          </div>
          <div v-for="item in trendData" :key="item.key" class="trend-column">
            <div class="bar-area">
              <span class="bar-value">{{ item.value }}</span>
              <div class="trend-bar" :style="{ height: `${item.percent}%` }"></div>
            </div>
            <span class="axis-label">{{ item.label }}</span>
          </div>
        </div>
      </div>

      <div class="chart-card category-card">
        <div class="chart-title-row">
          <div>
            <h4>工单类型分布</h4>
            <p>不同维修类型占比</p>
          </div>
        </div>

        <div class="donut-layout">
          <div class="donut" :style="{ background: donutBackground }">
            <div class="donut-center">
              <strong>{{ orders.length }}</strong>
              <span>工单总数</span>
            </div>
          </div>
          <div class="legend-list">
            <div v-for="item in categoryData" :key="item.key" class="legend-item">
              <span class="legend-dot" :style="{ background: item.color }"></span>
              <span class="legend-name">{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
              <small>{{ item.percent }}%</small>
            </div>
          </div>
        </div>
      </div>

      <div class="chart-card priority-card">
        <div class="chart-title-row">
          <div>
            <h4>优先级结构</h4>
            <p>识别高优先级维修需求</p>
          </div>
        </div>
        <div class="priority-list">
          <div v-for="item in priorityData" :key="item.key" class="priority-row">
            <div class="priority-meta">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
            <div class="priority-track">
              <div :style="{ width: `${item.percent}%`, background: item.color }"></div>
            </div>
          </div>
        </div>
      </div>

      <div class="chart-card metric-card">
        <div class="metric-item">
          <span>今日新增</span>
          <strong>{{ todayCount }}</strong>
          <small>按创建时间统计</small>
        </div>
        <div class="metric-item">
          <span>完成率</span>
          <strong>{{ completionRate }}%</strong>
          <small>已完成 / 全部工单</small>
        </div>
        <div class="metric-item">
          <span>紧急与高优先级</span>
          <strong>{{ importantCount }}</strong>
          <small>需要优先关注</small>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  orders: {
    type: Array,
    default: () => []
  }
})

const categoryOptions = [
  { key: 'ELECTRIC', label: '电器维修', color: '#0057a8' },
  { key: 'NETWORK', label: '网络问题', color: '#18a999' },
  { key: 'FURNITURE', label: '家具设施', color: '#d6a72c' },
  { key: 'OTHER', label: '其他问题', color: '#7c6ac7' }
]

const priorityOptions = [
  { key: 'URGENT', label: '紧急', color: '#e34d59' },
  { key: 'HIGH', label: '高', color: '#f08c46' },
  { key: 'MEDIUM', label: '中', color: '#d6a72c' },
  { key: 'LOW', label: '低', color: '#28a879' }
]

function localDateKey(value) {
  if (!value) return ''
  const date = new Date(String(value).replace(' ', 'T'))
  if (Number.isNaN(date.getTime())) return ''
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const trendData = computed(() => {
  const days = []
  const now = new Date()
  for (let offset = 6; offset >= 0; offset--) {
    const date = new Date(now.getFullYear(), now.getMonth(), now.getDate() - offset)
    const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    days.push({ key, label: `${date.getMonth() + 1}/${date.getDate()}`, value: 0 })
  }

  const index = new Map(days.map(item => [item.key, item]))
  props.orders.forEach(order => {
    const item = index.get(localDateKey(order.createdAt))
    if (item) item.value++
  })

  const max = Math.max(1, ...days.map(item => item.value))
  return days.map(item => ({ ...item, percent: Math.max(item.value ? 8 : 2, item.value / max * 100) }))
})

const recentTotal = computed(() => trendData.value.reduce((sum, item) => sum + item.value, 0))

const categoryData = computed(() => {
  const total = props.orders.length || 1
  return categoryOptions.map(option => {
    const value = props.orders.filter(order => (order.category || 'OTHER') === option.key).length
    const share = value / total * 100
    return { ...option, value, share, percent: Math.round(share) }
  })
})

const donutBackground = computed(() => {
  if (!props.orders.length) return '#e8eef5'
  let start = 0
  const segments = categoryData.value.map(item => {
    const end = start + item.share
    const segment = `${item.color} ${start}% ${end}%`
    start = end
    return segment
  })
  if (start < 100) segments.push(`#e8eef5 ${start}% 100%`)
  return `conic-gradient(${segments.join(', ')})`
})

const priorityData = computed(() => {
  const max = Math.max(1, ...priorityOptions.map(option =>
    props.orders.filter(order => order.priority === option.key).length
  ))
  return priorityOptions.map(option => {
    const value = props.orders.filter(order => order.priority === option.key).length
    return { ...option, value, percent: value / max * 100 }
  })
})

const todayCount = computed(() => {
  const today = localDateKey(new Date())
  return props.orders.filter(order => localDateKey(order.createdAt) === today).length
})

const completionRate = computed(() => {
  if (!props.orders.length) return 0
  const completed = props.orders.filter(order => order.status === 'COMPLETED').length
  return Math.round(completed / props.orders.length * 100)
})

const importantCount = computed(() =>
  props.orders.filter(order => order.priority === 'URGENT' || order.priority === 'HIGH').length
)
</script>

<style scoped>
.analytics-section { min-height: 0; margin-top: 10px; display: flex; flex: 1; flex-direction: column; overflow: hidden; }
.analytics-header { margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center; }
.analytics-header h3, .chart-title-row h4 { margin: 0; color: #24364b; }
.analytics-header h3 { font-size: 18px; }
.analytics-header p, .chart-title-row p { margin: 4px 0 0; color: #8a98aa; font-size: 12px; }
.live-badge { padding: 6px 10px; border: 1px solid #cce8dc; border-radius: 999px; color: #18835d; font-size: 11px; font-weight: 800; letter-spacing: .08em; background: #effaf5; }
.live-badge span { display: inline-block; width: 7px; height: 7px; margin-right: 6px; border-radius: 50%; background: #22b47b; box-shadow: 0 0 0 4px rgba(34, 180, 123, .12); }
.analytics-grid { min-height: 0; display: grid; flex: 1; grid-template-columns: 1.6fr 1.1fr 1fr 1.1fr; gap: 10px; }
.chart-card { min-width: 0; min-height: 0; padding: 13px; overflow: hidden; border: 1px solid #e9eff5; border-radius: 16px; background: #fff; box-shadow: 0 8px 22px rgba(0, 63, 120, .055); }
.chart-title-row { display: flex; justify-content: space-between; align-items: flex-start; }
.chart-title-row h4 { font-size: 16px; }
.chart-title-row > strong { color: #0057a8; font-size: 28px; }
.trend-chart { position: relative; height: 165px; margin-top: 8px; padding: 8px 3px 0; display: grid; grid-template-columns: repeat(7, 1fr); gap: 5px; }
.y-guides { position: absolute; inset: 8px 0 25px; display: flex; flex-direction: column; justify-content: space-between; pointer-events: none; }
.y-guides span { border-top: 1px dashed #e6edf4; }
.trend-column { z-index: 1; min-width: 0; display: flex; flex-direction: column; align-items: center; }
.bar-area { width: 100%; height: 122px; display: flex; flex-direction: column; justify-content: flex-end; align-items: center; }
.bar-value { margin-bottom: 5px; color: #526579; font-size: 11px; font-weight: 700; }
.trend-bar { width: min(32px, 72%); min-height: 3px; border-radius: 8px 8px 3px 3px; background: linear-gradient(180deg, #1780c8, #0057a8); box-shadow: 0 6px 12px rgba(0, 87, 168, .18); transition: height .3s ease; }
.axis-label { margin-top: 8px; color: #7c8b9d; font-size: 11px; }
.donut-layout { margin-top: 12px; display: flex; flex-direction: column; align-items: center; gap: 10px; }
.donut { width: 105px; height: 105px; flex: 0 0 105px; display: grid; place-items: center; border-radius: 50%; }
.donut-center { width: 64px; height: 64px; display: flex; flex-direction: column; align-items: center; justify-content: center; border-radius: 50%; background: #fff; box-shadow: 0 0 0 1px #edf1f5; }
.donut-center strong { color: #183a5d; font-size: 24px; }
.donut-center span { color: #91a0b0; font-size: 10px; }
.legend-list { min-width: 0; flex: 1; display: flex; flex-direction: column; gap: 10px; }
.legend-item { display: grid; grid-template-columns: 9px 1fr auto 34px; gap: 7px; align-items: center; font-size: 12px; }
.legend-dot { width: 8px; height: 8px; border-radius: 50%; }
.legend-name { color: #607185; white-space: nowrap; }
.legend-item strong { color: #26394d; }
.legend-item small { color: #98a5b4; text-align: right; }
.priority-list { margin-top: 12px; display: flex; flex-direction: column; gap: 11px; }
.priority-meta { margin-bottom: 6px; display: flex; justify-content: space-between; color: #627387; font-size: 12px; }
.priority-meta strong { color: #26394d; }
.priority-track { height: 9px; overflow: hidden; border-radius: 999px; background: #edf2f6; }
.priority-track div { height: 100%; min-width: 2px; border-radius: inherit; transition: width .3s ease; }
.metric-card { display: grid; grid-template-columns: 1fr; gap: 4px; background: linear-gradient(135deg, #003f78, #0878c9); }
.metric-item { padding: 8px; display: flex; flex-direction: column; border-right: 0; border-bottom: 1px solid rgba(255, 255, 255, .16); color: #fff; }
.metric-item:last-child { border-right: 0; }
.metric-item span { color: rgba(255, 255, 255, .72); font-size: 12px; }
.metric-item strong { margin: 4px 0 2px; font-size: 23px; }
.metric-item small { color: rgba(255, 255, 255, .62); font-size: 10px; }
@media (max-width: 1180px) { .analytics-grid { grid-template-columns: 1.5fr 1fr; grid-template-rows: repeat(2, minmax(0, 1fr)); } .trend-chart { height: 125px; } .bar-area { height: 88px; } .donut-layout { flex-direction: row; } .metric-card { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 760px) { .category-card, .priority-card { display: none; } .analytics-grid { grid-template-columns: 1.5fr 1fr; grid-template-rows: 1fr; } .metric-card { grid-template-columns: 1fr; } .metric-item small { display: none; } }
</style>
