package com.yiqiu.shirohaquiz.ui.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.ImportExport
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.ui.screens.BankDetailScreen
import com.yiqiu.shirohaquiz.ui.screens.BankListScreen
import com.yiqiu.shirohaquiz.ui.screens.ExamScreen
import com.yiqiu.shirohaquiz.ui.screens.HomeScreen
import com.yiqiu.shirohaquiz.ui.screens.ImportScreen
import com.yiqiu.shirohaquiz.ui.screens.MeScreen
import com.yiqiu.shirohaquiz.ui.screens.PracticeScreen
import com.yiqiu.shirohaquiz.ui.screens.RecordsScreen
import com.yiqiu.shirohaquiz.ui.screens.WrongBookScreen

private enum class MainTab(
    val title: String,
    val icon: ImageVector,
    val showInBottomBar: Boolean = true
) {
    Home("首页", Icons.Rounded.Dashboard),
    Practice("练习", Icons.Rounded.School),
    Import("导入", Icons.Rounded.ImportExport),
    Me("我的", Icons.Rounded.Settings),
    Exam("考试", Icons.Rounded.School, showInBottomBar = false),
    BankList("题库管理", Icons.Rounded.Dashboard, showInBottomBar = false),
    BankDetail("题库详情", Icons.Rounded.Dashboard, showInBottomBar = false),
    WrongBook("错题本", Icons.Rounded.School, showInBottomBar = false),
    Records("记录", Icons.Rounded.Dashboard, showInBottomBar = false)
}

@Composable
fun ShirohaAppShell() {
    var currentTab by rememberSaveable { mutableStateOf(MainTab.Home) }
    var detailBankId by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White.copy(alpha = 0.82f),
                tonalElevation = 0.dp
            ) {
                MainTab.entries.filter { it.showInBottomBar }.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF7F9FF),
                            Color(0xFFF1F4FB),
                            Color(0xFFF6F7FB)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "main_tabs"
            ) { tab ->
                when (tab) {
                    MainTab.Home -> HomeScreen(
                        onGoImport = { currentTab = MainTab.Import },
                        onGoPractice = { currentTab = MainTab.Practice },
                        onGoExam = { currentTab = MainTab.Exam },
                        onOpenBankList = { currentTab = MainTab.BankList },
                        onOpenBankDetail = { bankId ->
                            detailBankId = bankId
                            currentTab = MainTab.BankDetail
                        },
                        onOpenWrongBook = { currentTab = MainTab.WrongBook },
                        onOpenRecords = { currentTab = MainTab.Records }
                    )

                    MainTab.Practice -> PracticeScreen(onGoExam = { currentTab = MainTab.Exam })
                    MainTab.Import -> ImportScreen(onImportSaved = { currentTab = MainTab.Home })
                    MainTab.Me -> MeScreen(
                        onOpenWrongBook = { currentTab = MainTab.WrongBook },
                        onOpenRecords = { currentTab = MainTab.Records }
                    )
                    MainTab.Exam -> ExamScreen(
                        onBackHome = { currentTab = MainTab.Home },
                        onGoPractice = { currentTab = MainTab.Practice }
                    )
                    MainTab.BankList -> BankListScreen(
                        onBack = { currentTab = MainTab.Home },
                        onOpenBankDetail = { bankId ->
                            detailBankId = bankId
                            currentTab = MainTab.BankDetail
                        }
                    )
                    MainTab.BankDetail -> BankDetailScreen(
                        bankId = detailBankId,
                        onBack = { currentTab = MainTab.Home },
                        onGoPractice = { currentTab = MainTab.Practice },
                        onGoExam = { currentTab = MainTab.Exam }
                    )
                    MainTab.WrongBook -> WrongBookScreen(
                        onBack = { currentTab = MainTab.Home },
                        onGoPractice = { currentTab = MainTab.Practice }
                    )
                    MainTab.Records -> RecordsScreen(
                        onBack = { currentTab = MainTab.Home }
                    )
                }
            }
        }
    }
}
