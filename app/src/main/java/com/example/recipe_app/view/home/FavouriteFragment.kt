package com.example.recipe_app.view.home

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.recipe_app.R
import com.example.recipe_app.databinding.FragmentFavouriteBinding
import com.example.recipe_app.model.MealX
import com.example.recipe_app.utils.CurrentUser
import com.example.recipe_app.utils.GreenSnackBar
import com.example.recipe_app.utils.GreenSnackBar.showSnackBarLong
import com.example.recipe_app.utils.GreenSnackBar.showSnackBarWithDismiss
import com.example.recipe_app.utils.NetworkUtils.isInternetAvailable
import com.example.recipe_app.viewModels.FavouriteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavouriteFragment : Fragment(), OnClickListener {
    private var favouriteFragmentBinding: FragmentFavouriteBinding? = null
    val favouriteViewModel: FavouriteViewModel by viewModels()
    lateinit var favRecyclerAdapter : FavMealAdapter
        override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favourite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding=FragmentFavouriteBinding.bind(view)
        favouriteFragmentBinding=binding
        val userid=CurrentUser.getCurrentUser(requireActivity())
        if(isInternetAvailable(requireActivity())) {
            favouriteViewModel.getFavMealsByUserId(userid)
        }
        else{
            showSnackBarWithDismiss(view,"No Internet Connection")
            }
        favRecyclerAdapter = FavMealAdapter(this)
        binding.FavRecyclerView.adapter = favRecyclerAdapter
        binding.FavRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        favouriteViewModel.favMeal.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.txtBackFav.visibility=View.VISIBLE
            }
            else{
                binding.txtBackFav.visibility=View.GONE
            }
            favRecyclerAdapter.setDataAdapter(it)
        }

            val slideGesture = object : SlideGesture(requireContext()){
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    when(direction)
                    {
                        ItemTouchHelper.LEFT -> {
                            val builder = AlertDialog.Builder(context)
                            builder.setMessage("Do you want to delete the item ?")
                                .setCancelable(true)
                                .setPositiveButton("Yes"){ _, _ ->
                                    favouriteViewModel.deleteFavMealById(favRecyclerAdapter.listOfMeals[viewHolder.adapterPosition].idMeal,userid!!)
                                    favRecyclerAdapter.deleteItem(viewHolder.adapterPosition)
                                    showSnackBarLong(view,"Item Deleted")
                                    if(favRecyclerAdapter.listOfMeals.isEmpty()){
                                        binding.txtBackFav.visibility=View.VISIBLE
                                    }
                                }

                                .setNegativeButton("No"){ dialog, _ ->
                                    dialog.cancel()
                                    favRecyclerAdapter.notifyItemChanged(viewHolder.adapterPosition)
                                }
                                .setCancelable(false)
                            val dialog = builder.create()
                            dialog.show()
                        }
                    }
                }
            }
            val touchHelper= ItemTouchHelper(slideGesture)
            touchHelper.attachToRecyclerView(binding.FavRecyclerView)
    }
    override fun onClick(model: MealX) {
       findNavController().navigate(FavouriteFragmentDirections.actionFavouriteFragmentToDetailsFragment(model))
    }
    override fun onFav(isChecked: Boolean, meal: MealX) {
        //do nothing
    }
    override fun onDestroy() {
        favouriteFragmentBinding=null
        super.onDestroy()
    }





}