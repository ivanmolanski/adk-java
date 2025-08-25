/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.adk.web.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {

  private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

  @Value("${firebase.project-id:}")
  private String projectId;

  @Value("${firebase.service-account-key:}")
  private String serviceAccountKey;

  @PostConstruct
  public void initializeFirebase() {
    try {
      if (FirebaseApp.getApps().isEmpty()) {
        GoogleCredentials credentials;

        if (serviceAccountKey != null && !serviceAccountKey.trim().isEmpty()) {
          // Decode base64 service account key
          byte[] keyBytes = Base64.getDecoder().decode(serviceAccountKey);
          credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(keyBytes));
        } else {
          // Use Application Default Credentials
          credentials = GoogleCredentials.getApplicationDefault();
        }

        FirebaseOptions options =
            FirebaseOptions.builder().setCredentials(credentials).setProjectId(projectId).build();

        FirebaseApp.initializeApp(options);
        logger.info("Firebase initialized successfully for project: {}", projectId);
      }
    } catch (IOException e) {
      logger.error("Failed to initialize Firebase", e);
      throw new RuntimeException("Failed to initialize Firebase", e);
    }
  }

  @Bean
  public FirebaseAuth firebaseAuth() {
    return FirebaseAuth.getInstance();
  }

  @Bean
  public Firestore firestore() {
    return FirestoreOptions.getDefaultInstance().getService();
  }
}
