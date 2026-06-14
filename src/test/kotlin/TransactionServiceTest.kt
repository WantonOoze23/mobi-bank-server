package ua.mobibank.features.transactions

import features.transactions.TransactionRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.ResultRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ua.mobibank.features.transactions.model.TopUpRequest
import ua.mobibank.features.transactions.model.TransferRequest
import java.util.UUID

class TransactionServiceTest {

    private val repository = mockk<TransactionRepository>()
    private val service = TransactionService(repository)

    @Test
    fun `transfer should return failure when amount is negative or zero`() = runTest {
        // Arrange (Підготовка)
        val userId = UUID.randomUUID().toString()
        val request = TransferRequest(
            senderAccountId = UUID.randomUUID().toString(),
            receiverCardNumber = "4149123456789012",
            amount = -100.0 // Некоректна сума
        )

        val result = service.transfer(userId, request)

        assertTrue(result.isFailure)
        assertEquals("Сума має бути більшою за нуль", result.exceptionOrNull()?.message)
    }

    @Test
    fun `topUpCard should return success when amount is valid`() = runTest {
        val request = TopUpRequest(cardNumber = "4149123456789012", amount = 500.0)

        coEvery {
            repository.topUpAccount(request.cardNumber, request.amount, any())
        } returns true

        // Act
        val result = service.topUpCard(request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("Картку успішно поповнено на 500.0", result.getOrNull())
    }
}