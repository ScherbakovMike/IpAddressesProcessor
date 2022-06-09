import java.nio.ByteBuffer;

public class LongArray {
    private ByteBuffer[] byteArrays = {
        ByteBuffer.allocateDirect(Integer.MAX_VALUE), ByteBuffer.allocateDirect(Integer.MAX_VALUE)
    };

    byte getValue (long position) {
        int arrayNumber = (int) (position / Integer.MAX_VALUE);
        int arrayPosition = (int) (position % Integer.MAX_VALUE);
        return byteArrays[arrayNumber].get(arrayPosition);
    }

    void setValue (long position, byte value) {
        int arrayNumber = (int) (position / Integer.MAX_VALUE);
        int arrayPosition = (int) (position % Integer.MAX_VALUE);
        byteArrays[arrayNumber].put(arrayPosition, value);
    }

    long countOfUnique () {
        long sum1 = 0L;
        long sum2 = 0L;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            sum1 += byteArrays[0].get(i);
            sum2 += byteArrays[1].get(i);
        }
        return sum1 + sum2;
    }
}
