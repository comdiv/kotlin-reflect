import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.11"
    id("io.gitlab.arturbosch.detekt") version "1.0.0-RC12"
    id("org.jmailen.kotlinter") version "1.20.1"
    jacoco
}

group = "codes.comdiv.kotlin"
version = "1.0-SNAPSHOT"



repositories {
    mavenCentral()
    jcenter()
}

val juintVersion = "5.3.2"
val assertjVersion = "3.11.1"
dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
    testCompile("org.junit.jupiter:junit-jupiter-api:$juintVersion")
    testCompile("org.junit.jupiter:junit-jupiter-params:$juintVersion")
    testCompile("org.assertj:assertj-core:$assertjVersion")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$juintVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}


detekt {
    toolVersion = "1.0.0-RC12"
    input = files("src/main/kotlin")
    filters = ".*/resources/.*,.*/build/.*"
    isIgnoreFailures = false
}


tasks {
    val detektTask = tasks.withType<Detekt>()
    val check by existing
    val jacocoTestReport by existing
    val jacocoTestCoverageVerification by existing
    val lintKotlin by existing
    val compileKotlin by existing
    val formatKotlin by existing
    check {
        dependsOn(detektTask)
        dependsOn(jacocoTestReport)
        dependsOn(jacocoTestCoverageVerification)
    }
    compileKotlin{
        dependsOn(lintKotlin)
    }
    detektTask.forEach {
        it.dependsOn(lintKotlin)
    }
    lintKotlin  {
        dependsOn(formatKotlin)
    }
}



