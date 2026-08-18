import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Donut extends JPanel {

    private double angleX = -0.35;
    private double angleY = 0.0;
    private double angleZ = 0.0;

    private final Timer timer;

    // =============================================================
    // DONUT SIZE
    // =============================================================

    private static final double MAJOR_RADIUS = 175;
    private static final double MINOR_RADIUS = 70;

    // Camera
    private static final double CAMERA = 700;
    private static final double FOCAL_LENGTH = 560;

    // Geometry resolution
    private static final int THETA_STEPS = 180;
    private static final int PHI_STEPS = 90;

    // =============================================================
    // LIGHT
    // =============================================================

    private static final Vec3 LIGHT =
            normalize(
                    new Vec3(
                            -0.45,
                            -0.65,
                            -1.0
                    )
            );

    public Donut() {

        setBackground(Color.BLACK);

        timer = new Timer(
                16,
                (ActionEvent e) -> {

                    angleX += 0.010;
                    angleY += 0.018;
                    angleZ += 0.006;

                    repaint();
                }
        );

        timer.start();
    }

    // =============================================================
    // PAINT
    // =============================================================

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 =
                (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

        int width = getWidth();
        int height = getHeight();

        int centerX = width / 2;
        int centerY = height / 2 + 20;

        // =========================================================
        // BACKGROUND
        // =========================================================

        drawBackground(
                g2,
                width,
                height
        );

        // =========================================================
        // SHADOW
        // =========================================================

        drawGroundShadow(
                g2,
                centerX,
                centerY
        );

        // =========================================================
        // PARTICLES
        // =========================================================

        List<Particle> particles =
                new ArrayList<>();

        // =========================================================
        // CREATE DONUT
        // =========================================================

        for (int i = 0; i < THETA_STEPS; i++) {

            double theta =
                    2.0 *
                            Math.PI *
                            i /
                            THETA_STEPS;

            for (int j = 0; j < PHI_STEPS; j++) {

                double phi =
                        2.0 *
                                Math.PI *
                                j /
                                PHI_STEPS;

                // -------------------------------------------------
                // TORUS POSITION
                // -------------------------------------------------

                Vec3 position =
                        torusPoint(
                                theta,
                                phi
                        );

                // -------------------------------------------------
                // TORUS NORMAL
                // -------------------------------------------------

                Vec3 normal =
                        torusNormal(
                                theta,
                                phi
                        );

                // -------------------------------------------------
                // ROTATE
                // -------------------------------------------------

                Vec3 rotated =
                        rotate(position);

                Vec3 rotatedNormal =
                        rotateVector(normal);

                // -------------------------------------------------
                // LIGHTING
                // -------------------------------------------------

                double light =
                        lighting(
                                rotated,
                                rotatedNormal
                        );

                // -------------------------------------------------
                // BAKED TEXTURE
                // -------------------------------------------------

                double texture =
                        1.0
                                + Math.sin(
                                theta * 23.0 +
                                        phi * 11.0
                        ) * 0.025
                                + Math.sin(
                                theta * 47.0 -
                                        phi * 17.0
                        ) * 0.015;

                light *= texture;

                // -------------------------------------------------
                // INNER SHADOW
                // -------------------------------------------------

                double inner =
                        Math.max(
                                0,
                                -Math.cos(phi)
                        );

                light *=
                        1.0 -
                                inner * 0.18;

                // -------------------------------------------------
                // DONUT COLOR
                // -------------------------------------------------

                Color donutColor =
                        donutColor(
                                light
                        );

                addProjectedParticle(
                        particles,
                        rotated,
                        donutColor,
                        4.5
                );

                // =================================================
                // STRAWBERRY FROSTING
                // =================================================

                /*
                 * IMPORTANT FIX
                 *
                 * The frosting is determined using PHI.
                 *
                 * phi:
                 *
                 *       0
                 *       |
                 *   PI/2 ----> upper part
                 *       |
                 *       PI
                 *
                 * sin(phi) > 0 means the frosting is on the
                 * upper half of the donut tube.
                 *
                 * This wraps around BOTH sides of the donut.
                 */

                double upperSurface =
                        Math.sin(phi);

                /*
                 * Small irregularity around the frosting edge.
                 *
                 * This changes with THETA, so the frosting is
                 * naturally uneven all the way around the donut.
                 */

                double edgeNoise =
                        Math.sin(theta * 5.0) * 0.07
                                + Math.sin(theta * 11.0) * 0.04
                                + Math.sin(theta * 19.0) * 0.025;

                /*
                 * 0 means approximately the middle of the tube.
                 *
                 * Lowering this value makes the frosting cover
                 * more of the donut.
                 */

                double frostingBoundary =
                        -0.08 +
                                edgeNoise;

                /*
                 * =================================================
                 * FROSTING COVERAGE
                 * =================================================
                 *
                 * This is the important part.
                 *
                 * We use SIN(PHI), NOT NORMAL.Y.
                 *
                 * Therefore the frosting exists around the entire
                 * circumference of the donut.
                 */

                if (upperSurface >
                        frostingBoundary) {

                    // -------------------------------------------------
                    // THICK FROSTING
                    // -------------------------------------------------

                    int frostingLayers = 6;

                    for (int layer = 0;
                         layer < frostingLayers;
                         layer++) {

                        double layerAmount =
                                layer /
                                        (double)
                                                (frostingLayers - 1);

                        /*
                         * Thick frosting.
                         *
                         * Total thickness is approximately
                         * 14-16 pixels.
                         */

                        double thickness =
                                3.5 +
                                        layerAmount * 11.0;

                        /*
                         * Slightly irregular surface.
                         */

                        thickness +=
                                Math.sin(
                                        theta * 8.0 +
                                                phi * 5.0
                                ) * 0.6;

                        // -------------------------------------------------
                        // MOVE OUT FROM DONUT SURFACE
                        // -------------------------------------------------

                        Vec3 cream =
                                add(
                                        position,
                                        multiply(
                                                normal,
                                                thickness
                                        )
                                );

                        // =================================================
                        // FROSTING DRIPS
                        // =================================================

                        /*
                         * Only create drips close to the lower edge
                         * of the frosting.
                         */

                        double dripRegion =
                                Math.max(
                                        0,
                                        frostingBoundary
                                                + 0.28
                                                - upperSurface
                                );

                        /*
                         * Periodic drip pattern around the donut.
                         */

                        double dripPattern =
                                Math.sin(
                                        theta * 7.0
                                );

                        /*
                         * Only some areas drip.
                         */

                        if (dripRegion > 0 &&
                                dripPattern > 0.35) {

                            double dripAmount =
                                    dripRegion *
                                            (dripPattern - 0.25) *
                                            18.0;

                            /*
                             * IMPORTANT:
                             *
                             * Move in LOCAL Z direction.
                             *
                             * This keeps the drip attached to the
                             * donut and makes it rotate correctly.
                             */

                            cream.z -=
                                    dripAmount;
                        }

                        // -------------------------------------------------
                        // ROTATE FROSTING
                        // -------------------------------------------------

                        Vec3 rotatedCream =
                                rotate(
                                        cream
                                );

                        Vec3 creamNormal =
                                rotateVector(
                                        normal
                                );

                        // -------------------------------------------------
                        // FROSTING LIGHTING
                        // -------------------------------------------------

                        double creamLight =
                                lighting(
                                        rotatedCream,
                                        creamNormal
                                );

                        /*
                         * Outer layers slightly brighter.
                         */

                        creamLight *=
                                0.92 +
                                        layerAmount *
                                                0.10;

                        // -------------------------------------------------
                        // CREAM COLOR
                        // -------------------------------------------------

                        Color creamColor =
                                creamColor(
                                        creamLight,
                                        layerAmount
                                );

                        // -------------------------------------------------
                        // ADD CREAM
                        // -------------------------------------------------

                        addProjectedParticle(
                                particles,
                                rotatedCream,
                                creamColor,
                                5.0
                        );
                    }
                }
            }
        }

        // =========================================================
        // DEPTH SORT
        // =========================================================

        particles.sort(
                Comparator.comparingDouble(
                        p -> -p.depth
                )
        );

        // =========================================================
        // DRAW PARTICLES
        // =========================================================

        for (Particle p : particles) {

            if (p.screenX < -40 ||
                    p.screenX > width + 40 ||
                    p.screenY < -40 ||
                    p.screenY > height + 40) {

                continue;
            }

            g2.setColor(
                    p.color
            );

            double size =
                    p.size *
                            p.scale;

            size =
                    Math.max(
                            1.5,
                            Math.min(
                                    9.0,
                                    size
                            )
                    );

            g2.fill(
                    new Ellipse2D.Double(
                            p.screenX -
                                    size / 2,

                            p.screenY -
                                    size / 2,

                            size,
                            size
                    )
            );
        }

        g2.dispose();
    }

    // =============================================================
    // TORUS POINT
    // =============================================================

    private Vec3 torusPoint(
            double theta,
            double phi
    ) {

        double cosPhi =
                Math.cos(phi);

        double sinPhi =
                Math.sin(phi);

        double cosTheta =
                Math.cos(theta);

        double sinTheta =
                Math.sin(theta);

        double ringRadius =
                MAJOR_RADIUS +
                        MINOR_RADIUS *
                                cosPhi;

        /*
         * Slight vertical compression.
         */

        return new Vec3(

                ringRadius *
                        cosTheta,

                ringRadius *
                        sinTheta *
                        0.88,

                MINOR_RADIUS *
                        sinPhi
        );
    }

    // =============================================================
    // TORUS NORMAL
    // =============================================================

    private Vec3 torusNormal(
            double theta,
            double phi
    ) {

        double cosPhi =
                Math.cos(phi);

        double sinPhi =
                Math.sin(phi);

        double cosTheta =
                Math.cos(theta);

        double sinTheta =
                Math.sin(theta);

        return normalize(
                new Vec3(

                        cosPhi *
                                cosTheta,

                        cosPhi *
                                sinTheta,

                        sinPhi
                )
        );
    }

    // =============================================================
    // ROTATION
    // =============================================================

    private Vec3 rotate(
            Vec3 p
    ) {

        double cosX =
                Math.cos(angleX);

        double sinX =
                Math.sin(angleX);

        double cosY =
                Math.cos(angleY);

        double sinY =
                Math.sin(angleY);

        double cosZ =
                Math.cos(angleZ);

        double sinZ =
                Math.sin(angleZ);

        // ---------------------------------------------------------
        // X
        // ---------------------------------------------------------

        double y1 =
                p.y * cosX -
                        p.z * sinX;

        double z1 =
                p.y * sinX +
                        p.z * cosX;

        // ---------------------------------------------------------
        // Y
        // ---------------------------------------------------------

        double x2 =
                p.x * cosY +
                        z1 * sinY;

        double z2 =
                -p.x * sinY +
                        z1 * cosY;

        // ---------------------------------------------------------
        // Z
        // ---------------------------------------------------------

        double x3 =
                x2 * cosZ -
                        y1 * sinZ;

        double y3 =
                x2 * sinZ +
                        y1 * cosZ;

        return new Vec3(
                x3,
                y3,
                z2
        );
    }

    // =============================================================
    // ROTATE NORMAL
    // =============================================================

    private Vec3 rotateVector(
            Vec3 v
    ) {

        return rotate(v);
    }

    // =============================================================
    // LIGHTING
    // =============================================================

    private double lighting(
            Vec3 position,
            Vec3 normal
    ) {

        // ---------------------------------------------------------
        // MAIN LIGHT
        // ---------------------------------------------------------

        double diffuse =
                dot(
                        normal,
                        LIGHT
                );

        diffuse =
                Math.max(
                        0,
                        diffuse
                );

        // ---------------------------------------------------------
        // SOFT FILL
        // ---------------------------------------------------------

        Vec3 fill =
                normalize(
                        new Vec3(
                                0.2,
                                0.7,
                                -0.5
                        )
                );

        double fillLight =
                Math.max(
                        0,
                        dot(
                                normal,
                                fill
                        )
                );

        // ---------------------------------------------------------
        // AMBIENT
        // ---------------------------------------------------------

        double result =
                0.22 +
                        diffuse * 0.63 +
                        fillLight * 0.15;

        // ---------------------------------------------------------
        // INNER OCCLUSION
        // ---------------------------------------------------------

        double distanceFromCenter =
                Math.sqrt(
                        position.x *
                                position.x +

                                position.y *
                                        position.y
                );

        double occlusion =
                1.0;

        if (distanceFromCenter < 125) {

            occlusion =
                    0.76;
        }

        return result *
                occlusion;
    }

    // =============================================================
    // DONUT COLOR
    // =============================================================

    private Color donutColor(
            double brightness
    ) {

        brightness =
                Math.max(
                        0.15,
                        Math.min(
                                1.15,
                                brightness
                        )
                );

        int red =
                clamp(
                        (int)
                                (210 *
                                        brightness)
                );

        int green =
                clamp(
                        (int)
                                (108 *
                                        brightness)
                );

        int blue =
                clamp(
                        (int)
                                (36 *
                                        brightness)
                );

        /*
         * Warm baked crust.
         */

        red =
                clamp(
                        red + 15
                );

        green =
                clamp(
                        green + 3
                );

        return new Color(
                red,
                green,
                blue
        );
    }

    // =============================================================
    // STRAWBERRY CREAM COLOR
    // =============================================================

    private Color creamColor(
            double brightness,
            double layer
    ) {

        brightness =
                Math.max(
                        0.20,
                        Math.min(
                                1.20,
                                brightness
                        )
                );

        /*
         * Strawberry cream.
         */

        int red =
                clamp(
                        (int)
                                (255 *
                                        brightness)
                );

        int green =
                clamp(
                        (int)
                                (142 *
                                        brightness)
                );

        int blue =
                clamp(
                        (int)
                                (174 *
                                        brightness)
                );

        /*
         * Outer frosting is slightly brighter.
         */

        int highlight =
                (int)
                        (layer * 10);

        red =
                clamp(
                        red + highlight
                );

        green =
                clamp(
                        green + highlight
                );

        blue =
                clamp(
                        blue + highlight
                );

        return new Color(
                red,
                green,
                blue
        );
    }

    // =============================================================
    // PROJECT PARTICLE
    // =============================================================

    private void addProjectedParticle(
            List<Particle> particles,
            Vec3 p,
            Color color,
            double size
    ) {

        double depth =
                CAMERA +
                        p.z;

        if (depth <= 20) {

            return;
        }

        double scale =
                FOCAL_LENGTH /
                        depth;

        int screenX =
                getWidth() / 2 +
                        (int)
                                (p.x * scale);

        int screenY =
                getHeight() / 2 +
                        20 +
                        (int)
                                (p.y * scale);

        particles.add(
                new Particle(
                        screenX,
                        screenY,
                        depth,
                        scale,
                        size,
                        color
                )
        );
    }

    // =============================================================
    // BACKGROUND
    // =============================================================

    private void drawBackground(
            Graphics2D g2,
            int width,
            int height
    ) {

        GradientPaint gradient =
                new GradientPaint(

                        0,
                        0,

                        new Color(
                                30,
                                21,
                                32
                        ),

                        0,
                        height,

                        new Color(
                                4,
                                4,
                                7
                        )
                );

        g2.setPaint(
                gradient
        );

        g2.fillRect(
                0,
                0,
                width,
                height
        );
    }

    // =============================================================
    // GROUND SHADOW
    // =============================================================

    private void drawGroundShadow(
            Graphics2D g2,
            int centerX,
            int centerY
    ) {

        for (int i = 25; i >= 1; i--) {

            double t =
                    i / 25.0;

            int shadowWidth =
                    (int)
                            (360 +
                                    t * 130);

            int shadowHeight =
                    (int)
                            (55 +
                                    t * 45);

            int alpha =
                    (int)
                            (2 +
                                    (1 - t) * 18);

            g2.setColor(
                    new Color(
                            0,
                            0,
                            0,
                            alpha
                    )
            );

            g2.fillOval(

                    centerX -
                            shadowWidth / 2,

                    centerY +
                            175 -
                            shadowHeight / 2,

                    shadowWidth,
                    shadowHeight
            );
        }
    }

    // =============================================================
    // VECTOR ADD
    // =============================================================

    private static Vec3 add(
            Vec3 a,
            Vec3 b
    ) {

        return new Vec3(
                a.x + b.x,
                a.y + b.y,
                a.z + b.z
        );
    }

    // =============================================================
    // VECTOR MULTIPLY
    // =============================================================

    private static Vec3 multiply(
            Vec3 a,
            double value
    ) {

        return new Vec3(
                a.x * value,
                a.y * value,
                a.z * value
        );
    }

    // =============================================================
    // DOT PRODUCT
    // =============================================================

    private static double dot(
            Vec3 a,
            Vec3 b
    ) {

        return
                a.x * b.x +
                        a.y * b.y +
                        a.z * b.z;
    }

    // =============================================================
    // NORMALIZE
    // =============================================================

    private static Vec3 normalize(
            Vec3 v
    ) {

        double length =
                Math.sqrt(
                        v.x * v.x +
                                v.y * v.y +
                                v.z * v.z
                );

        return new Vec3(
                v.x / length,
                v.y / length,
                v.z / length
        );
    }

    // =============================================================
    // CLAMP
    // =============================================================

    private static int clamp(
            int value
    ) {

        return Math.max(
                0,
                Math.min(
                        255,
                        value
                )
        );
    }

    // =============================================================
    // 3D VECTOR
    // =============================================================

    private static class Vec3 {

        double x;
        double y;
        double z;

        Vec3(
                double x,
                double y,
                double z
        ) {

            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    // =============================================================
    // PARTICLE
    // =============================================================

    private static class Particle {

        int screenX;
        int screenY;

        double depth;
        double scale;
        double size;

        Color color;

        Particle(
                int screenX,
                int screenY,
                double depth,
                double scale,
                double size,
                Color color
        ) {

            this.screenX = screenX;
            this.screenY = screenY;

            this.depth = depth;
            this.scale = scale;
            this.size = size;

            this.color = color;
        }
    }

    // =============================================================
    // MAIN
    // =============================================================

    public static void main(
            String[] args
    ) {

        SwingUtilities.invokeLater(
                () -> {

                    JFrame frame =
                            new JFrame(
                                    "Strawberry Cream Donut"
                            );

                    frame.setDefaultCloseOperation(
                            JFrame.EXIT_ON_CLOSE
                    );

                    frame.setSize(
                            1000,
                            800
                    );

                    frame.setLocationRelativeTo(
                            null
                    );

                    frame.setContentPane(
                            new Donut()
                    );

                    frame.setVisible(
                            true
                    );
                }
        );
    }
}