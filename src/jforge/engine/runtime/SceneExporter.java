package jforge.engine.runtime;

import jforge.engine.Scene;
import jforge.engine.rendering.camera.Camera;
import jforge.engine.rendering.RenderBackend;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipFile;

public final class SceneExporter {

    private SceneExporter() {
    }

    /**
     * Exporta únicamente la escena y sus recursos.
     */
    public static File export(
            Scene scene,
            Camera camera,
            RenderBackend backend,
            File outputDirectory
    ) throws IOException {

        if (scene == null) {
            throw new IllegalArgumentException(
                    "Scene no puede ser null."
            );
        }

        if (camera == null) {
            throw new IllegalArgumentException(
                    "Camera no puede ser null."
            );
        }

        if (outputDirectory == null) {
            throw new IllegalArgumentException(
                    "Output directory no puede ser null."
            );
        }

        Files.createDirectories(
                outputDirectory.toPath()
        );

        Map<String, String> textureMap =
                new HashMap<>();

        File texturesDirectory =
                new File(
                        outputDirectory,
                        "assets/textures"
                );

        Files.createDirectories(
                texturesDirectory.toPath()
        );

        File sceneFile =
                new File(
                        outputDirectory,
                        "game.jscene"
                );

        SceneSerializer.save(
                scene,
                camera,
                backend,
                sceneFile,
                textureFile -> {

                    if (textureFile == null ||
                            !textureFile.isFile()) {

                        return null;
                    }

                    try {

                        String absoluteSource =
                                textureFile
                                        .getCanonicalPath();

                        String existing =
                                textureMap.get(
                                        absoluteSource
                                );

                        if (existing != null) {
                            return existing;
                        }

                        String safeName =
                                createSafeFileName(
                                        textureFile
                                );

                        File destination =
                                new File(
                                        texturesDirectory,
                                        safeName
                                );

                        Files.copy(
                                textureFile.toPath(),
                                destination.toPath(),
                                StandardCopyOption.REPLACE_EXISTING
                        );

                        String relative =
                                "assets/textures/" +
                                        safeName;

                        textureMap.put(
                                absoluteSource,
                                relative
                        );

                        return relative;

                    } catch (IOException e) {

                        System.err.println(
                                "[Export] No se pudo copiar textura: " +
                                        textureFile
                        );

                        return null;
                    }
                }
        );

        return sceneFile;
    }

    /**
     * Exporta la escena y genera el ejecutable Windows.
     *
     * No depende de Maven.
     *
     * Utiliza:
     *
     * - las clases con las que JForge se está ejecutando
     * - los JAR que ya están en el classpath
     * - jpackage del JDK
     */
    public static File exportExecutable(
            Scene scene,
            Camera camera,
            RenderBackend backend,
            File outputDirectory,
            String gameName
    ) throws IOException {

        if (!isWindows()) {

            throw new IOException(
                    "La exportación .exe solo está disponible en Windows."
            );
        }

        if (!isJPackageAvailable()) {

            throw new IOException(
                    "No se encontró jpackage.\n\n" +
                            "JForge necesita ejecutarse con un JDK que incluya jpackage."
            );
        }

        String applicationName =
                sanitizeApplicationName(gameName);

        if (applicationName.isBlank()) {
            applicationName = "JForgeGame";
        }

        Files.createDirectories(
                outputDirectory.toPath()
        );

        Path temporaryDirectory =
                Files.createTempDirectory(
                        "jforge-export-"
                );

        try {

            File inputDirectory =
                    temporaryDirectory.toFile();

            System.out.println(
                    "[Export] Preparando escena..."
            );

            /*
             * 1. Escena + texturas.
             */
            export(
                    scene,
                    camera,
                    backend,
                    inputDirectory
            );

            /*
             * 2. Preparar JForge Runtime directamente
             * desde el classpath actual.
             */
            File mainJar =
                    prepareRuntime(
                            inputDirectory
                    );

            /*
             * 3. Preparar salida.
             */
            File applicationDirectory =
                    new File(
                            outputDirectory,
                            applicationName
                    );

            deleteDirectory(
                    applicationDirectory.toPath()
            );

            /*
             * 4. Ejecutar jpackage.
             */
            Path jpackage =
                    findJPackage();

            List<String> command =
                    new ArrayList<>();

            command.add(
                    jpackage.toString()
            );

            command.add("--type");
            command.add("app-image");

            command.add("--name");
            command.add(applicationName);

            command.add("--input");
            command.add(
                    inputDirectory
                            .getAbsolutePath()
            );

            command.add("--main-jar");
            command.add(
                    mainJar.getName()
            );

            command.add("--main-class");
            command.add(
                    GameMain.class.getName()
            );

            command.add("--dest");
            command.add(
                    outputDirectory
                            .getAbsolutePath()
            );

            command.add("--app-version");
            command.add("1.0.0");

            command.add("--vendor");
            command.add("JForge");

            /*
             * Permite que LWJGL cargue librerías nativas.
             */
            command.add("--java-options");
            command.add(
                    "--enable-native-access=ALL-UNNAMED"
            );

            System.out.println();
            System.out.println(
                    "[Export] Ejecutando jpackage..."
            );
            System.out.println(
                    String.join(
                            " ",
                            command
                    )
            );
            System.out.println();

            ProcessResult processResult =
                    runProcess(command);

            if (processResult.exitCode != 0) {

                throw new IOException(
                        "jpackage terminó con error.\n\n" +
                                processResult.output
                );
            }

            File executable =
                    new File(
                            applicationDirectory,
                            applicationName + ".exe"
                    );

            if (!executable.isFile()) {

                throw new IOException(
                        "jpackage terminó pero no se encontró el EXE:\n" +
                                executable.getAbsolutePath()
                );
            }

            System.out.println();
            System.out.println(
                    "[Export] EXE creado correctamente:"
            );
            System.out.println(
                    executable.getAbsolutePath()
            );
            System.out.println();

            return executable;

        } finally {

            deleteDirectory(
                    temporaryDirectory
            );
        }
    }

    /**
     * Crea el JAR principal usando las clases con las
     * que se está ejecutando actualmente JForge.
     */
    private static File prepareRuntime(
            File inputDirectory
    ) throws IOException {

        System.out.println(
                "[Export] Preparando runtime desde las clases compiladas..."
        );

        /*
         * Usamos GameMain directamente.
         *
         * Esto obliga a que GameMain forme parte del
         * mismo conjunto compilado que JForge.
         */
        Class<?> mainClass = GameMain.class;

        File codeSource;

        try {

            codeSource =
                    new File(
                            mainClass
                                    .getProtectionDomain()
                                    .getCodeSource()
                                    .getLocation()
                                    .toURI()
                    );

        } catch (Exception e) {

            throw new IOException(
                    "No se pudo localizar GameMain.class.",
                    e
            );
        }

        File mainJar =
                new File(
                        inputDirectory,
                        "JForgeRuntime.jar"
                );

        if (codeSource.isDirectory()) {

            File gameMainClass =
                    new File(
                            codeSource,
                            "jforge/engine/runtime/GameMain.class"
                    );

            if (!gameMainClass.isFile()) {

                throw new IOException(
                        "GameMain.class no está compilado.\n\n" +
                                "Ubicación esperada:\n" +
                                gameMainClass.getAbsolutePath() +
                                "\n\n" +
                                "Haz Build > Rebuild Project en IntelliJ."
                );
            }

            createJarFromDirectory(
                    codeSource,
                    mainJar
            );

        } else {

            /*
             * JForge ya se está ejecutando desde un JAR.
             */
            Files.copy(
                    codeSource.toPath(),
                    mainJar.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        /*
         * Copiar dependencias reales del classpath.
         */
        copyClasspathJars(
                inputDirectory,
                mainJar
        );

        if (!containsGameMain(mainJar)) {

            throw new IOException(
                    "Se creó el JAR, pero GameMain.class no quedó dentro.\n\n" +
                            "JAR:\n" +
                            mainJar.getAbsolutePath()
            );
        }

        System.out.println(
                "[Export] GameMain encontrado correctamente."
        );

        System.out.println(
                "[Export] Runtime preparado:"
        );

        System.out.println(
                mainJar.getAbsolutePath()
        );

        return mainJar;
    }

    /**
     * Obtiene la ubicación real desde la que se cargó JForge.
     */
    private static File getCodeSource() {

        try {

            return new File(
                    SceneExporter.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );

        } catch (Exception e) {

            return null;
        }
    }

    /**
     * Copia todos los JAR que ya están en el classpath.
     *
     * Esto sustituye la necesidad de ejecutar Maven.
     */
    private static void copyClasspathJars(
            File inputDirectory,
            File mainJar
    ) throws IOException {

        String classPath =
                System.getProperty(
                        "java.class.path"
                );

        if (classPath == null ||
                classPath.isBlank()) {

            System.out.println(
                    "[Export] No hay classpath externo que copiar."
            );

            return;
        }

        String[] entries =
                classPath.split(
                        java.util.regex.Pattern.quote(
                                File.pathSeparator
                        )
                );

        int copied = 0;

        for (String entry : entries) {

            if (entry == null ||
                    entry.isBlank()) {

                continue;
            }

            File file =
                    new File(entry);

            if (!file.isFile()) {
                continue;
            }

            if (!file.getName()
                    .toLowerCase()
                    .endsWith(".jar")) {

                continue;
            }

            /*
             * No copiamos el mismo JAR principal.
             */
            try {

                if (file.getCanonicalFile()
                        .equals(
                                mainJar.getCanonicalFile()
                        )) {

                    continue;
                }

            } catch (IOException ignored) {
            }

            /*
             * No queremos sources ni javadocs.
             */
            String lower =
                    file.getName()
                            .toLowerCase();

            if (lower.contains("-sources") ||
                    lower.contains("-javadoc")) {

                continue;
            }

            File destination =
                    new File(
                            inputDirectory,
                            file.getName()
                    );

            try {

                Files.copy(
                        file.toPath(),
                        destination.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );

                copied++;

                System.out.println(
                        "[Export] Dependencia: " +
                                file.getName()
                );

            } catch (IOException e) {

                throw new IOException(
                        "No se pudo copiar la dependencia:\n" +
                                file.getAbsolutePath(),
                        e
                );
            }
        }

        System.out.println(
                "[Export] Dependencias copiadas: " +
                        copied
        );
    }

    /**
     * Convierte target/classes en JAR.
     */
    private static void createJarFromDirectory(
            File sourceDirectory,
            File targetJar
    ) throws IOException {

        if (!sourceDirectory.isDirectory()) {

            throw new IOException(
                    "No existe el directorio de clases:\n" +
                            sourceDirectory
                                    .getAbsolutePath()
            );
        }

        Path root =
                sourceDirectory.toPath();

        try (
                JarOutputStream jarOutput =
                        new JarOutputStream(
                                Files.newOutputStream(
                                        targetJar.toPath()
                                )
                        )
        ) {

            Files.walk(root)
                    .filter(
                            Files::isRegularFile
                    )
                    .forEach(
                            file -> {

                                String entryName =
                                        root.relativize(file)
                                                .toString()
                                                .replace(
                                                        File.separatorChar,
                                                        '/'
                                                );

                                try {

                                    JarEntry entry =
                                            new JarEntry(
                                                    entryName
                                            );

                                    jarOutput.putNextEntry(
                                            entry
                                    );

                                    Files.copy(
                                            file,
                                            jarOutput
                                    );

                                    jarOutput.closeEntry();

                                } catch (IOException e) {

                                    throw new UncheckedIOException(
                                            e
                                    );
                                }
                            }
                    );
        } catch (UncheckedIOException e) {

            throw e.getCause();
        }
    }

    private static boolean containsGameMain(
            File jar
    ) {

        try (
                ZipFile zipFile =
                        new ZipFile(jar)
        ) {

            return zipFile.getEntry(
                    "jforge/engine/runtime/GameMain.class"
            ) != null;

        } catch (IOException e) {

            return false;
        }
    }

    private static Path findJPackage()
            throws IOException {

        String javaHome =
                System.getProperty(
                        "java.home"
                );

        Path home =
                Path.of(javaHome);

        Path direct =
                home.resolve(
                        "bin"
                ).resolve(
                        "jpackage.exe"
                );

        if (Files.isRegularFile(direct)) {
            return direct;
        }

        /*
         * Si estamos en un JRE dentro de un JDK.
         */
        Path parent =
                home.getParent();

        if (parent != null) {

            Path parentJPackage =
                    parent.resolve(
                            "bin"
                    ).resolve(
                            "jpackage.exe"
                    );

            if (Files.isRegularFile(
                    parentJPackage
            )) {

                return parentJPackage;
            }
        }

        /*
         * Último intento usando PATH.
         */
        return Path.of(
                "jpackage.exe"
        );
    }

    private static boolean isJPackageAvailable() {

        try {

            Path path =
                    findJPackage();

            if (Files.isRegularFile(path)) {
                return true;
            }

            Process process =
                    new ProcessBuilder(
                            "jpackage.exe",
                            "--version"
                    )
                            .redirectErrorStream(true)
                            .start();

            int exit =
                    process.waitFor();

            return exit == 0;

        } catch (
                IOException |
                InterruptedException e
        ) {

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            return false;
        }
    }

    private static ProcessResult runProcess(
            List<String> command
    ) throws IOException {

        Process process =
                new ProcessBuilder(command)
                        .redirectErrorStream(true)
                        .start();

        StringBuilder output =
                new StringBuilder();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        process.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while (
                    (line = reader.readLine()) != null
            ) {

                output.append(line);
                output.append(
                        System.lineSeparator()
                );

                System.out.println(
                        "[jpackage] " +
                                line
                );
            }

        } catch (IOException e) {

            process.destroyForcibly();

            throw e;
        }

        try {

            int exitCode =
                    process.waitFor();

            return new ProcessResult(
                    exitCode,
                    output.toString()
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            process.destroyForcibly();

            throw new IOException(
                    "jpackage fue interrumpido.",
                    e
            );
        }
    }

    private static boolean isWindows() {

        return System.getProperty(
                        "os.name",
                        ""
                )
                .toLowerCase()
                .contains("win");
    }

    private static String sanitizeApplicationName(
            String name
    ) {

        if (name == null) {
            return "JForgeGame";
        }

        String result =
                name.trim()
                        .replaceAll(
                                "[^a-zA-Z0-9._-]",
                                "_"
                        );

        if (result.isBlank()) {
            return "JForgeGame";
        }

        return result;
    }

    private static String createSafeFileName(
            File file
    ) {

        String name =
                file.getName();

        int extensionIndex =
                name.lastIndexOf('.');

        String base =
                extensionIndex > 0
                        ? name.substring(
                        0,
                        extensionIndex
                )
                        : name;

        String extension =
                extensionIndex > 0
                        ? name.substring(
                        extensionIndex
                )
                        : "";

        String hash =
                Integer.toHexString(
                        file.getAbsolutePath()
                                .hashCode()
                );

        return base +
                "_" +
                hash +
                extension;
    }

    private static void deleteDirectory(
            Path path
    ) {

        if (path == null ||
                !Files.exists(path)) {

            return;
        }

        try {

            Files.walk(path)
                    .sorted(
                            (a, b) ->
                                    b.compareTo(a)
                    )
                    .forEach(
                            current -> {

                                try {

                                    Files.deleteIfExists(
                                            current
                                    );

                                } catch (IOException e) {

                                    System.err.println(
                                            "[Export] No se pudo borrar: " +
                                                    current
                                    );
                                }
                            }
                    );

        } catch (IOException e) {

            System.err.println(
                    "[Export] No se pudo limpiar temporal: " +
                            e.getMessage()
            );
        }
    }

    private static class ProcessResult {

        private final int exitCode;
        private final String output;

        private ProcessResult(
                int exitCode,
                String output
        ) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }
}