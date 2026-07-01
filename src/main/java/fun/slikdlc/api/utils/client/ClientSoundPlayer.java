package fun.slikdlc.api.utils.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineEvent.Type;
import lombok.Generated;

public final class ClientSoundPlayer {
   private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
      Thread thread = new Thread(r, "SlikDlc-ClientSounds");
      thread.setDaemon(true);
      return thread;
   });

   public static void playSound(String fileName, double volume, float pitch) {
      EXECUTOR.execute(() -> playInternal(fileName, volume, pitch));
   }

   private static void playInternal(String fileName, double volume, float pitch) {
      String resourcePath = "/assets/slikdlc/sounds/" + fileName;

      try {
         try (InputStream inputStream = ClientSoundPlayer.class.getResourceAsStream(resourcePath)) {
            if (inputStream != null) {
               try (
                  BufferedInputStream bufferedIn = new BufferedInputStream(inputStream);
                  AudioInputStream baseStream = AudioSystem.getAudioInputStream(bufferedIn);
                  AudioInputStream pitchedStream = resampleStream(baseStream, pitch);
               ) {
                  Clip clip = AudioSystem.getClip();
                  clip.addLineListener(event -> {
                     if (event.getType() == Type.STOP) {
                        clip.close();
                     }
                  });
                  clip.open(pitchedStream);
                  setVolume(clip, volume);
                  clip.start();
                  return;
               }
            }
         }
      } catch (IOException | LineUnavailableException | UnsupportedAudioFileException var19) {
      }
   }

   private static AudioInputStream resampleStream(AudioInputStream originalStream, float pitch) throws IOException {
      AudioFormat originalFormat = originalStream.getFormat();
      byte[] audioBytes = originalStream.readAllBytes();
      float newSampleRate = originalFormat.getSampleRate() * Math.max(0.5F, Math.min(2.0F, pitch));
      AudioFormat newFormat = new AudioFormat(
         newSampleRate, originalFormat.getSampleSizeInBits(), originalFormat.getChannels(), true, originalFormat.isBigEndian()
      );
      return new AudioInputStream(new ByteArrayInputStream(audioBytes), newFormat, audioBytes.length / newFormat.getFrameSize());
   }

   private static void setVolume(Clip clip, double volume) {
      if (clip.isControlSupported(javax.sound.sampled.FloatControl.Type.MASTER_GAIN)) {
         double clampedVolume = Math.max(0.0, Math.min(1.0, volume));
         FloatControl volumeControl = (FloatControl)clip.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN);
         float dB = (float)(Math.log10(clampedVolume <= 0.0 ? 1.0E-4 : clampedVolume) * 20.0);
         volumeControl.setValue(dB);
      }
   }

   @Generated
   private ClientSoundPlayer() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
