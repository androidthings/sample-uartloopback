/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.loopback

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log

import com.google.android.things.pio.PeripheralManager
import com.google.android.things.pio.UartDevice
import com.google.android.things.pio.UartDeviceCallback

private const val TAG = "LoopbackActivity"

/**
 * Example activity that provides a UART loopback on the
 * specified device. All data received at the specified
 * baud rate will be transferred back out the same UART.
 */
class LoopbackActivity : Activity() {

    lateinit var inputThread: HandlerThread
    lateinit var inputHandler: Handler

    lateinit var peripheralManager: PeripheralManager
    lateinit var loopbackDevice: UartDevice

    /**
     * Callback invoked when UART receives new incoming data.
     */
    private val callback = object : UartDeviceCallback {
        override fun onUartDeviceDataAvailable(uart: UartDevice): Boolean {
            // Queue up a data transfer
            transferUartData()
            // Continue listening for more interrupts
            return true
        }

        override fun onUartDeviceError(uart: UartDevice?, error: Int) {
            Log.w(TAG, "$uart: Error event $error")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Loopback Created")

        // Create a background looper thread for I/O
        inputThread = HandlerThread("InputThread")
        inputThread.start()
        inputHandler = Handler(inputThread.looper)

        // Attempt to access the UART device
        peripheralManager = PeripheralManager.getInstance()
        loopbackDevice = openUart(BoardDefaults.uartName, BoardDefaults.BAUD_RATE)
        loopbackDevice.registerUartDeviceCallback(inputHandler, callback)

        // Read any initially buffered data
        inputHandler.post { transferUartData() }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Loopback Destroyed")

        // Terminate the worker thread
        inputThread.quitSafely()

        // Attempt to close the UART device
        loopbackDevice.unregisterUartDeviceCallback(callback)
        loopbackDevice.close()
    }

    /* Private Helper Methods */

    /**
     * Access and configure the requested UART device for 8N1.
     *
     * @param name Name of the UART peripheral device to open.
     * @param baudRate Data transfer rate. Should be a standard UART baud,
     * such as 9600, 19200, 38400, 57600, 115200, etc.
     */
    private fun openUart(name: String, baudRate: Int): UartDevice {
        return peripheralManager.openUartDevice(name).apply {
            // Configure the UART
            setBaudrate(baudRate)
            setDataSize(BoardDefaults.DATA_BITS)
            setParity(UartDevice.PARITY_NONE)
            setStopBits(BoardDefaults.STOP_BITS)
        }
    }

    /**
     * Loop over the contents of the UART RX buffer, transferring each
     * one back to the TX buffer to create a loopback service.
     *
     * Potentially long-running operation. Call from a worker thread.
     */
    private fun transferUartData() {
        // Loop until there is no more data in the RX buffer.
        val buffer = ByteArray(BoardDefaults.CHUNK_SIZE)
        do {
            val read = loopbackDevice.read(buffer, buffer.size)
            if (read > 0) {
                loopbackDevice.write(buffer, read)
            }
        } while (read > 0)
    }
}
