import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";

const firebaseConfig = {
  apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY || "",
  authDomain: process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN || "contentforge-ai-ygy25.firebaseapp.com",
  projectId: process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID || "contentforge-ai-ygy25",
  storageBucket: process.env.NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET || "contentforge-ai-ygy25.firebasestorage.app",
  messagingSenderId: process.env.NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID || "51060608349",
  appId: process.env.NEXT_PUBLIC_FIREBASE_APP_ID || "1:51060608349:web:12c14f56648ced0ae96cb4"
};

const app = initializeApp(firebaseConfig);
const db = getFirestore(app);

export { db };