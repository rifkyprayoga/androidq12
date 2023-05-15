package mega.privacy.android.domain.usecase.meeting

import kotlinx.coroutines.flow.Flow
import mega.privacy.android.domain.entity.CallsMeetingReminders
import mega.privacy.android.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Gets calls meeting reminders preference.
 * @property settingsRepository
 */
class GetCallsMeetingReminders @Inject constructor(
    val settingsRepository: SettingsRepository,
) {

    /**
     * Invoke.
     *
     * @return callsMeetingInvitations meeting invitations.
     */
    operator fun invoke(): Flow<CallsMeetingReminders> =
        settingsRepository.getCallsMeetingReminders()
}
