package com.example

import com.example.data.model.DreamEntry
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun dreamEntry_supportsPredefinedAndCustomWakingMoods() {
    val entry1 = DreamEntry(
      title = "Floating in the Aurora",
      rawTranscription = "I was floating above a shimmering sea...",
      wakingMood = "Peaceful"
    )
    assertEquals("Peaceful", entry1.wakingMood)

    val entry2 = DreamEntry(
      title = "The Shattered Labyrinth",
      rawTranscription = "The walls turned into mirrored puzzles...",
      wakingMood = "Nostalgic and Disoriented"
    )
    assertEquals("Nostalgic and Disoriented", entry2.wakingMood)
  }
}
