package codes.comdiv.kotlin.reflection.inheritance

import java.lang.annotation.Inherited
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.superclasses

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

    private fun isMatch(a: AnnotationDescriptor, repeatCache: MutableSet<KClass<*>>): Boolean {
        if (!options.useInterfaces) {
            if (a.inheritanceLevel > 0 && a.inheritanceTarget is KClass<*>) {
                if (a.inheritanceTarget.java.isInterface) return false
            }
        }
        if (a.inheritanceLevel > 0) {
            if (options.explicitInheritOnly && a.annotation.annotationClass.findAnnotation<Inherited>() == null) {
                return false
            }
        }
        if (options.useRepeatable) {
            if (options.explicitRepeatableOnly) {
                if (null == a.annotation.annotationClass.findAnnotation<Repeatable>()) {
                    if (repeatCache.contains(a.annotation.annotationClass)) return false
                    repeatCache.add(a.annotation.annotationClass)
                }
            }
        } else {
            if (repeatCache.contains(a.annotation.annotationClass)) return false
            repeatCache.add(a.annotation.annotationClass)
        }

        return true
    }

    private fun iterateAllAnnotations(annotated: KAnnotatedElement) = iterator {
        val self = this@AnnotationInheritanceProvider
        yieldAll(annotated.annotations.map { AnnotationDescriptor(it, 0, annotated) })
        if (annotated is KClass<*>) {
            for (desc in annotated.allSuperTypes(self.options.classInheritanceOptions)) {
                yieldAll(desc.klass.annotations.map { AnnotationDescriptor(it, desc.level, desc.klass) })
            }
        }
    }

    private fun iterateClassAnnotations(annotated: KClass<*>, level: Int = 0): MutableList<AnnotationDescriptor> {
        val result = mutableListOf<AnnotationDescriptor>()
        result.addAll(annotated.annotations.map { AnnotationDescriptor(it, level, annotated) })
        for (supertype in annotated.superclasses) {
            result.addAll(iterateClassAnnotations(supertype, level + 1))
        }
        return result
    }

    companion object {
        val instance = AnnotationInheritanceProvider()
    }
}
