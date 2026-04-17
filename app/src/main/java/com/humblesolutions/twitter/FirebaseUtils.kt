package com.humblesolutions.twitter

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


private val db = Firebase.firestore.collection("tweets")


fun addTweet(username: String, content: String, onDone: () -> Unit) {
    db.add(
        mapOf(
            "username" to username,
            "content" to content,
            "likeCount" to 0,
            "timestamp" to System.currentTimeMillis()
        )
    ).addOnSuccessListener {
        onDone()
    }
}
fun getTweets(onResult: (List<Tweet>) -> Unit) {
    val db = Firebase.firestore.collection("tweets")

    db.get().addOnSuccessListener { result ->
        val tweets = result.map { doc ->

            val timestampAny = doc.get("timestamp")

            val timestamp = when (timestampAny) {
                is Long -> timestampAny
                is Double -> timestampAny.toLong()
                is com.google.firebase.Timestamp -> timestampAny.seconds * 1000
                else -> 0L
            }

            Tweet(
                id = doc.id,
                username = doc.getString("username") ?: "",
                content = doc.getString("content") ?: "",
                likeCount = doc.getLong("likeCount")?.toInt() ?: 0,
                timestamp = timestamp
            )
        }
        onResult(tweets)
    }
}
fun updateLike(tweetId: String, isLiked: Boolean) {
    val docRef = Firebase.firestore.collection("tweets").document(tweetId)

    docRef.get().addOnSuccessListener { snapshot ->

        val currentLikes = snapshot.getLong("likeCount") ?: 0

        val newLikes = if (isLiked) {
            currentLikes + 1
        } else {
            maxOf(0, currentLikes - 1)   // prevent negative
        }

        docRef.update("likeCount", newLikes)
    }
}
fun addComment(tweetId: String, username: String, content: String) {
    Firebase.firestore.collection("tweets")
        .document(tweetId)
        .collection("comments")
        .add(
            mapOf(
                "username" to username,
                "content" to content,
                "timestamp" to System.currentTimeMillis()
            )
        )
}

fun getComments(tweetId: String, onResult: (List<Comment>) -> Unit) {
    Firebase.firestore.collection("tweets")
        .document(tweetId)
        .collection("comments")
        .get()
        .addOnSuccessListener { result ->
            val comments = result.map {
                Comment(
                    id = it.id,
                    username = it.getString("username") ?: "",
                    content = it.getString("content") ?: "",
                    timestamp = it.getLong("timestamp") ?: 0L
                )
            }
            onResult(comments)
        }
}
fun deleteTweet(tweetId: String) {
    val db = Firebase.firestore.collection("tweets")

    db.document(tweetId).delete()
}
