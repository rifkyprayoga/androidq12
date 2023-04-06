package mega.privacy.android.domain.entity.camerauploads

/**
 * To hold Camera UploadState
 * @param primaryPendingUploads
 * @param secondaryPendingUploads
 * @param lastPrimaryTimeStamp
 * @param lastSecondaryTimeStamp
 * @param lastPrimaryHandle
 * @param lastSecondaryHandle
 * @param primaryTotalUploadedBytes
 * @param secondaryTotalUploadedBytes
 * @param totalToUpload
 * @param totalUploaded
 */
data class CameraUploadsState(
    var primaryPendingUploads: Int = 0,
    var secondaryPendingUploads: Int = 0,
    var lastPrimaryTimeStamp: Long = -1,
    var lastSecondaryTimeStamp: Long = -1,
    var lastPrimaryHandle: Long = -1,
    var lastSecondaryHandle: Long = -1,
    var primaryTotalUploadedBytes: Long = 0,
    val secondaryTotalUploadedBytes: Long = 0,
    var totalToUpload: Int = 0,
    var totalUploaded: Int = 0,
)
