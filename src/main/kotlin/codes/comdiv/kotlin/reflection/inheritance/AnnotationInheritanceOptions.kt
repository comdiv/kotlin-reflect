package codes.comdiv.kotlin.reflection.inheritance

data class AnnotationInheritanceOptions(
    val useInterfaces: Boolean = true,
    val useAnotations: Boolean = true,
    val useRepeatable: Boolean = true,
    val explicitInheritOnly: Boolean = true,
    val explicitRepeatableOnly: Boolean = true,
    val classInheritanceOptions: ClassInheritanceProviderOptions = ClassInheritanceProviderOptions.instance.copy(includeInterfaces = useInterfaces)
) {
    companion object {
        val instance = AnnotationInheritanceOptions()
    }
}
