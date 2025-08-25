import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";

const firebaseConfig = {
  apiKey: "AIzaSyAPDjj37OF9fdO2nsq2Qezwea-xGfPJRlA",
  authDomain: "contentforge-ai-ygy25.firebaseapp.com",
  projectId: "contentforge-ai-ygy25",
  storageBucket: "contentforge-ai-ygy25.firebasestorage.app",
  messagingSenderId: "51060608349",
  appId: "1:51060608349:web:12c14f56648ced0ae96cb4"
};

const app = initializeApp(firebaseConfig);
const db = getFirestore(app);

export { db };