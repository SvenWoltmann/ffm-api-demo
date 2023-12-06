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

public class FFMTestArrayOfArrays {
  private static final int INNER_ARRAY_LENGTH = 8;
  private static final int OUTER_ARRAY_LENGTH = 4;

  public static void main(String[] args) throws Throwable {
    SequenceLayout innerArrayLayout = MemoryLayout.sequenceLayout(INNER_ARRAY_LENGTH, JAVA_INT);
    SequenceLayout outerArrayLayout =
        MemoryLayout.sequenceLayout(OUTER_ARRAY_LENGTH, innerArrayLayout);
    VarHandle elementHandle =
        outerArrayLayout.varHandle(PathElement.sequenceElement(), PathElement.sequenceElement());

    MemorySegment segment = Arena.global().allocate(outerArrayLayout);

    for (int i = 0; i < OUTER_ARRAY_LENGTH; i++) {
      for (int j = 0; j < INNER_ARRAY_LENGTH; j++) {
        elementHandle.set(segment, 0, i, j, ThreadLocalRandom.current().nextInt(0, 1000));
      }
    }

    segment
        .elements(innerArrayLayout)
        .forEach(
            arraySegment -> {
              int[] array = arraySegment.toArray(JAVA_INT);
              System.out.println(Arrays.toString(array));
            });
  }
}
