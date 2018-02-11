package com.bstsnail.activiti

import com.bstsnail.activiti.utils.UUIDGenerator
import org.activiti.bpmn.model.*
import org.activiti.engine.HistoryService
import org.activiti.engine.RepositoryService
import org.activiti.engine.RuntimeService
import org.activiti.engine.TaskService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
open class Application {

    @Bean
    open fun init(repositoryService: RepositoryService,
                  runtimeService: RuntimeService,
                  taskService: TaskService,
                  historyService: HistoryService) =
            CommandLineRunner {
                logger.info("Run the command line runner")
                val process = Process().apply {
                    id = "p-${UUIDGenerator.generate()}"
                    name = "Test"
                    documentation = "description"
                }

                val bpmnModel = BpmnModel().apply {
                    targetNamespace = "test"
                    addProcess(process)
                }

                val startEvent = StartEvent().apply {
                    name = "testStartEvent"
                    id = "s-${UUIDGenerator.generate()}"
                }

                val task = ServiceTask().apply {
                    name = "testDelayTask"
                    id = "t-${UUIDGenerator.generate()}"
                    isAsynchronous = true
                    implementation = "\${delayTaskDelegate}"
                    implementationType = ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION
                    fieldExtensions = listOf(
                            FieldExtension().apply {
                                fieldName = "delaySeconds"
                                stringValue = "2"
                            }
                    )
                }

                val userTask = UserTask().apply {
                    name = "testUserTask"
                    id = "t-${UUIDGenerator.generate()}"
                    isAsynchronous = true
                }

                val userTask1 = UserTask().apply {
                    name = "testUserTask1"
                    id = "t-${UUIDGenerator.generate()}"
                    isAsynchronous = true
                }

                val userTask2 = UserTask().apply {
                    name = "testUserTask2"
                    id = "t-${UUIDGenerator.generate()}"
                    isAsynchronous = true
                }

                val end = EndEvent().apply {
                    name = "End"
                    id = "e-${UUIDGenerator.generate()}"
                }

                process.addFlowElement(startEvent)
                process.addFlowElement(task)
                process.addFlowElement(userTask)
                process.addFlowElement(userTask1)
                process.addFlowElement(userTask2)
                process.addFlowElement(end)

                process.addFlowElement(SequenceFlow().apply {
                    sourceRef = startEvent.id
                    targetRef = task.id
                })

                process.addFlowElement(SequenceFlow().apply {
                    sourceRef = task.id
                    targetRef = userTask.id
                })

                process.addFlowElement(SequenceFlow().apply {
                    sourceRef = userTask.id
                    targetRef = userTask1.id
                })

                process.addFlowElement(SequenceFlow().apply {
                    sourceRef = userTask1.id
                    targetRef = userTask2.id
                })

                process.addFlowElement(SequenceFlow().apply {
                    sourceRef = userTask2.id
                    targetRef = end.id
                })

                repositoryService.createDeployment()
                        .addBpmnModel("test.bpmn20.xml", bpmnModel)
                        .name("test")
                        .category("test")
                        .deploy()

                val processDefinition = repositoryService.createProcessDefinitionQuery()
                        .processDefinitionKey(process.id)
                        .singleResult()

                runtimeService.startProcessInstanceById(processDefinition.id)
                val processInstance = runtimeService.createProcessInstanceQuery()
                        .processDefinitionId(processDefinition.id)
                        .singleResult()


                //Thread.sleep(10000)
                //val t = taskService.createTaskQuery().taskUnassigned().singleResult()
                //taskService.claim(t.id, "claim")
                //taskService.complete(t.id)

                Thread.sleep(10000)
                val t = taskService.createTaskQuery().taskUnassigned().singleResult()
                taskService.claim(t.id, "claim")
                taskService.complete(t.id)
                /*
                taskService.createTaskQuery().list()
                val hiInstance = historyService.createHistoricTaskInstanceQuery()
                        .processInstanceId(processInstance.id)
                        .list()
                logger.info("Active task size(${hiInstance.size})")
                */
                Thread.sleep(5000)
                logger.info("Start to shutdown the process")
                runtimeService.deleteProcessInstance(processInstance.id, "Cancel by me....")
            }

    companion object {
        private val logger = LoggerFactory.getLogger(Application::class.java)
    }
}

fun main(argv: Array<String>) {
    SpringApplication.run(Application::class.java, *argv)
}