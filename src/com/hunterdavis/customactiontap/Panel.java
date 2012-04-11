package com.hunterdavis.customactiontap;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

class Panel extends SurfaceView implements SurfaceHolder.Callback {

	InventorySQLHelper scoreData = null;

	// member variables
	private CanvasThread canvasthread;
	public Boolean surfaceCreated;
	public Context mContext;
	int difficulty = 0;
	public Boolean gameOver = false;
	public Boolean gamePaused = false;
	public Boolean introScreenOver = false;
	Point player1Wants = new Point(0, 0);
	public Bitmap cometBitmap = null;
	public Bitmap cometBitmapLarge = null;
	public Bitmap cometBitmapSmall = null;
	public Uri selectedImageUri = null;
	public Uri selectedPlayerUri = null;
	String playerName = null;
	int mwidth = 0;
	int mheight = 0;
	int player1Score = 0;
	int cometSize = 0;
	int player1Lives = 0;
	int player1Health = 0;
	int enemiesKilled = 0;
	int enemiesPerLevel = 0;
	int enemiesKilledThisLevel = 0;
	int level = 1;
	int enemyDamage = 0;
	int numberOfEnemiesAllowed = 0;
	int numberOfBulletsAllowed = 0;
	int weaponLevel = 0;
	Random myrandom = new Random();
	List<Enemy> enemyList;
	List<bloodPoint> bloodList;
	List<Point> tapList;
	List<PowerUp> powerUpList;
	List<effectPoint> effectPointList;

	// tweaking for game mechanics
	int numberOfPowerUps = 7;
	int axeSize = 10;
	int axeLevel = 100;
	int enemiesPerLevelConstant = 10;
	int powerUpDropPercentage = 10;
	int numberOfBloodSpots = 5000;
	int numFireParticles = 100;
	int fireRadius = 100;
	int iceRadius = 100;
	int bombRadius = 100;
	int maxDepthValue = 3;
	int player1Step = 2;
	int player1IconSize = 12;
	int bloodTTL = 5;
	int powerUpTTL = 2500;
	int effectTTL = 5;
	int bulletStep = 10;
	int player1Size = 5;
	int emenyStartDamage = 4;
	int player1StartingHealth = 40;
	int boomStickHurtValue = 50;
	int playerColor = Color.rgb(0, 0, 234);
	int scoreColor = Color.rgb(0, 0, 234);
	int healthBarColor = Color.rgb(255, 35, 35);
	int enemyHealthColor = Color.rgb(255, 65, 65);
	int enemyBloodColor = Color.rgb(255, 255, 255);
	int itemBackgroundColor = Color.LTGRAY;
	int bulletColor = Color.rgb(47, 79, 79);
	int player1LivesStarting = 3;
	int maxBulletSize = 25;
	int initialNumberOfEnemies = 5;
	int numCracks = 3;
	Boolean shootReverse = false;
	private static final float EPS = (float) 0.000001;

	public class Enemy {
		public int x;
		public int y;
		public int size;
		public int healthPoints;
		public Boolean left;
		public Boolean down;

		Enemy(int xa, int ya, int sizea, int healtha, Boolean lefta,
				Boolean downa) {
			x = xa;
			y = ya;
			size = sizea;
			healthPoints = healtha;
			left = lefta;
			down = downa;
		}

		Enemy(int xa, int ya) {
			x = xa;
			y = ya;
			size = myrandom.nextInt(3);
			healthPoints = (int) Math.pow(2, size);
			left = myrandom.nextBoolean();
			down = true;

		}

		Enemy(Enemy newComet) {
			x = newComet.x;
			y = newComet.y;
			size = newComet.size;
			healthPoints = newComet.healthPoints;
			left = newComet.left;
			down = newComet.down;
		}
	}

	public class PowerUp {
		int left;
		int right;
		int top;
		int bottom;
		int powerUpType;
		int age;

		PowerUp(int lefta, int righta, int topa, int bottoma, int powerUpTypea,
				int agea) {
			left = lefta;
			right = righta;
			top = topa;
			bottom = bottoma;
			powerUpType = powerUpTypea;
			age = agea;
		}

		PowerUp(int lefta, int righta, int topa, int bottoma) {
			left = lefta;
			right = righta;
			top = topa;
			bottom = bottoma;
			powerUpType = myrandom.nextInt(numberOfPowerUps);
			age = 0;
		}

		PowerUp(PowerUp powerUpA) {
			left = powerUpA.left;
			right = powerUpA.right;
			top = powerUpA.top;
			bottom = powerUpA.bottom;
			powerUpType = powerUpA.powerUpType;
			age = powerUpA.age;
		}
	}

	public class effectPoint {
		public int x;
		public int y;
		public int age;
		public int color;

		effectPoint(int xa, int ya, int agea, int colora) {
			x = xa;
			y = ya;
			age = agea;
			color = colora;
		}

		effectPoint(effectPoint b) {
			x = b.x;
			y = b.y;
			age = b.age;
			color = b.color;

		}

		effectPoint() {
			x = myrandom.nextInt(mwidth);
			y = myrandom.nextInt(mheight);
			age = 0;
			color = Color.CYAN;
		}

	}

	public class bloodPoint {
		public int x;
		public int y;
		public int age;

		bloodPoint(int xa, int ya, int agea) {
			x = xa;
			y = ya;
			age = agea;
		}

		bloodPoint(bloodPoint b) {
			x = b.x;
			y = b.y;
			age = b.age;

		}

		bloodPoint() {
			x = myrandom.nextInt(mwidth);
			y = myrandom.nextInt(mheight);
			age = 0;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		synchronized (getHolder()) {

			int action = event.getAction();
			if (action == MotionEvent.ACTION_DOWN) {

				Point mypoint = new Point((int) event.getX(),
						(int) event.getY());
				tapList.add(mypoint);

				player1Wants = mypoint;

				if (gamePaused == true) {
					gamePaused = false;
				}

				return true;
			} else if (action == MotionEvent.ACTION_MOVE) {
				if (weaponLevel > 0) {
					int historySize = event.getHistorySize();
					for (int i = 0; i < historySize - 1; i++) {
						Point mypoint = new Point(
								(int) event.getHistoricalX(i),
								(int) event.getHistoricalY(i));
						tapList.add(mypoint);

					}

					Point mypoint = new Point((int) event.getX(),
							(int) event.getY());
					tapList.add(mypoint);
					player1Wants = mypoint;

					return true;
				}
			} else if (action == MotionEvent.ACTION_UP) {

				return true;
			}
			return true;
		}
	}

	public void setDifficulty(int difficult) {
		difficulty = difficult;
		initialNumberOfEnemies = (difficulty + 2) * 2;
		maxBulletSize = 25 + (5 * difficult);
		reset();
	}

	public void setUri(Uri uri) {
		selectedImageUri = uri;
		cometBitmap = null;
	}

	public void setScoreData(InventorySQLHelper scoreDataB) {
		scoreData = scoreDataB;
	}

	public void changeName(String name) {
		playerName = name;
	}

	public void setPlayerUri(Uri uri) {
		selectedPlayerUri = uri;
		cometBitmap = null;
	}

	public void setShootReverse(Boolean shot) {
		shootReverse = shot;
	}

	public void reset() {
		// reset everything
		gameOver = false;
		gamePaused = true;
		introScreenOver = false;
		enemiesPerLevel = enemiesPerLevelConstant;
		player1Score = 0;
		player1Lives = player1LivesStarting;
		player1StartingHealth = 20;
		player1Health = player1StartingHealth;
		enemiesKilled = 0;
		enemiesKilledThisLevel = 0;
		weaponLevel = 0;
		level = 1;
		enemyDamage = emenyStartDamage;
		numberOfEnemiesAllowed = initialNumberOfEnemies;
		numberOfBulletsAllowed = maxBulletSize;

		// clear lists
		enemyList = new ArrayList();
		tapList = new ArrayList();
		bloodList = new ArrayList();
		powerUpList = new ArrayList();
		effectPointList = new ArrayList();

		cometBitmap = null;
		cometBitmapLarge = null;
		cometBitmapSmall = null;
	}

	float fdistance(float x1, float y1, float x2, float y2) {
		return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}

	public Panel(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		//
		surfaceCreated = false;

		reset();

		getHolder().addCallback(this);
		setFocusable(true);
	}

	public void createThread(SurfaceHolder holder) {
		canvasthread = new CanvasThread(getHolder(), this, mContext,
				new Handler());
		canvasthread.setRunning(true);
		canvasthread.start();
	}

	public void terminateThread() {
		canvasthread.setRunning(false);
		try {
			canvasthread.join();
		} catch (InterruptedException e) {

		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		reset();

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		//
		if (surfaceCreated == false) {
			createThread(holder);
			// Bitmap kangoo = BitmapFactory.decodeResource(getResources(),
			// R.drawable.kangoo);
			surfaceCreated = true;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceCreated = false;

	}

	public void movePlayer1Tick() {
		if (introScreenOver == false) {

			introScreenOver = true;
		}
	}

	public void moveEnemiesTick() {
		for (int i = 0; i < enemyList.size(); i++) {
			Enemy enemy;
			try {
				enemy = enemyList.get(i);
			} catch (Exception e) {
				//
				return;
			}

			if (enemy.down == true) {
				// we won't move the ball the first tick so we have the correct
				// screen
				// width and hieght
				if ((mwidth == mheight) && (mwidth == 0)) {
					return;
				}

				try {
					enemyList.set(i, incrementEnemyOnLine(enemy));
				} catch (Exception e) {
					//
					return;
				}
			}

		}
	}

	public void testForCollisionAndProcess() {

		// loop through all comets

		for (int i = enemyList.size() - 1; i >= 0; i--) {

			Enemy enemy;
			try {
				enemy = enemyList.get(i);
			} catch (Exception e) {
				//
				return;
			}

			int localEnemySize = cometSize;
			if (enemy.size == 0) {
				localEnemySize = cometSize / 2;
			} else if (enemy.size == 2) {
				localEnemySize = cometSize * 2;
			}

			Rect enemyRect = new Rect();
			enemyRect.left = enemy.x - localEnemySize / 2;
			enemyRect.right = enemy.x + localEnemySize / 2;
			enemyRect.top = enemy.y + localEnemySize / 2;
			enemyRect.bottom = enemy.y - localEnemySize / 2;

			// test if comet went offscreen
			if (enemyRect.bottom < 1) {
				enemyList.remove(i);
				decrementHealth();
				return;
			}

			Boolean changedComet = false;
			for (int j = tapList.size() - 1; j >= 0; j--) {
				Point localPoint;
				try {
					localPoint = tapList.get(j);
				} catch (Exception e) {
					//
					return;
				}

				if (localPoint != null) {

					if ((localPoint.x <= enemyRect.right)
							&& (localPoint.x >= enemyRect.left)) {

						if ((localPoint.y >= enemyRect.bottom)
								&& (localPoint.y <= enemyRect.top)) {
							// we hit this comet with a bullet
							enemy.healthPoints--;
							player1Score += 50;
							changedComet = true;

							try {
								if (i > 0) {
									tapList.remove(j);
								}
							} catch (Exception e) {
								//
								return;
							}
						}

					}
				}
			} // end of taplist
			if (changedComet) {
				try {
					enemyList.set(i, enemy);
				} catch (Exception e) {
					//
					return;
				}
			}

		}// end of main for loop of enemies

		for (int i = powerUpList.size() - 1; i >= 0; i--) {
			PowerUp myPower;
			try {
				myPower = powerUpList.get(i);
			} catch (Exception e) {
				//
				return;
			}

			Boolean powerUpRemoved = false;
			for (int j = tapList.size() - 1; j > 0; j--) {
				if (powerUpRemoved == false) {
					Point localPoint;
					try {
						localPoint = tapList.get(j);
					} catch (Exception e) {
						//
						return;
					}
					if (localPoint != null) {
						if ((localPoint.x <= myPower.right)
								&& (localPoint.x >= myPower.left)) {

							if ((localPoint.y >= myPower.bottom)
									&& (localPoint.y <= myPower.top)) {
								// we hit this powerup
								activatePowerUp(myPower.powerUpType,
										localPoint.x, localPoint.y);
								try {
									powerUpList.remove(i);
								} catch (Exception e1) {
									//
									e1.printStackTrace();
								}
								powerUpRemoved = true;
								try {
									if (i > 0) {
										tapList.remove(j);
									}
								} catch (Exception e) {
									//
									return;
								}
							}

						}
					}
				}
				// if we're at the end of the loop, kill all errant taplist
				if (i == 0) {
					tapList.remove(j);
				}
			}

		}

	}

	public void activatePowerUp(int powerUpType, int x, int y) {
		switch (powerUpType) {
		case 0:
			activateLightning(x, y);
			break;
		case 1:
			activateFire(x, y);
			break;
		case 2:
			activateIce(x, y);
			break;
		case 3:
			activateWind(x, y);
			break;
		case 4:
			activateBomb(x, y);
			break;
		case 5:
			activateAxe(x, y);
			break;
		case 6:
			activateMega(x, y);
			break;
		default:
			break;
		}
	}

	public void activateLightning(int x, int y) {
		// create a lightning strike that runs from x,y to four corners
		Boolean lightningStretching = true;
		Boolean edgea = false;
		Boolean edgeb = false;
		Boolean edgec = false;
		Boolean edged = false;
		int distance = 0;
		int newxa = 0;
		int newya = 0;
		int newxb = 0;
		int newyb = 0;

		int color = Color.YELLOW;

		int oldEffectsPointSize = effectPointList.size();

		while (lightningStretching) {
			distance++;

			if (edgea == false) {
				newxa = x + distance;
				if (newxa >= mwidth) {
					edgea = true;
				}
			}
			if (edgeb == false) {
				newya = y + distance;
				if (newya >= mheight) {
					edgeb = true;
				}
			}
			if (edgec == false) {
				newyb = y - distance;
				if (newyb < 1) {
					edgec = true;
				}
			}
			if (edged == false) {
				newxb = x - distance;
				if (newxb < 1) {
					edged = true;
				}
			}

			effectPoint epa = new effectPoint(newxa, newya, 0, color);
			effectPointList.add(epa);
			epa = new effectPoint(newxa, newyb, 0, color);
			effectPointList.add(epa);
			epa = new effectPoint(newxb, newya, 0, color);
			effectPointList.add(epa);
			epa = new effectPoint(newxb, newyb, 0, color);
			effectPointList.add(epa);

			if ((edgea == true) && (edgeb == true) && (edgec == true)
					&& (edged == true)) {
				lightningStretching = false;
			}

		}

		for (int i = enemyList.size() - 1; i >= 0; i--) {

			Enemy enemy;
			try {
				enemy = enemyList.get(i);
			} catch (Exception e) {
				//
				return;
			}

			int localEnemySize = cometSize;
			if (enemy.size == 0) {
				localEnemySize = cometSize / 2;
			} else if (enemy.size == 2) {
				localEnemySize = cometSize * 2;
			}

			Rect enemyRect = new Rect();
			enemyRect.left = enemy.x - localEnemySize / 2;
			enemyRect.right = enemy.x + localEnemySize / 2;
			enemyRect.top = enemy.y + localEnemySize / 2;
			enemyRect.bottom = enemy.y - localEnemySize / 2;

			Boolean changedComet = false;
			for (int j = oldEffectsPointSize; j < effectPointList.size(); j++) {
				effectPoint localPoint;
				try {
					localPoint = effectPointList.get(j);
				} catch (Exception e) {
					//
					return;
				}

				if ((localPoint.x <= enemyRect.right)
						&& (localPoint.x >= enemyRect.left)) {

					if ((localPoint.y >= enemyRect.bottom)
							&& (localPoint.y <= enemyRect.top)) {
						// we hit this comet with a bullet
						enemy.healthPoints -= 500;
						player1Score += 50;
						changedComet = true;
					}

				}
			} // end of taplist

			if (changedComet == true) {
				try {
					enemyList.set(i, enemy);
				} catch (Exception e) {
					//
					return;
				}
			}
		}

	}

	public void activateFire(int x, int y) {
		Rect playerRect = new Rect();
		playerRect.bottom = y - fireRadius;
		playerRect.top = y + fireRadius;
		playerRect.left = x - fireRadius;
		playerRect.right = x + fireRadius;

		boomstick(playerRect, -1, 0);
	}

	public void activateIce(int x, int y) {
		// draw a circle of effect points
		rasterEffectsCircle(x, y, iceRadius, Color.BLUE, 0);
		rasterEffectsCircle(x, y, iceRadius / 2, Color.BLUE, 2);
		rasterEffectsCircle(x, y, iceRadius / 3, Color.BLUE, 4);
		rasterEffectsCircle(x, y, iceRadius / 4, Color.BLUE, 8);

		for (int i = enemyList.size() - 1; i >= 0; i--) {

			Enemy enemy;
			try {
				enemy = enemyList.get(i);
			} catch (Exception e) {
				//
				return;
			}

			if (fdistance(x, y, enemy.x, enemy.y) < iceRadius) {
				enemy.down = false;
				try {
					enemyList.set(i, enemy);
				} catch (Exception e) {
					//
					return;
				}
			}
		}
	}

	public void activateWind(int x, int y) {

		// move all comets to the edge
		// loop through all comets
		for (int i = 0; i < enemyList.size(); i++) {

			Enemy enemy;
			try {
				enemy = enemyList.get(i);
			} catch (Exception e) {
				//
				return;
			}
			enemy.y = mheight;

			try {
				enemyList.set(i, enemy);
			} catch (Exception e) {
				//
				return;
			}

		}

	}

	public void activateBomb(int x, int y) {
		rasterEffectsCircle(x, y, bombRadius, Color.RED, 0);
		rasterEffectsCircle(x, y, 3 * bombRadius / 4, Color.RED, 2);
		rasterEffectsCircle(x, y, 2 * bombRadius / 4, Color.RED, 4);
		rasterEffectsCircle(x, y, bombRadius / 4, Color.RED, 8);

		for (int i = enemyList.size() - 1; i >= 0; i--) {

			Enemy enemy;
			try {
				enemy = enemyList.get(i);
			} catch (Exception e) {
				//
				return;
			}
			if (fdistance(x, y, enemy.x, enemy.y) < iceRadius) {
				enemy.healthPoints -= boomStickHurtValue;
				try {
					enemyList.set(i, enemy);
				} catch (Exception e) {
					return;
				}
			}
		}

	}

	public void activateAxe(int x, int y) {
		weaponLevel += axeLevel;
	}

	public void activateMega(int x, int y) {
		numberOfEnemiesAllowed++;
	}

	public void rasterEffectsCircle(int x0, int y0, int radius, int color,
			int age) {
		int f = 1 - radius;
		int ddF_x = 1;
		int ddF_y = -2 * radius;
		int x = 0;
		int y = radius;
		int localColor = color;

		effectPoint myEP = new effectPoint(x0, y0 + radius, age, localColor);
		effectPointList.add(myEP);
		myEP = new effectPoint(x0, y0 - radius, age, localColor);
		effectPointList.add(myEP);
		myEP = new effectPoint(x0 + radius, y0, age, localColor);
		effectPointList.add(myEP);
		myEP = new effectPoint(x0 - radius, y0, age, localColor);
		effectPointList.add(myEP);

		while (x < y) {
			// ddF_x == 2 * x + 1;
			// ddF_y == -2 * y;
			// f == x*x + y*y - radius*radius + 2*x - y + 1;
			if (f >= 0) {
				y--;
				ddF_y += 2;
				f += ddF_y;
			}
			x++;
			ddF_x += 2;
			f += ddF_x;

			myEP = new effectPoint(x0 + x, y0 + y, age, localColor);
			effectPointList.add(myEP);

			myEP = new effectPoint(x0 - x, y0 + y, age, localColor);
			effectPointList.add(myEP);

			myEP = new effectPoint(x0 + x, y0 - y, age, localColor);
			effectPointList.add(myEP);

			myEP = new effectPoint(x0 - x, y0 - y, age, localColor);
			effectPointList.add(myEP);

			myEP = new effectPoint(x0 + y, y0 + x, age, localColor);
			effectPointList.add(myEP);

			myEP = new effectPoint(x0 - y, y0 + x, age, localColor);
			effectPointList.add(myEP);

			myEP = new effectPoint(x0 + y, y0 - x, age, localColor);
			effectPointList.add(myEP);

			myEP = new effectPoint(x0 - y, y0 - x, age, localColor);
			effectPointList.add(myEP);

		}
	}

	public void boomstick(Rect playerRect, Integer offset, Integer depth) {

		if (depth == maxDepthValue) {
			return;
		}
		for (int j = 0; j < enemyList.size(); j++) {
			if (j != offset) {
				Enemy enemy;
				try {
					enemy = enemyList.get(j);
				} catch (Exception e) {
					//
					return;
				}

				if (enemy.healthPoints > 0) {
					int localEnemySize = cometSize;
					if (enemy.size == 0) {
						localEnemySize = cometSize / 2;
					} else if (enemy.size == 2) {
						localEnemySize = cometSize * 2;
					}

					Rect enemyRect = new Rect();
					enemyRect.left = enemy.x - localEnemySize / 2;
					enemyRect.right = enemy.x + localEnemySize / 2;
					enemyRect.top = enemy.y + localEnemySize / 2;
					enemyRect.bottom = enemy.y - localEnemySize / 2;

					// test if player hit a comet
					boolean collision = doTheyOverlap(enemyRect, playerRect);
					if (collision) {
						enemy.healthPoints -= boomStickHurtValue;
						boomstick(enemyRect, j, depth + 1);
					}
				}
			}
		}
	}

	public boolean betweenOrOn(int a, int b, int c) {
		if (a >= b) {
			if (a <= c) {
				return true;
			}
		}
		return false;
	}

	public boolean doTheyOverlap(Rect one, Rect two) {

		// left side of one is in two
		if (betweenOrOn(one.left, two.left, two.right)) {
			// top side of one is in two
			if (betweenOrOn(one.top, two.bottom, two.top)) {
				return true;
			}

			// bottom side of one is in two
			if (betweenOrOn(one.bottom, two.bottom, two.top)) {
				return true;
			}

			// one is bigger and contains two
			if (betweenOrOn(two.bottom, one.bottom, one.top)
					&& betweenOrOn(two.top, one.bottom, one.top)) {
				return true;
			}

		}
		// right side of one is in two
		// left side of one is in two
		if (betweenOrOn(one.right, two.left, two.right)) {
			// top side of one is in two
			if (betweenOrOn(one.top, two.bottom, two.top)) {
				return true;
			}

			// bottom side of one is in two
			if (betweenOrOn(one.bottom, two.bottom, two.top)) {
				return true;
			}
			// one is bigger and contains two
			if (betweenOrOn(two.bottom, one.bottom, one.top)
					&& betweenOrOn(two.top, one.bottom, one.top)) {
				return true;
			}
		}

		// one is bigger and contains two
		if (betweenOrOn(two.left, one.left, one.right)
				&& betweenOrOn(two.right, one.left, one.right)) {
			// top side of one is in two
			if (betweenOrOn(one.top, two.bottom, two.top)) {
				return true;
			}

			// bottom side of one is in two
			if (betweenOrOn(one.bottom, two.bottom, two.top)) {
				return true;
			}
			// one is bigger and contains two
			if (betweenOrOn(two.bottom, one.bottom, one.top)
					&& betweenOrOn(two.top, one.bottom, one.top)) {
				return true;
			}
		}

		return false;
	}

	public void updatePowerUps() {
		for (int i = powerUpList.size() - 1; i >= 0; i--) {
			PowerUp bp;
			try {
				bp = powerUpList.get(i);
			} catch (Exception e) {
				//
				return;
			}

			bp.age++;
			if (bp.age > powerUpTTL) {
				try {
					powerUpList.remove(i);
				} catch (Exception e) {
					//
					return;
				}
			}
		}

	}

	public void updateEffectsTicks() {
		for (int i = effectPointList.size() - 1; i >= 0; i--) {
			effectPoint bp;
			try {
				bp = effectPointList.get(i);
			} catch (Exception e) {
				//
				return;
			}

			bp.age++;
			if (bp.age > effectTTL) {
				try {
					effectPointList.remove(i);
				} catch (Exception e) {
					//
					return;
				}
			}
		}
	}

	public void updateBloodTicks() {
		for (int i = bloodList.size() - 1; i >= 0; i--) {
			bloodPoint bp;
			try {
				bp = bloodList.get(i);
			} catch (Exception e) {
				//
				return;
			}

			bp.age++;
			if (bp.age > bloodTTL) {
				try {
					bloodList.remove(i);
				} catch (Exception e) {
					//
					return;
				}
			}
		}
	}

	public void saveHighScore() {
		SQLiteDatabase db = scoreData.getWritableDatabase();
		ContentValues values = new ContentValues();
		String scoreString = level + " " + playerName;
		values.put(InventorySQLHelper.NAMES, scoreString);
		values.put(InventorySQLHelper.SCORES, player1Score);
		db.insert(InventorySQLHelper.TABLE, null, values);
		db.close();
	}

	public void decrementHealth() {

		player1Health -= 500;

		if (player1Health < 1) {
			gamePaused = true;
			player1Lives--;
			player1Health = player1StartingHealth;
			if (player1Lives < 1) {
				gameOver = true;
				gamePaused = false;
				saveHighScore();

			}

			// move all comets to the edge
			// loop through all comets
			for (int i = 0; i < enemyList.size(); i++) {

				Enemy enemy;
				try {
					enemy = enemyList.get(i);
				} catch (Exception e) {
					//
					return;
				}
				enemy.y = mheight;

				try {
					enemyList.set(i, enemy);
				} catch (Exception e) {
					//
					return;
				}

			}

		}

	}

	public void updateGameState() {

		if (gameOver == true) {
			return;
		}

		if (gamePaused == true) {
			return;
		}

		// make sure there's a graphics init round
		if (mwidth == 0) {
			return;
		}

		// update the score a point for being alive
		// player1Score++;

		// make sure there are enough comets onscreen
		int cometDiff = numberOfEnemiesAllowed - enemyList.size();
		if (cometDiff > 0) {
			for (int i = 0; i < cometDiff; i++) {
				generateEnemy();
			}
		}

		// move player 1 a tick
		movePlayer1Tick();

		// move all asteroids a tick
		moveEnemiesTick();

		// test for bullet or player collision
		testForCollisionAndProcess();

		// update all the "blood" explosions
		updateBloodTicks();

		// update all the "effects" explosions
		updateEffectsTicks();

		// update all the power ups
		updatePowerUps();

		// update the weapon level
		if (weaponLevel > 0) {
			weaponLevel--;
		}

	}

	public void generateEnemy() {

		int randomwElement = myrandom.nextInt(mwidth);
		int randomhElement = myrandom.nextInt((mheight / 2)) + (mheight / 2);
		int whichElement = myrandom.nextInt(3);
		int x = 0;
		int y = 0;

		switch (whichElement) {
		case 0:
			// left edge
			y = randomhElement;
			break;
		case 1:
			// right edge
			y = randomhElement;
			x = mwidth;
			break;
		case 2:
			// top edge
			y = randomhElement;
			x = randomwElement;
			break;
		case 3:
			// bottom edge is! allowed
			x = randomwElement;
			y = mheight;
			break;
		default:
			break;
		}

		//
		Enemy myComet = new Enemy(x, y);
		enemyList.add(myComet);

	}

	@Override
	public void onDraw(Canvas canvas) {

		mwidth = canvas.getWidth();
		mheight = canvas.getHeight();

		Paint paint = new Paint();

		// our player sizes should be a function both of difficulty and of
		// screen size
		int visualDivisor = mwidth;
		if (mheight < mwidth) {
			visualDivisor = mheight;
		}
		player1Size = visualDivisor / 8;
		cometSize = visualDivisor / 12;
		int cometSizeSmall = cometSize / 2;
		int cometSizeLarge = cometSize * 2;

		// draw player 1
		if (introScreenOver == true) {
			// Draw all the blood from the last round
			paint.setColor(enemyBloodColor);
			for (int i = bloodList.size() - 1; i >= 0; i--) {
				bloodPoint bp;
				try {
					bp = bloodList.get(i);
				} catch (Exception e) {
					//
					return;
				}
				canvas.drawPoint(bp.x, bp.y, paint);
			}

			// draw all the special effects from last round
			for (int i = effectPointList.size() - 1; i >= 0; i--) {
				effectPoint bp;
				try {
					bp = effectPointList.get(i);
				} catch (Exception e) {
					//
					return;
				}
				paint.setColor(bp.color);
				canvas.drawPoint(bp.x, bp.y, paint);
			}

			// draw the powerups
			for (int i = powerUpList.size() - 1; i >= 0; i--) {
				PowerUp bp;
				try {
					bp = powerUpList.get(i);
				} catch (Exception e) {
					//
					return;
				}
				switch (bp.powerUpType) {
				case 0:
					drawLightning(canvas, bp.left, bp.right, bp.top, bp.bottom);
					break;
				case 1:
					drawFire(canvas, bp.left, bp.right, bp.top, bp.bottom);
					break;
				case 2:
					drawIce(canvas, bp.left, bp.right, bp.top, bp.bottom);
					break;
				case 3:
					drawWind(canvas, bp.left, bp.right, bp.top, bp.bottom);
					break;
				case 4:
					drawBomb(canvas, bp.left, bp.right, bp.top, bp.bottom);
					break;
				case 5:
					drawAxe(canvas, bp.left, bp.right, bp.top, bp.bottom, true);
					break;
				case 6:
					drawMega(canvas, bp.left, bp.right, bp.top, bp.bottom);
					break;
				default:
					// we shouldn't get here ever
					break;
				}
			}

		}// introscreenover = true

		// draw comets
		if (cometBitmap == null) {
			// cometSize = mwidth / 5;
			// if we can't load somebody else's bitmap
			if (selectedImageUri == null) {
				Bitmap _scratch = BitmapFactory.decodeResource(getResources(),
						R.drawable.megusta);

				if (_scratch == null) {
					Toast.makeText(getContext(), "WTF", Toast.LENGTH_SHORT)
							.show();
				}

				// now scale the bitmap using the scale value
				cometBitmap = Bitmap.createScaledBitmap(_scratch, cometSize,
						cometSize, false);
				cometBitmapLarge = Bitmap.createScaledBitmap(_scratch,
						cometSizeLarge, cometSizeLarge, false);
				cometBitmapSmall = Bitmap.createScaledBitmap(_scratch,
						cometSizeSmall, cometSizeSmall, false);
			} else {
				//
				// THIS IS WHERE YOU LOAD FILE URIS AT
				InputStream photoStream = null;

				Context context = getContext();
				try {
					photoStream = context.getContentResolver().openInputStream(
							selectedImageUri);
				} catch (FileNotFoundException e) {
					//
					e.printStackTrace();
				}
				int scaleSize = decodeFile(photoStream, cometSize, cometSize);

				try {
					photoStream = context.getContentResolver().openInputStream(
							selectedImageUri);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inSampleSize = scaleSize;

				Bitmap photoBitmap = BitmapFactory.decodeStream(photoStream,
						null, o);
				cometBitmap = Bitmap.createScaledBitmap(photoBitmap, cometSize,
						cometSize, true);
				cometBitmapLarge = Bitmap.createScaledBitmap(photoBitmap,
						cometSizeLarge, cometSizeLarge, false);
				cometBitmapSmall = Bitmap.createScaledBitmap(photoBitmap,
						cometSizeSmall, cometSizeSmall, false);
				photoBitmap.recycle();

			}

		}

		// draw the comet bitmaps all over
		// for each comet
		if (cometBitmap != null) {
			for (int i = enemyList.size() - 1; i >= 0; i--) {
				int enemyListSize = enemyList.size();
				if (enemyListSize > i) {

					Enemy myComet;
					try {
						myComet = enemyList.get(i);
					} catch (Exception e) {
						//
						return;
					}

					switch (myComet.size) {
					case 0:
						if (myComet.healthPoints < 1) {
							drawExplosion(canvas, myComet.x
									- (cometSizeSmall / 2), myComet.x
									+ (cometSizeSmall / 2), myComet.y
									+ (cometSizeSmall / 2), myComet.y
									- (cometSizeSmall / 2));
							try {
								enemyList.remove(i);
							} catch (Exception e) {
								//
								return;
							}
							incrementEnemiesKilled(myComet.x
									- (cometSizeSmall / 2), myComet.y
									- (cometSizeSmall / 2), cometSizeSmall);
						} else {
							canvas.drawBitmap(cometBitmapSmall, myComet.x
									- (cometSizeSmall / 2), myComet.y
									- (cometSizeSmall / 2), paint);
							drawHealthAndTail(canvas,
									myComet.x - (cometSizeSmall / 2), myComet.x
											+ (cometSizeSmall / 2), myComet.y
											+ (cometSizeSmall / 2), myComet.y
											- (cometSizeSmall / 2), 100);

						}
						break;
					case 1:
						if (myComet.healthPoints < 1) {
							drawExplosion(canvas, myComet.x - (cometSize / 2),
									myComet.x + (cometSize / 2), myComet.y
											+ (cometSize / 2), myComet.y
											- (cometSize / 2));
							try {
								enemyList.remove(i);
							} catch (Exception e) {
								//
								return;
							}
							incrementEnemiesKilled(myComet.x - (cometSize / 2),
									myComet.y - (cometSize / 2), cometSize);
							enemyList.add(new Enemy(myComet.x, myComet.y, 0, 1,
									false, true));

						} else if (myComet.healthPoints < 2) {
							canvas.drawBitmap(cometBitmap, myComet.x
									- (cometSize / 2), myComet.y
									- (cometSize / 2), paint);
							drawCracks(canvas, myComet.x - (cometSize / 2),
									myComet.x + (cometSize / 2), myComet.y
											+ (cometSize / 2), myComet.y
											- (cometSize / 2));
							drawHealthAndTail(canvas, myComet.x - (cometSize / 2),
									myComet.x + (cometSize / 2), myComet.y
											+ (cometSize / 2), myComet.y
											- (cometSize / 2), 50);
						} else {
							canvas.drawBitmap(cometBitmap, myComet.x
									- (cometSize / 2), myComet.y
									- (cometSize / 2), paint);
							drawHealthAndTail(canvas, myComet.x - (cometSize / 2),
									myComet.x + (cometSize / 2), myComet.y
											+ (cometSize / 2), myComet.y
											- (cometSize / 2), 100);
						}
						break;
					case 2:
						if (myComet.healthPoints < 1) {
							drawExplosion(canvas, myComet.x
									- (cometSizeLarge / 2), myComet.x
									+ (cometSizeLarge / 2), myComet.y
									+ (cometSizeLarge / 2), myComet.y
									- (cometSizeLarge / 2));
							try {
								enemyList.remove(i);
							} catch (Exception e) {
								//
								return;
							}
							incrementEnemiesKilled(myComet.x
									- (cometSizeLarge / 2), myComet.y
									- (cometSizeLarge / 2), cometSizeLarge);
							enemyList.add(new Enemy(myComet.x, myComet.y, 1, 2,
									false, true));
						} else if (myComet.healthPoints < 4) {
							canvas.drawBitmap(cometBitmapLarge, myComet.x
									- (cometSizeLarge / 2), myComet.y
									- (cometSizeLarge / 2), paint);
							drawCracks(canvas,
									myComet.x - (cometSizeLarge / 2), myComet.x
											+ (cometSizeLarge / 2), myComet.y
											+ (cometSizeLarge / 2), myComet.y
											- (cometSizeLarge / 2));
							drawHealthAndTail(canvas,
									myComet.x - (cometSizeLarge / 2), myComet.x
											+ (cometSizeLarge / 2), myComet.y
											+ (cometSizeLarge / 2), myComet.y
											- (cometSizeLarge / 2),
									(25 * myComet.healthPoints));
						} else {

							canvas.drawBitmap(cometBitmapLarge, myComet.x
									- (cometSizeLarge / 2), myComet.y
									- (cometSizeLarge / 2), paint);
							drawHealthAndTail(canvas,
									myComet.x - (cometSizeLarge / 2), myComet.x
											+ (cometSizeLarge / 2), myComet.y
											+ (cometSizeLarge / 2), myComet.y
											- (cometSizeLarge / 2), 100);
						}

						break;
					default:
						Toast.makeText(getContext(),
								"Error in Enemy Rendering", Toast.LENGTH_SHORT)
								.show();
						break;
					}
				}
			}
		}

		// draw the hammer if your level is high enough
		if (weaponLevel > 0) {
			drawAxe(canvas, player1Wants.x - axeSize, player1Wants.x + axeSize,
					player1Wants.y - axeSize, player1Wants.y + axeSize, false);
		}

		// - draw comet in its position
		// - draw comet with its level of asplosion
		// - after drawing a comet that's fully asploded
		// - - remove it from the list
		// - - - generate a new comet
		// generateComet();
		if (introScreenOver == true) {
			// draw score 1
			paint.setColor(scoreColor);
			// draw score 2
			String nameString;
			nameString = playerName;
			canvas.drawText(nameString, 0, 22, paint);
			nameString = "Score:" + String.valueOf(player1Score);
			canvas.drawText(nameString, 0, 9, paint);

			if (cometBitmapSmall != null) {
				// for each live left draw a tiny life bitmap
				for (int i = 0; i < player1Lives; i++) {
					Paint painter = new Paint();
					canvas.drawBitmap(cometBitmapSmall,
							((3 + player1IconSize) * i), 30, painter);
				}
			}
			// draw the level number
			String levelString = "Level " + level;
			canvas.drawText(levelString, mwidth - 50, 9, paint);
			// draw the number of kills
			levelString = enemiesKilled + " Kills";
			canvas.drawText(levelString, mwidth - 50, 22, paint);

			// draw instructions for hammer
			if (weaponLevel > 0) {
				paint.setColor(Color.RED);
				canvas.drawText("HAMMER ACTIVE - HOLD TO DESTROY",
						mwidth / 2 - 100, mheight, paint);
			}

		}
		// draw game over if game over
		if (gameOver == true) {

			paint.setTextSize(20);
			canvas.drawText("Game Over", (mwidth / 2) - 50, mheight / 4, paint);

			paint.setColor(enemyBloodColor);
			bloodPoint bp;
			for (int i = 0; i < bloodList.size(); i++) {

				try {
					bp = bloodList.get(i);
				} catch (Exception e) {
					//
					return;
				}
				canvas.drawPoint(bp.x, bp.y, paint);
			}

		}

		if (gamePaused == true) {
			paint.setTextSize(25);
			paint.setColor(scoreColor);
			paint.setAntiAlias(true);
			canvas.drawText("Touch To Continue", (mwidth / 2) - 110, mheight
					- (mheight / 5), paint);

			paint.setTextSize(10);
			canvas.drawText("Don't Let The \"Balloons\" Escape!",
					(mwidth / 2) - 100, mheight - (mheight / 10), paint);
		}

	}

	public void incrementEnemiesKilled(int left, int bottom, int size) {

		// add bloodPointExplosion function

		if (myrandom.nextInt(100) < powerUpDropPercentage) {
			addPowerUp(left, left + size, bottom + size, bottom);
		} else {
			drawBloodExplosion(left, left + size, bottom + size, bottom);
		}

		enemiesKilledThisLevel++;
		enemiesKilled++;
		if (enemiesKilledThisLevel >= enemiesPerLevel) {
			level++;
			player1Lives++;
			// levelUpCharacter();
			enemiesKilledThisLevel = 0;
			numberOfEnemiesAllowed++;
			// numberOfBulletsAllowed += 5;
			enemiesPerLevel += enemiesPerLevelConstant;
		}
	}

	private void addPowerUp(int left, int right, int top, int bottom) {
		PowerUp mypower = new PowerUp(left, right, top, bottom);
		powerUpList.add(mypower);
	}

	private void drawBloodExplosion(int left, int right, int top, int bottom) {

		for (int i = 0; i < ((top - bottom) / 4); i++) {
			bloodPoint bpa = new bloodPoint(left, bottom, 0);
			bloodPoint bpb = new bloodPoint(left, top, 0);
			bloodPoint bpc = new bloodPoint(right, bottom, 0);
			bloodPoint bpd = new bloodPoint(right, top, 0);

			bloodPoint bpe = new bloodPoint(left, bottom
					+ Math.abs(top - bottom) / 2, 0);
			bloodPoint bpf = new bloodPoint(left + Math.abs(left - right) / 2,
					top, 0);
			bloodPoint bpg = new bloodPoint(left + Math.abs(left - right) / 2,
					bottom, 0);
			bloodPoint bph = new bloodPoint(right, bottom
					+ Math.abs(bottom - top) / 2, 0);

			bpa.x += i;
			bpa.y += i;

			bpb.x += i;
			bpb.y -= i;

			bpc.x -= i;
			bpc.y += i;

			bpd.x -= i;
			bpd.y -= i;

			bpe.x -= i;

			bpf.y -= i;

			bpg.y += i;

			bph.x += i;

			bloodList.add(bpa);
			bloodList.add(bpb);
			bloodList.add(bpc);
			bloodList.add(bpd);
			bloodList.add(bpe);
			bloodList.add(bpf);
			bloodList.add(bpg);
			bloodList.add(bph);

		}

	}

	public String getWeaponString() {
		switch (weaponLevel) {
		case 1:
			return "Double Pistol";
		case 2:
			return "Triple Pistol";
		case 3:
			return "Shotgun";
		case 4:
			return "BOOMSTICK";
		default:
			return "Pistol";

		}
	}

	private void drawHealthAndTail(Canvas canvas, int left, int right, int top,
			int bottom, int percentage) {
		Paint paint = new Paint();
		paint.setColor(enemyHealthColor);

		int oldWidth = right - left;
		double newWidth = (.01) * percentage * oldWidth;
		int newR = (int) (left + newWidth);
		int newBottom = top - (top - bottom) / 12;

		canvas.drawRect(left, top - (top - bottom) / 6, newR, newBottom, paint);
		
		int tailLength = 30;
		int randomWave = myrandom.nextInt(15);
		int tailx = left + ((right - left)/2);
		int taily = top;
		paint.setColor(Color.WHITE);
		for(int i= 0;i<tailLength;i++){
			canvas.drawPoint(tailx, taily, paint);
			taily++;
			if(i > tailLength-randomWave) {
				tailx++;
			}
		}

	}

	private void drawCracks(Canvas canvas, int left, int right, int top,
			int bottom) {
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(1);
		int xa, xb, ya, yb;
		int width = right - left;
		int height = top - bottom;
		for (int i = 0; i < numCracks; i++) {
			xa = myrandom.nextInt(width) + left;
			xb = myrandom.nextInt(width) + left;
			ya = myrandom.nextInt(height) + bottom;
			yb = myrandom.nextInt(height) + bottom;
			canvas.drawLine(xa, ya, xb, yb, paint);
		}
	}

	private void clearSlate(Canvas canvas, int left, int right, int top,
			int bottom) {
		Paint paint = new Paint();
		paint.setColor(itemBackgroundColor);
		canvas.drawRect(left, top, right, bottom, paint);

	}

	private void drawLightning(Canvas canvas, int left, int right, int top,
			int bottom) {
		int width = right - left;
		clearSlate(canvas, left, right, top, bottom);
		Paint paint = new Paint();
		paint.setColor(Color.YELLOW);

		canvas.drawLine(left + 2 * width / 3, bottom, left + width / 4, top,
				paint);

		canvas.drawPoint(left + width / 4 - 1, top - 1, paint);
		canvas.drawPoint(left + width / 4 + 1, top - 1, paint);
		canvas.drawPoint(left + width / 4 - 2, top - 2, paint);
		canvas.drawPoint(left + width / 4 + 2, top - 2, paint);

	}

	private void drawFire(Canvas canvas, int left, int right, int top,
			int bottom) {
		int width = right - left;
		int height = top - bottom;
		clearSlate(canvas, left, right, top, bottom);
		int x = 0;
		int y = 0;
		Paint paint = new Paint();
		paint.setColor(Color.RED);
		for (int i = 0; i < numFireParticles; i++) {
			x = myrandom.nextInt(width);
			y = myrandom.nextInt(height);
			canvas.drawPoint(left + x, bottom + y, paint);
		}
	}

	private void drawIce(Canvas canvas, int left, int right, int top, int bottom) {
		int width = right - left;
		int height = top - bottom;
		clearSlate(canvas, left, right, top, bottom);

		Paint borderPaint = new Paint();
		borderPaint.setColor(Color.BLUE);
		borderPaint.setAntiAlias(true);
		borderPaint.setStyle(Style.STROKE);
		borderPaint.setStrokeWidth(1);
		canvas.drawCircle(left + width / 2, bottom + height / 2, height / 3,
				borderPaint);

	}

	private void drawWind(Canvas canvas, int left, int right, int top,
			int bottom) {
		int height = top - bottom;
		clearSlate(canvas, left, right, top, bottom);
		// 3 white lines
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);

		int windLineSize = height / 10;

		canvas.drawRect(left + windLineSize, bottom + height / 5, right
				- windLineSize, bottom + height / 5 + windLineSize, paint);

		canvas.drawRect(left + windLineSize, top - height / 5, right
				- windLineSize, top - height / 5 - windLineSize, paint);

		canvas.drawRect(left + windLineSize, bottom + height / 2 - windLineSize
				/ 2, right - windLineSize, bottom + height / 2 + windLineSize
				/ 2, paint);

	}

	private void drawBomb(Canvas canvas, int left, int right, int top,
			int bottom) {
		int width = right - left;
		int height = top - bottom;
		clearSlate(canvas, left, right, top, bottom);
		int fuseWidth = 3;
		// draw a white fuse
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		canvas.drawRect(left + (width / 2), bottom + 2, left + (width / 2)
				+ fuseWidth, bottom + height / 5, paint);

		paint.setColor(Color.BLACK);
		canvas.drawCircle(left + (width / 2), bottom + (height / 2),
				height / 3, paint);

	}

	private void drawAxe(Canvas canvas, int left, int right, int top,
			int bottom, Boolean drawBackground) {
		int width = right - left;
		int height = top - bottom;
		if (drawBackground == true) {
			clearSlate(canvas, left, right, top, bottom);
		}
		// brown color for handle
		Paint paint = new Paint();
		paint.setColor(Color.rgb(153, 84, 84));

		int axeBuffer = height / 4;
		int handleWidth = height / 10;
		int handleLength = width / 2;

		canvas.drawRect(left, bottom + height / 2, left + handleLength, bottom
				+ height / 2 + handleWidth, paint);

		paint.setColor(Color.GRAY);

		canvas.drawRect(left + handleLength, bottom + axeBuffer, right, top
				- axeBuffer, paint);

	}

	private void drawMega(Canvas canvas, int left, int right, int top,
			int bottom) {
		int width = right - left;
		int height = top - bottom;
		clearSlate(canvas, left, right, top, bottom);

		// black color for plus plus
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);

		int lineWidth = width / 10;

		// plus
		// horiz line
		canvas.drawRect(left, bottom + height / 2 - lineWidth, right, bottom
				+ height / 2 + lineWidth, paint);
		// vert line
		canvas.drawRect(left + width / 2 - lineWidth, top, left + width / 2
				+ lineWidth, bottom, paint);

	}

	private void drawExplosion(Canvas canvas, int left, int right, int top,
			int bottom) {
		int x = (left + right) / 2;
		int y = (top + bottom) / 2;
		int radius = (right - left) / 2;

		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(1);
		canvas.drawCircle(x, y, radius, paint);

		canvas.drawLine(x - radius / 2, y + radius / 2, x + radius / 2, y
				+ radius / 2, paint);

		int eyestop = y - radius / 4;
		int eyesbottom = y - radius / 2;
		int lefteyeleft = x - radius / 2;
		int lefteyeright = x - radius / 4;
		int righteyeleft = x + radius / 4;
		int righteyeright = x + radius / 2;

		// left eye x
		canvas.drawLine(lefteyeleft, eyestop, lefteyeright, eyesbottom, paint);
		canvas.drawLine(lefteyeright, eyestop, lefteyeleft, eyesbottom, paint);

		// right eye x
		canvas.drawLine(righteyeleft, eyestop, righteyeright, eyesbottom, paint);
		canvas.drawLine(righteyeright, eyestop, righteyeleft, eyesbottom, paint);

	}

	// decodes image and scales it to reduce memory consumption
	private int decodeFile(InputStream photostream, int h, int w) {
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(photostream, null, o);

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < w || height_tmp / 2 < h)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		return scale;
	}

	public Enemy incrementEnemyOnLine(Enemy enemy) {
		Enemy returnEnemy = new Enemy(enemy);
		int speed = 1;

		// bounce off left wall or move left
		if (enemy.left == true) {
			if (enemy.x < speed) {
				returnEnemy.left = false;
			} else {
				returnEnemy.x -= speed;
			}
		}

		// bounce off right wall or move right
		if (enemy.left == false) {
			if (enemy.x > (mwidth - speed)) {
				returnEnemy.left = true;
			} else {
				returnEnemy.x += speed;
			}
		}

		// move up or down the field
		// bounce off paddles or walls
		if (enemy.down == true) {
			// if we hit player 2's paddle at bottom of screen (bottom = top
			// in
			// graphics)

			// if we hit the bottom of the screen
			{
				returnEnemy.y -= speed;
			}
		}

		if (enemy.down == false) {
			if (enemy.y > (mheight - speed)) {
				returnEnemy.down = true;
			} else {
				returnEnemy.y += speed;
			}
		}
		return returnEnemy;
	}

} // end class