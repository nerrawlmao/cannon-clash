package codes;

import java.awt.*;

public class Player {
    public int hp;
    public Rectangle hitbox;
    public Color color;
    public int id;
    public int cannonX, cannonY;
    
    public Player(int id, int x, int y, int width, int height, Color color) {
        this.id = id;
        this.hp = 100;
        this.hitbox = new Rectangle(x, y, width, height);
        this.color = color;
    }
    
    public void takeDamage(int damage) {
        hp -= damage;
        if (hp < 0) hp = 0;
    }
    
    public boolean isAlive() {
        return hp > 0;
    }
}