package com.android.mediaserver.voip;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import com.android.mediaserver.encoder.AmrInputStream;
import com.android.mediaserver.musicg.dsp.Resampler;
import io.kvh.media.amr.AmrEncoder;
import io.kvh.media.amr.AmrEncoder.Mode;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import timber.log.Timber;

public class AudioEncoder {
  public static final int BUFFER_SIZE = 88200;
  public static final int CODEC_TIMEOUT_IN_MS = 5000;
  public static final int COMPRESSED_AUDIO_FILE_BIT_RATE = 128000;
  public static final String COMPRESSED_AUDIO_FILE_MIME_TYPE = "audio/mp4a-latm";
  public static final int SAMPLING_RATE = 44100;
  private static final int CHUNK_SIZE = 640;

  public boolean encodeToAmr(String inFilePath, String outFilePath, int sampleRate) {
    try {
      Timber.i("(encodeToAmr): Encoding raw to: %s", outFilePath);
      File outFile = new File(outFilePath);
      outFile.createNewFile();
      OutputStream fos = new FileOutputStream(outFile);
      FileInputStream fis = new FileInputStream(new File(inFilePath));
      fos.write(35);
      fos.write(33);
      fos.write(65);
      fos.write(77);
      fos.write(82);
      fos.write(10);
      Resampler resampler = new Resampler();
      byte[] inData = new byte[CHUNK_SIZE];
      byte[] outData = new byte[CHUNK_SIZE];
      while (fis.read(inData) > 0) {
        ByteArrayInputStream byteArrayInputStream =
            new ByteArrayInputStream(resampler.reSample(inData, 16, sampleRate, 8000));
        AmrInputStream amrStream = new AmrInputStream(byteArrayInputStream);
        while (true) {
          int len = amrStream.read(outData);
          if (len <= 0) {
            break;
          }
          fos.write(outData, 0, len);
        }
        amrStream.close();
        byteArrayInputStream.close();
      }
      fis.close();
      fos.close();
      return true;
    } catch (Exception e) {
      Timber.e(e, "Failed to encode", new Object[0]);
      return false;
    }
  }

  public boolean mixAndEncodeToAmr(String inChannelFilePath, String outChannelFilePath,
      String destAmrFilePath, int sampleRate) {
    try {
      Timber.i("(encodeToAmr): Encoding raw to: %s", destAmrFilePath);
      File file = new File(destAmrFilePath);
      file.createNewFile();
      OutputStream fos = new FileOutputStream(file);
      FileInputStream inChannelFis = new FileInputStream(new File(inChannelFilePath));
      FileInputStream fileInputStream = new FileInputStream(new File(outChannelFilePath));
      fos.write(35);
      fos.write(33);
      fos.write(65);
      fos.write(77);
      fos.write(82);
      fos.write(10);
      Resampler resampler = new Resampler();
      byte[] inChannelData = new byte[CHUNK_SIZE];
      byte[] outChannelData = new byte[CHUNK_SIZE];
      byte[] dstData = new byte[CHUNK_SIZE];
      AmrEncoder.init(0);
      int mode = Mode.MR122.ordinal();
      while (true) {
        int inLen = inChannelFis.read(inChannelData);
        if (inLen != -1) {
          int outLen = fileInputStream.read(outChannelData);
          if (outLen != -1) {
            short[] resampledDataShort =
                resampler.mixAndResampleShort(inChannelData, inLen, outChannelData, outLen, 16,
                    sampleRate, 8000);
            if (160 == resampledDataShort.length) {
              int len = AmrEncoder.encode(mode, resampledDataShort, dstData);
              if (32 == len) {
                fos.write(dstData, 0, len);
              }
            }
          }
        }
        break;
      }
      inChannelFis.close();
      fileInputStream.close();
      fos.close();
      return true;
    } catch (Exception e) {
      Timber.e(e, "Failed to encode", new Object[0]);
      return false;
    }
  }

  @TargetApi(18) public void encodeToMP4(String inChannelFilePath, String outChannelFilePath,
      String destAmrFilePath) {
    try {
      FileInputStream fis = new FileInputStream(new File(inChannelFilePath));
      File file = new File(destAmrFilePath);
      if (file.exists()) {
        file.delete();
      }
      MediaMuxer mediaMuxer = new MediaMuxer(file.getAbsolutePath(), 0);
      MediaFormat outputFormat =
          MediaFormat.createAudioFormat(COMPRESSED_AUDIO_FILE_MIME_TYPE, SAMPLING_RATE, 1);
      outputFormat.setInteger("aac-profile", 2);
      outputFormat.setInteger("bitrate", COMPRESSED_AUDIO_FILE_BIT_RATE);
      MediaCodec codec = MediaCodec.createEncoderByType(COMPRESSED_AUDIO_FILE_MIME_TYPE);
      codec.configure(outputFormat, null, null, 1);
      codec.start();
      ByteBuffer[] codecInputBuffers = codec.getInputBuffers();
      ByteBuffer[] codecOutputBuffers = codec.getOutputBuffers();
      BufferInfo outBuffInfo = new BufferInfo();
      byte[] tempBuffer = new byte[BUFFER_SIZE];
      boolean hasMoreData = true;
      double presentationTimeUs = 0.0d;
      int audioTrackIdx = 0;
      int totalBytesRead = 0;
      do {
        int inputBufIndex = 0;
        while (inputBufIndex != -1 && hasMoreData) {
          inputBufIndex = codec.dequeueInputBuffer(5000);
          if (inputBufIndex >= 0) {
            ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
            dstBuf.clear();
            int bytesRead = fis.read(tempBuffer, 0, dstBuf.limit());
            if (bytesRead == -1) {
              hasMoreData = false;
              codec.queueInputBuffer(inputBufIndex, 0, 0, (long) presentationTimeUs, 4);
            } else {
              totalBytesRead += bytesRead;
              dstBuf.put(tempBuffer, 0, bytesRead);
              codec.queueInputBuffer(inputBufIndex, 0, bytesRead, (long) presentationTimeUs, 0);
              presentationTimeUs = (double) ((1000000 * ((long) (totalBytesRead / 2))) / 44100);
            }
          }
        }
        int outputBufIndex = 0;
        while (outputBufIndex != -1) {
          outputBufIndex = codec.dequeueOutputBuffer(outBuffInfo, 5000);
          if (outputBufIndex >= 0) {
            ByteBuffer encodedData = codecOutputBuffers[outputBufIndex];
            encodedData.position(outBuffInfo.offset);
            encodedData.limit(outBuffInfo.offset + outBuffInfo.size);
            if ((outBuffInfo.flags & 2) == 0 || outBuffInfo.size == 0) {
              mediaMuxer.writeSampleData(audioTrackIdx, codecOutputBuffers[outputBufIndex],
                  outBuffInfo);
              codec.releaseOutputBuffer(outputBufIndex, false);
            } else {
              codec.releaseOutputBuffer(outputBufIndex, false);
            }
          } else if (outputBufIndex == -2) {
            audioTrackIdx = mediaMuxer.addTrack(codec.getOutputFormat());
            mediaMuxer.start();
          } else if (outputBufIndex != -3 && outputBufIndex == -1) {
          }
        }
      } while (outBuffInfo.flags != 4);
      fis.close();
      mediaMuxer.stop();
      mediaMuxer.release();
    } catch (Exception e) {
    }
  }
}
