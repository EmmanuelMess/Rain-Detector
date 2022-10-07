package ar.com.emmanuelmessulam.raindetector.views

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.view.get
import ar.com.emmanuelmessulam.raindetector.R
import ar.com.emmanuelmessulam.raindetector.color
import ar.com.emmanuelmessulam.raindetector.dataclasses.City
import arrow.core.curried

class CitiesListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.listViewStyle
) : ListView(context, attrs, defStyleAttr) {

    private var itemSelected: Int? = null

    fun initializeAdapter(
        activity: Activity,
        items: List<City>,
        onClick: (city: City, position: Int, View) -> Unit
    ) {
        adapter = CitiesListAdapter(activity, items.map { Item(it, false) }, onClick)
    }

    fun setSelected(position: Int) {
        itemSelected?.let { itemSelected ->
            (adapter as CitiesListAdapter).items[itemSelected].selected = false
        }

        itemSelected = position
        (adapter as CitiesListAdapter).items[position].selected = true

        (adapter as CitiesListAdapter).notifyDataSetChanged()
    }

    private data class Item(val city: City, var selected: Boolean)

    private class CitiesListAdapter(
        val activity: Activity,
        val items: List<Item>,
        val onClick: (city: City, position: Int, View) -> Unit
    ) : BaseAdapter() {

        override fun getCount(): Int = items.size

        override fun getItem(position: Int): Item = items[position]

        override fun getItemId(position: Int): Long = items[position].hashCode().toLong()

        override fun getView(position: Int, convertView: View?, container: ViewGroup): View {
            val usableView: View = convertView
                ?: activity.layoutInflater.inflate(R.layout.city_item, container, false)

            val currentItem = getItem(position)

            val cityNameTextView: TextView = usableView.findViewById(R.id.cityNameTextView)
            cityNameTextView.text = currentItem.city.readableName

            if(currentItem.selected) {
                usableView.setBackgroundColor(
                    activity.resources.color(
                        R.color.blue_500,
                        activity.theme
                    )
                )
            } else {
                usableView.setBackgroundColor(0x00_00_00_00)
            }

            usableView.setOnClickListener(onClick.curried()(currentItem.city)(position))
            return usableView
        }
    }
}