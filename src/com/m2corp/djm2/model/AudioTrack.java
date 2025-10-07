package com.m2corp.djm2.model;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioTrack implements Runnable {

    private final String trackName;
    private Clip audioClip;
    private BooleanControl muteControl;
    private FloatControl gainControl;
    private volatile boolean isMuted = false;
    private volatile boolean isRunning = true;

    public AudioTrack(String name, File audioFile) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        this.trackName = name;
        loadAudio(audioFile);
    }

    private void loadAudio(File audioFile) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
        audioClip = AudioSystem.getClip();
        audioClip.open(audioStream);

        muteControl = (BooleanControl) audioClip.getControl(BooleanControl.Type.MUTE);
        gainControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        cleanup();
        System.out.println("Thread for [" + trackName + "] has finished.");
    }

    public void play() {
        if (audioClip != null) {
            audioClip.setMicrosecondPosition(0);
            audioClip.start();
        }
    }

    public void pause() {
        if (audioClip != null && audioClip.isRunning()) {
            audioClip.stop();
        }
    }

    public void resume() {
        if (audioClip != null && !audioClip.isRunning()) {
            audioClip.start();
        }
    }

    public void stop() {
        this.isRunning = false;
    }

    private void cleanup() {
        if (audioClip != null) {
            audioClip.stop();
            audioClip.close();
        }
    }

    public void setVolume(int percentage) {
        if (gainControl == null) return;
        float min = gainControl.getMinimum();
        float max = gainControl.getMaximum();
        float range = max - min;
        float gain = (range * (percentage / 100.0f)) + min;
        gainControl.setValue(gain);
    }


    public String getName() { return this.trackName; }
    public boolean isMuted() { return this.isMuted; }
    public long getMicrosecondLength() { return audioClip != null ? audioClip.getMicrosecondLength() : 0; }
    public long getCurrentPosition() { return audioClip != null ? audioClip.getMicrosecondPosition() : 0; }
    public void seek(long microseconds) { if (audioClip != null) audioClip.setMicrosecondPosition(microseconds); }


    public void toggleMute() {
        if (isMuted()) unmute(); else mute();
    }
    public void mute() {
        if (muteControl != null) {
            muteControl.setValue(true);
            this.isMuted = true;
        }
    }
    public void unmute() {
        if (muteControl != null) {
            muteControl.setValue(false);
            this.isMuted = false;
        }
    }
}