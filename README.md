# Cannon Clash

A two-player artillery duel where players take turns aiming cannons and firing projectiles at each other. Each shot is affected by wind and random projectile mass — heavier balls deal more damage but must clear a central wall to hit the opponent. Built with Java Swing.

## Features

- Mouse-aimed cannon with power scaling based on cursor distance
- Random projectile mass (1.0–6.0 kg) each turn — heavier = more damage + less wind drift
- Dynamic wind system affecting projectile trajectory
- Central wall obstacle blocking direct shots
- Full-screen pixel-art-style presentation
- How to Play screen with controls explained

## Controls

- Move mouse to aim
- Click to shoot (power increases with mouse distance from cannon)
- Left-click "Exit" during gameplay to return to menu

## Tech Stack

- Java
- Swing (JFrame, JPanel, Graphics2D)
- Custom pixel font rendering
- Image-based sprites with fallback procedural rendering
