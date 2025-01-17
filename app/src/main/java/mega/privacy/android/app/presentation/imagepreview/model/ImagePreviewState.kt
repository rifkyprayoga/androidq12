package mega.privacy.android.app.presentation.imagepreview.model

import de.palm.composestateevents.StateEventWithContent
import de.palm.composestateevents.consumed
import mega.privacy.android.app.namecollision.data.NameCollision
import mega.privacy.android.app.presentation.transfers.startdownload.model.TransferTriggerEvent
import mega.privacy.android.domain.entity.account.AccountDetail
import mega.privacy.android.domain.entity.node.ImageNode

data class ImagePreviewState(
    val isInitialized: Boolean = false,
    val imageNodes: List<ImageNode> = emptyList(),
    val currentImageNode: ImageNode? = null,
    val currentImageNodeIndex: Int = 0,
    val isCurrentImageNodeAvailableOffline: Boolean = false,
    val showAppBar: Boolean = true,
    val inFullScreenMode: Boolean = false,
    val transferMessage: String = "",
    val resultMessage: String = "",
    val copyMoveException: Throwable? = null,
    val nameCollision: NameCollision? = null,
    val downloadEvent: StateEventWithContent<TransferTriggerEvent> = consumed(),
    val showDeletedMessage: Boolean = false,
    val accountDetail: AccountDetail? = null,
    val isHiddenNodesOnboarded: Boolean? = null,
)