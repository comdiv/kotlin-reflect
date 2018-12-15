package codes.comdiv.kotlin.reflection.inheritance

import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

data class ClassInheritanceDescriptor(
    val klass: KClass<*>,
    val level: Int = 0,
    val inheritaneIndex: Int = 0,
    val inheritanceSource: KClass<*>
) {
    companion object {
        private val internalIterator = ClassInheritanceProvider()
        val comparator = object : Comparator<ClassInheritanceDescriptor> {
            override fun compare(first: ClassInheritanceDescriptor, second: ClassInheritanceDescriptor): Int {
                if (first.klass == second.klass) return 0
                if (internalIterator.allSuperTypes(first.klass).asSequence()
                        .any { it.klass == second.klass }
                ) return -1
                if (internalIterator.allSuperTypes(second.klass).asSequence()
                        .any { it.klass == first.klass }
                ) return 1
                return 0
            }
        }

        fun buildFromSuperClasses(klass: KClass<*>, level: Int = 1): List<ClassInheritanceDescriptor> {
            return klass.superclasses.mapIndexed { i, t ->
                ClassInheritanceDescriptor(
                    t,
                    level,
                    i,
                    klass
                )
            }
        }
    }
}
