# 🍩 Strawberry Cream Donut

A Java Swing 3D-style animated donut rendered entirely with `Graphics2D`.

The project creates a rotating baked donut with:

* 🍩 3D torus-style donut geometry
* 🍓 Strawberry-colored thick frosting
* 🍰 Frosting that wraps evenly around both sides
* 💡 Dynamic lighting and shading
* 🌑 Soft ground shadow
* ✨ Anti-aliased rendering
* 🔄 Smooth continuous rotation
* 🎨 Dark gradient background
* 🚫 No floating red strawberry particles

## Requirements

* Java JDK 8 or newer
* Swing and AWT (included with standard Java)
* No external libraries are required

## Project Structure

```text
StrawberryCreamDonut/
└── Donut.java
```

## Running the Project

### 1. Save the source code

Save the Java code as:

```text
Donut.java
```

Make sure the filename matches the public class:

```java
public class Donut
```

### 2. Compile

Open a terminal in the directory containing `Donut.java` and run:

```bash
javac Donut.java
```

### 3. Run

```bash
java Donut
```

A window named **Strawberry Cream Donut** should appear.

## Controls

The current version is fully automatic.

The donut continuously rotates around the X, Y, and Z axes.

The rotation speed is controlled here:

```java
angleX += 0.010;
angleY += 0.018;
angleZ += 0.006;
```

For faster rotation:

```java
angleX += 0.020;
angleY += 0.030;
angleZ += 0.015;
```

For slower rotation:

```java
angleX += 0.005;
angleY += 0.009;
angleZ += 0.003;
```

## Donut Geometry

The donut is generated using a mathematical torus.

The main radius is controlled by:

```java
private static final double MAJOR_RADIUS = 175;
```

The thickness of the donut is controlled by:

```java
private static final double MINOR_RADIUS = 70;
```

Increasing `MAJOR_RADIUS` makes the donut wider.

Increasing `MINOR_RADIUS` makes the donut thicker.

For example:

```java
private static final double MAJOR_RADIUS = 190;
private static final double MINOR_RADIUS = 80;
```

creates a larger and thicker donut.

## Frosting

The frosting is generated directly from the donut's 3D surface.

This is important because the frosting rotates together with the donut instead of being drawn as a separate 2D object.

The frosting covers the upper half of the donut using:

```java
double upperSurface = Math.sin(phi);
```

The coverage is controlled by:

```java
double frostingBoundary = -0.08 + edgeNoise;
```

Lowering the boundary causes more of the donut to be covered.

For example:

```java
double frostingBoundary = -0.20 + edgeNoise;
```

creates more frosting.

Raising it:

```java
double frostingBoundary = 0.15 + edgeNoise;
```

creates less frosting.

## Frosting Thickness

The frosting uses multiple 3D layers:

```java
int frostingLayers = 6;
```

The thickness is calculated with:

```java
double thickness =
        3.5 +
        layerAmount * 11.0;
```

For thicker frosting, increase the final value:

```java
double thickness =
        4.0 +
        layerAmount * 16.0;
```

For thinner frosting:

```java
double thickness =
        2.0 +
        layerAmount * 7.0;
```

## Frosting Drips

The frosting has subtle drips around the lower edge.

The drip amount is controlled by:

```java
double dripAmount =
        dripRegion *
        (dripPattern - 0.25) *
        18.0;
```

Increase `18.0` for longer drips:

```java
* 28.0;
```

Reduce it for smaller drips:

```java
* 10.0;
```

## Lighting

The donut uses a directional light:

```java
private static final Vec3 LIGHT =
        normalize(
                new Vec3(
                        -0.45,
                        -0.65,
                        -1.0
                )
        );
```

The lighting system includes:

* Diffuse lighting
* Ambient lighting
* Fill lighting
* Inner-hole darkening
* Surface shading

Changing the light values changes the direction of the light.

For example:

```java
new Vec3(
        0.5,
        -0.8,
        -1.0
)
```

moves the main light toward the opposite side.

## Donut Color

The baked donut color is generated in:

```java
private Color donutColor(double brightness)
```

The main colors are:

```java
int red =
        clamp((int)(210 * brightness));

int green =
        clamp((int)(108 * brightness));

int blue =
        clamp((int)(36 * brightness));
```

For a darker chocolate donut, you could use:

```java
int red =
        clamp((int)(120 * brightness));

int green =
        clamp((int)(55 * brightness));

int blue =
        clamp((int)(20 * brightness));
```

## Frosting Color

The strawberry cream color is generated in:

```java
private Color creamColor(
        double brightness,
        double layer
)
```

The current frosting is pink:

```java
red   = 255
green = 142
blue  = 174
```

For a lighter strawberry cream, increase the green and blue values.

For example:

```java
red   = 255
green = 175
blue  = 195
```

## Rendering Resolution

The donut's smoothness is controlled by:

```java
private static final int THETA_STEPS = 180;
private static final int PHI_STEPS = 90;
```

Higher values produce a smoother donut but require more calculations.

For higher quality:

```java
private static final int THETA_STEPS = 240;
private static final int PHI_STEPS = 120;
```

For better performance:

```java
private static final int THETA_STEPS = 120;
private static final int PHI_STEPS = 60;
```

## How the 3D Rendering Works

The donut begins as mathematical 3D coordinates.

```text
3D donut
    ↓
Calculate surface normal
    ↓
Calculate lighting
    ↓
Create frosting on the same surface
    ↓
Rotate donut + frosting
    ↓
Perspective projection
    ↓
Depth sorting
    ↓
Draw particles
```

The frosting is generated before the 3D rotation.

This is what keeps it attached to the donut.

## Why There Are No Strawberry Pieces

Earlier versions contained separate red strawberry particles.

Those were removed because they were being rendered as independent objects and visually distracted from the donut.

The current version focuses on:

* Baked donut
* Thick strawberry frosting
* 3D frosting drips
* Lighting
* Shadows

## Performance

The renderer creates thousands of small particles every frame.

If the animation is slow on your computer, reduce:

```java
THETA_STEPS
```

and:

```java
PHI_STEPS
```

For example:

```java
private static final int THETA_STEPS = 140;
private static final int PHI_STEPS = 70;
```

If you want maximum visual quality and your computer can handle it:

```java
private static final int THETA_STEPS = 240;
private static final int PHI_STEPS = 120;
```

## Future Improvements

Possible improvements include:

* Real triangle-based 3D rendering
* Z-buffer rendering
* Specular reflections
* More realistic pastry texture
* Powdered sugar
* Chocolate glaze
* More realistic frosting drips
* Individual frosting highlights
* Better ambient occlusion
* Realistic soft shadows
* Mouse-controlled rotation
* Keyboard controls
* Zoom functionality
* JavaFX 3D rendering
* OpenGL hardware acceleration

## License

This project is provided for educational and personal use.

Feel free to modify the code, experiment with the lighting, change the frosting, and create your own donut variations.

---

### 🍩 Enjoy your 3D Strawberry Cream Donut!
