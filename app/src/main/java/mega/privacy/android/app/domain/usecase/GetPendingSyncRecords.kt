package mega.privacy.android.app.domain.usecase

import mega.privacy.android.domain.entity.SyncRecord

/**
 * Get pending sync records
 *
 */
interface GetPendingSyncRecords {

    /**
     * Invoke
     *
     * @return pending sync records
     */
    operator fun invoke(): List<SyncRecord>
}
