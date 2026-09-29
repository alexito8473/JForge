package jforge.engine.debug;

public class DebugStats {

    private int fps;
    private double frameTime;
    private long totalFrames;

    private int objects;
    private int triangles;

    public DebugStats() {

        fps = 0;
        frameTime = 0;
        totalFrames = 0;

        objects = 0;
        triangles = 0;
    }

    public void setFPS(int fps) {
        this.fps = fps;
    }

    public void setFrameTime(
            double frameTime) {

        this.frameTime =
                frameTime;
    }

    public void setTotalFrames(
            long totalFrames) {

        this.totalFrames =
                totalFrames;
    }

    public void setObjects(int objects) {
        this.objects = objects;
    }

    public void setTriangles(int triangles) {
        this.triangles = triangles;
    }

    public int getFPS() {
        return fps;
    }

    public double getFrameTime() {
        return frameTime;
    }

    public long getTotalFrames() {
        return totalFrames;
    }

    public int getObjects() {
        return objects;
    }

    public int getTriangles() {
        return triangles;
    }
}