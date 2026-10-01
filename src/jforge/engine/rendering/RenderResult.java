package jforge.engine.rendering;
 public class RenderResult {

    private final int objects;
    private final int triangles;

    public RenderResult(
            int objects,
            int triangles) {

        this.objects =
                objects;

        this.triangles =
                triangles;
    }

     public int getObjects() {
         return objects;
     }

     public int getTriangles() {
         return triangles;
     }
 }
