import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileOutputStream
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

    var lineIndex = 0
    File("log.txt").readLines().map { line ->
        val timeIndex = line.indexOf(timeKeyword).takeIf { it >= 0 } ?: return@map
        val speedIndex = line.indexOf(speedKeyword).takeIf { it >= 0 } ?: return@map
        val speedAccuracyIndex = line.indexOf(speedAccuracyKeyword).takeIf { it >= 0 } ?: return@map
        val hasSpeedAccuracyIndex = line.indexOf(hasSpeedAccuracyKeyword).takeIf { it >= 0 } ?: return@map
        val intervalIndex = line.indexOf(intervalKeyword).takeIf { it >= 0 } ?: return@map
        val accelerationIndex = line.indexOf(accelerationKeyword).takeIf { it >= 0 } ?: return@map

        val time = line.substring(timeIndex + timeKeyword.length, timeIndex + timeKeyword.length + 12)
        val speed = line.substring(speedIndex + speedKeyword.length, speedAccuracyIndex).toDouble()
        val speedAccuracy = line.substring(speedAccuracyIndex + speedAccuracyKeyword.length, hasSpeedAccuracyIndex).toDouble()
        val hasSpeedAccuracy = line.substring(hasSpeedAccuracyIndex + hasSpeedAccuracyKeyword.length, intervalIndex).toBoolean()//.compareTo(false).toString()
        val interval = line.substring(intervalIndex + intervalKeyword.length, accelerationIndex).toDouble()
        val acceleration = line.substring(accelerationIndex + accelerationKeyword.length).toDouble()

        if (interval >= 1100 || interval < 900) return@map

        val refinedSpeedAccuracy = when {
            hasSpeedAccuracy.not() -> 3.0
            speedAccuracy > 3 -> 3.0
            else -> speedAccuracy
        }

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

    val file = FileOutputStream("log.xls")
    book.write(file)
    book.close()
    file.close()
}