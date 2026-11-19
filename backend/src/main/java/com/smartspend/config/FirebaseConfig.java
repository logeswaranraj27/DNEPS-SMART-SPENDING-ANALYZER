package com.smartspend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Server-side Firebase Admin SDK configuration.
 * Automatically checks FIREBASE_CREDENTIALS_PATH or GOOGLE_APPLICATION_CREDENTIALS.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @Value("${firebase.project.id:smartspend-college}")
    private String projectId;

    private boolean isFirebaseInitialized = false;

    @PostConstruct
    public void initializeFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            isFirebaseInitialized = true;
            return;
        }

        try {
            GoogleCredentials credentials = null;

            // 1. Check explicit credentials path
            if (credentialsPath != null && !credentialsPath.trim().isEmpty()) {
                File file = new File(credentialsPath.trim());
                if (file.exists()) {
                    logger.info("Loading Firebase credentials from file: {}", file.getAbsolutePath());
                    try (InputStream is = new FileInputStream(file)) {
                        credentials = GoogleCredentials.fromStream(is);
                    }
                }
            }

            // 2. Check Application Default Credentials if file not provided
            if (credentials == null) {
                try {
                    credentials = GoogleCredentials.getApplicationDefault();
                    logger.info("Loaded Firebase credentials from Google Application Default Credentials");
                } catch (Exception ignored) {
                    // ADC not set
                }
            }

            // 3. Initialize FirebaseApp if credentials were found
            if (credentials != null) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setProjectId(projectId)
                        .build();

                FirebaseApp.initializeApp(options);
                isFirebaseInitialized = true;
                logger.info("Firebase Admin SDK successfully initialized for project: {}", projectId);
            } else {
                logger.warn("==================================================================================");
                logger.warn(" Firebase service account credentials NOT detected!                              ");
                logger.warn(" SmartSpend backend is operating with persistent Local/Dev storage fallback.     ");
                logger.warn(" To enable live Firestore: set FIREBASE_CREDENTIALS_PATH in application.properties");
                logger.warn("==================================================================================");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
        }
    }

    @Bean
    public Firestore firestore() {
        if (isFirebaseInitialized && !FirebaseApp.getApps().isEmpty()) {
            try {
                return FirestoreClient.getFirestore(FirebaseApp.getInstance());
            } catch (Exception e) {
                logger.warn("Could not acquire live Firestore client: {}", e.getMessage());
            }
        }
        return null;
    }

    public boolean isInitialized() {
        return isFirebaseInitialized;
    }
}
