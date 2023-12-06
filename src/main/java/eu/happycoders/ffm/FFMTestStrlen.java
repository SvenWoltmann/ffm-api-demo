package eu.happycoders.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public class FFMTestStrlen {
  public static void main(String[] args) throws Throwable {
    // 1. Get a linker - the central element for accessing foreign functions
    Linker linker = Linker.nativeLinker();

    // 2. Get a lookup object for commonly used libraries
    SymbolLookup stdlib = linker.defaultLookup();

    // 3. Get the address of the "strlen" function in the C standard library
    MemorySegment strlenAddress = stdlib.find("strlen").orElseThrow();

    // 4. Define the input and output parameters of the "strlen" function
    FunctionDescriptor descriptor =
        FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS);

    // 5. Get a handle to the "strlen" function
    MethodHandle strlen = linker.downcallHandle(strlenAddress, descriptor);

    // 6. Get a confined memory area (one that we can close explicitly)
    try (Arena offHeap = Arena.ofConfined()) {

      // 7. Convert the Java String to a C string and store it in off-heap memory
      MemorySegment str = offHeap.allocateFrom("Happy Coding!");

      // 8. Invoke the "strlen" function
      long len = (long) strlen.invoke(str);
      System.out.println("len = " + len);
    }
    // 9. Off-heap memory is deallocated at end of try-with-resources
  }
}
