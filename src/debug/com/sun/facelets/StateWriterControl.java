package com.sun.facelets;

import java.io.Writer;
import java.lang.reflect.*;

import javax.faces.context.ResponseWriter;

/**
 * This is a hack to instantiate a thread-local object that Facelets uses to
 * write the STATE_KEY into the response when directed by JSF. The STATE_KEY is
 * written in the case when there is a form on the page. This hack is necessary
 * since we are not calling Facelets in the normal way (and hence it is not
 * completely initialized).
 */
public class StateWriterControl {
    final static String STATEWRITER_CLASS_NAME = "com.sun.facelets.StateWriter";

    static Class getStateWriter() {
        try {
            return Class.forName(STATEWRITER_CLASS_NAME);
        } catch (Exception e) {
            throw new RuntimeException("Could not load class com.sun.facelets.StateWriter using reflection", e);
        }
    }

    public static void initialize(Writer writer) {
        try {
            Class sw = getStateWriter();
            Constructor constructor = sw.getConstructor(Writer.class, int.class);
            constructor.setAccessible(true);
            constructor.newInstance(writer, 1024);
        } catch (Exception e) {
            throw new RuntimeException("Could not initilise com.sun.facelets.StateWriter using reflection", e);
        }
    }

    public static ResponseWriter createClone(ResponseWriter writer) {
        try {
            Class sw = getStateWriter();
            Method meth = sw.getMethod("getCurrentInstance");
            meth.setAccessible(true);
            Writer w = (Writer) meth.invoke(null);
            return writer.cloneWithWriter(w);
        } catch (Exception e) {
            throw new RuntimeException("Could not create clone of com.sun.facelets.StateWriter using reflection", e);
        }
    }

    public static boolean isStateWritten() {
        try {
            Class sw = getStateWriter();
            Method meth = sw.getMethod("getCurrentInstance");
            meth.setAccessible(true);
            Object o = meth.invoke(null);
            Method instMeth = sw.getMethod("isStateWritten");
            instMeth.setAccessible(true);
            return (Boolean) instMeth.invoke(o);
        } catch (Exception e) {
            throw new RuntimeException("Could not call isStateWritten on com.sun.facelets.StateWriter using reflection", e);
        }
    }

    public static String getAndResetBuffer() {
        try {
            Class sw = getStateWriter();
            Method meth = sw.getMethod("getCurrentInstance");
            meth.setAccessible(true);
            Object o = meth.invoke(null);
            Method instMeth = sw.getMethod("getAndResetBuffer");
            instMeth.setAccessible(true);
            return (String) instMeth.invoke(o);
        } catch (Exception e) {
            throw new RuntimeException("Could not call getAndResetBuffer on com.sun.facelets.StateWriter using reflection", e);
        }
    }

    public static void release() {
        try {
            Class sw = getStateWriter();
            Method meth = sw.getMethod("getCurrentInstance");
            meth.setAccessible(true);
            Object o = meth.invoke(null);
            Method instMeth = sw.getMethod("release");
            instMeth.setAccessible(true);
            instMeth.invoke(o);
        } catch (Exception e) {
            throw new RuntimeException("Could not call release on com.sun.facelets.StateWriter using reflection",e);
        }
    }

}
