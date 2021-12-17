package spacegraph.space3d.raytrace;

import jcog.Util;
import jcog.math.v3d;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public abstract class Entity {

    public Surface surface;
    public v3d position;
    public BufferedImage texture;


    public abstract Ray3 collide(v3d rayPosition, v3d rayDirection);

    public abstract int textureColor(v3d position);

    public void texture(String texture) {
//        try {
        if (texture == null) texture = "";
        try {
            this.texture = texture!=null ? ImageIO.read(new File(texture)) : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
//        } catch (IOException e) {
//            this.texture = null;
//            e.printStackTrace();
//        }
    }

    public static class Cube extends Entity {
        double sideLength;

        private final Ray3[] faces;

        public Cube(v3d position, double sideLength, Surface surface, String texture) {
            texture(texture);
            this.position = position;
            this.sideLength = sideLength;
            this.surface = surface;

            double hs = sideLength / 2;
            faces = new Ray3[]{
                new Ray3(
                    position.add(new v3d(-hs, 0, 0)),
                    new v3d(-1, 0, 0)
                ),
                new Ray3(
                    position.add(new v3d(hs, 0, 0)),
                    new v3d(1, 0, 0)
                ),
                new Ray3(
                    position.add(new v3d(0, -hs, 0)),
                    new v3d(0, -1, 0)
                ),
                new Ray3(
                    position.add(new v3d(0, hs, 0)),
                    new v3d(0, 1, 0)
                ),
                new Ray3(
                    position.add(new v3d(0, 0, -hs)),
                    new v3d(0, 0, -1)
                ),
                new Ray3(
                    position.add(new v3d(0, 0, hs)),
                    new v3d(0, 0, 1)
                )
            };
        }

        @Override
        public int textureColor(v3d position) {
            v3d fp = position.minus(this.position);
            v3d afp = new v3d(Math.abs(fp.x), Math.abs(fp.y), Math.abs(fp.z));
            v3d axis1 = null;
            v3d axis2 = null;
            if (afp.x < afp.z && afp.y < afp.z) {
                axis1 = v3d.X_AXIS;
                axis2 = v3d.Y_AXIS;
            } else if (afp.x < afp.y && afp.z < afp.y) {
                axis1 = v3d.X_AXIS;
                axis2 = v3d.Z_AXIS;
            } else if (afp.y < afp.x && afp.z < afp.x) {
                axis1 = v3d.Y_AXIS;
                axis2 = v3d.Z_AXIS;
            } else {
                return 0; //???
            }
            double x = 5 * (fp.dot(axis1) / sideLength + 0.5) % 1;
            double y = 5 * (fp.dot(axis2) / sideLength + 0.5) % 1;
            return texture.getRGB(
                (int) (x * texture.getWidth()),
                (int) (y * texture.getHeight())
            );
        }

        @Override
        public Ray3 collide(v3d rayPosition, v3d rayDirection) {
            Ray3 closestNormal = null;
            double distanceSquared = 0;
            double hs = sideLength / 2;

            for (Ray3 face : faces) {
                v3d faceNormal = face.direction;
                v3d facePos = face.position;

                double distance = rayPosition.minus(facePos).dot(faceNormal);
                if (distance < 0) {
                    faceNormal = faceNormal.scale(-1);
                    distance = -distance;
                }

                Ray3 normal = new Ray3(
                    rayPosition.addScale(
                        rayDirection, -distance / rayDirection.dot(faceNormal)
                    ),
                    faceNormal
                );

                v3d normPos = normal.position;

                v3d fp = normPos.minus(facePos);
                if (Math.abs(fp.x) > hs || Math.abs(fp.y) > hs || Math.abs(fp.z) > hs)
                    continue;

                v3d normPosMinusrayPos = normPos.minus(rayPosition);
                if (normPosMinusrayPos.dot(rayDirection) < Scene.Epsilon)
                    continue;

                double d = normPosMinusrayPos.lengthSquared();
                if (closestNormal == null || d < distanceSquared) {
                    closestNormal = normal;
                    distanceSquared = d;
                }
            }
            return closestNormal;
        }
    }

    public static class Sphere extends Entity {


        double radius;

        public Sphere(v3d position, double radius, Surface surface, String texture) {
            this.position = position;
            this.radius = radius;
            this.surface = surface;
            texture(texture);
        }


        @Override
        public int textureColor(v3d position) {
            v3d rp = position.minus(this.position);
            double x = Math.atan2(rp.y, rp.x) / (2 * Math.PI) + 0.5;
            double y = Math.asin(rp.z / rp.length()) / Math.PI + 0.5;
            return texture.getRGB(
                (int) (x * texture.getWidth()),
                (int) ((1 - y) * texture.getHeight())
            );
        }

        @Override
        public Ray3 collide(v3d rayPosition, v3d rayDirection) {
            v3d closestPoint = rayDirection.scale(
                position.minus(rayPosition).dot(rayDirection)
            ).add(rayPosition);

            v3d perpendicular = closestPoint.minus(position);
            double perpLenSq = perpendicular.lengthSquared();
            double radSq = Util.sqr(radius);
            if (perpLenSq >= radSq)
                return null;

            v3d opposite = rayDirection.scale(
                Math.sqrt(radSq - perpLenSq)
            );
            v3d posPerp = position.add(perpendicular);
            v3d intersection1 = posPerp.minus(opposite);
            v3d intersection2 = posPerp.add(opposite);
            double distance1 = intersection1.minus(rayPosition).dot(rayDirection);
            double distance2 = intersection2.minus(rayPosition).dot(rayDirection);

            if (distance1 <= Scene.Epsilon && distance2 <= Scene.Epsilon)
                return null;

            v3d intersection;
            if (distance1 > 0 && distance2 <= Scene.Epsilon) {
                intersection = intersection1;
            } else if (distance2 > 0 && distance1 <= Scene.Epsilon) {
                intersection = intersection2;
            } else if (distance1 < distance2) {
                intersection = intersection1;
            } else {
                intersection = intersection2;
            }

            Ray3 normal = new Ray3(intersection, intersection.minus(position));

            if (rayPosition.minus(position).lengthSquared() < radSq)
                normal.direction.invertThis();

            normal.direction.normalizeThis();
            return normal;
        }
    }

    public enum Surface {
        Specular, Diffuse, Transparent
    }
}