package mega.privacy.android.app.contacts.requests.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.facebook.imagepipeline.request.ImageRequest
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import mega.privacy.android.app.contacts.requests.ContactRequestsViewModel
import mega.privacy.android.app.databinding.BottomSheetContactRequestBinding
import mega.privacy.android.app.utils.ExtraUtils.extraNotNull

@AndroidEntryPoint
class ContactRequestBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        private const val REQUEST_HANDLE = "REQUEST_HANDLE"

        fun newInstance(requestHandle: Long): ContactRequestBottomSheetDialogFragment =
            ContactRequestBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(REQUEST_HANDLE, requestHandle)
                }
            }
    }

    private val viewModel by viewModels<ContactRequestsViewModel>({ requireParentFragment() })
    private val requestHandle by extraNotNull<Long>(REQUEST_HANDLE)

    private lateinit var binding: BottomSheetContactRequestBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetContactRequestBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getContactRequest(requestHandle).observe(viewLifecycleOwner) { item ->
            requireNotNull(item)

            val isOutgoing = item.isOutgoing || item.name.isNullOrBlank()
            binding.txtTitle.text = if (isOutgoing) item.email else item.name
            binding.txtSubtitle.text = if (isOutgoing) item.createdTime else item.email
            binding.imgThumbnail.hierarchy.setPlaceholderImage(item.getPlaceholderDrawable(resources))
            binding.imgThumbnail.setImageRequest(ImageRequest.fromUri(item.imageUri))

            binding.btnAccept.setOnClickListener {
                viewModel.acceptRequest(requestHandle)
                dismiss()
            }
            binding.btnIgnore.setOnClickListener {
                viewModel.ignoreRequest(requestHandle)
                dismiss()
            }
            binding.btnDecline.setOnClickListener {
                viewModel.declineRequest(requestHandle)
                dismiss()
            }
        }
    }
}
