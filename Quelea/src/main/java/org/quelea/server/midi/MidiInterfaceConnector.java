/*
 * This file is part of Quelea, free projection software for churches.
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.server.midi;

import javax.sound.midi.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.quelea.server.RCHandler;
import org.quelea.services.utils.LoggerUtils;
import org.quelea.services.utils.QueleaProperties;


import java.util.List;


/**
 * The MIDI  control server, responsible for handling the midi  calls and
 * changing the correct content12.
 * <p>
 *
 * @author Ben
 */
public class MidiInterfaceConnector
{
    // Get logger
    private static final Logger LOGGER = LoggerUtils.getLogger();

    // State variables
    private static boolean midiInputReady = false;// Flag to indicate the input is ready
    private static boolean midiOutputReady = false;// Flag to indicate the output is ready
    private boolean RemoteControlDeferToMidiMode = false;
    //MIDI input and output devices
    private MidiDevice QueleaMidiDev_IN = null;
    private MidiDevice QueleaMidiDev_OUT = null;
    private Receiver OutputExternalReceiver = null;
    // Midi event map
    private Map<String, MidiEvent> midiEventMap = new HashMap<>();

    /**
     * Create a MIDI interface module.
     */
    public MidiInterfaceConnector() throws InvalidMidiDataException, MidiUnavailableException {
        LOGGER.log(Level.INFO, "Setup midi module with MIDI event list.");
        createMidiEventList();
    }

    // Midi Event list initialization
    public void createMidiEventList() {
        // Note: the list generation could be automated. It is also fine to leave it explicitly like this.
        List<MidiEvent> midiEventList = new ArrayList<>();
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_Clear           (), "clear"          ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_Black           (), "black"          ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_GoToItem        (), "goToItem"       ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_Next            (), "next"           ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_NextItem        (), "nextItem"       ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_Play            (), "play"           ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_Prev            (), "prev"           ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_PrevItem        (), "prevItem"       ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_Section         (), "section"        ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_Tlogo			  (), "logo"		   ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeDown1  (), "transposeDown1" ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeDown2  (), "transposeDown2" ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeDown3  (), "transposeDown3" ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeDown4  (), "transposeDown4" ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeDown5  (), "transposeDown5" ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeDown6  (), "transposeDown6" ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeUp0	  (), "transposeUp0"   ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeUp1	  (), "transposeUp1"   ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeUp2	  (), "transposeUp2"   ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeUp3	  (), "transposeUp3"   ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeUp4	  (), "transposeUp4"   ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeUp5	  (), "transposeUp5"   ));
        midiEventList.add(new MidiEvent( QueleaProperties.get().getMidiAction_TransposeUp6	  (), "transposeUp6"   ));

        for (MidiEvent me : midiEventList)
        {
            midiEventMap.put(me.callbackName,me);
        }
    }
    // Method to get the midiEventMap
    public Map<String, MidiEvent> getMidiEventMap() {
        return midiEventMap;  // Returning the original map
    }

    // Device management methods
    public void setupMidiInputConnection(String InputDevice) throws MidiUnavailableException {

        LOGGER.log(Level.INFO, "Setup midi connection with [{0}]",InputDevice);
        // Before anything we have to make sure device are closed
        closeInputDevice();

        LOGGER.log(Level.INFO, "Get midi device list");
        // Get midi information
        MidiDevice.Info[] midiInfo = MidiSystem.getMidiDeviceInfo();
        // Midi device placeholder
        MidiDevice device;
        // Loop over the midi devices
        for (int d = 0; d < midiInfo.length; d++)
        {
            try {
                // Get the midi device
                device = MidiSystem.getMidiDevice(midiInfo[d]);

                //Get list of receivers for this device
                List<Receiver> deviceReceivers = device.getReceivers();

                //Get list of transmitters for this device
                List<Transmitter> deviceTransmitters = device.getTransmitters();

                // Get the correct midi INPUT device
                if (InputDevice.equals(midiInfo[d].toString()) && device.getMaxTransmitters() != 0)  {
                    LOGGER.log(Level.INFO, "Quelea connector MIDI device INPUT Located [" + midiInfo[d] + "]");
                    QueleaMidiDev_IN = device;

                    //Info for the midi device
                    LOGGER.log(Level.INFO, "Deivce[" + String.valueOf(d) + "] [" + midiInfo[d] + "]"+"\n"+
                            "Transmitter list size for the device is ["+ deviceTransmitters.size()+"]"+
                            "  max transmitters: " + device.getMaxTransmitters()+"\n"+
                            "Receiver list size for the device is ["+ deviceReceivers.size()+"]"+
                            "  max receivers: " + device.getMaxReceivers());
                    break;
                }

            } catch (MidiUnavailableException e) {   // if anything goes wrong disable midi control
                QueleaProperties.get().setUseMidiControl(false);
                LOGGER.log(Level.WARNING, "MIDI device listing failed! Midi control disabled!");
                return;
            }
        }

        // If the device is located
        if (QueleaMidiDev_IN == null)
        {
            LOGGER.log(Level.INFO, "Quelea connector MIDI INPUT device NOT located!");
            return;
        }
        else
        {
            // If the device is located
            if (!(QueleaMidiDev_IN.isOpen()))
            {
                try
                {
                    QueleaMidiDev_IN.open();
                    LOGGER.log(Level.INFO, "MIDI INPUT device successfully opened.");
                }
                catch (MidiUnavailableException e)
                {   // if anything goes wrong disable midi control
                    QueleaProperties.get().setUseMidiControl(false);
                    LOGGER.log(Level.WARNING, "MIDI INPUT device couldn't open! Midi control disabled!");
                    return;
                }
            }


            //Get a transmitters for this device
            Transmitter externalTransmitter = QueleaMidiDev_IN.getTransmitter();
            LOGGER.log(Level.INFO, "External transmitter ["+ externalTransmitter.toString()+"]");
            externalTransmitter.setReceiver( new QueleaInputMidiReceiver(QueleaMidiDev_IN.getDeviceInfo().toString()) );
        }
        midiInputReady = true;
    }
    public void setupMidiOutputConnection(String OutputDevice) throws MidiUnavailableException {

        LOGGER.log(Level.INFO, "Setup midi connection with [{0}]",OutputDevice);
        // Before anything we have to make sure device are closed
        closeOutputDevice();

        LOGGER.log(Level.INFO, "Get midi device list");
        // Get midi information
        MidiDevice.Info[] midiInfo = MidiSystem.getMidiDeviceInfo();
        // Midi device placeholder
        MidiDevice device;
        // Loop over the midi devices
        for (int d = 0; d < midiInfo.length; d++)
        {
            try {
                // Get the midi device
                device = MidiSystem.getMidiDevice(midiInfo[d]);

                //Get list of receivers for this device
                List<Receiver> deviceReceivers = device.getReceivers();

                //Get list of transmitters for this device
                List<Transmitter> deviceTransmitters = device.getTransmitters();


                // Get the correct midi OUTPUT device
                if (OutputDevice.equals(midiInfo[d].toString()) && device.getMaxReceivers() != 0)  {
                    LOGGER.log(Level.INFO, "Quelea connector MIDI device OUTPUT Located [" + midiInfo[d] + "]");
                    QueleaMidiDev_OUT = device;

                    //Info for the midi device
                    LOGGER.log(Level.INFO, "Deivce[" + String.valueOf(d) + "] [" + midiInfo[d] + "]"+"\n"+
                            "Transmitter list size for the device is ["+ deviceTransmitters.size()+"]"+
                            "  max transmitters: " + device.getMaxTransmitters()+"\n"+
                            "Receiver list size for the device is ["+ deviceReceivers.size()+"]"+
                            "  max receivers: " + device.getMaxReceivers());
                    break;
                }

            } catch (MidiUnavailableException e) {   // if anything goes wrong disable midi control
                QueleaProperties.get().setUseMidiControl(false);
                LOGGER.log(Level.WARNING, "MIDI OUTPUT device couldn't open! Midi control disabled");
                return;
            }
        }

        // If the device is located
        if (QueleaMidiDev_OUT == null)
        {
            LOGGER.log(Level.INFO, "Quelea connector MIDI OUTPUT device NOT located!");
            return;
        }
        else
        {
            // If the device is located
            if (!(QueleaMidiDev_OUT.isOpen()))
            {
                try
                {
                    QueleaMidiDev_OUT.open();
                    LOGGER.log(Level.INFO, "MIDI OUTPUT device successfully opened.");
                }
                catch (MidiUnavailableException e)
                {   // if anything goes wrong disable midi control
                    QueleaProperties.get().setUseMidiControl(false);
                    LOGGER.log(Level.WARNING, "MIDI OUTPUT device listing failed! Midi control disabled!");
                    return;
                }
            }


            // !!! Here is a bit different. DO WE WANT TO CONNECT TO ONE RECIEVER OR MULTIPLE ONES?
            //Get a receiver for this device
            OutputExternalReceiver = QueleaMidiDev_OUT.getReceiver();;
            LOGGER.log(Level.INFO, "External transmitter ["+ OutputExternalReceiver.toString()+"]");

        }
        midiOutputReady = true;
    }
    public void closeInputDevice(){
        // If the device is located
        if (QueleaMidiDev_IN != null && QueleaMidiDev_IN.isOpen())
        {
            QueleaMidiDev_IN.close();
            LOGGER.log(Level.INFO, "INPUT MIDI device successfully is now closed.");
        }
        QueleaMidiDev_IN = null;//Reset the device
        midiInputReady = false;
    }
    public void closeOutputDevice(){
        // If the device is located
        if (QueleaMidiDev_OUT != null && QueleaMidiDev_OUT.isOpen())
        {
            QueleaMidiDev_OUT.close();
            LOGGER.log(Level.INFO, "OUTPUT MIDI device successfully is now closed.");
        }
        QueleaMidiDev_OUT = null;//Reset the device
        OutputExternalReceiver= null;// Also reset the receiver
        midiOutputReady = false;
    }

    // Event management
    public void sendMidiEvenMsg(String midiEventKey,int velocity) throws InvalidMidiDataException {
        if (OutputExternalReceiver != null)
        {// -1 means ASAP
            OutputExternalReceiver.send( midiEventMap.get(midiEventKey).toMidiMessage(velocity), -1);
        }
        // Call logging function
    }


    // Utility methods
    public boolean getInputReadyState() {
        return midiInputReady;
    }
    public boolean getOutputReadyState() {
        return midiOutputReady;
    }

    @Override
    protected void finalize() throws Throwable {
        //super.finalize();
        // If the device is located
        closeInputDevice();
        closeOutputDevice();
    }


    // This class is a bit redundant at the moment, but it could help set Quelea as an output device.
    // Similar thing could be done for the input.
    public class QueleaOutputMidiTransmitter implements Transmitter {

        @Override
        public void setReceiver(Receiver receiver) {

        }

        @Override
        public Receiver getReceiver() {
            return null;
        }

        @Override
        public void close() {

        }
    }

    public class QueleaInputMidiReceiver implements Receiver {
        private String name;
        public QueleaInputMidiReceiver(String toString) {
            this.name = name;
        }

        @Override
        public void send(MidiMessage message, long timeStamp)
        {
            byte[] aMsg = message.getMessage();
            int velocity = (int)aMsg[2];
            // take the MidiMessage msg and store it in a byte array
            LOGGER.log(Level.INFO, "Midi message received ["+aMsg.toString()+"]");
            try {
                if (false){}
                else if (midiEventMap.get("clear"          ).match(message)) { RCHandler.clear(); }
                else if (midiEventMap.get("black"          ).match(message)) { RCHandler.black(); }
                else if (midiEventMap.get("goToItem"       ).match(message)) { RCHandler.gotoItem( "gotoitem"+velocity  ); }
                else if (midiEventMap.get("next"           ).match(message)) { RCHandler.next() ; }
                else if (midiEventMap.get("nextItem"       ).match(message)) { RCHandler.nextItem(); }
                else if (midiEventMap.get("play"           ).match(message)) { RCHandler.play(); }
                else if (midiEventMap.get("prev"           ).match(message)) { RCHandler.prev(); }
                else if (midiEventMap.get("prevItem"       ).match(message)) { RCHandler.prevItem(); }
                else if (midiEventMap.get("section"        ).match(message)) { RCHandler.setLyrics("section"+velocity); }
                else if (midiEventMap.get("logo"		   	  ).match(message)) { RCHandler.logo(); }
                else if (midiEventMap.get("transposeDown1" ).match(message)) { RCHandler.transposeSong(1); }
                else if (midiEventMap.get("transposeDown2" ).match(message)) { RCHandler.transposeSong(2); }
                else if (midiEventMap.get("transposeDown3" ).match(message)) { RCHandler.transposeSong(3); }
                else if (midiEventMap.get("transposeDown4" ).match(message)) { RCHandler.transposeSong(4); }
                else if (midiEventMap.get("transposeDown5" ).match(message)) { RCHandler.transposeSong(5); }
                else if (midiEventMap.get("transposeDown6" ).match(message)) { RCHandler.transposeSong(6); }
                else if (midiEventMap.get("transposeUp0"   ).match(message)) { RCHandler.transposeSong(0); }
                else if (midiEventMap.get("transposeUp1"   ).match(message)) { RCHandler.transposeSong(-1); }
                else if (midiEventMap.get("transposeUp2"   ).match(message)) { RCHandler.transposeSong(-2); }
                else if (midiEventMap.get("transposeUp3"   ).match(message)) { RCHandler.transposeSong(-3); }
                else if (midiEventMap.get("transposeUp4"   ).match(message)) { RCHandler.transposeSong(-4); }
                else if (midiEventMap.get("transposeUp5"   ).match(message)) { RCHandler.transposeSong(-5); }
                else if (midiEventMap.get("transposeUp6"   ).match(message)) { RCHandler.transposeSong(-6); }

            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void close() {

        }
    }

    public void testTone() throws InvalidMidiDataException, MidiUnavailableException {
        //------------------------- FOR DEBUG
        ShortMessage myMsg = new ShortMessage();
        // Start playing the note Middle C (60),
        // moderately loud (velocity = 93).
        myMsg.setMessage(ShortMessage.NOTE_ON, 0, 60, 93);
        long timeStamp = -1;
        Receiver       rcvr = MidiSystem.getReceiver();
        rcvr.send(myMsg, timeStamp);

        //------------------------------- FOR DEBUG


    }
}