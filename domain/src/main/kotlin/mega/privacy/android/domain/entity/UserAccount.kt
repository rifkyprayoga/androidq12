package mega.privacy.android.domain.entity

import mega.privacy.android.domain.entity.user.UserId

/**
 * User account
 *
 * @property userId
 * @property email
 * @property isBusinessAccount
 * @property isMasterBusinessAccount
 * @property accountTypeIdentifier
 * @property accountTypeString
 */
data class UserAccount(
    val userId: UserId?,
    val email: String,
    val isBusinessAccount: Boolean,
    val isMasterBusinessAccount: Boolean,
    val accountTypeIdentifier: Int,
    val accountTypeString: String,
)
