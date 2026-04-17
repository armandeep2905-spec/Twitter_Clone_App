package com.humblesolutions.twitter

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.humblesolutions.twitter.ui.theme.TwitterSecondaryText
import kotlinx.coroutines.launch
import com.airbnb.lottie.compose.*
import androidx.compose.material.icons.filled.Sort
import com.humblesolutions.twitter.ui.theme.AppOrange
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun MainScreen() {

    var sortType by remember { mutableStateOf("time") }
    var showSortMenu by remember { mutableStateOf(false) }
    var isLoadingScreen by remember { mutableStateOf(false) }
    var selectedTweetId by rememberSaveable { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf("") }
    var isLoggedIn by remember { mutableStateOf(false) }
    var tweets by remember { mutableStateOf(listOf<Tweet>()) }
    var newTweet by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var likedTweetIds by remember { mutableStateOf(setOf<String>()) }
    var showSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var highlightedTweetId by remember { mutableStateOf<String?>(null) }
    var lastPostedTime by remember { mutableStateOf<Long?>(null) }
    var showSplash by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    fun loadTweets() {
        isLoading = true
        getTweets { data ->
            tweets = data
            isLoading = false
        }

    }
    if (showSplash) {
        SplashFuelScreen {
            showSplash = false
        }
        return
    }
    if (isLoadingScreen) {

        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(R.raw.paper_plane)
        )

        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = 1   // 🔥 play once fully
        )

        LaunchedEffect(progress) {
            if (progress == 1f) {
                   // small delay
                isLoadingScreen = false
                isLoggedIn = true
                loadTweets()
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            LottieAnimation(
                composition = composition,
                progress = progress,
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Text(
                    text = "Welcome aboard",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "@$username 🚀",
                    style = MaterialTheme.typography.titleMedium,
                    color = AppOrange
                )
            }
        }

        return
    }

    if (!isLoggedIn) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        )
        {
            Text("𝕏", color = Color.Black, style = MaterialTheme.typography.headlineLarge.copy(fontSize=100.sp))
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Enter username") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.DarkGray,
                    unfocusedTextColor = Color.DarkGray,
                    focusedBorderColor = AppOrange
,
                    focusedLabelColor = AppOrange

                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    isLoadingScreen = true
                },
                enabled = username.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = AppOrange
)
            ) {
                Text("Get Started",color=Color.DarkGray)
            }
        }


    } else {
        Scaffold(
            containerColor = Color.White,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)

            }
        ) { paddingValues ->
            LaunchedEffect(showSnackbar) {
                if (showSnackbar) {
                    snackbarHostState.showSnackbar("Tweet posted successfully 🚀")
                    showSnackbar = false
                }
            }

            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                Column{
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        "Sign Out",
                        color = AppOrange
,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = AppOrange
,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                isLoggedIn = false
                                username = ""
                                tweets = emptyList()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    Button(
                        onClick = {
                            loadTweets()
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = AppOrange
),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Refresh")
                    }
                    Box {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    AppOrange
,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { showSortMenu = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {

                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = AppOrange
,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Sort",
                                color = AppOrange
,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {

                            DropdownMenuItem(
                                text = { Text("Latest") },
                                onClick = {
                                    sortType = "time"
                                    showSortMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Most Liked 👍") },
                                onClick = {
                                    sortType = "likes"
                                    showSortMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("A → Z") },
                                onClick = {
                                    sortType = "az"
                                    showSortMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Z → A") },
                                onClick = {
                                    sortType = "za"
                                    showSortMenu = false
                                }
                            )
                        }
                    }


                }
                HorizontalDivider(color = AppOrange.copy(alpha = 0.3f))

                // Tweet input area
                Column(modifier = Modifier.padding(16.dp)) {

                    OutlinedTextField(
                        value = newTweet,
                        onValueChange = { newTweet = it },
                        placeholder = { Text("What's happening?", color = TwitterSecondaryText) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = AppOrange

                        )
                    )

                    Button(
                        onClick = {
                            val tweetText = newTweet
                            newTweet = ""

                            val currentTime = System.currentTimeMillis()
                            lastPostedTime = currentTime   // ✅ store time

                            scope.launch {
                                addTweet(
                                    username = username,
                                    content = tweetText,
                                    onDone = {
                                        loadTweets()   // 🔥 instant refresh after success
                                    }
                                )

                                snackbarHostState.showSnackbar("Tweet posted successfully 🚀")
                            }
                        },
                        enabled = newTweet.trim().isNotBlank(),
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = AppOrange
)
                    ) {
                        Text("Tweet")
                    }

                    if (isLoading == true) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AppOrange,
                                strokeWidth = 5.dp,
                                modifier = Modifier.size(45.dp)
)
                        }
                    } else {
                        val sortedTweets = remember(sortType, tweets) {
                            when (sortType) {

                                "time" -> tweets.sortedByDescending { it.timestamp }

                                "likes" -> tweets.sortedByDescending { it.likeCount }

                                "az" -> tweets.sortedBy { it.username.lowercase().trim() }

                                "za" -> tweets.sortedByDescending { it.username.lowercase().trim() }

                                else -> tweets
                            }
                        }
                        LazyColumn {
                            items(sortedTweets) { tweet ->
                                TweetCard(
                                    tweet = tweet,
                                    isLiked = likedTweetIds.contains(tweet.id),
                                    isHighlighted = false,
                                    onLikeClick = {
                                        val alreadyLiked = likedTweetIds.contains(tweet.id)

                                        likedTweetIds =
                                            if (alreadyLiked) likedTweetIds - tweet.id
                                            else likedTweetIds + tweet.id

                                        updateLike(tweet.id, !alreadyLiked)
                                        loadTweets()
                                    },
                                    onCommentClick = {
                                        selectedTweetId = null        // 🔥 reset first
                                        selectedTweetId = tweet.id    // 🔥 then set
                                    },
                                    onDeleteClick = {
                                        deleteTweet(tweet.id)
                                        loadTweets()
                                    }
                                )
                            }
                        }
                    }



                }}
                if (selectedTweetId != null) {
                    CommentSection(
                        tweetId = selectedTweetId!!,
                        username = username,
                        onClose = {
                            selectedTweetId = null
                            loadTweets()   // ✅ only called when closing comments
                        }
                    )
                }

                }






        }
    }
}
@Composable
fun SplashFuelScreen(onFinish: () -> Unit) {

    val Orange = Color(0xFFFF7A00)

    val scale = remember { Animatable(0.3f) }
    val alpha = remember { Animatable(1f) }
    val colorProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {

        // 🔥 BIG ZOOM (fills screen)
        scale.animateTo(
            targetValue = 12f,   // 👈 FULL SCREEN ZOOM
            animationSpec = tween(1200)
        )

        // 🔥 COLOR CHANGE
        colorProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000)
        )

        // 🔥 FADE OUT
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(400)
        )

        onFinish()
    }

    val animatedColor = lerp(Orange, Color.Black, colorProgress.value)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = "𝕏",
            color = animatedColor,
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            }
        )
    }
}



