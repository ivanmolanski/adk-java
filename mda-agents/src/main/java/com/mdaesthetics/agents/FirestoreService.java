package com.mdaesthetics.agents;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.DocumentSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/** Minimal Firestore access layer. */
public class FirestoreService {
  private static final Logger log = LoggerFactory.getLogger(FirestoreService.class);
  private final Firestore db;

  public FirestoreService() {
    String projectId = System.getenv("FIRESTORE_PROJECT_ID");
    FirestoreOptions.Builder builder = FirestoreOptions.getDefaultInstance().toBuilder();
    if (projectId != null && !projectId.isBlank()) {
      builder.setProjectId(projectId);
    }
    this.db = builder.build().getService();
  }

  public Map<String, Object> getDocument(String path) throws ExecutionException, InterruptedException {
    int idx = path.indexOf("/");
    if (idx < 0) throw new IllegalArgumentException("Path must contain collection/document: " + path);
    String[] parts = path.split("/");
    if (parts.length % 2 != 0) throw new IllegalArgumentException("Path must alternate collection/document: " + path);
    var ref = db.collection(parts[0]).document(parts[1]);
    for (int i = 2; i < parts.length; i += 2) {
      ref = ref.collection(parts[i]).document(parts[i + 1]);
    }
    DocumentSnapshot snap = ref.get().get();
    if (!snap.exists()) throw new IllegalArgumentException("Document not found: " + path);
    return snap.getData();
  }
}
