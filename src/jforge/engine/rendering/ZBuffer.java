package jforge.engine.rendering;

public class ZBuffer {

    private double[] depth;

    private int width;

    private int height;

    public ZBuffer(
            int width,
            int height) {

        resize(width, height);
    }

    public void resize(
            int width,
            int height) {

        this.width = Math.max(1, width);

        this.height = Math.max(1, height);

        depth =
                new double[
                        this.width *
                                this.height
                        ];

        clear();
    }

    public void clear() {

        for (int i = 0; i < depth.length; i++) {

            depth[i] =
                    Double.POSITIVE_INFINITY;
        }
    }

    public boolean testAndSet(
            int x,
            int y,
            double value) {

        if (
                x < 0 ||
                        y < 0 ||
                        x >= width ||
                        y >= height
        ) {

            return false;
        }

        int index =
                y * width + x;

        if (value < depth[index]) {

            depth[index] = value;

            return true;
        }

        return false;
    }
}