package codes.comdiv.kotlin.reflection.inheritance

import codes.comdiv.testutils.assume
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.annotation.Inherited

internal class AnnotationInheritanceProviderTest {

    @Inherited
    @Repeatable
    @Retention(AnnotationRetention.RUNTIME)
    private annotation class RepeatableInherited(val id: Int)

    @Inherited
    @Retention(AnnotationRetention.RUNTIME)
    private annotation class NonRepeatableInherited(val id: Int)

    @Repeatable
    @Retention(AnnotationRetention.RUNTIME)
    private annotation class NotInheritedAnnotation(val id: Int)

    @RepeatableInherited(0)
    @NotInheritedAnnotation(0)
    private interface IFaceWithAnnotations

    @RepeatableInherited(10)
    private interface ICoreInterface

    @RepeatableInherited(20)
    private interface ISubInterface : ICoreInterface

    @NonRepeatableInherited(1)
    @RepeatableInherited(1)
    @NotInheritedAnnotation(1)
    private abstract class AbstractWithAnnotations : ISubInterface

    @RepeatableInherited(2)
    @NonRepeatableInherited(2)
    @NotInheritedAnnotation(2)
    private class InterfaceImplementer : IFaceWithAnnotations

    @NonRepeatableInherited(3)
    @RepeatableInherited(3)
    @NotInheritedAnnotation(3)
    private open class BothImplement : AbstractWithAnnotations(), IFaceWithAnnotations, ICoreInterface

    @NonRepeatableInherited(4)
    @RepeatableInherited(4)
    @NotInheritedAnnotation(4)
    private class VeryInherited : BothImplement()

    private fun Annotation.inherit(value: Int) = this.matches<RepeatableInherited>(value)
    private fun Annotation.inheritNoRepeat(value: Int) = this.matches<NonRepeatableInherited>(value)
    private fun Annotation.notinherit(value: Int) = this.matches<NotInheritedAnnotation>(value)

    private inline fun <reified T> Annotation.matches(value: Int): List<() -> Unit> {
        val result = mutableListOf<() -> Unit>()
        result.add { assertThat(this).isInstanceOf(T::class.java) }
        if (this is T) {
            if (this is RepeatableInherited) {
                result.add { assertThat(this.id).isEqualTo(value) }
            } else if (this is NotInheritedAnnotation) {
                result.add { assertThat(this.id).isEqualTo(value) }
            }
        }
        return result
    }

    @Test
    fun defaultKotlinBehavior() {
        VeryInherited::class.annotations.assume {
            it.size.that { isEqualTo(3) }
            +it[0].inheritNoRepeat(4)
            +it[1].inherit(4)
            +it[2].notinherit(4)
        }
    }

    @Test
    fun defaultInheritBehavior() {
        AnnotationInheritanceProvider.instance.iterateAnnotations(VeryInherited::class)
            .toList().assume {
                it.size.that { isEqualTo(8) }
                +it[0].annotation.inheritNoRepeat(4)
                +it[1].annotation.inherit(4)
                +it[2].annotation.notinherit(4)
                it.getOrNull(3)?.let {
                    +it.annotation.inherit(3)
                    it.inheritanceLevel.that { isEqualTo(1) }
                    it.inheritanceTarget.that { isEqualTo(BothImplement::class) }
                }
                it.getOrNull(4)?.let {
                    +it.annotation.inherit(1)
                    it.inheritanceLevel.that { isEqualTo(2) }
                    it.inheritanceTarget.that { isEqualTo(AbstractWithAnnotations::class) }
                }
                it.getOrNull(5)?.let {
                    +it.annotation.inherit(0)
                    it.inheritanceLevel.that { isEqualTo(2) }
                    it.inheritanceTarget.that { isEqualTo(IFaceWithAnnotations::class) }
                }
                it.getOrNull(6)?.let {
                    +it.annotation.inherit(10)
                    it.inheritanceLevel.that { isEqualTo(2) }
                    it.inheritanceTarget.that { isEqualTo(ICoreInterface::class) }
                }
                it.getOrNull(7)?.let {
                    +it.annotation.inherit(20)
                    it.inheritanceLevel.that { isEqualTo(3) }
                    it.inheritanceTarget.that { isEqualTo(ISubInterface::class) }
                }
            }
    }
}