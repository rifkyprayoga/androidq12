package mega.privacy.android.app.presentation.meeting.chat.view.sheet

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import mega.privacy.android.analytics.Analytics
import mega.privacy.android.app.R
import mega.privacy.android.app.activities.GiphyPickerActivity
import mega.privacy.android.app.activities.GiphyPickerActivity.Companion.GIF_DATA
import mega.privacy.android.app.objects.GifData
import mega.privacy.android.core.ui.controls.chat.attachpanel.AttachItem
import mega.privacy.android.core.ui.controls.chat.attachpanel.AttachItemPlaceHolder
import mega.privacy.android.core.ui.preview.CombinedThemePreviews
import mega.privacy.android.shared.theme.MegaAppTheme
import mega.privacy.mobile.analytics.event.ChatConversationContactMenuItemEvent
import mega.privacy.mobile.analytics.event.ChatConversationFileMenuItemEvent
import mega.privacy.mobile.analytics.event.ChatConversationGIFMenuItemEvent
import mega.privacy.mobile.analytics.event.ChatConversationGalleryMenuItemEvent
import mega.privacy.mobile.analytics.event.ChatConversationLocationMenuItemEvent
import mega.privacy.mobile.analytics.event.ChatConversationScanMenuItemEvent
import mega.privacy.mobile.analytics.event.ChatConversationTakePictureMenuItemEvent
import mega.privacy.mobile.analytics.event.ChatImageAttachmentItemSelected
import mega.privacy.mobile.analytics.event.ChatImageAttachmentItemSelectedEvent
import nz.mega.documentscanner.DocumentScannerActivity

/**
 * Chat toolbar bottom sheet
 *
 * @param modifier
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChatToolbarBottomSheet(
    onAttachFileClicked: () -> Unit,
    onAttachContactClicked: () -> Unit,
    onTakePicture: () -> Unit,
    onPickLocation: () -> Unit,
    onSendGiphyMessage: (GifData?) -> Unit,
    modifier: Modifier = Modifier,
    onCameraPermissionDenied: () -> Unit = {},
    sheetState: ModalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden),
    onAttachFiles: (List<Uri>) -> Unit = {},
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val galleryPicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia()
        ) {
            if (it.isNotEmpty()) {
                onAttachFiles(it)
            }
            coroutineScope.launch { sheetState.hide() }
        }

    val gifPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {
            onSendGiphyMessage(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.data?.getParcelableExtra(GIF_DATA, GifData::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    it.data?.getParcelableExtra(GIF_DATA)
                }
            )
            coroutineScope.launch { sheetState.hide() }
        }

    val scanDocumentLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {
            it.data?.data?.let { uri ->
                onAttachFiles(listOf(uri))
            }
            coroutineScope.launch { sheetState.hide() }
        }

    Column(modifier = modifier.fillMaxWidth()) {
        ChatGallery(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            sheetState = sheetState,
            onTakePicture = {
                Analytics.tracker.trackEvent(ChatConversationTakePictureMenuItemEvent)
                onTakePicture()
            },
            onFileGalleryItemClicked = {
                Analytics.tracker.trackEvent(
                    ChatImageAttachmentItemSelectedEvent(
                        ChatImageAttachmentItemSelected.SelectionType.SingleMode,
                        1
                    )
                )
                it.fileUri?.toUri()?.let { uri ->
                    onAttachFiles(listOf(uri))
                    coroutineScope.launch { sheetState.hide() }
                }
            },
            onCameraPermissionDenied = onCameraPermissionDenied,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AttachItem(
                iconId = R.drawable.ic_attach_from_gallery,
                itemName = stringResource(id = R.string.chat_attach_panel_gallery),
                onItemClick = {
                    Analytics.tracker.trackEvent(ChatConversationGalleryMenuItemEvent)
                    galleryPicker.launch(PickVisualMediaRequest())
                },
                modifier = Modifier.testTag(TEST_TAG_ATTACH_FROM_GALLERY)
            )
            AttachItem(
                iconId = R.drawable.ic_attach_from_file,
                itemName = pluralStringResource(id = R.plurals.general_num_files, count = 1),
                onItemClick = {
                    Analytics.tracker.trackEvent(ChatConversationFileMenuItemEvent)
                    onAttachFileClicked()
                },
                modifier = Modifier.testTag(TEST_TAG_ATTACH_FROM_FILE)
            )
            AttachItem(
                iconId = R.drawable.ic_attach_from_gif,
                itemName = stringResource(id = R.string.chat_room_toolbar_gif_option),
                onItemClick = {
                    Analytics.tracker.trackEvent(ChatConversationGIFMenuItemEvent)
                    openGifPicker(context, gifPickerLauncher)
                },
                modifier = Modifier.testTag(TEST_TAG_ATTACH_FROM_GIF)
            )
            AttachItem(
                iconId = R.drawable.ic_attach_from_scan,
                itemName = stringResource(id = R.string.chat_room_toolbar_scan_option),
                onItemClick = {
                    Analytics.tracker.trackEvent(ChatConversationScanMenuItemEvent)
                    openDocumentScanner(context, scanDocumentLauncher)
                },
                modifier = Modifier.testTag(TEST_TAG_ATTACH_FROM_SCAN)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AttachItem(
                iconId = R.drawable.ic_attach_from_location,
                itemName = stringResource(id = R.string.chat_room_toolbar_location_option),
                onItemClick = {
                    Analytics.tracker.trackEvent(ChatConversationLocationMenuItemEvent)
                    onPickLocation()
                },
                modifier = Modifier.testTag(TEST_TAG_ATTACH_FROM_LOCATION)
            )
            AttachItem(
                iconId = R.drawable.ic_attach_from_contact,
                itemName = stringResource(id = R.string.attachment_upload_panel_contact),
                onItemClick = {
                    Analytics.tracker.trackEvent(ChatConversationContactMenuItemEvent)
                    coroutineScope.launch { sheetState.hide() }
                    onAttachContactClicked()
                },
                modifier = Modifier.testTag(TEST_TAG_ATTACH_FROM_CONTACT)
            )
            AttachItemPlaceHolder()
            AttachItemPlaceHolder()
        }
    }
}

private fun openGifPicker(
    context: Context,
    pickGifLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
) {
    Intent(context, GiphyPickerActivity::class.java).also {
        pickGifLauncher.launch(it)
    }
}

private fun openDocumentScanner(
    context: Context,
    scanDocumentLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
) {
    DocumentScannerActivity.getIntent(context, arrayOf(context.getString(R.string.section_chat)))
        .also {
            scanDocumentLauncher.launch(it)
        }
}

@OptIn(ExperimentalMaterialApi::class)
@CombinedThemePreviews
@Composable
private fun ChatToolbarBottomSheetPreview() {
    MegaAppTheme(isDark = isSystemInDarkTheme()) {
        ChatToolbarBottomSheet(
            onAttachFileClicked = {},
            onAttachContactClicked = {},
            onPickLocation = {},
            onSendGiphyMessage = {},
            onTakePicture = {},
            sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Expanded)
        )
    }
}

internal const val TEST_TAG_GALLERY_LIST = "chat_gallery_list"
internal const val TEST_TAG_ATTACH_FROM_GALLERY = "chat_view:attach_panel:attach_from_gallery"
internal const val TEST_TAG_ATTACH_FROM_FILE = "chat_view:attach_panel:attach_from_file"
internal const val TEST_TAG_ATTACH_FROM_GIF = "chat_view:attach_panel:attach_from_gif"
internal const val TEST_TAG_ATTACH_FROM_SCAN = "chat_view:attach_panel:attach_from_scan"
internal const val TEST_TAG_ATTACH_FROM_LOCATION = "chat_view:attach_panel:attach_from_location"
internal const val TEST_TAG_ATTACH_FROM_CONTACT = "chat_view:attach_panel:attach_from_contact"
internal const val TEST_TAG_LOADING_GALLERY = "chat_view:attach_panel:loading_gallery"
internal const val TEST_TAG_ATTACH_GALLERY_ITEM = "chat_view:attach_panel:gallery_item"


