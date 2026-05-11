package com.yiqiu.shirohaquiz.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.state.StudyRecord
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.EmptyStateIllustration
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.IllustrationHeroCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordsScreen(
    onBack: () -> Unit
) {
    val records = QuizRepository.studyRecords
    var expandedId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Records",
            title = "学习记录",
            subtitle = ""
        )

        if (records.isEmpty()) {
            EmptyStateIllustration(
                title = "这里还没有学习记录",
                message = "完成练习或考试后，记录会自动出现在这里。",
                imageRes = R.drawable.illus_rest_state,
                action = { Spacer(Modifier.height(12.dp)) }
            )
            GlassCard {
                ActionPillButton(
                    icon = Icons.AutoMirrored.Rounded.Undo,
                    text = "返回首页",
                    primary = false,
                    onClick = onBack
                )
            }
            return
        }

        IllustrationHeroCard(
            title = "学习记录会在这里慢慢积累",
            subtitle = "这里会收录练习和考试的记录。",
            imageRes = R.drawable.illus_rest_state,
            imageSize = 96.dp
        )

        records.forEach { record ->
            RecordCard(
                record = record,
                expanded = expandedId == record.id,
                onToggle = { expandedId = if (expandedId == record.id) null else record.id }
            )
        }
    }
}

@Composable
private fun RecordCard(
    record: StudyRecord,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val wrong = (record.total - record.correct).coerceAtLeast(0)
    val accuracy = if (record.total == 0) 0 else record.correct * 100 / record.total
    val finishTime = record.timestamp
    val startTime = record.durationSeconds?.let { finishTime - it * 1000L } ?: finishTime

    GlassCard(modifier = Modifier.clickable(onClick = onToggle)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip(record.source, selected = true)
            StatusChip(record.bankName)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = record.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = if (expanded) 3 else 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "正确 ${record.correct} / ${record.total} · 正确率 $accuracy%",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (expanded) {
            Spacer(Modifier.height(10.dp))
            Text("题库：${record.bankName}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text("题量：${record.total} 题", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text("进入时间：${formatRecordTime(startTime)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text("完成时间：${formatRecordTime(finishTime)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text("用时：${record.durationSeconds?.let(::formatDuration) ?: "未记录"}${if (record.autoSubmitted) " · 自动交卷" else ""}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text("正确：${record.correct} 题 · 错误：$wrong 题 · 正确率：$accuracy%", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "点击查看详情",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun formatRecordTime(timestamp: Long): String {
    return SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}
