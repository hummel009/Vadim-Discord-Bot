import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
	id("application")
	id("org.jetbrains.kotlin.jvm") version "latest.release"
}

group = "com.github.hummel"
version = LocalDate.now().format(DateTimeFormatter.ofPattern("yy.MM.dd"))

val embed: Configuration by configurations.creating

dependencies {
	embed("org.jetbrains.kotlin:kotlin-stdlib:latest.release")

	embed("com.google.code.gson:gson:latest.release")
	embed("org.apache.httpcomponents.client5:httpclient5:latest.release")
	embed("net.lingala.zip4j:zip4j:latest.release")
	embed("net.dv8tion:JDA:latest.release")
	embed("org.telegram:telegrambots-longpolling:latest.release")
	embed("org.telegram:telegrambots-client:latest.release")
	embed("org.sejda.imageio:webp-imageio:latest.release")

	implementation("com.google.code.gson:gson:latest.release")
	implementation("org.apache.httpcomponents.client5:httpclient5:latest.release")
	implementation("net.lingala.zip4j:zip4j:latest.release")
	implementation("net.dv8tion:JDA:latest.release")
	implementation("org.telegram:telegrambots-longpolling:latest.release")
	implementation("org.telegram:telegrambots-client:latest.release")
	implementation("org.sejda.imageio:webp-imageio:latest.release")
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

application {
	mainClass = "com.github.hummel.vadim.MainKt"
}

tasks {
	named<JavaExec>("run") {
		standardInput = System.`in`
	}
	jar {
		manifest {
			attributes(
				mapOf(
					"Main-Class" to "com.github.hummel.vadim.MainKt"
				)
			)
		}
		from(embed.map {
			if (it.isDirectory) it else zipTree(it)
		})
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	}
}