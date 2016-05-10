package com.minemeander.objects;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import com.minemeander.Art;
import com.minemeander.Constant;
import com.minemeander.Level;
import com.minemeander.ai.PathNode;

public class Zombie extends GameObject implements Climber {

	private final static float ZOMBIE_SPEED = 4.0f;
	private final static float ZOMBIE_PURSUIT_DISTANCE = 10.0f;
	private final static float ZOMBIE_MAX_TIME_TO_REACH_WAYPOINT = 20.0f; // sec

	private Vector2 antiGravityVector;
	private boolean climbingUp = false;
	private boolean climbingDown = false;

	private float targetTileSelectionTimestamp;
	private ZombieAnimationEnum animation = ZombieAnimationEnum.IDLE;

	final static Vector2 upImpulseVector = new Vector2(0.0f, ZOMBIE_SPEED * 3);
	final static Vector2 jumpRightImpulseVector = new Vector2(ZOMBIE_SPEED * 50, ZOMBIE_SPEED * 50);
	final static Vector2 jumpLeftImpulseVector = new Vector2(-ZOMBIE_SPEED * 50, ZOMBIE_SPEED * 60);
	final static Vector2 downImpulseVector = new Vector2(0.0f, -ZOMBIE_SPEED * 3);
	final static Vector2 leftImpulseVector = new Vector2(-ZOMBIE_SPEED, 0.0f);
	final static Vector2 rightImpulseVector = new Vector2(ZOMBIE_SPEED, 0.0f);

	public Zombie(int id, Level level, float x, float y) {
		super(id, level, x, y, CollisionCategory.ENEMY, true);
		antiGravityVector = level.gravityVector.cpy().scl(-body.getMass()*1.1f);
	}
	
	@Override
	protected void createFixtures(Level level, Body body, float widthRatio, float heightRatio) {
		PolygonShape polyShape = new PolygonShape();
		polyShape.setAsBox(width*0.2f, height*0.4f);
		fixture = body.createFixture(polyShape, 5);
		fixture.setFriction(0f);
		polyShape.dispose();		
		
		CircleShape circle = new CircleShape();
		circle.setRadius(0.40f);
		circle.setPosition(new Vector2(0f, -0.5f));
		fixture = body.createFixture(circle, 0);
		fixture.setFriction(0f);
		circle.dispose();
	}

	ShapeRenderer shapeRenderer = new ShapeRenderer();
	SpriteBatch spriteBatch = new SpriteBatch();

	@Override
	public void renderDebugInfo() {
		Vector2 position = body.getPosition();

		if (wayPointList != null) {
			shapeRenderer.setProjectionMatrix(level.camera.front.combined);

			float oldx = position.x;
			float oldy = position.y;

			for (int i = wayPointList.size() - 1; i >= 0; i--) {
				PathNode wayPointNode = wayPointList.get(i);
				float x1 = wayPointNode.x * Constant.METERS_PER_TILE;
				float y1 = wayPointNode.y * Constant.METERS_PER_TILE;

				shapeRenderer.begin(ShapeType.Line);
				shapeRenderer.setColor(Color.WHITE);
				shapeRenderer.rect(x1, y1, 2, 2);
				shapeRenderer.end();

				shapeRenderer.begin(ShapeType.Line);
				shapeRenderer.line(oldx, oldy, x1+1, y1+1);
				shapeRenderer.end();
				oldx = x1 + 1;
				oldy = y1 + 1;
			}

			shapeRenderer.end();
		}
		else {
			spriteBatch.begin();
			level.font.draw(spriteBatch, "NO TARGET", position.x, position.y);
			spriteBatch.end();
		}
	}

	private ArrayList<PathNode> wayPointList = new ArrayList<PathNode>();

	@Override
	public TextureRegion getTextureRegion(float tick) {

		Vector2 currentTile = getTile();

		if (wayPointList.size() == 0 || (tick - targetTileSelectionTimestamp) > ZOMBIE_MAX_TIME_TO_REACH_WAYPOINT) {
			resetWayPoints(tick, currentTile);
		}
		else {
			Vector2 vectorToWayPoint = distanceToWayPoint(wayPointList.get(wayPointList.size() - 1));			
			if (vectorToWayPoint.len() <= 0.2f) {
				wayPointList.remove(wayPointList.size() - 1); // way point reached
				body.setLinearVelocity(0f, 0f);
			}
			else {
				// Way point not reached
				PathNode targetPathNode = wayPointList.get(wayPointList.size() - 1);

				if (targetPathNode.x != currentTile.x && targetPathNode.y != currentTile.y) {
					wayPointList.clear(); // "lost in translation", due to a bounce or something
				}
				else {
					decideAction(vectorToWayPoint);
				}
			}
		}

		return computeAnimation(tick);
	}

	private void decideAction(Vector2 vectorToWayPoint) {		
		if (Math.abs(vectorToWayPoint.x) > Math.abs(vectorToWayPoint.y)) {
			// Move horizontally
			if (vectorToWayPoint.x > 0) {
				walkRight();
			}
			else if (vectorToWayPoint.x < 0) {
				walkLeft();
			}
		}
		else {
			// Move vertically
			if (vectorToWayPoint.y > 0) {
				climbUp();
			}
			else if (vectorToWayPoint.y < 0) {
				climbDown();
			}
		}
	}

	Vector2 distanceToWayPointVector = new Vector2();

	private Vector2 distanceToWayPoint(PathNode pathNode) {
		Vector2 position = body.getPosition();
		float tileX = pathNode.x * Constant.METERS_PER_TILE + 1;
		float tileY = pathNode.y * Constant.METERS_PER_TILE + 1;
		return distanceToWayPointVector.set(tileX, tileY).sub(position);
	}

	private void resetWayPoints(float tick, Vector2 currentTile) {
		// Pick an new target if none exists, or if last one is reached, or took too long to reach
		targetTileSelectionTimestamp = tick;

		Vector2 jackTile = level.objectManager.getJack().getTile();
		if (currentTile.dst(jackTile) < ZOMBIE_PURSUIT_DISTANCE) {
			Vector2 targetTile = findWayPointTile(wayPointList, jackTile);
			if (targetTile == null) {
				findRandomWayPointTile(wayPointList, 20);
			}
		}
		else {
			findRandomWayPointTile(wayPointList, 20);
		}
	}

	private TextureRegion computeAnimation(float tick) {
		// Compute animation
		animation = ZombieAnimationEnum.IDLE;

		if (climbingDown || climbingUp) {
			animation = ZombieAnimationEnum.CLIMBING;
		}
		else if (body.getLinearVelocity().x > 0) {
			animation = ZombieAnimationEnum.WALK_RIGHT;
		}
		else if (body.getLinearVelocity().x < 0) {
			animation = ZombieAnimationEnum.WALK_LEFT;
		}

		return animation.getAnimation().getKeyFrame(tick, true);
	}

	private void climbUp() {
		climbingUp = true;
		climbingDown = false;
		body.applyForce(antiGravityVector, FORCE_APPLICATION_POINT, true);
		body.applyLinearImpulse(upImpulseVector, FORCE_APPLICATION_POINT, true);
	}

	private void climbDown() {
		climbingUp = false;
		climbingDown = true;
		body.applyForce(antiGravityVector, FORCE_APPLICATION_POINT, true);
		body.applyLinearImpulse(downImpulseVector, FORCE_APPLICATION_POINT, true);
	}

	private void walkLeft() {
		if (climbingUp) {
			body.applyLinearImpulse(jumpLeftImpulseVector, FORCE_APPLICATION_POINT, true);	
		}
		climbingUp = false;
		climbingDown = false;
		body.applyLinearImpulse(leftImpulseVector, FORCE_APPLICATION_POINT, true);
	}

	private void walkRight() {
		if (climbingUp) {
			body.applyLinearImpulse(jumpRightImpulseVector, FORCE_APPLICATION_POINT, true);	
		}
		climbingUp = false;
		climbingDown = false;
		body.applyLinearImpulse(rightImpulseVector, FORCE_APPLICATION_POINT, true);
	}

	public ZombieAnimationEnum getState() {
		return animation;
	}

	@Override
	public boolean isClimbing() {
		return climbingUp || climbingDown;
	}

	public static enum ZombieAnimationEnum {
		IDLE(Art.zombieWalkingLeftAnimation), WALK_LEFT(Art.zombieWalkingLeftAnimation), WALK_RIGHT(Art.zombieWalkingRightAnimation), CLIMBING(Art.zombieClimbAnimation);
		private Animation animation;

		private ZombieAnimationEnum(Animation animation) {
			this.animation = animation;
		}

		public Animation getAnimation() {
			return animation;
		}
	}
}
