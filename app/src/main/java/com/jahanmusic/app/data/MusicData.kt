package com.jahanmusic.app.data

data class Song(
    val id: Int,
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val albumArtRes: Int = 0
)

object MusicData {
    val songs = listOf(
        Song(1, "Blinding Lights", "The Weeknd", "After Hours", "3:20"),
        Song(2, "Save Your Tears", "The Weeknd", "After Hours", "3:35"),
        Song(3, "Someone You Loved", "Lewis Capaldi", "Divinely Uninspired", "3:02"),
        Song(4, "Stressed Out", "Twenty One Pilots", "Blurryface", "3:22"),
        Song(5, "Believer", "Imagine Dragons", "Evolve", "3:24"),
        Song(6, "Hotel California", "Eagles", "Hotel California", "6:30"),
        Song(7, "Shape of You", "Ed Sheeran", "Divide", "3:53"),
        Song(8, "Starboy", "The Weeknd", "Starboy", "3:50"),
        Song(9, "Thunder", "Imagine Dragons", "Evolve", "3:07"),
        Song(10, "Perfect", "Ed Sheeran", "Divide", "4:23")
    )

    val albums = listOf(
        Song(1, "After Hours", "The Weeknd", "After Hours", "16 songs"),
        Song(2, "Divide", "Ed Sheeran", "Divide", "16 songs"),
        Song(3, "Evolve", "Imagine Dragons", "Evolve", "12 songs")
    )

    val playlists = listOf(
        Song(1, "Chill Vibes", "Various", "Playlist", "24 songs"),
        Song(2, "Workout Mix", "Various", "Playlist", "18 songs"),
        Song(3, "Focus Flow", "Various", "Playlist", "32 songs")
    )
}
