package jp.gr.java_conf.nuranimation.my_bookshelf.base;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings({"unused","WeakerAccess"})
public class BundleBuilder {
    private final Bundle mBundle;

    public BundleBuilder() {
        this(null);
    }

    public BundleBuilder(final Bundle bundle) {
        mBundle = bundle == null ? new Bundle() : new Bundle(bundle);
    }

    public Bundle build() {
        return new Bundle(mBundle);
    }

    public BundleBuilder clear() {
        mBundle.clear();
        return this;
    }

    public BundleBuilder remove(final String key) {
        mBundle.remove(key);
        return this;
    }

    public BundleBuilder put(final String key, final boolean value) {
        mBundle.putBoolean(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final boolean[] value) {
        mBundle.putBooleanArray(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final Bundle value) {
        mBundle.putBundle(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final byte value) {
        mBundle.putByte(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final byte[] value) {
        mBundle.putByteArray(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final char value) {
        mBundle.putChar(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final char[] value) {
        mBundle.putCharArray(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final CharSequence value) {
        mBundle.putCharSequence(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final CharSequence[] value) {
        mBundle.putCharSequenceArray(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final double value) {
        mBundle.putDouble(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final double[] value) {
        mBundle.putDoubleArray(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final float value) {
        mBundle.putFloat(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final float[] value) {
        mBundle.putFloatArray(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final int value) {
        mBundle.putInt(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final int[] value) {
        mBundle.putIntArray(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final long value) {
        mBundle.putLong(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final long[] value) {
        mBundle.putLongArray(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final Parcelable value) {
        mBundle.putParcelable(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final Parcelable[] value) {
        mBundle.putParcelableArray(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final Serializable value) {
        mBundle.putSerializable(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final short value) {
        mBundle.putShort(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final short[] value) {
        mBundle.putShortArray(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final SparseArray<? extends Parcelable> value) {
        mBundle.putSparseParcelableArray(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final String value) {
        mBundle.putString(key, value);
        return this;
    }

    public BundleBuilder put(final String key, final String[] value) {
        mBundle.putStringArray(key, value);
        return this;
    }

    public BundleBuilder putAll(final Bundle bundle) {
        mBundle.putAll(bundle);
        return this;
    }

    public BundleBuilder putCharSequenceList(final String key, final ArrayList<CharSequence> value) {
        mBundle.putCharSequenceArrayList(key, value);
        return this;
    }

    public BundleBuilder putIntegerList(final String key, final ArrayList<Integer> value) {
        mBundle.putIntegerArrayList(key, value);
        return this;
    }

    public BundleBuilder putStringList(final String key, final ArrayList<String> value) {
        mBundle.putStringArrayList(key, value);
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return mBundle.toString();
    }

}
