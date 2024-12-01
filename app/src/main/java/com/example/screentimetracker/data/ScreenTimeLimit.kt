data class ScreenTimeLimit(
    val hours: Int,
    val minutes: Int
) {
    fun toMinutes(): Int = hours * 60 + minutes
    
    companion object {
        fun fromMinutes(totalMinutes: Int): ScreenTimeLimit {
            return ScreenTimeLimit(
                hours = totalMinutes / 60,
                minutes = totalMinutes % 60
            )
        }
    }
} 