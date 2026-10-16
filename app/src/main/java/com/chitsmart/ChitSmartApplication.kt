package com.chitsmart

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class — entry point for Hilt dependency injection.
 * All ViewModels, repositories, and services are provided via Hilt modules.
 */
@HiltAndroidApp
class ChitSmartApplication : Application()
