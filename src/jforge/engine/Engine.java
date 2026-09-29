package jforge.engine;
public class Engine {

    private boolean running;

    private final GameLoop gameLoop;

    public Engine() {

        running = false;

        gameLoop = new GameLoop();
    }

    public void start() {

        if (running) {
            return;
        }

        running = true;

        System.out.println(
                "JForge Engine iniciado."
        );

        gameLoop.start();
    }

    public void stop() {

        if (!running) {
            return;
        }

        running = false;

        gameLoop.stop();

        System.out.println(
                "JForge Engine detenido."
        );
    }

    public boolean isRunning() {

        return running;
    }
}