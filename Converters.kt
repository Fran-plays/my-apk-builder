package com.petmorph.ai.data.local

import androidx.room.TypeConverter
import com.petmorph.ai.data.model.CompanionType
import com.petmorph.ai.data.model.Personality

class Converters {
    @TypeConverter fun typeToString(t: CompanionType) = t.name
    @TypeConverter fun stringToType(s: String) = CompanionType.valueOf(s)
    @TypeConverter fun personalityToString(p: Personality) = p.name
    @TypeConverter fun stringToPersonality(s: String) = Personality.valueOf(s)
}
