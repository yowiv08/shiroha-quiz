package com.yiqiu.shirohaquiz.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.IllustrationHeroCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaDimens
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import java.util.Calendar

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
    val todayPracticeCount = QuizRepository.studyRecords.count { record ->
        record.source == "练习" && isToday(record.timestamp)
    }
    val pendingReviewCount = QuizRepository.wrongBookActiveCount()
    val homeSectionGap = ShirohaSpacing.Lg

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm)
    ) {
        ShirohaHeader(
            kicker = "Shiroha Quiz",
            title = "首页",
            subtitle = ""
        )

        Spacer(Modifier.height(homeSectionGap))

        IllustrationHeroCard(
            title = "欢迎回来",
            subtitle = "继续练习、考试或查看学习记录。",
            imageRes = R.drawable.illus_home_welcome_v2,
            modifier = Modifier.height(ShirohaDimens.HeroCardHeight),
            imageSize = ShirohaDimens.HeroImageSize
        )

        Spacer(Modifier.height(homeSectionGap))

        TodayStatusCard(
            bankName = bankName,
            todayPracticeCount = todayPracticeCount,
            pendingReviewCount = pendingReviewCount,
            onGoPractice = onGoPractice,
            onGoExam = onGoExam
        )

        Spacer(Modifier.height(homeSectionGap))

        HomeShortcutGrid(
            bankCount = bankCount,
            questionCount = questionCount,
            wrongCount = QuizRepository.wrongBook.size,
            recordCount = QuizRepository.studyRecords.size,
            onOpenBankList = onOpenBankList,
            onOpenBankDetail = { activeBank?.let { onOpenBankDetail(it.id) } },
            onOpenWrongBook = onOpenWrongBook,
            onOpenRecords = onOpenRecords,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = homeSectionGap)
        )
    }
}

@Composable
private fun TodayStatusCard(
    bankName: String,
    todayPracticeCount: Int,
    pendingReviewCount: Int,
    onGoPractice: () -> Unit,
    onGoExam: () -> Unit
) {
    val currentBankHeight = 60.dp
    val metricCardHeight = 56.dp
    val actionButtonHeight = 46.dp

    GlassCard(contentPadding = ShirohaSpacing.Lg) {
        Text(
            text = "今日学习状态",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(8.dp))
        MiniStatusCard(
            title = "当前题库",
            value = bankName,
            modifier = Modifier
                .fillMaxWidth()
                .height(currentBankHeight)
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MiniStatusCard(
                title = "今日练习",
                value = "${todayPracticeCount} 次",
                modifier = Modifier
                    .weight(1f)
                    .height(metricCardHeight)
            )
            MiniStatusCard(
                title = "待复习",
                value = "${pendingReviewCount} 题",
                modifier = Modifier
                    .weight(1f)
                    .height(metricCardHeight)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CompactHomeActionButton(
                icon = Icons.Rounded.PlayArrow,
                text = "继续练习",
                modifier = Modifier
                    .weight(1f)
                    .height(actionButtonHeight),
                onClick = onGoPractice
            )
            CompactHomeActionButton(
                icon = Icons.Rounded.Timer,
                text = "模拟考试",
                modifier = Modifier
                    .weight(1f)
                    .height(actionButtonHeight),
                onClick = onGoExam
            )
        }
    }
}

@Composable
private fun CompactHomeActionButton(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = ShirohaColors.CardWhite86,
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineStrong)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

@Composable
private fun MiniStatusCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = ShirohaColors.CardWhite62,
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSoft)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HomeShortcutGrid(
    bankCount: Int,
    questionCount: Int,
    wrongCount: Int,
    recordCount: Int,
    onOpenBankList: () -> Unit,
    onOpenBankDetail: () -> Unit,
    onOpenWrongBook: () -> Unit,
    onOpenRecords: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeShortcutCard(
                icon = Icons.Rounded.AutoStories,
                label = "题库数量",
                value = bankCount.toString(),
                desc = "进入题库管理",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = onOpenBankList
            )
            HomeShortcutCard(
                icon = Icons.Rounded.Description,
                label = "当前题量",
                value = questionCount.toString(),
                desc = "查看当前题库详情",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = onOpenBankDetail
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeShortcutCard(
                icon = Icons.Rounded.Warning,
                label = "错题本",
                value = wrongCount.toString(),
                desc = "打开错题本",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = onOpenWrongBook
            )
            HomeShortcutCard(
                icon = Icons.Rounded.Timer,
                label = "学习记录",
                value = recordCount.toString(),
                desc = "查看学习记录",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = onOpenRecords
            )
        }
    }
}

@Composable
private fun HomeShortcutCard(
    icon: ImageVector,
    label: String,
    value: String,
    desc: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        color = ShirohaColors.CardWhite72,
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSoft)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun isToday(timestamp: Long): Boolean {
    if (timestamp <= 0L) return false
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = timestamp }
    return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
}
