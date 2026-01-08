## Online Demo
    
http://13.62.187.151/   public-facing company website
    
http://13.62.187.151/admin/  ERP management system

---

## System Overview

This ERP system is a full-stack management platform designed for consulting-style companies.

It provides **two separate user interfaces**: an **Admin Portal** for system management and an **Employee Portal** for daily business operations.

---

## Admin Portal

### Employee Management

- Manage employee accounts, including creating, editing, enabling, and disabling users.
- Supports paginated employee lists, fuzzy search by name, and role-based access control.
- New employees are created with a default password and required to reset it via email.

### Department Management

- Create, edit, search, and delete departments.
- Departments are centrally managed and used as references across the system.

### Position Management

- Manage job positions and associate them with existing departments.
- Supports full CRUD operations with department validation.

---

## Employee Portal

### Order Dashboard

- Displays consultation orders submitted through the company website.
- Managers can assign orders to consultants; employees can view only their assigned orders.
- Supports pagination, fuzzy search, and order archiving/restoration.

### Contract Management

- Manage customer contracts with role-based visibility.
- Supports creating, editing, archiving, and restoring contracts, including PDF attachment uploads.
- Managers can view contracts within their department, while employees see only their own.

### Customer Management

- Maintain customer records linked to employees and departments.
- Supports CRUD operations, fuzzy search, and customer archiving.
- Access is restricted based on user roles and responsibilities.


### Data Visualization

- Interactive dashboards displaying key business metrics, including:
    
    Number of signed contracts within a selected time range
    
    Total contract value within a selected time range
    
    Number of consultation orders received
    
    Top 5 employees by contract value

### AI-Powered Query Assistant

- Integrated AI chatbot that allows users to query predefined business metrics.
- Supports natural-language queries related to contracts, orders, and employee performance.
- Powered by the OpenAI API with controlled query scope.

In addition, the system integrates with a public-facing company website where potential clients can submit consultation requests. These requests are automatically synchronised into the ERP system as orders, enabling managers to review, assign, and track them within the internal workflow.

---

## Key Features

- **Role-Based Access Control (RBAC)**
    
    The system enforces role-based permissions, separating administrator, manager, and employee responsibilities to ensure secure and structured access to data.
    
- **Dual-Portal Architecture**
    
    Designed with separate Admin and Employee portals, enabling clear separation between system management and daily business operations.
    
- **End-to-End Order Lifecycle Management**
    
    Consultation orders are created via the company website, assigned by managers, tracked by employees, and archived upon completion.
    
- **Department-Aware Data Visibility**
    
    Contracts and customers are displayed based on departmental ownership, ensuring managers can oversee their teams while employees access only relevant records.
    
- **Interactive Data Visualization Dashboard**
    
    Business metrics such as contract volume, revenue trends, and top-performing employees are visualized using dynamic charts.
    
- **AI-Powered Business Query Assistant**
    
    An integrated AI chat interface allows users to query predefined business metrics using natural language, backed by the OpenAI API.
    
---

## Deployment Guide

This project is deployed on an AWS EC2 instance using Nginx as a reverse proxy for the Spring Boot backend and static front-end assets.

### Requirements

- Linux server (e.g. Ubuntu)
- Java (JDK 11)
- MySQL
- Redis
- Nginx

### Build & Run

- Build the backend with Maven and run the generated JAR.
- Build the front-end projects and serve the generated `dist` files via Nginx.
- Configure Nginx to proxy API requests to the backend service.

### Configuration

Application configuration is managed via `application.yml`.

A **sanitised configuration file** is included in this repository for reference.

Actual credentials and environment-specific values should be provided via local configuration or environment variables.
