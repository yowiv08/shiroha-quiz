package com.yiqiu.shirohaquiz.importer.validate

import com.yiqiu.shirohaquiz.importer.model.ImportWarning
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.importer.model.WarningLevel

object ImportValidator {
    fun validate(questions: List<Question>): List<ImportWarning> {
        val warnings = mutableListOf<ImportWarning>()

        questions.forEach { question ->
            if (question.question.isBlank()) {
                warnings += ImportWarning(WarningLevel.ERROR, question.number, "题干为空")
            }

            when (question.type) {
                QuestionType.SINGLE -> {
                    if (question.options.size < 2) {
                        warnings += ImportWarning(WarningLevel.ERROR, question.number, "单选题缺少足够选项")
                    }
                    if (question.answer.isEmpty()) {
                        warnings += ImportWarning(WarningLevel.WARNING, question.number, "单选题未识别到答案")
                    }
                    if (question.answer.size > 1) {
                        warnings += ImportWarning(WarningLevel.ERROR, question.number, "单选题出现多个答案")
                    }
                    if (question.options.size == 2) {
                        val optionTexts = question.options.map { it.text }
                        if (optionTexts.all { it in listOf("正确", "错误", "对", "错", "是", "否") }) {
                            warnings += ImportWarning(
                                WarningLevel.WARNING,
                                question.number,
                                "两选项单选题，请确认这不是判断题分区内容"
                            )
                        }
                    }
                }

                QuestionType.MULTIPLE -> {
                    if (question.options.size < 2) {
                        warnings += ImportWarning(WarningLevel.ERROR, question.number, "多选题缺少足够选项")
                    }
                    if (question.answer.size < 2) {
                        warnings += ImportWarning(WarningLevel.WARNING, question.number, "多选题答案数量偏少，请确认")
                    }
                }

                QuestionType.JUDGE -> {
                    if (question.answer.isEmpty()) {
                        warnings += ImportWarning(WarningLevel.WARNING, question.number, "判断题未识别到答案")
                    }
                }

                QuestionType.BLANK, QuestionType.SHORT -> {
                    if (question.answer.isEmpty()) {
                        warnings += ImportWarning(WarningLevel.WARNING, question.number, "主观题未识别到参考答案")
                    }
                }
            }
        }

        return warnings
    }
}
