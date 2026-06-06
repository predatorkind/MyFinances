package net.vertexgraphics.myfinances

object DebugSampleData {
    suspend fun populateIfNeeded(
        database: MyFinancesDatabase,
        preferenceManager: PreferenceManager
    ) {
        // Do nothing in release build
    }
}
