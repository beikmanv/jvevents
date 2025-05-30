# JVEvents – Full Stack Event Management Application

JVEvents is a complete Android-based event management solution powered by a Spring Boot backend. It supports user authentication via Google Sign-In, secure payments via Google Pay and Stripe, and Firebase integration for authentication and administrative operations.

---

# 🌐 TO DEMO THE APP ONLINE

---

## 📱 Android App (Frontend)

**Live on Appetize:**  
🔗 https://appetize.io/app/b_uheicun4h4jepejqnsjjx3hd2m  
> ⚠️ Appetize has a free 30-minute monthly limit. Demo mode is publicly accessible.
---

## ⚙️ API Server (Backend)

**Hosted on Render:**  
🔗 https://jvevents-api-1.onrender.com  
> ⚠️ Initial cold start may take ~1–2 minutes.

---

# 🛠 TO RUN THE APP LOCALLY

---

## 📲 FRONTEND

#### Repository: https://github.com/beikmanv/jvevents  
#### Recommended IDE: Android Studio  
#### Location of dependencies: `jvevents/app/build.gradle`
---


### 📦 Required Libraries / Technologies

- Firebase Authentication
- Google Sign-In (OAuth 2.0)
- Google Pay / Google Wallet
- Jetpack Navigation Component
- Retrofit
- Gson Converter**
- OkHttp & OkHttp Logging Interceptor

---

### 🔐 Google Sign-In Setup

The **Google OAuth 2.0 Web Client ID** is stored in:  
`jvevents/app/src/main/res/values/strings.xml`

Replace the `default_web_client_id` with your own credential from:  
**Firebase Console → Project Settings → OAuth 2.0 Client IDs**

It is used for:

- Google Sign-In  
- Firebase Authentication (Google provider)  
- Google API integrations (e.g. Calendar, Drive)

---

## 🖥 Backend

#### Repository: [https://github.com/beikmanv/jvevents-api](https://github.com/beikmanv/jvevents-api)  
#### Recommended IDE: IntelliJ IDEA  
#### Dependencies location: `jvevents-api/pom.xml`
---

### 📦 Required Libraries / Technologies

- Spring Boot (Web, Security, Validation, Mail)  
- Spring Security + OAuth 2.0 Client  
- PostgreSQL + Spring Data JPA  
- Lombok  
- Firebase Admin SDK  
- Google API Client & Google Calendar API  
- Stripe Java SDK  
- JUnit, Spring Test, DevTools  

---

### 🔧 Required Environment Variables

Refer to relevant guides to obtain all the secret keys and passwords. 

Set the following variables via IntelliJ **Run Configurations → Environment Variables**, or use a `.env` file:

```env
STRIPE_SECRET_KEY=sk_test_...
SPRING_PROFILES_ACTIVE=rds
GMAIL_EMAIL=...
GMAIL_APP_PASSWORD=...
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=GOCSPX-...
SPRING_DATASOURCE_URL=...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
```

✅ Alternatively, inject these via terminal or external config.

---

### 🗄 PostgreSQL Database Configuration

Ensure you have a local or hosted PostgreSQL instance.

---

### 🔧 application.properties / Environment Variables
You can either update `application.properties` directly or inject values using environment variables:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
```

---

### 🔐 Firebase Admin SDK

For backend operations like managing users and pushing to Firebase: 
- Firebase Console → Project Settings → Service Accounts
- Click "Generate new private key"
- Save as: 
```ini
jvevents-api/src/main/resources/serviceAccountKey.json
```

Ensure the private key uses \n as newline characters or is loaded via FileInputStream. A malformed key will break deserialization.

Example key structure:
```json
{
  "type": "service_account",
  "project_id": "your-project-id",
  "private_key_id": "...",
  "private_key": "-----BEGIN PRIVATE KEY-----\\n...\\n-----END PRIVATE KEY-----\\n",
  "client_email": "firebase-adminsdk@your-project.iam.gserviceaccount.com",
  ...
}
```

---

### 🧪 Runtime Configuration Note
Some environment variables are currently hardcoded in IntelliJ’s run configuration:

```ini
STRIPE_SECRET_KEY=...;SPRING_PROFILES_ACTIVE=rds;GMAIL_APP_PASSWORD=...
```
This can be replaced with .env loading.

---

### 👮 Admin Access Setup
Admin privileges (via the staff boolean) can be assigned programatically or directly in the database.

**Option A: In code**

Inside JwtAuthFilter.java (doFilterInternal method):
```java
user.setStaff(true);
```
Option B: Manually in the database (app_users table):
```sql
UPDATE app_users
SET staff = true
WHERE email = 'user@example.com';
```
---

### For technical support or implementation help:
📧 v.beikmanis@gmail.com








