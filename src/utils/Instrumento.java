package utils;

// Cole isto na sua classe Instrumento.java

import javax.sound.midi.*;

public class Instrumento implements Runnable {

    private final String nomeDaFaixa;
    private final Track track;
    private final MidiChannel channel;
    private final int resolution;
    private final int channelIndex; // Armazena o número do canal

    private volatile boolean executando = true;
    private volatile boolean tocando = true;
    private final long mpq = 500000; // Microseconds per quarter note (120 BPM)

    public Instrumento(String nome, Track track, MidiChannel channel, int resolution, int channelIndex) {
        this.nomeDaFaixa = nome;
        this.track = track;
        this.channel = channel;
        this.resolution = resolution;
        this.channelIndex = channelIndex; // Recebe o número do canal
    }

    // ... (os métodos getNome, pausar, retomar, etc. podem continuar os mesmos)

    @Override
    public void run() {
        System.out.println("[" + getNome() + "] Thread iniciada.");
        long ultimoTick = 0;

        try {
            for (int i = 0; i < track.size() && executando; i++) {
                MidiEvent event = track.get(i);

                // Lógica de tempo e pausa (não precisa mudar)
                long tickAtual = event.getTick();
                if (tickAtual > ultimoTick) {
                    long deltaTicks = tickAtual - ultimoTick;
                    long millisParaEsperar = (deltaTicks * mpq) / (resolution * 1000L);
                    Thread.sleep(millisParaEsperar);
                }
                synchronized (this) {
                    while (!tocando) {
                        wait();
                    }
                }
                if (!executando) break;

                // **CORREÇÃO #2: Lógica de processamento de mensagens**
                if (event.getMessage() instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) event.getMessage();

                    // A condição CORRETA: verifica se a mensagem é para o canal desta thread
                    if (sm.getChannel() == this.channelIndex) {
                        switch (sm.getCommand()) {
                            case ShortMessage.NOTE_ON:
                                if (sm.getData2() > 0) { // Velocidade > 0
                                    channel.noteOn(sm.getData1(), sm.getData2());
                                } else { // Velocidade 0 é igual a NOTE_OFF
                                    channel.noteOff(sm.getData1());
                                }
                                break;
                            case ShortMessage.NOTE_OFF:
                                channel.noteOff(sm.getData1(), sm.getData2());
                                break;
                            case ShortMessage.PROGRAM_CHANGE:
                                channel.programChange(sm.getData1());
                                break;
                        }
                    }
                }
                ultimoTick = tickAtual;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            channel.allNotesOff();
            System.out.println("[" + getNome() + "] Thread encerrada.");
        }
    }
    // Adicione os outros métodos aqui (getNome, pausar, retomar, etc.)
    public String getNome() { return this.nomeDaFaixa; }
    public boolean isTocando() { return this.tocando; }
    public void pausar() { tocando = false; channel.allNotesOff(); }
    public synchronized void retomar() { tocando = true; notify(); }
    public void alternarPausa() { if(isTocando()) pausar(); else retomar(); }
    public synchronized void parar() { executando = false; notify(); }
}