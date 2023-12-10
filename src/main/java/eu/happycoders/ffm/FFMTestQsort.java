package eu.happycoders.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class FFMTestQsort {

  public static void main(String[] args) throws Throwable {
    // 1. Get a linker - the central element for accessing foreign functions
    Linker linker = Linker.nativeLinker();

    // 2. Get a lookup object for commonly used libraries
    SymbolLookup stdlib = linker.defaultLookup();

    // 3. Get the address of the "qsort" function in the C standard library
    MemorySegment qsortAddress = stdlib.find("qsort").orElseThrow();

    // 4. Define the input and output parameters of the "qsort" function:
    // - Pointer to the array to sort
    // - Number of elements in the array
    // - Size of each element in the array in bytes
    // - Address of the comparison function
    FunctionDescriptor qsortDescriptor =
        FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS);

    // 5. Get a method handle to the "qsort" function
    MethodHandle qsortHandle = linker.downcallHandle(qsortAddress, qsortDescriptor);

    // 6. Define the input and output parameters of the "compare" function:
    FunctionDescriptor compareDescriptor =
        FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT),
            ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT));

    // 7. Get a handle to the "compare" function
    MethodHandle compareHandle =
        MethodHandles.lookup()
            .findStatic(FFMTestQsort.class, "compare", compareDescriptor.toMethodType());

    // 8. Get a confined memory area (one that we can close explicitly)
    try (Arena offHeap = Arena.ofConfined()) {
      // 9. Allocate off-heap memory and store unsorted array in it
      int[] unsorted = createUnsortedArray();
      MemorySegment arrayAddress = offHeap.allocateFrom(ValueLayout.JAVA_INT, unsorted);

      // 10. Allocate off-head memory for an "upcall stub" to the Java comparison function
      MemorySegment compareAddress = linker.upcallStub(compareHandle, compareDescriptor, offHeap);

      // 11. Invoke the qsort function
      qsortHandle.invoke(
          arrayAddress, unsorted.length, ValueLayout.JAVA_INT.byteSize(), compareAddress);

      // 12. Read array from off-heap memory
      int[] sorted = arrayAddress.toArray(ValueLayout.JAVA_INT);
      System.out.println("sorted   = " + Arrays.toString(sorted));
    }
    // 13. Off-heap memory is deallocated at end of try-with-resources
  }

  private static int compare(MemorySegment aAddr, MemorySegment bAddr) {
    int a = aAddr.get(ValueLayout.JAVA_INT, 0);
    int b = bAddr.get(ValueLayout.JAVA_INT, 0);
    return Integer.compare(a, b);
  }

  private static int[] createUnsortedArray() {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    int[] unsorted = IntStream.generate(() -> random.nextInt(1000)).limit(10).toArray();
    System.out.println("unsorted = " + Arrays.toString(unsorted));
    return unsorted;
  }
}
