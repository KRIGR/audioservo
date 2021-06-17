package AudioServo;

import android.util.Log;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

import java.nio.ByteBuffer;

public class AudioPPM_8ch {
    int sampleRate = 48000;
    int pulseRate = 50;
    int frameSize = sampleRate / pulseRate;
    int bufSize = frameSize * 2;

    ByteBuffer buildBuf;
    ByteBuffer playbackBuf;
    AudioTrack aTrack;
    final static byte HIGH = (byte)127;
    final static byte LOW = (byte) 0;

    public AudioPPM_8ch(){ //Constructor
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
            .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build();
        playbackBuf = ByteBuffer.allocateDirect(bufSize + 2);
        buildBuf = ByteBuffer.allocateDirect(bufSize + 2);

    } //End constructor


     public void WritePPM(int[] microsArray) {
         //Writes a PPM signal to the left channel(even indices), with a reset on the right channel(odd indices)
         //to set the external counter IC back to 0.

         int channelCount = microsArray.length;
         //Check array of values passed is correct size
         assert (channelCount < 8 & channelCount > 0);
         buildBuf.rewind();
         //Write a reset signal
         FrameReset(buildBuf);
         //For each provided channel:
         for (int currChannel = 0; currChannel < channelCount; currChannel++) {
             //Get number of frames to wait before switching output channel
             int ppmLen = MicrosToFrames(microsArray[currChannel])-1;
             //Set corresponding number of array values to 0
             for (int currFrame = 0; currFrame < ppmLen; currFrame++) {
                 FrameBlank(buildBuf);
             }
             //Write a high val to the left channel
             FrameClock(buildBuf);
         }
         //Set remaining buffer values to 0
         while (buildBuf.remaining() > 0){
             PutFrame(buildBuf, LOW, LOW);
         }
         buildBuf.rewind();
         aTrack.write(buildBuf, bufSize, AudioTrack.WRITE_BLOCKING);

     }

    public void EnableOutput(){
        aTrack.setLoopPoints(0, frameSize, -1);
        Log.e("BufferVal", "Enabling output");
        aTrack.play();
        buildBuf.rewind();
        for (int i = 0; i < frameSize; i++){
            int leftVal = buildBuf.get();
            int rightVal = buildBuf.get();
            if (leftVal > 0 |  rightVal > 0) {
                Log.i("BufferVal", String.valueOf(leftVal) + ", " + String.valueOf(rightVal));
            }
        }
    }

    public void DisableOutput() {
        aTrack.pause();
        aTrack.stop();
    }

    int MicrosToFrames(int micros){
        return (int)Math.round(micros * sampleRate / 1e6);
    }

    private void PutFrame(ByteBuffer buf, byte LeftVal, byte RightVal){
        buf.put(LeftVal);
        buf.put(RightVal);
    }

    private void FrameClock(ByteBuffer buf){
        PutFrame(buf,HIGH, LOW);
    }
    private void FrameReset(ByteBuffer buf){
        PutFrame(buf,LOW, HIGH);
    }
    private void FrameBlank(ByteBuffer buf){
        PutFrame(buf,LOW, LOW);
    }
}
