package mega.privacy.android.app.presentation.node.view.bottomsheetmenuitems

import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import mega.privacy.android.app.main.FileContactListActivity
import mega.privacy.android.app.presentation.extensions.isOutShare
import mega.privacy.android.app.presentation.node.model.menuaction.ManageShareFolderMenuAction
import mega.privacy.android.core.ui.model.MenuAction
import mega.privacy.android.core.ui.model.MenuActionWithIcon
import mega.privacy.android.domain.entity.node.TypedNode
import mega.privacy.android.domain.entity.shares.AccessPermission
import javax.inject.Inject

/**
 * Manage share folder bottom sheet menu item
 *
 * @param menuAction [ManageShareFolderMenuAction]
 */
class ManageShareFolderBottomSheetMenuItem @Inject constructor(
    override val menuAction: ManageShareFolderMenuAction,
) : NodeBottomSheetMenuItem<MenuActionWithIcon> {
    override suspend fun shouldDisplay(
        isNodeInRubbish: Boolean,
        accessPermission: AccessPermission?,
        isInBackups: Boolean,
        node: TypedNode,
        isConnected: Boolean,
    ) = node.isTakenDown.not()
            && accessPermission == AccessPermission.OWNER
            && isNodeInRubbish.not()
            && node.isOutShare()

    override fun getOnClickFunction(
        node: TypedNode,
        onDismiss: () -> Unit,
        actionHandler: (menuAction: MenuAction, node: TypedNode) -> Unit,
        navController: NavHostController,
        parentCoroutineScope: CoroutineScope,
    ): () -> Unit = {
        onDismiss()
        val context = navController.context
        val intent = FileContactListActivity.launchIntent(
            context,
            node.id.longValue
        )
        context.startActivity(intent)
    }

    override val groupId = 7

}


