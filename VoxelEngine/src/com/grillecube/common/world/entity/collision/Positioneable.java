package com.grillecube.common.world.entity.collision;

public interface Positioneable {

	/** positions */
	public float getPositionX();

	public float getPositionY();

	public float getPositionZ();

	/** position velocity */
	public float getPositionVelocityX();

	public float getPositionVelocityY();

	public float getPositionVelocityZ();

	/** position acceleration */
	public float getPositionAccelerationX();

	public float getPositionAccelerationY();

	public float getPositionAccelerationZ();

	/** positions */
	public void setPositionX(float x);

	public void setPositionY(float y);

	public void setPositionZ(float z);

	/** position velocity */
	public void setPositionVelocityX(float vx);

	public void setPositionVelocityY(float vy);

	public void setPositionVelocityZ(float vz);

	/** position acceleration */
	public void setPositionAccelerationX(float ax);

	public void setPositionAccelerationY(float ay);

	public void setPositionAccelerationZ(float az);

	/**
	 * update the velocity of the positioneable objects depending on the elasped
	 * time 'dt' and it current acceleration
	 * 
	 * @param positioneable
	 *            : the positioneable object
	 * @param dt
	 *            : elasped time
	 */
	public static void velocity(Positioneable positioneable, double dt) {
		positioneable.setPositionVelocityX(
				(float) (positioneable.getPositionVelocityX() + positioneable.getPositionAccelerationX() * dt));
		positioneable.setPositionVelocityY(
				(float) (positioneable.getPositionVelocityY() + positioneable.getPositionAccelerationY() * dt));
		positioneable.setPositionVelocityZ(
				(float) (positioneable.getPositionVelocityZ() + positioneable.getPositionAccelerationZ() * dt));
	}

	/**
	 * update the position of the positioneable objects depending on the elasped
	 * time 'dt' and it current velocity
	 * 
	 * @param positioneable
	 *            : the positioneable object
	 * @param dt
	 */
	public static void position(Positioneable positioneable, double dt) {
		positioneable.setPositionX((float) (positioneable.getPositionX() + positioneable.getPositionVelocityX() * dt));
		positioneable.setPositionY((float) (positioneable.getPositionY() + positioneable.getPositionVelocityY() * dt));
		positioneable.setPositionZ((float) (positioneable.getPositionZ() + positioneable.getPositionVelocityZ() * dt));
	}

	/** @return true if this positioneable objects is rotating */
	public static boolean isMoving(Positioneable positioneable) {
		return (positioneable.getPositionVelocityX() != 0 || positioneable.getPositionVelocityY() != 0
				|| positioneable.getPositionVelocityZ() != 0);
	}

}