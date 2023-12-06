package eu.happycoders.ffm;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SequenceLayout;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

  public class FFMTestMultipleArrays {
    private static final int ARRAY_LENGTH = 8;
    private static final int NUMBER_OF_ARRAYS = 4;

    public static void main(String[] args) {
      SequenceLayout arrayLayout = MemoryLayout.sequenceLayout(ARRAY_LENGTH, JAVA_INT);
      VarHandle arrayHandle = arrayLayout.varHandle(PathElement.sequenceElement());

      MemorySegment segment = Arena.global().allocate(arrayLayout, NUMBER_OF_ARRAYS);

      ThreadLocalRandom random = ThreadLocalRandom.current();
      for (int i = 0; i < NUMBER_OF_ARRAYS; i++) {
        long offset = i * arrayLayout.byteSize();
        for (int j = 0; j < ARRAY_LENGTH; j++) {
          arrayHandle.set(segment, offset, j, random.nextInt(0, 1000));
        }
      }

      segment
          .elements(arrayLayout)
          .forEach(
              arraySegment -> {
                int[] array = arraySegment.toArray(JAVA_INT);
                System.out.println(Arrays.toString(array));
              });
    }
  }
