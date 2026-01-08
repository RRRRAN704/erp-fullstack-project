<template>
  <div class="home">
    <section class="section">
      <div class="content">
        <div class="text">
          <h2>What We Do</h2>
          <p>
            At Ubalance Consulting, we specialize in providing strategic insights and tailored solutions to help businesses grow and thrive in a fast-changing world.
            From digital transformation to operational excellence, our team delivers actionable results across industries.
          </p>
        </div>
        <div class="image">
          <img src="@/assets/image1.jpg" alt="What We Do" /> 
        </div>
      </div>
    </section>

    <section class="section reverse">
      <div class="content">
        <div class="text">
          <h2>Why Choose Us</h2>
          <p>
            We combine deep industry knowledge with a data-driven mindset to craft impactful strategies that work.
            Our clients choose us for our integrity, precision, and unwavering commitment to results.
          </p>
        </div>
        <div class="image">
          <img src="@/assets/image2.jpg" alt="Why Choose Us" /> 
        </div>
      </div>
    </section>

    <section class="section">
      <div class="content">
        <div class="text">
          <h2>Client Success Stories</h2>
          <p>
            We partner with organizations of all sizes — from emerging startups to global enterprises.
            Discover how we've helped clients overcome challenges and unlock new growth opportunities.
          </p>
        </div>
        <div class="image">
          <img src="@/assets/image3.jpg" alt="Client Stories" /> 
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
</script>

<style scoped lang="scss">
.home {
  display: flex;
  flex-direction: column;
  gap: 80px;
  padding: 40px 20px;

  .section {
    display: flex;
    justify-content: center;

    .content {
      display: flex;
      flex-direction: row;
      align-items: center;
      max-width: 1200px;
      gap: 40px;

      .text {
        flex: 1;
        h2 {
          font-size: 28px;
          margin-bottom: 20px;
        }
        p {
          font-size: 16px;
          line-height: 1.8;
        }
      }

      .image {
        flex: 1;
        img {
          width: 100%;
          border-radius: 12px;
          object-fit: cover;
        }
      }
    }

    &.reverse .content {
      flex-direction: row-reverse;
    }
  }

  @media (max-width: 768px) {
    .section .content {
      flex-direction: column;

      .image img {
        max-height: 250px;
        object-fit: cover;
      }
    }

    .section.reverse .content {
      flex-direction: column;
    }
  }
}
</style>
