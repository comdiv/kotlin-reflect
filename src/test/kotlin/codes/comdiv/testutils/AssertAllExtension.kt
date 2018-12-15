package codes.comdiv.testutils

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ObjectAssert
import org.junit.jupiter.api.assertAll

internal class AssertAllBuilder<T>(val value: T?) {
    val assertions = mutableListOf<()->Unit>({ assertThat(value).isNotNull })
    val it get() = value!!
    fun that(assertsSupplyer: (T.() -> Unit)) {
        assertions.add { assertsSupplyer.invoke(value!!) }
    }
    inline fun <T> T.that(crossinline assertion: ObjectAssert<T>.() -> Unit) {
        assertions.add { assertThat(this).assertion() }
    }
    operator fun List<()->Unit>.unaryPlus() {
        assertions.addAll(this)
    }
}

internal inline fun <reified T> T?.assume(builder: (AssertAllBuilder<T>.() -> Unit)): T {
    val assertBuilder = AssertAllBuilder(this)
    builder.invoke(assertBuilder)
    assertAll(*assertBuilder.assertions.toTypedArray())
    return this!!
}