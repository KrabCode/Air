import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;

public class MainApp extends PApplet{

    public static void main(String[] args) {
        PApplet.main("MainApp");
    }

    public void settings() {
        size(1600,800,P2D);
        noSmooth();
//        fullScreen(P2D);
    }

    float dockingDistance;
    enum PlaneType{THIN,BIG,HELI}

    private ArrayList<Plane> planes = new ArrayList();
    private ArrayList<Dock> docks = new ArrayList();
    private ArrayList<Explosion> explosions = new ArrayList();
    private Plane lockedPlane = null;
    private PImage imgPlaneThin;
    private PImage imgPlaneBig;
    private PImage imgHeliBody;
    private PImage imgHeliBlades;

    private PImage imgPause;
    private PImage imgPlay;
    private PImage imgFastF;

    enum GameState {MAIN_MENU, GAME_PLAY}
    boolean fastForward = false;
    float ffSpdMagMod = 2f;

    private GameState state = GameState.MAIN_MENU;
    private ArrayList<Button> mainMenu = new ArrayList();
    private ArrayList<Button> gameHUD = new ArrayList();

    private PImage imgMap;
    private PVector center;
    private float screenScaleX;
    private float screenScaleY;
    private int deadCounter = 0;
    private int dockCounter = 0;

    private float spawnFrequency;
    private float origSpawnFrequency = 300;
    private boolean mouseControlsPlanes = false;

    public void setup() {
        colorMode(RGB);
        orientation(LANDSCAPE);
        frameRate(60);
        textAlign(CENTER, TOP);
        imgMap = loadImage("map.jpg");
        imgMap.resize(height,width);
        imgPlaneThin = loadImage("planeThin_crop.png");
        imgPlaneBig = loadImage("planeBig_crop.png");
        imgHeliBody = loadImage("heliBody.png");
        imgHeliBlades = loadImage("heliBlades.png");
        imgPause = loadImage("imgPause.png");
        imgPlay = loadImage("imgPlay.png");
        imgFastF = loadImage("imgFastF.png");

        screenScaleX = width/1200f;
        screenScaleY = height/800f;

        center = new PVector(width/2, height/2);
        dockingDistance = 20*(screenScaleX+screenScaleY)*.8f;
        spawnNewPlane();
        spawnAllDocks();
        spawnButtons();
    }

    public void draw() {

        switch(state){
            case MAIN_MENU: {
                mouseControlsPlanes = false;
                collectGarbage(planes);
                trySpawnNewPlane();
                scene();
                updateExplosions();
                updatePlanes();
                updateDocks();
                noStroke();
                rectMode(CORNER);
                fill(0,200);
                rect(0,0,width,height);
                for (Button b : mainMenu) {
                    b.draw();
                }
                break;
            }
            case GAME_PLAY:
                mouseControlsPlanes = true;
                collectGarbage(planes);
                trySpawnNewPlane();
                scene();
                updateExplosions();
                updatePlanes();
                updateDocks();
                for (Button b : gameHUD) {
                    b.draw();
                }
                break;
        }

    }

    private void trySpawnNewPlane(){
        if(fastForward){
            spawnFrequency = origSpawnFrequency / ffSpdMagMod;
        }else{
            spawnFrequency = origSpawnFrequency;
        }
        if(frameCount % spawnFrequency == 0){
            spawnNewPlane();
        }
    }

    private void updatePlanes() {
        for(Plane f : planes){
            if(f != null){
                f.move();
                f.collide();
                f.draw();
            }
        }
    }

    private void updateDocks(){
        for(Dock d : docks){
            d.draw();
        }
    }


    private void scene() {
        background(100);
        pushMatrix();
        translate(width/2, height/2);
        rotate(radians(90));
        imageMode(CENTER);
        tint(220);
        image(imgMap,0,0);
        popMatrix();
        stroke(0);
        textSize(20);
        fill(0, 200);
        text("frames per second: " + round(frameRate), width/2, 20*screenScaleY);
        text("successfully landed: " + dockCounter, width/2, 50*screenScaleY);
    }



    private void spawnNewPlane(){
        float r = random(1);
        if(r<.33f){
            planes.add(new Plane(PlaneType.THIN));
        }else if(r<.66f){
            planes.add(new Plane(PlaneType.BIG));
        }else{
            planes.add(new Plane(PlaneType.HELI));
        }
    }

    private void spawnAllDocks() {
        docks.add(new Dock(PlaneType.THIN,new PVector(center.x-width/5.5f, center.y+height/12)));
        docks.add(new Dock(PlaneType.HELI, new PVector(center.x+width/5.8f, center.y+height/50)));
        docks.add(new Dock(PlaneType.BIG, new PVector(center.x+width/10f, center.y+height/6)));
    }

    private int getColorByType(PlaneType type){
        if(type==PlaneType.BIG){
            return color(246,83,51);
        }else if(type == PlaneType.THIN){
            return color(37,138,188);
        }else if(type == PlaneType.HELI){
            return color(80,45,194);
        }
        return 0;
    }

    /*
    * DOCK
    * */

    class Dock{
        PVector pos ;
        float size ;
        PlaneType type;
        boolean lit = false;
        int colour;
        Dock(PlaneType dockType, PVector pos){
            this.pos = pos;
            this.type = dockType;
            this.size = 70*(screenScaleX+screenScaleY)/2;
            colour = getColorByType(type);
        }

        void draw(){
            if(lit){
                pushMatrix();
                fill(colour);

                if(type == PlaneType.THIN){
                    strokeCap(PROJECT);
                    strokeWeight(10);
                    translate(pos.x,pos.y);
                    rotate(HALF_PI);
                    for(int i = 0; i < 3; i++){
                        float flicker = 50*sin((frameCount-i*5)/6);
                        stroke(colour,255-flicker);
                        line(0-size/6, (-i*size/2), 0, (-i*size/2)-size/6);
                        line(0, (-i*size/2)-size/6, +size/6, -i*size/2);
                    }
                } else if(type == PlaneType.BIG){
                    strokeCap(PROJECT);
                    strokeWeight(10);
                    translate(pos.x,pos.y);
                    rotate(.1f);
                    for(int i = 0; i < 3; i++){
                    float flicker = 50*sin((frameCount-i*5)/6);
                        stroke(colour, 255-flicker);
                        line(0-size/6, (-i*size/2), 0, (-i*size/2)-size/6);
                        line(0, (-i*size/2)-size/6, +size/6, -i*size/2);
                    }
                }else if(type == PlaneType.HELI){
                    /*
                    noFill();
                    strokeWeight(8);
                    ellipseMode(CENTER);
                    float flickerA = 20*sin((frameCount)/8);
                    stroke(colour, 255-flickerA);
                    ellipse(pos.x,pos.y, size*1.2f,size*1.2f);
                    float flickerB = 20*sin((frameCount-5)/8);
                    stroke(colour, 220-flickerB);
                    ellipse(pos.x,pos.y, size*.8f,size*.8f);
                    float flickerC = 20*sin((frameCount-10)/8);
                    stroke(colour, 200-flickerC);
                    ellipse(pos.x,pos.y, size*.3f,size*.3f);*/
                }
                popMatrix();
            }
            lit = false;
        }


        void lightUp() {
            lit = true;
        }
    }


    class Vertex{
        PVector pos;
        boolean visible;
        Vertex(PVector pos){
            this.pos = pos;
            visible = true;
        }

        Vertex(PVector pos, boolean visible){
            this.pos = pos;
            this.visible = visible;
        }
    }


    /*
     * MAIN GAME OBJECT
     * */
    class Plane {
        boolean isLockTarget = false;
        boolean isDying = false;
        boolean hasDied = false;
        boolean isDocking = false;
        boolean hasDocked = false;
        boolean isWarning = false;
        boolean canCollide = true;

        PlaneType planeType;
        ArrayList<Vertex> route = new ArrayList();
        PVector pos;
        float size;
        Dock myDock;
        PVector spd;
        float angle;
        float spdMag;
        float origSpdMag;

        int colour;
        boolean willDock = false;

        Plane(PlaneType planeType){
            this.planeType = planeType;
            colour = getColorByType(planeType);

            size = 80*(screenScaleX+screenScaleY)/2;
            if(planeType == PlaneType.THIN){
                origSpdMag = 1.3f;
            }else if(planeType == PlaneType.BIG){
                origSpdMag = 1f;
            }else if(planeType == PlaneType.HELI){
                origSpdMag = .7f;
            }
            pos = getRandomBorderPos();
            route.add(new Vertex(pos));
            PVector mockTarget = getRandomPointRoughlyInCenter();
            float routeAngle = getAngle(pos.x, pos.y, mockTarget.x, mockTarget.y);
            route.add(new Vertex(getPointAtAngle(pos, size/2,degrees(routeAngle)), false));
            angle = getAngle(route.get(0).pos.x,route.get(0).pos.y, route.get(1).pos.x,route.get(1).pos.y)+HALF_PI;
            myDock = tryFindNearestDock(docks, pos.x, pos.y, planeType);
        }


        void move(){
            if(!fastForward){
                spdMag = origSpdMag;
            }else{
                spdMag = origSpdMag*ffSpdMagMod;
            }
            if(hasDied){
                return;
            }
            if(route.size()>0){
                float dist = PVector.dist(route.get(0).pos, pos);
                spd = PVector.sub(route.get(0).pos, pos).normalize().mult(spdMag);
                if(dist < spdMag){
                    route.remove(0);
                }
            }else{
                spd = spd.normalize().mult(spdMag);
            }
            Dock d = tryFindNearestDock(docks, pos.x, pos.y, planeType);
            if (d==null){
                pos.add(spd);
            }else{
                if(isDocking && !hasDocked){
                    canCollide = false;
                    size-=.5f;
                    if(planeType == PlaneType.THIN){
                        spd.mult(0);
                        spd.add(origSpdMag,0);
                        pos.add(spd);
                    }else if(planeType == PlaneType.BIG){
                        spd.mult(0);
                        spd.add(0,-origSpdMag);
                        pos.add(spd);
                    }
                    if(size<20){
                        hasDocked = true;
                    }
                }else if(isDying && !hasDied){
                    pos.add(spd);
                    size-=.5f;
                    if(size<30){
                        hasDied = true;
                        canCollide = false;
                        newExplosion(pos.x,pos.y);
                    }
                }
                else if(dist(pos.x,pos.y,d.pos.x, d.pos.y) < dockingDistance){
                    isDocking = true;
                    canCollide = false;
                    route = new ArrayList();
                }else{
                    pos.add(spd);
                }
            }
        }

        void draw(){
            if(isDying){
                tint(255,0,0);
            }else if(!willDock){
                tint(colour);
            }else{
                noTint();
            }
            if(hasDied){
                tint(100);
                drawPlane();
                return;
            }
            if(isLockTarget){
//                strokeWeight(6);
                myDock.lightUp();
//                noFill();
//                stroke(colour);
//                ellipseMode(CENTER);
//                ellipse(pos.x,pos.y,100,100);
            }else{
                strokeWeight(3);
            }


            for(int i = route.size()-1; i > 1; i--){
                if(i%2==0 || !isLockTarget){
                    if(route.get(i-1).visible && route.get(i).visible){
                            PVector from = route.get(i-1).pos;
                            PVector to = route.get(i).pos;
                            stroke(0);
                            line(to.x,to.y,from.x,from.y );
                    }
                }
            }
            fill(0,0,255);

            if(willDock && route.size()>0){
                stroke(colour);
                strokeWeight(5);
                noFill();
            }
            drawPlane();
        }

        private void drawPlane(){
            pushMatrix();
            //rotate
            translate(pos.x, pos.y);
            float angleTarget = (spd.heading() + HALF_PI);
            if ( angle < angleTarget - PI ) angle = angle + TWO_PI;
            if ( angle > angleTarget + PI ) angle = angle - TWO_PI;
            float amt = .05f;
            angle = lerp(angle, angleTarget, amt );
            rotate(angle);
            //background warning
            if(isWarning && !hasDied && !isDocking){
                noStroke();
                fill(255,0,0,150);
                ellipseMode(CENTER);
                ellipse(0,0,size,size);
            }
            //image
            imageMode(CENTER);
            if(planeType==PlaneType.BIG){
                image(imgPlaneBig, 0,0,size,size);
            }else if(planeType==PlaneType.THIN){
                image(imgPlaneThin, 0,0,size,size);
            }else if(planeType==PlaneType.HELI){
                image(imgHeliBody, 0,0, size/2,size/2+size/2);
                float bladeAngle = frameCount;
                if(hasDied){
                    bladeAngle = 0;
                }
                translate(0,-size/6.5f);
                if(fastForward){
                    rotate(-radians(bladeAngle*20));
                }else{
                    rotate(-radians(bladeAngle*10));
                }
                image(imgHeliBlades,0,0, 120*(size/2/50), 12*(size/2/50));
                rotate(HALF_PI);
                image(imgHeliBlades,0,0, 120*(size/2/50), 12*(size/2/50));
            }
            popMatrix();
        }

        void tryAddRoutePoint(int x, int y) {
            float started = millis();
            PVector lastVertex = pos;
            if(route.size()>0){
                lastVertex = route.get(route.size()-1).pos;
            }
            float newVertexDistance = 15;
            if(dist(x,y, lastVertex.x,lastVertex.y) > newVertexDistance){

                Dock d = tryFindNearestDock(docks, x, y, planeType);
                if(d == null){
                    return;
                }
                if(dist(x,y,d.pos.x, d.pos.y) < dockingDistance){
                    Vertex dockingVertex = new Vertex(new PVector(d.pos.x,d.pos.y));
                    route.add(dockingVertex);
                    println("dock added");
                    locked = false;
                    lockedPlane = null;
                    willDock = true;
                    isLockTarget = false;
                }else{
                    willDock = false;
                    route.add(new Vertex(new PVector(x,y)));
                    println("route added");
                }
                //or just coasting
            }
        }

        void collide() {
            if(canCollide){
                Plane f = tryFindNearestPlaneThatCollides(planes, pos.x, pos.y, this);
                isWarning = f != null && dist(f.pos.x, f.pos.y, pos.x, pos.y) < size * 1.2f;
                float edgeBufferZone = 200;
                if(!isDying){
                    isDying = f != null && dist(f.pos.x, f.pos.y, pos.x,pos.y) < size*.8f;
                    if(isDying){
                        route.clear();
                    }
                }else if(pos.x < -edgeBufferZone || pos.x > width+ edgeBufferZone
                        || pos.y < -edgeBufferZone || pos.y>height+ edgeBufferZone){
                    isDying = true;
                    route.clear();
                }
            }
        }


    }


    private void newExplosion(float x, float y) {
        explosions.add(new Explosion(x,y));
    }

    private void updateExplosions(){
        for(Explosion e : explosions){
            e.update();
            e.draw();
        }
    }


    class Explosion{
        PVector pos;
        float radius;
        boolean finished = false;
        int fillAlpha = 150;
        Explosion(float x, float y){
            pos = new PVector(x,y);
            radius = 0;
        }

        void update(){
            if(!finished){
                radius+=1;
                if(radius > width/16){
                    finished = true;
                }
            }
        }

        void draw(){

            if(!finished){
                strokeWeight(5);
                stroke(255,0,0);
            }else{
                if(fillAlpha>50){
                    fillAlpha--;
                    if(state==GameState.GAME_PLAY){
                        countScore();
                        state = GameState.MAIN_MENU;
                    }
                }
                noStroke();
            }
            fill(50,fillAlpha);
            ellipseMode(CENTER);
            ellipse(pos.x,pos.y, radius,radius);
        }
    }

    private void countScore() {

    }

    /*
    * MATH
    * */
    private PVector getRandomBorderPos() {
        float r = random(1);
        if(r < .25f){
            return new PVector(random(width), 0);
        }else if(r < .5f){
            return new PVector(random(width), height);
        }else if(r < .75f){
            return new PVector(0, random(height));
        }else{
            return new PVector(width, random(height));
        }
    }

    private PVector getRandomPointRoughlyInCenter(){
        return new PVector(center.x+random(-50,50), center.y+random(-50,50));
    }

    private PVector getPointAtAngle(PVector center, float radius, float angle){
        return new PVector(
                center.x + radius * cos(angle * PI / 180),
                center.y + radius * sin(angle * PI / 180)
        );
    }

    private float getAngle(float x0, float y0, float x1, float y1)
    {
        return (atan2(y1 - y0, x1 - x0));
    }

    private Plane tryFindNearestPlane(ArrayList<Plane> listToSearch, float x, float y){
        float dist = width;
        Plane candidate = null;
        for(Plane f:listToSearch){
            float temp = dist(x,y,f.pos.x, f.pos.y);
            if(temp < dist){
                dist = temp;
                candidate = f;
            }
        }
        return candidate;
    }

    private Plane tryFindNearestPlane(ArrayList<Plane> listToSearch, float x, float y, Plane except){
        float dist = width;
        Plane candidate = null;
        for(Plane f:listToSearch){
            if(f==except){
                continue;
            }
            float temp = dist(x,y,f.pos.x, f.pos.y);
            if(temp < dist){
                dist = temp;
                candidate = f;
            }
        }
        return candidate;
    }

    private Plane tryFindNearestPlaneThatCollides(ArrayList<Plane> listToSearch, float x, float y, Plane p){
        float dist = width;
        Plane candidate = null;
        for(Plane f:listToSearch){
            if(!f.canCollide || f == p){
                continue;
            }
            float temp = dist(x,y,f.pos.x, f.pos.y);
            if(temp < dist){
                dist = temp;
                candidate = f;
            }
        }
        return candidate;
    }

    private Dock tryFindNearestDock(ArrayList<Dock> listToSearch, float x, float y, PlaneType planeType) {
        float dist = width;
        Dock candidate = null;
        for(Dock d:listToSearch){
            if(d.type != planeType){
                continue;
            }
            float temp = dist(x,y,d.pos.x, d.pos.y);
            if(temp < dist){
                dist = temp;
                candidate = d;
                if(temp < dockingDistance){
                    break;
                }
            }
        }
        return candidate;
    }


    private void collectGarbage(ArrayList<Plane> flies){
        ArrayList<Plane> toRemove = new ArrayList();
        for(Plane f : flies){
            if(f.hasDocked){
                dockCounter++;
                toRemove.add(f);
                println("docked: " + toRemove.size());
            }
        }
        flies.removeAll(toRemove);
    }


    /*
     * INPUT
     * */
    private boolean locked = false;
    public void mousePressed() {
        println("MOUSE PRESSED");
        buttonInput();
        if(mouseControlsPlanes){
            Plane plane = tryFindNearestPlaneThatCollides(planes, mouseX, mouseY,null);
            if(plane ==null){
                return;
            }
            float dist = dist(mouseX, mouseY, plane.pos.x, plane.pos.y);
            float lockDistance = 50;
            if(dist < plane.size + lockDistance) {
                println("lock");
                locked = true;
                plane.isLockTarget = true;
                plane.route = new ArrayList();
                lockedPlane = plane;
            } else {
                println("unlock");
                locked = false;
                lockedPlane = null;
                plane.isLockTarget = false;
            }
            println(dist);
        }

    }

    public void mouseDragged(){
        println("MOUSE DRAGGED");
        if(locked && mouseControlsPlanes) {
            lockedPlane.tryAddRoutePoint(mouseX,mouseY);
        }
    }

    public void mouseReleased() {

        locked = false;
        println("unlock");
        locked = false;
        if(lockedPlane !=null){
            lockedPlane.isLockTarget = false;
        }
        lockedPlane = null;

        println("MOUSE RELEASED");

    }
    enum ButtonType{NEW_GAME, FAST_FORWARD}

    class Button {
        PVector pos;
        PVector size;
        ButtonType type;

        Button(ButtonType type){
            this.type = type;
        }

        void draw(){
            noStroke();
            fill(20,100);
            rectMode(CENTER);
            rect(pos.x,pos.y,size.x,size.y);
            fill(255);
            if(type == ButtonType.NEW_GAME){
                textSize(20);
                text("New game",pos.x,pos.y);
            }else if(type == ButtonType.FAST_FORWARD && fastForward){
                textSize(28);
                text(">", pos.x,pos.y);
            }else if(type == ButtonType.FAST_FORWARD ){
                textSize(28);
                text(">>", pos.x,pos.y);
            }
        }

        boolean isPointInside(float x, float y){
            return(x > pos.x-size.x/2 && x < pos.x+size.x/2 && y > pos.y-size.y/2 && y < pos.y+size.y/2);
        }

        void input() {
            if(type==ButtonType.NEW_GAME){
                resetGame();
            }
            if(type == ButtonType.FAST_FORWARD){
                if(state == GameState.GAME_PLAY && !fastForward){
                    fastForward = true;
                    println("FF set");
                }else{
                    fastForward = false;
                    println("FF unset");
                }
            }

        }
    }

    void resetGame(){
        state=GameState.GAME_PLAY;
        explosions.clear();
        docks.clear();
        planes.clear();
        spawnAllDocks();
        spawnNewPlane();
        deadCounter = 0;
        dockCounter = 0;
    }

    private void spawnButtons(){
        mainMenu.clear();
        Button startGame = new Button(ButtonType.NEW_GAME);
        startGame.pos = new PVector(width/2, height/2);
        startGame.size = new PVector(width/2,height/8);
        mainMenu.add(startGame);

        gameHUD.clear();
        Button fastForward = new Button(ButtonType.FAST_FORWARD);
        fastForward.pos = new PVector(40,40);
        fastForward.size = new PVector(120,120);
        gameHUD.add(fastForward);

    }

    private void buttonInput() {
        if(state == GameState.MAIN_MENU){
            for(Button b : mainMenu){
                b.draw();
                if(b.isPointInside(mouseX,mouseY)){
                    b.input();
                }
            }
        }
        if(state == GameState.GAME_PLAY){
            for(Button b : gameHUD){
                b.draw();
                if(b.isPointInside(mouseX,mouseY)){
                    b.input();
                }
            }
        }
    }

}
