package com.fury.tiktoksample.utils

import com.fury.tiktoksample.extension.printLog
import kotlin.concurrent.thread

private val TAG = Collections::class.java.simpleName

class Collections {

    fun main() {
        val arrayInt = arrayOf(1,2,3,4,5,6)
        val arrayString = arrayOf("Amir", "Vicky", "Jay", "Akshat")
        arrayString.findElement("Amir") { index, element ->
            printLog(TAG, "Index : $index & Element : $element")
        }
    }

    private fun rollDice(callBack: ((result: Int) -> Unit)? = null): String {
        thread {
            Thread.sleep(3000)
            callBack?.invoke(4)
        }
        return "Dice Rolled"
    }

    private fun rollDice(range: IntRange, times: Int, callBack: (result: Int) -> Unit) {
        for (i in 0 until times) {
            val result = range.random()
            callBack(result)
        }
    }

    private fun <T> Array<T>.findElement(
        element: T,
        foundElement: (index: Int, element: T?) -> Unit
    ) {
        forEachIndexed { index, i ->
            if (i == element) {
                foundElement.invoke(index, i)
                return
            }
        }
        foundElement.invoke(-1, null)
        return
    }
}