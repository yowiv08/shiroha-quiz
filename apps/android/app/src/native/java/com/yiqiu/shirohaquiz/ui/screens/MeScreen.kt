package com.yiqiu.shirohaquiz.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.state.QuizBank
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.IllustrationHeroCard
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MeScreen(
    onOpenWrongBook: () -> Unit,
    onOpenRecords: () -> Unit
) {
    val context = LocalContext.current
    var statusText by remember { mutableStateOf<String?>(null) }
    var pendingExportText by remember { mutableStateOf<String?>(null) }
    var showBankExportDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var selectedBankIds by remember { mutableStateOf(QuizRepository.banks.map { it.id }.toSet()) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val text = pendingExportText
        if (uri != null && text != null) {
            val ok = writeTextToUri(context, uri, text)
            statusText = if (ok) "已导出到所选位置。" else "导出失败：无法写入文件。"
        }
        pendingExportText = null
    }

    val importBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = readTextFromUri(context, uri)
        statusText = if (text.isNullOrBlank()) {
            "导入失败：没有读取到有效内容。"
        } else {
            QuizRepository.importBackupJson(context, text)
        }
        selectedBankIds = QuizRepository.banks.map { it.id }.toSet()
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Me",
            title = "设置与资料",
            subtitle = ""
        )
        IllustrationHeroCard(
            title = "资料页先保持轻一点",
            subtitle = "设置入口保持清爽，不堆太多卡片。",
            imageRes = R.drawable.illus_home_welcome,
            imageSize = 88.dp
        )

        statusText?.let { message ->
            NoticeCard(text = message, warning = message.contains("失败") || message.contains("清除"))
        }

        GlassCard {
            Text(
                text = "数据管理",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DataActionTile(
                    icon = Icons.Rounded.FileOpen,
                    title = "导入题库",
                    desc = "导入备份 JSON",
                    modifier = Modifier.weight(1f),
                    onClick = { importBackupLauncher.launch(arrayOf("application/json", "text/*", "*/*")) }
                )
                DataActionTile(
                    icon = Icons.Rounded.Description,
                    title = "导出题库",
                    desc = "多选导出",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedBankIds = QuizRepository.banks.map { it.id }.toSet()
                        showBankExportDialog = true
                    }
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DataActionTile(
                    icon = Icons.Rounded.Save,
                    title = "导出全部",
                    desc = "完整备份",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        pendingExportText = QuizRepository.exportFullBackupJson()
                        createDocumentLauncher.launch("shiroha_full_backup_${backupTimeStamp()}.json")
                    }
                )
                DataActionTile(
                    icon = Icons.Rounded.Delete,
                    title = "清除数据",
                    desc = "需二次确认",
                    modifier = Modifier.weight(1f),
                    warning = true,
                    onClick = { showClearDialog = true }
                )
            }
        }

        GlassCard {
            Text(
                text = "功能与档案",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            FeaturePlanStrip(
                icon = Icons.Rounded.AutoStories,
                title = "标准文本档案",
                desc = "后续整理标准文本导入模板和历史档案。"
            )
            Spacer(Modifier.height(16.dp))
            FeaturePlanStrip(
                icon = Icons.Rounded.Timer,
                title = "原生考试模式",
                desc = "考试入口已接入首页和练习页。"
            )
        }
    }

    if (showBankExportDialog) {
        BankExportDialog(
            banks = QuizRepository.banks,
            selectedBankIds = selectedBankIds,
            onSelectedChange = { selectedBankIds = it },
            onDismiss = { showBankExportDialog = false },
            onExport = {
                if (selectedBankIds.isEmpty()) {
                    statusText = "请至少选择一个题库。"
                } else {
                    pendingExportText = QuizRepository.exportBanksBackupJson(selectedBankIds)
                    createDocumentLauncher.launch("shiroha_selected_banks_${backupTimeStamp()}.json")
                }
                showBankExportDialog = false
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("确认清除本地全部数据？") },
            text = { Text("这会删除本地题库、错题本、学习记录和当前状态。操作不可撤销，建议先导出全部数据。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        QuizRepository.clearAllLocalData(context)
                        selectedBankIds = emptySet()
                        statusText = "已清除本地全部数据。"
                        showClearDialog = false
                    }
                ) { Text("确认清除") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun BankExportDialog(
    banks: List<QuizBank>,
    selectedBankIds: Set<String>,
    onSelectedChange: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
    onExport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择要导出的题库") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (banks.isEmpty()) {
                    Text("当前没有可导出的题库。")
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectedChange(if (selectedBankIds.size == banks.size) emptySet() else banks.map { it.id }.toSet())
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedBankIds.size == banks.size,
                            onCheckedChange = { checked ->
                                onSelectedChange(if (checked) banks.map { it.id }.toSet() else emptySet())
                            }
                        )
                        Text("全选")
                    }
                    banks.forEach { bank ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectedChange(
                                        if (bank.id in selectedBankIds) selectedBankIds - bank.id else selectedBankIds + bank.id
                                    )
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = bank.id in selectedBankIds,
                                onCheckedChange = { checked ->
                                    onSelectedChange(if (checked) selectedBankIds + bank.id else selectedBankIds - bank.id)
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bank.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${bank.questions.size} 题",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onExport, enabled = selectedBankIds.isNotEmpty()) { Text("导出选中") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun DataActionTile(
    icon: ImageVector,
    title: String,
    desc: String,
    modifier: Modifier = Modifier,
    warning: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.68f),
        border = BorderStroke(1.dp, if (warning) ShirohaColors.StateWarningSoft else ShirohaColors.LineSoft)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (warning) Color(0xFFE29A00) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FeaturePlanStrip(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun readTextFromUri(context: Context, uri: Uri): String? {
    return runCatching {
        context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
    }.getOrNull()
}

private fun writeTextToUri(context: Context, uri: Uri, text: String): Boolean {
    return runCatching {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            output.write(text.toByteArray(Charsets.UTF_8))
            output.flush()
        } ?: return false
        true
    }.getOrDefault(false)
}

private fun backupTimeStamp(): String {
    return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
}
