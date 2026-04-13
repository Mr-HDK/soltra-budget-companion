package com.hdk.soltra.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hdk.soltra.data.local.dao.AccountDao
import com.hdk.soltra.data.local.dao.AccountTransferDao
import com.hdk.soltra.data.local.dao.BalanceCheckpointDao
import com.hdk.soltra.data.local.dao.BudgetConfigDao
import com.hdk.soltra.data.local.dao.CategoryDao
import com.hdk.soltra.data.local.dao.ExpenseDao
import com.hdk.soltra.data.local.dao.QuickTemplateDao
import com.hdk.soltra.data.local.dao.RecurringRuleDao
import com.hdk.soltra.data.local.entity.AccountEntity
import com.hdk.soltra.data.local.entity.AccountTransferEntity
import com.hdk.soltra.data.local.entity.BalanceCheckpointEntity
import com.hdk.soltra.data.local.entity.BudgetConfigEntity
import com.hdk.soltra.data.local.entity.CategoryEntity
import com.hdk.soltra.data.local.entity.ExpenseEntity
import com.hdk.soltra.data.local.entity.QuickTemplateEntity
import com.hdk.soltra.data.local.entity.RecurringRuleEntity
import com.hdk.soltra.util.resolveDefaultCurrencyCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExpenseEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        AccountTransferEntity::class,
        BalanceCheckpointEntity::class,
        BudgetConfigEntity::class,
        QuickTemplateEntity::class,
        RecurringRuleEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun accountTransferDao(): AccountTransferDao
    abstract fun balanceCheckpointDao(): BalanceCheckpointDao
    abstract fun budgetConfigDao(): BudgetConfigDao
    abstract fun quickTemplateDao(): QuickTemplateDao
    abstract fun recurringRuleDao(): RecurringRuleDao

    companion object {
        private const val DB_NAME = "budget_companion.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context.applicationContext).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .addMigrations(MIGRATION_4_5)
                .addCallback(
                    object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            seedOnCreate(context)
                        }
                    },
                )
                .build()
        }

        private fun seedOnCreate(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val database = getInstance(context)
                database.categoryDao().insertAll(defaultCategories())
                database.budgetConfigDao().upsert(
                    BudgetConfigEntity(currencyCode = resolveDefaultCurrencyCode()),
                )
            }
        }

        fun defaultCategories(): List<CategoryEntity> {
            val names = listOf(
                "Alimentation",
                "Cafes",
                "Transport",
                "Sorties",
                "Shopping",
                "Sante",
                "Maison",
                "Telecom / Abonnements",
                "Cadeaux / Social",
                "Administratif",
                "Divers",
            )
            val colors = listOf(
                "#2E7D32",
                "#6D4C41",
                "#1565C0",
                "#8E24AA",
                "#EF6C00",
                "#C62828",
                "#00897B",
                "#3949AB",
                "#7B1FA2",
                "#455A64",
                "#5D4037",
            )
            val icons = listOf(
                "utensils",
                "coffee",
                "transport",
                "party",
                "shopping",
                "health",
                "home",
                "subscription",
                "gift",
                "document",
                "tag",
            )
            return names.mapIndexed { index, name ->
                CategoryEntity(
                    name = name,
                    colorHex = colors[index % colors.size],
                    iconName = icons[index % icons.size],
                    sortOrder = index,
                )
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS recurring_rules (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        amountMinor INTEGER NOT NULL,
                        categoryId INTEGER NOT NULL,
                        paymentMethod TEXT NOT NULL,
                        note TEXT,
                        frequency TEXT NOT NULL,
                        intervalValue INTEGER NOT NULL,
                        nextRunEpochMillis INTEGER NOT NULL,
                        isActive INTEGER NOT NULL,
                        createdAtEpochMillis INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL,
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_rules_categoryId ON recurring_rules(categoryId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_rules_nextRunEpochMillis ON recurring_rules(nextRunEpochMillis)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS accounts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL,
                        balanceMinor INTEGER NOT NULL,
                        colorHex TEXT NOT NULL,
                        isActive INTEGER NOT NULL,
                        createdAtEpochMillis INTEGER NOT NULL,
                        updatedAtEpochMillis INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_accounts_isActive ON accounts(isActive)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_accounts_type ON accounts(type)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS account_transfers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        fromAccountId INTEGER NOT NULL,
                        toAccountId INTEGER NOT NULL,
                        amountMinor INTEGER NOT NULL,
                        occurredAtEpochMillis INTEGER NOT NULL,
                        note TEXT,
                        createdAtEpochMillis INTEGER NOT NULL,
                        FOREIGN KEY(fromAccountId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE RESTRICT,
                        FOREIGN KEY(toAccountId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_account_transfers_fromAccountId ON account_transfers(fromAccountId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_account_transfers_toAccountId ON account_transfers(toAccountId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_account_transfers_occurredAtEpochMillis ON account_transfers(occurredAtEpochMillis)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN iconName TEXT NOT NULL DEFAULT 'tag'")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_categories_sortOrder_name ON categories(sortOrder, name)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_categories_isActive_sortOrder_name ON categories(isActive, sortOrder, name)")
            }
        }
    }
}
