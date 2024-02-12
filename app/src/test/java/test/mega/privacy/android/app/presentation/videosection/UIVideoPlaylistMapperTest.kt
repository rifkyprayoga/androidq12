package test.mega.privacy.android.app.presentation.videosection

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import mega.privacy.android.app.presentation.time.mapper.DurationInSecondsTextMapper
import mega.privacy.android.app.presentation.videosection.mapper.UIVideoMapper
import mega.privacy.android.app.presentation.videosection.mapper.UIVideoPlaylistMapper
import mega.privacy.android.app.presentation.videosection.model.UIVideoPlaylist
import mega.privacy.android.domain.entity.node.NodeId
import mega.privacy.android.domain.entity.node.TypedVideoNode
import mega.privacy.android.domain.entity.videosection.VideoPlaylist
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.time.Duration.Companion.seconds

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UIVideoPlaylistMapperTest {
    private lateinit var underTest: UIVideoPlaylistMapper

    private val durationInSecondsTextMapper = DurationInSecondsTextMapper()
    private val uiVideoMapper = mock<UIVideoMapper>()

    private val id = NodeId(123456L)
    private val title = "title name"
    private val cover: Long? = null
    private val creationTime = 100L
    private val modificationTime = 200L
    private val thumbnailList = listOf(
        "thumbnail1",
        "thumbnail2",
        "thumbnail3",
        "thumbnail4",
    )
    private val numberOfVideos = 4
    private val totalDurationString = "1:40"
    private val videos = listOf<TypedVideoNode>(mock(), mock(), mock(), mock())

    @BeforeAll
    fun setUp() {
        underTest = UIVideoPlaylistMapper(
            durationInSecondsTextMapper,
            uiVideoMapper
        )
    }

    @Test
    fun `test that UIVideoPlaylist can be mapped correctly`() = runTest {
        whenever(uiVideoMapper(anyOrNull())).thenReturn(mock())
        assertMappedUIVideoPlaylistObject(underTest(initVideoPlaylist()))
    }

    private fun initVideoPlaylist() = mock<VideoPlaylist> {
        on { id }.thenReturn(id)
        on { title }.thenReturn(title)
        on { cover }.thenReturn(cover)
        on { creationTime }.thenReturn(creationTime)
        on { modificationTime }.thenReturn(modificationTime)
        on { thumbnailList }.thenReturn(thumbnailList)
        on { numberOfVideos }.thenReturn(numberOfVideos)
        on { totalDuration }.thenReturn(100.seconds)
        on { videos }.thenReturn(videos)
    }

    private fun assertMappedUIVideoPlaylistObject(uiVideoPlaylist: UIVideoPlaylist) {
        uiVideoPlaylist.let {
            assertAll(
                "Grouped Assertions of ${UIVideoPlaylist::class.simpleName}",
                { assertThat(it.id).isEqualTo(id) },
                { assertThat(it.title).isEqualTo(title) },
                { assertThat(it.cover).isEqualTo(cover) },
                { assertThat(it.creationTime).isEqualTo(creationTime) },
                { assertThat(it.modificationTime).isEqualTo(modificationTime) },
                { assertThat(it.thumbnailList?.size).isEqualTo(thumbnailList.size) },
                { assertThat(it.thumbnailList?.get(0)?.path).isEqualTo(thumbnailList[0]) },
                { assertThat(it.numberOfVideos).isEqualTo(numberOfVideos) },
                { assertThat(it.totalDuration).isEqualTo(totalDurationString) },
                { assertThat(it.videos?.size).isEqualTo(videos.size) }
            )
        }
    }
}