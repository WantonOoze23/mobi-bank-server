import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import ua.mobibank.features.deposits.DepositRepository
import ua.mobibank.features.deposits.DepositService
import ua.mobibank.features.deposits.models.OpenDepositRequest
import java.util.UUID

class DepositServiceTest {

    private val repository = mockk<DepositRepository>()
    private val service = DepositService(repository)

    @Test
    fun `openDeposit should apply 14 percent interest rate for 12 months duration`() = runTest {
        // Arrange
        val userId = UUID.randomUUID().toString()
        val accountId = UUID.randomUUID().toString()
        val request = OpenDepositRequest(accountId = accountId, amount = 5000.0, durationMonths = 12)

        coEvery {
            repository.openDeposit(any(), any(), any(), any(), any(), any())
        } returns mockk(relaxed = true)

        // Act
        service.openDeposit(userId, request)

        // Assert: перевіряємо, чи сервіс передав у репозиторій правильну ставку (14.0)
        coVerify(exactly = 1) {
            repository.openDeposit(
                userId = any(),
                accountId = any(),
                amount = eq(5000.0),
                interestRate = eq(14.0), // Перевірка бізнес-правила
                startDate = any(),
                endDate = any()
            )
        }
    }
}