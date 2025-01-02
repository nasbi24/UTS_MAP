package com.example.uts_map

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class HeatMapDecorator(private val dates: Collection<CalendarDay>) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.OVAL
        drawable.setColor(Color.rgb(100, 200, 255))
        view.setBackgroundDrawable(drawable)
        view.addSpan(ForegroundColorSpan(Color.WHITE)) // White text
        view.addSpan(StyleSpan(Typeface.BOLD)) // Bold text
    }
}