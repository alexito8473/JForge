package jforge.engine.rendering.camera;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

    public class CameraController
            implements KeyListener {

        private Camera camera;

        private boolean forward;
        private boolean backward;

        private boolean left;
        private boolean right;

        private boolean up;
        private boolean down;

        private boolean rotateLeft;
        private boolean rotateRight;

        private boolean rotateUp;
        private boolean rotateDown;

        private double moveSpeed;
        private double rotationSpeed;

        public CameraController(
                Camera camera) {

            this.camera =
                    camera;

            moveSpeed =
                    5.0;

            rotationSpeed =
                    90.0;
        }

        public void update(
                double deltaTime) {

            double movement =
                    moveSpeed *
                            deltaTime;

            double rotation =
                    rotationSpeed *
                            deltaTime;

            /*
             * =========================
             * MOVIMIENTO
             * =========================
             */

            if (forward) {

                camera.moveForward(
                        movement
                );
            }

            if (backward) {

                camera.moveForward(
                        -movement
                );
            }

            if (left) {

                camera.moveRight(
                        -movement
                );
            }

            if (right) {

                camera.moveRight(
                        movement
                );
            }

            if (up) {

                camera.moveUp(
                        movement
                );
            }

            if (down) {

                camera.moveUp(
                        -movement
                );
            }

            /*
             * =========================
             * ROTACIÓN
             * =========================
             */

            if (rotateLeft) {

                camera.rotate(
                        0.0,
                        -rotation
                );
            }

            if (rotateRight) {

                camera.rotate(
                        0.0,
                        rotation
                );
            }

            if (rotateUp) {

                camera.rotate(
                        -rotation,
                        0.0
                );
            }

            if (rotateDown) {

                camera.rotate(
                        rotation,
                        0.0
                );
            }
        }

        public void setMoveSpeed(
                double moveSpeed) {

            this.moveSpeed =
                    Math.max(
                            0.0,
                            moveSpeed
                    );
        }

        public void setRotationSpeed(
                double rotationSpeed) {

            this.rotationSpeed =
                    Math.max(
                            0.0,
                            rotationSpeed
                    );
        }

        @Override
        public void keyPressed(
                KeyEvent event) {

            switch (
                    event.getKeyCode()
            ) {

                case KeyEvent.VK_W:

                    forward = true;

                    break;

                case KeyEvent.VK_S:

                    backward = true;

                    break;

                case KeyEvent.VK_A:

                    left = true;

                    break;

                case KeyEvent.VK_D:

                    right = true;

                    break;

                case KeyEvent.VK_SPACE:

                    up = true;

                    break;

                case KeyEvent.VK_SHIFT:

                    down = true;

                    break;

                case KeyEvent.VK_LEFT:

                    rotateLeft = true;

                    break;

                case KeyEvent.VK_RIGHT:

                    rotateRight = true;

                    break;

                case KeyEvent.VK_UP:

                    rotateUp = true;

                    break;

                case KeyEvent.VK_DOWN:

                    rotateDown = true;

                    break;
            }
        }

        @Override
        public void keyReleased(
                KeyEvent event) {

            switch (
                    event.getKeyCode()
            ) {

                case KeyEvent.VK_W:

                    forward = false;

                    break;

                case KeyEvent.VK_S:

                    backward = false;

                    break;

                case KeyEvent.VK_A:

                    left = false;

                    break;

                case KeyEvent.VK_D:

                    right = false;

                    break;

                case KeyEvent.VK_SPACE:

                    up = false;

                    break;

                case KeyEvent.VK_SHIFT:

                    down = false;

                    break;

                case KeyEvent.VK_LEFT:

                    rotateLeft = false;

                    break;

                case KeyEvent.VK_RIGHT:

                    rotateRight = false;

                    break;

                case KeyEvent.VK_UP:

                    rotateUp = false;

                    break;

                case KeyEvent.VK_DOWN:

                    rotateDown = false;

                    break;
            }
        }

        @Override
        public void keyTyped(
                KeyEvent event) {
        }
        public void setCamera(
                Camera camera) {

            if (camera == null) {
                return;
            }

            this.camera =
                    camera;
        }
    }