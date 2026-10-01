package jforge.engine.rendering.gpu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class GPUManager {

    private final List<GPUInfo> gpus = new ArrayList<>();

    private GPURenderConfig configuration;

    public GPUManager() {
        refresh();
    }

    public synchronized void refresh() {
        gpus.clear();

        String os = System.getProperty("os.name", "").toLowerCase();

        if (os.contains("win")) {
            detectWindows();
        } else if (os.contains("linux")) {
            detectLinux();
        } else if (os.contains("mac")) {
            detectMacOS();
        }

        removeDuplicates();

        if (gpus.size() == 1) {
            configuration = GPURenderConfig.single(0);
        } else {
            configuration = GPURenderConfig.auto();
        }

        System.out.println();
        System.out.println("========== JForge GPU Manager ==========");

        if (gpus.isEmpty()) {
            System.out.println("No se han podido detectar GPUs mediante el sistema operativo.");
        } else {
            for (GPUInfo gpu : gpus) {
                System.out.println(
                        "GPU " + gpu.getIndex() +
                                ": " + gpu.getVendor() +
                                " " + gpu.getName() +
                                " | VRAM: " + gpu.getMemoryMB() + " MB" +
                                " | Driver: " + gpu.getDriverVersion()
                );
            }
        }

        System.out.println("Configuración inicial: " + configuration);
        System.out.println("=========================================");
        System.out.println();
    }

    public synchronized List<GPUInfo> getGPUs() {
        return new ArrayList<>(gpus);
    }

    public synchronized int getGPUCount() {
        return gpus.size();
    }

    public synchronized GPUInfo getGPU(int index) {
        for (GPUInfo gpu : gpus) {
            if (gpu.getIndex() == index) {
                return gpu;
            }
        }

        return null;
    }

    public synchronized GPURenderConfig getConfiguration() {
        if (configuration == null) {
            configuration = GPURenderConfig.auto();
        }

        return configuration.copy();
    }

    public synchronized void setConfiguration(GPURenderConfig newConfiguration) {

        if (newConfiguration == null) {
            configuration = GPURenderConfig.auto();
            return;
        }

        configuration = normalizeConfiguration(newConfiguration);
    }

    public synchronized boolean supportsMultiGPU() {
        return gpus.size() >= 2;
    }

    public synchronized boolean containsGPU(int index) {
        return getGPU(index) != null;
    }

    public synchronized GPURenderConfig createAutomaticConfiguration() {
        return GPURenderConfig.auto();
    }

    public synchronized GPURenderConfig createSingleConfiguration(int gpuIndex) {
        if (!containsGPU(gpuIndex)) {
            throw new IllegalArgumentException(
                    "La GPU " + gpuIndex + " no existe."
            );
        }

        return GPURenderConfig.single(gpuIndex);
    }

    public synchronized GPURenderConfig createMultiConfiguration(
            int[] gpuIndices,
            GPUMultiStrategy strategy
    ) {
        if (gpuIndices == null || gpuIndices.length < 2) {
            throw new IllegalArgumentException(
                    "El modo multi-GPU requiere al menos 2 GPUs."
            );
        }

        for (int index : gpuIndices) {
            if (!containsGPU(index)) {
                throw new IllegalArgumentException(
                        "La GPU " + index + " no existe."
                );
            }
        }

        return GPURenderConfig.multi(gpuIndices, strategy);
    }

    private GPURenderConfig normalizeConfiguration(
            GPURenderConfig source
    ) {

        GPUSelectionMode mode = source.getSelectionMode();

        if (mode == GPUSelectionMode.AUTO) {
            return GPURenderConfig.auto();
        }

        int[] indices = source.getSelectedGpuIndices();

        if (mode == GPUSelectionMode.SINGLE_GPU) {

            if (indices.length == 0) {
                if (!gpus.isEmpty()) {
                    return GPURenderConfig.single(gpus.get(0).getIndex());
                }

                return GPURenderConfig.auto();
            }

            if (!containsGPU(indices[0])) {
                throw new IllegalArgumentException(
                        "La GPU seleccionada no existe."
                );
            }

            return new GPURenderConfig(
                    GPUSelectionMode.SINGLE_GPU,
                    new int[]{indices[0]},
                    source.getMultiStrategy(),
                    source.isAllowFallback()
            );
        }

        if (mode == GPUSelectionMode.MULTI_GPU) {

            if (gpus.size() < 2) {
                throw new IllegalArgumentException(
                        "No hay suficientes GPUs para utilizar multi-GPU."
                );
            }

            List<Integer> valid = new ArrayList<>();

            for (int index : indices) {
                if (containsGPU(index) && !valid.contains(index)) {
                    valid.add(index);
                }
            }

            if (valid.size() < 2) {
                throw new IllegalArgumentException(
                        "Debes seleccionar al menos 2 GPUs."
                );
            }

            int[] validIndices = new int[valid.size()];

            for (int i = 0; i < valid.size(); i++) {
                validIndices[i] = valid.get(i);
            }

            return new GPURenderConfig(
                    GPUSelectionMode.MULTI_GPU,
                    validIndices,
                    source.getMultiStrategy(),
                    source.isAllowFallback()
            );
        }

        return GPURenderConfig.auto();
    }

    private void detectWindows() {

        String command =
                "Get-CimInstance Win32_VideoController | " +
                        "ForEach-Object { " +
                        "'{0}|||{1}|||{2}|||{3}' -f " +
                        "$_.Name, " +
                        "$_.AdapterRAM, " +
                        "$_.DriverVersion, " +
                        "$_.PNPDeviceID " +
                        "}";

        List<String> output = runCommand(
                List.of(
                        "powershell.exe",
                        "-NoProfile",
                        "-ExecutionPolicy",
                        "Bypass",
                        "-Command",
                        command
                )
        );

        int index = 0;

        for (String line : output) {

            if (line == null || line.isBlank()) {
                continue;
            }

            String[] parts = line.split("\\|\\|\\|", -1);

            if (parts.length < 4) {
                continue;
            }

            String name = clean(parts[0]);
            String memoryRaw = clean(parts[1]);
            String driver = clean(parts[2]);

            if (name.isBlank()) {
                continue;
            }

            long memoryMB = parseMemoryMB(memoryRaw);
            String vendor = detectVendor(name);

            boolean discrete = isDiscreteGPU(name);

            gpus.add(
                    new GPUInfo(
                            index++,
                            name,
                            vendor,
                            driver,
                            memoryMB,
                            discrete,
                            "Windows"
                    )
            );
        }
    }

    private void detectLinux() {

        List<String> output = runCommand(
                List.of(
                        "sh",
                        "-c",
                        "lspci -Dnn | grep -Ei " +
                                "'VGA compatible controller|3D controller|Display controller'"
                )
        );

        int index = 0;

        for (String line : output) {

            if (line == null || line.isBlank()) {
                continue;
            }

            int colon = line.indexOf(':');

            if (colon < 0 || colon + 1 >= line.length()) {
                continue;
            }

            String description = line.substring(colon + 1).trim();

            if (description.isBlank()) {
                continue;
            }

            String vendor = detectVendor(description);

            gpus.add(
                    new GPUInfo(
                            index++,
                            description,
                            vendor,
                            "Unknown",
                            0,
                            isDiscreteGPU(description),
                            "Linux/lspci"
                    )
            );
        }
    }

    private void detectMacOS() {

        List<String> output = runCommand(
                List.of(
                        "system_profiler",
                        "SPDisplaysDataType"
                )
        );

        int index = 0;

        for (String line : output) {

            if (line == null) {
                continue;
            }

            String trimmed = line.trim();

            if (!trimmed.startsWith("Chipset Model:")) {
                continue;
            }

            String name = trimmed
                    .substring("Chipset Model:".length())
                    .trim();

            if (name.isBlank()) {
                continue;
            }

            gpus.add(
                    new GPUInfo(
                            index++,
                            name,
                            detectVendor(name),
                            "Unknown",
                            0,
                            isDiscreteGPU(name),
                            "macOS/system_profiler"
                    )
            );
        }
    }

    private void removeDuplicates() {

        Set<String> seen = new HashSet<>();

        gpus.removeIf(gpu -> {

            String key = (
                    gpu.getVendor() +
                            "|" +
                            gpu.getName()
            ).toLowerCase();

            if (!seen.add(key)) {
                return true;
            }

            return false;
        });

        gpus.sort(Comparator.comparingInt(GPUInfo::getIndex));

        for (int i = 0; i < gpus.size(); i++) {

            GPUInfo original = gpus.get(i);

            gpus.set(
                    i,
                    new GPUInfo(
                            i,
                            original.getName(),
                            original.getVendor(),
                            original.getDriverVersion(),
                            original.getMemoryMB(),
                            original.isDiscrete(),
                            original.getDetectionSource()
                    )
            );
        }
    }

    private List<String> runCommand(List<String> command) {

        List<String> result = new ArrayList<>();

        try {

            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();

            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return result;
            }

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

                while ((line = reader.readLine()) != null) {
                    result.add(line);
                }
            }

        } catch (IOException | InterruptedException e) {

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            System.out.println(
                    "No se pudo ejecutar detección de GPUs: " +
                            e.getMessage()
            );
        }

        return result;
    }

    private String clean(String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replace("\u0000", "");
    }

    private long parseMemoryMB(String value) {

        if (value == null || value.isBlank()) {
            return 0;
        }

        try {

            long bytes = Long.parseLong(value.trim());

            return bytes / (1024L * 1024L);

        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String detectVendor(String name) {

        String value = name.toLowerCase();

        if (value.contains("nvidia")) {
            return "NVIDIA";
        }

        if (value.contains("amd") ||
                value.contains("ati") ||
                value.contains("radeon")) {
            return "AMD";
        }

        if (value.contains("intel")) {
            return "Intel";
        }

        if (value.contains("apple")) {
            return "Apple";
        }

        if (value.contains("microsoft")) {
            return "Microsoft";
        }

        return "Unknown";
    }

    private boolean isDiscreteGPU(String name) {

        String value = name.toLowerCase();

        if (value.contains("microsoft basic")) {
            return false;
        }

        if (value.contains("geforce")) {
            return true;
        }

        if (value.contains("rtx")) {
            return true;
        }

        if (value.contains("quadro")) {
            return true;
        }

        if (value.contains("tesla")) {
            return true;
        }

        if (value.contains("radeon rx")) {
            return true;
        }

        if (value.contains("radeon pro")) {
            return true;
        }

        if (value.contains("arc")) {
            return true;
        }

        return false;
    }
}