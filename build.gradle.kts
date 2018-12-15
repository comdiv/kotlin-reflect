import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.11"
    id("io.gitlab.arturbosch.detekt") version "1.0.0-RC12"
    id("org.jmailen.kotlinter") version "1.20.1"
    id("com.jfrog.bintray") version "1.8.4"
    jacoco
    `maven-publish`
}

val packageversion = "0.0.2"

group = "codes.comdiv.kotlin"
version = packageversion



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

tasks.withType<JacocoReport> {
    reports {
        xml.isEnabled = true
    }
}



tasks {
    val detekt by existing
    val check by existing
    val jacocoTestReport by existing
    val jacocoTestCoverageVerification by existing
    val lintKotlin by existing
    val compileKotlin by existing
    val formatKotlin by existing

    check {
        dependsOn(detekt)
        dependsOn(jacocoTestReport)
        dependsOn(jacocoTestCoverageVerification)
    }
    compileKotlin{
        dependsOn(lintKotlin)
    }

    (detekt as TaskProvider<*>){ //it cannot resolve between extension and task
        dependsOn(lintKotlin)
    }
    lintKotlin  {
        dependsOn(formatKotlin)
    }
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_KEY")
    publish = true
    setPublications("mavenJava")
    pkg.apply {
        repo = "main"
        name = "kotlin-reflect"
        setLicenses( "Apache-2.0" )
        vcsUrl = "https://github.com/comdiv/kotlin-reflect"
        version.apply {
            name = packageversion
        }
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.get("main").allSource)
}

publishing {

    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}