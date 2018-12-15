package codes.comdiv.kotlin.reflection.inheritance

import codes.comdiv.kotlin.WalkStrategy

data class ClassInheritanceProviderOptions(
    val includeInterfaces: Boolean = true,
    val includeDuplicates: Boolean = false,
    val includeAny: Boolean = false,
    val nativeInterfaceOrder: Boolean = false,
    val strategy: WalkStrategy = WalkStrategy.BreadthFirst
) {
    companion object {
        val instance = ClassInheritanceProviderOptions()
    }
}
