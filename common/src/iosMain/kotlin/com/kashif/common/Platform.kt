@file:OptIn(ExperimentalForeignApi::class)

package com.kashif.common

import CVPObserver.CVPObserverProtocol
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import io.ktor.client.engine.darwin.*
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.dsl.module
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.play
import platform.AVKit.AVPlayerViewController
import platform.CoreGraphics.CGRect
import platform.Foundation.NSKeyValueObservingOptionNew
import platform.Foundation.NSURL
import platform.Foundation.addObserver
import platform.Foundation.removeObserver
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIColor
import platform.UIKit.UIResponder
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.darwin.NSObject


actual fun platformModule() = module {
    single {
        Darwin.create()
    }
}



internal fun UIView.parent(count: Int): UIView? {
    if (count <= 0) return null
    var c = count - 1
    var v: UIView? = this.superview
    while ((c-- > 0 && v != null)) {
        v = v.superview
    }
    return v
}

/**
 * UIKitView factory maybe reattach to window,Observe backgroundColor change
 */
internal class InteropWrappingViewWatching(val view: UIView) : NSObject(), CVPObserverProtocol {
    override fun observeValueForKeyPath(
        keyPath: String?,
        ofObject: Any?,
        change: Map<Any?, *>?,
        context: COpaquePointer?
    ) {
        if (keyPath == "backgroundColor") {
            if (view.backgroundColor != UIColor.clearColor) {
                view.opaque = true
                view.backgroundColor = UIColor.clearColor
                view.setTag(10088)
                view.removeObserver(this, "backgroundColor")
            }
        }
    }
}

internal fun UIView.removeInteropWrappingViewColor(scope: CoroutineScope) {
    if (tag.toInt() != 10087 && tag.toInt() != 10088) {
        opaque = true
        backgroundColor = UIColor.clearColor
        val parent = superview ?: return
        val obs = InteropWrappingViewWatching(parent)
        findViewController()?.apply controller@{
            scope.launch {
                var notStop = true
                while (notStop) {
                    withContext(Dispatchers.IO) {
                        delay(100)
                        withContext(Dispatchers.Main) {
                            if (this@controller.isViewLoaded()) {
                                notStop = false
                                parent.apply {
                                    opaque = true
                                    backgroundColor = UIColor.clearColor
                                    setTag(10087)
                                    addObserver(
                                        obs,
                                        forKeyPath = "backgroundColor",
                                        options = NSKeyValueObservingOptionNew,
                                        context = null
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

internal fun UIView.removeControllerColor(scope: CoroutineScope) {
    if (tag.toInt() != 10086) {
        opaque = true
        backgroundColor = UIColor.clearColor
        findViewController()?.apply controller@{
            scope.launch {
                var notStop = true
                while (notStop) {
                    withContext(Dispatchers.IO) {
                        delay(100)
                        withContext(Dispatchers.Main) {
                            if (this@controller.isViewLoaded()) {
                                notStop = false
                                this@controller.view.opaque = true
                                this@controller.view.backgroundColor = UIColor.clearColor
                                this@controller.view.setTag(10086)
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun UIView.findViewController(): UIViewController? {
    var nextResponder: UIResponder? = this
    while (nextResponder != null) {
        if (nextResponder is UIViewController) {
            return nextResponder
        }
        nextResponder = nextResponder.nextResponder
    }
    return null
}


@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(modifier: Modifier, url: String) {
    val player = remember { AVPlayer(uRL = NSURL.URLWithString(url)!!) }
    val playerLayer = remember { AVPlayerLayer() }
    val avPlayerViewController = remember { AVPlayerViewController() }
    avPlayerViewController.player = player
    avPlayerViewController.showsPlaybackControls = true
    playerLayer.backgroundColor= UIColor.clearColor.CGColor
    playerLayer.player = player
    val coroutineScope = rememberCoroutineScope()
    // Use a UIKitView to integrate with your existing UIKit views
    UIKitView(
        factory = {
            // Create a UIView to hold the AVPlayerLayer
            val playerContainer = UIView()
            playerContainer.addSubview(avPlayerViewController.view.apply {
//                backgroundColor = UIColor.clearColor
            })
            // Return the playerContainer as the root UIView
            playerContainer
        },
        onResize = { view: UIView, rect: CValue<CGRect> ->
            CATransaction.begin()
            CATransaction.setValue(true, kCATransactionDisableActions)
            view.layer.setFrame(rect)
            playerLayer.setFrame(rect)
            avPlayerViewController.view.layer.frame = rect
            CATransaction.commit()
        },
        update = { view ->
            view.backgroundColor=UIColor.clearColor
            view.layer.backgroundColor=UIColor.clearColor.CGColor
            view.removeInteropWrappingViewColor(coroutineScope)
            view.parent(4)?.removeControllerColor(coroutineScope)
            player.play()
            avPlayerViewController.player!!.play()
        },
        modifier = modifier)
}
