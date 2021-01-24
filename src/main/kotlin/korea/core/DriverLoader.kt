package korea.core

import korea.Main.Companion.pluginRoot
import arc.Core
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.net.URLClassLoader
import java.net.URLConnection
import java.sql.*
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow


class DriverLoader : Driver {
    private var tried = false
    private lateinit var driver: Driver

    companion object{
        lateinit var h2: URLClassLoader
    }

    constructor(driver: Driver?) {
        requireNotNull(driver) { "Driver must not be null." }
        this.driver = driver
    }

    constructor()

    fun init() {
        try {
            val cla = URLClassLoader(arrayOf(pluginRoot.child("Driver/h2.jar").file().toURI().toURL()), this.javaClass.classLoader)
            val driver = Class.forName("org.h2.Driver", true, cla).getDeclaredConstructor().newInstance() as Driver
            DriverManager.registerDriver(DriverLoader(driver))
            h2 = cla
        } catch (e: Throwable) {
            if (!tried) {
                tried = true
                download()
            } else {
                e.printStackTrace()
                Core.app.exit()
            }
        }
    }

    private fun download() {
        try {
            pluginRoot.child("Driver/h2.jar").writeString("")
            download(pluginRoot.child("Driver/h2.jar").file(), URL("https://repo1.maven.org/maven2/com/h2database/h2/1.4.200/h2-1.4.200.jar"))
            init()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    @Throws(SQLException::class)
    override fun connect(url: String, info: Properties): Connection {
        return driver.connect(url, info)
    }

    @Throws(SQLException::class)
    override fun acceptsURL(url: String): Boolean {
        return driver.acceptsURL(url)
    }

    @Throws(SQLException::class)
    override fun getPropertyInfo(url: String, info: Properties): Array<DriverPropertyInfo> {
        return driver.getPropertyInfo(url, info)
    }

    override fun getMajorVersion(): Int {
        return driver.majorVersion
    }

    override fun getMinorVersion(): Int {
        return driver.minorVersion
    }

    override fun jdbcCompliant(): Boolean {
        return driver.jdbcCompliant()
    }

    @Throws(SQLFeatureNotSupportedException::class)
    override fun getParentLogger(): Logger {
        return driver.parentLogger
    }

    fun download(path: File, url: URL){
        try {
            val outputStream = BufferedOutputStream(FileOutputStream(path))
            val urlConnection: URLConnection = url.openConnection()
            val `is`: InputStream = urlConnection.getInputStream()
            val size: Int = urlConnection.contentLength
            val buf = ByteArray(256)
            var byteRead: Int
            var byteWritten = 0
            val startTime = System.currentTimeMillis()
            while (`is`.read(buf).also { byteRead = it } != -1) {
                outputStream.write(buf, 0, byteRead)
                byteWritten += byteRead
                printProgress(startTime, size, byteWritten)
            }
            `is`.close()
            outputStream.close()
        } catch (e: Throwable) {
        }
    }

    private fun printProgress(startTime: Long, total: Int, remain: Int) {
        val eta = if (remain == 0) 0 else (total - remain) * (System.currentTimeMillis() - startTime) / remain
        val etaHms = if (total == 0) "N/A" else String.format(
            "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
            TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1)
        )
        require(remain <= total)
        val maxBareSize = 20
        val remainProcent = 20 * remain / total
        val defaultChar = '-'
        val icon = "*"
        val bare = String(CharArray(maxBareSize)).replace('\u0000', defaultChar) + "]"
        val bareDone = StringBuilder()
        bareDone.append("[")
        for (i in 0 until remainProcent) {
            bareDone.append(icon)
        }
        val bareRemain = bare.substring(remainProcent)
        print(
            "\r" + humanReadableByteCount(remain, true) + "/" + humanReadableByteCount(
                total,
                true
            ) + "\t" + bareDone.toString() + bareRemain + " " + (remainProcent * 5).toString() + "%, ETA: " + etaHms
        )
        if (remain == total) {
            print("\n")
        }
    }

    @Strictfp
    fun humanReadableByteCount(bytes: Int, si: Boolean): String {
        var ba = bytes
        val unit = if (si) 1000 else 1024
        val absBytes = abs(ba).toLong()
        if (absBytes < unit) return "$ba B"
        var exp = (ln(absBytes.toDouble()) / ln(unit.toDouble())).toInt()
        val th = (unit.toDouble().pow(exp.toDouble()) * (unit - 0.05)).toLong()
        if (exp < 6 && absBytes >= th - (if (th and 0xfff == 0xd00L) 52 else 0)) exp++
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1].toString() + if (si) "" else "i"
        if (exp > 4) {
            ba /= unit
            exp -= 1
        }
        return String.format("%.1f %sB", ba / unit.toDouble().pow(exp.toDouble()), pre)
    }
}