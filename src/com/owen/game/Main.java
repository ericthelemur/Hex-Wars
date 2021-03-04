package com.owen.game;

public class Main {
    public static void main(String[] args) {
        new Thread() {
            {
                setDaemon(true);
                start();
            }
            public void run() {
                  while(true) {
                    try {Thread.sleep(Long.MAX_VALUE); }
                    catch(Exception ignored) {}
                }
            }
        };
        // Start game
        new Game();
    }
}
