package net.vertexgraphics.myfinances

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.vertexgraphics.myfinances.ui.FinanceApp
import net.vertexgraphics.myfinances.ui.theme.MyFinancesTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataMigrator: DataMigrator

    @Inject
    lateinit var database: MyFinancesDatabase

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            dataMigrator.migrateIfNeeded()
            DebugSampleData.populateIfNeeded(database, preferenceManager)
        }

        setContent {
            MyFinancesTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    FinanceApp()
                }
            }
        }
    }
}
