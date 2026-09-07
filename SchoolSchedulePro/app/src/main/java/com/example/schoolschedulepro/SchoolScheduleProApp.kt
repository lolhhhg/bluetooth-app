package com.example.schoolschedulepro

import android.app.Application
import com.example.schoolschedulepro.data.local.AppDatabase
import com.example.schoolschedulepro.data.parser.PdfParser
import com.example.schoolschedulepro.data.repository.ScheduleRepositoryImpl
import com.example.schoolschedulepro.data.repository.VkRepositoryImpl
import com.example.schoolschedulepro.domain.usecase.*

class SchoolScheduleProApp : Application() {
    
    lateinit var database: AppDatabase
        private set
    
    // Repositories
    lateinit var scheduleRepository: ScheduleRepositoryImpl
        private set
    lateinit var vkRepository: VkRepositoryImpl
        private set
    
    // Use Cases - Schedule
    lateinit var getDayScheduleUseCase: GetDayScheduleUseCase
        private set
    lateinit var getAllScheduleUseCase: GetAllScheduleUseCase
        private set
    lateinit var insertScheduleItemUseCase: InsertScheduleItemUseCase
        private set
    lateinit var deletePairUseCase: DeletePairUseCase
        private set
    lateinit var updateNumeratorHomeworkUseCase: UpdateNumeratorHomeworkUseCase
        private set
    lateinit var updateDenominatorHomeworkUseCase: UpdateDenominatorHomeworkUseCase
        private set
    lateinit var parsePdfUseCase: ParsePdfUseCase
        private set
    lateinit var exportToJsonUseCase: ExportToJsonUseCase
        private set
    
    // Use Cases - VK
    lateinit var fetchVkImagesUseCase: FetchVkImagesUseCase
        private set
    lateinit var getAllVkImagesUseCase: GetAllVkImagesUseCase
        private set
    lateinit var clearVkCacheUseCase: ClearVkCacheUseCase
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // Init Database
        database = AppDatabase.getDatabase(this)
        
        // Init Repositories
        val pdfParser = PdfParser()
        scheduleRepository = ScheduleRepositoryImpl(database.scheduleDao(), pdfParser)
        vkRepository = VkRepositoryImpl(database.vkImageDao(), this)
        
        // Init Use Cases - Schedule
        getDayScheduleUseCase = GetDayScheduleUseCase(scheduleRepository)
        getAllScheduleUseCase = GetAllScheduleUseCase(scheduleRepository)
        insertScheduleItemUseCase = InsertScheduleItemUseCase(scheduleRepository)
        deletePairUseCase = DeletePairUseCase(scheduleRepository)
        updateNumeratorHomeworkUseCase = UpdateNumeratorHomeworkUseCase(scheduleRepository)
        updateDenominatorHomeworkUseCase = UpdateDenominatorHomeworkUseCase(scheduleRepository)
        parsePdfUseCase = ParsePdfUseCase(scheduleRepository)
        exportToJsonUseCase = ExportToJsonUseCase(scheduleRepository)
        
        // Init Use Cases - VK
        fetchVkImagesUseCase = FetchVkImagesUseCase(vkRepository)
        getAllVkImagesUseCase = GetAllVkImagesUseCase(vkRepository)
        clearVkCacheUseCase = ClearVkCacheUseCase(vkRepository)
    }
}
