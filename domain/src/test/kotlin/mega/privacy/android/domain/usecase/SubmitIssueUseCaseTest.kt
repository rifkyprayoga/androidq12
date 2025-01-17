package mega.privacy.android.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import mega.privacy.android.domain.entity.Progress
import mega.privacy.android.domain.entity.SubmitIssueRequest
import mega.privacy.android.domain.entity.SupportTicket
import mega.privacy.android.domain.repository.SupportRepository
import mega.privacy.android.domain.usecase.logging.GetZippedLogsUseCase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File

@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubmitIssueUseCaseTest {
    private lateinit var underTest: SubmitIssueUseCase
    private val supportTicket = SupportTicket(
        androidAppVersion = "appVersion",
        sdkVersion = "sdkVersion",
        device = "device",
        accountEmail = "accountEmail",
        accountType = "accountTypeString",
        currentLanguage = "languageCode",
        description = "description",
        logFileName = "123-fileName.zip",
        deviceSdkVersionInt = 31,
        deviceSdkVersionName = "Android 12"
    )

    private val compressedLogs = File("path/to/${supportTicket.logFileName}")
    private val percentage = 1F
    private val formattedSupportTicket = "formattedSupportTicket"

    private val supportRepository = mock<SupportRepository>()
    private val createSupportTicketUseCase = mock<CreateSupportTicketUseCase>()
    private val formatSupportTicketUseCase = mock<FormatSupportTicketUseCase>()
    private val getZippedLogsUseCase = mock<GetZippedLogsUseCase>()

    @BeforeEach
    fun setUp() {
        getZippedLogsUseCase.stub {
            onBlocking { invoke() }.thenReturn(compressedLogs)
        }

        supportRepository.stub {
            on { uploadFile(compressedLogs) }.thenReturn(flowOf(percentage))
        }

        createSupportTicketUseCase.stub {
            onBlocking {
                invoke(supportTicket.description, supportTicket.logFileName)
            }.thenReturn(supportTicket)
        }

        formatSupportTicketUseCase.stub {
            on {
                invoke(any())
            }.thenReturn(formattedSupportTicket)
        }

        underTest = SubmitIssueUseCase(
            supportRepository = supportRepository,
            createSupportTicketUseCase = createSupportTicketUseCase,
            formatSupportTicketUseCase = formatSupportTicketUseCase,
            getZippedLogsUseCase = getZippedLogsUseCase,
        )
    }

    @AfterEach
    fun resetMocks() {
        Mockito.clearInvocations(
            getZippedLogsUseCase,
            supportRepository,
            createSupportTicketUseCase,
            formatSupportTicketUseCase,
        )
        reset(
            getZippedLogsUseCase,
            supportRepository,
            createSupportTicketUseCase,
            formatSupportTicketUseCase,
        )
    }

    @Test
    fun `test that logs are compressed when include logs is set to true`() = runTest {
        underTest.call(true).test { cancelAndIgnoreRemainingEvents() }
        verify(getZippedLogsUseCase).invoke()
    }

    @Test
    fun `test that logs are not compressed when include logs is set to false`() = runTest {
        underTest.call(false).test { cancelAndIgnoreRemainingEvents() }
        verify(getZippedLogsUseCase, never()).invoke()
    }

    @Test
    fun `test that logs are uploaded after compressing`() = runTest {
        underTest.call(true).test { cancelAndIgnoreRemainingEvents() }
        verify(supportRepository).uploadFile(compressedLogs)
    }

    @Test
    fun `test that file upload progress is returned`() = runTest {
        underTest.call(true).test {
            assertThat(awaitItem()).isEqualTo(Progress(percentage))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test that support ticket is created`() = runTest {
        underTest.call(false).test { cancelAndIgnoreRemainingEvents() }

        verify(createSupportTicketUseCase).invoke(supportTicket.description, null)
    }

    @Test
    fun `test that support ticket is created with log file name if include logs is set to true`() =
        runTest {
            val compressedLogs = File("log/path/file.zip")
            whenever(getZippedLogsUseCase()).thenReturn(compressedLogs)
            whenever(supportRepository.uploadFile(any())).thenReturn(emptyFlow())

            underTest.call(true).test { cancelAndIgnoreRemainingEvents() }

            verify(createSupportTicketUseCase).invoke(supportTicket.description, compressedLogs.name)
        }

    @Test
    fun `test that ticket is formatted`() = runTest {

        underTest.call(true).test { cancelAndIgnoreRemainingEvents() }

        verify(formatSupportTicketUseCase).invoke(supportTicket)
    }

    @Test
    fun `test that formatted ticket is submitted`() = runTest {
        underTest(
            SubmitIssueRequest(
                description = supportTicket.description,
                includeLogs = true
            )
        ).test { cancelAndIgnoreRemainingEvents() }

        verify(supportRepository).logTicket(formattedSupportTicket)
    }

    @Test
    fun `test that ticket is not created if cancelled`() = runTest {
        underTest.call(true).first()

        verify(getZippedLogsUseCase).invoke()
        verify(formatSupportTicketUseCase, never()).invoke(any())
        verify(supportRepository, never()).logTicket(any())
    }

    private suspend fun SubmitIssueUseCase.call(includeLogs: Boolean) = invoke(
        SubmitIssueRequest(
            description = supportTicket.description,
            includeLogs = includeLogs
        )
    )
}