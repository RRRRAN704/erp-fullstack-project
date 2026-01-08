<script setup>

import { useRouter } from 'vue-router'
const router = useRouter()

const goHome = () => {
  router.push('/')
}

</script>

<template>
  <div class="thank-you-page">
    <div class="content-card">
      <!-- Success Icon -->
      <div class="success-icon">
        <svg viewBox="0 0 24 24" width="80" height="80">
          <circle cx="12" cy="12" r="10" fill="#52c41a"/>
          <path d="M9 12l2 2 4-4" stroke="white" stroke-width="2" fill="none"/>
        </svg>
      </div>

      <!-- Main Content -->
      <h1 class="title">Thank You!</h1>
      <p class="subtitle">
        Your consultation request has been submitted successfully. 
        We'll get back to you within 24 hours.
      </p>

      <!-- Contact Info -->
      <div class="contact-info">
        <p>Need immediate assistance?</p>
        <p>
          Email: <a href="mailto:contact@company.com">contact@company.com</a> | 
          Phone: <a href="tel:+15551234567">+44 123-4567-910</a>
        </p>
      </div>

      <!-- Action Button -->
      <div class="actions">
        <button class="back-btn" @click="goHome">
          Back to Homepage
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.thank-you-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.content-card {
  background: white;
  border-radius: 12px;
  padding: 60px 40px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
}

.success-icon {
  margin-bottom: 30px;
}

.title {
  font-size: 2.5rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

.subtitle {
  font-size: 1.1rem;
  color: #666;
  line-height: 1.6;
  margin-bottom: 40px;
}

.contact-info {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 40px;
  font-size: 0.9rem;
  color: #666;
}

.contact-info a {
  color: #409eff;
  text-decoration: none;
}

.contact-info a:hover {
  text-decoration: underline;
}

.actions {
  margin-top: 30px;
}

.back-btn {
  background: #409eff;
  color: white;
  border: none;
  padding: 12px 30px;
  font-size: 1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.back-btn:hover {
  background: #3a8ee6;
}

/* Mobile responsive */
@media (max-width: 768px) {
  .content-card {
    padding: 40px 20px;
    margin: 20px;
  }
  
  .title {
    font-size: 2rem;
  }
  
  .subtitle {
    font-size: 1rem;
  }
}
</style>