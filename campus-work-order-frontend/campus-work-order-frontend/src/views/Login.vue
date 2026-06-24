<template>
  <AuthShell>
    <div class="form-heading">
      <span class="form-kicker">WELCOME BACK</span>
      <h2>欢迎回来</h2>
      <p>登录账号，继续处理你的校园工单。</p>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      autocomplete="on"
      label-position="top"
      size="large"
      @keyup.enter="handleLogin"
    >
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="form.username"
          name="username"
          autocomplete="username"
          :prefix-icon="User"
          placeholder="请输入用户名"
          maxlength="20"
          clearable
        />
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <el-input
          v-model="form.password"
          name="password"
          autocomplete="current-password"
          :prefix-icon="Lock"
          type="password"
          placeholder="请输入密码"
          maxlength="32"
          show-password
        />
      </el-form-item>

      <el-form-item label="验证码" prop="captchaCode">
        <div class="captcha-row">
          <el-input
            v-model="form.captchaCode"
            placeholder="请输入验证码"
            maxlength="4"
            clearable
          />
          <button class="captcha-image" type="button" title="刷新验证码" :disabled="captchaLoading" @click="loadCaptcha">
            <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
            <span v-else>{{ captchaLoading ? '加载中' : '点击刷新' }}</span>
          </button>
        </div>
      </el-form-item>

      <div class="form-options">
        <el-checkbox v-model="rememberAccount">记住账号（30 天）</el-checkbox>
        <span class="secure-tip">安全登录</span>
      </div>

      <el-button type="primary" class="submit-btn" :loading="submitting" @click="handleLogin">
        登录系统
      </el-button>
    </el-form>

    <div class="switch-entry">
      还没有账号？
      <button type="button" @click="router.push('/register')">立即注册</button>
    </div>
  </AuthShell>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Lock, User } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AuthShell from '../components/AuthShell.vue'
import { getCaptcha, login } from '../api/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)
const rememberAccount = ref(false)
const captchaImage = ref('')
const captchaLoading = ref(false)

const form = reactive({
  username: '',
  password: '',
  captchaId: '',
  captchaCode: ''
})

const rules = {
  username: [
    { required: true, whitespace: true, message: '请输入用户名', trigger: 'blur' },
    { max: 20, message: '用户名不能超过 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度应为 6 到 32 个字符', trigger: 'blur' }
  ],
  captchaCode: [
    { required: true, whitespace: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为 4 个字符', trigger: 'blur' }
  ]
}

onMounted(() => {
  const remembered = localStorage.getItem('rememberedLogin')
  if (remembered) {
    try {
      const data = JSON.parse(remembered)
      if (data.expiresAt > Date.now() && data.username) {
        form.username = data.username
        rememberAccount.value = true
      } else {
        localStorage.removeItem('rememberedLogin')
      }
    } catch (error) {
      localStorage.removeItem('rememberedLogin')
    }
  }
  loadCaptcha()
})

async function loadCaptcha() {
  if (captchaLoading.value) return
  captchaLoading.value = true
  form.captchaCode = ''
  form.captchaId = ''
  captchaImage.value = ''
  try {
    const data = await getCaptcha()
    form.captchaId = data.captchaId
    captchaImage.value = data.image
  } catch (error) {
    // 请求拦截器已展示错误，保留刷新入口即可。
  } finally {
    captchaLoading.value = false
  }
}

async function handleLogin() {
  if (submitting.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (!form.captchaId) {
    ElMessage.error('请先加载验证码')
    return
  }

  submitting.value = true
  try {
    const data = await login({
      username: form.username.trim(),
      password: form.password,
      captchaId: form.captchaId,
      captchaCode: form.captchaCode.trim()
    })

    if (rememberAccount.value) {
      localStorage.setItem('rememberedLogin', JSON.stringify({
        username: form.username.trim(),
        expiresAt: Date.now() + 30 * 24 * 60 * 60 * 1000
      }))
    } else {
      localStorage.removeItem('rememberedLogin')
    }

    userStore.setUser(data)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    await loadCaptcha()
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.form-heading { margin-bottom: 32px; }
.form-kicker { color: #1687e8; font-size: 12px; font-weight: 800; letter-spacing: 0.18em; }
.form-heading h2 { margin: 10px 0 8px; color: #10213b; font-size: 34px; letter-spacing: -0.04em; }
.form-heading p { margin: 0; color: #718096; font-size: 14px; }
.form-options { margin: -2px 0 22px; display: flex; justify-content: space-between; align-items: center; }
.captcha-row { width: 100%; display: grid; grid-template-columns: minmax(0, 1fr) 120px; gap: 12px; }
.captcha-image { width: 120px; height: 48px; padding: 0; overflow: hidden; border: 1px solid #dbe5f1; border-radius: 8px; color: #718096; background: #f2f7fc; cursor: pointer; }
.captcha-image:disabled { cursor: wait; }
.captcha-image img { display: block; width: 100%; height: 100%; object-fit: cover; }
.secure-tip { color: #34a474; font-size: 13px; font-weight: 700; }
.submit-btn { width: 100%; height: 48px; border: 0; border-radius: 12px; font-weight: 800; letter-spacing: 0.08em; background: linear-gradient(100deg, #1687e8, #19b89a); box-shadow: 0 12px 28px rgba(22, 135, 232, 0.22); }
.switch-entry { margin-top: 26px; color: #718096; font-size: 14px; text-align: center; }
.switch-entry button { padding: 0; border: 0; color: #1687e8; background: transparent; cursor: pointer; font: inherit; font-weight: 800; }
:deep(.el-form-item__label) { color: #34445d; font-weight: 700; }
:deep(.el-input__wrapper) { min-height: 48px; border-radius: 12px; box-shadow: 0 0 0 1px #dbe5f1 inset; }
:deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1px #1687e8 inset, 0 0 0 4px rgba(22, 135, 232, 0.09); }
</style>
