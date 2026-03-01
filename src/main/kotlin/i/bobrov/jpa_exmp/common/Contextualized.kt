package i.bobrov.jpa_exmp.common

data class Contextualized<T>(
    val context: Map<String, String> = emptyMap(),
    val data: T
)

data class ApiError(
    val code: String = "999",
    val message: String = ""
)

data class Response<T>(
    val success: Boolean,
    val result: T? = null,
    val error: ApiError? = null
)

object Result {

    fun <T> success(value: T): Response<T> =
        Response(
            success = true,
            result = value,
            error = null
        )

    fun failure(code: String, message: String = ""): Response<Nothing> =
        Response(
            success = false,
            result = null,
            error = ApiError(code = code, message = message)
        )
}