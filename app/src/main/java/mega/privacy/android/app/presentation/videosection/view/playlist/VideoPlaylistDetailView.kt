package mega.privacy.android.app.presentation.videosection.view.playlist

import mega.privacy.android.icon.pack.R as iconPackR
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mega.privacy.android.app.R
import mega.privacy.android.app.presentation.videosection.model.VideoPlaylistUIEntity
import mega.privacy.android.app.presentation.videosection.model.VideoUIEntity
import mega.privacy.android.app.presentation.videosection.view.allvideos.VideoItemView
import mega.privacy.android.app.utils.MegaNodeUtil
import mega.privacy.android.core.formatter.formatFileSize
import mega.privacy.android.core.ui.controls.dividers.DividerType
import mega.privacy.android.core.ui.controls.dividers.MegaDivider
import mega.privacy.android.core.ui.controls.text.LongTextBehaviour
import mega.privacy.android.core.ui.controls.text.MegaText
import mega.privacy.android.core.ui.preview.CombinedThemePreviews
import mega.privacy.android.core.ui.theme.black
import mega.privacy.android.core.ui.theme.extensions.grey_050_grey_800
import mega.privacy.android.core.ui.theme.tokens.TextColor
import mega.privacy.android.core.ui.theme.white
import mega.privacy.android.domain.entity.node.NodeId
import mega.privacy.android.domain.entity.node.thumbnail.ThumbnailRequest
import mega.privacy.android.legacy.core.ui.controls.LegacyMegaEmptyView
import mega.privacy.android.shared.theme.MegaAppTheme
import nz.mega.sdk.MegaNode

/**
 * Video playlist detail view
 */
@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun VideoPlaylistDetailView(
    playlist: VideoPlaylistUIEntity?,
    isInputTitleValid: Boolean,
    shouldDeleteVideoPlaylistDialog: Boolean,
    shouldRenameVideoPlaylistDialog: Boolean,
    shouldDeleteVideosDialog: Boolean,
    shouldShowVideoPlaylistBottomSheetDetails: Boolean,
    numberOfAddedVideos: Int,
    numberOfRemovedItems: Int,
    addedMessageShown: () -> Unit,
    removedMessageShown: () -> Unit,
    setShouldDeleteVideoPlaylistDialog: (Boolean) -> Unit,
    setShouldRenameVideoPlaylistDialog: (Boolean) -> Unit,
    setShouldShowVideoPlaylistBottomSheetDetails: (Boolean) -> Unit,
    setShouldDeleteVideosDialog: (Boolean) -> Unit,
    inputPlaceHolderText: String,
    setInputValidity: (Boolean) -> Unit,
    onRenameDialogPositiveButtonClicked: (playlistID: NodeId, newTitle: String) -> Unit,
    onDeleteDialogPositiveButtonClicked: (List<VideoPlaylistUIEntity>) -> Unit,
    onDeleteVideosDialogPositiveButtonClicked: (VideoPlaylistUIEntity) -> Unit,
    onAddElementsClicked: () -> Unit,
    onPlayAllClicked: () -> Unit,
    onUpdatedTitle: (String?) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: Int? = null,
    onClick: (item: VideoUIEntity, index: Int) -> Unit = { _, _ -> },
    onMenuClick: (VideoUIEntity) -> Unit = { _ -> },
    onLongClick: ((item: VideoUIEntity, index: Int) -> Unit) = { _, _ -> },
) {
    val items = playlist?.videos ?: emptyList()
    val lazyListState = rememberLazyListState()

    val isInFirstItem by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex != 0
        }
    }

    LaunchedEffect(isInFirstItem) {
        onUpdatedTitle(if (isInFirstItem) playlist?.title else null)
    }

    val snackBarHostState = remember { SnackbarHostState() }
    val isLight = MaterialTheme.colors.isLight

    val coroutineScope = rememberCoroutineScope()
    val modalSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = false,
        confirmValueChange = { value ->
            if (value == ModalBottomSheetValue.Hidden) {
                setShouldShowVideoPlaylistBottomSheetDetails(false)
            }
            true
        }
    )
    val scrollNotInProgress by remember {
        derivedStateOf { !lazyListState.isScrollInProgress }
    }

    LaunchedEffect(shouldShowVideoPlaylistBottomSheetDetails) {
        if (shouldShowVideoPlaylistBottomSheetDetails) {
            modalSheetState.show()
        }
    }

    LaunchedEffect(numberOfAddedVideos) {
        if (numberOfAddedVideos > 0) {
            val message = if (numberOfAddedVideos == 1) {
                "1 item"
            } else {
                "$numberOfAddedVideos items"
            }
            snackBarHostState.showSnackbar("Added $message to \'${playlist?.title}\'")
            addedMessageShown()
        }
    }

    LaunchedEffect(numberOfRemovedItems) {
        if (numberOfRemovedItems > 0) {
            val message = if (numberOfRemovedItems == 1) {
                "1 item"
            } else {
                "$numberOfRemovedItems items"
            }
            snackBarHostState.showSnackbar("Removed $message from \'${playlist?.title}\'")
            removedMessageShown()
        }
    }

    BackHandler(enabled = modalSheetState.isVisible) {
        coroutineScope.launch {
            modalSheetState.hide()
        }
    }

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        scaffoldState = rememberScaffoldState(),
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        backgroundColor = black.takeIf { isLight } ?: white,
                    )
                }
            )
        },
        floatingActionButton = {
            CreateVideoPlaylistFabButton(
                showFabButton = scrollNotInProgress,
                onCreateVideoPlaylistClick = onAddElementsClicked
            )
        }
    ) { paddingValue ->
        playlist?.let {
            if (shouldRenameVideoPlaylistDialog) {
                CreateVideoPlaylistDialog(
                    modifier = Modifier.testTag(DETAIL_RENAME_VIDEO_PLAYLIST_DIALOG_TEST_TAG),
                    title = "Rename",
                    positiveButtonText = "Rename",
                    inputPlaceHolderText = { inputPlaceHolderText },
                    errorMessage = errorMessage,
                    onDialogInputChange = setInputValidity,
                    onDismissRequest = {
                        setShouldRenameVideoPlaylistDialog(false)
                        setInputValidity(true)
                        coroutineScope.launch { modalSheetState.hide() }
                    },
                    initialInputText = { playlist.title },
                    onDialogPositiveButtonClicked = { newTitle ->
                        onRenameDialogPositiveButtonClicked(playlist.id, newTitle)
                    },
                ) {
                    isInputTitleValid
                }
            }

            if (shouldDeleteVideoPlaylistDialog) {
                DeleteItemsDialog(
                    modifier = Modifier.testTag(DETAIL_DELETE_VIDEO_PLAYLIST_DIALOG_TEST_TAG),
                    title = "Delete playlist?",
                    text = "Do we need additional explanation to delete playlists?",
                    confirmButtonText = "Delete",
                    onDeleteButtonClicked = {
                        onDeleteDialogPositiveButtonClicked(listOf(playlist))
                    },
                    onDismiss = {
                        setShouldDeleteVideoPlaylistDialog(false)
                        coroutineScope.launch { modalSheetState.hide() }
                    }
                )
            }

            if (shouldDeleteVideosDialog) {
                DeleteItemsDialog(
                    modifier = Modifier.testTag(DETAIL_DELETE_VIDEOS_DIALOG_TEST_TAG),
                    title = "Remove from playlist?",
                    text = null,
                    confirmButtonText = "Remove",
                    onDeleteButtonClicked = {
                        onDeleteVideosDialogPositiveButtonClicked(playlist)
                    },
                    onDismiss = {
                        setShouldDeleteVideosDialog(false)
                        coroutineScope.launch { modalSheetState.hide() }
                    }
                )
            }
        }

        when {
            items.isEmpty() -> VideoPlaylistEmptyView(
                thumbnailList = playlist?.thumbnailList,
                title = playlist?.title,
                totalDuration = playlist?.totalDuration,
                numberOfVideos = playlist?.numberOfVideos,
                onPlayAllClicked = {},
                modifier = Modifier.testTag(
                    VIDEO_PLAYLIST_DETAIL_EMPTY_VIEW_TEST_TAG
                )
            )

            else -> {
                LazyColumn(state = lazyListState, modifier = modifier.padding(paddingValue)) {
                    item(
                        key = "header"
                    ) {
                        Column {
                            VideoPlaylistHeaderView(
                                thumbnailList = playlist?.thumbnailList,
                                title = playlist?.title,
                                totalDuration = playlist?.totalDuration,
                                numberOfVideos = playlist?.numberOfVideos,
                                modifier = Modifier.padding(16.dp),
                                onPlayAllClicked = onPlayAllClicked
                            )
                            MegaDivider(
                                dividerType = DividerType.Centered,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }
                    items(count = items.size, key = { items[it].id.longValue }) {
                        when {
                            else -> {
                                val videoItem = items[it]
                                VideoItemView(
                                    icon = iconPackR.drawable.ic_video_medium_solid,
                                    name = videoItem.name,
                                    fileSize = formatFileSize(videoItem.size, LocalContext.current),
                                    duration = videoItem.durationString,
                                    isFavourite = videoItem.isFavourite,
                                    isSelected = videoItem.isSelected,
                                    thumbnailData = if (videoItem.thumbnail?.exists() == true) {
                                        videoItem.thumbnail
                                    } else {
                                        ThumbnailRequest(videoItem.id)
                                    },
                                    isSharedWithPublicLink = videoItem.isSharedItems,
                                    labelColor = if (videoItem.label != MegaNode.NODE_LBL_UNKNOWN)
                                        colorResource(
                                            id = MegaNodeUtil.getNodeLabelColor(
                                                videoItem.label
                                            )
                                        ) else null,
                                    nodeAvailableOffline = videoItem.nodeAvailableOffline,
                                    onClick = { onClick(videoItem, it) },
                                    onMenuClick = { onMenuClick(videoItem) },
                                    onLongClick = { onLongClick(videoItem, it) }
                                )
                            }
                        }
                    }
                }
            }
        }

        VideoPlaylistBottomSheet(
            modalSheetState = modalSheetState,
            coroutineScope = coroutineScope,
            onRenameVideoPlaylistClicked = {
                setShouldRenameVideoPlaylistDialog(true)
            },
            onDeleteVideoPlaylistClicked = {
                setShouldDeleteVideoPlaylistDialog(true)
            }
        )
    }
}

@Composable
internal fun VideoPlaylistEmptyView(
    thumbnailList: List<Any?>?,
    title: String?,
    totalDuration: String?,
    numberOfVideos: Int?,
    onPlayAllClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        VideoPlaylistHeaderView(
            thumbnailList = thumbnailList,
            title = title,
            totalDuration = totalDuration,
            numberOfVideos = numberOfVideos,
            modifier = Modifier.padding(16.dp),
            onPlayAllClicked = onPlayAllClicked
        )
        MegaDivider(
            dividerType = DividerType.Centered,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LegacyMegaEmptyView(
            modifier = Modifier
                .fillMaxSize(),
            text = stringResource(id = R.string.homepage_empty_hint_video),
            imagePainter = painterResource(id = R.drawable.ic_homepage_empty_video)
        )
    }
}

@Composable
internal fun VideoPlaylistHeaderView(
    thumbnailList: List<Any?>?,
    title: String?,
    totalDuration: String?,
    numberOfVideos: Int?,
    onPlayAllClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val thumbnailModifier = Modifier
                .width(126.dp)
                .aspectRatio(1.6f)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colors.grey_050_grey_800)

            ThumbnailListView(
                icon = R.drawable.ic_playlist_item_empty,
                modifier = thumbnailModifier,
                thumbnailList = thumbnailList
            )

            VideoPlaylistInfoView(
                title = title ?: "",
                totalDuration = totalDuration ?: "00:00:00",
                numberOfVideos = numberOfVideos ?: 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )
        }
        PlayAllButtonView(
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 16.dp)
                .clickable { onPlayAllClicked() }
                .testTag(DETAIL_PLAY_ALL_BUTTON_TEST_TAG)
        )
    }
}

@Composable
internal fun VideoPlaylistInfoView(
    title: String,
    totalDuration: String,
    numberOfVideos: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(start = 16.dp)
    ) {
        MegaText(
            modifier = modifier
                .fillMaxSize()
                .weight(1.5f)
                .testTag(PLAYLIST_TITLE_TEST_TAG),
            text = title,
            textColor = TextColor.Primary,
            style = MaterialTheme.typography.subtitle1,
            overflow = LongTextBehaviour.Clip(2)
        )

        MegaText(
            modifier = modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag(PLAYLIST_TOTAL_DURATION_TEST_TAG),
            text = totalDuration,
            textColor = TextColor.Secondary,
            style = MaterialTheme.typography.caption
        )

        MegaText(
            modifier = modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag(PLAYLIST_NUMBER_OF_VIDEOS_TEST_TAG),
            text = if (numberOfVideos != 0) {
                if (numberOfVideos == 1) {
                    "1 Video"
                } else {
                    "$numberOfVideos Videos"
                }
            } else {
                "no videos"
            },
            textColor = TextColor.Secondary,
            style = MaterialTheme.typography.caption
        )
    }
}

@Composable
internal fun PlayAllButtonView(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(36.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.secondary,
                shape = RoundedCornerShape(5.dp)
            )
    ) {
        Image(
            painter = painterResource(id = iconPackR.drawable.ic_playlist_play_all),
            contentDescription = "play all",
            modifier = Modifier
                .padding(start = 20.dp, end = 5.dp)
                .size(12.dp)
                .align(Alignment.CenterVertically)
        )

        MegaText(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = 20.dp),
            text = "Play all",
            textColor = TextColor.Accent,
            style = MaterialTheme.typography.caption
        )
    }
}

@CombinedThemePreviews
@Composable
private fun VideoPlaylistDetailViewPreview() {
    MegaAppTheme(isDark = isSystemInDarkTheme()) {
        VideoPlaylistDetailView(
            playlist = null,
            isInputTitleValid = true,
            shouldDeleteVideoPlaylistDialog = false,
            shouldRenameVideoPlaylistDialog = false,
            setShouldDeleteVideoPlaylistDialog = {},
            setShouldRenameVideoPlaylistDialog = {},
            inputPlaceHolderText = "",
            setInputValidity = {},
            onRenameDialogPositiveButtonClicked = { _, _ -> },
            onDeleteDialogPositiveButtonClicked = {},
            onAddElementsClicked = {},
            shouldShowVideoPlaylistBottomSheetDetails = false,
            setShouldShowVideoPlaylistBottomSheetDetails = {},
            addedMessageShown = {},
            numberOfAddedVideos = 0,
            shouldDeleteVideosDialog = false,
            setShouldDeleteVideosDialog = {},
            onDeleteVideosDialogPositiveButtonClicked = {},
            removedMessageShown = {},
            numberOfRemovedItems = 0,
            onPlayAllClicked = {},
            onUpdatedTitle = {}
        )
    }
}

@CombinedThemePreviews
@Composable
private fun VideoPlaylistHeaderViewPreview() {
    MegaAppTheme(isDark = isSystemInDarkTheme()) {
        VideoPlaylistHeaderView(
            modifier = Modifier,
            thumbnailList = listOf(null),
            title = "New Playlist",
            totalDuration = "00:00:00",
            numberOfVideos = 0,
            onPlayAllClicked = {}
        )
    }
}

@CombinedThemePreviews
@Composable
private fun PlayAllButtonViewPreview() {
    MegaAppTheme(isDark = isSystemInDarkTheme()) {
        PlayAllButtonView()
    }
}

/**
 * Test tag for empty view
 */
const val VIDEO_PLAYLIST_DETAIL_EMPTY_VIEW_TEST_TAG = "video_playlist_detail_empty_view_test_tag"

/**
 * Test tag for playlist title
 */
const val PLAYLIST_TITLE_TEST_TAG = "playlist_title_test_tag"

/**
 * Test tag for playlist total duration
 */
const val PLAYLIST_TOTAL_DURATION_TEST_TAG = "playlist_total_duration_test_tag"

/**
 * Test tag for playlist number of videos
 */
const val PLAYLIST_NUMBER_OF_VIDEOS_TEST_TAG = "playlist_number_of_videos_test_tag"

/**
 * Test tag for RenameVideoPlaylistDialog in detail page
 */
const val DETAIL_RENAME_VIDEO_PLAYLIST_DIALOG_TEST_TAG =
    "detail_rename_video_playlist_dialog_test_tag"

/**
 * Test tag for delete video playlist in detail page
 */
const val DETAIL_DELETE_VIDEO_PLAYLIST_DIALOG_TEST_TAG =
    "detail_delete_video_playlist_dialog_test_tag"

/**
 * Test tag for delete videos dialog in detail page
 */
const val DETAIL_DELETE_VIDEOS_DIALOG_TEST_TAG = "detail_delete_videos_dialog_test_tag"

/**
 * Test tag for play all button in detail page
 */
const val DETAIL_PLAY_ALL_BUTTON_TEST_TAG = "detail_play_all_button_test_tag"


internal const val videoPlaylistDetailRoute = "videoSection/video_playlist/detail"