package eu.happycoders.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.util.concurrent.ThreadLocalRandom;

public class FFMTestArrayOfStructs {
  private static final int ARRAY_LENGTH = 8;

  public static void main(String[] args) {
    StructLayout dateLayout =
        MemoryLayout.structLayout(
            ValueLayout.JAVA_SHORT.withName("year"),
            ValueLayout.JAVA_SHORT.withName("month"),
            ValueLayout.JAVA_SHORT.withName("day"));

    SequenceLayout positionArrayLayout = MemoryLayout.sequenceLayout(ARRAY_LENGTH, dateLayout);

    MemorySegment segment = Arena.global().allocate(positionArrayLayout);
    writeToSegment(segment, positionArrayLayout);
    readFromSegment(segment, dateLayout);
  }

  private static void writeToSegment(MemorySegment segment, SequenceLayout positionArrayLayout) {
    VarHandle yearInArrayHandle =
        positionArrayLayout.varHandle(
            PathElement.sequenceElement(), PathElement.groupElement("year"));
    VarHandle monthInArrayHandle =
        positionArrayLayout.varHandle(
            PathElement.sequenceElement(), PathElement.groupElement("month"));
    VarHandle dayInArrayHandle =
        positionArrayLayout.varHandle(
            PathElement.sequenceElement(), PathElement.groupElement("day"));

    ThreadLocalRandom random = ThreadLocalRandom.current();
    for (int i = 0; i < ARRAY_LENGTH; i++) {
      yearInArrayHandle.set(segment, 0, i, (short) random.nextInt(1900, 2100));
      monthInArrayHandle.set(segment, 0, i, (short) random.nextInt(1, 13));
      dayInArrayHandle.set(segment, 0, i, (short) random.nextInt(1, 31));
    }
  }

  private static void readFromSegment(MemorySegment segment, StructLayout dateLayout) {
    VarHandle yearHandle = dateLayout.varHandle(PathElement.groupElement("year"));
    VarHandle monthHandle = dateLayout.varHandle(PathElement.groupElement("month"));
    VarHandle dayHandle = dateLayout.varHandle(PathElement.groupElement("day"));

    segment
        .elements(dateLayout)
        .forEach(
            positionSegment -> {
              int year = (int) yearHandle.get(positionSegment, 0);
              int month = (int) monthHandle.get(positionSegment, 0);
              int day = (int) dayHandle.get(positionSegment, 0);
              System.out.printf("%04d-%02d-%02d\n", year, month, day);
            });
  }
}
