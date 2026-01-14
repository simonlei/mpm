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
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.HostnameVerifier

/**
 * 视频缓存管理器
 * 使用单例模式管理视频缓存
 */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
private object VideoCacheManager {
    private var simpleCache: SimpleCache? = null
    
    fun getCache(context: android.content.Context): SimpleCache {
        if (simpleCache == null) {
            val cacheDir = File(context.cacheDir, "video_cache")
            // 增加缓存大小到1GB，提升缓存命中率
            val cacheEvictor = LeastRecentlyUsedCacheEvictor(1024 * 1024 * 1024L) // 1GB 缓存
            simpleCache = SimpleCache(cacheDir, cacheEvictor, androidx.media3.database.StandaloneDatabaseProvider(context))
        }
        return simpleCache!!
    }
}

/**
 * SSL配置工具
 * 用于创建信任所有证书的SSLSocketFactory（仅用于开发环境）
 * 注意：生产环境应使用正确的证书验证
 */
private object SSLHelper {
    /**
     * 创建信任所有证书的TrustManager
     */
    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })
    
    /**
     * 创建信任所有主机名的HostnameVerifier
     */
    val trustAllHostnameVerifier = HostnameVerifier { _, _ -> true }
    
    /**
     * 获取信任所有证书的SSLSocketFactory
     */
    fun getTrustAllSSLSocketFactory(): SSLSocketFactory {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        return sslContext.socketFactory
    }
}

/**
 * 视频播放器组件
 * 使用 Media3 ExoPlayer 实现，支持缓存和流式播放
 */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var bufferedPercentage by remember { mutableStateOf(0) }
    
    // 创建 ExoPlayer 实例，配置缓存和加载策略
    val exoPlayer = remember {
        // 配置更激进的加载策略，优化首次加载速度
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                5000,   // 最小缓冲时长 5秒（减少初始缓冲）
                20000,  // 最大缓冲时长 20秒（保持合理缓冲）
                500,    // 播放前缓冲 0.5秒（快速开始播放）
                1000    // 重新缓冲 1秒（快速恢复播放）
            )
            .setPrioritizeTimeOverSizeThresholds(true)  // 优先考虑时间而非大小
            .build()
        
        // 配置 HTTP 数据源，优化网络请求并支持HTTPS自签名证书
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setConnectTimeoutMs(5000)      // 连接超时 5秒（更快失败重试）
            .setReadTimeoutMs(8000)         // 读取超时 8秒
            .setAllowCrossProtocolRedirects(true)
            .setUserAgent("MPM-Android/1.0")  // 添加用户代理，避免服务器限速
            .setKeepPostFor302Redirects(true) // 保持重定向时的POST方法
        
        // 为HTTPS配置SSL证书信任（支持自签名证书）
        try {
            httpDataSourceFactory.setDefaultRequestProperties(mapOf())
            // 注意：Media3的DefaultHttpDataSource内部使用HttpURLConnection
            // 我们需要通过全局配置来设置SSL
            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(
                SSLHelper.getTrustAllSSLSocketFactory()
            )
            javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
                SSLHelper.trustAllHostnameVerifier
            )
        } catch (e: Exception) {
            android.util.Log.e("VideoPlayer", "Failed to configure SSL: ${e.message}")
        }
        
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
                
                // 监听播放状态和缓冲状态
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        isPlaying = playing
                    }
                    
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        isBuffering = playbackState == Player.STATE_BUFFERING
                        
                        // 当视频播放完成时，重置播放位置到开头
                        if (playbackState == Player.STATE_ENDED) {
                            seekTo(0)
                            isPlaying = false
                        }
                    }
                })
                
                // 监听缓冲进度
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            bufferedPercentage = bufferedPosition.toInt() * 100 / duration.toInt().coerceAtLeast(1)
                        }
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
        
        // 加载指示器
        if (isBuffering) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "加载中...",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // 自定义播放控制按钮
        if (showControls && !isBuffering) {
            FloatingActionButton(
                onClick = {
                    if (isPlaying) {
                        exoPlayer.pause()
                    } else {
                        // 如果播放器处于结束状态，先重置到开头
                        if (exoPlayer.playbackState == Player.STATE_ENDED) {
                            exoPlayer.seekTo(0)
                        }
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
