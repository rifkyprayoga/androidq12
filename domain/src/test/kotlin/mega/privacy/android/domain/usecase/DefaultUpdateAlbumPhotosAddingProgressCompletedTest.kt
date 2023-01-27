package mega.privacy.android.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import mega.privacy.android.domain.entity.photos.AlbumId
import mega.privacy.android.domain.repository.AlbumRepository
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.fail

@ExperimentalCoroutinesApi
class DefaultUpdateAlbumPhotosAddingProgressCompletedTest {
    private lateinit var underTest: UpdateAlbumPhotosAddingProgressCompleted

    private val albumRepository = mock<AlbumRepository>()

    @Before
    fun setUp() {
        underTest = DefaultUpdateAlbumPhotosAddingProgressCompleted(
            albumRepository = albumRepository,
        )
    }

    @Test
    fun `test that use case returns correct result`() = runTest {
        val albumId = AlbumId(1L)
        whenever(albumRepository.updateAlbumPhotosAddingProgressCompleted(albumId)).thenReturn(Unit)

        try {
            underTest(albumId)
        } catch (e: Exception) {
            fail(message = "${e.message}")
        }
    }
}
