package com.example.recipe_app.view.home

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.recipe_app.R
import com.example.recipe_app.databinding.FragmentSearchBinding
import com.example.recipe_app.model.MealX
import com.example.recipe_app.utils.CurrentUser
import com.example.recipe_app.utils.GreenSnackBar
import com.example.recipe_app.utils.GreenSnackBar.showSnackBarLong
import com.example.recipe_app.utils.GreenSnackBar.showSnackBarWithDismiss
import com.example.recipe_app.utils.NetworkUtils
import com.example.recipe_app.utils.NetworkUtils.isInternetAvailable
import com.example.recipe_app.viewModels.DetailsViewModel
import com.example.recipe_app.viewModels.SearchViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() , OnClickListener {
    private var searchFragmentBinding: FragmentSearchBinding? = null
    val searchViewModel: SearchViewModel by viewModels()
    private lateinit var recyclerAdapter: SearchAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding=FragmentSearchBinding.bind(view)
        searchFragmentBinding=binding

        var userid=CurrentUser.getCurrentUser(requireActivity())
        val searchView = binding.searchView
        val recyclerView = binding.searchRecyclerView
        val typeToSearchLayout = binding.lottieTypeToSearchLayout
        val lootieNotFound = binding.lottieNotFound
        val shimmerFrameLayout = binding.shimmerViewContainerSearch
        recyclerAdapter = SearchAdapter(this)
        recyclerView.adapter = recyclerAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
            searchViewModel.listOfMeals.observe(viewLifecycleOwner){meals->
                if(meals.isNullOrEmpty() && searchView.query.isNullOrEmpty()){
                    recyclerView.visibility = View.GONE
                    typeToSearchLayout.visibility = View.VISIBLE
                    lootieNotFound.visibility = View.GONE
                    shimmerFrameLayout.visibility = View.GONE
                }
                 else if (meals.isNullOrEmpty()){
                    recyclerView.visibility = View.GONE
                    typeToSearchLayout.visibility = View.GONE
                    lootieNotFound.visibility = View.VISIBLE
                    shimmerFrameLayout.visibility = View.GONE
                }else{
                    recyclerView.visibility = View.VISIBLE
                    lootieNotFound.visibility = View.GONE
                    typeToSearchLayout.visibility = View.GONE
                    recyclerAdapter.setDataAdapter(meals)
                    shimmerFrameLayout.visibility = View.GONE
                }
            }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(query.isNullOrEmpty()){
                    typeToSearchLayout.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    lootieNotFound.visibility = View.GONE
                }
                else{
                    typeToSearchLayout.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    typeToSearchLayout.visibility = View.GONE
                    searchViewModel.getMealsWithFavourite(userid!!,query!!)
                    shimmerFrameLayout.visibility = View.VISIBLE
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText.isNullOrEmpty()){
                    typeToSearchLayout.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    lootieNotFound.visibility = View.GONE
                }
                else{
                    typeToSearchLayout.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    shimmerFrameLayout.visibility = View.VISIBLE
                    if(isInternetAvailable(requireActivity())) {
                        searchViewModel.getMealsWithFavourite(userid!!,newText!!)
                    }
                    else{
                        showSnackBarWithDismiss(view,"Internet disconnected")                 }
                }
                return false
            }
        })
    }

    override fun onClick(model: MealX) {
        findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToDetailsFragment(model))
    }
    override fun onFav(isChecked: Boolean, meal: MealX) {
        var userid=CurrentUser.getCurrentUser(requireActivity())
        if (isChecked)
        {
            searchViewModel.insertFavMealToUser(meal,userid!!)
            recyclerAdapter.updateItem(isChecked,meal)
            showSnackBarLong(requireView(),"Added to favourites")
        }
        else
        {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Do you want to delete the item ?")
                .setCancelable(true)
                .setPositiveButton("Yes"){dialog , it ->
                    recyclerAdapter.updateItem(false,meal)
                    searchViewModel.deleteFavMealById(meal.idMeal,userid!!)
                    showSnackBarLong(requireView(),"Removed from favourites")
                }
                .setNegativeButton("No"){dialog , it ->
                    dialog.cancel()
                    recyclerAdapter.updateItem(true,meal)
                    recyclerAdapter.notifyDataSetChanged()
                }

            val dialog = builder.create()
            dialog.show()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        searchViewModel.resetList()
        searchFragmentBinding=null
    }

}




