package com.yiqiu.shirohaquiz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.EmptyStateIllustration
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing

@Composable
fun BankDetailScreen(
    bankId: String?,
    onBack: () -> Unit,
    onGoPractice: () -> Unit,
    onGoExam: () -> Unit
) {
    val context = LocalContext.current
    val bank = QuizRepository.banks.firstOrNull { it.id == bankId } ?: QuizRepository.activeBank()
    val isActive = bank?.id == QuizRepository.activeBank()?.id

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Bank Detail",
            title = bank?.name ?: "题库详情",
            subtitle = "题库摘要、题型分布和快速操作。"
        )

        if (bank == null) {
            EmptyStateIllustration(
                title = "没有找到对应题库",
                message = "这通常说明题库已经被切换或删除。回到首页重新选择一份题库就好。",
                imageRes = R.drawable.illus_empty_state,
                action = {
                    Spacer(Modifier.height(12.dp))
                }
            )
            GlassCard {
                ActionPillButton(
                    icon = Icons.Rounded.Done,
                    text = "返回首页",
                    primary = true,
                    onClick = onBack
                )
            }
            return
        }

        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "题库摘要",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                ActionPillButton(
                    icon = Icons.Rounded.Done,
                    text = if (isActive) "当前题库" else "设为当前",
                    primary = true,
                    modifier = Modifier.height(44.dp),
                    onClick = {
                        if (!isActive) {
                            QuizRepository.setActiveBank(context, bank.id)
                        }
                    }
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip("${bank.questions.size} 题", selected = true)
                StatusChip(if (isActive) "活动题库" else "可切换题库", selected = isActive)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "单选 ${bank.questions.count { it.type == QuestionType.SINGLE }} · 多选 ${bank.questions.count { it.type == QuestionType.MULTIPLE }} · 判断 ${bank.questions.count { it.type == QuestionType.JUDGE }} · 主观 ${bank.questions.count { it.type == QuestionType.BLANK || it.type == QuestionType.SHORT }}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionPillButton(
                    icon = Icons.Rounded.PlayArrow,
                    text = "进入练习",
                    primary = false,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    fillWidthContent = true,
                    onClick = onGoPractice
                )
                ActionPillButton(
                    icon = Icons.Rounded.Timer,
                    text = "进入考试",
                    primary = false,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    fillWidthContent = true,
                    onClick = onGoExam
                )
            }
        }

        GlassCard {
            Text(
                text = "题目预览",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            bank.questions.take(5).forEach { question ->
                StatusChip(typeLabel(question.type))
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${question.number}. ${question.question}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (question.options.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = question.options.joinToString("  ") { "${it.key}. ${it.text}" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(14.dp))
            }
        }

        if (bank.id != "demo-bank") {
            GlassCard {
                Text(
                    text = "危险操作",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                NoticeCard("删除题库后，会一起清理这份原生题库关联的本地记录。")
                Spacer(Modifier.height(12.dp))
                ActionPillButton(
                    icon = Icons.Rounded.DeleteOutline,
                    text = "删除这份题库",
                    primary = false,
                    onClick = {
                        QuizRepository.deleteBank(context, bank.id)
                        onBack()
                    }
                )
            }
        }
    }
}

private fun typeLabel(type: QuestionType): String = when (type) {
    QuestionType.SINGLE -> "单选题"
    QuestionType.MULTIPLE -> "多选题"
    QuestionType.JUDGE -> "判断题"
    QuestionType.BLANK -> "填空题"
    QuestionType.SHORT -> "简答题"
}
