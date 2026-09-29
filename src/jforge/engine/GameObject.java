package jforge.engine;

import jforge.engine.components.MeshRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameObject {

    private String name;

    private final Transform transform;

    private MeshRenderer meshRenderer;

    private boolean active;

    private GameObject parent;

    private final List<GameObject> children;

    public GameObject(String name) {

        this.name = name;

        this.transform =
                new Transform();

        this.active = true;

        this.parent = null;

        this.children =
                new ArrayList<>();
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public Transform getTransform() {

        return transform;
    }

    public MeshRenderer getMeshRenderer() {

        return meshRenderer;
    }

    public void setMeshRenderer(
            MeshRenderer meshRenderer) {

        this.meshRenderer =
                meshRenderer;
    }

    public boolean isActive() {

        return active;
    }

    public void setActive(
            boolean active) {

        this.active = active;
    }

    public GameObject getParent() {

        return parent;
    }

    public List<GameObject> getChildren() {

        return Collections.unmodifiableList(
                children
        );
    }

    public void addChild(
            GameObject child) {

        if (child == null) {
            return;
        }

        if (child == this) {
            throw new IllegalArgumentException(
                    "Un GameObject no puede ser hijo de sí mismo."
            );
        }

        /*
         * Evitar ciclos:
         *
         * A
         *  └ B
         *     └ C
         *
         * C no puede convertirse
         * en padre de A.
         */
        if (isDescendantOf(child)) {

            throw new IllegalArgumentException(
                    "No se puede crear un ciclo en la jerarquía."
            );
        }

        /*
         * Si ya tenía otro padre,
         * lo quitamos primero.
         */
        if (child.parent != null) {

            child.parent.children.remove(child);
        }

        child.parent = this;

        children.add(child);
    }

    public void removeChild(
            GameObject child) {

        if (child == null) {
            return;
        }

        if (children.remove(child)) {

            child.parent = null;
        }
    }

    private boolean isDescendantOf(
            GameObject object) {

        GameObject current = this;

        while (current != null) {

            if (current == object) {
                return true;
            }

            current =
                    current.parent;
        }

        return false;
    }
}