import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

class MatrixMath {
    // Multiply two matrices
    public static float[][] multiplyMatrices(float[][] matrix1, float[][] matrix2) {
        if (matrix1[0].length != matrix2.length) {
            throw new IllegalArgumentException("Matrix dimensions do not match");
        }
        float[][] result = new float[matrix1.length][matrix2[0].length];
        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix2[0].length; j++) {
                for (int k = 0; k < matrix1[0].length; k++) {
                    result[i][j] += matrix1[i][k] * matrix2[k][j];
                }
            }
        }
        return result;
    }
}


class Vertex {
    public float[] position; // x, y, z, w (world space)

    public Vertex(float[] position) {
        this.position = position;
    }

    public float[] getScreenSpace(float[][] transformationMatrix) {
        // Create a 4x1 matrix with the position of the vertex
        float[][] positionMatrix = new float[4][1];
        for (int i = 0; i < 4; i++) {
            positionMatrix[i][0] = position[i];
        }
        
        // Multiply the position by the transformation matrix
        float[][] resultMatrix = MatrixMath.multiplyMatrices(transformationMatrix, positionMatrix);

        //normalize (note I dont understand why z and w are conventially normalized)
        float[] result = new float[4];
        result[0] = resultMatrix[0][0] / resultMatrix[3][0];
        result[1] = resultMatrix[1][0] / resultMatrix[3][0];
        result[2] = resultMatrix[2][0];
        result[3] = resultMatrix[3][0];

        return result;
    }
}

class Triangle {
    public Vertex[] vertices;
    public Color color;

    public Triangle(Vertex[] vertices, Color color) {
        this.color = color;
        this.vertices = vertices;
    }

    // Convert the triangle to screen space
    // This is done by multiplying the vertices by the transformation matrix
    // Then converting the x and y values to screen space
    public float[][] screenSpace(float[][] transformationMatrix, Camera camera) {
        float[][] result = new float[3][4];
        // convert vertices to screen space
        for (int i = 0; i < 3; i++) {
            result[i] = vertices[i].getScreenSpace(transformationMatrix);
        }
        //convert to screen space
        for (int i = 0; i < 3; i++) {
            result[i][0] = (result[i][0] + 1) * camera.width / 2;
            result[i][1] = (result[i][1] + 1) * camera.height / 2;
        }
        return result;
    }
}

class Camera {
    public float [] position; // x, y, z
    public float [] rotation; // x, y, z (radians)
    public int height; // pixels
    public int width; // pixels
    public float fov; // radians
    public float aspectRatio;

    public float[][] rotationMatrix;


    public Camera(float[] position, float[] rotation, int height, int width, float fov) {
        this.position = position;
        this.rotation = rotation;
        this.height = height;
        this.width = width;
        this.fov = (float) Math.toRadians(fov);
        this.aspectRatio = (float) height/width;
        this.rotationMatrix = new float[][] {
            {(float) Math.cos(this.rotation[0]) * (float) Math.cos(this.rotation[1]), (float) Math.cos(this.rotation[0]) * (float) Math.sin(this.rotation[1]) * (float) Math.sin(this.rotation[2]) - (float) Math.sin(this.rotation[0]) * (float) Math.cos(this.rotation[2]), (float) Math.cos(this.rotation[0]) * (float) Math.sin(this.rotation[1]) * (float) Math.cos(this.rotation[2]) + (float) Math.sin(this.rotation[0]) * (float) Math.sin(this.rotation[2]), 0},
            {(float) Math.sin(this.rotation[0]) * (float) Math.cos(this.rotation[1]), (float) Math.sin(this.rotation[0]) * (float) Math.sin(this.rotation[1]) * (float) Math.sin(this.rotation[2]) + (float) Math.cos(this.rotation[0]) * (float) Math.cos(this.rotation[2]), (float) Math.sin(this.rotation[0]) * (float) Math.sin(this.rotation[1]) * (float) Math.cos(this.rotation[2]) - (float) Math.cos(this.rotation[0]) * (float) Math.sin(this.rotation[2]), 0},
            {(float) -Math.sin(this.rotation[1]), (float) Math.cos(this.rotation[1]) * (float) Math.sin(this.rotation[2]), (float) Math.cos(this.rotation[1]) * (float) Math.cos(this.rotation[2]), 0},
            {0, 0, 0, 1}
        };
    }

    public void rotateX (float angle) {
        this.rotation[0] += angle;
        float[][] xRotationMatrix = new float[][] {
            {1, 0, 0, 0},
            {0, (float) Math.cos(this.rotation[0]), (float) -Math.sin(this.rotation[0]), 0},
            {0, (float) Math.sin(this.rotation[0]), (float) Math.cos(this.rotation[0]), 0},
            {0, 0, 0, 1}
        };
        this.rotationMatrix = MatrixMath.multiplyMatrices(xRotationMatrix, this.rotationMatrix);
    }

    public void rotateY (float angle) {
        this.rotation[1] += angle;
        float[][] yRotationMatrix = new float[][] {
            {(float) Math.cos(this.rotation[1]), 0, (float) Math.sin(this.rotation[1]), 0},
            {0, 1, 0, 0},
            {(float) -Math.sin(this.rotation[1]), 0, (float) Math.cos(this.rotation[1]), 0},
            {0, 0, 0, 1}
        };
        this.rotationMatrix = MatrixMath.multiplyMatrices(yRotationMatrix, this.rotationMatrix);
    }

    public void rotateZ (float angle) {
        this.rotation[2] += angle;
        float[][] zRotationMatrix = new float[][] {
            {(float) Math.cos(this.rotation[2]), (float) -Math.sin(this.rotation[2]), 0, 0},
            {(float) Math.sin(this.rotation[2]), (float) Math.cos(this.rotation[2]), 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}
        };
        this.rotationMatrix = MatrixMath.multiplyMatrices(zRotationMatrix, this.rotationMatrix);
    }
}

// Unused
class TriangleMesh {
    public Triangle[] triangles;
    public int[] position;

    public TriangleMesh(Triangle[] triangles, int x, int y, int z) {
        this.triangles = triangles;
        this.position = new int[] {x, y, z};
    }

    public TriangleMesh(Triangle[] triangles, int[] position) {
        this.triangles = triangles;
        this.position = position;
    }
}


public class GraphicsEngine {
    public static void main(String[] args) {
        // Create a window
        JFrame frame = new JFrame("3D Engine");
        frame.setSize(800,600);
        BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        JLabel fpsLabel = new JLabel("FPS: 0");
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, null);
            }
        };
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(fpsLabel, BorderLayout.SOUTH);
        frame.setVisible(true);

        // Main camera
        Camera camera = new Camera(new float[] {0, 0, 0}, new float[] {0, (float) Math.PI/ 4, 0}, image.getHeight(), image.getWidth(), 90);

        
        // Key listener adds and removes keys from the pressed keys set
        Set<Integer> pressedKeys = new HashSet<>();
        KeyListener keyListener = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                pressedKeys.add(e.getKeyCode());
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                pressedKeys.remove(e.getKeyCode());
            }
            
            @Override
            public void keyTyped(KeyEvent e) {
                
            }
        };
        frame.addKeyListener(keyListener);

        // Thread for camera movement by checking if keys are in the pressed keys set, so it is independent of the frame rate
        // in a thread so movement is smooth and so the movement can be paused
        Thread movementControls = new Thread(() -> {
            while (true) {
                float movementSpeed = 0.1f;
                float rotationSpeed = 0.001f;
                if (!pressedKeys.isEmpty()) {

                    // Movement (only relative to the camera's y axis rotation)
                    if (pressedKeys.contains(KeyEvent.VK_W)) {
                        camera.position[2] -= movementSpeed * Math.cos(-camera.rotation[1]);
                        camera.position[0] -= movementSpeed * Math.sin(-camera.rotation[1]);
                    }
                    if (pressedKeys.contains(KeyEvent.VK_S)) {
                        camera.position[2] += movementSpeed * Math.cos(-camera.rotation[1]);
                        camera.position[0] += movementSpeed * Math.sin(-camera.rotation[1]);
                    }
                    if (pressedKeys.contains(KeyEvent.VK_A)) {
                        camera.position[2] += movementSpeed * Math.sin(-camera.rotation[1]);
                        camera.position[0] -= movementSpeed * Math.cos(-camera.rotation[1]);
                    }
                    if (pressedKeys.contains(KeyEvent.VK_D)) {
                        camera.position[2] -= movementSpeed * Math.sin(-camera.rotation[1]);
                        camera.position[0] += movementSpeed * Math.cos(-camera.rotation[1]);
                    }
                    if (pressedKeys.contains(KeyEvent.VK_R)) {
                        camera.position[1] -= movementSpeed;
                    }
                    if (pressedKeys.contains(KeyEvent.VK_F)) {
                        camera.position[1] += movementSpeed;
                    }

                    // Rotation (currently not working)
                    // it is currently in world space, not camera space
                    // it should take into account the camera's current rotation
                    if (pressedKeys.contains(KeyEvent.VK_UP)) {
                        camera.rotateX(rotationSpeed);
                    }
                    if (pressedKeys.contains(KeyEvent.VK_DOWN)) {
                        camera.rotateX(-rotationSpeed);
                    }
                    if (pressedKeys.contains(KeyEvent.VK_LEFT)) {
                        camera.rotateY(rotationSpeed);
                    }
                    if (pressedKeys.contains(KeyEvent.VK_RIGHT)) {
                        camera.rotateY(-rotationSpeed);
                    }
                    if (pressedKeys.contains(KeyEvent.VK_Q)) {
                        camera.rotateZ(rotationSpeed);
                    }
                    if (pressedKeys.contains(KeyEvent.VK_E)) {
                        camera.rotateZ(-rotationSpeed);
                    }

                    // Debugging
                    if (pressedKeys.contains(KeyEvent.VK_T)) {
                        System.out.println(camera.position[0] + " " + camera.position[1] + " " + camera.position[2]);
                    }
                    if (pressedKeys.contains(KeyEvent.VK_Y)) {
                        System.out.println(camera.rotation[0] + " " + camera.rotation[1] + " " + camera.rotation[2]);
                    }
                }
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    // Its fineee
                }
            }
        });

        movementControls.start();


        


        // Create a triangle for testing
        // this is just for testing and will be replaced with a more general solution
        Triangle tri = new Triangle(new Vertex[] {
            new Vertex(new float[] {0, 0, -1, 1}),
            new Vertex(new float[] {1, 1, -1, 1}),
            new Vertex(new float[] {0, 1, -1, 1})
        }, Color.RED);
        

        // Thread for updating the screen
        Thread update = new Thread(() -> {
            long lastFrameTime = System.nanoTime();
            int frameCount = 0;
            while (true) {
                // Calculate FPS
                long currentTime = System.nanoTime();
                frameCount++;
                if (currentTime - lastFrameTime >= 1_000_000_000) {
                    fpsLabel.setText("FPS: " + frameCount);
                    frameCount = 0;
                    lastFrameTime = currentTime;
                }

                //clear the image
                // needs heavy optimization
                // fps goes from 200_000 to 150
                // using the built in clearRect is 7000 fps, but causes flickering (maybe some form of vsync as a fix?)
                
                for (int j = 0; j < 800; j++) {
                    for (int k = 0; k < 600; k++) {
                        image.setRGB(j, k, 0);
                    }
                }


                //translation matrix
                float[][] translationMatrix = {
                    {1, 0, 0, -camera.position[0]},
                    {0, 1, 0, -camera.position[1]},
                    {0, 0, 1, -camera.position[2]},
                    {0, 0, 0, 1}
                };

                
                //perspective projection matrix
                // potential optimization: only calculate the tan of the fov once
                // additionally im not confident that this is the correct way to calculate the perspective projection matrix
                // there are multiple ways varying slightly in the values of the matrix
                // for example, the 3rd row could be {0, 0, (far+near)/(far - near), -far * near/(far - near)}
                // and the 4th row could be {0, 0, 1, 0}
                // this would make the z axis increase as it moves away from the camera
                // and i have no idea what the multiplication by 2 is for

                //but this does seem to work
                float near = 0.1f;
                float far = 100f;
                float[][] perspectiveProjectionMatrix = {
                    {camera.aspectRatio * ((float)Math.tan(camera.fov/2)), 0, 0, 0},
                    {0, 1/((float)Math.tan(camera.fov/2)), 0, 0},
                    {0, 0, -(far+near)/(far - near), -2*far * near/(far - near)},
                    {0, 0, -1, 0}
                };
                
                //multiply the matrices
                // can be optimized by multiplying the matrices by hand once and hardcoding the cells (maybe an improvement?)
                float[][] transformationMatrix = MatrixMath.multiplyMatrices(perspectiveProjectionMatrix, camera.rotationMatrix);
                float[][] fullMatrix = MatrixMath.multiplyMatrices(transformationMatrix, translationMatrix);

                // get the screen space of the triangle based on this transformation matrix
                float[][] result = tri.screenSpace(fullMatrix, camera);


                // draw a crosshair in the center of the screen (just a white pixel)
                image.setRGB(camera.width / 2, camera.height / 2, Color.WHITE.getRGB());



                Graphics g = image.getGraphics();
                // dont draw if the triangle is behind the camera
                // this needs significant improvement as it doesnt properly check if the triangle is behind the camera
                // it could be done before the transformation matrix is calculated
                // it also only applies to the one triangle, not a general solution
                if (result[0][2] > 0 && result[1][2] > 0 && result[2][2] > 0) {
                    g.setColor(tri.color);
                    g.fillPolygon(new int[] {(int) result[0][0], (int) result[1][0], (int) result[2][0]}, new int[] {(int) result[0][1], (int) result[1][1], (int) result[2][1]}, 3);
                }
                panel.repaint();
            }
        });

        update.start();
    }
}