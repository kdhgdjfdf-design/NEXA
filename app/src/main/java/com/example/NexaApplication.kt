package com.example

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.data.local.DefaultSeedData
import com.example.data.local.NexaDatabase
import com.example.data.local.NexaPreferences
import com.example.domain.backup.BackupRepository
import com.example.services.FocusGuardManager
import com.example.services.LocalAudioPlayerController
import com.example.services.PodcastPlayerController
import com.example.services.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppContainer(val application: Application) {
    val db: NexaDatabase by lazy { NexaDatabase.getInstance(application) }
    val prefs: NexaPreferences by lazy { NexaPreferences(application) }
    val backupRepository: BackupRepository by lazy { BackupRepository(application, db, prefs) }
    val focusGuardManager: FocusGuardManager by lazy { FocusGuardManager(application, db, prefs) }
    val localAudioController: LocalAudioPlayerController by lazy {
        LocalAudioPlayerController(application, db.audioDao(), prefs)
    }
    val podcastController: PodcastPlayerController by lazy {
        PodcastPlayerController(application, db)
    }
}

class NexaApplication : Application() {
    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderScheduler.ensureChannels(this)
        // Initialize FocusGuardManager early for reboot/accessibility readiness
        container.focusGuardManager

        // Enforce Section 10 rule: Music & recitations pause on app background/lock and resume at same position
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                container.localAudioController.onAppBackgrounded()
            }

            override fun onStart(owner: LifecycleOwner) {
                container.localAudioController.onAppForegrounded()
            }
        })

        seedInitialStructuralData()
    }

    private fun seedInitialStructuralData() {
        appScope.launch {
            val state = container.prefs.state.first()
            if (!state.seededInitialData) {
                if (container.db.designDao().getAllProfiles().isEmpty()) {
                    var firstId = 1L
                    DefaultSeedData.defaultDesignProfiles().forEachIndexed { idx, profile ->
                        val id = container.db.designDao().insertProfile(profile)
                        if (idx == 0) firstId = id
                    }
                    container.prefs.setActiveProfileId(firstId)
                }
                if (container.db.libraryDao().getAllLibraryItemsSync().isEmpty()) {
                    DefaultSeedData.defaultLibraryItems().forEach {
                        container.db.libraryDao().insertLibraryItem(it)
                    }
                }
                if (container.db.libraryDao().getAllPodcastEpisodesSync().isEmpty()) {
                    DefaultSeedData.defaultPodcastEpisodes().forEach {
                        container.db.libraryDao().insertPodcastEpisode(it)
                    }
                }
                if (container.db.audioDao().getAllAudioTracksSync().isEmpty()) {
                    DefaultSeedData.defaultAudioTracks().forEach {
                        container.db.audioDao().insertAudioTrack(it)
                    }
                }
                if (container.db.systemDao().getAllAchievementsSync().isEmpty()) {
                    DefaultSeedData.defaultAchievements().forEach {
                        container.db.systemDao().upsertAchievement(it)
                    }
                }
                container.prefs.markSeeded()
                container.prefs.markMigratedToNoxTheme()
            } else if (!state.migratedToNoxTheme) {
                val profiles = container.db.designDao().getAllProfiles()
                val firstBuiltIn = profiles.firstOrNull { it.isBuiltIn }
                if (firstBuiltIn != null && (firstBuiltIn.paletteKey == "SERENE_SAGE" || firstBuiltIn.name.startsWith("NOX"))) {
                    container.db.designDao().updateProfile(
                        firstBuiltIn.copy(
                            name = "NEXA • القهوة والأبيض",
                            paletteKey = "NOX_ESPRESSO"
                        )
                    )
                }
                container.prefs.markMigratedToNoxTheme()
            }
        }
    }
}
