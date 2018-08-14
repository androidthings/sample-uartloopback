/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.loopback

import android.os.Build

object BoardDefaults {
    private const val DEVICE_RPI3 = "rpi3"
    private const val DEVICE_RPI3BP = "rpi3bp"
    private const val DEVICE_IMX7D_PICO = "imx7d_pico"

    /**
     * Return the default UART for loopback.
     */
    val uartName = when (Build.DEVICE) {
        DEVICE_RPI3, DEVICE_RPI3BP -> "UART0"
        DEVICE_IMX7D_PICO -> "UART6"
        else -> throw IllegalStateException("Unknown Build.DEVICE ${Build.DEVICE}")
    }

    /** UART Configuration Parameters */
    const val BAUD_RATE = 115200
    const val DATA_BITS = 8
    const val STOP_BITS = 1

    const val CHUNK_SIZE = 512
}
