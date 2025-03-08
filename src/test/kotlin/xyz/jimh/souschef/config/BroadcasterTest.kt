package xyz.jimh.souschef.config

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

class BroadcasterTest {

    @Test
    fun `add and remove Listener test`() {
        val size = broadcaster.numListeners()
        val myListener = MyListener()
        broadcaster.addListener(myListener)
        Assertions.assertEquals(size + 1, broadcaster.numListeners())
        broadcaster.removeListener(myListener)
        Assertions.assertEquals(size, broadcaster.numListeners())
    }

    @Test
    fun `broadcast test`() {
        broadcaster.broadcast("foo", "bar")
        broadcaster.broadcast("bar", 12)
        broadcaster.broadcast("foo", 71.5)

        val set1 = listener1.getMessages()
        val set2 = listener2.getMessages()

        Assertions.assertAll(
            Executable { Assertions.assertEquals(3, set1.size) },
            Executable { Assertions.assertEquals(3, set2.size) },
            Executable { Assertions.assertEquals(set1, set2) }
        )
    }

    companion object {
        val broadcaster = Broadcaster()

        val listener1 = MyListener()
        val listener2 = MyListener()

        @BeforeAll
        @JvmStatic
        fun init() {
            broadcaster.addListener(listener1)
            broadcaster.addListener(listener2)
        }

    }
}

class MyListener : Listener {
    val mySet = HashSet<Pair<String, Any>>()

    override fun listen(name: String, value: Any) {
        mySet.add(name to value)
    }

    fun getMessages() = mySet
}
