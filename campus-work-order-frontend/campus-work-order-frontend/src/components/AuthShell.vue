<template>
  <main class="auth-page">
    <section class="visual-panel">
      <div class="visual-overlay"></div>
      <div class="brand-row">
        <img :src="schoolBrand" alt="安徽师范大学" />
        <span class="brand-system">校园智慧运维平台</span>
      </div>

      <div class="visual-content">
        <div class="eyebrow">ANHUI NORMAL UNIVERSITY · SMART CAMPUS</div>
        <h1>让每一次报修<br />都更快抵达解决</h1>
        <p>连接学生、管理员与维修人员，让校园服务从提交到完成全程清晰、高效可追踪。</p>

        <div class="feature-grid">
          <div class="feature-item">
            <strong>01</strong>
            <span>统一受理</span>
          </div>
          <div class="feature-item">
            <strong>02</strong>
            <span>实时流转</span>
          </div>
          <div class="feature-item">
            <strong>03</strong>
            <span>全程追踪</span>
          </div>
        </div>
      </div>

      <div class="visual-footer">
        <span class="pulse-dot"></span>
        校园设施服务在线
      </div>
    </section>

    <section class="form-panel">
      <div class="mobile-brand">
        <img :src="schoolBrand" alt="安徽师范大学" />
        <span class="brand-system">校园智慧运维平台</span>
      </div>
      <div class="form-card">
        <slot />
      </div>
      <div class="copyright">安徽师范大学 · 校园工单管理系统 · 安全、高效、可信赖</div>
    </section>
  </main>
</template>

<script setup>
import schoolBrand from '../assets/anhui-normal-university-brand.png'
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(540px, 1.25fr) minmax(420px, 0.75fr);
  overflow: hidden;
  background: #071426;
}

.visual-panel {
  position: relative;
  min-height: 100vh;
  padding: 42px 54px;
  display: flex;
  flex-direction: column;
  color: #fff;
  background: url('../assets/auth/anhui-normal-university-campus.png') center center / cover no-repeat;
  isolation: isolate;
}

.visual-overlay {
  position: absolute;
  inset: 0;
  z-index: -1;
  background:
    linear-gradient(90deg, rgba(0, 35, 70, 0.76), rgba(0, 48, 91, 0.42) 58%, rgba(0, 27, 52, 0.62)),
    linear-gradient(0deg, rgba(0, 24, 47, 0.78), transparent 62%);
}

.brand-row,
.mobile-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 17px;
  font-weight: 800;
  letter-spacing: 0.08em;
}

.brand-row img,
.mobile-brand img {
  width: 235px;
  max-width: 68%;
  padding: 8px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 10px 28px rgba(0, 30, 70, 0.2);
}

.brand-system {
  padding-left: 14px;
  border-left: 2px solid #d6a72c;
  white-space: nowrap;
  font-size: 13px;
  letter-spacing: 0.04em;
}

.visual-content {
  width: min(620px, 88%);
  margin: auto 0;
  padding-bottom: 40px;
}

.eyebrow {
  margin-bottom: 22px;
  color: #f1c85a;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.22em;
}

.visual-content h1 {
  margin: 0;
  font-size: clamp(42px, 4.2vw, 68px);
  line-height: 1.13;
  letter-spacing: -0.04em;
  text-shadow: 0 10px 34px rgba(0, 0, 0, 0.28);
}

.visual-content p {
  max-width: 560px;
  margin: 24px 0 30px;
  color: rgba(232, 244, 255, 0.82);
  font-size: 16px;
  line-height: 1.9;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(110px, 1fr));
  gap: 12px;
}

.feature-item {
  padding: 15px 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 16px;
  background: rgba(7, 30, 57, 0.38);
  backdrop-filter: blur(12px);
}

.feature-item strong {
  color: #63dcff;
  font-size: 13px;
}

.feature-item span {
  font-size: 13px;
  font-weight: 700;
}

.visual-footer {
  display: flex;
  align-items: center;
  gap: 9px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 13px;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #4ade80;
  box-shadow: 0 0 0 0 rgba(74, 222, 128, 0.55);
  animation: pulse 2s infinite;
}

.form-panel {
  position: relative;
  min-height: 100vh;
  padding: 54px clamp(38px, 5vw, 82px) 34px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background:
    radial-gradient(circle at 90% 8%, rgba(52, 211, 153, 0.12), transparent 24%),
    radial-gradient(circle at 15% 88%, rgba(47, 168, 255, 0.13), transparent 28%),
    #f8fbff;
}

.form-card {
  width: 100%;
  max-width: 460px;
  margin: auto;
}

.mobile-brand {
  display: none;
  color: #11233f;
}

.mobile-brand .brand-system {
  color: #0057a8;
}

.copyright {
  margin-top: auto;
  padding-top: 28px;
  color: #94a3b8;
  font-size: 12px;
  text-align: center;
}

@keyframes pulse {
  70% { box-shadow: 0 0 0 9px rgba(74, 222, 128, 0); }
  100% { box-shadow: 0 0 0 0 rgba(74, 222, 128, 0); }
}

@media (max-width: 980px) {
  .auth-page {
    display: block;
    background: url('../assets/auth/anhui-normal-university-campus.png') center / cover fixed;
  }

  .visual-panel {
    display: none;
  }

  .form-panel {
    min-height: 100vh;
    padding: 28px 22px;
    background: rgba(244, 249, 255, 0.94);
    backdrop-filter: blur(16px);
  }

  .mobile-brand {
    display: flex;
    flex-wrap: wrap;
    margin-bottom: 44px;
  }
}

@media (max-width: 520px) {
  .form-panel {
    padding: 22px 20px;
  }

  .mobile-brand {
    margin-bottom: 30px;
  }

  .mobile-brand img {
    width: 100%;
    max-width: 100%;
  }

  .mobile-brand .brand-system {
    padding-left: 0;
    border-left: 0;
  }
}
</style>
