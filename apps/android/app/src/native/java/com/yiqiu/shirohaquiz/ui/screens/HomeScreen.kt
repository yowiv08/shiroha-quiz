package com.yiqiu.shirohaquiz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.IllustrationHeroCard
import com.yiqiu.shirohaquiz.ui.components.MetricGlassCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing

@Composable
fun HomeScreen(
    onGoImport: () -> Unit,
    onGoPractice: () -> Unit,
    onGoExam: () -> Unit,
    onOpenBankList: () -> Unit,
    onOpenBankDetail: (String) -> Unit,
    onOpenWrongBook: () -> Unit,
    onOpenRecords: () -> Unit
) {
    val activeBank = QuizRepository.activeBank()
    val bankCount = QuizRepository.banks.size
    val questionCount = activeBank?.questions?.size ?: 0
    val bankName = activeBank?.name ?: "尚未导入题库"
    val bankNameSize = when {
        bankName.length > 28 -> 15.sp
        bankName.length > 20 -> 17.sp
        bankName.length > 14 -> 19.sp
        else -> 22.sp
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Shiroha Quiz",
            title = "首页",
            subtitle = ""
        )

        IllustrationHeroCard(
            title = "欢迎回来",
            subtitle = "继续练习、考试或查看学习记录。",
            imageRes = R.drawable.illus_home_welcome,
            imageSize = 84.dp
        )

        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "当前题库",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = bankName,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = bankNameSize),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionPillButton(
                    Icons.Rounded.PlayArrow,
                    "进入练习",
                    primary = false,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    fillWidthContent = true,
                    onClick = onGoPractice
                )
                ActionPillButton(
                    Icons.Rounded.Timer,
                    "开始考试",
                    primary = false,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    fillWidthContent = true,
                    onClick = onGoExam
                )
            }
        }

        CurrentStatusCard(
            bankCount = bankCount,
            wrongCount = QuizRepository.wrongBook.size,
            recordCount = QuizRepository.studyRecords.size,
            onOpenWrongBook = onOpenWrongBook,
            onOpenRecords = onOpenRecords
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricGlassCard(
                label = "题库数量",
                value = bankCount.toString(),
                desc = "进入题库管理",
                modifier = Modifier.weight(1f),
                onClick = onOpenBankList
            )
            MetricGlassCard(
                label = "当前题量",
                value = questionCount.toString(),
                desc = "查看当前题库详情",
                modifier = Modifier.weight(1f),
                onClick = {
                    activeBank?.let { onOpenBankDetail(it.id) }
                }
            )
        }
    }
}

@Composable
private fun CurrentStatusCard(
    bankCount: Int,
    wrongCount: Int,
    recordCount: Int,
    onOpenWrongBook: () -> Unit,
    onOpenRecords: () -> Unit
) {
    GlassCard {
        Text(
            text = "当前状态",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "题库 $bankCount 份 · 错题 $wrongCount 条 · 记录 $recordCount 条",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ActionPillButton(
                    icon = Icons.Rounded.ReportProblem,
                    text = "打开错题本",
                    primary = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    fillWidthContent = true,
                    onClick = onOpenWrongBook
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "收拢练习和考试里的错题",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                ActionPillButton(
                    icon = Icons.Rounded.History,
                    text = "查看学习记录",
                    primary = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    fillWidthContent = true,
                    onClick = onOpenRecords
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "查看最近的练习和考试结果",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
