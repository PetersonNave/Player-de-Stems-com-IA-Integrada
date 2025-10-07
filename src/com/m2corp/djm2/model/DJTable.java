// Em model/DJTable.java
package com.m2corp.djm2.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class DJTable {
    private List<AudioTrack> tracks;
    private long songLengthMicroseconds = 0;
    private Timer progressTimer;
    private volatile boolean isPlaying = false;

    public DJTable() {
        this.tracks = Collections.emptyList();
        this.progressTimer = new Timer(true);
    }

    public void loadSong(File[] audioFiles) {
        stopAllTracks();
        List<AudioTrack> newTracks = new ArrayList<>();

        for (int i = 0; i < audioFiles.length; i++) {
            try {
                String trackName = "(" + audioFiles[i].getName().replace(".wav", "") + ")";
                AudioTrack track = new AudioTrack(trackName, audioFiles[i]);
                newTracks.add(track);
            } catch (Exception e) {
                System.err.println("Error loading file: " + audioFiles[i].getName());
                e.printStackTrace();
            }
        }
        this.tracks = newTracks;

        this.songLengthMicroseconds = 0;
        for (AudioTrack track : tracks) {
            if (track.getMicrosecondLength() > this.songLengthMicroseconds) {
                this.songLengthMicroseconds = track.getMicrosecondLength();
            }
        }
        startPlayback();
    }

    private void startPlayback() {
        if (tracks.isEmpty()) return;

        for (AudioTrack track : tracks) {
            new Thread(track).start();
        }

        try { Thread.sleep(50); } catch (InterruptedException e) {}

        for (AudioTrack track : tracks) {
            track.play();
        }
        this.isPlaying = true;
    }

    public void pauseAll() {
        for (AudioTrack track : tracks) {
            track.pause();
        }
        this.isPlaying = false;
    }

    public void resumeAll() {
        for (AudioTrack track : tracks) {
            track.resume();
        }
        this.isPlaying = true;
    }

    public void seekAll(long microseconds) {
        for (AudioTrack track : tracks) {
            track.seek(microseconds);
        }
    }

    public void startProgressUpdater(Consumer<Long> progressConsumer) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (!tracks.isEmpty() && isPlaying) {
                    long currentPosition = tracks.get(0).getCurrentPosition();
                    progressConsumer.accept(currentPosition);
                }
            }
        };

        progressTimer.scheduleAtFixedRate(task, 0, 200);
    }

    public void stopAllTracks() {
        if (tracks == null) return;
        for (AudioTrack track : tracks) {
            track.stop();
        }
        this.isPlaying = false;
    }

    public long getSongLengthMicroseconds() {
        return songLengthMicroseconds;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public List<AudioTrack> getTracks() {
        return tracks;
    }
}