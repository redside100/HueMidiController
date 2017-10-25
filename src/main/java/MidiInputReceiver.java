import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

public class MidiInputReceiver implements Receiver {
    public String name;
    public MidiInputReceiver(String name) {
        this.name = name;
    }
    public void send(MidiMessage msg, long time) {
        if (msg instanceof ShortMessage){
            HueMidiController.midiEvent((ShortMessage) msg, time);
        }
    }
    public void close() {}
}