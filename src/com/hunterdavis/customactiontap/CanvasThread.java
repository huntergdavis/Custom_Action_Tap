package com.hunterdavis.customactiontap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceHolder;

public class CanvasThread extends Thread {
	private SurfaceHolder _surfaceHolder;
	private Panel _panel;
	private boolean _run = false;
	private Handler mHandler;
	private Context mContext;

	// for consistent rendering
	private long sleepTime;
	// amount of time to sleep for (in milliseconds)
	private long delay = 70;

	public CanvasThread(SurfaceHolder surfaceHolder, Panel panel,
			Context context, Handler handler) {
		_surfaceHolder = surfaceHolder;
		_panel = panel;
		this.mHandler = handler;
		this.mContext = context;

	}

	public void setRunning(boolean run) {
		_run = run;
	}

	public boolean getRunning() {
		return _run;
	}

	@Override
	public void run() {

		// UPDATE
		while (_run) {
			// time before update
			long beforeTime = System.nanoTime();
			// This is where we update the game engine
			_panel.updateGameState();

			// DRAW
			Canvas c = null;
			try {
				// lock canvas so nothing else can use it
				c = _surfaceHolder.lockCanvas(null);
				synchronized (_surfaceHolder) {
					Paint paint = new Paint();
					paint.setColor(Color.rgb(48,187,230));
					// clear the screen with the gray painter.
					c.drawRect(0, 0, c.getWidth(), c.getHeight(), paint);
					
					// add a sun
					paint.setColor(Color.YELLOW);
					c.drawCircle(c.getWidth(), 0, 55, paint);
					c.drawLine(c.getWidth() - 60,10,c.getWidth() - 70,20,paint);
					c.drawLine(c.getWidth() - 60,30,c.getWidth() - 70,40,paint);
					c.drawLine(c.getWidth() - 50,50,c.getWidth() - 60,60,paint);
					c.drawLine(c.getWidth() - 30,55,c.getWidth() - 40,65,paint);
					c.drawLine(c.getWidth() - 10,60,c.getWidth() - 20,70,paint);

					// draw us a nice black border
					//paint.setColor(Color.BLACK);
					// draw line 1
					/*c.drawLine(c.getWidth() / 4, c.getHeight(),
							c.getWidth() / 4,
							c.getHeight() - c.getHeight() / 6, paint);
					// draw line 2
					c.drawLine(c.getWidth() - c.getWidth() / 4, c.getHeight(),
							c.getWidth() - c.getWidth() / 4,
							c.getHeight() - c.getHeight() / 6, paint);
					// draw connecting line top
					c.drawLine(c.getWidth() / 4, c.getHeight() - c.getHeight()
							/ 6, c.getWidth() - c.getWidth() / 4, c.getHeight()
							- c.getHeight() / 6, paint);

					int leftBoxLeft = c.getWidth() / 6;
					int leftBoxRight = c.getWidth() / 4;
					int rightBoxLeft;
					int rightBoxRight;
					int topBoxTop = c.getHeight() / 6;
					int middleBoxTop = c.getHeight() / 3;
					int bottomBoxTop = c.getHeight() - c.getHeight() / 3;
					int topBoxBottom = c.getHeight() / 8;
					int middleBoxBottom = c.getHeight() / 2;
					int bottomBoxBottom = c.getHeight() - c.getHeight() / 4;

					paint.setColor(Color.LTGRAY);
					// draw top-left box
					// c.drawRect(leftBoxLeft, topBoxTop,
					// leftBoxRight,topBoxBottom, paint);

					// draw middle-left box
					// c.drawRect(leftBoxLeft, middleBoxTop,
					// leftBoxRight,middleBoxBottom, paint);

					// draw bottom-left box
					// c.drawRect(leftBoxLeft, bottomBoxTop,
					// leftBoxRight,bottomBoxBottom, paint);

				
					
					paint.setPathEffect(new DashPathEffect(new float[] { 15, 5,
							8, 5 }, 0));
					
					
					
					// show us a nice divider paint.setColor(Color.BLUE);
					c.drawLine(0, (c.getHeight() / 2),
							c.getWidth()/2,
							(c.getHeight() / 2 + c.getHeight() / 5), paint);
					c.drawLine(0, (c.getHeight() /2),
							c.getWidth()/2,
							(c.getHeight() / 2 - c.getHeight() / 5), paint);
					
					// opposite v
					c.drawLine(c.getWidth(), (c.getHeight() / 2),
							c.getWidth()/2,
							(c.getHeight() / 2 - c.getHeight() / 5), paint);
					c.drawLine(c.getWidth(), (c.getHeight() / 2),
							c.getWidth()/2,
							(c.getHeight() / 2 + c.getHeight() / 5), paint);
					*/
					// This is where we draw the game engine.
					_panel.onDraw(c);
				}
			} finally {
				// do this in a finally so that if an exception is thrown
				// during the above, we don't leave the Surface in an
				// inconsistent state
				if (c != null) {
					_surfaceHolder.unlockCanvasAndPost(c);
				}
			}

			// SLEEP
			// Sleep time. Time required to sleep to keep game consistent
			// This starts with the specified delay time (in milliseconds) then
			// subtracts from that the
			// actual time it took to update and render the game. This allows
			// our game to render smoothly.
			this.sleepTime = delay
					- ((System.nanoTime() - beforeTime) / 1000000L);

			try {
				// actual sleep code
				if (sleepTime > 0) {
					CanvasThread.sleep(sleepTime);
				}
			} catch (InterruptedException ex) {

			}
		}

	}
}