<template>
  <div class="our-services">
    <h1 class="title">Our Services</h1>
    <p class="subtitle">Tailored solutions to move your business forward.</p>

    <div class="services-grid">
      <div class="service-card">
        <img src="@/assets/service1.jpg" alt="Digital Transformation" class="service-img" />
        <div class="card-content">
          <h2 class="service-title">Digital Transformation</h2>
          <p class="summary">Empowering businesses with modern tech and automation.</p>
          <p class="description">
            We help organizations adopt digital solutions that streamline operations,
            improve customer experiences, and drive innovation across departments.
          </p>
        </div>
      </div>

      <div class="service-card">
        <img src="@/assets/service2.jpeg" alt="Operational Excellence" class="service-img" />
        <div class="card-content">
          <h2 class="service-title">Operational Excellence</h2>
          <p class="summary">Enhancing efficiency, reducing cost, and boosting productivity.</p>
          <p class="description">
            Through process analysis and optimization, we support companies in building scalable,
            agile, and resilient operations.
          </p>
        </div>
      </div>

      <div class="service-card">
        <img src="@/assets/service3.jpg" alt="Market Strategy & Entry" class="service-img" />
        <div class="card-content">
          <h2 class="service-title">Market Strategy & Entry</h2>
          <p class="summary">Unlock new markets with a data-backed approach.</p>
          <p class="description">
            We provide end-to-end market research, competitive analysis, and go-to-market strategy
            for businesses expanding into new regions or launching new offerings.
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>

</script>

<style scoped lang="scss">
.our-services {
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;

  .title {
    font-size: 36px;
    font-weight: bold;
    margin-bottom: 10px;
  }

  .subtitle {
    font-size: 18px;
    color: #555;
    margin-bottom: 40px;
  }

  .services-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
    gap: 30px;
  }

  .service-card {
    background-color: #fff;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
    text-align: left;
    transition: transform 0.2s ease;

    &:hover {
      transform: translateY(-5px);
    }

    .service-img {
      width: 100%;
      height: 180px;
      object-fit: cover;
    }

    .card-content {
      padding: 20px;

      .service-title {
        font-size: 20px;
        font-weight: 600;
        margin-bottom: 10px;
      }

      .summary {
        font-size: 15px;
        font-weight: 500;
        color: #333;
        margin-bottom: 10px;
      }

      .description {
        font-size: 14px;
        color: #666;
        line-height: 1.6;
      }
    }
  }
}
</style>
