package codes;

import java.awt.*;

public class Projectile {
    public double x, y;
    public double vx, vy;
    public double radius;
    public boolean active;
    public double mass;
    
    public static final double GRAVITY = 900.0;
    private static final double WIND_FORCE_FACTOR = 1.5;
    
    public Projectile(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
        this.active = false;
    }
    
    public void launch(double startX, double startY, double vx0, double vy0) {
        this.x = startX;
        this.y = startY;
        this.vx = vx0;
        this.vy = vy0;
        this.active = true;
    }
    
    public void update(double dt, double windVelocity) {
        if (!active) return;
        double windAcc = (windVelocity * WIND_FORCE_FACTOR) / mass;
        vx += windAcc * dt;
        vy += GRAVITY * dt;
        x += vx * dt;
        y += vy * dt;
    }
    
    public void draw(Graphics2D g2d) {
        if (!active) return;
        g2d.setColor(Color.BLACK);
        g2d.fillOval((int)(x - radius), (int)(y - radius), (int)(2*radius), (int)(2*radius));
    }
    
    public boolean isOutOfBounds(int screenWidth, int screenHeight) {
        return (x + radius < 0 || x - radius > screenWidth ||
                y + radius < 0 || y - radius > screenHeight + 200);
    }
}