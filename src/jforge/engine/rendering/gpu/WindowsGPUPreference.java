package jforge.engine.rendering.gpu;

import java.io.IOException;
import java.nio.file.Path;

public final class WindowsGPUPreference {

    private WindowsGPUPreference() {
    }

    public static boolean isWindows() {
        return System.getProperty("os.name", "")
                .toLowerCase()
                .contains("win");
    }

    public static Path getCurrentJavaExecutable() {
        String command = ProcessHandle.current()
                .info()
                .command()
                .orElse(null);

        if (command == null || command.isBlank()) {
            return null;
        }

        return Path.of(command).toAbsolutePath().normalize();
    }

    public static boolean setHighPerformanceForCurrentJava() {

        if (!isWindows()) {
            return false;
        }

        Path executable = getCurrentJavaExecutable();

        if (executable == null) {
            return false;
        }

        String path = executable.toString();

        String key =
                "HKCU\\Software\\Microsoft\\DirectX\\UserGpuPreferences";

        try {

            Process process = new ProcessBuilder(
                    "reg",
                    "add",
                    key,
                    "/v",
                    path,
                    "/t",
                    "REG_SZ",
                    "/d",
                    "GpuPreference=2;",
                    "/f"
            )
                    .redirectErrorStream(true)
                    .start();

            int exitCode = process.waitFor();

            return exitCode == 0;

        } catch (IOException e) {

            System.err.println(
                    "[GPU] No se pudo configurar Windows: " +
                            e.getMessage()
            );

            return false;

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            return false;
        }
    }
}