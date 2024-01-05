package mega.privacy.android.domain.usecase.chat.link

import mega.privacy.android.domain.repository.ChatRepository
import javax.inject.Inject

/**
 * Join public chat
 *
 * @property chatRepository
 */
class JoinPublicChatUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
) {

    /**
     * Invoke
     *
     * @param chatId
     * @param chatPublicHandle
     * @param autoJoin
     */
    suspend operator fun invoke(
        chatId: Long,
        chatPublicHandle: Long,
        exist: Boolean,
    ) {
        if (exist) {
            chatRepository.autorejoinPublicChat(chatId, chatPublicHandle)
        } else {
            chatRepository.autojoinPublicChat(chatId)
        }
        chatRepository.setLastPublicHandle(chatId)
    }
}