package com.yiqiu.shirohaquiz.ui.util

import com.yiqiu.shirohaquiz.state.DEFAULT_BANK_GROUP_NAME
import com.yiqiu.shirohaquiz.state.QuizBank

fun bankDisplayPath(groupName: String, bankName: String): String {
    val cleanGroupName = groupName.ifBlank { DEFAULT_BANK_GROUP_NAME }
    return "$cleanGroupName / $bankName"
}

fun bankDisplayPath(bank: QuizBank): String {
    return bankDisplayPath(bank.groupName, bank.name)
}
