package mega.privacy.android.domain.usecase.chat.message

import mega.privacy.android.domain.entity.chat.PendingMessage
import mega.privacy.android.domain.entity.chat.PendingMessageState
import mega.privacy.android.domain.entity.chat.messages.pending.SavePendingMessageRequest
import mega.privacy.android.domain.entity.node.NodeId
import mega.privacy.android.domain.repository.ChatRepository
import mega.privacy.android.domain.repository.chat.ChatMessageRepository
import mega.privacy.android.domain.usecase.chat.GetChatMessageUseCase
import javax.inject.Inject

/**
 * Attach a Node with the information contained in a PendingMessage once the file is uploaded
 * It will update the status of the pending message and delete it if everything goes right
 */
class AttachNodeWithPendingMessageUseCase @Inject constructor(
    private val chatMessageRepository: ChatMessageRepository,
    private val chatRepository: ChatRepository,
    private val getChatMessageUseCase: GetChatMessageUseCase,
    private val createSaveSentMessageRequestUseCase: CreateSaveSentMessageRequestUseCase,
) {
    /**
     * Invoke
     *
     * @param pendingMessageId
     * @param nodeId of the already uploaded file that will be attached to the chat
     */
    suspend operator fun invoke(pendingMessageId: Long, nodeId: NodeId) {

        chatMessageRepository.getPendingMessage(pendingMessageId)
            ?.let { pendingMessage ->
                pendingMessage.updateState(
                    PendingMessageState.ATTACHING,
                    nodeId.longValue
                )
                val chatId = pendingMessage.chatId
                chatMessageRepository.attachNode(chatId, nodeId.longValue)
                    ?.let {
                        getChatMessageUseCase(chatId, it)?.let { message ->
                            val request = createSaveSentMessageRequestUseCase(message, chatId)
                            chatRepository.storeMessages(listOf(request))
                            chatMessageRepository.deletePendingMessage(pendingMessage)
                        }
                    } ?: run {
                    pendingMessage.updateState(
                        PendingMessageState.ERROR_ATTACHING
                    )
                }
            }
    }

    private suspend fun PendingMessage.updateState(
        state: PendingMessageState,
        nodeHandle: Long = this.nodeHandle,
    ) {
        chatMessageRepository.savePendingMessage(
            SavePendingMessageRequest(
                chatId = this.chatId,
                type = this.type,
                uploadTimestamp = this.uploadTimestamp,
                state = state,
                tempIdKarere = this.tempIdKarere,
                videoDownSampled = this.videoDownSampled,
                filePath = this.filePath,
                nodeHandle = nodeHandle,
                fingerprint = this.fingerprint,
                name = this.name,
                transferTag = this.transferTag,
            )
        )
    }
}