import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.sound.midi.ShortMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class HueMidiController {

    // Assigned hue ID as the key, and the bridge IP
    public static String key = "JO-e8UGLxzaTcb-5YeH2ZbhHbQecKY1VKe3OH9C1";
    public static String bridge = "192.168.1.100";
    public static String baseUrl = "http://" + bridge + "/api/" + key + "/lights/";

    // For some reason, the IDs for my lights are 1, 4, and 5
    public static int[] lights = {1, 4, 5};


    private static HashMap<Integer, Long> activeKeys = new HashMap<>();
    private static ArrayList<Integer> lightsOn = new ArrayList<>();
    private static ArrayList<Integer> lightsOff = new ArrayList<>();
    private static ArrayList<Integer> toggleTrigger = new ArrayList<>();
    private static boolean toggle = false;
    private static int lastHue = -1;

    public static void main(String[] args){
        new MidiListener();
        /* Add chords for lights on, off, and switching modes.
         * These are just examples with a keyboard with 88 keys
         */

        // C3, E3, G3 (C major triad)
        Collections.addAll(lightsOn, 60, 64, 67);
        // A3, C3, E3 (A minor triad)
        Collections.addAll(lightsOff, 57, 60, 64);
        // Gb3, Ab3, Bb4, Db4, Eb4, Gb4, Ab4, Bb4 (Gb major add2 add6 thingy)
        Collections.addAll(toggleTrigger, 66, 68, 70, 73, 75, 78, 80, 82);
    }

    // Basic HTTP PUT request using apache HTTP client
    public static void put(String url, String body) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("Content-type", "application/json");
        try {
            StringEntity stringEntity = new StringEntity(body);
            httpPut.getRequestLine();
            httpPut.setEntity(stringEntity);
            httpClient.execute(httpPut);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void midiEvent(ShortMessage msg, long time){
        // Only check for when a key is played (not released), the command ID should be 144
        if (msg.getCommand() == 144){
            System.out.println("[MIDI] Received note " + msg.getData1() + " at " + time + " ms");

            // Register key in hash map
            activeKeys.put(msg.getData1(), time);
            checkTriggers();

            // If in second mode, change the color of the lights according to the key
            if (toggle){
                spectrum(msg.getData1());
            }
        }
    }

    private static void spectrum(int key){
        /* This is assuming that the max hue value we want is 52530, and there are 88 keys
         * Since the first key id is 21, 21 should be subtracted from the factor
         */
        int hue = ((52530 / 88) * (key - 21));

        // Check for repeating notes to prevent some useless calls
        if (lastHue != hue){
            setColor(hue);
            lastHue = hue;
        }
    }

    private static void checkTriggers(){
        if (!toggle){
            if (checkChord(lightsOn)){
                System.out.println("[TRIGGER] Opening");
                openLights();
            } else if (checkChord(lightsOff)){
                System.out.println("[TRIGGER] Closing");
                closeLights();
            }
        }
        if (checkChord(toggleTrigger)){
            System.out.println("[TRIGGER] Switching modes");
            toggle = !toggle;

            if (!toggle){
                reset();
            }
        }
    }
    private static boolean checkChord(ArrayList<Integer> chord){

        /* Time difference: used to check if it truly is a chord
         * The notes should be no more than 20000 units apart
         * (I have no idea what the units are)
         */

        long prevTimeDiff = 0;

        // Literally loop through each note in the specified chord
        for (int i = 0; i < chord.size(); i++){
            // And check if the active keys contain it
            if (activeKeys.containsKey(chord.get(i))){
                if (i != 0){
                    // Calc time diff
                    if (activeKeys.containsKey(chord.get(i - 1))){
                        prevTimeDiff = Math.abs(activeKeys.get(chord.get(i - 1)) - activeKeys.get(chord.get(i)));
                    }
                    if (prevTimeDiff > 20000){
                        break;
                    }

                    // In the case it's the last note checked, the chord must be complete
                    if (i == chord.size() - 1){
                        for (int remove : chord){
                            activeKeys.remove(remove);
                        }
                        return true;
                    }
                }
            }else{
                break;
            }
        }
        return false;
    }

    /* Rest of this stuff is pretty spaghetti, but whatever
     * Just API calls to do stuff with the lights
     */
    private static void openLights(){
        for (int lightId : lights){
            put(baseUrl + lightId + "/state", "{\"on\":true}");
        }
    }
    private static void closeLights(){
        for (int lightId : lights){
            put(baseUrl + lightId + "/state", "{\"on\":false}");
        }
    }
    private static void setColor(int hue){
        for (int lightId : lights){
            put(baseUrl + lightId + "/state", "{\"hue\":" + hue + ", \"sat\":255}");
        }
    }
    private static void reset(){
        for (int lightId : lights){
            put(baseUrl + lightId + "/state", "{\"hue\":33016, \"sat\":53}");
        }
    }
}
