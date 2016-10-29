package com.example.zach.spaceinvader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Zach on 10/25/2016.
 */
public class MyView extends SurfaceView implements SurfaceHolder.Callback {

    protected Context context;
    int speed;
    int width,height;
    DrawingThread thread;
    Paint text;
    Ship player;
    ArrayList<Alien> aliens;
    ArrayList<Explosion> booms;
    Bullet bul;
    int x,y;
    boolean direction;
    boolean addBullet;
    boolean addAlien;
    Random ran;
    int score;

    public MyView(Context ctx, AttributeSet attrs) {
        super(ctx,attrs);
        context = ctx;
        direction = false;
        addBullet = false;
        addAlien = false;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        ran = new Random();

        aliens = new ArrayList<Alien>();
        booms = new ArrayList<Explosion>();
        bul = new Bullet(-50,-50);

        text=new Paint();
        text.setTextAlign(Paint.Align.LEFT);
        text.setColor(Color.WHITE);
        text.setTextSize(24);
        x = 0;
        y = 0;
        score = 0;
        player = new Ship(x,y);
        speed = 12;

    }


    public Bitmap resizeBitmap(Bitmap b, int newWidth, int newHeight) {
        int w = b.getWidth();
        int h = b.getHeight();
        float scaleWidth = ((float) newWidth) / w;
        float scaleHeight = ((float) newHeight) / h;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                b, 0, 0, w, h, matrix, false);
        b.recycle();
        return resizedBitmap;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread.setRunning(false);
        boolean waitingForDeath = true;
        while(waitingForDeath) {
            try {
                thread.join();
                waitingForDeath = false;
            }
            catch (Exception e) {
                Log.v("Thread Exception", "Waiting on drawing thread to die: " + e.getMessage());
            }
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread= new DrawingThread(holder, context, this);
        thread.setRunning(true);
        thread.start();
    }


    public void customDraw(Canvas canvas) {

        Random r2 = new Random();
        if(r2.nextInt(500) <= 10){
            aliens.add(new Alien(ran.nextInt(width - 100),0));
        }
        if(player.getY() == 0){
            width = canvas.getWidth();
            height = canvas.getHeight();

            player.setX(width/2);
            player.setY((height /2) + (height/3));
        }

        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(player.getImage(),player.getX(),player.getY(),null);

        if(player.getX() <= 0){
            direction = true;
        }

        if(player.getX() >= width - 100){
            direction = false;
        }

        if(direction == true){
            player.setX(player.getX() + speed);
        }else{
            player.setX(player.getX() - speed);
        }
        for(int i = 0; i<aliens.size(); i++){
            aliens.get(i).update(canvas);
            if(bul.getX() +10  >= aliens.get(i).getX() && bul.getX()  <= aliens.get(i).getX() + aliens.get(i).getW()
                    && bul.getY() <= aliens.get(i).getY() + aliens.get(i).getH() + 10){
                booms.add(new Explosion());
                booms.get(booms.size()-1).setX((int)bul.getX()-50);
                booms.get(booms.size()-1).setY((int)bul.getY()-50);

                aliens.get(i).setDead(true);
                aliens.get(i).setY(-700);
                aliens.get(i).setX(500);
                bul.setDead(true);
                bul.setX(-500);
                bul.setY(-500);
                score++;

            }
            if(aliens.get(i).getDead() == true){

                aliens.remove(i);

            }
        }
        bul.update(canvas, aliens);
        if(bul.getDead() == true){
            addBullet = false;
            bul.setDead(false);
        }

        if(booms.size() > 0){
            for(int i = 0; i<booms.size(); i++){
                booms.get(i).display(canvas);
                if(booms.get(i).getGo() == false){
                    booms.remove(i);
                }
            }
        }

        text.setColor(Color.WHITE);
        text.setTextSize(40);
        canvas.drawText("SCORE: ", 50,100,text );
        canvas.drawText(score + "",200,100,text);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(addBullet == false) {
            bul.setX((int) player.getX());
            bul.setY((int) player.getY());
            addBullet = true;
        }
        return true;
    }


    class DrawingThread extends Thread {
        private boolean running;
        private Canvas canvas;
        private SurfaceHolder holder;
        private Context context;
        private MyView view;

        private int FRAME_RATE = 30;
        private double delay = 1.0 / FRAME_RATE * 1000;
        private long time;

        public DrawingThread(SurfaceHolder holder, Context c, MyView v) {
            this.holder=holder;
            context = c;
            view = v;
            time = System.currentTimeMillis();
        }

        void setRunning(boolean r) {
            running = r;
        }

        @Override
        public void run() {
            super.run();
            while(running){
                if(System.currentTimeMillis() - time > delay) {
                    time = System.currentTimeMillis();
                    canvas = holder.lockCanvas();
                    if(canvas!=null){
                        view.customDraw(canvas);
                        holder.unlockCanvasAndPost(canvas);
                    }

                }
            }
        }



    }
    class Ship{
        private long x,y;
        private Bitmap Oship,ship;

        Ship(int nx, int ny){
            Oship = BitmapFactory.decodeResource(context.getResources(),R.drawable.ship);
            ship=Oship.copy(Bitmap.Config.ARGB_8888, true);
            ship = resizeBitmap(ship,100,100);

            x = nx;
            y = ny;
        }

        public long getX(){return x;}
        public long getY(){return y;}
        public void setX(long nx){x = nx;}
        public void setY(long ny){y = ny;}
        public Bitmap getImage(){return ship;}
    }

    class Bullet{
        private long x,y,w,h;
        private Bitmap oBullet, bullet;
        private double bSpeed,bAccel;
        boolean dead;

        Bullet(int nx, int ny){
            x = nx;
            y = ny;
            w = 50;
            h = 100;
            oBullet = BitmapFactory.decodeResource(context.getResources(),R.drawable.missile);
            bullet=oBullet.copy(Bitmap.Config.ARGB_8888, true);
            bullet = resizeBitmap(bullet,(int)w,(int)h);
            bSpeed = 10;
            bAccel = .02;
            dead = false;
        }

        public void update(Canvas c, ArrayList<Alien> a){
                c.drawBitmap(bullet,x,y,null);
                y-= bSpeed;
                bSpeed+=bAccel;
                bAccel+=.05;
            if(y<= 0){
                dead = true;
                y = -500;
                bSpeed = 10;
                bAccel = .02;
            }

        }

        public boolean checkCollision(ArrayList<Alien> a){
            boolean destroyed = false;
            if(a.size() != 0) {
                for (Alien e : a) {
                    if(x >= e.getX() - 10 && x + w <= e.getX() + e.getW()+10 && y <= e.getY()+e.getH()){
                        destroyed = true;
                    }
                }
            }
            return destroyed;
        }

        public long getX(){return x;}
        public long getY(){return y;}
        public long getH(){return h;}
        public long getW(){return w;}
        public void setX(int nx){x = nx;}
        public void setY(int ny){y = ny;}
        public boolean getDead(){return dead;}
        public void setDead(boolean b){dead = b;}
        public Bitmap getBitmap(){return bullet;}
    }

    class Alien{
        long x,y,w,h;
        private Bitmap oAlien, alien;
        private int aSpeed;
        boolean d;

        public Alien(int nx, int ny){
            x = nx;
            y = ny;
            w = 100;
            h = 100;
            d = false;
            oAlien = BitmapFactory.decodeResource(context.getResources(),R.drawable.alien);
            alien=oAlien.copy(Bitmap.Config.ARGB_8888, true);
            alien = resizeBitmap(alien,(int)w,(int)h);
            aSpeed = 10;
        }

        public void update(Canvas c){
            if(y <= height ) {
                c.drawBitmap(alien, x, y, null);
            }else{
                booms.add(new Explosion());
                booms.get(booms.size()-1).setX((int)x - 50);
                booms.get(booms.size()-1).setY(height - 100);
                y = -500;
                x = 500;
                d = true;
            }

            y+=aSpeed;


        }

        public long getX(){return x;}
        public long getY(){return y;}
        public long getW(){return w;}
        public long getH(){return h;}
        public void setX(int nx){x = nx;}
        public void setY(int ny){y = ny;}
        public boolean getDead(){return d;}
        public void setDead(boolean b){d = b;}

    }

    class Explosion{

        int x, y;
        int timer;
        ArrayList<Bitmap> imgs;
        boolean go;
        public Explosion(){
            x = 900;
            y = -900;
            imgs = new ArrayList<Bitmap>();
            Bitmap a = BitmapFactory.decodeResource(context.getResources(),R.drawable.a);
            Bitmap b = BitmapFactory.decodeResource(context.getResources(),R.drawable.b);
            Bitmap c = BitmapFactory.decodeResource(context.getResources(),R.drawable.c);
            Bitmap d = BitmapFactory.decodeResource(context.getResources(),R.drawable.d);
            Bitmap e = BitmapFactory.decodeResource(context.getResources(),R.drawable.e);
            Bitmap f = BitmapFactory.decodeResource(context.getResources(),R.drawable.f);
            Bitmap g = BitmapFactory.decodeResource(context.getResources(),R.drawable.g);
            Bitmap h = BitmapFactory.decodeResource(context.getResources(),R.drawable.h);
            Bitmap i = BitmapFactory.decodeResource(context.getResources(),R.drawable.i);
            Bitmap j = BitmapFactory.decodeResource(context.getResources(),R.drawable.j);
            Bitmap k = BitmapFactory.decodeResource(context.getResources(),R.drawable.k);
            Bitmap l = BitmapFactory.decodeResource(context.getResources(),R.drawable.l);
            Bitmap m = BitmapFactory.decodeResource(context.getResources(),R.drawable.m);
            Bitmap n = BitmapFactory.decodeResource(context.getResources(),R.drawable.n);
            Bitmap o = BitmapFactory.decodeResource(context.getResources(),R.drawable.o);

            imgs.add(a);
            imgs.add(b);
            imgs.add(c);
            imgs.add(d);
            imgs.add(e);
            imgs.add(f);
            imgs.add(g);
            imgs.add(h);
            imgs.add(i);
            imgs.add(j);
            imgs.add(k);
            imgs.add(l);
            imgs.add(m);
            imgs.add(n);
            imgs.add(o);


            timer = 0;
            go = true;

        }

        public void display(Canvas c){
            if(go == true){
                c.drawBitmap(imgs.get(timer), x,y,null);
                if(timer < imgs.size()-1){
                    timer++;
                }else{
                    timer = 0;
                    go = false;
                }
            }
        }

        public int getX(){return x;}
        public int getY(){return y;}
        public void setY(int ny){y = ny;}
        public void setX(int nx){x = nx;}
        public boolean getGo(){return go;}
        public void setGo(boolean b){go = b;}
    }
}

