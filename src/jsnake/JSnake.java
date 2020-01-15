/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsnake;

import java.awt.BasicStroke;
import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.JOIN_ROUND;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Christopher
 */
public class JSnake extends JFrame implements KeyListener, ActionListener, MouseMotionListener, MouseListener
  {

    private final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension offDimension;
    Image offImage;
    Graphics2D offGraphics;
    int width;
    int height;
    int borderSize;
    int warning;
    boolean dead = false;
    boolean red = true;

    int score = 0;

    int blockSize = 10;

    int[] dir = new int[]
      {
        0, 0
      };
    long moveSpeed;
    long foodTime;
    long moveTime;
    long foodSpeed;

    double percent = 0.0;
    boolean click = false;
    ArrayList<Polygon> snake = new ArrayList<>(0);
    ArrayList<Polygon> food = new ArrayList<>(0);

    public static void main(String... args)
      {
        int choice = JOptionPane.showConfirmDialog(null, "Are you sensitive to flashing lights?", "Epilepsy Warning", JOptionPane.YES_NO_OPTION);
        JSnake game = new JSnake(Toolkit.getDefaultToolkit().getScreenSize(), 10, choice);
        game.start();
      }

    public JSnake(int width, int height, int borderSize, int warning)
      {
        this.width = width;
        this.height = height;
        this.borderSize = borderSize;
        this.warning = warning;
      }

    public JSnake(Dimension d, int borderSize, int warning)
      {
        this.width = d.width;
        this.height = d.height;
        this.borderSize = borderSize;
        this.warning = warning;
      }

    public void start()
      {
        addKeyListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
        init();
      }

    public void init()
      {
        setBounds(screenSize.width / 2 - width, screenSize.height / 2 - height, screenSize.width, screenSize.height);
        setSize(1920, 1080);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setVisible(true);
        new javax.swing.Timer(1, this).start();
        snake.add(genStart());
        foodTime = 0;
        moveTime = System.currentTimeMillis();
        foodSpeed = 10000;
        moveSpeed = 100;
      }

    @Override
    public void paint(Graphics g)
      {

        paintComponents(g);

      }

    @Override
    public void paintComponents(Graphics g)
      {
        //super.paintComponents(g);

        //Graphics2D offGraphics2D = (Graphics2D) offGraphics;
        if ((offGraphics == null)
                || (screenSize.width != offDimension.width)
                || (screenSize.height != offDimension.height))
          {
            offDimension = screenSize;
            offImage = createImage(screenSize.width, screenSize.height);
            offGraphics = (Graphics2D) offImage.getGraphics();
            offGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
          }

        offGraphics.setColor(getBackground());
        offGraphics.fillRect(0, 0, screenSize.width, screenSize.height);
        offGraphics.setColor(Color.black);

        addFood();
        draw(offGraphics);

        g.drawImage(offImage, 0, 0, this);
      }

    public void draw(Graphics g)
      {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.WHITE);
        g.fillRect(borderSize, borderSize, width - 2 * borderSize, height - 2 * borderSize);
        g.setFont(new Font("Score", 0, 10));
        g.drawString("Score: " + score, borderSize, borderSize);
        g.setColor(Color.BLACK);
        if (!dead)
          {
            if (System.currentTimeMillis() - moveTime >= moveSpeed)
              {
                updateSnake();
              }

            for (Polygon p : snake)
              {
                g.setColor(Color.GREEN);
                g.fillPolygon(p);
                g.setColor(Color.BLACK);
                g.drawPolygon(p);
              }

            for (Polygon p : food)
              {
                g.fillPolygon(p);
              }
          } else
          {
            kill(g);
          }

      }

    public Polygon makeRect(int x, int y, int width, int height)
      {
        Polygon rect = new Polygon();

        rect.addPoint(x, y);
        rect.addPoint(x, y + height);
        rect.addPoint(x + width, y + height);
        rect.addPoint(x + width, y);

        return rect;
      }

    public void updateSnake()
      {
        Polygon tail = snake.get(snake.size() - 1);
        for (int loop = snake.size() - 1; loop > 0; loop--)
          {
            snake.set(loop, snake.get(loop - 1));

            Polygon[] foodCopy = food.toArray(new Polygon[0]);

            for (Polygon f : foodCopy)
              {
                if (snake.get(loop).intersects(f.getBounds2D()))
                  {
                    food.remove(f);
                    moveSpeed -= 2;
                    score += 10;
                    snake.add(tail);
                  }
              }
          }

        Polygon p = snake.get(0);

        int[] newX = new int[4];
        int[] newY = new int[4];

        for (int loop = 0; loop < 4; loop++)
          {
            newX[loop] = p.xpoints[loop] + blockSize * dir[0];
            newY[loop] = p.ypoints[loop] + blockSize * dir[1];
          }
        Polygon newHead = new Polygon(newX, newY, p.npoints);

        Polygon[] foodCopy = food.toArray(new Polygon[0]);

        for (Polygon f : foodCopy)
          {
            if (newHead.intersects(f.getBounds2D()))
              {
                food.remove(f);
                moveSpeed -= 2;
                score += 10;
                snake.add(tail);
              }
          }

        snake.set(0, newHead);

        for (int loop = 0; loop < 4; loop++)
          {
            if (snake.get(0).xpoints[loop] <= borderSize
                    || snake.get(0).xpoints[loop] >= width - 2 * borderSize
                    || snake.get(0).ypoints[loop] <= borderSize
                    || snake.get(0).ypoints[loop] >= height - 2 * borderSize)
              {
                dead = true;
              }
          }
        Rectangle2D head = snake.get(0).getBounds2D();
        for (int loop = 1; loop < snake.size(); loop++)
          {
            if (snake.get(loop).intersects(head))
              {
                dead = true;
              }
          }

        moveTime = System.currentTimeMillis();
      }

    public void addFood()
      {
        if (System.currentTimeMillis() - foodTime >= foodSpeed)
          {
            int xPos = (int) (Math.random() * (width - 2 * blockSize)) + blockSize;
            int yPos = (int) (Math.random() * (height - 2 * blockSize)) + blockSize;

            food.add(makeRect(xPos - blockSize / 2, yPos - blockSize / 2, blockSize, blockSize));
            foodTime = System.currentTimeMillis();
            foodSpeed -= foodSpeed / 20;
          }
      }

    public Polygon genStart()
      {
        int startX = width / 2;
        int startY = height / 2;

        return makeRect(startX - blockSize / 2, startY - blockSize / 2, blockSize, blockSize);

      }

    public void kill(Graphics g)
      {
        if (warning == JOptionPane.NO_OPTION)
          {
            red = !red;
            if (red)
              {
                g.setColor(Color.RED);
                g.fillRect(borderSize, borderSize, width - 2 * borderSize, height - 2 * borderSize);
                g.setColor(Color.BLACK);
                g.setFont(new Font("Dead Font", 0, 150));
                int startX = g.getFontMetrics().stringWidth("YOU DIED");
                int strHeight = g.getFontMetrics().getHeight();
                g.drawString("YOU DIED", (width - startX) / 2, height / 2);

                g.setFont(new Font("Score Dead Font", 0, 30));
                startX = g.getFontMetrics().stringWidth("Score: " + score);
                g.drawString("Score: " + score, (width - startX) / 2, height / 2 + strHeight);
              } else
              {
                g.setColor(Color.BLACK);
                g.fillRect(borderSize, borderSize, width - 2 * borderSize, height - 2 * borderSize);
                g.setColor(Color.RED);
                g.setFont(new Font("Dead Font", 0, 150));
                int startX = g.getFontMetrics().stringWidth("YOU DIED");
                int strHeight = g.getFontMetrics().getHeight();
                g.drawString("YOU DIED", (width - startX) / 2, height / 2);

                g.setFont(new Font("Score Dead Font", 0, 30));
                startX = g.getFontMetrics().stringWidth("Score: " + score);
                g.drawString("Score: " + score, (width - startX) / 2, height / 2 + strHeight);
              }
          } else
          {
            g.setColor(Color.BLACK);
            g.fillRect(borderSize, borderSize, width - 2 * borderSize, height - 2 * borderSize);
            g.setColor(Color.RED);
            g.setFont(new Font("Dead Font", 0, 150));
            int startX = g.getFontMetrics().stringWidth("YOU DIED");
            int strHeight = g.getFontMetrics().getHeight();
            g.drawString("YOU DIED", (width - startX) / 2, height / 2);

            g.setFont(new Font("Score Dead Font", 0, 30));
            startX = g.getFontMetrics().stringWidth("Score: " + score);
            g.drawString("Score: " + score, (width - startX) / 2, height / 2 + strHeight);
          }

      }

    public boolean checkPoint(int x, int y)
      {
        if (snake.size() == 1)
          {
            return true;
          }
        Point p = new Point((int) Math.round(snake.get(0).getBounds2D().getCenterX()) + blockSize * x, (int) Math.round(snake.get(0).getBounds2D().getCenterY()) + blockSize * y);
        return !(snake.get(1).contains(p));
      }

    @Override
    public void keyTyped(KeyEvent e)
      {
      }

    @Override
    public void keyPressed(KeyEvent e)
      {
        int keyCode = e.getKeyCode();
        switch (keyCode)
          {
            case VK_Q:
                this.dispose();
                System.exit(0);
                break;
            case VK_LEFT:
                if (checkPoint(-1, 0))
                  {
                    dir[0] = -1;
                    dir[1] = 0;
                  }
                break;
            case VK_RIGHT:
                if (checkPoint(1, 0))
                  {
                    dir[0] = 1;
                    dir[1] = 0;
                  }
                break;
            case VK_UP:
                if (checkPoint(0, -1))
                  {
                    dir[0] = 0;
                    dir[1] = -1;
                  }
                break;
            case VK_DOWN:
                if (checkPoint(0, 1))
                  {
                    dir[0] = 0;
                    dir[1] = 1;
                  }
                break;

          }
      }

    @Override
    public void keyReleased(KeyEvent e)
      {
      }

    @Override
    public void actionPerformed(ActionEvent e)
      {
        repaint();
      }

    @Override
    public void mouseClicked(MouseEvent e)
      {
      }

    @Override
    public void mousePressed(MouseEvent e)
      {

      }

    @Override
    public void mouseReleased(MouseEvent e)
      {

      }

    @Override
    public void mouseEntered(MouseEvent e)
      {
      }

    @Override
    public void mouseExited(MouseEvent e)
      {
      }

    @Override
    public void mouseDragged(MouseEvent e)
      {

      }

    @Override
    public void mouseMoved(MouseEvent e)
      {

      }

  }

class ColorPoint
  {

    int x;
    int y;
    Point p;
    Color col;
    int strokeSize;

    public ColorPoint(Point p, Color col, int StrokeSize)
      {
        this.p = p;
        this.x = p.x;
        this.y = p.y;
        this.col = col;
        this.strokeSize = StrokeSize;
      }

  }
