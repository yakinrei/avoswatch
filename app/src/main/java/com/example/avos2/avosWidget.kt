package com.example.avos2.AvosWidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import com.example.avos2.R
import java.util.Calendar

class AvosWidgetActivity : AppWidgetProvider() {

    // Handler e Runnable para atualizar o widget periodicamente
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private val periods = arrayOf("Madrugada", "Manhã", "Tarde", "Noite")
    private val totalMillisInDay = getTotalMillisInDay()
    private var currentPeriod = setupPeriod(totalMillisInDay)
    private var currentAvo = setupAvo((totalMillisInDay))
    private var currentCent = setupCent((totalMillisInDay))


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // Inicializa o Runnable para atualizar a cada 500ms


                runnable = object : Runnable {
            override fun run() {
                Log.d("WidgetUpdate", "Runnable está rodando")  // Log para verificar se o Runnable é chamado
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
                handler.postDelayed(this, 3000)
            }
        }

        // Inicia a execução do Runnable
        handler.post(runnable)
    }

    override fun onEnabled(context: Context) {
        // Iniciar o Runnable quando o widget for ativado
        handler.post(runnable)
    }

    // Este método é chamado quando o último widget é removido
    override fun onDisabled(context: Context) {
        // Remover callbacks para evitar vazamento de memória
        handler.removeCallbacks(runnable)
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        updateTime()

        val views = RemoteViews(context.packageName, R.layout.avos_widget)

        // Atualizar os TextViews do layout do widget
        views.setTextViewText(R.id.avoTextView, String.format("%02d", currentAvo))
        views.setTextViewText(R.id.centTextView, String.format("%02d", currentCent))
        views.setTextViewText(R.id.periodoTextView, periods[currentPeriod])

        // Atualizar o fundo de acordo com o período - usando setInt() para RelativeLayout
        when (currentPeriod) {
            0 -> views.setInt(R.id.background, "setBackgroundResource", R.drawable.raposa)
            1 -> views.setInt(R.id.background, "setBackgroundResource", R.drawable.eagle)
            2 -> views.setInt(R.id.background, "setBackgroundResource", R.drawable.badger)
            3 -> views.setInt(R.id.background, "setBackgroundResource", R.drawable.wolf)
            else -> views.setInt(R.id.background, "setBackgroundResource", R.drawable.raposa)
        }

        // Atualizar o widget no AppWidgetManager
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun updateTime() {
        currentCent = (currentCent + 1) % 100
            if (currentCent == 0) {
                currentAvo = (currentAvo + 1) % 72
                if (currentAvo == 0) {
                    currentPeriod = (currentPeriod + 1) % periods.size // Corrigido para não exceder o tamanho
                }
            }
        }

    private fun calculateCurrentTimeSection(threshold: Int, initialValue: Int): Int {
        var count = 0
        var value = initialValue
        while (value >= threshold) {
            count++
            value -= threshold
        }
        return count
    }

    private fun getTotalMillisInDay(): Int {
        val calendar = Calendar.getInstance()
        return (calendar.get(Calendar.HOUR_OF_DAY) * 3600 * 1000 +
                calendar.get(Calendar.MINUTE) * 60 * 1000 +
                calendar.get(Calendar.SECOND) * 1000 +
                calendar.get(Calendar.MILLISECOND))
    }


private fun setupPeriod(totalMillisInDay: Int): Int {
        return calculateCurrentTimeSection(21600000, totalMillisInDay)
    }

    private fun setupAvo(totalMillisInDay: Int): Int {
        var miliavos = totalMillisInDay % 21600000
        return calculateCurrentTimeSection(300000, miliavos)
    }

    private fun setupCent(totalMillisInDay: Int): Int {
        var milicents = totalMillisInDay % 300000
        return calculateCurrentTimeSection(3000, milicents)
    }

}


