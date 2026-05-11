package com.yiqiu.shirohaquiz.ui.screens

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
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.IllustrationHeroCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing

@Composable
fun MeScreen(
    onOpenWrongBook: () -> Unit,
    onOpenRecords: () -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Me",
            title = "设置与资料",
            subtitle = "错题本、学习记录和后续设置入口。"
        )
        IllustrationHeroCard(
            title = "资料页先保持轻一点",
            subtitle = "设置入口保持清爽，不堆太多卡片。",
            imageRes = R.drawable.illus_home_welcome,
            imageSize = 72.dp
        )

        GlassCard {
            Text(
                text = "功能与档案",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            FeaturePlanStrip(
                icon = Icons.Rounded.AutoStories,
                title = "标准文本档案",
                desc = "后续用于整理标准文本导入模板和历史档案，当前先保留占位。"
            )
            Spacer(Modifier.height(16.dp))
            FeaturePlanStrip(
                icon = Icons.Rounded.Timer,
                title = "原生考试模式",
                desc = "考试入口已接入首页和练习页，这里保留功能状态说明。"
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
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
