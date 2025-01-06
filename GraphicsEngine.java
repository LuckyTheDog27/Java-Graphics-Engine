import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

class Mat {
    float[][] m;

    public Mat(float[][] m) {
        this.m = m;
    }

    public Mat multiply(Mat m2) {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = 0;
                for (int k = 0; k < 4; k++) {
                    result[i][j] += this.m[i][k] * m2.m[k][j];
                }
            }
        }
        return new Mat(result);
    }

    public Mat makeIdentity() {
        float[][] result = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i][j] = (i == j) ? 1 : 0;
            }
        }
        return new Mat(result);
    }
}

class Vector3D {
    public float x, y, z, w;

    public Vector3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = 1;
    }

    public Vector3D(float[] position) {
        this.x = position[0];
        this.y = position[1];
        this.z = position[2];
        this.w = (position.length > 3) ? position[3] : 1;
    }

    public Vector3D(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vector3D multiply(float[][] m) {
        return new Vector3D(
            this.x * m[0][0] + this.y * m[1][0] + this.z * m[2][0] + this.w * m[3][0],
            this.x * m[0][1] + this.y * m[1][1] + this.z * m[2][1] + this.w * m[3][1],
            this.x * m[0][2] + this.y * m[1][2] + this.z * m[2][2] + this.w * m[3][2],
            this.x * m[0][3] + this.y * m[1][3] + this.z * m[2][3] + this.w * m[3][3]
        );
    }

    public Vector3D add(Vector3D v) {
        return new Vector3D(this.x + v.x, this.y + v.y, this.z + v.z);
    }
    
    public Vector3D subtract(Vector3D v) {
        return new Vector3D(this.x - v.x, this.y - v.y, this.z - v.z);
    }

    public Vector3D multiply(float s) {
        return new Vector3D(this.x * s, this.y * s, this.z * s);
    }

    public Vector3D multiply(Mat m) {
        return new Vector3D(
            this.x * m.m[0][0] + this.y * m.m[1][0] + this.z * m.m[2][0] + this.w * m.m[3][0],
            this.x * m.m[0][1] + this.y * m.m[1][1] + this.z * m.m[2][1] + this.w * m.m[3][1],
            this.x * m.m[0][2] + this.y * m.m[1][2] + this.z * m.m[2][2] + this.w * m.m[3][2],
            this.x * m.m[0][3] + this.y * m.m[1][3] + this.z * m.m[2][3] + this.w * m.m[3][3]
        );
    }

    public Vector3D divide(float s) {
        return new Vector3D(this.x / s, this.y / s, this.z / s);
    }

    public float dot(Vector3D v) {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    public float length() {
        return (float) Math.sqrt(dot(this));
    }

    public Vector3D normalize() {
        return divide(length());
    }

    public Vector3D cross(Vector3D v) {
        return new Vector3D(this.y * v.z - this.z * v.y, this.z * v.x - this.x * v.z, this.x * v.y - this.y * v.x);
    }

    public Vector3D rotate(Quaternion q) {
        Quaternion p = new Quaternion(0, this);
        Quaternion qConjugate = new Quaternion(q.w, -q.x, -q.y, -q.z);
        Quaternion result = q.multiply(p).multiply(qConjugate);
        return new Vector3D(result.x, result.y, result.z);
    }
}

class Quaternion extends Vector3D {
    public Quaternion(float w, float x, float y, float z) {
        super(x, y, z, w);
    }

    public Quaternion(float w, Vector3D v) {
        super(v.x, v.y, v.z, w);
    }

    public Quaternion(Vector3D v) {
        super(v.x, v.y, v.z, 0);
    }

    public Quaternion multiply(Quaternion q) {
        return new Quaternion(
            this.w * q.w - this.x * q.x - this.y * q.y - this.z * q.z,
            this.w * q.x + this.x * q.w + this.y * q.z - this.z * q.y,
            this.w * q.y - this.x * q.z + this.y * q.w + this.z * q.x,
            this.w * q.z + this.x * q.y - this.y * q.x + this.z * q.w
        );
    }

    public Quaternion createFromAxisAngle(Vector3D axis, float angle) {
        float sinHalfAngle = (float) Math.sin(angle / 2);
        float cosHalfAngle = (float) Math.cos(angle / 2);
        return new Quaternion(cosHalfAngle, axis.multiply(sinHalfAngle));
    }

}


class Triangle {
    public Vector3D[] v = new Vector3D[3];
    public Color color = Color.WHITE;

    public Triangle(Vector3D v1, Vector3D v2, Vector3D v3, Color color) {
        this.v = new Vector3D[] {v1, v2, v3};
        this.color = color;
    }

    public Triangle(Vector3D[] vertices, Color color) {
        this.v = vertices;
        this.color = color;
    }

    public Triangle(Vector3D[] vertices) {
        this.v = vertices;
    }

    public Triangle(Vector3D v1, Vector3D v2, Vector3D v3) {
        this.v = new Vector3D[] {v1, v2, v3};
    }

    public Triangle add(Vector3D v) {
        Vector3D[] newVertices = new Vector3D[3];
        for (int i = 0; i < 3; i++) {
            newVertices[i] = this.v[i].add(v);
        }
        return new Triangle(newVertices, color);
    }

    public Triangle subtract(Vector3D v) {
        Vector3D[] newVertices = new Vector3D[3];
        for (int i = 0; i < 3; i++) {
            newVertices[i] = this.v[i].subtract(v);
        }
        return new Triangle(newVertices, color);
    }

    public Triangle multiply(Mat m) {
        Vector3D[] newVertices = new Vector3D[3];
        for (int i = 0; i < 3; i++) {
            newVertices[i] = this.v[i].multiply(m);
        }
        return new Triangle(newVertices, color);
    }

    public Triangle divide(float s) {
        Vector3D[] newVertices = new Vector3D[3];
        for (int i = 0; i < 3; i++) {
            newVertices[i] = this.v[i].divide(s);
        }
        return new Triangle(newVertices, color);
    }

   
}

class Mesh {
    public List<Triangle> triangles;
    public Vector3D position;
    public Quaternion rotation;

    public Mesh() {
        this.triangles = new ArrayList<>();
        this.position = new Vector3D(0, 0, 0);
        this.rotation = new Quaternion(1, 0, 0, 0);
    }

    public Mesh(List<Triangle> triangles, int x, int y, int z) {
        this.triangles = triangles;
        this.position = new Vector3D(x, y, z);
        this.rotation = new Quaternion(1, 0, 0, 0);
    }

    public Mesh(List<Triangle> triangles, int[] position) {
        this.triangles = triangles;
        this.position = new Vector3D(position[0], position[1], position[2]);
    }

    public Mesh(File file) {
        this(file, new Vector3D(0, 0, 0));
    }

    public Mesh(File file, Vector3D position) {
        this.triangles = new ArrayList<>();
        this.position = position;
        List<Vector3D> vertices = new ArrayList<>();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("v ")) {
                    String[] parts = line.split(" ");
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    float z = Float.parseFloat(parts[3]);
                    vertices.add(new Vector3D(x, y, z));
                } else if (line.startsWith("f ")) {
                    String[] parts = line.split(" ");
                    int v1 = Integer.parseInt(parts[1]) - 1;
                    int v2 = Integer.parseInt(parts[2]) - 1;
                    int v3 = Integer.parseInt(parts[3]) - 1;
                    triangles.add(new Triangle(vertices.get(v1), vertices.get(v2), vertices.get(v3)));
                }
            }
        } catch (Exception e) {
            
        }
    }
}

final class Camera {
    public Vector3D position; // x, y, z
    public Quaternion rotation; // w, x, y, z
    public int height; // pixels
    public int width; // pixels
    public float fov; // degrees
    public float fovRad; // radians
    public float f; // 1/tan(fov/2)
    public float far; // far clipping plane
    public float near; // near clipping plane
    public float aspectRatio; // height/width
    public Mat perspectiveProjectionMatrix;

    public Mat rotationMatrix() {
        return new Mat( new float[][] {
            {2.0f * (rotation.w * rotation.w + rotation.x * rotation.x) - 1.0f, 2.0f * (rotation.x * rotation.y - rotation.w * rotation.z), 2.0f * (rotation.x * rotation.z + rotation.w * rotation.y), 0},
            {2.0f * (rotation.x * rotation.y + rotation.w * rotation.z), 2.0f * (rotation.w * rotation.w + rotation.y * rotation.y) - 1.0f, 2.0f * (rotation.y * rotation.z - rotation.w * rotation.x), 0},
            {2.0f * (rotation.x * rotation.z - rotation.w * rotation.y), 2.0f * (rotation.y * rotation.z + rotation.w * rotation.x), 2.0f * (rotation.w * rotation.w + rotation.z * rotation.z) - 1.0f, 0},
            {0, 0, 0, 1}
        });
    }

    public Mat translationMatrix() {
        return new Mat(new float[][] {
            {1, 0, 0, position.x},
            {0, 1, 0, position.y},
            {0, 0, 1, position.z},
            {0, 0, 0, 1}
        });
    }

    public void updatePerspectiveProjectionMatrix() {
        perspectiveProjectionMatrix = new Mat(new float[][] {
            {aspectRatio * f, 0, 0, 0},
            {0, f, 0, 0},
            {0, 0, far / (far - near), -far * near / (far - near)},
            {0, 0, -1, 0}
        });
    }

    public Vector3D scale(Vector3D v) {
        return new Vector3D((v.x + 1) * width / 2, (v.y + 1) * height / 2, v.z);
    }

    public Triangle scale(Triangle t) {
        return new Triangle(scale(t.v[0]), scale(t.v[1]), scale(t.v[2]));
    }
    

    public Camera(Vector3D position, Quaternion rotation, int height, int width, float fov, float far, float near) {
        this.position = position;
        this.rotation = rotation;
        this.height = height;
        this.width = width;
        this.fov = fov;
        this.fovRad = (float) Math.toRadians(fov);
        this.aspectRatio = (float) height / (float) width;
        this.f = 1 / (float) Math.tan(this.fovRad/2);
        this.far = far;
        this.near = near;
        updatePerspectiveProjectionMatrix();
    }
}

class Screen {
    public int width;
    public int height;
    public BufferedImage image;
    public Graphics g;
    public JFrame frame;
    public JLabel fpsLabel;
    public Set<Integer> keys = new HashSet<>();

    public Screen(int width, int height) {
        // Create a window
        frame = new JFrame("3D Engine");
        this.width = width;
        this.height = height;
        frame.setSize(width, height);
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        fpsLabel = new JLabel("FPS: 0");
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, null);
            }
        };
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(fpsLabel, BorderLayout.SOUTH);
        KeyListener listener = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
                keys.add(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                keys.remove(e.getKeyCode());
            }
        };
        frame.addKeyListener(listener);
        frame.setVisible(true);
    }

    public void clear() {
        for (int i = 0; i < width * height; i++) {
            image.setRGB(i % width, i / width, 0);
        }
    }

    public void clear(boolean[][] needsBlackening, boolean[][] updated) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (needsBlackening[i][j] && !updated[i][j]) {
                    image.setRGB(i, j, 0);
                }
            }
        }
    }

    public void drawTriangle(float x1, float y1, float x2, float y2, float x3, float y3, Color color) {
        g = image.getGraphics();
        g.setColor(color);
        g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
        g.drawLine((int) x2, (int) y2, (int) x3, (int) y3);
        g.drawLine((int) x3, (int) y3, (int) x1, (int) y1);
    }

    public void fillTriangle(float x1, float y1, float x2, float y2, float x3, float y3, Color color) {
        g = image.getGraphics();
        g.setColor(color);
        g.fillPolygon(new int[] {(int) x1, (int) x2, (int) x3}, new int[] {(int) y1, (int) y2, (int) y3}, 3);
    }

    public void fillTriangleWithDepth(Triangle tri, float[][] depthBuffer, boolean [][] updated) {
        g = image.getGraphics();
        g.setColor(tri.color);

        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        for (int i = 0; i < 3; i++) {
            minX = Math.min(minX, tri.v[i].x);
            minY = Math.min(minY, tri.v[i].y);
            maxX = Math.max(maxX, tri.v[i].x);
            maxY = Math.max(maxY, tri.v[i].y);
        }

        minX = Math.max(0, Math.min(width, minX));
        minY = Math.max(0, Math.min(height, minY));
        maxX = Math.max(0, Math.min(width, maxX));
        maxY = Math.max(0, Math.min(height, maxY));

        for (int x = (int) minX; x < maxX; x++) {
            for (int y = (int) minY; y < maxY; y++) {
            float w0 = ((tri.v[1].y - tri.v[2].y) * (x - tri.v[2].x) + (tri.v[2].x - tri.v[1].x) * (y - tri.v[2].y)) /
                   ((tri.v[1].y - tri.v[2].y) * (tri.v[0].x - tri.v[2].x) + (tri.v[2].x - tri.v[1].x) * (tri.v[0].y - tri.v[2].y));
            float w1 = ((tri.v[2].y - tri.v[0].y) * (x - tri.v[2].x) + (tri.v[0].x - tri.v[2].x) * (y - tri.v[2].y)) /
                   ((tri.v[1].y - tri.v[2].y) * (tri.v[0].x - tri.v[2].x) + (tri.v[2].x - tri.v[1].x) * (tri.v[0].y - tri.v[2].y));
            float w2 = 1 - w0 - w1;

            if (w0 >= 0 && w1 >= 0 && w2 >= 0) {
                float z = w0 * tri.v[0].z + w1 * tri.v[1].z + w2 * tri.v[2].z;
                if (z < depthBuffer[x][y]) {
                    depthBuffer[x][y] = z;
                    image.setRGB(x, y, tri.color.getRGB());
                    updated[x][y] = true;
                }
            }
            }
        }

    }

    public void setFPS(long fps) {
        fpsLabel.setText("FPS: " + fps);
    }

    public void repaint() {
        frame.repaint();
    }
}

public class GraphicsEngine {
    private static Mesh objectMesh;
    private static final Screen screen = new Screen(1600, 900);
    private static Thread updateThread;
    private static Camera camera;
    private static final float[][] depthBuffer = new float[screen.width][screen.height];

    private static void clearDepthBuffer() {
        for (int i = 0; i < screen.width; i++) {
            for (int j = 0; j < screen.height; j++) {
                depthBuffer[i][j] = Float.POSITIVE_INFINITY;
            }
        }
    }

    public static boolean[][] update(float fElapsedTime, boolean[][] needsBlackening) {
        clearDepthBuffer();
        boolean[][] updated = new boolean[screen.width][screen.height];

        if (screen.keys.contains(KeyEvent.VK_W)) {
            camera.position = camera.position.add(new Vector3D(0, 0, -80.0f * fElapsedTime).rotate(camera.rotation));
        }
        if (screen.keys.contains(KeyEvent.VK_S)) {
            camera.position = camera.position.add(new Vector3D(0, 0, 80.0f * fElapsedTime).rotate(camera.rotation));
        }
        if (screen.keys.contains(KeyEvent.VK_A)) {
            camera.position = camera.position.add(new Vector3D(-80.0f * fElapsedTime, 0, 0).rotate(camera.rotation));
        }
        if (screen.keys.contains(KeyEvent.VK_D)) {
            camera.position = camera.position.add(new Vector3D(80.0f * fElapsedTime, 0, 0).rotate(camera.rotation));
        }

        if (screen.keys.contains(KeyEvent.VK_UP)) {
            camera.rotation = camera.rotation.createFromAxisAngle(new Vector3D(1, 0, 0), 0.001f).multiply(camera.rotation);
        }
        if (screen.keys.contains(KeyEvent.VK_DOWN)) {
            camera.rotation = camera.rotation.createFromAxisAngle(new Vector3D(1, 0, 0), -0.001f).multiply(camera.rotation);
        }
        if (screen.keys.contains(KeyEvent.VK_LEFT)) {
            camera.rotation = camera.rotation.createFromAxisAngle(new Vector3D(0, 1, 0), 0.001f).multiply(camera.rotation);
        }
        if (screen.keys.contains(KeyEvent.VK_RIGHT)) {
            camera.rotation = camera.rotation.createFromAxisAngle(new Vector3D(0, 1, 0), -0.001f).multiply(camera.rotation);
        }
        camera.updatePerspectiveProjectionMatrix();
        


        // Draw Triangles
        for (Triangle tri : objectMesh.triangles) {

            Triangle worldSpace = tri;
            worldSpace = worldSpace.add(objectMesh.position);

            // Get Normal
            Vector3D line1 = worldSpace.v[1].subtract(worldSpace.v[0]);
            Vector3D line2 = worldSpace.v[2].subtract(worldSpace.v[0]);
            Vector3D normal = line1.cross(line2).normalize();

            worldSpace = worldSpace.subtract(camera.position);
            worldSpace = worldSpace.multiply(camera.rotationMatrix());

            if (
                normal.x * (worldSpace.v[0].x - camera.position.x) +
                normal.y * (worldSpace.v[0].y - camera.position.y) +
                normal.z * (worldSpace.v[0].z - camera.position.z) < 0
            ) {
                Vector3D lightDirection = camera.position.subtract(worldSpace.v[0]).normalize();

                float dp = Math.max(0.1f, normal.dot(lightDirection));
                
                for (int i = 0; i < 3; i++) {
                    worldSpace.v[i] = worldSpace.v[i].multiply(camera.perspectiveProjectionMatrix);
                    worldSpace.v[i] = worldSpace.v[i].divide(worldSpace.v[i].w);
                }
                
                worldSpace = camera.scale(worldSpace);
                
                worldSpace.color = new Color((int) (255 * dp), (int) (255 * dp), (int) (255 * dp));
                
                screen.fillTriangleWithDepth(worldSpace, depthBuffer, updated);
            }
        }
        screen.clear(needsBlackening, updated);
        screen.repaint();
        return updated;
    }

    public GraphicsEngine() {
        objectMesh = new Mesh(new File("VideoShip.obj"));
        objectMesh.position = new Vector3D(0, 0, -100);


        // Main camera
        camera = new Camera(new Vector3D(0, 0, 0), new Quaternion(1,0,0,0), screen.height, screen.width, 90, 1000, 0.1f);
        
        // Thread for updating the screen
        updateThread = new Thread(() -> {
            System.out.println("Graphics Engine Running");
            long frames = 0;
            long lastFrameCheck = System.nanoTime();
            long lastTime = System.nanoTime();
            boolean[][] needsBlackening = new boolean[screen.width][screen.height];
            while (true) {
                frames++;
                long now = System.nanoTime();
                if (now - lastFrameCheck >= 1000000000) {
                    screen.setFPS(frames);
                    frames = 0;
                    lastFrameCheck = now;
                }
                needsBlackening = update((float) ((now - lastTime) / 1000000000.0), needsBlackening);
                lastTime = now;
            }
        });

        System.out.println("Graphics Engine Initialized");
    }

    public static void start() {
        updateThread.start();
        
        System.out.println("Graphics Engine Started");
    }

    public static void main(String[] args) {
        @SuppressWarnings("unused")
        GraphicsEngine engine = new GraphicsEngine();
        start();
    }
}
