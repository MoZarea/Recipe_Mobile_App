package com.example.recipe_app.view.home

import android.app.AlertDialog
import android.os.Bundle
import android.support.annotation.NonNull
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import at.blogc.android.views.ExpandableTextView
import com.bumptech.glide.Glide
import com.example.recipe_app.R
import com.example.recipe_app.databinding.FragmentDetailsBinding
import com.example.recipe_app.utils.CurrentUser
import com.example.recipe_app.utils.GreenSnackBar
import com.example.recipe_app.utils.GreenSnackBar.showSnackBarLong
import com.example.recipe_app.viewModels.DetailsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailsFragment : Fragment()  {
    private var detailsFragmentBinding: FragmentDetailsBinding? = null
    lateinit var id :String
    var videoId : String = ""
    private lateinit var  youtubeVideo : YouTubePlayerView
    private val args : DetailsFragmentArgs by navArgs()
    private val detailsViewModel : DetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentDetailsBinding.bind(view)
        detailsFragmentBinding = binding


        binding.textView2.text = args.meal.strMeal
        binding.textView3.text = args.meal.strInstructions
        Glide.with(this).load(args.meal.strMealThumb).into(binding.imageView2)
        videoId = args.meal.strYoutube!!
        binding.mealArea.text = args.meal.strArea
        binding.mealCategory.text = args.meal.strCategory
        binding.favBoxV3.isChecked = args.meal.isFavourite

        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet, null)
        binding.btnBottomSheet.setOnClickListener {
            val dialog = BottomSheetDialog(requireContext())
            val btnDismissBottomSheet: Button = bottomSheetView.findViewById(R.id.btnDismiss)

            btnDismissBottomSheet.setOnClickListener {
                dialog.dismiss()
            }
            dialog.setCancelable(false)
            if (bottomSheetView.parent != null) {
                (bottomSheetView.parent as ViewGroup).removeView(bottomSheetView)
            }
            dialog.setContentView(bottomSheetView)
            dialog.show()
        }

        youtubeVideo = bottomSheetView.findViewById(R.id.youtube_player_view)
        lifecycle.addObserver(youtubeVideo)
        youtubeVideo.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(@NonNull youTubePlayer: YouTubePlayer) {
                val result = videoId.substringAfter("v=")
                youTubePlayer.cueVideo(result, 0f)
            }
        })

        binding.textView3.setAnimationDuration(750L)
        binding.textView3.setInterpolator(OvershootInterpolator())
        binding.textView8.setOnClickListener {
            if (binding.textView3.isExpanded) {
                binding.textView3.collapse()
                binding.textView8.setText(R.string.see_more)
            } else {
                binding.textView3.expand()
                binding.textView8.setText(R.string.see_less)
            }
        }


        binding.favBoxV3.setOnCheckedChangeListener { buttonView, isChecked ->

            val userid=CurrentUser.getCurrentUser(requireActivity())
            if (isChecked)
            {
                detailsViewModel.insertFavMealToUser(args.meal,userid)
                showSnackBarLong(view,"Added to favourites")
            }
            else
            {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Do you want to delete the item ?")
                    .setCancelable(true)
                    .setPositiveButton("Yes"){ _, _ ->
                        detailsViewModel.deleteFavMealById(args.meal.idMeal,userid)
                        showSnackBarLong(view,"Removed from favourites")
                    }
                    .setNegativeButton("No"){dialog , it ->
                        dialog.cancel()
                        buttonView.isChecked=true
                    }
                val dialog = builder.create()
                dialog.show()
            }
        }
    }

    override fun onDestroy() {
        detailsFragmentBinding = null
        super.onDestroy()
    }
}
