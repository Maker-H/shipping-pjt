import org.gradle.kotlin.dsl.openapi3

plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    id ("com.epages.restdocs-api-spec") version "0.18.2"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    id("jacoco")
}

val asciidoctorExt: Configuration by configurations.creating

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

allprojects {
    group = property("app.group").toString()
}

dependencyManagement {
    imports {
        mavenBom(libs.spring.cloud.dependencies.get().toString())
    }
}



dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    runtimeOnly("com.h2database:h2")

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.spring.boot.configuration.processor)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.restdocs.restassured)
    testImplementation(libs.restassured)
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.18.2")
    // asciidoctor 의존성을 올바른 configuration으로 변경
    asciidoctorExt(libs.spring.boot.restdocs.asciidoctor)

}

// about source and compilation
java {
    sourceCompatibility = JavaVersion.VERSION_17
}

with(extensions.getByType(JacocoPluginExtension::class.java)) {
    toolVersion = "0.8.7"
}

// bundling tasks
tasks.getByName("bootJar") {
    enabled = true
}
tasks.getByName("jar") {
    enabled = false
}

// test tasks
val snippetsDir by extra { file("build/generated-snippets") }


tasks {
    test {
        outputs.dir(snippetsDir)
        useJUnitPlatform()
        ignoreFailures = true
        finalizedBy(jacocoTestReport)
    }

    asciidoctor {
        dependsOn (test)
        inputs.dir (snippetsDir)
        configurations ("asciidoctorExt")
        baseDirFollowsSourceFile()
    }

    // openapi3 task를 제대로 설정
    openapi3 {
        title = "My API"
        description = "An ecommerce sample demonstrating restdocs-api-spec"
        version = "0.1.0"
        format = "yaml"
        outputFileNamePrefix = "openapi-3"
        outputDirectory = "build/api-spec"
    }

    // bootJar가 asciidoctor와 openapi3에 의존하도록 설정
    bootJar {
        dependsOn(asciidoctor)
        dependsOn(openapi3)
    }

    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required = true
            html.required = true
        }
    }
}