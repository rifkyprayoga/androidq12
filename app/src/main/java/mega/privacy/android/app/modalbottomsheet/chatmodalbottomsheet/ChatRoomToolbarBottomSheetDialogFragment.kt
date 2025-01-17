package mega.privacy.android.app.modalbottomsheet.chatmodalbottomsheet

import android.Manifest
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import mega.privacy.android.analytics.Analytics
import mega.privacy.android.app.R
import mega.privacy.android.app.arch.extensions.collectFlow
import mega.privacy.android.app.databinding.BottomSheetChatRoomToolbarBinding
import mega.privacy.android.app.interfaces.ChatRoomToolbarBottomSheetDialogActionListener
import mega.privacy.android.app.main.megachat.ChatActivity
import mega.privacy.android.app.main.megachat.chatAdapters.FileStorageChatAdapter
import mega.privacy.android.app.utils.Constants
import mega.privacy.android.app.utils.Util
import mega.privacy.android.app.utils.permission.PermissionUtils.getAudioPermissionByVersion
import mega.privacy.android.app.utils.permission.PermissionUtils.getImagePermissionByVersion
import mega.privacy.android.app.utils.permission.PermissionUtils.getReadExternalStoragePermission
import mega.privacy.android.app.utils.permission.PermissionUtils.getVideoPermissionByVersion
import mega.privacy.android.app.utils.permission.PermissionUtils.hasPermissions
import mega.privacy.android.domain.entity.chat.FileGalleryItem
import mega.privacy.mobile.analytics.event.ChatConversationContactMenuItemEvent
import mega.privacy.mobile.analytics.event.ChatConversationFileMenuItemEvent
import mega.privacy.mobile.analytics.event.ChatConversationGIFMenuItemEvent
import mega.privacy.mobile.analytics.event.ChatConversationLocationMenuItemEvent
import mega.privacy.mobile.analytics.event.ChatConversationScanMenuItemEvent
import mega.privacy.mobile.analytics.event.ChatConversationSendImageFilesFloatingActionButtonPressedEvent
import mega.privacy.mobile.analytics.event.ChatConversationTakePictureMenuItemEvent
import mega.privacy.mobile.analytics.event.ChatConversationVideoMenuItemEvent
import mega.privacy.mobile.analytics.event.ChatConversationVoiceClipMenuItemEvent
import mega.privacy.mobile.analytics.event.ChatConversationVoiceMenuItemEvent
import mega.privacy.mobile.analytics.event.ChatImageAttachmentItemSelected
import mega.privacy.mobile.analytics.event.ChatImageAttachmentItemSelectedEvent

/**
 * Bottom Sheet Dialog which shows the chat options
 */
@AndroidEntryPoint
class ChatRoomToolbarBottomSheetDialogFragment : BottomSheetDialogFragment() {

    val viewModel: ChatRoomToolbarViewModel by viewModels()

    private lateinit var binding: BottomSheetChatRoomToolbarBinding

    private lateinit var listener: ChatRoomToolbarBottomSheetDialogActionListener

    private var isMultiselectMode = false
    private var hasCameraPermission = false
    private var hasStoragePermission = false

    private val filesAdapter by lazy {
        FileStorageChatAdapter(
            ::onTakePictureClick,
            ::onClickItem,
            ::onLongClickItem,
            viewLifecycleOwner
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetFragmentWithTransparentBackground
        ).apply {
            setCanceledOnTouchOutside(true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        listener = requireActivity() as ChatRoomToolbarBottomSheetDialogActionListener
        binding = BottomSheetChatRoomToolbarBinding.inflate(layoutInflater, container, false)
        binding.textFile.text = resources.getQuantityString(R.plurals.general_num_files, 1)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog ?: return
        BottomSheetBehavior.from(dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)).state =
            BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupButtons()

        viewLifecycleOwner.collectFlow(viewModel.filesGallery) { filesList ->
            binding.emptyGallery.isVisible = filesList.isEmpty()
            binding.list.isVisible = filesList.isNotEmpty()
            filesAdapter.submitList(filesList)
        }

        viewLifecycleOwner.collectFlow(viewModel.showSendImagesButton) { visibility ->
            isMultiselectMode = visibility
            binding.sendFilesButton.isVisible = visibility
        }

        viewLifecycleOwner.collectFlow(viewModel.hasReadStoragePermissionsGranted) { isGranted ->
            hasStoragePermission = isGranted
            if (!hasStoragePermission) {
                viewModel.checkStoragePermission()
            }
        }

        viewLifecycleOwner.collectFlow(viewModel.hasCameraPermissionsGranted) { isGranted ->
            hasCameraPermission = isGranted
            if (!hasCameraPermission && hasStoragePermission) {
                viewModel.checkCameraPermission()
            }
        }

        viewLifecycleOwner.collectFlow(viewModel.checkReadStoragePermissions) { shouldCheck ->
            if (shouldCheck) {
                checkPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        viewLifecycleOwner.collectFlow(viewModel.checkCameraPermissions) { shouldCheck ->
            if (shouldCheck) {
                checkPermissions(Manifest.permission.CAMERA)
            }
        }

        setupView()
        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * Setup recycler view
     */
    private fun setupView() {
        binding.list.apply {
            clipToPadding = false
            setHasFixedSize(true)
            itemAnimator = Util.noChangeRecyclerViewItemAnimator()
            adapter = filesAdapter
        }
    }

    /**
     * Setup option buttons of the toolbar.
     */
    private fun setupButtons() {
        binding.sendFilesButton.setOnClickListener {
            Analytics.tracker.trackEvent(
                ChatConversationSendImageFilesFloatingActionButtonPressedEvent
            )
            val list = viewModel.getSelectedFiles()
            if (list.isNotEmpty()) {
                listener.onSendFilesSelected(list)
                dismiss()
            }
        }

        binding.optionVoiceClip.setOnClickListener {
            Analytics.tracker.trackEvent(ChatConversationVoiceClipMenuItemEvent)
            listener.onRecordVoiceClipClicked()
            dismiss()
        }

        binding.optionFile.setOnClickListener {
            Analytics.tracker.trackEvent(ChatConversationFileMenuItemEvent)
            listener.onSendFileOptionClicked()
            dismiss()
        }

        binding.optionVoice.setOnClickListener {
            Analytics.tracker.trackEvent(ChatConversationVoiceMenuItemEvent)
            listener.onStartCallOptionClicked(false)
            dismiss()
        }

        binding.optionVideo.setOnClickListener {
            Analytics.tracker.trackEvent(ChatConversationVideoMenuItemEvent)
            listener.onStartCallOptionClicked(true)
            dismiss()
        }

        binding.optionScan.setOnClickListener {
            Analytics.tracker.trackEvent(ChatConversationScanMenuItemEvent)
            listener.onScanDocumentOptionClicked()
            dismiss()
        }

        binding.optionGif.setOnClickListener {
            Analytics.tracker.trackEvent(ChatConversationGIFMenuItemEvent)
            listener.onSendGIFOptionClicked()
            dismiss()
        }

        binding.optionLocation.setOnClickListener {
            Analytics.tracker.trackEvent(ChatConversationLocationMenuItemEvent)
            listener.onSendLocationOptionClicked()
            dismiss()
        }

        binding.optionContact.setOnClickListener {
            Analytics.tracker.trackEvent(ChatConversationContactMenuItemEvent)
            listener.onSendContactOptionClicked()
            dismiss()
        }
    }

    /**
     * Check whether a permit needs to be applied for or whether it is already granted
     *
     * @param typePermission Type of permission: READ_EXTERNAL_STORAGE or CAMERA
     */
    private fun checkPermissions(typePermission: String) {
        val chatActivity = requireActivity() as ChatActivity
        val hasPermission =
            hasPermissions(chatActivity, typePermission)

        when (typePermission) {
            Manifest.permission.READ_EXTERNAL_STORAGE -> {
                if (hasPermission) {
                    viewModel.updatePermissionsGranted(typePermission, hasPermission)
                } else {
                    val permissions = arrayOf(
                        getImagePermissionByVersion(),
                        getAudioPermissionByVersion(),
                        getVideoPermissionByVersion(),
                        getReadExternalStoragePermission()
                    )
                    ActivityCompat.requestPermissions(
                        chatActivity,
                        permissions,
                        Constants.REQUEST_READ_STORAGE
                    )
                }
            }

            Manifest.permission.CAMERA -> {
                if (hasPermission) {
                    viewModel.updatePermissionsGranted(typePermission, hasPermission)
                } else {
                    ActivityCompat.requestPermissions(
                        chatActivity,
                        arrayOf(typePermission),
                        Constants.REQUEST_CAMERA_SHOW_PREVIEW
                    )
                }
            }
        }
    }

    private fun onTakePictureClick() {
        Analytics.tracker.trackEvent(ChatConversationTakePictureMenuItemEvent)
        if (hasCameraPermission) {
            listener.onTakePictureOptionClicked()
            dismiss()
        } else {
            viewModel.updateCheckCameraPermissions()
        }
    }

    private fun onClickItem(file: FileGalleryItem) {
        if (isMultiselectMode) {
            viewModel.longClickItem(file)
            Analytics.tracker.trackEvent(
                ChatImageAttachmentItemSelectedEvent(
                    selectionType = ChatImageAttachmentItemSelected.SelectionType.MultiSelectMode,
                    imageCount = viewModel.getSelectedFiles().size
                )
            )
        } else {
            val files = ArrayList<FileGalleryItem>()
            files.add(file)
            Analytics.tracker.trackEvent(
                ChatImageAttachmentItemSelectedEvent(
                    selectionType = ChatImageAttachmentItemSelected.SelectionType.SingleMode,
                    imageCount = files.size
                )
            )
            listener.onSendFilesSelected(files)
            dismiss()
        }
    }

    private fun onLongClickItem(file: FileGalleryItem) {
        viewModel.longClickItem(file)
        Analytics.tracker.trackEvent(
            ChatImageAttachmentItemSelectedEvent(
                selectionType = ChatImageAttachmentItemSelected.SelectionType.MultiSelectMode,
                imageCount = viewModel.getSelectedFiles().size
            )
        )
    }
}