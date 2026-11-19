# SmartSpend: Personal Finance & Spending Analytics for College Students

**SmartSpend** is a web-based personal finance management and spending analytics system designed specifically for college students. Developed as an advanced Computer Science Engineering capstone project, SmartSpend combines a modern, fintech-grade responsive web frontend, a robust **Java 21 Spring Boot** REST backend, **Firebase Authentication & Cloud Firestore** persistence, and **DNEPS AI** ("Your Personal Smart Spending & Savings Buddy").

---

## 1. Problem Statement & Project Objectives

College students face unique financial realities: erratic allowances, part-time job income, shared room rents, study book expenses, canteen snacks, and discretionary group outings. Traditional banking and budgeting tools either assume fixed salaries or generic household expenses, lacking the granularity to handle itemized student receipts.

### Key Objectives:
* **Record & Manage Transactions:** Easily track income (allowances, scholarships, part-time jobs) and daily expenses.
* **Essential Itemized Expense Recording:** Break down complex single expenses into exact line items (e.g. ₹350 Canteen bill: Lunch ₹150, Chips ₹60, Drinks ₹70, Chocolate ₹70) with zero double-counting.
* **4-Tier Budget Limits:** Set monthly overall and category limits with clear visual health states (Within budget, Approaching limit, Fully used, Exceeded).
* **Authoritative Financial Calculations:** All monetary arithmetic, net period balances, and potential savings are authoritatively computed on the **Java 21 backend** to prevent floating-point errors.
* **Where You Could Save (Avoidable Expense Analysis):** Analyze flexible student spending patterns with an interactive reduction slider to estimate potential savings towards real financial milestones.
* **Savings Goals:** Set targets for laptops, headphones, or semester trips with automated weekly savings rate calculations.
* **DNEPS AI:** A personal smart spending & savings conversational buddy that provides tailored 7-day spending reviews, budget alerts, and advice grounded in the student's actual Java-calculated financial records.

> **Note:** SmartSpend is a personal record-keeping and spending analytics system, not a banking institution. All records are recorded and verified by the user.

---

## 2. Three Core Modules

```
+---------------------------------------------------------------------------------------+
|                                    SMARTSPEND SYSTEM                                  |
+---------------------------------------------------------------------------------------+
|  MODULE 1: EXPENSE & INCOME      |  MODULE 2: BUDGETS & ANALYTICS |  MODULE 3: SAVINGS & AI   |
|  ---------------------------     |  ----------------------------- |  -----------------------  |
|  • Income & Expense tracking     |  • Monthly overall & category  |  • Milestone savings goals|
|  • Itemized receipts breakdown   |  • 4-tier status thresholds    |  • Weekly savings rate    |
|  • Search by purchased item name |  • Chart.js visual analytics   |  • Deposit contributions  |
|  • Authoritative Java net calc   |  • "Where You Could Save"      |  • DNEPS AI Chat Assistant|
|  • Multi-criteria filter & sort  |  • Dynamic reduction slider    |  • 7-Day Weekly Review    |
+---------------------------------------------------------------------------------------+
```

---

## 3. Technology Stack

* **Frontend:**
  * HTML5, CSS3, Modern JavaScript ES6+
  * Chart.js (Interactive Doughnut, Bar, Line graphs)
  * Firebase Authentication Client SDK
  * Responsive Design System with Emerald Green (`#059669`) / Teal (`#0D9488`) aesthetics
* **Java Backend:**
  * **Java 21 (LTS)**
  * **Spring Boot 3.3.4**
  * Spring Web (REST APIs)
  * Spring Security (Bearer Token Authentication Filter)
  * Jakarta Bean Validation (`@Valid`, `@Positive`, `@NotBlank`)
  * Firebase Admin SDK for Java (ID Token verification & Firestore connectivity)
  * JUnit 5 & Spring Boot Test
  * Apache Maven 3.9+
* **Database & Cloud:**
  * Firebase Cloud Firestore (`users/{userId}/...` isolated schema)
  * Firestore Security Rules (`firestore.rules`)
* **AI Engine:**
  * Server-side Google Gemini / OpenAI language model integration
  * Server-side secret management (Zero API key exposure to frontend)

---

## 4. System Architecture

```
                         COLLEGE STUDENT
                                |
                                v
                HTML5 + Vanilla CSS3 + JavaScript
                                |
                   Firebase Authentication
                                |
                     Firebase ID Token
                                |
                                v
                   JAVA 21 SPRING BOOT BACKEND
                                |
      +-------------------------+-------------------------+
      |                         |                         |
      v                         v                         v
Firebase Token Auth       Business Logic              DNEPS AI
Verification Filter        & Java Calc                Service
 (Derives verified UID)   (Zero double-count)   (Prompt enriched with
      |                         |                exact Java math)
      +-------------------------+-------------------------+
                                |
                                v
                    FIREBASE CLOUD FIRESTORE
                users/{userId}/transactions
                users/{userId}/budgets
                users/{userId}/savingsGoals
                                |
                                v
               JSON REST APIs to Frontend Dashboard
```

---

## 5. Folder Structure

```
smartspend/
├── frontend/
│   ├── index.html              # Marketing landing page & module overview
│   ├── login.html              # Student sign-in with quick Demo credentials filler
│   ├── register.html           # Student registration page
│   ├── dashboard.html          # Financial metrics, charts, review & DNEPS AI
│   ├── transactions.html       # Full transaction CRUD & itemized line items
│   ├── budgets.html            # Category budgets & Where You Could Save
│   ├── savings.html            # Savings goals & contribution tracker
│   ├── profile.html            # Profile, spending mode selector & diagnostics
│   ├── css/
│   │   ├── style.css           # Core design tokens, typography, buttons, tables
│   │   ├── auth.css            # Authentication form layouts & cards
│   │   ├── dashboard.css       # Grid, metrics, itemized builder, AI widget
│   │   └── responsive.css      # Mobile, tablet, and desktop media queries
│   └── js/
│       ├── firebase-config.js  # Firebase client SDK initialization & fallback
│       ├── auth.js             # Client session management & token provider
│       ├── api.js              # Authorized Fetch client with Bearer token
│       ├── utils.js            # Indian Rupee (₹) formatting & date utilities
│       ├── ui.js               # Toast alerts, modals, and confirmations
│       ├── app.js              # Global header, user menu, and route guards
│       ├── transactions.js     # Itemized receipt addition & sync logic
│       ├── budgets.js          # 4-tier budget thresholds & calculations
│       ├── analytics.js        # Chart.js Doughnut, Bar, and Line rendering
│       ├── savings.js          # Savings goal math & weekly rate calculator
│       └── dneps-ai.js         # DNEPS AI chat assistant & weekly review card
├── backend/
│   ├── pom.xml                 # Maven build file with Java 21 & Spring Boot 3
│   └── src/
│       ├── main/
│       │   ├── java/com/smartspend/
│       │   │   ├── SmartSpendApplication.java
│       │   │   ├── config/       # Firebase, Security, and CORS configurations
│       │   │   ├── controller/   # REST Controllers for all 3 modules
│       │   │   ├── service/      # Authoritative calculations & AI integration
│       │   │   ├── repository/   # User-isolated Firestore data access
│       │   │   ├── model/        # Entities (Transaction, Budget, Goal, Profile)
│       │   │   ├── dto/          # Validated requests and responses
│       │   │   ├── security/     # Firebase ID token verification filter
│       │   │   └── exception/    # Centralized REST exception handlers
│       │   └── resources/
│       │       └── application.properties
│       └── test/java/com/smartspend/service/
│           ├── TransactionServiceTest.java
│           ├── BudgetServiceTest.java
│           ├── AnalyticsServiceTest.java
│           ├── SavingsGoalServiceTest.java
│           └── DnepsAIServiceTest.java
├── functions/
│   └── README.md               # Cloud Functions architectural documentation
├── firestore.rules             # Strict user-isolated Firestore security rules
├── firestore.indexes.json      # Firestore composite indexes
├── firebase.json               # Firebase Hosting & Firestore configuration
├── .env.example                # Example configuration variables template
├── .gitignore
└── README.md
```

---

## 6. Setup & Installation Guide

### Prerequisites
1. **Java Development Kit (JDK 21):** Make sure `java -version` returns Java 21+.
2. **Apache Maven:** Make sure `mvn -v` is installed or accessible.

---

### Step 1: Clone and Configure Environment

1. Copy `.env.example` to `.env` if using environment variables:
   ```bash
   cp .env.example .env
   ```

2. Configure `backend/src/main/resources/application.properties` with your server settings:
   ```properties
   server.port=8080
   ai.provider=GEMINI
   ai.api.key=YOUR_GEMINI_API_KEY
   ```

---

### Step 2: Configure Firebase (Optional for Live Cloud, Demo Mode Works Immediately)

#### A. Firebase Console Setup
1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project (e.g. `smartspend-college`).
2. Navigate to **Authentication** > **Sign-in method** > Enable **Email/Password**.
3. Navigate to **Firestore Database** > Create Database (Start in production mode).
4. Deploy the included Firestore rules:
   ```bash
   firebase deploy --only firestore:rules
   ```

#### B. Frontend Firebase Client Configuration
Open `frontend/js/firebase-config.js` and paste your web app credentials from Project Settings:
```javascript
const firebaseConfig = {
  apiKey: "YOUR_FIREBASE_WEB_API_KEY",
  authDomain: "your-project.firebaseapp.com",
  projectId: "your-project",
  storageBucket: "your-project.appspot.com",
  messagingSenderId: "123456789012",
  appId: "1:123456789012:web:abcdef..."
};
```

#### C. Backend Firebase Admin SDK Credentials
1. In Firebase Console, go to **Project Settings** > **Service accounts**.
2. Click **Generate new private key** and download the JSON file.
3. Save it as `backend/firebase-service-account.json`.
4. In `backend/src/main/resources/application.properties`, configure:
   ```properties
   firebase.credentials.path=./firebase-service-account.json
   ```

*(Note: If you run without a service account file, SmartSpend automatically operates in Local Development mode with persistent memory storage, allowing full local evaluation without cloud credentials).*

---

### Step 3: Run the Java Backend

Open a terminal in the `backend/` directory:

```powershell
# Set JAVA_HOME and PATH if not already in system environment
$env:JAVA_HOME = "C:\Users\Sonaa.R\tools\jdk-21.0.6+7"
$env:PATH = "C:\Users\Sonaa.R\tools\jdk-21.0.6+7\bin;C:\Users\Sonaa.R\tools\apache-maven-3.9.6\bin;" + $env:PATH

# Compile and start Spring Boot
mvn spring-boot:run
```

The backend starts at `http://localhost:8080/api`. Verify via:
```
http://localhost:8080/api/health
```

---

### Step 4: Run the Frontend

You can serve the `frontend/` folder using any static HTTP server or Python:

```bash
# Using Python
python -m http.server 5000 --directory frontend

# Or using Node
npx serve frontend -p 5000
```

Open `http://localhost:5000` in your web browser.

---

## 7. Running Automated Tests

Run the complete JUnit 5 test suite:

```powershell
cd backend
mvn test
```

### Test Coverage Highlights:
1. `TransactionServiceTest`: Positive amount validation, single expense creation, itemized expense verification (`₹150 + ₹60 + ₹70 + ₹70 = ₹350`), rejection of mismatched totals, and search by item name.
2. `BudgetServiceTest`: 4-tier status thresholds (<75% Within budget, 75-99% Approaching limit, 100% Fully used, >100% Exceeded), and duplicate budget prevention.
3. `AnalyticsServiceTest`: Authoritative net balance calculation (`income - expenses`), and Section 6.D "Where You Could Save" reduction calculations (`₹500 * 30% = ₹150 saved, target ₹350`).
4. `SavingsGoalServiceTest`: Target vs saved progress, remaining amount, weekly savings rate estimation, contribution deposits, and fully funded milestones.
5. `DnepsAIServiceTest`: Contextual answering using actual Java financial statistics, honest demonstration fallback when API key is unconfigured, and 7-day weekly reviews.

---

## 8. REST API Documentation

All protected endpoints require `Authorization: Bearer <ID_TOKEN>`.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | Public service health and diagnostics |
| `POST` | `/api/transactions` | Create income or itemized expense |
| `GET` | `/api/transactions` | List & filter transactions (type, category, search) |
| `GET` | `/api/transactions/{id}` | Retrieve single transaction |
| `PUT` | `/api/transactions/{id}` | Update existing transaction |
| `DELETE` | `/api/transactions/{id}` | Delete transaction |
| `GET` | `/api/analytics/summary` | Authoritative income, expense & net totals |
| `GET` | `/api/analytics/categories` | Expense breakdown by category for Doughnut chart |
| `GET` | `/api/analytics/trends` | Daily spending trend points for Line chart |
| `GET` | `/api/analytics/monthly-comparison` | Monthly income vs expenses for Bar chart |
| `GET` | `/api/analytics/where-you-could-save` | Avoidable expense analysis with reduction slider |
| `POST` | `/api/budgets` | Create category or overall monthly budget |
| `GET` | `/api/budgets` | List budgets with spent amounts & 4-tier status |
| `PUT` | `/api/budgets/{id}` | Update budget limit or month |
| `DELETE` | `/api/budgets/{id}` | Delete budget limit |
| `POST` | `/api/savings-goals` | Create savings milestone goal |
| `GET` | `/api/savings-goals` | List goals with progress & required weekly rates |
| `POST` | `/api/savings-goals/{id}/contribute` | Deposit contribution towards a goal |
| `DELETE` | `/api/savings-goals/{id}` | Delete savings goal |
| `POST` | `/api/ai/ask` | Submit conversational question to DNEPS AI |
| `GET` | `/api/ai/weekly-review` | 7-day spending review vs previous period |
| `GET` | `/api/user/profile` | Retrieve user profile & settings |
| `PUT` | `/api/user/preferences` | Update spending mode (Easy, Balanced, Savings Focus) |

---

## 9. Capstone Evaluation & Demonstration Instructions

When presenting to your professors or evaluators:
1. **Start Backend & Frontend:** Launch both processes using the commands above.
2. **One-Click Demo Dataset:**
   - Log in using the **"Fill Demo"** button on `login.html`.
   - Go to **Settings (`profile.html`)** and click **"Populate College Sample Data"**.
   - This automatically creates the ₹8,000 monthly allowance, the ₹350 canteen expense with itemized items (Lunch ₹150, Chips ₹60, Cool drink ₹70, Chocolate ₹70), textbooks expense, budgets, and headphones savings goal.
3. **Inspect Module 1 (Transactions):** Show the itemized receipt breakdown without double counting. Filter by "Food" or search by "Chips" to see item-level searching in action.
4. **Inspect Module 2 (Budgets & Analytics):** View the 4-tier visual progress bars and the interactive Chart.js charts. Adjust the reduction slider in **"Where You Could Save"** to demonstrate dynamic savings targets.
5. **Inspect Module 3 (Savings Goals & DNEPS AI):** Show the target date countdown and suggested weekly savings rate. Click the floating **DNEPS AI** button and ask: *"How much did I spend on snacks this week?"* to showcase the assistant using the real Java calculations.
