package mega.privacy.android.domain.repository

import kotlinx.coroutines.flow.Flow
import mega.privacy.android.domain.entity.SortOrder
import mega.privacy.android.domain.entity.node.NodeId
import mega.privacy.android.domain.entity.node.TypedVideoNode
import mega.privacy.android.domain.entity.videosection.VideoPlaylist

/**
 * Repository related to video section
 */
interface VideoSectionRepository {
    /**
     * Get all videos
     *
     * @param order the list order
     * @return typed video node list
     */
    suspend fun getAllVideos(order: SortOrder): List<TypedVideoNode>

    /**
     * Get video playlists
     *
     * @return video playlist lists
     */
    suspend fun getVideoPlaylists(): List<VideoPlaylist>

    /**
     * Create a video playlist
     *
     * @param title video playlist title
     * @return created video playlist
     */
    suspend fun createVideoPlaylist(title: String): VideoPlaylist

    /**
     * Add videos to the playlist
     *
     * @param playlistID playlist id
     * @param videoIDs added video ids
     *
     * @return the number of added videos
     */
    suspend fun addVideosToPlaylist(playlistID: NodeId, videoIDs: List<NodeId>): Int

    /**
     * Remove video playlists
     *
     * @param playlistIDs removed playlist ids
     */
    suspend fun removeVideoPlaylists(playlistIDs: List<NodeId>): List<Long>

    /**
     * Remove videos from the playlist
     *
     * @param playlistID playlist id
     * @param videoElementIDs removed video element ids
     * @return the number of removed videos
     */
    suspend fun removeVideosFromPlaylist(playlistID: NodeId, videoElementIDs: List<Long>): Int

    /**
     * Update video playlist title
     *
     * @param playlistID playlist id
     * @param newTitle new title
     * @return updated title
     */
    suspend fun updateVideoPlaylistTitle(playlistID: NodeId, newTitle: String): String

    /**
     * Monitor video playlist sets update
     *
     * @return a flow of all new video playlist set ids update
     */
    fun monitorVideoPlaylistSetsUpdate(): Flow<List<Long>>
}