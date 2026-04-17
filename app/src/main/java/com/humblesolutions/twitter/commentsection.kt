package com.humblesolutions.twitter


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.humblesolutions.twitter.ui.theme.TwitterSecondaryText
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Alignment
import com.google.firebase.firestore.FirebaseFirestore
import com.humblesolutions.twitter.ui.theme.AppOrange
import com.humblesolutions.twitter.ui.theme.TwitterBorder

@Composable
fun CommentSection(
    tweetId: String,
    username: String,
    onClose: () -> Unit
) {
    var comments by remember { mutableStateOf(listOf<Comment>()) }
    var newComment by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        getComments(tweetId) {
            comments = it
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.95f))
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 🔥 BIG BACK BUTTON
            Box(
                modifier = Modifier
                    .size(40.dp)   // 👈 bigger clickable area
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { onClose() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppOrange
,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 🔥 TITLE
            Text(
                text = "Comments",
                color = Color.Black,
                style = MaterialTheme.typography.titleLarge
            )
        }

        HorizontalDivider(color = TwitterBorder)

        Spacer(modifier = Modifier.height(8.dp))

        // 🔥 COMMENT INPUT (TOP)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = newComment,
                onValueChange = { newComment = it },
                placeholder = { Text("Write a comment...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppOrange
,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    addComment(tweetId, username, newComment)
                    newComment = ""

                    // 🔥 refresh instantly
                    getComments(tweetId) { comments = it }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = AppOrange
)
            ) {
                Text("Post")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔥 COMMENTS LIST BELOW
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(comments.sortedByDescending { it.timestamp }) { comment ->

                val formattedTime = java.text.SimpleDateFormat(
                    "hh:mm a",
                    java.util.Locale.getDefault()
                ).format(java.util.Date(comment.timestamp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(
                            1.dp,
                            AppOrange
.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // LEFT: username
                        Text("@${comment.username}", color = TwitterSecondaryText)

                        // RIGHT: time + delete icon
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Text(
                                formattedTime,
                                color = Color.Black,
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // 🔥 DELETE ICON
                            if (comment.username == username) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable {
                                            deleteComment(tweetId, comment.id)
                                            getComments(tweetId) { comments = it }
                                        }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(comment.content, color = Color.Black)
                }
            }
        }
    }
    }
    
fun deleteComment(tweetId: String, commentId: String) {
    val db = FirebaseFirestore.getInstance()

    db.collection("tweets")
        .document(tweetId)
        .collection("comments")
        .document(commentId)
        .delete()
}