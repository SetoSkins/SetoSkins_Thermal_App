package com.setoskins.thermal.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton as MaterialIconButton
import androidx.compose.material3.Text as MaterialText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.setoskins.thermal.R
import com.setoskins.thermal.ui.component.VerticalScrollBar
import com.setoskins.thermal.ui.component.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(top.yukonga.miuix.kmp.interfaces.ExperimentalScrollBarApi::class)
@Composable
fun DonatePage(
    useMonet: Boolean,
    onDismiss: () -> Unit,
    progressProvider: () -> Float,
    modifier: Modifier = Modifier
) {
    val isZh = LocalConfiguration.current.locales.get(0).language == "zh"
    val isDark = isSystemInDarkTheme()
    val colors = MiuixTheme.colorScheme
    val scrollState = rememberScrollState()
    val donateCardShape = remember { RoundedCornerShape(24.dp) }
    val donateCardColor = remember(isDark, useMonet) {
        if (isDark) colors.surfaceVariant.copy(alpha = if (useMonet) 0.82f else 0.78f)
        else colors.surface.copy(alpha = if (useMonet) 0.72f else 0.62f)
    }
    val donateCardBorderColor = remember(isDark, useMonet) {
        if (isDark) colors.primary.copy(alpha = if (useMonet) 0.30f else 0.22f)
        else colors.outline.copy(alpha = if (useMonet) 0.18f else 0.12f)
    }
    val donateCardShadowColor = remember(isDark, useMonet) {
        if (isDark) colors.primary.copy(alpha = if (useMonet) 0.18f else 0.10f)
        else Color.Black.copy(alpha = 0.08f)
    }
    val donateCardModifier = remember(donateCardShape, donateCardBorderColor, donateCardShadowColor, isDark) {
        Modifier
            .fillMaxWidth()
            .shadow(
            elevation = if (isDark) 10.dp else 4.dp,
            shape = donateCardShape,
            clip = false,
            ambientColor = donateCardShadowColor,
            spotColor = donateCardShadowColor
        )
        .border(1.dp, donateCardBorderColor, donateCardShape)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                val transitionProgress = progressProvider()
                // Donation page slides out to the right (0 to screenWidth)
                translationX = (1f - transitionProgress) * size.width
                
                // No scaling or rounding for a clean push transition
                scaleX = 1f
                scaleY = 1f
            }
            .background(if (useMonet) MiuixTheme.colorScheme.background else if (isDark) Color.Black else MiuixTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (useMonet) {
                    MaterialIconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MiuixTheme.colorScheme.onBackground
                        )
                    }
                    MaterialText(
                        text = if (isZh) "捐赠" else "Donate",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.onBackground
                    )
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Back,
                                contentDescription = "Back",
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Text(
                            text = if (isZh) "捐赠" else "Donate",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Normal,
                            color = MiuixTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 26.dp, top = 12.dp, bottom = 4.dp)
                        )
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(16.dp))
                Image(painter = painterResource(id = R.drawable.seto), contentDescription = null, modifier = Modifier.size(80.dp).clip(CircleShape).align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(12.dp))
                Text(text = "SetoSkins", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MiuixTheme.colorScheme.onBackground, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Text(text = if (isZh) "温度调控模块" else "Thermal Control Module", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                Card(modifier = donateCardModifier, cornerRadius = 24.dp, colors = CardDefaults.defaultColors(color = donateCardColor, contentColor = colors.onSurface)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = if (isZh) "支持项目" else "Support the Project", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text(text = if (isZh) "如果你喜欢这个项目，欢迎通过以下方式支持我" else "If you like this project, feel free to support us:", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurfaceSecondary)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = donateCardModifier,
                    cornerRadius = 24.dp,
                    colors = CardDefaults.defaultColors(
                        color = donateCardColor,
                        contentColor = colors.onSurface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        DonateQrImage(
                            title = if (isZh) "微信" else "WeChat",
                            imageRes = R.drawable.weixin,
                            contentDescription = "WeChat QR Code",
                            height = 180.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        DonateQrImage(
                            title = if (isZh) "支付宝" else "Alipay",
                            imageRes = R.drawable.zfb,
                            contentDescription = "Alipay QR Code",
                            height = 260.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
            VerticalScrollBar(
                adapter = rememberScrollBarAdapter(scrollState)
            )
        }
    }
    }
}

@Composable
private fun DonateQrImage(
    title: String,
    imageRes: Int,
    contentDescription: String,
    height: Dp,
    imageOffsetY: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = imageOffsetY)
                .clip(RoundedCornerShape(18.dp))
        )
    }
}