package com.yiqiu.shirohaquiz.ui.screens

import androidx.compose.foundation.BorderStroke
import com.yiqiu.shirohaquiz.ui.components.shirohaNoRippleClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.state.QuizBank
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaDangerConfirmDialog
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaRadius
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing

@Composable
fun BankListScreen(
    onBack: () -> Unit,
    onOpenBankDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val activeBank = QuizRepository.activeBank()
    var renameTarget by remember { mutableStateOf<QuizBank?>(null) }
    var renameText by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<QuizBank?>(null) }

    if (renameTarget != null) {
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("重命名题库") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("题库名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val bank = renameTarget
                        if (bank != null && renameText.isNotBlank()) {
                            QuizRepository.renameBank(context, bank.id, renameText)
                        }
                        renameTarget = null
                    }
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("取消") }
            }
        )
    }

    deleteTarget?.let { bank ->
        ShirohaDangerConfirmDialog(
            title = "确认删除题库？",
            message = "将删除“${bank.name}”，并清理这份题库关联的错题、斩题和学习记录。操作不可撤销。",
            confirmText = "确认删除",
            onDismiss = { deleteTarget = null },
            onConfirm = {
                QuizRepository.deleteBank(context, bank.id)
                deleteTarget = null
            }
        )
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Banks",
            title = "题库管理",
            subtitle = "管理原生题库，支持切换、查看、重命名和删除。"
        )

        GlassCard {
            ActionPillButton(
                icon = Icons.Rounded.ArrowBack,
                text = "返回首页",
                primary = false,
                onClick = onBack
            )
        }

        QuizRepository.banks.forEach { bank ->
            val isActive = bank.id == activeBank?.id
            val singleCount = bank.questions.count { it.type == QuestionType.SINGLE }
            val multipleCount = bank.questions.count { it.type == QuestionType.MULTIPLE }
            val judgeCount = bank.questions.count { it.type == QuestionType.JUDGE }
            val subjectiveCount = bank.questions.count { it.type == QuestionType.BLANK || it.type == QuestionType.SHORT }

            GlassCard(
                modifier = Modifier.shirohaNoRippleClickable { onOpenBankDetail(bank.id) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip("${bank.questions.size} 题", selected = true)
                    Spacer(Modifier.weight(1f))
                    CompactBankStateChip(
                        text = if (isActive) "当前" else "设为当前",
                        selected = isActive,
                        onClick = {
                            if (!isActive) {
                                QuizRepository.setActiveBank(context, bank.id)
                            }
                        }
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = bank.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "单选 $singleCount · 多选 $multipleCount · 判断 $judgeCount · 主观 $subjectiveCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionPillButton(
                        icon = Icons.Rounded.Visibility,
                        text = "详情",
                        primary = false,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        fillWidthContent = true,
                        onClick = { onOpenBankDetail(bank.id) }
                    )
                    ActionPillButton(
                        icon = Icons.Rounded.Edit,
                        text = "重命名",
                        primary = false,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        fillWidthContent = true,
                        onClick = {
                            renameTarget = bank
                            renameText = bank.name
                        }
                    )
                    ActionPillButton(
                        icon = Icons.Rounded.DeleteOutline,
                        text = "删除",
                        primary = false,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        fillWidthContent = true,
                        onClick = {
                            if (bank.id != "demo-bank") {
                                deleteTarget = bank
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactBankStateChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.shirohaNoRippleClickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(ShirohaRadius.Pill),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.84f),
        border = if (selected) null else BorderStroke(1.dp, ShirohaColors.LineStrong)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Done,
                contentDescription = text,
                modifier = Modifier.size(14.dp),
                tint = if (selected) Color.White else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = text,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
        }
    }
}
