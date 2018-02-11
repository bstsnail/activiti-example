package com.bstsnail.activiti.tasks

import com.bstsnail.activiti.utils.SpringContextUtil
import org.activiti.engine.HistoryService
import org.activiti.engine.delegate.DelegateExecution
import org.activiti.engine.delegate.Expression
import org.activiti.engine.delegate.JavaDelegate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class DelayTaskDelegate: JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val delaySecondsValue = delaySeconds.getValue(execution) as String
        val delay = delaySecondsValue.toInt()
        logger.info("Execute delay task($delay)")
        Thread.sleep(delay * 1000L)
        logger.info("Finish the delay task")
        val historyService = SpringContextUtil.getBean(HistoryService::class.java)
        val processInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(execution.processInstanceId)
                .singleResult()
        logger.info("Get the delete reason(${processInstance.deleteReason})")
    }

    private lateinit var delaySeconds: Expression

    companion object {
        private val logger = LoggerFactory.getLogger(DelayTaskDelegate::class.java)
    }
}