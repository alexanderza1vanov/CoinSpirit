package com.example.coinspirit2

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.coinspirit2.ui.activitys.MainActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Test
    fun testLaunchMainActivity() {
        // Запускаем MainActivity
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            // Проверяем, что активность не null
            assert(activity != null)

            // Проверяем некоторые элементы интерфейса
            val recyclerView = activity.findViewById<RecyclerView>(R.id.main_recyclerview)
            assert(recyclerView != null)
        }
    }
}
