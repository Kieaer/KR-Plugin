package korea.core
import arc.util.async.Threads.sleep
import korea.Main
import korea.Main.Companion.isDispose
import korea.PluginData
import korea.command.Permissions
import mindustry.gen.Groups
import java.io.IOException
import java.nio.file.*

object PermissionFileRead : Runnable{
    private lateinit var watchKey: WatchKey
    private lateinit var path: Path

    private var watchService: WatchService? = null
    private var tried = false

    override fun run() {
        Thread.currentThread().name = "Essential Permission Watch thread"
        while (!isDispose) {
            try {
                watchKey = watchService!!.take()
                sleep(50)
                val events = watchKey.pollEvents()
                for (event in events) {
                    val kind = event.kind()
                    val paths = (event.context() as Path).fileName.toString()
                    if (paths == "permission_user.hjson" || paths == "permission.hjson") {
                        if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            Permissions.load()
                            tried = !tried
                            Log.info("$paths 파일 업데이트됨")

                            for(a in Groups.player){
                                val data = PluginData[a.uuid()]
                                if (data != null){
                                    val perm = Permissions.userData.get(a.uuid()).asObject()
                                    a.name = perm.getString("name", a.name)
                                    data.permission = perm.getString("group", data.permission)
                                    a.admin(perm.getBoolean("admin", false))
                                }
                            }
                        }
                    }
                    /*if(kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                        System.out.println("created something in directory");
                    }else if(kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                        System.out.println("delete something in directory");
                    }else if(kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                        System.out.println("modified something in directory");
                    }else if(kind.equals(StandardWatchEventKinds.OVERFLOW)) {
                        System.out.println("overflow");
                    }else {
                        System.out.println("hello world");
                    }*/
                }
                if (!watchKey.reset()) {
                    try {
                        watchService!!.close()
                        break
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (ignored: Exception) {
            }
        }
    }

    init {
        try {
            watchService = FileSystems.getDefault().newWatchService()
            path = Paths.get(Main.pluginRoot.absolutePath())
            path.register(
                watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.OVERFLOW)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}