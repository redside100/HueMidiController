import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;
import java.util.List;
public class MidiListener {

    private MidiDevice midiDevice;

    public MidiListener() {
        MidiDevice.Info[] midiInfo = MidiSystem.getMidiDeviceInfo();

        // Initialize all midi devices
        for (int i = 0; i < midiInfo.length; i++) {

            try {

                midiDevice = MidiSystem.getMidiDevice(midiInfo[i]);

                // Get the current transmitters, and add a new input receiver
                List<Transmitter> transmitters = midiDevice.getTransmitters();
                String deviceName = midiDevice.getDeviceInfo().toString();

                for(int j = 0; j < transmitters.size(); j++) {
                    transmitters.get(j).setReceiver(new MidiInputReceiver(deviceName));
                }

                Transmitter trans = midiDevice.getTransmitter();
                trans.setReceiver(new MidiInputReceiver(deviceName));

                midiDevice.open();
                System.out.println(midiDevice.getDeviceInfo() + " connection opened!");


            } catch (MidiUnavailableException e) {}
        }


    }
    public void close(){
        midiDevice.close();
    }
}