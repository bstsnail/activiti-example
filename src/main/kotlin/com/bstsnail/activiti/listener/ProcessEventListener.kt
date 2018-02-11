package com.bstsnail.activiti.listener

import com.bstsnail.activiti.utils.SpringContextUtil
import org.activiti.engine.TaskService
import org.activiti.engine.delegate.event.ActivitiEvent
import org.activiti.engine.delegate.event.ActivitiEventListener
import org.activiti.engine.delegate.event.ActivitiEventType
import org.slf4j.LoggerFactory

class ProcessEventListener: ActivitiEventListener {

    override fun isFailOnException() = false

    override fun onEvent(event: ActivitiEvent) {
        logger.info("Receive the activiti event - $event")
        if (event.type == ActivitiEventType.PROCESS_CANCELLED) {
            logger.info("Cancel the process")
            logger.info("Active task size(${SpringContextUtil.getBean(TaskService::class.java).createTaskQuery().processInstanceId(event.processInstanceId).active().list().size})")
            logger.info("Task size(${SpringContextUtil.getBean(TaskService::class.java).createTaskQuery().processInstanceId(event.processInstanceId).list().size})")
        }
    }
    companion object {
        private val logger = LoggerFactory.getLogger(ProcessEventListener::class.java)
    }
}