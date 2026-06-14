import com.mobibank.features.auth.AuthRepository
import com.mobibank.features.auth.models.RegisterRequest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.ResultRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.mobibank.features.auth.AuthService

class AuthServiceTest {

    private val repository = mockk<AuthRepository>()
    private val service = AuthService(repository)

    @Test
    fun `register should fail if user with this email already exists`() = runTest {
        // Arrange
        val request = RegisterRequest(
            firstName = "Ivan",
            lastName = "Ivanov",
            middleName = null,
            phone = "+380991234567",
            email = "existing@email.com",
            password = "securePassword123"
        )

        coEvery { repository.getUserByLogin(request.email) } returns mockk<ResultRow>()

        // Act & Assert
        val exception = try {
            service.register(request)
            null // Якщо помилки не було, повертаємо null
        } catch (e: Exception) {
            e
        }

        assertTrue("Має бути викинуто виняток", exception != null)
        assertEquals("User with this email or phone number already exists", exception?.message)
    }
}