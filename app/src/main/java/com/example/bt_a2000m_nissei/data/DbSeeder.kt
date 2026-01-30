package com.example.bt_a2000m_nissei.data

import android.content.Context
import com.example.bt_a2000m_nissei.data.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

object DbSeeder {

    suspend fun seedIfNeeded(context: Context) = withContext(Dispatchers.IO) {
        val db = AppDatabase.get(context)
        val userDao = db.userDao()

        // 1) Seed admin nếu chưa có
        if (userDao.countUsers() == 0) {
            userDao.insert(
                UserEntity(
                    id = UUID.randomUUID().toString(),
                    fullName = "Administrator",
                    username = "admin",
                    passwordHash = hashPasswordSimple("admin123"),
                    role = "ADMIN",
                    isActive = true,
                    createdAt = System.currentTimeMillis()
                )
            )
        }

        // 2) Seed machine + checklist nếu chưa có
        val seedDao = db.seedDao()

        if (seedDao.countMachines() == 0 && seedDao.countTemplates() == 0) {
            val now = System.currentTimeMillis()

            val machineId = UUID.randomUUID().toString()
            seedDao.insertMachine(
                MachineEntity(
                    id = machineId,
                    machineCode = "may ok", // barcode đúng y chang yêu cầu
                    machineName = "Dây chuyền MAY OK",
                    description = "Line sản xuất mẫu để demo offline-first",
                    location = "Xưởng 1",
                    note = "Seed demo",
                    createdAt = now
                )
            )

            // Templates
            val tDailyId = UUID.randomUUID().toString()
            val tWeeklyId = UUID.randomUUID().toString()
            val tMonthlyId = UUID.randomUUID().toString()

            seedDao.insertTemplates(
                listOf(
                    ChecklistTemplateEntity(tDailyId, "Kiểm tra HÀNG NGÀY", InspectionPeriod.DAILY, true, now),
                    ChecklistTemplateEntity(tWeeklyId, "Kiểm tra HÀNG TUẦN", InspectionPeriod.WEEKLY, true, now),
                    ChecklistTemplateEntity(tMonthlyId, "Kiểm tra HÀNG THÁNG", InspectionPeriod.MONTHLY, true, now),
                )
            )

            // Items (đa dạng: đạt/không đạt + nhập liệu)
            seedDao.insertChecklistItems(
                listOf(
                    // DAILY
                    ChecklistItemEntity(UUID.randomUUID().toString(), tDailyId, "Vệ sinh khu vực (Đạt/Không đạt)", ChecklistItemType.BOOLEAN, 1, true),
                    ChecklistItemEntity(UUID.randomUUID().toString(), tDailyId, "Nhiệt độ vận hành (°C) - Nhập số", ChecklistItemType.INPUT_NUMBER, 2, true),
                    ChecklistItemEntity(UUID.randomUUID().toString(), tDailyId, "Ghi chú bất thường - Nhập chữ", ChecklistItemType.INPUT_TEXT, 3, false),

                    // WEEKLY
                    ChecklistItemEntity(UUID.randomUUID().toString(), tWeeklyId, "Kiểm tra dây curoa (Đạt/Không đạt)", ChecklistItemType.BOOLEAN, 1, true),
                    ChecklistItemEntity(UUID.randomUUID().toString(), tWeeklyId, "Độ rung đo được (mm/s) - Nhập số", ChecklistItemType.INPUT_NUMBER, 2, false),
                    ChecklistItemEntity(UUID.randomUUID().toString(), tWeeklyId, "Ghi chú bảo trì tuần - Nhập chữ", ChecklistItemType.INPUT_TEXT, 3, false),

                    // MONTHLY
                    ChecklistItemEntity(UUID.randomUUID().toString(), tMonthlyId, "Kiểm tra tổng thể an toàn (Đạt/Không đạt)", ChecklistItemType.BOOLEAN, 1, true),
                    ChecklistItemEntity(UUID.randomUUID().toString(), tMonthlyId, "Số giờ chạy trong tháng - Nhập số", ChecklistItemType.INPUT_NUMBER, 2, true),
                    ChecklistItemEntity(UUID.randomUUID().toString(), tMonthlyId, "Đề xuất cải tiến - Nhập chữ", ChecklistItemType.INPUT_TEXT, 3, false),
                )
            )

            // Bind machine -> template theo period (mỗi period 1 template)
            seedDao.insertBindings(
                listOf(
                    MachineChecklistBindingEntity(UUID.randomUUID().toString(), machineId, InspectionPeriod.DAILY, tDailyId),
                    MachineChecklistBindingEntity(UUID.randomUUID().toString(), machineId, InspectionPeriod.WEEKLY, tWeeklyId),
                    MachineChecklistBindingEntity(UUID.randomUUID().toString(), machineId, InspectionPeriod.MONTHLY, tMonthlyId),
                )
            )
        }
    }

    // Demo hash tạm thời
    fun hashPasswordSimple(plain: String): String = plain.reversed() + "|v1"
    fun verifyPasswordSimple(plain: String, hash: String): Boolean = hash == hashPasswordSimple(plain)
}
