package AudioServo;


import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class AudioPPM{
    int sampleRate = 48000;
    int pulseRate = 50;
    int frameLength = sampleRate / pulseRate;
    int bufSize = frameLength * 2;

    // Byte values are cast to a signed 8-bit value
    //Zero V output
    final static byte VAL_OFF = (byte)0x80;
    //Approx 50% magnitude
    final static byte VAL_CLOCK = (byte)0xC0;
    //100% magnitude
    final static byte VAL_RESET = (byte)0xFE;

    ByteBuffer audioBuf;

    AudioTrack aTrack;

    public AudioPPM(){ //Constructor
        aTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_8BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build())
                .setBufferSizeInBytes(bufSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .build();
        audioBuf = ByteBuffer.allocateDirect(bufSize);
        WritePPM(new int[]{72, 72, 72, 72, 72, 72});

    } //End constructor

    public void WritePPM(int[] samplenumArray){
        //Writes a PPM signal to the left outputs(even indices) and right outputs(odd indices)
        //to set the external counter IC back to 0.
        int channelCount = samplenumArray.length;
        assert(channelCount <= 18 & channelCount > 0);
        Arrays.fill(audioBuf.array(), VAL_OFF);
        audioBuf.rewind();

        //Set even array values according to even channel numbers
        audioBuf.put(VAL_RESET);
        audioBuf.put(VAL_RESET);
        int bufPos = 2;

        for (int evenChannelNum = 0; evenChannelNum < channelCount; evenChannelNum+=2){
            bufPos += samplenumArray[evenChannelNum] * 2;
            Log.e("Even channel", "Set channel " + String.valueOf(evenChannelNum) + " at " + String.valueOf(bufPos/2 - 1));
            audioBuf.put(bufPos, VAL_CLOCK);
        }
        //Set odd array values according to odd channel numbers
        bufPos = 3;
        for (int oddChannelNum = 1; oddChannelNum < channelCount; oddChannelNum+=2){
            bufPos += samplenumArray[oddChannelNum] * 2;
            Log.e("Odd channel", "Set channel " + String.valueOf(oddChannelNum) + " at " + String.valueOf(bufPos/2 - 1));

            audioBuf.put(bufPos, VAL_CLOCK);
        }
        audioBuf.rewind();
        aTrack.setLoopPoints(0, frameLength-1, -1);
        aTrack.write(audioBuf, bufSize,AudioTrack.WRITE_BLOCKING);
    }

    public void EnableOutput(){
        aTrack.play();
    }

    public void DisableOutput() {
        aTrack.pause();
        aTrack.setPlaybackHeadPosition(0);
    }

}