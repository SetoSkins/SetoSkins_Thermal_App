// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 btm_m
//
// 从 HyperChanger **v1.1.3**（https://github.com/ColdP/HyperChanger，Apache-2.0）移植：
// app/src/main/java/btm/m/liquidglass/hook/GlassNavigation.kt 的 HyperFloatingNavigationBar。
// ⚠️ 版本号不能删（2026-11-02 查证）：v1.1.3 是 Apache-2.0，上游后来换成了 AGPL-3.0-or-later；
//    Apache-2.0 不可撤销，本文件按 v1.1.3 的授权使用。
// 改动：去掉与业务无关的 tabIconContent/FontNavigationItem 分支，标签用传进来的 ImageVector；
//      宽度改成参数（2026-10-04 按主题商店 4-tab 底栏截图实测对齐：0.88 屏宽 + 内边距
//      —— 主题商店实测 4dp，用户随后要求改成 3dp；原上游的 floatingScale 缩放整体移除）。
package com.setoskins.thermal.ui.component.liquid

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.shapes.Capsule
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.highlight.BloomStroke
import top.yukonga.miuix.kmp.blur.highlight.Highlight
import top.yukonga.miuix.kmp.blur.highlight.LightPosition
import top.yukonga.miuix.kmp.blur.highlight.LightSource
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.textureBlur
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt
import com.setoskins.thermal.ui.component.animation.Os4DampedDragAnimation

/** 底栏一项：文字 + 图标 + 点击。 */
internal data class NavTab(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

/**
 * 底栏的几何（2026-10-04 用户按官方实测定：胶囊高 170px = 56.7dp、离屏幕底 26dp）。
 *
 * 这里原来用的是 navigationBarsPadding()，位置会跟着系统导航条开关上下跳：
 * 关掉导航条时 inset = 0，胶囊几乎贴到屏幕底；开出来时又被整体抬高一截。
 * 现在换成固定值，底栏位置与导航条是否开启无关；开启时胶囊正好叠在导航条上方，就是要的沉浸效果。
 */
internal val NavBarBottomGap = 26.dp
internal val NavBarPillHeight = 56.7.dp

/**
 * 图标框 / 标签字号 / 图文间距 —— 与系统悬浮底栏**逐像素**对齐。
 *
 * 锚点是同一台设备（1200×2670、密度 3.0）上 HyperOS 文件管理器的悬浮底栏截图，实测：
 *
 * | 量 | 系统 | 我们（改前） |
 * | --- | --- | --- |
 * | 胶囊高 | 162 px = 54 dp | 162 px ✓ |
 * | 图标墨迹 | 61 × 60 px | 57 px（偏小约 5%） |
 * | 标签墨迹高 | 30 px | 34 px（**偏大约 13%**） |
 * | 图标底 → 文字顶 | 23 px | 21 px |
 *
 * 换算：MiuixIcons 的图形在框内约占 83%（28dp 框实测 57px 墨迹），所以
 * `24dp 框 → 墨迹 ≈ 20dp = 60px`；而 MiSans 的汉字墨迹高约为字号的 0.9 倍，
 * `11sp → 墨迹 ≈ 30px`。间距则是把标签行高上方那点留白算进去之后倒推的。
 *
 * 注意这三个值**不乘任何缩放系数**（上游那个 `floatingScale` 已在 2026-10-04 整体移除：
 * 宽度改成直接对齐主题商店 4-tab 底栏的 0.88 屏宽，内边距固定 3dp（用户要求）。原来的
 * `28.dp * floatingScale`、`15.sp * floatingScale` 写出来是 22.96dp / 12.3sp，
 * 正是上面"偏小 / 偏大"的那两个数）。
 */
private val NavIconBox = 24.dp
private val NavLabelSize = 11.sp

/** 只有文字、没有图标时（`showIcons = false`）用的字号：此时没有图标分担视线，略大一点。 */
private val NavLabelSizeOnly = 14.sp

/**
 * 图标与标签之间。
 *
 * 这个值比"墨迹间隔"小，因为标签的行框上方还有一段留白（MiSans 的行高约 1.3 倍字号，
 * 11sp 的字上下各多出约 6px）；实测出来的墨迹间隔 ≈ 行留白 + 这个值 + 图标框底部余量。
 * 首版 3.5dp 实测 8.76dp、3.0dp 实测 8.38dp（取整跳变），2.5dp 实测 7.24dp，最接近系统的 7.67dp，定这个。
 */
private val NavIconLabelGap = 2.5.dp

/**
 * 图标 + 文字的"光学"垂直微调（2026-10-04 用户要求"保证与选中块居中"）。
 *
 * 布局盒（图标框顶 → 文字行框底）用 `Arrangement.Center` 居中后，行框**底部**留白比顶部多，
 * 视觉上"图标墨迹 + 汉字墨迹"会略偏上；整体下移这么多，让墨迹盒看起来才是真正居中。
 * 真机觉得偏上/偏下就调这个值（正数 = 下移）。
 */
private val NavContentOpticalOffset = 0.25.dp

/**
 * 选中块宽度与"块步进"的比值（4-tab 下实测 280px / 252.67px = 1.108）。
 *
 * 保持这个比值就能让任意 tabCount 下选中块都"压"在相邻格上（块边缘超出相邻图标中心）。
 * 3-tab 时块会相应变宽以保持同样的视觉比例。
 */
private const val NavIndicatorStepRatio = 1.108f

/**
 * 内边距（dp）—— 胶囊内缩量。选中块到胶囊边缘的距离，2026-10-04 用户定 3dp。
 */
private val NavBarInnerPadding = 3.dp

/**
 * 胶囊宽度占屏宽的比例（4-tab 下实测 0.88）。
 * 3-tab 时缩小到 0.72，保持与 4-tab 相同的步进密度感。
 */
private fun navWidthFractionFor(tabCount: Int): Float = when (tabCount) {
    3 -> 0.72f
    4 -> 0.88f
    5 -> 1.00f
    else -> 0.88f
}

/**
 * 根据内宽和 tabCount 计算选中块宽度（px）。
 * 公式：blockWidth = ratio * innerWidth / (tabCount - 1 + ratio)
 * 推导：blockWidth / step = ratio, step = (innerWidth - blockWidth) / (tabCount-1)
 */
private fun calcIndicatorWidthPx(innerWidthPx: Float, tabCount: Int): Float {
    val r = NavIndicatorStepRatio
    return r * innerWidthPx / (tabCount - 1 + r)
}

/**
 * 从屏幕宽度直接算选中块宽度（px）。给 BoxWithConstraints 外部的 LaunchedEffect 用。
 */
private fun calcIndicatorWidthFromScreenPx(
    screenWidthPx: Float,
    widthFraction: Float,
    tabCount: Int,
    innerPaddingPx: Float,
): Float {
    val innerWidthPx = screenWidthPx * widthFraction - innerPaddingPx * 2
    return calcIndicatorWidthPx(innerWidthPx, tabCount)
}

/**
 * 背景模糊半径（dp）—— miuix `textureBlur` 的语义：层内 sigma ≈ 半径(px) × 0.45。
 *
 * **这个值是被"滚动时闪烁"一路逼下来的**，取值有两个硬约束（都来自 miuix 内部机制）：
 *
 * 1. **降采样级别必须低。** miuix 按 sigma 自适应选降采样（`downScaleExpFor(sigma²)`：
 *    σ²<12.6→1×、<90.25→2×、<400→4×、<1945→8×），模糊层就是按 `1/倍率` 录制的。
 *    滚动时内容在动，倍率越高、每个采样点覆盖的屏幕区域越大，整块跳变越明显 —— 观感就是闪。
 *    25dp（density 3.0 → sigma ≈ 33.8 → 8× 降采样）时闪得明显；11dp → sigma ≈ 14.85
 *    → σ² ≈ 220 → **4× 降采样**。miuix 自己的 example（`LiquidGlassNavigationBar`）用的是
 *    `blur(4.dp)`，只有 2× —— 这就是"官方不闪"的原因。
 * 2. **必须避开交叉渐变带。** 相邻两级的边界 `BOUNDARY_SIGMA = [3.55, 9.5, 20, 44.1]`（带宽 ±12%）
 *    上要做两级渲染再混合，落在带里每帧渲两遍、也更容易闪。11dp 的 14.85σ 离 20 那条带
 *    （17.6~22.4）还有约 16% 余量 —— 12dp（16.2σ）余量只剩 8%，13dp 就进带了。
 *
 * 代价：模糊强度只有 25dp 时的一半左右。想要"又强又不闪"，只能换成 miuix example 那套
 * 「小半径 blur + lens() 折射 + vibrancy()」的配方（那是改观感，不是纯调参）。
 */
private const val GlassBlurRadiusDp = 11f

/**
 * 投影：胶囊四周各外扩这么多，再整体做一次高斯模糊 —— 四周均匀、边缘柔。
 *
 * **为什么不用 `graphicsLayer.shadowElevation`**（原来那套 6dp elevation）：那是系统的 elevation
 * 阴影，光源固定在屏幕正上方，投影只出现在**下缘**、上缘几乎没有，做不出"光从看屏幕的方向打过来"
 * 这种四周均等的效果，而且模糊核是系统定的、调不柔。这里改成自绘：一个比胶囊大一圈的胶囊形状
 * 交给 [androidx.compose.ui.draw.blur] 模糊，四周同源同强度、糊度可调。
 * 代价是多一个离屏层（只有胶囊这么大，开销可忽略）。
 */
private val NavBarShadowSpread = 3.dp

/**
 * 投影的模糊半径 —— **这个值决定"过渡区域"有多宽**（从胶囊边缘到阴影完全消失的那段）。
 *
 * 2026-09-28 一路加过来的：12dp（首版自绘）→ 20dp（用户说过渡带偏窄、边缘还能看出"一圈边"）
 * → **28dp**（用户说"再加一点"）。这里的模糊是普通 RenderEffect 高斯（`Modifier.blur`），
 * **不受** miuix `textureBlur` 那套降采样／交叉渐变带的约束（那条只管玻璃本身，见 §3.9 约束 3），
 * 所以可以放心加。
 */
private val NavBarShadowBlur = 28.dp

/**
 * 投影的黑色透明度。2026-09-28 由 0.25 降到 0.18（用户："再稍微降低一下"）；
 * 2026-09-29 再降到 0.13（用户："降到 0.13"）。
 */
private const val NavBarShadowAlpha = 0.13f

/**
 * 点击回弹（2026-10-04 用户要求、对照官方手感；只点击触发、拖动不触发）。
 *
 * **不是整块一起弹，而是两个边缘分开弹**：以 4→2（向左）为例 ——
 * 左缘（前缘）先做"过冲 + 回退"，右缘（后缘）晚 [NavBounceStaggerMs]（50ms）做同样动作；
 * 向右移动则镜像（右缘先、左缘后）。每个边缘的动作时长相同，观感像"物体停下时
 * 前缘先停、后缘跟着停"。边缘格（4→1 / 1→4）时贴边那一侧不能越界，只让另一侧动（立即、无错峰）。
 *
 * 幅度 = **块步进 ×（`1.5%` + `2%` × 跨的格数）**（2026-10-06 用户改）：
 * 跨 1 格 **3.5%（8.84px）**、跨 2 格 **5.5%（13.90px）**、跨 3 格 **7.5%（18.95px）**，
 * 反向同理 —— "跨得越远弹得越猛"。
 *
 * ⚠️ 用户给的这三个数是**等差**（公差 2%），所以系数是 `1.5% + 2%/格`，
 * **不是** "每格 3.5%"（那样跨 3 格会变成 10.5%）。写成"基准 + 斜率"而不是只认那三个点，
 * 是因为跨度取 `|target − from|` 的**连续值**（连点会把 `from` 停在半路，日志里见过
 * `from=2.8921013`），连续值让幅度平滑过渡、不会因为 `round()` 突然跳一档。
 * 跨 3 格那一档和旧值完全相同（18.95px），变大的只有近距两档。
 * 基准是**块步进 252.67px**（沿用 2026-10-04 的改法，更早的基准是"格宽"）。
 *
 * 历程：固定 5%（2026-10-04 五改）→ 固定 3.5%（七改）→ 2.5%/格（2026-10-05）
 * → **1.5% + 2%/格**（2026-10-06）。
 * 时长不变：过冲仍是 [NavBounceRiseStiffness] 弹簧，回退仍是 [NavBounceReturnMs]。
 */
private const val NavBounceBaseAmplitude = 0.015f
private const val NavBouncePerTabAmplitude = 0.02f
private const val NavBounceStaggerMs = 50L

/**
 * 每个边缘脉冲的两段（2026-10-04 用户要求"回退比过冲慢"）。
 *
 * ① **过冲段**：临界阻尼弹簧、硬（`spring(1, 3200)`）—— 快冲到过冲位（到峰 ≈173ms，
 *    峰值处速度自然为 0）。**保留弹簧**：这一段要的就是"弹出去"的物理感。
 * ② **回退段**：**三次贝塞尔 ease-out（先快后慢）** `tween(200, (0, 0, 0.58, 1))` ——
 *    2026-10-05 由 `spring(0.55, 900)` 改来（用户："回弹恢复的动画更柔和，不像现在突一下"）；
 *    2026-10-06 **对齐系统那套**（用户量到系统是"可感知 **0.25s**、**先快后慢**、但差得不多"）——
 *    试完"感觉对了，压到 200ms"，曲线形状原样、整条时间轴快 20%。
 *
 * ⚠️ **为什么回退段必须离开弹簧**（2026-10-05 用曲线数据定位）：
 * `spring(0.55, 900)` 名义 433ms，但**实际位移 90ms 就走完了** —— 60ms 已走 73%、**86ms 过零**，
 * 之后 340ms 全在 0 附近做 ±2.4px 的微小回摆（观感等于没有）。而且它和过冲段**不对称**：
 * 慢悠悠打出去（173ms）、猛一下收回来（90ms）—— 这个不对称正是"突"。
 *
 * **最终形态（2026-10-06）：`tween(200)` + 标准 ease-out `(0, 0, 0.58, 1)`** ——
 * 对齐系统那套"**先快后慢、但差得不多**"（以跨 3 格 18.95px 计）：
 * 25ms 走 **19.9%**、100ms **68.5%**、150ms **90.7%**、200ms 精确到 0；
 * 前 30ms 走 ≈4.5px、末 50ms 走 ≈1.8px。无回摆（最深回摆 -2.39px → 0）。
 * 与前身 `tween(250)` **形状完全相同**，只是整条时间轴快 20%（同 t 处的进度一一对应：
 * 250ms 的 25ms 走 16.1% ≙ 200ms 的 20ms 走 16.1%）。
 *
 * 曲线 / 时长历程（一路试出来的）：
 * `spring(0.55, 900)`（名义 433ms、**实际 90ms 就走完位移**，观感"突"）
 * → `tween(300, (0.37,0,0.63,1))` → `225` → `180` → `tween(250, (0,0,0.58,1))` → **`tween(200)`**。
 * ⚠️ 中途有一版是 **`(0.5, 0, 0.5, 1)`（ease-in-out，慢起手）**，已换掉 ——
 * 它 25ms 只走 **1.5%**，和系统的"快起手"（16~30%）**方向相反**。
 * 要"快起手"就用 ease-out 系，第一个采样点（25ms）分别走：
 * `(0,0,.58,1)` → 16.1% / `(0,0,.4,1)` → 21.1% / `(0,0,.2,1)`（Material LinearOutSlowIn）→ 30.4%。
 *
 * 当时算过、没选的备选：`spring(1, 900)` 临界阻尼（去回摆但起步仍快，30ms 走 23%）；
 * `spring(0.55, 400)`（只放慢 1.5 倍，回摆 -2.39px 原样保留）。
 *
 * 历程：`spring(0.55, 400)`（回零 1.35s 太长）→ `spring(0.55, 900)`（0.6s）→ `tween(300)` → **`tween(200)`**。
 * 2026-10-04 六改：错峰减半（[NavBounceStaggerMs] 150ms → 75ms）；八改：再降到 50ms。
 */
private const val NavBounceRiseDamping = 1f
private const val NavBounceRiseStiffness = 3200f
/** 回退段时长（ms）—— 先快后慢、无回摆（见上一条 KDoc）。 */
private const val NavBounceReturnMs = 200
private val NavBounceReturnEasing = CubicBezierEasing(0f, 0f, 0.58f, 1f)

/**
 * **后缘收回的启动延迟**（ms）—— 前缘开始归位后，隔这么久后缘才开始归位
 * （2026-10-06 用户要求："后端收缩恢复比前端慢，就像后端的动作是前端传导过来的"）。
 *
 * 用**滞后启动**而不是"把时长拉长"：时长不变（仍是 [NavBounceReturnMs]）才会读成
 * "同一个动作晚一点到"，拉长时长会变成"另一个更慢的动作"。
 * 延迟期间后缘停在过冲位置，于是块会短暂变窄一点 —— 那就是"传导"的形变。
 * 贴边格（1↔4）不走这条（那边后缘只走长度缩过头、没有独立的后缘量），
 * 所以收尾循环里的总时长会按它是否在用自动收缩回去。
 */
private const val NavBounceTrailDelayMs = 50L

/**
 * **惯性形变的触发距离**（px）—— 中间格飞行时，块离目标还剩这么多就开始形变，
 * 到位时正好涨到满量程 `overshoot`（见 `updateInertia()`），之后由收尾分支收回。
 *
 * ⚠️ 这个值的意义在于**把形变推到块速降下来之后**，否则看不见：2026-10-06 第一版拿
 * "长度拉伸量"当驱动量，实测形变峰值落在起飞后 ~200ms、那时块速还有 **764px/s**，
 * 而块真正停下（≈360ms）时两个偏移已经衰减到 1px 以内 ⇒ 视觉上"完全没效果"。
 * 注意这个值和块的总行程无关（跨 1 格 252px / 跨 2 格 505px 都用它）。
 *
 * 取 20px（2026-10-06 从 40 收紧）：形变的两个峰分别落在 `p = 0.35 / 0.85`，
 * 对应"块还剩 13px / 3px"—— 块速约 **190 / 55 px/s**，都在低速区。
 * 40px 时前缘峰落在块速 ≈400px/s，仍然偏快、不容易看清。
 */
private const val NavInertiaRangePx = 20f

/**
 * **后缘（惯性内缩）的触发距离**（px）—— 比前缘的 [NavInertiaRangePx] 小，所以**更晚开始**，
 * 得到"前缘先顶出去、后缘过一会儿才被带进来"的传导感（2026-10-06 用户要求：
 * "后端的动作是前端传导过来的"）。
 *
 * 实测跨 2 格时，块从剩 20px 走到剩 10px 约 **50ms** —— 这就是内缩段后缘的滞后量。
 */
private const val NavInertiaRangeTrailPx = 10f

/**
 * 回弹的**等待上限**（ms）—— 保护 `waitStretchHandoff()` / `waitGlideHandoff()`：
 * 万一条件因为某种原因一直不满足（effect 被打断、状态异常…），也不能把回弹永远等掉，
 * 到点就照踢。
 *
 * 正常需要的等待时间：飞行结束（`animateValueTo` 跑到阈值 ≈0.46s）+ 长度收到交接区
 * ≈ **0.47s**；滑页路径是块滑到剩余 8px ≈ **0.18s**。900 留了余量。
 * 真等超时也只是回弹早踢一点，不会丢。
 */
private const val NavBounceStretchWaitMs = 900L

/**
 * **长度交接阈值**：长度系数（0 ~ `NavStretchMax`）降到 `NavStretchMax` 的 **6%** 就踢回弹，
 * **不等它精确归零**。
 *
 * 为什么要留残量（2026-10-05 用户："回弹和恢复间还会顿一下，希望视觉上连续"）：
 * 严格串行会把两个**速度谷**首尾接起来 —— 长度回落是 ease-out，**末端速度趋近 0**
 * （"平滑停在终点"的数学必然）；回弹过冲是 `spring(1, 3200)`，**起手从 0 加速**
 * （前 16.7ms 只走 2.16px）。两段一接，中间就是 ~45ms 的"几乎不动" ⇒ 肉眼读作"顿一下"。
 *
 * 6% 残量 = 屏上长度差 `0.25 × 0.06 × 256.2 ≈ 3.8px`（单侧 1.9px，基本不可辨），
 * 却能让长度收尾的减速尾和回弹的加速头**重叠**：踢出时回弹已能走 4.38px。
 * 实测提前量 **29.4ms**（ease-out `(0,0,0.58,1)` 0.15s：剩 6% 出现在时间进度 0.804 处）。
 *
 * 6% 不会让回弹又被"淹没"：当初看不见是因为它被 252.67px 的**飞行位移**盖住，
 * 而这里块已经基本到位了。但也不能取大 —— 剩 30% 时残量 19.2px，那才会真盖住。
 *
 * ⚠️ **这个阈值才是"顿"的主瓶颈**（2026-10-05 二次优化时算出来的）：跨 3 格时块在 **329ms**
 * 就剩 8px，但块速已跌到 139px/s（≈静止），而长度还在 `NavStretchFallDuration` 的 ease-out
 * 尾段里慢慢收 —— `飞行启动 → 回弹踢` 那 424ms 里 **95ms 全花在等它**。
 *
 * ⚠️ 单位是**长度系数**、不是百分比：满量程是 `NavStretchMax = 0.25f`，所以 12% 要写成
 * **`0.03f`**（= 0.25 × 0.12，屏上 7.7px 全长差 / 单侧 3.8px）。
 * 历程：`0.06f`（第一版写错，那是满量程 24%）→ `0.015f`（= 6%）→ **`0.03f`**。
 * （这里不能写 `NavStretchMax * 0.12f`：`const val` 不能前向引用。）
 *
 * 上限：残量不宜超过单侧 4~6px（回弹幅度是 8.84px），再大就会和回弹互相干扰。
 *
 * 滑页路径没有拉伸（`stretchNow` 恒为 0）→ 任何正阈值都立即通过。
 */
private const val NavStretchHandoffRemain = 0.03f

/**
 * 滑页路径的**回弹接手阈值**（px，块的剩余位移）：块离目标还剩这么点就踢回弹。
 *
 * 滑页那条走 `glideTo`（`spring(1f, 1000f)`，挂起到收敛）—— 它从 0.15s（95%）到 0.29s
 * （收敛）这 140ms 里只走最后 12.6px（跨 1 格），块速已从 330px/s 掉到 70px/s，
 * 然后回弹再从 0 起步 ⇒ 谷比飞行路径更长（用户 2026-10-05 报的"手指滑页时回弹慢半拍"）。
 *
 * **历程：8px → `16f`**（2026-10-05 二次优化）。8px 的立意是"剩余位移和回弹幅度同量级"
 * （回弹幅度 8.84px），但**太晚**：跨 3 格要到 **329ms** 才满足，那时块速已经跌到
 * **139px/s（≈静止）** —— 回弹只能在"块停住之后"才开始，于是仍有"顿"感。
 *
 * 16px 实测：跨 3 格 **289ms** 就满足，块速还有 **273px/s**（在明显移动）—— 回弹和块的收尾
 * **重叠**，从根上消掉"块停了才弹"。跨 1 格是 223ms / 261px/s，同样不会被淹没。
 *
 * 用**绝对像素**而不是比例 —— 跨 3 格时块速按比例放大 3 倍，比例阈值会让回弹被块的
 * 剩余运动盖住。再往大（20~24px）收益递减、且要同步放宽长度阈值，别乱加。
 */
private const val NavBounceHandoffPx = 16f

/** 临时调试日志 TAG（2026-10-05 排查"回弹看不见"，定位完就删）。 */
private const val NAV_TAG = "HyperExtremeNav"

/**
 * 「按下标签 → 块飞过去」用的弹簧（2026-10-05 用户定："只改 A"）。
 *
 * ⚠️ **不能复用 `DampedDragAnimation.valueAnimationSpec`**（`spring(1f, 1000f)`）—— 那是
 * **拖动跟手**共用的：拖动时块必须快速贴住手指，把它调软会让拖动发黏。所以这里单独给
 * 标签这条路一条更软的弹簧。
 *
 * 为什么需要调软：上游（和我们的"改造前"）是**抬手才动** —— 宿主 View 的 `onClick` 触发
 * `onTabSelected` 才改 `selectedIndex`。现在是"**按下即飞**"，等于把那次按压（≈0.1s）从
 * "触屏 → 块到位"的总时间里省掉了，所以同样的弹簧会显得更急。
 *
 * | stiffness | 到 95% | 说明 |
 * | --- | --- | --- |
 * | 1000 | ≈0.15s | 上游原值；拖动跟手仍用它（`valueAnimationSpec`） |
 * | **400（本条）** | **≈0.24s** | 补回那次按压，"触屏 → 到位" ≈ 改造前的 0.25s |
 * | 250 | ≈0.30s | 更慢一档，备用 |
 *
 * 临界阻尼（`ζ = 1`）的收敛时间 ∝ `1/√k`：`t95 ≈ 4.74 / √k`。
 * 时间与**跨几格无关**（线性系统，跨界只按比例缩放幅度）—— 跨 3 格时中段瞬时速度是
 * 3 倍，但时长一模一样。
 *
 * 历程：上游/改造前是 `spring(1f, 1000f)`（抬手才动）→ 试过 `tween(350)` → `tween(100)`
 * → `tween(150)` → 用户明确"**改弹簧**"回到 `spring(1f, 1000f)` → 又觉得"还是太快"
 * → 用户定"**只改 A**"：给飞行单独调软到本条 `spring(1f, 400f)`。
 *
 * 交互本身是"**按下即飞、离开标签才切页**"：手指落到标签上那一刻块就起飞，页面**不动**；
 * 手指离开（松开或滑出）才调 `onTabSelected` 去翻页。所以这条动画是**纯视觉**的，
 * 和页面切换的节奏解耦。
 *
 * ⚠️ **2026-10-06 改：飞行时长要与跨度无关，并对齐"页面视觉停住"**（用户拍板方案 B）
 *
 * 用户要的（原话）："无论飞行距离有多少，时长都是一样的，距离更长速度更快是正常的"。
 * 再加一条实测出来的对齐目标：**过冲到顶点的那一刻，要正好落在页面视觉停住时**。
 *
 * 实测（真机 1200px/页，`su -c input swipe` 驱动、日志带 `uptimeMillis`）：
 *
 * | 量 | 值 | 说明 |
 * | --- | --- | --- |
 * | 页面**视觉停住** | 抬手 + **344ms** | 欠阻尼过零 π/ωd，**与跨度无关**（实测 350/359） |
 * | 页面库动画判停 | 抬手 + 551 / 591 / 615 | 后面 200~260ms 只有 ≤2px 残余，看不见 |
 * | 块到过冲顶点（`整体移回`） | 按下 + 490 / 530 / 548 | 旧值 `spring(1f, 400f, 0.001f)` |
 *
 * 旧值的问题有两层：
 * 1. **顶点比页面视觉停住更晚**（按住 80ms 时晚 55ms）—— 这正是用户报的"不同步"。
 *    注意 **顶点时刻与按住时长无关**（恒 ≈按下+490），而页面是**抬手才起步** ⇒
 *    差 = 按住时长 + (490 − 344)。按得越快，顶点就越是落在页面停住之后。
 * 2. **时长随跨度长**（490 / 530 / 548）：弹簧的终止判据是 |剩余| < 阈值，
 *    而剩余是**格**为单位的，跨度越大、走完固定比例就越久。
 *
 * **修法 = 阈值按跨度缩放**：`visibilityThreshold = NavTabFlyThreshold × 跨度`。
 * 临界阻尼下 `(1 + u)·e^−u = 阈值 / 跨度`，阈值 ∝ 跨度 ⇒ 右边恒定 ⇒ `u` 恒定 ⇒
 * **收敛时间与幅度完全无关**（`t = u / √k`）。弹簧形状一字未改，只是"什么时候算到位"
 * 跟着幅度走 —— 于是跨 1 / 2 / 3 格的飞行时长变成同一个数。
 *
 * **刚度**按"顶点落在页面视觉停住"反解：`T = 按住时长 + 344`，且 `T ∝ 1/√k`
 * ⇒ `k = 400 × (490 / T)²`。名义按住时长取 **80ms** ⇒ `T = 424` ⇒ `k ≈ 535`。
 *
 * | 按住时长 | 需要的 T | 需要的 k |
 * | --- | --- | --- |
 * | 60ms | 404 | 588 |
 * | **80ms（当前取值）** | **424** | **535** |
 * | 100ms | 444 | 487 |
 * | 135ms | 479 | 419（≈旧值 400） |
 *
 * ⚠️ 按住时长是**结构性**变量（块在按下起飞、页在抬手才滚），固定弹簧只能对准一个名义值；
 * 想任意时长都严格重合，得在抬手时重排块的剩余动画（会换掉曲线，用户没选）。
 *
 * ⚠️ 只作用于**点击飞行**这条路。拖动跨页 / 手指滑页走 `glideTo`（`valueAnimationSpec`
 * = `spring(1f, 1000f)`）——那条是拖动跟手共用的，不动，见 `DampedDragAnimation`。
 *
 * 历程：上游/改造前是 `spring(1f, 1000f)`（抬手才动）→ 试过 `tween(350)` → `tween(100)`
 * → `tween(150)` → 用户明确"**改弹簧**"回到 `spring(1f, 1000f)` → 又觉得"还是太快"
 * → 用户定"**只改 A**"：给飞行单独调软到 `spring(1f, 400f)`（2026-10-05）
 * → **按跨度缩放阈值 + 刚度 400 → 535**（2026-10-06）。
 */
private const val NavTabFlyStiffness = 535f

/**
 * 飞行弹簧的**阈值基数**（单位 = 格）：实际阈值 = 本值 × 本次飞行的跨度。
 *
 * 除以跨度得到的是"相对幅度还剩多少才算到位"——固定它，收敛时间就与幅度无关。
 * 0.001 是沿用旧值在跨 1 格时的阈值（`spring(1f, 400f, 0.001f)`），
 * 所以跨 1 格的时长、曲线、手感与旧版**逐帧一致**，变的只有跨 2 / 3 格（变短）。
 */
private const val NavTabFlyThreshold = 0.001f

/**
 * 按本次飞行的**跨度**（单位格，= |target − from|）生成飞行弹簧。
 *
 * 跨度取连续值（连点会把 `from` 停在半路）；下钳 0.05 是防 `animateTo` 拿到 0 阈值 ——
 * 那种情况本身不会动（首末值相同），只是不想让阈值退化成非法值。
 */
private fun navTabFlySpec(span: Float) =
    spring(1f, NavTabFlyStiffness, NavTabFlyThreshold * span.coerceAtLeast(0.05f))

/**
 * 按压拖动时的「速度 → 长度」拉伸（2026-10-05 用户定稿）。
 *
 * 按住块拖动时，块的长度按**自身的瞬时位移速度** `|v|`（px/s）线性拉长：
 * `stretch = clamp((|v| − v0) / (v1 − v0), 0, 1) × NavStretchMax`，
 * 宽度走**真实布局尺寸**（`Modifier.layout`、不是缩放 —— 见选中块那两层的说明）。
 *
 * **长度只加在"运动方向的反侧"**（2026-10-05 用户改，原为居中）：速度朝右 → **右缘钉死**、
 * 全往左长（拖尾），朝左镜像。钉死端 = 拉伸不改变它的屏幕位置（它当然还是跟着块一起移动）。
 * 方向是 −1~+1 的连续量（`dirNow`），手指掉头时钉死端平滑滑过去、不会一下跳。
 * 两端各自"**顶到 gap 线就停**" —— **边界检查永远优先**，所以贴边格朝外拖时那一端干脆
 * 不伸（1 号位向右拖 → 块完全不伸长）。
 *
 * 长度：256.2px（100%）→ **320.25px（125%）**。
 *
 * | 参数 | 值 | 说明 |
 * | --- | --- | --- |
 * | `NavStretchV0` | 150 px/s | 起拉门槛：低于它长度保持 100%（≈ 每秒 0.59 格） |
 * | `NavStretchV1` | 900 px/s | 满量程：到它封顶 125%（≈ 每秒 3.56 格） |
 * | `NavStretchMax` | 0.25f | 最大增量（125% − 100%） |
 *
 * v0 / v1 之间是 750 px/s（≈ 3 格/秒），**长度对速度极其敏感** —— 逐帧速度估计的
 * 噪声会被一比一放大成长度抖动，所以必须过 [NavStretchTauDrag] 的平滑。
 *
 * ⚠️ 速度取的是**块的实际位移速度**（`d(dragAnimation.value)/dt × 块步进`），不是手指速度。
 * 两者在跟手时相等；**块贴住边界、手指继续推时块速 = 0**（`updateValue` 里被 `coerceIn`
 * 截断），于是拉伸自动不参与，只剩 §「边界越界挤压」那套 —— 这正是需求 A / B 不冲突的原因。
 */
private const val NavStretchV0 = 150f
private const val NavStretchV1 = 900f
private const val NavStretchMax = 0.25f

/**
 * 速度 → 长度的映射曲线（三次贝塞尔）—— 2026-10-05 用户要求把线性换成"低速增长慢、高速增长快"。
 *
 * 横轴 = 归一化速度 `clamp((|v| − V0) / (V1 − V0), 0, 1)`，纵轴 = 长度系数（`0 ~ NavStretchMax`）。
 * 现在用**标准 ease-in** `(0.42, 0, 1, 1)` —— 2026-10-05 试了四版后的定稿
 * （线性 → ease-in → `(0.55,0,0.85,1)` → `(0.55,0,1,0.65)` → 回到 ease-in，用户认为它最符合）。
 * `t` 走到 0.25 / 0.5 / 0.75 时输出 **0.093 / 0.315 / 0.622**。
 *
 * 它和"幂 1.5"（`t^1.5`）**几乎等价**：幂 1.5 是 0.125 / 0.354 / 0.650，最大差 0.039、
 * 换算到屏上不到 2.6px。选贝塞尔是因为它起点切线水平（低速区更"躺平"）、
 * 而且参数是标准语汇、别的动画能复用同一条。
 *
 * 调参口诀（四个数 = 两个控制点 P1/P2，起点 (0,0) 与终点 (1,1) 固定）：
 * * `x1` —— 前半段"躺平"程度，越大低速走得越慢，**主旋钮**（0.4 ~ 0.75）。
 *   实测（`x2 = 1, y2 = 1` 时）：0.42 → `t=0.5` 走 0.315；0.55 → 0.256；0.65 → 0.214；0.75 → 0.177。
 * * `y1` —— 起步初速，**保持 0**：原点切线水平，低速区才真正躺平。
 * * `x2` —— 后半段过渡位置，**越小"猛涨"来得越早、后段收口越快**（0.85 ~ 1）。
 * * `y2` —— 收尾。**保持 1**：平滑抵达终点；**< 1 则后段增长放缓**（如 0.65 时 `t=0.75` 只走 0.421）。
 * ⚠️ `y1` / `y2` 超出 [0,1] 会让曲线**非单调**（拉伸量冲过 125%、或"速度越快反而越短"），别碰；
 * `x1` / `x2` 必须在 [0,1] 内，否则抛异常。
 *
 * 做成具名常量是为了后面别的动画也能复用同一条曲线。
 */
private val NavStretchEasing = CubicBezierEasing(0.42f, 0f, 1f, 1f)

/**
 * 长度**上升**时的平滑时间常数（秒）—— 指数逼近：`alpha = 1 − exp(−dt / τ)`。
 *
 * [NavStretchTauDrag] = 60ms（用户 2026-10-05 选"跟手优先"）：抹掉速度估计的高频抖动，
 * 又不明显拖手。**必须做**，否则"手指突然停住但没松手"会让速度从 600 掉到 0、
 * 长度一帧内从约 320.25px 掉回 256.2px —— 一记肉眼可见的大跳。
 *
 * [NavStretchTauFly] = 20ms（2026-10-05 二次优化新增）—— **标签飞行期间**用它替代 60ms。
 * 理由：60ms 是为**拖动**选的抗抖（拖动时速度来自手指、有噪声）；**飞行时块速来自弹簧、
 * 本身就平滑**，不需要这么强的平滑。
 *
 * ⚠️ 它同时是"回弹顿感"的**最后一个瓶颈**（2026-10-05 用帧级模拟定位）：
 * 长度是"先拉满 0.25、再收"，而回弹的条件②要等它收到 `NavStretchHandoffRemain` ——
 * 60ms 的滞后让"块已剩 16px、块速 277px/s"那一刻长度还有 **0.1165**，于是踢出被硬拖到
 * 371ms（实测 395ms）。20ms 压到 **313ms（实测，模拟 300ms）**；15ms 模拟能到 292ms
 * （那是条件①的地板，再小无意义）。附带好处：长度"拉满"从 230ms 缩到 ≈70ms ——
 * 块一冲起来就变长，更有冲劲。
 *
 * ⚠️ **只用于上升**。回落（长度要变小）不再走指数，见 [NavStretchFallDuration]。
 */
private const val NavStretchTauDrag = 0.06f
private const val NavStretchTauFly = 0.02f

/**
 * 长度**回落**（速度归零 → 收到 0）的时长与曲线（秒 / 三次贝塞尔）。
 *
 * 2026-10-05 用户定：**0.15s、标准 ease-out**，形状"先快后慢"。
 * 从指数衰减换过来的 —— 指数其实天然就是先快后慢，但它渐近 0、**没有终点**
 * （3τ 时还差 5% = 屏上 3.2px，那条尾巴要再拖 0.2s 才看不见）；换成带终点的 ease-out 后
 * `t = 1` 精确归零、不留尾。
 *
 * 归一化时间下"已收掉"的比例（方便和旧的指数对比）：
 *
 * | 已过时间 | 20% | 40% | 60% | 80% | 100% |
 * | --- | --- | --- | --- | --- | --- |
 * | ease-out（现在） | 30.8% | 57.1% | 78.5% | 93.8% | **100%** |
 * | 指数 τ=83ms（旧） | 45.1% | 69.9% | 83.5% | 90.9% | 95.0% |
 *
 * 覆盖**两种**触发：松手、以及"手指还按着但速度归零"（判据见 [NavStretchFingerStillSpeed]）。
 * 实现上要记回落起点值和起点时刻（指数那条是无状态的），换来的是精确的无尾终止。
 *
 * 不做弹簧，避免和 `release()` 里那条 `scaleX: 0.915 → 1.0` 的弹簧叠成"先缩后涨"。
 */
private const val NavStretchFallDuration = 0.15f
private val NavStretchFallEasing = CubicBezierEasing(0f, 0f, 0.58f, 1f)

/**
 * 判"手指停住了"的速度阈值（px/s）—— 手指速 = `d(dragAnimation.targetValue)/dt × 块步进`。
 *
 * 掉到它以下就认定手指停了，拉伸目标**直接置 0**、长度当帧开始回落（2026-10-05 用户要求：
 * `targetValue` 的差分**只**用来判这件事，拉伸量和方向仍然由"块的实际位移速度"驱动）。
 *
 * 不能取严格 0：触摸事件稀疏时（手指极慢移动、几帧才来一个事件）会把"还在动"误判成"停"，
 * 长度会一抽一抽。50 px/s ≈ 0.2 格/秒（用户 2026-10-05 定）。
 */
private const val NavStretchFingerStillSpeed = 50f

/**
 * **贴边格飞行的「长度回落」最迟启动时刻**（秒，自飞行开始算）—— 2026-10-06 修"跨 3 格顶点偏晚 40ms"。
 *
 * 贴边格（1↔4）的"过冲顶点"就是**长度缩过头到 `-stretchOverShoot` 那一刻**，
 * 而那一段是 [NavStretchFallDuration] = 150ms 的 ease-out。原来的回落触发条件是
 * "块速跌破 [NavStretchV0]"，而**跨得越远块越快、速度掉下来越晚** ⇒ 回落起点随跨度后移：
 *
 * | 跨度 | 回落起点（实测） | 起点 + 150ms | 飞行结束（实测） | 谁在拖后腿 |
 * | --- | --- | --- | --- | --- |
 * | 1 格 | 255ms | 405ms | 426ms | 飞行（✓ 对齐） |
 * | 3 格 | **306ms** | **456ms** | 425ms | **回落，晚 31ms** |
 *
 * 方案 B 之后飞行时长已经与跨度无关（恒 ≈425~436ms），所以这里给回落加一条**时间兜底**：
 * 飞行开始满 [NavEdgeFallDelaySec] 就无条件启动回落。两个触发条件取**先到者**，
 * 于是跨 1 格仍是原来的 255ms（行为不变），跨 3 格被拉到 250ms。
 *
 * 取 0.25s 的余量怎么算的：`回落起点 + 150ms` 必须**早于**飞行结束，而飞行结束实测在
 * 424~436ms 之间波动、`isFlying` 翻 false 到 else 分支接手还有约 16ms ⇒
 * 起点要 ≤ 436 − 150 − 16 = **270ms**；再留一点余量取 **250ms**。
 * 此时块离目标还剩约 8px、块速约 190px/s，视觉上仍然是"块快到了才开始收"。
 * ⚠️ 这个数与 `NavTabFlyStiffness` / `NavStretchFallDuration` 联动：
 * 改了飞行刚度（飞行时长变了）或回落时长，这里要跟着改成 `飞行时长 − 回落时长 − 接手延迟`。
 * 中间格不受影响（`stretchOverShoot` 恒为 0，这条兜底不成立；它们的顶点由
 * `flyLeadShift` 到位决定，见 [flyLeadShift]）。
 */
private const val NavEdgeFallDelaySec = 0.25f

/**
 * 边界越界挤压（2026-10-05 用户定稿）。
 *
 * 块**贴住边界**（1 号位 / 4 号位）而手指仍朝越界方向推时，越界方向的**对侧**边缘向内挤：
 * 1 号位向左推 → **右缘**内收；4 号位向右推 → **左缘**内收（镜像）。手指往回拉即恢复。
 *
 * 挤压量 = `blockWidthPx × NavEdgeSqueezeFraction`（**未缩放坐标**，即 280 × 0.05 = 14px）。
 * 外层还带着按压的 0.915 居中缩放，所以屏上看到的内收正好是 `14 × 0.915 = 12.81px`
 * = 按压态长度 256.2px 的 5%（用户给的基准就是"按压态长度"）。
 *
 * 实现上直接借用选中块那两层现成的**两缘通道**（左缘 = 外层 translationX、右缘 = 内层 scaleX）：
 * 两个 Animatable 独立于点击回弹的 `bounceLeft/bounceRight`，最后在 graphicsLayer 里相加，
 * 互不 `snapTo(0f)` 打断。挤压 0.15s / 恢复 0.15s（[NavEdgeSqueezeMs]）。
 */
private const val NavEdgeSqueezeFraction = 0.05f
private const val NavEdgeSqueezeMs = 150

/**
 * 底栏玻璃的高光（2026-10-04 用户对照官方定稿）。
 *
 * miuix 的高光 shader = ① 均匀描边（宽度 = `Highlight.width`，只由 sdf 决定，1px 过渡、本身就是硬的）
 * + ② 两个**方向性光源**（`dot(法线, lightDir)` 的 bloom，羽化半径 = `innerBlurRadius`）。
 * 预设（GlassStroke*）自带的②偏打某些边（我们用的 MiddleDark 偏上下、0.5/0.25 强度、2dp 羽化），
 * 导致"上下比左右宽、整体偏软"。
 *
 * 官方是"**宽 1.35dp、四周均匀、边缘硬**"的干净描边 —— 所以这里自定义 style，把两个光源
 * 强度归零（只留①）；宽度按官方量到的 **1.35dp**。
 *
 * 亮度（2026-10-04 用户要求）：描边色取**纯白**（`color.alpha` → 进 shader 是 `strokeAlphaMul` = 1），
 * `Highlight.alpha`（shader 里的 `highlightAlpha`）= **浅色 0.9 / 深色 0.1** ——
 * 即高光不透明度**浅色 0.9、深色 0.1**（深浅分开；深色由 0.15 再降到 0.1）。
 */
private val NavGlassStroke = BloomStroke(
    color = Color.White,
    innerBlurRadius = 1.35.dp,
    primaryLight = LightSource(LightPosition(0.5f, 0.5f, -0.5f), intensity = 0f),
    secondaryLight = LightSource(LightPosition(0.5f, 0.8f, -0.5f), intensity = 0f),
)
private val NavGlassHighlightLight = Highlight(width = 1.35.dp, alpha = 0.9f, style = NavGlassStroke)
private val NavGlassHighlightDark = Highlight(width = 1.35.dp, alpha = 0.1f, style = NavGlassStroke)

/**
 * OS4 风格的悬浮导航：一个胶囊 + 会滑动的选中块 + 阻尼拖动。
 * 玻璃效果走 kyant backdrop，运行时不支持时自动退化成半透明胶囊。
 */
@Composable
internal fun Os4FloatingNavigationBar(
    tabs: List<NavTab>,
    selectedIndex: MutableIntState,
    backdrop: Backdrop?,
    modifier: Modifier = Modifier,
    showIcons: Boolean = true,
    showLabels: Boolean = true,
    advancedMaterial: Boolean = true,
    isDarkTheme: Boolean = false,
    accentColorOverride: Color? = null,
    // 胶囊宽度占屏宽的比例。若不传则按 tabCount 自动适配（3-tab→0.72、4-tab→0.88、5-tab→1.0）。
    widthFraction: Float? = null,
    onTabSelected: (Int) -> Unit,
) {
    val density = LocalDensity.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val innerPaddingPx = with(density) { NavBarInnerPadding.toPx() }
    val animationScope = rememberCoroutineScope()
    // 点击回弹的两个边缘偏移（px，+ 向右）：左缘看 bounceLeft、右缘看 bounceRight，两缘错峰播放。
    val bounceLeft = remember { Animatable(0f) }
    val bounceRight = remember { Animatable(0f) }
    // 越界挤压的两个边缘偏移（px，未缩放坐标，+ 向右）—— 与上面的回弹**分开**存，
    // 最后在 graphicsLayer 里相加：这样两套动画互不 `snapTo(0f)` 打断（详见 NavEdgeSqueezeFraction）。
    val squeezeLeft = remember { Animatable(0f) }
    val squeezeRight = remember { Animatable(0f) }
    // 越界方向：0 = 无；−1 = 贴左边界仍向左推（挤右缘）；1 = 贴右边界仍向右推（挤左缘）。
    var squeezeEdge by remember { mutableStateOf(0) }
    // 按压拖动的长度拉伸系数（0f ~ NavStretchMax），由下面的逐帧循环写入 ——
    // 读的地方只有 graphicsLayer（绘制阶段），不触发重组。
    val stretchNow = remember { mutableFloatStateOf(0f) }
    // 拉伸的方向量（−1 = 块在向左跑、+1 = 向右跑、0 = 居中）—— 决定长度加在哪一端：
    // 朝右跑 → 右缘钉死、全往左长；朝左镜像。同样由帧循环平滑写入，
    // 所以手指掉头时钉死端是从一端连续滑到另一端的，中间那几帧表现为"两端同时动"。
    val dirNow = remember { mutableFloatStateOf(0f) }
    // 手指是否按在拖动层上（由拖动回调维护）—— 用它**启停**帧循环。不能写成 `while (true)`：
    // 那会让底栏在空闲时也一直 `withFrameNanos` 请求 vsync（玻璃层每帧都被拉起来重绘）。
    var isHolding by remember { mutableStateOf(false) }
    // 标签飞行中（2026-10-05 加）：飞行期间**也要按速度拉伸**，所以帧循环的启停要多看它一眼。
    // 为什么必须单独一个标记：帧循环里那道"手指停没停"的闸用的是 `d(targetValue)/dt`，
    // 而飞行时 `targetValue` 第一帧就跳到终点、之后恒定 → `fingerSpeed` 恒为 0 → 会被判成
    // "手指停住"、拉伸目标全程被置 0，一根都伸不出来。所以飞行期间要跳过那道闸（见 fingerGate）。
    var isFlying by remember { mutableStateOf(false) }
    // 「按下标签 → 块立刻飞过去」的目标格（-1 = 无）。标签的按下回调写它、由下面的 effect 消费。
    // 走 tween 固定时长、**不碰按压反馈**（按压由标签自己的 press/release 管），所以用 animateValueTo。
    // 配一个自增 token 当 effect 的 key：连按**同一个**标签时目标值不变，光看值不会重启动画。
    var tabFlyTarget by remember { mutableIntStateOf(-1) }
    var tabFlyToken by remember { mutableIntStateOf(0) }
    // 拖动触发的切换不弹：拖动收尾置位、由 selectedIndex 的 effect 消费一次。
    var suppressBounce by remember { mutableStateOf(false) }
    // 标签触发的切换**不重新定位块**：块在"按下"那一刻就已经起飞、由 tabFly 那条 effect 走完，
    // 松手只是改 selectedIndex 去翻页。这里若再 glide 一次会把 tween 打断、曲线接不上。
    var suppressSync by remember { mutableStateOf(false) }
    // 块步进（px，= (胶囊内宽 − 块宽)/(格数−1)），由下面的布局写回 ——
    // 拖动手势与 playBounce 的回弹幅度都按它换算。
    val layoutTabWidthPx = remember { mutableFloatStateOf(0f) }
    // **贴边格（1↔4）飞行时，把"回弹的压缩"并进「长度恢复」里**（2026-10-05 用户的方案）：
    // 长度不再收到 0，而是**缩过头** `-stretchOverShoot`（= 回弹幅度 / 块宽），到位后再用回弹的
    // 恢复曲线（[NavBounceReturnMs] / [NavBounceReturnEasing]）弹回 0。
    //
    // 为什么成立：贴边格上"回弹让一缘内移 overshoot"和"块变窄 overshoot"**是同一个几何效果**
    // （贴边侧本来就不动）—— 所以"回弹的压缩"和"长度恢复"本来就是**同一条边上的连续运动**，
    // 拆成两段、还要在中间"等"才是错的。并进长度通道后，两段合成一条宽度曲线，**中间没有交接点**，
    // 顿感从根上消失；`waitHandoff()` 对贴边格也不再需要。
    //
    // ⚠️ **中间格（2↔3）不再用它**（2026-10-06 当天改掉）：用户要让**后缘比前缘慢**，
    // 而长度是**单一量**、没法只滞后一半 —— 中间格的后缘改走独立量 `flyTrailShift`
    // （可见 [flyLeadShift] 那段说明）。这里只剩**贴边格**和拖动路径。
    // 拖动路径恒为 0。
    val stretchOverShoot = remember { mutableFloatStateOf(0f) }
    // **中间格（2↔3）的「惯性过冲」**（2026-10-06 用户的方案）：回弹不再是一发独立脉冲，
    // 而是让块**自己冲过目标位置一点点**，再整体移回来 ——
    //
    // * **前缘**：由"块离目标还剩多少"连续驱动的前伸量 `flyLeadShift`。它**涨到 `overshoot`
    //   就保持住**（不回落），等块到位之后才用回弹的恢复曲线收回。
    // * **后缘**：**内缩同样的 `overshoot`**，走**自己的量** `flyTrailShift`。
    //   两者**同向**（都朝运动方向）⇒ 叠加起来就是**整块过冲 overshoot**，也就是用户那句
    //   "把目标位置过冲成伸长量、然后整体移回去"。
    //   （`stretchSpan()` 里前伸量加在运动方向那端、内缩量减在反侧那端，各就各位。）
    // * ⚠️ 后缘**必须独立成量、不能借长度通道**（[stretchOverShoot]）：长度是**单一量**，
    //   没法只让它的一半滞后 —— 而用户要的恰恰是"后缘比前缘慢"：内缩晚
    //   [NavInertiaRangeTrailPx] 开始、收回晚 [NavBounceTrailDelayMs] 开始
    //   （"后端的动作是前端传导过来的"）。
    //
    // ⚠️ 为什么"保持到到位才收回"是**能看见的前提**（2026-10-06 实测踩到两次）：
    // 第一版拿长度拉伸当驱动量 → 峰值落在起飞后 ~200ms（块速 764px/s），块停下时已衰减到 1px；
    // 第二版用 `sin(2πp)` 包络 → p 到 1（块刚到位）时它**已经归零**，同样什么都看不到。
    // 只有"峰值保持到块停、收回放在块停之后"，前缘那一下才真的在屏幕上停留得住。
    //
    // 单位是**未缩放坐标 px**，正 = 朝运动方向；只在中间格飞行期间非零（贴边格仍走
    // [stretchOverShoot] 那套、拖动路径恒为 0）。
    val flyLeadShift = remember { mutableFloatStateOf(0f) }
    val flyTrailShift = remember { mutableFloatStateOf(0f) }
    // 本次飞行的回弹幅度（px，带符号 = 朝运动方向为正）—— 由飞行 effect 按跨度写一次。
    val flyBoxOvershoot = remember { mutableFloatStateOf(0f) }
    // 惯性形变是否处于激活状态（中间格飞行期间为 true，块到位后关掉）。
    var flyInertiaActive by remember { mutableStateOf(false) }
    // **长度回落的三件状态挂在组合作用域上**（2026-10-06 修"跨 3 格顶点晚 171ms"）：
    // 帧循环的 key 是 `isHolding / isFlying`，飞行结束那一刻 `isFlying` 由 true 翻 false
    // ⇒ 这个 effect **被取消并重启**，写在它内部的局部变量一律清零。而贴边格飞行的
    // 回落是要一路落到 `-stretchOverShoot` 的 150ms ease-out，跨 3 格时它**跨不过**那个
    // 重启点（主分支只走了一部分），于是 else 分支**从头又走一遍 150ms** ——
    // 实测 `fly 结束` 在 424ms（正确），顶点却落在 595ms。
    // 挂到外面，两段就能**接着走完**，顶点回到 424ms（与页面视觉停住对齐）。
    val fallFromV = remember { mutableFloatStateOf(0f) }
    val fallElapsedV = remember { mutableFloatStateOf(0f) }
    val fallingV = remember { mutableStateOf(false) }
    val tabCount = tabs.size.coerceAtLeast(1)
    // 胶囊宽度：用户显式传值优先，否则按 tabCount 自动适配
    val effectiveWidthFraction = widthFraction ?: navWidthFractionFor(tabCount)
    // 选中块宽度（px）—— 与 BoxWithConstraints 内部算的一致，供 LaunchedEffect 直接使用
    val indicatorWidthPx = calcIndicatorWidthFromScreenPx(
        screenWidthPx, effectiveWidthFraction, tabCount, innerPaddingPx,
    )
    val initialIndex = selectedIndex.intValue.coerceIn(0, tabCount - 1)
    val dragAnimation = remember(tabCount) {
        Os4DampedDragAnimation(
            animationScope = animationScope,
            initialValue = initialIndex.toFloat(),
            valueRange = 0f..(tabCount - 1).toFloat(),
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            // OS4 compresses the floating pill while it is held, then springs back on release.
            // 2026-10-05 用户定 0.9 → 0.93 → **0.915**（基准是**未按压**尺寸，即按压态宽 280 × 0.915 = 256.2px）。
            pressedScale = 0.915f,
            onDragStopped = {
                // 手指一离开：越界状态不再成立 → 走 0.15s 恢复；长度也切到"松手收回"分支。
                isHolding = false
                squeezeEdge = 0
                // ⚠️ 只有**真的拖动过**才走收尾。单纯按一下标签也会经过这里（压力层是铺在
                // 标签之上的整层透明 Box），若照旧执行，下面的 `animateToValue` 会用
                // `mutatorMutex` 把标签那条**正在飞的 tween 当场取消** —— 曲线断掉、回弹也丢。
                if (hasDragged) {
                    val target = targetValue.roundToInt().coerceIn(0, tabCount - 1)
                    if (target == selectedIndex.intValue) {
                        // 没跨页：只把选中块弹回整数位。
                        animateToValue(target.toFloat())
                    } else {
                        // 跨页：交给 App 的 onTabSelected（它会**立刻**定住 selectedIndex 再翻页），
                        // 块位移动画统一由 `LaunchedEffect(selectedIndex)` 驱动 —— 只跑一次、不重复、不折返。
                        // 拖动路径不做微回弹：置一次标记，由那个 effect 消费。
                        suppressBounce = true
                        onTabSelected(target)
                    }
                }
            },
            onDrag = { _, amount ->
                // 2026-10-04：拖动层现在与选中块同宽（280px），但"一格"是**块步进**
                // （= (胶囊内宽 − 块宽)/(格数−1) ≈ 252.67px），所以灵敏度要用步进、不能用层宽。
                // （历史上这里用过 `size.width`、更早还多除过 tabCount —— 见 2026-09-29 的灵敏度坑。）
                // 走到这里的位移已经过拖动层的**死区**过滤（按标签的那几 px 抖动不会进来），
                // 所以这里不必再判零位移。
                isHolding = true
                val step = layoutTabWidthPx.floatValue
                if (step > 0f) {
                    val maxValue = (tabCount - 1).toFloat()
                    // 手指想要的原始值（**未截断**）—— 超出边界的那部分就是"越界量"，
                    // 下面 `coerceIn` 会把它丢掉（块因此停在边界不动），这里顺手拿它判挤压。
                    val raw = targetValue + amount.x / step
                    updateValue(raw.coerceIn(0f, maxValue))
                    // 需求 B：块已贴住边界、手指仍在往越界方向推 → 对侧边缘内挤 5%。
                    // 与速度无关（慢慢推也挤），只看"贴边 + 方向"。
                    squeezeEdge = when {
                        targetValue <= 0f && raw < 0f -> -1
                        targetValue >= maxValue && raw > maxValue -> 1
                        else -> 0
                    }
                }
            },
            // OS4 restores the pressed scale as soon as the finger is released.
            awaitTargetBeforeRelease = false,
        )
    }
    // 单个边缘的脉冲：快速过冲（硬弹簧） → 回退（[NavBounceReturnMs] 先快后慢、无回摆）。
    suspend fun runEdgePulse(edge: Animatable<Float, AnimationVector1D>, offsetPx: Float) {
        Log.d(NAV_TAG, "runEdgePulse 起  offset=$offsetPx  (edge 当前=${edge.value})")
        edge.snapTo(0f)
        edge.animateTo(
            targetValue = offsetPx,
            animationSpec = spring(dampingRatio = NavBounceRiseDamping, stiffness = NavBounceRiseStiffness),
        )
        Log.d(NAV_TAG, "runEdgePulse 到峰  edge=${edge.value}")
        // 回退：三次贝塞尔缓入缓出（2026-10-05 由 `spring(0.55, 900)` 改来）——
        // 弹簧在这段"名义 433ms、实际 90ms 就走完位移"，观感是"突"；见 [NavBounceReturnMs] 的 KDoc。
        edge.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = NavBounceReturnMs, easing = NavBounceReturnEasing),
        )
        Log.d(NAV_TAG, "runEdgePulse 归零  edge=${edge.value}")
    }
    /**
     * **回弹的接手点** —— 两个条件按序满足就踢：
     *
     * ① **块接近目标**（剩余位移 ≤ [NavBounceHandoffPx]）—— **主判据**。
     *    ⚠️ **不能等 `isFlying` 变 false**（2026-10-05 实测踩到）：那是弹簧的**收敛判据**
     *    （`animateValueTo` 跑到 `visibilityThreshold = 0.001` 格），跨 3 格要 **≈0.45s**；
     *    而块 **≈0.30s 就已经剩不到 8px**，之后那 150ms 里只再走 8px ⇒ "几乎不动"。
     *    拿它当判据就是整整 150ms 的"谷"，光收长度根本救不回来
     *    （实测：只改长度阈值，`飞行启动 → 回弹踢` 只从 533ms 变 530ms）。
     *    它同时天然解决 race：距离一开始是满的，只有块真跑起来才可能满足。
     * ② **长度收到交接区**（`≤ NavStretchHandoffRemain`）—— 留残量、不等精确归零，
     *    免得把 ease-out 的"减速尾"和回弹的"加速头"首尾接成一段 ~45ms 的停顿
     *    （用户 2026-10-05："回弹和恢复间还会顿一下，希望视觉上连续"）。
     *    滑页路径**没有拉伸**（`stretchNow` 恒为 0）→ 立即通过。
     *
     * 为什么要等、而不是干脆并行：并行时两股形变互相掩盖 —— 长度 0.32s 起收、0.15s 收完，
     * 回弹 0.15s 起、0.327s 到峰，**在 0.32~0.6s 完全重叠**，于是"恢复"和"回弹"两段在
     * 1↔4（跨 3 格、拉伸最明显）上**都读不出来**（用户报的"后两个都看不到"）。
     *
     * 真等不到也不会卡死：外面套了 [NavBounceStretchWaitMs] 超时。
     */
    suspend fun waitHandoff(target: Float) {
        val step = layoutTabWidthPx.floatValue
        withTimeoutOrNull(NavBounceStretchWaitMs) {
            snapshotFlow { dragAnimation.value }
                .first { abs(it - target) * step <= NavBounceHandoffPx }
            snapshotFlow { stretchNow.floatValue }.first { it <= NavStretchHandoffRemain }
        }
    }
    // 点击移动途中的回弹（拖动不调用）：两个边缘分开动 —— 前缘先、后缘晚 0.15s；
    // 边缘格时贴边侧不动、只让另一侧动（立即、无错峰）。
    suspend fun playBounce(from: Float, target: Float) {
        // 手指已经接管拖动（比如点完立刻按住）：这一轮不弹，避免和拖动叠在一起。
        // ⚠️ 守卫用 `hasDragged`（真的拖过）而不是 `isDragging`（手指按在拖动层上）：
        // 拖动层是铺在标签之上的整层透明 Box，按标签时它也会收到 down、`isDragging`
        // 一直挂到抬手 —— 于是"按标签 → 块到位后播回弹"会被误伤、一脚都弹不出来
        // （用户 2026-10-05 报的"回弹怎么没了"就是这个）。
        Log.d(
            NAV_TAG,
            "playBounce 入口 from=$from target=$target hasDragged=${dragAnimation.hasDragged} " +
                "isDragging=${dragAnimation.isDragging} step=${layoutTabWidthPx.floatValue}",
        )
        if (dragAnimation.hasDragged) {
            Log.d(NAV_TAG, "playBounce 跳过：hasDragged=true")
            return
        }
        val delta = target - from
        if (delta == 0f) {
            Log.d(NAV_TAG, "playBounce 跳过：delta=0（按的是当前已选中的标签）")
            return
        }
        val movingRight = delta > 0f
        // 幅度 = 块步进 ×（基准 + 每格斜率 × **跨度**）—— 跨得越远弹得越猛：
        // 跨 1 格 3.5%（8.84px）/ 2 格 5.5%（13.90px）/ 3 格 7.5%（18.95px）。
        val span = abs(delta)
        val overshoot = layoutTabWidthPx.floatValue *
            (NavBounceBaseAmplitude + NavBouncePerTabAmplitude * span) *
            (if (movingRight) 1f else -1f)
        Log.d(NAV_TAG, "playBounce 执行 movingRight=$movingRight span=$span overshoot=$overshoot")
        // 贴边侧不能朝运动方向过冲：4→1 时左缘不动、1→4 时右缘不动（表现为"另一侧收缩"）。
        val leftAllowed = !(!movingRight && target <= 0f)
        val rightAllowed = !(movingRight && target >= (tabCount - 1).toFloat())
        // 前缘：向左移动时是左缘，向右移动时是右缘。
        val lead = if (movingRight) bounceRight else bounceLeft
        val trail = if (movingRight) bounceLeft else bounceRight
        val leadAllowed = if (movingRight) rightAllowed else leftAllowed
        val trailAllowed = if (movingRight) leftAllowed else rightAllowed
        try {
            coroutineScope {
                if (leadAllowed) launch { runEdgePulse(lead, overshoot) }
                if (trailAllowed) {
                    launch {
                        if (leadAllowed) delay(NavBounceStaggerMs)
                        runEdgePulse(trail, overshoot)
                    }
                }
            }
        } finally {
            // ⚠️ **中断也必须把两缘清零**（2026-10-05 修"贴边格偶发越界/收进来"）。
            // 脉冲是 `snapTo(0) → animateTo(±overshoot) → animateTo(0)` 三段，**被取消时会停在
            // 途中的任意值**；而取消是常态 —— 连点/滑页都会重启 effect、把上一轮脉冲掐掉。
            // 更要命的是贴边格规则下**只播一侧**（1→4 只有左缘、4→1 只有右缘），另一侧的残余
            // 没人去 `snapTo(0)`，就会一直挂在屏幕上 ⇒ 块整体偏移几 px、贴边格看起来"越过去
            // 或收进来"，而且偶发（用户报的现象）。日志实证：`runEdgePulse 起 ... edge 当前=5.9567213`。
            // 用 NonCancellable：这里已经在取消路径上了，普通挂起调用会被直接丢掉。
            withContext(NonCancellable) {
                bounceLeft.snapTo(0f)
                bounceRight.snapTo(0f)
            }
        }
    }
    // 逐帧把「块速 → 长度系数」算出来并平滑（需求 A），由 **[isHolding]（手指按着）
    // 或 [isFlying]（标签飞行中）** 启停：只要块在动就跟踪；都停下后按 0 收回、收完退出循环。
    // 必须用帧循环、不能只在 onDrag 里算 —— 手指停住时不再有 pointer 事件、
    // 飞行也不是由 pointer 驱动的，只有帧循环能把长度收回去。
    //
    // **拖动和飞行共用同一套驱动量、共用同一段循环**（用户 2026-10-05 要求"飞也要拉伸，
    // 逻辑和按压拖动一致"）—— 因为飞行改的就是 `dragAnimation.value`，块速是同一个量。
    //
    // 这里同时算**两个**速度，分工是用户 2026-10-05 定死的：
    // * 块速 = `d(dragAnimation.value)/dt × 块步进` —— 驱动**拉伸量和方向**（拖动 + 飞行都看它）。
    //   用 value 而不是 targetValue：贴边界时 `updateValue` 里的 `coerceIn` 把它锁死 → 0，
    //   拉伸自然退场、只剩挤压，这是需求 A / B 不冲突的实现依据。
    // * 手指速 = `d(dragAnimation.targetValue)/dt × 块步进` —— **只**用来判"手指是不是停住了"，
    //   而且**只在真的按着手指时启用**（`fingerGate`，见下）。
    //   value 是 spring(1f, 1000f) 跟出来的、自带约 30ms 的尾巴：手指停下后块速要晚 30~100ms
    //   才归零，长度收回会肉眼可见地慢一拍（用户反馈的现象）。所以手指速一掉到
    //   [NavStretchFingerStillSpeed] 以下，就认定手指停了、把拉伸目标**直接置 0**，
    //   长度当帧开始回落，不必等块速自己衰减。
    //   ⚠️ 飞行时**必须跳过这道闸**：`targetValue` 第一帧就跳到终点、之后恒定 →
    //   `fingerSpeed` 恒为 0 → 会被判成"手指停住"，拉伸目标全程置 0、一根都伸不出来。
    LaunchedEffect(isHolding, isFlying) {
        // **惯性过冲**（中间格飞行，2026-10-06 用户的方案）：回弹不再是一发独立脉冲，
        // 而是让块**自己冲过目标一点点**，到位后再整体移回来 ——
        // * **前缘**：由"块离目标还剩多少"连续驱动的前伸量 `flyLeadShift`，**涨到 `overshoot`
        //   就保持住**，等块到位之后才由收尾分支用回弹的恢复曲线收回（见下面 `else` 里那段）。
        // * **后缘**：内缩量 `flyTrailShift`，同样由剩余位移驱动，但用**更小的触发距离**，
        //   所以比前缘晚一步；收回时再滞后 [NavBounceTrailDelayMs] —— "后端的动作是前端
        //   传导过来的"（2026-10-06 用户要求）。
        // 定义在这里（而不是主分支内）是因为**两个分支都要用**：
        // 飞行期间主分支逐帧写它，收尾分支负责把它们收回去。
        fun updateInertia() {
            if (!flyInertiaActive) {
                flyLeadShift.floatValue = 0f
                flyTrailShift.floatValue = 0f
                return
            }
            // ⚠️ 驱动量是**块离目标还剩多少**，不是"长度拉伸量"（2026-10-06 实测）：
            // 拿长度当驱动量时峰值落在起飞后 ~200ms（块速 764px/s），块停下时已衰减到 1px 以内。
            val step = layoutTabWidthPx.floatValue
            val remainPx = abs(dragAnimation.value - dragAnimation.targetValue) * step
            val p = (1f - remainPx / NavInertiaRangePx).coerceIn(0f, 1f)
            // ⚠️ **涨到峰值就保持，不要做成包络**（2026-10-06 第三次修正）：此前用
            // `sin(2πp)` 让两端归零，结果 p 到 1（块刚到位）时它**已经收完了** ——
            // 块停下那一刻屏幕上什么都没有，用户连着两轮报"没效果"。
            // 现在峰值保持到块停，收回交给收尾分支（≈200ms ease-out），那一下才停留得住。
            val overshoot = flyBoxOvershoot.floatValue
            // 前缘用 [NavInertiaRangePx]、后缘用**更小**的 [NavInertiaRangeTrailPx] ——
            // 后缘因此更晚开始内缩（"前缘先顶出去、后缘过一会儿才被带进来"）。
            val pLead = (1f - remainPx / NavInertiaRangePx).coerceIn(0f, 1f)
            val pTrail = (1f - remainPx / NavInertiaRangeTrailPx).coerceIn(0f, 1f)
            flyLeadShift.floatValue = overshoot * pLead
            flyTrailShift.floatValue = overshoot * pTrail
            Log.d(
                NAV_TAG,
                "惯性过冲 pLead=${"%.3f".format(pLead)} pTrail=${"%.3f".format(pTrail)} " +
                    "remain=${"%.1f".format(remainPx)}px " +
                    "lead=${"%.2f".format(flyLeadShift.floatValue)} " +
                    "trail=${"%.2f".format(flyTrailShift.floatValue)}",
            )
        }
        if (isHolding || isFlying) {
            // ⚠️ "手指停住"那道闸**只在手指真的按着时**才启用（2026-10-05 加飞行拉伸时定的）：
            // 飞行期间 `targetValue` 第一帧就跳到终点、之后恒定 → `fingerSpeed` 恒为 0 →
            // 走闸就会被判成"手指停住"、拉伸目标全程置 0。飞行只看块速，速度掉下来它自己会收。
            val fingerGate = isHolding
            // 拖动接管：清掉上一次飞行残留的"缩过头"（否则松手后长度会往负方向缩）
            // 和惯性过冲的前伸量。
            if (isHolding) {
                stretchOverShoot.floatValue = 0f
                flyInertiaActive = false
                flyLeadShift.floatValue = 0f
                flyTrailShift.floatValue = 0f
            }
            var prevValue = dragAnimation.value
            var prevTarget = dragAnimation.targetValue
            // 本次飞行已过时间（秒）：effect 在 `isFlying` 翻 true 时启动，从第一帧起累加即可
            // （不与别的时钟混用）。一次飞行期间 `isHolding / isFlying` 都不变，所以 effect
            // 不会重启，这个累加是连续的；用于 [NavEdgeFallDelaySec] 那条时间兜底。
            var flyElapsedSec = 0f
            var prevNanos = withFrameNanos { it }
            while (true) {
                withFrameNanos { nanos ->
                    val dtSec = (nanos - prevNanos) / 1_000_000_000f
                    val value = dragAnimation.value
                    val targetValue = dragAnimation.targetValue
                    if (dtSec > 0f) {
                        val step = layoutTabWidthPx.floatValue
                        // 块的实际位移速度（px/s、**带符号**）—— 拉伸量与方向都看它。
                        val velocity = (value - prevValue) / dtSec * step
                        val speed = abs(velocity)
                        // 手指速（指令值的差分）—— 只判"停没停"。
                        val fingerSpeed = abs(targetValue - prevTarget) / dtSec * step
                        if (isFlying) flyElapsedSec += dtSec
                        // 贴边格的时间兜底：飞行开始满 [NavEdgeFallDelaySec] 就启动回落，
                        // 保证那 150ms 的落点赶在块落地之前（跨 3 格时速度触发会太晚）。
                        val edgeFallDue =
                            stretchOverShoot.floatValue > 1e-4f &&
                                flyElapsedSec >= NavEdgeFallDelaySec
                        val target = if (
                            (fingerGate && fingerSpeed < NavStretchFingerStillSpeed) || edgeFallDue
                        ) {
                            0f
                        } else {
                            // 归一化速度先过贝塞尔（低速增长慢、高速增长快），再乘最大增量。
                            NavStretchEasing.transform(
                                ((speed - NavStretchV0) / (NavStretchV1 - NavStretchV0))
                                    .coerceIn(0f, 1f),
                            ) * NavStretchMax
                        }
                        if (target <= 0f) {
                            // 回落：手指停住（或速度够不到门槛）→ 用带终点的 ease-out 收回。
                            // 起点值和时钟只在"刚进入回落"那帧记一次，之后按归一化时间查曲线。
                            // ⚠️ **落点不是 0**：贴边格飞行时落到 `-stretchOverShoot`（缩过头）——
                            // "回弹的压缩"就并进这一段，后面只剩弹回（见 [stretchOverShoot]）。
                            // ⚠️ 状态在 `fallFromV / fallElapsedV / fallingV`（组合作用域）而不是
                            // 本 effect 的局部变量：飞行结束时 effect 会重启，局部变量会被清零
                            // （见那三个状态的说明）。
                            if (!fallingV.value) {
                                fallFromV.floatValue = stretchNow.floatValue
                                fallElapsedV.floatValue = 0f
                                fallingV.value = true
                            }
                            fallElapsedV.floatValue += dtSec
                            val t =
                                (fallElapsedV.floatValue / NavStretchFallDuration).coerceIn(0f, 1f)
                            val fallTarget = -stretchOverShoot.floatValue
                            stretchNow.floatValue = fallFromV.floatValue +
                                (fallTarget - fallFromV.floatValue) *
                                NavStretchFallEasing.transform(t)
                            if (t >= 1f) {
                                stretchNow.floatValue = fallTarget
                                fallingV.value = false
                            }
                        } else {
                            // 上升：指数逼近，抹掉速度估计的高频抖动。
                            // ⚠️ 飞行期间用更小的 τ（[NavStretchTauFly]）：块速来自弹簧、本来就平滑，
                            // 60ms 的滞后会让长度收得太晚，把"回弹接手"硬拖后 ~70ms
                            // （2026-10-05 二次优化）。每帧读一次 `isFlying` —— effect 是异步重启的，
                            // 循环可能还会多跑一两帧。
                            fallingV.value = false
                            val tau = if (isFlying) NavStretchTauFly else NavStretchTauDrag
                            val alpha = 1f - exp(-dtSec / tau)
                            stretchNow.floatValue += (target - stretchNow.floatValue) * alpha
                        }
                        // 方向量只在"真的在拉长"时更新；速度落下去（含松手）就保持上一次的方向，
                        // 免得收回的过程里钉死端左右乱跳。
                        if (speed > NavStretchV0) {
                            val dirTarget = if (velocity > 0f) 1f else -1f
                            dirNow.floatValue +=
                                (dirTarget - dirNow.floatValue) * (1f - exp(-dtSec / NavStretchTauDrag))
                        }
                        prevValue = value
                    }
                    // 惯性形变跟着本帧的 `stretchNow` 走（放在 `if (dtSec > 0f)` 外面：
                    // 它只读长度、不需要 dt）。
                    updateInertia()
                    prevTarget = targetValue
                    prevNanos = nanos
                }
            }
        } else {
            // 松手 / 飞行结束：速度信号消失 → 同一套 ease-out 收回（t=1 精确到位、无尾巴）。
            // ⚠️ 落点和上面一样是 `-stretchOverShoot`（贴边格飞行时缩过头；其余情况就是 0）。
            val over = stretchOverShoot.floatValue
            val fallTarget = -over
            // ⚠️ 这里**不再**逐帧调 `updateInertia()`：飞行结束后 `flyLeadShift` 要**保持**
            // 在它的峰值上（那正是"块冲过头停在前面"的状态），收回在下面单独做。
            //
            // ⚠️ **接着主分支没走完的那一段继续**，而不是从头再走一遍 150ms（2026-10-06 修）：
            // 帧循环的 key 含 `isFlying`，飞行结束那一刻 effect 被取消重启，主分支里那 150ms
            // 回落（贴边格要落到 `-stretchOverShoot`）会被清零。状态挂在外面的
            // `fallFromV / fallElapsedV / fallingV` 上，所以这里能续上 —— 跨 3 格实测
            // `fly 结束` 424ms → 顶点 424ms（修前是 595ms，晚了整整一段回落）。
            if (abs(stretchNow.floatValue - fallTarget) > 5e-4f) {
                if (!fallingV.value) {
                    fallFromV.floatValue = stretchNow.floatValue
                    fallElapsedV.floatValue = 0f
                    fallingV.value = true
                }
                var prevFallNanos = withFrameNanos { it }
                while (true) {
                    var finished = false
                    withFrameNanos { nanos ->
                        fallElapsedV.floatValue += (nanos - prevFallNanos) / 1_000_000_000f
                        prevFallNanos = nanos
                        val t =
                            (fallElapsedV.floatValue / NavStretchFallDuration).coerceIn(0f, 1f)
                        stretchNow.floatValue = fallFromV.floatValue +
                            (fallTarget - fallFromV.floatValue) * NavStretchFallEasing.transform(t)
                        if (t >= 1f) finished = true
                    }
                    if (finished) break
                }
                stretchNow.floatValue = fallTarget
                fallingV.value = false
            }
            // **"整体移回去"**（2026-10-06）：块到位时正处在"过冲"状态 ——
            // * 长度**缩过头** `-stretchOverShoot`（= 后缘内缩 overshoot，贴边格那套）
            // * 前缘**前伸** `flyLeadShift`（= overshoot，中间格那套）
            // ⚠️ 这两个量**必须用同一条曲线、在同一个循环里一起收回**：顺序写会先花 200ms
            // 弹回后缘、再花 200ms 收前缘，总共 400ms 且两缘不同步 —— "整块过冲"当场散掉。
            // 曲线沿用回弹的恢复曲线（[NavBounceReturnMs] / [NavBounceReturnEasing]）。
            val backFromStretch = stretchNow.floatValue
            val backFromLead = flyLeadShift.floatValue
            val backFromTrail = flyTrailShift.floatValue
            // 后缘**滞后 [NavBounceTrailDelayMs] 才开始收回** —— 用户要的"后端的动作是前端
            // 传导过来的"就落在这一行：前缘立即归位、后缘隔一会儿才跟上，延迟期间它还缩着，
            // 于是块会短暂变窄一点。用的是**同一条曲线、同一个时长**，所以读起来是"同一个动作
            // 晚一点到"，而不是"另一个更慢的动作"。
            // 贴边格没有独立的后缘量（`backFromTrail = 0`）→ 延迟归零，整段仍是 200ms、行为不变。
            val trailDelayMs = if (backFromTrail > 0.01f) NavBounceTrailDelayMs else 0L
            if (over > 1e-4f || backFromLead > 0.01f || backFromTrail > 0.01f) {
                Log.d(
                    NAV_TAG,
                    "整体移回：stretch=$backFromStretch lead=$backFromLead " +
                        "trail=$backFromTrail（后缘滞后 ${trailDelayMs}ms）",
                )
                val startNanos = withFrameNanos { it }
                while (true) {
                    var finished = false
                    withFrameNanos { nanos ->
                        val elapsedMs = (nanos - startNanos) / 1_000_000f
                        val tLead = (elapsedMs / NavBounceReturnMs).coerceIn(0f, 1f)
                        val tTrail =
                            ((elapsedMs - trailDelayMs) / NavBounceReturnMs).coerceIn(0f, 1f)
                        val kLead = 1f - NavBounceReturnEasing.transform(tLead)
                        val kTrail = 1f - NavBounceReturnEasing.transform(tTrail)
                        stretchNow.floatValue = backFromStretch * kLead
                        flyLeadShift.floatValue = backFromLead * kLead
                        flyTrailShift.floatValue = backFromTrail * kTrail
                        if (elapsedMs >= NavBounceReturnMs + trailDelayMs) finished = true
                    }
                    if (finished) break
                }
                stretchNow.floatValue = 0f
                flyLeadShift.floatValue = 0f
                flyTrailShift.floatValue = 0f
            }
            flyInertiaActive = false
        }
    }
    // 边界越界挤压：0.15s 到 5%（未缩放坐标 —— 外层还带 0.915 居中缩放，
    // 所以屏上正好是 blockWidth × 0.05 × 0.915 = 按压态长度的 5%）。
    LaunchedEffect(squeezeEdge) {
        val amount = indicatorWidthPx * NavEdgeSqueezeFraction
        coroutineScope {
            launch {
                squeezeRight.animateTo(
                    targetValue = if (squeezeEdge == -1) -amount else 0f,
                    animationSpec = tween(NavEdgeSqueezeMs),
                )
            }
            launch {
                squeezeLeft.animateTo(
                    targetValue = if (squeezeEdge == 1) amount else 0f,
                    animationSpec = tween(NavEdgeSqueezeMs),
                )
            }
        }
    }
    // 调试：确认日志通道本身是通的（R8 没把 Log.d 剥掉）+ 记录实际步进值。
    LaunchedEffect(Unit) {
        Log.d(NAV_TAG, "=== 底栏组合完成 tabCount=$tabCount ===")
    }
    // 按下标签 → 块起飞。移动用**飞行专用弹簧** [navTabFlySpec]（按跨度现生成，
    // 名义 `spring(1f, 535f)`，比拖动跟手那条 `spring(1f, 1000f)` 软 —— "按下即飞"
    // 省掉了上游/改造前那次按压，这里把它补回来；理由见常量的说明）。这条**不带按压** ——
    // 压感由标签自己的 press/release 管（按下时已经压上了），这里再压一次会闪。
    //
    // 回弹（点击回弹）**和移动并行、但等"块接近目标 + 长度收到交接区"才踢**
    // （2026-10-05 用户定："伸长 → 恢复 → 立刻回弹"三段**串行**，见 [waitHandoff]）。
    // 历程：延迟 0（起步就踢，实测回弹整个藏在飞行途中、根本看不见）→ 固定 150ms
    // （峰值 ≈327ms 落在到位之后）→ 等 `stretchNow` 归零（最干净，但串行交界处"顿一下"，
    // 用户 2026-10-05 报）→ **等它降到 6%**（留残量让两段重叠、视觉连续）。
    //
    // ⚠️ 必须 `launch` 出去再移动，**不能顺序写** —— `playBounce` 要跑完一整个脉冲
    // （前缘到峰 + 回退），顺序写会把块起飞的时间整整推迟。
    // ⚠️ `from` 要在**任何动画开始之前**取：一旦开始，`dragAnimation.value` 就在变了，
    // delta 会算错方向；等到位再取更是 delta == 0、`playBounce` 直接返回、一脚都不弹。
    LaunchedEffect(tabFlyToken) {
        if (tabFlyToken > 0) {
            val target = tabFlyTarget.coerceIn(0, tabCount - 1).toFloat()
            val from = dragAnimation.value
            Log.d(NAV_TAG, "fly effect 启动 token=$tabFlyToken from=$from target=$target")
            // 飞行方向是**已知**的（不像拖动会掉头）→ 直接把方向量置位。
            // 不置位的话，上一次拖动留下的反方向会残留，飞行前半段长度会从"另一端"甩过来一下。
            dirNow.floatValue = if (target >= from) 1f else -1f
            // 回弹幅度（px、**未缩放坐标**）—— 按跨度算，贴边格 / 中间格两套方案共用。
            val spanPx = abs(target - from)
            val overshootPx = layoutTabWidthPx.floatValue *
                (NavBounceBaseAmplitude + NavBouncePerTabAmplitude * spanPx)
            val edgeCase = target <= 0f || target >= (tabCount - 1).toFloat()
            if (edgeCase) {
                // 贴边格（1↔4）：回弹的"压缩"并进**长度恢复**（长度缩过头 overshoot → 弹回），
                // 不播独立脉冲（2026-10-05 用户的方案，见 [stretchOverShoot]）。
                stretchOverShoot.floatValue = overshootPx / indicatorWidthPx
                flyBoxOvershoot.floatValue = 0f
                flyInertiaActive = false
                flyLeadShift.floatValue = 0f
                flyTrailShift.floatValue = 0f
            } else {
                // 中间格（2↔3）：**惯性过冲**（2026-10-06 用户的方案）—— 块自己冲过目标一点点，
                // 到位后再整体移回去。两侧**同向**叠加 = 整块过冲 `overshoot`：
                // * **前缘**：`flyLeadShift` 前伸；
                // * **后缘**：`flyTrailShift` 内缩 —— **自己的量**，所以能比前缘慢
                //   （内缩晚 [NavInertiaRangeTrailPx] 开始、收回晚 [NavBounceTrailDelayMs] 开始）。
                // ⚠️ 这里**不设** `stretchOverShoot`：长度是**单一量**，没法只让后缘那一半滞后。
                // 两个量都由帧循环按"块离目标还剩多少"写（见 `updateInertia`）。
                // 所以这里**不再** `waitHandoff` + `playBounce`：没有要等的交接点了。
                stretchOverShoot.floatValue = 0f
                flyBoxOvershoot.floatValue = overshootPx
                flyLeadShift.floatValue = 0f
                flyTrailShift.floatValue = 0f
                flyInertiaActive = true
                Log.d(
                    NAV_TAG,
                    "中间格惯性过冲：overshoot=$overshootPx span=$spanPx" +
                        "（前缘前伸 + 后缘内缩，后缘滞后）",
                )
            }
            // 打开"飞行中"标记 —— 帧循环据此跟踪块速、也就是这段飞行的**拉伸**（见上面的循环）。
            // 关掉它（下面 `animateValueTo` 返回后）帧循环会走另一支：把残余长度收回 0。
            isFlying = true
            // 飞行专用弹簧（比拖动跟手那条软，见 [navTabFlySpec]）—— 不能用 `glideTo`，
            // 它走的是 `valueAnimationSpec`，那条是拖动跟手共用的，调软会让拖动发黏。
            // ⚠️ 每次飞行**按本次跨度现生成**：阈值 ∝ 跨度，时长才与"跨几格"无关（方案 B）。
            // `spanPx` 就是跨度，但单位是**格**（名字里的 px 是历史遗留，见它的定义处）。
            dragAnimation.animateValueTo(target, navTabFlySpec(spanPx))
            // 飞行结束（弹簧收敛）→ 关掉标记，帧循环转去把残余长度收回 0。
            // 注意：本 effect 被下一次按下取消时这一行不会执行，但**新一次 effect 会接管**
            // 这个标记（它也置 true、结束时也会置回 false），所以不会卡在 true。
            isFlying = false
        }
    }
    LaunchedEffect(selectedIndex.intValue) {
        val target = selectedIndex.intValue.coerceIn(0, tabCount - 1).toFloat()
        val from = dragAnimation.value
        val fromDrag = suppressBounce
        suppressBounce = false
        val fromTab = suppressSync
        suppressSync = false
        // 标签路径：块在"按下"那一刻就已经起飞、由上面那条 effect 走完（松手不停它），
        // 这里只翻页、**不再定位块** —— 否则会打断 tween、曲线中途接上弹簧会顿一下。
        // 回弹也在那条 effect 末尾播，这里不重复。
        if (!fromTab) {
            // 只滑、不压：拖动跨页时拖动层自己压着 —— 这里再压一次会闪一下。
            // `glideTo` 用的是内部默认弹簧，拖动路径仍要快速收敛、不走 0.1s 那条 tween。
            if (fromDrag) {
                // 拖动跨页收尾：不弹，直接滑完（拖动路径不做微回弹）。
                dragAnimation.glideTo(target)
            } else {
                // 先清掉上一轮可能残留的脉冲；连点/滑页会重启这个 effect，等待中的回弹自然被取消。
                bounceLeft.snapTo(0f)
                bounceRight.snapTo(0f)
                // ⚠️ 这里**不能**再放 `delay(...)`：`glideTo` 是挂起的，已经把"等到位"含进去了，
                // 再加一层延迟就变成"**到位之后再等**"—— 用户 2026-10-05 报的"手指滑页时回弹
                // 慢半拍"就是这个（以前 `animateToValue` 不挂起，同一个 delay 的含义是"起步后"，
                // 改成 `glideTo` 之后含义悄悄变了）。
                // 滑页这条**没有长度拉伸**（`stretchNow` 恒为 0 → 第二个条件立即通过），
                // 接手点完全由**块的位移**决定：块离目标还剩 [NavBounceHandoffPx] 就踢，
                // 别等 `glideTo` 的弹簧尾巴收敛（那 140ms 里块只走 12.6px、几乎不动）。
                // `glide` 必须 `join` —— effect block 返回时子协程会被一起取消，不 join 的话
                // 块会停在半路。
                val glide = launch { dragAnimation.glideTo(target) }
                waitHandoff(target)
                playBounce(from, target)
                glide.join()
            }
        }
    }
    val contentColor = if (isDarkTheme) Color.White else Color.Black
    val selectedContentColor = accentColorOverride ?: contentColor
    // 按住时图标 / 文字被"弱化"到的颜色（2026-10-05 用户定：浅色 `#434343`、深色 `#B4B4B4`）。
    // 原先是两边共用一个 `Color.Gray`（#888888）—— 那个在浅色主题下"变太浅"、深色下又"变太暗"。
    val pressedContentColor = if (isDarkTheme) Color(0xFFB4B4B4) else Color(0xFF434343)
    val pressedIndex = if (dragAnimation.pressProgress > 0.001f) {
        dragAnimation.value.roundToInt().coerceIn(0, tabCount - 1)
    } else {
        -1
    }
    val selectedSurface = if (isDarkTheme) {
        Color.White.copy(alpha = 0.18f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }
    // 按住时颜色加深（深色主题相应地变亮）——随 pressProgress 与"缩小 / 文字变灰"同一条进度。
    val selectedSurfacePressed = if (isDarkTheme) {
        Color.White.copy(alpha = 0.30f)
    } else {
        Color.Black.copy(alpha = 0.19f)
    }
    val selectedSurfaceNow = lerp(
        selectedSurface,
        selectedSurfacePressed,
        dragAnimation.pressProgress.coerceIn(0f, 1f),
    )
    // 玻璃底色（纱）—— **只作用于 `useGlass` 那条路**（`fallbackSurface` 是降级路径，没动）。
    //
    // 2026-11 用户要求浅色下底栏必须是**白色**（原来浅色纱是 `#F2F2F2`，
    // 叠在白色页面上会整体压成灰色 ≈ #F7F7F7，肉眼看起来就是灰的）。
    // 所以浅色纱改成纯白 —— 这样无论底图是白还是浅灰，最终都呈现白色。
    // 深色维持 `#252525` / 0.5 不变。
    val containerSurface = if (isDarkTheme) {
        Color(0xFF252525).copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = 0.625f)
    }
    val fallbackSurface = if (isDarkTheme) {
        Color(0xFF242424).copy(alpha = 0.88f)
    } else {
        Color.White.copy(alpha = 0.82f)
    }
    val capsule = Capsule()
    val shadowColor = Color.Black.copy(alpha = NavBarShadowAlpha)
    val useGlass = advancedMaterial && isRuntimeShaderSupported() && backdrop != null

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(
            Modifier
                .fillMaxWidth(effectiveWidthFraction)
                // 固定间距，不跟随 systemBars inset —— 见 NavBarBottomGap 的说明。
                .padding(bottom = NavBarBottomGap)
                .height(NavBarPillHeight),
            contentAlignment = Alignment.CenterStart,
        ) {
            // 选中块到胶囊边缘的距离（用户 2026-10-04 要求：4dp → 3dp）。
            val innerPadding = NavBarInnerPadding
            val innerWidthPx = with(density) { (maxWidth - innerPadding * 2).toPx() }
            // 选中块宽度根据 tabCount 动态计算：保持块/步进比 NavIndicatorStepRatio，
            // 这样任意 tabCount 下块都"压"在相邻格上（块边缘超出相邻图标中心）。
            val blockWidthPx = calcIndicatorWidthPx(innerWidthPx, tabCount)
            val blockStepPx = ((innerWidthPx - blockWidthPx) / (tabCount - 1).coerceAtLeast(1))
                .coerceAtLeast(1f)
            val iconCentersPx = List(tabCount) { blockWidthPx / 2f + it * blockStepPx }
            val cellBoundsPx = List(tabCount + 1) { idx ->
                when (idx) {
                    0 -> 0f
                    tabCount -> innerWidthPx
                    else -> (iconCentersPx[idx - 1] + iconCentersPx[idx]) / 2f
                }
            }
            val cellWidthsPx = List(tabCount) { cellBoundsPx[it + 1] - cellBoundsPx[it] }
            // 图标在各点击区内的微偏移（两端格子比"图标等距区"宽，所以图标要往里挪 ~6.83px）。
            val iconOffsetsPx = List(tabCount) {
                iconCentersPx[it] - (cellBoundsPx[it] + cellWidthsPx[it] / 2f)
            }
            // 布局值写回：拖动手势与点击回弹都用"块步进"换算（只在成功重组后写入）。
            SideEffect { layoutTabWidthPx.floatValue = blockStepPx }
            val blockWidth = with(density) { blockWidthPx.toDp() }
            val selectedTranslation = dragAnimation.value * blockStepPx
            // 投影层：与胶囊同几何、四周各外扩 NavBarShadowSpread，整体做一次高斯模糊 ——
            // 四周均匀、边缘柔和（像光从看屏幕的方向打过来）。画在胶囊层**之前**所以压在它下面。
            // 注意这里**不用** graphicsLayer 的 elevation 阴影：那套光源固定在上方，只有下缘有投影。
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(
                        width = maxWidth + NavBarShadowSpread * 2,
                        height = NavBarPillHeight + NavBarShadowSpread * 2,
                    )
                    // Unbounded：让模糊往层外扩散，否则会被这层的矩形边界裁掉、露出硬边。
                    .blur(NavBarShadowBlur, BlurredEdgeTreatment.Unbounded)
                    .background(shadowColor, capsule),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // 这一层只剩一个作用：强制自己先去离屏缓冲里合成完，再交给屏幕。
                        //
                        // 这一层的内容是「对内容副本层（RenderNode）的引用 + AGSL 模糊 + 描边高光」，
                        // 滚动时每帧都在变；就地合成容易出现"这一层还没画完、屏幕已经合成到这一帧"
                        // 的时序抖动 —— 观感就是模糊在闪。只在胶囊这么大的范围离屏，开销可忽略。
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .then(
                        if (useGlass) {
                            Modifier
                                // miuix 官方的纹理模糊：内部是「自适应降采样的离屏层 + AGSL 手写
                                // 可分离高斯」，**不是** RenderEffect.createBlurEffect 那条原生模糊 ——
                                // 后者在我们这台真机上会被整条丢掉（HyperChanger 里也留了这条 miuix 路径）。
                                .textureBlur(
                                    backdrop = backdrop,
                                    shape = capsule,
                                    blurRadius = GlassBlurRadiusDp,
                                    // 关掉抗条带噪声：它虽然按坐标生成（不随时间变），
                                    // 但滚动时降采样层的采样网格会微动，噪声图案跟着变，
                                    // 观感上就是"模糊在闪"。
                                    noiseCoefficient = 0f,
                                    // 高光：宽 1.35dp、四周均匀、边缘硬的"干净描边"——
                                    // 不透明度浅色 0.9 / 深色 0.1（自定义 style 关掉了方向性光源，
                                    // 见 NavGlassHighlightLight / NavGlassHighlightDark 的说明）。
                                    highlight = if (isDarkTheme) NavGlassHighlightDark else NavGlassHighlightLight,
                                )
                                // 半透明底色：textureBlur 先画模糊、再 drawContent，
                                // 所以这层底色正好落在「模糊之上、图标文字之下」。
                                .background(containerSurface, capsule)
                        } else {
                            Modifier.clip(capsule).background(fallbackSurface)
                        },
                    ),
            )
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                // 选中块画在文字下面；最下面的透明层负责接收拖动。
                //
                // 外层：位移（跟手 + 回弹 + 越界挤压）+ **等比**按压缩放。
                // 拉伸**不在这里**做 —— 非等比 scaleX 会把胶囊两端的半圆横向拉成椭圆弧
                // （半径被放大 1 + stretch 倍），见内层的说明。
                Box(
                    Modifier
                        .width(blockWidth)
                        .fillMaxHeight()
                        .graphicsLayer {
                            // press 用来换算挤压量：挤压按**局部像素**给（280 × 5% = 14px），
                            // 左缘走的是 translationX（**渲染空间**、1:1），所以这里必须乘 press ——
                            // 右缘走内层 scaleX（本身就带 press），这样两侧才落在同一个渲染尺度上
                            // （否则左缘动 14px、右缘动 12.81px，差 9.3%）。
                            val press = dragAnimation.scaleX.coerceIn(0.01f, 1f)
                            translationX =
                                selectedTranslation + bounceLeft.value + squeezeLeft.value * press
                            scaleX = dragAnimation.scaleX
                            scaleY = dragAnimation.scaleY
                        },
                ) {
                    // 拉伸几何：把长度按方向量分摊到两端，再**各自按可用空间截断**（边界检查优先）。
                    // 返回 (左伸出量, 右伸出量)，单位是**未缩放坐标**（外层盒局部）。
                    //
                    // ⚠️ 不能像以前那样"夹整块的位置 dx"：单侧规则下那样会把钉死端一起推走 ——
                    // 1 号位向右拖时，夹 dx 会让左缘钉回线上、**右缘反而往外伸 63px**，
                    // 正好违背"右缘钉死"。所以改成两端各自算、各自截断。
                    fun stretchSpan(): Pair<Float, Float> {
                        val stretchPx = blockWidthPx * stretchNow.floatValue
                        // ⚠️ **负值也要算**：贴边格飞行的"回弹压缩"并进了长度恢复，长度会缩过头
                        // （`-stretchOverShoot`，见 [stretchOverShoot]）。负伸出量 = "那一端往内收"，
                        // 不会撞边界 —— 下面的 `coerceAtMost(room)` 对它天然无效（负 < 0 ≤ room），
                        // 所以直接照原逻辑走。原来这里的 `<= 0f` 早退会把缩过头整个吃掉。
                        // ⚠️ 惯性形变的两个偏移也必须在早退判据里：块快到位时**速度先跌破
                        // `NavStretchV0` → `stretchPx` 归零**，而那时前缘还有 4~5px 没收回
                        // （`p ≈ 0.875`）。只看 `stretchPx` 会让那几 px **突然消失**、一帧的宽度跳变。
                        if (stretchPx == 0f && flyLeadShift.floatValue == 0f &&
                            flyTrailShift.floatValue == 0f
                        ) {
                            return 0f to 0f
                        }
                        val press = dragAnimation.scaleX.coerceIn(0.01f, 1f)
                        val pivot = blockWidthPx / 2f
                        // 允许的渲染范围 = [gap, 内宽 − gap]（gap = 按压居中缩放在单侧造成的内缩量，
                        // 按压态 = 140 × 0.085 = 11.9px、未按压 = 0），反推回未缩放坐标，
                        // 得到"左缘还能往左多少"和"右缘还能往右多少"。
                        val gap = pivot * (1f - press)
                        val tx = selectedTranslation + bounceLeft.value + squeezeLeft.value * press
                        val leftRoom = (-((gap - tx - pivot) / press + pivot)).coerceAtLeast(0f)
                        val rightRoom =
                            ((innerWidthPx - gap - tx - pivot) / press + pivot - blockWidthPx)
                                .coerceAtLeast(0f)
                        // 方向量：+1 = 块在向右跑 → 右缘钉死、长度全加到左边（拖尾）；−1 时镜像。
                        val leftShare = ((1f + dirNow.floatValue) / 2f).coerceIn(0f, 1f)
                        // **惯性过冲**（中间格飞行，2026-10-06）：前缘前伸 / 后缘内缩，
                        // 两个量都定义成"正 = 朝运动方向"，各按 `leftShare` 分摊：
                        // * `leftShare = 1`（向右跑）→ 前缘 = **右端**（`+lead`，往外伸）、
                        //   后缘 = **左端**（`−trail`，往运动方向缩回去）；
                        // * `leftShare = 0`（向左跑）→ 两者对调，自然镜像。
                        // 因为同向，叠加起来就是**整块过冲 `overshoot`**。
                        val lead = flyLeadShift.floatValue
                        val trail = flyTrailShift.floatValue
                        return (
                            stretchPx * leftShare + lead * (1f - leftShare) - trail * leftShare
                            ).coerceAtMost(leftRoom) to (
                            stretchPx * (1f - leftShare) + lead * leftShare -
                                trail * (1f - leftShare)
                            ).coerceAtMost(rightRoom)
                    }
                    // 内层：长度走**真实布局尺寸**（不是缩放）。
                    //
                    // 为什么必须用真实尺寸：`Capsule` 按实际 size 生成轮廓、半径恒为 高/2 ——
                    // 宽度一变，它自己就是"圆半径不变 + 中间直线段变长"，正是要的
                    // "中间新伸出来一节"。用 graphicsLayer 放大则会把半圆拉扁。
                    //
                    // 位置：长度全加在**运动方向的反侧**（钉死端不动），挨到 gap 线的先停 ——
                    // 见 stretchSpan()。所以贴边格朝外拖时那一端干脆不伸。
                    Box(
                        Modifier
                            .layout { measurable, constraints ->
                                val (left, right) = stretchSpan()
                                val w = (blockWidthPx + left + right).roundToInt().coerceAtLeast(1)
                                val placeable =
                                    measurable.measure(Constraints.fixed(w, constraints.maxHeight))
                                layout(constraints.maxWidth, constraints.maxHeight) {
                                    placeable.place(0, 0)
                                }
                            }
                            .graphicsLayer {
                                val (left, right) = stretchSpan()
                                val w = blockWidthPx + left + right
                                val press = dragAnimation.scaleX.coerceIn(0.01f, 1f)
                                // 两缘各自的目标**渲染**偏移：
                                // * 左缘：外层 translationX，1:1（点击回弹维持原行为）；
                                //   越界挤压的 14px 在外层已乘过 press，所以这里直接相加。
                                // * 右缘：本层的 scaleX，天然带 press。
                                // 即挤压两侧都是"局部 14px → 屏上 12.81px = 按压态 256.2px 的 5%"。
                                val lShift = bounceLeft.value + squeezeLeft.value * press
                                val rRender = (bounceRight.value + squeezeRight.value) * press
                                // 反推本层 scaleX 需要的局部值：左缘位移会被 (1 − press) 的比例
                                // 串扰到右缘上（lShift × 0.1 = 1.26px），这里精确扣掉 ——
                                // 4 号位挤左缘时右缘必须纹丝不动，否则又变回"右缘收缩"。
                                val rLocal = (rRender - lShift * (1f - press)) / press
                                // 左缘落在 −left（宽度已把 left / right 都算进去）。
                                translationX = -left
                                scaleX = 1f + (rLocal - lShift) / w.coerceAtLeast(1f)
                                transformOrigin = TransformOrigin(0f, 0.5f)
                            }
                            .clip(capsule)
                            .background(selectedSurfaceNow),
                    )
                }
                Row(
                    Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val pressedProgress =
                            if (pressedIndex == index) dragAnimation.pressProgress else 0f
                        // 与选中块的颜色加深共用同一条 pressProgress 插值 —— 时机与过渡完全同步。
                        // （原来是 `> 0f` 的二值判断：文字"秒变再秒回"，和块的渐变节奏对不上。）
                        val color = lerp(
                            selectedContentColor,
                            pressedContentColor,
                            pressedProgress.coerceIn(0f, 1f),
                        )
                        // 标签自己的按压源（2026-10-05 用户定：**按下即飞、离开标签才切页**）。
                        // 按下 → 块立刻起飞（0.35s）+ 压上；离开（松开或滑出）→ 恢复 + 翻页。
                        // 长按短触一视同仁，都算"按"，只是持续时长不同 —— 按多久决定压到多深。
                        val tabInteraction = remember(index) { MutableInteractionSource() }
                        val tabPressed by tabInteraction.collectIsPressedAsState()
                        // 首帧 `pressed` 就是 false；没有这个标记的话，初始组合会把每个未选中标签
                        // 都当成"松开过一次"、当场把页面切走。
                        var everPressed by remember(index) { mutableStateOf(false) }
                        LaunchedEffect(tabPressed) {
                            Log.d(
                                NAV_TAG,
                                "tab[$index] pressed=$tabPressed everPressed=$everPressed " +
                                    "selected=${selectedIndex.intValue} hasDragged=${dragAnimation.hasDragged}",
                            )
                            if (tabPressed) {
                                everPressed = true
                                // 复位"真的拖过"标记：拖动层只有 blockWidth 宽、跟着块走，
                                // 按远处的标签时它收不到 down、`onDragStart` 不跑，这个标记就会
                                // **一直粘住** —— 于是之后所有回弹都被 playBounce 的守卫吞掉
                                // （用户 2026-10-05 报的"点几下回弹就没了"）。
                                dragAnimation.resetDragState()
                                dragAnimation.pressFromOutside()
                                tabFlyTarget = index
                                tabFlyToken++
                            } else if (everPressed) {
                                dragAnimation.releaseFromOutside()
                                if (index != selectedIndex.intValue) {
                                    suppressSync = true
                                    onTabSelected(index)
                                }
                            }
                        }
                        Column(
                            // 点击区 = 相邻图标中线划出的格子（不等宽：两端更宽）—— 2026-10-04 用户定稿。
                            Modifier
                                .weight(cellWidthsPx[index])
                                .fillMaxHeight()
                                // 按住时**不缩放**（2026-09-29 用户要求）。原来这里有一层
                                // `graphicsLayer { scaleX = scaleY = lerp(1f, 0.85f, pressedProgress) }`，
                                // 长按会把图标与标题一起缩到 0.85 —— 已去掉。
                                // 按压的反馈只保留"变灰"（见上面的 `color`）。
                                // ⚠️ 胶囊（选中块）自己的 `pressedScale = 0.915f` 是另一回事，那个保留。
                                // 切换改由上面的 `tabInteraction` 驱动（按下飞、松手切），
                                // 所以这里的 onClick 是空的 —— 留着只是为了拿到按压事件。
                                .clickable(
                                    interactionSource = tabInteraction,
                                    indication = null,
                                    role = Role.Tab,
                                ) { },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            // 内容整体做光学微调：x 走"图标等距 vs 点击区中心"的差值，
                            // y 走 NavContentOpticalOffset（抵消文字行框底部留白）。
                            Column(
                                Modifier.offset(
                                    x = with(density) { iconOffsetsPx[index].toDp() },
                                    y = NavContentOpticalOffset,
                                ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                if (showIcons) {
                                    Image(
                                        tab.icon,
                                        null,
                                        Modifier.size(NavIconBox),
                                        colorFilter = ColorFilter.tint(color),
                                    )
                                }
                                if (showLabels) {
                                    BasicText(
                                        tab.label,
                                        style = TextStyle(
                                            color = color,
                                            fontSize = if (showIcons) NavLabelSize else NavLabelSizeOnly,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center,
                                        ),
                                        modifier = if (showIcons) {
                                            Modifier.padding(top = NavIconLabelGap)
                                        } else {
                                            Modifier
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
                Box(
                    Modifier
                        .width(blockWidth)
                        .fillMaxHeight()
                        .graphicsLayer {
                            translationX = selectedTranslation
                            scaleX = dragAnimation.scaleX
                            scaleY = dragAnimation.scaleY
                        }
                        .then(dragAnimation.modifier),
                )
            }
        }
    }
}
