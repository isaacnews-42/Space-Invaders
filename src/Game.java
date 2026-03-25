import java.awt.*;
import java.awt.event.*;
import java.security.Key;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Game extends JPanel implements ActionListener, KeyListener {

    class Block{
        int x;
        int y;
        int width;
        int height;
        Image image;
        boolean alive = true;
        boolean used = false;

        Block(int x, int y, int width, int height, Image image){
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.image = image;
        }
    }
    int tileSize = 32;
    int rows = 16;
    int columns = 16;
    int boardWidth = tileSize * columns;
    int boardHeight = tileSize * rows;

    Image shipImage;
    Image alienImage;
    Image alienCyanImage;
    Image alienMagentaImage;
    Image alienYellowImage;
    ArrayList<Image> alienImageArray;

    int shipWidth = tileSize * 2;
    int shipHeight = tileSize;
    int shipX = tileSize * columns / 2 - tileSize;
    int shipY = boardHeight - tileSize * 2;
    int shipVelocityX = tileSize;
    Block ship;

    ArrayList<Block> aliensArray;
    int alienWidth = tileSize * 2;
    int alienHeight = tileSize;
    int alienX = tileSize;
    int alienY = tileSize;

    int alienRows = 2;
    int alienColumns = 3;
    int alienCount = 0;
    int alienVelocityX = 1;

    ArrayList<Block> bulletsArray;
    int bulletWidth = tileSize / 8;
    int bulletHeight = tileSize / 2;
    int bulletVelocityY = -10;
    Timer gameLoop;
    int score = 0;
    boolean gameOver = false;

    Game(){
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        shipImage = new ImageIcon("spaceinvadercharacters/ship.png").getImage();
        alienImage = new ImageIcon("spaceinvadercharacters/alien.png").getImage();
        alienCyanImage = new ImageIcon("spaceinvadercharacters/alien-cyan.png").getImage();
        alienMagentaImage = new ImageIcon("spaceinvadercharacters/alien-magenta.png").getImage();
        alienYellowImage = new ImageIcon("spaceinvadercharacters/alien-yellow.png").getImage();

        alienImageArray = new ArrayList<Image>();
        alienImageArray.add(alienImage);
        alienImageArray.add(alienCyanImage);
        alienImageArray.add(alienMagentaImage);
        alienImageArray.add(alienYellowImage);

        ship = new Block(shipX, shipY, shipWidth, shipHeight, shipImage);
        aliensArray = new ArrayList<Block>();
        bulletsArray = new ArrayList<Block>();

        gameLoop = new Timer(1000/60, this);
        createAliens();
        gameLoop.start();
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g){
        g.drawImage(ship.image, ship.x, ship.y, ship.width, ship.height, null);

        for(int i = 0; i < aliensArray.size(); i++){
            Block alien = aliensArray.get(i);
            if(alien.alive){
                g.drawImage(alien.image, alien.x, alien.y, alien.width, alien.height, null);
            }
        }

        g.setColor(Color.WHITE);
        for(int i = 0; i < bulletsArray.size(); i++){
            Block bullet = bulletsArray.get(i);
            if(!bullet.used){
                g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
            }
        }
        g.setColor(Color.WHITE);
        g.setFont(new Font("Tahoma", Font.BOLD, 32));
        if(gameOver){
            g.drawString("Game over! Final score: " + String.valueOf(score), 10, 35);
            g.drawString("Press Space to restart", 10, 85);
            g.drawString("Press Escape to exit", 10, 135);
        }
        else{
            g.drawString(String.valueOf(score), 10, 35);
        }
    }

    public void move(){
        for(int i = 0; i < aliensArray.size(); i++){
            Block alien = aliensArray.get(i);
            if(alien.alive){
                alien.x += alienVelocityX;

                if (alien.x + alien.width >= boardWidth || alien.x <= 0){
                    alienVelocityX *= -1;
                    alien.x += alienVelocityX * 2;

                    for (int j = 0; j < aliensArray.size(); j++){
                        aliensArray.get(j).y += alienHeight;
                    }

                }

                if(alien.y >= ship.y){
                    gameOver = true;
                }

            }

            while(bulletsArray.size() > 0 && (bulletsArray.get(0).used || bulletsArray.get(0).y < 0)){
                bulletsArray.remove(0);
            }

            if(alienCount == 0){
                score += 500;
                alienColumns = Math.min(alienColumns + 1, columns / 2 - 2);
                alienRows = Math.min(alienRows + 1, rows - 6);
                aliensArray.clear();
                bulletsArray.clear();
                createAliens();
            }
        }

        for(int i = 0; i < bulletsArray.size(); i++){
            Block bullet = bulletsArray.get(i);
            bullet.y += bulletVelocityY;

            for(int k = 0; k < aliensArray.size(); k++){
                Block alien = aliensArray.get(k);
                if(!bullet.used && alien.alive && detectCollision(bullet, alien)){
                    bullet.used = true;
                    alien.alive = false;
                    alienCount--;
                    score += 100;
                }

            }
        }
    }

    public void createAliens(){
        Random random = new Random();
        for(int a = 0; a < alienRows; a++){
            for(int b = 0; b < alienColumns; b++){
                int randomImageIndex = random.nextInt(alienImageArray.size());
                Block alien = new Block(alienX + b*alienWidth, alienY + a*alienHeight, alienWidth, alienHeight, alienImageArray.get(randomImageIndex));
                aliensArray.add(alien);
            }
        }
        alienCount = aliensArray.size();
    }

    public boolean detectCollision(Block a, Block b){
        return a.x < b.x + b.width && a.x + a.width > b.x && a.y < b.y + b.height && a.y + a.height > b.y;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if(gameOver){
            gameLoop.stop();
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_LEFT && ship.x - shipVelocityX >= 0){
            ship.x -= shipVelocityX;
        }
        else if(e.getKeyCode() == KeyEvent.VK_RIGHT && ship.x + ship.width + shipVelocityX <= boardWidth){
            ship.x += shipVelocityX;
        }
        else if(e.getKeyCode() == KeyEvent.VK_SPACE){
            if(gameOver){
                ship.x = shipX;
                bulletsArray.clear();
                aliensArray.clear();
                score = 0;
                alienVelocityX = 1;
                alienColumns = 3;
                alienRows = 2;
                gameOver = false;
                createAliens();
                gameLoop.start();
            }
            else {
                Block bullet = new Block(ship.x + shipWidth * 15 / 32, ship.y, bulletWidth, bulletHeight, null);
                bulletsArray.add(bullet);
            }
        }
        else if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
            System.exit(0);
        }
    }

}
