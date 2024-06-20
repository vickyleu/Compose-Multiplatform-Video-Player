package com.kashif.common

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

@Suppress("unused","FunctionName")
fun MainViewController(): UIViewController = ComposeUIViewController{
    App("Ios")
}