import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

if (args.length != 1) {
    System.err.println("Usage: updateDocVersions <new-version>");
    return;
}

def version = args[0]

Files.walk(Paths.get(".")).forEach({ f ->
    def modifiedText = null;
    def text = null;
    if (f.toString().endsWith(".md")) {
        // examples inside .md
        text = new String(Files.readAllBytes(f), StandardCharsets.UTF_8);
        modifiedText = text.replaceAll("<version>[^<]*</version>", (String) ("<version>" + version + "</version>"))
        modifiedText = modifiedText.replaceAll("ea-async-[.0-9a-zA-Z_-]+[.]jar", (String) ("ea-async-" + version + ".jar"))
    }
    if (f.toString().matches(".*project-to-test[/\\\\]pom[.]xml\$")) {
        // sample project
        text = new String(Files.readAllBytes(f), StandardCharsets.UTF_8);
        modifiedText = text.replaceAll("(<artifactId>ea-async(-maven-plugin)?</artifactId>\\s*<version)>[^<]*(</version>)",
                (String) ("\$1>" + version + "\$3"))
    }
    if (modifiedText != null && !text.equals(modifiedText)) {
        println f
        Files.write(f, modifiedText.getBytes(StandardCharsets.UTF_8));
    }
})

// this script will run during deployment
//mvn versions:set -DnewVersion=newVer
//mvn clean install
//mvn deploy -PstageRelease -DskipTests=true  <--- here
//git add .
//git commit -m "Version newVer"
//git tag -a vnewVer -m "Version newVer"
//git push master --tags
//mvn versions:set -DnewVersion=newVer+1-SNAPSHOT

