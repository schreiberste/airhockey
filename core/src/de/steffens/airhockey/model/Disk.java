/*
 * Created on 26.04.2010
 *
 */
package de.steffens.airhockey.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.steffens.airhockey.model.vector.Vector2D;
import de.steffens.airhockey.model.vector.VectorFactory;

/**
 * A model representing a moving object shaped like a disk.
 *
 * @author Steffen Schreiber
 */
public class Disk extends MovingObject {

    private final double radius;
    private final double height;

    private short lastHitPlayerIndex = -1;
    
    private double mass = 0.1;
    
    private Vector2D tmp = VectorFactory.getVector(0, 0);
    
    /**
     * Create a new disk model with the given radius and height.
     * 
     * @param radius the radius of the new disk
     * @param height the height of the new disk
     */
    public Disk(double radius, double height) {
        this.radius = radius;
        this.height = height;
    }
    
    /**
     * Get the mass of this disk.
     * 
     * @return the mass
     */
    public double getMass() {
        return mass;
    }
    
    /**
     * Set the mass of this disk.
     * The mass will influence the response to collisions with other objects.
     * 
     * @param mass the new mass
     */
    public void setMass(double mass) {
        this.mass = mass;
    }
    
    public boolean isFixed() {
        return mass == Double.MAX_VALUE;
    }

    public void setFixed() {
        mass = Double.MAX_VALUE;
    }

    public double getRadius() {
        return radius;
    }

    public double getHeight() {
        return height;
    }


    public void setLastHitPlayerIndex(short lastHitPlayerIndex) {
        this.lastHitPlayerIndex = lastHitPlayerIndex;
    }


    public short getLastHitPlayerIndex() {
        return lastHitPlayerIndex;
    }


    @Override
    public String toString() {
        return "disk at " + getPosition() + ", velocity " + getVelocity().asVelocityString() +
               ", mass = " + (mass == Double.MAX_VALUE ? "infinite" : mass);
    }
    
    /**
     * Writes the disk and its attributes so
     * that a remote client may set it up correctly.
     * 
     * @param os the output stream to use for writing
     * @throws IOException if an error occurs during writing
     */
    public void write(DataOutputStream os) throws IOException {
    	os.writeDouble(radius);
    	os.writeDouble(height);
    	os.writeDouble(mass);
    	Vector2D v = tmp.reset(getPosition());
    	os.writeDouble(v.getX());
    	os.writeDouble(v.getY());
    	v = tmp.reset(getVelocity());
    	os.writeDouble(v.getX());
    	os.writeDouble(v.getY());
    	os.writeDouble(getAcceleration());
        os.writeShort(lastHitPlayerIndex);
        os.writeInt(getMaterial().getMaterialIdx());
        os.writeFloat(getMaterial().getAlpha());
    }

    /**
     * Writes the changeable attributes of the disk so
     * that a remote client may update it up correctly.
     * 
     * @param os the output stream to use for writing
     * @throws IOException if an error occurs during writing
     */
    public void writeUpdate(DataOutputStream os) throws IOException {
    	Vector2D v = tmp.reset(getPosition());
    	os.writeDouble(v.getX());
    	os.writeDouble(v.getY());
    	v = tmp.reset(getVelocity());
    	os.writeDouble(v.getX());
    	os.writeDouble(v.getY());
    	os.writeDouble(getAcceleration());
        os.writeShort(lastHitPlayerIndex);
        // NOTE: make sure that methods update() and skipUpdate() match!!!!
    }

    /**
     * Reads the changeable attributes of the disk so
     * that a remote client may update it up correctly.
     * 
     * @param is the input stream to use for reading
     * @throws IOException if an error occurs during reading
     */
    public void update(DataInputStream is) throws IOException {
    	setPosition(getPosition().reset(is.readDouble(), is.readDouble()));
    	setVelocity(getVelocity().reset(is.readDouble(), is.readDouble()));
    	setAcceleration(is.readDouble());
        lastHitPlayerIndex = is.readShort();
        // NOTE: make sure that methods writeUpdate() and skipUpdate() match!!!!
    }


    public static void skipUpdate(DataInputStream is) throws IOException {
        is.readDouble();
        is.readDouble();
        is.readDouble();
        is.readDouble();
        is.readDouble();
        is.readShort();
        // NOTE: make sure that methods update() and writeUpdate() match!!!!
    }

    /**
     * Reads the disk and its attributes so
     * that a remote client may set it up correctly.
     * 
     * @param is the input stream to use for reading
     * @return the disk read from the stream
     * @throws IOException if an error occurs during reading
     */
    public static Disk read(DataInputStream is) throws IOException {
    	Disk result = new Disk(is.readDouble(), is.readDouble());
    	result.setMass(is.readDouble());
    	result.setPosition(result.getPosition().reset(is.readDouble(), is.readDouble()));
    	result.setVelocity(result.getVelocity().reset(is.readDouble(), is.readDouble()));
    	result.setAcceleration(is.readDouble());
        result.setLastHitPlayerIndex(is.readShort());
    	int materialID = is.readInt();
        if (materialID < 0 || materialID > Material.defaultMaterials.length-1) {
            throw new RuntimeException("Unknown material: " + materialID);
        }
    	result.setMaterial(Material.defaultMaterials[materialID].getCopy());
        result.getMaterial().setAlpha(is.readFloat());

    	return result;
    }
}
