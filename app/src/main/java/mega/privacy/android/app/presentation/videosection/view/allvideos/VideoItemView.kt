package mega.privacy.android.app.presentation.videosection.view.allvideos

import mega.privacy.android.core.R as coreR
import mega.privacy.android.icon.pack.R as iconPackR
import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import mega.privacy.android.app.R
import mega.privacy.android.core.ui.controls.text.LongTextBehaviour
import mega.privacy.android.core.ui.controls.text.MegaText
import mega.privacy.android.core.ui.preview.CombinedThemePreviews
import mega.privacy.android.core.ui.theme.extensions.textColorSecondary
import mega.privacy.android.core.ui.theme.tokens.TextColor
import mega.privacy.android.shared.theme.MegaAppTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun VideoItemView(
    @DrawableRes icon: Int,
    name: String,
    fileSize: String?,
    duration: String?,
    isFavourite: Boolean,
    isSelected: Boolean,
    isSharedWithPublicLink: Boolean,
    labelColor: Color?,
    onClick: () -> Unit,
    thumbnailData: Any?,
    modifier: Modifier = Modifier,
    showMenuButton: Boolean = true,
    nodeAvailableOffline: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onMenuClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .fillMaxWidth()
            .height(87.dp)
            .testTag(VIDEO_ITEM_VIEW_TEST_TAG)
    ) {
        VideoThumbnailView(
            icon = icon,
            modifier = Modifier.fillMaxHeight(),
            thumbnailData = thumbnailData,
            duration = duration,
            isFavourite = isFavourite
        )

        VideoInfoView(
            modifier = Modifier.weight(1f),
            name = name,
            fileSize = fileSize,
            showMenuButton = showMenuButton,
            isSelected = isSelected,
            nodeAvailableOffline = nodeAvailableOffline,
            onMenuClick = onMenuClick,
            labelColor = labelColor,
            isSharedWithPublicLink = isSharedWithPublicLink,
        )

        if (showMenuButton) {
            Image(
                painter = painterResource(
                    id = if (isSelected)
                        R.drawable.ic_select_thumbnail
                    else
                        coreR.drawable.ic_dots_vertical_grey
                ),
                contentDescription = VIDEO_ITEM_MENU_ICON_CONTENT_DESCRIPTION,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .clickable {
                        if (!isSelected) {
                            onMenuClick()
                        }
                    }.testTag(VIDEO_ITEM_MENU_ICON_TEST_TAG),
                alignment = Alignment.CenterEnd,
            )
        }
    }
}

@Composable
internal fun VideoThumbnailView(
    @DrawableRes icon: Int,
    modifier: Modifier,
    thumbnailData: Any?,
    duration: String?,
    isFavourite: Boolean,
) {
    Box(modifier = modifier) {
        val thumbnailModifier = Modifier
            .width(126.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(5.dp))
            .background(colorResource(id = R.color.white_045_grey_045))

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(thumbnailData)
                .crossfade(true)
                .build(),
            contentDescription = VIDEO_ITEM_THUMBNAIL_CONTENT_DESCRIPTION,
            placeholder = painterResource(id = icon),
            error = painterResource(id = icon),
            contentScale = ContentScale.FillBounds,
            modifier = thumbnailModifier.testTag(VIDEO_ITEM_THUMBNAIL_TEST_TAG)
        )

        duration?.let {
            Text(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 5.dp, end = 5.dp)
                    .height(16.dp)
                    .testTag(VIDEO_ITEM_DURATION_VIEW_TEST_TAG),
                text = it,
                style = MaterialTheme.typography.caption,
                color = Color.White
            )
        }
        Image(
            painter = painterResource(id = R.drawable.ic_play_circle),
            contentDescription = VIDEO_ITEM_PLAY_ICON_CONTENT_DESCRIPTION,
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.Center)
                .testTag(VIDEO_ITEM_PLAY_ICON_TEST_TAG)
        )
        if (isFavourite) {
            Image(
                painter = painterResource(id = R.drawable.ic_favourite_white),
                contentDescription = VIDEO_ITEM_FAVOURITE_ICON_CONTENT_DESCRIPTION,
                modifier = Modifier
                    .padding(top = 5.dp, end = 5.dp)
                    .size(12.dp)
                    .align(Alignment.TopEnd)
                    .testTag(VIDEO_ITEM_FAVOURITE_ICON_TEST_TAG)
            )
        }
    }
}

@Composable
internal fun VideoInfoView(
    name: String,
    fileSize: String?,
    isSelected: Boolean,
    showMenuButton: Boolean,
    nodeAvailableOffline: Boolean,
    isSharedWithPublicLink: Boolean,
    labelColor: Color?,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        VideoNameAndLabelView(
            name = name,
            labelColor = labelColor
        )

        VideoSizeAndIconsView(
            fileSize = fileSize,
            isSelected = isSelected,
            showMenuButton = showMenuButton,
            nodeAvailableOffline = nodeAvailableOffline,
            isSharedWithPublicLink = isSharedWithPublicLink,
            onMenuClick = onMenuClick
        )
    }
}

@Composable
internal fun VideoNameAndLabelView(
    name: String,
    labelColor: Color?,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        MegaText(
            modifier = Modifier
                .padding(start = 10.dp)
                .weight(1f)
                .align(Alignment.CenterVertically)
                .testTag(VIDEO_ITEM_NAME_VIEW_TEST_TAG),
            text = name,
            textColor = TextColor.Primary,
            overflow = LongTextBehaviour.Ellipsis(2),
            style = MaterialTheme.typography.subtitle1
        )

        labelColor?.let {
            Box(
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .size(10.dp)
                    .background(shape = CircleShape, color = it)
                    .align(Alignment.CenterVertically)
                    .testTag(VIDEO_ITEM_LABEL_VIEW_TEST_TAG),
                contentAlignment = Alignment.CenterEnd
            ) {}
        }
    }
}

@Composable
internal fun VideoSizeAndIconsView(
    fileSize: String?,
    isSelected: Boolean,
    showMenuButton: Boolean,
    nodeAvailableOffline: Boolean,
    isSharedWithPublicLink: Boolean,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.padding(start = 10.dp, top = 10.dp)) {
        fileSize?.let {
            MegaText(
                modifier = Modifier.testTag(VIDEO_ITEM_SIZE_VIEW_TEST_TAG),
                text = it,
                style = MaterialTheme.typography.caption,
                textColor = TextColor.Secondary,
            )
        }

        if (nodeAvailableOffline) {
            Image(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .size(16.dp)
                    .testTag(VIDEO_ITEM_OFFLINE_ICON_TEST_TAG),
                colorFilter = ColorFilter.tint(
                    MaterialTheme.colors.textColorSecondary
                ),
                painter = painterResource(id = iconPackR.drawable.ic_offline_available),
                contentDescription = VIDEO_ITEM_OFFLINE_ICON_CONTENT_DESCRIPTION
            )
        }

        if (isSharedWithPublicLink) {
            Image(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .size(16.dp)
                    .testTag(VIDEO_ITEM_LINK_ICON_TEST_TAG),
                painter = painterResource(id = iconPackR.drawable.ic_link),
                contentDescription = VIDEO_ITEM_LINK_ICON_CONTENT_DESCRIPTION,
                colorFilter = ColorFilter.tint(
                    MaterialTheme.colors.textColorSecondary
                )
            )
        }
    }
}


@CombinedThemePreviews
@Composable
private fun VideoItemViewWithFavouritePreview() {
    MegaAppTheme(isDark = isSystemInDarkTheme()) {
        VideoItemView(
            icon = iconPackR.drawable.ic_video_medium_solid,
            name = "testing_video_file_name_long_name_testing.mp4",
            fileSize = "1.3MB",
            duration = "04:00",
            isFavourite = true,
            onClick = {},
            thumbnailData = null,
            nodeAvailableOffline = true,
            isSelected = true,
            isSharedWithPublicLink = true,
            labelColor = Color.Red
        )
    }
}

@CombinedThemePreviews
@Composable
private fun VideoItemViewWithoutFavouritePreview() {
    MegaAppTheme(isDark = isSystemInDarkTheme()) {
        VideoItemView(
            icon = iconPackR.drawable.ic_video_medium_solid,
            name = "name.mp4",
            fileSize = "1.3MB",
            duration = "04:00",
            isFavourite = false,
            onClick = {},
            thumbnailData = null,
            isSelected = false,
            isSharedWithPublicLink = false,
            labelColor = Color.Red
        )
    }
}

@CombinedThemePreviews
@Composable
private fun VideoInfoViewPreview() {
    MegaAppTheme(isDark = isSystemInDarkTheme()) {
        VideoInfoView(
            name = "testing_video_file_name_long_name_testing_testing_video_file_name_long_name_testing_testing_video_file_name_long_name_testing.mp4",
            fileSize = "1.3MB",
            isSelected = true,
            showMenuButton = true,
            nodeAvailableOffline = true,
            isSharedWithPublicLink = true,
            labelColor = Color.Red,
            onMenuClick = {},
            modifier = Modifier.height(87.dp)
        )
    }
}

/**
 * Test tag for the video item view
 */
const val VIDEO_ITEM_VIEW_TEST_TAG = "video_item:row_item"

/**
 * Test tag for the video item label view
 */
const val VIDEO_ITEM_LABEL_VIEW_TEST_TAG = "video_item:box_label"

/**
 * Test tag for the video item duration view
 */
const val VIDEO_ITEM_DURATION_VIEW_TEST_TAG = "video_item:text_duration"

/**
 * Test tag for the video item name view
 */
const val VIDEO_ITEM_NAME_VIEW_TEST_TAG = "video_item:text_name"

/**
 * Test tag for the video item size view
 */
const val VIDEO_ITEM_SIZE_VIEW_TEST_TAG = "video_item:text_size"

/**
 * Content description for the video item thumbnail
 */
const val VIDEO_ITEM_THUMBNAIL_CONTENT_DESCRIPTION = "Video thumbnail"

/**
 * Test tag for the video item thumbnail view
 */
const val VIDEO_ITEM_THUMBNAIL_TEST_TAG = "video_item:image_thumbnail"

/**
 * Content description for the video item menu icon
 */
const val VIDEO_ITEM_MENU_ICON_CONTENT_DESCRIPTION = "menu icon"

/**
 * Test tag for the video item menu icon view
 */
const val VIDEO_ITEM_MENU_ICON_TEST_TAG = "video_item:image_menu"

/**
 * Content description for the video item play icon
 */
const val VIDEO_ITEM_PLAY_ICON_CONTENT_DESCRIPTION = "play icon"

/**
 * Test tag for the video item play icon view
 */
const val VIDEO_ITEM_PLAY_ICON_TEST_TAG = "video_item:image_play"

/**
 * Content description for the video item favourite icon
 */
const val VIDEO_ITEM_FAVOURITE_ICON_CONTENT_DESCRIPTION = "favourite icon"

/**
 * Test tag for the video item favourite icon view
 */
const val VIDEO_ITEM_FAVOURITE_ICON_TEST_TAG = "video_item:image_favourite"

/**
 * Content description for the video item offline icon
 */
const val VIDEO_ITEM_OFFLINE_ICON_CONTENT_DESCRIPTION = "Available Offline"

/**
 * Test tag for the video item offline icon view
 */
const val VIDEO_ITEM_OFFLINE_ICON_TEST_TAG = "video_item:image_offline"

/**
 * Content description for the video item link icon
 */
const val VIDEO_ITEM_LINK_ICON_CONTENT_DESCRIPTION = "Link icon"

/**
 * Test tag for the video item link icon view
 */
const val VIDEO_ITEM_LINK_ICON_TEST_TAG = "video_item:image_link"