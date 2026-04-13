package com.hdk.soltra.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.hdk.soltra.data.local.entity.AccountTransferEntity

data class AccountTransferWithAccounts(
    @Embedded val transfer: AccountTransferEntity,
    @ColumnInfo(name = "fromAccountName") val fromAccountName: String,
    @ColumnInfo(name = "toAccountName") val toAccountName: String,
)
