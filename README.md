# Vidar 

Vidar is a Java library designed to simplify interactions with Docker Compose projects. Its primary purpose is to add, remove, and configure services that include their own Docker Compose files, integrating them into a larger project.

Please note: Vidar was developed for a personal project and is not intended to fully replicate all Docker Compose features. It currently provides a limited feature set and may not cover the full functionality of Docker Compose in the near future.

## Installation
Use Gradle or Maven with a jitpack repository to install Vidar.
<details>
<summary>Gradle(.kts)</summary>

```kotlin
// Add Jitpack repository
repositories {
    maven("https://jitpack.io")
}
```
```kotlin
// Add Vidar dependency
dependencies {
    implementation("com.github.DominicMoser:Vidar:1.0")
}
```
</details>
<details>
<summary>Gradle Groovy DSL</summary>

```groovy
//Add Jitpack repository
repositories {
maven { url 'https://jitpack.io' }
}
```
```groovy
// Add Vidar dependency
dependencies {
    implementation 'com.github.DominicMoser:Vidar:1.0'
}
```
</details>
<details>
<summary>Maven</summary>

```xml
<!-- Add Jitpack repository -->
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
```xml
<!-- Add Vidar dependency -->
<dependencies>
    <dependency>
        <groupId>com.github.DominicMoser</groupId>
        <artifactId>Vidar</artifactId>
        <version>1.0</version>
    </dependency>
</dependencies>
```
</details>



## Contributing

Pull requests are welcome. For major changes, please open an issue first
to discuss what you would like to change.

Please make sure to update tests as appropriate.

Please make sure to comply with the [conventionalcommits](https://www.conventionalcommits.org/en/v1.0.0/) specification.
A `git-conventional-commits.yaml` file is provided for use with the `commit-msg` hook provided by https://github.com/qoomon/git-conventional-commits 


## License
[MIT](https://choosealicense.com/licenses/mit/)