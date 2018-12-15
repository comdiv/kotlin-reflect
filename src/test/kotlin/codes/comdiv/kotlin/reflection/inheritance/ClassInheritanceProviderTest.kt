package codes.comdiv.kotlin.reflection.inheritance

import codes.comdiv.kotlin.WalkStrategy
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class ClassInheritanceProviderTest {

    interface ICore
    interface ISubCore : ICore
    interface ISubSubCore : ISubCore
    interface ISome
    interface ISubSome : ISome
    abstract class Root : ICore
    open class Sub : Root(), ISubSubCore
    open class Some : Sub(), ISubSome

    companion object {
        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            ClassInheritanceProvider.instance.allSuperTypes(Some::class).asSequence().toList()
        }
    }

    @Test
    fun BUG_invalid_class_levels() {
        val allsupertypes = Some::class.allSuperTypes().asSequence().toList()
        assertThat(allsupertypes).anyMatch { it.level == 3 }
    }

    @Test
    fun defaultSuperclassIteration() {
        assertThat(
            ClassInheritanceProvider.instance
                .allSuperTypes(Some::class)
                .asSequence()
                .map { it.klass }
                .toList()
        ).containsExactly(
            Sub::class,
            ISubSome::class,
            Root::class,
            ISubSubCore::class,
            ISome::class,
            ICore::class,
            ISubCore::class
        )
    }

    @Test
    fun depthFirstSuperclassIteration() {
        assertThat(
            ClassInheritanceProvider(
                ClassInheritanceProviderOptions.instance
                    .copy(strategy = WalkStrategy.DepthFirst)
            ).allSuperTypes(Some::class)
                .asSequence()
                .map { it.klass }
                .toList()
        ).containsExactly(
            Sub::class,
            Root::class,
            ICore::class,
            ISubSubCore::class,
            ISubCore::class,
            ISubSome::class,
            ISome::class
        )
    }

    @Test
    fun depthFirstNativeInterfaceOrderSuperclassIteration() {
        assertThat(
            ClassInheritanceProvider(
                ClassInheritanceProviderOptions.instance
                    .copy(strategy = WalkStrategy.DepthFirst, nativeInterfaceOrder = true)
            ).allSuperTypes(Some::class)
                .asSequence()
                .map { it.klass }
                .toList()
        ).containsExactly(
            Sub::class,
            Root::class,
            ISubSubCore::class,
            ISubCore::class,
            ICore::class,
            ISubSome::class,
            ISome::class
        )
    }

    @Test
    fun iterateNotIncludingInterfaces() {
        assertThat(
            ClassInheritanceProvider(
                ClassInheritanceProviderOptions.instance
                    .copy(includeInterfaces = false)
            ).allSuperTypes(Some::class)
                .asSequence()
                .map { it.klass }
                .toList()
        ).containsExactly(
            Sub::class,
            Root::class
        )
    }

    @Test
    fun iterateWithAllDulicates() {
        assertThat(
            ClassInheritanceProvider(
                ClassInheritanceProviderOptions.instance
                    .copy(includeDuplicates = true)
            ).allSuperTypes(Some::class)
                .asSequence()
                .map { it.klass }
                .toList()
        ).containsExactly(
            Sub::class,
            ISubSome::class,
            Root::class,
            ISubSubCore::class,
            ISome::class,
            ICore::class,
            ISubCore::class,
            ICore::class
        )
    }

    @Test
    fun breadFirstNativeInterfaceOrderSuperclassIteration() {
        assertThat(
            ClassInheritanceProvider(
                ClassInheritanceProviderOptions.instance
                    .copy(strategy = WalkStrategy.BreadthFirst, nativeInterfaceOrder = true)
            ).allSuperTypes(Some::class)
                .asSequence()
                .map { it.klass }
                .toList()
        ).containsExactly(
            Sub::class,
            Root::class,
            ISubSome::class,
            ISubSubCore::class,
            ISome::class,
            ISubCore::class,
            ICore::class
        )
    }
}