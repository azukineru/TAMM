package com.minemeander;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactFilter;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import com.minemeander.objects.Climber;
import com.minemeander.objects.Collectable;
import com.minemeander.objects.CollisionCategory;
import com.minemeander.objects.GameObject;
import com.minemeander.objects.GameObjectData;
import com.minemeander.objects.Jack;
import com.minemeander.objects.LevelObjectManager;
import com.minemeander.objects.Jack.JackStateEnum;

public class CollisionManager implements ContactFilter, ContactListener{

	public LevelObjectManager objectManager;

	public CollisionManager(LevelObjectManager objectManager) {
		this.objectManager = objectManager;
	}
	
	@Override
	public boolean shouldCollide(Fixture fixtureA, Fixture fixtureB) {
		final Body bodyA = fixtureA.getBody();
		final Body bodyB = fixtureB.getBody();
		GameObjectData userDataA = (GameObjectData) bodyA.getUserData();
		GameObjectData userDataB = (GameObjectData) bodyB.getUserData();
		CollisionCategory categoryA = userDataA.collisionCategory;
		CollisionCategory categoryB = userDataB.collisionCategory;		
		Jack jack = objectManager.getJack();
		
		if (categoryA == CollisionCategory.JACK || categoryB == CollisionCategory.JACK) {
			if (jack.isDead()) {
				return false;
			}
		}
		
		// Disable jack-platform collision when climbing
		if ((categoryA == CollisionCategory.JACK && categoryB == CollisionCategory.TRAVERSABLE_PLATFORM) 
					|| (categoryB == CollisionCategory.JACK && categoryA == CollisionCategory.TRAVERSABLE_PLATFORM)) {	
			return !jack.isClimbing() || !jack.wasDraggingDown;			
		}
		// Disable enemy-platform collision when climbing
		else if (categoryA == CollisionCategory.ENEMY && categoryB == CollisionCategory.TRAVERSABLE_PLATFORM) {
			GameObject gameObject = objectManager.get(userDataA.id);
			if (gameObject instanceof Climber) {
				return !((Climber)gameObject).isClimbing();
			}
			return true;
		}
		else if (categoryB == CollisionCategory.ENEMY && categoryA == CollisionCategory.TRAVERSABLE_PLATFORM) {			
			GameObject gameObject = objectManager.get(userDataB.id);
			if (gameObject instanceof Climber) {
				return !((Climber)gameObject).isClimbing();
			}
			return true;			
		}
				
		if (categoryA == CollisionCategory.ENEMY && categoryB == CollisionCategory.COLLECTABLE) {			
			return false;
		}
		else if (categoryB == CollisionCategory.ENEMY && categoryA == CollisionCategory.COLLECTABLE) {			
			return false;			
		}
		// Disable enemy-enemy collision (always)
		else if (categoryA == CollisionCategory.ENEMY && categoryB == CollisionCategory.ENEMY) {
			return false;
		}

		if (categoryA == CollisionCategory.ENEMY && categoryB == CollisionCategory.JACK) {
			return !objectManager.getJack().isInvicible();
		}
		else if (categoryB == CollisionCategory.ENEMY && categoryA == CollisionCategory.JACK) {
			return !objectManager.getJack().isInvicible();
		}
		
		return true;
	}
	
	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		Jack jack = objectManager.getJack();
		
		Fixture fixtureA = contact.getFixtureA();
		Fixture fixtureB = contact.getFixtureB();
		Body bodyA = fixtureA.getBody();
		Body bodyB = fixtureB.getBody();
		GameObjectData userDataA = (GameObjectData) bodyA.getUserData();		
		GameObjectData userDataB = (GameObjectData) bodyB.getUserData();
	
		if (userDataA.collisionCategory == CollisionCategory.JACK || userDataB.collisionCategory == CollisionCategory.JACK) {
			if (jack.isDead()) {
				contact.setEnabled(false);
			}
		}
		
		if ((userDataA.collisionCategory == CollisionCategory.JACK && userDataB.collisionCategory == CollisionCategory.TRAVERSABLE_PLATFORM)
				|| (userDataB.collisionCategory == CollisionCategory.JACK && userDataA.collisionCategory == CollisionCategory.TRAVERSABLE_PLATFORM)) {
			
			// drop down the the ladder below
			if (jack.wasDraggingDown || jack.isClimbing()) {
				contact.setEnabled(false);
			}
		}
		else if (userDataA.collisionCategory == CollisionCategory.ENEMY && userDataB.collisionCategory == CollisionCategory.TRAVERSABLE_PLATFORM) {
			GameObject gameObject = objectManager.get(userDataA.id);			
			if (gameObject instanceof Climber) { 
				if (((Climber) gameObject).isClimbing()) {
					contact.setEnabled(false);
				}
			}
		}
		else if ((userDataB.collisionCategory == CollisionCategory.ENEMY && userDataA.collisionCategory == CollisionCategory.TRAVERSABLE_PLATFORM)) {
			GameObject gameObject = objectManager.get(userDataB.id);
			if (gameObject instanceof Climber) {
				if (((Climber) gameObject).isClimbing()) {
					contact.setEnabled(false);
				}
			}
		}		
	}
	
	@Override
	public void beginContact(Contact contact) {
		Fixture fixtureA = contact.getFixtureA();
		Fixture fixtureB = contact.getFixtureB();
		Body bodyA = fixtureA.getBody();
		Body bodyB = fixtureB.getBody();
		GameObjectData userDataA = (GameObjectData) bodyA.getUserData();
		GameObjectData userDataB = (GameObjectData) bodyB.getUserData();
		if ((userDataA.collisionCategory == CollisionCategory.JACK && userDataB.collisionCategory == CollisionCategory.ENEMY)) {

			Jack jack = objectManager.getJack();
			if (!jack.isInvicible()) {				
				Vector2 positionA = bodyA.getPosition();
				Vector2 positionB = bodyB.getPosition();
				Vector2 b2a = positionA.sub(positionB).nor();
				bodyA.applyLinearImpulse(b2a.scl(200f), new Vector2(32, 32), true);					
				jack.onHit();
			}
		}
		else if ((userDataB.collisionCategory == CollisionCategory.JACK && userDataA.collisionCategory == CollisionCategory.ENEMY)) {
			//Art.hurtSound.play();
			Jack jack = objectManager.getJack();			
			if (!jack.isInvicible()) {
				Vector2 positionA = bodyA.getPosition();
				Vector2 positionB = bodyB.getPosition();
				Vector2 b2a = positionA.sub(positionB).nor();
				bodyB.applyLinearImpulse(b2a.rotate(180f).scl(200f), new Vector2(32, 32), true);			
				jack.onHit();
			}			
		}
		else if ((userDataB.collisionCategory == CollisionCategory.JACK && userDataA.collisionCategory == CollisionCategory.COLLECTABLE)) {
			//Art.coinSound.play();
			if (!userDataA.hidden) {
				Jack jack = objectManager.getJack();
				jack.onItemCollected((Collectable)objectManager.get(userDataA.id));
			}
			userDataA.hidden = true;
		}
		else if ((userDataA.collisionCategory == CollisionCategory.JACK && userDataB.collisionCategory == CollisionCategory.COLLECTABLE)) {
			//Art.coinSound.play();
			if (!userDataB.hidden) {
				Jack jack = objectManager.getJack();
				jack.onItemCollected((Collectable)objectManager.get(userDataB.id));
			}
			userDataB.hidden = true;
		}	
		else if (userDataA.collisionCategory == CollisionCategory.SOLID_PLATFORM && userDataB.collisionCategory != CollisionCategory.SOLID_PLATFORM) {
			userDataB.flying = false;
		}
		else if (userDataB.collisionCategory == CollisionCategory.SOLID_PLATFORM && userDataA.collisionCategory != CollisionCategory.SOLID_PLATFORM) {
			userDataA.flying = false;
		}
	}

	@Override
	public void endContact(Contact contact) {
		Fixture fixtureA = contact.getFixtureA();
		Fixture fixtureB = contact.getFixtureB();
		
		if (fixtureA == null || fixtureB == null) {
			return;
		}
		
		Body bodyA = fixtureA.getBody();
		Body bodyB = fixtureB.getBody();
		GameObjectData userDataA = (GameObjectData) bodyA.getUserData();
		GameObjectData userDataB = (GameObjectData) bodyB.getUserData();
		
		if (userDataA.collisionCategory == CollisionCategory.SOLID_PLATFORM && userDataB.collisionCategory != CollisionCategory.SOLID_PLATFORM) {
			userDataB.flying = true;
		}
		else if (userDataB.collisionCategory == CollisionCategory.SOLID_PLATFORM && userDataA.collisionCategory != CollisionCategory.SOLID_PLATFORM) {
			userDataA.flying = true;
		}
	}



	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub
		
	}



}
