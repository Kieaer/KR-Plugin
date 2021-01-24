import Main.Companion.pluginRoot
import core.Log
import org.junit.Test

class InternalTest {
    @Test
    fun logTest(){
        for(a in 1..2000){
            for (day in Log.LogType.values()) {
                if(!pluginRoot.child("log/${day.name}.log").exists()) {
                    pluginRoot.child("log/${day.name}.log").writeString("")
                }
            }
        }
    }
}