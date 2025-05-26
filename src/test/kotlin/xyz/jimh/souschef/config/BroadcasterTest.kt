package xyz.jimh.souschef.config

import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class BroadcasterTest {

    @BeforeEach
    fun init() {
        listener1.clearMessages()
        listener2.clearMessages()
    }

    @Test
    fun `add and remove Listener test`() {
        val size = broadcaster.numListeners()
        val myListener = MyListener()
        broadcaster.addListener(myListener)
        assertEquals(size + 1, broadcaster.numListeners())
        broadcaster.removeListener(myListener)
        assertEquals(size, broadcaster.numListeners())
    }

    @Test
    fun `broadcast test`() {
        broadcaster.broadcast("bar", "foo")
        broadcaster.broadcast(12, "bar")
        broadcaster.broadcast(71.5, "foo")

        val set1 = listener1.getMessages()
        val set2 = listener2.getMessages()

        assertAll(
            { assertEquals(3, set1.size) },
            { assertEquals(3, set2.size) },
            { assertEquals(set1, set2) }
        )
    }

    @Test
    fun `noname broadcast test`() {
        broadcaster.broadcast("bar")
        broadcaster.broadcast(12)
        broadcaster.broadcast(71.5)

        val set1 = listener1.getMessages()
        val set2 = listener2.getMessages()

        assertAll(
            { assertEquals(3, set1.size) },
            { assertEquals(3, set2.size) },
            { assertEquals(set1, set2) }
        )
    }

    companion object {
        val broadcaster = Broadcaster()

        val listener1 = MyListener()
        val listener2 = MyListener()

        @BeforeAll
        @JvmStatic
        fun initAll() {
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
    fun clearMessages() = mySet.clear()
}
