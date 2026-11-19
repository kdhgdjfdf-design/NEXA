package com.example.services.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.NexaDatabase
import com.example.data.local.NexaPreferences
import com.example.domain.i18n.NexaLang
import com.example.domain.i18n.NexaStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object NexaWidgetUpdater {
    fun updateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        listOf(
            SmallTasksWidgetProvider::class.java to 1,
            MediumTasksWidgetProvider::class.java to 2,
            LargeTasksWidgetProvider::class.java to 3
        ).forEach { (cls, sizeType) ->
            val ids = manager.getAppWidgetIds(ComponentName(context, cls))
            if (ids.isNotEmpty()) {
                updateWidgets(context, manager, ids, sizeType)
            }
        }
    }

    fun updateWidgets(context: Context, manager: AppWidgetManager, ids: IntArray, sizeType: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = NexaDatabase.getInstance(context)
            val prefs = NexaPreferences(context).state.first()
            val strings = NexaStrings.forLang(NexaLang.fromCode(prefs.language))
            val activeTasks = db.taskDao().getAllTasksSync().filter { !it.isCompleted }

            val openPi = PendingIntent.getActivity(
                context,
                sizeType,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            ids.forEach { appWidgetId ->
                val layoutId = when (sizeType) {
                    1 -> R.layout.widget_tasks_small
                    2 -> R.layout.widget_tasks_medium
                    else -> R.layout.widget_tasks_large
                }
                val views = RemoteViews(context.packageName, layoutId)
                views.setOnClickPendingIntent(R.id.widget_root, openPi)
                views.setTextViewText(R.id.widget_title, "NEXA • ${strings.tasks}")
                views.setTextViewText(R.id.widget_count, "${activeTasks.size}")

                if (sizeType >= 2) {
                    val slotIds = listOf(R.id.widget_task_1, R.id.widget_task_2, R.id.widget_task_3)
                    slotIds.forEachIndexed { idx, viewId ->
                        val text = activeTasks.getOrNull(idx)?.title?.let { "• $it" } ?: ""
                        views.setTextViewText(viewId, text)
                    }
                    if (activeTasks.isEmpty()) {
                        views.setTextViewText(R.id.widget_task_1, strings.emptyTasksHint)
                    }
                }
                if (sizeType == 3) {
                    views.setTextViewText(R.id.widget_subtitle, strings.todayTasksCount)
                    val extraIds = listOf(R.id.widget_task_4, R.id.widget_task_5)
                    extraIds.forEachIndexed { idx, viewId ->
                        val text = activeTasks.getOrNull(idx + 3)?.title?.let { "• $it" } ?: ""
                        views.setTextViewText(viewId, text)
                    }
                }
                manager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}

class SmallTasksWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        NexaWidgetUpdater.updateWidgets(context, appWidgetManager, appWidgetIds, 1)
    }
}

class MediumTasksWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        NexaWidgetUpdater.updateWidgets(context, appWidgetManager, appWidgetIds, 2)
    }
}

class LargeTasksWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        NexaWidgetUpdater.updateWidgets(context, appWidgetManager, appWidgetIds, 3)
    }
}
