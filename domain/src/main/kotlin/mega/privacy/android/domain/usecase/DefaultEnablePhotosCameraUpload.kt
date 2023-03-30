package mega.privacy.android.domain.usecase

import mega.privacy.android.domain.entity.VideoQuality
import mega.privacy.android.domain.repository.CameraUploadRepository
import mega.privacy.android.domain.repository.SettingsRepository
import mega.privacy.android.domain.usecase.camerauploads.SetCameraUploadsByWifiUseCase
import mega.privacy.android.domain.usecase.camerauploads.SetChargingRequiredForVideoCompressionUseCase
import mega.privacy.android.domain.usecase.camerauploads.SetUploadVideoQualityUseCase
import mega.privacy.android.domain.usecase.camerauploads.SetVideoCompressionSizeLimitUseCase
import javax.inject.Inject

/**
 * Default implementation of [EnablePhotosCameraUpload]
 *
 * @property settingsRepository [SettingsRepository]
 * @property setCameraUploadsByWifiUseCase [SetCameraUploadsByWifiUseCase]
 * @property setChargingRequiredForVideoCompressionUseCase [SetChargingRequiredForVideoCompressionUseCase]
 * @property setUploadVideoQualityUseCase [SetUploadVideoQualityUseCase]
 * @property setVideoCompressionSizeLimitUseCase [SetVideoCompressionSizeLimitUseCase]
 * @property cameraUploadRepository [CameraUploadRepository]
 */
class DefaultEnablePhotosCameraUpload @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val setCameraUploadsByWifiUseCase: SetCameraUploadsByWifiUseCase,
    private val setChargingRequiredForVideoCompressionUseCase: SetChargingRequiredForVideoCompressionUseCase,
    private val setUploadVideoQualityUseCase: SetUploadVideoQualityUseCase,
    private val setVideoCompressionSizeLimitUseCase: SetVideoCompressionSizeLimitUseCase,
    private val cameraUploadRepository: CameraUploadRepository,
) : EnablePhotosCameraUpload {

    override suspend fun invoke(
        path: String?,
        syncVideo: Boolean,
        enableCellularSync: Boolean,
        videoQuality: VideoQuality,
        conversionChargingOnSize: Int,
    ) {
        with(settingsRepository) {
            setCameraUploadLocalPath(path)
            setCameraUploadsByWifiUseCase(!enableCellularSync)
            setCameraUploadFileType(syncVideo)
            setCameraFolderExternalSDCard(false)
            setUploadVideoQualityUseCase(videoQuality)
            setChargingRequiredForVideoCompressionUseCase(true)
            setVideoCompressionSizeLimitUseCase(conversionChargingOnSize)
            // After target and local folder setup, then enable CU.
            setEnableCameraUpload(true)
        }
        cameraUploadRepository.listenToNewMedia()
    }
}
