package i.bobrov.jpa_exmp.controller

import i.bobrov.jpa_exmp.common.Contextualized
import i.bobrov.jpa_exmp.common.Response
import i.bobrov.jpa_exmp.common.Result
import i.bobrov.jpa_exmp.dto.TasksGetDetailsRequest
import i.bobrov.jpa_exmp.dto.TasksGetDetailsResponse as TasksResponse
import i.bobrov.jpa_exmp.tasks.TasksService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/tasks")
class TasksController(
    private val service: TasksService
) {

    @PostMapping("/get-details")
    fun getTaskDetails(
        @Valid @RequestBody request: Contextualized<TasksGetDetailsRequest>,
        @RequestHeader(value = "x-user-id", required = false) userId: String?,
    ): Response<Contextualized<TasksResponse>> =
        Result.success(Contextualized(request.context, service.getDetails(request.data)))
}