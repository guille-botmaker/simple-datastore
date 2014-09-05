package com.zupcat.util;

import java.security.SecureRandom;
import java.util.*;

/**
 * Pack of random generators
 */
public final class RandomUtils {

    private static RandomUtils instance;

    private final static char[] SAFE_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private final static char[] SAFE_NUMBERS = "0123456789".toCharArray();
    private final static char[] SAFE_ALPHANUMBERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    private static final Object INIT_LOCK_OBJECT = new Object();

    private final SecureRandom random;
    //private Boolean randomBoolean;


    public static RandomUtils getInstance() {
        if (instance == null) {
            synchronized (INIT_LOCK_OBJECT) {
                if (instance == null) {
                    instance = new RandomUtils();
                }
            }
        }
        return instance;
    }

    private RandomUtils() {
        this.random = new SecureRandom();
    }

    public String getRandomSafeString() {
        return getRandomSafeString(10);
    }

    public String getRandomSafeString(final int size) {
        return getRandomVaryingSymbols(size, SAFE_STRING);
    }

    private String getRandomVaryingSymbols(final int size, final char[] safeString) {
        final StringBuilder _buffer = new StringBuilder(size);

        while (_buffer.length() < size) {
            _buffer.append(safeString[random.nextInt(safeString.length - 1)]);
        }
        return _buffer.toString();
    }

    public int getRandomInt(final int exclusiveMax) {
        return this.getRandomInt(0, exclusiveMax);
    }

    public int getIntBetweenInclusive(final int inclusiveMin, final int inclusiveMax) {
        return getRandomInt(inclusiveMin, inclusiveMax + 1);
    }

    public int getRandomInt(final int inclusiveMin, final int exclusiveMax) {
        return inclusiveMin + random.nextInt(exclusiveMax - inclusiveMin);
    }

    public float getRandomFloat() {
        return random.nextFloat();
    }

    public long getRandomLong() {
        return random.nextLong();
    }

    public double getRandomGaussian() {
        return random.nextGaussian();
    }

    public int[] getRandomsInRange(final int minInclusiveValue, final int maxInclusiveValue, final int randomQty) {
        final Set<Integer> tempSet = new HashSet<>(randomQty);

        while (tempSet.size() < randomQty) {
            tempSet.add(getIntBetweenInclusive(minInclusiveValue, maxInclusiveValue));
        }

        final int[] result = new int[randomQty];
        final Iterator<Integer> iterator = tempSet.iterator();
        for (int i = 0; i < randomQty; i++) {
            result[i] = iterator.next();
        }
        return result;
    }

    public double getRandomDouble() {
        return random.nextDouble();
    }

    public String getRandomSafeNumberString(final int size) {
        return getRandomVaryingSymbols(size, SAFE_NUMBERS);
    }

    public String getRandomSafeAlphaNumberString(final int size) {
        return getRandomVaryingSymbols(size, SAFE_ALPHANUMBERS);
    }

    public List<?> shuffle(final List<?> choices) {
        Collections.shuffle(choices, random);
        return choices;
    }

    public boolean getRandomBoolean() {
        return getIntBetweenInclusive(0, 1) == 0;
    }
}
