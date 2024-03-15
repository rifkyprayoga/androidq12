package mega.privacy.android.app.di.cameraupload

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import mega.privacy.android.app.di.GetNodeModule
import mega.privacy.android.domain.repository.CameraUploadRepository
import mega.privacy.android.domain.repository.FileSystemRepository
import mega.privacy.android.domain.repository.NodeRepository
import mega.privacy.android.domain.usecase.ClearCacheDirectory
import mega.privacy.android.domain.usecase.CreateCameraUploadFolder
import mega.privacy.android.domain.usecase.DefaultSetSecondarySyncHandle
import mega.privacy.android.domain.usecase.IsNodeInRubbish
import mega.privacy.android.domain.usecase.SetSecondarySyncHandle

/**
 * Provides the use case implementation for camera upload
 */
@Module(includes = [GetNodeModule::class])
@InstallIn(SingletonComponent::class, ViewModelComponent::class, ServiceComponent::class)
abstract class CameraUploadUseCases {

    companion object {
        /**
         * Provide the [IsNodeInRubbish] implementation
         */
        @Provides
        fun provideIsNodeInRubbish(nodeRepository: NodeRepository): IsNodeInRubbish =
            IsNodeInRubbish(nodeRepository::isNodeInRubbish)

        /**
         * Provide the [ClearCacheDirectory] implementation
         */
        @Provides
        fun provideClearCacheDirectory(cameraUploadRepository: CameraUploadRepository): ClearCacheDirectory =
            ClearCacheDirectory(cameraUploadRepository::clearCacheDirectory)

        /**
         * Provide the [CreateCameraUploadFolder] implementation
         */
        @Provides
        fun provideCreateCameraUploadFolder(fileSystemRepository: FileSystemRepository): CreateCameraUploadFolder =
            CreateCameraUploadFolder(fileSystemRepository::createFolder)
    }

    /**
     * Provide the [SetSecondarySyncHandle] implementation
     */
    @Binds
    abstract fun bindSetSecondarySyncHandle(setSecondarySyncHandle: DefaultSetSecondarySyncHandle): SetSecondarySyncHandle
}
