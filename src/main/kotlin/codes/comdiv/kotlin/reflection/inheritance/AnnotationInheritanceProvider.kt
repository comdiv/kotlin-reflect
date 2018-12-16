package codes.comdiv.kotlin.reflection.inheritance

import java.lang.annotation.Inherited
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

class AnnotationInheritanceProvider(
    val options: AnnotationInheritanceOptions = AnnotationInheritanceOptions.instance
) {

    fun iterateAnnotations(annotated: KAnnotatedElement): Sequence<AnnotationDescriptor> {
        val repeatCache = mutableSetOf<KClass<*>>()
        return iterateAllAnnotations(annotated).asSequence()
            .sortedBy { it.inheritanceLevel }
            .filter { isMatch(it, repeatCache) }
            .distinctBy { it.key }
    }

    private fun KAnnotatedElement?.isInterface() =
        (this as? KClass<*>)?.java?.isInterface ?: false

    private fun isMatch(a: AnnotationDescriptor, repeatCache: MutableSet<KClass<*>>): Boolean {
        return !checkInterfacesUsage(a) && !checkExplicitInheritance(a) && !checkRepeatable(a, repeatCache)
    }

    private fun checkRepeatable(
        a: AnnotationDescriptor,
        repeatCache: MutableSet<KClass<*>>
    ): Boolean {
        var checkRepeat = true
        if (options.useRepeatable) {
            checkRepeat = false
            if (options.explicitRepeatableOnly) {
                if (null == a.annotation.annotationClass.findAnnotation<Repeatable>()) {
                    checkRepeat = true
                }
            }
        }
        if (checkRepeat) {
            if (repeatCache.contains(a.annotation.annotationClass)) return true
            repeatCache.add(a.annotation.annotationClass)
        }
        return false
    }

    private fun checkExplicitInheritance(a: AnnotationDescriptor) = (
            a.inheritanceLevel > 0 &&
                    options.explicitInheritOnly &&
                    a.annotation.annotationClass.findAnnotation<Inherited>() == null
            )

    private fun checkInterfacesUsage(a: AnnotationDescriptor): Boolean =
        (!options.useInterfaces && a.inheritanceLevel > 0 && a.inheritanceTarget.isInterface())

    private fun iterateAllAnnotations(annotated: KAnnotatedElement) = iterator {
        val self = this@AnnotationInheritanceProvider
        yieldAll(annotated.annotations.map { AnnotationDescriptor(it, 0, annotated) })
        if (annotated is KClass<*>) {
            for (desc in annotated.allSuperTypes(self.options.classInheritanceOptions)) {
                yieldAll(desc.klass.annotations.map { AnnotationDescriptor(it, desc.level, desc.klass) })
            }
        }
    }

    companion object {
        val instance = AnnotationInheritanceProvider()
    }
}
