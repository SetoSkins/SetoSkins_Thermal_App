package com.setoskins.thermal.ui.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import top.yukonga.miuix.kmp.basic.ScrollBarColors
import top.yukonga.miuix.kmp.basic.ScrollBarDefaults
import top.yukonga.miuix.kmp.basic.VerticalScrollBar as MiuixVerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter as rememberMiuixScrollBarAdapter
import top.yukonga.miuix.kmp.interfaces.ExperimentalScrollBarApi

/**
 * A wrapper for the official Miuix VerticalScrollBar to maintain compatibility with existing code.
 */
@OptIn(ExperimentalScrollBarApi::class)
@Composable
fun VerticalScrollBar(
    adapter: top.yukonga.miuix.kmp.basic.ScrollBarAdapter,
    modifier: Modifier = Modifier,
    reverseLayout: Boolean = false,
    trackPadding: PaddingValues = PaddingValues(0.dp),
    thumbColor: Color = Color.Unspecified,
    trackColor: Color = Color.Unspecified,
    thumbWidth: Dp = ScrollBarDefaults.ThumbWidth,
    endPadding: Dp = ScrollBarDefaults.EndPadding,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        MiuixVerticalScrollBar(
            adapter = adapter,
            modifier = modifier.align(Alignment.CenterEnd),
            reverseLayout = reverseLayout,
            trackPadding = trackPadding,
            colors = ScrollBarDefaults.scrollBarColors(
                thumbColor = thumbColor,
                trackColor = trackColor
            ),
            thumbWidth = thumbWidth,
            endPadding = endPadding
        )
    }
}

/**
 * Adapter creation wrappers to maintain compatibility.
 */
@OptIn(ExperimentalScrollBarApi::class)
@Composable
fun rememberScrollBarAdapter(scrollState: ScrollState) = rememberMiuixScrollBarAdapter(scrollState)

@OptIn(ExperimentalScrollBarApi::class)
@Composable
fun rememberScrollBarAdapter(lazyListState: LazyListState) = rememberMiuixScrollBarAdapter(lazyListState)

@OptIn(ExperimentalScrollBarApi::class)
@Composable
fun rememberScrollBarAdapter(lazyGridState: LazyGridState) = rememberMiuixScrollBarAdapter(lazyGridState)
