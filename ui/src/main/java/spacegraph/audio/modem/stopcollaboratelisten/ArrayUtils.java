package spacegraph.audio.modem.stopcollaboratelisten;

/**
 * Copyright 2002 by the authors. All rights reserved.
 * <p>
 * Author: Cristina V Lopes
 */


import java.util.Arrays;

/**
 * A set of handy array manipulation utilities
 * @author CVL
 */
@Deprecated public enum ArrayUtils {
    ;

    /**
     * Create a new array of length 'length' and fill it from array 'array'
     * @param array the array from which to take the subsection
     * @param start the index from which to start the subsection copy
     * @param length the length of the returned array.
     * @return byte[] of length 'length', padded with zeros if array.length is shorter than 'start' + 'length'
     *
     * NOTE! if start + length goes beyond the end of array.length, the returned value will be padded with 0s.
     */
    @Deprecated public static byte[] subarray(byte[] array, int start, int length) {
//        return jcog.util.ArrayUtils.subarray(array, start, length);
        byte[] result = new byte[length];
        for (int i = 0; (i < length) && (i + start < array.length); i++) {
            result[i] = array[i + start];
        }
        return result;
    }

    /**
     * Converts the input matrix into a single dimensional array by transposing and concatenating the columns
     * @param input a 2D array whose columns will be concatenated
     * @return the concatenated array
     */
    public static byte[] concatenate(byte[][] input) {
        //sum the lengths of the columns
        int totalLength = Arrays.stream(input).mapToInt(value -> value.length).sum();
        //create the result array
        byte[] result = new byte[totalLength];

        //populate the result array
        int currentIndex = 0;
        for (byte[] bytes : input) {
            for (byte aByte : bytes) {
                result[currentIndex++] = aByte;
            }
        }
        return result;
    }

    /**
     * @param input1 array of bytes (possibly null)
     * @param input2 array of bytes
     * @return the concatenated array of bytes of length (input1.length + input2.length)
     */
    public static byte[] concatenate(byte[] input1, byte[] input2) {
        byte[] result;
        result = input1 != null ? new byte[input1.length + input2.length] : new byte[input2.length];

        for (int i = 0; i < result.length; i++) {
            if (input1 == null)
                result[i] = input2[i];
            else if (i < input1.length)
                result[i] = input1[i];
            else
                result[i] = input2[input2.length - (result.length - i)];
        }
        return result;
    }

}
