<script setup>


</script>


<template>
  <div class="layout">
    <!-- header -->
    <header class="app-header">
      <div class="container">
       
        <img src="@/assets/companylogo.png" alt="" />
       
        <nav class="nav-menu">
          <RouterLink to="/" class="nav-item" >Home</RouterLink>
          <RouterLink to="/service" class="nav-item">Our Services</RouterLink>
          <RouterLink to="/request" class="nav-item">Request Consultation</RouterLink>
        </nav>
      </div>
    </header>

    <!-- main content area -->
    <main class="main-content">
      <RouterView />
    </main>

    <!-- footer -->
    <footer class="app-footer">
      <div class="footer-container">
        <div class="footer-content">
          <div class="footer-section">
            <h3>Company Info</h3>
            <p>Professional consulting services for your business needs.</p>
          </div>
          
          <div class="footer-section">
            <h3>Quick Links</h3>
            <ul>
              <li><RouterLink to="/">Home</RouterLink></li>
              <li><RouterLink to="/service">Services</RouterLink></li>
              <li><RouterLink to="/request">Contact</RouterLink></li>
            </ul>
          </div>
          
          <div class="footer-section">
            <h3>Contact</h3>
            <p>Email: info@company.com</p>
            <p>Phone: +44 123-4567-910</p>
          </div>
        </div>
        
        <div class="footer-bottom">
          <div class="copyright">
            <p>&copy; 2025 UBalance Consulting. All rights reserved.</p>
            <p>Privacy Policy | Terms of Service | Cookie Policy</p>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>



<style scoped lang="scss">
.layout {
  min-height: 100vh;
  background-color: #f5f5f5;
  width: 100%;
  display: flex;
  flex-direction: column;

  .app-header {
    background-color: #000;
    position: sticky;
    top: 0;
    z-index: 1000;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
 
    img {
      width: 200px;
      height: 60px;
      margin-right: 20px;
    }
    .container {
      width: 100%;
      padding: 0 20px;
      display: flex;
      align-items: center;
      justify-content: flex-start;
      height: 120px;
    }

    .nav-menu {
      display: flex;
      align-items: center;
      gap: 40px;

      .nav-item {
        color: white;
        text-decoration: none;
        font-size: 16px;
        font-weight: 500;
        padding: 8px 16px;
        border-radius: 6px;
        transition: all 0.3s ease;
        position: relative;

        &:hover {
          color: #96e9c9;
          background-color: rgba(150, 233, 201, 0.1);
        }

        &.router-link-exact-active {
          color: #96e9c9;
          
          &::after {
            content: '';
            position: absolute;
            bottom: -10px;
            left: 50%;
            transform: translateX(-50%);
            width: 30px;
            height: 2px;
            background-color: #96e9c9;
          }
        }
      }
    }
  }

  .main-content {
    flex: 1;
    width: 100%;
    padding: 20px;
    background-color: white;
    min-height: calc(100vh - 120px - 200px);
  }

  .app-footer {
    background-color: #1a1a1a;
    color: white;
    margin-top: auto;

    .footer-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 40px 20px 20px;
    }

    .footer-content {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 30px;
      margin-bottom: 30px;

      .footer-section {
        h3 {
          color: #96e9c9;
          margin-bottom: 15px;
          font-size: 18px;
          font-weight: 600;
        }

        p {
          margin-bottom: 8px;
          color: #cccccc;
          line-height: 1.6;
        }

        ul {
          list-style: none;
          padding: 0;

          li {
            margin-bottom: 8px;

            a {
              color: #cccccc;
              text-decoration: none;
              transition: color 0.3s ease;

              &:hover {
                color: #96e9c9;
              }
            }
          }
        }
      }
    }

    .footer-bottom {
      border-top: 1px solid #333;
      padding-top: 20px;

      .copyright {
        text-align: center;

        p {
          margin: 5px 0;
          color: #888;
          font-size: 14px;

          &:last-child {
            font-size: 12px;
          }
        }
      }
    }
  }
}

// responsive design
@media (max-width: 768px) {
  .layout {
    .app-header {
      .container {
        flex-direction: column;
        height: auto;
        padding: 15px 20px;

        img {
          margin-bottom: 15px;
          margin-right: 0;
        }
      }

      .nav-menu {
        gap: 20px;
        flex-wrap: wrap;
        justify-content: center;
      }
    }

    .main-content {
      padding: 15px;
    }

    .app-footer {
      .footer-container {
        padding: 30px 15px 15px;
      }

      .footer-content {
        grid-template-columns: 1fr;
        gap: 20px;
        text-align: center;
      }
    }
  }
}
</style>