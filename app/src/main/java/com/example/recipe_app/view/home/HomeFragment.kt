package com.example.recipe_app.view.home

import android.app.AlertDialog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recipe_app.R
import com.example.recipe_app.databinding.FragmentHomeBinding
import com.example.recipe_app.model.MealX
import com.example.recipe_app.utils.CurrentUser
import com.example.recipe_app.utils.GreenSnackBar
import com.example.recipe_app.utils.GreenSnackBar.showSnackBarLong
import com.example.recipe_app.utils.GreenSnackBar.showSnackBarWithDismiss
import com.example.recipe_app.utils.NetworkUtils
import com.example.recipe_app.utils.NetworkUtils.isInternetAvailable
import com.example.recipe_app.viewModels.HomeMealsViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class HomeFragment : Fragment(), OnClickListener {
    private var homeFragmentBinding: FragmentHomeBinding? = null
    private val homeViewModel: HomeMealsViewModel by viewModels()
    private lateinit var recyclerAdapter: HomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHomeBinding.bind(view)
        homeFragmentBinding = binding

        val userId = CurrentUser.getCurrentUser(requireActivity())
        if (isInternetAvailable(requireActivity())) {
            homeViewModel.getRandomMeal()
            homeViewModel.getMealsWithFavourite(userId)
        } else {
            showSnackBarWithDismiss(view, "No Internet Connection")
        }

        recyclerAdapter = HomeAdapter(this)
        binding.HomeRecyclerView.adapter = recyclerAdapter
        binding.HomeRecyclerView.layoutManager =
            GridLayoutManager(requireActivity(), 2, GridLayoutManager.HORIZONTAL, false)

        homeViewModel.listOfMeals.observe(viewLifecycleOwner) { meals ->
            recyclerAdapter.setDataToAdapter(meals)
        }

        homeViewModel.randomMeal.observe(viewLifecycleOwner) { randomMeal ->
            binding.nameRandom.text = randomMeal.strMeal
            binding.catRandom.text = randomMeal.strCategory.plus(" |")
            binding.areaRandom.text = randomMeal.strArea
            Glide.with(this)
                .load(randomMeal.strMealThumb)
                .into(binding.imgRandom)
            binding.shimmerLayout.stopShimmer()
            binding.shimmerLayout.visibility = View.GONE
            binding.constrainRandom.visibility = View.VISIBLE
            binding.HomeRecyclerView.visibility = View.VISIBLE

            binding.constrainRandom.setOnClickListener {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToDetailsFragment(
                        randomMeal
                    )
                )
            }
        }
    }


    override fun onClick(model: MealX) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToDetailsFragment(
                model
            )
        )
    }

    override fun onFav(isChecked: Boolean, meal: MealX) {
        val userId = CurrentUser.getCurrentUser(requireActivity())

        if (isChecked) {
            homeViewModel.insertFavMealToUser(meal, userId)
            recyclerAdapter.updateItem(true, meal)
            showSnackBarLong(requireView(), "Added to favourites")
        } else {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Do you want to delete the item ?")
                .setCancelable(true)
                .setPositiveButton("Yes") { dialog, it ->
                    recyclerAdapter.updateItem(false, meal)
                    homeViewModel.deleteFavMealById(meal.idMeal, userId)
                    showSnackBarLong(requireView(), "Removed from favourites")
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.cancel()
                    recyclerAdapter.updateItem(true, meal)
                    recyclerAdapter.notifyDataSetChanged()
                }
            val dialog = builder.create()
            dialog.show()
        }
    }

    override fun onDestroy() {
        homeFragmentBinding = null
        super.onDestroy()
    }


}