package jforge.engine.debug;

public class RenderStats {

    private double frameTime;
    private double clearTime;
    private double renderTime;

    private int fps;
    private long totalFrames;

    private int objects;
    private int triangles;

    public void setFrameTime(double value) {
        frameTime = value;
    }

    public void setClearTime(double value) {
        clearTime = value;
    }

    public void setRenderTime(double value) {
        renderTime = value;
    }

    public void setFPS(int value) {
        fps = value;
    }

    public void setTotalFrames(long value) {
        totalFrames = value;
    }

    public void setObjects(int value) {
        objects = value;
    }

    public void setTriangles(int value) {
        triangles = value;
    }

    public double getFrameTime() {
        return frameTime;
    }

    public double getClearTime() {
        return clearTime;
    }

    public double getRenderTime() {
        return renderTime;
    }

    public int getFPS() {
        return fps;
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