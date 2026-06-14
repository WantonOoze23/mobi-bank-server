import features.accounts.AccountRepository
import features.accounts.AccountService
import features.accounts.models.OpenCardRequest
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class AccountServiceTest {

    private val repository = mockk<AccountRepository>()
    private val service = AccountService(repository)

    @Test
    fun `openCard should return failure for unsupported currency`() = runTest {
        // Arrange
        val userId = UUID.randomUUID().toString()
        val request = OpenCardRequest(currency = "PLN", type = "DEBIT")

        // Act
        val result = service.openCard(userId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Непідтримувана валюта. Доступні: USD, EUR, UAH", result.exceptionOrNull()?.message)
    }

    @Test
    fun `openCard should generate correct 16-digit BIN starting with 4149`() = runTest {
        // Arrange
        val userId = UUID.randomUUID().toString()
        val request = OpenCardRequest(currency = "UAH", type = "DEBIT")

        val cardNumberSlot = slot<String>()

        coEvery {
            repository.createAccount(any(), capture(cardNumberSlot), eq("UAH"), eq("DEBIT"))
        } returns null

        // Act
        service.openCard(userId, request)

        // Assert
        val capturedCardNumber = cardNumberSlot.captured
        assertTrue("Номер картки має починатися з 4149", capturedCardNumber.startsWith("4149"))
        assertEquals("Довжина номера картки має бути 16 цифр", 16, capturedCardNumber.length)
    }
}