package codes.comdiv.kotlin.reflection.inheritance

import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass

data class AnnotationDescriptor(
    val annotation: Annotation,
    val inheritanceLevel: Int = 0,
    val inheritanceTarget: KAnnotatedElement? = null,
    val inhertanceAnnotationTarget: KClass<out Annotation>? = null
) {
    val key = Triple(annotation.annotationClass, inheritanceTarget, inhertanceAnnotationTarget)
}
