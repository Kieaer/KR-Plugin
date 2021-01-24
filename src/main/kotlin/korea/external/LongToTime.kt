package korea.external

class LongToTime {
    operator fun get (mils: Long) : String{
        val seconds = mils / 1000
        val min = seconds / 60
        val hour = min / 60
        val days = hour / 24
        return String.format("%d:%02d:%02d:%02d",
            days % 365, hour % 24, min % 60, seconds % 60)
    }
}