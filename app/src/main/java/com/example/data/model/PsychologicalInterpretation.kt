package com.example.data.model

import com.squareup.moshi.JsonClass
import org.json.JSONArray
import org.json.JSONObject

@JsonClass(generateAdapter = true)
data class ArchetypeItem(
    val name: String,
    val description: String,
    val manifestation: String
)

@JsonClass(generateAdapter = true)
data class DreamSymbolItem(
    val symbol: String,
    val psychologicalMeaning: String,
    val mythicEcho: String
)

@JsonClass(generateAdapter = true)
data class PsychologicalInterpretation(
    val dreamTitle: String = "Dream Vision",
    val emotionalTheme: String = "Subconscious Discovery",
    val emotionalToneTags: List<String> = emptyList(),
    val surrealistImagePrompt: String = "",
    val archetypes: List<ArchetypeItem> = emptyList(),
    val keySymbols: List<DreamSymbolItem> = emptyList(),
    val corePsychologicalSynthesis: String = "",
    val activeImaginationPrompts: List<String> = emptyList()
) {
    companion object {
        fun fromJsonString(
            archetypesJson: String,
            symbolsJson: String,
            promptsJson: String,
            title: String,
            theme: String,
            toneTags: String,
            analysis: String,
            prompt: String
        ): PsychologicalInterpretation {
            val archetypes = parseArchetypes(archetypesJson)
            val symbols = parseSymbols(symbolsJson)
            val prompts = parseStringList(promptsJson)
            val tags = if (toneTags.isNotBlank()) toneTags.split(",").map { it.trim() } else emptyList()
            return PsychologicalInterpretation(
                dreamTitle = title,
                emotionalTheme = theme,
                emotionalToneTags = tags,
                surrealistImagePrompt = prompt,
                archetypes = archetypes,
                keySymbols = symbols,
                corePsychologicalSynthesis = analysis,
                activeImaginationPrompts = prompts
            )
        }

        fun parseArchetypes(json: String): List<ArchetypeItem> {
            return try {
                val array = JSONArray(json)
                val list = mutableListOf<ArchetypeItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ArchetypeItem(
                            name = obj.optString("name", "Archetype"),
                            description = obj.optString("description", ""),
                            manifestation = obj.optString("manifestation", "")
                        )
                    )
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun parseSymbols(json: String): List<DreamSymbolItem> {
            return try {
                val array = JSONArray(json)
                val list = mutableListOf<DreamSymbolItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        DreamSymbolItem(
                            symbol = obj.optString("symbol", "Symbol"),
                            psychologicalMeaning = obj.optString("psychologicalMeaning", ""),
                            mythicEcho = obj.optString("mythicEcho", "")
                        )
                    )
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun parseStringList(json: String): List<String> {
            return try {
                val array = JSONArray(json)
                val list = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    list.add(array.optString(i))
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun listToJsonString(list: List<String>): String {
            val array = JSONArray()
            list.forEach { array.put(it) }
            return array.toString()
        }

        fun archetypesToJsonString(list: List<ArchetypeItem>): String {
            val array = JSONArray()
            list.forEach {
                val obj = JSONObject()
                obj.put("name", it.name)
                obj.put("description", it.description)
                obj.put("manifestation", it.manifestation)
                array.put(obj)
            }
            return array.toString()
        }

        fun symbolsToJsonString(list: List<DreamSymbolItem>): String {
            val array = JSONArray()
            list.forEach {
                val obj = JSONObject()
                obj.put("symbol", it.symbol)
                obj.put("psychologicalMeaning", it.psychologicalMeaning)
                obj.put("mythicEcho", it.mythicEcho)
                array.put(obj)
            }
            return array.toString()
        }
    }
}
