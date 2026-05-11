package com.yiqiu.shirohaquiz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.state.ExamSummary
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.EmptyStateIllustration
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.IllustrationHeroCard
import com.yiqiu.shirohaquiz.ui.components.MetricGlassCard
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.QuizOptionCard
import com.yiqiu.shirohaquiz.ui.components.QuestionImagesBlock
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExamScreen(
    onBackHome: () -> Unit,
    onGoPractice: () -> Unit
) {
    val activeBank = QuizRepository.activeBank()
    var selectedQuestionCount by remember(activeBank?.id) {
        mutableIntStateOf(activeBank?.questions?.size?.coerceAtMost(20) ?: 20)
    }
    var selectedDurationMinutes by remember { mutableIntStateOf(20) }

    val examQuestion = QuizRepository.currentExamQuestion()
    val examSummary = QuizRepository.examSummary()

    if (QuizRepository.examQuestions.isNotEmpty() && !QuizRepository.examFinished) {
        LaunchedEffect(QuizRepository.examRemainingSeconds, QuizRepository.examFinished) {
            if (!QuizRepository.examFinished && QuizRepository.examRemainingSeconds > 0) {
                delay(1000)
                QuizRepository.tickExam()
            }
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(ShirohaSpacing.Xl),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Exam",
            title = "原生考试模式",
            subtitle = "这条流程只走原生 Android：题量设置、计时、交卷和结果页都在这里闭环。"
        )

        if (activeBank == null || activeBank.questions.isEmpty()) {
            EmptyStateIllustration(
                title = "还没有可用于考试的题库",
                message = "先在原生导入页导入题库，考试流就能真正跑起来。",
                action = {
                    Spacer(Modifier.height(14.dp))
                }
            )
            GlassCard {
                ActionPillButton(
                    icon = Icons.Rounded.PlayArrow,
                    text = "返回首页",
                    primary = true,
                    onClick = onBackHome
                )
            }
            return
        }

        if (QuizRepository.examQuestions.isEmpty() && !QuizRepository.examFinished) {
            ExamSetupPanel(
                bankName = activeBank.name,
                totalQuestions = activeBank.questions.size,
                selectedQuestionCount = selectedQuestionCount,
                onSelectQuestionCount = { selectedQuestionCount = it },
                selectedDurationMinutes = selectedDurationMinutes,
                onSelectDuration = { selectedDurationMinutes = it },
                onStartExam = {
                    if (QuizRepository.startExam(selectedQuestionCount, selectedDurationMinutes)) {
                        selectedQuestionCount = selectedQuestionCount.coerceAtMost(activeBank.questions.size)
                    }
                },
                onBackHome = onBackHome
            )
            return
        }

        if (!QuizRepository.examFinished && examQuestion != null) {
            ActiveExamPanel()
            return
        }

        FinishedExamPanel(
            examSummary = examSummary,
            onRestart = { QuizRepository.resetExam() },
            onGoPractice = {
                QuizRepository.resetExam()
                onGoPractice()
            },
            onBackHome = {
                QuizRepository.resetExam()
                onBackHome()
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExamSetupPanel(
    bankName: String,
    totalQuestions: Int,
    selectedQuestionCount: Int,
    onSelectQuestionCount: (Int) -> Unit,
    selectedDurationMinutes: Int,
    onSelectDuration: (Int) -> Unit,
    onStartExam: () -> Unit,
    onBackHome: () -> Unit
) {
    IllustrationHeroCard(
        title = "配置考试参数",
        subtitle = "思考状态图只放在考试设置页。真正答题时把画面让给题目、计时和选项。",
        imageRes = R.drawable.illus_thinking_state,
        imageSize = 104.dp
    )

    GlassCard {
        Text(
            text = "考试设置",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "当前题库：$bankName · 共 $totalQuestions 题",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = "题量",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(10, 20, 50, totalQuestions).distinct().forEach { count ->
                ActionPillButton(
                    icon = Icons.Rounded.PlayArrow,
                    text = if (count == totalQuestions) "全部 $count 题" else "$count 题",
                    primary = selectedQuestionCount == count,
                    onClick = { onSelectQuestionCount(count) }
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = "时长",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(10, 20, 30, 45).forEach { minutes ->
                ActionPillButton(
                    icon = Icons.Rounded.Timer,
                    text = "$minutes 分钟",
                    primary = selectedDurationMinutes == minutes,
                    onClick = { onSelectDuration(minutes) }
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionPillButton(
                icon = Icons.Rounded.PlayArrow,
                text = "开始考试",
                primary = true,
                onClick = onStartExam
            )
            ActionPillButton(
                icon = Icons.Rounded.Timer,
                text = "返回首页",
                primary = false,
                onClick = onBackHome
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveExamPanel() {
    val examQuestion = QuizRepository.currentExamQuestion() ?: return
    val questionType = examQuestion.type

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricGlassCard(
            label = "剩余时间",
            value = formatExamSeconds(QuizRepository.examRemainingSeconds),
            desc = "考试开始后会自动倒计时",
            modifier = Modifier.weight(1f)
        )
        MetricGlassCard(
            label = "已答题数",
            value = QuizRepository.examAnsweredCount().toString(),
            desc = "当前考试中已经填写答案的题目",
            modifier = Modifier.weight(1f)
        )
    }

    GlassCard {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusChip("第 ${QuizRepository.examIndex + 1} / ${QuizRepository.examQuestions.size} 题", selected = true)
            StatusChip(typeLabel(questionType))
            StatusChip("剩余 ${formatExamSeconds(QuizRepository.examRemainingSeconds)}")
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = examQuestion.question,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 34.sp
        )
        if (examQuestion.images.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            QuestionImagesBlock(examQuestion.images, maxPreviewHeight = 360.dp, showMeta = true)
        }
        Spacer(Modifier.height(18.dp))

        when (examQuestion.type) {
            QuestionType.SINGLE,
            QuestionType.MULTIPLE,
            QuestionType.JUDGE -> {
                val currentAnswer = QuizRepository.examAnswers[examQuestion.id].orEmpty()
                examQuestion.options.forEach { option ->
                    QuizOptionCard(
                        label = option.key,
                        text = option.text,
                        selected = currentAnswer.contains(option.key),
                        onClick = {
                            QuizRepository.toggleExamAnswer(
                                key = option.key,
                                multiple = examQuestion.type == QuestionType.MULTIPLE
                            )
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            QuestionType.BLANK,
            QuestionType.SHORT -> {
                NoticeCard("这道题属于 ${typeLabel(examQuestion.type)}。当前考试模式先完成客观题流程，主观题作答后续再补。")
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionPillButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                text = "上一题",
                primary = false,
                onClick = { QuizRepository.previousExamQuestion() }
            )
            ActionPillButton(
                icon = Icons.AutoMirrored.Rounded.ArrowForward,
                text = "下一题",
                primary = false,
                onClick = { QuizRepository.nextExamQuestion() }
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionPillButton(
                icon = Icons.Rounded.CheckCircle,
                text = "立即交卷",
                primary = true,
                onClick = { QuizRepository.submitExam() }
            )
            ActionPillButton(
                icon = Icons.Rounded.Timer,
                text = "结束本场",
                primary = false,
                onClick = { QuizRepository.resetExam() }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FinishedExamPanel(
    examSummary: ExamSummary,
    onRestart: () -> Unit,
    onGoPractice: () -> Unit,
    onBackHome: () -> Unit
) {
    GlassCard {
        Text(
            text = "考试结果",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        if (examSummary.autoSubmitted) {
            NoticeCard("本场考试因为倒计时结束而自动交卷。", warning = false)
            Spacer(Modifier.height(12.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricGlassCard(
                label = "总题数",
                value = examSummary.total.toString(),
                desc = "本场考试已载入的题目数",
                modifier = Modifier.weight(1f)
            )
            MetricGlassCard(
                label = "答题数",
                value = examSummary.answered.toString(),
                desc = "有作答记录的题目数",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricGlassCard(
                label = "正确数",
                value = examSummary.correct.toString(),
                desc = "当前已支持的客观题判分结果",
                modifier = Modifier.weight(1f)
            )
            MetricGlassCard(
                label = "正确率",
                value = "${examAccuracy(examSummary)}%",
                desc = "按本场已判分题目计算",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionPillButton(
                icon = Icons.Rounded.RestartAlt,
                text = "再来一场",
                primary = true,
                onClick = onRestart
            )
            ActionPillButton(
                icon = Icons.Rounded.PlayArrow,
                text = "去练习页",
                primary = false,
                onClick = onGoPractice
            )
            ActionPillButton(
                icon = Icons.Rounded.Timer,
                text = "返回首页",
                primary = false,
                onClick = onBackHome
            )
        }
    }

    GlassCard {
        Text(
            text = "逐题结果",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        QuizRepository.examQuestions.forEachIndexed { index, question ->
            val userAnswer = QuizRepository.examAnswers[question.id].orEmpty()
            val correctAnswer = question.answer
            val isCorrect = userAnswer.sorted() == correctAnswer.sorted() && correctAnswer.isNotEmpty()

            StatusChip(
                text = "第 ${index + 1} 题 · ${if (isCorrect) "正确" else if (userAnswer.isEmpty()) "未答" else "错误"}",
                selected = isCorrect
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = question.question,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            if (question.images.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                QuestionImagesBlock(question.images, maxPreviewHeight = 220.dp, showMeta = false)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "你的答案：${userAnswer.joinToString(" / ").ifBlank { "未作答" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "正确答案：${correctAnswer.joinToString(" / ").ifBlank { "未提供" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (question.analysis.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "解析：${question.analysis}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun formatExamSeconds(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun examAccuracy(summary: ExamSummary): Int {
    if (summary.total == 0) return 0
    return summary.correct * 100 / summary.total
}

private fun typeLabel(type: QuestionType): String = when (type) {
    QuestionType.SINGLE -> "单选题"
    QuestionType.MULTIPLE -> "多选题"
    QuestionType.JUDGE -> "判断题"
    QuestionType.BLANK -> "填空题"
    QuestionType.SHORT -> "简答题"
}
