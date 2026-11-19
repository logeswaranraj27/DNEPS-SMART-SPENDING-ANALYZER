/**
 * SmartSpend - Firebase Client SDK Configuration
 * 
 * Replace placeholders with your Firebase project credentials from:
 * Firebase Console -> Project Settings -> General -> Your apps -> Web app
 */

const firebaseConfig = {
  apiKey: "AIzaSyDwyOBOzTHpkxKa4j3JL81BkjiBP00e-7Y",
  authDomain: "smart-spend-dneps.firebaseapp.com",
  projectId: "smart-spend-dneps",
  storageBucket: "smart-spend-dneps.firebasestorage.app",
  messagingSenderId: "608753031381",
  appId: "1:608753031381:web:c7723c8e9529585b9aea5c"
};

// Check if Firebase credentials are the initial placeholders
const isConfiguredFirebase = () => {
  return firebaseConfig.apiKey && !firebaseConfig.apiKey.includes("REPLACE_ME");
};

// Initialize Firebase
let firebaseApp = null;
let firebaseAuth = null;

try {
  if (typeof firebase !== 'undefined' && isConfiguredFirebase()) {
    firebaseApp = firebase.initializeApp(firebaseConfig);
    firebaseAuth = firebase.auth();
    console.log("Firebase initialized successfully with project:", firebaseConfig.projectId);
  } else {
    console.info("SmartSpend running in Local/Demo Auth mode. Configure firebase-config.js for live Firebase Auth.");
  }
} catch (err) {
  console.warn("Firebase initialization skipped or error:", err.message);
}

window.SmartSpendFirebase = {
  config: firebaseConfig,
  isConfigured: isConfiguredFirebase,
  auth: () => firebaseAuth
};
