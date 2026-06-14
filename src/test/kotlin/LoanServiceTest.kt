import features.loans.LoanRepository
import features.loans.LoanService
import features.loans.models.ApplyLoanRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.ResultRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class LoanServiceTest {

    private val repository = mockk<LoanRepository>()
    private val service = LoanService(repository)

    @Test
    fun `applyForLoan should return failure when term in days is zero`() = runTest {
        // Arrange
        val userId = UUID.randomUUID().toString()
        val request = ApplyLoanRequest(
            accountId = UUID.randomUUID().toString(),
            amount = 1000.0,
            termInDays = 0 // Некоректний термін
        )

        // Act
        val result = service.applyForLoan(userId, request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Термін кредиту має бути більшим за нуль", result.exceptionOrNull()?.message)
    }

    @Test
    fun `applyForLoan should calculate totalToRepay correctly`() = runTest {
        // Arrange
        val userId = UUID.randomUUID().toString()
        val accountId = UUID.randomUUID().toString()
        val request = ApplyLoanRequest(accountId = accountId, amount = 1000.0, termInDays = 30)

        coEvery {
            repository.createLoanAndFundAccount(
                userId = eq(UUID.fromString(userId)),
                accountId = eq(UUID.fromString(accountId)),
                amount = eq(1000.0),
                interestRate = eq(5.0),
                totalToRepay = eq(1050.0),
                dueDate = any()
            )
        } returns null

        // Act
        val result = service.applyForLoan(userId, request)

        // Assert
        coVerify(exactly = 1) {
            repository.createLoanAndFundAccount(
                userId = eq(UUID.fromString(userId)),
                accountId = eq(UUID.fromString(accountId)),
                amount = eq(1000.0),
                interestRate = eq(5.0),
                totalToRepay = eq(1050.0), // Ось наша перевірка: 1000 + 5% = 1050!
                dueDate = any()
            )
        }
    }
}