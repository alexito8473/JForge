package jforge.engine.runtime;

import java.io.File;

public class GameMain {

    public static void main(String[] args) {

        try {

            File sceneFile;

            if (args.length > 0) {

                sceneFile = new File(args[0]);

            } else {

                sceneFile = new File(
                        getApplicationDirectory(),
                        "game.jscene"
                );
            }

            System.out.println(
                    "[JForge Runtime] Cargando escena:"
            );

            System.out.println(
                    sceneFile.getAbsolutePath()
            );

            if (!sceneFile.isFile()) {

                throw new RuntimeException(
                        "No se encontró game.jscene:\n" +
                                sceneFile.getAbsolutePath()
                );
            }

            LoadedScene loadedScene =
                    SceneSerializer.load(sceneFile);

            GameRuntime runtime =
                    new GameRuntime(loadedScene);

            runtime.start();

        } catch (Exception e) {

            e.printStackTrace();

            System.exit(1);
        }
    }

    private static File getApplicationDirectory() {

        try {

            File location =
                    new File(
                            GameMain.class
                                    .getProtectionDomain()
                                    .getCodeSource()
                                    .getLocation()
                                    .toURI()
                    );

            /*
             * Cuando se ejecuta desde un JAR:
             *
             * MiJuego/
             * ├── MiJuego.exe
             * ├── app/
             * └── runtime/
             *
             * El GameMain.class estará dentro del JAR.
             */
            if (location.isFile()) {

                File parent =
                        location.getParentFile();

                if (parent != null) {
                    return parent;
                }
            }

            /*
             * Cuando se está ejecutando desde IntelliJ:
             * target/classes
             */
            if (location.isDirectory()) {

                File current = location;

                while (current != null) {

                    File scene =
                            new File(
                                    current,
                                    "game.jscene"
                            );

                    if (scene.isFile()) {
                        return current;
                    }

                    current =
                            current.getParentFile();
                }
            }

            return new File(
                    System.getProperty(
                            "user.dir"
                    )
            );

        } catch (Exception e) {

            return new File(
                    System.getProperty(
                            "user.dir"
                    )
            );
        }
    }
}