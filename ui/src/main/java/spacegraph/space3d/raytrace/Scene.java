package spacegraph.space3d.raytrace;

import jcog.data.list.Lst;
import jcog.math.v3d;

import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public class Scene {

    private static final int MAX_REFLECTIONS = 4;

    static final double Epsilon =
        //Float.MIN_NORMAL;
        0.00000001;
        //0.0000001;
        //0.000001;
        //0.001;

    public final Collection<Light> lights = new Lst<>();
    public final List<Entity> entities = new Lst<>();
    public Camera camera;

    public Scene() {

    }

    public Scene(String src) {
        Scanner scanner;
        try {
            scanner = new Scanner(new StringReader(src));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String line = Utils.nextLineOrEmpty(scanner);
        while (!line.isEmpty()) {
            switch (line) {
                case "camera:": {
                    v3d position = new v3d(0, 0, 0);
                    v3d direction = new v3d(1, 0, 0);
                    double fov = 90;
                    double size = 0;
                    while (Utils.isIndented(line = Utils.nextLineOrEmpty(scanner))) {
                        Scanner s = new Scanner(line);
                        switch (s.next()) {
                            case "position:" -> position = Utils.readVector3(s);
                            case "direction:" -> direction = Utils.readVector3(s);
                            case "fov:" -> fov = s.nextDouble();
                            case "size:" -> size = s.nextDouble();
                        }
                    }
                    camera = new Camera(position, direction, fov, size);
                }
                case "cube:": {
                    v3d position = new v3d(0, 0, 0);
                    double sideLength = 1;
                    Entity.Surface surface = null;
                    String texture = "";
                    while (Utils.isIndented(line = Utils.nextLineOrEmpty(scanner))) {
                        Scanner s = new Scanner(line);
                        switch (s.next()) {
                            case "position:" -> position = Utils.readVector3(s);
                            case "sideLength:" -> sideLength = s.nextDouble();
                            case "surface:" -> surface = Utils.readSurface(s);
                            case "texture:" -> texture = s.next();
                        }
                    }
                    entities.add(new Entity.Cube(position, sideLength, surface, texture));
                    break;
                }
                case "sphere:": {
                    v3d position = new v3d(0, 0, 0);
                    double radius = 1;
                    Entity.Surface surface = null;
                    String texture = "";
                    while (Utils.isIndented(line = Utils.nextLineOrEmpty(scanner))) {
                        Scanner s = new Scanner(line);
                        switch (s.next()) {
                            case "position:" -> position = Utils.readVector3(s);
                            case "radius:" -> radius = s.nextDouble();
                            case "surface:" -> surface = Utils.readSurface(s);
                            case "texture:" -> texture = s.next();
                        }
                    }
                    entities.add(new Entity.Sphere(position, radius, surface, texture));
                    break;
                }
                case "light:": {
                    v3d position = new v3d(0, 0, 0);
                    int color = 0xffffff;
                    while (Utils.isIndented(line = Utils.nextLineOrEmpty(scanner))) {
                        Scanner s = new Scanner(line);
                        switch (s.next()) {
                            case "position:" -> position = Utils.readVector3(s);
                            case "color:" -> color = s.nextInt(16);
                        }
                    }
                    lights.add(new Light(position, color));
                    break;
                }
            }
        }
    }


    private Collision castRay(v3d rayPosition, v3d rayDirection) {
        double closestCollisionDistanceSquared = Double.POSITIVE_INFINITY;
        Entity closestEntity = null;
        Ray3 closestNormal = null;

        //TODO use spatial index
        for (Entity entity : entities) {
            Ray3 normal = entity.collide(rayPosition, rayDirection);
            if (normal != null) {
                double distanceSquared = normal.position.distanceSquared(rayPosition);
                if (distanceSquared < closestCollisionDistanceSquared) {
                    closestEntity = entity;
                    closestNormal = normal;
                    closestCollisionDistanceSquared = distanceSquared;
                }
            }
        }
        return closestEntity != null ? new Collision(closestEntity, closestNormal) : null;
    }

    int rayColor(Ray3 ray) {
        return rayColor(ray.position, ray.direction);
    }

    int rayColor(v3d rayPosition, v3d rayDirection) {
        Collision collision;
        int reflections = 0;
        do {
            collision = castRay(rayPosition, rayDirection);
            if (collision == null)
                return 0x000000;

            Entity.Surface surface = collision.entity.surface;
            v3d d = collision.normal.direction;
            v3d p = collision.normal.position;
            if (surface == Entity.Surface.Transparent) {


                v3d tangent = d.cross(d.cross(rayDirection)).normalizeThis();
                double nProj = -rayDirection.dot(d);
                rayDirection.scaleThis(1 / nProj);
                double tProj = rayDirection.dot(tangent);
                double r = 1.5;
                //ray = new Ray3(
                rayPosition =      p.minus(d.scale(0.001));
                rayDirection = d.scale(-1).add(tangent.scale(tProj / r)).normalizeThis();
                //);
                collision = castRay(rayPosition, rayDirection);
                tangent = d.cross(d.cross(rayDirection)).normalizeThis();
                nProj = -rayDirection.dot(d);
                rayDirection.scaleThis(1 / nProj);
                tProj = rayDirection.dot(tangent);
                //ray = new Ray3(
                rayPosition =        p.minus(d.scale(0.001));
                rayDirection =        d.scale(-1).add(tangent.scale(tProj * r)).normalizeThis();
                //);
            } else if (surface == Entity.Surface.Diffuse) {
                break;
            } else if (surface == Entity.Surface.Specular) {
                //ray = new Ray3(
                rayPosition = p;
                rayDirection =        rayDirection.minus(d.scale(2 * rayDirection.dot(d)));
                //);
            } else {
                //return 0x000000;
                throw new UnsupportedOperationException();
            }

        } while (++reflections < MAX_REFLECTIONS);
        return getDiffuseColor(collision);
    }


    private int getDiffuseColor(Collision collision) {
        double intensityR = 0;
        double intensityG = 0;
        double intensityB = 0;
        v3d collPos = collision.normal.position;
        v3d collNorm = collision.normal.direction;
        for (Light light : lights) {
            v3d lightVector = light.position.minus(collPos);
            double lightVectorLenSq = lightVector.lengthSquared();
            v3d lightDirection = lightVector.normalizeThis();
            Collision c = castRay(collPos, lightDirection);
            if (c == null || c.entity.surface == Entity.Surface.Transparent || c.normal.position.minus(collPos).lengthSquared() > lightVectorLenSq) {
                double intensity = Math.abs(collNorm.dot(lightDirection)) / lightVectorLenSq;
                intensityR += (double) (light.color >> 16) / 255 * intensity;
                intensityG += (double) ((light.color >> 8) & 0xff) / 255 * intensity;
                intensityB += (double) (light.color & 0xff) / 255 * intensity;
            }
        }

        double m = 10;
        intensityR *= m;
        intensityG *= m;
        intensityB *= m;

        intensityR += 0.05;
        intensityG += 0.05;
        intensityB += 0.05;
        if (collision.entity.surface == Entity.Surface.Diffuse) {
            if (collision.entity.texture!=null) { //HACK
                int textureColor = collision.entity.textureColor(collPos);
                if (textureColor != -1) {
                    intensityR *= (double) ((textureColor >> 16) & 0xff) / 255;
                    intensityG *= (double) ((textureColor >> 8) & 0xff) / 255;
                    intensityB *= (double) (textureColor & 0xff) / 255;
                }
            }
        }

        int r = Math.min(255, (int) (intensityR * 256));
        int g = Math.min(255, (int) (intensityG * 256));
        int b = Math.min(255, (int) (intensityB * 256));
        return (r << 16) + (g << 8) + b;
    }


    static final class Collision {
        final Entity entity;
        final Ray3 normal;

        Collision(Entity entity, Ray3 normal) {
            this.entity = entity;
            this.normal = normal;
        }
    }

    enum Utils {
        ;

        static String nextLineOrEmpty(Scanner scanner) {
            return scanner.hasNextLine() ? scanner.nextLine() : "";
        }

        static boolean isIndented(String line) {
            return !line.isEmpty() && (line.charAt(0) == '\t' || line.charAt(0) == ' ');
        }

        static Entity.Surface readSurface(Scanner scanner) {
            switch (scanner.next()) {
                case "diffuse": return Entity.Surface.Diffuse;
                case "specular": return Entity.Surface.Specular;
                case "transparent": return Entity.Surface.Transparent;
            }
            throw new RuntimeException("Non-existent surface!");
        }

        static v3d readVector3(Scanner scanner) {
            String str = scanner.nextLine().trim();
            if (str.charAt(0) != '(' || str.charAt(str.length() - 1) != ')') {
                throw new RuntimeException("Coordinates must be parenthesized!");
            }
            str = str.substring(1, str.length() - 1);
            String[] coords = str.split(",");
            if (coords.length != 3) {
                throw new RuntimeException("A coordinates must have exactly 3 components!");
            }
            for (int i = 0; i < coords.length; i++) {
                coords[i] = coords[i].trim();
            }
            double[] parsedCoords = new double[coords.length];
            for (int i = 0; i < parsedCoords.length; i++) {
                try {
                    parsedCoords[i] = Double.parseDouble(coords[i]);
                } catch (Exception e) {
                    throw new RuntimeException("Components of coordinate must be numbers!");
                }
            }
            return new v3d(parsedCoords[0], parsedCoords[1], parsedCoords[2]);
        }
    }

	public static final class Light {
		public final v3d position;
		public final int color;

		public Light(v3d position, int color) {
			this.position = position;
			this.color = color;
		}
	}
}