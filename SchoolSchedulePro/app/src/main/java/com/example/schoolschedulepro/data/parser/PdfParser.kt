package com.example.schoolschedulepro.data.parser

import android.util.Log
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.InputStream

class PdfParser {
    
    companion object {
        private const val TAG = "PdfParser"
    }
    
    /**
     * Парсит PDF файл с расписанием
     * Ожидается структура таблицы с днями недели и парами
     */
    suspend fun parseSchedule(inputStream: InputStream): ParseResult {
        return try {
            val document = PDDocument.load(inputStream)
            val stripper = PDFTextStripper()
            val text = stripper.getText(document)
            document.close()
            
            Log.d(TAG, "Parsed PDF text: $text")
            
            // Простой парсинг - разбиваем по строкам
            val lines = text.lines()
            val scheduleItems = mutableListOf<ParsedScheduleItem>()
            
            var currentDay = -1
            var currentPair = 0
            
            for (line in lines) {
                val trimmedLine = line.trim()
                if (trimmedLine.isEmpty()) continue
                
                // Определяем день недели
                currentDay = when {
                    trimmedLine.contains("понедельник", ignoreCase = true) || 
                    trimmedLine.equals("ПН", ignoreCase = true) -> 1
                    trimmedLine.contains("вторник", ignoreCase = true) || 
                    trimmedLine.equals("ВТ", ignoreCase = true) -> 2
                    trimmedLine.contains("среда", ignoreCase = true) || 
                    trimmedLine.equals("СР", ignoreCase = true) -> 3
                    trimmedLine.contains("четверг", ignoreCase = true) || 
                    trimmedLine.equals("ЧТ", ignoreCase = true) -> 4
                    trimmedLine.contains("пятница", ignoreCase = true) || 
                    trimmedLine.equals("ПТ", ignoreCase = true) -> 5
                    trimmedLine.contains("суббота", ignoreCase = true) || 
                    trimmedLine.equals("СБ", ignoreCase = true) -> 6
                    trimmedLine.contains("воскресенье", ignoreCase = true) || 
                    trimmedLine.equals("ВС", ignoreCase = true) -> 7
                    else -> currentDay
                }
                
                // Пытаемся найти номер пары
                val pairMatch = Regex("(\\d+)\\s*[.)]").find(trimmedLine)
                if (pairMatch != null) {
                    currentPair = pairMatch.groupValues[1].toInt()
                }
                
                // Если есть день и пара, пытаемся извлечь предмет и аудиторию
                if (currentDay in 1..7 && currentPair in 1..5) {
                    val parts = trimmedLine.split(Regex("\\s{2,}"), limit = 3)
                    
                    val subject = parts.getOrNull(0)?.trim()?.takeIf { it.isNotEmpty() }
                    val room = parts.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }
                    
                    if (subject != null && subject.length > 2) {
                        scheduleItems.add(
                            ParsedScheduleItem(
                                dayOfWeek = currentDay,
                                pairNumber = currentPair,
                                numeratorSubject = subject,
                                numeratorRoom = room
                            )
                        )
                    }
                }
            }
            
            inputStream.close()
            ParseResult.Success(scheduleItems)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing PDF", e)
            inputStream.close()
            ParseResult.Error(e.message ?: "Неизвестная ошибка парсинга")
        }
    }
}

sealed class ParseResult {
    data class Success(val items: List<ParsedScheduleItem>) : ParseResult()
    data class Error(val message: String) : ParseResult()
}

data class ParsedScheduleItem(
    val dayOfWeek: Int,
    val pairNumber: Int,
    val numeratorSubject: String? = null,
    val denominatorSubject: String? = null,
    val numeratorRoom: String? = null,
    val denominatorRoom: String? = null
)
