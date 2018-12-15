package codes.comdiv.kotlin.reflection.inheritance

import codes.comdiv.kotlin.WalkStrategy
import java.util.LinkedList as Queue
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

class ClassInheritanceProvider(
    val options: ClassInheritanceProviderOptions = ClassInheritanceProviderOptions.instance
) {
    fun allSuperTypes(klass: KClass<*>, level: Int = 1): Iterator<ClassInheritanceDescriptor> =
        iterator {
            val queue = Queue(
                ClassInheritanceDescriptor.buildFromSuperClasses(
                    klass,
                    level
                )
            )
            val visited = mutableSetOf<KClass<*>>()
            val interfaces = mutableSetOf<ClassInheritanceDescriptor>()
            while (true) {
                val current = queue.poll() ?: break
                val it = current.klass
                val isInterface = it.java.isInterface
                if (!isMatch(current, visited)) continue
                if (!options.nativeInterfaceOrder || !isInterface) {
                    yield(current)
                } else {
                    interfaces.add(current)
                }
                val superclasses = it.superclasses
                if (options.strategy == WalkStrategy.BreadthFirst) {
                    queue.addAll(
                        ClassInheritanceDescriptor.buildFromSuperClasses(
                            it,
                            current.level + 1
                        )
                    )
                } else {
                    for ((i, superclass) in superclasses.reversed().withIndex()) {
                        queue.addFirst(
                            ClassInheritanceDescriptor(
                                superclass,
                                current.level + 1,
                                i,
                                it
                            )
                        )
                    }
                }
            }
            if (interfaces.any()) {
                yieldAll(interfaces.sortedWith(ClassInheritanceDescriptor.comparator))
            }
        }

    private fun isMatch(desc: ClassInheritanceDescriptor, visited: MutableSet<KClass<*>>): Boolean {
        val it = desc.klass
        val isInterface = it.java.isInterface
        if (!options.includeInterfaces && isInterface) return false
        if (!options.includeDuplicates) {
            if (visited.contains(it)) return false
            visited.add(it)
        }
        if (!options.includeAny && it == Any::class) return false
        return true
    }

    companion object {
        val instance = ClassInheritanceProvider()
    }
}

fun KClass<*>.allSuperTypes(options: ClassInheritanceProviderOptions = ClassInheritanceProviderOptions.instance) =
    when (options) {
        ClassInheritanceProviderOptions.instance -> ClassInheritanceProvider.instance.allSuperTypes(this)
        else -> ClassInheritanceProvider(options).allSuperTypes(this)
    }
