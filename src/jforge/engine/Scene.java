package jforge.engine;

import jforge.engine.rendering.DirectionalLight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Scene {

    private final String name;

    private final List<GameObject> rootObjects;

    private final DirectionalLight light;

    public Scene(String name) {

        this.name = name;

        rootObjects =
                new ArrayList<>();

        light =
                new DirectionalLight();
    }

    public void add(
            GameObject object) {

        if (object == null) {
            return;
        }

        /*
         * Si tenía padre,
         * lo quitamos de él.
         */
        if (object.getParent() != null) {

            object.getParent()
                    .removeChild(object);
        }

        if (!rootObjects.contains(object)) {

            rootObjects.add(object);
        }
    }

    public void remove(
            GameObject object) {

        if (object == null) {
            return;
        }

        rootObjects.remove(object);
    }

    public List<GameObject> getObjects() {

        return Collections.unmodifiableList(
                rootObjects
        );
    }

    public String getName() {

        return name;
    }

    public DirectionalLight getLight() {

        return light;
    }
}