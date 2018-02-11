package com.bstsnail.activiti.utils

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class SpringContextUtil : ApplicationContextAware {

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        SpringContextUtil.applicationContext = applicationContext
    }

    companion object {
        private var applicationContext: ApplicationContext? = null

        @Throws(BeansException::class)
        fun <T> getBean(clazz: Class<T>): T {
            return applicationContext!!.getBean(clazz)
        }
    }
}
