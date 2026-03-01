package i.bobrov.jpa_exmp.common

enum class ErrorCode { TASK_IS_NOT_FOUND }

class PickingServiceException(
    val code: ErrorCode,
    override val message: String
) : RuntimeException(message)