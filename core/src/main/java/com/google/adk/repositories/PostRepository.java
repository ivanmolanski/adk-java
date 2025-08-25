package com.google.adk.repositories;

import com.google.adk.models.SocialMediaPost;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class PostRepository {
    private final Firestore firestore;
    private static final String COLLECTION = "viral_posts";

    public PostRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public void savePost(SocialMediaPost post) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION)
                .document(post.getId())
                .set(post)
                .get();
    }

    public List<SocialMediaPost> getTopPosts(int limit) throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = firestore.collection(COLLECTION)
                .orderBy("engagementRate")
                .limit(limit)
                .get()
                .get();

        return snapshot.getDocuments().stream()
                .map(doc -> doc.toObject(SocialMediaPost.class))
                .collect(Collectors.toList());
    }
}