package com.example.recipe_app.view.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recipe_app.R
import com.example.recipe_app.model.MealX


class home_adapter(var OnClick : OnClickListener) : RecyclerView.Adapter<home_adapter.MyViewHolder>() {
    var list = emptyList<MealX>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.simple_row_v2, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val meal = list[position]
        holder.title.text = list[position].strMeal
        holder.category.text = list[position].strCategory
        holder.country.text = list[position].strArea
        holder.favButton.isChecked = list[position].fav
        Glide.with(holder.itemView.context).load(list[position].strMealThumb).into(holder.image)
        holder.itemView.setOnClickListener {
            OnClick.onClick(meal)
        }
        holder.favButton.setOnCheckedChangeListener {_, isChecked ->
            OnClick.onFav(isChecked, meal)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.img_row_v2)
        var title: TextView = itemView.findViewById(R.id.txt_meal_name_v2)
        var category: TextView = itemView.findViewById(R.id.txt_meal_category_v2)
        var country: TextView = itemView.findViewById(R.id.txt_country_v2)
        var favButton :CheckBox = itemView.findViewById(R.id.fav_box_v2)

    }
    fun setDataToAdapter(newList: List<MealX>){
        list = newList
        notifyDataSetChanged()
    }
}