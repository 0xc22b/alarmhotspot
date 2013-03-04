package com.wit.alarmhotspot.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.os.Bundle;
import android.test.AndroidTestCase;

import com.wit.alarmhotspot.AlarmHotspotService;

public class AlarmHotspotServiceTest extends AndroidTestCase {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object invokePrivateMethod(Class targetClass,
            String methodName, Class[] argClasses, Object object,
            Object[] argObjects){

        Method method;
        try {
            method = targetClass.getDeclaredMethod(methodName, argClasses);
            method.setAccessible(true);
            return method.invoke(object, argObjects);
        } catch (NoSuchMethodException e) {
            fail("Error: " + e.toString() + " at invokePrivateStaticMethod");
        } catch (IllegalAccessException e) {
            fail("Error: " + e.toString() + " at invokePrivateStaticMethod");
        } catch (IllegalArgumentException e) {
            fail("Error: " + e.toString() + " at invokePrivateStaticMethod");
        } catch (InvocationTargetException e){
            fail("Error: " + e.toString() + " at invokePrivateStaticMethod");
        }
        return null;
    }
    
    public void testGenerateBundle() {
        @SuppressWarnings("rawtypes")
        Class[] argClasses1 = {int.class};
        AlarmHotspotService alarmHotspotService = new AlarmHotspotService();
        Object[] argObjects1 = {AlarmHotspotService.FROM_WIDGET};
        Bundle bundle = (Bundle)invokePrivateMethod(AlarmHotspotService.class,
                "generateServiceBundle", argClasses1, alarmHotspotService, argObjects1);
        assertNotNull(bundle);
        assertEquals(AlarmHotspotService.FROM_WIDGET, bundle.getInt(AlarmHotspotService.FROM));
    }
    
    public void testCalculateInterval() {
        
        @SuppressWarnings("rawtypes")
        Class[] argClasses1 = {long.class, long.class, long.class, long.class,
                long.class, long.class, long.class};
        AlarmHotspotService alarmHotspotService = new AlarmHotspotService();
        Object[] argObjects1 = {120000l, 1l, 1l, 360000l, 2l, 2l, 4l};
        long interval = (Long) invokePrivateMethod(AlarmHotspotService.class,
                "calculateInterval", argClasses1, alarmHotspotService, argObjects1);
        assertEquals(510000, interval);
    }
}
