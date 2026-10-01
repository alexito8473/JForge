package jforge.engine.debug;

public class FPSCounter {

    private long frames;
    private long totalFrames;

    private int fps;

    private long lastTime;

    public FPSCounter() {

        frames = 0;
        totalFrames = 0;
        fps = 0;

        lastTime = System.nanoTime();
    }

    public void update() {

        frames++;
        totalFrames++;

        long currentTime = System.nanoTime();
        long elapsed = currentTime - lastTime;
        if (elapsed >= 1_000_000_000L) {
            fps = (int) frames;
            frames = 0;
            lastTime = currentTime;
        }
    }

    public void frameRendered() {
        update();
    }

    public int getFPS() {
        return fps;
    }

    public long getTotalFrames() {
        return totalFrames;
    }
}