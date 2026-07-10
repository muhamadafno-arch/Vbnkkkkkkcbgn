package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.CallLogEntity
import com.example.data.ChatEntity
import com.example.data.ContactEntity
import com.example.data.MessageEntity
import com.example.data.UserEntity
import com.example.ui.viewmodel.ActiveCallState
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.CallStage
import com.example.ui.viewmodel.NovaChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// COLOR CONFIGS
// ==========================================

val ObsidianCharcoal = Color(0xFF0F1013)
val SlateBackground = Color(0xFF14161B)
val DeepNavyAccent = Color(0xFF1E2330)
val CyberPinkAccent = Color(0xFFFF2A7A)
val MintGreen = Color(0xFF00E676)
val SoftCyanAccent = Color(0xFF00E5FF)
val CardGray = Color(0xFF1E212A)
val BorderGray = Color(0xFF2C3142)

// ==========================================
// 1. AUTH SCREEN
// ==========================================

@Composable
fun AuthScreen(
    viewModel: NovaChatViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.authUiState.collectAsState()
    var isRegister by remember { mutableStateOf(false) }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianCharcoal),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background shapes
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = CyberPinkAccent.copy(alpha = 0.08f),
                radius = 350f,
                center = Offset(size.width * 0.1f, size.height * 0.2f)
            )
            drawCircle(
                color = SoftCyanAccent.copy(alpha = 0.08f),
                radius = 450f,
                center = Offset(size.width * 0.9f, size.height * 0.8f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Nova Logo Canvas Drawing
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .graphicsLayer(scaleX = glowScale, scaleY = glowScale),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val brush = Brush.linearGradient(
                        colors = listOf(CyberPinkAccent, SoftCyanAccent)
                    )
                    drawRoundRect(
                        brush = brush,
                        size = size,
                        cornerRadius = CornerRadius(28f, 28f),
                        style = Stroke(width = 4.dp.toPx())
                    )
                    drawRoundRect(
                        color = CyberPinkAccent.copy(alpha = 0.15f),
                        size = size,
                        cornerRadius = CornerRadius(28f, 28f)
                    )
                }
                Text(
                    text = "N",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontFamily = FontFamily.Serif
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "NovaChat",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Zero-Knowledge Cryptographic Messaging",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardGray),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (isRegister) "Create Account" else "Log In",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input"),
                        leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = "Username icon") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberPinkAccent,
                            unfocusedBorderColor = BorderGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedLabelColor = CyberPinkAccent
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password icon") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberPinkAccent,
                            unfocusedBorderColor = BorderGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedLabelColor = CyberPinkAccent
                        ),
                        singleLine = true
                    )

                    if (isRegister) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Display Name") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Display name icon") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPinkAccent,
                                unfocusedBorderColor = BorderGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedLabelColor = CyberPinkAccent
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Bio / Secure Quote") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = "Bio icon") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPinkAccent,
                                unfocusedBorderColor = BorderGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedLabelColor = CyberPinkAccent
                            ),
                            maxLines = 2
                        )
                    }

                    if (state is AuthUiState.Error) {
                        Text(
                            text = (state as AuthUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    if (state is AuthUiState.Loading) {
                        CircularProgressIndicator(
                            color = CyberPinkAccent,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 8.dp)
                        )
                    } else {
                        Button(
                            onClick = {
                                if (isRegister) {
                                    viewModel.register(username, password, displayName, bio)
                                } else {
                                    viewModel.login(username, password)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("auth_submit_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPinkAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isRegister) "Sign Up" else "Log In",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRegister) "Already have an account? " else "Don't have an account? ",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
                Text(
                    text = if (isRegister) "Log In" else "Sign Up",
                    color = SoftCyanAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            isRegister = !isRegister
                            viewModel.changeAuthStage(AuthUiState.Unauthenticated)
                        }
                        .testTag("toggle_auth_mode")
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Security assurance visual
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(BorderGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = "Shield Icon",
                    tint = MintGreen,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Signal E2EE • Argon2 Hashing • TLS 1.3",
                    fontSize = 11.sp,
                    color = MintGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ==========================================
// 2. MAIN CONTAINER SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: NovaChatViewModel,
    currentUser: UserEntity,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showCreateChannelDialog by remember { mutableStateOf(false) }

    val scaffoldColor = if (viewModel.isDarkTheme.collectAsState().value) SlateBackground else MaterialTheme.colorScheme.background

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = scaffoldColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            0 -> "NovaChat"
                            1 -> "Contacts"
                            2 -> "Calls"
                            else -> "My Profile"
                        },
                        fontWeight = FontWeight.Bold,
                        color = if (viewModel.isDarkTheme.collectAsState().value) Color.White else MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(onClick = { viewModel.triggerMockIncomingCall() }) {
                            Icon(
                                Icons.Default.PhoneCallback,
                                contentDescription = "Simulate Incoming Call",
                                tint = CyberPinkAccent
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.toggleTheme() }) {
                        Icon(
                            imageVector = if (viewModel.isDarkTheme.collectAsState().value) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Theme Toggle",
                            tint = if (viewModel.isDarkTheme.collectAsState().value) Color.White else MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (viewModel.isDarkTheme.collectAsState().value) ObsidianCharcoal else MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = if (viewModel.isDarkTheme.collectAsState().value) ObsidianCharcoal else MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val darkTheme = viewModel.isDarkTheme.collectAsState().value
                val selectedColor = CyberPinkAccent
                val unselectedColor = if (darkTheme) Color.Gray else Color.DarkGray

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.Forum else Icons.Outlined.Forum,
                            contentDescription = "Chats"
                        )
                    },
                    label = { Text("Chats") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        unselectedIconColor = unselectedColor,
                        unselectedTextColor = unselectedColor,
                        indicatorColor = selectedColor.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_chats")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.PeopleAlt else Icons.Outlined.PeopleAlt,
                            contentDescription = "Contacts"
                        )
                    },
                    label = { Text("Contacts") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        unselectedIconColor = unselectedColor,
                        unselectedTextColor = unselectedColor,
                        indicatorColor = selectedColor.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_contacts")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.PhoneInTalk else Icons.Outlined.PhoneInTalk,
                            contentDescription = "Calls"
                        )
                    },
                    label = { Text("Calls") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        unselectedIconColor = unselectedColor,
                        unselectedTextColor = unselectedColor,
                        indicatorColor = selectedColor.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_calls")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        unselectedIconColor = unselectedColor,
                        unselectedTextColor = unselectedColor,
                        indicatorColor = selectedColor.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                var fabExpanded by remember { mutableStateOf(false) }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (fabExpanded) {
                        FloatingActionButton(
                            onClick = {
                                fabExpanded = false
                                showCreateChannelDialog = true
                            },
                            containerColor = SoftCyanAccent,
                            contentColor = Color.Black,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = "Create Channel")
                        }
                        FloatingActionButton(
                            onClick = {
                                fabExpanded = false
                                showCreateGroupDialog = true
                            },
                            containerColor = CyberPinkAccent,
                            contentColor = Color.White,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.GroupAdd, contentDescription = "Create Group")
                        }
                    }

                    FloatingActionButton(
                        onClick = { fabExpanded = !fabExpanded },
                        containerColor = CyberPinkAccent,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_create_menu")
                    ) {
                        Icon(
                            imageVector = if (fabExpanded) Icons.Default.Close else Icons.Default.AddComment,
                            contentDescription = "New chat menu"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ChatsListTab(viewModel)
                1 -> ContactsTab(viewModel, currentUser)
                2 -> CallsTab(viewModel)
                else -> SettingsTab(viewModel, currentUser)
            }
        }
    }

    if (showCreateGroupDialog) {
        CreateGroupDialog(
            isChannel = false,
            onDismiss = { showCreateGroupDialog = false },
            onCreate = { title, desc ->
                viewModel.createGroupChat(title, desc, "")
                showCreateGroupDialog = false
            }
        )
    }

    if (showCreateChannelDialog) {
        CreateGroupDialog(
            isChannel = true,
            onDismiss = { showCreateChannelDialog = false },
            onCreate = { title, desc ->
                viewModel.createBroadcastChannel(title, desc, "")
                showCreateChannelDialog = false
            }
        )
    }
}

// ==========================================
// 3. CHATS LIST TAB (with Stories & Search)
// ==========================================

@Composable
fun ChatsListTab(viewModel: NovaChatViewModel) {
    val chats by viewModel.activeChats.collectAsState()
    val isDark = viewModel.isDarkTheme.collectAsState().value

    // Mock stories for visual polish
    val stories = listOf(
        Pair("My Story", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80"),
        Pair("Alice", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80"),
        Pair("Bob", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80"),
        Pair("Support", "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=150&q=80"),
        Pair("Tech News", "https://images.unsplash.com/photo-1614741118887-7a4ee193a5fa?auto=format&fit=crop&w=150&q=80")
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Quick Stories Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isDark) ObsidianCharcoal else Color.LightGray.copy(alpha = 0.15f))
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = "Recent Stories",
                fontSize = 12.sp,
                color = if (isDark) Color.Gray else Color.DarkGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(stories) { story ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { /* Stories simulation */ }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .border(
                                    BorderStroke(
                                        2.dp,
                                        Brush.linearGradient(listOf(CyberPinkAccent, SoftCyanAccent))
                                    ),
                                    CircleShape
                                )
                                .padding(3.dp)
                        ) {
                            AsyncImage(
                                model = story.second,
                                contentDescription = story.first,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = story.first,
                            fontSize = 11.sp,
                            color = if (isDark) Color.White else Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(60.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Active Chats List
        if (chats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ChatBubbleOutline,
                        contentDescription = "No chats",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No Chats Active", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Start a secure conversation from the Contacts tab", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("chat_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(chats) { chat ->
                    ChatItemCard(chat = chat, isDark = isDark, onClick = {
                        viewModel.selectChat(chat)
                    }, onPinToggle = {
                        viewModel.togglePin(chat)
                    }, onArchiveToggle = {
                        viewModel.toggleArchive(chat)
                    })
                }
            }
        }
    }
}

@Composable
fun ChatItemCard(
    chat: ChatEntity,
    isDark: Boolean,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onArchiveToggle: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("chat_item_${chat.chatId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) CardGray else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, if (isDark) BorderGray else Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar with Online Indicator
            Box {
                AsyncImage(
                    model = chat.avatarUrl,
                    contentDescription = chat.title,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )

                // Render secure lock icon for channels vs private chats
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (chat.chatType == "CHANNEL") SoftCyanAccent else CyberPinkAccent)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (chat.chatType == "CHANNEL") Icons.Default.Campaign else Icons.Default.Lock,
                        contentDescription = "Secured key indicator",
                        tint = if (chat.chatType == "CHANNEL") Color.Black else Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = chat.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isDark) Color.White else Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                    Text(
                        text = format.format(Date(chat.lastMessageTime)),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = chat.lastMessageText,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (chat.isPinned) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = "Pinned chat",
                            tint = CyberPinkAccent,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    if (chat.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(CyberPinkAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chat.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action options button
            Box {
                IconButton(onClick = { expandedMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Chat Options",
                        tint = Color.Gray
                    )
                }
                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (chat.isPinned) "Unpin Chat" else "Pin Chat") },
                        onClick = {
                            onPinToggle()
                            expandedMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(if (chat.isArchived) "Unarchive Chat" else "Archive Chat") },
                        onClick = {
                            onArchiveToggle()
                            expandedMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) }
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. CONTACTS TAB
// ==========================================

@Composable
fun ContactsTab(viewModel: NovaChatViewModel, currentUser: UserEntity) {
    val friends by viewModel.friends.collectAsState()
    val pending by viewModel.pendingRequests.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val searchResult by viewModel.searchResult.collectAsState()
    val isSearching by viewModel.isSearchingNetwork.collectAsState()
    val isDark = viewModel.isDarkTheme.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Direct search bar
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.updateSearchQuery(it) },
            label = { Text("Search by User ID (NC-xxxxxx) or Username") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("contact_search"),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberPinkAccent,
                unfocusedBorderColor = BorderGray,
                focusedTextColor = if (isDark) Color.White else Color.Black,
                unfocusedTextColor = Color.LightGray
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (query.isNotEmpty()) {
            Text(
                text = "Secure Network Search Results",
                fontSize = 12.sp,
                color = CyberPinkAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (isSearching) {
                CircularProgressIndicator(
                    color = CyberPinkAccent,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else if (searchResult == null) {
                Text(
                    text = "No user found with ID or Username: $query",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                // Render Search result card
                val res = searchResult!!
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardGray),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = res.avatarUrl,
                            contentDescription = res.displayName,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(res.displayName, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(res.userId, fontSize = 12.sp, color = Color.Gray)
                        }

                        if (res.status == "NONE") {
                            Button(
                                onClick = {
                                    viewModel.addNewContact(res.userId, res.username, res.displayName, res.avatarUrl)
                                    viewModel.updateSearchQuery("")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPinkAccent),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Add Friend", fontSize = 11.sp, color = Color.White)
                            }
                        } else {
                            Text(
                                text = res.status,
                                fontSize = 11.sp,
                                color = MintGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Pending Invitations Section
        if (pending.isNotEmpty()) {
            Text(
                text = "Pending Friend Requests (${pending.size})",
                fontSize = 12.sp,
                color = SoftCyanAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(pending) { request ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardGray),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = request.avatarUrl,
                                contentDescription = request.displayName,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(request.displayName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(request.userId, fontSize = 11.sp, color = Color.Gray)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = { viewModel.acceptRequest(request.userId) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Accept", tint = MintGreen)
                                }
                                IconButton(
                                    onClick = { viewModel.rejectRequest(request.userId) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Cancel, contentDescription = "Reject", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Friends list section
        Text(
            text = "My Contact Book (${friends.size})",
            fontSize = 12.sp,
            color = if (isDark) Color.Gray else Color.DarkGray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (friends.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No secure contacts added yet.\nSearch and add a User ID above!",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(friends) { friend ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectChat(
                                    ChatEntity(
                                        chatId = friend.userId,
                                        chatType = "PRIVATE",
                                        title = friend.displayName,
                                        avatarUrl = friend.avatarUrl,
                                        description = friend.bio
                                    )
                                )
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) CardGray else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, if (isDark) BorderGray else Color.LightGray.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                AsyncImage(
                                    model = friend.avatarUrl,
                                    contentDescription = friend.displayName,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                )
                                if (friend.isOnline) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(MintGreen)
                                            .border(1.5.dp, ObsidianCharcoal, CircleShape)
                                            .align(Alignment.BottomEnd)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = friend.displayName,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color.Black
                                )
                                Text(
                                    text = friend.userId,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = {
                                    viewModel.startCall(
                                        friend.userId,
                                        friend.displayName,
                                        friend.avatarUrl,
                                        "VOICE"
                                    )
                                }) {
                                    Icon(Icons.Default.Phone, contentDescription = "Voice Call", tint = SoftCyanAccent)
                                }
                                IconButton(onClick = {
                                    viewModel.startCall(
                                        friend.userId,
                                        friend.displayName,
                                        friend.avatarUrl,
                                        "VIDEO"
                                    )
                                }) {
                                    Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = CyberPinkAccent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. CALLS LOG TAB
// ==========================================

@Composable
fun CallsTab(viewModel: NovaChatViewModel) {
    val logs by viewModel.callLogs.collectAsState()
    val isDark = viewModel.isDarkTheme.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Call History",
                fontSize = 12.sp,
                color = if (isDark) Color.Gray else Color.DarkGray,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Clear All",
                fontSize = 12.sp,
                color = CyberPinkAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    // Trigger call log clear
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CallMissed,
                        contentDescription = "No calls",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No Call Logs", color = if (isDark) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                    Text("Try calling a contact from the list", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(logs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) CardGray else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, if (isDark) BorderGray else Color.LightGray.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = log.otherUserAvatar,
                                contentDescription = log.otherUserName,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(log.otherUserName, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (log.isOutgoing) Icons.Default.CallMade else Icons.Default.CallReceived,
                                        contentDescription = null,
                                        tint = if (log.callStatus == "MISSED") Color.Red else MintGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "${log.callType} • ${if (log.durationSeconds > 0) "${log.durationSeconds}s" else log.callStatus}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            IconButton(onClick = {
                                viewModel.startCall(
                                    log.otherUserId,
                                    log.otherUserName,
                                    log.otherUserAvatar,
                                    log.callType
                                )
                            }) {
                                Icon(
                                    imageVector = if (log.callType == "VIDEO") Icons.Default.Videocam else Icons.Default.Phone,
                                    contentDescription = "Redial",
                                    tint = SoftCyanAccent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. SETTINGS & HIGH-FIDELITY QR SCREEN
// ==========================================

@Composable
fun SettingsTab(viewModel: NovaChatViewModel, currentUser: UserEntity) {
    val isDark = viewModel.isDarkTheme.collectAsState().value
    var bioText by remember { mutableStateOf(currentUser.bio) }
    var displayName by remember { mutableStateOf(currentUser.displayName) }
    var e2eEnabled by remember { mutableStateOf(currentUser.isE2EEnabled) }

    var showQRDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High fidelity Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardGray else MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, if (isDark) BorderGray else Color.LightGray.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = currentUser.avatarUrl,
                    contentDescription = currentUser.displayName,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, CyberPinkAccent, CircleShape)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(currentUser.displayName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = if (isDark) Color.White else Color.Black)
                Text(currentUser.userId, fontSize = 12.sp, color = CyberPinkAccent, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { showQRDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPinkAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = "Show QR", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("My QR Code Card", fontSize = 12.sp, color = Color.White)
                }
            }
        }

        // Profile Form settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardGray else Color.LightGray.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Edit Secure Identity", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)

                OutlinedTextField(
                    value = displayName,
                    onValueChange = {
                        displayName = it
                        viewModel.updateProfile(displayName, bioText, currentUser.avatarUrl, e2eEnabled)
                    },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberPinkAccent,
                        unfocusedBorderColor = BorderGray,
                        focusedTextColor = if (isDark) Color.White else Color.Black
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = bioText,
                    onValueChange = {
                        bioText = it
                        viewModel.updateProfile(displayName, bioText, currentUser.avatarUrl, e2eEnabled)
                    },
                    label = { Text("Secure Quote / Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberPinkAccent,
                        unfocusedBorderColor = BorderGray,
                        focusedTextColor = if (isDark) Color.White else Color.Black
                    )
                )
            }
        }

        // Security settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardGray else Color.LightGray.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Security & Privacy Defaults", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Double-Ratchet Encryption", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color.White else Color.Black)
                        Text("Force E2E cryptographic verification on all node links", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = e2eEnabled,
                        onCheckedChange = {
                            e2eEnabled = it
                            viewModel.updateProfile(displayName, bioText, currentUser.avatarUrl, e2eEnabled)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberPinkAccent)
                    )
                }
            }
        }

        // Logs & backup simulation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) CardGray else Color.LightGray.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Backup & Recovery Data", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* Simulated recovery backup */ },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Secure Backup", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { /* Simulated restore backup */ },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Decrypted Restore", fontSize = 11.sp)
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.logout() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("logout_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.85f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Logout icon")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Destroy Session Keys (Log Out)", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }

    if (showQRDialog) {
        QRDialog(user = currentUser, onDismiss = { showQRDialog = false })
    }
}

// ==========================================
// 7. CHAT DETAIL SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(viewModel: NovaChatViewModel, currentUser: UserEntity) {
    val chat by viewModel.selectedChat.collectAsState()
    val msgs by viewModel.messages.collectAsState()
    val replyingMsg by viewModel.replyingToMessage.collectAsState()
    val selfDestructSec by viewModel.selfDestructSeconds.collectAsState()
    val isDark = viewModel.isDarkTheme.collectAsState().value

    var messageText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showReactionMenuMessageId by remember { mutableStateOf<String?>(null) }
    var selfDestructExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(msgs.size) {
        if (msgs.isNotEmpty()) {
            listState.animateScrollToItem(msgs.size - 1)
        }
    }

    if (chat == null) return

    Scaffold(
        containerColor = if (isDark) SlateBackground else MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { /* Show chat profile dialog */ }
                    ) {
                        AsyncImage(
                            model = chat!!.avatarUrl,
                            contentDescription = chat!!.title,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(chat!!.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isDark) Color.White else Color.Black)
                            Text(
                                text = if (chat!!.chatType == "CHANNEL") "Broadcast Channel • Closed Node" else "Signal Double-Ratchet Armed",
                                fontSize = 11.sp,
                                color = MintGreen
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.selectChat(null) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to list",
                            tint = if (isDark) Color.White else Color.Black
                        )
                    }
                },
                actions = {
                    if (chat!!.chatType == "PRIVATE") {
                        IconButton(onClick = {
                            viewModel.startCall(
                                chat!!.chatId,
                                chat!!.title,
                                chat!!.avatarUrl,
                                "VOICE"
                            )
                        }) {
                            Icon(Icons.Default.Phone, contentDescription = "Voice Call", tint = SoftCyanAccent)
                        }
                        IconButton(onClick = {
                            viewModel.startCall(
                                chat!!.chatId,
                                chat!!.title,
                                chat!!.avatarUrl,
                                "VIDEO"
                            )
                        }) {
                            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = CyberPinkAccent)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) ObsidianCharcoal else MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Secure Handshake Notification banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepNavyAccent)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = SoftCyanAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "End-to-End Encrypted. Keys verified on-device.",
                    fontSize = 11.sp,
                    color = SoftCyanAccent,
                    fontWeight = FontWeight.Medium
                )
            }

            // Message thread lists
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(msgs) { msg ->
                    val isMyMsg = msg.senderId == currentUser.userId
                    MessageBubble(
                        message = msg,
                        isMine = isMyMsg,
                        isDark = isDark,
                        onReact = { emoji ->
                            viewModel.reactToMessage(msg, emoji)
                            showReactionMenuMessageId = null
                        },
                        onLongClick = {
                            showReactionMenuMessageId = msg.messageId
                        },
                        onDeleteEveryone = {
                            viewModel.deleteMessage(msg, forEveryone = true)
                        },
                        onReply = {
                            viewModel.setReplyingTo(msg)
                        }
                    )
                }
            }

            // Replying to overlay indicator
            if (replyingMsg != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDark) CardGray else Color.LightGray.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Reply, contentDescription = null, tint = SoftCyanAccent)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Replying to ${replyingMsg!!.senderName}", fontSize = 11.sp, color = SoftCyanAccent, fontWeight = FontWeight.Bold)
                        Text(replyingMsg!!.text, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = { viewModel.setReplyingTo(null) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel reply", tint = Color.Gray)
                    }
                }
            }

            // Message Composer Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) ObsidianCharcoal else MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Self destruct timer selector button
                Box {
                    IconButton(onClick = { selfDestructExpanded = true }) {
                        Icon(
                            imageVector = if (selfDestructSec > 0) Icons.Default.Timer else Icons.Default.TimerOff,
                            contentDescription = "Self destruct selection",
                            tint = if (selfDestructSec > 0) CyberPinkAccent else Color.Gray
                        )
                    }
                    DropdownMenu(
                        expanded = selfDestructExpanded,
                        onDismissRequest = { selfDestructExpanded = false }
                    ) {
                        listOf(0L, 5L, 10L, 30L, 60L).forEach { secs ->
                            DropdownMenuItem(
                                text = { Text(if (secs == 0L) "Off" else "${secs} seconds") },
                                onClick = {
                                    viewModel.setSelfDestruct(secs)
                                    selfDestructExpanded = false
                                }
                            )
                        }
                    }
                }

                // Add attachment menu simulation
                IconButton(onClick = {
                    // Send an image mock easily
                    viewModel.sendTextMessage("📷 Media Document attachment link securely encrypted.", "IMAGE", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=400&q=80")
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Attach media", tint = SoftCyanAccent)
                }

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Encrypted message...", fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("message_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberPinkAccent,
                        unfocusedBorderColor = BorderGray,
                        focusedTextColor = if (isDark) Color.White else Color.Black
                    ),
                    singleLine = false,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        if (messageText.trim().isNotEmpty()) {
                            viewModel.sendTextMessage(messageText.trim())
                            messageText = ""
                        }
                    },
                    containerColor = CyberPinkAccent,
                    contentColor = Color.White,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("send_message_button"),
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: MessageEntity,
    isMine: Boolean,
    isDark: Boolean,
    onReact: (String) -> Unit,
    onLongClick: () -> Unit,
    onDeleteEveryone: () -> Unit,
    onReply: () -> Unit
) {
    var showSelfDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        // Reply Preview inside bubble
        if (message.replyToText != null) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .background(BorderGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "↳ ${message.replyToText}",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isMine) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=100&q=80",
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Message Bubble box
            Box(
                modifier = Modifier
                    .combinedClickable(
                        onClick = { showSelfDropdown = true },
                        onLongClick = { onLongClick() }
                    )
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMine) 16.dp else 4.dp,
                            bottomEnd = if (isMine) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (isMine) CyberPinkAccent else if (isDark) CardGray else Color.LightGray.copy(alpha = 0.4f)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .widthIn(max = 260.dp)
            ) {
                Column {
                    if (message.mediaType == "IMAGE") {
                        AsyncImage(
                            model = message.mediaUrl,
                            contentDescription = "Attachment Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Text(
                        text = message.text,
                        color = if (isMine) Color.White else if (isDark) Color.LightGray else Color.Black,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.isSelfDestruct) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = "Self destruct countdown active",
                                tint = if (isMine) Color.White.copy(alpha = 0.8f) else CyberPinkAccent,
                                modifier = Modifier
                                    .size(11.dp)
                                    .padding(end = 2.dp)
                            )
                        }

                        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                        Text(
                            text = format.format(Date(message.timestamp)),
                            fontSize = 10.sp,
                            color = if (isMine) Color.White.copy(alpha = 0.7f) else Color.Gray
                        )
                        if (isMine) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                                contentDescription = "Read receipts status",
                                tint = if (message.isRead) SoftCyanAccent else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }

                // Dropdown menu trigger
                DropdownMenu(
                    expanded = showSelfDropdown,
                    onDismissRequest = { showSelfDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Reply Message") },
                        onClick = {
                            onReply()
                            showSelfDropdown = false
                        },
                        leadingIcon = { Icon(Icons.Default.Reply, contentDescription = null) }
                    )
                    if (isMine) {
                        DropdownMenuItem(
                            text = { Text("Delete For Everyone") },
                            onClick = {
                                onDeleteEveryone()
                                showSelfDropdown = false
                            },
                            leadingIcon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.Red) }
                        )
                    }
                }
            }
        }

        // Reactions visual rendering row
        if (message.reactions.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 2.dp, start = 32.dp, end = 12.dp)
            ) {
                message.reactions.split(";").forEach { item ->
                    val emoji = item.split(",").firstOrNull() ?: ""
                    if (emoji.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(BorderGray, RoundedCornerShape(10.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(emoji, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. REALTIME WEBRTC CALL OVERLAY SCREEN
// ==========================================

@Composable
fun CallOverlayScreen(
    viewModel: NovaChatViewModel,
    call: ActiveCallState
) {
    if (call.callStage == CallStage.IDLE) return

    val infiniteTransition = rememberInfiniteTransition(label = "audio_ripple")
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianCharcoal)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // High fidelity calling elements
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header stats (Bitrate, Codec, Encryption)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BorderGray.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.NetworkWifi, contentDescription = null, tint = MintGreen, modifier = Modifier.size(14.dp))
                    Text("P2P WebRTC: ${call.bitrateKbps} kbps", fontSize = 11.sp, color = MintGreen)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = SoftCyanAccent, modifier = Modifier.size(12.dp))
                    Text("Opus • VP9 • E2EE", fontSize = 11.sp, color = SoftCyanAccent)
                }
            }

            // Remote peer rendering frame
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsating ring visual
                    if (call.callStage == CallStage.RINGING) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .graphicsLayer(scaleX = rippleScale, scaleY = rippleScale)
                                .border(2.dp, CyberPinkAccent, CircleShape)
                        )
                    }

                    AsyncImage(
                        model = call.otherUserAvatar,
                        contentDescription = call.otherUserName,
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(3.dp, CyberPinkAccent, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(call.otherUserName, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White)
                
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when (call.callStage) {
                        CallStage.RINGING -> if (call.isOutgoing) "Ringing secure nodes..." else "Incoming secure call..."
                        CallStage.CONNECTING -> "Handshaking crypto parameters..."
                        CallStage.ACTIVE -> {
                            val mins = call.durationSeconds / 60
                            val secs = call.durationSeconds % 60
                            String.format("HD Connected • %02d:%02d", mins, secs)
                        }
                        CallStage.DISCONNECTED -> "Session Closed"
                        else -> ""
                    },
                    fontSize = 14.sp,
                    color = CyberPinkAccent,
                    fontWeight = FontWeight.Medium
                )
            }

            // Controls actions panel
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Feature state badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { viewModel.toggleNoiseSuppression() }
                            .background(if (call.isNoiseSuppressionEnabled) DeepNavyAccent else BorderGray, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (call.isNoiseSuppressionEnabled) Icons.Default.Hearing else Icons.Default.HearingDisabled,
                            contentDescription = null,
                            tint = if (call.isNoiseSuppressionEnabled) SoftCyanAccent else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Suppression", fontSize = 10.sp, color = Color.White)
                    }

                    Row(
                        modifier = Modifier
                            .clickable { viewModel.toggleEchoCancellation() }
                            .background(if (call.isEchoCancellationEnabled) DeepNavyAccent else BorderGray, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = if (call.isEchoCancellationEnabled) SoftCyanAccent else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Echo Filter", fontSize = 10.sp, color = Color.White)
                    }
                }

                // Interactive calling buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (call.callStage == CallStage.RINGING && !call.isOutgoing) {
                        // Accept Call
                        FloatingActionButton(
                            onClick = { viewModel.acceptCall() },
                            containerColor = MintGreen,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Accept Incoming Call", modifier = Modifier.size(28.dp))
                        }

                        // Decline Call
                        FloatingActionButton(
                            onClick = { viewModel.declineCall() },
                            containerColor = Color.Red,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "Decline Incoming Call", modifier = Modifier.size(28.dp))
                        }
                    } else {
                        // Mute button
                        IconButton(
                            onClick = { viewModel.toggleMute() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(if (call.isMuted) Color.Red else BorderGray, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (call.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute mic",
                                tint = Color.White
                            )
                        }

                        // Hangup button
                        FloatingActionButton(
                            onClick = { viewModel.hangUpCall() },
                            containerColor = Color.Red,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "End Call", modifier = Modifier.size(28.dp))
                        }

                        // Camera Toggle button
                        IconButton(
                            onClick = { viewModel.toggleCamera() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(if (call.isCameraOn) SoftCyanAccent else BorderGray, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (call.isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = "Toggle video camera",
                                tint = if (call.isCameraOn) Color.Black else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. DIALOGS & HELPER CUSTOM CANVAS QR
// ==========================================

@Composable
fun CreateGroupDialog(
    isChannel: Boolean,
    onDismiss: () -> Unit,
    onCreate: (title: String, desc: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardGray),
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isChannel) "Create Secure Broadcast Channel" else "New Decentralized Group",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isChannel) "Channel Name" else "Group Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberPinkAccent,
                        unfocusedBorderColor = BorderGray,
                        focusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberPinkAccent,
                        unfocusedBorderColor = BorderGray,
                        focusedTextColor = Color.White
                    ),
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (title.isNotEmpty()) {
                                onCreate(title, desc)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPinkAccent)
                    ) {
                        Text("Create", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun QRDialog(
    user: UserEntity,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardGray),
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "My Identity Secure QR",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Scan this from another NovaChat app client to establish a direct cryptographic peer handshake instantly.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                // High-Fidelity Custom Canvas QR Drawing
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 3.dp.toPx()
                        val boxSize = size.width

                        // Draw QR Corners anchor indicators
                        // Top-Left anchor
                        drawRoundRect(
                            color = ObsidianCharcoal,
                            topLeft = Offset(0f, 0f),
                            size = Size(50f, 50f),
                            cornerRadius = CornerRadius(8f, 8f),
                            style = Stroke(strokeWidth)
                        )
                        drawRoundRect(
                            color = ObsidianCharcoal,
                            topLeft = Offset(12f, 12f),
                            size = Size(26f, 26f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )

                        // Top-Right anchor
                        drawRoundRect(
                            color = ObsidianCharcoal,
                            topLeft = Offset(boxSize - 50f, 0f),
                            size = Size(50f, 50f),
                            cornerRadius = CornerRadius(8f, 8f),
                            style = Stroke(strokeWidth)
                        )
                        drawRoundRect(
                            color = ObsidianCharcoal,
                            topLeft = Offset(boxSize - 38f, 12f),
                            size = Size(26f, 26f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )

                        // Bottom-Left anchor
                        drawRoundRect(
                            color = ObsidianCharcoal,
                            topLeft = Offset(0f, boxSize - 50f),
                            size = Size(50f, 50f),
                            cornerRadius = CornerRadius(8f, 8f),
                            style = Stroke(strokeWidth)
                        )
                        drawRoundRect(
                            color = ObsidianCharcoal,
                            topLeft = Offset(12f, boxSize - 38f),
                            size = Size(26f, 26f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )

                        // Simulated random QR data blocks using a structured seed loop
                        val rows = 12
                        val cols = 12
                        val blockW = boxSize / cols
                        val blockH = boxSize / rows

                        val random = Random(user.userId.hashCode().toLong())

                        for (r in 0 until rows) {
                            for (c in 0 until cols) {
                                // Skip corner anchor areas
                                val isTopLeft = r < 4 && c < 4
                                val isTopRight = r < 4 && c >= cols - 4
                                val isBottomLeft = r >= rows - 4 && c < 4
                                if (isTopLeft || isTopRight || isBottomLeft) continue

                                // Render random secure noise blocks
                                if (random.nextBoolean()) {
                                    drawRect(
                                        color = ObsidianCharcoal,
                                        topLeft = Offset(c * blockW + 2f, r * blockH + 2f),
                                        size = Size(blockW - 4f, blockH - 4f)
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = user.userId,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberPinkAccent
                )

                Button(
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPinkAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}
