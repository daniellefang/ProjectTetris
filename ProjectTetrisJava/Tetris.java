import java.applet.*;
import java.awt.*;

/**
 * A Tetris Applet
 * (Please set the size to 200 X 400)
 *
 * @author Danielle F. and Jina Y.
 * @date 05.28.2015
 */

public class Tetris extends Applet implements Runnable 
{
    private Thread thread;
    private int[][] screen;
    private Image image = null;
    private Graphics graphics = null;
    // in millisec
    private int speed;

    private static final int[][][][] pieces = {
        {   // ####
            {{0,1},{0,0},{0,-1},{0,-2}},
            {{-1,0},{0,0},{1,0},{2,0}},
            {{0,1},{0,0},{0,-1},{0,-2}},
            {{-1,0},{0,0},{1,0},{2,0}}
        },
        {   // #
            // ##
            // #
            {{-1,0},{0,1},{1,0},{0,0}},
            {{0,1},{1,0},{0,-1},{0,0}},
            {{1,0},{0,-1},{-1,0},{0,0}},
            {{0,-1},{-1,0},{0,1},{0,0}}
        },
        {
            // ##
            // ##
            {{0,0},{0,-1},{1,0},{1,-1}},
            {{0,0},{0,-1},{1,0},{1,-1}},
            {{0,0},{0,-1},{1,0},{1,-1}},
            {{0,0},{0,-1},{1,0},{1,-1}}
        },
        {
            // ##
            //  ##
            {{-1,0},{0,0},{0,1},{1,1}},
            {{1,-1},{1,0},{0,0},{0,1}},
            {{-1,0},{0,0},{0,1},{1,1}},
            {{1,-1},{1,0},{0,0},{0,1}}
        },
        {
            //  ##
            // ##
            {{-1,1},{0,1},{0,0},{1,0}},
            {{0,-1},{0,0},{1,0},{1,1}},
            {{-1,1},{0,1},{0,0},{1,0}},
            {{0,-1},{0,0},{1,0},{1,1}}
        },
        {
            // #
            // ###
            {{-1,-1},{-1,0},{0,0},{1,0}},
            {{-1,1},{0,1},{0,0},{0,-1}},
            {{1,1},{1,0},{0,0},{-1,0}},
            {{1,-1},{0,-1},{0,0},{0,1}}
        },
        {
            //   #
            // ###
            {{-1,1},{-1,0},{0,0},{1,0}},
            {{-1,-1},{0,-1},{0,0},{0,1}},
            {{1,-1},{1,0},{0,0},{-1,0}},
            {{1,1},{0,1},{0,0},{0,-1}}
        }
    };

    private static final Color[] colors = {
        new Color(0xFF00FF),
        new Color(0x00CED1),
        new Color(0x32CD32),
        new Color(0xDC143C),
        new Color(0xFFA500),
        new Color(0x008080),
        new Color(0xFFD700)
    };

    private static final int EMPTY = pieces.length + 1;
    private int currentPiece;
    private int currentPos;
    private int currentX;
    private int currentY;
    private boolean gameOver;

    /**
     * Constructor
     */
    public Tetris(){
        thread = null;
        screen = new int[20][10];
    }

    /**
     * Starts the game and prevents weird flickering
     */
    public void init(){
        startGame();
        image = createImage(size().width,size().height);
        graphics = image.getGraphics();
    }

    /**
     * Randomizes next piece
     */
    public void pickPiece(){
        currentPiece = (int)(Math.random()*pieces.length);
        currentPos = (int)(Math.random()*pieces[currentPiece].length);
        currentX = 5;
        currentY = 2;
    }

    /**
     * Repaints the screen
     */
    public void paint(Graphics g){

        int[] xs = new int[3];
        int[] ys = new int[3];

        int w = size().width,h = size().height;
        int s = h / screen.length;
        graphics.setColor(Color.black);
        graphics.fillRect(0,0,w,h);

        String score = "["+(1000-speed)+"]";

        graphics.setColor(Color.gray);
        graphics.setFont(new Font("Monospaced",Font.PLAIN,10));
        graphics.drawString(score,10,10);

        if(gameOver){
            graphics.setColor(Color.red);

            score = "Score: "+score;
            Font f = new Font("Impact",Font.BOLD,20);
            FontMetrics fm = getFontMetrics(f);
            int x = w/2 - fm.stringWidth(score) / 2;
            graphics.setFont(f);
            graphics.drawString(score,x,h/2-70);

            String gameOver = "GAME OVER";
            f = new Font("Impact",Font.BOLD,35);
            fm = getFontMetrics(f);
            x = w/2 - fm.stringWidth(gameOver) / 2;
            graphics.setFont(f);
            graphics.drawString(gameOver,x,h/2);

            String clickToPlayAgain = "(Click To Play Again)";
            f = new Font("Monospaced",Font.BOLD,10);
            fm = getFontMetrics(f);
            x = w/2 - fm.stringWidth(clickToPlayAgain) / 2;
            graphics.setFont(f);
            graphics.drawString(clickToPlayAgain,x,h/2+15);
        }
        else
        {
            graphics.setColor(Color.gray);
            graphics.drawLine(0,4*s,w,4*s); 

            for (int i=0;i<screen.length;i++)
                for (int j=0;j<screen[i].length;j++) {
                    if (screen[i][j] != EMPTY) {
                        xs[0] = j*s;    ys[0] = i*s + s;
                        xs[1] = j*s+s;  ys[1] = i*s;
                        xs[2] = j*s+s;  ys[2] = i*s + s;

                        graphics.setColor(colors[screen[i][j]]);
                        graphics.fillRect(j*s+3,i*s+3,s-6,s-6);

                        graphics.setColor(Color.black);
                        graphics.drawRect(j*s,i*s,s,s);
                    }
                }
        }
        g.drawImage(image,0,0,null);
    }

    /**
     * Starts a thread
     */
    public void start(){
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    /**
     * Stops thread
     */
    public void stop(){
        thread = null;
    }

    /**
     * does a piece move; given a vector where to move it
     */
    private synchronized boolean doPieceMove(int nextOr,int x,int y){
        boolean retValue = true;
        int oldOrient = currentPos,oldX = currentX,oldY = currentY;
        setPiece(EMPTY);
        if(nextOr > 0){
            currentPos = (currentPos + 1) % pieces[currentPiece].length;
        }else if(nextOr < 0){
            currentPos = (currentPos - 1 + pieces[currentPiece].length) % pieces[currentPiece].length;
        }
        currentX += x;
        currentY += y;
        if(collisions()){
            currentPos = oldOrient;
            currentX = oldX;
            currentY = oldY;
            retValue = false;
        }
        setPiece(currentPiece);
        return retValue;
    }


    /**
     * Ends the game
     */
    private void endGame(){
        gameOver = true;
        for (int i=0;i<screen.length;i++)
            for (int j=0;j<screen[i].length;j++)
                screen[i][j] = EMPTY;
    }

    /**
     * Restarts the game
     */
    private void startGame(){
        speed = 1000;
        for (int i=0;i<screen.length;i++)
            for (int j=0;j<screen[i].length;j++)
                screen[i][j] = EMPTY;
        pickPiece();
        gameOver = false;
    }

    /**
     * clear pieces that fill a row
     */
    private void clearPieces(){
        int i,j,k;

        // Makes the game harder by dropping speed by 5 milliseconds 
        speed -= 5;
        if(speed < 10)
            speed = 10;

        for (i=0;i<screen.length;i++){
            for (j=0;j<screen[i].length;j++){
                if(screen[i][j] == EMPTY)
                    break;
            }
            if(j == screen[i].length){
                for(k=i;k>0;k--)
                    for(j=0;j<screen[k].length;j++){
                        screen[k][j] = screen[k-1][j];
                        screen[k-1][j] = EMPTY;
                    }
                i--;
            }
        }
    }

    /**
     * Run method for thread
     */
    public void run(){
        for (;;) {
            if(!gameOver){
                if(!doPieceMove(0,0,1)){
                    clearPieces();
                    pickPiece();
                    if(collisions())
                        endGame();
                    else
                        setPiece(currentPiece);
                }
                repaint();
            }
            try {
                Thread.sleep(speed);
            } catch (InterruptedException e) {

            }
        }
    }

    private boolean collisions(){
        int[][] points = pieces[currentPiece][currentPos];
        for(int i = 0; i < points.length; i++){
            //Model points
            int mx = points[i][0] + currentX;
            int my = points[i][1] + currentY;

            if(mx < 0 || mx >= 10)
                return true;
            if(my < 0 || my >= 20)
                return true;
            if(screen[my][mx] != EMPTY)
                return true;
        }
        return false;
    }
    
    private void setPiece(int value){
        int[][] points = pieces[currentPiece][currentPos];
        for(int k = 0; k < points.length; k++)
            screen[points[k][1] + currentY][points[k][0] + currentX] = value;
    }

    /**
     * Lets users use arrow keys to play
     */
    public boolean keyDown(Event evt,int key){
        if(gameOver)
            return false;
        
        //arrow Up = changes the piece's orientation
        if (key == Event.UP) 
            doPieceMove(1,0,0);
        else if (key == Event.DOWN) 
            doPieceMove(0,0,1);
        else if (key == Event.LEFT) 
            doPieceMove(0,-1,0);
        else if (key == Event.RIGHT) 
            doPieceMove(0,1,0);
        repaint();
        
        return true;
    }

    /**
     * Restarts game when game is over and the mouse is clicked 
     */
    public boolean mouseUp(Event evt, int x, int y){
        if(gameOver){
            startGame();
            return true;
        }
        return false;
    }

}


