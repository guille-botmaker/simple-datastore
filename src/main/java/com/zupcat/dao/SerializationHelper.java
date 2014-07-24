package com.zupcat.dao;


import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public final class SerializationHelper {

    private static final Logger log = Logger.getLogger(SerializationHelper.class.getName());


//    public static Object fromBytes(final byte[] _b) {
//        return fromInputStream(new ByteArrayInputStream(_b));
//    }
//
//    public static Object fromInputStream(InputStream stream) {
//        ObjectInputStream inputStream = null;
//        Object result = null;
//
//        try {
//            inputStream = new ObjectInputStream(stream);
//            result = inputStream.readObject();
//            inputStream.close();
//            inputStream = null;
//
//        } catch (final Throwable e) {
//            throw new GAEException(ErrorType.PROGRAMMING, e);
//        } finally {
//            if (inputStream != null) {
//                try {
//                    inputStream.close();
//                } catch (IOException e) {
//                    log.log(Level.WARNING, "Error closing fromBytes stream: " + e.getMessage(), e);
//                }
//            }
//        }
//        return result;
//    }

    public static byte[] getCompressedBytes(final Object obj) {
        ByteArrayOutputStream byteOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        byte[] result = null;

        if (obj != null) {
            try {
                byteOutputStream = new ByteArrayOutputStream(2000);

                objectOutputStream = new ObjectOutputStream(new DeflaterOutputStream(byteOutputStream));
                objectOutputStream.writeObject(obj);
                objectOutputStream.close();

                result = byteOutputStream.toByteArray();

            } catch (final IOException ioe) {
                throw new RuntimeException("Error when compressing object [" + obj + "]: " + ioe.getMessage(), ioe);
            } finally {
                if (objectOutputStream != null) {
                    try {
                        objectOutputStream.close();
                    } catch (final IOException e) {
                        log.log(Level.WARNING, "Error closing getCompressedBytes stream: " + e.getMessage(), e);
                    }
                }
                if (byteOutputStream != null) {
                    try {
                        byteOutputStream.close();
                    } catch (final IOException e) {
                        log.log(Level.WARNING, "Error closing getCompressedBytes stream: " + e.getMessage(), e);
                    }
                }
            }
        }
        return result;
    }


    public static Object getObjectFromCompressedBytes(final byte[] compressedBytes) {
        ObjectInputStream objectIntputStream = null;
        Object result = null;

        if (compressedBytes != null) {
            try {
                objectIntputStream = new ObjectInputStream(new InflaterInputStream(new ByteArrayInputStream(compressedBytes)));
                result = objectIntputStream.readObject();

            } catch (final Exception ioe) {
                throw new RuntimeException("Error when uncompressing bytes: " + ioe.getMessage(), ioe);
            } finally {
                if (objectIntputStream != null) {
                    try {
                        objectIntputStream.close();
                    } catch (final IOException e) {
                        log.log(Level.WARNING, "Error when closing fromCompressedBytes stream: " + e.getMessage(), e);
                    }
                }
            }
        }
        return result;
    }

//    public static byte[] getBytes(final Object obj) {
//        ByteArrayOutputStream bos = null;
//        ObjectOutputStream oos = null;
//        byte[] result = null;
//
//        try {
//            bos = new ByteArrayOutputStream();
//            oos = new ObjectOutputStream(bos);
//
//            oos.writeObject(obj);
//            oos.close();
//            oos = null;
//            bos.close();
//
//            result = bos.toByteArray();
//            bos = null;
//
//        } catch (final Exception exception) {
//            throw new GAEException(ErrorType.PROGRAMMING, exception);
//        } finally {
//            if (oos != null) {
//                try {
//                    oos.close();
//                } catch (final IOException e) {
//                    log.log(Level.WARNING, "Error closing getBytes stream: " + e.getMessage(), e);
//                }
//            }
//            if (bos != null) {
//                try {
//                    bos.close();
//                } catch (final IOException e) {
//                    log.log(Level.WARNING, "Error closing getBytes stream: " + e.getMessage(), e);
//                }
//            }
//        }
//        return result;
//    }


    public static Serializable loadObjectFromFile(final File file) {
        try {
            return loadObjectFromFileImpl(file);
        } catch (final Exception _exception) {
            throw new RuntimeException("Problems loading object from file [" + file + "]: " + _exception.getMessage(), _exception);
        }
    }

    private static Serializable loadObjectFromFileImpl(final File file) throws Exception {
        ObjectInputStream input = null;
        Serializable result = null;

        try {
            input = new ObjectInputStream(new BufferedInputStream(new InflaterInputStream(new FileInputStream(file)), 1024 * 1024));

            result = (Serializable) input.readObject();

            input.close();
            input = null;
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return result;
    }


    public static void saveObject2File(final Serializable object, final File file) {
        try {
            saveObject2FileImpl(object, file);
        } catch (final Exception _exception) {
            throw new RuntimeException("Problems saving object [" + object + "] to file [" + file + "]: " + _exception.getMessage(), _exception);
        }
    }

    private static void saveObject2FileImpl(final Serializable object, final File file) throws IOException {
        ObjectOutputStream output = null;

        try {
            output = new ObjectOutputStream(new BufferedOutputStream(new DeflaterOutputStream(new FileOutputStream(file)), 1024 * 1024));

            output.writeObject(object);

            output.flush();
            output.close();
            output = null;
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }
}
