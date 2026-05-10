package com.yiqiu.shirohaquiz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.MetricGlassCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.ShortcutGlassCard
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing

@Composable
fun HomeScreen(
    onGoImport: () -> Unit,
    onGoPractice: () -> Unit,
    onGoExam: () -> Unit
) {
    val context = LocalContext.current
    val activeBank = QuizRepository.activeBank()
    val bankCount = QuizRepository.banks.size
    val questionCount = activeBank?.questions?.size ?: 0

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(ShirohaSpacing.Xl),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Shiroha Quiz",
            title = "原生题库首页",
            subtitle = "这里只服务原生安卓。导入成功后的题库会直接进入原生状态，并接入练习与考试流程。"
        )

        GlassCard {
            Text(
                text = "当前原生题库",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = activeBank?.name ?: "尚未导入题库",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (questionCount > 0) {
                    "当前题库共 $questionCount 题，已经可以直接进入原生练习与考试。"
                } else {
                    "当前还没有真实导入题目。先到“导入”页导入一份标准文本题库。"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionPillButton(Icons.Rounded.CloudUpload, "去导入", primary = true, onClick = onGoImport)
                ActionPillButton(Icons.Rounded.PlayArrow, "进入练习", primary = false, onClick = onGoPractice)
                ActionPillButton(Icons.Rounded.Timer, "开始考试", primary = false, onClick = onGoExam)
            }
        }

        GlassCard {
            Text(
                text = "题库列表",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            QuizRepository.banks.forEach { bank ->
                val isActive = bank.id == activeBank?.id
                Text(
                    text = bank.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${bank.questions.size} 题${if (isActive) " · 当前活动题库" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionPillButton(
                        icon = Icons.Rounded.Done,
                        text = if (isActive) "当前题库" else "切换到此题库",
                        primary = isActive,
                        onClick = { if (!isActive) QuizRepository.setActiveBank(context, bank.id) }
                    )
                    ActionPillButton(
                        icon = Icons.Rounded.DeleteOutline,
                        text = "删除",
                        primary = false,
                        onClick = {
                            if (bank.id != "demo-bank") {
                                QuizRepository.deleteBank(context, bank.id)
                            }
                        }
                    )
                }
                Spacer(Modifier.height(18.dp))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricGlassCard(
                label = "题库数量",
                value = bankCount.toString(),
                desc = "原生侧当前挂载的题库",
                modifier = Modifier.weight(1f)
            )
            MetricGlassCard(
                label = "当前题量",
                value = questionCount.toString(),
                desc = "活动题库中的题目数",
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShortcutGlassCard(
                title = "标准文本导入",
                icon = Icons.Rounded.AutoStories,
                desc = "优先覆盖最常见题库格式",
                modifier = Modifier.weight(1f)
            )
            ShortcutGlassCard(
                title = "原生考试模式",
                icon = Icons.Rounded.Schedule,
                desc = "开始接入题量、计时、交卷和结果页",
                modifier = Modifier.weight(1f)
            )
        }
    }
}
