package edu.iu.alex.selfieapp

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide

class ImageDialogFragment : DialogFragment() {

    private var imageUrl: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.full_image_dialog_fragment, null)
        val imageView: ImageView = view.findViewById(R.id.fullImageView)

        imageUrl = arguments?.getString("imageUrl")
        Glide.with(this)
            .load(imageUrl)
            .into(imageView)

        Log.d("FullImageDialogFragment", "Image URL: $imageUrl")


        return AlertDialog.Builder(requireActivity())
            .setView(view)
            .create()
    }

    companion object {
        fun newInstance(imageUrl: String): ImageDialogFragment {
            val args = Bundle()
            args.putString("imageUrl", imageUrl)
            val fragment = ImageDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }
}