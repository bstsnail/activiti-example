package com.bstsnail.activiti.config

import com.bstsnail.activiti.listener.ProcessEventListener
import com.bstsnail.activiti.utils.UUIDGenerator
import com.zaxxer.hikari.HikariDataSource
import org.activiti.engine.*
import org.activiti.engine.impl.cfg.IdGenerator
import org.activiti.engine.impl.history.HistoryLevel
import org.activiti.spring.*
import org.mariadb.jdbc.Driver
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.util.concurrent.Executor
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
open class ProcessEngineConfiguration {
    @Value("\${spring.datasource.url:#{null}}")
    private val datasourceUrl: String? = null
    @Value("\${spring.datasource.username:#{null}}")
    private val datasourceUsername: String? = null
    @Value("\${spring.datasource.password:#{null}}")
    private val datasourcePassword: String? = null

    @Bean
    open fun dataSource(): DataSource {
        if (datasourceUrl.isNullOrBlank()) {
            throw IllegalArgumentException("Process engine database address is not configured")
        }
        return HikariDataSource().apply {
            poolName  = "DBPool-Process"
            jdbcUrl = datasourceUrl
            username = datasourceUsername
            password = datasourcePassword
            driverClassName = Driver::class.java.name
            minimumIdle = 5
            maximumPoolSize = 50
            idleTimeout = 60000
        }
    }

    @Bean
    open fun transactionManager(dataSourceEngine: DataSource): PlatformTransactionManager {
        return DataSourceTransactionManager(dataSourceEngine)
    }

    @Bean
    open fun taskExecutor(): Executor {
        return SimpleAsyncTaskExecutor()
    }

    @Bean
    open fun taskScheduler(): TaskScheduler {
        return ConcurrentTaskScheduler()
    }

    @Bean(name = ["processEngineThreadPoolTaskExecutor"])
    open fun processEngineThreadPoolTaskExecutor() = ThreadPoolTaskExecutor().apply {
        corePoolSize = 10
        maxPoolSize = 80
        setQueueCapacity(1000)
    }

    @Bean
    open fun springRejectedJobsHandler(): SpringRejectedJobsHandler {
        return SpringCallerRunsRejectedJobsHandler()
    }

    @Bean
    open fun springAsyncExecutor(
            @Qualifier("processEngineThreadPoolTaskExecutor") processEngineThreadPoolTaskExecutor: ThreadPoolTaskExecutor,
            springRejectedJobsHandler: SpringRejectedJobsHandler) : SpringAsyncExecutor {
        return SpringAsyncExecutor(processEngineThreadPoolTaskExecutor, springRejectedJobsHandler)
    }

    @Bean
    open fun springProcessEngineConfiguration(
            transactionManager: PlatformTransactionManager,
            dataSource: DataSource,
            springAsyncExecutor: SpringAsyncExecutor
    ) : SpringProcessEngineConfiguration {
        return SpringProcessEngineConfiguration().apply {
            this.dataSource = dataSource
            this.transactionManager = transactionManager
            asyncExecutor = springAsyncExecutor
            isAsyncExecutorActivate = true
            isActiviti5CompatibilityEnabled = false
            deploymentName = "soda"
            databaseSchemaUpdate = "true"
            isDbHistoryUsed = true
            isDbIdentityUsed = false
            historyLevel = HistoryLevel.AUDIT
            idGenerator = IdGenerator {
                UUIDGenerator.generate()
            }
            eventListeners = listOf(
                    ProcessEventListener()
            )
        }
    }

    @Bean
    open fun processEngine(springProcessEngineConfiguration: SpringProcessEngineConfiguration): ProcessEngineFactoryBean {
        return ProcessEngineFactoryBean().apply {
            processEngineConfiguration = springProcessEngineConfiguration
        }
    }

    @Bean
    open fun runtimeService(processEngine: ProcessEngine): RuntimeService {
        return processEngine.runtimeService
    }

    @Bean
    open fun repositoryService(processEngine: ProcessEngine): RepositoryService {
        return processEngine.repositoryService
    }

    @Bean
    open fun taskService(processEngine: ProcessEngine): TaskService {
        return processEngine.taskService
    }

    @Bean
    open fun historyService(processEngine: ProcessEngine): HistoryService {
        return processEngine.historyService
    }

    @Bean
    open fun managementServiceBean(processEngine: ProcessEngine): ManagementService {
        return processEngine.managementService
    }

    @Bean
    open fun formService(processEngine: ProcessEngine): FormService {
        return processEngine.formService
    }

    @Bean
    open fun identityService(processEngine: ProcessEngine): IdentityService {
        return processEngine.identityService
    }
}