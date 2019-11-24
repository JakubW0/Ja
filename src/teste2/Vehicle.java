package teste2;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * Referencia: http://www.asawicki.info/Mirror/Car%20Physics%20for%20Games/Car%20Physics%20for%20Games.html
 * http://www.carrosinfoco.com.br/carros/2014/06/a-arte-de-dirirgir-gerenciando-a-carga-sobre-os-pneus/
 * http://engineeringdotnet.blogspot.com.br/2010/04/simple-2d-car-physics-in-games.html
 * 
 * @author leonardo
 */
public class Vehicle {

    public double mass = 3000; // em kilograma

    public Vec3 position = new Vec3();
    public Vec3 direction = new Vec3(0, 1, 0);
    public Vec3 velocity = new Vec3();
    public Vec3 acceleration = new Vec3();

    public Vec3 position1 = new Vec3();
    public Vec3 direction1 = new Vec3(0, 1, 0);
    public Vec3 velocity1 = new Vec3();
    public Vec3 acceleration1 = new Vec3();


    public double cBraking = 500;
    public Vec3 fBraking = new Vec3();
    
    public double engineForce = 0;
    public Vec3 fTraction = new Vec3();
    
    public double cDrag = 0.4257;
    public Vec3 fDrag = new Vec3();

    public Vec3 fRolingResistence = new Vec3();
    public double cRolingResistence = 12.8;
    
    public Vec3 fLongtitudinal = new Vec3();
    
    public boolean isDrifting;
    public Vehicle() {
    }
    
    public void update() {
        
        double cTyre = 0.6;
        
        double dif = cTyre * (velocity.getSize() / 30.0);
        
        if (Keyboard.keydown[37]) {
            direction.rotateZ(Math.toRadians((cTyre) * velocity.getSize()));
            //velocity.rotateZ(Math.toRadians((cTyre - dif) * velocity.getSize()));
        }
        else if (Keyboard.keydown[39]) {
            direction.rotateZ(Math.toRadians((-cTyre) * velocity.getSize()));
            //velocity.rotateZ(Math.toRadians((-cTyre + dif) * velocity.getSize()));
        }
        
        // alinha a velocidade do carro com a direcao do carro
        // quanto menor a velocidade, maior o alinhamento
        {
            double difAngle = velocity.getRelativeAngleBetween(direction);
           // System.out.println("dif="+difAngle);
            if (!Double.isNaN(difAngle)) {
                double r = Math.random() * 50;
                velocity.rotateZ(difAngle/((50+r)*(5*dif)));
                
              //  isDrifting = Math.abs(Math.toDegrees(difAngle)) > 30;
            }
            
            //Vec3 velTmp = new Vec3();
            //velTmp.set(velocity);
            //double difAngle2 = 2 * dif * velTmp.relativeAngleBetween(direction);
            
            //if (difAngle2 > difAngle) {
            //    velocity.rotateZ(Math.toRadians(- 2 * dif / 10));
            //}
        
        }
        
        if (Keyboard.keydown[38]) {
            engineForce = 300;
        }
        else if (Keyboard.keydown[40]) {
            engineForce = -300;
        }
        else  {
            engineForce = 0;
        }
        
        if (isDrifting) {
            engineForce = 1  ;
        }
        

        boolean isBraking = Keyboard.keydown[66];
        
        calculateBraking();
        calculateTraction();
        calculateDrag();
        calculateRolingResistence();
        calculateLongtitudinalForce(isBraking);
        calculateAcceleration();
        calculateVelocity(1);
        calculatePosition(1);
    }
    
    private Point2D driftLastPoint1;
    private Point2D driftLastPoint2;
    
    public void draw(Graphics2D g, Graphics2D backright, Graphics2D backleft) {




        double angle = -Math.atan2(direction.x, direction.y);
        g.rotate(angle, position.x, position.y + 0);
        g.setColor(Color.ORANGE);
        g.fillRect((int) (position.x - 50), (int) (position.y - 30), 30, 50);


        AffineTransform transf = new AffineTransform();
        transf.rotate(angle, position.x, position.y + 0);
         backright.setColor(new Color(0, 0, 0, 16));
         BasicStroke basicStroke = new BasicStroke(5);
         backright.setStroke(basicStroke);
         backleft.setStroke(basicStroke);
        backright.rotate(angle, position.x, position.y + 0);
        backright.drawLine((int) (position.x -50),(int) (position.y - 0),(int) (position.x -50 ),(int) (position.y + 80));
        backleft.rotate(angle, position.x, position.y + 0);
        backleft.drawLine((int) (position.x -20),(int) (position.y - 0),(int) (position.x -20 ),(int) (position.y + 80));
      //  backg.clearRect((int) (position.x -20),(int) (position.y - 0),(int) (position.x -20 ),(int) (position.y + 80));
        AffineTransform transf1 = new AffineTransform();
        transf1.rotate(angle, position.x, position.y + 0);
    }

    
    // Ftraction = u * Engineforce, 
    private void calculateTraction() {
        fTraction.set(direction);
        fTraction.normalize();
        fTraction.scale(engineForce);
    }
 
    // Fdrag = - Cdrag * v * |v| 
    // where Cdrag is a constant and v is the velocity vector and the notation |v| refers to the magnitude of vector v    
    // speed = sqrt(v.x*v.x + v.y*v.y); 
    // fdrag.x = - Cdrag * v.x * speed; 
    // fdrag.y = - Cdrag * v.y * speed;    
    private void calculateDrag() {
        double speed = velocity.getSize();
        fDrag.set(velocity);
        fDrag.scale(speed);
        fDrag.scale(-cDrag);
    }

    // Frr = - Crr * v 
    // where Crr is a constant and v is the velocity vector.    
    private void calculateRolingResistence() {
        fRolingResistence.set(velocity);
        fRolingResistence.scale(-cRolingResistence);
    }
    
    // Flong = Ftraction + Fdrag + Frr
    private void calculateLongtitudinalForce(boolean isBraking) {
        if (isBraking) {
            fLongtitudinal.set(fBraking);
        }
        else {
            fLongtitudinal.set(fTraction);
        }
        fLongtitudinal.add(fDrag);
        fLongtitudinal.add(fRolingResistence);
    }
    
    private void calculateAcceleration() {
        acceleration.set(fLongtitudinal);
        acceleration.scale(1/mass);
    }
    
    private void calculateVelocity(double deltaTime) {
        acceleration.scale(deltaTime);
        velocity.add(acceleration);
    }
 
    private void calculatePosition(double deltaTime) {
        velocity.scale(deltaTime);
        position.add(velocity);
    }
    
    private void calculateBraking() {
        fBraking.set(velocity);
        fBraking.normalize();
        fBraking.scale(-cBraking);
    }



}
