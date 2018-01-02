package com.openkotlin.socksdroid

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import java.net.Socket

class MainActivity : AppCompatActivity() {

    val headr: ByteArray = byteArrayOf(0x05, 0x01, 0x02)
    val auth: ByteArray = byteArrayOf( /* Packet 107 */
            0x01, 0x0c, 0x63, 0x6f, 0x6e, 0x6e, 0x65, 0x63,
            0x74, 0x5f, 0x70, 0x6c, 0x75, 0x73, 0x05, 0x35,
            0x35, 0x35, 0x35, 0x35)
    val dns: ByteArray = byteArrayOf( 0x05, 0x01, 0x00, 0x03, 0x0d, 0x77, 0x77, 0x77,
            0x2e, 0x62, 0x61, 0x69, 0x64, 0x75, 0x2e, 0x63,
            0x6f, 0x6d, 0x00, 0x50)
    val real: ByteArray = byteArrayOf(0x47, 0x45, 0x54, 0x20, 0x2f, 0x20, 0x48, 0x54,
            0x54, 0x50, 0x2f, 0x31, 0x2e, 0x31, 0x0d, 0x0a,
            0x48, 0x6f, 0x73, 0x74, 0x3a, 0x20, 0x77, 0x77,
            0x77, 0x2e, 0x62, 0x61, 0x69, 0x64, 0x75, 0x2e,
            0x63, 0x6f, 0x6d, 0x3a, 0x38, 0x30, 0x0d, 0x0a,
            0x0d, 0x0a)
    var result = true
    companion object {
        val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Thread({
            val socketClient = Socket("192.168.1.2", 1080)
            val sOs = socketClient.getOutputStream()
            val sIs = socketClient.getInputStream()
            sOs.use {
                sOs.write(headr)
                while (result) {
                    var length = sIs.available()
                    Thread.sleep(500)
                    while (length > 0) {
                        var content = ByteArray(length)
                        sIs.read(content)
                        Log.d(TAG, "server rsp:" + content.contentToString())
                        when (length) {
                            2 -> {
                                if (content[0] == 0x05.toByte() && content[1] == 0x02.toByte()) {
                                    sOs.write(auth)
                                    length = 0
                                } else if(content[0] == 0x01.toByte() && content[1] == 0x00.toByte()) {
                                    sOs.write(dns)
                                    length = 0
                                }
                            }
                            10->{
                                sOs.write(real)
                                sOs.flush()
                                length = 0
                            }
                            else->{
                                Log.d(TAG, "final server rsp:" + String(content))
                                length = 0
                                result = false
                            }
                        }
                    }
                }
            }
        }).start()

    }
}
