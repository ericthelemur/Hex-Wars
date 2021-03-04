package com.owen.game.modules;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardModule implements KeyListener {

	private static final boolean[] held = new boolean[255];
	private static final boolean[] pressed = new boolean[255];
	private static final boolean[] released = new boolean[255];
	private static boolean ctrl = false;

	public void keyPressed(KeyEvent e) {
		int status = e.getID(), key = e.getKeyCode();
		if(status == KeyEvent.KEY_PRESSED && held.length > key) {
			if (!held[key]) {
				held[key] = true;
				pressed[key] = true;
			}
		}
		ctrl = e.isControlDown();
		e.consume();
	}

	public void keyReleased(KeyEvent e) {
		int status = e.getID(), key = e.getKeyCode();
		if(status == KeyEvent.KEY_RELEASED && held.length > key) {
			held[key] = false;
			released[key] = true;
		}
		ctrl = e.isControlDown();
		e.consume();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		e.consume();
	}

	public boolean isHeld(int keyCode){
		return held[keyCode];
	}

	public boolean isPressed(int keyCode) {
		if (pressed[keyCode]) {
			pressed[keyCode] = false;
			return true;
		}
		return false;
	}

	public boolean isReleased(int keyCode) {
		if (released[keyCode]) {
			released[keyCode] = false;
			return true;
		}
		return false;
	}

	public static boolean isCtrlPressed() {
		return ctrl;
	}
}
