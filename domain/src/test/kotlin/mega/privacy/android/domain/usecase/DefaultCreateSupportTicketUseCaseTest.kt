package mega.privacy.android.domain.usecase

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import mega.privacy.android.domain.entity.AccountType
import mega.privacy.android.domain.entity.AppInfo
import mega.privacy.android.domain.entity.DeviceInfo
import mega.privacy.android.domain.entity.SupportTicket
import mega.privacy.android.domain.entity.UserAccount
import mega.privacy.android.domain.entity.user.UserId
import mega.privacy.android.domain.repository.EnvironmentRepository
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class DefaultCreateSupportTicketUseCaseTest {
    private lateinit var underTest: CreateSupportTicketUseCase
    private val deviceRepository = mock<EnvironmentRepository>()
    private val getAccountDetailsUseCase = mock<GetAccountDetailsUseCase>()

    private val device = "device"
    private val languageCode = "languageCode"
    private val appVersion = "appVersion"
    private val sdkVersion = "sdkVersion"
    private val accountEmail = "accountEmail"
    private val accountFullName = "accountFullName"
    private val accountTypeString = "accountTypeString"
    private val fileName = "123-fileName.zip"
    private val description = "Issue description"
    private val deviceSdkVersionInt = 31
    private val deviceSdkVersionName = "Android 12"

    @Before
    fun setUp() {
        deviceRepository.stub {
            onBlocking { getDeviceInfo() }.thenReturn(
                DeviceInfo(
                    device = device,
                    language = languageCode
                )
            )

            onBlocking { getAppInfo() }.thenReturn(
                AppInfo(
                    appVersion = appVersion,
                    sdkVersion = sdkVersion
                )
            )
            onBlocking { getDeviceSdkVersionInt() }.thenReturn(deviceSdkVersionInt)
            onBlocking { getDeviceSdkVersionName() }.thenReturn(deviceSdkVersionName)
        }


        runBlocking {
            whenever(getAccountDetailsUseCase(false)).thenReturn(
                UserAccount(
                    userId = UserId(1L),
                    email = accountEmail,
                    fullName = accountFullName,
                    isBusinessAccount = true,
                    isMasterBusinessAccount = true,
                    accountTypeIdentifier = AccountType.FREE,
                    accountTypeString = accountTypeString
                )
            )
        }

        underTest = CreateSupportTicketUseCase(
            environmentRepository = deviceRepository,
            getAccountDetailsUseCase = getAccountDetailsUseCase
        )
    }

    @Test
    fun `test that device and app info is retrieved`() = runTest {
        underTest(description = description, null)

        verify(deviceRepository).getDeviceInfo()
        verify(deviceRepository).getAppInfo()
    }

    @Test
    fun `test that account information is retrieved`() = runTest {
        underTest(description = description, null)
        verify(getAccountDetailsUseCase).invoke(false)
    }

    @Test
    fun `test that device sdk version int is retrieved`() = runTest {
        underTest(description = description, null)
        verify(deviceRepository).getDeviceSdkVersionInt()
    }

    @Test
    fun `test that device sdk version name is retrieved`() = runTest {
        underTest(description = description, null)
        verify(deviceRepository).getDeviceSdkVersionName()
    }

    @Test
    fun `test that expected ticket is returned`() = runTest {
        val expected = SupportTicket(
            androidAppVersion = appVersion,
            sdkVersion = sdkVersion,
            device = device,
            accountType = accountTypeString,
            accountEmail = accountEmail,
            currentLanguage = languageCode,
            description = description,
            logFileName = fileName,
            deviceSdkVersionInt = deviceSdkVersionInt,
            deviceSdkVersionName = deviceSdkVersionName,
        )

        val actual = underTest(description, fileName)

        assertThat(actual).isEqualTo(expected)
    }
}