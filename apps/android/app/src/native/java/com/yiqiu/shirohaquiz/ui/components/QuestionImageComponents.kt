package com.yiqiu.shirohaquiz.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yiqiu.shirohaquiz.importer.model.QuestionImage
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaRadius
import java.io.File
import kotlin.math.roundToInt

@Composable
fun QuestionImagesBlock(
    images: List<QuestionImage>,
    modifier: Modifier = Modifier,
    maxPreviewHeight: Dp = 320.dp,
    showMeta: Boolean = true
) {
    if (images.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        images.forEach { image ->
            QuestionImagePreview(
                image = image,
                maxPreviewHeight = maxPreviewHeight,
                showMeta = showMeta
            )
        }
    }
}

@Composable
private fun QuestionImagePreview(
    image: QuestionImage,
    maxPreviewHeight: Dp,
    showMeta: Boolean
) {
    var expanded by remember(image.localPath) { mutableStateOf(false) }
    val bitmap = remember(image.localPath) {
        BitmapFactory.decodeFile(image.localPath)?.asImageBitmap()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ShirohaRadius.Lg),
        color = Color.White.copy(alpha = 0.82f),
        border = androidx.compose.foundation.BorderStroke(1.dp, ShirohaColors.LineStrong)
    ) {
        Column(Modifier.padding(10.dp)) {
            if (bitmap == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "图片文件无法读取，请在沉浸核对中检查。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                Image(
                    bitmap = bitmap,
                    contentDescription = image.sourceName.ifBlank { "题目图片" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxPreviewHeight)
                        .clickable { expanded = true },
                    contentScale = ContentScale.Fit
                )
            }

            if (showMeta) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = imageMetaText(image),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    if (expanded && bitmap != null) {
        Dialog(onDismissRequest = { expanded = false }) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.92f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.92f))
                        .clickable { expanded = false }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = image.sourceName.ifBlank { "题目图片大图" },
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

private fun imageMetaText(image: QuestionImage): String {
    val size = if (image.sizeBytes > 0) formatBytes(image.sizeBytes) else File(image.localPath).length().takeIf { it > 0 }?.let(::formatBytes) ?: "未知大小"
    val dimension = if (image.width != null && image.height != null) "${image.width}×${image.height}" else "尺寸未知"
    return "图片 ${image.order.takeIf { it > 0 } ?: ""} · $dimension · $size · 点击放大"
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "${bytes}B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "${kb.roundToInt()}KB"
    val mb = kb / 1024.0
    return "${String.format("%.1f", mb)}MB"
}
