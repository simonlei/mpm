package com.simon.mpm.feature.photos

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import java.io.File

/**
 * 视频缓存管理器
 * 使用单例模式管理视频缓存
 */
private object VideoCacheManager {
    private var simpleCache: SimpleCache? = null
    
    fun getCache(context: android.content.Context): SimpleCache {
        if (simpleCache == null) {
            val cacheDir = File(context.cacheDir, "video_cache")
            val cacheEvictor = LeastRecentlyUsedCacheEvictor(500 * 1024 * 1024) // 500MB 缓存
            simpleCache = SimpleCache(cacheDir, cacheEvictor, androidx.media3.database.StandaloneDatabaseProvider(context))
        }
        return simpleCache!!
    }
}

/**
 * 视频播放器组件
 * 使用 Media3 ExoPlayer 实现，支持缓存和流式播放
 */
@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    
    // 创建 ExoPlayer 实例，配置缓存和加载策略
    val exoPlayer = remember {
        // 配置更激进的加载策略
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                15000,  // 最小缓冲时长 15秒（默认50秒）
                30000,  // 最大缓冲时长 30秒（默认50秒）
                1500,   // 播放前缓冲 1.5秒（默认2.5秒）
                2000    // 重新缓冲 2秒（默认5秒）
            )
            .build()
        
        // 配置 HTTP 数据源
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setConnectTimeoutMs(10000)
            .setReadTimeoutMs(10000)
            .setAllowCrossProtocolRedirects(true)
        
        // 配置缓存数据源
        val cache = VideoCacheManager.getCache(context)
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        
        // 创建 MediaSource 工厂
        val mediaSourceFactory = DefaultMediaSourceFactory(cacheDataSourceFactory)
        
        ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                val mediaItem = MediaItem.fromUri(videoUrl)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = false
                
                // 监听播放状态
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        isPlaying = playing
                    }
                })
            }
    }
    
    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // ExoPlayer 视图
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false  // 使用自定义控制器
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // 自定义播放控制按钮
        if (showControls) {
            FloatingActionButton(
                onClick = {
                    if (isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
