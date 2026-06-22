package com.seto.myapplication

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import top.yukonga.miuix.kmp.basic.Switch as MiuixSwitch
import androidx.compose.material3.Switch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem as MiuixNavigationBarItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import com.seto.myapplication.data.ThemePreferences
import com.seto.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.TextField

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val prefs = getSharedPreferences(
            "settings",
            MODE_PRIVATE
        )

        val savedUseMonet =
            prefs.getBoolean("useMonet", false)

        setContent {

            var useMonet by remember {
                mutableStateOf(savedUseMonet)
            }

            MyApplicationTheme(
                useMonet = useMonet
            ) {

                MyApplicationApp(
                    useMonet = useMonet,

                    onUseMonetChange = { value ->

                        useMonet = value

                        prefs.edit()
                            .putBoolean("useMonet", value)
                            .apply()
                    }
                )
            }
        }
    }
}

@Composable

fun MyApplicationApp(
    useMonet: Boolean,
    onUseMonetChange: (Boolean) -> Unit
) {

    var currentDestination by rememberSaveable {
        mutableStateOf(AppDestinations.HOME)
    }

    val scrollBehavior =
        MiuixScrollBehavior(
            rememberTopAppBarState()
        )

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        topBar = {
            TopAppBar(
                title = currentDestination.label,
                largeTitle = currentDestination.label,
                scrollBehavior = scrollBehavior
            )
        },

        bottomBar = {
            ThemedNavigationBar(
                currentDestination = currentDestination,

                onDestinationSelected = {
                    currentDestination = it
                },

                useMonet = useMonet
            )
        }

    ) { innerPadding ->

        AnimatedContent(
            targetState = currentDestination,

            transitionSpec = {

                val direction =
                    if (targetState.ordinal > initialState.ordinal) {
                        1
                    } else {
                        -1
                    }

                val slideAnimation =
                    tween<IntOffset>(
                        durationMillis = 320,

                        easing = CubicBezierEasing(
                            0.2f,
                            0f,
                            0f,
                            1f
                        )
                    )

                slideInHorizontally(
                    animationSpec = slideAnimation,

                    initialOffsetX = {
                        it * direction
                    }

                ) + fadeIn(

                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )

                ) togetherWith

                        slideOutHorizontally(
                            animationSpec = slideAnimation,

                            targetOffsetX = {
                                -it * direction
                            }

                        ) + fadeOut(

                    animationSpec = tween(
                        durationMillis = 220,
                        easing = LinearOutSlowInEasing
                    )
                )
            },

            label = "page_transition"

        ) { destination ->

            when (destination) {

                AppDestinations.HOME -> {

                    HomeScreen(
                        useMonet = useMonet,

                        modifier = Modifier
                            .padding(innerPadding)
                            .overScrollVertical()
                            .nestedScroll(
                                scrollBehavior.nestedScrollConnection
                            )
                    )
                }

                AppDestinations.FAVORITES -> {

                    FavoritesScreen(
                        modifier = Modifier
                            .padding(innerPadding)
                            .overScrollVertical()
                            .nestedScroll(
                                scrollBehavior.nestedScrollConnection
                            )
                    )
                }

                AppDestinations.PROFILE -> {

                    ProfileScreen(
                        useMonet = useMonet,

                        onUseMonetChange = onUseMonetChange,

                        modifier = Modifier
                            .padding(innerPadding)
                            .overScrollVertical()
                            .nestedScroll(
                                scrollBehavior.nestedScrollConnection
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun ThemedNavigationBar(
    currentDestination: AppDestinations,
    onDestinationSelected: (AppDestinations) -> Unit,
    useMonet: Boolean,
    modifier: Modifier = Modifier
) {
    if (useMonet) {
        val miuixColors = MiuixTheme.colorScheme
        NavigationBar(
            modifier = modifier.fillMaxWidth(),
            containerColor = miuixColors.surface,
            contentColor = miuixColors.onSurface
        ) {
            AppDestinations.entries.forEach { destination ->
                NavigationBarItem(
                    icon = {
                        androidx.compose.material3.Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label
                        )
                    },
                    label = {
                        androidx.compose.material3.Text(text = destination.label)
                    },
                    selected = destination == currentDestination,
                    onClick = { onDestinationSelected(destination) },
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        selectedIconColor = miuixColors.primary,
                        selectedTextColor = miuixColors.primary,
                        unselectedIconColor = miuixColors.onSurfaceSecondary,
                        unselectedTextColor = miuixColors.onSurfaceSecondary,
                        indicatorColor = miuixColors.primary.copy(alpha = 0.12f)
                    )
                )
            }
        }
    } else {
        MiuixNavigationBar(
            modifier = modifier.fillMaxWidth()
        ) {
            AppDestinations.entries.forEach { destination ->
                MiuixNavigationBarItem(
                    icon = destination.icon,
                    label = destination.label,
                    selected = destination == currentDestination,
                    onClick = { onDestinationSelected(destination) }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    useMonet: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    var switch2 by remember { mutableStateOf(prefs.getBoolean("switch2", false)) }
    var switch3 by remember { mutableStateOf(prefs.getBoolean("switch3", true)) }
    var switch4 by remember { mutableStateOf(prefs.getBoolean("switch4", false)) }

    // 状态变化时保存
    LaunchedEffect(switch2) { prefs.edit().putBoolean("switch2", switch2).apply() }
    LaunchedEffect(switch3) { prefs.edit().putBoolean("switch3", switch3).apply() }
    LaunchedEffect(switch4) { prefs.edit().putBoolean("switch4", switch4).apply() }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        // 红色警告卡片 - 未安装
        item {
            val uriHandler = LocalUriHandler.current
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF3D0C0C))
                    .clickable {
                        uriHandler.openUri("https://github.com/SetoSkins/SetoSkins_Thermal/releases")
                    }
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "未安装",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "模块未安装",
                            fontSize = 14.sp,
                            color = Color(0xFFE0E0E0)
                        )
                    }
                    Canvas(modifier = Modifier.size(32.dp)) {
                        val strokeWidth = 2.dp.toPx()
                        val circleColor = Color(0xFFFF4444)
                        drawCircle(
                            color = circleColor,
                            radius = (size.minDimension - strokeWidth) / 2,
                            style = Stroke(width = strokeWidth),
                            center = center
                        )
                        drawLine(
                            color = circleColor,
                            start = Offset(center.x, center.y - 6.dp.toPx()),
                            end = Offset(center.x, center.y + 2.dp.toPx()),
                            strokeWidth = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawCircle(
                            color = circleColor,
                            radius = 1.5.dp.toPx(),
                            center = Offset(center.x, center.y + 7.dp.toPx())
                        )
                    }
                }
            }
        }

        // 绿色状态卡片 - 已激活（莫奈取色）
        item {
            val themeColor = MiuixTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(themeColor.copy(alpha = 0.1f))
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "已激活",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "R1.8",
                            fontSize = 14.sp,
                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                        )
                    }
                    Canvas(modifier = Modifier.size(40.dp)) {
                        val strokeWidth = 2.5.dp.toPx()
                        drawArc(
                            color = themeColor,
                            startAngle = 90f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            size = Size(size.width - strokeWidth, size.height - strokeWidth),
                            topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                        )
                        val checkPath = Path().apply {
                            moveTo(center.x - 5.dp.toPx(), center.y)
                            lineTo(center.x - 1.dp.toPx(), center.y + 4.dp.toPx())
                            lineTo(center.x + 6.dp.toPx(), center.y - 4.dp.toPx())
                        }
                        drawPath(
                            path = checkPath,
                            color = themeColor,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }
            }
        }

        item {
            SmallTitle(
                text = "配置",
                modifier = Modifier
                    .offset(x = (-12).dp)
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        // 高度减 8dp，文字位置不变，上方空隙缩小
                        layout(placeable.width, placeable.height - 8.dp.roundToPx()) {
                            placeable.place(0, 0)
                        }
                    }
            )
        }

        // 开关选项卡片（4个开关，前2个带tips）
        // 开关卡片
        item {
            MiuixCard {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    BasicComponent(
                        title = "简洁版配置",
                        summary = "目前无法更改",
                        endActions = {
                            ThemedSwitch(
                                checked = true,
                                onCheckedChange = null,
                                enabled = false,
                                useMonet = useMonet
                            )
                        }
                    )
                    BasicComponent(
                        title = "快充模式",
                        endActions = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (switch2) "True" else "False",
                                    fontSize = 17.sp,
                                    color = if (switch2) MiuixTheme.colorScheme.primary
                                    else MiuixTheme.colorScheme.onSurfaceSecondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                ThemedSwitch(
                                    checked = switch2,
                                    onCheckedChange = null,
                                    useMonet = useMonet
                                )
                            }
                        },
                        onClick = {
                            switch2 = !switch2
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                    BasicComponent(
                        title = "温控空挂载模式",
                        summary = "非必要建议不开启此选项",
                        endActions = {
                            ThemedSwitch(
                                checked = switch3,
                                onCheckedChange = null,
                                useMonet = useMonet
                            )
                        },
                        onClick = { 
                            switch3 = !switch3
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                    var currentValue by rememberSaveable {
                        mutableStateOf("")
                    }

                    Column {

                        BasicComponent(
                            title = "修改最大电流数",

                            endActions = {
                                ThemedSwitch(
                                    checked = switch4,
                                    onCheckedChange = null,
                                    useMonet = useMonet
                                )
                            },

                            onClick = {

                                switch4 = !switch4

                                hapticFeedback.performHapticFeedback(
                                    HapticFeedbackType.LongPress
                                )
                            }
                        )

                        AnimatedVisibility(
                            visible = switch4,

                            enter = fadeIn() + expandVertically(),

                            exit = fadeOut() + shrinkVertically()
                        ) {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        bottom = 12.dp
                                    )
                            ) {

                                Spacer(
                                    modifier = Modifier.height(10.dp)
                                )

                                if (useMonet) {

                                    OutlinedTextField(
                                        value = currentValue,

                                        onValueChange = {
                                            currentValue = it
                                        },

                                        modifier = Modifier.fillMaxWidth(),

                                        label = {
                                            Text("输入电流值")
                                        },

                                        singleLine = true,

                                        shape = RoundedCornerShape(16.dp),

                                        colors = OutlinedTextFieldDefaults.colors(

                                            focusedBorderColor =
                                                MiuixTheme.colorScheme.primary,

                                            focusedLabelColor =
                                                MiuixTheme.colorScheme.primary,

                                            cursorColor =
                                                MiuixTheme.colorScheme.primary,

                                            focusedTextColor =
                                                MiuixTheme.colorScheme.onSurface
                                        )
                                    )

                                }  else {

                                    TextField(
                                        value = currentValue,

                                        onValueChange = {
                                            currentValue = it
                                        },

                                        modifier = Modifier.fillMaxWidth(),

                                        label = "输入电流值",

                                        singleLine = true
                                    )

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

@Composable
fun FavoritesScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {

            SectionTitle {

                SmallTitle(
                    text = "配置"
                )
            }
        }
        item {
            MiuixCard {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无收藏内容",
                        fontSize = 14.sp,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun ProfileScreen(
    useMonet: Boolean,
    onUseMonetChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    val hapticFeedback = LocalHapticFeedback.current

    var showThemeSheet by remember {
        mutableStateOf(false)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),

        contentPadding = PaddingValues(16.dp),

        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {

            MiuixCard {

                BasicComponent(
                    title = "界面风格",

                    summary =
                        if (useMonet) {
                            "Material You 动态颜色"
                        } else {
                            "MIUIX 主题风格"
                        },

                    endActions = {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text =
                                    if (useMonet) {
                                        "Monet"
                                    } else {
                                        "MIUIX"
                                    },

                                color = MiuixTheme.colorScheme.primary,

                                fontSize = 16.sp
                            )

                            Spacer(
                                modifier = Modifier.width(4.dp)
                            )

                            Icon(
                                imageVector =
                                    Icons.Rounded.KeyboardArrowRight,

                                contentDescription = null,

                                tint =
                                    MiuixTheme.colorScheme
                                        .onSurfaceSecondary
                            )
                        }
                    },

                    onClick = {

                        hapticFeedback.performHapticFeedback(
                            HapticFeedbackType.LongPress
                        )

                        showThemeSheet = true
                    }
                )
            }
        }
    }

    if (showThemeSheet) {

        ModalBottomSheet(
            onDismissRequest = {
                showThemeSheet = false
            },

            containerColor =
                MiuixTheme.colorScheme.surface
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp)
            ) {

                Text(
                    text = "选择界面风格",

                    fontSize = 24.sp,

                    fontWeight = FontWeight.Bold,

                    color = MiuixTheme.colorScheme.onSurface,

                    modifier = Modifier.padding(
                        horizontal = 24.dp,
                        vertical = 20.dp
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()

                        .clickable {

                            hapticFeedback.performHapticFeedback(
                                HapticFeedbackType.LongPress
                            )

                            onUseMonetChange(false)

                            showThemeSheet = false
                        }

                        .padding(
                            horizontal = 24.dp,
                            vertical = 18.dp
                        ),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    RadioButton(
                        selected = !useMonet,

                        onClick = null,

                        colors = RadioButtonDefaults.colors(
                            selectedColor =
                                MiuixTheme.colorScheme.primary,

                            unselectedColor =
                                MiuixTheme.colorScheme
                                    .onSurfaceSecondary
                        )
                    )

                    Spacer(
                        modifier = Modifier.width(18.dp)
                    )

                    Column {

                        Text(
                            text = "MIUIX",

                            fontSize = 18.sp,

                            color =
                                MiuixTheme.colorScheme.onSurface
                        )

                        Spacer(
                            modifier = Modifier.height(2.dp)
                        )

                        Text(
                            text = "使用 MIUIX 风格",

                            fontSize = 14.sp,

                            color =
                                MiuixTheme.colorScheme
                                    .onSurfaceSecondary
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()

                        .clickable {

                            hapticFeedback.performHapticFeedback(
                                HapticFeedbackType.LongPress
                            )

                            onUseMonetChange(true)

                            showThemeSheet = false
                        }

                        .padding(
                            horizontal = 24.dp,
                            vertical = 18.dp
                        ),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    RadioButton(
                        selected = useMonet,

                        onClick = null,

                        colors = RadioButtonDefaults.colors(
                            selectedColor =
                                MiuixTheme.colorScheme.primary,

                            unselectedColor =
                                MiuixTheme.colorScheme
                                    .onSurfaceSecondary
                        )
                    )

                    Spacer(
                        modifier = Modifier.width(18.dp)
                    )

                    Column {

                        Text(
                            text = "Material You",

                            fontSize = 18.sp,

                            color =
                                MiuixTheme.colorScheme.onSurface
                        )

                        Spacer(
                            modifier = Modifier.height(2.dp)
                        )

                        Text(
                            text = "使用 Monet 动态颜色",

                            fontSize = 14.sp,

                            color =
                                MiuixTheme.colorScheme
                                    .onSurfaceSecondary
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun MiuixCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        content()
    }
}

@Composable
fun MiuixListItem(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                color = MiuixTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                )
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("温语", Icons.Filled.Home),
    FAVORITES("日志", MiuixIcons.Favorites),
    PROFILE("设置", MiuixIcons.Contacts),
}

@Composable
fun ThemedSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    enabled: Boolean = true,
    useMonet: Boolean
) {
    val colors = MiuixTheme.colorScheme
    if (useMonet) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,

        thumbContent = {
            Icon(
                imageVector = if (checked) {
                    Icons.Rounded.Check
                } else {
                    Icons.Rounded.Close
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },

        colors = SwitchDefaults.colors(

            // 开启
            checkedThumbColor =
                if (isSystemInDarkTheme()) {
                    colors.primaryContainer
                } else {
                    Color.White
                },

            checkedTrackColor =
                if (isSystemInDarkTheme()) {
                    colors.primary

                } else {
                    colors.primary
                },
            checkedIconColor =
                if (isSystemInDarkTheme()) {
                    colors.primary
                } else {
                    colors.primary
                },

            // 关闭（重点）
            uncheckedThumbColor = Color(0xFF7E8785),
            uncheckedTrackColor = Color.Transparent,
            uncheckedBorderColor = Color(0xFF7E8785),
            uncheckedIconColor = Color.White,

            // 禁用
            disabledCheckedThumbColor = Color.White.copy(alpha = 0.5f),
            disabledCheckedTrackColor = colors.primary.copy(alpha = 0.3f),

            disabledUncheckedThumbColor = Color(0xFFBEBEBE),
            disabledUncheckedTrackColor = Color.Transparent,
            disabledUncheckedBorderColor = Color(0xFFD8D8D8),
            disabledUncheckedIconColor = Color.White.copy(alpha = 0.7f)
        ),

        modifier = Modifier.scale(1.02f)
    )
    } else {
        MiuixSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}
@Composable
fun SectionTitle(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .offset(x = (-12).dp)
            .layout { measurable, constraints ->

                val placeable =
                    measurable.measure(constraints)

                layout(
                    placeable.width,
                    placeable.height - 8.dp.roundToPx()
                ) {

                    placeable.place(0, 0)
                }
            }
    ) {
        content()
    }
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

    MyApplicationTheme(
        useMonet = false
    ) {

        MyApplicationApp(
            useMonet = false,
            onUseMonetChange = {}
        )
    }
}