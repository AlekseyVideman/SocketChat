
plugins {
    id("java")
    id("application")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(26))
}

val mainClazz = "alekseyvideman.socket_chat.client.Client"
tasks {
    application {
        mainClass.set(mainClazz)
    }
    jar {
        manifest {
            attributes(
                "Main-Class" to mainClazz,
            )
        }
    }
    compileJava {
        options.encoding = "UTF-8"
        options.isFork = true // compile in a separate and reusable process (fast)
    }
}
