package com.example.client.service;

import javafx.application.Platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameService {
    private final Counter counter;
    private final List<Target> targets;
    private final List<Thread> threads;
    private final Projectile projectile;
    private boolean isStopped = true;
    private boolean makeShot = false;

    public GameService(Counter counter, Projectile projectile, Target... _targets) {
        this.counter = counter;
        this.projectile = projectile;
        targets = new ArrayList<>(Arrays.asList(_targets));
        threads = new ArrayList<>();

        threads.add(
                new Thread(() -> {
                    while (true) {
                        while (isStopped) {
                            try {
                                synchronized (this) {
                                    wait();
                                }
                            } catch (InterruptedException ignored) {
                            }
                        }
                        for (var target: targets) {
                            target.move();
                        }
                        try {
                            Thread.sleep(60);
                        } catch (InterruptedException ignored) {}
                    }
                })
        );

        threads.add(
                new Thread(() -> {
                    Runnable updater = counter::incrementScoreCount;
                    while (true) {
                        while (isStopped) {
                            try {
                                synchronized (this) {
                                    wait();
                                }
                            } catch (InterruptedException ignored) {
                            }
                        }
                        while (makeShot && !isStopped) {
                            try {
                                projectile.move();
                            } catch (Exception e) {
                                makeShot = false;
                                break;
                            }
                            for (var target: targets) {
                                if (target.isInTarget(projectile.getX(), projectile.getY())) {
                                    Platform.runLater(updater);
                                    makeShot = false;
                                    break;
                                }
                            }
                        }
                        if (!isStopped) {
                            projectile.rollback();
                        }
                    }
                })
        );

        for (var thread: threads) {
            thread.start();
        }
    }

    public void start() {
        isStopped = false;
        synchronized (this) {
            notifyAll();
        }
    }

    public void stop(){
        isStopped = true;
    }

    public void makeShot(){
        if (isStopped || makeShot) return;
        makeShot = true;
        counter.incrementShotsCount();
    }

}
