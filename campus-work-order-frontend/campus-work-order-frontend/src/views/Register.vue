<template>
  <AuthShell>
    <div class="form-heading">
      <span class="form-kicker">CREATE ACCOUNT</span>
      <h2>创建学生账号</h2>
      <p>填写基本信息，加入校园智慧服务平台。</p>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      size="large"
      @keyup.enter="handleRegister"
    >
      <div class="form-grid">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            :prefix-icon="User"
            placeholder="请输入用户名"
            :maxlength="20"
            clearable
          />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="form.realName"
            :prefix-icon="Postcard"
            placeholder="请输入真实姓名"
            :maxlength="20"
            clearable
          />
        </el-form-item>
      </div>

      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="form.phone"
          :prefix-icon="Iphone"
          placeholder="请输入 11 位手机号"
          :maxlength="11"
          clearable
          @input="handlePhoneInput"
        />
      </el-form-item>

      <div class="form-grid">
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            :prefix-icon="Lock"
            type="password"
            placeholder="6-32 位密码"
            :maxlength="32"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            :prefix-icon="CircleCheck"
            type="password"
            placeholder="请再次输入密码"
            :maxlength="32"
            show-password
          />
        </el-form-item>
      </div>

      <el-button type="primary" class="submit-btn" :loading="submitting" @click="handleRegister">
        创建账号
      </el-button>
    </el-form>

    <div class="switch-entry">
      已有账号？
      <button type="button" @click="router.push('/login')">返回登录</button>
    </div>
  </AuthShell>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { CircleCheck, Iphone, Lock, Postcard, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import AuthShell from '../components/AuthShell.vue'
import { register } from '../api/auth'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: ''
})

function validateConfirmPassword(rule, value, callback) {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, whitespace: true, message: '请输入用户名', trigger: 'blur' },
    { max: 20, message: '用户名不能超过 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, whitespace: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '真实姓名长度应为 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的 11 位手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度应为 6 到 32 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

function handlePhoneInput(value) {
  form.phone = value.replace(/\D/g, '').slice(0, 11)
}

async function handleRegister() {
  if (submitting.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await register({
      username: form.username.trim(),
      password: form.password,
      realName: form.realName.trim(),
      phone: form.phone
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.form-heading { margin-bottom: 26px; }
.form-kicker { color: #1687e8; font-size: 12px; font-weight: 800; letter-spacing: 0.18em; }
.form-heading h2 { margin: 9px 0 7px; color: #10213b; font-size: 31px; letter-spacing: -0.04em; }
.form-heading p { margin: 0; color: #718096; font-size: 14px; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.submit-btn { width: 100%; height: 48px; margin-top: 4px; border: 0; border-radius: 12px; font-weight: 800; letter-spacing: 0.08em; background: linear-gradient(100deg, #1687e8, #19b89a); box-shadow: 0 12px 28px rgba(22, 135, 232, 0.22); }
.switch-entry { margin-top: 22px; color: #718096; font-size: 14px; text-align: center; }
.switch-entry button { padding: 0; border: 0; color: #1687e8; background: transparent; cursor: pointer; font: inherit; font-weight: 800; }
:deep(.el-form-item) { margin-bottom: 18px; }
:deep(.el-form-item__label) { color: #34445d; font-weight: 700; }
:deep(.el-input__wrapper) { min-height: 46px; border-radius: 12px; box-shadow: 0 0 0 1px #dbe5f1 inset; }
:deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1px #1687e8 inset, 0 0 0 4px rgba(22, 135, 232, 0.09); }
@media (max-width: 520px) { .form-grid { grid-template-columns: 1fr; gap: 0; } .form-heading h2 { font-size: 28px; } }
</style>
