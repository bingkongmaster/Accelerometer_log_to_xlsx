import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

fun main(args: Array<String>) {

    val book = HSSFWorkbook()
    val sheet = book.createSheet()

    val timeKeyword = "T"
    val speedKeyword = "speed:"
    val speedAccuracyKeyword = ",speedAccuracy:"
    val hasSpeedAccuracyKeyword = ",hasSpeedAccuracy:"
    val intervalKeyword = ",interval:"
    val accelerationKeyword = ",acceleration:"

    val firstRow = sheet.createRow(0)
    firstRow.createCell(0).setCellValue("time")
    firstRow.createCell(1).setCellValue("speed")
    firstRow.createCell(2).setCellValue("speedAccuracy")
    firstRow.createCell(3).setCellValue("hasSpeedAccuracy")
    firstRow.createCell(4).setCellValue("acceleration")
    firstRow.createCell(5).setCellValue("acceleration(abs)")
    firstRow.createCell(6).setCellValue("interval")
    firstRow.createCell(7).setCellValue("totalSeconds")
    firstRow.createCell(8).setCellValue("accurateSpeedCount")
    firstRow.createCell(9).setCellValue("accurateAccelerationCount")

    var saveTimeMillis = 0L
    var saveRefinedSpeedAccuracy = 3.0
    var accurateSpeedCount = 0
    var accurateAccelerationCount = 0
    val speedAccuracyFilter = 1.8
    var startTime: Long? = null
    var endTime: Long? = null

    var lineIndex = 0
    File("log.txt").readLines().mapIndexed { _, line ->
        val timeIndex = line.indexOf(timeKeyword).takeIf { it >= 0 } ?: return@mapIndexed
        val speedIndex = line.indexOf(speedKeyword).takeIf { it >= 0 } ?: return@mapIndexed
        val speedAccuracyIndex = line.indexOf(speedAccuracyKeyword).takeIf { it >= 0 } ?: return@mapIndexed
        val hasSpeedAccuracyIndex = line.indexOf(hasSpeedAccuracyKeyword).takeIf { it >= 0 } ?: return@mapIndexed
        val intervalIndex = line.indexOf(intervalKeyword).takeIf { it >= 0 } ?: return@mapIndexed
        val accelerationIndex = line.indexOf(accelerationKeyword).takeIf { it >= 0 } ?: return@mapIndexed

        val time = line.substring(timeIndex + timeKeyword.length, timeIndex + timeKeyword.length + 12)
        val speed = line.substring(speedIndex + speedKeyword.length, speedAccuracyIndex).toDouble()
        val speedAccuracy = line.substring(speedAccuracyIndex + speedAccuracyKeyword.length, hasSpeedAccuracyIndex).toDouble()
        val hasSpeedAccuracy = line.substring(hasSpeedAccuracyIndex + hasSpeedAccuracyKeyword.length, intervalIndex).toBoolean()//.compareTo(false).toString()
        val interval = line.substring(intervalIndex + intervalKeyword.length, accelerationIndex).toDouble()
        val acceleration = line.substring(accelerationIndex + accelerationKeyword.length).toDouble()

        val refinedSpeedAccuracy = when {
            hasSpeedAccuracy.not() -> 3.0
            speedAccuracy > 3 -> 3.0
            else -> speedAccuracy
        }

        val hour = time.substring(0..1).toLong()
        val min = time.substring(3..4).toLong()
        val sec = time.substring(6..7).toLong()
        val milliSec = time.substring(9..11).toLong()
        val timeMillis = milliSec + TimeUnit.SECONDS.toMillis(sec) + TimeUnit.MINUTES.toMillis(min) + TimeUnit.HOURS.toMillis(hour)
        if (startTime == null) {
            startTime = timeMillis
        }
        endTime = timeMillis

        if (refinedSpeedAccuracy < speedAccuracyFilter) {
            accurateSpeedCount++
        }

        if (refinedSpeedAccuracy < speedAccuracyFilter &&
            saveRefinedSpeedAccuracy < speedAccuracyFilter &&
            interval.toInt() in 999..1001
        ) {
            accurateAccelerationCount++
        }

        saveRefinedSpeedAccuracy = refinedSpeedAccuracy
        saveTimeMillis = timeMillis

        val row = sheet.createRow(lineIndex+1)
        row.createCell(0).setCellValue(time)
        row.createCell(1).setCellValue(speed)
        row.createCell(2).setCellValue(refinedSpeedAccuracy)
        row.createCell(3).setCellValue(hasSpeedAccuracy)
        row.createCell(4).setCellValue(acceleration)
        row.createCell(5).setCellValue(acceleration.absoluteValue)
        row.createCell(6).setCellValue(interval)

        lineIndex++
    }

    val secondRow = sheet.getRow(1)
    if (startTime != null && endTime != null) {
        secondRow.createCell(7).setCellValue(((endTime!!-startTime!!)/1000).toString())
    }
    secondRow.createCell(8).setCellValue(accurateSpeedCount.toString())
    secondRow.createCell(9).setCellValue(accurateAccelerationCount.toString())

    val file = FileOutputStream("log.xls")
    book.write(file)
    book.close()
    file.close()
}