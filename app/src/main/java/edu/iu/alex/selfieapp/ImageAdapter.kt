package edu.iu.alex.selfieapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import edu.iu.alex.selfieapp.R

class ImageAdapter(private val context: Context, private var imageUrls: List<String> = listOf()) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {


    fun setImageUrls(newUrls: List<String>) {
        imageUrls = newUrls
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        Glide.with(context)
            .load(imageUrl)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            val dialogFragment = ImageDialogFragment.newInstance(imageUrl)
            dialogFragment.show((context as AppCompatActivity).supportFragmentManager, "fullImage")
        }
    }


    override fun getItemCount() = imageUrls.size

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_view)
    }
}
