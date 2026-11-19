# SmartSpend — Firebase Cloud Functions Documentation

In the **SmartSpend** system architecture, all authoritative business logic, financial arithmetic, itemized expense verification, budget calculations, and DNEPS AI integrations are implemented in the **Java 21 Spring Boot Backend**.

## Why Java Spring Boot instead of Cloud Functions?
1. **Authoritative Financial Calculations:** All monetary math, net balances, category aggregations, and potential savings estimations are centrally computed in Java, ensuring strict consistency, zero floating-point errors, and zero double-counting.
2. **Secure Token Verification:** The Spring Boot backend intercepts incoming HTTP requests via `FirebaseAuthenticationFilter`, extracts the verified Firebase UID from the decoded ID token, and strictly enforces user data isolation.
3. **Server-Side AI Integration:** DNEPS AI queries are processed through server-side Java services with credentials kept secure in environment variables, preventing client credential leakage.

If future scheduled background maintenance jobs (such as automated monthly archiving or email digests) are required, Firebase Cloud Functions can be added here using Node.js or Python without disrupting the Spring Boot architecture.
