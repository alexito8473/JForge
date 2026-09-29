package jforge.engine;

public class GameLoop implements Runnable {

    private static final double FPS = 60.0;

    private static final double FRAME_TIME =
            1_000_000_000.0 / FPS;

    private boolean running;

    private Thread thread;

    public void start() {

        if (running) {
            return;
        }

        running = true;

        thread = new Thread(
                this,
                "JForge-GameLoop"
        );

        thread.start();
    }

    public void stop() {

        running = false;
    }

    @Override
    public void run() {

        System.out.println(
                "Game Loop iniciado."
        );

        long lastTime =
                System.nanoTime();

        double accumulator = 0;

        while (running) {

            long currentTime =
                    System.nanoTime();

            long elapsed =
                    currentTime - lastTime;

            lastTime = currentTime;

            accumulator += elapsed;

            while (accumulator >= FRAME_TIME) {

                update();

                accumulator -= FRAME_TIME;
            }

            render();
        }

        System.out.println(
                "Game Loop detenido."
        );
    }

    private void update() {

    }

    private void render() {

    }
}