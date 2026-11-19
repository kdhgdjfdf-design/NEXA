package com.example.data.local

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class DhikrItem(
    val id: String,
    val category: String, // MORNING, EVENING, AFTER_PRAYER, SLEEP, WAKEUP
    val arabicText: String,
    val virtueAr: String,
    val virtueEn: String,
    val virtueFr: String,
    val targetCount: Int
)

data class DuaItem(
    val id: String,
    val category: String, // STUDY, CALM, PARENTS, GUIDANCE, DAILY
    val titleAr: String,
    val titleEn: String,
    val titleFr: String,
    val arabicText: String,
    val reference: String
)

data class IslamicOccasion(
    val id: String,
    val hijriMonth: Int,
    val hijriDay: Int,
    val titleAr: String,
    val titleEn: String,
    val titleFr: String,
    val noteAr: String,
    val noteEn: String,
    val noteFr: String
)

data class MotivationalQuote(
    val id: String,
    val textAr: String,
    val authorAr: String,
    val textEn: String,
    val authorEn: String,
    val textFr: String,
    val authorFr: String
)

object DefaultSeedData {
    val defaultHomeCardsJson: String by lazy {
        Json.encodeToString(
            listOf(
                CardLayoutConfig("greeting_progress", true, "EXPANDED", 0),
                CardLayoutConfig("tasks_summary", true, "STANDARD", 1),
                CardLayoutConfig("focus_summary", true, "STANDARD", 2),
                CardLayoutConfig("study_habits_pulse", true, "STANDARD", 3),
                CardLayoutConfig("prayer_next", true, "COMPACT", 4),
                CardLayoutConfig("quick_shortcuts", true, "STANDARD", 5),
                CardLayoutConfig("mini_audio", true, "COMPACT", 6)
            )
        )
    }

    fun defaultDesignProfiles(): List<DesignProfileEntity> = listOf(
        DesignProfileEntity(
            name = "NEXA • القهوة والأبيض",
            isBuiltIn = true,
            paletteKey = "NOX_ESPRESSO",
            themeMode = "SYSTEM",
            fontKey = "CAIRO",
            iconStyle = "ROUNDED",
            cardConfigJson = defaultHomeCardsJson
        ),
        DesignProfileEntity(
            name = "Warm Sand • رمل دافئ",
            isBuiltIn = true,
            paletteKey = "WARM_SAND",
            themeMode = "LIGHT",
            fontKey = "TAJAWAL",
            iconStyle = "MINIMAL",
            cardConfigJson = defaultHomeCardsJson
        ),
        DesignProfileEntity(
            name = "Midnight Focus • تركيز ليلي",
            isBuiltIn = true,
            paletteKey = "MIDNIGHT_FOCUS",
            themeMode = "DARK",
            fontKey = "READEX_PRO",
            iconStyle = "SHARP",
            cardConfigJson = defaultHomeCardsJson
        )
    )

    fun defaultAchievements(): List<AchievementProgressEntity> = listOf(
        AchievementProgressEntity("first_task", "TASKS", "ach_first_task", "ach_first_task_desc", 0, 1, 40),
        AchievementProgressEntity("tasks_10", "TASKS", "ach_tasks_10", "ach_tasks_10_desc", 0, 10, 120),
        AchievementProgressEntity("study_subject", "STUDY", "ach_study_subject", "ach_study_subject_desc", 0, 1, 50),
        AchievementProgressEntity("study_reviews_20", "STUDY", "ach_study_reviews_20", "ach_study_reviews_20_desc", 0, 20, 150),
        AchievementProgressEntity("habit_streak_7", "HABITS", "ach_habit_streak_7", "ach_habit_streak_7_desc", 0, 7, 140),
        AchievementProgressEntity("focus_60m", "FOCUS", "ach_focus_60m", "ach_focus_60m_desc", 0, 60, 100),
        AchievementProgressEntity("focus_600m", "FOCUS", "ach_focus_600m", "ach_focus_600m_desc", 0, 600, 300),
        AchievementProgressEntity("life_adhkar_7", "LIFE", "ach_life_adhkar_7", "ach_life_adhkar_7_desc", 0, 7, 100),
        AchievementProgressEntity("library_reader", "LIBRARY", "ach_library_reader", "ach_library_reader_desc", 0, 1, 80)
    )

    fun defaultLibraryItems(): List<LibraryItemEntity> = listOf(
        LibraryItemEntity(
            title = "كليلة ودمنة — باب الأسد والثور",
            author = "عبد الله بن المقفع (تراث أدبي عام)",
            category = "BOOK",
            shelf = "READING",
            coverKey = "SAGE",
            sourceInfo = "نص تراثي عربي متاح في الملكية العامة (Public Domain)",
            contentText = buildKalilaWaDimnaText(),
            totalPages = 6,
            currentPage = 0,
            isDownloaded = true
        ),
        LibraryItemEntity(
            title = "مختارات من رحلة ابن بطوطة",
            author = "محمد بن بطوطة (تراث أدبي عام)",
            category = "NOVEL",
            shelf = "WANT_TO_READ",
            coverKey = "SAND",
            sourceInfo = "تحفة النظار في غرائب الأمصار — نص في الملكية العامة (Public Domain)",
            contentText = buildIbnBattutaText(),
            totalPages = 5,
            currentPage = 0,
            isDownloaded = true
        ),
        LibraryItemEntity(
            title = "جواهر الحكمة والأدب العربي",
            author = "مختارات الجاحظ وابن عبد ربه",
            category = "BOOK",
            shelf = "WANT_TO_READ",
            coverKey = "TEAL",
            sourceInfo = "مختارات أدبية عربية كلاسيكية في الملكية العامة (Public Domain)",
            contentText = buildArabicWisdomText(),
            totalPages = 5,
            currentPage = 0,
            isDownloaded = true
        ),
        LibraryItemEntity(
            title = "الأيام — خواطر في طلب العلم",
            author = "مختارات أدبية تربوية",
            category = "NOVEL",
            shelf = "WANT_TO_READ",
            coverKey = "AMBER",
            sourceInfo = "نص أدبي تعريفي مناسب للناشئة والشباب (Public Domain / Educational)",
            contentText = buildLearningReflectionsText(),
            totalPages = 4,
            currentPage = 0,
            isDownloaded = true
        )
    )

    fun defaultPodcastEpisodes(): List<PodcastEpisodeEntity> = listOf(
        PodcastEpisodeEntity(
            showId = "show_hikma",
            showTitle = "رحلة مع المعرفة",
            showAuthor = "إذاعة السكينة الثقافية",
            title = "الحلقة ١: فن تنظيم الوقت والهدوء الذهني",
            description = "حلقة عربية هادئة تناقش بناء العادات اليومية المتوازنة بعيداً عن الإرهاق.",
            audioUrl = "https://archive.org/download/testmp3testfile/mpthreetest.mp3",
            durationMs = 28000L,
            playlistTag = "Productivity"
        ),
        PodcastEpisodeEntity(
            showId = "show_hikma",
            showTitle = "رحلة مع المعرفة",
            showAuthor = "إذاعة السكينة الثقافية",
            title = "الحلقة ٢: كيف تذاكر بذكاء دون تشتت؟",
            description = "استراتيجيات المراجعة المتباعدة واسترجاع المعلومات بأسلوب عملي مبسط.",
            audioUrl = "https://archive.org/download/testmp3testfile/mpthreetest.mp3",
            durationMs = 28000L,
            playlistTag = "Study"
        ),
        PodcastEpisodeEntity(
            showId = "show_adab",
            showTitle = "حكايات التراث والأدب",
            showAuthor = "نادي القراءة العربي",
            title = "الحلقة ١: قصص من كليلة ودمنة",
            description = "قراءة صوتية وقصة ملهمة في الحكمة والتأمل مناسبة لجميع الأعمار.",
            audioUrl = "https://archive.org/download/testmp3testfile/mpthreetest.mp3",
            durationMs = 28000L,
            playlistTag = "Literature"
        )
    )

    fun defaultAudioTracks(): List<AudioTrackEntity> = listOf(
        AudioTrackEntity(
            title = "مطر هادئ للتركيز • Gentle Rain",
            artist = "NEXA Studio",
            playlistName = "CALM",
            uriString = "builtin://rain",
            durationMs = 1800000L,
            isBuiltInCalm = true
        ),
        AudioTrackEntity(
            title = "ضوضاء دافئة • Warm Espresso Breeze",
            artist = "NEXA Studio",
            playlistName = "CALM",
            uriString = "builtin://warm_noise",
            durationMs = 1800000L,
            isBuiltInCalm = true
        ),
        AudioTrackEntity(
            title = "رنين التأمل الصافي • Tranquil Bowl",
            artist = "NEXA Studio",
            playlistName = "CALM",
            uriString = "builtin://chime",
            durationMs = 1800000L,
            isBuiltInCalm = true
        )
    )

    val motivationalQuotes: List<MotivationalQuote> = listOf(
        MotivationalQuote(
            id = "q1",
            textAr = "قليلٌ دائمٌ في هدوء، خيرٌ من كثيرٍ منقطعٍ مع الإرهاق.",
            authorAr = "حكمة طالب العلم",
            textEn = "Small consistent steps taken in calm outlast bursts driven by exhaustion.",
            authorEn = "Scholar's Maxim",
            textFr = "De petits pas réguliers dans le calme surpassent l'effort épuisant.",
            authorFr = "Maxime d'étude"
        ),
        MotivationalQuote(
            id = "q2",
            textAr = "ساعةُ تركيزٍ صادقة تفتح ما عجزت عنه ساعاتُ التشتّت.",
            authorAr = "NEXA",
            textEn = "One honest hour of deep focus unlocks what days of distraction cannot.",
            authorEn = "NEXA",
            textFr = "Une heure de concentration sincère ouvre ce que la dispersion ferme.",
            authorFr = "NEXA"
        ),
        MotivationalQuote(
            id = "q3",
            textAr = "اطلب العلمَ كما يطلبُ الغريقُ النجاة، ولكن بقلبٍ مطمئنٍ ونيةٍ صافية.",
            authorAr = "تأملات معرفية",
            textEn = "Seek knowledge with steady devotion, a tranquil heart, and clear intention.",
            authorEn = "Reflections on Learning",
            textFr = "Cherche le savoir avec constance, un cœur apaisé et une intention pure.",
            authorFr = "Réflexions"
        ),
        MotivationalQuote(
            id = "q4",
            textAr = "مقارنتُك العادلة هي مع نفسك بالأمس؛ كل صفحةٍ تلخّصها اليوم هي بناءٌ لغدك.",
            authorAr = "منهج السكينة",
            textEn = "Compare yourself only with yesterday; every page you summarize builds tomorrow.",
            authorEn = "Method of Serenity",
            textFr = "Ne te compare qu'à toi-même hier ; chaque résumé bâtit ton demain.",
            authorFr = "Méthode Sérénité"
        ),
        MotivationalQuote(
            id = "q5",
            textAr = "اللهم بارك لنا في أوقاتنا، واجعل تعب المذاكرة نوراً ورفعةً في الدنيا والآخرة.",
            authorAr = "إشراقة صباح",
            textEn = "Bless our time and turn quiet study effort into lasting clarity and light.",
            authorEn = "Morning Intention",
            textFr = "Que notre temps soit béni et que notre étude devienne lumière durable.",
            authorFr = "Intention du matin"
        )
    )

    val adhkarList: List<DhikrItem> = listOf(
        DhikrItem("m1", "MORNING", "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ.", "ذكر الصباح الافتتاحي", "Morning opening remembrance", "Invocation du matin", 1),
        DhikrItem("m2", "MORNING", "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ النُّشُورُ.", "تجديد التوكل مع مطلع اليوم", "Renewing trust at dawn", "Confiance en Dieu au lever du jour", 1),
        DhikrItem("m3", "MORNING", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ.", "ثلاث مرات صباحاً", "Three times in the morning", "Trois fois le matin", 3),
        DhikrItem("m4", "MORNING", "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.", "حصن وطمأنينة طوال اليوم", "Protection and peace all day", "Protection tout au long de la journée", 3),
        DhikrItem("m5", "MORNING", "حَسْبِيَ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ، عَلَيْهِ تَوَكَّلْتُ، وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ.", "سبع مرات صباحاً", "Seven times in the morning", "Sept fois le matin", 7),

        DhikrItem("e1", "EVENING", "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ.", "ذكر المساء الافتتاحي", "Evening opening remembrance", "Invocation du soir", 1),
        DhikrItem("e2", "EVENING", "اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ الْمَصِيرُ.", "ختام اليوم بالسكينة", "Ending the day in serenity", "Terminer la journée dans la sérénité", 1),
        DhikrItem("e3", "EVENING", "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ.", "ثلاث مرات مساءً", "Three times in the evening", "Trois fois le soir", 3),
        DhikrItem("e4", "EVENING", "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ ﷺ نَبِيًّا.", "ثلاث مرات", "Three times", "Trois fois", 3),

        DhikrItem("p1", "AFTER_PRAYER", "أَسْتَغْفِرُ اللَّهَ (ثلاثاً) اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ.", "عقب السلام من الصلاة", "Immediately after prayer", "Juste après la prière", 1),
        DhikrItem("p2", "AFTER_PRAYER", "سُبْحَانَ اللَّهِ.", "التسبيح دبر كل صلاة", "Tasbih after prayer", "Tasbih après la prière", 33),
        DhikrItem("p3", "AFTER_PRAYER", "الْحَمْدُ لِلَّهِ.", "التحميد دبر كل صلاة", "Tahmid after prayer", "Tahmid après la prière", 33),
        DhikrItem("p4", "AFTER_PRAYER", "اللَّهُ أَكْبَرُ.", "التكبير دبر كل صلاة", "Takbir after prayer", "Takbir après la prière", 33),

        DhikrItem("s1", "SLEEP", "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا.", "عند النوم", "Before sleeping", "Avant de dormir", 1),
        DhikrItem("s2", "SLEEP", "اللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ.", "ثلاث مرات عند الاضطجاع", "Three times before sleep", "Trois fois avant de dormir", 3),
        DhikrItem("s3", "SLEEP", "سُبْحَانَ اللَّهِ (٣٣) وَالْحَمْدُ لِلَّهِ (٣٣) وَاللَّهُ أَكْبَرُ (٣٤).", "تسبيح النوم", "Bedtime Tasbih", "Tasbih du coucher", 1),

        DhikrItem("w1", "WAKEUP", "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ.", "عند الاستيقاظ", "Upon waking up", "Au réveil", 1),
        DhikrItem("w2", "WAKEUP", "الْحَمْدُ لِلَّهِ الَّذِي عَافَانِي فِي جَسَدِي، وَرَدَّ عَلَيَّ رُوحِي، وَأَذِنَ لِي بِذِكْرِهِ.", "شكر العافية في الصباح", "Gratitude for well-being", "Gratitude au réveil", 1)
    )

    val duasList: List<DuaItem> = listOf(
        DuaItem("d1", "STUDY", "دعاء طلب العلم والفهم", "Dua for Knowledge & Understanding", "Doua pour la science et la compréhension", "رَبِّ زِدْنِي عِلْمًا، اللَّهُمَّ انْفَعْنِي بِمَا عَلَّمْتَنِي، وَعَلِّمْنِي مَا يَنْفَعُنِي، وَزِدْنِي عِلْمًا.", "طه: ١١٤ / مأثور"),
        DuaItem("d2", "STUDY", "دعاء تيسير المذاكرة", "Dua for Ease in Study", "Doua pour faciliter les études", "اللَّهُمَّ لَا سَهْلَ إِلَّا مَا جَعَلْتَهُ سَهْلًا، وَأَنْتَ تَجْعَلُ الْحَزْنَ إِذَا شِئْتَ سَهْلًا.", "صحيح ابن حبان"),
        DuaItem("d3", "CALM", "دعاء السكينة وزوال الهم", "Dua for Peace & Relief", "Doua pour la sérénité", "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْجُبْنِ وَالْبُخْلِ، وَضَلَعِ الدَّيْنِ، وَغَلَبَةِ الرِّجَالِ.", "صحيح البخاري"),
        DuaItem("d4", "CALM", "دعاء طمأنينة القلب", "Dua for Heart Tranquility", "Doua pour l'apaisement du cœur", "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ، وَلَا تَكِلْنِي إِلَىٰ نَفْسِي طَرْفَةَ عَيْنٍ.", "حسنه الألباني"),
        DuaItem("d5", "PARENTS", "دعاء للوالدين", "Dua for Parents", "Doua pour les parents", "رَبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا، اللَّهُمَّ اغْفِرْ لِوَالِدَيَّ وَارْزُقْهُمَا الْعَافِيَةَ وَالسَّعَادَةَ.", "الإسراء: ٢٤"),
        DuaItem("d6", "GUIDANCE", "دعاء الهداية والتوفيق", "Dua for Guidance", "Doua pour la guidance", "اللَّهُمَّ اهْدِنِي وَسَدِّدْنِي، اللَّهُمَّ إِنِّي أَسْأَلُكَ الْهُدَىٰ وَالتُّقَىٰ وَالْعَفَافَ وَالْغِنَىٰ.", "صحيح مسلم"),
        DuaItem("d7", "DAILY", "دعاء البركة في الوقت والعمل", "Dua for Blessing in Time", "Doua pour la bénédiction du temps", "اللَّهُمَّ بَارِكْ لَنَا فِي أَوْقَاتِنَا وَأَعْمَالِنَا، وَوَفِّقْنَا لِمَا تُحِبُّ وَتَرْضَىٰ.", "دعاء جامع")
    )

    val islamicOccasions: List<IslamicOccasion> = listOf(
        IslamicOccasion("new_year", 1, 1, "رأس السنة الهجرية", "Islamic New Year", "Nouvel An hégirien", "١ محرم — بداية العام الهجري الجديد", "1 Muharram — Beginning of Hijri Year", "1 Mouharram — Début de l'année hégirienne"),
        IslamicOccasion("ashura", 1, 10, "يوم عاشوراء", "Day of Ashura", "Jour d'Achoura", "١٠ محرم — يوم صيام وذكر وشكر", "10 Muharram — Day of fasting and gratitude", "10 Mouharram — Jour de jeûne et gratitude"),
        IslamicOccasion("mawlid", 3, 12, "ذكرى المولد النبوي الشريف", "Prophetic Mawlid", "Mawlid al-Nabawi", "١٢ ربيع الأول — تذكّر السيرة النبوية العطرة", "12 Rabi' al-Awwal — Reflecting on the Seerah", "12 Rabi' al-Awwal — Sîra du Prophète"),
        IslamicOccasion("isra", 7, 27, "ليلة الإسراء والمعراج", "Isra and Mi'raj", "Isra et Mi'raj", "٢٧ رجب — ذكرى الإسراء والمعراج", "27 Rajab — Night Journey", "27 Rajab — Voyage nocturne"),
        IslamicOccasion("shaban_15", 8, 15, "ليلة النصف من شعبان", "Mid-Sha'ban", "Mi-Cha'ban", "١٥ شعبان — الاستعداد القلبي لرمضان", "15 Sha'ban — Spiritual preparation for Ramadan", "15 Cha'ban — Préparation au Ramadan"),
        IslamicOccasion("ramadan", 9, 1, "غرة شهر رمضان المبارك", "Start of Ramadan", "Début du Ramadan", "١ رمضان — شهر القرآن والصيام والقيام", "1 Ramadan — Month of Quran & Fasting", "1 Ramadan — Mois du Coran et du jeûne"),
        IslamicOccasion("qadr", 9, 27, "ليالي العشر الأواخر وليلة القدر", "Last 10 Nights & Laylat al-Qadr", "Les 10 dernières nuits", "تحرّي ليلة القدر في الوتر من العشر الأواخر", "Seeking Laylat al-Qadr in the odd nights", "Recherche de Laylat al-Qadr"),
        IslamicOccasion("eid_fitr", 10, 1, "عيد الفطر المبارك", "Eid al-Fitr", "Aïd al-Fitr", "١ شوال — فرحة إتمام الصيام وصلة الرحم", "1 Shawwal — Celebration after Ramadan", "1 Chawwal — Fête de fin du jeûne"),
        IslamicOccasion("arafah", 12, 9, "يوم عرفة", "Day of Arafah", "Jour d'Arafat", "٩ ذو الحجة — خير يوم طلعت عليه الشمس", "9 Dhul-Hijjah — Day of Dua and Mercy", "9 Dhul-Hijja — Jour d'invocation"),
        IslamicOccasion("eid_adha", 12, 10, "عيد الأضحى المبارك", "Eid al-Adha", "Aïd al-Adha", "١٠ ذو الحجة — أيام التشريق والبهجة", "10 Dhul-Hijjah — Festival of Sacrifice", "10 Dhul-Hijja — Fête du Sacrifice")
    )

    private fun buildKalilaWaDimnaText(): String = """
باب الأسد والثور — من كتاب كليلة ودمنة

زعموا أن تاجراً كان له مال كثير، وكان له بنون ثلاثة، فلما بلغوا أشدهم أسرفوا في مال أبيهم ولم يحترفوا حرفة تدر عليهم رزقاً، فجمعهم أبوهم ووعظهم قائلاً: يا بني، إن أهل الدنيا يطلبون ثلاثة أشياء: السعة في العيش، والرفعة في الناس، والزاد للآخرة. ولا يُنال ذلك إلا باكتساب المال من وجهه الحلال، وحسن تدبيره، والإنفاق منه في مواضعه.

فاتعظ أكبرهم بكلام أبيه، وانطلق في تجارة مع قافلة ومعه ثوران عظيمان يقال لأحدهما شتربة والآخر بندبة. فلما بلغوا أرضاً وحلة، وحل شتربة في الوحل، وأعيى القوم إخراجه، خلف التاجر عنده رجلاً يحفظه حتى يبرأ ويلحقه. فلما طال المقام على الرجل سئم وتركه ولحق بالتاجر وأخبره أن الثور قد هلك.

ولم يلبث شتربة أن خلص من الوحل، ومشى حتى انتهى إلى مرج خصيب كثير الماء والكلأ، فأقام فيه آمناً مطمئناً حتى سمن واشتد، فجعل يخور خواراً عالياً يسمع عن بعد. وكان قريباً من ذلك المرج أسد يهابه جميع السباع، ولم يكن قد سمع خوار ثور قط، فلما سمع صوت شتربة هاله أمره وأقام مكانه لا يبرح.

وكان في حاشية الأسد ابنا آوى يقال لأحدهما كليلة وللآخر دمنة، وكلاهما ذو دهاء وأدب. فقال دمنة لأخيه كليلة: ما شأن الملك قد لزم مكانه لا يتحرك ولا ينشط؟ فأشار عليه كليلة بأن يلزم شأنه ولا يتدخل فيما لا يعنيه، غير أن دمنة طمع في القرب من الملك ونيل المنزلة، فدخل على الأسد وسأله بلطف وأدب عن سبب تريثه.

وتعلم هذه الحكاية في أبوابها التالية قيمة المشورة الصادقة، وخطر النميمة والتسرع، وأهمية التثبت قبل الحكم على الناس؛ فإن العاقل لا يقطع أمراً حتى ينظر في عواقبه، ولا يفرط في صديق أمين بسماع وشاية عابرة.
""".trimIndent()

    private fun buildIbnBattutaText(): String = """
مختارات من رحلة ابن بطوطة

خرجتُ من طنجة مسقط رأسي في يوم الخميس الثاني من شهر الله رجب الفرد عام خمسة وعشرين وسبعمائة، وأنا يومئذٍ ابن اثنتين وعشرين سنة، معتزماً حج بيت الله الحرام وزيارة قبر الرسول عليه السلام، منفرداً عن رفيق آنس بصحبته، وركب أكون في جملته، لباعثٍ على النفس شديد العزائم، وشوقٍ إلى تلك المعاهد الشريفة كامنٍ في الحيازم.

فجزت البلاد متأملاً في طبائع أهلها، وعمران مدنها، ومجالس علمائها. فلما وصلت الإسكندرية رأيت منارتها الشامخة ومرفأها العظيم الذي يلتقي فيه المسافرون من أقطار الأرض، ولقيت بها من العلماء والصلحاء من أكرم وفادتي وأفادني علماً وأدباً.

ثم سرت إلى القاهرة أم البلاد وقرارة فرعون ذي الأوتاد، ذات الأقاليم العريضة والبلاد الأريضة، المتناهية في كثرة العمارة، المتباهية بالحسن والنضارة، مجمع الوارد والصادر، ومحط رحل الضعيف والقادر، تموج موج البحر بسكانها وتكاد تضيق بهم على سعة مكانها.

وفي كل مدينة كان يمر بها، لم يكن ابن بطوطة يكتفي بوصف القصور والأسواق، بل كان يدون أسماء المدارس والمكتبات، ويشيد بوقف الخيرات للطلبة وعابري السبيل؛ فكانت الرحلة مدرسة مفتوحة تزيد الإنسان حكمة وتواضعاً وسعة أفق.
""".trimIndent()

    private fun buildArabicWisdomText(): String = """
جواهر الحكمة والأدب العربي

١. قيمة الوقت والهمة:
قيل: الوقت أنفس ما عُنيت بحفظه، وأراه أسهل ما عليك يضيع. ومن لم ينظم يومه ضاعت عليه أيامه في فضول القول والعمل.

٢. أدب طلب العلم:
قال الحكماء: أول العلم الصمت، والثاني حسن الاستماع، والثالث الحفظ، والرابع العمل به، والخامس نشره وتعليمه. ولا يستحي المتعلم أن يقول فيما لا يعلم: لا أعلم؛ فإن ذلك نصف العلم.

٣. الصحبة والخلق الكريم:
المرء على دين خليله، وخير الإخوان من دلك على الخير وأعانك عليه. والكلمة الطيبة تفتح مغاليق القلوب، والرفق ما كان في شيء إلا زانه، وما نُزع من شيء إلا شانه.

٤. التوازن والسكينة:
لا تأسفنّ على ما فاتك من عرض الدنيا، ولكن اجعل همك في إتقان يومك هذا؛ فإن السكينة ثمرة الرضا والعمل الصادق المتواصل ولو كان قليلاً.
""".trimIndent()

    private fun buildLearningReflectionsText(): String = """
خواطر في طلب العلم والمثابرة

إن رحلة التعلم لا تُقاس بسرعة الوصول بقدر ما تُقاس بصدق الخطوات واستمرارها. كثيرون يبدأون بحماس متقد ثم يتوقفون عند أول عقبة، بينما يمضي الهادئون بخطوات صغيرة يومية، فإذا بهم بعد أشهر قد شيدوا صرحاً من المعرفة والمهارة.

اجعل لنفسك دفتراً جانبياً تدون فيه خلاصة ما فهمت بأسلوبك الخاص؛ فالكتابة تثبت المعنى في الذهن وتكشف مواطن الغموض. وإذا تعثرت في درس فارجع إليه في اليوم التالي بعقل مستريح، وقسّمه إلى بطاقات صغيرة تسأل نفسك بها بصدق.

تذكر أن المقارنة العادلة الوحيدة هي مقارنتك بنفسك بالأمس: هل تعلمت اليوم كلمة جديدة؟ هل كنت أهدأ في تركيزك ولو بخمس دقائق؟ هذا التقدم الصغير هو الوقود الحقيقي للاستمرار.
""".trimIndent()
}
